/** TypedIORelation wraps ptolemy.actor.TypedIORelation

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

package org.ptolemy.scala.actor

import ptolemy.kernel.CompositeEntity

/**
 * @author Moez Ben Hajhmida
 *
 */
case class TypedIORelation(name: String)(implicit container: CompositeEntity) {
	/**
	 * The wrapped actor
	 */
	val typedIORelation = container.newRelation(name).asInstanceOf[ptolemy.actor.TypedIORelation]

			/**
			 * Reference to the field 'width' of the IO typedIORelation object.
			 */
			var width = typedIORelation.width

			/**
			 * Stores the width of the IO relation.
			 * Modified by the method --/(width: Int).  
			 */
			var relationWidth = -1

			/**
			 * Sets the relation width to the a value.
			 * @param n the relation width
			 * @return reference to the current object
			 */
			def setWidth(n: Integer):TypedIORelation ={
					typedIORelation.width.setExpression(n.toString())
					this
	}


	/**
	 * Sets the variable relationWidth to the value of width.
	 * Permits to write a composite operator  '--/width-->'.
	 * @param width The width of the relation.
	 * @return reference to the current object.
	 */
	def --/(width: Int): TypedIORelation = {
			relationWidth = width      
					this
	}  
	/**
	 * Connects the object relation to a Ptolemy port of type IOPort.
	 * Sets the relation width to the value of relationWidth, if positive.
	 * @param port The Ptolemy port.
	 * @return reference to the current object.
	 */
	def -->(port: ptolemy.actor.IOPort):TypedIORelation = {
			port.link(typedIORelation)
			if (relationWidth > 0 ) {
				typedIORelation.width.setExpression(relationWidth.toString())
				relationWidth = -1
			}
			this
	}

	/**
	 * Connects the object relation to a pair of Ptolemy ports of type IOPort.
	 * Sets the relation width to the value of relationWidth, if positive.
	 * @param port The pair of Ptolemy ports.
	 */
	def -->( port : (ptolemy.actor.IOPort, ptolemy.actor.IOPort) ) = {
			port._1.link(typedIORelation)
			port._2.link(typedIORelation)			
			if (relationWidth > 0 ) {
				typedIORelation.width.setExpression(relationWidth.toString())						
				relationWidth = -1
			}
	}    



	/**
	 * Connects the object relation to a Ptolemy actor.
	 * Sets the relation width to the value of relationWidth, if positive.
	 * @param actor The Ptolemy actor.
	 * @return reference to the actor.
	 */
	def -->[B <: ComponentEntity](actor: B): B = {
			actor.inputPortList().get(0).link(typedIORelation)
			if (relationWidth > 0 ) {
				typedIORelation.width.setExpression(relationWidth.toString())
				relationWidth = -1
			}
			actor
	}

	/**
	 * Connects the object relation to a pair of Ptolemy actors.
	 * Sets the relation width to the value of relationWidth, if positive.
	 * @param actor The pair of Ptolemy actors.
	 */

	def -->[B <: ComponentEntity]( actors : (B, B) )(implicit d: DummyImplicit) = {
			actors._1.inputPortList().get(0).link(typedIORelation)
			actors._2.inputPortList().get(0).link(typedIORelation)
			if (relationWidth > 0 ) {
				typedIORelation.width.setExpression(relationWidth.toString())						
				relationWidth = -1
			}
	}   


}