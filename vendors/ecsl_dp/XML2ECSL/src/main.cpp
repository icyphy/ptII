#include <iostream>
#include <string>

#include "UdmBase.h"	// for cg_exception

#include "Matlab2EcslDP.h"

const std::string XML2ECSL_VERSION= "1.1";

#ifndef __INTEGRATED_INTO_OTIF__

void printVer()
{
	std::cerr << "Xml2Ecsl model converter v" << XML2ECSL_VERSION << " \n(c) 2003-2004 Vanderbilt University\nInstitute for Software Integrated Systems\n\n";
}

int usage()
{
	printVer();
	std::cerr << "Usage: xml2ecsl.exe <XmlFileName> [EcslFileName] \n";
	return 1;
}

std::string dropExtension( const std::string& filename)
{
	std::string nameNoExt( filename);
	std::string::size_type findExt= nameNoExt.rfind( '.');
	if ( findExt != std::string::npos)
		if( *(nameNoExt.begin()+ findExt+ 1) != '\\')
			nameNoExt.erase( nameNoExt.begin()+ findExt, nameNoExt.end());	// delete '.' too.
	return nameNoExt;
}

int main( int argc, char* argv[])
{
	if(argc < 2) {
		return usage();
	}
	std::cerr << "Converting " << argv[1] << " ..." << std::endl;
	try {
		Matlab2EcslDP matlab2EcslDP( argv[ 1], (argc == 3 ? argv[ 2] : dropExtension( argv[ 1])+ ".mga"));
		std::cerr << "Calling matlab2EcslDP()" << std::endl;
		matlab2EcslDP();
	}
	catch(const udm_exception &e) {
		std::cerr << "Exception: " << e.what() << std::endl;
		return 1;
	}
	std::cerr << "Done." << std::endl;
	return 0;
}

#endif //__INTEGRATED_INTO_OTIF__