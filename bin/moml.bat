@echo off
rem MSDOS batch script to start ptolemy

rem @author Edward A. Lee
rem @version $Id$
rem @copyright: Copyright (c) 1997-2003
rem The Regents of the University of California.

rem Moml application
rem This has no default configuration, run moml when you want
rem to run your own graphical editor, for example:
rem moml.bat myBiggerBetterVergilConfiguration.xml foo.xml

java -classpath %PTII% ptolemy.actor.gui.MoMLApplication %1 %2 %3 %4 %5 %6 %7 %8 %9

