# Script that starts up the demo
echo "The output will be in demo.txt"
echo "--------------------" >> demo.txt
date >> demo.txt
java -Dptolemy.ptII.dir=${PTII} \
    -classpath "${PTII};${PTII}/lib/diva.jar;${PTII}/vendors/sun/commapi/comm.jar;${PTII}/vendors/misc/x10/tjx10p-11/lib/x10.jar" \
    ptolemy.actor.gui.PtExecuteApplication \
    -full ${PTII}/ptolemy/actor/lib/x10/demo/MotionDrivenDemos/MotionDrivenDemosRandom.xml \
 >> demo.txt 2>&1
