/*
 * Created on Apr 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.c.actor.lib.gui;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Jackie
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XYPlotter extends CCodeGeneratorHelper {
    public XYPlotter(ptolemy.actor.lib.gui.XYPlotter actor) {
        super(actor);
    }
    
    /**
     * @param stream
     */
    public void  generateFireCode(StringBuffer stream)
        throws IllegalActionException {

        ptolemy.actor.lib.gui.XYPlotter actor = 
            (ptolemy.actor.lib.gui.XYPlotter) getComponent();
        // FIXME: how do we add legend to the file??
        
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("writeFile");
        stream.append(processCode(tmpStream.toString()));
    }

    public String generateInitializeCode()
        throws IllegalActionException {
        super.generateInitializeCode();
        
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("initBlock");
        return processCode(tmpStream.toString());
    }
    
    /**
     * @param stream
     */
    public void generateWrapupCode(StringBuffer stream)
        throws IllegalActionException {

        ptolemy.actor.lib.gui.Plotter actor = 
            (ptolemy.actor.lib.gui.Plotter) getComponent();

        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("closeFile");

        if (actor.fillOnWrapup.getExpression().equals("true")) {
        	tmpStream.appendCodeBlock("graphPlot");
        }

        stream.append(processCode(tmpStream.toString()));
    }
    
    /** Get the files needed by the code generated for the
     *  XYPlotter actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the XYPlotter actor.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }
}
