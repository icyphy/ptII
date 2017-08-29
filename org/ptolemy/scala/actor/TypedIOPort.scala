/**
 * TypedIOPort wraps ptolemy.actor.TypedIOPort
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

//import ptolemy.kernel.ComponentEntity  
import ptolemy.kernel.util.Workspace

//import org.ptolemy.scala.implicits._
import org.ptolemy.scala.actor._

/**
 * Overloading constructor.
 * @param container The container.
 * @param name The name of this port
 * @param  isInput True if the port is an input
 * @param  isOutput True if the port is an output
 */
/**
 * @author Moez Ben Hajhmida
 *
 */
case class TypedIOPort(container: ComponentEntity, name: String, isInput: Boolean, isOutput: Boolean) {
   /**
   *  The Java Actor Port wrapper by this Scala Class.
   */
  var typedIOPort: ptolemy.actor.TypedIOPort = new ptolemy.actor.TypedIOPort(container.getActor(), name, isInput, isOutput)

  /**
   * Returns a reference to the wrapped actor.
   * This method returns the Ptolemy actor (java) wrapped  by the scala actor.
   * It's useful when the developer wants to access the java methods.
   *
   *  @return Reference to the wrapped actor.
   */
  def getActor(): ptolemy.actor.TypedIOPort = typedIOPort

  /**
   * If the argument is true, make the port a multiport.
   *  @param isMultiport True to make the port a multiport.
   */
  def setMultiport(isMultiPort: Boolean): TypedIOPort = {
    typedIOPort.setMultiport(isMultiPort)
    this
  }

  /**
   *  Overloading constructor.
   * @param container The container.
   *  @param name The name of this port
   */
  def apply(container: ComponentEntity, name: String) = {
    typedIOPort = new ptolemy.actor.TypedIOPort(container.getActor(), name)
  }
  /**
   *  Overloading constructor.
   * @param container The container.
   */
  def apply(container: ComponentEntity) = {
    typedIOPort = new ptolemy.actor.TypedIOPort(container.getActor().workspace())
  }

  /**
   *  Overloading constructor.
   * @param workspace The workspace.
   */
  def apply(workspace: Workspace) = {
    typedIOPort = new ptolemy.actor.TypedIOPort(workspace)
  }
  
  
  /**
   * True if the port is dynamically created. 
   * See selectDynamic() in org.ptolemy.scala.actor.TypedAtomicActor. 
   */
  var isDynamicInstance = false

}

