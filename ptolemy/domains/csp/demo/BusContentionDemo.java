/* 

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package ptolemy.domains.csp.demo;

import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// BusContentionDemo
/** 


@author John S. Davis II
@version @(#)BusContentionDemo.java	1.1	11/12/98
*/
public class BusContentionDemo {

    /** 
     */
    public BusContentionDemo() {
        ;
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     * @exception IllegalActionException If there are problems
     * @exception IllegalStateException If there are problems
     * @exception NameDuplicationException If there are problems
     */
    public static void main( String args[] ) throws IllegalActionException,
            IllegalStateException, NameDuplicationException {
        
        // Set up Manager, Director and top level CompositeActor
        Workspace workSpc = new Workspace();
        CompositeActor topLevelActor = new CompositeActor(workSpc);
        topLevelActor.setName("universe");
        Manager manager = new Manager(workSpc, "manager");
        CSPDirector director = new CSPDirector(workSpc, "director");
        topLevelActor.setManager( manager );
        topLevelActor.setDirector( director );
        
        
        // Set up next level actors
        CSPController controller = 
                new CSPController( topLevelActor, "controller" );
        CSPContentionAlarm alarm = 
                new CSPContentionAlarm( topLevelActor, "alarm" );
        CSPMemory memory = new CSPMemory( topLevelActor, "memory" );
        CSPProcessor proc1 = new CSPProcessor( topLevelActor, "proc1" );
        CSPProcessor proc2 = new CSPProcessor( topLevelActor, "proc2" );
        CSPProcessor proc3 = new CSPProcessor( topLevelActor, "proc3" );
        // System.out.println("Actors have been instantiated.");
        
        
        // Set up ports, relation 
        IOPort reqOut = (IOPort)controller.getPort("requestOut");
        IOPort reqIn = (IOPort)controller.getPort("requestIn");
        IOPort contendOut = (IOPort)controller.getPort("contendOut");
        IOPort contendIn = (IOPort)controller.getPort("contendIn");
        
        IOPort alarmOut = (IOPort)alarm.getPort("output");
        IOPort alarmIn = (IOPort)alarm.getPort("input");
        
        IOPort memOut = (IOPort)memory.getPort("output");
        IOPort memIn = (IOPort)memory.getPort("input");
        
        IOPort p1_ReqOut = (IOPort)proc1.getPort("requestOut");
        IOPort p2_ReqOut = (IOPort)proc2.getPort("requestOut");
        IOPort p3_ReqOut = (IOPort)proc3.getPort("requestOut");
        IOPort p1_ReqIn = (IOPort)proc1.getPort("requestIn");
        IOPort p2_ReqIn = (IOPort)proc2.getPort("requestIn");
        IOPort p3_ReqIn = (IOPort)proc3.getPort("requestIn");
        
        IOPort p1_MemOut = (IOPort)proc1.getPort("memoryOut");
        IOPort p2_MemOut = (IOPort)proc2.getPort("memoryOut");
        IOPort p3_MemOut = (IOPort)proc3.getPort("memoryOut");
        IOPort p1_MemIn = (IOPort)proc1.getPort("memoryIn");
        IOPort p2_MemIn = (IOPort)proc2.getPort("memoryIn");
        IOPort p3_MemIn = (IOPort)proc3.getPort("memoryIn");
        
        IORelation inReqs, outReqs, reads, writes, outContends, inContends; 
        // System.out.println("Ports and relations are finished.");
        
        
        // Set up connections
        inReqs = (IORelation)topLevelActor.connect( reqIn, p1_ReqOut );
        inReqs = (IORelation)topLevelActor.connect( reqIn, p2_ReqOut );
        inReqs = (IORelation)topLevelActor.connect( reqIn, p3_ReqOut );
        
        outContends = (IORelation)topLevelActor.connect( contendOut, alarmIn );
        inContends = (IORelation)topLevelActor.connect( contendIn, alarmOut );
        
        System.out.println("Made It");
        
        outReqs = (IORelation)topLevelActor.connect( reqOut, p1_ReqIn );
        outReqs = (IORelation)topLevelActor.connect( reqOut, p2_ReqIn );
        outReqs = (IORelation)topLevelActor.connect( reqOut, p3_ReqIn );
        
        reads = (IORelation)topLevelActor.connect( memOut, p1_MemIn );
        reads = (IORelation)topLevelActor.connect( memOut, p2_MemIn );
        reads = (IORelation)topLevelActor.connect( memOut, p3_MemIn );
        
        writes = (IORelation)topLevelActor.connect( memIn, p1_MemOut );
        writes = (IORelation)topLevelActor.connect( memIn, p2_MemOut );
        writes = (IORelation)topLevelActor.connect( memIn, p3_MemOut );
        
        
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




















