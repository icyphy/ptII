package ptolemy.actor.lib;

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
import ptolemy.kernel.util.NameDuplicationException;

import cern.jet.random.AbstractDistribution;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;

import edu.cornell.lassp.houle.RngPack.Ranecu;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.cornell.lassp.houle.RngPack.Ranmar;

import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;

public abstract class ColtRandomSource extends RandomSource implements ChangeListener
{

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtRandomSource(CompositeEntity container, String name)
                                    throws NameDuplicationException, IllegalActionException
    {
        super(container, name);

        index = 0;
        _coltSeed = 1;
        randomElement = new DRand((int)_coltSeed);

        _removeAttribute(super.seed);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method creates the parameter object if it does not yet exist,
     *  and sets it in the container, or simply gets it from the container
     *  and adds it to the high level RNG actor.
     */
    public Parameter getRandomElementClass(CompositeEntity container)
                                throws IllegalActionException, NameDuplicationException
    {
        randomElementClass = (Parameter) container.getAttribute("Random Number Generator");
        coltSeed = (Parameter) container.getAttribute("coltSeed");

        if(randomElementClass == null)
        {
                randomElementClass = new Parameter(container, "Random Number Generator",
                                new StringToken(randomElementClassNames[index]));

                ChoiceStyle s = new ChoiceStyle(randomElementClass, "s");
                for(int i = 0; i < randomElementClassNames.length; i++)
                {
                        Parameter a =
                                new Parameter(s, "s"+i, new StringToken(randomElementClassNames[i]));
                }
        }

        if(coltSeed == null)
        {
                coltSeed = new Parameter(container, "coltSeed", new LongToken(_coltSeed));
                coltSeed.setTypeEquals(BaseType.LONG);
        }

        _addAttribute(randomElementClass);
        _addAttribute(coltSeed);

        randomElementClass.addChangeListener(this);
        coltSeed.addChangeListener(this);

        if(randomElement == null)
                System.err.println("Unable to create randomElement!");

        return randomElementClass;
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

    /** Override the changeFailed method in ChangeListener.
     *  No defined processing.
     */
    public void changeFailed(ChangeRequest req, Exception e)
    {
        //System.err.println("Changed failed");
    }

    /** Override the changeExecuted method in ChangeListener.
     *  When a change is made, setup the correct randomElement.
     */
    public void changeExecuted(ChangeRequest req)
    {
        //System.err.println("Request desc: " + req.getDescription());

        if(-1 != req.getDescription().indexOf("coltSeed"))
        {
                try
                {
                        _coltSeed = ((LongToken) (coltSeed.getToken())).longValue();
                } catch(IllegalActionException e)
                {
                        e.printStackTrace();
                }

                //System.err.println("New _coltSeed: " + _coltSeed);
                return;
        }

        if(-1 == req.getDescription().indexOf("Random Number Generator"))
                return;

        String reClass = randomElementClass.getExpression();

        if(-1 != reClass.indexOf(randomElementClassNames[0]) && index != 0)
        {
                randomElement = new DRand((int)_coltSeed);
                index = 0;
        } else if(-1 != reClass.indexOf(randomElementClassNames[1]) && index != 1)
        {
                randomElement = new MersenneTwister((int)_coltSeed);
                index = 1;
        } else if(-1 != reClass.indexOf(randomElementClassNames[2]) && index != 2)
        {
                randomElement = new Ranecu(_coltSeed);
                index = 2;
        } else if(-1 != reClass.indexOf(randomElementClassNames[3]) && index != 3)
        {
                randomElement = new Ranlux(_coltSeed);
                index = 3;
        } else if(-1 != reClass.indexOf(randomElementClassNames[4]) && index != 4)
        {
                randomElement = new Ranmar(_coltSeed);
                index = 4;
        }

        //System.err.println("New randomElement: " + index);
        //System.err.println("Changed executed");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** randomElementClass.
     *  This parameter contains the desired low-level RNG class name.
     */
    protected Parameter randomElementClass = null;

    /** coltSeed.
     *  This int contains the _coltSeed for the RNG.
     */
    protected Parameter coltSeed = null;

    /** rng.
     *  This is the high-level RNG.
     */
    protected AbstractDistribution rng;

    /** randomElement.
     *  This is the low-level RNG shared between all high level RNGs.
     */
    protected static RandomElement randomElement;

    /** randomElementClassNames.
     *  This is the list of available RandomElement classes.
     */
    protected String [] randomElementClassNames =
    {
        "DRand",
        "MersenneTwister (MT19937)",
        "Ranecu",
        "Ranlux",
        "Ranmar"
    };

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** index.
     *  This int is the index of the currently used randomElement in the
     *  randomElementClassNames array.
     */
    private static int index = 0;

    /** _coltSeed.
     *  The actual _coltSeed value as int.
     */
    private static long _coltSeed = 1;
}
