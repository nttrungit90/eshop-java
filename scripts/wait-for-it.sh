#!/bin/bash
# wait-for-it.sh - Wait for a service to be available
# Usage: ./wait-for-it.sh host:port [-t timeout] [-- command args]

TIMEOUT=30
HOST=""
PORT=""
QUIET=0
CMD=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        *:* )
            HOST="${1%%:*}"
            PORT="${1##*:}"
            shift
            ;;
        -t)
            TIMEOUT="$2"
            shift 2
            ;;
        -q)
            QUIET=1
            shift
            ;;
        --)
            shift
            CMD="$@"
            break
            ;;
        *)
            echo "Unknown argument: $1"
            exit 1
            ;;
    esac
done

if [[ -z "$HOST" || -z "$PORT" ]]; then
    echo "Usage: $0 host:port [-t timeout] [-- command args]"
    exit 1
fi

START_TIME=$(date +%s)
while ! nc -z "$HOST" "$PORT" >/dev/null 2>&1; do
    ELAPSED=$(($(date +%s) - START_TIME))
    if [[ $ELAPSED -ge $TIMEOUT ]]; then
        echo "Timeout waiting for $HOST:$PORT"
        exit 1
    fi
    if [[ $QUIET -eq 0 ]]; then
        echo "Waiting for $HOST:$PORT... ($ELAPSED/$TIMEOUT)"
    fi
    sleep 1
done

if [[ $QUIET -eq 0 ]]; then
    echo "$HOST:$PORT is available"
fi

if [[ -n "$CMD" ]]; then
    exec $CMD
fi
