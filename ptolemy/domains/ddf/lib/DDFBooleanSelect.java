/* A polymorphic multiplexor with boolean select used in DDF domain.

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

package ptolemy.domains.ddf.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

/**
   A type polymorphic select with boolean valued control. In the first 
   iteration, an input token at the <i>control</i> port is read and 
   its value is noted. In the second iteration, if the <i>control</i>
   input read from previous iteration is true, then an input token at
   the <i>trueInput</i> port is read and sent to the output. Likewise 
   with a false input and the <i>falseInput</i> port. It alternates
   between these two kinds of iterations until stopped. The 
   <i>control</i> port must receive boolean Tokens. The <i>trueInput</i> 
   and <i>falseInput</i> port may receive Tokens of any type. 
   Because tokens are immutable, the same Token is sent to the output, 
   rather than a copy.  
   <p>
   Note this actor sends an output token every two iterations. Contrast 
   this with BooleanSelect which sends an output token every iteration. 

   @author Gang Zhou
*/

public class DDFBooleanSelect extends TypedAtomicActor {
    
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DDFBooleanSelect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        StringAttribute controlCardinal;

        trueInput = new TypedIOPort(this, "trueInput", true, false);
        falseInput = new TypedIOPort(this, "falseInput", true, false);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(trueInput);
        output.setTypeAtLeast(falseInput);
        
        trueInputTokenConsumptionRate = 
                new Parameter(trueInput, "tokenConsumptionRate");
        trueInputTokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        trueInputTokenConsumptionRate.setTypeEquals(BaseType.INT);
        
        falseInputTokenConsumptionRate = 
                new Parameter(falseInput, "tokenConsumptionRate");
        falseInputTokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        falseInputTokenConsumptionRate.setTypeEquals(BaseType.INT);

        controlTokenConsumptionRate = 
                new Parameter(control, "tokenConsumptionRate");
        controlTokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        controlTokenConsumptionRate.setTypeEquals(BaseType.INT);

        // Put the control input on the bottom of the actor.
        controlCardinal = new StringAttribute(control, "_cardinal");
        controlCardinal.setExpression("SOUTH");

        /** Make the icon show T, F, and C for trueInput, falseInput
         *  and control.
         */
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-20\" y=\"-20\" "
                + "width=\"40\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-17\" y=\"-3\" "
                + "style=\"font-size:14\">\n"
                + "T \n"
                + "</text>\n"
                + "<text x=\"-17\" y=\"15\" "
                + "style=\"font-size:14\">\n"
                + "F \n"
                + "</text>\n"
                + "<text x=\"-5\" y=\"16\" "
                + "style=\"font-size:14\">\n"
                + "C \n"
                + "</text>\n"
                + "</svg>\n");
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens on the true path.  The type can be anything.
     */
    public TypedIOPort trueInput;
    /** Input for tokens on the false path.  The type can be anything.
     */
    public TypedIOPort falseInput;
    /** Input that selects one of the other input ports.  The type is
     *  BooleanToken.
     */
    public TypedIOPort control;
    /** The output port.  The type is at least the type of
     *  <i>trueInput</i> and <i>falseInput</i>
     */
    public TypedIOPort output;
    /** This parameter provides token consumption rate for true input. 
     */
    public Parameter trueInputTokenConsumptionRate;
    /** This parameter provides token consumption rate for false input. 
     */
    public Parameter falseInputTokenConsumptionRate;
    /** This parameter provides token consumption rate for control. 
     */
    public Parameter controlTokenConsumptionRate;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Read a new token from the <i>control</i> port and note its value
     *  if it hasn't done so. This concludes the current firing. Otherwise 
     *  if the token read from the <i>control</i> port in previous firing 
     *  is true, output the token consumed from the <i>trueInput</i> port. 
     *  Likewise with a false <i>control</i> input and the <i>falseInput</i> 
     *  port. Then reset an internal variable so that it will read from 
     *  <i>control</i> port in the next iteration.
     *  This method will throw a NoTokenException if any input channel 
     *  does not have a token.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created.
     */
    public void fire() throws IllegalActionException {
        if (_isControlRead) {
            if (_control) {
                output.send(0, trueInput.get(0));                
            } else {
                output.send(0, falseInput.get(0));
            }
            _isControlRead = false;
        } else {
            _control = ((BooleanToken)control.get(0)).booleanValue();
            _isControlRead = true;
        }        
    }
    
    /** Initialize this actor and rate parameters so that it will read 
     *  from the <i>control</i> port in the first iteration.
     *  @exception IllegalActionException If setToken() throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isControlRead = false;
        trueInputTokenConsumptionRate.setToken(new IntToken(0));
        falseInputTokenConsumptionRate.setToken(new IntToken(0));
        controlTokenConsumptionRate.setToken(new IntToken(1));       
    }
    
    /** Update rate parameters for the next iteration.
     *  Then return whatever the superclass returns. 
     *  @return True if execution can continue into the next iteration. 
     *  @exception IllegalActionException If setToken() throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (_isControlRead) {
            if (_control) {
                trueInputTokenConsumptionRate.setToken(new IntToken(1));
                falseInputTokenConsumptionRate.setToken(new IntToken(0));
                controlTokenConsumptionRate.setToken(new IntToken(0));
            } else {
                trueInputTokenConsumptionRate.setToken(new IntToken(0));
                falseInputTokenConsumptionRate.setToken(new IntToken(1));
                controlTokenConsumptionRate.setToken(new IntToken(0));
            }
        } else {
            trueInputTokenConsumptionRate.setToken(new IntToken(0));
            falseInputTokenConsumptionRate.setToken(new IntToken(0));
            controlTokenConsumptionRate.setToken(new IntToken(1));
        }
        return super.postfire();
    }
    
    /** Return false if the port it needs to read from in the following
     *  firing does not have a token. 
     *  Otherwise, return whatever the superclass returns.
     *  @return False if there are not enough tokens to fire.
     *  @exception IllegalActionException If the receivers do not support
     *   the query, or if there is no director, and hence no receivers.
     */
    public boolean prefire() throws IllegalActionException {
        if (_isControlRead) {
            if (_control) {
                if (!trueInput.hasToken(0)) {
                    return false;
                }
            } else {
                if (!falseInput.hasToken(0)) {
                    return false;
                }
            }
        } else {
            if (!control.hasToken(0)) {
                return false;
            }
        }        
        return super.prefire();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The most recently read control token.
    private boolean _control;
    
    // The boolean to determine to read control or true/false input.
    private boolean _isControlRead;
}
