/* BusContentionDemo

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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

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
    public BusContentionDemo() throws IllegalActionException, NameDuplicationException {

        // Set up Manager, Director and top level CompositeActor
        _workSpc = new Workspace();
        _topLevelActor = new CompositeActor(_workSpc);
        _topLevelActor.setName("universe");
        _manager = new Manager(_workSpc, "manager");
        _director = new CSPDirector(_workSpc, "director");
        _topLevelActor.setManager( _manager );
        _topLevelActor.setDirector( _director );

        // Set up next level actors
        _controller = new CSPController( _topLevelActor, "controller" );
        _alarm = new CSPContentionAlarm( _topLevelActor, "alarm" );
        _memory = new CSPMemory( _topLevelActor, "memory" );
        _proc1 = new CSPProcessor( _topLevelActor, "proc1", 1 );
        _proc2 = new CSPProcessor( _topLevelActor, "proc2", 2 );
        _proc3 = new CSPProcessor( _topLevelActor, "proc3", 3 );
    }

    /**
     */
    public BusContentionDemo(BusContentionGraphic bcg)
            throws IllegalActionException, NameDuplicationException {
        this();
        _proc1.setGraphicFrame( bcg );
        _proc2.setGraphicFrame( bcg );
        _proc3.setGraphicFrame( bcg );
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

        BusContentionDemo demo = new BusContentionDemo();
        demo.makeConnections();
        demo.run();
    }

    /**
     * @exception IllegalActionException If there are problems
     * @exception IllegalStateException If there are problems
     * @exception NameDuplicationException If there are problems
     */
    public void makeConnections() throws IllegalActionException {
        // Set up ports, relation
        IOPort reqOut = (IOPort)_controller.getPort("requestOut");
        IOPort reqIn = (IOPort)_controller.getPort("requestIn");
        IOPort contendOut = (IOPort)_controller.getPort("contendOut");
        IOPort contendIn = (IOPort)_controller.getPort("contendIn");

        IOPort _alarmOut = (IOPort)_alarm.getPort("output");
        IOPort _alarmIn = (IOPort)_alarm.getPort("input");

        IOPort memOut = (IOPort)_memory.getPort("output");
        IOPort memIn = (IOPort)_memory.getPort("input");

        IOPort p1_ReqOut = (IOPort)_proc1.getPort("requestOut");
        IOPort p2_ReqOut = (IOPort)_proc2.getPort("requestOut");
        IOPort p3_ReqOut = (IOPort)_proc3.getPort("requestOut");
        IOPort p1_ReqIn = (IOPort)_proc1.getPort("requestIn");
        IOPort p2_ReqIn = (IOPort)_proc2.getPort("requestIn");
        IOPort p3_ReqIn = (IOPort)_proc3.getPort("requestIn");

        IOPort p1_MemOut = (IOPort)_proc1.getPort("memoryOut");
        IOPort p2_MemOut = (IOPort)_proc2.getPort("memoryOut");
        IOPort p3_MemOut = (IOPort)_proc3.getPort("memoryOut");
        IOPort p1_MemIn = (IOPort)_proc1.getPort("memoryIn");
        IOPort p2_MemIn = (IOPort)_proc2.getPort("memoryIn");
        IOPort p3_MemIn = (IOPort)_proc3.getPort("memoryIn");

        IORelation inReqs, outReqs, reads, writes, outContends, inContends;
        // System.out.println("Ports and relations are finished.");


        // Set up connections
        inReqs = (IORelation)_topLevelActor.connect( reqIn, p1_ReqOut );
        inReqs = (IORelation)_topLevelActor.connect( reqIn, p2_ReqOut );
        inReqs = (IORelation)_topLevelActor.connect( reqIn, p3_ReqOut );

        outContends = (IORelation)_topLevelActor.connect( contendOut, _alarmIn );
        inContends = (IORelation)_topLevelActor.connect( contendIn, _alarmOut );

        outReqs = (IORelation)_topLevelActor.connect( reqOut, p1_ReqIn );
        outReqs = (IORelation)_topLevelActor.connect( reqOut, p2_ReqIn );
        outReqs = (IORelation)_topLevelActor.connect( reqOut, p3_ReqIn );

        reads = (IORelation)_topLevelActor.connect( memOut, p1_MemIn );
        reads = (IORelation)_topLevelActor.connect( memOut, p2_MemIn );
        reads = (IORelation)_topLevelActor.connect( memOut, p3_MemIn );

        writes = (IORelation)_topLevelActor.connect( memIn, p1_MemOut );
        writes = (IORelation)_topLevelActor.connect( memIn, p2_MemOut );
        writes = (IORelation)_topLevelActor.connect( memIn, p3_MemOut );


        // System.out.println("Connections are complete.");


        System.out.println();
        System.out.println();
        System.out.println();

        // int width = input.getWidth();
        // System.out.println("Width of input port is " + width);
    }

    /**
     * @exception IllegalActionException If there are problems
     * @exception IllegalStateException If there are problems
     * @exception NameDuplicationException If there are problems
     */
    public void run() {

        // Start simulation
        _manager.run();

        System.out.println();
        System.out.println();
        System.out.println();

    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private Workspace _workSpc;
    private CompositeActor _topLevelActor;
    private Manager _manager;
    private CSPDirector _director;
    private CSPController _controller;
    private CSPContentionAlarm _alarm;
    private CSPMemory _memory;
    private CSPProcessor _proc1;
    private CSPProcessor _proc2;
    private CSPProcessor _proc3;

}




















