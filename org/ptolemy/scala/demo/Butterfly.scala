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
import org.ptolemy.scala.actor._
import org.ptolemy.scala.actor.lib._
import org.ptolemy.scala.actor.lib.gui._
import org.ptolemy.scala.actor.lib.conversions._
import org.ptolemy.scala.domains.sdf.kernel._

class Butterfly(workspace: Workspace) extends ApplicationActor {

  // The container is declared as an implicit value to avoid passing 'this' 
  // as a parameter for each actor constructor
  //implicit val container = this
  setName("Butterfly")

  val director = SDFDirector("director").set("period", "10").set("iterations", "2500")
  val ramp = Ramp("Ramp").set("step", "PI/100.0")
  val expression =
    Expression("Expression").set("expression", "-2.0*cos(4.0*ramp) + "
      + "exp(cos(ramp)) + (sin(ramp/12.0) * (sin(ramp/12.0))^4)")
  expression.ramp = addTypedInputPort(expression, "ramp")
  val polarToCartesian = PolarToCartesian("Polar to Cartesian")
  val xyPlotter = XYPlotter("xyPlotter").set("grid", false).set("xRange", (-3, 4)).set("yRange", (-4, 4))
  // Create a relation link to relation
  val node = TypedIORelation("node")

  //link output to relation
  ramp.output --> node --> (expression.ramp, polarToCartesian.angle)

  //point-to-point connections
  expression.output --> polarToCartesian.magnitude
  polarToCartesian ==> xyPlotter

}
