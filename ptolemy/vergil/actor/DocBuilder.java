/* Build documentation for Java and Actors

 Copyright (c) 2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.vergil.actor;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// DocBuilder

/** Build Documentation for Java and Actors.
 *
 *  <p>This class sets the commands that build the Java classes.
 *  
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public class DocBuilder extends Attribute {

    // In principle, this class should be usable from both within a UI
    // and without a UI.

    /** Create a new instance of the DocBuilder.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public DocBuilder(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        cleanFirst = new Parameter(this, "cleanFirst");
        cleanFirst.setTypeEquals(BaseType.BOOLEAN);
        cleanFirst.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then clean before building documentation.  The default
     *  value is false.
     */ 
    public Parameter cleanFirst; 

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    
    /** Build the Java class and Actor documentation.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.     
     *  @exception IllegalActionException If there is a problem building
     *  the documentation.
     */
    public int buildDocs() throws IllegalActionException {
        if (_executeCommands == null) {
            _executeCommands = new StreamExec();
        }
        return _executeCommands();
    }

    /** Get the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @return executeCommands The subprocess command executor.
     *  @see #setExecuteCommands(ExecuteCommands)
     */
    public ExecuteCommands getExecuteCommands() {
        return _executeCommands;
    }

    /** Set the application name.
     * We handle the applicationName specially so that we create
     * only the docs for the app we are running.
     */
    public void setApplicationName(String applicationName) {
        _applicationName = applicationName;
    }

    /** Set the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @param executeCommands The subprocess command executor.
     *  @see #getExecuteCommands()
     */
    public void setExecuteCommands(ExecuteCommands executeCommands) {
        _executeCommands = executeCommands;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Build the documentation. 
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     */
    private int _executeCommands() throws IllegalActionException {

        List commands = new LinkedList();

        if (_applicationName == null) {
            if (((BooleanToken) cleanFirst.getToken()).booleanValue()) {
                commands.add("rm -rf codeDoc");
            }
            commands.add("make codeDoc/tree.html");
            commands.add("make codeDoc/ptolemy/actor/lib/Ramp.xml");
            commands.add("make codeDoc/ptolemy/actor/lib/RampIdx.xml");
        } else {
            if (((BooleanToken) cleanFirst.getToken()).booleanValue()) {
                commands.add("rm -rf codeDoc" + _applicationName);
            }
            commands.add("make codeDoc" + _applicationName
                    + "/doc/codeDoc/tree.html");
            commands.add("make APPLICATION=" + _applicationName
                    + " \"PTDOCFLAGS=-d doc/codeDoc" + _applicationName
                    + "/doc/codeDoc" 
                    + " codeDoc" + _applicationName
                    + "/ptolemy/actor/lib/Ramp.xml");
            commands.add("make APPLICATION=" + _applicationName
                    + " codeDoc" + _applicationName
                    + "/ptolemy/actor/lib/RampIdx.xml");
        }
        if (commands.size() == 0) {
            return -1;
        }

        _executeCommands.setCommands(commands);
        File ptII = new File(StringUtilities.getProperty("ptolemy.ptII.dir")
                + "/doc");
        _executeCommands.setWorkingDirectory(ptII);

        try {
            // FIXME: need to put this output in to the UI, if any. 
            _executeCommands.start();
        } catch (Exception ex) {
            StringBuffer errorMessage = new StringBuffer();
            Iterator allCommands = commands.iterator();
            while (allCommands.hasNext()) {
                errorMessage.append((String) allCommands.next() + "\n");
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:\n" + errorMessage);
        }
        return _executeCommands.getLastSubprocessReturnCode();
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the application, usually from the _applicationName
     *  StringAttribute in configuration.xml.
     *  If null, then use the default documentation in doc/codeDoc. 
     */
    private String _applicationName;

    /** The object that actually executes the commands.
     */   
    private ExecuteCommands _executeCommands;
}
