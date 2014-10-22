/** Director that invokes the test() method on objects implementing Testable.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.domains.tester.kernel;

import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.tester.lib.Testable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This director does not execute a model, but instead looks for
 *  attributes contained by the model at the same level of the
 *  hierarchy as this director that implement the Testable
 *  interface. If the <i>train</i> parameter is false, it
 *  invokes the test() method on them, and otherwise, it invokes
 *  the train() method on them. It does everything
 *  in preinitialize(). It first invokes preinitialize() on all contained
 *  actors, and then runs test() or train() on all contained Testable attributes.
 *  If any of those throws an exception, then preinitialize() throws
 *  an exception.
 *  @author Christopher Brooks, Dai Bui, Edward A. Lee, Ben Lickly
@version $Id$
@since Ptolemy II 8.0
 */
public class TesterDirector extends Director {

    /** Construct a director.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    public TesterDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        trainingMode.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indicator of whether to invoke train() or test() on the Testable
     *  objects. This defaults to false which means test().
     */
    public Parameter trainingMode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing. */
    @Override
    public void addInitializable(Initializable initializable) {
    }

    /** Do nothing. */
    @Override
    public void fire() {
    }

    /** Do nothing. */
    @Override
    public void initialize() {
    }

    /** Return false, indicating that we are done. */
    @Override
    public boolean postfire() {
        return false;
    }

    /** Return true. */
    @Override
    public boolean prefire() {
        return true;
    }

    /** Override the base class to first invoke preinitialize() on all
     *  contained actors and then invoke either test() or train() on all
     *  Testable attributes.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        boolean training = ((BooleanToken) trainingMode.getToken())
                .booleanValue();
        List<Testable> testables = getContainer().attributeList(Testable.class);
        for (Testable testable : testables) {
            if (training) {
                testable.train();
            } else {
                testable.test();
            }
        }
    }

    /** Do nothing. */
    @Override
    public boolean transferInputs(IOPort port) {
        return false;
    }

    /** Do nothing. */
    @Override
    public boolean transferOutputs(IOPort port) {
        return false;
    }

    /** Do nothing. */
    @Override
    public void wrapup() {
    }
}
