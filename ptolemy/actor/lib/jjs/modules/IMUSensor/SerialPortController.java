/* SerialPortController class that handles a serial port - base class for module (addon) IMUSensor for the PTII accessor host

// Copyright (c) 2015-2016 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
*/
package ptolemy.actor.lib.jjs.modules.IMUSensor;

///////////////////////////////////////////////////////////////////
///// SerialPortController

/**
 * This class provides a set of functions for use with an
 * IMUSensor accessor from within Ptolemy II. This thread keeps
 * an object of type ReaderM that starts a thread and
 * continuously reads from the serial port that the sensor is
 * connected on.
 *
 * @author Hunter Massey and Rajesh Kuni
 * @version $Id$
 * @see ReaderM
 * @Pt.ProposedRating Yellow Hunter
 * @Pt.AcceptedRating
 */
public class SerialPortController {

    /** Base constructor. No input values */
    public SerialPortController() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Starts a serial port connection with comm port with number x
     *  (ex: COM8).
     *  @param x The serial port number to connect to
     */
    public void start(int x) {
        r1COM = x;
        h1 = new ReaderM(r1COM, baudrate, window);
        h1.start();
        h1.isStart = true;
    }

    /** Grab the latest unread sample from the buffer and return
     *        it. Returns latest sample upon call if read index has caught
     *        up to write index.
     *  @return The latest unread sample, or the latest read sample if
     *  write index = read index in buffer
    */
    public int[] getSample() {
        int[] sample = new int[9];
        // If the collection thread has started, grab data
        if (h1.isStart) {
            try {
                sample = h1.getNextUnreadSample();
            } catch (Exception e) {
            }
        } else {
            System.out.println("getSamples called before collection started");
        }
        return sample;
    }

    /** Stops the reading ReaderM thread and terminates the serial
     * connection.
     */
    public void stop() {
        h1.isStart = false;
        h1.stopRead();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Parameters for the program.
    private static int baudrate = 115200;
    private int r1COM;
    private static int window = 60;

    //input correct values before running
    private ReaderM h1;
}
