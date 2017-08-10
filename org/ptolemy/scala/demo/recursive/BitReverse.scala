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

import ptolemy.kernel.CompositeEntity
import ptolemy.kernel.util.Workspace



import org.ptolemy.scala.implicits._
import org.ptolemy.scala.actor._
import org.ptolemy.scala.actor.lib._
import org.ptolemy.scala.actor.lib.gui._
import org.ptolemy.scala.actor.lib.conversions._
import org.ptolemy.scala.domains.sdf.kernel._


/**
 * @author Moez Ben Hajhmida
 *
 */
case class BitReverse(name: String, N: Integer)(implicit var container: CompositeEntity) extends TypedCompositeActor{
    
	   val bitReverse = new ptolemy.actor.TypedCompositeActor (container, name) 
			def getActor(): ptolemy.actor.TypedCompositeActor = bitReverse

			// Override the container which is an implicit variable to avoid passing 'this' 
			// as a parameter for each actor constructor
			container = getActor()
			setName(name)
 if (N < 1) throw new ptolemy.kernel.util.IllegalActionException("Number of inputs should be greater than 0")			
					
					
			if (N > 1){
			  //input
				    val distributor     = new Distributor(this, name+"Distributor").set("blockSize","1")
				    val input   =  TypedIOPort(this, "input", true, false);
						input              -->    distributor 
					//output	
						val commutator  = Commutator(name+"Commutator").set("blockSize", Math.pow(2, N-1).toInt.toString)	
						val output =  TypedIOPort(this, "output", false, true);	
						commutator   -->    output
						
						val bitReverse2 = BitReverse(name+"BitReverse2", N-1)
						val bitReverse3 = BitReverse(name+"BitReverse3", N-1)
						
						distributor --> bitReverse2 --/(N/2)--> commutator
						distributor --> bitReverse3 --/(N/2)--> commutator
			}
			else{			
			  val input   =  TypedIOPort(this, "input", true, false);
			  val output =  TypedIOPort(this, "output", false, true);	
						input    -->      output        						
			}
}
