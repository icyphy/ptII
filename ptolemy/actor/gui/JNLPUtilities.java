/* Utilities for JNLP aka Web Start

 Copyright (c) 2002-2015 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ptolemy.util.ClassUtilities;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JNLPUtilities

/** This class contains utilities for use with JNLP, aka Web Start.

 <p>For more information about Web Start, see
 <a href="http://www.oracle.com/technetwork/java/javase/tech/index-jsp-136112.html" target="_top"><code>http://www.oracle.com/technetwork/java/javase/tech/index-jsp-136112.html</code></a>
 or <code>$PTII/doc/webStartHelp</code>

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
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
            String possibleJarURLPath = StringUtilities.substitute(
                    possibleJarURL.toExternalForm(), " ", "%20");
            if (possibleJarURLPath.contains("..")) {
                // A jar URL with a relative path.  about:checkCompleteDemos will generate these.
                String[] path = possibleJarURLPath.split("/");
                ArrayList<String> paths = new ArrayList(Arrays.asList(path));

                for (int j = 0; j < paths.size(); j++) {
                    // System.out.println(paths.size() + " paths.get(" + j + "): "+ paths.get(j) + " paths: " + paths);
                    if (paths.get(j).equals("..")) {
                        if (j > 0) {
                            //System.out.println(j-1 + " Removing: " + paths.get(j-1));
                            paths.remove(j - 1);
                        }
                        //System.out.println(j-1 + "Removing: " + paths.get(j-1));
                        paths.remove(j - 1);
                        j = j - 2;
                    }
                }
                StringBuffer newPath = new StringBuffer();
                for (String pathElement : paths) {
                    newPath.append(pathElement + "/");
                }
                possibleJarURLPath = newPath.toString().substring(0,
                        newPath.length() - 1);
                //System.out.println("JNLPUtilities: possibleJarURLPath: " + possibleJarURLPath);
                try {
                    URL jarURL = ClassUtilities
                            .jarURLEntryResource(possibleJarURLPath);
                    //System.out.println("JNLPUtilities: jarURL: " + jarURL);
                    return jarURL;
                } catch (IOException ex) {
                    throw new java.net.MalformedURLException(ex.toString());
                }
            }

            // FIXME: Could it be that we only want to convert spaces before
            // the '!/' string?
            URL jarURL = new URL(possibleJarURLPath);
            //System.out.println("JNLPUtilities: 2 jarURL: " + jarURL);
            // FIXME: should we check to see if the jarURL exists here?
//            if (jarURL == null) {
//                try {
//                    return ClassUtilities
//                            .jarURLEntryResource(possibleJarURLPath);
//                } catch (IOException ex) {
//                    throw new java.net.MalformedURLException(ex.toString());
//                }
//            }
            return jarURL;
        }

        return possibleJarURL;
    }

    /** Get the resource, if it is in a jar URL, then
     *  copy the resource to a temporary file first.
     *
     *  If the file is copied to a temporary location, then
     *  it is deleted when the process exits.
     *
     *  This method is used when jar URLs are not
     *  able to be read in by a function call.
     *
     *  If the spec refers to a URL that is a directory,
     *  then the possibly shortened spec is returned
     *  with a trailing /.  No temporary directory
     *  is created.
     *
     *  @param spec The string to be found as a resource.
     *  @return The File.
     *  @exception IOException If the jar URL cannot be saved as a temporary file.
     */
    public static File getResourceSaveJarURLAsTempFile(String spec) throws IOException {
        // System.out.println("JNLPUtilities.g.r.s.j.u.a.t.f(): start spec: " + spec);
        // If the spec is not a jar URL, then check in file system.
        // This method is used by CapeCode to find .js file resources with require().
        int jarSeparatorIndex = spec.indexOf("!/");
        File results = null;
        if (jarSeparatorIndex == -1) {
            results = new File(spec);
            if (results.exists()) {
                // System.out.println("JNLPUtilities.g.r.s.j.u.a.t.f(): start spec: " + spec + " 0 return: " + results);
                return results;
            }
        } else {
            // Strip off the text leading up to !/.
            spec = spec.substring(jarSeparatorIndex + 2);
        }

        // If the resources is not found at all, return null.
        URL url = ClassUtilities.getResource(spec);
        if (url == null) {
            // System.out.println("JNLPUtilities.g.r.s.j.u.a.t.f(): start spec: " + spec + " 0.5");
            // If we are trying to read something with a path like ./decode.js, then check the _lastSpec
            if (spec.startsWith("./") && _lastSpec != null) {
                String parentLastSpec = _lastSpec.substring(0, _lastSpec.lastIndexOf("/") + 1);
                return getResourceSaveJarURLAsTempFile(parentLastSpec + spec.substring(2));
            }
            return null;
        }

        results = null;

        // For jar urls, copy the file to a temporary
        // location that is removed when the process exits.
        if (url.toExternalForm().startsWith("jar:")) {
            // If we have already seen the url, then return
            // what was returned last time
            try {
                // We use a map of URIs because FindBugs reports:
                // "Dm: Maps and sets of URLs can be performance hogs (DMI_COLLECTION_OF_URLS)"
                // See http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html
                if (_jarURITemporaryFiles != null
                        && _jarURITemporaryFiles.containsKey(url.toURI())) {
                    results = _jarURITemporaryFiles.get(url.toURI());
                    _lastSpec = spec;
                    //System.out.println("JNLPUtilities.g.r.s.j.u.a.t.f(): start spec: " + spec + " 1 return: " + results);
                    return results;
                }
            } catch (URISyntaxException ex) {
                IOException ioException = new IOException("Failed to look up " + url + " in the cache.");
                ioException.initCause(ex);
                throw ioException;
            }
            String prefix = "";
            String suffix = "";
            int lastIndexOfSlash = spec.lastIndexOf("/");
            int lastIndexOfDot = spec.lastIndexOf(".");
            if (lastIndexOfSlash == -1) {
                if (lastIndexOfDot == -1) {
                    prefix = spec;
                } else {
                    prefix = spec.substring(0, lastIndexOfDot);
                    suffix = "." + spec.substring(lastIndexOfDot + 1);
                }
            } else {
                if (lastIndexOfDot == -1) {
                    prefix = spec.substring(lastIndexOfSlash + 1);
                } else {
                    prefix = spec.substring(lastIndexOfSlash + 1, lastIndexOfDot);
                    suffix = "." + spec.substring(lastIndexOfDot + 1);
                }
            }
            try {
                String temporaryFileName = saveJarURLAsTempFile(url.toString(),
                        prefix, suffix, null /*directory*/);
                results =  new File(temporaryFileName);
                // System.out.println("JNLPUtilities.g.r.s.j.u.a.t.f(): start spec: " + spec + " 1.5 reslts: " + results + " exists: " + results.exists());
            } catch (IOException ex) {
                // If the spec exists with a trailing /, then just
                // return that so that we can detect that it is a
                // directory.  FIXME: the directory is not actually
                // created here, which could be confusing.
                if (spec.length() > 0 && spec.charAt(spec.length()-1) != '/') {
                    URL urlDirectory = ClassUtilities.getResource(spec + "/");
                    if (urlDirectory != null) {
                        results = new File(spec + "/");
                    }
                } else {
                    results = null;
                }
            }
            if (_jarURITemporaryFiles == null) {
                _jarURITemporaryFiles = new HashMap<URI, File>();
            }
            try {
                _jarURITemporaryFiles.put(url.toURI(), results);
            } catch (URISyntaxException ex) {
                IOException ioException = new IOException("Failed to add " + url + " in the cache.");
                ioException.initCause(ex);
                throw ioException;
            }
            _lastSpec = spec;
            // System.out.println("JNLPUtilities.g.r.s.j.u.a.t.f(): start spec: " + spec + " 2 return: " + results);
            return results;
        } else {
            // If the resource is not a jar URL, try
            // creating a file.
            try {
                results = new File(url.toURI());
            } catch(URISyntaxException e) {
                results = new File(url.getPath());
            } catch(IllegalArgumentException e) {
                results = new File(url.getPath());
            }
            // System.out.println("JNLPUtilities.g.r.s.j.u.a.t.f(): start spec: " + spec + " 3 return: " + results);
            return results;
        }
    }

    /** Return true if we are running under WebStart.
     *  @return True if we are running under WebStart.
     */
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

    /** Given a jar url of the format jar:{url}!/{entry}, return
     *  the resource, if any of the {entry}.
     *  If the string does not contain <code>!/</code>, then return
     *  null.  Web Start uses jar URL, and there are some cases where
     *  if we have a jar URL, then we may need to strip off the
     *  <code>jar:<i>url</i>!/</code> part so that we can search for
     *  the {entry} as a resource.
     *
     *  @param spec The string containing the jar url.
     *  @exception IOException If it cannot convert the specification to
     *   a URL.
     *  @return the resource if any.
     *  @deprecated Use ptolemy.util.ClassUtilities#jarURLEntryResource(String)
     *  @see ptolemy.util.ClassUtilities#jarURLEntryResource(String)
     *  @see java.net.JarURLConnection
     */
    @Deprecated
    public static URL jarURLEntryResource(String spec) throws IOException {
        return ClassUtilities.jarURLEntryResource(spec);
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
     *  @exception IOException If there is a problem saving the jar URL.
     */
    public static String saveJarURLAsTempFile(String jarURLName, String prefix,
            String suffix, File directory) throws IOException {
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

        try {
            // The resource pointed to might be a pdf file, which
            // is binary, so we are careful to read it byte by
            // byte and not do any conversions of the bytes.
            FileUtilities.binaryCopyURLToFile(jarURL, temporaryFile);
        } catch (Throwable throwable) {
            // Hmm, jarURL could be referring to a directory.
            if (temporaryFile.delete()) {
                throw new IOException("Copying \"" + jarURL + "\" to \""
                                      + temporaryFile + "\" failed: " + throwable
                                      + "  Then deleting \"" + temporaryFile
                                      + "\" failed?");
            }
            Path directoryPath = directory.toPath();
            Path temporaryDirectory = Files.createTempDirectory(directoryPath, prefix);
            temporaryFile = temporaryDirectory.toFile();
            temporaryFile.deleteOnExit();
            FileUtilities.binaryCopyURLToFile(jarURL, temporaryFile);
        }
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
     *  @exception IOException If there is a problem saving the jar URL.
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
        String jarURLParentFileName = jarURLFileName.substring(0,
                jarURLFileName.lastIndexOf("/"));

        String parentEntryFileName = entryFileName.substring(0,
                entryFileName.lastIndexOf("/"));

        if (jarURLParentFileName.endsWith(parentEntryFileName)
                && jarURLParentFileName.startsWith("jar:file:/")) {
            // The top level directory, probably $PTII
            String jarURLTop = jarURLParentFileName.substring(
                    9,
                    jarURLParentFileName.length()
                    - parentEntryFileName.length());

            File temporaryFile = new File(jarURLTop, entryFileName);

            // If the file exists, we assume that it is the right one.
            // FIXME: we could do more here, like check for file sizes.
            if (!temporaryFile.exists()) {
                FileUtilities.binaryCopyURLToFile(jarURL, temporaryFile);
            }

            return temporaryFile.toString();
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Lookup a jarURLName as a resource.
    private static URL _lookupJarURL(String jarURLName) throws IOException {
        // We call jarURLEntryResource here so that we get a URL
        // that has the right jar file associated with the right entry.
        URL jarURL = jarURLEntryResource(jarURLName);

        if (jarURL == null) {
            jarURL = ClassUtilities.getResource(jarURLName);
        }

        if (jarURL == null) {
            throw new FileNotFoundException("Could not find '" + jarURLName
                    + "'");
        }

        return jarURL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The map of URIs to Files used by
     * getResourceSaveJarURIAsTempFile().
     */
    private static Map<URI,File> _jarURITemporaryFiles;

    private static String _lastSpec = null;
}
