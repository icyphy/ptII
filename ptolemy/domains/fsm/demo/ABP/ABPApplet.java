/* An applet that uses Ptolemy II FSM and DE domains.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.demo.ABP;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.gui.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.plot.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import java.util.Enumeration;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.BoxLayout;


//////////////////////////////////////////////////////////////////////////
//// ABPApplet
/**
An applet that uses Ptolemy II FSM and DE domains.

@author Xiaojun Liu, Steve Neuendorffer
@version $Id$
*/
public class ABPApplet extends DEApplet implements QueryListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the parameters of the model from the values in the query
     *  boxes.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        try {
            _forward.dropRate.setToken
                (new DoubleToken(_query.doubleValue("forwardRate")));
            _backward.dropRate.setToken
                (new DoubleToken(_query.doubleValue("backwardRate")));

            _go();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Initialize the applet.
     */
    public void init() {
        super.init();

        // Creating the topology.
        try {
            getContentPane().setLayout(
                    new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

            _query = new Query();
            _query.setBackground(getBackground());
            _query.addLine("forwardRate", "Forward Drop Rate", "0.3");
            _query.addLine("backwardRate", "Backward Drop Rate", "0.2");
            _query.addQueryListener(this);
            getContentPane().add(_query);

            // message source
            _msgSrc = new DEMessageSource(_toplevel,
                    "MessageSource");

            // timer
            _timer = new DETimer(_toplevel, "Timer");

            // forward packet channel
            _forward = new DEChannel(_toplevel, "ForwardChannel");
            _forward.dropRate.setToken(new DoubleToken(0.3));
            _forward.maxDelay.setToken(new DoubleToken(5.0));
            _forward.minDelay.setToken(new DoubleToken(0.5));

            // backward packet channel
            _backward = new DEChannel(_toplevel, "BackwardChannel");
            _backward.dropRate.setToken(new DoubleToken(0.2));
            _backward.maxDelay.setToken(new DoubleToken(2.0));
            _backward.minDelay.setToken(new DoubleToken(0.2));

            // the plot
            _plot = new TimedPlotter(_toplevel, "Plot");
 
            // sender - a hierarchical FSM
            TypedCompositeActor sender =
                    new TypedCompositeActor(_toplevel, "Sender");
            // create ports
            TypedIOPort sdrRequest = (TypedIOPort)sender.newPort("request");
            sdrRequest.setInput(true);
            sdrRequest.setTypeEquals(BaseType.GENERAL);
            TypedIOPort sdrMsgIn = (TypedIOPort)sender.newPort("msgIn");
            sdrMsgIn.setInput(true);
            sdrMsgIn.setTypeEquals(BaseType.INT);
            TypedIOPort sdrNext = (TypedIOPort)sender.newPort("next");
            sdrNext.setOutput(true);
            sdrNext.setTypeEquals(BaseType.GENERAL);
            TypedIOPort sdrError = (TypedIOPort)sender.newPort("error");
            sdrError.setOutput(true);
            sdrError.setTypeEquals(BaseType.GENERAL);
            TypedIOPort sdrAck = (TypedIOPort)sender.newPort("ack");
            sdrAck.setInput(true);
            sdrAck.setTypeEquals(BaseType.INT);
            TypedIOPort sdrPktOut = (TypedIOPort)sender.newPort("pktOut");
            sdrPktOut.setOutput(true);
            sdrPktOut.setTypeEquals(BaseType.INT);
            TypedIOPort sdrSetTimer = (TypedIOPort)sender.newPort("setTimer");
            sdrSetTimer.setOutput(true);
            sdrSetTimer.setTypeEquals(BaseType.DOUBLE);
            TypedIOPort sdrExpired = (TypedIOPort)sender.newPort("expired");
            sdrExpired.setInput(true);
            sdrExpired.setTypeEquals(BaseType.GENERAL);
            TypedIOPort sdrMonitor = (TypedIOPort)sender.newPort("monitor");
            sdrMonitor.setOutput(true);
            sdrMonitor.setTypeEquals(BaseType.INT);

            // sender's mode controller
            FSMActor ctrl = new FSMActor(sender, "Controller");

            TypedIOPort ctrlNext = (TypedIOPort)ctrl.newPort("next");
            ctrlNext.setInput(true);
            ctrlNext.setTypeEquals(BaseType.GENERAL);
            TypedIOPort ctrlError = (TypedIOPort)ctrl.newPort("error");
            ctrlError.setInput(true);
            ctrlError.setTypeEquals(BaseType.GENERAL);

            State ctrlConnecting = new State(ctrl, "Connecting");
            State ctrlDead = new State(ctrl, "Dead");
            State ctrlSending = new State(ctrl, "Sending");
            ctrl.initialStateName.setToken(new StringToken("Connecting"));
            Transition ctrlTr1 = new Transition(ctrl, "ctrlTr1");
            ctrlConnecting.outgoingPort.link(ctrlTr1);
            ctrlSending.incomingPort.link(ctrlTr1);
            ctrlTr1.guard.setExpression("next_S");
            Transition ctrlTr2 = new Transition(ctrl, "ctrlTr2");
            ctrlConnecting.outgoingPort.link(ctrlTr2);
            ctrlDead.incomingPort.link(ctrlTr2);
            ctrlTr2.guard.setExpression("error_S");

            // sender's director
            FSMDirector sdrDir = new FSMDirector(sender, "SenderDirector");
            sdrDir.controllerName.setToken(new StringToken("Controller"));

            // submachine refining sender's connecting state
            FSMActor connect = new FSMActor(sender, "Connect");
            // ports
            TypedIOPort conRequest = (TypedIOPort)connect.newPort("request");
            conRequest.setInput(true);
            conRequest.setTypeEquals(BaseType.GENERAL);
            TypedIOPort conNext = (TypedIOPort)connect.newPort("next");
            conNext.setOutput(true);
            conNext.setTypeEquals(BaseType.GENERAL);
            TypedIOPort conError = (TypedIOPort)connect.newPort("error");
            conError.setOutput(true);
            conError.setTypeEquals(BaseType.GENERAL);
            TypedIOPort conAck = (TypedIOPort)connect.newPort("ack");
            conAck.setInput(true);
            conAck.setTypeEquals(BaseType.INT);
            TypedIOPort conPktOut = (TypedIOPort)connect.newPort("pktOut");
            conPktOut.setOutput(true);
            conPktOut.setTypeEquals(BaseType.INT);
            TypedIOPort conSetTimer = (TypedIOPort)connect.newPort("setTimer");
            conSetTimer.setOutput(true);
            conSetTimer.setTypeEquals(BaseType.DOUBLE);
            TypedIOPort conExpired = (TypedIOPort)connect.newPort("expired");
            conExpired.setInput(true);
            conExpired.setTypeEquals(BaseType.GENERAL);
            // connect's states and transitions
            State conInit = new State(connect, "Init");
            State conWait = new State(connect, "Wait");
            State conSucc = new State(connect, "Success");
            State conFail = new State(connect, "Fail");
            connect.initialStateName.setToken(new StringToken("Init"));
            Transition conTr1 = new Transition(connect, "conTr1");
            conInit.outgoingPort.link(conTr1);
            conWait.incomingPort.link(conTr1);
            conTr1.guard.setExpression("request_S");
            BroadcastOutput conTr1Act1 =
                    new BroadcastOutput(conTr1, "conTr1Act1");
            conTr1Act1.portName.setToken(new StringToken("pktOut"));
            conTr1Act1.expression.setToken(new StringToken("-1"));
            BroadcastOutput conTr1Act2 =
                    new BroadcastOutput(conTr1, "conTr1Act2");
            conTr1Act2.portName.setToken(new StringToken("setTimer"));
            conTr1Act2.expression.setToken(new StringToken(TIME_OUT));
            SetVariable conTr1Act3 =
                    new SetVariable(conTr1, "conTr1Act3");
            conTr1Act3.variableName.setToken(new StringToken("count"));
            conTr1Act3.expression.setToken(new StringToken("5"));
            Transition conTr2 = new Transition(connect, "conTr2");
            conWait.outgoingPort.link(conTr2);
            conSucc.incomingPort.link(conTr2);
            conTr2.guard.setExpression("(ack_S ? ack_V : 0) == -1");
            BroadcastOutput conTr2Act1 =
                    new BroadcastOutput(conTr2, "conTr2Act1");
            conTr2Act1.portName.setToken(new StringToken("next"));
            conTr2Act1.expression.setToken(new StringToken("true"));
            BroadcastOutput conTr2Act2 =
                    new BroadcastOutput(conTr2, "conTr2Act2");
            conTr2Act2.portName.setToken(new StringToken("setTimer"));
            conTr2Act2.expression.setToken(new StringToken(RESET));
            Transition conTr3 = new Transition(connect, "conTr3");
            conWait.outgoingPort.link(conTr3);
            conFail.incomingPort.link(conTr3);
            conTr3.guard.setExpression("!ack_S && expired_S && count == 0");
            BroadcastOutput conTr3Act1 =
                    new BroadcastOutput(conTr3, "conTr3Act1");
            conTr3Act1.portName.setToken(new StringToken("error"));
            conTr3Act1.expression.setToken(new StringToken("true"));
            Transition conTr4 = new Transition(connect, "conTr4");
            conWait.outgoingPort.link(conTr4);
            conFail.incomingPort.link(conTr4);
            conTr4.guard.setExpression("(ack_S ? ack_V : -1) != -1 && "
                    + "expired_S && count == 0");
            BroadcastOutput conTr4Act1 =
                    new BroadcastOutput(conTr4, "conTr4Act1");
            conTr4Act1.portName.setToken(new StringToken("error"));
            conTr4Act1.expression.setToken(new StringToken("true"));
            Transition conTr5 = new Transition(connect, "conTr5");
            conWait.outgoingPort.link(conTr5);
            conWait.incomingPort.link(conTr5);
            conTr5.guard.setExpression("!ack_S && expired_S && count != 0");
            BroadcastOutput conTr5Act1 =
                    new BroadcastOutput(conTr5, "conTr5Act1");
            conTr5Act1.portName.setToken(new StringToken("pktOut"));
            conTr5Act1.expression.setToken(new StringToken("-1"));
            BroadcastOutput conTr5Act2 =
                    new BroadcastOutput(conTr5, "conTr5Act2");
            conTr5Act2.portName.setToken(new StringToken("setTimer"));
            conTr5Act2.expression.setToken(new StringToken(TIME_OUT));
            SetVariable conTr5Act3 =
                    new SetVariable(conTr5, "conTr5Act3");
            conTr5Act3.variableName.setToken(new StringToken("count"));
            conTr5Act3.expression.setToken(new StringToken("count - 1"));
            Transition conTr6 = new Transition(connect, "conTr6");
            conWait.outgoingPort.link(conTr6);
            conWait.incomingPort.link(conTr6);
            conTr6.guard.setExpression("(ack_S ? ack_V : -1) != -1 "
                    + "&& expired_S && count != 0");
            BroadcastOutput conTr6Act1 =
                    new BroadcastOutput(conTr6, "conTr6Act1");
            conTr6Act1.portName.setToken(new StringToken("pktOut"));
            conTr6Act1.expression.setToken(new StringToken("-1"));
            BroadcastOutput conTr6Act2 =
                    new BroadcastOutput(conTr6, "conTr6Act2");
            conTr6Act2.portName.setToken(new StringToken("setTimer"));
            conTr6Act2.expression.setToken(new StringToken(TIME_OUT));
            SetVariable conTr6Act3 =
                    new SetVariable(conTr6, "conTr6Act3");
            conTr6Act3.variableName.setToken(new StringToken("count"));
            conTr6Act3.expression.setToken(new StringToken("count - 1"));
            // create the local variable
            Variable conCount = new Variable(connect, "count");
            conCount.setTypeEquals(BaseType.INT);
            conCount.setToken(new IntToken(0));
            // set connect to be ctrlConnecting's refinement
            ctrlConnecting.refinementName.setToken(new StringToken("Connect"));

            // the submachine refining sender's sending state
            FSMActor send = new FSMActor(sender, "Send");
            // create ports
            TypedIOPort sendMsgIn = (TypedIOPort)send.newPort("msgIn");
            sendMsgIn.setInput(true);
            sendMsgIn.setTypeEquals(BaseType.INT);
            TypedIOPort sendNext = (TypedIOPort)send.newPort("next");
            sendNext.setOutput(true);
            sendNext.setTypeEquals(BaseType.GENERAL);
            TypedIOPort sendAck = (TypedIOPort)send.newPort("ack");
            sendAck.setInput(true);
            sendAck.setTypeEquals(BaseType.INT);
            TypedIOPort sendPktOut = (TypedIOPort)send.newPort("pktOut");
            sendPktOut.setOutput(true);
            sendPktOut.setTypeEquals(BaseType.INT);
            TypedIOPort sendSetTimer = (TypedIOPort)send.newPort("setTimer");
            sendSetTimer.setOutput(true);
            sendSetTimer.setTypeEquals(BaseType.DOUBLE);
            TypedIOPort sendExpired = (TypedIOPort)send.newPort("expired");
            sendExpired.setInput(true);
            sendExpired.setTypeEquals(BaseType.GENERAL);
            TypedIOPort sendMonitor = (TypedIOPort)send.newPort("monitor");
            sendMonitor.setOutput(true);
            sendMonitor.setTypeEquals(BaseType.INT);
            // the states and transitions
            State s0 = new State(send, "0");
            State s1 = new State(send, "1");
            send.initialStateName.setToken(new StringToken("0"));
            Transition sendTr1 = new Transition(send, "sendTr1");
            s0.outgoingPort.link(sendTr1);
            s0.incomingPort.link(sendTr1);
            sendTr1.guard.setExpression("msgIn_S");
            BroadcastOutput sendTr1Act1 =
                    new BroadcastOutput(sendTr1, "sendTr1Act1");
            sendTr1Act1.portName.setToken(new StringToken("pktOut"));
            sendTr1Act1.expression.setToken(new StringToken("msgIn_V*2"));
            BroadcastOutput sendTr1Act2 =
                    new BroadcastOutput(sendTr1, "sendTr1Act2");
            sendTr1Act2.portName.setToken(new StringToken("monitor"));
            sendTr1Act2.expression.setToken(new StringToken("0"));
            BroadcastOutput sendTr1Act3 =
                    new BroadcastOutput(sendTr1, "sendTr1Act3");
            sendTr1Act3.portName.setToken(new StringToken("setTimer"));
            sendTr1Act3.expression.setToken(new StringToken(TIME_OUT));
            SetVariable sendTr1Act4 =
                    new SetVariable(sendTr1, "sendTr1Act4");
            sendTr1Act4.variableName.setToken(new StringToken("trying"));
            sendTr1Act4.expression.setToken(new StringToken("true"));
            SetVariable sendTr1Act5 =
                    new SetVariable(sendTr1, "sendTr1Act5");
            sendTr1Act5.variableName.setToken(new StringToken("msg"));
            sendTr1Act5.expression.setToken(new StringToken("msgIn_V"));
            Transition sendTr2 = new Transition(send, "sendTr2");
            s0.outgoingPort.link(sendTr2);
            s0.incomingPort.link(sendTr2);
            sendTr2.guard.setExpression("!ack_S && expired_S && trying");
            BroadcastOutput sendTr2Act1 =
                    new BroadcastOutput(sendTr2, "sendTr2Act1");
            sendTr2Act1.portName.setToken(new StringToken("pktOut"));
            sendTr2Act1.expression.setToken(new StringToken("msgIn_V*2"));
            BroadcastOutput sendTr2Act2 =
                    new BroadcastOutput(sendTr2, "sendTr2Act2");
            sendTr2Act2.portName.setToken(new StringToken("monitor"));
            sendTr2Act2.expression.setToken(new StringToken("0"));
            BroadcastOutput sendTr2Act3 =
                    new BroadcastOutput(sendTr2, "sendTr2Act3");
            sendTr2Act3.portName.setToken(new StringToken("setTimer"));
            sendTr2Act3.expression.setToken(new StringToken(TIME_OUT));
            Transition sendTr3 = new Transition(send, "sendTr3");
            s0.outgoingPort.link(sendTr3);
            s0.incomingPort.link(sendTr3);
            sendTr3.guard.setExpression("(ack_S ? ack_V : 0) != 0 "
                    + "&& expired_S && trying");
            BroadcastOutput sendTr3Act1 =
                    new BroadcastOutput(sendTr3, "sendTr3Act1");
            sendTr3Act1.portName.setToken(new StringToken("pktOut"));
            sendTr3Act1.expression.setToken(new StringToken("msgIn_V*2"));
            BroadcastOutput sendTr3Act2 =
                    new BroadcastOutput(sendTr3, "sendTr3Act2");
            sendTr3Act2.portName.setToken(new StringToken("monitor"));
            sendTr3Act2.expression.setToken(new StringToken("0"));
            BroadcastOutput sendTr3Act3 =
                    new BroadcastOutput(sendTr3, "sendTr3Act3");
            sendTr3Act3.portName.setToken(new StringToken("setTimer"));
            sendTr3Act3.expression.setToken(new StringToken(TIME_OUT));
            Transition sendTr4 = new Transition(send, "sendTr4");
            s0.outgoingPort.link(sendTr4);
            s1.incomingPort.link(sendTr4);
            sendTr4.guard.setExpression("(ack_S ? ack_V : -1) == 0 "
                    + "&& trying");
            BroadcastOutput sendTr4Act1 =
                    new BroadcastOutput(sendTr4, "sendTr4Act1");
            sendTr4Act1.portName.setToken(new StringToken("setTimer"));
            sendTr4Act1.expression.setToken(new StringToken(RESET));
            BroadcastOutput sendTr4Act2 =
                    new BroadcastOutput(sendTr4, "sendTr4Act2");
            sendTr4Act2.portName.setToken(new StringToken("next"));
            sendTr4Act2.expression.setToken(new StringToken("true"));
            SetVariable sendTr4Act3 =
                    new SetVariable(sendTr4, "sendTr4Act3");
            sendTr4Act3.variableName.setToken(new StringToken("trying"));
            sendTr4Act3.expression.setToken(new StringToken("false"));
            Transition sendTr5 = new Transition(send, "sendTr5");
            s1.outgoingPort.link(sendTr5);
            s1.incomingPort.link(sendTr5);
            sendTr5.guard.setExpression("msgIn_S");
            BroadcastOutput sendTr5Act1 =
                    new BroadcastOutput(sendTr5, "sendTr5Act1");
            sendTr5Act1.portName.setToken(new StringToken("pktOut"));
            sendTr5Act1.expression.setToken(new StringToken("msgIn_V*2+1"));
            BroadcastOutput sendTr5Act2 =
                    new BroadcastOutput(sendTr5, "sendTr5Act2");
            sendTr5Act2.portName.setToken(new StringToken("monitor"));
            sendTr5Act2.expression.setToken(new StringToken("1"));
            BroadcastOutput sendTr5Act3 =
                    new BroadcastOutput(sendTr5, "sendTr5Act3");
            sendTr5Act3.portName.setToken(new StringToken("setTimer"));
            sendTr5Act3.expression.setToken(new StringToken(TIME_OUT));
            SetVariable sendTr5Act4 =
                    new SetVariable(sendTr5, "sendTr5Act4");
            sendTr5Act4.variableName.setToken(new StringToken("trying"));
            sendTr5Act4.expression.setToken(new StringToken("true"));
            SetVariable sendTr5Act5 =
                    new SetVariable(sendTr5, "sendTr5Act5");
            sendTr5Act5.variableName.setToken(new StringToken("msg"));
            sendTr5Act5.expression.setToken(new StringToken("msgIn_V"));
            Transition sendTr6 = new Transition(send, "sendTr6");
            s1.outgoingPort.link(sendTr6);
            s1.incomingPort.link(sendTr6);
            sendTr6.guard.setExpression("!ack_S && expired_S && trying");
            BroadcastOutput sendTr6Act1 =
                    new BroadcastOutput(sendTr6, "sendTr6Act1");
            sendTr6Act1.portName.setToken(new StringToken("pktOut"));
            sendTr6Act1.expression.setToken(new StringToken("msgIn_V*2+1"));
            BroadcastOutput sendTr6Act2 =
                    new BroadcastOutput(sendTr6, "sendTr6Act2");
            sendTr6Act2.portName.setToken(new StringToken("monitor"));
            sendTr6Act2.expression.setToken(new StringToken("1"));
            BroadcastOutput sendTr6Act3 =
                    new BroadcastOutput(sendTr6, "sendTr6Act3");
            sendTr6Act3.portName.setToken(new StringToken("setTimer"));
            sendTr6Act3.expression.setToken(new StringToken(TIME_OUT));
            Transition sendTr7 = new Transition(send, "sendTr7");
            s1.outgoingPort.link(sendTr7);
            s1.incomingPort.link(sendTr7);
            sendTr7.guard.setExpression("(ack_S ? ack_V : 1) != 1 "
                    + "&& expired_S && trying");
            BroadcastOutput sendTr7Act1 =
                    new BroadcastOutput(sendTr7, "sendTr7Act1");
            sendTr7Act1.portName.setToken(new StringToken("pktOut"));
            sendTr7Act1.expression.setToken(new StringToken("msgIn_V*2+1"));
            BroadcastOutput sendTr7Act2 =
                    new BroadcastOutput(sendTr7, "sendTr7Act2");
            sendTr7Act2.portName.setToken(new StringToken("monitor"));
            sendTr7Act2.expression.setToken(new StringToken("1"));
            BroadcastOutput sendTr7Act3 =
                    new BroadcastOutput(sendTr7, "sendTr7Act3");
            sendTr7Act3.portName.setToken(new StringToken("setTimer"));
            sendTr7Act3.expression.setToken(new StringToken(TIME_OUT));
            Transition sendTr8 = new Transition(send, "sendTr8");
            s1.outgoingPort.link(sendTr8);
            s0.incomingPort.link(sendTr8);
            sendTr8.guard.setExpression("(ack_S ? ack_V : -1) == 1 "
                    + "&& trying");
            BroadcastOutput sendTr8Act1 =
                    new BroadcastOutput(sendTr8, "sendTr8Act1");
            sendTr8Act1.portName.setToken(new StringToken("setTimer"));
            sendTr8Act1.expression.setToken(new StringToken(RESET));
            BroadcastOutput sendTr8Act2 =
                    new BroadcastOutput(sendTr8, "sendTr8Act2");
            sendTr8Act2.portName.setToken(new StringToken("next"));
            sendTr8Act2.expression.setToken(new StringToken("true"));
            SetVariable sendTr8Act3 =
                    new SetVariable(sendTr8, "sendTr8Act3");
            sendTr8Act3.variableName.setToken(new StringToken("trying"));
            sendTr8Act3.expression.setToken(new StringToken("false"));
            // create the local variables
            Variable sendFlag = new Variable(send, "trying");
            sendFlag.setTypeEquals(BaseType.BOOLEAN);
            sendFlag.setToken(BooleanToken.FALSE);
            Variable msgCount = new Variable(send, "msg");
            msgCount.setTypeEquals(BaseType.INT);
            msgCount.setToken(new IntToken(0));
            // set to be ctrlSending's refinement
            ctrlSending.refinementName.setToken(new StringToken("Send"));

            // connect sender's components
            TypedIORelation sdrR1 =
                (TypedIORelation)sender.newRelation("request");
            sdrRequest.link(sdrR1);
            conRequest.link(sdrR1);
            TypedIORelation sdrR2 =
                (TypedIORelation)sender.newRelation("setTimer");
            sdrSetTimer.link(sdrR2);
            conSetTimer.link(sdrR2);
            sendSetTimer.link(sdrR2);
            TypedIORelation sdrR3 =
                (TypedIORelation)sender.newRelation("ack");
            sdrAck.link(sdrR3);
            conAck.link(sdrR3);
            sendAck.link(sdrR3);
            TypedIORelation sdrR4 =
                (TypedIORelation)sender.newRelation("pktOut");
            sdrPktOut.link(sdrR4);
            conPktOut.link(sdrR4);
            sendPktOut.link(sdrR4);
            TypedIORelation sdrR5 =
                (TypedIORelation)sender.newRelation("expired");
            sdrExpired.link(sdrR5);
            conExpired.link(sdrR5);
            sendExpired.link(sdrR5);
            TypedIORelation sdrR6 =
                (TypedIORelation)sender.newRelation("next");
            sdrNext.link(sdrR6);
            conNext.link(sdrR6);
            sendNext.link(sdrR6);
            ctrlNext.link(sdrR6);
            TypedIORelation sdrR7 =
                (TypedIORelation)sender.newRelation("msgIn");
            sdrMsgIn.link(sdrR7);
            sendMsgIn.link(sdrR7);
            TypedIORelation sdrR8 =
                (TypedIORelation)sender.newRelation("monitor");
            sdrMonitor.link(sdrR8);
            sendMonitor.link(sdrR8);
            TypedIORelation sdrR9 =
                (TypedIORelation)sender.newRelation("error");
            conError.link(sdrR9);
            ctrlError.link(sdrR9);

            // the receiver FSM
            FSMActor receiver = new FSMActor(_toplevel, "Receiver");
            // ports
            TypedIOPort recPktIn = (TypedIOPort)receiver.newPort("pktIn");
            recPktIn.setInput(true);
            recPktIn.setTypeEquals(BaseType.INT);
            TypedIOPort recAck = (TypedIOPort)receiver.newPort("ack");
            recAck.setOutput(true);
            recAck.setTypeEquals(BaseType.INT);
            TypedIOPort recMsgOut = (TypedIOPort)receiver.newPort("msgOut");
            recMsgOut.setOutput(true);
            recMsgOut.setTypeEquals(BaseType.INT);
            // states and transitions
            State recInit = new State(receiver, "Init");
            State recS0 = new State(receiver, "S0");
            State recS1 = new State(receiver, "S1");
            receiver.initialStateName.setToken(new StringToken("Init"));
            Transition recTr1 = new Transition(receiver, "recTr1");
            recInit.outgoingPort.link(recTr1);
            recInit.incomingPort.link(recTr1);
            recTr1.guard.setExpression("(pktIn_S ? pktIn_V : 0) == -1");
            BroadcastOutput recTr1Act1 =
                    new BroadcastOutput(recTr1, "recTr1Act1");
            recTr1Act1.portName.setToken(new StringToken("ack"));
            recTr1Act1.expression.setToken(new StringToken("-1"));
            Transition recTr2 = new Transition(receiver, "recTr2");
            recInit.outgoingPort.link(recTr2);
            recS1.incomingPort.link(recTr2);
            recTr2.guard.setExpression("(pktIn_S ? pktIn_V%2 : 1) == 0");
            BroadcastOutput recTr2Act1 =
                    new BroadcastOutput(recTr2, "recTr2Act1");
            recTr2Act1.portName.setToken(new StringToken("ack"));
            recTr2Act1.expression.setToken(new StringToken("0"));
            BroadcastOutput recTr2Act2 =
                    new BroadcastOutput(recTr2, "recTr2Act2");
            recTr2Act2.portName.setToken(new StringToken("msgOut"));
            recTr2Act2.expression.setToken(new StringToken("pktIn_V/2"));
            Transition recTr3 = new Transition(receiver, "recTr3");
            recS1.outgoingPort.link(recTr3);
            recS1.incomingPort.link(recTr3);
            recTr3.guard.setExpression("(pktIn_S ? pktIn_V%2 : 1) == 0");
            BroadcastOutput recTr3Act1 =
                    new BroadcastOutput(recTr3, "recTr3Act1");
            recTr3Act1.portName.setToken(new StringToken("ack"));
            recTr3Act1.expression.setToken(new StringToken("0"));
            Transition recTr4 = new Transition(receiver, "recTr4");
            recS1.outgoingPort.link(recTr4);
            recS0.incomingPort.link(recTr4);
            recTr4.guard.setExpression("(pktIn_S ? pktIn_V%2 : 0) == 1");
            BroadcastOutput recTr4Act1 =
                    new BroadcastOutput(recTr4, "recTr4Act1");
            recTr4Act1.portName.setToken(new StringToken("ack"));
            recTr4Act1.expression.setToken(new StringToken("1"));
            BroadcastOutput recTr4Act2 =
                    new BroadcastOutput(recTr4, "recTr4Act2");
            recTr4Act2.portName.setToken(new StringToken("msgOut"));
            recTr4Act2.expression.setToken(new StringToken("pktIn_V/2"));
            Transition recTr5 = new Transition(receiver, "recTr5");
            recS0.outgoingPort.link(recTr5);
            recS0.incomingPort.link(recTr5);
            recTr5.guard.setExpression("(pktIn_S ? pktIn_V%2 : 0) == 1");
            BroadcastOutput recTr5Act1 =
                    new BroadcastOutput(recTr5, "recTr5Act1");
            recTr5Act1.portName.setToken(new StringToken("ack"));
            recTr5Act1.expression.setToken(new StringToken("1"));
            Transition recTr6 = new Transition(receiver, "recTr6");
            recS0.outgoingPort.link(recTr6);
            recS1.incomingPort.link(recTr6);
            recTr6.guard.setExpression("(pktIn_S ? pktIn_V%2 : 1) == 0");
            BroadcastOutput recTr6Act1 =
                    new BroadcastOutput(recTr6, "recTr6Act1");
            recTr6Act1.portName.setToken(new StringToken("ack"));
            recTr6Act1.expression.setToken(new StringToken("0"));
            BroadcastOutput recTr6Act2 =
                    new BroadcastOutput(recTr6, "recTr6Act2");
            recTr6Act2.portName.setToken(new StringToken("msgOut"));
            recTr6Act2.expression.setToken(new StringToken("pktIn_V/2"));

            // connect the top level system
            TypedIORelation sysR1 =
                (TypedIORelation)_toplevel.newRelation("request");
            _msgSrc.request.link(sysR1);
            sdrRequest.link(sysR1);
            TypedIORelation sysR2 =
                (TypedIORelation)_toplevel.newRelation("msgIn");
            _msgSrc.output.link(sysR2);
            sdrMsgIn.link(sysR2);
            TypedIORelation sysR3 =
                (TypedIORelation)_toplevel.newRelation("pktOut");
            _forward.input.link(sysR3);
            sdrPktOut.link(sysR3);
            TypedIORelation sysR4 =
                (TypedIORelation)_toplevel.newRelation("sdrAck");
            _backward.output.link(sysR4);
            sdrAck.link(sysR4);
            TypedIORelation sysR5 =
                (TypedIORelation)_toplevel.newRelation("recAck");
            _backward.input.link(sysR5);
            recAck.link(sysR5);
            TypedIORelation sysR6 =
                (TypedIORelation)_toplevel.newRelation("msgOut");
            recMsgOut.link(sysR6);
            _plot.input.link(sysR6);
            TypedIORelation sysR7 =
                (TypedIORelation)_toplevel.newRelation("setTimer");
            _timer.set.link(sysR7);
            sdrSetTimer.link(sysR7);
            TypedIORelation sysR8 =
                (TypedIORelation)_toplevel.newRelation("expired");
            _timer.expired.link(sysR8);
            sdrExpired.link(sysR8);
            TypedIORelation sysR9 =
                (TypedIORelation)_toplevel.newRelation("pktIn");
            _forward.output.link(sysR9);
            recPktIn.link(sysR9);
            TypedIORelation sysR10 =
                (TypedIORelation)_toplevel.newRelation("next");
            _msgSrc.next.link(sysR10);
            sdrNext.link(sysR10);

            _plot.input.link(sysR2);
            TypedIORelation sysR11 =
                (TypedIORelation)_toplevel.newRelation("monitor");
            sdrMonitor.link(sysR11);
            _plot.input.link(sysR11);
    
            // Configure the plotter.
            _plot.place(getContentPane());
            _plot.plot.setBackground(getBackground());
            _plot.plot.setGrid(false);
            _plot.plot.setTitle("Events");
            _plot.plot.addLegend(0, "Received");
            _plot.plot.addLegend(1, "Sent");
            _plot.plot.addLegend(2, "AltBit");
            _plot.plot.setXLabel("Time");
            _plot.plot.setYLabel("Events");
            _plot.plot.setXRange(0.0, _getStopTime());
            _plot.plot.setYRange(0.0, 12.0);
            _plot.plot.setConnected(false);
            _plot.plot.setImpulses(true);
            _plot.plot.setMarksStyle("dots");
            _plot.fillOnWrapup.setToken(new BooleanToken(false));

            // We are now allowed to run the model.
            _initCompleted = true;

            // The 2 argument requests a go and stop button.
            getContentPane().add(_createRunControls(2));

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final boolean DEBUG = true;
    public static final String TIME_OUT = "2.5";
    public static final String RESET = "-1.0";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the model.  This overrides the base class to read the
     *  values in the query box first and set parameters.
     *  @exception IllegalActionException If topology changes on the
     *   model or parameter changes on the actors throw it.
     */     
    protected void _go() throws IllegalActionException {
        // If an exception occurred during initialization, then we don't
        // want to run here.  The model is probably not complete.
        if (!_initCompleted) return;

        // If the manager is not idle then either a run is in progress
        // or the model has been corrupted.  In either case, we do not
        // want to run.
        if (_manager.getState() != _manager.IDLE) return;

        // Set the values from the query.
         
        _forward.dropRate.setToken
            (new DoubleToken(_query.doubleValue("forwardRate")));
        _backward.dropRate.setToken
            (new DoubleToken(_query.doubleValue("backwardRate")));
 
        // The the X range of the plotter to show the full run.
        // The method being called is a protected member of DEApplet.
        _plot.plot.setXRange(0.0, _getStopTime());

        // The superclass sets the stop time of the director based on
        // the value in the entry box on the screen.  Then it starts
        // execution of the model in its own thread, leaving the user
        // interface of this applet live.
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag to prevent spurious exception being thrown by _go() method.
    // If this flag is not true, the _go() method will not execute the model.
    private boolean _initCompleted = false;

    private Query _query;

    // message source
    private DEMessageSource _msgSrc;
    
    // timer
    private DETimer _timer;

    // forward packet channel
    private DEChannel _forward;
    
    // backward packet channel
    private DEChannel _backward;
    
    // the plot
    private TimedPlotter _plot;
}
