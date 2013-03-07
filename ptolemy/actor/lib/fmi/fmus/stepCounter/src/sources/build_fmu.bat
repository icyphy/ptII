@echo off 
rem ------------------------------------------------------------
rem This batch builds an FMU of the FMU SDK
rem Usage: build_fmu (me|cs) <fmu_dir_name> 
rem (c) 2011 QTronic GmbH
rem ------------------------------------------------------------

echo -----------------------------------------------------------
if %1==cs (^
echo building FMU %2 - FMI for Co-Simulation 1.0) else ^
echo building FMU %2 - FMI for Model Exchange 1.0

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

rem create the %2.dll in the temp dir
if not exist temp mkdir temp 
pushd temp
if exist *.dll del /Q *.dll

rem /wd4090 disables warnings about different 'const' qualifiers
if %1==cs (set FMI_DIR=co_simulation) else set FMI_DIR=model_exchange
if %1==cs (set DEF=/DFMI_COSIMULATION) else set DEF=
cl /LD /wd4090 /nologo %DEF% ..\%2\%2.c /I ..\. /I ..\..\%FMI_DIR%\include
if not exist %2.dll goto compileError

rem create FMU dir structure with root 'fmu'
set BIN_DIR=fmu\binaries\win32
set SRC_DIR=fmu\sources
set DOC_DIR=fmu\documentation
if not exist %BIN_DIR% mkdir %BIN_DIR%
if not exist %SRC_DIR% mkdir %SRC_DIR%
if not exist %DOC_DIR% mkdir %DOC_DIR%
move /Y %2.dll %BIN_DIR%
if exist ..\%2\*~ del /Q ..\%2\*~
copy ..\%2\%2.c %SRC_DIR% 
type ..\%2\modelDescription.xml ..\%1.xml > fmu\modelDescription.xml
copy ..\%2\model.png fmu
copy ..\fmuTemplate.c %SRC_DIR%
copy ..\fmuTemplate.h %SRC_DIR%
copy ..\%2\*.html %DOC_DIR%
copy ..\%2\*.png  %DOC_DIR%
del %DOC_DIR%\model.png 

rem zip the directory tree and move to fmu directory 
cd fmu
set FMU_FILE=..\..\..\..\fmu\%1\%2.fmu
if exist %ZIP_FILE% del %FMU_FILE%
..\..\..\..\bin\7z.exe a -tzip -xr!.svn %FMU_FILE% ^
  modelDescription.xml model.png binaries sources documentation
goto cleanup

:noCompiler
echo No Microsoft Visual C compiler found
exit

:compileError
echo build of %2 failed

:cleanup
popd
if exist temp rmdir /S /Q temp

rem undo variable settings performed by vsvars32.bat
set PATH=%PREV_PATH%
if defined PREV_INCLUDE set INCLUDE=%PREV_INCLUDE%
if defined PREV_LIB     set LIB=%PREV_LIB%
if defined PREV_LIBPATH set LIBPATH=%PREV_LIBPATH%
echo done.



