/* A tool for alphabetizing the code block files under
   $CLASSPATH/ptolemy/codegen/java/kernel/type/polymorphic/.

 Copyright (c) 2005-2009 The Regents of the University of California.
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

package ptolemy.codegen.java.kernel.type.polymorphic;

import java.io.File;
import java.io.FileWriter;
import java.util.TreeSet;

import ptolemy.codegen.kernel.CodeStream;
import ptolemy.util.FileUtilities;

/**
   A utility class for alphabetizing code blocks in a code template file.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)

*/
public class AlphabetizeOperation {

    /** Alphabetize all the blocks in the files in
     *   $CLASSPATH/ptolemy/codegen/java/kernel/type/polymorphic/.
     * @param args Not used.
     */
    public static void main(String[] args) {
        // for every file in type/polymorphic/
        try {
            File directory = new File(FileUtilities.nameToURL(
                    "$CLASSPATH/ptolemy/codegen/java/kernel/type/polymorphic",
                    null, null).getFile());
            for (File file : directory.listFiles()) {
                String filename = file.getPath();
                CodeStream stream = new CodeStream(filename, null);

                TreeSet sortedSet = new TreeSet(
                        stream.getAllCodeBlockSignatures());

                StringBuffer code = new StringBuffer();
                for (Object signature : sortedSet) {
                    code.append(stream.getCodeBlockTemplate(signature));
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
