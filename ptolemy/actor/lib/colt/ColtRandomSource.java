package ptolemy.actor.lib.colt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.actor.lib.RandomSource;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.data.LongToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import cern.jet.random.AbstractDistribution;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.Ranecu;
import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.cornell.lassp.houle.RngPack.Ranmar;
import edu.cornell.lassp.houle.RngPack.RandomElement;

public abstract class ColtRandomSource extends RandomSource
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
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        if(-1 == index)
            {
                index = 0;
                randomElement = new DRand(coltSeed);
            }

        randomElementClass = new StringAttribute(this, "Random Number Generator");
        randomElementClass.setExpression(randomElementClassNames[index]);

        style = new ChoiceStyle(randomElementClass, "style");

        for (int i = 0; i < randomElementClassNames.length; i++)
            {
                a = new StringAttribute(style, "style" + i);
                a.setExpression(randomElementClassNames[i]);
            }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static StringAttribute randomElementClass;
    public static ChoiceStyle style = null;
    public static StringAttribute a;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a Gamma distribution to the output.
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

    public void attributeChanged(Attribute attribute) throws IllegalActionException
    {
        if (null != seed) {
            coltSeed = (int) ((LongToken) seed.getToken()).longValue();
        }

        if (attribute == randomElementClass) {
            String reClass = ((StringAttribute) attribute).getExpression();

            System.err.println("Old randomElement: " + index);

            if(reClass.equals(randomElementClassNames[0])) {
                randomElement = new DRand(coltSeed);
                index = 0;
            } else if(reClass.equals(randomElementClassNames[1])) {
                randomElement = new MersenneTwister(coltSeed);
                index = 1;
            } else if(reClass.equals(randomElementClassNames[2])) {
                randomElement = new Ranecu(coltSeed);
                index = 2;
            } else if(reClass.equals(randomElementClassNames[3])) {
                randomElement = new Ranlux(coltSeed);
                index = 3;
            } else if(reClass.equals(randomElementClassNames[4])) {
                randomElement = new Ranmar(coltSeed);
                index = 4;
            } else {
                // need something ..
                randomElement = new DRand(coltSeed);
                index = 0;
            }
            System.err.println("New randomElement: " + index);
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The Colt random element object.
    protected AbstractDistribution rng = null;
    protected static RandomElement randomElement = new DRand();

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

    private int coltSeed = 1;
    private static int index = -1;
}
