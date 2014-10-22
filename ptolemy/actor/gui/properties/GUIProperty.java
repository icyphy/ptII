/* Base class for the properties that can be used to configure a tableau.

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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;

import ptolemy.actor.gui.Tableau;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// GUIProperty

/**
 Base class for the properties that can be used to configure a tableau.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class GUIProperty extends Attribute {

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
    public GUIProperty(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null, null);
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
    public GUIProperty(NamedObj container, String name, JComponent component)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, component, null);
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
    public GUIProperty(NamedObj container, String name, JComponent component,
            Object constraint) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        preferredSize = new Parameter(this, "preferredSize");
        preferredSize.setTypeEquals(BaseType.INT_MATRIX);
        preferredSize.setToken("[-1, -1]");

        _component = component == null ? _createComponent() : component;
        _constraint = constraint;
        _add(container);
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
    public GUIProperty(NamedObj container, String name, Object constraint)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null, constraint);
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes. If the changed attribute
     *  is {@link #preferredSize}, then the preferred size of the Swing
     *  component in this GUI property is adjusted accordingly.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == preferredSize) {
            JComponent component = getComponent();
            if (component != null) {
                IntMatrixToken size = (IntMatrixToken) preferredSize.getToken();
                int width = size.getElementAt(0, 0);
                int height = size.getElementAt(0, 1);
                if (width >= 0 && height >= 0) {
                    component.setPreferredSize(new Dimension(width, height));
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the property into the specified workspace.
     *  @param workspace The workspace in to which the object is cloned.
     *  @return A new property
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GUIProperty newObject = (GUIProperty) super.clone(workspace);
        newObject._component = null;
        newObject._constraint = null;

        return newObject;
    }

    /** Return the Swing component.
     *  @return The Swing component.
     */
    public JComponent getComponent() {
        return _component;
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  If the container already
     *  contains an attribute with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this object being garbage collected.
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  <p>
     *  Subclasses may constrain the type of container by overriding
     *  {@link #setContainer(NamedObj)}.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if (getComponent() == null || _add(container)) {
            super.setContainer(container);
        } else {
            throw new IllegalActionException(getName()
                    + " cannot be contained by " + container.getName());
        }
    }

    /** The preferred size of the Swing component. It would be a matrix of 2
     *  integers, such as [100, 20]. It is ignored if any integer is less than
     *  0.
     */
    public Parameter preferredSize;

    /** Create a new Java Swing component.
     *
     *  @return A Swing component that can be enclosed in this GUI property.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected abstract JComponent _createComponent()
            throws IllegalActionException;

    /** Add the Java Swing component to the tableau or GUI property that
     *  contains this GUI property. If this GUI property is already added to one
     *  such container, it will be removed from its old container first.
     *
     *  @param container The new container.
     *  @return true if the container can be used; false if the adding is not
     *   successful.
     */
    private boolean _add(NamedObj container) {
        if (container == null) {
            _remove();
            return true;
        } else {
            JComponent component = getComponent();
            if (component != null) {
                if (container instanceof Tableau) {
                    _remove();
                    JFrame frame = ((Tableau) container).getFrame();
                    frame.getContentPane().add(component, _constraint);
                    return true;
                } else if (container instanceof GUIProperty) {
                    _remove();
                    ((GUIProperty) container).getComponent().add(component,
                            _constraint);
                    return true;
                }
            }
            return false;
        }
    }

    /** Remove the Java Swing component from the tableau or GUI property that
     *  contains this GUI property.
     */
    private void _remove() {
        NamedObj container = getContainer();
        if (container instanceof Tableau) {
            JFrame frame = ((Tableau) container).getFrame();
            frame.getContentPane().remove(getComponent());
        } else if (container instanceof GUIProperty) {
            JComponent component = ((GUIProperty) container).getComponent();
            component.remove(getComponent());
        }
    }

    /** The Java Swing component.
     */
    private JComponent _component;

    /** The layout constraint.
     */
    private Object _constraint;
}
