/* Utilities used to manipulate files

 Copyright (c) 2004 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.util;

// Note that classes in ptolemy.util do not depend on any
// other ptolemy packages.

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// FileUtilities
/**
A collection of utilities for manipulating files
These utilities do not depend on any other ptolemy.* packages.

@author Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 4.0
*/
public class FileUtilities {

    /** Instances of this class cannot be created.
     */
    private FileUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Copy sourceURL to destinationFile without doing any byte conversion.
     *  @param sourceURL The source URL
     *  @param destinationFile The destination File.
     */   
    public static void binaryCopyURLToFile(URL sourceURL,
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
                        System.out.println("Ignoring failure to close stream "
                                + "on " + sourceURL);
                        throwable.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (Throwable throwable) {
                    System.out.println("Ignoring failure to close stream "
                            + "on " + destinationFile);
                    throwable.printStackTrace();
                }
            }
        }
    }
}


