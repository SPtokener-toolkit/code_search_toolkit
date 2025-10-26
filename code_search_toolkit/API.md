# API Documentation v1

## 1. Create Search Task

**Endpoint**  
`POST /api/search`

**Content-Type**  
`multipart/form-data`

---

### GitHub Mode

#### Required Parameters

| Field          | Type     | Description                                                                         |
|---------------|---------|----------------------------------------------------------------------------------|
| `mode`        | string  | Must be "github"                                                           |
| `repository`  | string  | GitHub repository URL                                                           |
| `branch`      | string  | Branch name (default "main")                                           |
| `snippet`     | string  | Source code (fragment) to search for                                               |
| `methods`     | string  | JSON array of methods                                                              |
| `combination` | string  | JSON configuration for results combination strategy                              |
| `language`    | string  | Source code language (e.g. `"java"`, `"python"`, `"cpp"`, `"c"`, `"csharp"`) |

#### combination: example

1. Simple union

Combines all clone pairs from all methods without any filters:

```json
{ "strategy": "union" }
```

2. threshold_union

Includes only pairs that were found in at least N different methods:

```json
{
  "strategy": "threshold_union",
  "min_methods": 2
}
```

3. weighted_union

Each pair is assigned the sum of weights from methods where it was found. We keep pairs whose total weight $\geq$ threshold.

```json
{
  "strategy": "weighted_union",
  "weights": {
    "NIL-fork": 0.6,
    "CCAligner": 0.3,
    "CCSTokener": 0.1
  },
  "threshold": 0.5
}
```

threshold - threshold value (0.0 ... 1.0).

4. intersection_union

By default (if strategy is not specified), the intersection of results from all specified methods is taken:

```json
{ "strategy": "intersection_union" }
```

#### Example Request

```bash
curl -X POST "http://api.example.com/api/search" \
  -H "Content-Type: multipart/form-data" \
  -F "mode=github" \
  -F "repository=https://github.com/user/repo" \
  -F "branch=main" \
  -F "snippet=def calculate(a, b):\n    return a + b" \
  -F 'methods=[{"name":"NIL-fork","params":{"threshold":0.75}}]' \
  -F 'combination={"strategy":"threshold_union","min_methods":2}' \
  -F "language=python"
```

---

### Local Mode

#### Required Parameters

| Field        | Type   | Description                   |
|--------------|--------|-------------------------------|
| `mode`       | string | Must be "local"               |
| `file`       | file   | Project ZIP archive           |
| `snippet`    | string | Source code to search for     |
| `methods`    | string | JSON array of methods         |
| `combination`| string | JSON combination config       |
| `language`   | string | Source code language          |

#### Example Request
```bash
curl -X POST "http://api.example.com/api/search" \
  -H "Content-Type: multipart/form-data" \
  -F "mode=local" \
  -F "snippet=def calculate(a, b):\n    return a + b" \
  -F 'methods=[{"name":"NIL-fork","params":{"threshold":0.75}}]' \
  -F 'combination={"strategy":"union"}' \
  -F "language=python" \
  -F "file=@path/to/project.zip"
```

---

**Response for Both Modes**
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "status_url": "/api/search/550e8400-e29b-41d4-a716-446655440000/status",
  "results_url": "/api/search/550e8400-e29b-41d4-a716-446655440000/results"
}
```

---

## 2. Get Task Status

**Endpoint**  
`GET /api/search/{task_id}/status`

**Example Response**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "pending|processing|completed|error|deleted",
  "started_at": "2023-12-20T10:00:00Z",
  "processed_snippet": "def calculate(a, b):\n    return a + b"
}
```

### Status

- pending - waiting to start processing
- processing - currently being processed
- completed - successfully completed, results are ready
- error - processing ended with an error. Additional fields may be present in this case
- deleted - removed from queue due to expired storage time

---

## 3. Get Results

**Endpoint**  
`GET /api/search/{task_id}/results`

**Example Response**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "results": [
    ...
  ],
  "metrics": {
    "total_files_processed": 142,
    "execution_time": 12.7
  }
}
```

If the task status is not completed, returns HTTP 400 Bad Request with a message that the task is not ready yet

---

## 4. Get Method Information

**Endpoint**  
`GET /api/methods`

**Example Response**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "available_methods": [
    {
      "name": "NIL-fork",
      "description": "Large-variance clone detection",
      "params": {
        "threshold": {
          "type": "float",
          "default": 0.7,
          "min": 0.1,
          "max": 1.0
        }
      }
    },
    {
      "name": "CCAligner",
      "description": "Token-based clone detection",
      "params": {
        "min_tokens": {
          "type": "integer",
          "default": 50,
          "min": 10,
          "max": 1000
        }
      }
    }
  ]
}
```

--- 

## Notes

1. Complex parameters (`methods`, `combination`) must:
  - Be serialized as JSON
  - Match the schema from `/api/methods`

2. Processing time depends on:
  - Repository size (GitHub)
  - Archive contents (Local)
  - Selected methods

3. Storage time and deletion
  - After successful completion, the task and its results are stored for 24 hours.
  - Once the storage period expires, the status changes to deleted, and all files (extracted source code and results) are deleted from the file system
