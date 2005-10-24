/* Multiplexor for use with ECSL.

Copyright (c) 2004-2005 The Regents of the University of California.
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

package vendors.ecsl_dp.ptolemy;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ECSLMux
/**
   Multiplexor for use with ECSL.

   @author Christopher Brooks.
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/

public class ECSLMux extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ECSLMux(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);

        // FIXME: input and output types forced to DOUBLE?
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        DisplayOption = new StringParameter(this, "DisplayOption");
        Inputs = new StringParameter(this, "Inputs");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Determines how the icon is displayed.  A common value is the
     *  string "bar", which means to display as a bar
     *  <p>FIXME: Currently ignored.
     */
    public StringParameter DisplayOption;

    /** The number of inputs.
     *  <p>FIXME: Currently ignored.
     */
    public StringParameter Inputs;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** FIXME: noop
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        throw new IllegalActionException(this, "fire() not yet supported.");
    }

    /** Set the type of all ports to double
     */
    public void preinitialize() throws IllegalActionException {
        List inputPorts = inputPortList();
        Iterator inputPortsIterator = inputPorts.iterator();
        while (inputPortsIterator.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort)inputPortsIterator.next();
            inputPort.setTypeEquals(BaseType.DOUBLE);
        }

        List outputPorts = outputPortList();
        Iterator outputPortsIterator = outputPorts.iterator();
        while (outputPortsIterator.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort)outputPortsIterator.next();
            outputPort.setTypeEquals(BaseType.DOUBLE);
        }
    }
}
