/* An up-down counter.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.LinkedList;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ChannelInput
/**
This Actor transmit the input of the composite wireless channel to
the model inside the channel. 

@author Yang Zhao
@version $$
@since Ptolemy II 3.0
*/

public class ChannelInput extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ChannelInput(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        reception = new TypedIOPort(this, "reception", false, true);
        reception.setTypeEquals(BaseType.OBJECT);
        //sender = new TypedIOPort(this, "sender", false, true);
        //properties = new TypedIOPort(this, "properties", false, true);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The out port.
     */
    public TypedIOPort reception;
    
    //public TypedIOPort sender;
    
    //public TypedIOPort properties;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output an ObjectToken with a Reception value.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (_receptions != null) {
            Reception rec = (Reception)_receptions.removeFirst();
            reception.send(0, new ObjectToken(rec));
            //sender.send(0, new ObjectToken(rec.sender));
            //properties.send(0, rec.properties);
        }
    }

    /** Reset the count of inputs to zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _receptions = null;
    }

    /** This method will be called by the transmit() method of the
     *  CompositeWirelessChannel.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void put
            (Token token, WirelessIOPort port, Token properties)
            throws IllegalActionException {
        if (_receptions == null) {
            _receptions = new LinkedList();
        }        
        Reception reception = new Reception();
        reception.token = token;
        reception.sender = port;
        reception.properties = properties;
        reception.receivers = new LinkedList();
        _receptions.add(reception);
        Director director = getDirector();
        director.fireAtCurrentTime(this);
        //_getInput = true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private LinkedList _receptions;
    //private boolean _getInput = false;
      
}
