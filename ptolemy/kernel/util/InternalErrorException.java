/* Some object or set of objects has a state that in theory is not permitted.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// InternalErrorException
/**
This exception should be thrown if an unexpected error is encountered
other than one for which InvalidStateException would be appropriate.
Our design should make it impossible for this exception to ever occur,
so occurrence is a bug. This exception is a RuntimeException
so that it does not have to be declared.

@author Edward A. Lee, Christopher Hylands
@version $Id$
*/
public class InternalErrorException extends KernelRuntimeException {

    /** Constructs an Exception with a detail message.
     *  @param detail The message.
     */
    public InternalErrorException(String detail) {
        super(detail);
    }
}
