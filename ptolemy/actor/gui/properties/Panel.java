/* A GUI property that encloses a JPanel component.

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
package ptolemy.actor.gui.properties;

import javax.swing.JComponent;
import javax.swing.JPanel;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Panel

/**
 A GUI property that encloses a JPanel component.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Panel extends GUIProperty {

    /** Construct a GUI property with the given name contained by the specified
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
    public Panel(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a GUI property with the given name contained by the specified
     *  entity with the given Java Swing component. The container argument must
     *  not be null, or a NullPointerException will be thrown.  This attribute
     *  will use the workspace of the container for synchronization and version
     *  counts. If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param component The Java Swing component.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Panel(NamedObj container, String name, JComponent component)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, component);
    }

    /** Construct a GUI property with the given name contained by the specified
     *  entity with the given Java Swing component and the given layout
     *  constraint. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute
     *  will use the workspace of the container for synchronization and version
     *  counts. If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param component The Java Swing component.
     *  @param constraint The layout constraint.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Panel(NamedObj container, String name, JComponent component,
            Object constraint) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, component, constraint);
    }

    /** Construct a GUI property with the given name contained by the specified
     *  entity with the given layout
     *  constraint. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute
     *  will use the workspace of the container for synchronization and version
     *  counts. If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param constraint The layout constraint.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Panel(NamedObj container, String name, Object constraint)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, constraint);
    }

    /** Create a new JPanel component.
     *
     *  @return A Swing component that can be enclosed in this GUI property.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected JComponent _createComponent() throws IllegalActionException {
        return new JPanel();
    }

}
