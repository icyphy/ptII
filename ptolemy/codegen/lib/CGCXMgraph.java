/* XMgraph, CGC domain: CGCXMgraph.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCXMgraph.pl by ptlang
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
//// CGCXMgraph
/**
Generate a multi-signal plot with the pxgraph program.
<p>
The input signal is plotted using the <i>pxgraph</i> program.
This program must be in your path, or this star will not work!
The <i>title</i> parameter specifies a title for the plot.
The <i>saveFile</i> parameter optionally specifies a file for
storing the data in a syntax acceptable to pxgraph.
A null string prevents any such storage.
The <i>options</i> string is passed directly to the pxgraph program
as command-line options.  See the manual section describing pxgraph
for a complete explanation of the options.

 @Author Soonhoi Ha
 @Version $Id$, based on version 1.27 of /users/ptolemy/src/domains/cgc/stars/CGCXMgraph.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCXMgraph extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCXMgraph(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        // Title for the plot. StringState
        title = new Parameter(this, "title");
        title.setExpression("X graph");

        // File name for saving plottable data. StringState
        saveFile = new Parameter(this, "saveFile");
        saveFile.setExpression("");

        // Command line options for the pxgraph program. StringState
        options = new Parameter(this, "options");
        options.setExpression("-bb -tk =800x400");

        // Number of initial values to ignore. IntState
        ignore = new Parameter(this, "ignore");
        ignore.setExpression("0");

        // For labeling, horizontal increment between samples. FloatState
        xUnits = new Parameter(this, "xUnits");
        xUnits.setExpression("1.0");

        // For labeling, horizontal value of the first sample. FloatState
        xInit = new Parameter(this, "xInit");
        xInit.setExpression("0.0");

        // numIn IntState
        numIn = new Parameter(this, "numIn");
        numIn.setExpression("1");

        // index FloatState
        index = new Parameter(this, "index");
        index.setExpression("0.0");

        // resources StringArrayState
        resources = new Parameter(this, "resources");
        resources.setExpression("STDIO");

        // Samplecounter IntState
        count = new Parameter(this, "count");
        count.setExpression("0");

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
     *  Title for the plot. parameter with initial value "X graph".
     */
     public Parameter title;

    /**
     *  File name for saving plottable data. parameter with initial value "".
     */
     public Parameter saveFile;

    /**
     *  Command line options for the pxgraph program. parameter with initial value "-bb -tk =800x400".
     */
     public Parameter options;

    /**
     *  Number of initial values to ignore. parameter with initial value "0".
     */
     public Parameter ignore;

    /**
     *  For labeling, horizontal increment between samples. parameter with initial value "1.0".
     */
     public Parameter xUnits;

    /**
     *  For labeling, horizontal value of the first sample. parameter with initial value "0.0".
     */
     public Parameter xInit;

    /**
     *  numIn parameter with initial value "1".
     */
     public Parameter numIn;

    /**
     *  index parameter with initial value "0.0".
     */
     public Parameter index;

    /**
     *  resources parameter with initial value "STDIO".
     */
     public Parameter resources;

    /**
     *  Samplecounter parameter with initial value "0".
     */
     public Parameter count;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
return 6 * ((IntToken)((numIn).getToken())).intValue() + 1;
     }

    /**
     */
    public void  wrapup() {
        
int i;
		addCode("{\n"); 
		addCode("int i; \n");

		// close the files
		addCode(closeFile); 

StringBuffer cmd = new StringBuffer("( ");

		// save File
		String sf = saveFile;
		if (sf != null && *sf != 0) {
			for (i = 0; i<((IntToken)((numIn).getToken())).intValue(); i++) {
				cmd.append("/bin/cat ");
				cmd << target()->name(); 
				cmd.append("_$starSymbol(temp)" + i  + " >> ");
				cmd.append(sf  + "; /bin/echo \"\" >> " + sf);
				cmd.append("; ");
			}
		}

		cmd.append("pxgraph ");

		// put title on command line

		String ttl = title;

		if (ttl && *ttl) {
			if (strchr(ttl,'\'')) {
				cmd.append("-t \"" + ttl  + "\" ");
			}
			else {
				cmd.append("-t '" + ttl  + "' ");
			}
		}

		String opt = options;

		// put options on the command line
		if (opt && *opt) {
			cmd.append(opt  + " ");
		}

		// put file names
		for (i = 0; i < ((IntToken)((numIn).getToken())).intValue(); i++) {
			cmd.append(target()->name()  + "_$starSymbol(temp)");
			cmd.append(i  + " ");
		}

		// remove temporary files
		for (i = 0; i < ((IntToken)((numIn).getToken())).intValue(); i++) {
			cmd.append("; /bin/rm -f " + target()->name());
			cmd.append("_$starSymbol(temp)" + i);
		}

		cmd.append(") &");
StringBuffer out = new StringBuffer("system(\"");
		out.append(sanitizeString(cmd)  + "\");\n}\n");
		addCode(out); 
     }

    /**
     */
    public void  generatePreinitializeCode() {
        
StringBuffer s = new StringBuffer("FILE* $starSymbol(fp)[");
		s.append(input.numberPorts()  + "];");
                addDeclaration(s);
                addInclude("<stdio.h>");
		for (int i = 0; i < input.numberPorts(); i++) {
StringBuffer w = new StringBuffer("if(!($starSymbol(fp)[");
			w.append(i <<  "] = fopen(\"");
			w.append(target()->name()  + "_$starSymbol(temp)");
			w.append(i  + "\",\"w\")))");
			addCode(w); 
			addCode(err); 
		}
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        
index = xInit;
		numIn = input.numberPorts();
     }

    /**
     */
    public void  generateFireCode() {
        
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"    if (++$ref(count) >= $val(ignore)) {\n"

); 	 addCode(_str_);  } 
	  for (int i = 1; i <= ((IntToken)((numIn).getToken())).intValue(); i++) {
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        fprintf($starSymbol(fp)[" + i-1 + "],\"%g %g\\n\",\n"
"		 $ref(index),$ref(input#" + i + "));\n"

); 	 addCode(_str_);  } 
	  }
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"    }\n"
"    $ref(index) += $val(xUnits);"

); 	 addCode(_str_);  } 

     }

    /**
     */
    protected String sanitizeString (StringList s) {
        
// quick implementation of a string buffer
			static class Buffer {
			   public:
				Buffer()  { buf = null; vsize = psize = 0; }
			// omitting the destructor since GCC 2.5.8 reports an internal
			// compiler error
			//	~Buffer() { if (buf)  free(buf); }

				void initialize() {
				    if (buf)  free(buf), buf = null;
				    vsize = psize = 0;
				}

				void append(char c) {
				    if (vsize >= psize)
					    buf = (char*) (buf ? realloc(buf, psize += 1024)
							       : malloc(psize += 1024));
				    buf[vsize++] = c;
				}

				operator String ()
				{
				    if (vsize == 0 || buf[vsize-1])
					append('\0');
				    return buf;
				}
			   private:
				// the string buffer
				char* buf;
				// virtual/physical buffer size
				int vsize, psize;
			} buffer;

			buffer.initialize();
				
			for (String sp=s; *sp; sp++) {
			    if (*sp == '\"')
				    buffer.append('\\');
			    buffer.append(*sp);
			}
			return (String) buffer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String err = 
        "{\n"
        + "    fprintf(stderr,\"ERROR: cannot open output file for Xgraph star.\\n\");\n"
        + "    exit(1);\n"
        + "}\n";

    public String closeFile = 
        "for (i = 0; i < $val(numIn); i++) fclose($starSymbol(fp)[i]);\n";
}
