#!/bin/bash

if [ "$#" -lt 4 ]; then
    echo "Usage: $0 <dataset_path> <result_folder> <language> <worker_id> [flags]"
    exit 1
fi

DATASET_PATH=$(realpath "$1")
RESULT_FOLDER=$(realpath "$2")
LANGUAGE="$3"
WORKER_ID="$4"
shift 4

BCB_MODE="--no-bcb"
BETA="0.5"
THETA="0.35"
ETA="0.6"

while [[ "$#" -gt 0 ]]; do
    case "$1" in
        bcb_mode)
            BCB_MODE="$2"; shift 2;;
        beta)
            BETA="$2"; shift 2;;
        theta)
            THETA="$2"; shift 2;;
        eta)
            ETA="$2"; shift 2;;
        *) shift;;
    esac
done

IMAGE_NAME="sptokener-runner-$WORKER_ID"
CONTAINER_NAME="sptokener-container-$WORKER_ID"

OUTPUT_DIR="$RESULT_FOLDER/SPtokener"
mkdir -p "$OUTPUT_DIR"

docker run -d -it --quiet \
  --platform darwin/amd64 \
  -e BCB_MODE="$BCB_MODE" \
  -e BETA="$BETA" \
  -e THETA="$THETA" \
  -e ETA="$ETA" \
  -v "$DATASET_PATH:/data/input:ro" \
  --name "$CONTAINER_NAME" \
  "$IMAGE_NAME"

docker wait "$CONTAINER_NAME"  >/dev/null
docker logs "$CONTAINER_NAME"

if docker cp "$CONTAINER_NAME":/app/SPtokener/java/clonepairs.txt "$OUTPUT_DIR"/ 2>/dev/null; then
  echo "Results copied to: $OUTPUT_DIR"
else
  echo "Error: No clonepairs.txt file to copy"
fi

docker rm -f "$CONTAINER_NAME" >/dev/null

echo "Results saved to: $OUTPUT_DIR"