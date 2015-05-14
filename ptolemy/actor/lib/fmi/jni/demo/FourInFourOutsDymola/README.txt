ptolemy/actor/lib/fmi/jni/demo/FourInFourOutsDymola/README.txt

This is a performance test case that uses FMUs from Dymola.

It runs under Linux64 (Ubuntu) or Windows.

Note that it will not run under RHEL because of GLIBC problems.

To run with the default FMUImport class that uses JNA, use:

cd $PTII/ptolemy/actor/lib/fmi/jni/demo/FourInFourOutsDymola/
$PTII/bin/ptinvoke ptolemy.actor.gui.MoMLSimpleStatisticalApplication FourInFourOutsDymola.xml

To run with JNI:
cd $PTII/ptolemy/actor/lib/fmi/jni/demo/FourInFourOutsDymola/
$PTII/bin/ptinvoke ptolemy.actor.gui.MoMLSimpleStatisticalApplication FourInFourOutsDymolaJNI.xml


