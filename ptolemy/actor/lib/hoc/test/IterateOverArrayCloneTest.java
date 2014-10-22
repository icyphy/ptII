/* A test for IterateOverArray.clone(Workspace)

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc.test;

import java.lang.reflect.Field;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.hoc.IterateOverArray;
import ptolemy.kernel.util.Workspace;

/**
 * Test for clone(Workspace) of IterateOverArray.
 * To run:
 * java -classpath $PTII ptolemy.actor.lib.hoc.test.IterateOverArrayCloneTest
 */
public class IterateOverArrayCloneTest {
    /** Check the clone(Workspace) method of the IterateOverArray class.
     *  Instantiate an IterateOverArray actor and get the Workspace
     *  of the inner IterateDirector.  Then clone the actor into a
     *  new Workspace and get the Workspace of the inner IterateDirector
     *  of the *clone*.  The Workspaces should be different.
     *  <p>To run:</p>
     *  <code>
     *  java -classpath $PTII ptolemy.actor.lib.hoc.test.IterateOverArrayCloneTest
     *  </code>
     *
     *  @param args Ignored
     *  @exception Throwable If there is a problem with the test.
     */
    public static void main(String args[]) throws Throwable {
        // Create an IterateOverArray
        Workspace workspace = new Workspace("masterWorkspace");
        TypedCompositeActor container = new TypedCompositeActor(workspace);
        IterateOverArray iterateOverArray = new IterateOverArray(container,
                "iterateOverArray");

        // IterateOverArray has an inner class called IterateDirector.
        Class iterateDirectorClass = Class
                .forName("ptolemy.actor.lib.hoc.IterateOverArray$IterateDirector");
        List iterateDirectors = iterateOverArray
                .attributeList(iterateDirectorClass);
        Object iterateDirector = iterateDirectors.get(0);

        // Get the this$0 field, which refers to the outer class.
        Field thisZeroField = iterateDirectorClass.getDeclaredField("this$0");
        thisZeroField.setAccessible(true);

        // Get the outer object and then the Workspace
        IterateOverArray outerIterateOverArray = (IterateOverArray) thisZeroField
                .get(iterateDirectorClass.cast(iterateDirector));
        Workspace outerIterateOverArrayWorkspace = outerIterateOverArray
                .workspace();
        System.out.println("The workspace of the outer class is "
                + outerIterateOverArrayWorkspace.getName());

        ////////////
        // Call clone(Workspace) and get the outer object and the workspace.
        Workspace cloneWorkspace = new Workspace("cloneWorkspace");

        IterateOverArray clonedIterateOverArray = (IterateOverArray) iterateOverArray
                .clone(cloneWorkspace);
        List clonedIterateDirectors = clonedIterateOverArray
                .attributeList(iterateDirectorClass);
        Object clonedIterateDirector = clonedIterateDirectors.get(0);

        // Get the outer object and then the Workspace
        IterateOverArray clonedOuterIterateOverArray = (IterateOverArray) thisZeroField
                .get(iterateDirectorClass.cast(clonedIterateDirector));
        Workspace clonedOuterIterateOverArrayWorkspace = clonedOuterIterateOverArray
                .workspace();
        System.out.println("The workspace of the outer class of the clone is "
                + clonedOuterIterateOverArrayWorkspace.getName());

        if (outerIterateOverArray.equals(clonedOuterIterateOverArray)) {
            System.err
                    .println("Error! the outer IterateOverArray objects are equal?");
        } else {
            System.err
                    .println("Passed! the outer IterateOverArray objects are not equal!");
        }

        if (outerIterateOverArrayWorkspace
                .equals(clonedOuterIterateOverArrayWorkspace)) {
            System.err.println("Error! the workspaces are equal?");
            System.exit(1);
        } else {
            System.err.println("Passed! the workspaces are not equal!");
        }

    }
}
