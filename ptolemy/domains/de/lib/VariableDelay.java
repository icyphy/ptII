/* An actor that delays the input by the amount specified through another port.

Copyright (c) 1998-2004 The Regents of the University of California.
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
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// VariableDelay
/**
   This actor extends from the ptolemy.domains.de.lib.TimedDelay actor.
   It works in a similar way as the TimedDelay actor except that the 
   amount of time delayed is specified by an incoming token through 
   the delay port parameter.

   @see ptolemy.actor.util.FunctionDependency
   @see ptolemy.domains.de.lib.TimedDelay
   @see ptolemy.domains.de.lib.Server
   @see ptolemy.domains.sdf.lib.SampleDelay
   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (liuj)
*/
public class VariableDelay extends TimedDelay {

    /** Construct an actor with the specified container and name.
     *  Specify the IODependence attribute of the input and output ports.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
 
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" "
                + "width=\"60\" height=\"20\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount specifying delay.
     */
    public PortParameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        VariableDelay newObject = (VariableDelay)super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Read one token from the input and save it so that the
     *  postfire method can produce it to the output.
     *  Read one token from the delay input, if there is any.
     *  Use the new delay for later input tokens.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        delay.update();
        _delay = ((DoubleToken)delay.getToken()).doubleValue();
        // NOTE: the newDelay may be 0.0, which may change
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

    // Initialize the value for parameter.
    protected void _init() 
        throws NameDuplicationException, IllegalActionException  {
        delay = new PortParameter(this, "delay");
        delay.setExpression("1.0");
        delay.setTypeEquals(BaseType.DOUBLE);
        output.setTypeSameAs(input);
    }
}
