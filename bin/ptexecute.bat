@echo off
rem MSDOS batch script to start a model
rem This takes over what CompositeActorApplication used to do (with the
rem addition that you can give it either class files or MoML files).
rem That is, it instantiates models, and all the models that are
rem instances of CompositeActor, it runs.

rem @author Edward A. Lee
rem @version $Id$
rem @copyright: Copyright (c) 1997-2003
rem The Regents of the University of California.

java -classpath %PTII% ptolemy.actor.gui.PtExecuteApplication %1 %2 %3 %4 %5 %6 %7 %8 %9

