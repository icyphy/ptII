/* One line description

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEBus
/**


@author Lukito Muliadi
@version $Id$
*/
public class DEBus extends DEActor {

    /** Construct a clock that generates events with the specified values
     *  at the specified interval.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @param value The value of the output.
     *  @param interval The interval between clock ticks.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    // FIXME: The value should be an attribute, as should the interval.
    // FIXME: Should the value be a double? Probably not...
    public DEBus(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        ioport = new TypedIOPort(this, "ioport", true, true);
        ioport.setMultiport(true);
        ioport.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce an output event at the current time, and then schedule
     *  a firing in the future.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        for (int i = 0; i < ioport.getWidth(); i++) {
            while (ioport.hasToken(i)) {
                Token t = ioport.get(i);
                ioport.broadcast(t);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    // The output port.
    public TypedIOPort ioport;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}






