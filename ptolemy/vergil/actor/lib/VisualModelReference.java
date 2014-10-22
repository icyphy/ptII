/* An atomic actor that executes a model specified by a file or URL.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor.lib;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.lib.hoc.ModelReference;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.basic.ExtendedGraphFrame;

///////////////////////////////////////////////////////////////////
//// VisualModelReference

/**
 This is an atomic actor that can execute and/or open a model specified by
 a file or URL. This can be used to define an actor whose firing behavior
 is given by a complete execution of another model. It extends the base
 class with the following attributes and associated capabilities.
 <ul>
 <li> <i>openOnFiring</i>:
 The value of this string attribute determines what open
 happens when the fire() method is invoked.  The recognized
 values are:
 <ul>
 <li> "do not open" (the default) </li>
 <li> "open in Vergil" </li>
 <li> "open in Vergil (full screen)" </li>
 </ul>
 Note that it is dangerous to use the full-screen mode because it
 becomes difficult to stop execution of the model that contains this
 actor.  In full-screen mode, the referenced model will consume
 the entire screen.  Stopping that execution will only serve to
 stop the current iteration, and very likely, another iteration will
 begin immediately and again occupy the entire screen.
 Use this option with care.
 </li>
 <li> <i>closeOnPostfire</i>:
 The value of this string attribute determines what happens
 in the postfire() method.  The recognized values are:
 <ul>
 <li> "do nothing" (the default) </li>
 <li> "close Vergil graph" </li>
 </ul>
 </li>
 </ul>


 @author Edward A. Lee, Elaine Cheong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see ptolemy.data.expr.Variable
 @see ptolemy.data.expr.Parameter
 @see ptolemy.kernel.util.Settable
 */
public class VisualModelReference extends ModelReference {
    /** Construct a VisualModelReference with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public VisualModelReference(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the openOnFiring parameter.
        openOnFiring = new StringParameter(this, "openOnFiring");

        // Set the options for the parameters.
        openOnFiring.setExpression("do not open");
        openOnFiring.addChoice("doNotOpen");
        openOnFiring.addChoice("open in Vergil");
        openOnFiring.addChoice("open in Vergil (full screen)");

        // Create the closeOnPostfire parameter.
        closeOnPostfire = new StringParameter(this, "closeOnPostfire");
        closeOnPostfire.setExpression("do nothing");
        closeOnPostfire.addChoice("do nothing");
        closeOnPostfire.addChoice("close Vergil graph");

        // Create a tableau factory to override look inside behavior.
        new LookInside(this, "_lookInsideOverride");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The value of this string parameter determines what open
     *  happens when the fire() method is invoked.  The recognized
     *  values are:
     *  <ul>
     *  <li> "do not open" (the default) </li>
     *  <li> "open in Vergil" </li>
     *  <li> "open in Vergil (full screen)" </li>
     *  </ul>
     */
    public StringParameter openOnFiring;

