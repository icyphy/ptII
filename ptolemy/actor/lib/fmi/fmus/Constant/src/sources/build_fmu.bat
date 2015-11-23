rem @echo off 
rem ------------------------------------------------------------
rem $Id: build_fmu.bat 72988 2015-08-06 22:35:27Z cxh $
rem This batch builds an FMU of the FMU SDK
rem Usage: build_fmu  <fmu_dir_name> 

rem Copyright (c) 2013-2015 The Regents of the University of California.
rem All rights reserved.
rem 
rem Permission is hereby granted, without written agreement and without
rem license or royalty fees, to use, copy, modify, and distribute this
rem software and its documentation for any purpose, provided that the above
rem copyright notice and the following two paragraphs appear in all copies
rem of this software.
rem 
rem IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
rem FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
rem ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
rem THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
rem SUCH DAMAGE.
rem 
rem THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
rem INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
rem MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
rem PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
rem CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
rem ENHANCEMENTS, OR MODIFICATIONS.

rem build_fmu.bat is based on (c) 2011 QTronic GmbH

rem FMU SDK license 

rem Copyright (c) 2008-2011, QTronic GmbH. All rights reserved.
rem The FmuSdk is licensed by the copyright holder under the BSD License
rem (http://www.opensource.org/licenses/bsd-license.html):
rem Redistribution and use in source and binary forms, with or without
rem modification, are permitted provided that the following conditions are met:
rem - Redistributions of source code must retain the above copyright notice,
rem   this list of conditions and the following disclaimer.
rem - Redistributions in binary form must reproduce the above copyright notice,
rem   this list of conditions and the following disclaimer in the documentation
rem   and/or other materials provided with the distribution.

rem THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR 
rem IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
rem OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
rem IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT, 
rem INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
rem NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
rem DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
rem THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
rem (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
rem THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

rem This license is also present in org/ptolemy/fmi/driver/fmusdk-license.htm

rem ------------------------------------------------------------

echo -----------------------------------------------------------
echo building FMU %1 - FMI for Co-Simulation 1.0

rem save env variable settings
set PREV_PATH=%PATH%
if defined INCLUDE set PREV_INCLUDE=%INCLUDE%
if defined LIB     set PREV_LIB=%LIB%
if defined LIBPATH set PREV_LIBPATH=%LIBPATH%

rem setup the compiler
if defined VS250COMNTOOLS (call "%VS250COMNTOOLS%\vsvars32.bat") else ^
if defined VS240COMNTOOLS (call "%VS240COMNTOOLS%\vsvars32.bat") else ^
if defined VS230COMNTOOLS (call "%VS230COMNTOOLS%\vsvars32.bat") else ^
if defined VS220COMNTOOLS (call "%VS220COMNTOOLS%\vsvars32.bat") else ^
if defined VS210COMNTOOLS (call "%VS210COMNTOOLS%\vsvars32.bat") else ^
if defined VS200COMNTOOLS (call "%VS200COMNTOOLS%\vsvars32.bat") else ^
if defined VS190COMNTOOLS (call "%VS190COMNTOOLS%\vsvars32.bat") else ^
if defined VS180COMNTOOLS (call "%VS180COMNTOOLS%\vsvars32.bat") else ^
if defined VS170COMNTOOLS (call "%VS170COMNTOOLS%\vsvars32.bat") else ^
if defined VS160COMNTOOLS (call "%VS160COMNTOOLS%\vsvars32.bat") else ^
if defined VS150COMNTOOLS (call "%VS150COMNTOOLS%\vsvars32.bat") else ^
if defined VS140COMNTOOLS (call "%VS140COMNTOOLS%\vsvars32.bat") else ^
if defined VS130COMNTOOLS (call "%VS130COMNTOOLS%\vsvars32.bat") else ^
if defined VS120COMNTOOLS (call "%VS120COMNTOOLS%\vsvars32.bat") else ^
if defined VS110COMNTOOLS (call "%VS110COMNTOOLS%\vsvars32.bat") else ^
if defined VS110COMNTOOLS (call "%VS110COMNTOOLS%\vsvars32.bat") else ^
if defined VS100COMNTOOLS (call "%VS100COMNTOOLS%\vsvars32.bat") else ^
if defined VS90COMNTOOLS (call "%VS90COMNTOOLS%\vsvars32.bat") else ^
if defined VS80COMNTOOLS (call "%VS80COMNTOOLS%\vsvars32.bat") else ^
goto noCompiler

rem create the %1.dll in the temp dir
if not exist temp mkdir temp 
pushd temp
if exist *.dll del /Q *.dll

rem /wd4090 disables warnings about different 'const' qualifiers

rem cl /LD /wd4090 /nologo "/DFMIAPI=__declspec(dllexport)" ..\%1.c /I ..\.
cl /LD /wd4090 /nologo /DFMI_COSIMULATION ..\%1.c /I ..\.
rem cl /LD /wd4090 /nologo ..\%1.c /I ..\.
dumpbin /exports %1.dll

if not exist %1.dll goto compileError

rem copy the .dll to binaries/win32
if not exist ..\..\binaries\win32 mkdir ..\..\binaries\win32
cp %1.dll ..\..\binaries\win32

rem create FMU dir structure with root 'fmu'
set BIN_DIR=fmu\binaries\win32
set SRC_DIR=fmu\sources
set DOC_DIR=fmu\documentation
if not exist %BIN_DIR% mkdir %BIN_DIR%
if not exist %SRC_DIR% mkdir %SRC_DIR%
if not exist %DOC_DIR% mkdir %DOC_DIR%
move /Y %1.dll %BIN_DIR%
if exist ..\%1\*~ del /Q ..\%1\*~
type ..\..\modelDescription.xml > fmu\modelDescription.xml
copy ..\..\model.png fmu
copy ..\*.c %SRC_DIR%
copy ..\*.h %SRC_DIR%
copy ..\build_fmu.bat %SRC_DIR%
copy ..\build_fmu %SRC_DIR%
copy ..\makefile %SRC_DIR%
copy ..\..\documentation\*.html %DOC_DIR%
copy ..\..\documentation\*.png  %DOC_DIR%
rem del %DOC_DIR%\model.png 

rem If the 7z.exe binary is found, then
rem zip the directory tree and move to fmu directory 
for %%X in (7z.exe) do set FOUND=%%~$PATH:X
if defined FOUND (
  cd fmu
  set FMU_FILE=
  if exist ..\..\..\..\%1.fmu del ..\..\..\..\%1.fmu
  7z.exe a -tzip -xr!.svn ..\..\..\..\%1.fmu ^
  modelDescription.xml model.png binaries sources documentation
) else (
  echo Warning: Not building the .fmu file because 7z.exe is not found
  echo 7z.exe is available as part of the fmusdk or %PTII%\ptolemy\actor\lib\fmu\fmus\win32\7z.exe
)
goto cleanup


:noCompiler
echo No Microsoft Visual C compiler found
exit

:compileError
echo build of %1 failed

:cleanup
popd
if exist temp rmdir /S /Q temp

rem undo variable settings performed by vsvars32.bat
set PATH=%PREV_PATH%
if defined PREV_INCLUDE set INCLUDE=%PREV_INCLUDE%
if defined PREV_LIB     set LIB=%PREV_LIB%
if defined PREV_LIBPATH set LIBPATH=%PREV_LIBPATH%
echo done.
