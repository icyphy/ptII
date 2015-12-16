/* An actor that generates an empty token in response to a click of a button.

 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.gui;

import java.awt.Frame;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// EventButton

/**
 Output a token when the actor is fired.
 This actor customizes its interaction to request a firing
 whenever the icon is double clicked.
 
 By default, the value of the output is a boolean true.
 To change this, Alt-click on the icon.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (winthrop)
 @Pt.AcceptedRating Red (winthrop)
 */
public class EventButton extends TypedAtomicActor {

    /** Construct an actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public EventButton(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        output = new TypedIOPort(this, "output", false, true);
        
        value = new Parameter(this, "value");
        value.setExpression("true");
        
        output.setTypeAtLeast(value);
        
        new DoubleClickHandler(this, "_doubleClickHandler");
        
        buttonPressed = new Parameter(this, "buttonPressed");
        buttonPressed.setTypeEquals(BaseType.BOOLEAN);
        buttonPressed.setExpression("false");
        buttonPressed.setVisibility(Settable.NONE);
        
        pressDuration = new Parameter(this, "pressDuration");
        pressDuration.setExpression("0.2");
        pressDuration.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////
    
    /** Hidden parameter controlling the visual rendition of the button.
     */
    public Parameter buttonPressed;

    /** The output port.  The type of this port is the same as that of
     *  the value parameter.
     */
    public TypedIOPort output;
    
    /** Amount of time to keep the button depressed, in seconds.
     *  Additional button presses during this time will be ignored.
     *  This is a double with default value 0.2.
     */
    public Parameter pressDuration;
    
    /** The value produced. This is a boolean true by default.
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  This method sets the type constraint of the output
     *  to be at least the type of the value. 
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        EventButton newObject = (EventButton) super.clone(workspace);

        // set the type constraints.
        newObject.output.setTypeAtLeast(newObject.value);
        return newObject;
    }

    /** Fire the actor.
     */
    @Override
    public synchronized void fire() throws IllegalActionException {
        super.fire();
        
        // Do not produce an output if the purpose of the firing is
        // to restore the button.
        // Here, we assume that firings occur _only_ in response
        // to double clicks.
        if (_bounceBackTime != null) {
            // Restore the button.
            _setButtonPressed(false);
            _bounceBackTime = null;
        } else {
            output.broadcast(value.getToken());
            
            Director director = getDirector();
            Time currentTime = director.getModelTime();
            
            double pressDurationValue = ((DoubleToken)pressDuration.getToken()).doubleValue();
            // Mark that the button is depressed.
            _bounceBackTime = currentTime.add(pressDurationValue);
            // Request a firing in the future to restore the button.
            director.fireAt(EventButton.this, _bounceBackTime);

            // Mark the button pressed.
            _setButtonPressed(true);
        }
    }
    
    /** Mark that the model is now executing. 
     *  @throws IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _running = true;
        _bounceBackTime = null;
    }

    /** Mark that the model is no longer executing. 
     *  @throws IllegalActionException If the superclass throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _running = false;
        _setButtonPressed(false);
        _bounceBackTime = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Set whether the button is pressed.
     *  @param pressed True to be pressed.
     */
    private void _setButtonPressed(boolean pressed) {
        String moml = "<property name=\"buttonPressed\" value=\""
                + pressed
                + "\"/>";
        MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
        request.setPersistent(false);
        requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    /** Time at which the button should revert to full size. */
    private Time _bounceBackTime;
    
    /** Indicator that the model is running. */
    private boolean _running;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** Class to respond to double click. */
    class DoubleClickHandler extends EditorFactory {

        public DoubleClickHandler(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Respond to double click. */
        @Override
        public void createEditor(NamedObj object, Frame parent) {
            if (!_running) {
                try {
                    MessageHandler.warning("Model is not running. No output produced.");
                } catch (CancelException e) {
                    // Ignore.
                }
                return;
            }
            Director director = getDirector();
            if (director != null) {
                try {
                    // This will be called in the Swing event thread.
                    // Make sure that the actor is not currently firing.
                    synchronized(EventButton.this) {
                        if (_bounceBackTime != null) {
                            // Ignore.  Still in the interval of the previous button press.
                            return;
                        }
                        // Request a firing now to produce an output.
                        director.fireAtCurrentTime(EventButton.this);
                    }
                } catch (IllegalActionException e) {
                    MessageHandler.error("Director is unable to fire the actor as requested.", e);
                }
            } else {
                MessageHandler.error("No director!");
            }
        }
    }
}
