/* Validate MPDU.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib.network.mac;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ValidateMPDU
/**
Describe your class here, in complete sentences.
What does it do?  What is its intended use?

Validate MPDU. ...

@author yourname
@version $Id$
*/

public class ValidateMPDU extends TypedAtomicActor {
    
    /** Construct an actor with the specified name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ValidateMPDU(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // create parameters
        aSifsTime = new Parameter(this, "aSifsTime");
        aSifsTime.setExpression("1.0");
   
        // more parameters
        
        // create ports
        fromPHYLayer = new TypedIOPort(this, "fromPHYLayer", true, false);
        // FIXME: figure out types
        
        // more ports
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    /** Desription of the parameter. */
    public Parameter aSifsTime;

    /** Document the port. */    
    public TypedIOPort fromPHYLayer;
    public TypedIOPort toChannelState;
    public TypedIOPort toFilterMpdu;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** NOTE: react to parameter changes.
     *  Otherwise,
     *  defer to the base class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        return;
    }
    
    /** If a new message is available at the inputs, record it in the
     *  hashtable indexed with the time that the message shall be completed,
     *  and loop through the hashtable to check whether there is collision.
     *  If the current time matches one of the times that we have previously
     *  recorded as the completion time for a transmission, then output the 
     *  received message to the <i>received<i> output port if it is not
     *  lost to a collision; otherwise, output it to the <i>collided<i>
     *  output port.
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // perform the actions/computation done in the handleMessage()
        // method
        
        // working with record tokens to represent messages
        
        Token[] values = {
            new IntToken(0),
            new StringToken("contents"),
            new IntToken(10)
        };
        RecordToken output = new RecordToken(msgFields, values);
        toFilterMpdu.send(0, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Describe your method, again using imperative case.
     *  @see RelevantClass#methodName()
     *  @param parameterName Description of the parameter.
     *  @return Description of the returned value.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    protected int _protectedMethodName(Parameter parameterName)
            throws Exception {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Description of the variable. */
    protected int _aProtectedVariable;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Private methods need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected methods.
    private int _privateMethodName() {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected variables.
    private int _aPrivateVariable;
    
    private static final String[] msgFields = {"durID", "RA", "TA"};
}
