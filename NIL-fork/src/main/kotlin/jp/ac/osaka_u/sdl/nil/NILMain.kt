package jp.ac.osaka_u.sdl.nil

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import jp.ac.osaka_u.sdl.nil.entity.HuntSzymanskiLCS
import jp.ac.osaka_u.sdl.nil.entity.InvertedIndex
import jp.ac.osaka_u.sdl.nil.entity.TokenSequence
import jp.ac.osaka_u.sdl.nil.presenter.logger.LoggerWrapperFactory
import jp.ac.osaka_u.sdl.nil.presenter.output.FormatFactory
import jp.ac.osaka_u.sdl.nil.usecase.cloneDetection.LCSBasedVerification
import jp.ac.osaka_u.sdl.nil.usecase.cloneDetection.NGramBasedFiltration
import jp.ac.osaka_u.sdl.nil.usecase.cloneDetection.NGramBasedLocation
import jp.ac.osaka_u.sdl.nil.usecase.cloneDetection.OptimizedCloneDetection
import jp.ac.osaka_u.sdl.nil.usecase.preprocess.PreprocessFactory
import jp.ac.osaka_u.sdl.nil.util.parallelIfSpecified
import jp.ac.osaka_u.sdl.nil.util.toTime
import java.io.File

class NILMain(private val config: NILConfig) {
    companion object {
        const val CODE_BLOCK_FILE_NAME = "code_blocks"
        const val CLONE_PAIR_FILE_NAME = "clone_pairs"
    }

    private val logger =
        LoggerWrapperFactory.create(config.isForMutationInjectionFramework, this.javaClass, config.outputFileName)

    fun run() {
        val startTime = System.currentTimeMillis()
        logger.infoStart()

        val preprocessor = PreprocessFactory.create(config)

        val queryTokenSequences = preprocessor.collectTokenSequences(config.queryFile)
        logger.infoPreprocessCompletion(queryTokenSequences.size)

        if (queryTokenSequences.isEmpty()) {
            logger.infoError("Error: no code block was extracted from the query file")
            return
        } else {
            File(CODE_BLOCK_FILE_NAME).forEachLine { line ->
                val parts = line.split(',')
                val blockStartLine = parts[1]
                val blockEndLine = parts[2]
                println("Find block in lines $blockStartLine-$blockEndLine") // TODO: redo using logger (add info function)
            }
        }

        val tokenSequences: List<TokenSequence> = preprocessor.collectTokenSequences(config.src, true)
        logger.infoPreprocessCompletion(tokenSequences.size)

        val partitionSize = (tokenSequences.size + config.partitionNum - 1) / config.partitionNum
        val filtrationPhase = NGramBasedFiltration(config.filtrationThreshold)
        val filtrationBasedVerificationPhase = NGramBasedFiltration(config.verificationThreshold)
        val verificationPhase = LCSBasedVerification(HuntSzymanskiLCS(), config.verificationThreshold)

        File(CLONE_PAIR_FILE_NAME).bufferedWriter().use { bw ->
            repeat(config.partitionNum) { i ->
                val startIndex: Int = i * partitionSize

                val invertedIndex =
                    InvertedIndex.create(
                        partitionSize,
                        config.gramSize,
                        tokenSequences,
                        queryTokenSequences,
                        startIndex
                    )
                logger.infoInvertedIndexCreationCompletion(i + 1)

                val locationPhase = NGramBasedLocation(invertedIndex)
                val cloneDetection =
                    OptimizedCloneDetection(
                        locationPhase,
                        filtrationPhase,
                        filtrationBasedVerificationPhase,
                        verificationPhase,
                        tokenSequences,
                        queryTokenSequences,
                        config.gramSize
                    )
                Flowable.range(0, queryTokenSequences.size)
                    .parallelIfSpecified(config.threads)
                    .runOn(Schedulers.computation())
                    .flatMap { cloneDetection.exec(it) }
                    .sequential()
                    .blockingSubscribe { bw.appendLine("${it.first},${it.second}") }
                logger.infoCloneDetectionCompletion(i + 1)
            }
        }
        val endTime = System.currentTimeMillis()
        logger.infoEnd((endTime - startTime).toTime())

        FormatFactory.create(config.isForBigCloneEval)
            .convert(config.outputFileName)
    }
}

fun main(args: Array<String>) {
    val config: NILConfig = parseArgs(args)
    NILMain(config).run()
}
