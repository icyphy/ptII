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

package org.ptolemy.scala.domains.de.lib
import ptolemy.kernel.CompositeEntity  


import org.ptolemy.scala.implicits._
import org.ptolemy.scala.actor.TypedAtomicActor 
 

/**
 * @author Moez Ben Hajhmida
 *
 */
case class Merge(name: String)(implicit container: CompositeEntity) extends TypedAtomicActor{
  val merge = new ptolemy.domains.de.lib.Merge (container, name) 
  var discardEvents = merge.discardEvents
  var input = merge.input
  var output = merge.output

 
  def getActor():ptolemy.domains.de.lib.Merge = merge


  def set(parameterName: String, expressionString: String):Merge = {   
    setExpression (merge, parameterName, expressionString)
    this 
  }
}
