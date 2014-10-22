/* A tool for alphabetizing the code block files.

 Copyright (c) 2005-2014 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program.procedural.java.type;

import java.io.File;
import java.io.FileWriter;
import java.util.TreeSet;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.CodeStream.Signature;
import ptolemy.util.FileUtilities;

/**
A utility class for alphabetizing code blocks in a code template file.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */
public class AlphabetizeOperation {

    /**
     * Alphabetize code block (template) files
     * Parse code blocks from each file, sort and write them
     * back in-place.  Usage:
     * <pre>
     * java -classpath $PTII ptolemy.cg.kernel.generic.program.procedural.java.type.AlphabetizeOperation $PTII/ptolemy/cg/kernel/generic/program/procedural/java/type/polymorphic
     * </pre>
     * @param args The first argument should be the path to the directory
     * that contains the templates to be alphabetized.
     */
    public static void main(String[] args) {
        try {
            File directory = new File(FileUtilities.nameToURL(args[0], null,
                    null).getFile());

            // Iterate through every file in type/polymorphic/
            for (File file : directory.listFiles()) {
                String filename = file.getPath();
                CodeStream stream = new CodeStream(filename, null);

                TreeSet<Signature> sortedSet = new TreeSet<Signature>(
                        stream.getAllCodeBlockSignatures());

                StringBuffer code = new StringBuffer();
                for (Signature signature : sortedSet) {
                    String templateCode = stream
                            .getCodeBlockTemplate(signature);

                    String functionHeader = templateCode.split("\n")[1];
                    String[] fragments = functionHeader
                            .split(signature.functionName);

                    // The templateCode should contain at least two occurrences of
                    // the functionName. One for the code block header, and one for
                    // the function definition. If not, that means something is
                    // mis-typed, and will create compile bugs in code generation.
                    if (fragments.length <= 1) {
                        System.err.println("Warning -- " + signature
                                + " does not" + " contains the definition for "
                                + signature.functionName);
                    }
                    code.append(templateCode);

                }

                if (code.toString().trim().length() > 0) {
                    FileWriter writer = new FileWriter(new File(filename));
                    writer.write(code.toString());
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
