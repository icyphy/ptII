/* A code generator for each actor in a MoML system.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.codegen.saveasjava;

import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;


//////////////////////////////////////////////////////////////////////////
//// MoMLToJava
/**
A MoML-reading wrapper for the saving of Ptolemy II models as Java source code.
This class converts MoML files to equivalent standalone Ptolemy II Java code.

@author Shuvra S. Bhattacharyya, Christopher Hylands
@version $Id$
*/
public class MoMLToJava {


    /** Convert a MoML specification to Java code that implements the
     *  specification. The Java code is stored in a file called XXX.java,
     *  where XXX is the model name given in the MoML specification.
     *  If the model has no name, then the basename of the xml file is used.
     *
     *  @param filename The file name that contains the MoML specification.
     *  @return The name of the java file that was created
     */
    public String convert(String fileName) throws IllegalActionException {

        // The Ptolemy II model returned by the Java parser.
        NamedObj toplevel;

        // The Java code generated from the Ptolemy II model.
        String code;

        // The model representation returned from saving as Java
        String generatedCode;

        // Call the MOML parser on the test file to generate a Ptolemy II
        // model.
        try {
            MoMLParser parser = new MoMLParser();
            toplevel = parser.parseFile(fileName);
        } catch (Exception exception) {
            throw new IllegalActionException("MoMLtoJava failed to parse '" 
					     + fileName + "': " + exception);

        }

	// If the name of the model is the empty string, change it to
	// the basename of the file.
	if (toplevel.getName().length() == 0) {
	    String baseName = (new File(fileName)).getName();
	    if (baseName.lastIndexOf('.') != -1) {
		baseName = baseName.substring(0,
					      baseName.lastIndexOf('.'));
	    }
	    try {
		toplevel.setName(baseName);
	    } catch (NameDuplicationException nameDuplication) {
		throw new IllegalActionException("MoMLToJava could not change "
						 + "the model name to '" 
						 + baseName + "': " 
						 + nameDuplication
						 );
	    }
	}

        // Convert the Ptolemy II model to Java code.
	String outputFileName = null;
        try {
            SaveAsJava saver = new SaveAsJava();
            generatedCode = saver.generate(toplevel);
	    // We call the class we are generating CGFoo.java so that it
	    // will not collide with Foo.java.
	    outputFileName = "CG" + saver.sanitizeName(toplevel) + ".java";
        } catch (Exception ex) {
            throw new IllegalActionException(ex.getMessage()
                    + "Exception raised when attempting to generate Java\n");
        }

        // Write the Java text to a file.
        try {
            FileWriter outputFile = new FileWriter(outputFileName);
            PrintWriter outputPrinter = new PrintWriter(outputFile);
            outputPrinter.print(generatedCode);
            outputFile.close();
        } catch (IOException ex) {
            MessageHandler.error("Could not create output file:\n\n"
                    + ex.getMessage(), ex);
        }
	return outputFileName;
    }

    /** A simple main() to test the saving of Ptolemy II MoML models
     *  as Java source code. Exactly one argument is expected. This argument
     *  specifies the name of a MoML file. The equivalent Java code for
     *  the MoML file is saved in file CGXXX.java, where XXX is the model name
     *  specified in the MoML file.
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            throw new Exception("Usage: MoMLToJava file.xml");
        }

        MoMLToJava converter = new MoMLToJava();

	// Print the name of the java file that was generated
        System.out.println(converter.convert(args[0]));
    }
}
