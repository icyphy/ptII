/* Compute the alternating simulation of two interface automata.

 Copyright (c) 1999-2002 The Regents of the University of California.
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

package ptolemy.domains.fsm.kernel.test;

import ptolemy.actor.gui.MoMLApplication;
import ptolemy.moml.MoMLParser;
import ptolemy.domains.fsm.kernel.InterfaceAutomaton;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.StatePair;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// AlternatingSimulation
/**
Compute the alternating simulation of two interface automata.
This class reads the MoML description of a super automaton and a sub automaton,
computes the unique maximal alternating simulation relation from the
sub automaton to the super one, then lists all the state pairs in the relation
to stdout. The usage is:
<pre>
java ptolemy.domains.fsm.kernel.test.AlternatingSimulation <super_automaton.xml> <sub_automaton.xml>
</pre>

@author Yuhong Xiong
@version $Id$
*/

public class AlternatingSimulation {

    /** Compute the alternating simulation from the sub automaton to the
     *  super one and list the result to stdout.
     *  @param superMoML The MoML file name for the super interface automaton.
     *  @param subMoML The MoML file name for the sub interface automaton.
     *  @exception Exception If the specified automata cannot be constructed.
     */
    public AlternatingSimulation (String superMoML, String subMoML)
            throws Exception {
        // Construct the super automaton
        URL url = MoMLApplication.specToURL(superMoML);
        // following the comments in MoMLApplication, use the same URL for
        // the two arguments (base and URL) to parse().
        MoMLParser parser = new MoMLParser();
        InterfaceAutomaton superAutomaton =
                                (InterfaceAutomaton)parser.parse(url, url);
        superAutomaton.addPorts();

        // Construct the sub automaton
        url = MoMLApplication.specToURL(subMoML);
        // following the comments in MoMLApplication, use the same URL for
        // the two arguments (base and URL) to parse().  Also, a new instance
        // of MoMLParser must be used to parse each file, otherwise
        // the same automaton will be returned the second time parse() is
        // called.
        parser = new MoMLParser();
        InterfaceAutomaton subAutomaton =
                                (InterfaceAutomaton)parser.parse(url, url);
        subAutomaton.addPorts();

        // Compute alternating simulation
        Set alternatingSimulation =
                superAutomaton.computeAlternatingSimulation(subAutomaton);

        // Display result
        if (alternatingSimulation.isEmpty()) {
            System.out.println("No alternating simulation between the "
                        + "specified automata.");
        } else {
            System.out.println("Alternating simulation (state_in_"
                    + superAutomaton.getName() + " - state_in_"
                    + subAutomaton.getName() + "):");
            Iterator pairs = alternatingSimulation.iterator();
            while (pairs.hasNext()) {
                StatePair pair = (StatePair)pairs.next();
                System.out.println(pair.toString());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Pass the command line arguments to the constructor. The command line
     *  arguments are two MoML files for InterfaceAutomaton.
     *  @param args The command line arguments.
     */
    public static void main (String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ptolemy.domains.fsm.kernel."
                    + "test.AlternatingSimulation <super_automaton.xml> "
                    + "<sub_automaton.xml>");
        } else {
            try {
                new AlternatingSimulation(args[0], args[1]);
            } catch (Exception exception) {
                System.out.println(exception.getClass().getName() + ": "
                        + exception.getMessage());
                exception.printStackTrace();
            }
        }
    }
}

