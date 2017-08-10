/** An actor that creates multiple instances of given Ptolemy  TypedIORelation

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
 *  A  scala class that creates multiple instances of TypedIORelation during the runtime.
 */

/** Construct multiple instances of a TypedIORelation.
 *  @param name The name used to instantiate the relations.
 *  @param N The number of relation instances to be created
 */

/**
 * @author Moez Ben Hajhmida
 *
 */
case class MultiInstanceRelation (name: String, N: Integer)(implicit var container: CompositeEntity) {

  /**
	 * A ListBuffer to store the references of the instantiated relations
	 */
	val relations  = scala.collection.mutable.ListBuffer.empty[TypedIORelation]  
		// instantiate the relations and store their references in the ListBuffer
			for( a <- 0 until N){
				relations +=  TypedIORelation( name+a)
			}


		/** Returns a List of relation references.
			 * @return List of relation references.	
			 */  
	def getRelations() = relations
	  	/**
     --width-->
		 */     
		def --/(width: Int):  MultiInstanceRelation = {
				connectionWidth = width      
						this 
		}       

		/**
   Connects HoC actor output to  an OPort

		 */    
		def -->(port: ptolemy.actor.IOPort)(implicit container: CompositeEntity):Unit ={ 
				if (connectionWidth > 0 ) {
					val width = connectionWidth
							this.relations.foreach{relation => 
							port.link(relation.typedIORelation)
							relation.typedIORelation.width.setExpression(width.toString())		
					}
					connectionWidth = -1
				}else
					this.relations.foreach(relation  => port.link(relation.typedIORelation))				
		}


		/**
   Connects HoC actor output to  a TypedIOPort. 

		 */    
		def -->(port: TypedIOPort)(implicit container: CompositeEntity):Unit ={ 

				if (connectionWidth > 0 ) {
					val width = connectionWidth
							this.relations.foreach{relation => 
							port.getActor().link(relation.typedIORelation)
							relation.typedIORelation.width.setExpression(width.toString())		
					}
					connectionWidth = -1
				}else
					this.relations.foreach(relation  => port.getActor().link(relation.typedIORelation))
		}


		/**
   Connects each relation to an actor  input. 

		 */     
		def -->[B <: ComponentEntity](actor: B)(implicit container: CompositeEntity): B = {
				if (connectionWidth > 0 ) {
					val width = connectionWidth
							this.relations.foreach{relation => 
							actor.inputPortList().get(0).link(relation.typedIORelation)
							relation.typedIORelation.width.setExpression(width.toString())		
					}
					connectionWidth = -1
				}else
					this.relations.foreach(relation  => actor.inputPortList().get(0).link(relation.typedIORelation))

					actor
		}   

	
		/**
   Connects MultiInstanceRelation to a HoC actor. 

		 */    
		def -->[B <: ComponentEntity](hocActor : MultiInstance [B])(implicit container: CompositeEntity):MultiInstance [B] ={ 
				if (connectionWidth > 0 ) {
					val width = connectionWidth
							hocActor.actors.zip(this.relations).map(x => x match{case (actor:ComponentEntity, relation:TypedIORelation) => actor --/width--> relation}  )    				
							connectionWidth = -1
				}else
					hocActor.actors.zip(this.relations).map(x => x match{case (actor:ComponentEntity, relation:TypedIORelation) => actor --> relation}  )
					hocActor
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