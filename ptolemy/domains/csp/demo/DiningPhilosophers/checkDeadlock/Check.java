/* Check the dining philosopher model.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.csp.demo.DiningPhilosophers.checkDeadlock;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.ia.InterfaceAutomaton;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// Check

/**
 Check the dining philosopher model.
 <p>Construct the interface automata model for the dining philosopher demo
 and check for deadlock.
 <br/>The primitive components in the model are: CSPReceiver, ConditionalSend,
 ConditionalBranchController, Philosopher, and Chopstick. It is assumed
 that the MoML files for these models are in the current directory.</p>
 <p>
 The number of philosophers around the dining table can be controlled on
 the command line. The usage is:</p>
 <pre>
 java ptolemy.domains.csp.demo.DiningPhilosophers.checkDeadlock.Check <i>numberOfPhilosophers</i> <i>useSimple</i>
 </pre>

 <p>The useSimple argument is either "simple" or "full", indicating if
 the simple or the full conditional send model is used. This
 argument is optional. The default is simple.</p>

 @author Yuhong Xiong
 @version $Id$
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 */
public class Check {
    /** Load the base automata.
     *  @param numberOfPhilosophers The number of philosophers.
     *  @param useSimple True if SimpleSend.xml is to be used
     *  @exception Exception If the automata cannot be loaded.
     */
    public Check(int numberOfPhilosophers, boolean useSimple) throws Exception {
        _numberOfPhilosophers = numberOfPhilosophers;
        _useSimple = useSimple;

        // path to the automata MoML files. Set to current directory for now,
        // should set it to
        // $PTII/ptolemy/domains/csp/demo/DiningPhilosophers/checkDeadlock
        String base = "./";

        // following the comments in MoMLApplication, use the same URL for
        // the two arguments (base and URL) to parse(). Also, a instance
        // of MoMLParser must be used to parse each file, otherwise
        // the same automaton will be returned the second time parse() is
        // called.
        URL url = ConfigurationApplication.specToURL(base + "CSPReceiver.xml");
        MoMLParser parser = new MoMLParser();
        _receiver = (InterfaceAutomaton) parser.parse(url, url);

        if (useSimple) {
            url = ConfigurationApplication.specToURL(base + "SimpleSend.xml");
            parser = new MoMLParser();
            _simpleSend = (InterfaceAutomaton) parser.parse(url, url);
        } else {
            url = ConfigurationApplication.specToURL(base
                    + "ConditionalSend.xml");
            parser = new MoMLParser();
            _send = (InterfaceAutomaton) parser.parse(url, url);

            url = ConfigurationApplication.specToURL(base
                    + "ConditionalBranchController.xml");
            parser = new MoMLParser();
            _controller = (InterfaceAutomaton) parser.parse(url, url);
        }

        url = ConfigurationApplication.specToURL(base + "Philosopher.xml");
        parser = new MoMLParser();
        _philosopher = (InterfaceAutomaton) parser.parse(url, url);

        url = ConfigurationApplication.specToURL(base + "Chopstick.xml");
        parser = new MoMLParser();
        _chopstick = (InterfaceAutomaton) parser.parse(url, url);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compose the automata and check for deadlock.
     *  @exception IllegalActionException If the automata cannot be composed.
     *  @exception NameDuplicationException If name clashes during composition.
     */
    public void go() throws IllegalActionException, NameDuplicationException {
        // Composition strategy:
        // (1) Compose Philosopher and two CSPReceiver;
        // (2) Compose Chopstick, ConditionalBranchController, two
        //     ConditionalSend, and two CSPReceiver;
        // (3) Compose the philosopher and chopstick model to form
        //     philosopher/chopstick pair;
        // (4) Compose all the pairs to form the complete model.
        //
        // Compose numberOfPhilosophers instances of philosopher/chopstick
        // pairs. Each instance is composed from scratch from clones of the
        // basic automata loaded above. This is for make the state names
        // of all the instances correct. If cloning the philosopher/chopstick
        // pairs from one instance, the state names in all the clones will
        // be the same as that in the master instance.
        InterfaceAutomaton[] phiCho = new InterfaceAutomaton[_numberOfPhilosophers];

        for (int i = 0; i < _numberOfPhilosophers; i++) {
            InterfaceAutomaton phiAndReceiver = _composePhiAndReceiver(i);
            phiAndReceiver.combineInternalTransitions();

            System.out.println(i + "th phiAndReceiver:");
            System.out.println(phiAndReceiver.getInfo());

            InterfaceAutomaton choAndReceiver = _composeChoAndReceiver(i);
            choAndReceiver.combineInternalTransitions();

            System.out.println(i + "th cho, receiver and send:");
            System.out.println(choAndReceiver.getInfo());

            phiCho[i] = phiAndReceiver.compose(choAndReceiver);
            phiCho[i].combineInternalTransitions();

            System.out.println(i + "th philosopher/chopstick pair:");
            System.out.println(phiCho[i].getInfo());
        }

        // compose all philosopher/chopstick pairs.
        InterfaceAutomaton all = phiCho[0];

        for (int i = 1; i < _numberOfPhilosophers; i++) {
            all = all.compose(phiCho[i]);
            all.combineInternalTransitions();

            System.out.println("0 to " + i + "th philosopher/chopstick pairs:");
            System.out.println(all.getInfo());
        }

        // System.out.println(all.exportMoML());
        // check for deadlock
        System.out.println("Deadlock States:");

        Iterator deadlockStates = all.deadlockStates().iterator();

        while (deadlockStates.hasNext()) {
            State state = (State) deadlockStates.next();
            System.out.println(state.getFullName());
        }
    }

    /** Obtain the command line arguments and create a checker.
     *  @param args The command line arguments. The first argument is the
     *   number of philosophers, the second is "simple" or "full", indicating
     *   if the simple or the full version of conditional send model is
     *   used. The second argument is optional, the default is simple.
     */
    public static void main(String[] args) {
        try {
            int number = Integer.parseInt(args[0]);
            boolean useSimple = true;

            if (args.length > 1) {
                if (args[1].equals("full")) {
                    useSimple = false;
                }
            }

            Check check = new Check(number, useSimple);
            check.go();
        } catch (Exception ex) {
            System.out
            .println(ex.getClass().getName() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // compose a chopstick and its left and right receivers, conditional
    // send, and the controller. The argument specifies the index of this
    // chopstick.
    private InterfaceAutomaton _composeChoAndReceiver(int index)
            throws IllegalActionException, NameDuplicationException {
        try {
            // general a chopstick with the correct name
            InterfaceAutomaton cho = (InterfaceAutomaton) _chopstick.clone();
            cho.setName("c" + index);

            HashMap nameMap = new HashMap();
            nameMap.put("g1", "c" + index + "gl");
            nameMap.put("g1R", "c" + index + "glR");

            nameMap.put("g2", "c" + index + "gr");
            nameMap.put("g2R", "c" + index + "grR");

            nameMap.put("c", "c" + index + "c");
            nameMap.put("c1", "c" + index + "cl");
            nameMap.put("c2", "c" + index + "cr");

            cho.renameTransitionLabels(nameMap);

            // create left receiver
            InterfaceAutomaton leftReceiver = (InterfaceAutomaton) _receiver
                    .clone();
            leftReceiver.setName("c" + index + "lr");

            nameMap = new HashMap();
            nameMap.put("p", "p" + index + "pr");
            nameMap.put("pR", "p" + index + "prR");

            nameMap.put("iGW", "p" + index + "iGWr");
            nameMap.put("iGWT", "p" + index + "iGWrT");
            nameMap.put("iGWF", "p" + index + "iGWrF");

            nameMap.put("g", "c" + index + "gl");
            nameMap.put("gR", "c" + index + "glR");

            leftReceiver.renameTransitionLabels(nameMap);

            // create right receiver
            InterfaceAutomaton rightReceiver = (InterfaceAutomaton) _receiver
                    .clone();
            rightReceiver.setName("c" + index + "rr");

            nameMap = new HashMap();

            // compute the index of the philosopher on the right
            int rightIndex = (index + 1) % _numberOfPhilosophers;

            nameMap.put("p", "p" + rightIndex + "pl");
            nameMap.put("pR", "p" + rightIndex + "plR");

            nameMap.put("iGW", "p" + rightIndex + "iGWl");
            nameMap.put("iGWT", "p" + rightIndex + "iGWlT");
            nameMap.put("iGWF", "p" + rightIndex + "iGWlF");

            nameMap.put("g", "c" + index + "gr");
            nameMap.put("gR", "c" + index + "grR");

            rightReceiver.renameTransitionLabels(nameMap);

            // compose
            InterfaceAutomaton choWithReceivers = cho.compose(leftReceiver);
            choWithReceivers = choWithReceivers.compose(rightReceiver);
            choWithReceivers.combineInternalTransitions();

            InterfaceAutomaton send = _composeSend(index);

            InterfaceAutomaton whole = choWithReceivers.compose(send);

            return whole;
        } catch (CloneNotSupportedException cnse) {
            throw new InternalErrorException("Check._composeChoAndReceiver: "
                    + "clone not supported: " + cnse.getMessage());
        }
    }

    // compose a philosopher and its left and right receivers. The
    // argument specify the index of this philosopher.
    private InterfaceAutomaton _composePhiAndReceiver(int index)
            throws IllegalActionException, NameDuplicationException {
        try {
            // generate a philosopher with the correct name
            InterfaceAutomaton phi = (InterfaceAutomaton) _philosopher.clone();
            phi.setName("p" + index);

            HashMap nameMap = new HashMap();
            nameMap.put("g1", "p" + index + "gl");
            nameMap.put("g1R", "p" + index + "glR");
            nameMap.put("g2", "p" + index + "gr");
            nameMap.put("g2R", "p" + index + "grR");

            nameMap.put("p1", "p" + index + "pl");
            nameMap.put("p1R", "p" + index + "plR");
            nameMap.put("p2", "p" + index + "pr");
            nameMap.put("p2R", "p" + index + "prR");

            nameMap.put("iGW1", "p" + index + "iGWl");
            nameMap.put("iGW1T", "p" + index + "iGWlT");
            nameMap.put("iGW1F", "p" + index + "iGWlF");

            nameMap.put("iGW2", "p" + index + "iGWr");
            nameMap.put("iGW2T", "p" + index + "iGWrT");
            nameMap.put("iGW2F", "p" + index + "iGWrF");

            phi.renameTransitionLabels(nameMap);

            // create left receiver
            InterfaceAutomaton leftReceiver = (InterfaceAutomaton) _receiver
                    .clone();
            leftReceiver.setName("p" + index + "lr");

            nameMap = new HashMap();

            // compute the index of the chopstick on the left
            int leftIndex = (index + _numberOfPhilosophers - 1)
                    % _numberOfPhilosophers;

            nameMap.put("p", "c" + leftIndex + "pr");
            nameMap.put("pR", "c" + leftIndex + "prR");

            nameMap.put("iGW", "c" + leftIndex + "iGWr");
            nameMap.put("iGWT", "c" + leftIndex + "iGWrT");
            nameMap.put("iGWF", "c" + leftIndex + "iGWrF");

            nameMap.put("g", "p" + index + "gl");
            nameMap.put("gR", "p" + index + "glR");

            leftReceiver.renameTransitionLabels(nameMap);

            // create right receiver
            InterfaceAutomaton rightReceiver = (InterfaceAutomaton) _receiver
                    .clone();
            rightReceiver.setName("p" + index + "rr");

            nameMap = new HashMap();

            nameMap.put("p", "c" + index + "pl");
            nameMap.put("pR", "c" + index + "plR");

            nameMap.put("iGW", "c" + index + "iGWl");
            nameMap.put("iGWT", "c" + index + "iGWlT");
            nameMap.put("iGWF", "c" + index + "iGWlF");

            nameMap.put("g", "p" + index + "gr");
            nameMap.put("gR", "p" + index + "grR");

            rightReceiver.renameTransitionLabels(nameMap);

            InterfaceAutomaton phiWithReceivers = phi.compose(leftReceiver);
            phiWithReceivers = phiWithReceivers.compose(rightReceiver);

            return phiWithReceivers;
        } catch (CloneNotSupportedException cnse) {
            throw new InternalErrorException("Check._composePhiAndReceiver: "
                    + "clone not supported: " + cnse.getMessage());
        }
    }

    // compose the simple or the full version of conditional send.
    private InterfaceAutomaton _composeSend(int index)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        InterfaceAutomaton send;

        if (_useSimple) {
            send = (InterfaceAutomaton) _simpleSend.clone();
            send.setName("c" + index + "s");

            HashMap nameMap = new HashMap();
            nameMap.put("p1", "c" + index + "pl");
            nameMap.put("p1R", "c" + index + "plR");

            nameMap.put("iGW1", "c" + index + "iGWl");
            nameMap.put("iGW1T", "c" + index + "iGWlT");
            nameMap.put("iGW1F", "c" + index + "iGWlF");

            nameMap.put("p2", "c" + index + "pr");
            nameMap.put("p2R", "c" + index + "prR");

            nameMap.put("iGW2", "c" + index + "iGWr");
            nameMap.put("iGW2T", "c" + index + "iGWrT");
            nameMap.put("iGW2F", "c" + index + "iGWrF");

            nameMap.put("c", "c" + index + "c");
            nameMap.put("c1", "c" + index + "cl");
            nameMap.put("c2", "c" + index + "cr");

            send.renameTransitionLabels(nameMap);
            return send;
        } else {
            // create conditional branch controller
            InterfaceAutomaton controller = (InterfaceAutomaton) _controller
                    .clone();
            controller.setName("c" + index + "c");

            HashMap nameMap = new HashMap();
            nameMap.put("iA1", "c" + index + "iAl");
            nameMap.put("iA1T", "c" + index + "iAlT");
            nameMap.put("iA1F", "c" + index + "iAlF");

            nameMap.put("iF1", "c" + index + "iFl");
            nameMap.put("iF1T", "c" + index + "iFlT");
            nameMap.put("iF1F", "c" + index + "iFlF");

            nameMap.put("r1", "c" + index + "rl");
            nameMap.put("d1", "c" + index + "dl");

            nameMap.put("iA2", "c" + index + "iAr");
            nameMap.put("iA2T", "c" + index + "iArT");
            nameMap.put("iA2F", "c" + index + "iArF");

            nameMap.put("iF2", "c" + index + "iFr");
            nameMap.put("iF2T", "c" + index + "iFrT");
            nameMap.put("iF2F", "c" + index + "iFrF");

            nameMap.put("r2", "c" + index + "rr");
            nameMap.put("d2", "c" + index + "dr");

            nameMap.put("c", "c" + index + "c");
            nameMap.put("c1", "c" + index + "cl");
            nameMap.put("c2", "c" + index + "cr");

            controller.renameTransitionLabels(nameMap);

            // create left conditional send
            InterfaceAutomaton leftSend = (InterfaceAutomaton) _send.clone();
            leftSend.setName("c" + index + "sl");

            nameMap = new HashMap();
            nameMap.put("p", "c" + index + "pl");
            nameMap.put("pR", "c" + index + "plR");

            nameMap.put("iGW", "c" + index + "iGWl");
            nameMap.put("iGWT", "c" + index + "iGWlT");
            nameMap.put("iGWF", "c" + index + "iGWlF");

            nameMap.put("iA", "c" + index + "iAl");
            nameMap.put("iAT", "c" + index + "iAlT");
            nameMap.put("iAF", "c" + index + "iAlF");

            nameMap.put("iF", "c" + index + "iFl");
            nameMap.put("iFT", "c" + index + "iFlT");
            nameMap.put("iFF", "c" + index + "iFlF");

            nameMap.put("r", "c" + index + "rl");
            nameMap.put("d", "c" + index + "dl");

            leftSend.renameTransitionLabels(nameMap);

            // create right conditional send
            InterfaceAutomaton rightSend = (InterfaceAutomaton) _send.clone();
            rightSend.setName("c" + index + "sr");

            nameMap = new HashMap();
            nameMap.put("p", "c" + index + "pr");
            nameMap.put("pR", "c" + index + "prR");

            nameMap.put("iGW", "c" + index + "iGWr");
            nameMap.put("iGWT", "c" + index + "iGWrT");
            nameMap.put("iGWF", "c" + index + "iGWrF");

            nameMap.put("iA", "c" + index + "iAr");
            nameMap.put("iAT", "c" + index + "iArT");
            nameMap.put("iAF", "c" + index + "iArF");

            nameMap.put("iF", "c" + index + "iFr");
            nameMap.put("iFT", "c" + index + "iFrT");
            nameMap.put("iFF", "c" + index + "iFrF");

            nameMap.put("r", "c" + index + "rr");
            nameMap.put("d", "c" + index + "dr");

            rightSend.renameTransitionLabels(nameMap);

            send = controller.compose(leftSend);
            send = send.compose(rightSend);

            send.combineInternalTransitions();

            System.out.println("Controller and two send, "
                    + "after combining internals:");
            System.out.println(send.getInfo());
        }

        return send;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private InterfaceAutomaton _receiver;

    // if useSimple is false:
    private InterfaceAutomaton _send;

    private InterfaceAutomaton _controller;

    // if useSimple is true:
    private InterfaceAutomaton _simpleSend;

    private InterfaceAutomaton _philosopher;

    private InterfaceAutomaton _chopstick;

    private int _numberOfPhilosophers;

    private boolean _useSimple = true;
}
