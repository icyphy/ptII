/*

 Copyright (c) 2008 The Regents of the University of California.
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
 * @author tfeng
 *
 */
public class AverageOverTime extends DETransformer {

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public AverageOverTime(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeAtMost(BaseType.SCALAR);
        output.setTypeEquals(BaseType.SCALAR.divide(BaseType.DOUBLE));
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AverageOverTime newObject = (AverageOverTime) super.clone(workspace);
        newObject._sum = null;
        newObject._lastToken = null;
        newObject._lastTime = null;
        return newObject;
    }

    public void fire() throws IllegalActionException {
        super.fire();

        Time modelTime = getDirector().getModelTime();
        DoubleToken currentTime = new DoubleToken(modelTime.getDoubleValue());
        if (_lastToken != null) {
            DoubleToken lastTime = new DoubleToken(_lastTime.getDoubleValue());
            Token increase = _lastToken.multiply(currentTime.subtract(
                    lastTime));
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

    public void initialize() throws IllegalActionException {
        super.initialize();

        _sum = null;
        _lastToken = null;
        _lastTime = getDirector().getModelTime();
    }

    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        _lastTime = getDirector().getModelTime();
        return result;
    }

    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();
        if (!result) {
            result = input.hasToken(0) || !_lastTime.equals(
                    getDirector().getModelTime());
        }
        return result;
    }

    private Time _lastTime;

    private Token _lastToken;

    private Token _sum;
}
