/* Singleton class for displaying exceptions.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.vergil;

import java.awt.Component;

import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// ExceptionHandler
/**
This is a static class that is used to report errors.
When an applet or application starts up, it should call setContext()
to specify a component with respect to which the display window
should be created.  This ensures that if the application is iconfied
or deiconified, that the display window goes with it. If the context
is not specified, then the display window is centered on the screen,
but iconifying and deiconifying may not work as desired.

@author  Edward A. Lee
@version $Id$
*/
public class ExceptionHandler {
    
    // This constructor is private because the class is a singleton.
    private ExceptionHandler() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the component with respect to which the display window
     *  should be created.  This ensures that if the application is
     *  iconfied or deiconified, that the display window goes with it.
     *  @param context The component context.
     */
    public static void setContext(Component context) {
        _context = context;
    }

    /** Show the specified message and exception information.
     *  @param message The message.
     *  @param exception The exception.
     */
    public static void show(String message, Exception exception) {
	GUIUtilities.showException(_context, exception, message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The context.
    private static Component _context = null;
}
