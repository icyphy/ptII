/* Dining Philosophers problem

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.DiningPhilosophers;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Implementation of the Dining Philosophers problem.
/**
Five philosophers are seated at a table with a large bowl of food in
the middle. Between each pair of philosophers is one chopstick, and to
eat a philosopher must use both chopsticks beside him. Each philosopher
spends his life in the following cycle: He thinks for a while, gets hungry,
 picks up one of the chopsticks beside him, then the other, eats for a
while and puts the chopsticks down on the table again. If a philosopher
tries to grab a chopstick  but it is already being used by another
philosopher, then the philosopher waits until that chopstick becomes
available. This implies that no neighbouring philosophers can eat at the
same time and at most two philosophers can eat at a time.
<p>
The Dining Philosophers problem was first dreamt up by Edsger W. Dijkstra
in 1965. It is a classic concurrent programming problem that illustrates
the two basic properties of concurrent programming:
<LI>
<B>Liveness</B>. How can we design the program to avoid deadlock, where
none of the the philosophers can make progress because each is waiting
for someone else to do something?
<LI>
<B>Fairness</B>. How can we design the program to avoid starvation, where
one of the philosoph ers could make progress but does not because others
always go first?
<p>
This demo uses an algorithm that lets each philosopher randomly chose
which chopstick to pick up first, and all philosophers eat and think at the
same rates. This algorithm is fair as any time a chopstick is not being used
and both philosophers try to use it, they both have an equal chance of
succeeding. However this algorithm does not guarantee the absence of
deadlock, and if it is let run long enough this will eventually occur.
The probability that deadlock occurs sooner increases as he thinking
times are decreased relative to the eating times.
<p>
@author Neil Smyth
@version $Id$
*/
public class DiningPhilosophers {

    /** Create an instance of the Dining Philosophers demo.
     */
    public DiningPhilosophers() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The application code.
     */
    public static void main(String[] args) {
        try {
            TypedCompositeActor univ = new TypedCompositeActor();
            univ.setName( "DiningPhilosophers demo");
            Manager manager = new Manager("Manager");
            CSPDirector localdir = new CSPDirector(univ, "Local Director");
            univ.setManager(manager);

            Parameter thinkingRate = new Parameter(univ, "thinkingRate");
            thinkingRate.setExpression("1.0");
            thinkingRate.getToken();

            Parameter eatingRate = new Parameter(univ, "eatingRate");
            eatingRate.setExpression("1.0");
            eatingRate.getToken();

            // Set up the actors and connections
            Philosopher p1 = new Philosopher(univ, "Aristotle");
            Philosopher p2 = new Philosopher(univ, "Plato");
            Philosopher p4 = new Philosopher(univ, "Descartes");
            Philosopher p3 = new Philosopher(univ, "Sartre");

            Philosopher p5 = new Philosopher(univ, "Socrates");

            Chopstick f1 = new Chopstick(univ, "Chopstick1");
            Chopstick f2 = new Chopstick(univ, "Chopstick2");
            Chopstick f3 = new Chopstick(univ, "Chopstick3");
            Chopstick f4 = new Chopstick(univ, "Chopstick4");
            Chopstick f5 = new Chopstick(univ, "Chopstick5");

            // Now connect up the Actors
            TypedIORelation r1 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p1.getPort("leftIn"),
                    (TypedIOPort)f5.getPort("rightOut"));
            TypedIORelation r2 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p1.getPort("leftOut"),
                    (TypedIOPort)f5.getPort("rightIn"));
            TypedIORelation r3 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p1.getPort("rightIn"),
                    (TypedIOPort)f1.getPort("leftOut"));
            TypedIORelation r4 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p1.getPort("rightOut"),
                    (TypedIOPort)f1.getPort("leftIn"));
            TypedIORelation r5 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p2.getPort("leftIn"),
                    (TypedIOPort)f1.getPort("rightOut"));
            TypedIORelation r6 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p2.getPort("leftOut"),
                    (TypedIOPort)f1.getPort("rightIn"));
            TypedIORelation r7 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p2.getPort("rightIn"),
                    (TypedIOPort)f2.getPort("leftOut"));
            TypedIORelation r8 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p2.getPort("rightOut"),
                    (TypedIOPort)f2.getPort("leftIn"));
            TypedIORelation r9  = (TypedIORelation)univ.connect(
                    (TypedIOPort)p3.getPort("leftIn"),
                    (TypedIOPort)f2.getPort("rightOut"));
            TypedIORelation r10 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p3.getPort("leftOut"),
                    (TypedIOPort)f2.getPort("rightIn"));
            TypedIORelation r11 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p3.getPort("rightIn"),
                    (TypedIOPort)f3.getPort("leftOut"));
            TypedIORelation r12 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p3.getPort("rightOut"),
                    (TypedIOPort)f3.getPort("leftIn"));
            TypedIORelation r13 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p4.getPort("leftIn"),
                    (TypedIOPort)f3.getPort("rightOut"));
            TypedIORelation r14 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p4.getPort("leftOut"),
                    (TypedIOPort)f3.getPort("rightIn"));
            TypedIORelation r15 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p4.getPort("rightIn"),
                    (TypedIOPort)f4.getPort("leftOut"));
            TypedIORelation r16 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p4.getPort("rightOut"),
                    (TypedIOPort)f4.getPort("leftIn"));
            TypedIORelation r17 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p5.getPort("leftIn"),
                    (TypedIOPort)f4.getPort("rightOut"));
            TypedIORelation r18 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p5.getPort("leftOut"),
                    (TypedIOPort)f4.getPort("rightIn"));
            TypedIORelation r19 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p5.getPort("rightIn"),
                    (TypedIOPort)f5.getPort("leftOut"));
            TypedIORelation r20 = (TypedIORelation)univ.connect(
                    (TypedIOPort)p5.getPort("rightOut"),
                    (TypedIOPort)f5.getPort("leftIn"));

            //System.out.println(univ.description(1023));
            System.out.println(univ.getFullName() + " starting!");
            univ.getManager().startRun();
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": " + e.getClass().getName());
            throw new InvalidStateException(e.getMessage());
        }
    }
}
