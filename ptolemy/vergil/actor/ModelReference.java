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

package ptolemy.vergil.actor;

import java.net.URL;

import javax.swing.JFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.basic.ExtendedGraphFrame;

//////////////////////////////////////////////////////////////////////////
//// ModelReference
/**
An atomic actor that executes a model specified by a file or URL.
<p>
FIXME: More details.
<P>

@author Edward A. Lee
@version $Id$
*/
public class ModelReference extends TypedAtomicActor {

    /** Construct a ModelReference with a name and a container.
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
    public ModelReference(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: Need a way to specify a filter for the file browser.
        modelFileOrURL = new FileAttribute(this, "modelFileOrURL");

        // Create the executionOnFiring parameter.
        executionOnFiring = new StringAttribute(this, "executionOnFiring");
        // Set the options for the parameters.
        ChoiceStyle style = new ChoiceStyle(executionOnFiring, "choiceStyle");
        new StringAttribute(style, "runInCallingThread").setExpression(
            "run in calling thread");
        new StringAttribute(style, "runInNewThread").setExpression(
            "run in a new thread");
        new StringAttribute(style, "doNothing").setExpression("do nothing");

        // Create the openOnFiring parameter.
        openOnFiring = new StringAttribute(this, "openOnFiring");
        // Set the options for the parameters.
        ChoiceStyle style2 = new ChoiceStyle(openOnFiring, "choiceStyle");
        new StringAttribute(style2, "doNotOpen").setExpression("do not open");
        new StringAttribute(style2, "openInVergil").setExpression(
            "open in Vergil");
        new StringAttribute(style2, "openInVergilFullScreen").setExpression(
            "open in Vergil (full screen)");
        new StringAttribute(style2, "openRunControlPanel").setExpression(
            "open run control panel");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The amount of time (in milliseconds) to linger in the fire()
     *  method of this actor.  This is a long that defaults to 0L.
     *  If the model is run, then the linger occurs after the run
     *  is complete.
     */
    public Parameter lingerTime;

    /** The file name or URL of the model that this actor represents.
     */
    public FileAttribute modelFileOrURL;

    /** The value of this string attribute determines what execution
     *  happens when the fire() method is invoked.  The recognized
     *  values are:
     *  <ul>
     *  <li> "run in calling thread" (the default)
     *  <li> "run in a new thread"
     *  <li> "do nothing".
     *  </ul>
     */
    public StringAttribute executionOnFiring;

