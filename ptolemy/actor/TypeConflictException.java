/* Exception thrown on detecting a type conflict.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package ptolemy.actor;

import ptolemy.kernel.util.KernelException;

//////////////////////////////////////////////////////////////////////////
//// TypeConflictException
/**
Thrown on detecting a type conflict.

@author Yuhong Xiong
$Id$
*/
public class TypeConflictException extends KernelException {

    /** Constructs an Exception with a detail message.
     *  @param detail The message.
     */
    public TypeConflictException(String detail) {
        this(null, null, detail);
    }

    /** Constructs an Exception with a TypedIOPort and a message.
     *  The port is the place where type conflict occured. The detailed
     *  message of this Exception will include the name of the port and
     *  the specified message.
     *  @param port a TypedIOPort with type conflict. 
     *  @param detail a message.
     */
    public TypeConflictException(TypedIOPort port, String detail) {
        this(port, null, detail);
    }

    /** Constructs an Exception with two TypedIOPorts and a message.
     *  The ports usually represent a type constraint that require
     *  the type of the first port to be less than or equal to the type
     *  of the second port. Throwing an Exception using this constructor
     *  means that this constraint is not satisfied.
     *  The detailed message of this Exception will include the names
     *  of both ports and the specified message.
     *  @param port1 a TypedIOPort.
     *  @param port2 a TypedIOPort.
     *  @param detail a message.
     */
    public TypeConflictException(TypedIOPort port1, TypedIOPort port2,
            String detail) {
        super(port1, port2, detail);
    }
}

