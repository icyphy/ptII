/* An application that contains models and frames for interacting with them.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

// Ptolemy imports
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

// XML Imports
import com.microstar.xml.XmlException;

//////////////////////////////////////////////////////////////////////////
//// PtolemyApplication
/**
An application that contains models and frames for interacting
with them. Any number of models can be simultaneously running under
the same application.  Each one is assigned an instance of ModelFrame,
a class with which this class works very closely.

@author Edward A. Lee
@version $Id$
*/
public class PtolemyApplication extends MoMLApplication {

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PtolemyApplication(String args[]) throws Exception {
        // Invoke the base class constructor with null arguments to prevent
        // the base class from running any specified models.
        super(null);
        _parseArgs(args);
        _commandTemplate = "ptolemy [ options ] [file ...]";

        // The Java look & feel is pretty lame, so we use the native
        // look and feel of the platform we are running on.
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a model to the application and create a frame for interacting
     *  with it.  If the model has no manager, then create one.
     *  If a frame already exists for the model, then return the existing one.
     *  The caller is responsible for calling setVisible(true)
     *  on the returned frame to make the frame appear on the screen.
     *  Note that this method does not automatically include the placeable
     *  objects of the model in the interactive frame.  If the caller
     *  wishes to include placeable objects, then the caller must take
     *  care of calling place() for each.
     *  @param model The model to add.
     *  @return The frame that was created.
     */
    public ModelFrame createFrame(final CompositeActor model) {
        add(model);
        // The add() method registers this as an execution listener.
        // Reverse this, since we assume the frame will handle reporting.
        Manager manager = model.getManager();
        if (manager != null) {
            manager.removeExecutionListener(this);
        }
        // Unless there is already a frame for this model, make one.
        ModelFrame frame = (ModelFrame)_frames.get(model);
        if (frame == null) {
            frame = new ModelFrame(model, this);
            frame.setBackground(BACKGROUND_COLOR);
            _frames.put(model, frame);
            // Set up a listener for window closing events.
            frame.addWindowListener(new WindowAdapter() {
                // This is invoked if the window is closed
                // via the window manager.
                public void windowClosing(WindowEvent e) {
                    remove(model);
                }
                // This is invoked if the window is closed via dispose()
                // (which is via the close menu command).
                public void windowClosed(WindowEvent e) {
                    remove(model);
                }
            });
        }
        return frame;
    }

    /** Create a new application with the specified command-line arguments.
     *  If the command-line arguments include the names of MoML files or
     *  URLs for MoML files, then one window is opened for each model.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            PtolemyApplication plot = new PtolemyApplication(args);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the specified stream, which is expected to be a MoML file.
     *  This overrides the base class to provide a container into which
     *  to put placeable objects, and to create a top-level frame
     *  for interacting with the model.  The top-level frame includes
     *  the placeable objects.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(URL base, InputStream in) throws IOException {
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBackground(BACKGROUND_COLOR);
        MoMLParser parser = new MoMLParser(new Workspace(), displayPanel);
        try {
            NamedObj toplevel = parser.parse(base, in);
            if (toplevel instanceof TypedCompositeActor) {
                CompositeActor castTopLevel = (CompositeActor)toplevel;
                ModelFrame frame = createFrame(castTopLevel);
                frame.modelPane().setDisplayPane(displayPanel);

                // Calculate the size.
                Dimension frameSize = frame.getPreferredSize();
                // Swing classes produce a preferred size that is too small...
                frameSize.height += 30;
                frameSize.width += 30;
                frame.setSize(frameSize);

                // Center on screen.
                Dimension screenSize
                    = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (screenSize.width - frameSize.width) / 2;
                int y = (screenSize.height - frameSize.height) / 2;
                frame.setLocation(x, y);

                frame.setVisible(true);
            }
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                XmlException xmlEx = (XmlException)ex;
                // FIXME: The file reported below is wrong... Why?
                report("MoML exception on line " + xmlEx.getLine()
                        + ", column " + xmlEx.getColumn() + ", in entity:\n"
                        + xmlEx.getSystemId(), ex);
            } else {
                report("Failed to read file:\n", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    // A table of frames associated with models.
    private Map _frames = new HashMap();
}
