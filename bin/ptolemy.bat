@echo off
rem MSDOS batch script to start ptolemy

rem @author Edward A. Lee
rem @version $Id$
rem @copyright: Copyright (c) 1997-2002
rem The Regents of the University of California.

java -classpath %PTII% ptolemy.actor.gui.PtolemyApplication %1 %2 %3 %4 %5 %6 %7 %8 %9

