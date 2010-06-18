/* A polymorphic While actor.

 */
package ptolemy.domains.sequence.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// While

/**
 <p>A type polymorphic While actor. 
 In an iteration, if an input token is available at the <i>If</i> input,
 that token is read.
 The <i>Then<i> output is set to true, if then <i>If</i> input is true.
 The <i>If</i> port may only receive Tokens of type Boolean. The output 
 ports are also of type boolean.</p>
 
 While is a ControlActor, meaning that it keeps a list of
 enabled output ports. 
 The sequence and process director will keep scheduling the While actor
 until its input condition becomes false. Care must be taken when
 using this actor, since it can potentially cause an infinite loop in
 a model's execution.
 */
public class While extends IfThen {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public While(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
}
