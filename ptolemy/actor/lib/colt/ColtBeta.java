/* An actor that outputs a random sequence with a Beta distribution.

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
import cern.jet.random.Beta;

///////////////////////////////////////////////////////////////////
//// Beta

/**
 Produce a random sequence with a Beta distribution.  On each
 iteration, a new random number is produced.  The output port is of
 type DoubleToken.  The values that are generated are independent
 and identically distributed with the mean and the standard
 deviation given by parameters.  In addition, the seed can be
 specified as a parameter to control the sequence that is generated.

 <p> This actor instantiates a
 <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/Beta.html">cern.jet.random.Beta</a> object with
 alpha and beta both set to 2.0.

 <p>A definition of NegativeBinomial by Wolfgang Hoschek can be found at
 <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/stat/Probability.html#beta(double,%20double,%20double)"><code>http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/stat/Probability.html#beta(double,%20double,%20double)</code></a>:</p>
 <blockquote>
 <h3>beta</h3>
 <pre>public static double <b>beta</b>(double&nbsp;a,
 double&nbsp;b,
 double&nbsp;x)</pre>
 <p>Returns the area from zero to <tt>x</tt> under the beta density
 function.</p>
 <pre>                          x
            -             -
           | (a+b)       | |  a-1      b-1
 P(x)  =  ----------     |   t    (1-t)    dt
           -     -     | |
          | (a) | (b)   -
                         0
 </pre>

 <p>This function is identical to the incomplete beta
 integral function <tt>Gamma.incompleteBeta(a, b, x)</tt>.</p>

 <p>The complemented function is</p>

 <p><tt>1 - P(1-x)  =  Gamma.incompleteBeta( b, a, x )</tt>;</p>
 </blockquote>

 The above description of beta() is
 <a href="doc-files/colt-copyright.htm">copyrighted</a>.
 <br>In this actor, <i>alpha</i> corresponds with <i>a</i>
 <i>beta</i> corresponds with <i>b</i>.


 @author David Bauer and Kostas Oikonomou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ColtBeta extends ColtRandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtBeta(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        alpha = new PortParameter(this, "alpha");
        alpha.setExpression("2.0");
        alpha.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(alpha.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        beta = new PortParameter(this, "beta");
        beta.setExpression("2.0");
        beta.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(beta.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        beta.moveToFirst();
        alpha.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Alpha.
     *  This has type double with default 2.0.
     */
    public PortParameter alpha;

    /** Beta.
     *  This has type double with default 2.0.
     */
    public PortParameter beta;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a Beta distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        alpha.update();
        beta.update();
        super.fire();
        output.send(0, new DoubleToken(_current));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method that is called after _randomNumberGenerator is changed.
     */
    @Override
    protected void _createdNewRandomNumberGenerator() {
        _generator = new Beta(2.0, 2.0, _randomNumberGenerator);
    }

    /** Generate a new random number.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    @Override
    protected void _generateRandomNumber() throws IllegalActionException {
        double alphaValue = ((DoubleToken) alpha.getToken()).doubleValue();
        double betaValue = ((DoubleToken) beta.getToken()).doubleValue();
        _current = _generator.nextDouble(alphaValue, betaValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The random number for the current iteration. */
    private double _current;

    /** The random number generator. */
    private Beta _generator;
}
