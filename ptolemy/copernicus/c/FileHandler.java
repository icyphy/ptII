/*

A class that takes care of common File I/O functions.

Copyright (c) 2001-2005 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.


*/
package ptolemy.copernicus.c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;


/** A class that takes care of common File I/O functions.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
@Pt.ProposedRating Red (ankush)
@Pt.AcceptedRating Red (ankush)
*/
public class FileHandler {
    /** Tells whether a file or directory with a given name exists.
     *  @param fileName A fileName.
     *  @return True if a file or directory with that name exists.
     */
    public static boolean exists(String fileName) {
        File f = new File(fileName);
        return (f.exists());
    }

    /** Reads an object from a specified file.
     * @param fileName The file to read.
     * @return The object in this file.
     */
    public static Object readObject(String fileName) {
        try {
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(file);

            Object object = in.readObject();
            in.close();
            return object;
        } catch (IOException e) {
            throw new RuntimeException("IOException while reading object from "
                    + fileName + ": " + e.toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "ClassNotFoundException while reading object from " + fileName
                    + ": " + e.toString());
        }
    }

    /** Reads the contents of a text file and returns them as a String.
     *  Prints an error statement and returns an empty string if an IO
     *  error occurs.
     *  @param fileName The file to read from.
     *  @return A String containing the entire contents of the file.
     */
    public static String readStringFromFile(String fileName) {
        StringBuffer code = new StringBuffer();

        // We assume that its reading code. It can read any kind of text.
        try {
            BufferedReader input = new BufferedReader(new FileReader(fileName));
            String line; // We read from the file one line at a time.

            // Keep reading till no more lines are left. Append the lines
            // one-by-one to "code" in order.
            while ((line = input.readLine()) != null) {
                code.append(line + "\n");
            }
        } catch (IOException e) {
            System.err.println("FileHandler.readStringFromFile(String):\n"
                    + "ERROR!: Unable to access file " + fileName);
        }

        return code.toString();
    }

    /** Write out the given string to a file.
     *  @param fileName The file to write to.
     *  @param code The String to write.
     *  @exception RuntimeException If it fails to write.
     */
    public static void write(String fileName, String code) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(fileName));
            out.println(code);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e.toString()
                    + "\nFileHandler.write(String, String): "
                    + "could not create file: " + fileName + "\n");
        }
    }

    /** Write out the given Object to a file.
     * @param fileName The file to write to.
     * @param object The object to write.
     */
    public static void write(String fileName, Object object) {
        try {
            FileOutputStream file = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(file);

            out.writeObject((Serializable) object);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException while writing object to "
                    + fileName + ": " + e.toString() + "\nOBJECT: "
                    + object.toString());
        }
    }
}
