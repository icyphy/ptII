$Id$

Virtual Reality Peripheral Network (VRPN)

Available from http://www.cs.unc.edu/Research/vrpn/index.html

Windows
------
A quick start on how to build vrpn library and run the VRPN_Listener.cpp from the
NaturalPoints website:

1.Download the latest version of vrpn library from:
	http://www.cs.unc.edu/Research/vrpn/obtaining_vrpn.html
	ftp://ftp.cs.unc.edu/pub/packages/GRIP/vrpn

2.Start vrpn.dsw from: C:\ReacTable\vrpn_07_26\vrpn\vrpn.dsw
	***Loading solution projects might take some time (31 projects)***
	***You only need to build 2 of these projects:: Build vrpn
							Build quatlib ***
	Note: Other projects in the library might have errors but we only care about
	vrpn and quatlib to get built.

3.Download VRPN_Listener sample code from:
	http://www.naturalpoint.com/optitrack/support/downloads.html#streaming

4.Open VRPN_Listener.sln

5.Change the setttings as follow:
	Project-> VRPN_Listener Properties
				->Linker->input->Additional Dependencies 
					##Edit and Add vrpndll.lib, quat.lib, ws2_32.lib##
				->Linker->General->Additioal Library Directories
					##Edit and Add: C:\ReacTable\vrpn_07_26\quat\pc_win32\Debug
							C:\ReacTable\vrpn_07_26\vrpn\pc_win32\DLL\Debug
							.\vrpn_07_26\vrpn\pc_win32\Release
				->C/C++->Additional Include Directories
					##Edit and Add: C:\ReacTable\vrpn_07_26\vrpn
							C:\ReacTable\vrpn_07_26\quat
				->VC++ Directories->Include Directories
					##Edit and Add: C:\ReacTable\VRPN_Tracking-v3
			

