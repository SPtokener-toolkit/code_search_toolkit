package jp.ac.osaka_u.sdl.nil

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.writeText

object QueryPreprocessor {
    fun prepareQueryFile(file: File, lang: Language): File {
        return when (lang) {
            Language.JAVA -> wrapJavaFileIfNeeded(file)
            Language.CPP -> wrapCppFileIfNeeded(file)
            Language.PYTHON -> wrapPythonFileIfNeeded(file)
            else -> file
        }
    }

    private fun wrapJavaFileIfNeeded(file: File): File {
        val content = file.readText()

        val hasMethods = Regex("\\b(public|private|protected)?\\s+(static)?\\s+[\\w<>]+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{")
            .containsMatchIn(content)

        if (content.contains("class ") && content.contains("{")) {
            if (hasMethods) return file
        }

        val wrappedContent = when {
            content.contains("class ") -> content
            hasMethods -> "class GeneratedClass {\n$content\n}"
            else -> "class GeneratedClass {\npublic static void generatedMethod() {\n$content\n}\n}"
        }

        println("WRAPPED CONTENT:\n\n\n$wrappedContent\n\n\n") // TODO: redo to use logger

        val customDir = Files.createDirectories(Paths.get("temp_files_java"));
        val tempFile = kotlin.io.path.createTempFile(directory = customDir, "query", ".java")
        tempFile.writeText(wrappedContent)
        tempFile.toFile().deleteOnExit()

        return tempFile.toFile()
    }

    private fun wrapCppFileIfNeeded(file: File): File {
        val content = file.readText()

        val hasMethods =
            Regex("(\\b(?:static|public|private|protected|inline|virtual|const\\b\\s*)*(?!if|while|for|switch)(?:[^\\s;{]+(?:\\s*::\\s*\\w+)?\\s+)+(?:\\w+::)?~?\\w+\\s*\\(((?:[^()]|\\((?:[^()]|\\([^()]*\\))*\\))*)\\)\\s*(?:const|noexcept|override|final|throw\\(\\))*\\s*\\{)")
                .containsMatchIn(content)

        if (hasMethods) {
            return file
        }

        val wrappedContent = "static void generatedFunction() {\n$content\n}"

        println("WRAPPED CONTENT:\n\n\n$wrappedContent\n\n\n") // TODO: redo to use logger

        val customDir = Files.createDirectories(Paths.get("temp_files_cpp"))
        val tempFile = kotlin.io.path.createTempFile(directory = customDir, "query", ".cpp")
        tempFile.writeText(wrappedContent)
        tempFile.toFile().deleteOnExit()

        return tempFile.toFile()
    }

    private fun wrapPythonFileIfNeeded(file: File): File {
        val content = file.readText()

        val hasTopLevelFunctions = Regex("^\\s*def\\s+\\w+\\s*\\(", RegexOption.MULTILINE)
            .containsMatchIn(content)

        if (hasTopLevelFunctions) {
            return file
        }

        val indentedContent = content.lineSequence()
            .joinToString("\n") { line -> "    $line" }
        val wrappedContent = "def generated_function():\n$indentedContent"

        println("WRAPPED CONTENT:\n\n\n$wrappedContent\n\n\n") // TODO: redo to use logger

        val customDir = Files.createDirectories(Paths.get("temp_files_python"))
        val tempFile = kotlin.io.path.createTempFile(directory = customDir, "query", ".py")
        tempFile.writeText(wrappedContent)
        tempFile.toFile().deleteOnExit()

        return tempFile.toFile()
    }
}