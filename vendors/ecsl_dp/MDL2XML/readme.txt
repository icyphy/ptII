MDL2XML translator
-----------------------------
This folder contains the source files for the MDL2XML translator in the ECSL DP tool chain.

3rd party tool requirements to build MDL2XML:
	- STLport 4.5.3 (part of UDM 3rdParty source)
	- Antlr 2.7.1 (part of UDM 3rdParty source)
	- Zlib 1.2.1 (part of UDM 3rdParty source)
	- Xerces 2.2.0 (part of UDM 3rdParty source)
	- Udm 2.20

Include Directory Settings (INCLUDE):
	3RDPARTY\STL
	3RDPARTY\XERCES\XERCES-C2_2_0-WIN32\INCLUDE
	3RDPARTY\XERCES\XERCES-C2_2_0-WIN32\INCLUDE\XERCESC
	3RDPARTY\ANTLR\ANTLR-2.7.1\LIB\CPP

Lib Directory Settings (LIB):
	3RDPARTY\XERCES\XERCES-C2_2_0-WIN32\LIB
	3RDPARTY\ZLIB
	3RDPARTY\ANTLR\ANTLR-2.7.1\LIB

Bin Directory Settings (PATH):
	%UDM_PATH%\BIN
