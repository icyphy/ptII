/* An attribute representing a frame associated with the model.

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

import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// WindowAttribute
/**
An attribute representing a frame associated with the model.
The frame is required to be an instance of Top, which ensures
that all top-level windows in Ptolemy II have a consistent set of
capabilities.
This class is used to identify top-level user interface windows
associated with a model, such as run control panels and editors.
It is an attribute so that the model itself keeps track of the
windows that is associated with.

@author  Edward A. Lee
@version $Id$
@see Top
*/
public class WindowAttribute extends Attribute {

    /** Construct an attribute with the given name  and
     *  given container. The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version number of the workspace.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the container does not accept
     *   this attribute.
     *  @exception NameDuplicationException If the name coincides with an 
     *   attribute already in the container.
     */
    public WindowAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Center the associated frame on the screen. If there is no
     *  associated frame, do nothing.
     */
    public void centerOnScreen() {
        if (_frame != null) {
            Dimension screenSize
                    = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = _frame.getPreferredSize();

            int x = (screenSize.width - frameSize.width) / 2;
            int y = (screenSize.height - frameSize.height) / 2;
            _frame.setLocation(x, y);
        }
    }

    /** Write a MoML description of this object, which in this case is
     *  empty.  Nothing is written.
     *  MoML is an XML modeling markup language.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     */
    public void exportMoML(Writer output, int depth, String name)
             throws IOException {
    }

    /** Return the frame associated with this attribute.
     *  @returns The frame associated with the attribute.
     */
    public Top getFrame() {
        return _frame;
    }

    /** Return true if the window associated with this attribute
     *  is a master, which means that if that window is closed, then
     *  all windows associated with the model are closed.
     *  @return True if the window is a master.
     */
    public boolean isMaster() {
        return _master;
    }

    /** Override the base class so that if the argument is null and the
     *  window is a master, then all other windows associated with the
     *  container are closed and the model is removed from the ModelDirectory.
     *  If this window is not a master, but after removing it there are
     *  no more windows associated with the model, then also remove it
     *  from the ModelDirectory.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        NamedObj oldcontainer = (NamedObj)getContainer();
        super.setContainer(container);
        if (container == null) {
            if (_master) {
                // Window is a master.  Close all other windows.
                List attrList = oldcontainer.attributeList(
                        WindowAttribute.class);
                Iterator attributes = attrList.iterator();
                while (attributes.hasNext()) {
                    WindowAttribute attr = (WindowAttribute)attributes.next();
                    Top frame = attr.getFrame();
                    if (frame != null) {
                        frame.dispose();
                    }
                }
            }
            // If there are no more window attributes, then remove
            // the model from the directory.  If this makes the directory
            // empty, then this results in exiting the application.
            List attrList = oldcontainer.attributeList(WindowAttribute.class);
            if (attrList.size() == 0) {
                ModelDirectory.remove(_frame.getKey());
            }
        }
    }

    /** Set the frame associated with this attribute.
     *  @param frame The frame associated with the attribute.
     */
    public void setFrame(Top frame) {
        _frame = frame;

        // Set up a listener for window closing events.
        frame.addWindowListener(new WindowAdapter() {
            // This is invoked if the window is closed
            // via the window manager.
            public void windowClosing(WindowEvent e) {
                try {
                    setContainer(null);
                } catch (KernelException ex) {
                    try {
                        MessageHandler.warning(
                            "Cannot remove run window attribute: " + ex);
                    } catch (CancelException exception) {}
                }
            }
            // This is invoked if the window is closed via dispose()
            // (which is via the close menu command).
            public void windowClosed(WindowEvent e) {
                windowClosing(e);
            }
        });
    }

    /** Specify whether the window associated with this attribute
     *  is a master, which means that if that window is closed, then
     *  all windows associated with the model are closed.
     *  @param on If true, makes the window a master.
     */
    public void setMaster(boolean on) {
        _master = on;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The frame that this attribute represents. */
    protected Top _frame;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicator of whether this window is a master.
    private boolean _master = false;
}
