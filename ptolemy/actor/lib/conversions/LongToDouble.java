/* An actor that converts a LongToken into a DoubleToken.

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
package ptolemy.actor.lib.conversions;

import ptolemy.data.DoubleToken;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// LongToDouble

/**
 <p>This actor converts a LongToken into a DoubleToken.</p>
 <p>
 Note that a double cannot be losslessly converted to a long, and vice
 versa, as both have 64 bit representations in Java.</p>

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)

 @see ptolemy.data.DoubleToken
 @see ptolemy.data.LongToken
 */
public class LongToDouble extends Converter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LongToDouble(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setTypeEquals(BaseType.LONG);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read exactly one token from the input and output the converted
     *  value.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        LongToken inputToken = (LongToken) input.get(0);
        Long inputValue = Long.valueOf(inputToken.longValue());
        DoubleToken result = new DoubleToken(inputValue.doubleValue());

        output.send(0, result);
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
