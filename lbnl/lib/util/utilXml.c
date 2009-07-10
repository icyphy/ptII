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
/// \file   utilXml.c
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
///
///////////////////////////////////////////////////////
#include "utilXml.h"

/////////////////////////////////////////////////////////////////////
/// Writes the xml elements into \c res.
///
/// \param nodes the xml node set.
/// \param atrNam the name of the attribute to be written to \c res
/// \param res the value of the elements will be stored in this argument.
/// \return 0 if successful, or a negative integer if an error occurred.
int _getnodevalues(xmlNodeSetPtr nodes, 
		   const char *const atrNam,
		   char *const res[]) {

    int size;
    int i;
    xmlNodePtr cur;
    xmlChar *value;
    size = (nodes) ? nodes->nodeNr : 0;
    for(i = 0; i < size; ++i) {
      if (NULL == nodes->nodeTab[i] ){
	fprintf(stderr, "Error: XML node pointer is NULL, i=%d.\n", i);
	return -1;
      }
      //      if(nodes->nodeTab[i]->type == XML_ELEMENT_NODE) {
      cur = nodes->nodeTab[i];   	    
      value = xmlGetProp(cur, (xmlChar*)atrNam);
      if ( NULL == value ){
	fprintf(stderr, "Error: Did not find expected attribute name '%s'.\n", atrNam);
	return -2;
      }
      //  fprintf(stdout, "= value %s\n", value);
      strcpy(res[i], (char*)value);
		  //		  cur->ns->href, cur->name);
	//	fprintf(output, "= node \"%s\": type %d\n", cur->name, cur->type);
      //      }
    }
    return 0;
}
/**
 * execute_xpath_expression:
 * @filename:		the input XML filename.
 * @xpathExpr:		the xpath expression for evaluation.
 *
 * Parses input XML file, evaluates XPath expression and prints results.
 *
 * Returns 0 on success and a negative value otherwise.
 */

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
	       int* size){
  int retVal;
    xmlDocPtr doc;
    xmlXPathContextPtr xpathCtx; 
    xmlXPathObjectPtr xpathObj;
    xmlNodeSetPtr nodes;
    
    assert(filename);
    assert(xpathExpr);

    /* Load XML document */
    doc = xmlParseFile(filename);
    if (doc == NULL) {
	fprintf(stderr, "Error: unable to parse file \"%s\"\n", filename);
	return -1;
    }

    /* Create xpath evaluation context */
    xpathCtx = xmlXPathNewContext(doc);
    if(xpathCtx == NULL) {
        fprintf(stderr,"Error: unable to create new XPath context\n");
        xmlFreeDoc(doc); 
	return -1;
    }
    
    /* Evaluate xpath expression */

    xpathObj = xmlXPathEvalExpression(xpathExpr, xpathCtx);
    if(xpathObj == NULL) {
        fprintf(stderr,"Error: unable to evaluate xpath expression \"%s\"\n", xpathExpr);
        xmlXPathFreeContext(xpathCtx); 
        xmlFreeDoc(doc); 
        return -1;
    }

    // get number of elements found
    nodes = xpathObj->nodesetval;
    *size = (nodes) ? nodes->nodeNr : 0;

    /* Print results */
    retVal = 0;
    if ( NULL != res ){
     retVal = _getnodevalues(xpathObj->nodesetval, atrNam, res);
    }
 
    /* Cleanup */
    xmlXPathFreeObject(xpathObj);
    xmlXPathFreeContext(xpathCtx); 
    xmlFreeDoc(doc); 
    return retVal;
}

int _checkMalloc(const void* ptr){
  if ( NULL == ptr ){
    fprintf(stderr,"Failed to allocate memory in utilXml.c.\n");
    return -1;
  }
  return 0;      
}

