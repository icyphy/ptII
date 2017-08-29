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

package org.ptolemy.scala.demo.hocfft


import ptolemy.kernel.CompositeEntity
import ptolemy.kernel.util.Workspace



import org.ptolemy.scala.implicits._
import org.ptolemy.scala.actor._
import org.ptolemy.scala.actor.lib._
import org.ptolemy.scala.actor.lib.hoc._
import org.ptolemy.scala.actor.lib.gui._
import org.ptolemy.scala.actor.lib.conversions._
import org.ptolemy.scala.domains.sdf.kernel._


/**
 * @author Moez Ben Hajhmida
 *
 */
case class FFT(name: String, N: Integer)(implicit var container: CompositeEntity) extends TypedCompositeActor{

	val fft = new ptolemy.actor.TypedCompositeActor (container, name) 
			def getActor(): ptolemy.actor.TypedCompositeActor = fft

			val director   = SDFDirector (name+"director")

			// Override the container which is an implicit variable to avoid passing 'this' 
			// as a parameter for each actor constructor
			container = getActor()
			setName(name)

			val input  =  TypedIOPort(this, "input", true, false).setMultiport(true)
			val output =  TypedIOPort(this, "output", false, true).setMultiport(true)



			if (N >= 2 && N%2 == 0){
				if (N == 2){
					val node1 = TypedIORelation ("Node1")
							val node2   = TypedIORelation ("Node2")
							val adder1  = AddSubtract(name+"Adder1")
							val adder2  = AddSubtract(name+"Adder2")
							val gainPos = Scale(name+"GainPos").set("factor","1")
							val gainNeg = Scale(name+"GainNeg").set("factor","-1")


							input --> (node1,node2)
							node1 --> (gainPos, gainNeg)
							node2 --> (adder1.plus, adder2.plus) 
							gainPos --> adder1 --> output
							gainNeg --> adder2 --> output			    					
				}else{
					    val fft1= FFT(name+"fft1", N/2)
							val fft2= FFT(name+"fft2", N/2)
							
							//MultiInstanceRelation is a HOC Relation handling N nodes (TypedIORelation)
							val nodes  = MultiInstanceRelation("node", N)
							
							// MultiInstance is a HOC Actor handling N Actors 
							// Actors can be a sub-type of ptolemy.kernel.ComponentEntity[ptolemy.kernel.ComponentPort]  
							val adders = MultiInstance(classOf[AddSubtract], name+"Adder", N)
							val gains  = MultiInstance(classOf[Scale], name+"Gain", N)

							// set factor expression for each gain (Scale actor)
							for (n <- 0 until N)
								gains.actors(n).set("factor", "exp(-j*2*"+n+"*pi/"+N+")")			  			  


								input --/(N/2)--> fft1
								input --/(N/2)--> fft2 		

								//link fft1 output to nodes with index < (N/2)
								// operator  --<(n: Integer) iterates on the elements with index < n
								fft1 --<(N/2)--/1--> nodes

								//link fft2 output to nodes with index > (N/2)
								// operator  -->(n: Integer) iterates on the elements with index > n
								fft2  -->(N/2)--/1--> nodes



								// create a list of tuples of adders: (adder_i, adder_(i+N/2). list.lenght =N/2	   
								val adderTuples = adders.actors.dropRight(N/2).zip(adders.actors.drop(N/2))

								// create  a list of  tuples of gains: (gain_i, gain_(i+N/2). list.lenght =N/2	  			   
								val gainTuples = gains.actors.dropRight(N/2).zip(gains.actors.drop(N/2))

								//concatenate adderTuples and gainTuples lists in one list of size N
								val allTuples = adderTuples++gainTuples

								//create a new list of tuples. 
								// for i < N/2, tuple_i = (node_i,(adder_i, adder_(i+N/2))
								// for i> N/2, tuple_i = (node_i, (gain_i, gain_(i+N/2))
								val connectionList = nodes.relations.zip(allTuples)

								// for each tuple (node_i,(x_i, y_i)) in the list apply  node_i --> (x_i, y_i)   
								connectionList.foreach(tuple =>tuple._1 --/1--> tuple._2.asInstanceOf[ComponentEntity])
								
								

								
								// connects one-to-one  actors of the 2 HOC actors
								// for (i form 1 toN):  gain_i --/1--> adder_i
								gains --/1--> adders

								// connects many-to-one  actors of the HOC actor (adders) to the output port
								// for (i form 1 toN): adder_i --/1--> output
								adders --/1-->output
				}
			}
}
