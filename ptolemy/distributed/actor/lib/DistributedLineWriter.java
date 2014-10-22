/* An actor that writes the value of string tokens to the standard output,
 one per line.

 @Copyright (c) 2005-2014 The Regents of Aalborg University.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
 HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.distributed.actor.lib;

import ptolemy.actor.lib.Sink;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////LineWriter

/**
 An actor that writes the value of string tokens to the standard output,
 one per line.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 */
public class DistributedLineWriter extends Sink {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DistributedLineWriter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.STRING);
        input.setMultiport(false);
    }

    /** Read an input string token and write it to the standard output.
     *  If there is no input, do nothing.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token token = input.get(0);
            System.out.println(token.toString());
        }

        return super.postfire();
    }
}