    /** The value of this string attribute determines what open
     *  happens when the fire() method is invoked.  The recognized
     *  values are:
     *  <ul>
     *  <li> "do not open" (the default)
     *  <li> "open in Vergil"
     *  <li> "open in Vergil (full screen)"
     *  <li> "open run control panel"
     *  </ul>
     */
    public StringAttribute openOnFiring;

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
        if (attribute == modelFileOrURL) {
            // Open the file and read the MoML to create a model.
            URL url = modelFileOrURL.asURL();
            if (url != null) {
                MoMLParser parser = new MoMLParser(workspace());
                try {
                    _model = parser.parse(null, url);
                    _modelChanged = true;
                } catch (Exception ex) {
                    throw new IllegalActionException(
                        this,
                        ex,
                        "Failed to read model.");
                }
            }
        } else if (attribute == executionOnFiring) {
            String executionOnFiringValue = executionOnFiring.getExpression();
            if (executionOnFiringValue.equals("run in calling thread")) {
                _executionOnFiringValue = _RUN_IN_CALLING_THREAD;
            } else if (executionOnFiringValue.equals("run in a new thread")) {
                _executionOnFiringValue = _RUN_IN_A_NEW_THREAD;
            } else {
                _executionOnFiringValue = _DO_NOTHING;
            }
        } else if (attribute == openOnFiring) {
            String openOnFiringValue = openOnFiring.getExpression();
            if (openOnFiringValue.equals("do not open")) {
                _openOnFiringValue = _DO_NOT_OPEN;
            } else if (openOnFiringValue.equals("open in Vergil")) {
                _openOnFiringValue = _OPEN_IN_VERGIL;
            } else if (
                openOnFiringValue.equals("open in Vergil (full screen)")) {
                _openOnFiringValue = _OPEN_IN_VERGIL_FULL_SCREEN;
            } else if (openOnFiringValue.equals("open run control panel")) {
                _openOnFiringValue = _OPEN_RUN_CONTROL_PANEL;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Run a complete execution of the referenced model.  A complete
     *  execution consists of invocation of super.initialize(), repeated
     *  invocations of super.prefire(), super.fire(), and super.postfire(),
     *  followed by super.wrapup().  The invocations of prefire(), fire(),
     *  and postfire() are repeated until either the model indicates it
     *  is not ready to execute (prefire() returns false), or it requests
     *  a stop (postfire() returns false or stop() is called).
     *  Before running the complete execution, this method calls the
     *  director's transferInputs() method to read any available inputs.
     *  After running the complete execution, it calls transferOutputs().
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("---- Firing ModelReference.");
        }

        if (_model instanceof CompositeActor) {
            CompositeActor executable = (CompositeActor) _model;

            Manager manager = null;
            if (_modelChanged) {
                _modelChanged = false;

                // Need an effigy for the model, or else graphical elements
                // of the model will not work properly.  That effigy needs
                // to be contained by the effigy responsible for this actor.
                NamedObj toplevel = toplevel();
                Effigy myEffigy = Configuration.findEffigy(toplevel);

                // If there is no such effigy, then we proceed.
                // The model may have no graphical elements.
                if (myEffigy != null) {
                    Configuration configuration =
                        (Configuration) myEffigy.toplevel();
                    try {
                        // Conditionally show this tableau. The openModel() method
                        // also creates the right effigy.
                        if (_openOnFiringValue == _OPEN_IN_VERGIL
                            || _openOnFiringValue
                                == _OPEN_IN_VERGIL_FULL_SCREEN) {
                            _tableau = configuration.openModel(_model);
                        } else {
                            PtolemyEffigy newEffigy =
                                new PtolemyEffigy(
                                    myEffigy,
                                    myEffigy.uniqueName(_model.getName()));
                            newEffigy.setModel(_model);
                        }
                    } catch (NameDuplicationException ex) {
                        // This should not be thrown.
                        throw new InternalErrorException(ex);
                    }
                }
                manager = new Manager(_model.workspace(), "Manager");
                executable.setManager(manager);
            } else {
                manager = executable.getManager();
            }

            try {
                if (_openOnFiringValue == _OPEN_IN_VERGIL) {
                    _tableau.show();
                } else if (_openOnFiringValue == _OPEN_IN_VERGIL_FULL_SCREEN) {
                    JFrame frame = _tableau.getFrame();
                    if (frame instanceof ExtendedGraphFrame) {
                        ((ExtendedGraphFrame)frame).fullScreen();
                    } else {
                        // No support for full screen.
                        _tableau.show();
                    }
                }
                if (_executionOnFiringValue == _RUN_IN_CALLING_THREAD) {
                    manager.execute();
                } else if (_executionOnFiringValue == _RUN_IN_A_NEW_THREAD) {
                    // FIXME: Listen for exections.
                    manager.startRun();
                }
            } catch (KernelException ex) {
                throw new IllegalActionException(
                    this,
                    ex,
                    "Failed to execute referenced model");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    /** The value of the executionOnFiring parameter. */
    private int _executionOnFiringValue = _RUN_IN_CALLING_THREAD;

    // Possible values for executionOnFiring.
    private static int _DO_NOTHING = 0;
    private static int _RUN_IN_CALLING_THREAD;
    private static int _RUN_IN_A_NEW_THREAD;

    /** Indicator of what the last call to iterate() returned. */
    private int _lastIterateResult = NOT_READY;

    /** The model. */
    private NamedObj _model;

    /** An indicator of whether the model has changes since the last fire(). */
    private boolean _modelChanged = false;

    /** The value of the executionOnFiring parameter. */
    private int _openOnFiringValue = _DO_NOT_OPEN;

    // Possible values for executionOnFiring.
    private static int _DO_NOT_OPEN = 0;
    private static int _OPEN_IN_VERGIL = 1;
    private static int _OPEN_IN_VERGIL_FULL_SCREEN = 2;
    private static int _OPEN_RUN_CONTROL_PANEL = 3;

    // Tableau that has been created (if any).
    private Tableau _tableau;
}