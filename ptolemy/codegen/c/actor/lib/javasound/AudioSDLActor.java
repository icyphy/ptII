/* Base class for C Code Generation Audio Actors that use SDL.

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
package ptolemy.codegen.c.actor.lib.javasound;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// AudioSDLActor

/**
 * Base class for C codegen audio actors that use SDL. 
 * SDL can be found at <a href="http://www.libsdl.org">http://www.libsdl.org</a>.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */

public class AudioSDLActor extends CCodeGeneratorHelper {
    /**
     * Construct an AudioSDLActor. 
     * @param actor the associated actor.
     */
    public AudioSDLActor(TypedAtomicActor actor) {
        super(actor);
    }

    /**
     * Get the files needed by the code generated for the
     * AudioSDLActor. 
     * @return A set of Strings that are names of the files
     *  needed by the code generated for the actor that uses SDL.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();

        _codeGenerator.addInclude("-I/usr/local/include/SDL");

        if (StringUtilities.getProperty("os.name").equals("SunOS")) { 
            _codeGenerator.addLibrary(" -Wl,-Bstatic -D_REENTRANT "
                    + "-R/usr/local/lib -lSDL -Wl,-Bdynamic "
                    + "-lpthread -lposix4 -lm -L/usr/openwin/lib "
                    + "-R/usr/openwin/lib -lX11 -lXext");
        } else {
            _codeGenerator.addLibrary("-L/usr/local/lib -lsdl");
        }

        files.add("<stdio.h>");
        files.add("<math.h>");
        files.add("\"SDL.h\"");
        files.add("\"SDL_audio.h\"");
        files.add("\"SDL_thread.h\"");
        return files;
    }
}
