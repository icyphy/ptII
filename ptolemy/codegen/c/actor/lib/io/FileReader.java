/*
 @Copyright (c) 2006 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.codegen.c.actor.lib.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.util.IllegalActionException;


/**
 * A helper class for ptolemy.actor.lib.io.FileReader.
 *
 * @author Jackie
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class FileReader extends CCodeGeneratorHelper {
    /**
     * Constructor method for the LineReader helper.
     * @param actor the associated actor.
     */
    public FileReader(ptolemy.actor.lib.io.FileReader actor) {
        super(actor);
    }

    /**
     * Generate initialization code.
     * Get the file path from the actor's fileOrURL parameter. Read the
     * <code>initBlock</code> from AudioReader.c and pass the file path
     * string as an argument to code block. Replace macros with their values
     * and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the file path parameter is invalid
     *  or the code stream encounters an error in processing the specified code
     *  block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.io.FileReader actor = (ptolemy.actor.lib.io.FileReader) getComponent();
        String fileNameString = getFileName(actor.fileOrURL);
        ArrayList args = new ArrayList();
        args.add(fileNameString);
        _codeStream.appendCodeBlock("openForRead", args);
        return processCode(_codeStream.toString());
    }

    /** Get the file name from a parameter and convert backward slashes
     *  to forward slashes.
     *  @param fileOrURL The file name or URL.
     *  @return a pathname suitable for use with C: no backslashes,
     *  "C:/foo/bar", not "/C:/foo/bar"
     *  @exception If the file cannot be found.
     */
    public static String getFileName(FileParameter fileOrURL) 
            throws IllegalActionException {
                String fileNameString;
        try {
            // Handle $CLASSPATH, return a file name with forward slashes.
            fileNameString = fileOrURL.asURL().getPath();
            // Under Windows, convert /C:/foo/bar to C:/foo/bar
            fileNameString = new File(fileNameString).getCanonicalPath()
                    .replace('\\', '/');
        } catch (IOException e) {
            throw new IllegalActionException("Cannot find file: "
                    + fileOrURL.getExpression());
        }
        return fileNameString;
    }
}
