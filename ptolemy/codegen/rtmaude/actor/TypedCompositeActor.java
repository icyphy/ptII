/* RTMaude Code generator helper for typed composite actor.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.actor;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IORelation;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 * Generate RTMaude code for a typed composite actor.
 *
 * @see ptolemy.actor.TypedCompositeActor
 * @author Kyungmin Bae
@version $Id$
@since Ptolemy II 7.1
 * @version $Id$
 * @Pt.ProposedRating Red (kquine)
 *
 */
public class TypedCompositeActor extends ptolemy.codegen.rtmaude.kernel.Entity {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    @Override
    protected String _generateInfoCode(String name, List<String> parameters)
            throws IllegalActionException {
        ptolemy.actor.TypedCompositeActor c_actor = (ptolemy.actor.TypedCompositeActor) getComponent();

        // code for the actor, which is Maude term for the actor.
        // "entityList" method is used instead of "deepEntityList", because
        // the hierarchy of actor structure do *not* need to be flattened in the Real-time Maude
        if (name.equals("actors"))
            return new ListTerm<Actor>("none", _eol, c_actor.entityList(Actor.class)) {
                    public String item(Actor v) throws IllegalActionException {
                        return ((RTMaudeAdaptor) _getHelper(v)).generateFireCode();
                    }
                }.generateCode();

        if (name.equals("connections"))
            return new ListTerm<IORelation>("none", _eol, c_actor.relationList()) {
                    public String item(IORelation v) throws IllegalActionException {
                        return ((RTMaudeAdaptor) _getHelper(v)).generateTermCode();
                    }
                }.generateCode();
        return super._generateInfoCode(name, parameters);
    }

    @Override
    public List<String> getBlockCodeList(String blockName, String ... args)
            throws IllegalActionException {
        Director directorHelper = (Director) _getHelper(((ptolemy.actor
                .CompositeActor) getComponent()).getDirector());

        List self = super.getBlockCodeList(blockName, args);
        self.addAll(directorHelper.getBlockCodeList(blockName, args));

        return self;
    }

    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        Director directorHelper = (Director) _getHelper(((ptolemy.actor
                .CompositeActor) getComponent()).getDirector());

        return super.generateFireFunctionCode() +
            _eol + directorHelper.generateFireFunctionCode();
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
