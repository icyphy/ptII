$Id$

XML2ECSL translator
-----------------------------

This folder contatins the source files for the XML2ECSL translator in
the ECSL DP tool chain.

Requirements to build XML2ECSL:
  - Udm 2.19 
  - STLport 4.5.3 (part of UDM 3rdParty source)
  - Xerces-C 2.6.0

UDM
---
Download from http://www.isis.vanderbilt.edu/Projects/mobies/downloads.asp#UDM

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

Solution: Add $(STL_PATH)/3rdpart/stl to 
	   Project -> Settings -> C++ -> C++ filter
	   

-------
Problem:
LINK : fatal error LNK1104: cannot open file "xerces-c_2D.lib"
Error executing link.exe.

Solution: Download from Xerces-C 2.6.0 from
http://xml.apache.org/xerces-c/download.cgi

1. Edit the Xml2Ecsl project settings and add the Xerces Library to 
Link -> Input -> Additional library path
2. Change library to xerces-c_2d

-------
Problem:
LINK : fatal error LNK1104: cannot open file "zlibD.lib"
Solution: Download Zlib Developer files from
http://gnuwin32.sourceforge.net/downlinks/zlib-lib-zip.php
and add that directory to the link list
Also, change the library to libz.lib