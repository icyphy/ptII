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

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////NetworkTransmitter

/** 
*  Note this actor (or some other subclass of this class) should
*  be directly connected to a network output port in a PtidesBasicDirector.
*  
*  Unlike ActuatorTransmitter for example, this actor is necessarily needed for
*  both simulation and code generation purposes.
*   
*  This actor takes the input token, and creates a RecordToken, with two lables:
*  timestamp, and tokenValue. It then sends the output token to its output port. 
*   
*  @author jiazou, matic
*  @version 
*  @since Ptolemy II 7.1
*/
public class NetworkTransmitter extends EnvironmentTransmitter {
    public NetworkTransmitter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////
    /** Creates a RecordToken with two lables: timestamp, and tokenValue. 
     *  The tokenValue is the token consumed from the input.
     *  It then sends the output token to its output port. 
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        if (input.hasToken(0)) {
            
            String[] labels = new String[]{"timestamp", "tokenValue"};
            Token[] values = new Token[]{
                    new DoubleToken(director.getModelTime().getDoubleValue()),
                    input.get(0)};
            RecordToken record = new RecordToken(labels, values);
            
            output.send(0, record);
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
