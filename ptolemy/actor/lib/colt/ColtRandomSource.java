/* Base class for Colt Random Sources.

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

import java.util.Random;

import ptolemy.actor.lib.RandomSource;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import edu.cornell.lassp.houle.RngPack.Ranecu;
import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.cornell.lassp.houle.RngPack.Ranmar;

///////////////////////////////////////////////////////////////////
//// ColtRandomSource

/** Base class for Colt random sources.
 This base class manages the choice of
 random number generator class.  When the random number
 generator class is set for any one actor in a model,
 it gets set to match in all other actors within the
 same top-level container (unless this actor is within
 an EntityLibrary).

 @author David Bauer, Kostas Oikonomou, and Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class ColtRandomSource extends RandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtRandomSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        generatorClass = new SharedParameter(this, "generatorClass",
                ColtRandomSource.class, "DRand");
        generatorClass.setStringMode(true);

        generatorClass.addChoice("DRand");
        generatorClass.addChoice("MersenneTwister (MT19937)");
        generatorClass.addChoice("Ranecu");
        generatorClass.addChoice("Ranlux");
        generatorClass.addChoice("Ranmar");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameter               ////

    /** The low-level random number generator (RNG) class name.
     *  This is a string that defaults to "DRand".
     *  Below are possible values for this parameter and links
     *  to pages that define the meaning of the parameter.
     *  This is a "shared parameter", which means that
     *  changing this parameter in any one actor will cause
     *  a similar change to be applied to all other Colt
     *  actors within the same top level (unless this is in
     *  a library).
     *  <menu>
     *  <li><code>"DRand"</code>
     *  (<a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/engine/DRand.html#in_browser">Definition</a>)
     *  <li><code>"MersenneTwister (MT19937)"</code>
     *  (<a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/engine/MersenneTwister.html#in_browser">Definition</a>)
     *  <li><code>"Ranecu""</code>
     *  (<a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/edu/cornell/lassp/houle/RngPack/Ranecu.html#in_browser">Definition</a>)
     *  <li><code>"Ranlux"</code>
     *  (<a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/engine/Ranlux.html#in_browser">Definition</a>)
     *  <li><code>"Ranmar"</code>
     *  (<a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/engine/Ranmar.html#in_browser">Definition</a>)
     *  </menu>
     */
    public SharedParameter generatorClass;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>generatorClass</i>
     *  then create the base random number generator.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == generatorClass) {
            String generatorClassValue = ((StringToken) generatorClass
                    .getToken()).stringValue();

            if (generatorClassValue != null
                    && !generatorClassValue.equals(_generatorClassName)) {
                _needNewGenerator = true;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method that is called after _randomNumberGenerator is changed. */
    protected abstract void _createdNewRandomNumberGenerator();

    /** Create the random number generator using current parameter values. */
    @Override
    protected void _createGenerator() throws IllegalActionException {
        long seedValue = ((LongToken) seed.getToken()).longValue();
        Token token = privateSeed.getToken();
        if (token != null) {
            seedValue = ((LongToken) token).longValue();
            _generatorSeed = seedValue;
        } else {
            _generatorSeed = seedValue;
            if (seedValue == 0L) {
                seedValue = System.currentTimeMillis() + hashCode();
            } else {
                // BTW - the reason to use the full name here is so that
                // each random number generator generates a sequence
                // of different random numbers.  If we use just the
                // display name, then two actors that have the same
                // name will generate the same sequence of numbers which
                // is bad for Monte Carlo simulations.
                // See privateSeed in RandomSource for an alternate
                // way to set seeds.
                seedValue = seedValue + getFullName().hashCode();
            }
        }

        StringToken generatorToken = (StringToken) generatorClass.getToken();
        String generatorClassValue = null;

        if (generatorToken != null) {
            generatorClassValue = generatorToken.stringValue();
        }

        _generatorClassName = generatorClassValue;

        if (generatorClassValue == null || generatorClassValue.equals("DRand")) {
            _randomNumberGenerator = new DRand((int) seedValue);
        } else if (generatorClassValue.equals("MersenneTwister (MT19937)")) {
            _randomNumberGenerator = new MersenneTwister((int) seedValue);
        } else if (generatorClassValue.equals("Ranecu")) {
            _randomNumberGenerator = new Ranecu((int) seedValue);
        } else if (generatorClassValue.equals("Ranlux")) {
            _randomNumberGenerator = new Ranlux((int) seedValue);
        } else if (generatorClassValue.equals("Ranmar")) {
            _randomNumberGenerator = new Ranmar((int) seedValue);
        }

        // In the base class, if _random is null, then initialize()
        // will re-run this method. We don't want this, so even though
        // we don't need it, we create an instance of Random.
        _random = new Random();
        _needNewGenerator = false;
        _needNew = true;

        _createdNewRandomNumberGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The low-level random number generator.
     */
    protected RandomElement _randomNumberGenerator;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The class name of the current _randomNumberGenerator.
     */
    private String _generatorClassName;
}
