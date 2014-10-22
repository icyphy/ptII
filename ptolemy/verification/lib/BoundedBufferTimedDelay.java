/*    A bounded buffer with a timed delay.

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

import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BoundedBufferTimedDelay

/**
   A bounded buffer with a timed delay.

   @author Patrick Cheng
   @version $Id$
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class BoundedBufferTimedDelay extends TimeDelay {

    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BoundedBufferTimedDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The size of the buffer. The default for this parameter is 1.
     *  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter bufferSize;

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == bufferSize) {
            int newBufferSize = ((IntToken) bufferSize.getToken()).intValue();

            if (newBufferSize < 1) {
                throw new IllegalActionException(this,
                        "Cannot have buffer less than one: " + newBufferSize);
            } else {
                _bufferSize = newBufferSize;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

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
     *  @exception IllegalActionException If a PortParameter cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has a
     *   PortParameter named "delay" or "bufferSize".
     */
    protected void _init() throws NameDuplicationException,
            IllegalActionException {
        delay = new PortParameter(this, "delay");
        delay.setExpression("1.0");
        delay.setTypeEquals(BaseType.DOUBLE);
        bufferSize = new PortParameter(this, "bufferSize");
        bufferSize.setExpression("1");
        bufferSize.setTypeEquals(BaseType.INT);
    }

    /** The amount of buffer size.
     */
    protected int _bufferSize;
}
