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
import org.ptolemy.scala.implicits.ImplicitComponentEntity

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
	
		
	 /**
	   * Sets the variable relationWidth to the value of width.
	   * Permits to write a composite operator  '--/width-->'.
	   * @param width The width of the relation.
	   * @return reference to the current object.
	   */        
		def --/(width: Int): MultiInstance [A] = {
				connectionWidth = width      
				this
		}       

	/**
	   * Connect to first N elements of an Actor or a Relation 
	   * High Order Component (Hoc).
	   * Permits to write a composite operator  '--/width-->'.
	   * @param n The first N elements of Hoc.
	   * @return Reference to the current object.
	   */
		def --<(n: Int): MultiInstance [A]  = {
				numberOfElements = n   
				first = true
				this
		}    

		/**
	   * Connect to last N elements of an Actor or a Relation 
	   * High Order Component (Hoc).
	   * Permits to write a composite operator  '--/width-->'.
	   * @param n The last N elements of Hoc.
	   * @return Reference to the current object.
	   */
		def -->(n: Int): MultiInstance [A]  = {
				numberOfElements = n     
				last = true
				this
		}    
		
		/**
	   * Connect the output port of each actor to a variable number of ports of Type ptolemy.actor.IOPort.
	   * Only the first port of the output Port List of each actor is connected.
	   * Set the relation width to the value of relationWidth, if positive.
	   * @param ports The port list to connect to.
	   * @param container The container.
	   * @return Reference to the first port passed in parameters list.
	   */      
		def -->(ports: ptolemy.actor.IOPort*)(implicit container: CompositeEntity):Unit ={ 
				if (connectionWidth > 0 ) {
					actors.foreach(actor1 => ports.foreach(port => actor1 --/connectionWidth--> port))
					connectionWidth = -1
				}else
					actors.foreach(actor1 => ports.foreach(port => actor1 --> port))
					 ports(0)
		}

		/**
	   * Connect the output port of the actor to a variable number of ports of type TypedIOPort.
	   * Only the first port of the output Port List is connected.
	   * Set the relation width to the value of relationWidth, if positive.
	   * @param ports The port list to connect to.
	   * @param container The container.
	   * @return Reference to the first port passed in parameters list.
	   */ 
		def -->(port: TypedIOPort)(implicit container: CompositeEntity):Unit ={ 
				if (connectionWidth > 0 ) {
							this.actors.foreach(actor1 => actor1 --/connectionWidth--> port)
							connectionWidth = -1
				}else
					this.actors.foreach(actor1 => actor1 --> port)
		}

		/**
	   * Connect the output port of the actor to a variable number of relations.
	   * Only the first port of the output Port List is connected.
	   * Set the relation width to the value of relationWidth, if positive.
	   * @param relations The relations list to connect to.
	   * @param container The container.
	   * @return Reference to the first relation passed in parameters list.
	   */
		def -->( relations: TypedIORelation* ): TypedIORelation = {
		  relations.foreach(relation => actors(0).outputPortList().get(0).link(relation.typedIORelation))
				if (connectionWidth > 0 ) {
					 relations.foreach(relation => relation.typedIORelation.width.setExpression(connectionWidth.toString()))
					connectionWidth = -1
				}
		  relations(0)
		}       

		/**
	   * Connect the output port of the actor to a to a variable number of actors.
	   * Only the first port of the output Port List is connected.
	   * Set the relation width to the value of relationWidth, if positive.
	   * @param actors The actor list to connect to.
	   * @param container The container.
	   * @return Reference to the first actor passed in parameters list.
	   */    
		def -->[B <: ComponentEntity](actor2: B)(implicit container: CompositeEntity): B = {
				if (connectionWidth > 0 ) {
					actors.foreach(actor1 => actor1 --/connectionWidth--> actor2)
					connectionWidth = -1
				}else{
					actors.foreach(actor1 => actor1 --> actor2)
				}
				actor2
		}   

	
	/**
	   * Connect the output port of the actor to a to a High Order Component actor.
	   * Only the first port of the output Port List is connected.
	   * Set the relation width to the value of relationWidth, if positive.
	   * @param hocActor The HOC actor to connect to.
	   * @param container The container.
	   * @return Reference to the HOC actor passed in parameter.
	   */      
		def -->[B <: ComponentEntity](this2 : MultiInstance [B])(implicit container: CompositeEntity):MultiInstance [B] ={ 
				if (connectionWidth > 0 ) {
							this.actors.zip(this2.actors).map(x => x match{case (actor1:ComponentEntity, actor2:ComponentEntity) => actor1 --/connectionWidth--> actor2}  )    				
							connectionWidth = -1
				}else
					this.actors.zip(this2.actors).map(x => x match{case (actor1:ComponentEntity, actor2:ComponentEntity) => actor1 --> actor2}  )

					this2
		}

		/**
	   * Connect the output port of the actor to a to a High Order Component relation.
	   * Set the relation width to the value of relationWidth, if positive.
	   * @param hocRelation The HOC actor to connect to.
	   * @param container The container.
	   * @return Reference to the HOC  relation passed in parameter.
	   */   
		def -->(hocRelation : MultiInstanceRelation)(implicit container: CompositeEntity):MultiInstanceRelation ={ 
				if (connectionWidth > 0 ) {
							this.actors.zip(hocRelation.relations).map(x => x match{case (actor1:ComponentEntity, relation:TypedIORelation) => actor1 --/connectionWidth--> relation}  )    				
							connectionWidth = -1
				}else
					this.actors.zip(hocRelation.relations).map(x => x match{case (actor1:ComponentEntity, relation:TypedIORelation) => actor1 --> relation}  )

					hocRelation
		}
		
		/**
		 * Stores the width of a relation.
		 * Modified by the method --/(width: Int).
		 * Used by the method --/(n: Integer) 
		 */	
		private var connectionWidth= 0	

				/**
				 * used by <t(n:Integer) and last(n:Integer) methods.
				 *  first == true, means iterating only the first numberOfElements.
				 */
				private var first = false

				/**
				 * used by >(n:Integer) and last(n:Integer) methods.
				 *  last == true, means iterating only the last numberOfElements.
				 */
				private var last = false

				/**
				 * used by first(n:Integer) and last(n:Integer) methods.   
				 *  numberOfElements is used to know on how many elements to iterate.
				 *   in the case of High Order Components.
				 */
				private var numberOfElements= 0
}