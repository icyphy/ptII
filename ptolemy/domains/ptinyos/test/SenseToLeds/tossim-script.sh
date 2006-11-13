#!/bin/bash

# This script will run the equivalent TOSSIM tests (compare to Viptos
# tests in viptos-command.txt)

#################### Begin 1 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-1.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 1 &> /dev/null
    
    let COUNTER=COUNTER+1 
done

#################### Begin 10 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-10.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 10 &> /dev/null
    
    let COUNTER=COUNTER+1 
done

#################### Begin 25 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-25.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 25 &> /dev/null
    
    let COUNTER=COUNTER+1 
done

#################### Begin 50 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-50.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 50 &> /dev/null
    
    let COUNTER=COUNTER+1 
done

#################### Begin 100 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-100.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 100 &> /dev/null
    
    let COUNTER=COUNTER+1 
done

#################### Begin 250 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-250.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 250 &> /dev/null
    
    let COUNTER=COUNTER+1 
done

#################### Begin 500 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-500.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 500 &> /dev/null
    
    let COUNTER=COUNTER+1 
done

#################### Begin 1000 nodes #####################
COUNTER=0
while [  $COUNTER -lt 10 ]; do
    echo The counter is $COUNTER
    
    /usr/bin/time -a -f "%e real,%U user,%S sys" -o tossim-log-1000.txt  $TOSROOT/apps/SenseToLeds/build/pc/main.exe -b=0 -t=300 1000 &> /dev/null
    
    let COUNTER=COUNTER+1 
done
