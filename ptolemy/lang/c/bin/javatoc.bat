@echo off
rem
rem A script to convert Java files to equivalent C code (.h and .c files).
rem Usage: 'javatoc <arg>',
rem where test\<arg>.java is a Java source file in .\test
rem
rem The following output is generated:
rem test\<arg-out>.txt:  diagnostic output
rem test\<arg>.c:  generated .c file
rem test\<arg>.h:  generated .h file
rem
rem @author Shuvra S. Bhattacharyya 
rem @version $Id$
rem
java ptolemy.lang.c.JavaToC test\%1.java > test\%1-out.txt
