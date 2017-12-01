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
    UTILS_LOG_FOLDER="$LOG_FOLDER/e_${VALUES[count]}"
    mkdir "$UTILS_LOG_FOLDER" || echo "Folder already exists"
#    echo "$LOG_FOLDER/e_${VALUES[count]}" | java -Dlogfolder="$UTILS_LOG_FOLDER" \
#                                                 -Dparame="${VALUES[count]}" \
#                                                 -XX:+UnlockExperimentalVMOptions \
#                                                 -XX:+UseCGroupMemoryLimitForHeap \
#                                                 -cp negosimulator.jar negotiator.xml.multipartyrunner.Runner cli_run.xml
    echo "$LOG_FOLDER/e_${VALUES[count]}" | java -Dlogfolder="$UTILS_LOG_FOLDER" \
                                                 -Dparame="${VALUES[count]}" \
                                                 -cp negosimulator.jar negotiator.xml.multipartyrunner.Runner cli_run.xml
    count=$(( $count + 1 ))
done