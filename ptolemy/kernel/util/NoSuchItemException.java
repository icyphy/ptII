/* Thrown on access (by name) to an item that doesn't exist.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
//// NoSuchItemException
/**
Thrown on access (by name) to an item that doesn't exist.
E.g., attempt to remove a port by name and no such port exists.

@author Edward A. Lee
@version $Id$
*/
public class NoSuchItemException extends KernelException {
    /** Constructs an Exception with a detail message.
     *  @param detail A message.
     */
    public NoSuchItemException(String detail) {
        super(null, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the argument.
     *  @param obj An object in which the item might have been.
     *  @param detail A message.
     */
    public NoSuchItemException(Nameable obj, String detail) {
        super(obj, null, detail);
    }
}
