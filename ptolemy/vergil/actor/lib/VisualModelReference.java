/* An atomic actor that executes a model specified by a file or URL.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

package ptolemy.vergil.actor.lib;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.lib.hoc.ModelReference;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.basic.ExtendedGraphFrame;

//////////////////////////////////////////////////////////////////////////
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
<li> "do not open" (the default)
<li> "open in Vergil"
<li> "open in Vergil (full screen)"
<li> "open run control panel"
</ul>
Note that it is dangerous to use the full-screen mode because it
becomes difficult to stop execution of the model that contains this
actor.  In full-screen mode, the referenced model will consume
the entire screen.  Stopping that execution will only serve to
stop the current iteration, and very likely, another iteration will
begin immediately and again occupy the entire screen.
Use this option with care.

<li> <i>closeOnPostfire</i>:
The value of this string attribute determines what happens
in the postfire() method.  The recognized values are:
<ul>
<li> "do nothing" (the default)
<li> "close Vergil graph"
</ul>

</ul>
<p>
There are currently a number of serious limitations:
<ul>
FIXME: Modifying and saving the referenced model, if done through the
Vergil window opened by this actor, results in overwriting the referenced
model with a copy of the model containing this actor!
<li>
FIXME: Modifying the referenced model in another window and saving
it does not result in this actor re-reading the model.
<li>
FIXME: Closing the master model doesn't close open referenced models.
<li>
FIXME: Supporting full-screen operation creates a dependence on vergil.
Without that, this actor could be in the actor package.  Need to figure
out how to remove this dependence.
</ul>
<P>

@author Edward A. Lee
@version $Id$
@see Variable
@see Parameter
@see Settable
*/
public class VisualModelReference
    extends ModelReference
    implements ExecutionListener {

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
        openOnFiring.addChoice("open run control panel");

        // Create the closeOnPostfire parameter.
        closeOnPostfire = new StringParameter(this, "closeOnPostfire");
        closeOnPostfire.setExpression("do nothing");
        closeOnPostfire.addChoice("do nothing");
        closeOnPostfire.addChoice("close Vergil graph");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The value of this string parameter determines what open
     *  happens when the fire() method is invoked.  The recognized
     *  values are:
     *  <ul>
     *  <li> "do not open" (the default)
     *  <li> "open in Vergil"
     *  <li> "open in Vergil (full screen)"
     *  <li> "open run control panel"
     *  </ul>
     */
    public StringParameter openOnFiring;

    /** The value of this string parameter determines what close action
     *  happens in the postfire() method.  The recognized values are:
     *  <ul>
     *  <li> "do nothing" (the default)
     *  <li> "close Vergil graph"
     *  </ul>
     */
    public StringParameter closeOnPostfire;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to open the model specified if the
     *  attribue is modelFileOrURL, or for other parameters, to cache
     *  their values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == openOnFiring) {
            String openOnFiringValue = openOnFiring.stringValue();
            if (openOnFiringValue.equals("do not open")) {
                _openOnFiringValue = _DO_NOT_OPEN;
            } else if (openOnFiringValue.equals("open in Vergil")) {
                _openOnFiringValue = _OPEN_IN_VERGIL;
            } else if (
                openOnFiringValue.equals("open in Vergil (full screen)")) {
                _openOnFiringValue = _OPEN_IN_VERGIL_FULL_SCREEN;
            } else if (openOnFiringValue.equals("open run control panel")) {
                _openOnFiringValue = _OPEN_RUN_CONTROL_PANEL;
            } else {
                throw new IllegalActionException(this,
                "Unrecognized option for openOnFiring: " + openOnFiringValue);
            }
        } else if (attribute == closeOnPostfire) {
            String closeOnPostfireValue = closeOnPostfire.stringValue();
            if (closeOnPostfireValue.equals("do nothing")) {
                _closeOnPostfireValue = _DO_NOTHING;
            } else if (closeOnPostfireValue.equals("close Vergil graph")) {
                _closeOnPostfireValue = _CLOSE_VERGIL_GRAPH;
            } else {
                throw new IllegalActionException(this,
                "Unrecognized option for closeOnPostfire: " + closeOnPostfireValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to ensure that private variables are reset to null.
     *  @return A new instance of VisualModelReference.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        VisualModelReference newActor = (VisualModelReference) super.clone(workspace);
        newActor._tableau = null;
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
    public void fire() throws IllegalActionException {
        if (_model instanceof CompositeActor) {
            CompositeActor executable = (CompositeActor) _model;

            // Will need the effigy for the model this actor is in.
            NamedObj toplevel = toplevel();
            Effigy myEffigy = Configuration.findEffigy(toplevel);

            // If there is no such effigy, then skip trying to open a tableau.
            // The model may have no graphical elements.
            if (myEffigy != null) {
                try {
                    // Conditionally show the model in Vergil. The openModel()
                    // method also creates the right effigy.
                    if (_openOnFiringValue == _OPEN_IN_VERGIL
                        || _openOnFiringValue == _OPEN_IN_VERGIL_FULL_SCREEN) {
                        Configuration configuration =
                            (Configuration) myEffigy.toplevel();
                        if (_debugging) {
                            _debug("** Using the configuration to open a tableau.");
                        }
                        // FIXME: Race condition... Previous close(), which was
                        // deferred to the UI thread, may not have completed, so
                        // we may open the old tableau as it is closing!
                        _tableau = configuration.openModel(_model, myEffigy);
                        
                        // Do not allow editing on this tableau.  In particular,
                        // if editing were allowed, then an attempt to save the
                        // changes will result in a spectacular failure.  The
                        // model that will be saved will actually be the referring
                        // model rather than the referred to model.  This will
                        // trash the referred to model, and will result in an
                        // infinite loop when attempting to open either model.
                        // FIXME: This doesn't work!!!! Can still save model!!!!
                        _tableau.setEditable(false);
                        // FIXME: Should instead prevent delegating the write.
                        // One way would be to create an effigy that overrides
                        // topEffigy() to return itself. However, there is a
                        // cast in PtolemyEffigy that will fail if we do that...
                        // ((Effigy)_tableau.getContainer()).setModifiable(false);

                        _tableau.show();
                    } else {
                        // Need an effigy for the model, or else graphical elements
                        // of the model will not work properly.  That effigy needs
                        // to be contained by the effigy responsible for this actor.
                        PtolemyEffigy newEffigy =
                            new PtolemyEffigy(
                                myEffigy,
                                myEffigy.uniqueName(_model.getName()));
                        newEffigy.setModel(_model);
                        // Since there is no tableau, this is probably not
                        // necessary, but as a safety precaution, we prevent
                        // writing of the model.
                        newEffigy.setModifiable(false);
                        if (_debugging) {
                            _debug("** Created new effigy for referenced model.");
                        }
                    }
                } catch (NameDuplicationException ex) {
                    // This should not be thrown.
                    throw new InternalErrorException(ex);
                }
            }
            // If we did not open in Vergil, then there is no tableau.
            if (_tableau != null) {
                JFrame frame = _tableau.getFrame();
                if (frame != null) {
                    if (_openOnFiringValue == _OPEN_IN_VERGIL) {
                        frame.toFront();
                    } else if (_openOnFiringValue == _OPEN_IN_VERGIL_FULL_SCREEN) {
                        if (frame instanceof ExtendedGraphFrame) {
                            ((ExtendedGraphFrame) frame).fullScreen();
                        } else {
                            // No support for full screen.
                            frame.toFront();
                        }
                    }
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
     */
    public boolean postfire() throws IllegalActionException {
        // Call this first so execution stops before closing.
        boolean result = super.postfire();
        if (_tableau != null) {
            final JFrame frame = _tableau.getFrame();
            if (_closeOnPostfireValue == _CLOSE_VERGIL_GRAPH) {
                if (_debugging) {
                    _debug("** Closing Vergil graph.");
                }
                if (frame instanceof ExtendedGraphFrame) {
                    ((ExtendedGraphFrame) frame).cancelFullScreen();
                }
                if (frame instanceof TableauFrame) {
                    // NOTE: The closing will happen in the swing event
                    // thread.  We should not proceed until it has
                    // happened, otherwise we could create a race condition
                    // where the next firing occurs before the close has
                    // completed.  Thus, we use the very dangerous
                    // invokeAndWait() facility here.
                    Runnable doClose = new Runnable() {
                        public void run() {
                            ((TableauFrame) frame).close();
                        }
                    };
                    try {
                        SwingUtilities.invokeAndWait(doClose);
                    } catch (Exception ex) {
                        // Ignore exceptions.  Bad side is model
                        // remains open.
                    }
                } else if (frame != null) {
                    // This should be done in the event thread.
                    Runnable doClose = new Runnable() {
                        public void run() {
                            frame.hide();
                        }
                    };
                    Top.deferIfNecessary(doClose);
                }
            }
        }
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // Possible values for openOnFiring.
    private static int _DO_NOT_OPEN = 0;
    private static int _OPEN_IN_VERGIL = 1;
    private static int _OPEN_IN_VERGIL_FULL_SCREEN = 2;
    private static int _OPEN_RUN_CONTROL_PANEL = 3;

    /** The value of the openOnFiring parameter. */
    private transient int _openOnFiringValue = _DO_NOT_OPEN;

    // Possible values for closeOnPostfire.
    private static int _DO_NOTHING = 0;
    private static int _CLOSE_VERGIL_GRAPH = 1;

    /** The value of the closeOnPostfire parameter. */
    private transient int _closeOnPostfireValue = _DO_NOTHING;

    // Tableau that has been created (if any).
    private Tableau _tableau;
}
