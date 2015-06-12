/* Copyright 2014 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.ptolemy.testsupport;

import ptolemy.actor.CompositeActor;


/**
 * Interface for builders that can be passed into ModelExecutionTester's multi-run test executions.
 * 
 * @author erwindl
 *
 */
public interface ModelBuilder {
  
  /**
   * Build a model instance with the given name.
   * 
   * @param name
   * @return a newly constructed model with the given name.
   * @throws Exception
   */
  CompositeActor buildModel(String name) throws Exception;
}
