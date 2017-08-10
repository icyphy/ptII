/** ApplicationActor

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


/**
 * A Ptolemy class to be extended by classes intending to run 
 * Ptolemy models.
 */
/**
 * @author Moez Ben Hajhmida
 *
 */
abstract class ApplicationActor  extends ptolemy.actor.TypedCompositeActor {
	/** implicit parameter container.
	 * By extending this /**
 * @author Moez Ben Hajhmida
 *
 */
abstract class, this implicit parameters avoids the use
	 * of the container as a parameter when instantiating actors.
	 */
	implicit var container = this

			/** Returns a reference to the wrapped actor.
			 * This method returns the Ptolemy actor (java) wrapped  by the scala actor.
			 * It's useful when the developer wants to access the java methods.
			 *	
			 *  @return Reference to the wrapped actor.	
			 */
			def getActor()=this
}