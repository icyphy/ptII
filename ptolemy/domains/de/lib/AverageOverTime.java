/* An actor to compute the average of the input values received so far over
model time.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 An actor to compute the average of the input values received so far over model
 time. This actor can fire whenever there is a token at its input port, or the
 model time has advanced since last firing. It records the last token that it
 received. In each firing, the produce of the last token and the amount of model
 time increment is added to a sum variable. The average value received so far is
 obtained by dividing the sum by the current model time. This average is sent to
 the output port in each firing. If a token is available at the input port, this
 actor reads that token and overwrites the last token with it.

 @author tfeng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AverageOverTime extends DETransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AverageOverTime(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeAtMost(BaseType.SCALAR);
        output.setTypeEquals(BaseType.SCALAR.divide(BaseType.DOUBLE));
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new AverageOverTime actor.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AverageOverTime newObject = (AverageOverTime) super.clone(workspace);
        newObject._sum = null;
        newObject._lastToken = null;
        newObject._lastTime = null;

        newObject.input.setTypeAtMost(BaseType.SCALAR);
        newObject.output.setTypeEquals(BaseType.SCALAR.divide(BaseType.DOUBLE));

        return newObject;
    }

    /** Fire this actor. If there is a least one token at the input port,
     *  consume the first token. Output the current average value via the output
     *  port.
     *  @exception IllegalActionException If thrown when trying to read the
     *  input token or to write the output token.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        Time modelTime = getDirector().getModelTime();
        DoubleToken currentTime = new DoubleToken(modelTime.getDoubleValue());
        if (_lastToken != null) {
            DoubleToken lastTime = new DoubleToken(_lastTime.getDoubleValue());
            Token increase = _lastToken
                    .multiply(currentTime.subtract(lastTime));
            if (_sum == null) {
                _sum = increase;
            } else {
                _sum = _sum.add(increase);
            }
        }
        _lastTime = modelTime;
        if (input.hasToken(0)) {
            _lastToken = input.get(0);
        }
        if (_sum != null) {
            output.broadcast(_sum.divide(currentTime));
        }
    }

    /** Initialize this actor.
     *
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _sum = null;
        _lastToken = null;
        _lastTime = getDirector().getModelTime();
    }

    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        _lastTime = getDirector().getModelTime();
        return result;
    }

    /** Determine whether this actor can fire in the current iteration. Return
     *  true if there is at least one token at the input port, or the model time
     *  has advanced since last firing.
     *
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();
        if (!result) {
            result = input.hasToken(0)
                    || !_lastTime.equals(getDirector().getModelTime());
        }
        return result;
    }

    // The last time at which this actor was fired.
    private Time _lastTime;

    // The last received input token.
    private Token _lastToken;

    // The sum accumulated so far.
    private Token _sum;
}
