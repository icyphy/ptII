@echo off
rem
rem Script to run the FIR demo
rem @author Shuvra S. Bhattacharyya 
rem @version $Id$
rem
rem The directory where the pccg runtime code resides
set RUNTIME=../../runtime
set RUNTIMEW=..\..\runtime
echo Remove earlier versions of .o and .exe files (if they exist) ...
rm -f *.o
rm -f *.exe
echo Generating the class file ...
javac FIR.java
echo Translating the class file to C ...
java -Dj2c_lib=\j2c_lib ptolemy.copernicus.c.JavaToC .;C:\jdk1.3.1\jre\lib\rt.jar -singleClass FIR
echo Compiling the generated C ...
gcc -c -I%RUNTIME% FIR.c
gcc -c -I%RUNTIME% FIRMain.c
echo Compiling the required runtime file(s)
pushd %RUNTIMEW%
rm -f runtime.o
gcc -c runtime.c
popd
echo Linking the .o files ...
gcc -o firtest FIRMain.o FIR.o %RUNTIME%/runtime.o
echo Executing the generated executable (firtest.exe) ...
firtest
