/*Attributes for ports decorated by a communication aspect.

@Copyright (c) 2011-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

/** Attributes for ports decorated by a communication aspect.
 *  A port on an actor decorated by a composite communication aspect must
 *  specify the request port that input tokens are routed to.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class CommunicationAspectAttributes extends ExecutionAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public CommunicationAspectAttributes(NamedObj target, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator);
        _init();
    }

    /** Constructor to use when parsing a MoML file.
     *  @param target The object being decorated.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public CommunicationAspectAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The sequenceNumber indicates the order in which communication aspects
     *  are used. It defaults to the integer value -1 to indicate that
     *  this communication aspect is not used. After enabling the quantity
     *  manager, this sequence number gets updated automatically.
     */
    public Parameter sequenceNumber;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If attribute is <i>messageLength</i> report the new value
     *  to the communication aspect.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *  Not thrown in this class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        IOPort port = (IOPort) getContainer();
        if (attribute == enable) {
            // When cloning, the contanier might be an EntityLibrary.
            // See vergil/test/VergilConfiguration.tcl.
            NamedObj container = port.getContainer().getContainer();
            if (container instanceof CompositeActor) {
                if (((CompositeActor) container).isOpaque()) {
                    port.createReceivers();
                }
                port.invalidateCommunicationAspects();
            }
        }
        super.attributeChanged(attribute);
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        sequenceNumber = new Parameter(this, "sequenceNumber", new IntToken(-1));
        sequenceNumber.setPersistent(true);
        sequenceNumber.setVisibility(Settable.EXPERT);
    }
}
