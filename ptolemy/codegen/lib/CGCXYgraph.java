/* XYgraph, CGC domain: CGCXYgraph.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCXYgraph.pl by ptlang
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
//// CGCXYgraph
/**
Generates an X-Y plot with the pxgraph program.
The X data is on "xInput" and the Y data is on "input".
<p>
The input signal is plotted using the <i>pxgraph</i> program, with one
input interpreted as the x-axis data, and the other input as y-axis data.
<a name="graph, X-Y"></a>
<a name="pxgraph program"></a>
@see ptolemy.domains.cgc.stars.Xgraph
@see ptolemy.domains.cgc.stars.XMgraph
<a href="$PTOLEMY/src/pxgraph/pxgraph.htm">$PTOLEMY/src/pxgraph/pxgraph.htm</a>@see ptolemy.domains.cgc.stars.Xhistogram

 @Author S. Ha
 @Version $Id$, based on version 1.11 of /users/ptolemy/src/domains/cgc/stars/CGCXYgraph.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCXYgraph extends CGCXgraph {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCXYgraph(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        xInput = new ClassicPort(this, "xInput", true, false);

/*
// make xInit and xUnits invisible
                xUnits.setAttributes(A_NONSETTABLE);
                xInit.setAttributes(A_NONSETTABLE);
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * xInput of type anytype.
     */
    public ClassicPort xInput;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return 6;
     }

    /**
     */
    public void  generateFireCode() {

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        if (++$ref(count) >= $val(ignore)) \n"
"                fprintf($starSymbol(fp),\"%g %g\\n\",$ref(xInput),$ref(input));"

);          addCode(_str_);  }

     }
}
