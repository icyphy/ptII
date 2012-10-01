/* An actor that simulates the master clock in a distributed system.

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

import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.domains.ptides.kernel.Tag;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////MasterClock

/** A model of master clock in a distributed system.
 *  This actor produces double values at its output
 *  port, where the values are the times in its own clock. {@link SlaveClock}
 *  use this information to update their clocks to track that of the master clock.
 *
 *  @author Jia Zou, Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
*/
public class MasterClock extends TypedAtomicActor {

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
    public MasterClock(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        trigger = new TypedIOPort(this, "trigger", true, false);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort output;

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public  methods                   ////

    /** Produces an double, which is the time kept track by its clock, whenever
     *  an input is received.
     */
    public void fire() throws NoTokenException, IllegalActionException {
        if (trigger.hasToken(0)) {
            trigger.get(0);
            Tag tag = ((PtidesBasicDirector) getDirector())
                    .getPlatformPhysicalTag(((PtidesBasicDirector) getDirector()).platformTimeClock);
            Token token = new DoubleToken(tag.timestamp.getDoubleValue());
            output.send(0, token);
        }
    }

    /** Ensure the director for this actor is a Ptides director.
     *  Instantiate a new clock, and make it the platform clock of the
     *  Ptides director.
     */
    public void initialize() throws IllegalActionException {
        Director director = getDirector();
        if (!(director instanceof PtidesBasicDirector)) {
            throw new IllegalActionException(this, "This actor can only "
                    + "work under a Ptides director.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Do not establish the usual default type constraints. Instead, the type
     * of the output port is constrained to be double (set in the constructor
     * of this class).
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }
}
