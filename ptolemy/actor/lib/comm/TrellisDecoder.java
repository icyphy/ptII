/* Decode convolutional code with non-antipodal constellation.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// ViterbiDecoder
/**
The TrellisDecoder is a generalization of the ViterbiDecoder. It
can handle trellis coding, which has non-antipodal constellation.
For a <i>k</i>/<i>n</i> convolutional code, the constellation
should map each codeword into a complex number. Hence the length
of the constellation should be a complex array of length
2<i><sup>n</sup></i>. For example, a 1/2 rate convolutional code
should use 4PSK. a <i>k</i>/3 convolutional code should use 8PSK.
<p>
The input port of the TrellisDecoder is complex. On each firing,
the TrellisDecoder reads one input. The Euclidean distance is defined
as the distance between the noisy input and the point in the
constellation mapped from the codeword. Like in ViterbiDecoder,
this actor produces <i>k</i> outputs on each firing.
<p>
See ConvolutionalCoder and ViterbiDecoder for details about
the meaning of these parameters.
<p>
For more information on convolutional codes, Viterbi decoder, and
trellis coding, see the ConvolutionalCoder actor, ViterbiDecoder
actor and Proakis, <i>Digital Communications</i>, Fourth Edition,
McGraw-Hill, 2001, pp. 471-477 and pp. 482-485,
or Barry, Lee and Messerschmitt, <i>Digital Communication</i>, Third Edition,
Kluwer, 2004.
<p>
@author Rachel Zhou, contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/

public class TrellisDecoder extends ViterbiDecoder {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TrellisDecoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        softDecoding.setVisibility(Settable.NONE);
        softDecoding.setExpression("false");

        trellisDecoding.setExpression("true");

        constellation.setTypeEquals(new ArrayType(BaseType.COMPLEX));
        constellation.setExpression("{1.0, i, -1.0, -i}");
    }
}
