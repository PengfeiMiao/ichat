#!/bin/bash

nohup java -jar ./libs/wechat-bc-1.1.9-SNAPSHOT.jar > output.log 2>&1 &
tail -f output.log