/* Utilities for JNLP aka Web Start

 Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.util.StringUtilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// JNLPUtilities
/** This class contains utilities for use with JNLP, aka Web Start.

<p>For more information about Web Start, see
<a href="http://java.sun.com/products/javawebstart/" target="_top"><code>http://java.sun.com/products/javawebstart</a></code>
or <code>$PTII/doc/webStartHelp</code>

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
@see Configuration
*/
public class JNLPUtilities {

    /** Instances of this class cannot be created.
     */
    private JNLPUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Canonicalize a jar URL.  If the possibleJarURL argument is a
     *  jar URL (that is, it starts with 'jar:'), then convert any
     *  space characters to %20.  If the possibleJarURL argument is
     *  not a jar URL, then return the possibleJarURL argument.
     *  @param possibleJarURL  A URL that may or may not be a jar URL
     *  @return either the original possibleJarURL or a canonicalized
     *  jar URL
     *  @exception java.net.MalformedURLException If new URL() throws it.
     */
    public static URL canonicalizeJarURL(URL possibleJarURL)
            throws java.net.MalformedURLException {
        // This method is needed so that under Web Start we are always
        // referring to files like intro.htm with the same URL.
        // The reason is that the Web Start under Windows is likely
        // to be in c:/Documents and Settings/username
        // so we want to always refer to the files with the same URL
        // so as to avoid duplicate windows

        if (possibleJarURL.toExternalForm().startsWith("jar:")) {
            // FIXME: Could it be that we only want to convert spaces before
            // the '!/' string?
            URL jarURL =
                new URL(StringUtilities
                        .substitute(possibleJarURL.toExternalForm(),
                                " ", "%20"));
            // FIXME: should we check to see if the jarURL exists here?
            return jarURL;
        }
        return possibleJarURL;
    }

    /** Return true if we are running under WebStart */
    public static boolean isRunningUnderWebStart() {
        try {
            // NOTE: getProperty() will probably fail in applets, which
            // is why this is in a try block.
            String javaWebStart = System.getProperty("javawebstart.version");
            if (javaWebStart != null) {
                return true;
            }
        } catch (SecurityException security) {
            // Ignored
        }
        return false;
    }

    /** Given a jar url of the format jar:<url>!/{entry}, return
     *  the resource, if any of the {entry}.
     *  If the string does not contain <code>!/</code>, then return null.
     *  Web Start uses jar URL, and there are some cases where
     *  if we have a jar URL, then we may need to strip off the jar:<url>!/
     *  part so that we can search for the {entry} as a resource.
     *
     *  @param spec The string containing the jar url.
     *  @exception IOException If it cannot convert the specification to
     *   a URL.
     *  @see java.net.JarURLConnection
     */
    public static URL jarURLEntryResource(String spec) throws IOException {
        // At first glance, it would appear that this method could appear
        // in specToURL(), but the problem is that specToURL() creates
        // a new URL with the spec, so it only does further checks if
        // the URL is malformed.  Unfortunately, in Web Start applications
        // the URL will often refer to a resource in another jar file,
        // which means that the jar url is not malformed, but there is
        // no resource by that name.  Probably specToURL() should return
        // the resource after calling new URL().
        int jarEntry = spec.indexOf("!/");
        if (jarEntry == -1) {
            return null;
        } else {
            try {
                // !/ means that this could be in a jar file.
                String entry = spec.substring(jarEntry + 2);
                // We might be in the Swing Event thread, so
                // Thread.currentThread().getContextClassLoader()
                // .getResource(entry) probably will not work.
                Class refClass = Class.forName("ptolemy.kernel.util.NamedObj");
                URL entryURL = refClass.getClassLoader().getResource(entry);
                return entryURL;
            } catch (Exception ex) {
                throw new IOException("File not found: " + spec + ": "
                        + ex);
            }
        }
    }

