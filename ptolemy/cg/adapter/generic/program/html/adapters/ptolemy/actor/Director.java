/* Code generator adapter for generating HTML code for Director.

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
package ptolemy.cg.adapter.generic.program.html.adapters.ptolemy.actor;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


////Director

/**
 Code generator adapter for generating HTML code for Director.

 @see GenericCodeGenerator
 @author Man-Kit Leung, Bert Rodiers
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)

 */
public class Director extends 
ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director {
    
    /** Construct the code generator adapter associated with the given director.
     *  Note before calling the generate*() methods, you must also call
     *  setCodeGenerator(GenericCodeGenerator).
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        super(director);
        _director = director;
    }

    /////////////////////////////////////////////////////////////////
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
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment("The firing of the director."));

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();
        
        code.append("<li>" + _director.getName() + "</li>" + _eol);        

        while (actors.hasNext()) {
            code.append("<li>");        
            Actor actor = (Actor) actors.next();
            CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter((NamedObj) actor);
            code.append(adapter.generateFireCode());
            code.append("</li>");        
        }
        return code.toString();
    }
}
