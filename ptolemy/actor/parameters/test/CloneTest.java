/* Clone an actor and compare the type constraints with the master.

 Copyright (c) 2015-2016 The Regents of the University of California.
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
package ptolemy.actor.parameters.test;

import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.ArrayContains;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;

/**
 Clone an actor and compare the type constraints with the master.

 <p>Invoke with:</p>
 <pre>
 java -classpath $PTII ptolemy.actor.parameters.test.CloneTest
 </pre>

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class CloneTest {
    public static void main(String[] args) throws Throwable {
        Workspace workspace = new Workspace();
        CompositeEntity compositeEntity = new CompositeEntity(workspace);

        // Use ArrayContains because it has few type constraints.
        TypedAtomicActor actor = new ArrayContains(compositeEntity, "myActor");
        TypedAtomicActor clone = (TypedAtomicActor)actor.clone(workspace);

        Set<Inequality> masterConstraints = actor.typeConstraints();
        System.out.println(masterConstraints.size() + " master type constraints:");
        Inequality firstElement = null;
        Inequality secondElement = null;
        Inequality lastElement = null;
        for (Inequality inequality : masterConstraints) {
            if (firstElement == null) {
                firstElement = inequality;
            } else if (secondElement == null) {
                secondElement = inequality;
            }
            lastElement = inequality;
            System.out.println(inequality);
        }
        System.out.println(secondElement.equals(lastElement));

        Set<Inequality> cloneConstraints = clone.typeConstraints();
        System.out.println("\n\n" + cloneConstraints.size() + " clone type constraints:");
        for (Inequality inequality : cloneConstraints) {
            System.out.println(inequality);
        }
        System.out.println("\n\n");

        if (masterConstraints.size() != cloneConstraints.size()) {
            throw new Exception("The number of type constraints in the master (" + masterConstraints.size()
                    + ") is not equal to the number of type constraints in the clone (" + cloneConstraints.size() + ")");
        }

        if (masterConstraints.equals(cloneConstraints)) {
            throw new Exception("The master type constraints and the clone type constraints are not equal.");
        }
    }
}
