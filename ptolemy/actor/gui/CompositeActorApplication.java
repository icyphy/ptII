/* A Ptolemy application that instantiates class names given on the command
 line.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CompositeActorApplication

/**
 This application creates one or more Ptolemy II models given a
 classname on the command line, and then executes those models, each in
 its own thread.  Each specified class should be derived from
 CompositeActor, and should have a constructor that takes a single
 argument, an instance of Workspace.  If the model does not contain
 a manager, then one will be created for it. The model is displayed using
 an instance of ModelFrame, which provides controls for executing
 the model and setting its top-level and director parameters.
 <p>
 The command-line arguments can also set parameter values for any
 parameter in the models, with the name given relative to the top-level
 entity.  For example, to specify the iteration count in an SDF model,
 you can invoke this on the command line as follows:
 <pre>
 java -classpath $PTII ptolemy.actor.gui.CompositeActorApplication \
 -director.iterations 1000 \
 -class ptolemy.domains.sdf.demo.Butterfly.Butterfly
 </pre>
 This assumes that the model given by the specified class name has a director
 named "director" with a parameter named "iterations".  If more than
 one model is given on the command line, then the parameter values will
 be set for all models that have such a parameter.
 <p>
 This class keeps count the number of open windows.  The waitForFinish
 method can then be used to determine when all of the windows opened by
 this class have been closed.  The main() method exits the application
 when all windows have been closed.

 @see ModelFrame
 @author Edward A. Lee, Brian K. Vogel, and Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (vogel)
 */
public class CompositeActorApplication extends CompositeActorSimpleApplication {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close any ModeFrames opened in processArgs().
     */
    public void close() {
        // Mainly used for testing.
        for (ModelFrame frame : _frames) {
            if (frame != null) {
                frame.close();
            }
        }
    }

    /** Create a new application with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String[] args) {
        CompositeActorApplication application = new CompositeActorApplication();
        _run(application, args);
    }

    /** Parse the command-line arguments, creating models as specified.
     *  @param args The command-line arguments.
     *  @exception Exception If something goes wrong.
     */
    @Override
    public void processArgs(String[] args) throws Exception {
        if (args != null) {
            _parseArgs(args);

            // start the models.
            Iterator models = _models.iterator();

            while (models.hasNext()) {
                _frames.add((ModelFrame) startRun((CompositeActor) models
                        .next()));
            }
        }
    }

    /** If the specified model has a manager and is not already running,
     *  then execute the model in a new thread.  Otherwise, do nothing.
     *  If the model contains an atomic entity that implements Placeable,
     *  we create create an instance of ModelFrame, if nothing implements
     *  Placeable, then we do not create an instance of ModelFrame.  This
     *  allows us to run non-graphical models on systems that do not have
     *  a display.
     *  <p>
     *  We then start the model running.
     *
     *  @param model The model to execute.
     *  @return The ModelFrame that for the model.
     *  @see ptolemy.actor.Manager#startRun()
     */
    @Override
    public synchronized Object startRun(CompositeActor model) {
        // This method is synchronized so that it can atomically modify
        // the count of executing processes.
        // NOTE: If you modify this method, please be sure that it
        // will work for non-graphical models in the nightly test suite.
        // Iterate through the model, looking for something that is Placeable.
        boolean hasPlaceable = false;
        Iterator atomicEntities = model.allAtomicEntityList().iterator();

        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();

            if (object instanceof Placeable
                    || object instanceof PortablePlaceable) {
                hasPlaceable = true;
                break;
            }
        }

        ModelFrame frame = null;
        if (hasPlaceable) {
            // The model has an entity that is Placeable, so create a frame.
            try {
                frame = new ModelFrame(model);
                _openCount++;
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent event) {
                        synchronized (CompositeActorApplication.this) {
                            _openCount--;
                            CompositeActorApplication.this.notifyAll();

                            // FIXME: is this right?  We need
                            // to exit if all the windows are closed?
                            if (_openCount == 0) {
                                StringUtilities.exit(0);
                            }
                        }
                    }
                });
                frame.setBackground(new Color(0xe5e5e5));
                frame.pack();
                frame.centerOnScreen();
                frame.setVisible(true);

                // FIXME: Use a JFrame listener to determine when all windows
                // are closed.
            } catch (Exception ex) {
                System.out.println("startRun: " + ex);
            }
        }

        Manager manager = model.getManager();

        if (manager != null) {
            try {
                manager.startRun();
            } catch (IllegalActionException ex) {
                // Model is already running.  Ignore.
            }
        } else {
            report("Model " + model.getFullName() + " cannot be executed "
                    + "because it does not have a manager.");
        }
        return frame;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // List of ModelFrames created processArgs();
    private List<ModelFrame> _frames = new LinkedList<ModelFrame>();
}
