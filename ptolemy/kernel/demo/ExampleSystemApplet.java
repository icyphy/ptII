/* ExampleSystem.java constructs a test hierachical graph using the pt.kernel classes.

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

package pt.kernel.demo;
import pt.kernel.*;
import pt.kernel.util.*;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ExampleSystem
/** 
ExapmleSystem constructs a hierachical graph as shown in 
Ptolemy 2 design document, Figure 8 
The graph has 10 entities, 14 ports, and 12 relations.
The main function also returns the results of some key functions of
ComponentRelation and ComponentPort.
See Ptolemy 2 design document, Figure 11 
@author Jie Liu
@version $Id$
*/
public class ExampleSystemApplet extends ExampleApplet 
{
    /** Construct the graph. */	
    public ExampleSystemApplet()
            throws IllegalActionException, NameDuplicationException {
        super();
        _exsys = new ExampleSystem();
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         public methods                          ////

    /** Action when click on the button
    *  Print all results in the text area. Override ExampleApplet.buttonAction
    *  @see pt.kernel.demo.Figure8.ExampleApplet#buttonAction()
    *  @see java.awt.buttton
    *  @param java.awt.event
    */	
    public boolean buttonAction(java.awt.Event event) {
        clearTextArea();
        printInTextArea(_exsys.toString());
        return true;
    }

    /** Initialize the applet. */ 
    public void init() {
        super.init();
        addNotify();
        createAppletForm();
    }
    
    /** Create the applet form. */
    public void createAppletForm() {
        try {
            create();
        } catch ( java.lang.Exception ex ) {}
    }

    /** Create and Example System window standalone application. */
    public static void main(String args[])
            throws NameDuplicationException,
            IllegalActionException {
        ExampleSystemApplet exsys = new ExampleSystemApplet();
        ExampleFrame f = new ExampleFrame();
        f.setResizable(true);
        f.add(exsys);
        f.addNotify();
        java.awt.Insets insets = f.insets(); 
        exsys.init();
        exsys.move(insets.left,insets.top);
        f.resize(exsys.preferredSize());
        exsys.resize(exsys.preferredSize());
        f.show();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    
    // Example System containing Entities, Relations and Ports.
    ExampleSystem _exsys;
}

