# Code Search Toolkit

REST service for targeted search of code fragments in GitHub repositories based on duplicate detection methods:
- CCSTokener
- NIL-fork
- CCAligner
- CCSTokener-fork
- CCAligner-fork
- NiCad
- SourcererCC
- SPtokener

## Description

The service provides API for:
- Creating search tasks for GitHub repositories or local ZIP archives
- Getting task status (including pending, processing, success, error, deleted states)
- Retrieving search results
- Getting information about available methods and their parameters

Swagger documentation is available at `/docs`

## Running

### Locally

1. Install dependencies:
```
pip install -r requirements.txt
```
2. Build containers for the methods you want to use (see below).
3. Start the server:
```
python3 -m app.main [--host <адрес>] [--port <номер>] [--retry_k <минуты>] [--retry_multiplier <множитель>] [--max_retries <число>] [--max_parallel_methods <число>] [--num_workers <число>] [--save_logs]
```
- `--host` — host address (default 0.0.0.0)
- `--port` — port number (default 1234)
- `--retry_k` — base delay (in minutes) before first retry attempt (default 5)
- `--retry_multiplier` — multiplier c for exponential backoff (default 2)
- `--max_retries` — maximum number of retry attempts (default 5)
- `--max_parallel_methods` - number of methods (Docker containers) to run simultaneously
- `--num_workers` - number of worker processes to start for task processing
- `--save_logs` - indicates whether to save method execution logs for each task

Example launch with custom values:

```
python3 -m app.main --host 0.0.0.0 --port 1234 --retry_k 5 --retry_multiplier 3 --max_retries 7
```
