/* Test for SharedParameter

 Copyright (c) 2013 The Regents of the University of California.
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

package ptolemy.actor.parameters.test.junit;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Initializable;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SharedParameterJUnitTest
/**
 * Tests for SharedParameter.
 * <pre>
 * (cd $PTII/ptolemy/actor/parameters/test/junit; java -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.actor.parameters.test.junit.SharedParameterJUnitTest)
 * </pre>
 * @author Christopher Brooks
 * @version $Id: JUnitTclTest.java 64753 2012-10-02 02:05:53Z cxh $
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class SharedParameterJUnitTest {
    /** Instantiate a CompositeActor with a SharedParameter and then
     *  clone it.
     *  @exception Exception If there is a problem cloning the
     *  CompositeActor or accessing the _initializable field.
     */   
    @org.junit.Test
    public void run() throws Exception {

        // - When a model contains an Initializable entity
        // (e.g. Director or Actor) that contains Initializable
        // Attributes/Parameters, each such entity typically
        // maintains a collection ..._initializables.

        Workspace workspace = new Workspace("myWorkspace");
        CompositeActor compositeActor = new CompositeActor(workspace);
        compositeActor.setName("compositeActor");
        
        // - At construction time (e.g. during parsing), such attributes
        // register themselves as Initializable with their containing
        // entity, and are then stored in that collection.

        // - The only way to be removed from there, is when a call
        // ....setContainer() is done with null or another container.

        SharedParameter sharedParameter = new SharedParameter(compositeActor, "sharedParameter", null, "4.5" );

        Collection<Initializable> initializables = _getInitializableField(compositeActor);

        // - During a clone(), the entities are not deep-cloned. The
        // result is that a cloned Director/Actor (i.e. during the
        // execution of its base NamedObj.clone(Workspace)), ends up
        // with a reference to the same _initializables collection as
        // the original instance.

        // NOTE: Shouldn't this be clone(workspace)?
        Workspace clonedWorkspace = new Workspace("clonedWorkspace");

        CompositeActor clonedCompositeActor = (CompositeActor) compositeActor.clone(clonedWorkspace);
        
        Collection<Initializable> clonedInitializables = _getInitializableField(clonedCompositeActor);

        assertTrue(initializables.size() == clonedInitializables.size());

        for (Initializable initializable : initializables) {
            NamedObj initializableContainer = ((NamedObj) initializable).getContainer();
            for (Initializable clonedInitializable : clonedInitializables) {
                NamedObj clonedInitializableContainer = ((NamedObj) clonedInitializable).getContainer();

                // First, check for equals.
                if (initializable.equals(clonedInitializable)) {
                    System.out.println("Error!.  An initializable: " + ((NamedObj) initializable).getFullName ()
                            + "(contained in " + initializableContainer.getFullName()
                            + ", with workspace: " + initializableContainer.workspace().getName()
                            + ") is equal to an initializable in the clone: "
                            + ((NamedObj) clonedInitializable).getFullName());
                }
                assertTrue(!initializable.equals(clonedInitializable));
                
                // Then, check for the same container.
                if (initializableContainer.equals(clonedInitializableContainer)) {
                    System.out.println("Error!.  The container of " + ((NamedObj) initializable).getFullName ()
                            + " is " + initializableContainer.getFullName()
                            + ", which is the same as the container of " 
                            + ((NamedObj) clonedInitializable).getFullName());
                }
                assertTrue(!initializableContainer.equals(clonedInitializableContainer));
            }
        }
        
        // Further in the NamedObj.clone(Workspace), all attributes
        // are cloned and are then set to their new container,
        // i.e. end up in the _initializables collection which is
        // still pointing to the original one.

        // The result is that the original model's entities are gathering
        // references to all the cloned Initializable parameters (e.g. a
        // SharedParameter), and so also to the containers.  => the cloned
        // models can never be garbage-collected after their execution...

        // In Ptolemy v7, the cloned entities even hang-on to this
        // "shared" initializables collection.

        // - On the trunk I noticed that the initializables collection
        // is set to null during the clone. But this is too late,
        // i.e. after the NameObj.clone() returns...

        // I think that the current organisation of clone() and
        // clone(Workspace) in NamedObj doesn't allow to intercept at
        // the right moment for this situation?

        // Probably similar issues could occur with other cases of
        // state maintained by entities, outside of their parameters.
    }

    /** Given a CompositeActor, return the value of the protected _initializable field.
     *  @param compositeActor The composite actor.
     *  @return The value of the protected _initializable field.
     *  @exception Exception If the field cannot be accessed.
     */   
    private Collection<Initializable> _getInitializableField(CompositeActor compositeActor) throws Exception {
        Field[] fields = compositeActor.getClass().getDeclaredFields();
        for (int i = 0 ; i < fields.length; i++) {
            fields[i].setAccessible(true);
            if (fields[i].getName().equals("_initializables")) {
                Collection<Initializable> initializables = (Collection<Initializable>) fields[i].get(compositeActor);
                System.out.println("fields: " + fields[i].getName() + ": " + initializables + " workspace: " + compositeActor.workspace());
                return initializables;
            }
        }
        return null;
    }
}
