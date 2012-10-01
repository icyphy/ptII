/* An actor that simulates a slave clock in a distributed system.

 Copyright (c) 1998-2012 The Regents of the University of California.
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
package ptolemy.domains.ptides.lib;

import ptolemy.actor.Director;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.domains.ptides.kernel.Tag;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////SlaveClock

/** A model of slave clock in a distributed system.
 *  This actor assumes that it receives double values at its input
 *  port, where the values are the current times from a {@link MasterClock}.
 *  It then updates its own clock to track that of the master clock.
 *
 *  @author Jia Zou, Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class SlaveClock extends TypedAtomicActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public SlaveClock(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);

        // set type constraints
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public  methods                   ////

    /** If an input is present, produce a DoubleToken at the output, which
     *  represents the current platform time at this slave clock.
     */
    public void fire() throws NoTokenException, IllegalActionException {
        super.fire();
        DoubleToken token;
        if (input.hasToken(0)) {
            token = (DoubleToken) input.get(0);
        } else {
            return;
        }
        PtidesBasicDirector director = (PtidesBasicDirector) getDirector();
        PtidesBasicDirector.RealTimeClock realTimeClock = director.platformTimeClock;
        Tag platformTag = director.getPlatformPhysicalTag(realTimeClock);
        if (token.doubleValue() > platformTag.timestamp.getDoubleValue()) {
            realTimeClock.updateClockDrift(1.3);
        } else if (token.doubleValue() < platformTag.timestamp.getDoubleValue()) {
            realTimeClock.updateClockDrift(0.7);
        } else {
            realTimeClock.updateClockDrift(1.0);
        }
        output.send(0, new DoubleToken(platformTag.timestamp.getDoubleValue()));
    }

    /** Make sure the director for this actor is a PtidesBasicDirector.
     *  Create a RealTimeClock, and make it the enclosing PtidesBasicDirector's
     *  platform clock.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Director director = getDirector();
        if (!(director instanceof PtidesBasicDirector)) {
            throw new IllegalActionException(this, "This actor can only "
                    + "work under a Ptides director.");
        }
    }

}
