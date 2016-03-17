/* Code generator adapter for generating JS code for Director.

 Copyright (c) 2005-2016 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.js.adapters.ptolemy.actor;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.js.JSCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////
//// Director

/**
 Code generator adapter for generating JS code for Director.

 @see GenericCodeGenerator
 @author Christopher Brooks, base on the html Director by Man-Kit Leung, Bert Rodiers
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Director extends JSCodeGeneratorAdapter {

    /** Construct the code generator adapter associated with the given director.
     *  Note before calling the generate*() methods, you must also call
     *  setCodeGenerator(GenericCodeGenerator).
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                Public Methods                           ////

    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the adapters for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @return The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating fire code for the actor.
     */
    @Override
    public String generateJS() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> actors = ((CompositeActor) getComponent().getContainer())
                .deepEntityList().iterator();

        code.append("// Start: " + getComponent().getName() + "ptolemy/cg/adapter/generic/js/adapters/ptolemy/actor/Director.java" + _eol);

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            JSCodeGeneratorAdapter adapter = null;
            Object object = getCodeGenerator().getAdapter(actor);
            try {
                adapter = (JSCodeGeneratorAdapter) object;
            } catch (ClassCastException ex) {
                throw new IllegalActionException(getComponent(), ex,
                        "Failed to cast " + object + " of class "
                                + object.getClass().getName() + " to "
                                + JSCodeGeneratorAdapter.class.getName()
                                + ".");

            }
            code.append(adapter.generateJS());
        }
        code.append("// End: " + getComponent().getName() + "ptolemy/cg/adapter/generic/js/adapters/ptolemy/actor/Director.java" + _eol);

        return code.toString();
    }
}
