/* Demonstrate using Serializable classes

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.kernel.test;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

//////////////////////////////////////////////////////////////////////////
//// TestSerializable
/**
This class constructs a system from the Ptolemy II design document, Figure 8,
saves it to a file and then reloads it.
@author Christopher Hylands
@version $Id$
@since Ptolemy II 0.2
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
    public static void main(String args[])
            throws NameDuplicationException, IllegalActionException {
        ExampleSystem exampleSystem = new ExampleSystem();
        String filename = new String("TestSerializable.data");

        if (args.length > 0 && args[0].equals("write")) {
            try {
                // Write the system out.
                FileOutputStream f = new FileOutputStream(filename);
                ObjectOutput s = new ObjectOutputStream(f);
                s.writeObject(exampleSystem);
                s.flush();
                f.close();
            } catch (IOException e) {
                System.err.println("Exception while writing: "+ e);
            }
            System.out.println("Wrote to " + filename);
        } else {
            try {
                // Read the system in
                FileInputStream f = new FileInputStream(filename);
                ObjectInputStream s = new ObjectInputStream(f);
                ExampleSystem newExampleSystem = (ExampleSystem)s.readObject();
                f.close();
                String newDescription = newExampleSystem.toString();
                String oldDescription = exampleSystem.toString();
                if (oldDescription.equals(newDescription)) {
                    System.out.println("OK: Description read in from " +
                            filename + " is the same as the original\n");
                } else {
                    System.out.println("ERROR\nDescription read in from " +
                            filename + "\n"+
                            newDescription + "\n" +
                            "is NOT the same as the original:\n"+
                            oldDescription);
                    System.exit(1);
                }
            } catch (IOException e) {
                System.err.println("IOException while reading: "+ e);
            } catch (ClassNotFoundException e) {
                System.err.println("ClassNotFoundException while reading: "
                        + e);
            }
        }
    }
}
