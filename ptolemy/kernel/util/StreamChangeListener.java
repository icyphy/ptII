/* A change listener that describes changes on the standard output.

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
package ptolemy.kernel.util;

import java.io.OutputStream;
import java.io.PrintStream;

///////////////////////////////////////////////////////////////////
//// StreamChangeListener

/**
 A change listener that describes the changes on the standard output.
 It simply prints the description of the change once it executes (or
 throws an exception).

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (neuendor)
 */
public class StreamChangeListener implements ChangeListener {
    /** Create a change listener that sends messages to the standard output.
     */
    public StreamChangeListener() {
        _output = System.out;
    }

    /** Create a change listener that sends messages to the specified stream.
     *  @param out The stream to send messages to.
     */
    public StreamChangeListener(OutputStream out) {
        _output = new PrintStream(out);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Print the description of the change to the stream output.
     *  @param change The change that has been executed.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
        String description = "";

        if (change != null) {
            description = change.getDescription();
        }

        _output.println("StreamChangeRequest.changeExecuted(): " + description
                + " succeeded");
    }

    /** Print the description of the failure to the stream output.
     *  @param change The change that has been executed.
     *  @param exception The exception that occurred.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        String description = "";

        if (change != null) {
            description = change.getDescription();
        }

        _output.println("StreamChangeRequest.changeFailed(): " + description
                + " failed: " + exception.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The PrintStream that we direct the output to. */
    protected PrintStream _output;
}
