/* A type-safe enumeration of actor enabling status.

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
package ptolemy.domains.ddf.kernel;

///////////////////////////////////////////////////////////////////
//// ActorEnablingStatus

/**
 A type-safe enumeration of actor enabling status.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.0
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (cxh)
 */
public final class ActorEnablingStatus {
    /** A private constructor.
     *  @param status The string representation of this object.
     */
    private ActorEnablingStatus(String status) {
        _status = status;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the string representation of this object.
     *  @return the string representation of this object.
     */
    @Override
    public String toString() {
        return _status;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** This final static member indicates an actor is enabled but
     *  deferrable.
     */
    public static final ActorEnablingStatus ENABLED_DEFERRABLE = new ActorEnablingStatus(
            "ENABLED_DEFERRABLE");

    /** This final static member indicates an actor is enabled and
     *  not deferrable.
     */
    public static final ActorEnablingStatus ENABLED_NOT_DEFERRABLE = new ActorEnablingStatus(
            "ENABLED_NOT_DEFERRABLE");

    /** This final static member indicates an actor is not enabled.
     */
    public static final ActorEnablingStatus NOT_ENABLED = new ActorEnablingStatus(
            "NOT_ENABLED");

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** String representation of this object.
     */
    private String _status;
}
