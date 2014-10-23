/* Compute the projection of an interface automaton to another one.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel.test;

import java.net.URL;

import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.domains.modal.kernel.ia.InterfaceAutomaton;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// Project

/**
 Compute the projection of an interface automaton to another one.
 This class reads the MoML description of two automata, computes the projection
 of the first one to the second, then writes the MoML description of the
 projection to stdout. The usage is:
 <pre>
 java ptolemy.domains.modal.kernel.test.Project <first_automaton.xml> <second_automaton.xml>
 </pre>

 @author Yuhong Xiong
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class Project {
    /** Compute the projection of the first automaton to the second one and
     *  write the result to stdout.
     *  @param firstMoML The MoML file name for the first interface automaton.
     *  @param secondMoML The MoML file name for the second interface automaton.
     *  @exception Exception If the specified automata cannot be constructed
     *   or are not consistent.
     */
    public Project(String firstMoML, String secondMoML) throws Exception {
        // Construct the first automaton
        URL url = ConfigurationApplication.specToURL(firstMoML);

        // following the comments in MoMLApplication, use the same URL for
        // the two arguments (base and URL) to parse().
        MoMLParser parser = new MoMLParser();
        InterfaceAutomaton firstAutomaton = (InterfaceAutomaton) parser.parse(
                url, url);
        firstAutomaton.addPorts();

        // Construct the second automaton
        url = ConfigurationApplication.specToURL(secondMoML);

        // following the comments in MoMLApplication, use the same URL for
        // the two arguments (base and URL) to parse().  Also, a new instance
        // of MoMLParser must be used to parse each file, otherwise
        // the same automaton will be returned the second time parse() is
        // called.
        parser = new MoMLParser();

        InterfaceAutomaton secondAutomaton = (InterfaceAutomaton) parser.parse(
                url, url);
        secondAutomaton.addPorts();

        // Compute the projection and write result
        firstAutomaton.project(secondAutomaton);
        System.out.println(firstAutomaton.exportMoML());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Pass the command line arguments to the constructor. The command line
     *  arguments are two MoML files for interface automaton.
     *  @param args The command line arguments.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out
            .println("Usage: java ptolemy.domains.modal.kernel."
                    + "test.Project <first_automaton.xml> <second_automaton.xml>");
            System.out.println("This program computes the projection of the "
                    + "first automaton to the second one.");
            StringUtilities.exit(1);
        } else {
            try {
                new Project(args[0], args[1]);
            } catch (Exception exception) {
                System.out.println(exception.getClass().getName() + ": "
                        + exception.getMessage());
                exception.printStackTrace();
            }
        }
    }
}
