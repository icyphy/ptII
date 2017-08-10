/** 

Copyright (c) 2013-2017 The Regents of the University of California.
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

package org.ptolemy.scala.demo.recursive



import ptolemy.kernel.util.Workspace



import org.ptolemy.scala.implicits._
import org.ptolemy.scala.actor._
import org.ptolemy.scala.actor.lib._
import org.ptolemy.scala.actor.lib.gui._
import org.ptolemy.scala.actor.lib.conversions._
import org.ptolemy.scala.domains.sdf.kernel._


class TestRecursive(workspace: Workspace) extends ApplicationActor{
	setName("BitReverseTest")
	
	val director   = SDFDirector ("director")

	val sequence   = new Sequence (this, "sequence").set("values", "{10,11,12,13,14,15,16,17}")
	val bitReverse = BitReverse("BitReverse", 3)
	val display      = Display ("display")

	sequence -->  bitReverse --> display
}