char** _allocateChar(char** c, const int nEle, const int len){
  int i;
  char** tmp = (char**)malloc(nEle * sizeof(char*));
  if ( _checkMalloc(tmp) )
    return NULL;

  for(i=0; i < nEle; i++){
    tmp[i] = (char *)malloc((len) * sizeof(char));
    if ( _checkMalloc(tmp[i]) )
      return NULL;
  }
  return tmp;
}


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
		char *const res){
  int retVal;
  const int nAtr = 1;
  char* atrVec[1];
  atrVec[0] = (char *) malloc(BUFFER_LENGTH);
  if (atrVec[0] == NULL){
    fprintf(stderr,"Failed to allocate memory when parsing xml document.\n");
    return -1;
  }
  retVal = getxmlvalues(docname, xpathExpr, atrNam, atrVec);
  if ( retVal == 0 )
    strcpy(res, atrVec[0]);
  free(atrVec[0]);
  return retVal;
}

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
		  const int sizeOfRes) {
  int i, retVal, len;

  char** resC = 0;
  resC = _allocateChar(resC, *nAtr, BUFFER_LENGTH);
  // check if all allocate statements were successfull
  if (resC == NULL)
    return -1;
  // get the xml values
  retVal = getxmlvalues(docname, xpathExpr, atrNam, resC);
  // copy the string array
  for(i=0; i < *nAtr; i++){
    if ( i == 0 ){

#ifdef _MSC_VER // Microsoft compiler
      len = _snprintf(res, sizeOfRes, "%s;", resC[i]);
#else
      len = snprintf(res, sizeOfRes, "%s;", resC[i]);
#endif
      if(len >= BUFFER_LENGTH){
	fprintf(stderr, "Error in utilXml: Buffer not long enough. Change defines.h\n");
	return -1;
      }
    }
    else{
      const int size = sizeOfRes - strlen(res);
#ifdef _MSC_VER // Microsoft compiler
      len = _snprintf(res + strlen(res), size, "%s;", resC[i]);
#else
      len = snprintf(res + strlen(res), size, "%s;", resC[i]);
#endif
      if(len >= size){
	fprintf(stderr, "Error in utilXml: Buffer not long enough. Change defines.h\n");
	return -1;
      }
    }
  }
  // free memory
  for(i=0; i < *nAtr; i++)
    free(resC[i]);
  free(resC);


  return 0;
}

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
			 const char* xpathExpr) {
  int size;
  int retVal = parsexpath(docname,
			  (const xmlChar*)xpathExpr,
			  NULL, NULL,
			  &size);
  if ( retVal != 0 )
    return retVal;
  else
    return size;
}

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
		 char *const res[]) {
  int size;
  const int retVal = parsexpath(docname,
				(const xmlChar*)xpathExpr,
				atrNam,
				res,
				&size);
  return retVal;
}

/*
/////////////////////////////////////////////////////////////////////////////////
/// Main routine for testing
///
int main(int argc, char *argv[]) {
  int i;
	char *docname;
	if (argc <= 1) {
		printf("Usage: %s docname\n", argv[0]);
		return(0);
	}
	docname = argv[1];
	//////////////////////////////////////////////////////
	char* expr = "//EnergyPlus[@dayschedule]";
	int nVal = getnumberofxmlvalues(docname, expr);
	fprintf(stdout, "number of day schedules = %d\n", nVal);
	// get the xml values
	char* res[nVal]; 
	for (i = 0; i < nVal; i++){
	  res[i] = (char *) malloc(BUFFER_LENGTH);
	  if (res[i] == NULL){
	    fprintf(stderr,"Failed to allocate memory.\n");
	    return -1;
	  }
	}
	int retVal;
	retVal = getxmlvalues(docname, expr, "dayschedule", res);
	if (retVal == 0){
	  for (i = 0; i < nVal; i++)
	    fprintf(stdout, "name = %s\n", res[i]);
	}
	else
	  fprintf(stdout, "Error when getting xml values for dayschedule.\n");

	//////////////////////////////////////////////////////
	expr = "//EnergyPlus[@type]";
	nVal = getnumberofxmlvalues(docname, expr);
	fprintf(stdout, "number of type = %d\n", nVal);
	// get the xml values
	char* res2[nVal]; 
	for (i = 0; i < nVal; i++){
	  res2[i] = (char *) malloc(BUFFER_LENGTH);
	  if (res2[i] == NULL){
	    fprintf(stderr,"Failed to allocate memory.\n");
	    return -1;
	  }
	}
	retVal = getxmlvalues(docname, expr, "type", res2);
	if (retVal == 0){
	  for (i = 0; i < nVal; i++)
	    fprintf(stdout, "type = %s\n", res2[i]);
	}
	else
	  fprintf(stdout, "Error when getting xml values for type.\n");

	//////////////////////////////////////////////////////
	expr = "//EnergyPlus[@name]";
	nVal = getnumberofxmlvalues(docname, expr);
	fprintf(stdout, "number of name = %d\n", nVal);
	// get the xml values
	char* res3[nVal]; 
	for (i = 0; i < nVal; i++){
	  res3[i] = (char *) malloc(BUFFER_LENGTH);
	  if (res3[i] == NULL){
	    fprintf(stderr,"Failed to allocate memory.\n");
	    return -1;
	  }
	}
	retVal = getxmlvalues(docname, expr, "name", res3);
	if (retVal == 0){
	  for (i = 0; i < nVal; i++)
	    fprintf(stdout, "name = %s\n", res3[i]);
	}
	else
	  fprintf(stdout, "Error when getting xml values for name.\n");


	return 0;
}
*/
