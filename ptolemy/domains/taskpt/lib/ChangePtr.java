/* An actor that changes address and/or size of a PtrToken.

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

import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.taskpt.kernel.PtrToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ChangePtr

/** An actor that changes address and size of a PtrToken.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public class ChangePtr extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors,
     *  define the ports and port parameters.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ChangePtr(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: Types of input and output should be set to PtrToken
        // once this type exists.

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.GENERAL);
        input.setMultiport(false);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.GENERAL);
        output.setMultiport(true);

        addressOffset = new PortParameter(this, "addressOffset");
        addressOffset.setTypeEquals(BaseType.INT);
        addressOffset.setExpression("0");

        sizeOffset = new PortParameter(this, "sizeOffset");
        sizeOffset.setTypeEquals(BaseType.INT);
        sizeOffset.setExpression("0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The offset that is added to the current address of the incoming token.*/
    public PortParameter addressOffset;

    /** The offset that is added to the current size of the incoming token.*/
    public PortParameter sizeOffset;

    /** The port that contains the PtrToken to be changed.*/
    public TypedIOPort input;

    /** The port that outputs the new PtrToken. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the PtrToken present on <i>input</i> and produce a new PtrToken
     * on <i>output</i> with address = address of the token present <i>input</i>
     * plus addressOffset and size = size of the token present at <i>input</i>
     * plus sizeOffset.
     *
     * @exception NoTokenException Thrown if there is no token.
     * @exception IllegalActionException Thrown if there is no director, and hence
     * no receivers have been created, if the port is not an input port, or
     * if the channel index is out of range.
     */
    @Override
    public void fire() throws NoTokenException, IllegalActionException {
        super.fire();
        Token token = input.get(0);
        if (token instanceof PtrToken) {
            PtrToken in = (PtrToken) token;
            addressOffset.update();
            int addr = in.getAddress()
                    + ((IntToken) addressOffset.getToken()).intValue();
            sizeOffset.update();
            int sz = in.getSize()
                    + +((IntToken) sizeOffset.getToken()).intValue();
            if (sz < 1) {
                throw new IllegalActionException("new size is smaller than 1.");
            }
            PtrToken result = new PtrToken(addr, sz);
            output.broadcast(result);
        }
        return;
    }

}
