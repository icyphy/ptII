/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.xml;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A collection of utility methods for XML-related
 * operations.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class XmlUtilities {
    /**
     * Given a file name in the current working directory,
     * complete it and turn it into a URL.
     */
    public static final String makeAbsoluteURL(String url) throws MalformedURLException {
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
        return new URL(baseURL,url).toString();
    }
}


