/* An actor that provides a virtual enviorment to run tinyOS code directly.

 Copyright (c) 2004-2005 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.wireless.lib.tinyOS;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;

//////////////////////////////////////////////////////////////////////////
//// VirtualTinyOS

/** Provide a virtual enviorment to run tinyOS code directly...
 FIXME: this class hasn't been fully implemented.
 FIXME: add more doc here.
 @author Yang Zhao, Xiaojun Liu
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red(ellen_zh)
 @Pt.AcceptedRating Red (cxh)
 */
public class VirtualTinyOS extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VirtualTinyOS(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException,
            java.lang.Exception {
        super(container, name);

        //timer = new TypedIOPort(this, "timer", true, false);
        //toLED = new TypedIOPort(this, "toLED", false, true);
        //create the node icon.
        EditorIcon node_icon = new EditorIcon(this, "_icon");

        // The icon has two parts: a circle and an antenna.
        // Create a circle that indicates the signal radius.
        _circle = new EllipseAttribute(node_icon, "_circle");
        _circle.centered.setToken("true");
        _circle.width.setToken("50");
        _circle.height.setToken("50");
        _circle.fillColor.setToken("{0.0, 0.0, 1.0, 0.08}");
        _circle.lineColor.setToken("{0.0, 0.5, 0.5, 1.0}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */

    //public TypedIOPort toLED;
    /** The input port for timer interupt. */

    //public TypedIOPort timer;
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        //tos = new TosSystem(this);
        UtilityFunctions.loadLibrary("ptolemy/domains/wireless/lib/tinyOS/TOS");
        _hasLed = false;
        _hasTimer = false;
        _timerPeriod = 0.0;
        _scheduledTime = new Time(getDirector());

        //call the native method to initialize the application.
        initMote();
    }

    /** pass the input to the proper event handler.
     *  @exception IllegalActionException something goes wrong.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // deal with timer interupt
        if (_debugging) {
            _debug("Called fire()");
        }

        Director director = getDirector();

        //If there is a timer component, we handel the timer interupt here.
        if (_hasTimer && director.getModelTime().compareTo(_scheduledTime) >= 0) {
            if (_debugging) {
                _debug("Called native method to trigger the time event");
            }

            // signal a timer interupt here to the application.
            triggerTimerEvent();
            _scheduledTime = _scheduledTime.add(_timerPeriod);
            _fireAt(_scheduledTime);
        } else if (_hasLed) {
            if (_debugging) {
                _debug("LED Blinking");
            }

            //toLED.send(0, new IntToken(_outputToLED));
            // Change the color of the icon to red.
            _circle.fillColor.setToken("{1.0, 0.0, 0.1, 0.7}");
            _hasLed = false;
            _fireAt(director.getModelTime().add(0.5));
        } else {
            // Set color back to blue.
            _circle.fillColor.setToken("{0.0, 0.0, 1.0, 0.05}");
        }
    }

    // a callback method for the native code. Potentially, the mote code can
    // call this method to render the led toggle animation.
    public void ledBlink(int x) {
        Director director = getDirector();

        if (director != null) {
            try {
                _hasLed = true;

                Time currentTime = director.getModelTime();
                _fireAt(currentTime);

                //then change the color of the node in fire();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public int triggerTimerEvent() {
        System.out
        .println("about to call the native method to signal an event");

        int r = signalTimerEvent();
        System.out.println("return from the native method");
        return r;
    }

    // A callback method for the application to notify this of the timer settings.
    // FIXME: should be able to handle muti-timers...
    public void setupTimer(int period) throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();

        if (period >= 0) {
            try {
                _fireAt(currentTime);
                _hasTimer = true;
                _timerPeriod = period;
                _scheduledTime = currentTime;
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Must specify port types using moml (TypeAttribute) - the default
     *  TypedAtomicActor type constraints do not apply in this case, since the
     *  input type may be totally unrelated to the output type and cannot be
     *  inferred.
     *  @return null
     */
    public Inequality _defaultConstraints() {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private native int signalTimerEvent();

    private native void initMote();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _hasTimer = false;

    private double _timerPeriod;

    private Time _scheduledTime;

    private boolean _hasLed;

    /** Icon indicating the led blinking. */
    private EllipseAttribute _circle;
}
