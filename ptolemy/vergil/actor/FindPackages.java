/* Look for class files under a directory and return package names.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.util.ExecuteCommands;

/** Look for class files under a directory and return package names.
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 5.2
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class FindPackages {
    /** Look for class files under a directory and return package names.
     *  Directories that end in adm, CVS and vendors are skipped.
     *  @param ptII The File under which to look for classes.
     *  @param executeCommands The ExecuteCommands object in which to report
     *  status.  If null, then print to standard out.
     *  @return A string of space separated package names, relative to
     *  the File named by the ptII argument.
     */
    public static String findPackages(File ptII, ExecuteCommands executeCommands) {
        StringBuffer results = new StringBuffer();
        Set directoriesSeen = new HashSet();
        Set classFilesSeen = new HashSet();
        String ptIIPath = ptII.getPath().replace('\\', '/');
        if (executeCommands == null) {
            System.out.println("Searching for .class files in " + ptIIPath);
        } else {
            executeCommands.stdout("Searching for .class files in " + ptIIPath);
        }
        _getDirectories(ptII, directoriesSeen, classFilesSeen);
        Set packages = new HashSet();
        Iterator classFiles = classFilesSeen.iterator();
        while (classFiles.hasNext()) {
            File files[] = (File[]) classFiles.next();
            for (File file : files) {
                String fullPath = file.toString().replace('\\', '/');
                String shortPath = fullPath.substring(0, file.toString()
                        .length() - 6);
                if (shortPath.startsWith(ptIIPath)) {
                    shortPath = shortPath.substring(ptIIPath.length() + 1);
                }
                shortPath = shortPath.substring(0, shortPath.lastIndexOf('/'));
                String packageName = shortPath.replace('/', '.');
                if (!packages.contains(packageName)) {
                    packages.add(packageName);
                    results.append(" " + packageName);
                }
            }
        }
        if (executeCommands == null) {
            System.out.println(results.toString());
        } else {
            executeCommands.stdout(results.toString());
        }
        return results.toString();
    }

    /** Print out any packages found under the directory named
     *  by the first argument.
     *  <p>Usage: java -classpath $PTII ptolemy.vergil.actor.FindPackages $PTII
     *  @param args An array of Strings, where the first argument is
     *  a string that names the directory under which we search for packages.
     */
    public static void main(String[] args) {
        findPackages(new File(args[0]), null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Recursively descend the filesystem.
     *  Directories that end in adm, CVS and vendors are skipped.
     *  @param directory  The top level directory.
     *  @param directoriesSeen A Set of objects of type File
     *  that name directories that have been seen.
     */
    private static void _getDirectories(File directory, Set directoriesSeen,
            Set classFilesSeen) {
        File directories[] = directory.listFiles(new DirectoryFileFilter());
        for (int i = 0; i < directories.length; i++) {
            if (!directoriesSeen.contains(directories[i])
                    && !directories[i].getName().endsWith("adm")
                    && !directories[i].getName().endsWith("CVS")
                    && !directories[i].getName().endsWith("vendors")) {
                File classFiles[] = directories[i]
                        .listFiles(new ClassFileFilter());
                classFilesSeen.add(classFiles);
                directoriesSeen.add(directories[i]);
                _getDirectories(directories[i], directoriesSeen, classFilesSeen);
            }
        }
    }

    /** Filter that returns true if the pathname ends with .class. */
    private static class ClassFileFilter implements FileFilter {
        /** Filter that returns true if the file name ends with .class.
         *  @param pathname The pathname to be checked
         *  @return true if the pathname ends with .class.
         */
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".class");
        }
    }

    /** Filter that returns true if the pathname is a directory. */
    private static class DirectoryFileFilter implements FileFilter {
        /** Filter that returns true if the pathname is a directory.
         *  @param pathname The pathname to be checked
         *  @return true if the pathname is a directory.
         */
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }

}
