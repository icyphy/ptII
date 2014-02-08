/* An Exception that occurs while trying to populate the PtalonActor in the Ptalon interpreter.

 Copyright (c) 2006-2007 The Regents of the University of California.
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

 */
package ptolemy.actor.ptalon;

/**
 * An Exception that occurs while trying to populate the PtalonActor
 * in the Ptalon interpreter.
 *
 * @author Adam Cataldo, Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Red (acataldo)
 * @Pt.AcceptedRating Red (acataldo)
 */
@SuppressWarnings("serial")
public class PtalonRuntimeException extends Exception {

    /** Create a PtalonRuntimeException.
     *
     *  @param message An explanation of the offense.
     */
    public PtalonRuntimeException(String message) {
        super(message);
    }

    /** Create a PtalonRuntimeException.
     *
     *  @param message An explanation of the offense.
     *  @param cause The cause of the offense.
     */
    public PtalonRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
