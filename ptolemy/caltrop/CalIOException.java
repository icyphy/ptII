/*
@Copyright (c) 2003 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)


*/
package ptolemy.caltrop;

//////////////////////////////////////////////////////////////////////////
//// CalIOException
/**
An exception used to indicate an IO error during interpretation of a
CAL actor in Ptolemy.  This can occur during the getting/putting of a
{@link ptolemy.data.Token caltrop.data.Token} on a channel.

@author Jörn W. Janneck <janneck@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
*/
public class CalIOException extends RuntimeException {

    /**
     * Create a <tt>CalIOException()</tt>.
     */
    public CalIOException() {}

    /**
     * Create a <tt>CalIOException</tt> with a message.
     * @param msg The message.
     */
    public CalIOException(String msg) {
        super(msg);
    }

    /**
     * Create a <tt>CalIOException</tt> with a message and a cause.
     * @param msg The message.
     * @param cause The cause.
     */
    public CalIOException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a <tt>CalIOException</tt> with a cause.
     * @param cause The cause.
     */
    public CalIOException(Throwable cause) {
        super(cause);
    }
}
