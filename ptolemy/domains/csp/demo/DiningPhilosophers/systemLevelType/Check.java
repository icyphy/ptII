/* Check the dining philosopher model.

 Copyright (c) 1999-2001 The Regents of the University of California.
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
@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.DiningPhilosophers.systemLevelType;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.moml.MoMLParser;
import ptolemy.domains.fsm.kernel.InterfaceAutomaton;

import java.net.URL;
import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// Check
/**
Check the dining philosopher model.
Construct the interface automata model for the dining philosopher demo
and check for deadlock.
The primitive components in the model are: CSPReceiver, ConditionalSend,
ConditionalBranchController, Philosopher, and Chopstick. It is assumed
that the MoML files for these models are in the current directory.
<p>
The number of philosophers around the dining table can be controlled on
the command line. The usage is:
<pre>
java ptolemy.domains.csp.demo.DiningPhilosophers.systemLevelType.Check <numberOfPhilosophers>
</pre>

@author Yuhong Xiong
@version $Id$
*/

public class Check {
    /** Load the base automata.
     *  @param numberOfPhilosophers The number of philosophers.
     *  @exception Exception If the automata cannot be loaded.
     */
    public Check(int numberOfPhilosophers) throws Exception {
        _numberOfPhilosophers = numberOfPhilosophers;

        // path to the automata MoML files. Set to current directory for now,
        // should set it to
        // $PTII/ptolemy/domains/csp/demo/DiningPhilosophers/systemLevelType
        String base = "./";

        // following the comments in MoMLApplication, use the same URL for
        // the two arguments (base and URL) to parse(). Also, a instance
        // of MoMLParser must be used to parse each file, otherwise
        // the same automaton will be returned the second time parse() is
        // called.
        URL url = MoMLApplication.specToURL(base + "CSPReceiver.xml");
        MoMLParser parser = new MoMLParser();
        _receiver = (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base + "ConditionalSend.xml");
        parser = new MoMLParser();
        _send = (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base
                                      + "ConditionalBranchController.xml");
        parser = new MoMLParser();
        _controller = (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base + "Philosopher.xml");
        parser = new MoMLParser();
        _philosopher = (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base + "Chopstick.xml");
        parser = new MoMLParser();
        _chopstick = (InterfaceAutomaton)parser.parse(url, url);
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
      InterfaceAutomaton[] phiCho =
                         new InterfaceAutomaton[_numberOfPhilosophers];
        for (int i=0; i<_numberOfPhilosophers; i++) {
          InterfaceAutomaton phiAndReceiver = _composePhiAndReceiver(i);
          InterfaceAutomaton choAndReceiver = _composeChoAndReceiver(i);
          phiCho[i] = phiAndReceiver.compose(choAndReceiver);
      }

      // compose all philosopher/chopstick pairs.
        InterfaceAutomaton all = phiCho[0];
      for (int i=1; i<_numberOfPhilosophers; i++) {
          all = all.compose(phiCho[i]);
      }

        // check for deadlock
    }

    /** Get the number of philosopher parameter from the command line
     *  argument and pass it to the constructor.
     *  @param args The command line arguments.
     */
    public static void main (String[] args) {
        try {
            int number = (Integer.valueOf(args[0])).intValue();
            Check check = new Check(number);
          check.go();
        } catch (Exception ex) {
            System.out.println(ex.getClass().getName() + ": "
                             + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

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
            InterfaceAutomaton cho = (InterfaceAutomaton)_chopstick.clone();




            return null;
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
            InterfaceAutomaton phi = (InterfaceAutomaton)_philosopher.clone();
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

System.out.println(phi.exportMoML());



            return null;
        } catch (CloneNotSupportedException cnse) {
            throw new InternalErrorException("Check._composePhiAndReceiver: "
              + "clone not supported: " + cnse.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private InterfaceAutomaton _receiver;
    private InterfaceAutomaton _send;
    private InterfaceAutomaton _controller;
    private InterfaceAutomaton _philosopher;
    private InterfaceAutomaton _chopstick;

    private int _numberOfPhilosophers;
}
