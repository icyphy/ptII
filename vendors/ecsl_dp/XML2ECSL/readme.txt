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