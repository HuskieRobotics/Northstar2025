#!/bin/bash

export GENICAM_CACHE_V3_1=/tmp/tmp2;
cd ~/Documents/GitHub/Northstar2025/northstar;
source ./venv/bin/activate;
while [ True ];
   do /Users/nnrobot/Documents/GitHub/Northstar2025/northstar/reenumerate/reenumerate -v -l 0x03200000
   nice -20 python3 __init__.py --config cameras/robots/practice/configBL.json --calibration calibration40708556.yml;
   sleep 1;
done
