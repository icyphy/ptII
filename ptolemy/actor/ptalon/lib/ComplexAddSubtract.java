/* A complex number adder/subtractor.

 Copyright (c) 2006-2007 The Regents of the University of California.
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

package ptolemy.actor.ptalon.lib;

import ptolemy.actor.lib.AddSubtract;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ComplexAddSubtract

/**
 A complex number adder/subtractor.
 This adder has two input ports, both of which are multiports,
 and one output port, which is not.
 The types on the ports are complex.  Data that arrives on the
 input port named <i>plus</i> will be added, and data that arrives
 on the input port named <i>minus</i> will be subtracted.
 Any token type supporting addition and subtraction can be used.
 In most domains, either input port can be left unconnected.
 Thus, to get a simple adder (with no subtractor), just leave the
 <i>minus</i> input unconnected.
 <p>
 This actor does not require that each input
 channel have a token upon firing. It will add or subtract available
 tokens at the inputs and ignore the channels that do not have tokens.
 It consumes at most one input token from each port.
 If no input tokens are available at all, then no output is produced.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */

public class ComplexAddSubtract extends AddSubtract {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */

    public ComplexAddSubtract(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        plus.setTypeEquals(BaseType.COMPLEX);
        minus.setTypeEquals(BaseType.COMPLEX);
        output.setTypeEquals(BaseType.COMPLEX);
    }

}
