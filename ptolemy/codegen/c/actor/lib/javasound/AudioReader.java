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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

/**
 * A helper class for ptolemy.actor.lib.javasound.AudioReader.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Yellow (mankit)
 * @Pt.AcceptedRating Yellow (mankit)
 */
public class AudioReader extends CCodeGeneratorHelper {
    /**
     * Constructor method for the AudioReader helper.
     * @param actor the associated actor.
     */
    public AudioReader(ptolemy.actor.lib.javasound.AudioReader actor) {
        super(actor);
    }

    
    /** Add the necessary include and library directives to the makefile.
     *  @param codeGenerator The code generator to which the include and
     *  library directives are added.
     */ 
    public static void addIncludesAndLibraries(CodeGenerator codeGenerator) {
        // This method is static so that all the Audio* actors can call it 
        codeGenerator.addInclude("-I/usr/local/include/SDL");

        if (StringUtilities.getProperty("os.name").equals("SunOS")) { 
            codeGenerator.addLibrary(" -Wl,-Bstatic -D_REENTRANT "
                    + "-R/usr/local/lib -lSDL -Wl,-Bdynamic"
                    + "-lpthread -lposix4 -lm -L/usr/openwin/lib "
                    + "-R/usr/openwin/lib -lX11 -lXext");
        } else {
            codeGenerator.addLibrary("-L/usr/local/lib -lsdl");
        }
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

        ptolemy.actor.lib.javasound.AudioReader actor = (ptolemy.actor.lib.javasound.AudioReader) getComponent();
        String fileNameString;
        try {
            // Handle $CLASSPATH, return a file name with forward slashes.
            fileNameString = actor.fileOrURL.asURL().getPath();
            // Under Windows, convert /C:/foo/bar to C:/foo/bar
            fileNameString = new File(fileNameString).getCanonicalPath()
                    .replace('\\', '/');
        } catch (IOException e) {
            throw new IllegalActionException("Cannot find file: "
                    + actor.fileOrURL.getExpression());
        }

        ArrayList args = new ArrayList();
        args.add(fileNameString);
        _codeStream.appendCodeBlock("initBlock", args);
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * AudioReader actor.
     * @return A set of Strings that are names of the files
     *  needed by the code generated for the AudioReader actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        super.getHeaderFiles();
        Set files = new HashSet();
        files.add("<math.h>");
        files.add("<stdio.h>");
        files.add("\"SDL.h\"");
        files.add("\"SDL_audio.h\"");
        return files;
    }
}
