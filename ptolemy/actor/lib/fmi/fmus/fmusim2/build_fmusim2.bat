@echo off 
rem ------------------------------------------------------------
rem Build fmusim2 under Windows
rem Based on (c) 2011 QTronic GmbH

rem FMU SDK license 

rem Copyright © 2008-2011, QTronic GmbH. All rights reserved.
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
echo building fmusim2 - a FMU-2.0 Co-Simulation driver.

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


rem /wd4090 disables warnings about different 'const' qualifiers
cl /wd4090 /nologo /DFMI_COSIMULATION /DFMI_VERSION=2 main.c sim_support.c stack.c xml_parser.c /Fefmusim2.exe /link libexpatMT.lib
goto cleanup


:noCompiler
echo No Microsoft Visual C compiler found
exit


:compileError
echo build of fmusim2 failed

:cleanup

rem undo variable settings performed by vsvars32.bat
set PATH=%PREV_PATH%
if defined PREV_INCLUDE set INCLUDE=%PREV_INCLUDE%
if defined PREV_LIB     set LIB=%PREV_LIB%
if defined PREV_LIBPATH set LIBPATH=%PREV_LIBPATH%
echo done.
