/* Base class for Colt Random Sources.

Copyright (c) 2004 The Regents of the University of California.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.String;

import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.actor.lib.RandomSource;
import ptolemy.actor.util.FunctionDependency;

import ptolemy.data.expr.Parameter;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import cern.jet.random.AbstractDistribution;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;

import edu.cornell.lassp.houle.RngPack.Ranecu;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.cornell.lassp.houle.RngPack.Ranmar;

import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// ColtRandomSource
/** Base class for Colt random sources.

   @author David Bauer and Kostas Oikonomou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public abstract class ColtRandomSource extends RandomSource
    implements ChangeListener {

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

        _index = 0;
        _seed = 1;
        _randomNumberGenerator = new DRand((int)_seed);

        _removeAttribute(super.seed);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameter               ////

    /** The desired low-level random number generator (RNG) class name.
     *  The {@link #_randomNumberGeneratorClassNames} array contains
     *  available class names.   The value of this parameter is shared
     *  between all Colt random source actors.  The initial default
     *  is the String "DRand".
     *  <p>Below are possible values for this parameter and links
     *  to pages that define the meaning of the parameter.
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
    public Parameter randomNumberGeneratorClass = null;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the changeFailed method in ChangeListener.
     *  No defined processing.
     */
    public void changeFailed(ChangeRequest req, Exception e) {
        if (_debugging) {
            _debug("Changed failed?" + req + "\n" + e);
        }
    }

    /** Override the changeExecuted method in ChangeListener.
     *  When a change is made, setup the correct randomNumberGenerator.
     */
    public void changeExecuted(ChangeRequest req) {
        if (_debugging) {
            _debug("Request desc: " + req.getDescription());
        }

        if (-1 != req.getDescription().indexOf("seed")) {
            try {
                _seed = ((LongToken) (seed.getToken())).longValue();
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(this, ex,
                        "Failed to get Colt seed");
            }
            return;
        }

        if (-1 == req.getDescription().indexOf("randomNumberGenerator")) {
            return;
        }

        String reClass = randomNumberGeneratorClass.getExpression();

        if (-1 != reClass.indexOf(_randomNumberGeneratorClassNames[0])
                && _index != 0) {
            _randomNumberGenerator = new DRand((int)_seed);
            _index = 0;
        } else if (-1 != reClass.indexOf(_randomNumberGeneratorClassNames[1])
                && _index != 1) {
            _randomNumberGenerator = new MersenneTwister((int)_seed);
            _index = 1;
        } else if (-1 != reClass.indexOf(_randomNumberGeneratorClassNames[2])
                && _index != 2) {
            _randomNumberGenerator = new Ranecu(_seed);
            _index = 2;
        } else if (-1 != reClass.indexOf(_randomNumberGeneratorClassNames[3])
                && _index != 3) {
            _randomNumberGenerator = new Ranlux(_seed);
            _index = 3;
        } else if (-1 != reClass.indexOf(_randomNumberGeneratorClassNames[4])
                && _index != 4) {
            _randomNumberGenerator = new Ranmar(_seed);
            _index = 4;
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then set the filename public member.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ColtRandomSource newObject = (ColtRandomSource)super.clone(workspace);
        newObject._randomNumberGenerator = new DRand((int)_seed);
        return newObject;
    }

    /** Send a random number to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Calculate the next random number.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** This method creates the parameter object if it does not yet exist,
     *  and sets it in the container, or simply gets it from the container
     *  and adds it to the high level RNG actor.
     * @param container  The container.
     * @return a Parameter that contains the random number generator class
     * @exception IllegalActionException If there is a problem getting the
     * randomNumberGenerator or seed attribute
     * @exception NameDuplicationException If there is a problem adding the
     * randomNumberGenerator or seed attribute.
     */
    protected Parameter _getRandomNumberGeneratorClass(
            CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        randomNumberGeneratorClass =
            (Parameter) container.getAttribute("randomNumberGenerator");
        seed = (Parameter) container.getAttribute("seed");

        if (randomNumberGeneratorClass == null) {
            randomNumberGeneratorClass =
                new Parameter(container, "randomNumberGenerator",
                        new StringToken(
                                _randomNumberGeneratorClassNames[_index]));

            ChoiceStyle s = new ChoiceStyle(randomNumberGeneratorClass, "s");
            for (int i = 0; i < _randomNumberGeneratorClassNames.length; i++) {
                Parameter a =
                    new Parameter(s, "s" + i,
                            new StringToken(
                                    _randomNumberGeneratorClassNames[i]));
            }
        }

        if (seed == null) {
            seed =
                new Parameter(container, "seed", new LongToken(_seed));
            seed.setTypeEquals(BaseType.LONG);
        }

        _addAttribute(randomNumberGeneratorClass);
        _addAttribute(seed);

        randomNumberGeneratorClass.addChangeListener(this);
        seed.addChangeListener(this);

        if (_randomNumberGenerator == null) {
            throw new InternalErrorException(this, null,
                    "Unable to create randomNumberGenerator!");
        }
        if (_debugging) {
            _debug("ColtRandomSource: random number generator = "
                    + randomNumberGeneratorClass);
        }

        return randomNumberGeneratorClass;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The high-level RNG.
     */
    protected AbstractDistribution _rng;

    /** The low-level RNG shared between all high level RNGs.
     */
    protected static RandomElement _randomNumberGenerator;

    /** The list of available RandomElement classes.
     */
    protected String [] _randomNumberGeneratorClassNames =
    {
        "DRand",
        "MersenneTwister (MT19937)",
        "Ranecu",
        "Ranlux",
        "Ranmar"
    };


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The index of the currently used _randomNumberGenerator in the
     *  _randomNumberGeneratorClassNames array.
     */
    private static int _index = 0;

    /** _seed.
     *  The actual _seed value as int.
     */
    private static long _seed = 1;
}
