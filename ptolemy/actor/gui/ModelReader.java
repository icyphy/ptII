/* Interface for objects that can read models from an input stream.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// ModelReader
/**
Interface for objects that can read models from an input stream.
An application will typically have one object that implements this
interface, and will register that object with the ModelDirectory.
Then whenever the occasion arises to create a model by reading a file
or a URL, the actual reading is delegated to the ModelDirectory, which
will delegate to the registered object that implements this interface.
Typically, the application class itself will implement this interface.
For example, MoMLApplication and PtolemyApplication implement this
interface.

@author Edward A. Lee
@version $Id$
@see ModelDirectory
@see MoMLApplicatoin
@see PtolemyApplication
*/
public interface ModelReader {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the specified input URL.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input.
     *  @param key The key to use to uniquely identify the model.
     *  @exception IOException If the stream cannot be read.
     */
    public void read(URL base, URL in, Object key) throws IOException;
}
