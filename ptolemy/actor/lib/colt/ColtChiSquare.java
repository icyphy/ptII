/* An actor that outputs a random sequence with a ChiSquare distribution.

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
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import cern.jet.random.ChiSquare;

///////////////////////////////////////////////////////////////////
//// ChiSquare

/**
 Produce a random sequence with a ChiSquare distribution.  On each
 iteration, a new random number is produced.  The output port is of
 type DoubleToken.  The values that are generated are independent
 and identically distributed with the freedom and the standard
 deviation given by parameters.  In addition, the seed can be
 specified as a parameter to control the sequence that is generated.

 <p> This actor instantiates a
 <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/ChiSquare.html">cern.jet.random.ChiSquare</a> object with
 freedom argument set to 1.0.

 The Chi Square Distribution is
 <blockquote>
 A special case of the Gamma distribution.
 <p>
 <tt>p(x) = (1/g(f/2)) * (x/2)^(f/2-1) * exp(-x/2)</tt> with <tt>g(a)</tt> being the gamma function and <tt>f</tt> being the degrees of freedom.
 </p><p>
 Valid parameter ranges: <tt>freedom &gt; 0</tt>.
 </blockquote>

 The above description of Chi Square is
 <a href="doc-files/colt-copyright.htm">copyrighted</a>.

 <p>A definition of the ChiSquare distribution can be found at
 <a href="http://www.cern.ch/RD11/rkb/AN16pp/node31.html#SECTION000310000000000000000"><code>http://www.cern.ch/RD11/rkb/AN16pp/node31.html#SECTION000310000000000000000</code></a>

 @author David Bauer and Kostas Oikonomou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ColtChiSquare extends ColtRandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtChiSquare(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        freedom = new PortParameter(this, "freedom", new DoubleToken(1.0));
        freedom.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(freedom.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        freedom.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** freedom.
     *  This has type double with default value 1.0.
     */
    public PortParameter freedom;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a ChiSquare distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        freedom.update();
        super.fire();
        output.send(0, new DoubleToken(_current));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method that is called after _randomNumberGenerator is changed.
     */
    @Override
    protected void _createdNewRandomNumberGenerator() {
        _generator = new ChiSquare(1.0, _randomNumberGenerator);
    }

    /** Generate a new random number.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    @Override
    protected void _generateRandomNumber() throws IllegalActionException {
        double freedomValue = ((DoubleToken) freedom.getToken()).doubleValue();

        _current = _generator.nextDouble(freedomValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The random number for the current iteration. */
    private double _current;

    /** The random number generator. */
    private ChiSquare _generator;
}
