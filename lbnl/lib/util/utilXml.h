// Methods for parsing XML files.

/*
********************************************************************
Copyright Notice
----------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy). All rights reserved.

If you have questions about your rights to use or distribute this
software, please contact Berkeley Lab's Technology Transfer Department
at TTD@lbl.gov

NOTICE.  This software was developed under partial funding from the U.S.
Department of Energy.  As such, the U.S. Government has been granted for
itself and others acting on its behalf a paid-up, nonexclusive,
irrevocable, worldwide license in the Software to reproduce, prepare
derivative works, and perform publicly and display publicly.  Beginning
five (5) years after the date permission to assert copyright is obtained
from the U.S. Department of Energy, and subject to any subsequent five
(5) year renewals, the U.S. Government is granted for itself and others
acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
license in the Software to reproduce, prepare derivative works,
distribute copies to the public, perform publicly and display publicly,
and to permit others to do so.


Modified BSD License agreement
------------------------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the
      distribution.
   3. Neither the name of the University of California, Lawrence
      Berkeley National Laboratory, U.S. Dept. of Energy nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes,
patches, or upgrades to the features, functionality or performance of
the source code ("Enhancements") to anyone; however, if you choose to
make your Enhancements available either publicly, or directly to
Lawrence Berkeley National Laboratory, without imposing a separate
written license agreement for such Enhancements, then you hereby grant
the following license: a non-exclusive, royalty-free perpetual license
to install, use, modify, prepare derivative works, incorporate into
other computer software, distribute, and sublicense such enhancements or
derivative works thereof, in binary and source code form.

********************************************************************
*/

///////////////////////////////////////////////////////
/// \file   utilXml.h
///
/// \brief  Methods for parsing XML files.
///
/// \author Michael Wetter,
///         Simulation Research Group, 
///         LBNL,
///         MWetter@lbl.gov
///
/// \date   2008-02-11
///
/// \version $Id$
///
/// This file provides methods that allow clients to
/// parse XML files needed by the BCVTB.
/// Clients typically call \c getsocketportnumber()
/// to obtain the socket port number, and then
/// call utilClient::establishclientsocket() 
/// to connect to the BSD socket.
/// The methods \c getnumberofxmlvalues() and
/// \c getxmlvalues() can be used by clients to obtain xml values
/// from a file. There are also wrappers to these functions
/// with the same name but a suffix \c 'f' appended.
/// These functions are easier to use with Fortran
/// clients because their string parameters have a different
/// format.
///
///////////////////////////////////////////////////////
#ifndef _UTILXML_H_
#define _UTILXML_H_

#ifdef _MSC_VER // Microsoft compiler
#include <windows.h>
#endif

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include <libxml/xmlmemory.h>
#include <libxml/parser.h>
#include <libxml/tree.h>
#include <libxml/xpath.h>
#include <libxml/xpathInternals.h>

#include "defines.h"

/////////////////////////////////////////////////////////////////////
/// Executes an XPath expression.
///
/// \param filename the name of the xml file.
/// \param xpathExpr the xpath expression to evaluate.
/// \param atrNam the attribute name.
/// \param res the value of the attributes will be stored in this argument.
/// \param size the number of elements found.
/// \return 0 if successful, or a negative integer if an error occurred.
int parsexpath(const char *const filename, 
	       const xmlChar* xpathExpr, 
	       const char *const atrNam,
	       char *const res[],
	       int* size);
/////////////////////////////////////////////////////////////////////
/// Gets the value of an xml element specified by an XPath expression.
///
/// This methods is a wrapper for \c getxmlvalues. 
/// It and can be used if only one attribute value is needed.
///
/// \param docname the name of the xml file.
/// \param xpathExpr the xpath expression to evaluate.
/// \param atrNam the attribute name.
/// \param res the value of the attributes will be stored in this argument.
/// \return 0 if successful, or a negative integer if an error occurred.
int getxmlvalue(const char *const docname,
		 const char* xpathExpr, 
		 const char *const atrNam,
		char *const res);

/////////////////////////////////////////////////////////////////////
/// Gets the xml values for a given XPath expression.
///
/// This method is wrapper for \c getxmlvalues for Fortran
/// programs. The only difference is the argument list, since
/// const char *const arguments are hard to pass from F90.
///
/// \param docname the name of the xml file.
/// \param xpathExpr the xpath expression to evaluate.
/// \param atrNam the attribute name.
/// \param nAtr the number of attributes to be obtained.
/// \param res the value of the attributes will be stored in this argument.
///          The terminating character is a semicolon (';')
/// \param sizeOfRes the size of \c res, used to prevent buffer overflow
/// \return 0 if successful, or a negative integer if an error occurred.
int getxmlvaluesf(const char *const docname,
		  const char* xpathExpr, 
		  const char *const atrNam,
		  const int* nAtr,
		  char * res,
		  const int sizeOfRes);
/////////////////////////////////////////////////////////////////////
/// Gets the number of xml values for a given XPath expression.
///
/// This method gets the number of xml values 
/// that are found by evaluating the XPath expression \c xpathExpr
///
/// \param docname the name of the xml file.
/// \param xpathExpr the xpath expression to evaluate.
/// \return the number of attributes found if successful, or a negative integer if an error occurred.
int getnumberofxmlvalues(const char *const docname,
			 const char* xpathExpr);

/////////////////////////////////////////////////////////////////////
/// Gets the xml values for a given XPath expression.
///
/// This method gets the xml values 
/// that are found by evaluating the XPath expression \c xpathExpr
///
/// \param docname the name of the xml file.
/// \param xpathExpr the xpath expression to evaluate.
/// \param atrNam the attribute name.
/// \param res the value of the attributes will be stored in this argument.
/// \return 0 if successful, or a negative integer if an error occurred.
int getxmlvalues(const char *const docname,
		 const char* xpathExpr, 
		 const char *const atrNam,
		 char *const res[]);

#endif /* _UTILXML_H_ */
