/* An actor that outputs a random sequence with a Binomial distribution.

Copyright (c) 2004-2005 The Regents of the University of California.
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

import cern.jet.random.Binomial;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// Binomial

/**
   Produce a random sequence with a Binomial distribution.  On each
   iteration, a new random number is produced.  The output port is of
   type DoubleToken.  The values that are generated are independent
   and identically distributed with the mean and the standard
   deviation given by parameters.  In addition, the seed can be
   specified as a parameter to control the sequence that is generated.

   <p> This actor instantiates a
   <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/Binomial.html">cern.jet.random.Binomial</a> object with
    n, the number of trials (also known as the sample size) set to 1
    and p, the probability of success, set to 0.5.

    A definition of Binomial by Wolfgang Hoschek can be found at
    <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/stat/Probability.html#binomial(int,%20int,%20double)"><code>http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/stat/Probability.html#binomial(int,%20int,%20double)</code></a>:
<blockquote>
<h3>
binomial</h3>
<pre>public static double <b>binomial</b>(int&nbsp;k,
                              int&nbsp;n,
                              double&nbsp;p)</pre>

<p>Returns the sum of the terms <tt>0</tt> through <tt>k</tt> of the Binomial
 probability density.
 <pre>   k
   --  ( n )   j      n-j
   &gt;   (   )  p  (1-p)
   --  ( j )
  j=0
 </pre>
 The terms are not summed directly; instead the incomplete
 beta integral is employed, according to the formula
 <p>
 <tt>y = binomial( k, n, p ) = Gamma.incompleteBeta( n-k, k+1, 1-p )</tt>.
 </p><p>

 All arguments must be positive,</p>
<p><b>Parameters:</b>
<br><code>k</code> - end term.
<br><code>n</code> - the number of trials.
<br><code>p</code> - the probability of success (must be in <tt>(0.0,1.0)</tt>).
</blockquote>
   The above description of binomial() is
   <a href="doc-files/colt-copyright.htm">copyrighted</a>.

   @author David Bauer and Kostas Oikonomou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class ColtBinomial extends ColtRandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtBinomial(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.INT);

        n = new Parameter(this, "n", new IntToken(1));
        n.setTypeEquals(BaseType.INT);
        p = new Parameter(this, "p", new DoubleToken(0.5));
        p.setTypeEquals(BaseType.DOUBLE);
        p.moveToFirst();
        n.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** n.
     *  This parameter contains a IntToken, initially with value 1.0.
     */
    public Parameter n;

    /** p.
     *  This parameter contains a DoubleToken, initially with value 1.0.
     */
    public Parameter p;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a Binomial distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new IntToken(_current));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method that is called after _randomNumberGenerator is changed.
     */
    protected void _createdNewRandomNumberGenerator() {
        _generator = new Binomial(1, 0.5, _randomNumberGenerator);
    }

    /** Generate a new random number.
     *  @exception If parameter values are incorrect.
     */
    protected void _generateRandomNumber() throws IllegalActionException {
        int nValue = ((IntToken) n.getToken()).intValue();
        double pValue = ((DoubleToken) p.getToken()).doubleValue();
        _current = _generator.nextInt(nValue, pValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The random number for the current iteration. */
    private int _current;

    /** The random number generator. */
    private Binomial _generator;
}
