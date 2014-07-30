/* Code generator adapter for typed composite actor.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.adapters.ptolemy.actor;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator adapter for typed composite actor.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends NamedProgramCodeGeneratorAdapter {
    /** Construct the code generator adapter associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    @Override
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super._generateFireCode());

        NamedProgramCodeGeneratorAdapter directorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.actor.CompositeActor) getComponent())
                        .getDirector());

        code.append(directorAdapter.generateFireCode());

        return processCode(code.toString());
    }

    /** Generate the preinitialize code of the associated composite actor.
     *  It first creates buffer size and offset map for its input ports and
     *  output ports. It then gets the result of generatePreinitializeCode()
     *  method of the local director adapter.
     *
     *  @return The preinitialize code of the associated composite actor.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating preinitialize code for the actor
     *   or while creating buffer size and offset map.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        NamedProgramCodeGeneratorAdapter directorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.actor.CompositeActor) getComponent())
                        .getDirector());

        code.append(directorAdapter.generatePreinitializeCode());

        return processCode(code.toString());
    }

    /** Generate the preinitialize code of the associated composite actor.
     *  It first creates buffer size and offset map for its input ports and
     *  output ports. It then gets the result of generatePreinitializeCode()
     *  method of the local director adapter.
     *
     *  @return The preinitialize code of the associated composite actor.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating preinitialize code for the actor
     *   or while creating buffer size and offset map.
     */
    @Override
    public String generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeMethodBodyCode());

        NamedProgramCodeGeneratorAdapter directorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.actor.CompositeActor) getComponent())
                        .getDirector());

        code.append(directorAdapter.generatePreinitializeMethodBodyCode());

        return processCode(code.toString());
    }

    /** Generate the initialize code for this director.
     *  The initialize code for the director is generated by appending the
     *  initialize code for each actor.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        NamedProgramCodeGeneratorAdapter directorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.actor.CompositeActor) getComponent())
                        .getDirector());

        code.append(directorAdapter.generateInitializeCode());

        return processCode(code.toString());
    }

    @Override
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        NamedProgramCodeGeneratorAdapter directorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.actor.CompositeActor) getComponent())
                        .getDirector());

        code.append(directorAdapter.generateWrapupCode());

        return processCode(code.toString());
    }

    @Override
    public Set<String> getSharedCode() throws IllegalActionException {

        // Use LinkedHashSet to give order to the shared code.
        Set<String> sharedCode = new LinkedHashSet<String>();
        sharedCode.addAll(super.getSharedCode());

        Iterator<?> actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            sharedCode.addAll(adapterObject.getSharedCode());
        }

        // Get shared code used by the director adapter.
        NamedProgramCodeGeneratorAdapter directorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.actor.CompositeActor) getComponent())
                        .getDirector());
        sharedCode.addAll(directorAdapter.getSharedCode());

        return sharedCode;
    }
}
