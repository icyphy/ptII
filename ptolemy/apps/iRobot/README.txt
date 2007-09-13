This directory contains a Ptolemy II development environment
for the iRobot Create, a programmable robot that is the
underlying platform for the Roomba vacuum cleaner.
The iRobot Create has a Command Module with an 8 bit
microcontroller. The examples directory provides some
sample C programs provided by the manufacturer. The
ptolemy directory provides some examples of Ptolemy II
models that can be code generated to produce programs
that run on the Command Module.

To install the software to use the iRobot, follow the
instructions here:

  http://www.irobot.com/filelibrary/create/CommandModuleGettingStarted.pdf

To use the Ptolemy models, open one of the .xml files
in a subdirectory of the ptolemy directory, and double
click on the code generator to generate code. Then

Overall information about the iRobot Create can be
found here:

  http://www.irobot.com/sp.cfm?pageid=305

Reference documentation on that website includes:
 - ATmega168Reference.pdf: The microcontroller
 - CreateManual_Final.pdf: The iRobot itself
 - CommandModuleManual_v2.pdf: The command module
 - CommandModuleGettingStarted.pdf: The quick start.
 - CreateOpenInterface_v2.pdf: The interface between
    the command module and the robot.
