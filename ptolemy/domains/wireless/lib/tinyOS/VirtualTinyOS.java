/* An actor that evaluates matlab expressions with input ports
   providing variables

 Copyright (c) 1998-2003 The Regents of the University of California and
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

@ProposedRating Yellow (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib.tinyOS;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// VirtualTinyOS
/** Provide a virtual enviorment to run tinyOS code directly...
FIXME: this class hasn't been fully implemented.
FIXME: add more doc here.
@author Yang Zhao, Xiaojun Liu
@version $ $
@since Ptolemy II 3.0
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
            java.lang.Exception  {
        super(container, name);
        //timer = new TypedIOPort(this, "timer", true, false);
        toLED = new TypedIOPort(this, "toLED", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */
    public TypedIOPort toLED;

    /** The input port for timer interupt. */
    //public TypedIOPort timer;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Must specify port types using moml (TypeAttribute) - the default
     *  TypedAtomicActor type constraints do not apply in this case, since the
     *  input type may be totally unrelated to the output type and cannot be
     *  inferred; return an empty list. */
    public List typeConstraintList()  {
        LinkedList result = new LinkedList();
        return result;
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        //tos = new TosSystem(this);
        UtilityFunctions.loadLibrary("ptolemy/domains/wireless/lib/TOS");
        _outputToLED = -1;
        _hasTimer = false;
        _timerPeriod = 0.0;
        _scheduledTime = 0.0;
        //call the native method to initialize the application.
        initMote();
       
    }

    /** pass the input to the proper event handler.
     *  @exception IllegalActionException something goes wrong.
     */
    public void fire() throws IllegalActionException {
        // deal with timer interupt
        if (_debugging) {
            _debug("Called fire()");
        }
        Director director = getDirector();
        //If there is a timer component, we handel the timer interupt here.
        if (_hasTimer && director.getCurrentTime()>= _scheduledTime) {
           
               if (_debugging) {
                   _debug("Called native method to trigger the time event");
               }
               // signal a timer interupt here to the application.
               triggerTimerEvent();
               _scheduledTime = _scheduledTime + _timerPeriod;
               director.fireAt(this, _scheduledTime);           
        } 
        
        if (_outputToLED >= 0) {
            if (_debugging) {
                _debug("output to LED");
            }
            toLED.send(0, new IntToken(_outputToLED));
            _outputToLED = -1;
        }
    }

    // a callback method for the native code. Potentially, the mote code can
    // call this method to render the led toggle animation.
    public void outputToLED(int s) {
        Director director = getDirector();
        if (director != null) {
            try {
                _outputToLED = s;
                director.fireAtCurrentTime(this);
                //then change the color of the node in fire();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
     }
    
    public int triggerTimerEvent() {
         System.out.println("about to call the native method to signal an event");
         int r = signalTimerEvent();
         System.out.println("return from the native method");
         return r;
    }
    
    // A callback method for the application to notify this of the timer settings.
    // FIXME: should be able to handle muti-timers... 
    public void setupTimer(int period) {
        double currentTime = getDirector().getCurrentTime();
        if (period >=0) {
            try {
                getDirector().fireAt(this, currentTime);
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
    ////                         private methods                 ////

    private native int signalTimerEvent();
    private native void initMote();
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////
    private boolean _hasTimer = false;
    private double _timerPeriod;
    private double _scheduledTime;
    private int _outputToLED;
}
