/* An actor that allocates memory.

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

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.taskpt.kernel.PtrToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Alloc

/** An actor that allocates room for Tokens in a (shared) memory.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public class Alloc extends MemoryAccess {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>size</i> port parameter and define the
     *  <i>ptr</i> port as output port. Initialize <i>size</i>
     *  to IntToken with value 1, and <i>init</i> to IntToken with value 0.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Alloc(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        ptr.setOutput(true);
        ptr.setMultiport(true);

        size = new PortParameter(this, "size");
        size.setTypeEquals(BaseType.INT);
        size.setExpression("1");

        init = new PortParameter(this, "init");
        init.setExpression("0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The initial value of the Tokens that are allocated.*/
    public PortParameter init;

    /** The size to be allocated in the memory. One unit is one Token.*/
    public PortParameter size;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Allocate room for Tokens in a (shared) memory. The size is determined
     * by the value of the <i>size</i> PortParameter, the initial value by the
     * <i>init</i> PortParamter.
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

        size.update();
        int sizeVal = ((IntToken) size.getToken()).intValue();

        PtrToken ptrTok = mem.allocate(sizeVal);

        int numWrites = 0;
        int addrVal = ptrTok.getAddress();

        while (numWrites < sizeVal) {
            init.update();
            mem.write(addrVal + numWrites, init.getToken());
            _debug("written to address " + (addrVal + numWrites));
            numWrites++;
        }

        _debug("allocated data in memory address " + ptrTok.getAddress()
                + " with size " + sizeVal);
        ptr.broadcast(ptrTok);
    }

}
