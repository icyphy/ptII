/* An object that hold information on a buffer used in the
   generated code.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.codegen;

import ptolemy.data.type.Type;

/**
An object that hold information on a buffer used in the
generated code.

@author Jeff Tsay
@version $Id$
 */
public class BufferInfo implements Cloneable {

    /** Construct a new instance of BufferInfo, without initializing the fields
     *  of the class.
     */
    public BufferInfo() {}

    /** Make a clone of this instance. */
    public Object clone() {
        Object obj;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException cne) {
            throw new InternalError("can't clone BufferInfo.");
        }
        return obj;
    }

    public String toString() {
        return "name = " + name + ", cg name = " + codeGenName +
               " width x length = " + width + " x " + length +
               ", type = " + type;
    }

    public String name;

    /** The unique name of the buffer used in the generated code. */
    public String codeGenName;

    /** The number of channels of this port. */
    public int width;

    /** The length of the buffer required. */
    public int length;

    /** The resolved type of the port. */
    public Type type;
}

