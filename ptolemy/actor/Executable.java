/* Interface for defining how an object can be invoked.

 Copyright (c) 1997- The Regents of the University of California.
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

package pt.actors;
import pt.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// Executable
/** 
This interface defines how an object can be invoked. This should be 
implemented by classes Actor and Director
@author Mudit Goel
@version $Id$
*/
public interface Executable {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** This fires an actor and maybe invoked several times between 
     *  invocations of prefire() and postfire().
     * @exception IllegalActionException Throw by derived classes.
     */
    public void fire() throws IllegalActionException;

    /** This method should be invoked exactly once during the lifetime of
     *  an application. It maybe invoked again to restart an execution
     */	
    public void initialize();

    /** This should be invoked before the first fire()
     * @return the state of the actor. TRUE indicates that the fire() and 
     *  postfire() methods can be invoked while FALSE indicates the opposite.
     */
    public boolean prefire();
    
    /** This should be invoked after the last fire() 
     */
    public void postfire();
    
    /** This should be called at the end of the execution
     */
    public void wrapup();

}
