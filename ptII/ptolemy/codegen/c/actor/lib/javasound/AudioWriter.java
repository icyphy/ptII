/* A helper class for actor.lib.javasound.AudioWriter

 @Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib.javasound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.javasound.AudioWriter.
 *
 * @author Man-Kit Leung, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Yellow (cxh)
 */
public class AudioWriter extends CCodeGeneratorHelper {
    /**
     * Construct an AudioWriter helper.
     * @param actor the associated actor.
     */
    public AudioWriter(ptolemy.actor.lib.javasound.AudioWriter actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>writeSoundFile</code> from AudioWriter.c,
     * Replace macros with their values and return the processed code block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());
        code.append(_generateBlockCode("writeSoundFile"));
        return code.toString();
    }

    /**
     * Generate initialization code.
     * Get the file path from the actor's fileOrURL parameter. Read the
     * <code>initBlock</code> from AudioWriter.c and pass the file path
     * string as an argument to code block. Replace macros with their values
     * and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the file path parameter is invalid
     *  or the code stream encounters an error in processing the specified code
     *  block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.javasound.AudioWriter actor = (ptolemy.actor.lib.javasound.AudioWriter) getComponent();
        String fileNameString;
        try {
            // Handle $CLASSPATH, return a file name with forward slashes.
            fileNameString = actor.pathName.getExpression();
            // Under Windows, convert /C:/foo/bar to C:/foo/bar
            fileNameString = new File(fileNameString).getCanonicalPath()
                    .replace('\\', '/');
        } catch (IOException e) {
            throw new IllegalActionException("Cannot find file: "
                    + actor.pathName.getExpression());
        }

        ArrayList args = new ArrayList();
        args.add(fileNameString);
        _codeStream.appendCodeBlock("initBlock", args);
        return processCode(_codeStream.toString());
    }

    /** Get the files needed by the code generated for the
     *  AudioWriter actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the AudioWriter actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        files.add("<stdio.h>");
        return files;
    }

}
