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
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////NetworkReceiver

/** 
*  Note this actor (or some other subclass of this class) should
*  be directly connected to a network input port in a PtidesBasicDirector.
*  
*  Unlike SensorReceiver for example, this actor is necessarily needed for
*  both simulation and code generation purposes.
*  
*  This actor assumes the incoming token is a RecordToken, and includes a 
*  token value as well as a timestamp associated with the token value. Thus 
*  this actor parses the RecordToken and sends the output token with the
*  timestamp equal to the timestamp stored in the RecordToken. 
*  
*  In other words, we assume the RecordToken has these two labels: timestamp,
*  tokenValue. 
*   
*  @author jiazou, matic
*  @version 
*  @since Ptolemy II 7.1
*/
public class NetworkReceiver extends EnvironmentReceiver {
    public NetworkReceiver(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////
    /** Parses the input RecordToken and produces an output token of a timestamp
     *  equal to the timestamp specified within the RecordToken.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent. Or, if the
     *  record has size != 2.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        if (input.hasToken(0)) {
            RecordToken record = (RecordToken) input.get(0);

            if (record.labelSet().size() != 2) {
                throw new IllegalActionException("the record has a size not equal to 2"
                        + "However here we assume the Record is of types: timestamp"
                        + "+ Token");
            }
            
            Time recordTimeStamp = new Time(getDirector(), 
                    ((DoubleToken)record.get("timestamp")).doubleValue());
            Time lastModelTime = director.getModelTime();
            director.setModelTime(recordTimeStamp);
            output.send(0, record.get("tokenValue"));
            director.setModelTime(lastModelTime);
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
