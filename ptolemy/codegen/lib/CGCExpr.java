/* Expr, CGC domain: CGCExpr.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCExpr.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
 */
package ptolemy.codegen.lib;

import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCExpr
/**
General expression evaluation.

 @Author T. M. Parks
 @Version $Id$, based on version 1.12 of /users/ptolemy/src/domains/cgc/stars/CGCExpr.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCExpr extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCExpr(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        in = new ClassicPort(this, "in", true, false);
        in.setMultiport(true);
        in.setTypeEquals(BaseType.DOUBLE);
        out = new ClassicPort(this, "out", false, true);
        out.setTypeEquals(BaseType.DOUBLE);

        // Expression to evaulate. StringState
        expr = new Parameter(this, "expr");
        expr.setExpression("$ref(in#1)");

        // \"DataType of `in` porthole, one of [float,int,complex,anytype]\" StringState
        inDataType = new Parameter(this, "inDataType");
        inDataType.setExpression("float");

        // \"DataType of `out` porthole, one of [float,int,complex,=in]\" StringState
        outDataType = new Parameter(this, "outDataType");
        outDataType.setExpression("float");

        // List of necessary include files. StringArrayState
        include = new Parameter(this, "include");
        include.setExpression("<math.h>");

        // execution time IntState
        runTime = new Parameter(this, "runTime");
        runTime.setExpression("2");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * in of type double.
     */
    public ClassicPort in;

    /**
     * out of type double.
     */
    public ClassicPort out;

    /**
     *  Expression to evaulate. parameter with initial value "$ref(in#1)".
     */
     public Parameter expr;

    /**
     *  "DataType of `in` porthole, one of [float,int,complex,anytype]" parameter with initial value "float".
     */
     public Parameter inDataType;

    /**
     *  "DataType of `out` porthole, one of [float,int,complex,=in]" parameter with initial value "float".
     */
     public Parameter outDataType;

    /**
     *  List of necessary include files. parameter with initial value "<math.h>".
     */
     public Parameter include;

    /**
     *  execution time parameter with initial value "2".
     */
     public Parameter runTime;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return ((IntToken)((runTime).getToken())).intValue();
     }

    /**
     */
    public void  generatePreinitializeCode() {

for (int i = 0; i < include.size(); i++)
	    addInclude(include[i]);
     }

    /**
     */
    public void  generateFireCode() {

StringBuffer code = new StringBuffer();
	code.append("$ref(out) = " + expr  + ";\n");
	addCode(code);
     }

    /**
     */
    public void preinitialize () {

// We must change the porthole types at preinitialize time
	// so that porthole type resolution works correctly.
	// That means we have to initialize the states ourselves.
	initState();
	String letter = inDataType;
	switch (letter[0]) {
	case 'F':
	case 'f':
	    in.setPort(in.name(),this,FLOAT);
	    break;
	case 'I':
	case 'i':
	    in.setPort(in.name(),this,INT);
	    break;
	case 'C':
	case 'c':
	    in.setPort(in.name(),this,COMPLEX);
	    break;
	case 'A':
	case 'a':
	    in.setPort(in.name(),this,ANYTYPE);
	    break;
	default:
	    throw new IllegalActionException(this,"CGC Expr does not support the type",
			    inDataType);
	    break;
	}
	letter = outDataType;
	switch (letter[0]) {
	case 'F':
	case 'f':
	    out.setPort(out.name(),this,FLOAT);
	    break;
	case 'I':
	case 'i':
	    out.setPort(out.name(),this,INT);
	    break;
	case 'C':
	case 'c':
	    out.setPort(out.name(),this,COMPLEX);
	    break;
	case '=':
	    out.setPort(out.name(),this,ANYTYPE);
	    out.inheritTypeFrom(in);
	    break;
	default:
	    throw new IllegalActionException(this,"CGC Expr does not support the type",
			    outDataType);
	    break;
	}
    }

}
