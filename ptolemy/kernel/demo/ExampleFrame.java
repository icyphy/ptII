/* ExampleApplet.java is a applet class which cotains a run button and a text area

 Copyright (c) 1997 The Regents of the University of California.
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


package ptolemy.kernel.demo;

//////////////////////////////////////////////////////////////////////////
//// ExampleFrame
/** 
ExapmleApplet is an frame contains the ExampleApplet.
It's for the conversion of an applet to an application.
@author Jie Liu
@version $Id$
@see java.lang.applet
@see ExampleFrame
*/
public class ExampleFrame extends java.awt.Frame
{
    /** Constructor  */	
    public ExampleFrame() {
        super();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Event handling method for the frame. The WINDOW_DESTROY event is
     *  handled. All the other events are passed to the applet inside.
     */	
    public boolean handleEvent(java.awt.Event event) {
        Object pEvtSource = event.target;
        if( pEvtSource == this && event.id == java.awt.Event.WINDOW_DESTROY ) {
            hide();
            dispose();
            System.exit( 0 );
            return false;
        } else {
            return super.handleEvent( event );
        }
    }
}

