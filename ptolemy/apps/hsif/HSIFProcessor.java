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

import diva.util.xml.*;
import diva.util.*;
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
//////////////////////////////////////////////////////////////////////////
//// HSIFProcessor
/**
This processor engine to transfer a HSIF model into a Ptolemy II model.

@author Haiyang Zheng
@version $Id:
*/
public class HSIFProcessor {

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

    System.out.println("parsing input file....");
    _parser();

    System.out.println("constructing models....");
//    _constructor();

/*    try {
      System.out.println("writing output file....");
//      _writer();
    } catch (IOException e) {
      throw new IllegalActionException(e.getMessage());
    }
*/
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

*/  // parse the HSIF file into hiearchical objects tree.
  private void _parser() throws IllegalActionException {
//    DNHA dnha;
      try {
	XmlDocument document = new XmlDocument(new File(_inputFileName));
	XmlReader reader = new XmlReader();
	reader.setVerbose(true);
	reader.parse(document);
	if (reader.getErrorCount() > 0) {
	    throw new IllegalActionException("Bad HSIF file!");
	}

	XmlElement root = document.getRoot();
	for (Iterator i = root.elements(); i.hasNext();) {
	  XmlElement xe = (XmlElement) i.next();
	  System.out.println(xe.toString());
	}
      } catch (Exception e) {
	throw new IllegalActionException (e.getMessage());
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

