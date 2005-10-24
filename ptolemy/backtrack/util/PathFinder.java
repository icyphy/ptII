/* A tool for path searching and class path creation.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.backtrack.util;

import ptolemy.util.StringUtilities;

import java.io.File;
import java.io.FileFilter;

//////////////////////////////////////////////////////////////////////////
//// PathFinder

/**
 A tool to search paths and set up class paths. It provides functions to
 search paths for certain files, and set up customized class paths from
 which class can be loaded with {@link LocalClassLoader}.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PathFinder {
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Get all the Java source files in a path.
     *
     *  @param path The path to be searched.
     *  @param subdirs If <tt>true</tt>, sub-directories of the path are
     *   also searched.
     *  @return All the Java files found. If no Java file is found, an
     *   array with 0 element is returned.
     */
    public static File[] getJavaFiles(String path, boolean subdirs) {
        File file = new File(path);
        String postfix = ".java";

        if (file.isFile()) {
            if (file.getName().endsWith(postfix)) {
                return new File[] { file };
            } else {
                return new File[0];
            }
        } else if (!subdirs) {
            return file.listFiles(new PostfixFilter(".java"));
        } else {
            File[] Directories = file.listFiles(new DirectoryFilter());
            File[][] filesInSubdirs = new File[Directories.length][];
            int nTotal = 0;

            for (int i = 0; i < Directories.length; i++) {
                filesInSubdirs[i] = getJavaFiles(Directories[i].getPath(), true);
                nTotal += filesInSubdirs[i].length;
            }

            File[] files = file.listFiles(new PostfixFilter(".java"));
            nTotal += files.length;

            File[] result = new File[nTotal];
            int pos = 0;

            for (int i = 0; i < filesInSubdirs.length; i++) {
                int length = filesInSubdirs[i].length;
                System.arraycopy(filesInSubdirs[i], 0, result, pos, length);
                pos += length;
            }

            System.arraycopy(files, 0, result, pos, files.length);

            return result;
        }
    }

    /** Return the class paths containing the root of the Ptolemy tree,
     *  and the Jar files in sub-directories <tt>lib/</tt>,
     *  <tt>vendors/sun/commapi/</tt> and <tt>vendors/sun/jxta</tt>.
     *
     *  @return The class paths.
     */
    public static String[] getPtClassPaths() {
        String PTII = getPtolemyPath();

        if ((PTII == null) || PTII.equals("")) {
            return new String[0];
        }

        String[] subdirs = new String[] { "lib", "vendors/sun/commapi",
                "vendors/sun/jxta" };
        File[][] files = new File[subdirs.length][];
        int totalNumber = 0;

        for (int i = 0; i < subdirs.length; i++) {
            files[i] = new File(PTII + subdirs[i]).listFiles(new PostfixFilter(
                    ".jar"));
            totalNumber += files[i].length;
        }

        String[] classPaths = new String[totalNumber + 1];
        classPaths[0] = getPtolemyPath();

        int currentNumber = 1;

        for (int i = 0; i < files.length; i++) {
            for (int j = 0; j < files[i].length; j++) {
                classPaths[currentNumber++] = files[i][j].getPath();
            }
        }

        return classPaths;
    }

    /** Get the Ptolemy path. If the Ptolemy path is set with {@link
     *  #setPtolemyPath(String)}, that path is returned. If it is not set,
     *  <tt>ptolemy.ptII.dir</tt> system property is used (see {@link
     *  StringUtilities#getProperty(String)}). If the property does not exist,
     *  simply "./" is returned, assuming that the current path contains a
     *  working version of Ptolemy (may not be correct).
     *
     *  @return The Ptolemy path.
     *  @see #setPtolemyPath(String)
     */
    public static String getPtolemyPath() {
        if (_ptolemyPath == null) {
            String PTII = StringUtilities.getProperty("ptolemy.ptII.dir");

            if (PTII != null) {
                _ptolemyPath = PTII;

                if (!_ptolemyPath.endsWith("" + File.separatorChar)
                        && !_ptolemyPath.endsWith("/")
                        && !_ptolemyPath.endsWith("\\")) {
                    _ptolemyPath += File.separatorChar;
                }
            } else {
                _ptolemyPath = "./";
            }
        }

        return _ptolemyPath;
    }

    /** Set the Ptolemy path.
     *
     *  @param path
     *  @see #getPtolemyPath()
     */
    public static void setPtolemyPath(String path) {
        _ptolemyPath = path;

        if ((_ptolemyPath != null) && !_ptolemyPath.equals("")
                && !_ptolemyPath.endsWith("" + File.separatorChar)
                && !_ptolemyPath.endsWith("/") && !_ptolemyPath.endsWith("\\")) {
            _ptolemyPath += File.separatorChar;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       nested classes                      ////
    //////////////////////////////////////////////////////////////////////////
    //// JarFileFilter

    /**
     *  Filter out all the files in a directory, except for those ending with
     *  the given postfix.
     *
     *  @author Thomas Feng
     *  @version $Id$
     *  @since Ptolemy II 4.1
     *  @Pt.ProposedRating Red (tfeng)
     */
    public static class PostfixFilter implements FileFilter {
        /** Construct a filter with a postfix.
         *
         *  @param postfix The postfix.
         */
        PostfixFilter(String postfix) {
            _postfix = postfix;
        }

        /** Accept only files with names ending with the given postfix.
         *
         *  @param file The file to be inspected.
         *  @return <tt>true</tt> if the file name ends with the given postfix.
         */
        public boolean accept(File file) {
            return file.getName().endsWith(_postfix);
        }

        /** The postfix.
         */
        private String _postfix;
    }

    //////////////////////////////////////////////////////////////////////////
    //// DirectoryFilter

    /**
     Filter out all the files in a directory, except for sub-directories.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class DirectoryFilter implements FileFilter {
        /** Accept only directories.
         *
         *  @param file The file to be inspected.
         *  @return <tt>true</tt> if the file corresponds to a directory.
         */
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The Ptolemy path.
     */
    private static String _ptolemyPath = null;
}
