/**
 * A Scala wrapper of the DE domain director.
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

package org.ptolemy.scala.domains.de.kernel

import org.ptolemy.scala.implicits._

import ptolemy.kernel.CompositeEntity

/**
 * Construct a director in the given container with the given name.
 *  The container argument is an implicit parameter.
 *  This class is a wrapper of the ptolemy.domains.sdf.kernel.DEDirector.
 *
 *  @param container ( implicit) Container of the director.
 *  @param name Name of this director.
 *
 */

/**
 * @author Moez Ben Hajhmida
 *
 */
case class DEDirector(name: String)(implicit container: CompositeEntity) {
  /**
   *  This field is a reference to the Java director.
   *   It makes possible to access all attributes and methods
   *   provided by PtolemyII in Java language.
   */
  val director = new ptolemy.domains.de.kernel.DEDirector(container, name)

  /**
   * Invokes the method 'setExpression(String expression)' of the Parameter named 'parameterName'
   * The field named 'parameterName' is of type 'ptolemy.data.expr.Parameter', and is a field of the object 'obj'.'
   * This function performs a java reflection to execute the java code:
   * objectName.parameterName.setExpression(expression) .
   *
   *  @param parameterName The name of the field to be set.
   *  @param expression The expression to be set to the field
   *  @return The reference of the object.
   */
  def set(paramaterName: String, expression: String): DEDirector = {
    setExpression(director, paramaterName, expression)
    this
  }
  
  /**
   * Overloading constructor 
   * Permits call a Class constructor like in PtolemyII Java classes
   * val actor  = new SDFDirector(container, name)
   * @param container The container.
   * @param name The name of this director
   */  
  def this (container: CompositeEntity, name: String) {  this (name)(container)} 

}