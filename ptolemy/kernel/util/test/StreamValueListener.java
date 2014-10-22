/* A value listener that describes value changes on the standard output.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.kernel.util.test;

import java.io.OutputStream;
import java.io.PrintStream;

import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;

///////////////////////////////////////////////////////////////////
//// StreamValueListener

/**
 A value listener that describes value changes on the standard output
 when the value of an object implementing Settable changes.

 @author  Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 @see ptolemy.data.expr.Variable
 */
public class StreamValueListener implements ValueListener {
    /** Create a value listener that sends messages to the standard output.
     */
    public StreamValueListener() {
        _output = System.out;
    }

    /** Create a value listener that sends messages to the specified stream.
     *  @param out The stream to send messages to.
     */
    public StreamValueListener(OutputStream out) {
        _output = new PrintStream(out);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that the specified Settable has changed.
     *  @param settable The object that has changed value.
     */
    @Override
    public void valueChanged(Settable settable) {
        _output.println(settable + " changed, new expression: "
                + settable.getExpression());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The PrintStream that we direct the output to. */
    protected PrintStream _output;
}
