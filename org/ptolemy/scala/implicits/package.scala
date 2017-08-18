/**
 * package object implicits. Provides the implicit type conversions.
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

package org.ptolemy.scala

import org.ptolemy.scala.actor.ComponentEntity
import org.ptolemy.scala.actor.TypedIOPort
import org.ptolemy.scala.actor.TypedIORelation
import org.ptolemy.scala.actor.lib.hoc.MultiInstance
import org.ptolemy.scala.actor.lib.hoc.MultiInstanceRelation

import ptolemy.kernel.CompositeEntity
import ptolemy.kernel.util.IllegalActionException
import ptolemy.kernel.util.NamedObj

import scala.language.implicitConversions

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
/**
 * @author Moez Ben Hajhmida
 *
 */
package object implicits {
    /**
     *  Convert implicitly  a pair of java Int '(Int, Int)' to a pair of java Double '(Double, Double)'.
     *  The compiler automatically detects and applies the conversion whenever it is needed
     *  @param x Pair of int
     *  @return Pair of Double
     */
    implicit def int2Double(x: (Int, Int)) = (x._1.asInstanceOf[Double], x._2.asInstanceOf[Double])

            /**
             *  Create a ptolemy.actor.TypedIOPort as Input.
             *  @param container The container
             *  @param name Name of the port
             *  @return The instantiated port
             */
            def addTypedInputPort(container: ComponentEntity, name: String): ptolemy.actor.TypedIOPort = {
                    new ptolemy.actor.TypedIOPort(container.getActor(), name, true, false)
    }
    /**
     *  Create a ptolemy.actor.TypedIOPort as Output.
     *  @param container The container
     *  @param name Name of the port
     *  @return The instantiated port
     */
    def addTypedOutputPort(container: ComponentEntity, name: String): ptolemy.actor.TypedIOPort = {
            new ptolemy.actor.TypedIOPort(container.getActor(), name, false, true)
    }
    /**
     *  Create a ptolemy.actor.TypedIOPort as InputOutput.
     *  @param container The container
     *  @param name Name of the port
     *  @return The instantiated port
     */
    def addTypedIOutputPort(container: ComponentEntity, name: String): ptolemy.actor.TypedIOPort = {
            new ptolemy.actor.TypedIOPort(container.getActor(), name, true, true)
    }

    /**
     * Invokes the method 'setExpression(String expression)' of the Parameter named 'parameterName'
     *  The field named 'parameterName' is of type 'ptolemy.data.expr.Parameter', and is a field of the object 'obj'.
     *  This function performs a java reflection to execute the java code:
     *  objectName.parameterName.setExpression(expression) .
     *
     *  @param parameterName The name of the field to be set
     *  @param expression The expression to be set to the field
     *  @return Reference to the current object.
     *  @exception NoSuchMethodException If the reflexed method is wrong.
     *  @exception NoSuchFieldException If the parameterName is not a field of the Class.
     */
    def setExpression(obj: NamedObj, parameterName: String, expression: String): Unit = {
            try {
                // get the field definition
                val fieldDefinition = obj.getClass().getField(parameterName)
                        // obtain the field value from the object instance
                        val fieldValue = fieldDefinition.get(obj)
                        // get declared method    
                        val myMethod = fieldValue.getClass().getMethod("setExpression", classOf[String])
                        // invoke method on the instance of the field from your object instance
                        myMethod.invoke(fieldValue, expression)
            } catch {
            case noMethod: NoSuchMethodException => println(noMethod.getMessage())
            case noField: NoSuchFieldException   => println("Parameter '" + parameterName + "' is not defined for '" + obj.getClass().getSimpleName() + "'. Please check spelling.")
            }
    }




    /**
     *                   class ImplicitComponentEntity
     *
     * Implicit Class to add methods to a Ptolemy actor
     * without modifying the source code.
     * The actor must extend the trait ComponentEntity.
     * Implicit conversion: when the compiler finds p --> x , with p of type
     * any subtype of ComponentEntity.
     * Consider p as an instance of class ImplicitComponentEntity and tries to match  x
     * to one of the applicable types declared below.
     */
    implicit class ImplicitComponentEntity[A <: ComponentEntity](actor: A) {

        /**
         * Sets the variable relationWidth to the value of width.
         * Permits to write a composite operator  '--/width-->'.
         * @param width The width of the relation.
         * @return Reference to the current object.
         */
        def --/(width: Int): A = {
                connectionWidth = width
                        actor
        }

        /**
         * Connect to first N elements of an Actor or a Relation
         * High Order Component (Hoc).
         * Permits to write a composite operator  '--/width-->'.
         * @param n The first N elements of Hoc.
         * @return Reference to the current object.
         */
        def --<(n: Int): A = {
                numberOfElements = n
                        first = true
                        actor
        }

        /**
         * Connect to last N elements of an Actor or a Relation
         * High Order Component (Hoc).
         * Permits to write a composite operator  '--/width-->'.
         * @param n The last N elements of Hoc.
         * @return Reference to the current object.
         */
        def -->(n: Int): A = {
                numberOfElements = n
                        last = true
                        actor
        }

        /**
         * Connect the output port of the actor to a variable number of ports of Type ptolemy.actor.IOPort.
         * Only the first port of the output Port List is connected.
         * Set the relation width to the value of relationWidth, if positive.
         * @param ports The port list to connect to.
         * @param container The container.
         * @return Reference to the first port passed in parameters list.
         */
        def -->(ports: ptolemy.actor.IOPort*)(implicit container: CompositeEntity): ptolemy.actor.IOPort = {
          // check actor type
          actor match {
            /** case a High order Component, the actor needs to be processed in a specific way.
             *  MultiInstance[ComponentEntity] is a sub-type of ComponentEntity, the compiler
             *  implicitly converts it to a ComponentEntity type.
             *  We explicitly make the conversion to MultiInstance[ComponentEntity] type.
            */
            case mutltiInstanceActor:MultiInstance[ComponentEntity] =>
              ports.foreach(port =>actor.asInstanceOf[MultiInstance[ComponentEntity]] --> port)
              
            
            case _ =>{   
                ports.foreach(port => {
                    val relation = container.connect(actor.outputPortList().get(0), port)
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
            }
          }
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
        def -->(ports: TypedIOPort*)(implicit container: CompositeEntity): TypedIOPort = {
          // check actor type
          actor match {
            /** case a High order Component, the actor needs to be processed in a specific way.
             *  MultiInstance[ComponentEntity] is a sub-type of ComponentEntity, the compiler
             *  implicitly converts it to a ComponentEntity type.
             *  We explicitly make the conversion to MultiInstance[ComponentEntity] type.
            */
            case mutltiInstanceActor:MultiInstance[ComponentEntity] =>
              ports.foreach(port2 =>actor.asInstanceOf[MultiInstance[ComponentEntity]] --> port2)
              
            
            case _ =>{          
                ports.foreach(port2 => {
                    val relation = container.connect(actor.outputPortList().get(0), port2.getActor())
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
           }
          }
                ports(0)
        }

        /**
         * Connect the output port of the actor to a variable number of relations.
         * Only the first port of the output Port List is connected.
         * Set the relation width to the value of relationWidth, if positive.
         * @param relations The relations list to connect to.
         * @param container The container.
         * @return Reference to the first relation passed in parameters list.
         */
        def -->(relations: TypedIORelation*): TypedIORelation = {
          // check actor type
          actor match {
            /** case a High order Component, the actor needs to be processed in a specific way.
             *  MultiInstance[ComponentEntity] is a sub-type of ComponentEntity, the compiler
             *  implicitly converts it to a ComponentEntity type.
             *  We explicitly make the conversion to MultiInstance[ComponentEntity] type.
            */
            case mutltiInstanceActor:MultiInstance[ComponentEntity] =>
              relations.foreach(relation => mutltiInstanceActor --> relation)
              
            
            case _ =>{ 
                relations.foreach(relation => actor.outputPortList().get(0).link(relation.typedIORelation))
                if (connectionWidth > 0) {
                    relations.foreach(relation => relation.typedIORelation.width.setExpression(connectionWidth.toString()))
                    connectionWidth = -1
                }
            }
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
        def -->[B <: ComponentEntity](actors: B*)(implicit container: CompositeEntity): B = {
          // check actor type
          actor match {
            /** case a High order Component, the actor needs to be processed in a specific way.
             *  MultiInstance[ComponentEntity] is a sub-type of ComponentEntity, the compiler
             *  implicitly converts it to a ComponentEntity type.
             *  We explicitly make the conversion to MultiInstance[ComponentEntity] type.
            */
            case mutltiInstanceActor:MultiInstance[ComponentEntity] =>
              actors.foreach(actor2 => mutltiInstanceActor --> actor2)
              
            case _ =>{         
                actors.foreach(actor2 => {                                   
                    val relation = container.connect(actor.outputPortList().get(0), actor2.inputPortList().get(0))
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
             }
          }
                actors(0)
        }

        /**
         * Connect the output port of the actor to a to a High Order Component actor.
         * Only the first port of the output Port List is connected.
         * Set the relation width to the value of relationWidth, if positive.
         * @param hocActor The HOC actor to connect to.
         * @param container The container.
         * @return Reference to the HOC actor passed in parameter.
         */
        def -->[B <: ComponentEntity](hocActor: MultiInstance[B])(implicit container: CompositeEntity): MultiInstance[B] = {
          // check actor type
          actor match {
            /** case a High order Component, the actor needs to be processed in a specific way.
             *  MultiInstance[ComponentEntity] is a sub-type of ComponentEntity, the compiler
             *  implicitly converts it to a ComponentEntity type.
             *  We explicitly make the conversion to MultiInstance[ComponentEntity] type.
            */
            case mutltiInstanceActor:MultiInstance[ComponentEntity] =>
              mutltiInstanceActor --> hocActor
              
            
            case _ =>{   
                if (connectionWidth > 0) {
                    if (first || last) {
                        if (first) {
                            hocActor.actors.dropRight(numberOfElements).foreach(actor2 => actor --/ connectionWidth --> actor2)
                            first = false
                        } else {
                            hocActor.actors.drop(numberOfElements).foreach(actor2 => actor --/ connectionWidth --> actor2)
                            last = false
                        }
                    } else
                        hocActor.actors.foreach(actor2 => actor --/ connectionWidth --> actor2)
                        connectionWidth = -1
                } else if (first || last) {
                    if (first) {
                        hocActor.actors.dropRight(numberOfElements).foreach(actor2 => actor --> actor2)
                        first = false
                    } else {
                        hocActor.actors.drop(numberOfElements).foreach(actor2 => actor --> actor2)
                        last = false
                    }
                } else
                    hocActor.actors.foreach(actor2 => actor --> actor2)
            }

                    hocActor
          }
        }

        /**
         * Connect the output port of the actor to a to a High Order Component relation.
         * Set the relation width to the valu relation actor to connect to.
         * @param container The container.
         * @return Reference to the HOC  relation passed in parameter.
         */
        def -->(hocRelation: MultiInstanceRelation)(implicit container: CompositeEntity): MultiInstanceRelation = {
          // check actor type
          actor match {
            /** case a High order Component, the actor needs to be processed in a specific way.
             *  MultiInstance[ComponentEntity] is a sub-type of ComponentEntity, the compiler
             *  implicitly converts it to a ComponentEntity type.
             *  We explicitly make the conversion to MultiInstance[ComponentEntity] type.
            */
            case mutltiInstanceActor:MultiInstance[ComponentEntity] =>
              mutltiInstanceActor --> hocRelation
              
            
            case _ =>{ 
                if (connectionWidth > 0) {
                    if (first || last) {
                        if (first) {
                            hocRelation.relations.dropRight(numberOfElements).foreach(relation => actor --/ connectionWidth --> relation)
                            first = false
                        } else {
                            hocRelation.relations.drop(numberOfElements).foreach(relation => actor --/ connectionWidth --> relation)
                            last = false
                        }
                    } else
                        hocRelation.relations.foreach(relation => actor --/ connectionWidth --> relation)
                        connectionWidth = -1
                } else if (first || last) {
                    if (first) {
                        hocRelation.relations.dropRight(numberOfElements).foreach(relation => actor --> relation)
                        first = false
                    } else {
                        hocRelation.relations.drop(numberOfElements).foreach(relation => actor --> relation)
                        last = false
                    }
                } else
                    hocRelation.relations.foreach(relation => actor --> relation)
            }
          }

                    hocRelation
        }

        /**
         * Connects actor output to  another actor (actor2) input.
         * Each actor should be a subclass of  ptolemy.actor.TypedCompositeActor.
         */

        /**
         * Connect one-to-one all output ports of the actor to
         * all input ports of the actor passed in parameter.
         * Set the relation width to the value of relationWidth, if positive.
         * @param actors The actor to connect to.
         * @param container The container.
         * @return Reference to the actor passed in parameter.
         * @exception If the actor is a MultiInstance Actor.
         */
        def ==>[B <: ComponentEntity](actor2: B)(implicit container: CompositeEntity): B = {
                if(actor.isInstanceOf[MultiInstance[ComponentEntity]])
                  throw new IllegalActionException(actor, "'==>' operator cannot be applied to MultiInstance Actor")
                if (connectionWidth > 0) {
                    var relation: ptolemy.actor.IORelation = null
                            for (i <- 0 to actor.outputPortList().size() - 1) {
                                relation = container.connect(actor.outputPortList().get(i), actor2.inputPortList().get(i)).asInstanceOf[ptolemy.actor.IORelation]
                                        relation.width.setExpression(connectionWidth.toString())
                            }
                connectionWidth = -1
                } else {
                    for (i <- 0 to actor.outputPortList().size() - 1)
                        container.connect(actor.outputPortList().get(i), actor2.inputPortList().get(i))
                }
                actor2
        }
    }

    /**
     *                   class ImplicitMultiInstance
     *
     * Implicit Class to add methods to a MultiInstance actor.   * 
     * Implicit conversion: when the compiler finds p --> x , with p of type MultiInstance.
     * Consider p as an instance of class MultiInstance and tries to match  x
     * to one of the applicable types declared below.
     */
    implicit class ImplicitMultiInstance(hoActor: MultiInstance[ComponentEntity]) {  
        /**
         * Sets the variable relationWidth to the value of width.
         * Permits to write a composite operator  '--/width-->'.
         * @param width The width of the relation.
         * @return reference to the current object.
         */        
        def --/(width: Int): MultiInstance[ComponentEntity] = {
                connectionWidth = width      
                        hoActor
        }       

        /**
         * Connect to first N elements of an Actor or a Relation 
         * High Order Component (Hoc).
         * Permits to write a composite operator  '--/width-->'.
         * @param n The first N elements of Hoc.
         * @return Reference to the current object.
         */
        def --<(n: Int): MultiInstance[ComponentEntity]  = {
                numberOfElements = n   
                        first = true
                        hoActor
        }    

        /**
         * Connect to last N elements of an Actor or a Relation 
         * High Order Component (Hoc).
         * Permits to write a composite operator  '--/width-->'.
         * @param n The last N elements of Hoc.
         * @return Reference to the current object.
         */
        def -->(n: Int): MultiInstance[ComponentEntity]  = {
                numberOfElements = n     
                        last = true
                        hoActor
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
                    hoActor.actors.foreach(actor1 => ports.foreach(port => actor1 --/connectionWidth--> port))
                    connectionWidth = -1
                }else
                    hoActor.actors.foreach(actor1 => ports.foreach(port => actor1 --> port))
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
                    hoActor.actors.foreach(actor1 => actor1 --/connectionWidth--> port)
                    connectionWidth = -1
                }else
                    hoActor.actors.foreach(actor1 => actor1 --> port)
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
                relations.foreach(relation => hoActor.actors(0).outputPortList().get(0).link(relation.typedIORelation))
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
                    hoActor.actors.foreach(actor1 => actor1 --/connectionWidth--> actor2)
                    connectionWidth = -1
                }else{
                    hoActor.actors.foreach(actor1 => actor1 --> actor2)
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
        def -->[B <: ComponentEntity](hoActor2 : MultiInstance [B])(implicit container: CompositeEntity):MultiInstance [B] ={ 
                if (connectionWidth > 0 ) {
                    hoActor.actors.zip(hoActor2.actors).map(x => x match{case (actor1:ComponentEntity, actor2:ComponentEntity) => actor1 --/connectionWidth--> actor2}  )    				
                    connectionWidth = -1
                }else
                    hoActor.actors.zip(hoActor2.actors).map(x => x match{case (actor1:ComponentEntity, actor2:ComponentEntity) => actor1 --> actor2}  )
                    
                hoActor2
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
                    hoActor.actors.zip(hocRelation.relations).map(x => x match{case (actor1:ComponentEntity, relation:TypedIORelation) => actor1 --/connectionWidth--> relation}  )    				
                    connectionWidth = -1
                }else
                    hoActor.actors.zip(hocRelation.relations).map(x => x match{case (actor1:ComponentEntity, relation:TypedIORelation) => actor1 --> relation}  )

                    hocRelation
        }

    }


    /**
     *                   class ImplicitMultiInstanceRelation
     *
     * Implicit Class to add methods to a MultiInstance actor.   * 
     * Implicit conversion: when the compiler finds p --> x , with p of type MultiInstance.
     * Consider p as an instance of class MultiInstance and tries to match  x
     * to one of the applicable types declared below.
     */
    implicit class ImplicitMultiInstanceRelation(hoRelation: MultiInstanceRelation) {

        /**
         * --width-->
         */     
        def --/(width: Int):  MultiInstanceRelation = {
                connectionWidth = width      
                        hoRelation 
        }       

        /**
         * Connects HoC actor output to  an OPort
         */    
        def -->(port: ptolemy.actor.IOPort)(implicit container: CompositeEntity):Unit ={ 
                if (connectionWidth > 0 ) {
                    val width = connectionWidth
                            hoRelation.relations.foreach{relation => 
                            port.link(relation.typedIORelation)
                            relation.typedIORelation.width.setExpression(width.toString())		
                    }
                    connectionWidth = -1
                }else
                    hoRelation.relations.foreach(relation  => port.link(relation.typedIORelation))				
        }


        /**
         * Connects HoC actor output to  a TypedIOPort.
         */    
        def -->(port: TypedIOPort)(implicit container: CompositeEntity):Unit ={ 

                if (connectionWidth > 0 ) {
                    val width = connectionWidth
                            hoRelation.relations.foreach{relation => 
                            port.getActor().link(relation.typedIORelation)
                            relation.typedIORelation.width.setExpression(width.toString())		
                    }
                    connectionWidth = -1
                }else
                    hoRelation.relations.foreach(relation  => port.getActor().link(relation.typedIORelation))
        }


        /**
         * Connects each relation to an actor  input.
         */     
        def -->[B <: ComponentEntity](actor: B)(implicit container: CompositeEntity): B = {
                if (connectionWidth > 0 ) {
                    val width = connectionWidth
                            hoRelation.relations.foreach{relation => 
                            actor.inputPortList().get(0).link(relation.typedIORelation)
                            relation.typedIORelation.width.setExpression(width.toString())		
                    }
                    connectionWidth = -1
                }else
                    hoRelation.relations.foreach(relation  => actor.inputPortList().get(0).link(relation.typedIORelation))

                    actor
        }   


        /**
         * Connects MultiInstanceRelation to a HoC actor.
         */    
        def -->[B <: ComponentEntity](hocActor : MultiInstance [B])(implicit container: CompositeEntity):MultiInstance [B] ={ 
                if (connectionWidth > 0 ) {
                    val width = connectionWidth
                            hocActor.actors.zip(hoRelation.relations).map(x => x match{case (actor:ComponentEntity, relation:TypedIORelation) => actor --/width--> relation}  )    				
                            connectionWidth = -1
                }else
                    hocActor.actors.zip(hoRelation.relations).map(x => x match{case (actor:ComponentEntity, relation:TypedIORelation) => actor --> relation}  )
                    hocActor
        }
    }


    /**
     *            class ImplicitIOPort
     *
     * Implicit Class to add methods to an object of type ptolemy.actor.IOPort
     *  without modifying the source code of the object.
     * Implicit conversion: when the compiler finds p --> x , with p of type ptolemy.actor.IOPort
     * it considers p as an instance of class ptolemy.actor.IOPort and tries to match  x
     * to one of the applicable types declared below.
     */

    implicit class ImplicitIOPort(port: ptolemy.actor.IOPort) {
        /**
         * Sets the variable relationWidth to the value of width.
         * Permits to write a composite operator  '--/width-->'.
         * @param width The width of the relation.
         * @return Reference to the current object.
         */
        def --/(width: Int): ptolemy.actor.IOPort = {
                connectionWidth = width
                        port
        }
        /**
         * Connect a port of type ptolemy.actor.IOPort to a variable number of ports of Type ptolemy.actor.IOPort.
         * Set the relation width to the value of relationWidth, if positive.
         * @param ports The port list to connect to.
         * @param container The container.
         * @return Reference to the first port passed in parameters list.
         */
        def -->(ports: ptolemy.actor.IOPort*)(implicit container: CompositeEntity): ptolemy.actor.IOPort = {
                ports.foreach(port2 => {
                    val relation = container.connect(port, port2)
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
                ports(0)
        }
        /**
         * Connect a port of type ptolemy.actor.IOPort to to a variable number of ports of type TypedIOPort.
         * Set the relation width to the value of relationWidth, if positive.
         * @param ports The port list to connect to.
         * @param container The container.
         * @return Reference to the first port passed in parameters list.
         */
        def -->(ports: TypedIOPort*)(implicit container: CompositeEntity): TypedIOPort = {
                ports.foreach(port2 => {
                    val relation = container.connect(port, port2.getActor())
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
                ports(0)
        }

        /**
         * Connect a port of type ptolemy.actor.IOPort to a variable number of relations.
         * Set the relation width to the value of relationWidth, if positive.
         * @param relations The relations list to connect to.
         * @param container The container.
         * @return Reference to the first relation passed in parameters list.
         */
        def -->(relations: TypedIORelation*): TypedIORelation = {
                relations.foreach(relation => port.link(relation.typedIORelation))
                if (connectionWidth > 0) {
                    relations.foreach(relation => relation.typedIORelation.width.setExpression(connectionWidth.toString()))
                    connectionWidth = -1
                }
                relations(0)
        }

        /**
         * Connect a port of type ptolemy.actor.IOPort to a to a variable number of actors.
         * Set the relation width to the value of relationWidth, if positive.
         * @param actors The actor list to connect to.
         * @param container The container.
         * @return Reference to the first actor passed in parameters list.
         */
        def -->[B <: ComponentEntity](actors: B*)(implicit container: CompositeEntity): B = {
                actors.foreach(actor => {
                    val relation = container.connect(port, actor.inputPortList().get(0))
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
                actors(0)
        }

        /**
         * Connect the port to a to a High Order Component actor.
         * Set the relation width to the value of relationWidth, if positive.
         * @param hocActor The HOC actor to connect to.
         * @param container The container.
         * @return Reference to the HOC actor passed in parameter.
         */
        def -->[B <: ComponentEntity](hocActor: MultiInstance[B])(implicit container: CompositeEntity): MultiInstance[B] = {
                if (connectionWidth > 0) {
                    if (first || last) {
                        if (first) {
                            hocActor.actors.dropRight(numberOfElements).foreach(actor2 => port --/ connectionWidth --> actor2)
                            first = false
                        } else {
                            hocActor.actors.drop(numberOfElements).foreach(actor2 => port --/ connectionWidth --> actor2)
                            last = false
                        }
                    } else
                        hocActor.actors.foreach(actor2 => port --/ connectionWidth --> actor2)
                        connectionWidth = -1
                } else if (first || last) {
                    if (first) {
                        hocActor.actors.dropRight(numberOfElements).foreach(actor2 => port --> actor2)
                        first = false
                    } else {
                        hocActor.actors.drop(numberOfElements).foreach(actor2 => port --> actor2)
                        last = false
                    }
                } else
                    hocActor.actors.foreach(actor2 => port --> actor2)

                    hocActor
        }

        /**
         * Connect the  port to a to a High Order Component relation.
         * Set the relation width to the value of relationWidth, if positive.
         * @param hocActor The HOC relation to connect to.
         * @param container The container.
         * @return Reference to the HOC  relation passed in parameter.
         */
        def -->(hocRelation: MultiInstanceRelation)(implicit container: CompositeEntity): MultiInstanceRelation = {
                if (connectionWidth > 0) {
                    if (first || last) {
                        if (first) {
                            hocRelation.relations.dropRight(numberOfElements).foreach(relation => port --/ connectionWidth --> relation)
                            first = false
                        } else {
                            hocRelation.relations.drop(numberOfElements).foreach(relation => port --/ connectionWidth --> relation)
                            last = false
                        }
                    } else
                        hocRelation.relations.foreach(relation => port --/ connectionWidth --> relation)
                        connectionWidth = -1
                } else if (first || last) {
                    if (first) {
                        hocRelation.relations.dropRight(numberOfElements).foreach(relation => port --> relation)
                        first = false
                    } else {
                        hocRelation.relations.drop(numberOfElements).foreach(relation => port --> relation)
                        last = false
                    }
                } else
                    hocRelation.relations.foreach(relation => port --> relation)

                    hocRelation
        }

    }

    /**
     *     class ImplicitTypedIOPort
     * Implicit Class to add methods to an object of type TypedIOPort
     *  without modifying the source code of the object.
     * Implicit conversion: when the compiler finds p --> x , with p of type  "TypedIOPort"
     * it considers p as an instance of class TypedIOPort and tries to match  x
     * to one of the applicable types declared below.
     */
    implicit class ImplicitTypedIOPort(port: TypedIOPort) {
        /**
         * Sets the variable relationWidth to the value of width.
         * Permits to write a composite operator  '--/width-->'.
         * @param width The width of the relation.
         * @return Reference to the current object.
         */
        def --/(width: Int): TypedIOPort = {
                connectionWidth = width
                        port
        }

        /**
         * Connect a port of type TypedIOPort to a variable number of ports of Type ptolemy.actor.IOPort.
         * Set the relation width to the value of relationWidth, if positive.
         * @param ports The port list to connect to.
         * @param container The container.
         * @return Reference to the first port passed in parameters list.
         */
        def -->(ports: ptolemy.actor.IOPort*)(implicit container: CompositeEntity): ptolemy.actor.IOPort = {
                ports.foreach(port2 => {
                    val relation = container.connect(port.getActor(), port2)
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
                ports(0)
        }
        /**
         * Connect a port of type TypedIOPort to to a variable number of ports of type TypedIOPort.
         * Set the relation width to the value of relationWidth, if positive.
         * @param ports The port list to connect to.
         * @param container The container.
         * @return Reference to the first port passed in parameters list.
         */
        def -->(ports: TypedIOPort*)(implicit container: CompositeEntity): TypedIOPort = {
                ports.foreach(port2 => {
                    val relation = container.connect(port.getActor(), port2.getActor())
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
                ports(0)
        }

        /**
         * Connect a port of type TypedIOPort to a variable number of relations.
         * Set the relation width to the value of relationWidth, if positive.
         * @param relations The relations list to connect to.
         * @param container The container.
         * @return Reference to the first relation passed in parameters list.
         */
        def -->(relations: TypedIORelation*): TypedIORelation = {
                relations.foreach(relation => port.getActor().link(relation.typedIORelation))
                if (connectionWidth > 0) {
                    relations.foreach(relation => relation.typedIORelation.width.setExpression(connectionWidth.toString()))
                    connectionWidth = -1
                }
                relations(0)
        }

        /**
         * Connect a port of type TypedIOPort to a to a variable number of actors.
         * Set the relation width to the value of relationWidth, if positive.
         * @param actors The actor list to connect to.
         * @param container The container.
         * @return Reference to the first actor passed in parameters list.
         */
        def -->[B <: ComponentEntity](actors: B*)(implicit container: CompositeEntity): B = {
                actors.foreach(actor => {
                    val relation = container.connect(port.getActor(), actor.inputPortList().get(0))
                            if (connectionWidth > 0) {
                                relation.asInstanceOf[ptolemy.actor.IORelation].width.setExpression(connectionWidth.toString())
                                connectionWidth = -1
                            }
                })
                actors(0)
        }
        /**
         * Connect the port to a to a High Order Component actor.
         * Set the relation width to the value of relationWidth, if positive.
         * @param hocActor The HOC actor to connect to.
         * @param container The container.
         * @return Reference to the HOC actor passed in parameter.
         */
        def -->[B <: ComponentEntity](hocActor: MultiInstance[B])(implicit container: CompositeEntity): MultiInstance[B] = {
                if (connectionWidth > 0) {
                    if (first || last) {
                        if (first) {
                            hocActor.actors.dropRight(numberOfElements).foreach(actor2 => port --/ connectionWidth --> actor2)
                            first = false
                        } else {
                            hocActor.actors.drop(numberOfElements).foreach(actor2 => port --/ connectionWidth --> actor2)
                            last = false
                        }
                    } else
                        hocActor.actors.foreach(actor2 => port --/ connectionWidth --> actor2)
                        connectionWidth = -1
                } else if (first || last) {
                    if (first) {
                        hocActor.actors.dropRight(numberOfElements).foreach(actor2 => port --> actor2)
                        first = false
                    } else {
                        hocActor.actors.drop(numberOfElements).foreach(actor2 => port --> actor2)
                        last = false
                    }
                } else
                    hocActor.actors.foreach(actor2 => port --> actor2)

                    hocActor
        }
        /**
         * Connect the  port to a to a High Order Component relation.
         * @param container The container.
         * @return Reference to the HOC  relation passed in parameter.
         */
        def -->(hocRelation: MultiInstanceRelation)(implicit container: CompositeEntity): MultiInstanceRelation = {
                if (connectionWidth > 0) {
                    if (first || last) {
                        if (first) {
                            hocRelation.relations.dropRight(numberOfElements).foreach(relation => port --/ connectionWidth --> relation)
                            first = false
                        } else {
                            hocRelation.relations.drop(numberOfElements).foreach(relation => port --/ connectionWidth --> relation)
                            last = false
                        }
                    } else
                        hocRelation.relations.foreach(relation => port --/ connectionWidth --> relation)
                        connectionWidth = -1
                } else if (first || last) {
                    if (first) {
                        hocRelation.relations.dropRight(numberOfElements).foreach(relation => port --> relation)
                        first = false
                    } else {
                        hocRelation.relations.drop(numberOfElements).foreach(relation => port --> relation)
                        last = false
                    }
                } else
                    hocRelation.relations.foreach(relation => port --> relation)

                    hocRelation
        }
    }
    /**
     * Stores the width of a relation.
     * Modified by the method --/(width: Int).
     * Used by the method --/(n: Integer)
     */
    private var connectionWidth = 0

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
            private var numberOfElements = 0
}
