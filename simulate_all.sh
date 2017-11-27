#!/bin/bash

LOG_FOLDER="logs"
if [ ! -z "$1" ]
then
    LOG_FOLDER=$1
fi

VALUES=(0.5 1 2 4 6 8 10 12 14)
count=0
while [ "x${VALUES[count]}" != "x" ]
do
    echo "SUMULATION FOR E=${VALUES[count]} started."
    PARAM_E=${VALUES[count]}
    echo $PARAM_E
    echo "$LOG_FOLDER/e_${VALUES[count]}" | java -cp negosimulator.jar negotiator.xml.multipartyrunner.Runner cli_run.xml
    count=$(( $count + 1 ))
done