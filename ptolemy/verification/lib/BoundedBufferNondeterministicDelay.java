/* A bounded buffer with nondeterministic dealy.

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
package ptolemy.verification.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BoundedBufferNondeterministicDelay

/**
   A bounded buffer with nondeterministic delay.

   @author Patrick Cheng
   @version $Id$
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class BoundedBufferNondeterministicDelay extends BoundedBufferTimedDelay {

    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BoundedBufferNondeterministicDelay(CompositeEntity container,
            String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    // FIXME: VariableDelay.delay overrides TimedDelay.delay.
    /** The amount specifying delay. Its default value is 1.0.
     */
    //public PortParameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an IllegalActionException to indicate that this actor
     *  is used for code generation only.
     *  @exception IllegalActionException No simulation
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        throw new IllegalActionException(this, getName() + " can not run in "
                + "simulation mode.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    /** Override the method of the super class to initialize the
     *  parameter values.
     */
    @Override
    protected void _init() throws NameDuplicationException,
            IllegalActionException {
        //delay = new PortParameter(this, "delay");
        //delay.setExpression("1.0");
        //delay.setTypeEquals(BaseType.DOUBLE);
        bufferSize = new PortParameter(this, "bufferSize");
        bufferSize.setExpression("1");
        bufferSize.setTypeEquals(BaseType.INT);
    }

}
