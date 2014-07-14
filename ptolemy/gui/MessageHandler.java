/* Base class for displaying exceptions, warnings, and messages.

 Copyright (c) 1999-2013 The Regents of the University of California.
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
package ptolemy.gui;

// ptolemy.gui.CancelException is deprecated, use ptolemy.util.CancelException.

///////////////////////////////////////////////////////////////////
//// MessageHandler

/**
 This is a class that is used to report errors.  It provides a
 set of static methods that are called to report errors.  However, the
 actual reporting of the errors is deferred to an instance of this class
 that is set using the setMessageHandler() method.  Normally there
 is only one instance, set up by the application, so the class is
 a singleton.  But this is not enforced.
 <p>
 This base class simply writes the errors to System.err.
 When an applet or application starts up, it may wish to set a subclass
 of this class as the message handler, to allow a nicer way of
 reporting errors.  For example, a swing application will probably
 want to report errors in a dialog box, using for example
 the derived class GraphicalMessageHandler.
 @see GraphicalMessageHandler

 @author  Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (janneck)
 @deprecated Use ptolemy.util.MessageHandler.
 */
public class MessageHandler extends ptolemy.util.MessageHandler{
    // The class body is empty so that callers use the static
    // methods in the superclass.
    // This class had duplicate code, which was confusing.
}