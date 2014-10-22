/* An actor that outputs a random sequence with a Zeta distribution.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import cern.jet.random.Zeta;

///////////////////////////////////////////////////////////////////
//// Zeta

/**
 Produce a random sequence with a Zeta distribution.  On each
 iteration, a new random number is produced.  The output port is of
 type DoubleToken.  The values that are generated are independent
 and identically distributed with the mean and the standard
 deviation given by parameters.  In addition, the seed can be
 specified as a parameter to control the sequence that is generated.

 <p> This actor instantiates a
 <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/Zeta.html">cern.jet.random.Zeta</a> object with
 ro and pk set to 1.0.

 @author David Bauer and Kostas Oikonomou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ColtZeta extends ColtRandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtZeta(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.INT);

        ro = new PortParameter(this, "ro", new DoubleToken(1.0));
        ro.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(ro.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        pk = new PortParameter(this, "pk", new DoubleToken(1.0));
        pk.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(pk.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        pk.moveToFirst();
        ro.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** ro.
     *  This has type double, initially with value 1.0.
     */
    public PortParameter ro;

    /** pk.
     *  This has type double, initially with value 1.0.
     */
    public PortParameter pk;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a Zeta distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        ro.update();
        pk.update();
        super.fire();
        output.send(0, new IntToken(_current));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method that is called after _randomNumberGenerator is changed.
     */
    @Override
    protected void _createdNewRandomNumberGenerator() {
        _generator = new Zeta(1.0, 1.0, _randomNumberGenerator);
    }

    /** Generate a new random number.
     *  @exception IllegalActionException If the parameter values are incorrect.
     */
    @Override
    protected void _generateRandomNumber() throws IllegalActionException {
        double roValue = ((DoubleToken) ro.getToken()).doubleValue();
        double pkValue = ((DoubleToken) pk.getToken()).doubleValue();

        _generator.setState(roValue, pkValue);
        _current = _generator.nextInt();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The random number for the current iteration. */
    private int _current;

    /** The random number generator. */
    private Zeta _generator;
}
