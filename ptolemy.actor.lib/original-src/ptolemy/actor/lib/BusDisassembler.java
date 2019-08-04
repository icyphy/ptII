/* BusDisassembler - split input bus channels onto output port channels

 Copyright (c) 2002-2014 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.lib;

import java.util.Iterator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BusDisassembler

/**
 Split the input bus relation into individual (possibly bus) output port
 relations. If the width of the first output port is W1, it receives the
 tokens from the first W1 channels of the input bus, the next output port
 receives its width worth share from subsequent channels of the input bus
 until either the input bus channels or all output port channels are
 exhausted.<p>

 Note: The width of a single relation (e.g. connected to an output port)
 may be controlled by adding a <em>width</em> parameter with an IntToken value
 representing the desired relation width.<p>

 @author Zoltan Kemenczy
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (zkemenczy)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.actor.IORelation
 */
public class BusDisassembler extends TypedAtomicActor {
    /** Construct a BusDisassembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BusDisassembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" width=\"6\" "
                + "height=\"40\" style=\"fill:black\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int inputWidth = input.getWidth();
        Iterator<TypedIOPort> outputPorts = outputPortList().iterator();
        TypedIOPort outputPort = outputPorts.hasNext() ? outputPorts.next()
                : null;
        int outputWidth = outputPort != null ? outputPort.getWidth() : 0;
        int j = 0;

        for (int i = 0; i < inputWidth; i++) {
            if (input.hasToken(i)) {
                Token t = input.get(i);

                if (outputPort != null) {
                    outputPort.send(j, t);
                }
            }

            if (outputPort != null) {
                if (j < outputWidth - 1) {
                    j++;
                } else {
                    outputPort = outputPorts.hasNext() ? outputPorts.next()
                            : null;
                    outputWidth = outputPort != null ? outputPort.getWidth()
                            : 0;
                    j = 0;
                }
            }
        }
    }

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        if (inputPortList().size() > 1) {
            throw new IllegalActionException(this,
                    "can have only one input port.");
        }
    }
}
