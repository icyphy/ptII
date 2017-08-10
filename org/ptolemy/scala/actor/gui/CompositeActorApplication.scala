/** A Ptolemy application that instantiates class names given on the command
 line.

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

package org.ptolemy.scala.actor.gui

/**
 * This is a wrapper of the ptolemy.actor.gui.CompositeActorSimpleApplication
 *  This application creates one or more Ptolemy II models given a
 *   classname on the command line, and then executes those models, each in
 *   its own thread.  Each specified class should be derived from
 *   CompositeActor, and should have a constructor that takes a single
 *   argument, an instance of Workspace.
 *   @see ptolemy.actor.gui.CompositeActorSimpleApplication
 * 
 */
object CompositeActorApplication extends ptolemy.actor.gui.CompositeActorSimpleApplication{
	/** Create a new application with the specified command-line arguments.
	 *  @param args The command-line arguments.
	 */
	def main(args: Array[String]){
		val application = new ptolemy.actor.gui.CompositeActorApplication()
				ptolemy.actor.gui.CompositeActorSimpleApplication._run(application, args)
	}

	/** Run the application.
	 *  @param args The arguments to be passed to the application.
	 */
	def run(args: Array[String]){
		val application = new ptolemy.actor.gui.CompositeActorApplication()
				ptolemy.actor.gui.CompositeActorSimpleApplication._run(application, args)
	}  
}