/* Container for decorator attributes that are provided to Ptides platform inputs
   by analysis tools that need guarantees about the input patterns on each input port.

 @Copyright (c) 2013 The Regents of the University of California.
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

package ptolemy.apps.hardrealtime;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// InputModelAttributes

/**
 Container for decorator attributes that are provided to Ptides platform inputs
 by analysis tools that need guarantess about the input patterns on eacn input port.
 The PtidesToMultiFrame attribute decorates input ports in a Ptides model with the
 attributes contained by an instance this class.

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class InputModelAttributes extends DecoratorAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public InputModelAttributes(NamedObj target, Decorator decorator)
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
    public InputModelAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The minimum distance of two successive events at an input port. */
    public Parameter minimumInterarrivalTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the attribute is
     *  <i>minimumInterarrivalTime</i>, check that it is non-negative.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, if the value of the minimum inter-arrival
     *   parameter cannot be read, or if that value is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == minimumInterarrivalTime
                && minimumInterarrivalTime.getToken() != null) {
            double newTime = ((DoubleToken) minimumInterarrivalTime.getToken())
                    .doubleValue();
            if (newTime < 0) {
                throw new IllegalActionException(getContainer(),
                        "The minimum interarrival time of an input source cannot be negative.");
            }
            _minimumInterarrivalTime = newTime;
        }
        super.attributeChanged(attribute);
    }

    /** Return the minimum inter-arrival time of the input port.
     *  @return the minimum inter-arrival time.
     */
    public double getMinimumInterarrivalTime() {
        return _minimumInterarrivalTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the parameter. */
    private void _init() {
        try {
            minimumInterarrivalTime = new Parameter(this,
                    "minimum inter-arrival time");
            minimumInterarrivalTime.setTypeEquals(BaseType.DOUBLE);
        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _minimumInterarrivalTime = -1;
}
