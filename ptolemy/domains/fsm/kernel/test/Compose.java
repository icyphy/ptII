/* Compute the composition of two interface automata.

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

package ptolemy.domains.fsm.kernel.test;

import ptolemy.actor.gui.MoMLApplication;
import ptolemy.moml.MoMLParser;
import ptolemy.domains.fsm.kernel.InterfaceAutomaton;

import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// Compose
/**
Compute the composition of two interface automata.
This class reads the MoML description of two interface automata, computes
their composition, then writes the MoML description of the composition to
stdout. The usage is:
<pre>
java ptolemy.domains.fsm.kernel.test.Compose <automaton1.xml> <automaton2.xml>
</pre>

@author Yuhong Xiong
@version $Id$
*/

public class Compose {

    /** Compose the two argument interface automata and write the MoML
     *  description for the composition to stdout.
     *  @param moml1 The MoML file for the first InterfaceAutomaton.
     *  @param moml2 The MoML file for the first InterfaceAutomaton.
     *  @exception Exception If the automata cannot be composed.
     */
    public Compose(String moml1, String moml2) throws Exception {
	URL url1 = MoMLApplication.specToURL(moml1);
	URL url2 = MoMLApplication.specToURL(moml2);

        // following the comments in MoMLApplication, use the same URL for
	// the two arguments (base and URL) to parse(). Also, two instances
	// of MoMLParser must be used to parse the two files, otherwise
	// the same automaton will be return the second time parse() is
	// called.
        MoMLParser parser1 = new MoMLParser();
	InterfaceAutomaton automaton1 =
	                     (InterfaceAutomaton)parser1.parse(url1, url1);
        MoMLParser parser2 = new MoMLParser();
	InterfaceAutomaton automaton2 =
	                     (InterfaceAutomaton)parser2.parse(url2, url2);

        automaton1.addPorts();
        automaton2.addPorts();

	InterfaceAutomaton composition = automaton1.compose(automaton2);
	System.out.println(composition.exportMoML());
    }

    /** Pass the command line argument to the constructor.
     *  @param args The command line arguments.
     */
    public static void main (String[] args) {
        try {
            new Compose(args[0], args[1]);
	} catch (Exception exception) {
	    System.out.println(exception.getMessage());
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
