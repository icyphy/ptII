# Script that starts up the demo
OUTPUT=${PTII}/ptolemy/demo/ExecDemos/demo.txt
echo "The output will be in"
echo "   $OUTPUT"
echo "--------------------" >> $OUTPUT
date >> $OUTPUT
java -Dptolemy.ptII.dir=${PTII} \
    -Xmx192M \
    -classpath "${PTII};${PTII}/lib/diva.jar" \
    ptolemy.actor.gui.PtExecuteApplication \
    -full ${PTII}/ptolemy/demo/ExecDemos/ExecDemos.xml \
 >> $OUTPUT 2>&1
