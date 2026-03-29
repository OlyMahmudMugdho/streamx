#!/bin/bash

set -e

VIDEO_PATH="/home/mugdho/codes/experiments/streamx/video.mp4"
UPLOAD_URL="${UPLOAD_URL:-http://localhost:8080/blob/upload}"

if [ ! -f "$VIDEO_PATH" ]; then
    echo "Error: Video file not found at $VIDEO_PATH"
    exit 1
fi

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$UPLOAD_URL" \
    -F "file=@$VIDEO_PATH;type=video/mp4")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    echo "$BODY"
else
    echo "Upload failed with status code: $HTTP_CODE"
    echo "Response: $BODY"
    exit 1
fi