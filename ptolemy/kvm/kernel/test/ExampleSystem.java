/* Construct a test hierarchal graph using the ptolemy.kernel classes.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.kvm.kernel.test;
import ptolemy.kvm.kernel.*;
import ptolemy.kvm.kernel.util.*;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ExampleSystem
/**
ExampleSystem constructs a hierarchal graph as shown in
Ptolemy II design document, Figure 8.
The graph has 10 entities, 14 ports, and 12 relations.
The main function also returns the results of some key functions of
ComponentRelation and ComponentPort.
See Ptolemy 2 design document, Figure 11
@author Jie Liu
@version $Id$
*/
public class ExampleSystem implements Serializable {

    /** Construct the graph.
     * @exception NameDuplicationException if the example system cannot
     * be built because of a duplicate name
     * @exception IllegalActionException if the example system cannot
     * be built.
     */
    public ExampleSystem() 
	throws IllegalActionException, NameDuplicationException {

        super();

        // Create composite entities


        e0 = new CompositeEntity();
        e0.setName("E0");
        e3 = new CompositeEntity(e0, "E3");
        e4 = new CompositeEntity(e3, "E4");


        e7 = new CompositeEntity(e0, "E7");
        e10 = new CompositeEntity(e0, "E10");


        // Create component entities
        e1 = new ComponentEntity(e4, "E1");
        e2 = new ComponentEntity(e4, "E2");
        e5 = new ComponentEntity(e3, "E5");
        e6 = new ComponentEntity(e3, "E6");
        e8 = new ComponentEntity(e7, "E8");
        e9 = new ComponentEntity(e10, "E9");

        // Create ports
        p0 = (ComponentPort) e4.newPort("P0");
        p1 = (ComponentPort) e1.newPort("P1");
        p2 = (ComponentPort) e2.newPort("P2");
        p3 = (ComponentPort) e2.newPort("P3");
        p4 = (ComponentPort) e4.newPort("P4");
        p5 = (ComponentPort) e5.newPort("P5");
        p6 = (ComponentPort) e5.newPort("P6");
        p7 = (ComponentPort) e3.newPort("P7");
        p8 = (ComponentPort) e7.newPort("P8");
        p9 = (ComponentPort) e8.newPort("P9");
        p10 = (ComponentPort) e8.newPort("P10");
        p11 = (ComponentPort) e7.newPort("P11");
        p12 = (ComponentPort) e10.newPort("P12");
        p13 = (ComponentPort) e10.newPort("P13");
        p14 = (ComponentPort) e9.newPort("P14");

        // Create links
        r1 = e4.connect(p1, p0, "R1");
        r2 = e4.connect(p1, p4, "R2");
        p3.link(r2);
        r3 = e4.connect(p1, p2, "R3");
        r4 = e3.connect(p4, p7, "R4");
        r5 = e3.connect(p4, p5, "R5");
        e3.allowLevelCrossingConnect(true);

        r6 = e3.connect(p3, p6, "R6");
        r7 = e0.connect(p7, p13, "R7");
        r8 = e7.connect(p9, p8, "R8");
        r9 = e7.connect(p10, p11, "R9");
        r10 = e0.connect(p8, p12, "R10");
        r11 = e10.connect(p12, p13, "R11");
        r12 = e10.connect(p14, p13, "R12");
        p11.link(r7);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the results as a String. */
    public String toString() {
        return ("----Methods of ComponentRelation----\n" +
                "linkedPorts:\n" +
                printLinkedPorts(r1) +
                printLinkedPorts(r2) +
                printLinkedPorts(r3) +
                printLinkedPorts(r4) +
                printLinkedPorts(r5) +
                printLinkedPorts(r6) +
                printLinkedPorts(r7) +
                printLinkedPorts(r8) +
                printLinkedPorts(r9) +
                printLinkedPorts(r10) +
                printLinkedPorts(r11) +
                printLinkedPorts(r12) +
                "\ndeepLinkedPorts:\n" +
                printDeepLinkedPorts(r1) +
                printDeepLinkedPorts(r2) +
                printDeepLinkedPorts(r3) +
                printDeepLinkedPorts(r4) +
                printDeepLinkedPorts(r5) +
                printDeepLinkedPorts(r6) +
                printDeepLinkedPorts(r7) +
                printDeepLinkedPorts(r8) +
                printDeepLinkedPorts(r9) +
                printDeepLinkedPorts(r10) +
                printDeepLinkedPorts(r11) +
                printDeepLinkedPorts(r12) +
                "\n----Methods of ComponentPort----\n" +
                "connectedPorts:\n" +
                printConnectedPorts(p0) +
                printConnectedPorts(p1) +
                printConnectedPorts(p2) +
                printConnectedPorts(p3) +
                printConnectedPorts(p4) +
                printConnectedPorts(p5) +
                printConnectedPorts(p6) +
                printConnectedPorts(p7) +
                printConnectedPorts(p8) +
                printConnectedPorts(p9) +
                printConnectedPorts(p10) +
                printConnectedPorts(p11) +
                printConnectedPorts(p12) +
                printConnectedPorts(p13) +
                printConnectedPorts(p14) +
                "\ndeepConnectedPorts:\n" +
                printDeepConnectedPorts(p0) +
                printDeepConnectedPorts(p1) +
                printDeepConnectedPorts(p2) +
                printDeepConnectedPorts(p3) +
                printDeepConnectedPorts(p4) +
                printDeepConnectedPorts(p5) +
                printDeepConnectedPorts(p6) +
                printDeepConnectedPorts(p7) +
                printDeepConnectedPorts(p8) +
                printDeepConnectedPorts(p9) +
                printDeepConnectedPorts(p10) +
                printDeepConnectedPorts(p11) +
                printDeepConnectedPorts(p12) +
                printDeepConnectedPorts(p13) +
                printDeepConnectedPorts(p14));

    }

    /**
     * Print the linked ports for a given ComponentRelation. The ports
     * are restricted in the same level of hierarchy
     * @see ptolemy.kernel.Relation#linkedPorts()
     * @param ComponentRelation Print the linked ports for this relation.
     */
    public String printLinkedPorts(ComponentRelation r) {
        String st = r.getName() + ": ";
        ComponentPort po;
        Enumeration ports = r.linkedPorts();
        while (ports.hasMoreElements()) {
            po = (ComponentPort) ports.nextElement();
            st += po.getName() + " ";
        }
        return(st+"\n");
    }

    /**
     * Print the deeply linked ports for a given
     * ComponentRelation. Look through all transparent ports and return
     * only non transparent ports (those with no inside links).
     * @see ptolemy.kernel.ComponentRelation#deepLinkedPorts()
     * @param ComponentRelation Print the deeply linked ports for this
     * relation.
     */
    public String printDeepLinkedPorts(ComponentRelation r) {
        String st = r.getName() + ": ";
        ComponentPort po;
        Iterator ports = r.deepLinkedPortList().iterator();
        while (ports.hasNext()) {
            po = (ComponentPort) ports.next();
            st += po.getName() + " ";
        }
        return(st+"\n");
    }

    /**
     * Print the connected ports for a given ComponentPort.  Restricted
     * to the same level of hierarchy.
     * @see ptolemy.kernel.Port#connectedPorts()
     * @param ComponentPort Print the connected ports for this Port.
     */
    public String printConnectedPorts(ComponentPort p) {
        String st = p.getName() + ": ";
        ComponentPort po;
        Iterator ports = p.connectedPortList().iterator();
        while (ports.hasNext()) {
            po = (ComponentPort) ports.next();
            st += po.getName() + " ";
        }
        return(st+"\n");
    }

    /**
     * Print the deeply connected ports for a given ComponentPort. Look through
     * all transparent ports and return only non transparent ports (those
     * with no inside links).
     * @see ptolemy.kernel.ComponentPort#deepConnectedPorts()
     * @param ComponentPort Print the deeply connected ports for this Port.
     */
    public String printDeepConnectedPorts(ComponentPort p) {
        String st = p.getName() + ": ";
        ComponentPort po;
        Iterator ports = p.deepConnectedPortList().iterator();
        while (ports.hasNext()) {
            po = (ComponentPort) ports.next();
            st += po.getName() + " ";
        }
        return(st+"\n");
    }

    /** Create an Example System, then print it out.
     * @exception NameDuplicationException if the example system cannot
     * be built because of a duplicate name
     * @exception IllegalActionException if the example system cannot
     * be built.
     */
    public static void main(String args[])
            throws NameDuplicationException, IllegalActionException {
        ExampleSystem exsys = new ExampleSystem();
        //System.out.println(exsys.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Components of the system.

    /** @serial Composite Entities that make up the Example System. */
    public CompositeEntity e0, e3, e4, e7, e10;

    /** @serial Component Entities that make up the Example System. */
    public ComponentEntity e1, e2, e5, e6, e8, e9;

    /** @serial Component Ports that make up the Example System. */
    public ComponentPort p0, p1, p2, p3, p4, p5, p6, p7, p8, p9,
        p10, p11, p12, p13, p14;

    /** @serial Component Relations that make up the Example System. */
    public ComponentRelation r1, r2, r3, r4, r5, r6, r7, r8, r9,
        r10, r11, r12;
}
