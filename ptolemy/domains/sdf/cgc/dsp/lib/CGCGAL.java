/* GAL, CGC domain: CGCGAL.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCGAL.pl by ptlang
*/
/*
Copyright (c) 1992-1996 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
 */
package ptolemy.domains.sdf.cgc.dsp.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCGAL
/**
Gradient Adaptive Lattice filter.

 @Author T. M. Parks
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCGAL.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCGAL extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCGAL(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        residual = new TypedIOPort(this, "residual", false, true);
        residual.setTypeEquals(BaseType.DOUBLE);

        // Lattice filter order.
        order = new Parameter(this, "order");
        order.setExpression("1");

        // Adaptation time constant.
        timeConstant = new Parameter(this, "timeConstant");
        timeConstant.setExpression("1.0");

        // Adaptation parameter.
        alpha = new Parameter(this, "alpha");
        alpha.setExpression("0.0");

        // Reflection coefficients.
        k = new Parameter(this, "k");
        k.setExpression("0.0");

        // Forward prediction error.
        f = new Parameter(this, "f");
        f.setExpression("0.0 0.0");

        // Backward prediction error.
        b = new Parameter(this, "b");
        b.setExpression("0.0 0.0");

        // Error power estimate.
        e = new Parameter(this, "e");
        e.setExpression("0.0 0.0");

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type double.
     */
    public TypedIOPort input;

    /**
     * residual of type double.
     */
    public TypedIOPort residual;

    /**
     *  Lattice filter order. parameter with initial value "1".
     */
     public Parameter order;

    /**
     *  Adaptation time constant. parameter with initial value "1.0".
     */
     public Parameter timeConstant;

    /**
     *  Adaptation parameter. parameter with initial value "0.0".
     */
     public Parameter alpha;

    /**
     *  Reflection coefficients. parameter with initial value "0.0".
     */
     public Parameter k;

    /**
     *  Forward prediction error. parameter with initial value "0.0 0.0".
     */
     public Parameter f;

    /**
     *  Backward prediction error. parameter with initial value "0.0 0.0".
     */
     public Parameter b;

    /**
     *  Error power estimate. parameter with initial value "0.0 0.0".
     */
     public Parameter e;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  setup() {
        //# line 122 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGAL.pl"
        //k.resize(order+1);
        //	f.resize(order+1);
        //	b.resize(order+1);
        //	e.resize(order+1);
        //	alpha = 1.0 - ((timeConstant - 1.0) / (timeConstant + 1.0));
     }

    /**
     */
    public void  go() {
        //# line 131 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGAL.pl"
        //addCode(main);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String main = 
        "	{\n"
        + "	    int m;\n"
        + "\n"
        + "	    /* Update forward errors. */\n"
        + "	    $ref(f,0) = $ref(input);\n"
        + "	    for(m = 1; m <= $val(order); m++)\n"
        + "	    {\n"
        + "	       $ref(f,m) = $ref(f,m-1) - $ref(k,m) * $ref(b,m-1);\n"
        + "	    }\n"
        + "\n"
        + "	    /* Update backward errors, reflection coefficients. */\n"
        + "	    for(m = $val(order); m > 0; m--)\n"
        + "	    {\n"
        + "		$ref(b,m) = $ref(b,m-1) - $ref(k,m)*$ref(f,m-1);\n"
        + "		$ref(e,m) *= 1.0 - $val(alpha);\n"
        + "		$ref(e,m) += $val(alpha) * ($ref(f,m-1)*$ref(f,m-1) + $ref(b,m-1)*$ref(b,m-1));\n"
        + "		if ($ref(e,m) != 0.0)\n"
        + "		{\n"
        + "		    $ref(k,m) += $val(alpha) * ($ref(f,m)*$ref(b,m-1) + $ref(b,m)*$ref(f,m-1)) / $ref(e,m);\n"
        + "		    if ($ref(k,m) > 1.0) $ref(k,m) = 1.0;\n"
        + "		    if ($ref(k,m) < -1.0) $ref(k,m) = -1.0;\n"
        + "		}\n"
        + "	    }\n"
        + "\n"
        + "	    $ref(b,0) = $ref(input);\n"
        + "	    $ref(residual) =  $ref(f,order);\n"
        + "	}\n";
}
