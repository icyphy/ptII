/* An actor that outputs a random sequence with a Binomial distribution.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
package ptolemy.actor.lib.colt;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import cern.jet.random.Binomial;

//////////////////////////////////////////////////////////////////////////
//// Binomial Selector

/**

 Assign trials from several populations using a conditional Binomial
 selection process.  For example, if a vector of <code>P</code>
 populations are presented (as <code>P</code> input channels) and
 <code>N</code> trials is specified, then this algorithm will
 distribute the <code>N</code> trials based on the proportions
 represented in the <code>P</code> populations.  This is done by
 performing a progressively conditional Binomial selection in which
 <code>n</code> and <code>p</code> change after each trial assignment
 step.  The Binomial trials (<code>n</code>) is decremented after each
 assignment step to represent the remaining trials, and the new
 Binomial probability (<code>p</code>) is calculated based on the
 populations that remain eligible for selection.

 <p> A new set of trial assignments is produced for each iteration and
 will not change until the next iteration.  The values that are
 generated are independent and the expected values of the assignments
 will have expected values that are representative of the population
 proportions.

 @see ptolemy.actor.lib.colt.ColtBinomial
 @see cern.jet.random.Binomial
 @author Raymond A. Cardillo, Matthew J. Robbins
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ColtBinomialSelector extends ColtRandomSource {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtBinomialSelector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        trials = new PortParameter(this, "trials", new IntToken(1));
        trials.setTypeEquals(BaseType.INT);

        populations = new TypedIOPort(this, "populations", true, false);
        populations.setMultiport(true);
        populations.setTypeEquals(BaseType.LONG);

        output.setMultiport(true);
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The total number of trials to assign.  This PortParameter is
     *  of type Int and has an initial default value of 1.
     */
    public PortParameter trials;

    /** The populations to select from.  This multiport is of type Long.
     */
    public TypedIOPort populations;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the trial distributions to the output.
     *  This set of trial distributions is only changed in the
     *  prefire() method, so it will remain constant throughout an
     *  iteration.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        trials.update();
        super.fire();

        for (int i = 0; i < _current.length; i++) {
            output.send(i, _current[i]);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new random number generator.  This method is called
     *  after _randomNumberGenerator is changed.
     */
    protected void _createdNewRandomNumberGenerator() {
        _generator = new Binomial(1, 0.5, _randomNumberGenerator);
    }

    /** Generate a new random number.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    protected void _generateRandomNumber() throws IllegalActionException {
        // Pull out the source values, make sure they're valid, and
        // calculate the total.
        long[] sourceValues = new long[populations.getWidth()];
        long sourceTotal = 0;
        for (int i = 0; i < sourceValues.length; i++) {
            sourceValues[i] = ((LongToken) populations.get(i)).longValue();
            if (sourceValues[i] < 0) {
                throw new IllegalActionException(this, "sourceValue[" + i
                        + "] is negative.");
            }

            sourceTotal += sourceValues[i];
        }

        // Process the binomial selections.
        int trialsRemaining = ((IntToken) trials.getToken()).intValue();
        long sourcePool = sourceTotal;
        _current = new IntToken[sourceValues.length];
        for (int i = 0; i < _current.length; i++) {
            int selected = 0;
            if ((trialsRemaining > 0) && (sourceValues[i] > 0)) {
                double p = (double) sourceValues[i] / (double) sourcePool;
                if (p < 1.0) {
                    selected = _generator.nextInt(trialsRemaining, p);
                } else {
                    selected = trialsRemaining;
                }
                ;
            }

            _current[i] = new IntToken(selected);

            trialsRemaining -= selected;
            sourcePool -= sourceValues[i];
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The tokens to emit during the current iteration. */
    private IntToken _current[];

    /** The random number generator. */
    private Binomial _generator;
}
