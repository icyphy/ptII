/* Code generator helper for typed composite actor.

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
package ptolemy.codegen.actor;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator helper for typed composite actor.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends CodeGeneratorHelper {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        code.append(directorHelper.generateFireCode());

        return processCode(code.toString());
    }

    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        code.append(directorHelper.generatePreinitializeCode());

        return processCode(code.toString());
    }

    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        code.append(directorHelper.generateInitializeCode());

        return processCode(code.toString());
    }

    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        code.append(directorHelper.generateWrapupCode());

        return processCode(code.toString());
    }

    public Set getSharedCode() throws IllegalActionException {

        // Use LinkedHashSet to give order to the shared code.
        Set sharedCode = new LinkedHashSet();
        sharedCode.addAll(super.getSharedCode());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            sharedCode.addAll(helperObject.getSharedCode());
        }

        // Get shared code used by the director helper.
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        sharedCode.addAll(directorHelper.getSharedCode());

        return sharedCode;
    }
}
