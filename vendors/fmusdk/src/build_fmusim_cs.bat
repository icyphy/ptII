@echo off 
rem ------------------------------------------------------------
rem This batch builds the FMU simulator fmusim_cs.exe
rem (c) 2011 QTronic GmbH
rem ------------------------------------------------------------

echo -----------------------------------------------------------
echo building fmusim_cs.exe - FMI for Co-Simulation 1.0
echo -----------------------------------------------------------

rem save env variable settings
set PREV_PATH=%PATH%
if defined INCLUDE set PREV_INCLUDE=%INCLUDE%
if defined LIB     set PREV_LIB=%LIB%
if defined LIBPATH set PREV_LIBPATH=%LIBPATH%

rem setup the compiler
if defined VS110COMNTOOLS (call "%VS110COMNTOOLS%\vsvars32.bat") else ^
if defined VS100COMNTOOLS (call "%VS100COMNTOOLS%\vsvars32.bat") else ^
if defined VS90COMNTOOLS (call "%VS90COMNTOOLS%\vsvars32.bat") else ^
if defined VS80COMNTOOLS (call "%VS80COMNTOOLS%\vsvars32.bat") else ^
goto noCompiler

set SRC=fmusim_cs\main.c ..\shared\xml_parser.c ..\shared\stack.c ..\shared\sim_support.c
set INC=/Iinclude /I../shared /Ifmusim_cs
set OPTIONS=/DFMI_COSIMULATION /wd4090 /nologo

rem create fmusim_cs.exe in the fmusim_cs dir
rem /wd4090 to disable warnings about different 'const' qualifiers
pushd co_simulation
cl %SRC% %INC% %OPTIONS% /Fefmusim_cs.exe /link ..\shared\libexpatMT.lib  
del *.obj
popd
if not exist co_simulation\fmusim_cs.exe goto compileError
move /Y co_simulation\fmusim_cs.exe ..\bin
goto done

:noCompiler
echo No Microsoft Visual C compiler found

:compileError
echo build of fmusim_cs.exe failed

:done
rem undo variable settings performed by vsvars32.bat
set PATH=%PREV_PATH%
if defined PREV_INCLUDE set INCLUDE=%PREV_INCLUDE%
if defined PREV_LIB     set LIB=%PREV_LIB%
if defined PREV_LIBPATH set LIBPATH=%PREV_LIBPATH%
echo done.
