/* 

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.od.demo;

import ptolemy.domains.od.kernel.*;
import ptolemy.domains.od.lib.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ODStringDemo
/** 


@author John S. Davis II
@version @(#)ODStringDemo.java	1.1	11/12/98
*/
public class ODStringDemo {

    /** 
     */
    public ODStringDemo() {
        ;
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public static void main( String args[] ) throws IllegalActionException,
            IllegalStateException, NameDuplicationException {
        
        // Set up Manager, Director and top level CompositeActor
        Workspace workSpc = new Workspace();
        CompositeActor topLevelActor = new CompositeActor(workSpc);
        topLevelActor.setName("universe");
        Manager manager = new Manager(workSpc, "manager");
        ODDirector director = new ODDirector(workSpc, "director");
        director.setCompletionTime(5500);
        topLevelActor.setManager( manager );
        topLevelActor.setDirector( director );
        
        // Set up next level actors
        ODPrintString printer = new ODPrintString( topLevelActor, "printer" );
        ODConsonants consonants = new ODConsonants( topLevelActor, "consonants" );
        ODVowels vowels = new ODVowels( topLevelActor, "vowels" );
        ODPunctuation punctuation = new ODPunctuation( topLevelActor, "punctuation" );
        // System.out.println("Actors have been instantiated.");
        
        // Set up ports, relation 
        IOPort output1 = (IOPort)consonants.getPort("output");
        IOPort output2 = (IOPort)vowels.getPort("output");
        IOPort output3 = (IOPort)punctuation.getPort("output");
        IOPort input = (IOPort)printer.getPort("input");
        IORelation relation; 
        // System.out.println("Ports and relations are finished.");
        
        // Set up connections
        relation = (IORelation)topLevelActor.connect( output1, input, "rel1" );
        relation = (IORelation)topLevelActor.connect( output2, input, "rel2" );
        relation = (IORelation)topLevelActor.connect( output3, input, "rel3" );
        // System.out.println("Connections are complete.");
        
        
        System.out.println();
        System.out.println();
        System.out.println();
        
        // int width = input.getWidth();
        // System.out.println("Width of input port is " + width);
        
        // Start simulation
        manager.run();
        
        System.out.println();
        System.out.println();
        System.out.println();
        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    
}




















