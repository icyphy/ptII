/* Utilities for more advanced model execution unit tests.

Copyright (c) 2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package org.ptolemy.testsupport;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.ptolemy.commons.FutureValue;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;

/**
 * Utilities for more advanced unit tests, e.g. involving high counts of repeated model executions
 * sequentially or concurrently.
 * 
 * @author erwindl
 *
 */
public class ModelExecutionTester {
  
  /**
   * Execute a model a number of times, one-after-the-other, and assert the results each time using the given assertion.
   * <p>
   * For each execution, a new instance of the model is constructed, using the given builder.
   * The model gets as name the given modelName + the run's index as postfix.
   * The actors in the model will be configured using the given paramOverrides map.
   * </p>
   * @param count
   * @param modelName
   * @param builder
   * @param paramOverrides a map between full names of selected model parameters, and their desired values 
   * @param assertion
   * @throws Exception
   */
  public static void runFlowSequentially(int count, String modelName, ModelBuilder builder, Map<String, String> paramOverrides, ModelExecutionAssertion assertion) throws Exception {
    FlowManager flowManager = FlowManager.getDefault();
    for (int i = 0; i < 100; ++i) {
      final CompositeActor model = builder.buildModel(modelName + "_" + i);
      final FutureValue<Boolean> modelFinished = new FutureValue<Boolean>();
      flowManager.execute(model, paramOverrides, new ExecutionListener() {
        public void managerStateChanged(Manager manager) {
        }
        public void executionFinished(Manager manager) {
          modelFinished.set(Boolean.TRUE);
        }
        public void executionError(Manager manager, Throwable throwable) {
          modelFinished.set(Boolean.FALSE);
        }
      });
      try {
        modelFinished.get(5, TimeUnit.SECONDS);
        assertion.assertModel(model);
      } catch (TimeoutException e) {
        Assert.fail("Model execution timed out, probable deadlock in "+model.getName());
      }
    }
  }

  /**
   * Execute a model a number of times, concurrently, and assert the results each time using the given assertion.
   * <p>
   * For each execution, a new instance of the model is constructed, using the given builder.
   * The model gets as name the given modelName + the run's index as postfix.
   * The actors in the model will be configured using the given paramOverrides map.
   * </p>
   * @param count
   * @param modelName
   * @param builder
   * @param paramOverrides a map between full names of selected model parameters, and their desired values 
   * @param assertion
   * @throws Exception
   */
  public static void runFlowConcurrently(int count, String modelName, ModelBuilder builder, Map<String, String> paramOverrides, ModelExecutionAssertion assertion) throws Exception {
    FlowManager flowManager = FlowManager.getDefault();
    Set<FutureValue<CompositeActor>> modelFinishedFutures = new HashSet<FutureValue<CompositeActor>>();
    for (int i = 0; i < 100; ++i) {
      final CompositeActor model = builder.buildModel(modelName+"_"+i);
      final FutureValue<CompositeActor> modelFinished = new FutureValue<CompositeActor>();
      modelFinishedFutures.add(modelFinished);
      flowManager.execute(model, paramOverrides, new ExecutionListener() {
        public void managerStateChanged(Manager manager) {
        }
        public void executionFinished(Manager manager) {
          modelFinished.set(model);
        }
        public void executionError(Manager manager, Throwable throwable) {
          modelFinished.set(model);
        }
      });
    }
    for(FutureValue<CompositeActor> modelFinished : modelFinishedFutures) {
      try {
        CompositeActor model = modelFinished.get(5, TimeUnit.SECONDS);
        assertion.assertModel(model);
      } catch (TimeoutException e) {
        Assert.fail("Model execution timed out, probable deadlock");
      }
    }
  }
}
