/* Test, CGC domain: CGCTest.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCTest.pl by ptlang
*/
/*
Copyright (c) 1990-2005 The Regents of the University of California.
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
//// CGCTest
/**
This star compares its two inputs "upper" and "lower".
The test "condition" can be any one of {EQ NE GT GE},
or equivalently any one of {== != > >=},
whose elements represent the binary operations of equals, not equals,
greater than, and greater than or equals, respectively.

If the "crossingsOnly" parameter is true, then the output is true only
when the outcome of the test changes from true to false
or false to true.
In this case, the first output is always true.
<p>
To implement the tests "&lt;" or "&lt;=", simply reverse the inputs.

 @Author Rolando Diesta, Brian L. Evans, and Edward A. Lee
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCTest.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCTest extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCTest(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        upper = new ClassicPort(this, "upper", true, false);
        upper.setTypeEquals(BaseType.DOUBLE);
        lower = new ClassicPort(this, "lower", true, false);
        lower.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // The test condition, i.e, one of EQ, NE, LT, or LE StringState
        condition = new Parameter(this, "condition");
        condition.setExpression("EQ");

        // If true, outputs are true only when the sense of the test changes IntState
        crossingsOnly = new Parameter(this, "crossingsOnly");
        crossingsOnly.setExpression("false");

        // Initial value indicates that prevResult has not yet gotten the result of a\ntest.  This ensures that the first test result will always be true. IntState
        prevResult = new Parameter(this, "prevResult");
        prevResult.setExpression("-1");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Left-hand side of the test
     */
    public ClassicPort upper;

    /**
Right-hand side of the test
     */
    public ClassicPort lower;

    /**
Result of the test
     */
    public ClassicPort output;

    /**
     *  The test condition, i.e, one of EQ, NE, LT, or LE parameter with initial value "EQ".
     */
     public Parameter condition;

    /**
     *  If true, outputs are true only when the sense of the test changes parameter with initial value "false".
     */
     public Parameter crossingsOnly;

    /**
     *  Initial value indicates that prevResult has not yet gotten the result of a
test.  This ensures that the first test result will always be true. parameter with initial value "-1".
     */
     public Parameter prevResult;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

String cn = condition;
                if ( cn.equalsIgnoreCase("EQ")) test = EQID;
                else if ( cn.equalsIgnoreCase("NE") ) test = NEID;
                else if ( cn.equalsIgnoreCase("GT") ) test = GTID;
                else if ( cn.equalsIgnoreCase("GE") ) test = GEID;
                else if ( strcasecmp(cn, "==") == 0 ) test = EQID;
                else if ( strcasecmp(cn, "!=") == 0 ) test = NEID;
                else if ( strcasecmp(cn, ">") == 0 ) test = GTID;
                else if ( strcasecmp(cn, ">=") == 0 ) test = GEID;
                else throw new IllegalActionException(this, "Unrecognized test.", cn);
     }

    /**
     */
    public void  generateFireCode() {

StringBuffer compare = new StringBuffer("($ref(upper) ");
                compare.append(test  + " $ref(lower)) ? 1 : 0;\n");

                if ( ((IntToken)((crossingsOnly).getToken())).intValue() ) {
                        addCode(decl);
StringBuffer setResult = new StringBuffer("result = ");
                        setResult.append(compare);
                        addCode(setResult);
                        addCode(crossings);
                }
                else {
StringBuffer simpleCode = new StringBuffer("$ref(output) = ");
                        simpleCode.append(compare);
                        addCode(simpleCode);
                }
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String decl =
        "                int result;\n";

    public String crossings =
        "                $ref(output) = ( $ref(prevResult) != result );\n"
        + "                $ref(prevResult) = result;\n";
}
