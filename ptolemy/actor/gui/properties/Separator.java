/* A GUI property that encloses a JSeparator component.

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
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Separator

/**
 A GUI property that encloses a JSeparator component.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Separator extends GUIProperty {

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
    public Separator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
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
    public Separator(NamedObj container, String name, JComponent component)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, component);
        _init();
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
    public Separator(NamedObj container, String name, JComponent component,
            Object constraint) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, component, constraint);
        _init();
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
    public Separator(NamedObj container, String name, Object constraint)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, constraint);
        _init();
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes. If the changed attribute
     *  is {@link #orientation}, then the orientation of the JSeparator
     *  component in this GUI property is adjusted accordingly.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == orientation) {
            String value = orientation.stringValue();
            if (value.equalsIgnoreCase("Horizontal")) {
                ((JSeparator) getComponent())
                        .setOrientation(SwingConstants.HORIZONTAL);
            } else if (value.equalsIgnoreCase("Vertical")) {
                ((JSeparator) getComponent())
                        .setOrientation(SwingConstants.VERTICAL);
            } else {
                throw new IllegalActionException(this, "Orientation of a "
                        + "separator must be either \"Horozontal\" or "
                        + "\"Vertical\".");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** The orientation of the JSeparator, which should be either "Horizontal"
     *  or "Vertical".
     */
    public StringParameter orientation;

    /** Create a new JSeparator component.
     *
     *  @return A Swing component that can be enclosed in this GUI property.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected JComponent _createComponent() throws IllegalActionException {
        return new JSeparator();
    }

    /** Initialize this separator.
     *
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        orientation = new StringParameter(this, "orientation");
        orientation.setExpression("Horizontal");
    }
}