    /** Given a jar URL, read in the resource and save it as a file.
     *  The file is created using the prefix and suffix in the
     *  directory referred to by the directory argument.  If the
     *  directory argument is null, then it is saved in the platform
     *  dependent temporary directory.
     *  The file is deleted upon exit.
     *  @see java.io.File#createTempFile(java.lang.String, java.lang.String, java.io.File)
     *  @param jarURLName The name of the jar URL to read.  jar URLS start
     *  with "jar:" and have a "!/" in them.
     *  @param prefix The prefix used to generate the name, it must be
     *  at least three characters long.
     *  @param suffix The suffix to use to generate the name.  If the
     *  suffix is null, then the suffix of the jarURLName is used.  If
     *  the jarURLName does not contain a ".", then ".tmp" will be used
     *  @param directory The directory where the temporary file is
     *  created.  If directory is null then the platform dependent
     *  temporary directory is used.
     *  @return the name of the temporary file that was created
     */
    public static String saveJarURLAsTempFile(String jarURLName,
            String prefix,
            String suffix,
            File directory)
            throws IOException {

        URL jarURL = _lookupJarURL(jarURLName);
        jarURLName = jarURL.toString();

        // File.createTempFile() does the bulk of the work for us,
        // we just check to see if suffix is null, and if it is,
        // get the suffix from the jarURLName.
        if (suffix == null) {
            // If the jarURLName does not contain a ".", then we pass
            // suffix = null to File.createTempFile(), which defaults
            // to ".tmp"
            if (jarURLName.lastIndexOf('.') != -1) {
                suffix = jarURLName.substring(jarURLName.lastIndexOf('.'));
            }
        }

        File temporaryFile = File.createTempFile(prefix, suffix, directory);
        temporaryFile.deleteOnExit();

        _binaryCopyURLToFile(jarURL, temporaryFile);

        return temporaryFile.toString();
    }

    /** Given a jar URL, read in the resource and save it as a file in
     *  a similar directory in the classpath if possible.  In this
     *  context, by similar directory, we mean the directory where
     *  the file would found if it was not in the jar url.
     *  For example, if the jar url is
     *  jar:file:/ptII/doc/design.jar!/doc/design/design.pdf
     *  then this method will read design.pdf from design.jar
     *  and save it as /ptII/doc/design.pdf.
     *
     *  @param jarURLName The name of the jar URL to read.  jar URLS start
     *  with "jar:" and have a "!/" in them.
     *  @return the name of the file that was created or
     *  null if the file cannot be created
     */
    public static String saveJarURLInClassPath(String jarURLName)
            throws IOException {

        URL jarURL = _lookupJarURL(jarURLName);
        jarURLName = jarURL.toString();

        int jarSeparatorIndex = jarURLName.indexOf("!/");
        if (jarSeparatorIndex == -1) {
            // Could be that we found a copy of the file in the classpath.
            return jarURLName;
        }

        // If the entry directory matches the jarURL directory, then
        // write out the file in the proper location.
        String jarURLFileName = jarURLName.substring(0, jarSeparatorIndex);
        String entryFileName = jarURLName.substring(jarSeparatorIndex + 2);

        // We assume / is the file separator here because URLs
        // _BY_DEFINITION_ have / as a separator and not the Microsoft
        // non-conforming hack of using a backslash.

        String jarURLParentFileName =
            jarURLFileName.substring(0, jarURLFileName.lastIndexOf("/"));

        String parentEntryFileName =
            entryFileName.substring(0, entryFileName.lastIndexOf("/"));

        if (jarURLParentFileName.endsWith(parentEntryFileName)
                && jarURLParentFileName.startsWith("jar:file:/")) {
            // The top level directory, probably $PTII
            String jarURLTop =
                jarURLParentFileName.substring(10,
                        jarURLParentFileName.length()
                        - parentEntryFileName.length());

            File temporaryFile = new File(jarURLTop, entryFileName);

            // If the file exists, we assume that it is the right one.
            // FIXME: we could do more here, like check for file sizes.
            if ( !temporaryFile.exists()) {
                _binaryCopyURLToFile(jarURL, temporaryFile);
            }
            return temporaryFile.toString();
        }
        return null;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////

    // Copy sourceURL to destinationFile without doing any byte conversion.
    private static void _binaryCopyURLToFile(URL sourceURL,
            File destinationFile)
            throws IOException {

        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            input = new BufferedInputStream(sourceURL.openStream());

            output = new BufferedOutputStream(
                        new FileOutputStream(destinationFile));

            // The resource pointed to might be a pdf file, which
            // is binary, so we are careful to read it byte by
            // byte and not do any conversions of the bytes.

            int c;
            while (( c = input.read()) != -1) {
                output.write(c);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable throwable) {
                    // Ignore, not much we can do
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (Throwable throwable) {
                    // Ignore, not much we can do
                }
            }
        }
    }


    // Lookup a jarURLName as a resource.
    private static URL _lookupJarURL(String jarURLName) throws IOException {
        // We call jarURLEntryResource here so that we get a URL
        // that has the right jar file associated with the right entry.
        URL jarURL = jarURLEntryResource(jarURLName);
        if (jarURL == null) {
            jarURL = Thread.currentThread()
                .getContextClassLoader().getResource(jarURLName);
        }

        if (jarURL == null) {
            throw new FileNotFoundException("Could not find '"
                    + jarURLName + "'");
        }
        return jarURL;
    }
}

