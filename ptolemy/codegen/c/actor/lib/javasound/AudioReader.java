/*
 @Copyright (c) 2005 The Regents of the University of California.
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;

/**
 * A helper class for ptolemy.actor.lib.javasound.AudioReader.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class AudioReader extends CCodeGeneratorHelper {

    /**
     * Constructor method for the AudioReader helper.
     * @param actor the associated actor.
     */
    public AudioReader(ptolemy.actor.lib.javasound.AudioReader actor) {
        super(actor);
    }
    

    /**
     * Generate fire code.
     * The method reads in <code>writeSoundFile</code> from AudioReader.c 
     * and puts into the given stream buffer.
     * @param stream the given buffer to append the code to.
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("readSoundFile");

        stream.append(processCode(tmpStream.toString()));
    }
    
    /** Generate initialization code.
     *  This method reads the <code>initBlock</code> from AudioReader.c,
     *  replaces macros with their values and returns the results.
     *  @return The processed <code>initBlock</code>.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();
        CodeStream tmpStream = new CodeStream(this);
        ptolemy.actor.lib.javasound.AudioReader actor = 
            (ptolemy.actor.lib.javasound.AudioReader) getComponent();
        String fileNameString;

        try {
            fileNameString = FileUtilities.nameToFile(actor.fileOrURL.
                    getExpression(), null).getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalActionException("Cannot open file: "
                    + actor.fileOrURL.getExpression());
        }
        tmpStream.appendCodeBlock("initBlock", fileNameString);
        return processCode(tmpStream.toString());
    }

    /**
     * Generate preinitialization code.
     * This method reads the <code>preinitBlock</code> from helperName.c,
     * replaces macros with their values and returns the results.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block.
     * @return The processed <code>preinitBlock</code>.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("preinitBlock");
        return processCode(tmpStream.toString());
    }

    /** Generate wrap up code.
     *  This method reads the <code>wrapupBlock</code> from helperName.c,
     *  replaces macros with their values and put the processed code block
     *  into the given stream buffer.
     * @param stream the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block.
     */
    public void generateWrapupCode(StringBuffer stream)
            throws IllegalActionException {
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("wrapupBlock");
        stream.append(processCode(tmpStream.toString()));
    }
    /** Get the files needed by the code generated for the
     *  AudioReader actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the AudioReader actor.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        files.add("\"math.h\"");
        files.add("\"stdio.h\"");
        return files;
    }
}
