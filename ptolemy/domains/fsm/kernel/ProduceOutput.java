/* An action sending a token to all connected receivers of a channel.

 Copyright (c) 2000 The Regents of the University of California.
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
@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// ProduceOutput
/**
A ProduceOutput action takes the token from evaluating the expression
specified by the <i>expression</i> parameter and sends it to all receivers
connected to the channel specified by the <i>channel</i> and <i>portName</i>
parameters. This action is a choice action contained by a transition in an
FSMActor, which will be called the associated FSMActor of this action. The
port with name specified by the <i>portName</i> parameter must be an output
port of the associated FSMActor and the channel specified must be within
range, otherwise an exception will be thrown when this action is executed.
The scope of the specified expression includes all the variables and
parameters contained by the associated FSMActor.

@author Xiaojun Liu
@version $Id$
@see Transition
@see FSMActor
*/
public class ProduceOutput extends BroadcastOutput {

    /** Construct a ProduceOutput action with the given name contained
     *  by the specified transition. The transition argument must not be
     *  null, or a NullPointerException will be thrown. This action will
     *  use the workspace of the transition for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. A variable for expression evaluation is
     *  created in the transition. The name of the variable is obtained
     *  by prepending an underscore to the name of this action.
     *  Increment the version of the workspace.
     *  @param transition The transition.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the transition already
     *   has an attribute with the name or that obtained by prepending
     *   an underscore to the name.
     */
    public ProduceOutput(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
        channel = new Parameter(this, "channel");
        channel.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Parameter specifying a channel. If the channel index given by
     *  this parameter is out of range, an exception will be thrown
     *  when this action is executed.
     */
    public Parameter channel = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>portName</i> parameter, record the change but do not
     *  check whether the associated FSMActor has an output port with
     *  the specified name. If the changed attribute is the <i>channel</i>
     *  parameter, record the change but do not check whether the channel
     *  index specified is out of range. If the changed attribute is the
     *  <i>expression</i> parameter, set the specified expression to the
     *  variable for expression evaluation.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == channel) {
            IntToken tok = (IntToken)channel.getToken();
            _channel = tok.intValue();
        }
    }

    /** Clone the action into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new action.
     *  @param ws The workspace for the new action.
     *  @return A new action.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        ProduceOutput newobj = (ProduceOutput)super.clone(ws);
        newobj.channel = (Parameter)newobj.getAttribute("channel");
        return newobj;
    }

    /** Take the token from evaluating the expression specified by the
     *  <i>expression</i> parameter and send it to all connected
     *  receivers of the channel specified by the <i>channel</i> and
     *  <i>portName</i> parameters.
     *  @exception IllegalActionException If expression evaluation fails,
     *   or the specified port is not found, or the specified channel is
     *   out of range, or sending to the channel throws a NoRoomException.
     */
    public void execute() throws IllegalActionException {
        IOPort port = _getPort();
        try {
            port.send(_channel, _evaluationVariable().getToken());
        } catch (NoRoomException ex) {
            throw new IllegalActionException(this, "Cannot complete "
                    + "action: " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The channel index specified.
    private int _channel;

}
