#!/bin/bash

if [ -z "$1" ]
then
    echo "Please set param e like so: simulate.sh <param-e>"
    exit 1
fi

LOG_FOLDER="logs"
if [ ! -z "$2" ]
then
    LOG_FOLDER=$2
fi

echo "SUMULATION FOR E=$1 started."
PARAM_E=$2
echo $PARAM_E
echo "$LOG_FOLDER/e_$1" | java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -cp negosimulator.jar negotiator.xml.multipartyrunner.Runner cli_run.xml
