$Id$
This demos use the iRobot and WinAVR.

Note that if you use Programmer's Notepad, then you will
need to generate code and then copy the resulting makefile and
overwrite the current makefile.

For example, in Sensors/, you would generate code, which will create
Sensors.mk and then, while in the Sensors directory, copy Sensors.mk
to makefile.

If you don't, then the Tools -> "[WinAVR] make all" in Programmer's
Notepad will be running the "all" command in the Ptolemy makefile, not
in the generated makefile.


