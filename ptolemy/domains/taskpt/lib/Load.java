/* An actor that loads tokens from memory.

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
//// Load

/** An actor that loads tokens from memory.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public class Load extends LoadStore {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, define the <i>data</i>
     *  port as an output port and the <i>ptr</i> port as an input port.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException Thrown if the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Thrown if the container already has an
     *   actor with this name.
     */
    public Load(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        ptr.setInput(true);
        data.setOutput(true);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Load tokens from a (shared) memory. Use the address given by the
     * the PtrToken in the <i>ptr</i> port.
     *
     * @exception IllegalActionException Thrown if no memory is found.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Memory mem = getMemory();
        if (mem == null) {
            throw new IllegalActionException("no memory found.");
        }

        Token in = ptr.get(0);
        if (in instanceof PtrToken) {
            PtrToken ptrTok = (PtrToken) in;
            int addr = ptrTok.getAddress();
            int size = ptrTok.getSize();
            for (int i = 0; i < size; ++i) {
                Token token = mem.read(addr + i);
                data.broadcast(token);
                _debug("read token from address " + (addr + i));
            }
        }
    }
}
