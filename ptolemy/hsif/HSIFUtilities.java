/* Utilities that operate on HSIF files

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.hsif;

import ptolemy.util.XSLTUtilities;

import java.util.LinkedList;

import org.w3c.dom.Document;

//////////////////////////////////////////////////////////////////////////
//// HSIFUtilities
/** Utilities methods for operating on HSIF files.  These methods
are in a separate non-graphical class so that we can test them
as part of the nightly build, or provide non-graphical tools
that use these methods 


@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.2

*/
public class HSIFUtilities {
    /** Instances of this class cannot be created.
     */
    private HSIFUtilities() {
    }

    /**  Read in an HSIF file ,transform it into MoML and generate an output
     *	 file.
     *   @param input HSIF file to be read in
     *   @param output MoML filename to be generated.
     *   @throws Exception if there is a problem with the transformation.
     */
    public static void HSIFToMoML(URL input, String output) throws Exception {
        Document inputDocument = XSLTUtilities.parse(input.toString());

        List transforms = new LinkedList();

	// The transform() method will look in the classpath.
        transforms.add("ptolemy/hsif/xsl/GlobalVariablePreprocessor.xsl");
        transforms.add("ptolemy/hsif/xsl/SlimPreprocessor.xsl");
        transforms.add("ptolemy/hsif/xsl/LocalVariablePreprocessor.xsl");
        Document outputDocument =
            XSLTUtilities.transform(inputDocument, transforms);

	String outputString = 

	
	FileWriter fileWriter = new FileWriter(output);
	fileWriter.write(XSLTUtilities.toString(outputDocument));
	fileWriter.close();
    }
}
