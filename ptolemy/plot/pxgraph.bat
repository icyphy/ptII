@echo off
rem MSDOS batch script to start up pxgraph

rem @author: Christopher Hylands
rem @version: $Id$
rem @copyright: Copyright (c) 1997 The Regents of the University of California.
rem All rights reserved.

rem Check the TYCHO variable.
if not "%tycho%" == "" set TYCHO=c:\tycho0.2.1devel

rem Make sure that we can find the parts of Tycho we need
if exist %tycho%\java\plot\pxgraph.bat goto tychoexists
echo %tycho\java\plot\pxgraph.bat does not exist! exiting.
exit
:tychoexists

echo Starting Tycho with $TYCHO = "%tycho%"

if not "%1" == "-java" goto jdb
rem Run Java
rem cd %tycho%\java\tycho
java -classpath %classpath%;%java_home%\lib\classes.zip;%tycho%\java plot.Pxgraph
goto end

:jdb
if not "%1" == "-jdb" goto end
rem Run Java Debugger (jdb)
rem cd %tycho%\java\tycho
jdb -classpath %classpath%;%java_home%\lib\classes.zip;%tycho%\java plot.Pxgraph
goto end

:end
