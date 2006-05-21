/* An interface that find documentation.

 Copyright (c) 2006 The Regents of the University of California.
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
 */

package ptolemy.vergil.actor;

import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// DocApplicationSpecializer

/**
 An interface used to convert class names to URLs.  If an application
 would like more control over how documentation is found, then the
 application can implement this interface and set the
 _docApplicationSpecializer parameter in the configuration to name
 the implementation class

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.0
 @see PortSite
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface DocApplicationSpecializer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a dot separated class name, return the URL of the
     *  documentation.
     *  @param remoteDocumentationURLBase If non-null, the URL of the
     *  documentation.  Usually, this is set by reading the 
     *  _remoteDocumentationBase parameter from the configuration in the
     *  caller.
     *  @param className The dot separated class name.
     *  @return The URL of the documentation, if any.  If no documentation
     *  was found, return null.
     */
    public URL docClassNameToURL(String remoteDocumentationURLBase, 
            String className);

}
