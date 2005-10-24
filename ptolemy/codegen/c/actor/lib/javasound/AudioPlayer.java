/* A helper class for actor.lib.javasound.AudioPlayer

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

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.javasound.AudioPlayer.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Yellow (mankit)
 * @Pt.AcceptedRating Yellow (mankit)
 */
public class AudioPlayer extends CCodeGeneratorHelper {
    /**
     * Constructor method for the AudioPlayer helper.
     * @param actor the associated actor.
     */
    public AudioPlayer(ptolemy.actor.lib.javasound.AudioPlayer actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Check the bitsPerSample parameter of the actor. If bitsPerSample is
     * equals to 8, then read the <code>fireBlock_8</code>, else read the
     * <code>fireBlock_16</code> from AudioPlayer.c, replace macros with
     * their values and append to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        super.generateFireCode(code);

        ptolemy.actor.lib.javasound.AudioPlayer actor = (ptolemy.actor.lib.javasound.AudioPlayer) getComponent();

        _codeStream.clear();

        if (Integer.parseInt(actor.bitsPerSample.getExpression()) == 8) {
            _codeStream.appendCodeBlock("fireBlock_8");
        } else { // assume bitsPerSample == 16 
            _codeStream.appendCodeBlock("fireBlock_16");
        }

        code.append(processCode(_codeStream.toString()));
    }

    /**
     * Generate initialization code.
     * Read the <code>initBlock</code> from AudioPlayer.c, replace
     * macros with their values and return the processed code string.
     * @return The processed code block.
     * @exception IllegalActionException If the code stream encounters
     * an error in processing the specified code block.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();
        return processCode(_generateBlockCode("initBlock"));
    }

    /**
     * Generate preinitialization code.
     * Check the bitsPerSample parameter of the actor. If bitsPerSample is
     * equals to 8, then read the <code>preinitBlock_8</code>, else read the
     * <code>preinitBlock_16</code> from AudioPlayer.c, replace macros with
     * their values and return the processed code string.
     * @return The processed code block.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.javasound.AudioPlayer actor = (ptolemy.actor.lib.javasound.AudioPlayer) getComponent();

        _codeStream.clear();

        if (Integer.parseInt(actor.bitsPerSample.getExpression()) == 8) {
            _codeStream.appendCodeBlock("preinitBlock_8");
        } else { // assume bitsPerSample == 16 
            _codeStream.appendCodeBlock("preinitBlock_16");
        }

        return processCode(_codeStream.toString());
    }

    /**
     * Generate shared code.
     * Read the <code>sharedBlock</code> from AudioPlayer.c,
     * replaces macros with their values and appends the processed code
     * block to the given code buffer.
     * @return A set of strings that are code shared by multiple instances of
     *  the same actor.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set generateSharedCode() throws IllegalActionException {
        super.generateSharedCode();

        // We don't need to process the code block here because the
        // sharedCode do not contain any macros.
        Set codeBlocks = new HashSet();
        codeBlocks.add(_generateBlockCode("sharedBlock", false));
        return codeBlocks;
    }

    /**
     * Generate wrap up code.
     * Read the <code>wrapupBlock</code> from AudioPlayer.c,
     * replace macros with their values and append the processed code block
     * to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        super.generateWrapupCode();
        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * AudioPlayer actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the AudioPlayer actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        super.getHeaderFiles();

        Set files = new HashSet();
        files.add("<stdio.h>");
        files.add("<math.h>");
        files.add("\"SDL.h\"");
        files.add("\"SDL_audio.h\"");
        files.add("\"SDL_thread.h\"");
        return files;
    }
}
