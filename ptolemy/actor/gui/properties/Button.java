/* A GUI property that encloses a JButton component.

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Button

/**
 A GUI property that encloses a JButton component.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Button extends ActionGUIProperty {

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
    public Button(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        icon = new FileParameter(this, "icon");
        tooltip = new StringParameter(this, "tooltip");
    }

    /** Set a name to present to the user.
     *  @param name A name to present to the user.
     *  @see #getDisplayName()
     */
    @Override
    public void setDisplayName(String name) {
        super.setDisplayName(name);
        ((JButton) getComponent()).setText(name);
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
        super.attributeChanged(attribute);

        if (attribute == icon) {
            URL url = icon.asURL();
            if (url != null) {
                ImageIcon imageIcon = new ImageIcon(url);
                JButton button = (JButton) getComponent();
                button.setIcon(imageIcon);
            }
        }

        if (attribute == tooltip) {
            String tooltipString = tooltip.stringValue();
            JButton button = (JButton) getComponent();
            button.setToolTipText(tooltipString);
        }

    }

    /** Create a new Java Swing component.
     *
     *  @return A Swing component that can be enclosed in this GUI property.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected JComponent _createComponent() throws IllegalActionException {
        JButton button = new JButton(getDisplayName());
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                perform();
            }
        });
        return button;
    }

    /** Icon for the button. Set an empty string to remove the icon.
     */
    public FileParameter icon;

    /** Tooltip for the button.
     */
    public StringParameter tooltip;
}
