/**  A Typed actor that creates multiple instances of given Ptolemy Actor

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

package org.ptolemy.scala.actor.lib.hoc


import org.ptolemy.scala.actor.ComponentEntity
import org.ptolemy.scala.actor.TypedCompositeActor
import org.ptolemy.scala.actor.TypedIOPort
import org.ptolemy.scala.actor.TypedIORelation

import ptolemy.kernel.CompositeEntity


/**
 *  A  scala TypedCompositeActor that creates multiple
 *  instances of a given Ptolemy actor during the runtime.
 */

/** Construct multiple instances of a given actor Class.
 *  @param actorClass The name of the actor class in the form classOf[Actor].
 *  @param name The name used to instantiate the actors
 *  @param N The number of actor instances to be created
 */

/**
 * @author Moez Ben Hajhmida
 *
 */
case class MultiInstance [A <: ComponentEntity](actorClass:Class[A], name: String, N: Integer)(implicit var container: CompositeEntity) extends TypedCompositeActor {

	/**
	 * A ListBuffer to store the references of the instantiated actors
	 */
	val actors  = scala.collection.mutable.ListBuffer.empty[A]  
			// instantiate the actors and store their references in the ListBuffer
			for( a <- 0 until N){
				actors +=  instantiate (actorClass, name+a)
			}

	/**
	 * Instantiates an actor with the given Class.
	 * @param actorClass The name of the actor class 
	 * @return Reference to the instantiated actor.
	 * @exception IllegalArgumentException If one of the argument types is wrong.
	 */
	def instantiate (actorClass:Class[_ <: ComponentEntity], name: String):A={	 
			actorClass.getConstructor(classOf[String], classOf[CompositeEntity]).newInstance( name, container).asInstanceOf[A]	  
	}


	/** 
	 *	! we have to decide about this method !
	 *  @return null there is no the wrapped actor.	
	 */
	def getActor(): ptolemy.actor.TypedCompositeActor = null

			/** Returns a List of actor references.
			 * @return List of actor references.	
			 */  
			def getActors() = actors


			/** For each actor in the list, invokes the method 'setExpression(String expression)' 
			 *  of the Parameter named 'parameterName'.
			 *
			 *  @param parameterName The name of the field to be set.
			 *  @param expression The expression to be set to the field
			 *  @return Reference to the current object.
			 */ 
			override def set(paramaterName: String, expression: String): TypedCompositeActor = {     
					actors.foreach(a => a.set (paramaterName, expression))    
					this
	}
}

