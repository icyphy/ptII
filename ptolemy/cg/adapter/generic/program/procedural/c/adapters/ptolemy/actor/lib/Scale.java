/* A adapter class for ptolemy.actor.lib.Scale

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Scale

/**
 A adapter class for ptolemy.actor.lib.Scale.

 @author Bert Rodiers, William Lucas
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */
public class Scale
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.Scale {

    /**
     *  Construct a Scale adapter.
     *  @param actor The given ptolemy.actor.lib.Scale actor.
     */
    public Scale(ptolemy.actor.lib.Scale actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate the declaration of the shared code.  If the Scale_scaleOnLeft() or
     * Scale_scaleOnRight methods are needed, include them in the shared
     * section
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Set<String> getDeclareSharedCode() throws IllegalActionException {
        Set<String> sharedCode = new HashSet<String>();
        CodeStream codestream = _templateParser.getCodeStream();
        codestream.clear();
        if (_needScaleMethods) {
            codestream.appendCodeBlocks("Scale_declareScaleOn.*");
            if (!codestream.isEmpty()) {
                sharedCode.add(_templateParser.processCode(codestream
                        .toString()));
            }
        }
        return sharedCode;
    }
}
