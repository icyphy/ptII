# Script that starts up the demo
OUTPUT=${PTII}/ptolemy/actor/lib/x10/demo/MotionDrivenDemos/demo.txt
echo "The output will be in"
echo "   $OUTPUT"
echo "--------------------" >> $OUTPUT
date >> $OUTPUT
java -Dptolemy.ptII.dir=${PTII} \
    -Xmx192M \
    -classpath "${PTII};${PTII}/lib/diva.jar;${PTII}/vendors/sun/commapi/comm.jar;${PTII}/vendors/misc/x10/tjx10p-11/lib/x10.jar" \
    ptolemy.actor.gui.PtExecuteApplication \
    -full ${PTII}/ptolemy/actor/lib/x10/demo/MotionDrivenDemos/MotionDrivenDemosRandom.xml \
 >> $OUTPUT 2>&1
