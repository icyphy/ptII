/* An attribute that specifies a file or URL.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

package ptolemy.kernel.attributes;

import ptolemy.kernel.util.*;
import ptolemy.kernel.attributes.URLAttribute;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


//////////////////////////////////////////////////////////////////////////
//// FileAttribute
/**
This is an attribute that specifies a file or URL.  The value of this
attribute, accessed by getExpression(), is a string that names a file
or URL.  The key method is the asURL() method, which converts the file
or URL specification into a java.net.URL object.  It first attempts to
directly use the file name to identify a readable file, and if it finds
one, returns that file as a URL.  If this fails, then it tries to open
the file relative to the URL of the first container above this one
that has a URLAttribute.  If there is no such URLAttribute, then it
tries to open the file relative to the classpath.  If that fails, then it
throws an exception.
<p>
@author Edward A. Lee
@version $Id$
*/
public class FileAttribute extends StringAttribute {

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FileAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the file as a URL.  This method first attempts to directly
     *  use the file name to identify a readable file, and if it finds one,
     *  returns that file as a URL.  If this fails, then it tries to open
     *  the file relative to the URL of the first container above this one
     *  that has a URLAttribute.  If there is no such base URL,
     *  then it tries to open the file relative to the classpath.
     *  If that fails, then it throws an exception.
     *  @return A URL, or null if no file name or URL has been specified.
     *  @exception IllegalActionException If the file cannot be read.
     */
    public URL asURL() throws IllegalActionException {
        String name = getExpression();
        if (name == null || name.trim().equals("")) {
            return null;
        }
        File file = new File(name);
        if (file.exists()) {
            if (!file.canRead()) {
                throw new IllegalActionException(this,
                "Cannot read file: " + name);
            }
            try {
                return file.toURL();
            } catch (MalformedURLException ex) {
                throw new IllegalActionException(this,
                "Cannot open file: " + ex.toString());
            }
        } else {
            // Try relative to the URLAttribute.
            // Search up the tree for this attribute.
            URL modelURL = getModelURL();
            if (modelURL != null) {
                try {
                    // Try an absolute URL
                    return new URL(modelURL, name);
                } catch (MalformedURLException e) {
                    throw new IllegalActionException(this,
                    "Unable to open as a file or URL: " + name);
                }
            }

            // NOTE: This doesn't seem right.  This code will never be
            // reached if there is a URLAttribute.  But there seems to
            // be no way to decide between these.  It won't work
            // to try to open the URL, because it is premature.
            // This method gets called when the model is opened,
            // not when it is run.  We do not want to require a net
            // connection to be present to open a model that refers
            // to a URL.

            // Try relative to classpath.
            URL result = getClass().getClassLoader().getResource(name);
            if (result != null) {
                return result;
            } else {
                try {
                    // Try an absolute URL
                    return new URL(name);
                } catch (MalformedURLException e) {
                    throw new IllegalActionException(this,
                    "Unable to open as a file or URL: " + name);
                }
            }
        }
    }

    /** Return the URL from which the model that
     *  contains this attribute was read, or null if there is none.
     *  This is obtained by finding a URLAttribute in the first
     *  container above this attribute in the hierarchy that has
     *  such an attribute.  Note that this URL may represent a
     *  file on the local filesystem, in which case it will use
     *  the "file" protocol.
     *  @return A URL, or null if none can be found.
     */
    public URL getModelURL() {
        // Search up the tree for this attribute.
        URLAttribute modelURL = null;
        NamedObj container = this;
        while (container != null && modelURL == null) {
            try {
                modelURL = (URLAttribute)container.getAttribute(
                       "_url", URLAttribute.class);
            } catch (IllegalActionException ex) {
                // An attribute was found with name "_url", but it is not
                // an instance of URLAttribute.  Continue the search.
                modelURL = null;
            }
            container = (NamedObj)container.getContainer();
        }
        if (modelURL != null) return modelURL.getURL();
        else return null;
    }
}
