/* Base class for Colt Random Sources.

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

import java.util.Iterator;

import ptolemy.actor.lib.Source;
import ptolemy.data.BooleanToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.SharedParameter;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import edu.cornell.lassp.houle.RngPack.Ranecu;
import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.cornell.lassp.houle.RngPack.Ranmar;


//////////////////////////////////////////////////////////////////////////
//// ColtRandomSource

/** Base class for Colt random sources.
    This base class manages the seed and the choice of
    random number generator class.  When the random number
    generator class is set for any one actor in a model,
    it gets set to match in all other actors within the
    same top-level container (unless this actor is within
    an EntityLibrary).  When the seed gets set
    to 0L in any one actor in a model, it gets set
    to 0L for all other actors within the same top-level
    container. When it gets set to anything other than
    0L, then it will be set for all other actors in
    the model in a pattern that ensures that every
    actor has a different seed.
    <p>
    A seed of zero is interpreted to mean that no seed is specified,
    which means that each execution of the model could result in
    distinct data. For the value 0, the seed is set to
    System.currentTimeMillis() + hashCode(), which means that
    with extremely high probability, two distinct actors will have
    distinct seeds.  The seed is set when the seed parameter value
    is given, typically right after construction of the actor.
    Subsequent runs of the same model, therefore, will continue
    using the same seed.
    <p>
    If the <i>resetOnEachRun</i> parameter is true (it is
    false by default), then each run resets the random number
    generator. If the seed is non-zero, then this makes
    each run identical.  This is useful for constructing
    tests. If the seed is zero, then a new seed is generated
    on each run using the same technique described above
    (combining current time and the hash code).

    @author David Bauer, Kostas Oikonomou, and Edward A. Lee
    @version $Id$
    @since Ptolemy II 4.1
    @Pt.ProposedRating Yellow (eal)
    @Pt.AcceptedRating Red (cxh)
*/
public abstract class ColtRandomSource extends Source {
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

        seed = new ColtSeedParameter(this, "seed", ColtRandomSource.class);

        generatorClass = new SharedParameter(this, "generatorClass",
                ColtRandomSource.class, "DRand");
        generatorClass.setStringMode(true);

        generatorClass.addChoice("DRand");
        generatorClass.addChoice("MersenneTwister (MT19937)");
        generatorClass.addChoice("Ranecu");
        generatorClass.addChoice("Ranlux");
        generatorClass.addChoice("Ranmar");

        _inferSeed();

        resetOnEachRun = new SharedParameter(this, "resetOnEachRun",
                ColtRandomSource.class, "false");
        resetOnEachRun.setTypeEquals(BaseType.BOOLEAN);
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

    /** If true, this parameter specifies that the random number
     *  generator should be reset on each run of the model (in
     *  the initialize() method). It is a boolean that defaults
     *  to false. This is a shared parameter, meaning that changing
     *  it somewhere in the model causes it to be changed everywhere
     *  in the model.
     */
    public SharedParameter resetOnEachRun;

