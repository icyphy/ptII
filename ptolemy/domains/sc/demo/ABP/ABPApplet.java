/* An applet that uses Ptolemy II CT and DE domains.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.sc.demo.ABP;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.sc.kernel.*;
import ptolemy.domains.sc.lib.*;
import ptolemy.automata.util.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.plot.*;
import java.util.Enumeration;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;


//////////////////////////////////////////////////////////////////////////
//// SigmaDeltaApplet
/**
An applet that uses Ptolemy II DE domain.

@author Lukito Muliadi
@version $Id$
*/
public class ABPApplet extends Applet {

    public static final boolean DEBUG = true;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {

        // Process the background parameter.
        Color background = Color.white;
        try {
            String colorspec = getParameter("background");
            if (colorspec != null) {
                background = Color.decode(colorspec);
            }
        } catch (Exception ex) {}
        setBackground(background);

        // Initialization

        _stopTimeBox = new TextField("60.0", 10);
        _fdrBox = new TextField("0.3", 10);
        _bdrBox = new TextField("0.3", 10);
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _goButton = new Button("Go");
        _pauseButton = new Button(" Pause ");
        _finishButton = new Button("Finish");
        _terminateButton = new Button("Terminate");

        

        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(1,1));
        add(appletPanel, "Center");
        
        // _la is the drawing panel for DELogicAnalyzer actor.
        Plot dePanel = new Plot();
        appletPanel.add(dePanel);

        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding simulation parameter panel in the control panel.
        Panel simulationParam = new Panel();
        simulationParam.setLayout(new GridLayout(2,2));
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

        // Adding sample time (minimum service time) in the simulation panel
        Panel fdrPanel = new Panel();
        simulationParam.add(fdrPanel);
        fdrPanel.add(new Label("Forward Drop Rate"));
        fdrPanel.add(_fdrBox);
        // done adding sample

        // Adding Stop time in the simulation panel.
        Panel subSimul = new Panel();
        simulationParam.add(subSimul);
        subSimul.add(new Label("Stop time:"));
        subSimul.add(_stopTimeBox);
        // Done adding stop time.

        // Adding IST (interrupt service time) in the simulation panel
        Panel bdrPanel = new Panel();
        simulationParam.add(bdrPanel);
        bdrPanel.add(new Label("Backward Drop Rate"));
        bdrPanel.add(_bdrBox);
        // done adding IST

        // Adding current time in the sub panel.
        simulationParam.add(_currentTimeLabel);
        // Done adding average wait time.
        
        // Adding go button in the control panel.
        controlPanel.add(_goButton);
        controlPanel.add(_pauseButton);
        controlPanel.add(_finishButton);
        controlPanel.add(_terminateButton);

        _goButton.addActionListener(new GoButtonListener());
        _pauseButton.addActionListener(new PauseButtonListener());
        _finishButton.addActionListener(new FinishButtonListener());
        _terminateButton.addActionListener(new TerminateButtonListener());
        // Done adding go button


