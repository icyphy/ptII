/* A polymorphic multiplexor with boolean select.

 Copyright (c) 1997-2010 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// BooleanMultiplexor

/**
 <p>A type polymorphic multiplexor with boolean valued select.</p>
 <p>If any input port has no token, the prefire method returns false and
 the fire method is not called. Tokens are only consumed in the fire method,
 where exactly one token is consumed from each input port, and one
 of the tokens from either <i>trueInput</i> or <i>falseInput</i> is sent
 to the output.  The token sent to the output
 is determined by the <i>select</i> input, which must be a boolean value.
 Because tokens are immutable, the same Token
 is sent to the output, rather than a copy.
 The <i>trueInput</i> and <i>falseInput</i> port may receive Tokens of
 any type.</p>
 <p> This actor is different from the BooleanSelect actor, which consumes
 one token from the control input and another token from either the
 trueInput or the falseInput in each firing.</p>
 <p> The actor can also implement non-strict behavior if the <i>isStrict</i>
 parameter is unset: see comments before parameter declaration below.</p> 

 @author Steve Neuendorffer, Stavros Tripakis (added behavior for nonstrictness)
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class BooleanMultiplexor extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public BooleanMultiplexor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        trueInput = new TypedIOPort(this, "trueInput", true, false);
        falseInput = new TypedIOPort(this, "falseInput", true, false);
        select = new TypedIOPort(this, "select", true, false);
        select.setTypeEquals(BaseType.BOOLEAN);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(trueInput);
        output.setTypeAtLeast(falseInput);
        
        new StringAttribute(select, "_cardinal").setExpression("SOUTH");
        
        isStrict = new Parameter(this, "isStrict");
        isStrict.setExpression("true");
        isStrict.setTypeEquals(BaseType.BOOLEAN);        
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
    public TypedIOPort select;

    /** The output port.  The type is at least the type of
     *  <i>trueInput</i> and <i>falseInput</i>
     */
    public TypedIOPort output;

    /** Parameter of type boolean: by default set to true.
     * If false, then actor is non-strict: it can produce an output even
     * when the value of the non-selected data port is unknown.
     * This helps resolve feedback loops in the SR domain, e.g.,
     * see Malik's example under FIXME: add path to model.
     * The value of the <i>select</i> port needs however to be known.
     */
    public Parameter isStrict;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Strict case:
     *  Consume a token from each input port.  If the token from the
     *  <i>select</i> input is true, then output the token consumed from the
     *  <i>trueInput</i> port, otherwise output the token from the
     *  <i>falseInput</i> port.
     *  This method will throw a NoTokenException if any
     *  input channel does not have a token.
     *
     *  Non-strict case: FIXME
     *  
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        boolean control = ((BooleanToken) select.get(0)).booleanValue();
        boolean strict = ((BooleanToken) isStrict.getToken()).booleanValue();

        if (strict) {
            Token trueToken = trueInput.get(0);
            Token falseToken = falseInput.get(0);

            if (control) {
                output.send(0, trueToken);
            } else {
                output.send(0, falseToken);
            }
        }
        else {
            if (control)
                output.send(0, trueInput.get(0));
            else
                output.send(0, falseInput.get(0));
        }
    }

    /** Strict case:
     *  Return false if any input channel does not have a token.
     *  Otherwise, return whatever the superclass returns.
     *  Non-strict case: FIXME
     *  @return False if there are not enough tokens to fire or the prefire
     *  method of the super class returns false.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!select.hasToken(0)) {
            return false;
        }

        boolean strict = ((BooleanToken) isStrict.getToken()).booleanValue();
        
        if (strict) {
            if (!trueInput.hasToken(0)) {
                return false;
            }

            if (!falseInput.hasToken(0)) {
                return false;
            }
            
            return super.prefire();
        }
        else /* non-strict */ {
            boolean control = ((BooleanToken) select.get(0)).booleanValue();
            if (!super.prefire())
                return false;
            else if (control) {
                if (!trueInput.isKnown())
                    return false;
                return trueInput.hasToken(0);
            }
            else {
                if (!falseInput.isKnown())
                    return false;
                return falseInput.hasToken(0);
            }
        }
    }
    
    /** Return true iff isStrict parameter is set.
     *      
     */
    public boolean isStrict() {
        boolean result = true;
        try {
            result = (((BooleanToken) isStrict.getToken()).booleanValue());
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     *  
     *  FIXME: How should the dependency/causality analysis be modified in the non-strict case?
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(input, output);
    }
     */

}
