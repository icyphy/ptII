XML2ECSL translator
-----------------------------
This folder contatins the source files for the XML2ECSL translator in the ECSL DP tool chain.

3rd party tool requirements to build XML2ECSL:
	- STLport 4.5.3 (part of UDM 3rdParty source)
	- Zlib 1.2.1 (part of UDM 3rdParty source)
	- Xerces 2.2.0 (part of UDM 3rdParty source)
	- Udm 2.20

Include Directory Settings (INCLUDE):
	3RDPARTY\STL
	3RDPARTY\XERCES\XERCES-C2_2_0-WIN32\INCLUDE
	3RDPARTY\XERCES\XERCES-C2_2_0-WIN32\INCLUDE\XERCESC

Lib Directory Settings (LIB):
	3RDPARTY\XERCES\XERCES-C2_2_0-WIN32\LIB
	3RDPARTY\ZLIB

Bin Directory Settings (PATH):
	%UDM_PATH%\BIN

The ECSL_DP toolchain workflow is the following.
First, the user creates the Matlab SL/SF models to capture the
signal/state-flow of the system.

Then, there are two translators to be used sequentially to produce an
ECSL-DP representation of the Matlab Simulink model.

These are: MDL2XML.exe, and XML2ECSL.exe. MDL2XML.exe takes a Matlab
Simulink .mdl file, and produces an XML model, whose schema is described
by matlab.xsd.

This XML model is the input of the next translator: XML2ECL.exe, which
converts this file into an ECSL-DP GME model file (.mga).
Then the ECSL-DP model can be expanded by various software and hardware
modeling elements available in the ECSL-DP language.
Finally, the ECSL-DP model can be transformed into analysis artifacts/
executable code with the help of other model transformers.
Some of the model transformers are GME interpreters, but most of them
are command line tools.

The etc_control.xml is an example XML file (output of MDL2XML.exe) which
represents the Electronic Throttle Control model in Matlab SL/SF.
You are not supposed to do anything before running the command line
translator XML2ECSL.exe on this file, except importing and registering
ECSL-DP metamodel in GME.

UDM
---
Download UDM 2.20 from
http://www.isis.vanderbilt.edu/Projects/mobies/downloads.asp#UDM
or http://www.isis.vanderbilt.edu/projects/mobies/downloads.asp

The XSD files are generated with a UDM tool. Udm.exe as part of the
compilation process in a custom build step.

Don't install Udm in c:/Program Files/ISIS, instead install it in a
directory that has no spaces in the name.  

If the name has spaces in it, you might see:

      Generating UDM interfaces from .\matlab.xml
      'C:\Program' is not recognized as an internal or external
      command, operable program or batch file. Error executing
      c:\windows\system32\cmd.exe.

Changing the Custom Build settings for ECSL_DP.xml and matlab.xml
from
        %UDM_PATH%\bin\Udm.exe $(InputPath) -d %UDM_PATH%\etc -t
to
        "%UDM_PATH%\bin\Udm.exe" $(InputPath) -d "%UDM_PATH%\etc" -t
did not fix the problem, but reinstalling UDM 2.20 did work.


STL
---

STL is included in UDM/3rdparty/stl/

It can also be downloaded from
http://www.stlport.org/download.html

-------
Problem:
While building Xml2Ecsl:
DataflowConverter.cpp
c:\cxh\src\udm\include\errhand.h(72) : fatal error C1083: Cannot open include file: 'stl_user_config.h': No such file or directory

Solution: Add $(STL_PATH)/3rdparty/stl to 
	   Project -> Settings -> C++ -> C++ filter
	   

-------
Problem:
LINK : fatal error LNK1104: cannot open file "xerces-c_2D.lib"
Error executing link.exe.

1st Solution: Download from Xerces-C 2.6.0 from
http://xml.apache.org/xerces-c/download.cgi

1. Edit the Xml2Ecsl project settings and add the Xerces Library to 
Link -> Input -> Additional library path
2. Change library to xerces-c_2d

Real Solution: xerces is included in UDM in the 3rdparty/xerces
directory, so I added $(UDM_PATH)\3rdparty\xerces-c2_2_0-win32\lib
to the Additional library path

-------
Problem:
LINK : fatal error LNK1104: cannot open file "zlibD.lib"

1st Solution: Download Zlib Developer files from
http://gnuwin32.sourceforge.net/downlinks/zlib-lib-zip.php
and add that directory to the link list
Also, change the library to libz.lib

Real Solution: zlib is included in UDM in the 3rdparty/zlib
directory, so I added $(UDM_PATH)\3rdparty\xerces-c2_2_0-win32\lib
to the Additional library path, which now looks like:

$(UDM_PATH)\lib,$(UDM_PATH)3rdparty\zlib,$(UDM_PATH)3rdparty\xerces\xerces-c2_2_0-win32\lib