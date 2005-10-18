# Microsoft Developer Studio Project File - Name="Matlab2XML" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Console Application" 0x0103

CFG=Matlab2XML - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "MDL2XML.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "MDL2XML.mak" CFG="Matlab2XML - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "Matlab2XML - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "Matlab2XML - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""$/MoBIES/Matlab2XML", AGEAAAAA"
# PROP Scc_LocalPath "."
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "Matlab2XML - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD CPP /nologo /MD /W3 /GR /GX /Zd /O2 /I "$(UDM_PATH)/3rdparty/stl" /I "." /I "$(UDM_PATH)/include" /I "..\include" /I "$(UDM_PATH)\3rdparty\antlr\antlr-2.7.1\lib\cpp" /D "NDEBUG" /D "WIN32" /D "_CONSOLE" /D "_MBCS" /D "WITH_SGI_STL" /D "UDM_DYNAMIC_LINKING" /YX /FD /Zm200 /c
# SUBTRACT CPP /Fr
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /machine:I386
# ADD LINK32 antlr.lib xerces-c_2.lib UdmDll.lib zlib.lib /nologo /subsystem:console /machine:I386 /out:"../bin/MDL2XML.exe" /libpath:"$(UDM_PATH)\Lib" /libpath:"$(UDM_PATH)\3rdParty\xerces\xerces-c2_2_0-win32\lib" /libpath:"$(UDM_PATH)\3rdParty\zlib" /libpath:"$(UDM_PATH)\3rdParty\antlr\antlr-2.7.1\lib"
# SUBTRACT LINK32 /verbose /pdb:none

!ELSEIF  "$(CFG)" == "Matlab2XML - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /GZ /c
# ADD CPP /nologo /MDd /W3 /Gm /GR /GX /ZI /Od /I "$(UDM_PATH)/3rdparty/stl" /I "." /I "$(UDM_PATH)/include" /I "..\include" /I "$(UDM_PATH)\3rdparty\antlr\antlr-2.7.1\lib\cpp" /D "_DEBUG" /D "WIN32" /D "_CONSOLE" /D "_MBCS" /D "WITH_SGI_STL" /D "UDM_DYNAMIC_LINKING" /FR /YX /FD /I /gme/include" /I /gme/interfaces" /GZ /Zm200 " " /c
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /pdbtype:sept
# ADD LINK32 antlr_D.lib xerces-c_2D.lib UdmDll_D.lib zlibD.lib /nologo /subsystem:console /debug /machine:I386 /out:"../bin/MDL2XMLd.exe" /pdbtype:sept /libpath:"$(UDM_PATH)\Lib" /libpath:"$(UDM_PATH)\3rdParty\xerces\xerces-c2_2_0-win32\lib" /libpath:"$(UDM_PATH)\3rdParty\zlib" /libpath:"$(UDM_PATH)\3rdParty\antlr\antlr-2.7.1\lib"
# SUBTRACT LINK32 /nodefaultlib

!ENDIF 

# Begin Target

# Name "Matlab2XML - Win32 Release"
# Name "Matlab2XML - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\Mainc.cpp
# End Source File
# Begin Source File

SOURCE=.\Matlab.cpp
# End Source File
# Begin Source File

SOURCE=.\MatlabIntersection2Clock.cpp
# End Source File
# Begin Source File

SOURCE=.\mdl.cpp
# End Source File
# Begin Source File

SOURCE=.\MDL2XML.rc
# End Source File
# Begin Source File

SOURCE=.\MDLLexer.cpp
# End Source File
# Begin Source File

SOURCE=.\MDLParser.cpp
# End Source File
# Begin Source File

SOURCE=.\TransLabelStringParser.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\Matlab.h
# End Source File
# Begin Source File

SOURCE=..\include\MatlabIntersection2Clock.h
# End Source File
# Begin Source File

SOURCE=.\mdl.h
# End Source File
# Begin Source File

SOURCE=.\MDLLexer.hpp
# End Source File
# Begin Source File

SOURCE=.\MDLParser.hpp
# End Source File
# Begin Source File

SOURCE=.\MDLParserTokenTypes.hpp
# End Source File
# Begin Source File

SOURCE=.\resource.h
# End Source File
# Begin Source File

SOURCE=..\include\TransLabelStringParser.h
# End Source File
# End Group
# Begin Group "Other Files"

# PROP Default_Filter "txt;dtd;xml"
# Begin Source File

SOURCE=.\Matlab.xml

!IF  "$(CFG)" == "Matlab2XML - Win32 Release"

# Begin Custom Build - Generating UDM interfaces from $(InputPath)
InputPath=.\Matlab.xml
InputName=Matlab

BuildCmds= \
	"%UDM_PATH%\bin\Udm.exe" $(InputPath) -d "%UDM_PATH%\etc" -t

"$(InputName).h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).dtd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).xsd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ELSEIF  "$(CFG)" == "Matlab2XML - Win32 Debug"

# Begin Custom Build - Generating UDM interfaces from $(InputPath)
InputPath=.\Matlab.xml
InputName=Matlab

BuildCmds= \
	"%UDM_PATH%\bin\Udm.exe" $(InputPath) -d "%UDM_PATH%\etc" -t

"$(InputName).h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).dtd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).xsd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\mdl.g

!IF  "$(CFG)" == "Matlab2XML - Win32 Release"

# Begin Custom Build - Generating Lexer&Parser source code on $(InputPath)
InputPath=.\mdl.g

BuildCmds= \
	java antlr.Tool $(InputPath)

"MDLLexer.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLParser.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLLexer.hpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLParserTokenTypes.hpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLParser.hpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ELSEIF  "$(CFG)" == "Matlab2XML - Win32 Debug"

# Begin Custom Build - Generating Lexer&Parser source code on $(InputPath)
InputPath=.\mdl.g

BuildCmds= \
	java -classpath "c:/Program Files/ISIS/UDM/3rdparty/antlr/antlr-2.7.1/antlr.jar" antlr.Tool $(InputPath)

"MDLLexer.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLParser.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLLexer.hpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLParserTokenTypes.hpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"MDLParser.hpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\mdl.xml

!IF  "$(CFG)" == "Matlab2XML - Win32 Release"

# Begin Custom Build - Generating UDM interfaces from $(InputPath)
InputPath=.\mdl.xml
InputName=mdl

BuildCmds= \
	"%UDM_PATH%\bin\Udm.exe" $(InputPath) -d "%UDM_PATH%\etc" -t

"$(InputName).h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).dtd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).xsd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ELSEIF  "$(CFG)" == "Matlab2XML - Win32 Debug"

# Begin Custom Build - Generating UDM interfaces from $(InputPath)
InputPath=.\mdl.xml
InputName=mdl

BuildCmds= \
	"%UDM_PATH%\bin\Udm.exe" $(InputPath) -d "%UDM_PATH%\etc" -t

"$(InputName).h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).dtd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName).xsd" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ENDIF 

# End Source File
# End Group
# Begin Source File

SOURCE=.\matlab.xsd
# End Source File
# Begin Source File

SOURCE=.\mdl.xsd
# End Source File
# End Target
# End Project
