@echo off
rem
rem Script to run the FIR demo
rem @author Shuvra S. Bhattacharyya 
rem @version $Id$
rem
echo Generating the class file ...
javac FIR.java
echo Translating the class file to C ...
java ptolemy.copernicus.c.JavaToC .;C:\jdk1.3.1\jre\lib\rt.jar FIR -singleClass
echo Compiling the generated C ...
gcc -c FIR.c
gcc -c FIRMain.c
echo Linking the .o files ...
gcc -o firtest FIRMain.o FIR.o
echo Executing the generated executable (firtest.exe) ...
firtest
