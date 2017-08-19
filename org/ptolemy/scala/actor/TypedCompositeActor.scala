/**
 *
 * Copyright (c) 2013-2017 The Regents of the University of California.
 * All rights reserved.
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2
 * COPYRIGHTENDKEY
 *
 */

package org.ptolemy.scala.actor

import language.dynamics
import scala.collection.mutable.HashMap

//import org.ptolemy.scala.implicits._

/**
 * @author Moez Ben Hajhmida
 *
 */
abstract class TypedCompositeActor extends ComponentEntity with Dynamic {

  /**
   * Creates a map between a name and a ptolemy.actor.TypedIOPort.
   * Used by selectDynamic(name: String) and updateDynamic(name:String)(value: ptolemy.actor.TypedIOPort).
   * Used to dynamically (during runtime) add and manage dynamic(new) fields to an object.
   */

  private val map = new HashMap[String, ptolemy.actor.TypedIOPort]

  /**
   * Returns a reference to the wrapped actor.
   * This method returns the Ptolemy actor (java) wrapped  by the scala actor.
   * It's useful when the developer wants to access the java methods.
   *
   *  @return Reference to the wrapped actor.
   */
  def getActor(): ptolemy.actor.TypedCompositeActor

  /**
   * Invokes the method 'setExpression(String expression)' of the Parameter named 'parameterName'
   * The field named 'parameterName' is of type 'ptolemy.data.expr.Parameter', and is a field of the object 'obj'.'
   * This function performs a java reflection to execute the java code:
   * objectName.parameterName.setExpression(expression) .
   *
   *  @param parameterName The name of the field to be set.
   *  @param expression The expression to be set to the field
   *  @return Reference to the current object.
   */
  def set(paramaterName: String, expression: String): ComponentEntity = {
    // This method should be overridden in the subclasses
    this
  }

  /**
   * Allows to write field accessors.
   * Used to dynamically create accessors for a dynamic field of an object.
   * @param name The name of the field to be dynamically added to the object.
   */

  def selectDynamic(name: String): ptolemy.actor.TypedIOPort = return map(name)

  /**
   * Allows to write field updates.
   * Used to set a dynamic field of an object.
   * @param name The name of the dynamic field of the object.
   * @param value The Ptolemy port to be added top the object (Ptolemy actor)
   */
  def updateDynamic(name: String)(value: ptolemy.actor.TypedIOPort) = {
    map(name) = value
  }

  /**
   * List all the output ports.
   *  @return A list of output TypedIOPort objects.
   */
  def outputPortList(): java.util.List[ptolemy.actor.TypedIOPort] = {
    getActor().outputPortList().asInstanceOf[java.util.List[ptolemy.actor.TypedIOPort]]
  }

  /**
   * List all the input ports.
   *  @return A list of input TypedIOPort objects.
   */
  def inputPortList(): java.util.List[ptolemy.actor.TypedIOPort] = {
    getActor().inputPortList().asInstanceOf[java.util.List[ptolemy.actor.TypedIOPort]]
  }
}
