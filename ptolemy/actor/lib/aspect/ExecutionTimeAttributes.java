/* Container for decorator attributes that are provided to actors by
 * an ExecutionAspect.

@Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.aspect;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionAttributes;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

/**
Container for decorator attributes that are provided to actors by
a ExecutionAspect that schedules execution times.
The ExecutionAspect decorates actors
in a model with the attributes contained by this object.

 @author  Patricia Derler
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ExecutionTimeAttributes extends ExecutionAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public ExecutionTimeAttributes(NamedObj target, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator);
        _init(target);
    }

    /** Constructor to use when parsing a MoML file.
     *  @param target The object being decorated.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public ExecutionTimeAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name);
        _init(target);
    }

    /** The executionTime parameter specifies the execution time of the
     *  decorated object. This means the time that the decorated actor occupies
     *  the decorator resource when it fires.
     *  This is a double that defaults to 0.0.
     */
    public Parameter executionTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  If the attribute is
     *  <i>executionTime</i>, check that it is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == executionTime) {
            double value = ((DoubleToken) executionTime.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(getContainer(),
                        "Cannot specify a negative number for executionTime.");
            }
        }
        super.attributeChanged(attribute);
    }

    /** Return whether the target can have an execution time that can be
     *  simulated. For instance, it does not make sense to simulate execution
     *  time of states in modal models, but it does make sense to monitor
     *  the execution of states such as entry times.
     * @param target The object decorated with the attributes.
     * @return True if execution target can have execution time.
     */
    public boolean canSimulateExecutionFor(NamedObj target) {
        return (target instanceof CompositeActor || target instanceof AtomicActor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the parameters.
     */
    private void _init(NamedObj target) {
        try {
            executionTime = new Parameter(this, "executionTime");
            executionTime.setExpression("0.0");
            executionTime.setTypeEquals(BaseType.DOUBLE);
            if (!canSimulateExecutionFor(target)) {
                executionTime.setVisibility(Settable.NOT_EDITABLE);
            }
        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }

}
