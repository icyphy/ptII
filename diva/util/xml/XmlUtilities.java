/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package diva.util.xml;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * A collection of utility methods for XML-related
 * operations.
 *
 * @author Michael Shilman
 * @version $Id$
 */
public class XmlUtilities {
    /**
     * Given a file name in the current working directory,
     * complete it and turn it into a URL.
     */
    public static final String makeAbsoluteURL(String url)
        throws MalformedURLException {
        URL baseURL;
        System.out.println("orig url: " + url);

        String currentDirectory = System.getProperty("user.dir");

        String fileSep = System.getProperty("file.separator");
        String file = currentDirectory.replace(fileSep.charAt(0), '/') + '/';

        if (file.charAt(0) != '/') {
            file = "/" + file;
        }

        System.out.println("new url: " + file);
        baseURL = new URL("file", null, file);
        return new URL(baseURL, url).toString();
    }
}
