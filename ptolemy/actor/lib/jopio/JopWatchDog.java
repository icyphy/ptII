/* Watch dog output for JOP.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jopio;

import ptolemy.actor.lib.Sink;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// JopWatchDog

/**
 <p>
 Watch dog output for the Java processor JOP (see http://www.jopdesign.com).
 The watch dog has to be triggered at least all 1.6 seconds. Best is to trigger
 it once a second, which gives a calming blinking at 0.5 Hz.
 <p>
 In the simulation the watch dog LED is just printed to standard out with
 '*' and 'o'.

 @author Martin Schoeberl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mschoebe)
 @Pt.AcceptedRating Red (mschoebe)
 */
public class JopWatchDog extends Sink {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public JopWatchDog(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        //        trigger = new TypedIOPort(this, "trigger", true, false);
        input.setTypeEquals(BaseType.BOOLEAN);
        input.setMultiport(false);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Trigger the watch dog LED to toggle.
     */
    //    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints on the ports.
     *  @param workspace The workspace into which to clone.
     *  @return A new instance of AddSubtract.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JopWatchDog newObject = (JopWatchDog) super.clone(workspace);
        newObject.input.setTypeEquals(BaseType.BOOLEAN);
        return newObject;
    }

    /** If there is at least one token on the input ports, toggle
     * or set the watch dog LED. A token on the <i>input</i> port sets the LED
     * according to the boolean value. A token on the <i>trigger</i> port
     * toggles the LED.
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _last_val = _val;

        //        if (trigger.hasToken(0)) {
        //            trigger.get(0);
        //            _last_val = !_val;
        //        }
        if (input.hasToken(0)) {
            _last_val = ((BooleanToken) input.get(0)).booleanValue();
        }
    }

    /** Record the most recent input for the watch dog value.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _val = _last_val;
        System.out.print(_val ? '*' : 'o');
        return super.postfire();
    }

    private boolean _last_val;
    private boolean _val;
}
