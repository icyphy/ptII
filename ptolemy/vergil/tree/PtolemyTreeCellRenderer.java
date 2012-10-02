/* A tree cell renderer for ptolemy objects.

 Copyright (c) 2000-2012 The Regents of the University of California.
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
package ptolemy.vergil.tree;

import java.awt.Component;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.Documentation;
import ptolemy.moml.EntityLibrary;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;

/**
 A tree cell renderer for Ptolemy objects.  This renderer renders
 the icon of an object, if it has one.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Revision$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class PtolemyTreeCellRenderer extends DefaultTreeCellRenderer {

    /** Construct a tree cell renderer that shows the expression value
     *  of any object that implements {@link Settable}.
     */
    public PtolemyTreeCellRenderer() {
        this(true);
    }

    /** Construct a tree cell renderer that shows the expression value
     *  of any object that implements {@link Settable}.
     *  @param showSettableValues If true, show the expression value
     *   for any object that implements {@link Settable}.
     */
    public PtolemyTreeCellRenderer(boolean showSettableValues) {
        _showSettableValues = showSettableValues;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public method                     ////

    /** Create a new rendition for the given object.  The rendition is
     *  the default provided by the base class with the text set to
     *  the name of the node (if it is an object implementing Nameable).
     *  If the object is an instance of NamedObj and it has attributes
     *  of type Documentation, then construct a tooltip as follows.
     *  If there is a Documentation attribute named "tooltip", then
     *  use that.  Otherwise, consolidate all the Documentation
     *  attributes and use those.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        DefaultTreeCellRenderer component = (DefaultTreeCellRenderer) super
                .getTreeCellRendererComponent(tree, value, selected, expanded,
                        leaf, row, hasFocus);

        if (value instanceof NamedObj) {
            NamedObj object = (NamedObj) value;

            // Fix the background colors because transparent
            // labels don't work quite right.
            if (!selected) {
                component.setBackground(tree.getBackground());
                component.setOpaque(true);
            } else {
                component.setOpaque(false);
            }

            if (object instanceof Settable) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(object.getDisplayName());
                if (_showSettableValues) {
                    buffer.append("=");
                    buffer.append(((Settable) object).getExpression().replace(
                            '\n', ' '));
                }
                component.setText(buffer.toString());
            } else {
                // Use the display name not the name so that what is
                // shown to the user in the tree view matches what is
                // shown in the regular canvas.
                component.setText(object.getDisplayName());
            }

            // Render an icon, if one has been defined.
            // NOTE: Avoid asking for attributes if the entity is a library
            // because this will trigger evaluation, defeating deferred
            // evaluation.
            if (!(object instanceof EntityLibrary)) {
                // Only if an object has an icon, an icon description, or
                // a small icon description is it rendered in the tree.
                List iconList = object.attributeList(EditorIcon.class);

                // jkillian removed the first condition (iconList.size() > 0), saying:
                // Removed iconList.size() > 0 condition from the if-clause because
                // it cached a blank image when an icon wasn't defined and then always
                // entered the block in subsequent repaints.  This caused the icon
                // to disappear in Homer when dragging attributes onto the canvas.
                // Doing so didn't seem to affect the tree display in Vergil.
                //
                // The last statement is wrong. The result is that icons do not
                // display in the library. Hence, I've reversed the change. EAL 12/15/11.
                if ((iconList.size() > 0)
                        || (object.getAttribute("_iconDescription") != null)
                        || (object.getAttribute("_smallIconDescription") != null)) {
                    // NOTE: this code is similar to that in IconController.
                    EditorIcon icon = null;

                    try {
                        if (iconList.size() == 0) {
                            icon = XMLIcon.getXMLIcon(object, "_icon");
                            icon.setPersistent(false);
                        } else {
                            icon = (EditorIcon) iconList
                                    .get(iconList.size() - 1);
                        }
                    } catch (KernelException ex) {
                        throw new InternalErrorException(
                                "could not create icon in "
                                        + object
                                        + " even though one did not previously exist.");
                    }

                    // Wow.. this is a confusing line of code.. :)
                    try {
                        component.setIcon(icon.createIcon());
                    } catch (Throwable throwable) {
                        // Ignore this, but print a message
                        System.err.println("Warning: Failed to create or "
                                + "set icon " + icon + " for component "
                                + component);
                        throwable.printStackTrace();
                    }
                }

                // NOTE: The following is not called on EntityLibrary,
                // which means no tooltip for those. Calling it would
                // force expansion of the library. No good solution here.
                Attribute tooltipAttribute = object.getAttribute("tooltip");

                if ((tooltipAttribute != null)
                        && tooltipAttribute instanceof Documentation) {
                    // FIXME: This doesn't work with calling this
                    // on either this or component.
                    this.setToolTipText(((Documentation) tooltipAttribute)
                            .getValueAsString());
                } else {
                    String tip = Documentation.consolidate(object);

                    if (tip != null) {
                        // FIXME: This doesn't work with calling this
                        // on either this or component.
                        this.setToolTipText(tip);
                    }
                }
            }
        }

        return component;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////

    /** Indicator of whether to show expression values of Settables. */
    private boolean _showSettableValues;
}