        // Creating the topology.
        try {
            // the top level composite actor
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName("ABP_Model");

            // the top level manager
            Manager mgr = new Manager();
            sys.setManager(mgr);
            mgr.addExecutionListener(new MyExecutionListener());

            // the top level DE director
            DEDirector dedir = new DEDirector("DETopLevelDirector");
            sys.setDirector(dedir);

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
            ABPPlot plot = new ABPPlot(sys, "Plot", dePanel);

            // sender - a hierarchical FSM
            TypedCompositeActor sender = new TypedCompositeActor(sys, "Sender");
            // create ports
            TypedIOPort sdrRequest = (TypedIOPort)sender.newPort("request");
            sdrRequest.setInput(true);
            sdrRequest.setDeclaredType(Token.class);
            TypedIOPort sdrMsgIn = (TypedIOPort)sender.newPort("msgIn");
            sdrMsgIn.setInput(true);
            sdrMsgIn.setDeclaredType(IntToken.class);
            TypedIOPort sdrNext = (TypedIOPort)sender.newPort("next");
            sdrNext.setOutput(true);
            sdrNext.setDeclaredType(Token.class);
            TypedIOPort sdrError = (TypedIOPort)sender.newPort("error");
            sdrError.setOutput(true);
            sdrError.setDeclaredType(Token.class);
            TypedIOPort sdrAck = (TypedIOPort)sender.newPort("ack");
            sdrAck.setInput(true);
            sdrAck.setDeclaredType(IntToken.class);
            TypedIOPort sdrPktOut = (TypedIOPort)sender.newPort("pktOut");
            sdrPktOut.setOutput(true);
            sdrPktOut.setDeclaredType(IntToken.class);
            TypedIOPort sdrSetTimer = (TypedIOPort)sender.newPort("setTimer");
            sdrSetTimer.setOutput(true);
            sdrSetTimer.setDeclaredType(DoubleToken.class);
            TypedIOPort sdrExpired = (TypedIOPort)sender.newPort("expired");
            sdrExpired.setInput(true);
            sdrExpired.setDeclaredType(Token.class);
            TypedIOPort sdrMonitor = (TypedIOPort)sender.newPort("monitor");
            sdrMonitor.setOutput(true);
            sdrMonitor.setDeclaredType(IntToken.class);

            // sender's top level controller
            DEFSMActor ctrl = new DEFSMActor(sender, "Controller");
            // controller has no port, only listen to its refinements
            // controller's states and transitions
            SCState ctrlConnecting = new SCState(ctrl, "Connecting");
            SCState ctrlDead = new SCState(ctrl, "Dead");
            SCState ctrlSending = new SCState(ctrl, "Sending");
            ctrl.setInitialState(ctrlConnecting);
            SCTransition ctrlTr1 =
                    ctrl.createTransition(ctrlConnecting, ctrlSending);
            ctrlTr1.setTriggerEvent("next");
            SCTransition ctrlTr2 =
                    ctrl.createTransition(ctrlConnecting, ctrlDead);
            ctrlTr2.setTriggerEvent("error");

            // sender's director
            SCDirector sdrDir = new SCDirector("SenderDirector");
            sender.setDirector(sdrDir);
            sdrDir.setController(ctrl);

            // submachine refining sender's connecting state
            DEFSMActor connect = new DEFSMActor(sender, "Connect");
            // ports
            TypedIOPort conRequest = (TypedIOPort)connect.newPort("request");
            conRequest.setInput(true);
            conRequest.setDeclaredType(Token.class);
            TypedIOPort conNext = (TypedIOPort)connect.newPort("next");
            conNext.setOutput(true);
            conNext.setDeclaredType(Token.class);
            TypedIOPort conError = (TypedIOPort)connect.newPort("error");
            conError.setOutput(true);
            conError.setDeclaredType(Token.class);
            TypedIOPort conAck = (TypedIOPort)connect.newPort("ack");
            conAck.setInput(true);
            conAck.setDeclaredType(IntToken.class);
            TypedIOPort conPktOut = (TypedIOPort)connect.newPort("pktOut");
            conPktOut.setOutput(true);
            conPktOut.setDeclaredType(IntToken.class);
            TypedIOPort conSetTimer = (TypedIOPort)connect.newPort("setTimer");
            conSetTimer.setOutput(true);
            conSetTimer.setDeclaredType(DoubleToken.class);
            TypedIOPort conExpired = (TypedIOPort)connect.newPort("expired");
            conExpired.setInput(true);
            conExpired.setDeclaredType(Token.class);
            // connect's states and transitions
            SCState conInit = new SCState(connect, "Init");
            SCState conWait = new SCState(connect, "Wait");
            SCState conSucc = new SCState(connect, "Success");
            SCState conFail = new SCState(connect, "Fail");
            connect.setInitialState(conInit);
            SCTransition conTr1 = connect.createTransition(conInit, conWait);
            conTr1.setTriggerEvent("request");
            conTr1.addTriggerAction("pktOut", "-1");
            conTr1.addTriggerAction("setTimer", TIME_OUT);
            conTr1.addLocalVariableUpdate("count", "5");
            SCTransition conTr2 = connect.createTransition(conWait, conSucc);
            conTr2.setTriggerEvent("ack");
            conTr2.setTriggerCondition("ack == -1");
            conTr2.addTriggerAction("next", null);
            conTr2.addTriggerAction("setTimer", RESET);
            SCTransition conTr3 = connect.createTransition(conWait, conFail);
            conTr3.setTriggerEvent("!ack & expired");
            conTr3.setTriggerCondition("count == 0");
            conTr3.addTriggerAction("error", null);
            SCTransition conTr4 = connect.createTransition(conWait, conFail);
            conTr4.setTriggerEvent("ack & expired");
            conTr4.setTriggerCondition("(ack != -1) && (count == 0)");
            conTr4.addTriggerAction("error", null);
            SCTransition conTr5 = connect.createTransition(conWait, conWait);
            conTr5.setTriggerEvent("!ack & expired");
            conTr5.setTriggerCondition("count != 0");
            conTr5.addTriggerAction("pktOut", "-1");
            conTr5.addTriggerAction("setTimer", TIME_OUT);
            conTr5.addLocalVariableUpdate("count", "count - 1");
            SCTransition conTr6 = connect.createTransition(conWait, conWait);
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
            sendMsgIn.setDeclaredType(IntToken.class);
            TypedIOPort sendNext = (TypedIOPort)send.newPort("next");
            sendNext.setOutput(true);
            sendNext.setDeclaredType(Token.class);
            TypedIOPort sendAck = (TypedIOPort)send.newPort("ack");
            sendAck.setInput(true);
            sendAck.setDeclaredType(IntToken.class);
            TypedIOPort sendPktOut = (TypedIOPort)send.newPort("pktOut");
            sendPktOut.setOutput(true);
            sendPktOut.setDeclaredType(IntToken.class);
            TypedIOPort sendSetTimer = (TypedIOPort)send.newPort("setTimer");
            sendSetTimer.setOutput(true);
            sendSetTimer.setDeclaredType(DoubleToken.class);
            TypedIOPort sendExpired = (TypedIOPort)send.newPort("expired");
            sendExpired.setInput(true);
            sendExpired.setDeclaredType(Token.class);
            TypedIOPort sendMonitor = (TypedIOPort)send.newPort("monitor");
            sendMonitor.setOutput(true);
            sendMonitor.setDeclaredType(IntToken.class);
            // the states and transitions
            SCState s0 = new SCState(send, "0");
            SCState s1 = new SCState(send, "1");
            send.setInitialState(s0);
            SCTransition sendTr1 = send.createTransition(s0, s0);
            sendTr1.setTriggerEvent("msgIn");
            sendTr1.addTriggerAction("pktOut", "msgIn*2");
            sendTr1.addTriggerAction("monitor", "0");
            sendTr1.addTriggerAction("setTimer", TIME_OUT);
            sendTr1.addLocalVariableUpdate("trying", "true");
            sendTr1.addLocalVariableUpdate("msg", "msgIn");
            SCTransition sendTr2 = send.createTransition(s0, s0);
            sendTr2.setTriggerEvent("!ack & expired");
            sendTr2.setTriggerCondition("trying");
            sendTr2.addTriggerAction("pktOut", "msg*2");
            sendTr2.addTriggerAction("monitor", "0");
            sendTr2.addTriggerAction("setTimer", TIME_OUT);
            SCTransition sendTr3 = send.createTransition(s0, s0);
            sendTr3.setTriggerEvent("ack & expired");
            sendTr3.setTriggerCondition("trying && (ack != 0)");
            sendTr3.addTriggerAction("pktOut", "msg*2");
            sendTr3.addTriggerAction("monitor", "0");
            sendTr3.addTriggerAction("setTimer", TIME_OUT);
            SCTransition sendTr4 = send.createTransition(s0, s1);
            sendTr4.setTriggerEvent("ack");
            sendTr4.setTriggerCondition("trying && (ack == 0)");
            sendTr4.addTriggerAction("setTimer", RESET);
            sendTr4.addTriggerAction("next", null);
            sendTr4.addLocalVariableUpdate("trying", "false");
            SCTransition sendTr5 = send.createTransition(s1, s1);
            sendTr5.setTriggerEvent("msgIn");
            sendTr5.addTriggerAction("pktOut", "msgIn*2 + 1");
            sendTr5.addTriggerAction("monitor", "1");
            sendTr5.addTriggerAction("setTimer", TIME_OUT);
            sendTr5.addLocalVariableUpdate("trying", "true");
            sendTr5.addLocalVariableUpdate("msg", "msgIn");
            SCTransition sendTr6 = send.createTransition(s1, s1);
            sendTr6.setTriggerEvent("!ack & expired");
            sendTr6.setTriggerCondition("trying");
            sendTr6.addTriggerAction("pktOut", "msg*2 + 1");
            sendTr6.addTriggerAction("monitor", "1");
            sendTr6.addTriggerAction("setTimer", TIME_OUT);
            SCTransition sendTr7 = send.createTransition(s1, s1);
            sendTr7.setTriggerEvent("ack & expired");
            sendTr7.setTriggerCondition("trying && (ack != 1)");
            sendTr7.addTriggerAction("pktOut", "msg*2 + 1");
            sendTr7.addTriggerAction("monitor", "1");
            sendTr7.addTriggerAction("setTimer", TIME_OUT);
            SCTransition sendTr8 = send.createTransition(s1, s0);
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
            recPktIn.setDeclaredType(IntToken.class);
            TypedIOPort recAck = (TypedIOPort)receiver.newPort("ack");
            recAck.setOutput(true);
            recAck.setDeclaredType(IntToken.class);
            TypedIOPort recMsgOut = (TypedIOPort)receiver.newPort("msgOut");
            recMsgOut.setOutput(true);
            recMsgOut.setDeclaredType(IntToken.class);
            // states and transitions
            SCState recInit = new SCState(receiver, "Init");
            SCState recS0 = new SCState(receiver, "S0");
            SCState recS1 = new SCState(receiver, "S1");
            receiver.setInitialState(recInit);
            SCTransition recTr1 = receiver.createTransition(recInit, recInit);
            recTr1.setTriggerEvent("pktIn");
            recTr1.setTriggerCondition("pktIn == -1");
            recTr1.addTriggerAction("ack", "-1");
            SCTransition recTr2 = receiver.createTransition(recInit, recS1);
            recTr2.setTriggerEvent("pktIn");
            recTr2.setTriggerCondition("(pktIn%2) == 0");
            recTr2.addTriggerAction("ack", "0");
            recTr2.addTriggerAction("msgOut", "pktIn/2");
            SCTransition recTr3 = receiver.createTransition(recS1, recS1);
            recTr3.setTriggerEvent("pktIn");
            recTr3.setTriggerCondition("(pktIn%2) == 0");
            recTr3.addTriggerAction("ack", "0");
            SCTransition recTr4 = receiver.createTransition(recS1, recS0);
            recTr4.setTriggerEvent("pktIn");
            recTr4.setTriggerCondition("(pktIn%2) == 1");
            recTr4.addTriggerAction("ack", "1");
            recTr4.addTriggerAction("msgOut", "pktIn/2");
            SCTransition recTr5 = receiver.createTransition(recS0, recS0);
            recTr5.setTriggerEvent("pktIn");
            recTr5.setTriggerCondition("(pktIn%2) == 1");
            recTr5.addTriggerAction("ack", "1");
            SCTransition recTr6 = receiver.createTransition(recS0, recS1);
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
            String[] deLegends = {"Received", "Sent", "AltBit"};
            plot.setLegend(deLegends);
            
            // Setting up parameters.
            _fdRate = (Parameter)forward.getAttribute("DropRate");
            _bdRate = (Parameter)backward.getAttribute("DropRate");
            _localDirector = dedir;
            _manager = mgr;

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The thread that runs the simulation.
    private boolean _isSimulationRunning;

    // FIXME: Under jdk 1.2, the following can (and should) be private
    private DEDirector _localDirector;
    private Manager _manager;

    private TextField _stopTimeBox;
    private TextField _fdrBox;
    private TextField _bdrBox;
    private double _stopTime = 100.0;
    private Button _goButton;
    private Button _pauseButton;
    private Button _finishButton;
    private Button _terminateButton;
    

    private Label _currentTimeLabel;
    private boolean _isSimulationPaused = false;

    // Parameters of DEProcessor that we want to change.
    private String _fdr;
    private String _bdr;
    private Parameter _fdRate;
    private Parameter _bdRate;
    public static final String TIME_OUT = "2.5";
    public static final String RESET = "-1.0";
    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////


    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    // Show simulation progress.
    private class CurrentTimeThread extends Thread {
        public void run() {
            while (_isSimulationRunning) {
                // get the current time from director.
                double currenttime = _localDirector.getCurrentTime();
                _currentTimeLabel.setText("Current time = "+currenttime);
                try {
                    sleep(500);
                } catch (InterruptedException e) {}
            }
        }
    }

    private class MyExecutionListener extends DefaultExecutionListener {
        public void executionFinished(ExecutionEvent e) {
            super.executionFinished(e);
            _isSimulationRunning = false;
        }

    }

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (_isSimulationRunning) {
                System.out.println("Simulation still running.. hold on..");
                return;
            }

            try {

                // The simulation is started non-paused (of course :-) )
                _isSimulationPaused = false;
                _pauseButton.setLabel(" Pause ");

                // Set the stop time.
                String timespec = _stopTimeBox.getText();
                try {
                    Double spec = Double.valueOf(timespec);
                    _stopTime = spec.doubleValue();
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid stop time: " +ex.getMessage());
                    return;
                }
                
                // Set the minimum service time.
                try {
                    _fdr = _fdrBox.getText();
                    double f1 = (Double.valueOf(_fdr)).doubleValue();
                    _fdRate.setToken(new DoubleToken(f1));
                    _fdRate.parameterChanged(null);
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid minimum service time: " + 
                                       ex.getMessage());
                }

                // Set the interrupt service time.
                try {
                     _bdr = _bdrBox.getText();
                     double f2 = (Double.valueOf(_bdr)).doubleValue();
                     _bdRate.setToken(new DoubleToken(f2)); 
                     _bdRate.parameterChanged(null);
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid interrupt service time: " + 
                                       ex.getMessage());
                }

                _localDirector.setStopTime(_stopTime);
               

                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                _isSimulationRunning = true;
                ctt.start();

                _manager.startRun();

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    private class PauseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {

            if (_isSimulationPaused) {
                _isSimulationPaused = false;
                _manager.resume();
                _pauseButton.setLabel(" Pause ");

            } else {
                _isSimulationPaused = true;
                _manager.pause();
                _pauseButton.setLabel("Resume");

            }

        }
    }

    private class FinishButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _manager.terminate();
            _isSimulationRunning = false;
        }
    }

    private class TerminateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _manager.terminate();
        }
    }
}



