@echo off 
rem ------------------------------------------------------------
rem To run a simulation, start this batch in this directory. 
rem Example: fmusim me fmu/me/dq.fmu 0.3 0.1 1 c
rem To build simulators bin\*.exe and FMUs, run src\build_all.bat
rem ------------------------------------------------------------

set FMUSDK_HOME=.
if %1==me (bin\fmusim_me.exe %2 %3 %4 %5 %6) else bin\fmusim_cs.exe %2 %3 %4 %5 %6
