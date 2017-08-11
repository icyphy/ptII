/**
 * ComponentEntity a wrapper of ptolemy.kernel.ComponentEntity[ptolemy.kernel.ComponentPort].
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
/**
 * @author Moez Ben Hajhmida
 *
 */
/**
 * This trait (equivalent to interface in java) is extended by TypedAtomicActor and TypedCompositeActor classes.
 * This trait is useful for handling references of atomic and composite actors. It provides more higher abstraction
 * while handling Ptolemy actors.
 */

trait ComponentEntity extends ptolemy.kernel.ComponentEntity[ptolemy.kernel.ComponentPort] {
  /**
   * Returns a reference to the wrapped actor.
   * This method returns the Ptolemy actor (java) wrapped  by the scala actor.
   * It's useful when the developer wants to access the java methods.
   *
   *  @return Reference to the wrapped actor.
   */
  def getActor(): ptolemy.kernel.ComponentEntity[_ <: ptolemy.kernel.ComponentPort]

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
  def set(paramaterName: String, expression: String): ComponentEntity

  /**
   * List all the output ports.
   *  @return A list of output TypedIOPort objects.
   */
  def outputPortList(): java.util.List[ptolemy.actor.TypedIOPort]

  /**
   * List all the input ports.
   *  @return A list of input TypedIOPort objects.
   */
  def inputPortList(): java.util.List[ptolemy.actor.TypedIOPort]
}