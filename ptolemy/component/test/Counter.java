/* An up-down counter.

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
package ptolemy.component.test;

import ptolemy.component.AtomicComponent;
import ptolemy.component.MethodCallPort;
import ptolemy.data.IntToken;
import ptolemy.data.TupleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Counter

/**
 This component implements an up-down counter.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (ellen_zh)
 @Pt.AcceptedRating Red (neuendor)
 */
public class Counter extends AtomicComponent {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Counter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        increment = new MethodCallPort(this, "increment", true) {
            @Override
            public synchronized TupleToken call(TupleToken args) {
                System.out.println("---call method Counter.increment");

                IntToken arg = (IntToken) args.getElement(0);
                _count += arg.intValue();

                IntToken[] t = new IntToken[1];
                t[0] = new IntToken(_count);
                return output.call(new TupleToken(t));
            }
        };

        //increment.setTypeEquals(BaseType.GENERAL);
        decrement = new MethodCallPort(this, "decrement", true);

        //decrement.setTypeEquals(BaseType.GENERAL);
        output = new MethodCallPort(this, "output", false);

        //output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The increment port. If this input port
     *  receives a token, then the counter is incremented.  The port
     *  has type general.
     */
    public MethodCallPort increment;

    /** The decrement port. If this input port
     *  receives a token, then the counter is decremented.  The port
     *  has type general.
     */
    public MethodCallPort decrement;

    /** The output port with type IntToken.
     */
    public MethodCallPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the count of inputs to zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _count = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private int _count = 0;
}
