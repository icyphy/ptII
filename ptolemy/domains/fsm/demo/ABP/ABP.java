/* An application demonstrating hierarchical FSM embedded in DE for protocol
   modeling.

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
*/

package ptolemy.domains.fsm.demo.ABP;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.util.VariableList;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ABP - Alternating Bit Protocol
/**
An application demonstrating hierarchical FSM embedded in DE for protocol
modeling.

@author Xiaojun Liu
@version $Id$
*/
public class ABP {

    public static void main(String[] argv) {

        try {
            // the top level composite actor
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName("ABP_Model");

            // the top level manager
            Manager mgr = new Manager();
            sys.setManager(mgr);

            // the top level DE director
            DEDirector dedir = new DEDirector(sys, "DETopLevelDirector");

            // message source
            DEMessageSource msgSrc = new DEMessageSource(sys,
                    "MessageSource", 0.5);

            // timer
            DETimer timer = new DETimer(sys, "Timer");

            // forward packet channel
            DEChannel forward = new DEChannel(sys, "ForwardChannel",
                    0.3, 5.0, 0.5);

            // backward packet channel
            DEChannel backward = new DEChannel(sys, "BackwardChannel",
                    0.2, 2.0, 0.2);

            // the plot
            ABPPlot plot = new ABPPlot(sys, "Plot");

            // sender - a hierarchical FSM
            TypedCompositeActor sender = new TypedCompositeActor(sys, "Sender");
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

            // sender's top level controller
            DEFSMActor ctrl = new DEFSMActor(sender, "Controller");
            // controller has no port, only listen to its refinements
            // controller's states and transitions
            FSMState ctrlConnecting = new FSMState(ctrl, "Connecting");
            FSMState ctrlDead = new FSMState(ctrl, "Dead");
            FSMState ctrlSending = new FSMState(ctrl, "Sending");
            ctrl.setInitialState(ctrlConnecting);
            FSMTransition ctrlTr1 =
                ctrl.createTransition(ctrlConnecting, ctrlSending);
            ctrlTr1.setTriggerEvent("next");
            FSMTransition ctrlTr2 =
                ctrl.createTransition(ctrlConnecting, ctrlDead);
            ctrlTr2.setTriggerEvent("error");

            // sender's director
            FSMDirector sdrDir = new FSMDirector(sender, "SenderDirector");
            sdrDir.setController(ctrl);

            // submachine refining sender's connecting state
            DEFSMActor connect = new DEFSMActor(sender, "Connect");
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
            FSMState conInit = new FSMState(connect, "Init");
            FSMState conWait = new FSMState(connect, "Wait");
            FSMState conSucc = new FSMState(connect, "Success");
            FSMState conFail = new FSMState(connect, "Fail");
            connect.setInitialState(conInit);
            FSMTransition conTr1 = connect.createTransition(conInit, conWait);
            conTr1.setTriggerEvent("request");
            conTr1.addTriggerAction("pktOut", "-1");
            conTr1.addTriggerAction("setTimer", TIME_OUT);
            conTr1.addLocalVariableUpdate("count", "5");
            FSMTransition conTr2 = connect.createTransition(conWait, conSucc);
            conTr2.setTriggerEvent("ack");
            conTr2.setTriggerCondition("ack == -1");
            conTr2.addTriggerAction("next", null);
            conTr2.addTriggerAction("setTimer", RESET);
            FSMTransition conTr3 = connect.createTransition(conWait, conFail);
            conTr3.setTriggerEvent("!ack & expired");
            conTr3.setTriggerCondition("count == 0");
            conTr3.addTriggerAction("error", null);
            FSMTransition conTr4 = connect.createTransition(conWait, conFail);
            conTr4.setTriggerEvent("ack & expired");
            conTr4.setTriggerCondition("(ack != -1) && (count == 0)");
            conTr4.addTriggerAction("error", null);
            FSMTransition conTr5 = connect.createTransition(conWait, conWait);
            conTr5.setTriggerEvent("!ack & expired");
            conTr5.setTriggerCondition("count != 0");
            conTr5.addTriggerAction("pktOut", "-1");
            conTr5.addTriggerAction("setTimer", TIME_OUT);
            conTr5.addLocalVariableUpdate("count", "count - 1");
            FSMTransition conTr6 = connect.createTransition(conWait, conWait);
            conTr6.setTriggerEvent("ack & expired");
            conTr6.setTriggerCondition("(ack != -1) && (count != 0)");
            conTr6.addTriggerAction("pktOut", "-1");
            conTr6.addTriggerAction("setTimer", TIME_OUT);
            conTr6.addLocalVariableUpdate("count", "count - 1");
            // create the local variable
            connect.addLocalVariable("count", new IntToken(0));
            // set connect to be ctrlConnecting's refinement
            ctrlConnecting.setRefinement(connect);

            // the submachine refining sender's sending state
            DEFSMActor send = new DEFSMActor(sender, "Send");
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
            FSMState s0 = new FSMState(send, "0");
            FSMState s1 = new FSMState(send, "1");
            send.setInitialState(s0);
            FSMTransition sendTr1 = send.createTransition(s0, s0);
            sendTr1.setTriggerEvent("msgIn");
            sendTr1.addTriggerAction("pktOut", "msgIn*2");
            sendTr1.addTriggerAction("monitor", "0");
            sendTr1.addTriggerAction("setTimer", TIME_OUT);
            sendTr1.addLocalVariableUpdate("trying", "true");
            sendTr1.addLocalVariableUpdate("msg", "msgIn");
            FSMTransition sendTr2 = send.createTransition(s0, s0);
            sendTr2.setTriggerEvent("!ack & expired");
            sendTr2.setTriggerCondition("trying");
            sendTr2.addTriggerAction("pktOut", "msg*2");
            sendTr2.addTriggerAction("monitor", "0");
            sendTr2.addTriggerAction("setTimer", TIME_OUT);
            FSMTransition sendTr3 = send.createTransition(s0, s0);
            sendTr3.setTriggerEvent("ack & expired");
            sendTr3.setTriggerCondition("trying && (ack != 0)");
            sendTr3.addTriggerAction("pktOut", "msg*2");
            sendTr3.addTriggerAction("monitor", "0");
            sendTr3.addTriggerAction("setTimer", TIME_OUT);
            FSMTransition sendTr4 = send.createTransition(s0, s1);
            sendTr4.setTriggerEvent("ack");
            sendTr4.setTriggerCondition("trying && (ack == 0)");
            sendTr4.addTriggerAction("setTimer", RESET);
            sendTr4.addTriggerAction("next", null);
            sendTr4.addLocalVariableUpdate("trying", "false");
            FSMTransition sendTr5 = send.createTransition(s1, s1);
            sendTr5.setTriggerEvent("msgIn");
            sendTr5.addTriggerAction("pktOut", "msgIn*2 + 1");
            sendTr5.addTriggerAction("monitor", "1");
            sendTr5.addTriggerAction("setTimer", TIME_OUT);
            sendTr5.addLocalVariableUpdate("trying", "true");
            sendTr5.addLocalVariableUpdate("msg", "msgIn");
            FSMTransition sendTr6 = send.createTransition(s1, s1);
            sendTr6.setTriggerEvent("!ack & expired");
            sendTr6.setTriggerCondition("trying");
            sendTr6.addTriggerAction("pktOut", "msg*2 + 1");
            sendTr6.addTriggerAction("monitor", "1");
            sendTr6.addTriggerAction("setTimer", TIME_OUT);
            FSMTransition sendTr7 = send.createTransition(s1, s1);
            sendTr7.setTriggerEvent("ack & expired");
            sendTr7.setTriggerCondition("trying && (ack != 1)");
            sendTr7.addTriggerAction("pktOut", "msg*2 + 1");
            sendTr7.addTriggerAction("monitor", "1");
            sendTr7.addTriggerAction("setTimer", TIME_OUT);
            FSMTransition sendTr8 = send.createTransition(s1, s0);
            sendTr8.setTriggerEvent("ack");
            sendTr8.setTriggerCondition("trying && (ack == 1)");
            sendTr8.addTriggerAction("setTimer", RESET);
            sendTr8.addTriggerAction("next", null);
            sendTr8.addLocalVariableUpdate("trying", "false");
            // create the local variables
            send.addLocalVariable("trying", new BooleanToken(false));
            send.addLocalVariable("msg", new IntToken(0));
            // set to be ctrlSending's refinement
            ctrlSending.setRefinement(send);

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
            TypedIORelation sdrR7 =
                (TypedIORelation)sender.newRelation("msgIn");
            sdrMsgIn.link(sdrR7);
            sendMsgIn.link(sdrR7);
            TypedIORelation sdrR8 =
                (TypedIORelation)sender.newRelation("monitor");
            sdrMonitor.link(sdrR8);
            sendMonitor.link(sdrR8);

            // the receiver FSM
            DEFSMActor receiver = new DEFSMActor(sys, "Receiver");
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
            FSMState recInit = new FSMState(receiver, "Init");
            FSMState recS0 = new FSMState(receiver, "S0");
            FSMState recS1 = new FSMState(receiver, "S1");
            receiver.setInitialState(recInit);
            FSMTransition recTr1 = receiver.createTransition(recInit, recInit);
            recTr1.setTriggerEvent("pktIn");
            recTr1.setTriggerCondition("pktIn == -1");
            recTr1.addTriggerAction("ack", "-1");
            FSMTransition recTr2 = receiver.createTransition(recInit, recS1);
            recTr2.setTriggerEvent("pktIn");
            recTr2.setTriggerCondition("(pktIn%2) == 0");
            recTr2.addTriggerAction("ack", "0");
            recTr2.addTriggerAction("msgOut", "pktIn/2");
            FSMTransition recTr3 = receiver.createTransition(recS1, recS1);
            recTr3.setTriggerEvent("pktIn");
            recTr3.setTriggerCondition("(pktIn%2) == 0");
            recTr3.addTriggerAction("ack", "0");
            FSMTransition recTr4 = receiver.createTransition(recS1, recS0);
            recTr4.setTriggerEvent("pktIn");
            recTr4.setTriggerCondition("(pktIn%2) == 1");
            recTr4.addTriggerAction("ack", "1");
            recTr4.addTriggerAction("msgOut", "pktIn/2");
            FSMTransition recTr5 = receiver.createTransition(recS0, recS0);
            recTr5.setTriggerEvent("pktIn");
            recTr5.setTriggerCondition("(pktIn%2) == 1");
            recTr5.addTriggerAction("ack", "1");
            FSMTransition recTr6 = receiver.createTransition(recS0, recS1);
            recTr6.setTriggerEvent("pktIn");
            recTr6.setTriggerCondition("(pktIn%2) == 0");
            recTr6.addTriggerAction("ack", "0");
            recTr6.addTriggerAction("msgOut", "pktIn/2");

            // connect the top level system
            TypedIORelation sysR1 =
                (TypedIORelation)sys.newRelation("request");
            msgSrc.request.link(sysR1);
            sdrRequest.link(sysR1);
            TypedIORelation sysR2 =
                (TypedIORelation)sys.newRelation("msgIn");
            msgSrc.output.link(sysR2);
            sdrMsgIn.link(sysR2);
            TypedIORelation sysR3 =
                (TypedIORelation)sys.newRelation("pktOut");
            forward.input.link(sysR3);
            sdrPktOut.link(sysR3);
            TypedIORelation sysR4 =
                (TypedIORelation)sys.newRelation("sdrAck");
            backward.output.link(sysR4);
            sdrAck.link(sysR4);
            TypedIORelation sysR5 =
                (TypedIORelation)sys.newRelation("recAck");
            backward.input.link(sysR5);
            recAck.link(sysR5);
            TypedIORelation sysR6 =
                (TypedIORelation)sys.newRelation("msgOut");
            recMsgOut.link(sysR6);
            plot.input.link(sysR6);
            TypedIORelation sysR7 =
                (TypedIORelation)sys.newRelation("setTimer");
            timer.set.link(sysR7);
            sdrSetTimer.link(sysR7);
            TypedIORelation sysR8 =
                (TypedIORelation)sys.newRelation("expired");
            timer.expired.link(sysR8);
            sdrExpired.link(sysR8);
            TypedIORelation sysR9 =
                (TypedIORelation)sys.newRelation("pktIn");
            forward.output.link(sysR9);
            recPktIn.link(sysR9);
            TypedIORelation sysR10 =
                (TypedIORelation)sys.newRelation("next");
            msgSrc.next.link(sysR10);
            sdrNext.link(sysR10);

            plot.input.link(sysR2);
            TypedIORelation sysR11 =
                (TypedIORelation)sys.newRelation("monitor");
            sdrMonitor.link(sysR11);
            plot.input.link(sysR11);


            // finally we can run the system
            double stopTime = 60.0;
            if (argv.length > 0) {
                stopTime = Double.valueOf(argv[0]).doubleValue();
            }
            dedir.setStopTime(stopTime);
            mgr.startRun();

            // we have finished
            //            System.exit(0);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static final String TIME_OUT = "2.5";
    public static final String RESET = "-1.0";

}
