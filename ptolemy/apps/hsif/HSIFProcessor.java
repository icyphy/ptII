/* A processor engine for transfer a HSIF model to Ptolemy II model.

 Copyright (c) 1998-2001 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @ProposedRating Red (hyzheng@eecs.berkeley.edu)
 @AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/
package ptolemy.apps.hsif;

// Ptolemy imports.
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;

import ptolemy.apps.hsif.lib.*;

// Java imports.
import java.net.URL;
import java.io.*;
import java.util.*;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;

// JAXP packages
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// HSIFProcessor
/**
This processor engine to transfer a HSIF model into a Ptolemy II model.

@author Haiyang Zheng
@version $Id:
*/
public class HSIFProcessor {

    /** All output will use this encoding */
    static final String outputEncoding = "UTF-8";

    /** Output goes here */
    private PrintWriter out;

    /** Indent level */
    private int indent = 0;

    /** Indentation will be in multiples of basicIndent  */
    private final String basicIndent = "  ";

    /** Constants used for JAXP 1.2 */
    static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    /** constructor
     */
    public HSIFProcessor(String inputFileName, String outputFileName) {
	_inputFileName = inputFileName;
	_outputFileName = outputFileName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The engine schedules the tranfer process.
     *
     */

    public void process() throws IllegalActionException {

	try {
	    System.out.println("parsing input file....");
	    _parser();

	    System.out.println("constructing models....");
	    //    _constructor();


	    System.out.println("writing output file....");
	    _writer();
	} catch (Exception e) {
	    throw new IllegalActionException(e.getMessage());
	}

    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    public static LinkedList dnhasList = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////

    /*  // Process the agent from the first elemnet of the agents list
	private void _constructor() throws IllegalActionException {
	System.out.println(dhnasList.size());
	if(DNHAsList.size() > 0) {
	DNHA dnha = (DNHA) dnhasList.get(0);
	_topLevel = dnha.constructor(new Workspace());
	} else {
	throw new IllegalActionException(" Not a good HSIF file! ");
	}
	}

    */

    // Export the model into MoML file.
    private void _writer() throws IOException {
	try {
	    System.out.println("Output File Name is: " + _outputFileName);
	    FileOutputStream fos = new FileOutputStream(_outputFileName);
	    _writer = new OutputStreamWriter(fos);
	    String resultXML = _topLevel.exportMoML();
	    _writer.write(resultXML);
	    _writer.flush();
	} catch (IOException e) {
	    System.err.println ("Error in IO operations: " + e.getMessage ());
	    System.exit(1);
	}
    }

    // parse the HSIF file into hiearchical objects tree.
    private void _parser() throws Exception {
	//    DNHA dnha;

	boolean dtdValidate = false;
	boolean xsdValidate = false;

	boolean ignoreWhitespace = false;
	boolean ignoreComments = false;
	boolean putCDATAIntoText = false;
	boolean createEntityRefs = false;

	// Step 1: create a DocumentBuilderFactory and configure it
	DocumentBuilderFactory dbf =
	    DocumentBuilderFactory.newInstance();

	// Set namespaceAware to true to get a DOM Level 2 tree with nodes
	// containing namesapce information.  This is necessary because the
	// default value from JAXP 1.0 was defined to be false.
	dbf.setNamespaceAware(true);

	// Set the validation mode to either: no validation, DTD
	// validation, or XSD validation
	dbf.setValidating(dtdValidate || xsdValidate);
	if (xsdValidate) {
	    try {
		dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
	    } catch (IllegalArgumentException x) {
		// This can happen if the parser does not support JAXP 1.2
		System.err.println(
				   "Error: JAXP DocumentBuilderFactory attribute not recognized: "
				   + JAXP_SCHEMA_LANGUAGE);
		System.err.println(
				   "Check to see if parser conforms to JAXP 1.2 spec.");
		System.exit(1);
	    }
	}

	// Optional: set various configuration options
	dbf.setIgnoringComments(ignoreComments);
	dbf.setIgnoringElementContentWhitespace(ignoreWhitespace);
	dbf.setCoalescing(putCDATAIntoText);
	// The opposite of creating entity ref nodes is expanding them inline
	dbf.setExpandEntityReferences(!createEntityRefs);

	// Step 2: create a DocumentBuilder that satisfies the constraints
	// specified by the DocumentBuilderFactory
	DocumentBuilder db = dbf.newDocumentBuilder();

	// Set an ErrorHandler before parsing
	OutputStreamWriter errorWriter =
	    new OutputStreamWriter(System.err, outputEncoding);
	db.setErrorHandler(
			   new MyErrorHandler(new PrintWriter(errorWriter, true)));

	// Step 3: parse the input file
	Document doc = db.parse(new File(_inputFileName));

	// Print out the DOM tree
	OutputStreamWriter outWriter =
	    new OutputStreamWriter(System.out, outputEncoding);
	DOMEcho domEcho = new DOMEcho(new PrintWriter(outWriter, true));
	domEcho.echo(doc, null, null);
	_topLevel = domEcho.getTopLevel();

    }

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private StreamTokenizer _input;
    private TypedCompositeActor _topLevel;

    private int _counter =0;
    private Reader _reader;
    private Writer _writer;
    private String _inputFileName;
    private String _outputFileName;
}

