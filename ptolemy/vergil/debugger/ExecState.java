/* A data structure that store the state of execution of a director

 Copyright (c) 1999-2000 SUPELEC.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL SUPELEC BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
 OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF SUPELEC HAS BEEN ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 SUPELEC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED
 TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND
 SUPELEC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (frederic.boulanger@supelec.fr)
@AcceptedRating Red 
*/

package ptolemy.vergil.debugger;

//////////////////////////////////////////////////////////////////////////
//// ExecState 
/**
This class is used to store the state of execution of a DbgDirector
it stores the next method of an actor that will be executed and the last 
user command entered through the debuggerUI. It is initialized with 
_dbgCommand = "go".
Each Director has its own ExecState that is passed to DbgController
each time before calling an actor's method among prefire(), fire(), 
or postfire().

@author SUPELEC team
@version $Id$
*/
public class ExecState {
    
    ////////////////////////////////////////////////////////////////////
    /////     Constructor
    /** 
     * Create a new execution state with the debug command set to "go".
     */
    public ExecState() {
	_dbgCommand = "go";
    }

    ///////////////////////////////////////////////////////////////////
    /// Public methods

    /** Set the last user's command entered through the DbgController.
     * @param s A string that contains the debug command.
     */
    public void setdbgCommand(String s) {
	if (s.equals("resume"))
	    _dbgCommand = "resume";
	else if (s.equals("pause"))
	    _dbgCommand = "pause";
	else if (s.equals("step"))
	    _dbgCommand = "step";
	else if (s.equals("stepin"))
	    _dbgCommand = "stepin";
	else if (s.equals("microstep"))
	    _dbgCommand = "microstep";
	else if (s.equals("stepout"))
	    _dbgCommand = "stepout";
	else 
	    /* send error message */
	    ;
    }

    /** Sets the next method to be executed.
     * @param s A string that contains the name of the next method to execute.
     */
    public void setnextMethod(String s) {
	if (s.equals("prefire"))  
	    _nextMethod = "prefire";
	else if (s.equals("fire"))
	    _nextMethod = "fire";
	else if (s.equals("postfire")) 
	    _nextMethod = "postfire";
        else if (s.equals("postpostfire"))
	    _nextMethod = "postpostfire";
	else
	    /* send error message */
	    ;
    }

    /** Return the last debug command.
     * @return a string containing the last command
     */
    public String getdbgCommand() {
	return _dbgCommand;
    }

     /** Return the next method to execute.
     * @return a string that contains the next method to execute
     */
    public String getnextMethod() {
	return _nextMethod;
    }

    ////////////////////////////////////////////////////////////
    //////// Private members
    // A variable that contains the name of the next "executable" method
    //  to call : prefire, fire, postfire.
    private String _nextMethod;
    
    // A variable that contains the last user command : resume, paused, 
    //  step, stepin, step, stepout.
    private String _dbgCommand;
}
