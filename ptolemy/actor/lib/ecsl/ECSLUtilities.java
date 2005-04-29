/* Utilities that operate on ECSL files

Copyright (c) 2004-2005 The Regents of the University of California.
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

*/
package ptolemy.actor.lib.ecsl;

import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import ptolemy.util.XSLTUtilities;


//////////////////////////////////////////////////////////////////////////
//// ECSLUtilities

/** Utilities methods for operating on ECSL files.  These methods
    are in a separate non-graphical class so that we can test them
    as part of the nightly build, or provide non-graphical tools
    that use these methods

    @author Christopher Brooks
    @version $Id$
    @since Ptolemy II 4.1
    @Pt.ProposedRating Red (eal)
    @Pt.AcceptedRating Red (cxh)
*/
public class ECSLUtilities {
    /** Instances of this class cannot be created.
     */
    private ECSLUtilities() {
    }

    /** Read in an ECSL file, transform it into MoML and write the output
     *  to a FileWriter.
     *  @param input ECSL file to be read in.
     *  @param fileWriter A FileWriter that will write to the MoML
     *  file.
     *  @exception Exception If there is a problem with the transformation.
     */
    public static void ECSLToMoML(String input, FileWriter fileWriter)
            throws Exception {
        // This method takes a FileWriter so that the user can
        // ensure that the FileWriter exists and is writable before going
        // through the trouble of doing the conversion.
        List transforms = new LinkedList();

        // The transform() method will look in the classpath.
        transforms.add("ptolemy/actor/lib/ecsl/xsl/ecsl.xsl");

        XSLTUtilities.transform(input, fileWriter, transforms);

        // Let the caller close the fileWriter.
        //fileWriter.close();
    }

    /** Read in an ECSL file, transform it into MoML and generate an output
     *  file.  Note that if the output file exists, then it is overwritten.
     *  @param input ECSL file to be read in
     *  @param output The MoMLFile to be generated.
     *  @exception Exception If there is a problem with the transformation.
     */
    public static void ECSLToMoML(String input, String output)
            throws Exception {
        // This method makes it much easier to test the conversion,
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(output);
            ECSLToMoML(input, fileWriter);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    /** Convert the first argument from a ECSL file into a MoML file
     *  named by the second argument.
     *  For example
     *  <pre>
     *  java -classpath $PTII ptolemy.actor.lib.ecsl.ECSLUtilities \
     *       $PTII/ptolemy/actor/lib/ecsl/demo/cruiseControl/cruiseControlECSL.xml   *       /tmp/cruiseControlECSL.moml
     *  </pre>
     *  will read in cruseControlECSL.xml and create CruiseControlECSL.moml
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.actor.lib.ecsl.ECSLUtilities ECSLInputFile "
                    + "MoMLOutputFile");
            System.exit(2);
        } else {
            ECSLToMoML(args[0], args[1]);
        }
    }
}
