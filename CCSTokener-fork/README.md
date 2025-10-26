# CCStokener

Implementation of the CCStokener algorithm ([article](https://www.sciencedirect.com/science/article/abs/pii/S0164121223000134)), adapted for code search tasks

## Usage

### 1. Token Extraction
```bash
python3 extract_tokens.py \
  --input_dir <path> \
  --output_dir <path>
```

Parameters:
- input_dir: Directory or file with source code
- output_dir: Directory for saving tokens

### 2. Code Search

```bash
python3 code_clone_detection.py \
  --input_tokens_dir <path> \
  --query_tokens_dir <path> \
  --query_file <absolute_path_to_query.java> \
  --beta <float> \
  --theta <float> \
  --eta <float> \
  [--bcb_flag] \
  [--report_dir <path>]
```

Parameters:
- input_tokens_dir: Directory with tokens (output from extract_tokens.py)
- query_tokens_dir: Directory with query_file tokens
- query_file: Absolute path to .java file to find clones for
- beta: Threshold value for action-token overlap
- theta: Threshold value for token count ratio
- eta: Threshold value for semantic token similarity
- bcb_flag (optional): Use BCB format in report
- report_dir (optional): Directory for report and results

### 3. Running Full Pipeline

```bash
./ccstokener_runner.sh <input_dir> <beta> <theta> <eta> --query_file <path_to_query.java> [--bcb_flag] [<report_dir>]
```

Example:
```bash
./ccstokener_runner.sh ./dataset/java_samples 0.5 0.3 0.8 --query_file ./dataset/java_samples/example/Foo.java
./ccstokener_runner.sh ./dataset/java_samples 0.6 0.4 0.7 --query_file ./dataset/java_samples/example/Foo.java --bcb_flag my_custom_report
```
