/* A simple demo that illustrates the basic operation of
 DDE feedforward topologies.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.dde.demo.HelloWorld;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Clock;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.dde.kernel.DDEDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HelloWorld

/**
 A simple demo that illustrates the basic operation of
 DDE feedforward topologies.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)
 */
public class HelloWorld {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public static void main(String[] args) throws IllegalActionException,
    IllegalStateException, NameDuplicationException {
        // Set up Manager, Director and top level CompositeActor
        Workspace workSpc = new Workspace();
        TypedCompositeActor topLevelActor = new TypedCompositeActor(workSpc);
        topLevelActor.setName("universe");

        Manager manager = new Manager(workSpc, "manager");
        DDEDirector director = new DDEDirector(topLevelActor, "director");
        Parameter stopTime = (Parameter) director.getAttribute("stopTime");
        stopTime.setToken(new DoubleToken(57.0));
        topLevelActor.setManager(manager);

        // Set up next level actors
        Clock vowelSrc = new Clock(topLevelActor, "vowelSrc");
        vowelSrc.values.setExpression("{1,1,1,1,1,1,1,1,1,1,1,1,1}");
        vowelSrc.period.setToken(new DoubleToken(100.0));
        vowelSrc.offsets
        .setExpression("{0.5,2.0,5.0,7.0,8.0,11.0,12.5,13.5,14.0,15.5,17.5,19.5,20.5}");
        vowelSrc.stopTime.setToken(new DoubleToken(30.0));

        Clock consonantsSrc = new Clock(topLevelActor, "consonantsSrc");
        consonantsSrc.values
        .setExpression("{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}");
        consonantsSrc.period.setToken(new DoubleToken(100.0));
        consonantsSrc.offsets
        .setExpression("{0.0, 1.0, 1.5, 4.0, 4.5, 6.0, 6.5, 7.5, 8.5, 9.0, 10.0, 10.5, 12.0, 13.0, 14.5, 16.0, 17.0, 18.0, 18.5, 19.0, 20.0}");
        consonantsSrc.stopTime.setToken(new DoubleToken(30.0));

        Clock punctuationSrc = new Clock(topLevelActor, "punctuationSrc");
        punctuationSrc.values.setExpression("{1, 1, 1, 1, 1, 1, 1, 1, 1}");
        punctuationSrc.period.setToken(new DoubleToken(100.0));
        punctuationSrc.offsets
        .setExpression("{2.5, 3.0, 3.5, 5.5, 9.5, 11.5, 15.0, 16.5, 21.0}");
        punctuationSrc.stopTime.setToken(new DoubleToken(30.0));

        PrintString printer = new PrintString(topLevelActor, "printer");
        Consonants consonants = new Consonants(topLevelActor, "consonants");
        Vowels vowels = new Vowels(topLevelActor, "vowels");
        Punctuation punctuation = new Punctuation(topLevelActor, "punctuation");

        // System.out.println("Actors have been instantiated.");
        // Set up ports, relation
        TypedIOPort vSrcOut = (TypedIOPort) vowelSrc.getPort("output");
        TypedIOPort cSrcOut = (TypedIOPort) consonantsSrc.getPort("output");
        TypedIOPort pSrcOut = (TypedIOPort) punctuationSrc.getPort("output");

        TypedIOPort cOutput = (TypedIOPort) consonants.getPort("output");
        TypedIOPort vOutput = (TypedIOPort) vowels.getPort("output");
        TypedIOPort pOutput = (TypedIOPort) punctuation.getPort("output");

        TypedIOPort input = (TypedIOPort) printer.getPort("input");

        TypedIOPort cInput = (TypedIOPort) consonants.getPort("input");
        TypedIOPort vInput = (TypedIOPort) vowels.getPort("input");
        TypedIOPort pInput = (TypedIOPort) punctuation.getPort("input");

        // System.out.println("Ports and relations are finished.");
        // Set up connections
        topLevelActor.connect(vSrcOut, vInput);
        topLevelActor.connect(vOutput, input);

        topLevelActor.connect(cSrcOut, cInput);
        topLevelActor.connect(cOutput, input);

        topLevelActor.connect(pSrcOut, pInput);
        topLevelActor.connect(pOutput, input);

        // System.out.println("Connections are complete.");
        // Uncomment the next line to print out the MoML version of the model.
        // System.out.println(topLevelActor.exportMoML());
        System.out.println();
        System.out.println();
        System.out.println();

        manager.run();

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }
}
