/* A GUI property that encloses a JComboBox component.

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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ComboBox

/**
 A GUI property that encloses a JComboBox component.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ComboBox extends GUIProperty implements ItemListener {

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
    public ComboBox(NamedObj container, String name)
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
    public ComboBox(NamedObj container, String name, JComponent component)
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
    public ComboBox(NamedObj container, String name, JComponent component,
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
    public ComboBox(NamedObj container, String name, Object constraint)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, constraint);
    }

    /** React to an action of changing the selected item in the combo box. The
     *  item must be an instance of {@link Item} and when it is selected, the
     *  {@link Item#perform()} method is invoked. After that, if the item
     *  specifies the next item to be selected in its {@link Item#next}
     *  attribute, that next item is selected, which may cause this method to be
     *  invoked again.
     *
     *  @param event The item event representing which item is selected.
     */
    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        Item item = (Item) event.getItem();
        item.perform();

        if (item.next == null) {
            return;
        }

        try {
            String next = item.next.stringValue();
            if (!next.equals("")) {
                Item nextItem = (Item) getAttribute(next);
                ((JComboBox) getComponent()).setSelectedItem(nextItem);
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(this, e,
                    "Unable to find the next item.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// Item

    /**
     The base class for an item that can be added to the combo box as a choice.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class Item extends ActionGUIProperty {

        /** Construct an item with the given name contained by the specified
         *  entity. The container argument must not be null, or a
         *  NullPointerException will be thrown.  This attribute will use the
         *  workspace of the container for synchronization and version counts.
         *  If the name argument is null, then the name is set to the empty
         *  string. Increment the version of the workspace.
         *  @param container The container.
         *  @param name The name of this attribute.
         *  @exception IllegalActionException If the attribute is not of an
         *   acceptable class for the container, or if the name contains a
         *   period.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Item(ComboBox container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            next = new StringParameter(this, "next");
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
        public void setContainer(NamedObj container)
                throws IllegalActionException, NameDuplicationException {
            ComboBox oldContainer = (ComboBox) getContainer();
            if (oldContainer != null) {
                ((JComboBox) oldContainer.getComponent()).removeItem(this);
            }
            super.setContainer(container);
            if (container != null) {
                ((JComboBox) ((ComboBox) container).getComponent())
                .addItem(this);
            }
        }

        /** Return the display name of this item, or its name if the display
         *  name is not specified. The returned string is shown in the combo box
         *  for this item.
         *
         *  @return The display name of this item.
         */
        @Override
        public String toString() {
            return getDisplayName();
        }

        /** The name of the next item in the same combo box to be selected when
         *  this GUI property is selected, or an empty string if there is no
         *  next item.
         */
        public StringParameter next;

        /** Create a new Java Swing component. In this case, the component is
         *  always null because a component is not needed for a combo box item.
         *
         *  @return A Swing component that can be enclosed in this GUI property.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        @Override
        protected JComponent _createComponent() throws IllegalActionException {
            // The component should not be used.
            return null;
        }
    }

    /** Create a new JComboBox component.
     *
     *  @return A Swing component that can be enclosed in this GUI property.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected JComponent _createComponent() throws IllegalActionException {
        JComboBox comboBox = new JComboBox();
        comboBox.addItemListener(this);
        return comboBox;
    }
}
