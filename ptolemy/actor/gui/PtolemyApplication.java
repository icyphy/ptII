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
// FIXME: Trim this.
import java.awt.Color;
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
import ptolemy.gui.MessageHandler;
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
a class with which this class works very closely.  The instance of
ModelFrame is constructed using the openWindow() method of the
RunWindowAttribute class.

@author Edward A. Lee
@version $Id$
@see ModelFrame
@see RunWindowAttribute
*/
public class PtolemyApplication extends MoMLApplication {

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.  If the size of the argument
     *  array is 0, then open a default model.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PtolemyApplication(String args[]) throws Exception {
        // Invoke the base class constructor with null arguments to prevent
        // the base class from running any specified models.
        super(null);
	if (args.length == 0) {
            // FIXME: We need a better initial default model,
            // perhaps something with a console that we can type
            // commands into?
            String temporaryArgs[] = {"ptolemy/moml/demo/modulation.xml"};
	   _parseArgs(temporaryArgs);
	} else { 
            _parseArgs(args);
        }
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

    /** Note that the model associated with the specified manager has
     *  finished running, for the benefit of the waitForFinish() method.
     *  This overrides the base class to not report the error,
     *  since the instance of ModelFrame associated with the
     *  model will report the finishing.
     *  This method is called by the specified manager.
     *  @param manager The manager calling this method.
     *  @param ex The exception being reported.
     */
    public void executionError(Manager manager, Exception ex) {
        _runningCount--;
        if (_runningCount == 0) {
            notifyAll();
        }
    }

    /** Note that the model associated with the specified manager has
     *  finished running, for the benefit of the waitForFinish() method.
     *  This overrides the base class to not report the finishing,
     *  since the instance of ModelFrame associated with the
     *  model will report the finishing.
     *  This is method is called by the specified manager.
     *  @param manager The manager calling this method.
     */
    public synchronized void executionFinished(Manager manager) {
        _runningCount--;
        if (_runningCount == 0) {
            notifyAll();
        }
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
            MessageHandler.error("Command failed", ex);
            System.exit(0);
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

    /** Do nothing, since it is assumed that the ModelFrame associated
     *  with the specified manager will report state changes.
     *  This is method is called by the specified manager.
     *  @param manager The manager calling this method.
     */
    public void managerStateChanged(Manager manager) {
    }

    /** Read the specified stream, which is expected to be a MoML file.
     *  This overrides the base class to provide a container into which
     *  to put placeable objects, and to create a top-level frame
     *  for interacting with the model.  The top-level frame includes
     *  the placeable objects.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input stream.
     *  @param key The key to use to uniquely identify the model.
     *  @exception IOException If the stream cannot be read.
     */
    public void read(URL base, URL in, Object key)
            throws IOException {

        // FIXME: Need to examine the extension and open
        // an appropriate, registered instance of Top.  E.g., we can
        // open a text file.

        // If a frame has already been opened with the specified key,
        // then simply make that frame visible.
        NamedObj model;
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(
                new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBackground(BACKGROUND_COLOR);
        MoMLParser parser = new MoMLParser(new Workspace(), displayPanel);
        try {
            model = parser.parse(base, in.openStream());
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
            return;
        }
        // Create a run control window for it if it is of the right type.
        if (model instanceof TypedCompositeActor) {
            CompositeActor castTopLevel = (CompositeActor)model;
            add(key, castTopLevel);
            RunWindowAttribute attr = RunWindowAttribute.openWindow(
                    castTopLevel, displayPanel);
            if (attr != null) {
                attr.getFrame().setKey(key);
                // Make this a master, meaning that when it is closed, all
                // other windows owned by the model will also close.
                attr.setMaster(true);
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
