/* One line description of file.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/
/* This class is the "glue" for all the elements needed by the debugger
 * It contains a DbgController for controlling the execution of a model,
 * a DebuggerUI to get the user's command and a reference to the current
 * Vergil document. It is created through the debugger menu in 
 * PtolemyPackage or PtolemyModule.
 */

package ptolemy.vergil.debugger;

import diva.gui.*;
import ptolemy.vergil.*;
import ptolemy.vergil.debugger.MMI.*;


//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
Description of the class
@author yourname
@version $Id$
@see classname
@see full-classname
*/
public class Pdb {

    ///////////////////////////////////////////////////////////
    //      Constructors
    /** Constructor
     * @see ptolemy.vergil.debugger.Pdb#Pdb()
     */
    public Pdb() {
    	_application = null;
    	_controller = new DbgController(this);
    	_mmi = new DebuggerUI(this);
    	_mmi.setTitle("Pdb");
    	_mmi.setSize(450, 260);
    	_mmi.setVisible(true);
    }   
    
    /** Constructor
     * @see ptolemy.vergil.debugger.Pdb#Pdb(VergilApplication application) 
     * @param application a reference to the current application
     */
    public Pdb(VergilApplication application) {
	_application = application;
	_controller = new DbgController(this);
	_mmi = new DebuggerUI(this);
	_mmi.setTitle("Pdb");
	_mmi.setSize(450, 260);
	_mmi.setVisible(true);
    }

    /////////////////////////////////////////////////////////
    ////   Public methods
     /** Return a reference to the DbgController that governs the execution
     * @see ptolemy.vergil.debugger.Pdb#getDbgController()
     * @return a reference to the DbgController that governs the execution
     */
    public DbgController getDbgController() {
	return _controller;
    }

    /** Return a reference to the UI
     * @see ptolemy.vergil.debugger.Pdb#getDebuggerUI()
     * @return a reference to the UI
     */
    public DebuggerUI getDebuggerUI() {
	return _mmi;
    }

    /** Return a reference to the current Vergil application
     * @see ptolemy.vergil.debugger.Pdb#getVergil()
     * @return a reference to the current Vergil application
     */
    public VergilApplication getVergil() {
	if (_application == null) System.out.println("getApplication returns null");
	return _application;
    }

    //////////////////////////////////////////////////////////////
    /// Private members
    // a link to the DbgController 
    private DbgController _controller;

    // a link to DebuggerUI 
    private DebuggerUI _mmi;

    // a link to the current Vergil application 
    private VergilApplication _application;
}
