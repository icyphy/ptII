@echo off
rem
rem A script to convert Java files to equivalent C code (.h and .c files).
rem Usage: 'javatoc <arg>',
rem where <arg>.java is a Java source file.
rem
rem The following output is generated:
rem <arg-out>.txt:  diagnostic output
rem <arg>.c:  generated .c file
rem <arg>.h:  generated .h file
rem
rem @author Shuvra S. Bhattacharyya 
rem @version $Id$
rem
if exist %1-out.txt del %1-out.txt
java ptolemy.lang.c.JavaToC %1.java > %1-out.txt 2>&1
