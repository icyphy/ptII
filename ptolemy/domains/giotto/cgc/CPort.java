/* A TypedIOPort with parameters specific to the Giotto domain.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

 review sendInside
 */
package ptolemy.domains.giotto.cgc;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// TypedIOPort

/**
 This port class is used with actors that are instances of CActor.
 It features additional parameters specific to the
 Giotto domain. If the port is specified as an output port, then
 it has two parameters specified by default.
 <ul>
 <li> The parameter <i>initialOutputValue</i> which specifies
 the value of the port
 until the container has fired and assigned it a value. The
 default value of this parameter is 0. It is constrained to
 have the same type as this port.</li>
 <li> The parameter <i>arrayLength</i>, which is used only if the type is
 an array. This specifies the length of the array. The default
 value of this parameter is 1.</li>
 </ul>

 @author N.Vinay Krishnan, Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (vkris)
 @Pt.AcceptedRating Red (cxh)
 */
public class CPort extends TypedIOPort {
    /** Construct a CPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must be an instance of CActor, or an exception will be thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container is not an instance
     *   of CActor.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public CPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        initialOutputValue = new Parameter(this, "initialOutputValue");
        initialOutputValue.setExpression("0");

        arrayLength = new Parameter(this, "arrayLength");
        arrayLength.setExpression("1");

        // FIXME: The type of the initialOutputValue parameter
        // should be constrained to be the same as the type
        // of this port, but only if the port is an output
        // port.  How to do that?
    }

    /** Construct a CPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container
     *  must be an instance of CActor, or an exception will be thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container is not an instance
     *   of CActor.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public CPort(ComponentEntity container, String name, boolean isInput,
            boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        this(container, name);
        setInput(isInput);
        setOutput(isOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The initial value of the port. */
    public Parameter initialOutputValue = null;

    /** The length of the array if the type is an array. */
    public Parameter arrayLength = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to add the functionality that if the
     *  port if made an output port, then make visible the two parameters
     *  <i>initialOutputValue</i> and <i>arrayLength</i>.
     *  @param isOutput True to make the port an output.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted (not thrown in this base class).
     */
    @Override
    public void setOutput(boolean isOutput) throws IllegalActionException {
        super.setOutput(isOutput);

        if (isOutput) {
            initialOutputValue.setVisibility(Settable.FULL);
            arrayLength.setVisibility(Settable.FULL);
        } else {
            initialOutputValue.setVisibility(Settable.NONE);
            arrayLength.setVisibility(Settable.NONE);
        }
    }
}
