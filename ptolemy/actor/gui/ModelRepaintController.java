/**  A controller to do scheduled repaints at certain user specified moments. */

/*
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
 */

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ModelRepaintController

/**
 * A class to do scheduled repaints at certain user specified moments in
 * the execution of the model. This component piggy-backs with the execution
 * of its container, and allows you to define that repaints need to happen when
 * certain functions of its container are executed. Currently only repaints at
 * wrapup or post-fire can be scheduled.
 *
 * @author Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (rodiers)
 * @Pt.AcceptedRating Red (rodiers)
 */
public class ModelRepaintController extends Attribute {
    /** Construct an instance of the ModelRepaintController.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ModelRepaintController(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        _repaintOnWrapUp = new Parameter(this, "repaintOnWrapUp");
        _repaintOnWrapUp.setTypeEquals(BaseType.BOOLEAN);
        _repaintOnWrapUp.setExpression("true");

        _repaintOnPostFire = new Parameter(this, "repaintOnPostFire");
        _repaintOnPostFire.setTypeEquals(BaseType.BOOLEAN);
        _repaintOnPostFire.setExpression("false");

        // The icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-60\" y=\"-10\" " + "width=\"135\" height=\"20\" "
                + "style=\"fill:#00FFFF\"/>\n" + "<text x=\"-55\" y=\"5\" "
                + "style=\"font-size:14; font-family:SansSerif; fill:blue\">\n"
                + "RepaintController\n" + "</text>\n" + "</svg>\n");

        // Hide the name.
        SingletonParameter hideName = new SingletonParameter(this, "_hideName");
        hideName.setToken(BooleanToken.TRUE);
        hideName.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the controller into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new controller
     *  @param workspace The workspace for the new controller
     *  @return A new controller
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelRepaintController newObject = (ModelRepaintController) super
                .clone(workspace);
        newObject._executable = null;
        newObject._repaintOnPostFire = (Parameter) newObject
                .getAttribute("repaintOnPostFire");
        newObject._repaintOnWrapUp = (Parameter) newObject
                .getAttribute("repaintOnWrapUp");
        return newObject;
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  If the container already
     *  contains an attribute with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this object being garbage collected.
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.  This method is write-synchronized on the
     *  workspace and increments its version number.
     *
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if (_executable == null) {
            // The inner class will be piggybacked as an executable for the container to
            // execute change request at the appropriate times. These change request will
            // lead to repaints of the GUI.
            _executable = new Executable() {
                @Override
                public void fire() throws IllegalActionException {
                }

                @Override
                public boolean isFireFunctional() {
                    return true;
                }

                @Override
                public boolean isStrict() throws IllegalActionException {
                    return true;
                }

                @Override
                public int iterate(int count) throws IllegalActionException {
                    return Executable.COMPLETED;
                }

                @Override
                public boolean postfire() throws IllegalActionException {
                    _scheduleRepaint(_repaintOnPostFire);
                    return true;
                }

                @Override
                public boolean prefire() throws IllegalActionException {
                    return true;
                }

                @Override
                public void stop() {
                }

                @Override
                public void stopFire() {
                }

                @Override
                public void terminate() {
                }

                @Override
                public void addInitializable(Initializable initializable) {
                }

                @Override
                public void initialize() throws IllegalActionException {
                }

                @Override
                public void preinitialize() throws IllegalActionException {
                }

                @Override
                public void removeInitializable(Initializable initializable) {
                }

                @Override
                public void wrapup() throws IllegalActionException {
                    _scheduleRepaint(_repaintOnWrapUp);
                }
            };
        }

        // Don't piggy back on previous container anymore.
        NamedObj previousContainer = getContainer();
        if (previousContainer instanceof CompositeActor) {
            ((CompositeActor) previousContainer).removePiggyback(_executable);
        }
        super.setContainer(container);

        if (container instanceof CompositeActor) {
            ((CompositeActor) container).addPiggyback(_executable);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Schedule a repaint in case the value parameter equals True.
     *  This is done by requesting a ChangeRequest.
     *  @param parameter The parameter.
     */
    private void _scheduleRepaint(Parameter parameter) {
        if (parameter.getExpression().equals("true")) {
            // The ChangeRequest has false as third argument to avoid complete
            // repaints of the model.
            ChangeRequest request = new ChangeRequest(this,
                    "SetVariable change request", true) {
                @Override
                protected void _execute() throws IllegalActionException {
                }
            };

            // To prevent prompting for saving the model, mark this
            // change as non-persistent.
            request.setPersistent(false);
            requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private parameters                   ////

    // An inner class that will be notified on certain events.
    // The inner class will be piggybacked as an executable for the container to
    // execute change request at the appropriate times. These change request will
    // lead to repaints of the GUI.
    private Executable _executable;

    // A flag that specifies whether a repaint should happen on wrapup.
    private Parameter _repaintOnWrapUp;

    // A flag that specifies whether a repaint should happen on post-fire.
    private Parameter _repaintOnPostFire;
}
