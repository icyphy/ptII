/**  A controller to do scheduled repaints at certain user specified moments. */

/*
 Copyright (c) 2008 The Regents of the University of California.
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
import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// ModelRepaintController

/**
 * A class to do scheduled repaints at certain user specified moments in
 * the execution of the model. This component piggy-backs with the execution
 * of its container, and allows you to define that repaints need to happen when
 * certain functions of its container are executed. Currently only repaints at
 * wrapup or post-fire can be scheduled. 
 *
 * @author Bert Rodiers
 * @version $Id: ModelRepaintController.java 49562 2008-05-26 04:25:04Z rodiers $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (rodiers)
 * @Pt.AcceptedRating Red (rodiers)
 */
public class ModelRepaintController extends ScopeExtendingAttribute {
    /** Construct an instance of the ModelRepaintController.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception IllegalActionException If the container is null.
     *  @exception IllegalActionException If the container is not a CompositeActor.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ModelRepaintController(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Names of the parameters start with underscores by convention to minimize
        // the probability of conflict with model-specific parameters.
        _repaintOnWrapUp = new Parameter(this, "_repaintOnWrapUp");
        _repaintOnWrapUp.setTypeEquals(BaseType.BOOLEAN);
        _repaintOnWrapUp.setExpression("true");
        
        _repaintOnPostFire = new Parameter(this, "_repaintOnPostFire");
        _repaintOnPostFire.setTypeEquals(BaseType.BOOLEAN);
        _repaintOnPostFire.setExpression("false");

        // The icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-60\" y=\"-10\" "
                + "width=\"120\" height=\"20\" "
                + "style=\"fill:#00FFFF\"/>\n"
                + "<text x=\"-55\" y=\"5\" "
                + "style=\"font-size:14; font-family:SansSerif; fill:blue\">\n"
                + "RepaintController\n" + "</text>\n"
                + "</svg>\n");
        
        // Hide the name.
        SingletonParameter _hideName = new SingletonParameter(this, "_hideName");
        _hideName.setToken(BooleanToken.TRUE);
        _hideName.setVisibility(Settable.EXPERT);
        
        if (container == null) {
            throw new IllegalActionException(this,
                    "This attribute should have a container different from null.");
        }                    
        
        // The inner class will be piggybacked as an executable for the container to
        // execute change request at the appropriate times. These change request will
        // lead to repaints of the GUI.
        
        Executable ex = new Executable() {
            public void fire() throws IllegalActionException {
            }

            public boolean isFireFunctional() {
                return true;
            }

            public boolean isStrict() throws IllegalActionException {
                return true;
            }
            public int iterate(int count) throws IllegalActionException {
                return Executable.COMPLETED;
            }
            
            public boolean postfire() throws IllegalActionException {
                _scheduleRepaint(_repaintOnPostFire);
                return true;
            }

            public boolean prefire() throws IllegalActionException {
                return true;
            }

            public void stop() {
            }

            public void stopFire() {
            }

            public void terminate() {
            }

            public void addInitializable(Initializable initializable) {
            }

            public void initialize() throws IllegalActionException {
            }

            public void preinitialize() throws IllegalActionException {
            }

            public void removeInitializable(Initializable initializable) {
            }

            public void wrapup() throws IllegalActionException {
                _scheduleRepaint(_repaintOnWrapUp);
            }
        };
        
        if (container instanceof CompositeActor) {
            ((CompositeActor) container).addPiggyback(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////
    
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

    // A flag that specifies whether a repaint should happen on wrapup.
    Parameter _repaintOnWrapUp;
    
    // A flag that specifies whether a repaint should happen on post-fire.
    Parameter _repaintOnPostFire;
}
