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

import ptolemy.actor.gui.MoMLApplication;
import ptolemy.moml.MoMLParser;
import ptolemy.domains.fsm.kernel.InterfaceAutomaton;

import java.net.URL;

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

    /** Compose the interface automata model for the dining philosopher
     *  model and check for deadlock.
     *  @param numberOfPhilosophers The number of philosophers.
     *  @exception Exception If the model cannot be constructored or checked.
     */
    public Check(int numberOfPhilosophers) throws Exception {
        // Composition strategy:
        // (1) Compose Philosopher and two CSPReceiver to form the philosopher
        //     model template;
        // (2) Compose Chopstick, ConditionalBranchController, two
        //     ConditionalSend, and two CSPReceiver to form the chopstick
        //     model template;
        // (3) Compose the philosopher and chopstick model templates to form
        //     philosopher-chopstick pair template;
        // (4) Compose the specified number of the above pair to form the
        //     complete model.
        // In each step, the transition labels in the templates need to be
        // renamed before composition.

        // load all automata.

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
        InterfaceAutomaton receiver =
                               (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base + "ConditionalSend.xml");
        parser = new MoMLParser();
        InterfaceAutomaton send = (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base
                                      + "ConditionalBranchController.xml");
        parser = new MoMLParser();
        InterfaceAutomaton controller =
                                 (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base + "Philosopher.xml");
        parser = new MoMLParser();
        InterfaceAutomaton philosopher =
                                 (InterfaceAutomaton)parser.parse(url, url);

        url = MoMLApplication.specToURL(base + "Chopstick.xml");
        parser = new MoMLParser();
        InterfaceAutomaton chopstick =
                                 (InterfaceAutomaton)parser.parse(url, url);

        //
        // Compose the philosopher model template
        //
    }

    /** Get the number of philosopher parameter from the command line
     *  argument and pass it to the constructor.
     *  @param args The command line arguments.
     */
    public static void main (String[] args) {
        try {
            int number = (Integer.valueOf(args[0])).intValue();
            new Check(number);
        } catch (Exception ex) {
            System.out.println(ex.getClass().getName() + ": "
                             + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
