/* An attribute that identifies the URI from which the container was read.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.attributes;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;

//////////////////////////////////////////////////////////////////////////
//// URIAttribute
/**
An attribute that identifies the URI from which the container was read.
This attribute is not persistent by default.  That is, it exports no
MoML description. This makes sense because it should be set by the
code that reads the container's specification.  It is also a singleton,
meaning that it will replace any previous attribute that has the same
name and is an instance of the base class, SingletonAttribute.
<p>
In most cases, this URI will specify a URL.  The difference between
a URL and a URI is that a URI is unevaluated. That is, it is a string
representation of a resource, without any assurance or indication of a
file, stream, or other associated network resource.  To access a URI,
it is common to create a URL from its specification.

<p>Unfortunately, URLs are not necessarily valid URIs.  For example, a
URL that has a space in it is not a valid URI, the space must be
quoted (converted) to <code>%20</code>.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class URIAttribute extends SingletonAttribute {

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public URIAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the URI from which the specified model was read,
     *  or null if there is no such URI.
     *  This is obtained by finding a URIAttribute in the first
     *  container above this attribute in the hierarchy that has
     *  such an attribute.  Note that this URI may represent a
     *  file on the local filesystem, in which case it will use
     *  the "file" scheme.
     *  @return A URI, or null if none can be found.
     */
    public static URI getModelURI(NamedObj container) {
        // Search up the tree for this attribute.
        URIAttribute modelURI = null;
        while (container != null && modelURI == null) {
            try {
                modelURI = (URIAttribute)container.getAttribute(
                        "_uri", URIAttribute.class);
            } catch (IllegalActionException ex) {
                // An attribute was found with name "_uri", but it is not
                // an instance of URIAttribute.  Continue the search.
                modelURI = null;
            }
            container = (NamedObj)container.getContainer();
        }
        if (modelURI != null) return modelURI.getURI();
        else return null;
    }

    /** Get the URI that has been set by setURI(),
     *  or null if there is none.
     *  @return The URI.
     */
    public URI getURI() {
        return _value;
    }

    /** Get a URL representation of the URI that has been set by setURI(),
     *  or null if there is none.  For this to succeed, it is necessary
     *  that the URI be absolute or an IllegalArgumentException will be
     *  thrown.
     *  @return A new URL.
     *  @exception MalformedURLException If the URI cannot be converted to
     *   a URL.
     *  @exception IllegalArgumentException If the URI is not absolute.
     */
    public URL getURL() throws MalformedURLException {
        // Warning, this method used to call java.net.URI.toURL(), which has
        // a bug with jar urls. If we have a jar url, (for example
        // jar:file:/C:/foo.jar!/intro.htm) then the java.net.URI toURL()
        // method will return a URL like jar:, which is missing the file: part
        // This causes problems with Web Start.
        //
        // BTW - Here is how to replicate the problem with
        // java.net.URI.toURL() in ptjacl:
        // % set uri [java::new java.net.URI "jar:file:/C:/foo.jar!/intro.htm"]
        // java0x1
        // % set url [$uri toURL]
        // java0x2
        // % $url toString
        // jar:
        // The above should be jar:file:/C:/foo.jar!/intro.htm

        if (_value == null) {
            return null;
        }
        return new URL(_value.toString());
    }

    /** Set the value of the URI, and call the attributeChanged() method
     *  of the container.
     *  @param uri The new URI.
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container.
     */
    public void setURI(URI uri) throws IllegalActionException {
        _value = uri;
        NamedObj container = (NamedObj)getContainer();
        if (container != null) {
            container.attributeChanged(this);
        }
    }

    /** Set the value of the URI by specifying a URL,
     *  and call the attributeChanged() method of the container.
     *  @param url The URL.
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container.
     */
    public void setURL(URL url) throws IllegalActionException {
        try {
            _value = new URI(url.toExternalForm());
        } catch (URISyntaxException ex) {
            // Unfortunately, URLs are not necessarily valid URIs.
            // For example, a URL that has a space in it is not
            // a valid URI, the space must be quoted (converted) to %20
            try {
                _value = new URI(url.getProtocol(), url.getFile(),
                        url.getRef());
            } catch (URISyntaxException ex2) {
                // Should not occur because a URL is a valid URI.
                throw new InternalErrorException(this, ex2,
                        "Error constructing URI from " + url);
            }
        }
        NamedObj container = (NamedObj)getContainer();
        if (container != null) {
            container.attributeChanged(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The value.
    private URI _value;
}
