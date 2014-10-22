/* Base class for source actors that will only a fire a limited number of times.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// LimitedFiringSource

/**
 Base class for source actors that will only fire a limited number of
 times.  This actor has a parameter,
 <i>firingCountLimit</i>, that optionally limits the number of iterations
 for which the actor is fired.  If this number is <i>n</i> > 0, then
 the <i>n</i>-th invocation of postfire() returns false, which indicates
 to the scheduler that it should stop invocations of this actor.
 The default value of <i>firingCountLimit</i>
 is NONE, which results in postfire always returning
 true.  Derived classes must call super.postfire() for this mechanism to
 work.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bilung)
 */
public class LimitedFiringSource extends Source {
    /** Construct an actor with the given container and name.
     *  The <i>firingCountLimit</i> parameter is also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LimitedFiringSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        firingCountLimit = new Parameter(this, "firingCountLimit");
        firingCountLimit.setExpression("NONE");
        firingCountLimit.setTypeEquals(BaseType.INT);

        Parameter NONE = new Parameter(this, "NONE");

        // Don't setExpression here, it breaks Copernicus, use setToken().
        // NONE.setExpression("0");
        NONE.setToken(IntToken.ZERO);

        // Don't setTypeEquals here, it breaks Copernicus.
        //NONE.setTypeEquals(BaseType.INT);
        NONE.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If a positive integer, then the number of iterations before the
     *  actor indicates to the scheduler that it is finished by returning
     *  false in its postfire() method.
     */
    public Parameter firingCountLimit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which attribute is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == firingCountLimit) {
            _firingCountLimit = ((IntToken) firingCountLimit.getToken())
                    .intValue();
        }
    }

    /** Initialize the iteration counter.  A derived class must call
     *  this method in its initialize() method or the <i>firingCountLimit</i>
     *  feature will not work.
     *  @exception IllegalActionException If the parent class throws it,
     *   which could occur if, for example, the director will not accept
     *   sequence actors.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
    }

    /** Increment the iteration counter, and if it equals the
     *  value of the <i>firingCountLimit</i> parameter, return false.
     *  Otherwise, return true.  Derived classes should call this
     *  at the end of their postfire() method and return its returned
     *  value.
     *  @exception IllegalActionException If firingCountLimit has
     *   an invalid expression.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_firingCountLimit != 0) {
            _iterationCount++;

            if (_iterationCount == _firingCountLimit) {
                return false;
            }
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** This is the value in parameter
     * firingCountLimit.
     * It may be convenient for derived classes to read this
     *  variable in the iterate() method.
     */
    protected int _firingCountLimit;

    /** The current number of elapsed iterations.
     * It may be convenient for derived classes to read/set this
     * variable in the iterate() method.
     */
    protected int _iterationCount = 0;
}
