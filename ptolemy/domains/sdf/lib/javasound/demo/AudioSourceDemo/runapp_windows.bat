rem This script will execute a Java application that uses Java Sound.
rem Pass the name of the application as the first parameter. 
rem If you specify a second parameter, it will be passed to the application.

rem This should specify the location of your Win32 JDK.
rem Modify this variable as needed.
set DEFAULT_JAVA_HOME=c:\jdk1.2.2\

rem This should specify the location of the Java Sound install.
rem Modify this variable as needed.
set SOUND_HOME=c:\javasound086\

rem You should not need to modify the script beyond this point.

set TEST_NAME=%1
set TEST_PARAM=%2

if "%JAVA_HOME%" == "" set JAVA_HOME=%DEFAULT_JAVA_HOME%

set EXAMPLES_HOME=%SOUND_HOME%\examples
set EXAMPLES_CLASSPATH=.;%SOUND_HOME%\lib\sound.jar;%EXAMPLES_HOME%\sampled;%EXAMPLES_HOME%\midi;../../../../../../..

set OLDPATH=%PATH%
set PATH=%SOUND_HOME%\lib;%PATH%

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%EXAMPLES_CLASSPATH%;%CLASSPATH%;%JAVA_HOME%\lib\classes.zip
set JAVA=%JAVA_HOME%\bin\java

set CMD=%JAVA% -classpath %CLASSPATH% %TEST_NAME% %TEST_PARAM%

%CMD%

set PATH=%OLDPATH%
set CLASSPATH=%OLDCLASSPATH%
