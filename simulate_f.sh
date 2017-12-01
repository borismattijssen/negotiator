#!/bin/bash

if [ -z "$1" ]
then
    echo "Please set param f like so: simulate.sh <param-f>"
    exit 1
fi

LOG_FOLDER="logs"
if [ ! -z "$2" ]
then
    LOG_FOLDER=$2
fi

echo "SUMULATION FOR F=$1 started."
UTILS_LOG_FOLDER="$LOG_FOLDER/f_$1"
mkdir "$UTILS_LOG_FOLDER" || echo "Folder already exists"
echo "$LOG_FOLDER/f_$1" | java -Dlogfolder="$UTILS_LOG_FOLDER" \
                                -Dparamf="$1" \
                                -XX:+UnlockExperimentalVMOptions \
                                -XX:+UseCGroupMemoryLimitForHeap \
                                -cp negosimulator.jar negotiator.xml.multipartyrunner.Runner cli_run.xml
