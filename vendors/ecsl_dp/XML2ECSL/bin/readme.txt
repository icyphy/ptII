Files in this folder:
	Xml2Ecsl.exe		Xml to Ecsl translator executable
	UdmDll.dll		Udm DLL used by Xml2Ecsl.exe
 
                               (UdmDll.dll is nothing else just the
                               generic UDM libraries linked
                               dynamically into one DLL. There's not
                               any GME interpreter that you want to
                               use in this setup.)

	xerces-c_2_2_0.dll	Xerces DLL used by Xml2Ecsl.exe
	etc_control.xml		sample input file for Xml2Ecsl.exe
	etc_control_ref_output.xme	the corresponding translated output as a reference in GME exported format.  
 

1. Build Xml2Ecsl_d.exe using MSVC 6.0 sp6.  See ../src/readme.txt
2. Run the translator on etc_control.xml
     bash-2.05b$ ./Xml2Ecsl_d.exe etc_control.xml
     Converting etc_control.xml ...
     growing...
     Done.
     bash-2.05b$ 
   This will create etc_control.mga
3. Start up GME and load etc_control.mga
4. Export the XML with File -> Export XML, 
   which will create etc_control_ref_output.xme


Xml2Ecsl Usage
--------------
Below is the usage message for Xml2Ecsl
bash-2.05b$ ./Xml2Ecsl_d
Xml2Ecsl model converter v1.1 
(c) 2003-2004 Vanderbilt University
Institute for Software Integrated Systems

Usage: xml2ecsl.exe <XmlFileName> [EcslFileName] 



Problems
--------
10/18/04:
Oddly, the release version of Xml2Ecsl crashes with:
  The application failed to initialized properly (0xc0000022)
  Click on OK to terminate the application

Running the debugger on the release version says:
  Loaded 'C:\cxh\ptII\vendors\ecsl_dp\XML2ECSL\bin\Xml2Ecsl.exe', no matching symbolic information found.
  Loaded 'ntdll.dll', no matching symbolic information found.
  Loaded 'C:\WINDOWS\system32\kernel32.dll', no matching symbolic information found.
  The thread 0x9AC has exited with code -1073741790 (0xC0000022).
  First-chance exception in Xml2Ecsl.exe (NTDLL.DLL): 0xC0000022: (no name).
  The program 'C:\cxh\ptII\vendors\ecsl_dp\XML2ECSL\bin\Xml2Ecsl.exe' has exited with code -1073741790 (0xC0000022).
