/* Sieve of Eratosthenes demo.

 Copyright (c) 1998 The Regents of the University of California.
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


import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Implementation of Sieve of Eratosthenes demo.
/**
FIXME: add description.
<p>
@author Neil Smyth
@version 
*/
public class Eratosthenes {
    /** Create an instance for excuoing the M/M/1 demo.
     */
    public Eratosthenes() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The application code.
     */
    public static void main(String[] args) {
        try {
            CompositeActor univ = new CompositeActor();
            univ.setName( "Sieve of Eratosthenes demo");
            Manager manager = new Manager("Manager");
            CSPDirector localdir = new CSPDirector("Local Director");
            univ.setManager(manager);
            univ.setDirector(localdir);

            Parameter custRate = new Parameter(univ, "thinkingRate");
            custRate.setExpression("1.0");
            custRate.evaluate();

            // Set up the actors and connections
            CSPPhilosopher p1 = new CSPPhilosopher(univ, "Aristotle");
            CSPPhilosopher p2 = new CSPPhilosopher(univ, "Plato");
            CSPPhilosopher p3 = new CSPPhilosopher(univ, "Sartre");
            CSPPhilosopher p4 = new CSPPhilosopher(univ, "DesCatres");
            CSPPhilosopher p5 = new CSPPhilosopher(univ, "Socrates");

            CSPFork f1 = new CSPFork(univ, "Fork1");
            CSPFork f2 = new CSPFork(univ, "Fork2");
            CSPFork f3 = new CSPFork(univ, "Fork3");
            CSPFork f4 = new CSPFork(univ, "Fork4");
            CSPFork f5 = new CSPFork(univ, "Fork5");
            
            // Now connect up the Actors
            IORelation r1 = (IORelation)univ.connect(p1.leftIn, f5.rightOut);
            IORelation r2 = (IORelation)univ.connect(p1.leftOut, f5.rightIn);
            IORelation r3 = (IORelation)univ.connect(p1.rightIn, f1.leftOut);
            IORelation r4 = (IORelation)univ.connect(p1.rightOut, f1.leftIn);
            
            IORelation r5 = (IORelation)univ.connect(p2.leftIn, f1.rightOut);
            IORelation r6 = (IORelation)univ.connect(p2.leftOut, f1.rightIn);
            IORelation r7 = (IORelation)univ.connect(p2.rightIn, f2.leftOut);
            IORelation r8 = (IORelation)univ.connect(p2.rightOut, f2.leftIn);
            
            IORelation r9  = (IORelation)univ.connect(p3.leftIn, f2.rightOut);
            IORelation r10 = (IORelation)univ.connect(p3.leftOut, f2.rightIn);
            IORelation r11 = (IORelation)univ.connect(p3.rightIn, f3.leftOut);
            IORelation r12 = (IORelation)univ.connect(p3.rightOut, f3.leftIn);
            
            IORelation r13 = (IORelation)univ.connect(p4.leftIn, f3.rightOut);
            IORelation r14 = (IORelation)univ.connect(p4.leftOut, f3.rightIn);
            IORelation r15 = (IORelation)univ.connect(p4.rightIn, f4.leftOut);
            IORelation r16 = (IORelation)univ.connect(p4.rightOut, f4.leftIn);
            
            IORelation r17 = (IORelation)univ.connect(p5.leftIn, f4.rightOut);
            IORelation r18 = (IORelation)univ.connect(p5.leftOut, f4.rightIn);
            IORelation r19 = (IORelation)univ.connect(p5.rightIn, f5.leftOut);
            IORelation r20 = (IORelation)univ.connect(p5.rightOut, f5.leftIn);
            
            //System.out.println(univ.description(1023));
            System.out.println(univ.getFullName() + " starting!");
            univ.getManager().startRun();
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": " + e.getClass().getName());
            throw new InvalidStateException(e.getMessage());
        }
    }
}
