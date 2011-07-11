/* RTMaude Code generator helper for typed composite actor.

 Copyright (c) 2009-2011 The Regents of the University of California.
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
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IORelation;
import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 * Generate RTMaude code for a typed composite actor.
 *
 * @see ptolemy.actor.TypedCompositeActor
 * @author Kyungmin Bae
 * @since Ptolemy II 8.0
 * @version $Id$
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class TypedCompositeActor extends Entity {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated TypedCompositeActor.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor#getBlockCodeList(java.lang.String, java.lang.String[])
     */
    public List<String> getBlockCodeList(String blockName, String... args)
            throws IllegalActionException {
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        List self = super.getBlockCodeList(blockName, args);
        self.addAll(directorHelper.getBlockCodeList(blockName, args));

        return self;
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor#generateFireFunctionCode()
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        return super.generateFireFunctionCode() + _eol
                + directorHelper.generateFireFunctionCode();
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor#getModuleCode(java.lang.String)
     */
    public List<String> getModuleCode(String header)
            throws IllegalActionException {

        List<String> modNames = super.getModuleCode(header);

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            Entity helperObject = (Entity) _getHelper(actor);

            List<String> childModNames = helperObject.getModuleCode(header);

            for (String s : childModNames) {
                if (!getRTMmodule().keySet().contains(s)) {
                    modNames.add(s);
                }
            }

            getRTMmodule().putAll(helperObject.getRTMmodule());
        }
        return modNames;
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.Entity#getInfo(java.lang.String, java.util.List)
     */
    protected String getInfo(String name, List<String> parameters)
            throws IllegalActionException {
        ptolemy.actor.TypedCompositeActor c_actor = (ptolemy.actor.TypedCompositeActor) getComponent();

        // code for the actor, which is Maude term for the actor.
        // "entityList" method is used instead of "deepEntityList", because
        // the hierarchy of actor structure do *not* need to be flattened in the Real-time Maude
        if (name.equals("actors")) {
            return new ListTerm<Actor>("none", _eol,
                    c_actor.entityList(Actor.class)) {
                public String item(Actor v) throws IllegalActionException {
                    return ((RTMaudeAdaptor) _getHelper(v)).generateFireCode();
                }
            }.generateCode();
        }

        if (name.equals("connections")) {
            return new ListTerm<IORelation>("none", _eol,
                    c_actor.relationList()) {
                public String item(IORelation v) throws IllegalActionException {
                    return ((RTMaudeAdaptor) _getHelper(v)).generateTermCode();
                }
            }.generateCode();
        }
        return super.getInfo(name, parameters);
    }
}
