/* A utility to construct a Ptolemy II model from a Charon model.

Copyright (c) 1998-2005 The Regents of the University of California.
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

@ProposedRating Red (hyzheng)
@AcceptedRating Red (hyzheng)
*/
package ptolemy.apps.charon;


// Ptolemy imports.
import ptolemy.kernel.util.IllegalActionException;

// Java imports.
import java.io.IOException;


//////////////////////////////////////////////////////////////////////////
//// CharonToPtolemyII

/**
   This class transfer a Charon model into a Ptolemy II model.

   @author Haiyang Zheng
   @version $Id:
*/
public class CharonToPtolemyII {
    /** constructor
     */
    public CharonToPtolemyII() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Ptolemy II xml code for the given Charon code.
     *  @return The Ptolemy II xml code.
     */
    public static void main(String[] args) {
        _processCommandArgs(args);

        try {
            charonProcessor = new CharonProcessor(_inputFileName,
                    _outputFileName);
            charonProcessor.process();
            System.exit(0);
        } catch (IllegalActionException e) {
            System.err.println("Error in IO operations: " + e.getMessage());
            System.exit(1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////
    public static CharonProcessor charonProcessor;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Print helpful usage message. */
    private static void _usage() {
        System.err.println("Usage: java CharonToPtolemyII [input-file]\n"
            + "          the 'input-file' should end with '.cn'\n");
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
            int dotPosition = arg.indexOf(".cn");

            if (dotPosition == -1) {
                _usage();
            }

            _inputFileName = arg;
            _outputFileName = arg.substring(0, dotPosition) + ".xml";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static String _inputFileName = "";
    private static String _outputFileName = "";
}
