/* An actor that outputs a sequence with a given step in values.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

package doc.books.systems.architecture.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Ramp

/**
 An actor that outputs the number of times fire has been called.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Count extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Count(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        trigger = new TypedIOPort(this, "trigger", true, false);
        initial = new Parameter(this, "initial", new IntToken(0));
        initial.setTypeEquals(BaseType.INT);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);
    }

    /** The trigger input port. */
    public TypedIOPort trigger;

    /** The output port. */
    public TypedIOPort output;

    /** The initial count. */
    public Parameter initial;

    /** Reset the count to the initial value. */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = ((IntToken) initial.getToken()).intValue();
    }

    /** Consume the trigger input and output the incremented count. */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (trigger.getWidth() > 0 && trigger.hasToken(0)) {
            trigger.get(0);
        }
        output.send(0, new IntToken(_count + 1));
    }

    /** Record the most updated count. */
    @Override
    public boolean postfire() throws IllegalActionException {
        _count += 1;
        return super.postfire();
    }

    /** The local variable. */
    private int _count = 0;
}
