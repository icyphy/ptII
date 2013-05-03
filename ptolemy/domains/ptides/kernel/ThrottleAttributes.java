/* This director implements the Ptides programming model.

@Copyright (c) 2008-2013 The Regents of the University of California.
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

package ptolemy.domains.ptides.kernel;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
Container for decorator attributes that are provided to local sources and 
other actors that schedule their own firings by
a {@link PtidesDirector}. These attributes are used to throttle the 
production of events by those sources.

The parameter maximumLookaheadTime makes sure that actors are not fired
at logical times bigger than the current platform time plus the parameter value.
The parameter maximumFutureEvents specifies that the decorated actor
can only create and put onto the event queue a certain number of events. These
events have to be consumed by downstream actors in order to allow the actor
to create more events.

The boolean parameters useMaximumLookaheadTime and useMaximumFutureEvents
are used to specify which paramter is used. Selecting one paramter automatically
deselects the other such that only one can be true at a time.

 @author  Patricia Derler 
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ThrottleAttributes extends DecoratorAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public ThrottleAttributes(NamedObj target, PtidesDirector decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator);
        _init();
    }

    /** Constructor to use when parsing a MoML file.
     *  @param target The object being decorated.
     *  @param name The name of this attribute.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public ThrottleAttributes(Object target, String name)
            throws IllegalActionException, NameDuplicationException {
        super((NamedObj) target, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** This parameter specifies that the parameter maximumFutureFiringTime
     *  should be used to throttle the decorated actor.
     *  This parameter contains a boolean value that defaults to false.
     */
    public Parameter useMaximumLookaheadTime;

    /** The maximumLookaheadTime parameter is the time added to the current
     *  platform time during which the decorated actor can safely be fired.
     *  This parameter contains a double value that defaults to 0.0.
     */
    public Parameter maximumLookaheadTime;

    /** This parameter specifies that the parameter maximumFutureEvents
     *  should be used to throttle the decorated actor.
     *  This parameter contains a boolean value that defaults to true.
     */
    public Parameter useMaximumFutureEvents;

    /** The maximumFutureEvents parameter specifies the maximum number 
     *  of events produced by the decorated actor that can be present 
     *  in the event queue at a time.
     *  This parameter contains an int value that defaults to 0.
     */
    public Parameter maximumFutureEvents;
    
    ///////////////////////////////////////////////////////////////////
    ////                        public methods                     ////
    
    /** Make sure that only one of the boolean values in the parameters 
     *  <i>useMaximumLookaheadTime</i> and <i>useMaximumFutureEvents</i>
     *  is true.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == useMaximumLookaheadTime) {
            boolean value = ((BooleanToken)useMaximumLookaheadTime.getToken()).booleanValue();
            if (value) {
                useMaximumFutureEvents.setExpression("false");
            }
        } else if (attribute == useMaximumFutureEvents) {
            boolean value = ((BooleanToken)useMaximumFutureEvents.getToken()).booleanValue();
            if (value) {
                useMaximumLookaheadTime.setExpression("false");
            }
        }
        super.attributeChanged(attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** Create the parameters.
     */
    private void _init() {
        try {
            useMaximumLookaheadTime = new Parameter(this,
                    "useMaximumLookaheadTime");
            useMaximumLookaheadTime.setExpression("false");
            useMaximumLookaheadTime.setTypeEquals(BaseType.BOOLEAN);

            maximumLookaheadTime = new Parameter(this,
                    "maximumLookaheadTime");
            maximumLookaheadTime.setExpression("0.0");

            useMaximumFutureEvents = new Parameter(this,
                    "useMaximumFutureEvents");
            useMaximumFutureEvents.setExpression("false");
            useMaximumFutureEvents.setTypeEquals(BaseType.BOOLEAN);

            maximumFutureEvents = new Parameter(this, "maximumFutureEvents");
            maximumFutureEvents.setExpression("0");
        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }
}