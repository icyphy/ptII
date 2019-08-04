/* A polymorphic switch, which routes inputs to specified output channels.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// Switch

/**
 <p>A polymorphic switch, which routes inputs to specified output channels.
 This actor has two input ports, the <i>input</i> port for data,
 and the <i>control</i> port to select which output channel to use.
 When it fires, if an input token is available at the <i>control</i>
 input, that token is read, and its value is noted.  If an input
 token is available on the <i>input</i> port, then that token is
 read, sent to the output channel specified by the most recently
 received value on the <i>control</i> port.  If no token has been
 received on the <i>control</i> port, then the token is sent to
 channel zero.  If the value of the most recently received token
 on the <i>control</i> port is out of range (less than zero,
 or greater than or equal to the width of the output), then no
 output is produced, and the token is lost.
</p><p>
 Note that it may be tempting to call an instance of this
 class "switch", but recall that "switch" is a Java keyword, and
 thus it cannot be the name of an object.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class Switch extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Switch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setMultiport(true);

        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);

        // Put the control input on the bottom of the actor.
        StringAttribute controlCardinal = new StringAttribute(control,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port for control tokens, which specify the output channel
     *  to produce data on.  The type is int. */
    public TypedIOPort control;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a control token, if there is one, and transfer an input
     *  token, if there is one, to the output channel specified by
     *  the most recent control token, if it is in range.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (control.hasToken(0)) {
            _control = ((IntToken) control.get(0)).intValue();
        }

        if (input.hasToken(0)) {
            Token token = input.get(0);

            if (_control >= 0 && _control < output.getWidth()) {
                output.send(_control, token);
            }
        }
    }

    /** Initialize this actor so that channel zero of <i>input</i> is read
     *  from until a token arrives on the <i>control</i> input.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _control = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The most recently read control token.
    private int _control = 0;
}
