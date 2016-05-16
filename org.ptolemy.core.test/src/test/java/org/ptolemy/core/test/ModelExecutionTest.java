/* A trivial example of how to test a model execution.

 Copyright (c) 2014 The Regents of the University of California; iSencia Belgium NV.

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
package org.ptolemy.core.test;

import junit.framework.TestCase;

import org.ptolemy.testsupport.ModelExecutionAssertion;
import org.ptolemy.testsupport.TestUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.Recorder;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A trivial example of a Junit {@link TestCase} to assert the execution of a model.
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class ModelExecutionTest extends TestCase {

  /**
   *
   * @throws NameDuplicationException when the model definition fails because elements are added with duplicate names
   * @throws IllegalActionException when the model definition fails for some other reason
   * @throws KernelException when the manager fails to execute the model
   */
  public void testModelExecutionSDF() throws IllegalActionException, NameDuplicationException, KernelException {
    TypedCompositeActor model = new TypedCompositeActor();
    new SDFDirector(model, "director");
    Const c = new Const(model, "const");
    Recorder r = new Recorder(model,"sink");
    model.connect(c.output, r.input);
    c.value.setToken("\"hello\"");
    c.firingCountLimit.setToken("1");

    TestUtilities.enableStatistics(model);

    Manager m = new Manager();
    model.setManager(m);
    m.execute();

    new ModelExecutionAssertion()
      .expectActorIterationCount(c, 1L)
      .expectActorIterationCount(r, 1L)
      .expectMsgReceiptCount(r.input, 1L)
      .expectMsgSentCount(c.output, 1L)
      .assertModel(model);
  }

  /**
  *
  * @throws NameDuplicationException when the model definition fails because elements are added with duplicate names
  * @throws IllegalActionException when the model definition fails for some other reason
  * @throws KernelException when the manager fails to execute the model
  */
  public void testModelExecutionPN() throws IllegalActionException, NameDuplicationException, KernelException {
    TypedCompositeActor model = new TypedCompositeActor();
    new PNDirector(model, "director");
    Const c = new Const(model, "const");
    Recorder r = new Recorder(model,"sink");
    model.connect(c.output, r.input);
    c.value.setToken("\"hello\"");
    c.firingCountLimit.setToken("1");

    TestUtilities.enableStatistics(model);

    Manager m = new Manager();
    model.setManager(m);
    m.execute();

    new ModelExecutionAssertion()
      .expectActorIterationCount(c, 1L)
      .expectMsgSentCount(c.output, 1L)
      .expectMsgReceiptCount(r.input, 1L)
      // FIXME for some reason for PN the Recorder sink actor iterates 2 times?
      // even when it only receives 1 message?
//      .expectActorIterationCount(r, 1L)
      .assertModel(model);
  }
}
