/* PcwzLinear, CGC domain: CGCPcwzLinear.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCPcwzLinear.pl by ptlang
*/
/*
Copyright (c) 1990-1996 The Regents of the University of California.
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
//// CGCPcwzLinear
/**
This star implements a piecewise linear mapping from the
input to the output.  The mapping is given by a sequence
of (x,y) pairs, which specify the breakpoints in the function.
The sequence of x values must be increasing. The function
implemented by the star can be represented by drawing straight
lines between the (x,y) pairs, in sequence.  The default
mapping is the "tent" map, in which inputs between -1.0
and 0.0 are linearly mapped into the range -1.0 to 1.0.
Inputs between 0.0 and 1.0 are mapped into the same range,
but with the opposite slope, 1.0 to -1.0. If the input is
outside the range specified in the "x" values of the breakpoints,
then the appropriate extreme value will be used for the output.
Thus, for the default map, if the input is -2.0, the output
will be -1.0.  If the input is +2.0, the output will again be -1.0.
<p>
<a name="table lookup"></a>
<a name="piecewise linear map"></a>
<a name="tent map"></a>
<a name="linear map"></a>

 @Author E. A. Lee
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCPcwzLinear.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCPcwzLinear extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCPcwzLinear(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // \"Endpoints and breakpoints in the mapping\" ComplexArrayState
        breakpoints = new Parameter(this, "breakpoints");
        breakpoints.setExpression("(-1.0, -1.0) (0.0, 1.0) (1.0, -1.0)");

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type double.
     */
    public ClassicPort input;

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  "Endpoints and breakpoints in the mapping" parameter with initial value "(-1.0, -1.0) (0.0, 1.0) (1.0, -1.0)".
     */
     public Parameter breakpoints;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  begin() {
        
// Check to make sure that x values are increasing
	  double previous = breakpoints[0].real();
	  for (int i = 1; i < breakpoints.size(); i++) {
	    if (breakpoints[i].real() <= previous) {
	      throw new IllegalActionException(this,"Breakpoints are not increasing in x");
	      return;
	    } else {
	      previous = breakpoints[i].real();
	    }
	  }
     }

    /**
     */
    public void  generateFireCode() {
        
addCode(lookup); 
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String lookup = 
        "	  double in, x1, x2, y1, y2;\n"
        + "	  int i, ind, sz;\n"
        + "	  in = $ref(input);\n"
        + "	  if (in < $ref(breakpoints,0).real) {\n"
        + "	    $ref(output) = $ref(breakpoints,0).imag;\n"
        + "	  } else {\n"
        + "	    ind = 0;\n"
        + "	    sz = $size(breakpoints);\n"
        + "	    for (i = 1; i < sz; i++) {\n"
        + "	      if (in < $ref(breakpoints,i).real) {\n"
        + "		x1 = $ref(breakpoints,i-1).real;\n"
        + "		y1 = $ref(breakpoints,i-1).imag;\n"
        + "		x2 = $ref(breakpoints,i).real;\n"
        + "		y2 = $ref(breakpoints,i).imag;\n"
        + "		$ref(output) = y1 + (y2 - y1)*(in - x1)/(x2 - x1);\n"
        + "		ind = 1;\n"
        + "		break;\n"
        + "	      }\n"
        + "	    }\n"
        + "	    if (!ind) {\n"
        + "	      sz--;\n"
        + "	      $ref(output) = $ref(breakpoints,sz).imag;\n"
        + "	    }\n"
        + "	  }\n";
}
