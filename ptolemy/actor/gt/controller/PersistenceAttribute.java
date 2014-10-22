/* A marker attribute indicating that a design pattern should be persistent.

 Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.actor.gt.controller;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DesignPatternImporter

/**
  A marker attribute indicating that a design pattern should be persistent.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PersistenceAttribute extends SingletonAttribute {

    // This code fixes a problem where an imported pattern is marked
    // non-persistent, and the instances automatically inherits the
    // non-persistence, so those instances are not exported to MoML.

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PersistenceAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the parameter into the specified workspace.  The cloned
     *  parameter is marked as being persistent.
     *  @param workspace The workspace for the new object.
     *  @return A new parameter.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public NamedObj clone(Workspace workspace)
            throws CloneNotSupportedException {
        PersistenceAttribute object = (PersistenceAttribute) super
                .clone(workspace);
        object._persistence = true;
        return object;
    }

    /** Always return false, indicating that this attribute is not
     *  persistent.
     *  @return Always return false, indicating that this attribute is not
     *  persistent.
     */
    @Override
    public boolean isPersistent() {
        // FIXME: shouldn't this return the value of _persistance?
        // For example, if I create a clone, then isn't _persistance true?
        return false;
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  For non-toplevel
     *  containers, before setting the container, the persistence of
     *  the old container is set to the previous persistence value, the
     *  container is set to the new value and the previous persistence value
     *  is updated.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer;
        if (_oldPersistence != null && (oldContainer = getContainer()) != null) {
            oldContainer.setPersistent(_oldPersistence);
        }
        super.setContainer(container);
        if (container == null) {
            _oldPersistence = null;
        } else {
            _oldPersistence = container.isPersistent();
            container.setPersistent(_persistence);
        }
    }

    private Boolean _oldPersistence;

    private boolean _persistence = false;
}
