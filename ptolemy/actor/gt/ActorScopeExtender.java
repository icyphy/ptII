/* A scope extender for CompositeActorMatcher that resolves entities inside.

 Copyright (c) 2007-2008 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** A scope extender for CompositeActorMatcher that resolves entities inside.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ActorScopeExtender extends ScopeExtendingAttribute {

    /** Construct a scope extender.
     * 
     *  @param container The CompositeActor or CompositeActorMatcher that
     *   contains this scope extender.
     *  @param name The name of the scope extender.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ActorScopeExtender(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of all entities contained in the container, wrapping each
     *  entity in a {@link NamedObjVariable}.
     * 
     *  @return A list of all entities.
     */
    public List<?> attributeList() {
        long version = workspace().getVersion();
        if (_attributeList == null || version > _version) {
            _attributeList = new LinkedList<Object>((List<?>) super
                    .attributeList());
            NamedObj scope = getContainer();
            Collection<?> children = GTTools.getChildren(scope, true, true,
                    true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                if (child instanceof ActorScopeExtender) {
                    continue;
                }

                try {
                    Variable variable = NamedObjVariable.getNamedObjVariable(
                            child, true);
                    if (variable != null) {
                        _attributeList.add(variable);
                    }
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
            }
            _version = version;
        }
        return Collections.unmodifiableList(_attributeList);
    }

    /** Return an entity in the container with the given name, wrapped in a
     * {@link NamedObjVariable}.
     * 
     *  @param name The name of the entity.
     *  @return The entity wrapped in a {@link NamedObjVariable}.
     */
    public Attribute getAttribute(String name) {
        NamedObj scope = getContainer();
        NamedObj child = GTTools.getChild(scope, name, true, true, true, true);
        if (child instanceof ActorScopeExtender) {
            child = GTTools.getChild(scope, name, false, true, true, true);
        }
        if (child == null) {
            return super.getAttribute(name);
        }

        try {
            Variable actorVariable = NamedObjVariable.getNamedObjVariable(
                    child, true);
            return actorVariable;
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Cache of the list of entities in the container.
     */
    private List<Object> _attributeList;

    /** The workspace version the last time when _attributeList was updated.
     */
    private long _version = -1;

}
