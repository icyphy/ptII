/* A utility to construct a Ptolemy II model from a HSIF model.

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
import ptolemy.kernel.util.IllegalActionException;

import diva.util.*;

// Java imports.
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// HSIFToPtolemyII
/**
This class transfer a HSIF model into a Ptolemy II model.

@author Haiyang Zheng
@version $Id:
*/

public class HSIFToPtolemyII {

    /** constructor
     */
    public HSIFToPtolemyII() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Ptolemy II xml code for the given HSIF code.
     *  @return The Ptolemy II xml code.
     */

    public static void main (String[] args) {
	_processCommandArgs (args);
	try {
	  hsifProcessor = new HSIFProcessor(_inputFileName, _outputFileName);
	  hsifProcessor.process();
	  System.exit(0);
	} catch (IllegalActionException e) {
	  System.err.println ("Error in IO operations: " + e.getMessage ());
	  System.exit(1);
	}
    }



    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    public static HSIFProcessor hsifProcessor;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////

    /** Print helpful usage message. */
    private static void _usage () {
      System.err.println ("Usage: java [classpath] HSIFToPtolemyII [input-file]\n" +
			  "	  the 'input-file' should end with '.xml'\n");
      System.exit(0);
    }

    /** process the input arguments
     *
     */

    private static void _processCommandArgs(String[] args) {
      if (args.length != 1) {
	_usage();
      } else {
      	String arg = args[0];
	int dotPosition = arg.indexOf(".xml");

	if (dotPosition == -1) _usage();

	_inputFileName = arg;
	_outputFileName = arg.substring(0, dotPosition) + "PtolemyII.xml";
      }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static String _inputFileName = "";
    private static String _outputFileName = "";
}
