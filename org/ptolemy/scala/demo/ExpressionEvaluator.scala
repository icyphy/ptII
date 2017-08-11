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

package org.ptolemy.scala.demo

import ptolemy.actor.TypedCompositeActor
import ptolemy.kernel.util.Workspace

import org.ptolemy.scala.implicits._
import org.ptolemy.scala.actor.lib.Expression
import org.ptolemy.scala.actor.lib.gui.InteractiveShell
import org.ptolemy.scala.domains.sdf.kernel.SDFDirector
import org.ptolemy.scala.domains.sdf.lib.SampleDelay

/**
 *
 *
 */
class ExpressionEvaluator(workspace: Workspace) extends TypedCompositeActor(workspace) {
  implicit val container = this
  setName("Shell")

  val director = SDFDirector("director").set("iterations", "3")
  val shell = InteractiveShell("shell")
  val expression = Expression("shellExpression").set("expression", "eval(shell)")
  expression.input = addTypedInputPort(expression, "shell")
  val delay = SampleDelay("delay")

  delay --> shell --> expression --> delay

}