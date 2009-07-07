/* A polymorphic IfThenElse actor.

 */
package ptolemy.domains.sequence.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// IfThenElse

/**
 <p>A type polymorphic If-Then-Else actor. 
 In an iteration, if an input token is available at the <i>If</i> input,
 that token is read.
 The <i>Then<i> output is set to true, if then <i>If</i> input is true.
 The <i>Else<i> output is set to true, if then <i>If</i> input is false.
 The <i>If</i> port may only receive Tokens of type Boolean. The output 
 ports are also of type boolean.</p>
 
 IfThenElse is a ControlActor, meaning that it keeps a list of
 enabled output ports.  
 */
public class IfThenElse extends ControlActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public IfThenElse(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set name to invisible
        StringAttribute hideName = new StringAttribute(this, "_hideName");
        hideName.setExpression("true");
        
        // create inports
        ifInput = new TypedIOPort(this, "If", true, false);

        // create outports
        thenOutput = new TypedIOPort(this, "Then", false, true);
        elseOutput = new TypedIOPort(this, "Else", false, true);

        // set portnames to visible
        StringAttribute ifShowName = new StringAttribute(ifInput, "_showName");
        ifShowName.setExpression("false");
        StringAttribute thenShowName = new StringAttribute(thenOutput, "_showName");
        thenShowName.setExpression("false");
        StringAttribute elseShowName = new StringAttribute(elseOutput, "_showName");
        elseShowName.setExpression("false");

        // set direction of ports
        StringAttribute ifCardinal = new StringAttribute(ifInput, "_cardinal");
        ifCardinal.setExpression("WEST");
        StringAttribute thenCardinal = new StringAttribute(thenOutput, "_cardinal");
        thenCardinal.setExpression("EAST");
        StringAttribute elseCardinal = new StringAttribute(elseOutput, "_cardinal");
        elseCardinal.setExpression("SOUTH");

        // set type constraints for ports        
        ifInput.setTypeEquals(BaseType.BOOLEAN);
        thenOutput.setTypeEquals(BaseType.BOOLEAN);
        elseOutput.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  The type can only be BOOLEAN.
     */
    public TypedIOPort ifInput;

    /** Same as Input
     */
    public TypedIOPort thenOutput;

    /** Not Input
     */
    public TypedIOPort elseOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from the input port.  
     *  If input = true: Then output = true, Else output = false 
     *  If input = false: Then output = false, Else output = true 
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        if (ifInput.hasToken(0)) {
            BooleanToken token = (BooleanToken) ifInput.get(0);

            // Set the enabled output ports accordingly
            clearEnabledOutports();
            
         // Beth - changed 02/06/09 - use the .equals() function instead of ==
            if (token.equals(BooleanToken.TRUE)) {
                addEnabledOutport(thenOutput);
                
                thenOutput.send(0, BooleanToken.TRUE);
                elseOutput.send(0, BooleanToken.FALSE);
            } else {
                addEnabledOutport(elseOutput);
                
                thenOutput.send(0, BooleanToken.FALSE);
                elseOutput.send(0, BooleanToken.TRUE);
            }
        }
    }
}
