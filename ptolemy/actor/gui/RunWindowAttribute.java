/* An attribute representing a run window for the container.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// RunWindowAttribute
/**
An attribute representing a run window for the container model.
There should be only one such attribute associated with the model.
Create the attribute by using the static method openWindow().

@author  Edward A. Lee
@version $Id$
*/
public class RunWindowAttribute extends WindowAttribute {

    /** Construct an attribute with the given name  and
     *  given container. The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version number of the workspace.
     *  This constructor is private to ensure that no more than one
     *  such attribute is ever created for a model.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the container does not accept
     *   this attribute.
     *  @exception NameDuplicationException If the name coincides with an 
     *   attribute already in the container.
     */
    private RunWindowAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a run window for the specified actor and an attribute to
     *  represent that window.  If there is already such a window,
     *  then pop it to the foreground and return its attribute.
     *  Otherwise, create a new instance of ModelFrame.  If the model
     *  does not have a manager, then create one for it.  If for some
     *  reason the attribute cannot be created, then issue a warning and
     *  return null.
     *  @param model The model to create a run window for.
     *  @param panel A display panel to put into the run window, or null if
     *   none.
     */
    public static RunWindowAttribute openWindow(
            CompositeActor model, JPanel panel) {
        // Check to see whether a window already exists.
        List attributes = model.attributeList(RunWindowAttribute.class);
        if (attributes.size() > 0) {
            return (RunWindowAttribute)(attributes.iterator().next());
        }
        // No pre-existing window, so create one.
        ModelFrame frame = new ModelFrame(model);
        frame.setBackground(BACKGROUND_COLOR);
        try {
            RunWindowAttribute attr = new RunWindowAttribute(
                    model, model.uniqueName("run window "));

            attr.setFrame(frame);

            if (panel != null) {
                frame.modelPane().setDisplayPane(panel);
                
                // Calculate the size.
                Dimension frameSize = frame.getPreferredSize();
                
                // Swing classes produce a preferred size that is too small...
                frameSize.height += 30;
                frameSize.width += 30;
                frame.setSize(frameSize);
            }
            attr.centerOnScreen();
        
            // Make visible.
            frame.setVisible(true);

            return attr;
        } catch (KernelException ex) {
            try {
                MessageHandler.warning("Failed to create run window: " + ex);
            } catch (CancelException exception) {}
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);
}