    /** The seed that controls the random number generation.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  in which case, the seed is set to
     *  System.currentTimeMillis() + hashCode(), which means that
     *  with extremely high probability, two distinct actors will have
     *  distinct seeds.  This is a "shared parameter", which means that
     *  changing this parameter in any one actor will cause
     *  a similar change to be applied to all other Colt
     *  actors within the same top level (unless this is in
     *  a library). In particular, if its value is set to zero,
     *  then all other shared seeds will be set to zero. If it
     *  is set to something other than zero, then all other shared
     *  seeds are set to unique numbers.
     *  This parameter has type long and defaults to zero.
     */
    public ColtSeedParameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>generatorClass</i> or <i>seed</i>
     *  then create the base random number generator.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == generatorClass) {
            String generatorClassValue = ((StringToken) generatorClass.getToken())
                .stringValue();

            if ((generatorClassValue != null)
                    && !generatorClassValue.equals(_generatorClassName)) {
                _createGenerator();
            }
        } else if (attribute == seed) {
            long seedValue = ((LongToken) (seed.getToken())).longValue();

            if (seedValue != _generatorSeed) {
                _createGenerator();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ColtRandomSource newObject = (ColtRandomSource) (super.clone(workspace));
        newObject._randomNumberGenerator = null;

        // Force creation of a new generator.
        try {
            newObject._createGenerator();
        } catch (IllegalActionException ex) {
            throw new CloneNotSupportedException("Failed to create generator: "
                    + ex);
        }

        return newObject;
    }

    /** Generate a new random number if this is the first firing
     *  of the iteration.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        if (_needNew) {
            _generateRandomNumber();
            _needNew = false;
        }
    }

    /** Initialize the random number generator with the seed, if it
     *  has been given.  A seed of zero is interpreted to mean that no
     *  seed is specified.  In such cases, a seed based on the current
     *  time and this instance of a RandomSource is used to be fairly
     *  sure that two identical sequences will not be returned.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (((BooleanToken) resetOnEachRun.getToken()).booleanValue()) {
            _createGenerator();
        }

        _needNew = true;
    }

    /** Calculate the next random number.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean postfire() throws IllegalActionException {
        _needNew = true;
        return super.postfire();
    }

    /** Override the base class to infer the value of the
     *  shared parameters from the new container.
     *  @param container The new container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (generatorClass != null) {
            generatorClass.inferValueFromContext("DRand");
        }

        if (resetOnEachRun != null) {
            resetOnEachRun.inferValueFromContext("false");
        }

        _inferSeed();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method that is called after _randomNumberGenerator is changed.
     */
    protected abstract void _createdNewRandomNumberGenerator();

    /** Generate a new random number.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes may throw it if there is a problem processing
     *  a parameter or if there is some other problem.
     */
    protected abstract void _generateRandomNumber()
            throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The low-level random number generator.
     */
    protected RandomElement _randomNumberGenerator;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the random number generator using current parameter values. */
    private void _createGenerator() throws IllegalActionException {
        long seedValue = ((LongToken) (seed.getToken())).longValue();
        _generatorSeed = seedValue;

        if (seedValue == 0L) {
            seedValue = System.currentTimeMillis() + hashCode();
        }

        StringToken generatorToken = ((StringToken) generatorClass.getToken());
        String generatorClassValue = null;

        if (generatorToken != null) {
            generatorClassValue = generatorToken.stringValue();
        }

        _generatorClassName = generatorClassValue;

        if ((generatorClassValue == null)
                || generatorClassValue.equals("DRand")) {
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

        _createdNewRandomNumberGenerator();
    }

    /** Infer the value of the seed from the container context.
     *  The inferred value is zero if all shared parameters have value
     *  zero, or if there are no shared parameters. Otherwise, it is
     *  one larger than the largest value encountered.
     */
    private void _inferSeed() throws IllegalActionException {
        // If the seed parameter has not yet been constructed, then
        // do nothing.
        if (seed == null) {
            return;
        }

        String seedValue = "0L";
        NamedObj root = seed.getRoot();

        if (root != null) {
            Iterator sharedParameters = seed.sharedParameterList(root).iterator();
            long value = 0L;

            while (sharedParameters.hasNext()) {
                ColtSeedParameter candidate = (ColtSeedParameter) sharedParameters
                    .next();

                if (candidate != seed) {
                    long candidateValue = ((LongToken) candidate.getToken())
                        .longValue();

                    if (candidateValue != 0L) {
                        if (candidateValue >= value) {
                            value = candidateValue + 1L;
                        }
                    }
                }
            }

            seedValue = value + "L";
        }

        seed.setSuppressingPropagation(true);
        seed.setExpression(seedValue);
        seed.setSuppressingPropagation(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The class name of the current _randomNumberGenerator.
     */
    private String _generatorClassName;

    /** The seed used by the current _randomNumberGenerator.
     */
    private long _generatorSeed = 0L;

    /** Indicator that a new random number is needed.
     */
    private boolean _needNew = false;

    /** Counter used to assign unique seeds.
     */
    private long _seedCount = 0;
}
