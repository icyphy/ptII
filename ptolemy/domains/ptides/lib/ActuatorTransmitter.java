/*
@Copyright (c) 2008-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////ActuatorTransmitter

/** 
*  Note this actor (or some other subclass of EnvironmentTransmitter) should
*  be directly connected to a actuator output port for a PtidesBasicDirector.
*  Though this actor does not do anything interesting, it is mainly used for
*  code generation purposes.
*  
*  In other words, this actor is not necessarily needed if the user is only 
*  running simulation, but not doing codegen to PtidyOS.
*  
*  However the same argument does not hold for a NetworkTransmitter.
*   
*  @author jiazou, matic
*  @version 
*  @since Ptolemy II 7.1
*/
public class ActuatorTransmitter extends EnvironmentTransmitter {
    
    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ActuatorTransmitter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////
    /** Read one token from the input. Send out an identical token.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        Token token;
        // consume input
        if (input.hasToken(0)) {
            token = input.get(0);
        } else {
            token = null;
        }

        // produce output
        if (token != null) {
            output.send(0, token);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this port is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;
}
