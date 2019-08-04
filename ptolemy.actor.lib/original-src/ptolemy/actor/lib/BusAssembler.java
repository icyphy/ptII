/* Assemble input port channels into output bus.

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
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BusAssembler

/**
 Aggregate all input relation channels into one output "bus" relation.
 The width of the output port (bus relation) is set to the sum of the
 input port widths. During the fire method, the input port channels are
 scanned sequentially for tokens with the output channel index
 incremented for each input channel scanned. If an input channel has a
 token, it is copied to the corresponding output channel.<p>

 Note: The width of a single relation (e.g. connected to an input port)
 may be controlled by adding a <em>width</em> parameter with an IntToken
 value
 representing the desired relation width<p>

 @author Zoltan Kemenczy, Research in Motion Limited
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (zkemenczy)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.actor.IORelation
 */
public class BusAssembler extends TypedAtomicActor {
    /** Construct a BusAssembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BusAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" width=\"6\" "
                + "height=\"40\" style=\"fill:black\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an input port width. */
    @Override
    public void connectionsChanged(Port p) {
        super.connectionsChanged(p);

        if (p == output) {
            return;
        }

        try {
            _recalculateOutputWidth();
        } catch (IllegalActionException ex) {
        }
    }

    /** Iterate through input ports and transfer data sequentially from
     *  input channels to output channels, maintaining input to output
     *  channel mapping.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Iterator inputPorts = inputPortList().iterator();
        TypedIOPort inputPort = (TypedIOPort) (inputPorts.hasNext() ? inputPorts
                .next() : null);
        int inputWidth = inputPort != null ? inputPort.getWidth() : 0;
        int i = 0;
        int j = 0;

        while (inputPort != null) {
            if (i < inputWidth && inputPort.hasToken(i)) {
                Token t = inputPort.get(i);

                if (j < _outputWidth) {
                    output.send(j, t);
                }
            }

            j++;

            if (++i >= inputWidth) {
                inputPort = (TypedIOPort) (inputPorts.hasNext() ? inputPorts
                        .next() : null);
                inputWidth = inputPort != null ? inputPort.getWidth() : 0;
                i = 0;
            }
        }
    }

    /** Makes sure that there is only one relation connected to the
     output port and recalculates its width. */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (outputPortList().size() > 1) {
            throw new IllegalActionException(this,
                    "can have only one output port.");
        }

        if (output.linkedRelationList().size() > 1) {
            throw new IllegalActionException(this,
                    "can have only one output relation linked.");
        }

        _recalculateOutputWidth();
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Recalculate the output relation width. */
    private void _recalculateOutputWidth() throws IllegalActionException {
        List outputRelations = output.linkedRelationList();
        _outputWidth = 0;

        if (outputRelations.isEmpty()) {
            return;
        }

        TypedIORelation outputRelation = (TypedIORelation) outputRelations
                .get(0);
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort port = (TypedIOPort) inputPorts.next();
            _outputWidth += port.getWidth(); // includes all linked relations
        }

        outputRelation.setWidth(_outputWidth);

        // TODO: figure out how to obey if the output relation width is
        // set (if isWidthFixed() would return a reliable true...)
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    int _outputWidth = 0;
}
