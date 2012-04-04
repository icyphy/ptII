package org.ptolemy.ptango.lib;
import java.net.URI;

import javax.servlet.http.HttpServlet;


/* An interface which actors implement to handle HTTP requests.

 Copyright (c) 1997-2011 The Regents of the University of California.
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

/** An interface which actors implement to handle HTTP requests.  The interface
 *  allows the relative path for the HTTP request to be set and obtained, 
 *  and provides a servlet to handle requests.
 *  
 *  Note that "HttpService" is also the name of an OSGi interface.  If OSGi is 
 *  incorporated in Ptolemy in the future, we might want to rename this 
 *  interface.
 *  http://www.osgi.org/javadoc/r4v42/org/osgi/service/http/HttpService.html
 *  
 *  @author ltrnc
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.WebServer
 */

public interface HttpService {

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Returns the relative path that this HttpService is mapped to. 
     * 
     * @return The relative path that this HttpService is mapped to.
     */
    public URI getRelativePath();
    
    /** Returns an HttpServlet which is used to handle requests that
     *  arrive at the given path.
     * 
     * @return An HttpServlet to handle requests. 
     */
    public HttpServlet getServlet();
    
    /** Set the relative path that this HttpService is mapped to.
     * 
     * @param path The relative path that this HttpService is mapped to.
     */
    public void setRelativePath(URI relativePath);
}