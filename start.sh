#!/bin/bash

pid=$(jps | grep "wechat-bc" | awk '{print $1}')

if [ -n "$pid" ]; then
    echo "Killing process with PID: $pid"
    kill -9 "$pid"
else
    echo "Process not found."
fi

nohup java -jar ./libs/wechat-bc-1.1.9-SNAPSHOT.jar > output.log 2>&1 &
tail -f output.log