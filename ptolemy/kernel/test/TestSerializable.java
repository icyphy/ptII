/* Demonstrate using Serializable classes

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.kernel.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TestSerializable

/**
 This class constructs a system from the Ptolemy II design document, Figure 8,
 saves it to a file and then reloads it.
 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red
 */
public class TestSerializable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an Example System, then print it out.
     * @exception NameDuplicationException if the example system cannot
     * be built because of a duplicate name
     * @exception IllegalActionException if the example system cannot
     * be built.
     */
    public static void main(String[] args) throws NameDuplicationException,
            IllegalActionException {
        ExampleSystem exampleSystem = new ExampleSystem();
        String filename = "TestSerializable.data";

        if (args.length > 0 && args[0].equals("write")) {
            FileOutputStream f = null;
            ObjectOutput s = null;

            try {
                // Write the system out.
                f = new FileOutputStream(filename);
                s = new ObjectOutputStream(f);
                s.writeObject(exampleSystem);
            } catch (IOException e) {
                System.err.println("Exception while writing: " + e);
                e.printStackTrace();
            } finally {
                if (f != null) {
                    try {
                        f.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on '" + filename + "'");
                        throwable.printStackTrace();
                    }
                }

                if (s != null) {
                    try {
                        s.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on '" + filename + "'");
                        throwable.printStackTrace();
                    }
                }
            }

            System.out.println("Wrote to " + filename);
        } else {
            FileInputStream f = null;
            ObjectInputStream s = null;

            try {
                // Read the system in
                f = new FileInputStream(filename);
                s = new ObjectInputStream(f);

                ExampleSystem newExampleSystem = (ExampleSystem) s.readObject();
                String newDescription = newExampleSystem.toString();
                String oldDescription = exampleSystem.toString();

                if (oldDescription.equals(newDescription)) {
                    System.out.println("OK: Description read in from "
                            + filename + " is the same as the original\n");
                } else {
                    System.out.println("ERROR\nDescription read in from "
                            + filename + "\n" + newDescription + "\n"
                            + "is NOT the same as the original:\n"
                            + oldDescription);
                    System.exit(1);
                }
            } catch (IOException e) {
                System.err.println("IOException while reading: " + e);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.err
                        .println("ClassNotFoundException while reading: " + e);
            } finally {
                if (f != null) {
                    try {
                        f.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on '" + filename + "'");
                        throwable.printStackTrace();
                    }
                }

                if (s != null) {
                    try {
                        s.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on '" + filename + "'");
                        throwable.printStackTrace();
                    }
                }
            }
        }
    }
}
