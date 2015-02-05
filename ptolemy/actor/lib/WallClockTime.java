/* Output the elapsed time in seconds.

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
package ptolemy.actor.lib;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 Upon firing, this actor outputs the elapsed real time in seconds
 since the start time of the model, as reported by the director.
 The output type is double.
 The resolution of time depends on the implementation of the Java
 virtual machine, but with Sun's JDK 1.3 under Windows 2000, it is
 10 milliseconds.
 <p>
 Note that relying on the data produced by this actor is tricky
 in domains where you do not have precise control over the
 scheduling, since the output reflects the wall-clock time at
 which this actor is fired, which may or may not be indicative
 of the times at which other actors fire.  So that you can get
 more control over the schedule, the input provided at the
 <i>trigger</i> port is passed through to the <i>passThrough</i>
 output port.  This can be used to ensure that this actor
 fires before another downstream actor.

 @see Director#elapsedTimeSinceStart()
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class WallClockTime extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WallClockTime(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        passThrough = new TypedIOPort(this, "passThrough", false, true);
        passThrough.setTypeAtLeast(trigger);
        passThrough.setMultiport(true);
        passThrough.setWidthEquals(trigger, true);

        // Override the clock to make it look a bit
        // different from the DiscreteClock and PoissonClock.
        _attachText(
                "_iconDescription",
                "<svg>\n"
                        + "<rect x=\"-20\" y=\"-20\" "
                        + "width=\"40\" height=\"40\" "
                        + "style=\"fill:lightGrey\"/>\n"
                        + "<circle cx=\"0\" cy=\"0\" r=\"17\""
                        + "style=\"fill:black\"/>\n"
                        + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"-13\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"14\" x2=\"0\" y2=\"16\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"-15\" y1=\"0\" x2=\"-13\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"14\" y1=\"0\" x2=\"16\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"-8\" x2=\"0\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\" style=\"stroke:white\"/>\n"
                        + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                        ports and parameters               ////

    /** The output port to which the <i>trigger</i> input is passed.
     *  The type is the same as the type of the <i>trigger</i> port,
     *  which is undeclared, meaning that it will resolve to any type.
     */
    public TypedIOPort passThrough;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set the type constraints on the ports.
     *  @param workspace The workspace for the cloned object.
     *  @return A new instance of WallClockTime.
     *  @exception CloneNotSupportedException If a derived class includes
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WallClockTime newObject = (WallClockTime) super.clone(workspace);
        newObject.passThrough.setTypeAtLeast(newObject.trigger);
        newObject.passThrough.setWidthEquals(newObject.trigger, true);
        return newObject;
    }

    /** Output the elapsed time in seconds since the invocation
     *  of the initialize() method.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        // NOTE: Do not call super.fire() because it reads the trigger
        // input.
        if (_debugging) {
            _debug("Called fire()");
        }

        output.broadcast(new DoubleToken(_getCurrentTime()));

        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                Token token = trigger.get(i);

                if (i < passThrough.getWidth()) {
                    passThrough.send(i, token);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the elapsed time since the model starts.
     *  @return A double value representing the elapsed time.
     */
    protected double _getCurrentTime() {
        // Note that we need not to use the actor.util.Time class
        // here because if we do, it breaks deep codegen because
        // deep codegen removes the Actor classes, and actor.util.Time
        // needs to keep track of the Director.
        return getDirector().elapsedTimeSinceStart() / 1000.0;
    }
}
