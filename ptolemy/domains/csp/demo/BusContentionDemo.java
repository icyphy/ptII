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
@version $Id$
*/
public class BusContentionDemo {

    /**
     */
    public BusContentionDemo()
            throws IllegalActionException, NameDuplicationException {

        // Set up Manager, Director and top level TypedCompositeActor
        _workSpc = new Workspace();
        _topLevelActor = new TypedCompositeActor(_workSpc);
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
        TypedIOPort reqOut = (TypedIOPort)_controller.getPort("requestOut");
        TypedIOPort reqIn = (TypedIOPort)_controller.getPort("requestIn");
        TypedIOPort contendOut = (TypedIOPort)_controller.getPort("contendOut");
        TypedIOPort contendIn = (TypedIOPort)_controller.getPort("contendIn");

        TypedIOPort _alarmOut = (TypedIOPort)_alarm.getPort("output");
        TypedIOPort _alarmIn = (TypedIOPort)_alarm.getPort("input");

        TypedIOPort memOut = (TypedIOPort)_memory.getPort("output");
        TypedIOPort memIn = (TypedIOPort)_memory.getPort("input");

        TypedIOPort p1_ReqOut = (TypedIOPort)_proc1.getPort("requestOut");
        TypedIOPort p2_ReqOut = (TypedIOPort)_proc2.getPort("requestOut");
        TypedIOPort p3_ReqOut = (TypedIOPort)_proc3.getPort("requestOut");
        TypedIOPort p1_ReqIn = (TypedIOPort)_proc1.getPort("requestIn");
        TypedIOPort p2_ReqIn = (TypedIOPort)_proc2.getPort("requestIn");
        TypedIOPort p3_ReqIn = (TypedIOPort)_proc3.getPort("requestIn");

        TypedIOPort p1_MemOut = (TypedIOPort)_proc1.getPort("memoryOut");
        TypedIOPort p2_MemOut = (TypedIOPort)_proc2.getPort("memoryOut");
        TypedIOPort p3_MemOut = (TypedIOPort)_proc3.getPort("memoryOut");
        TypedIOPort p1_MemIn = (TypedIOPort)_proc1.getPort("memoryIn");
        TypedIOPort p2_MemIn = (TypedIOPort)_proc2.getPort("memoryIn");
        TypedIOPort p3_MemIn = (TypedIOPort)_proc3.getPort("memoryIn");

        TypedIORelation inReqs, outReqs, reads, writes, outContends, inContends;
        // System.out.println("Ports and relations are finished.");


        // Set up connections
        inReqs = (TypedIORelation)_topLevelActor.connect(reqIn, p1_ReqOut );
        inReqs = (TypedIORelation)_topLevelActor.connect(reqIn, p2_ReqOut );
        inReqs = (TypedIORelation)_topLevelActor.connect(reqIn, p3_ReqOut );

        outContends = (TypedIORelation)_topLevelActor.connect(contendOut,
                _alarmIn );
        inContends = (TypedIORelation)_topLevelActor.connect(contendIn,
                _alarmOut );

        outReqs = (TypedIORelation)_topLevelActor.connect( reqOut, p1_ReqIn );
        outReqs = (TypedIORelation)_topLevelActor.connect( reqOut, p2_ReqIn );
        outReqs = (TypedIORelation)_topLevelActor.connect( reqOut, p3_ReqIn );

        reads = (TypedIORelation)_topLevelActor.connect( memOut, p1_MemIn );
        reads = (TypedIORelation)_topLevelActor.connect( memOut, p2_MemIn );
        reads = (TypedIORelation)_topLevelActor.connect( memOut, p3_MemIn );

        writes = (TypedIORelation)_topLevelActor.connect( memIn, p1_MemOut );
        writes = (TypedIORelation)_topLevelActor.connect( memIn, p2_MemOut );
        writes = (TypedIORelation)_topLevelActor.connect( memIn, p3_MemOut );


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
    private TypedCompositeActor _topLevelActor;
    private Manager _manager;
    private CSPDirector _director;
    private CSPController _controller;
    private CSPContentionAlarm _alarm;
    private CSPMemory _memory;
    private CSPProcessor _proc1;
    private CSPProcessor _proc2;
    private CSPProcessor _proc3;

}




















