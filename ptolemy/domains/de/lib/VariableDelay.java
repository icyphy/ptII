/* An actor that delays the input by the amount specified through another port.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// VariableDelay

/**
   This actor delays its inputs by a variable delay.
   It works in a similar way as the TimedDelay actor except that the
   amount of time delayed is specified by an incoming token through
   the delay port (a parameter port).

   @see ptolemy.domains.de.lib.TimedDelay
   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Yellow (hyzheng)
*/
public class VariableDelay extends TimedDelay {
    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount specifying delay. Its default value is 1.0.
     */
    public PortParameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the delay parameter from the delay port and ensure the delay
     *  is not negative. Call the fire method of super class to consume
     *  inputs and generate outputs.
     *  @exception IllegalActionException If the super class throws it,
     *  or a negative delay is received.
     */
    public void fire() throws IllegalActionException {
        delay.update();
        _delay = ((DoubleToken) delay.getToken()).doubleValue();

        if (_delay < 0) {
            throw new IllegalActionException("Can not have a "
                    + "negative delay: " + _delay + ". "
                    + "Check whether overflow happens.");
        }

        // NOTE: _delay may be 0.0, which may change
        // the causality property of the model.
        // We leave the model designers to decide whether the
        // zero delay is really what they want.
        super.fire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> or <i>delay</i> ports
     *  in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(delay.getPort(), output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    /** Override the method of the super class to initialize the
     *  parameter values.
     */
    protected void _init()
            throws NameDuplicationException, IllegalActionException {
        delay = new PortParameter(this, "delay");
        delay.setExpression("1.0");
        delay.setTypeEquals(BaseType.DOUBLE);
    }
}
