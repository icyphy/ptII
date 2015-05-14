@ECHO OFF


::--- Purpose.
::
::   Compile the source code file named as a command-line argument.
:: ** Use Microsoft Visual Studio 10/C.
:: ** Native address size.


::--- Set up command environment.
::
::   Run batch file {vcvarsall.bat} if necessary.
::   Work through a hierarchy of possible directory locations.
::
IF "%DevEnvDir%"=="" (
  ::CALL "C:\Program Files\Microsoft Visual Studio 10.0\VC\vcvarsall.bat"  >nul 2>&1
  CALL "C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\bin\amd64\vcvars64.bat"  >nul 2>&1
  IF ERRORLEVEL 1 (
    CALL "C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\vcvarsall.bat"  >nul 2>&1
    IF ERRORLEVEL 1 (
      ECHO Problem configuring the Visual Studio tools for command-line use
      GOTO done
      )
    )
  )

::--- Compile.
::
::   Note if the C system library implements memmove(), then #define HAVE_MEMMOVE.
:: This is necessary for compiling Expat.
::

:: Delete any old files
ECHO Ready to delete old files
rm jniTofmu.dll jniTofmu.lib jniTofmu.exp

ECHO Ready to compile jniTofmu
:: Compile libraries
cl -c jniTofmu.c /I"c:\Program Files\java\jdk1.7.0_71\include" /I"c:\Program Files\java\jdk1.7.0_71\include\win32"

:: Link and create shared libraries
ECHO Ready to link jniTofmu
link jniTofmu.obj /dll


:done