    /** The value of this string parameter determines what close action
     *  happens in the postfire() method.  The recognized values are:
     *  <ul>
     *  <li> "do nothing" (the default) </li>
     *  <li> "close Vergil graph" </li>
     *  </ul>
     */
    public StringParameter closeOnPostfire;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to open the model specified if the
     *  attribute is modelFileOrURL, or for other parameters, to cache
     *  their values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == openOnFiring) {
            String openOnFiringValue = openOnFiring.stringValue();

            if (openOnFiringValue.equals("do not open")) {
                _openOnFiringValue = _DO_NOT_OPEN;
            } else if (openOnFiringValue.equals("open in Vergil")) {
                _openOnFiringValue = _OPEN_IN_VERGIL;
            } else if (openOnFiringValue.equals("open in Vergil (full screen)")) {
                _openOnFiringValue = _OPEN_IN_VERGIL_FULL_SCREEN;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized option for openOnFiring: "
                                + openOnFiringValue);
            }
        } else if (attribute == closeOnPostfire) {
            String closeOnPostfireValue = closeOnPostfire.stringValue();

            if (closeOnPostfireValue.equals("do nothing")) {
                _closeOnPostfireValue = _DO_NOTHING;
            } else if (closeOnPostfireValue.equals("close Vergil graph")) {
                _closeOnPostfireValue = _CLOSE_VERGIL_GRAPH;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized option for closeOnPostfire: "
                                + closeOnPostfireValue);
            }
        }
        if (attribute == modelFileOrURL) {
            super.attributeChanged(attribute);
            // If there was previously an effigy or tableau
            // associated with this model, then delete it.
            if (_effigy != null) {
                try {
                    _effigy.setContainer(null);
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
                _effigy = null;
            }
            if (_tableau != null) {
                _tableau.close();
                try {
                    _tableau.setContainer(null);
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
                _tableau = null;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone this actor into the specified workspace.
     *  Override the base class to ensure that private variables are
     *  reset to null.
     *  @param workspace The workspace for the cloned object.
     *  @return A new instance of VisualModelReference.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        VisualModelReference newActor = (VisualModelReference) super
                .clone(workspace);
        newActor._tableau = null;
        newActor._effigy = null;
        return newActor;
    }

    /** Run a complete execution of the referenced model.  A complete
     *  execution consists of invocation of super.initialize(), repeated
     *  invocations of super.prefire(), super.fire(), and super.postfire(),
     *  followed by super.wrapup().  The invocations of prefire(), fire(),
     *  and postfire() are repeated until either the model indicates it
     *  is not ready to execute (prefire() returns false), or it requests
     *  a stop (postfire() returns false or stop() is called).
     *  Before running the complete execution, this method examines input
     *  ports, and if they are connected, have data, and if the referenced
     *  model has a top-level parameter with the same name, then one token
     *  is read from the input port and used to set the value of the
     *  parameter in the referenced model.
     *  After running the complete execution, if there are any output ports,
     *  then this method looks for top-level parameters in the referenced
     *  model with the same name as the output ports, and if there are any,
     *  reads their values and produces them on the output.
     *  If no model has been specified, then this method does nothing.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    @Override
    public void fire() throws IllegalActionException {
        // NOTE: Even though the superclass calls this, we have to
        // call it here before the actions below. Regrettably,
        // this requires disabling the call in the superclass
        // because otherwise, if there are two pending input
        // tokens, they will both be consumed in this firing.
        _readInputsAndValidateSettables();
        _alreadyReadInputs = true;

        if (_model instanceof CompositeActor) {
            // Will need the effigy for the model this actor is in.
            NamedObj toplevel = toplevel();
            final Effigy myEffigy = Configuration.findEffigy(toplevel);

            // If there is no such effigy, then skip trying to open a tableau.
            // The model may have no graphical elements.
            if (myEffigy != null) {
                try {
                    // Conditionally show the model in Vergil. The openModel()
                    // method also creates the right effigy.
                    if (_openOnFiringValue == _OPEN_IN_VERGIL
                            || _openOnFiringValue == _OPEN_IN_VERGIL_FULL_SCREEN) {
                        // NOTE: The opening must occur in the event thread.
                        // Regrettably, we cannot continue with the firing until
                        // the open is complete, so we use the very dangerous
                        // invokeAndWait() method.
                        Runnable doOpen = new Runnable() {
                            @Override
                            public void run() {
                                Configuration configuration = (Configuration) myEffigy
                                        .toplevel();

                                if (_debugging) {
                                    _debug("** Using the configuration to open a tableau.");
                                }

                                try {
                                    // NOTE: Executing this in the event thread averts
                                    // a race condition... Previous close(), which was
                                    // deferred to the UI thread, will have completed.
                                    _exception = null;
                                    _tableau = configuration.openModel(_model,
                                            myEffigy);

                                    // Set this tableau to be a master so that when it
                                    // gets closed, all its subwindows get closed.
                                    _tableau.setMaster(true);
                                } catch (KernelException e) {
                                    // Record the exception for later reporting.
                                    _exception = e;
                                }

                                if (_tableau != null) {
                                    _tableau.show();

                                    JFrame frame = _tableau.getFrame();

                                    if (frame != null) {
                                        if (_openOnFiringValue == _OPEN_IN_VERGIL_FULL_SCREEN) {
                                            if (frame instanceof ExtendedGraphFrame) {
                                                ((ExtendedGraphFrame) frame)
                                                        .fullScreen();
                                            }
                                        }

                                        frame.toFront();
                                    }
                                }
                            }
                        };

                        try {
                            if (!SwingUtilities.isEventDispatchThread()) {
                                SwingUtilities.invokeAndWait(doOpen);
                            } else {
                                // Exporting HTML for ptolemy/actor/lib/hoc/demo/ModelReference/ModelReference.xml
                                // ends up running this in the Swing event dispatch thread.
                                doOpen.run();
                            }

                        } catch (Exception ex) {
                            throw new IllegalActionException(this, null, ex,
                                    "Open failed.");
                        }

                        if (_exception != null) {
                            // An exception occurred while trying to open.
                            throw new IllegalActionException(this, null,
                                    _exception, "Failed to open.");
                        }
                    } else {

                        // Need an effigy for the model, or else
                        // graphical elements of the model will not
                        // work properly.  That effigy needs to be
                        // contained by the effigy responsible for
                        // this actor.

                        if (_effigy == null) {
                            _effigy = new PtolemyEffigy(myEffigy,
                                    myEffigy.uniqueName(_model.getName()));
                            _effigy.setModel(_model);

                            // Since there is no tableau, this is probably not
                            // necessary, but as a safety precaution, we prevent
                            // writing of the model.
                            _effigy.setModifiable(false);

                            if (_debugging) {
                                _debug("** Created new effigy for referenced model.");
                            }
                        }
                    }
                } catch (NameDuplicationException ex) {
                    // This should not be thrown.
                    throw new InternalErrorException(ex);
                }
            }
        }

        // Call this last so that we open before executing.
        super.fire();
    }

    /** Override the base class to perform requested close on postfire actions.
     *  Note that if a close is requested, then this method waits until the
     *  AWT event thread completes the close.  This creates the possibility
     *  of a deadlock.
     *  @return Whatever the superclass returns (probably true).
     *  @exception IllegalActionException Thrown if a parent class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // Call this first so execution stops before closing.
        boolean result = super.postfire();

        if (_tableau != null) {
            final JFrame frame = _tableau.getFrame();

            if (_closeOnPostfireValue == _CLOSE_VERGIL_GRAPH) {
                if (_debugging) {
                    _debug("** Closing Vergil graph.");
                }

                if (frame instanceof TableauFrame) {
                    // NOTE: The closing will happen in the swing event
                    // thread.  We can proceed on the assumption
                    // that the next firing, if it opens vergil, will
                    // do so in the event thread.
                    Runnable doClose = new Runnable() {
                        @Override
                        public void run() {
                            if (frame instanceof ExtendedGraphFrame) {
                                ((ExtendedGraphFrame) frame).cancelFullScreen();
                            }

                            ((TableauFrame) frame).close();
                        }
                    };

                    Top.deferIfNecessary(doClose);
                } else if (frame != null) {
                    // This should be done in the event thread.
                    Runnable doClose = new Runnable() {
                        @Override
                        public void run() {
                            if (frame instanceof ExtendedGraphFrame) {
                                ((ExtendedGraphFrame) frame).cancelFullScreen();
                            }

                            frame.setVisible(true);
                        }
                    };

                    Top.deferIfNecessary(doClose);
                }
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Tableau that has been created (if any). */
    protected Tableau _tableau;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Possible values for openOnFiring.
    private static int _DO_NOT_OPEN = 0;
    private static int _OPEN_IN_VERGIL = 1;
    private static int _OPEN_IN_VERGIL_FULL_SCREEN = 2;

    /** The value of the openOnFiring parameter. */
    private transient int _openOnFiringValue = _DO_NOT_OPEN;

    // Possible values for closeOnPostfire.
    private static int _DO_NOTHING = 0;

    private static int _CLOSE_VERGIL_GRAPH = 1;

    /** The value of the closeOnPostfire parameter. */
    private transient int _closeOnPostfireValue = _DO_NOTHING;

    /** Store exception thrown in event thread. */
    private Exception _exception = null;

    /** Effigy that has been created (if any). */
    private PtolemyEffigy _effigy;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A tableau factory to override the look inside behavior to open
     *  the referenced model, if there is one.
     */
    public class LookInside extends TableauFactory {
        /**
         * Construct a VisualModelReference$LookInside object.
         *
         * @param container The container of the LookInside to be constructed.
         * @param name The name of the LookInside to be constructed.
         * @exception IllegalActionException If thrown by the superclass.
         * @exception NameDuplicationException If thrown by the superclass.
         */
        public LookInside(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Open an instance of the model.
         *  @param effigy The effigy with which we open the model.
         *  @return The instance of the model.
         *  @exception Exception If there is a problem opening the
         *  model.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (_model == null) {
                throw new IllegalActionException(VisualModelReference.this,
                        "No model referenced.");
            }
            Configuration configuration = (Configuration) effigy.toplevel();
            return configuration.openInstance(_model, effigy);
        }
    }
}
