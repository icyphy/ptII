/* Display the deadlock states.

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
package ptolemy.domains.fsm.kernel.test;

import java.net.URL;
import java.util.Iterator;

import ptolemy.actor.gui.MoMLApplication;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.ia.InterfaceAutomaton;
import ptolemy.moml.MoMLParser;


//////////////////////////////////////////////////////////////////////////
//// DeadlockStates

/**
   Display the deadlock states.
   This class reads the MoML description of an interface automata,
   and writes the names of the deadlock states to stdout.
   The usage is:
   <pre>
   java ptolemy.domains.fsm.kernel.test.DeadlockStates <automaton.xml>
   </pre>

   @author Yuhong Xiong
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (yuhong)
   @Pt.AcceptedRating Red (reviewmoderator)
*/
public class DeadlockStates {
    /** Write the names of the deadlock states of the specified interface
     *  automaton to stdout.
     *  @param moml The MoML file name for an InterfaceAutomaton.
     *  @exception Exception If the MoML file is not valid or deadlock cannot
     *   be checked.
     */
    public DeadlockStates(String moml) throws Exception {
        URL url = MoMLApplication.specToURL(moml);

        // following the comments in MoMLApplication, use the same URL for
        // the two arguments (base and URL) to parse(). Also, a instance
        // of MoMLParser must be used to parse each file, otherwise
        // the same automaton will be returned the second time parse() is
        // called.
        MoMLParser parser = new MoMLParser();
        InterfaceAutomaton automaton = (InterfaceAutomaton) parser.parse(url,
                url);
        automaton.addPorts();

        System.out.println("Deadlock states:");

        Iterator deadlockStates = automaton.deadlockStates().iterator();

        while (deadlockStates.hasNext()) {
            State state = (State) deadlockStates.next();
            System.out.println("    " + state.getFullName());
        }
    }

    /** Pass the command line argument to the constructor. The command line
     *  argument the name of a MoML file for an InterfaceAutomaton.
     *  @param args The command line arguments.
     */
    public static void main(String[] args) {
        try {
            new DeadlockStates(args[0]);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
