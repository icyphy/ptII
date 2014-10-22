/* A composite actor that serves as container of the contents of a configurable
 entity.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Configurer

/**
 A composite actor that serves as container of the contents of a configurable
 entity.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Configurer extends CompositeActor {

    /** Construct a configurer in the given workspace.
     *
     *  @param workspace The workspace.
     */
    public Configurer(Workspace workspace) {
        super(workspace);

        try {
            new ContainmentExtender(this, "_containmentExtender");
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Clone the actor into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a composite actor with clones of the ports of the
     *  original actor, the contained actors, and the contained relations.
     *  The ports of the returned actor are not connected to anything.
     *  The connections of the relations are duplicated in the new composite,
     *  unless they cross levels, in which case an exception is thrown.
     *  The local director is cloned, if there is one.
     *  The executive director is not cloned.
     *  NOTE: This will not work if there are level-crossing transitions.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the actor contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new CompositeActor.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Configurer newObject = (Configurer) super.clone(workspace);
        newObject._configured = null;
        return newObject;
    }

    /** Get the object that this configurer configures.
     *
     *  @return The object that this configurer configures.
     *  @see #setConfiguredObject(NamedObj)
     */
    public NamedObj getConfiguredObject() {
        return _configured;
    }

    /** Set the object that this configurer configures.
     *
     *  @param configured The object that this configurer configures.
     *  @see #getConfiguredObject()
     */
    public void setConfiguredObject(NamedObj configured) {
        _configured = configured;
    }

    ///////////////////////////////////////////////////////////////////
    //// ContainmentExtender

    /**
     The containment extender that returns the configured object as the
     container of this configurer.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class ContainmentExtender extends Attribute implements
            ptolemy.data.expr.ContainmentExtender {

        /** Construct a containment extender.
         *
         *  @param container The container.
         *  @param name The name of this attribute.
         *  @exception IllegalActionException If the attribute is not of an
         *   acceptable class for the container, or if the name contains a
         *   period.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public ContainmentExtender(Configurer container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Get an object with the given name within the container.
         *
         *  @param name The name of the object.
         *  @return The object, or null if not found.
         *  @exception IllegalActionException If exception occurs when trying to get
         *   the contained object.
         */
        @Override
        public NamedObj getContainedObject(String name)
                throws IllegalActionException {
            return ((Configurer) getContainer()).getEntity(name);
        }

        /** Get the extended container.
         *
         *  @return The container.
         *  @exception IllegalActionException If exception occurs when trying to get
         *   the container.
         */
        @Override
        public NamedObj getExtendedContainer() throws IllegalActionException {
            return ((Configurer) getContainer()).getConfiguredObject();
        }

    }

    // The object that this configurer configures.
    private NamedObj _configured;
}
