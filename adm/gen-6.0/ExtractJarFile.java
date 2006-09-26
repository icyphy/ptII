/* IzPack utility to extract a jar file.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

import com.izforge.izpack.util.AbstractUIProcessHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//////////////////////////////////////////////////////////////////////////
//// ExtractJarFile

/**
 Extract a jar file from within IzPack.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ExtractJarFile {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Extract a jar file into a directory.  This is a trivial
     *  implementation of the <code>jar -xf</code> command.   
     *  @param jarFileName The name of the jar file to extract
     *  @param directoryName The name of the directory.  If this argument
     *  is null, then the files are extracted in the current directory.
     */
    public static void extractJarFile(AbstractUIProcessHandler handler,
            String jarFileName, String directoryName) throws IOException, SecurityException {

        JarFile jarFile = new JarFile(jarFileName);
        Enumeration entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) entries.nextElement();
            File destinationFile = new File(directoryName, jarEntry.getName());
            if (handler != null) {
                handler.logOutput("Extracting \"" + destinationFile + "\".",
                        false);
            }
            if (jarEntry.isDirectory()) {
                if (!destinationFile.isDirectory()
                        && !destinationFile.mkdirs()) {
                    throw new IOException("Warning, failed to create "
                            + "directory for \"" + destinationFile + "\".");
                }
            } else {
                _binaryCopyStream(jarFile.getInputStream(jarEntry),
                        destinationFile);
            }
        }
    }

    /** Extract the contents of a jar file.
     *  @param args An array of arguments.  The first argument
     *  names the jar file to be extracted.  The first argument
     *  is required.  The second argument names the directory in
     *  which to extract the files from the jar file.  The second
     *  argument is optional.
     */
    public static void main(String [] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.util.FileUtilities jarFile [directory]\n"
                    + "where jarFile is the name of the jar file\n"
                    + "and directory is the optional directory in which to "
                    + "extract.");
            System.exit(2);
        }
        String jarFileName = args[0];
        String directoryName = null;
        if ( args.length >=2) {
            directoryName = args[1];
        }
        try {
            extractJarFile(null, jarFileName, directoryName);
        } catch (Throwable throwable) {
            System.err.println("Failed to extract \"" + jarFileName + "\"");
            throwable.printStackTrace();
            System.exit(3);
        }
    }

    public void run( AbstractUIProcessHandler handler, String[] args) {
        String jarFileName = args[0];
        String directoryName = null;
        if ( args.length >=2) {
            directoryName = args[1];
        }
        try {
            extractJarFile(handler, jarFileName, directoryName);    
        } catch (Throwable throwable) {
            handler.logOutput("Failed to extract \"" + jarFileName 
                    + "\" into + \"" + directoryName + "\": " + throwable,
                    true); 
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Copy files safely.  If there are problems, the streams are
     *  close appropriately.
     *  @param inputStream The input stream.
     *  @param destinationFile The destination File. 
     *  @exception IOException If the input stream cannot be created
     *  or read, or * if there is a problem writing to the destination
     *  file.
     */
    private static void _binaryCopyStream(InputStream inputStream,
            File destinationFile) throws IOException {
        // Copy the source file. 
        BufferedInputStream input = null;

        try {
            input = new BufferedInputStream(inputStream);
            
            BufferedOutputStream output = null;

            try {
                File parent = destinationFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("Failed to create directories "
                                + "for \"" + parent + "\".");
                    }
                }

                output = new BufferedOutputStream(new FileOutputStream(
                        destinationFile));

                int c;

                while ((c = input.read()) != -1) {
                    output.write(c);
                }
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                }
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        }
    }
}
