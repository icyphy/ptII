/* Extract the ith element from an input array, where i is also an input

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ArrayElementI

/**
Extract the ith element from an array.  This actor reads an array from the
<i>input</i> port and sends one of its elements to the <i>output</i>
port.  The element that is extracted is determined by the
<i>index</i> input, if one is provided, and by the <i>index</i>
parameter, if not.  It is required that 0 &lt;= <i>index</i> &lt;
<i>N</i>, where <i>N</i> is the length of the input array, or
an exception will be thrown by the fire() method.

@see ArrayElement
@see LookupTable
@see RecordDisassembler
@author Edward A. Lee, Jim Armstrong
@version $Id$
*/

public class ArrayElementI extends ArrayElement {
    
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayElementI(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        indexPort = new TypedIOPort(this, "index", true, false);
        indexPort.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Ports and Parameters              ////

    /** The port for providing the index into the input array.
     *  This is an integer that is required to be less than or equal
     *  to the length of the input array.
     */
    public TypedIOPort indexPort;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one array from the input port and produce
     *  its <i>i</i>-th element on the output port. If the <i>index</i>
     *  input port has a token, then that token is used to set the
     *  value of the <i>index</i> parameter.  The <i>index</i> parameter
     *  is used to determine which element is the <i>i</i>-th element.
     *  If there is no input token on the <i>input</i> port, then no
     *  output is produced.
     *  @exception IllegalActionException If the <i>index</i>
     *   is out of range.
     */
    public void fire() throws IllegalActionException {
	if (indexPort.hasToken(0)) {
            index.setToken(indexPort.get(0));
	}
        super.fire();
    }
}

