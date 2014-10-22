/* An actor that stores tokens into a memory.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.taskpt.lib;

import ptolemy.data.Token;
import ptolemy.domains.taskpt.kernel.PtrToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Store

/** An actor that stores tokens into a memory.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public class Store extends LoadStore {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, define the <i>data</i>
     *  and the <i>ptr</i> ports as input ports.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException Thrown if the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Thrown if the container already has an
     *   actor with this name.
     */
    public Store(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        ptr.setInput(true);
        data.setInput(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Store tokens present in <i>data</i> to memory. Address and number
     * of tokens to be stored is given by the Token in the <i>ptr</i> port.
     *
     * @exception IllegalActionException Thrown if no memory is found, an exception is
     * thrown.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        Memory mem = getMemory();
        if (mem == null) {
            throw new IllegalActionException("no memory found.");
        }

        Token tok = ptr.get(0);

        if (!(tok instanceof PtrToken)) {
            throw new IllegalActionException(this,
                    "Token in port ptr has wrong type.");
        }

        PtrToken ptrTok = (PtrToken) tok;

        int addr = ptrTok.getAddress();
        int numWrites = 0;
        int size = ptrTok.getSize();

        int width = data.getWidth();
        for (int i = 0; i < width; ++i) {
            while (data.hasToken(i) && numWrites < size) {
                Token token = data.get(i);

                mem.write(addr + numWrites, token);
                _debug("written token to address " + (addr + i));
                numWrites++;
            }
        }
    }
}
