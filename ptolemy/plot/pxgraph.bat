@echo off
rem MSDOS batch script to start up pxgraph

rem @author Christopher Hylands
rem @version $Id$
rem @copyright: Copyright (c) 1997-1998 The Regents of the University of California.
rem All rights reserved.

rem Check the TYCHO variable.
if not "%tycho%" == "" set TYCHO=c:\tycho0.2.1

rem Make sure that we can find the parts of Tycho we need
if exist %tycho%\java\pt\plot\pxgraph.bat goto tychoexists
echo %tycho\java\pt\plot\pxgraph.bat does not exist! exiting.
exit
:tychoexists

rem echo Starting Plotter with $TYCHO = "%tycho%"

java -classpath %classpath%;%java_home%\lib\classes.zip;%tycho%\java pt.plot.Pxgraph %1 %2 %3 %4 %5 %6 %7 %8 %9

