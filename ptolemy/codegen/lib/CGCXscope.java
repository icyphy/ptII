/* Xscope, CGC domain: CGCXscope.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCXscope.pl by ptlang
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
//// CGCXscope
/**
Generate a multi-trace plot with the pxgraph program.
<p>
This star is an enhanced version of Xgraph.  It is identical
except that it can plot multiple traces, like an oscilloscope.
As for Xgraph,
the <i>title</i> parameter specifies a title for the plot.
The <i>saveFile</i> parameter optionally specifies a file for
storing the data in a syntax acceptable to pxgraph.
A null string prevents any such storage.
The <i>options</i> string is passed directly to the pxgraph program
as command-line options.  See the manual section describing pxgraph
for a complete explanation of the options.
<p>
Multiple traces may be plotted by setting the <i>traceLength</i>
state to a nonzero value.  In this case, a new plot (starting at
x value zero) is started every <i>traceLength</i> samples.  The
first <i>ignore</i> samples are not plotted; this is useful for letting
transients die away.
<a name="pxgraph program"></a>
<a name="oscilloscope, X window"></a>
<a name="graph, X window, multi-trace"></a>

 @Author S. Ha
 @Version $Id$, based on version 1.11 of /users/ptolemy/src/domains/cgc/stars/CGCXscope.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCXscope extends CGCXgraph {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCXscope(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Number of samples per trace.  If 0, only one trace. IntState
        traceLength = new Parameter(this, "traceLength");
        traceLength.setExpression("0");

        // Counter for samples in trace interval IntState
        traceCount = new Parameter(this, "traceCount");
        traceCount.setExpression("0");

        // Counter for trace intervals IntState
        nTracesSoFar = new Parameter(this, "nTracesSoFar");
        nTracesSoFar.setExpression("0");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     *  Number of samples per trace.  If 0, only one trace. parameter with initial value "0".
     */
     public Parameter traceLength;

    /**
     *  Counter for samples in trace interval parameter with initial value "0".
     */
     public Parameter traceCount;

    /**
     *  Counter for trace intervals parameter with initial value "0".
     */
     public Parameter nTracesSoFar;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return 8;
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

traceCount= 0-(int)ignore;
            super.generateInitializeCode();
     }

    /**
     */
    public void  generateFireCode() {

if (((IntToken)((traceLength).getToken())).intValue() > 0) {
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        if ($ref(traceCount) >= $val(traceLength)) {\n"
"                $ref(traceCount) = 0;\n"
"                fprintf($starSymbol(fp), \"move \");\n"
"                $ref(index) = $val(xInit);\n"
"                $ref(nTracesSoFar)++;\n"
"        }\n"
"        $ref(traceCount)++;\n"
"        if (!$ref(traceCount)) $ref(index) = 0;\n"

);          addCode(_str_);  }
                }

                super.go();
     }
}
