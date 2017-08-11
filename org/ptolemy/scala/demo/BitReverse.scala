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

class BitReverse(workspace: Workspace) extends TypedCompositeActor(workspace) {

  // The container is declared as an implicit value to avoid passing 'this' 
  // as a parameter for each actor constructor
  implicit val container = this

  val director = SDFDirector("BitReverseDirector").set("iterations", "2500")
  val ramp1 = Ramp("Ramp1").set("step", "1")
  val ramp2 = Ramp("Ramp2").set("step", "1")

  val commutator1 = Commutator("Commutator1").set("blockSize", "2")
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  val commutator2 = Commutator("Commutator2").set("blockSize", "1")
  val commutator3 = Commutator("Commutator3").set("blockSize", "1")
  val commutator4 = Commutator("Commutator4").set("blockSize", "2")

  val distributor1 = Distributor("Distributor1").set("blockSize", "1")
  val distributor2 = Distributor("Distributor2").set("blockSize", "1")
  val distributor3 = Distributor("Distributor3").set("blockSize", "1")
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val distributor4 = Distributor("Distributor4").set("blockSize", "1")

  val xyPlotter = XYPlotter("xyPlotter").set("grid", false)

  ramp1 --> commutator1
  ramp2 --> commutator1
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  commutator1 --> distributor1

  distributor1 --> distributor3 --/ 1 --> commutator3 --> commutator4
  distributor1 --> distributor2 --/ 1 --> commutator2 --> commutator4

  commutator4 --> distributor4
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  distributor4 --> xyPlotter.inputX
  distributor4 --> xyPlotter.inputY

}
