/* Class for modular code generators.

   Copyright (c) 2009 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program.procedural.java.modular;

import java.util.Date;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
////GenericCodeGenerator

/** Class for modular code generator.
*
*  @author Dai Bui, Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating red (rodiers)
*  @Pt.AcceptedRating red (daib)
*/

public class ModularCodeGenerator extends JavaCodeGenerator {

    /** Create a new instance of the Modular java code generator.
     *  @param container The container.
     *  @param name The name of the Java code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public ModularCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        generatorPackageList.setExpression("generic.program.procedural.java.modular");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    /** Generate code.  This is the main entry point.
     *  @param code The code buffer into which to generate the code.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    public int generateCode(StringBuffer code) throws KernelException {

        int returnValue = -1;

        // If the container is in the top level, we are generating code
        // for the whole model. We have to make sure there is a manager,
        // and then preinitialize and resolve types.
        if (_isTopLevel()) {

            // If necessary, create a manager.
            Actor container = ((Actor) getContainer());
            Manager manager = container.getManager();

            if (manager == null) {
                CompositeActor toplevel = (CompositeActor) ((NamedObj) container)
                        .toplevel();
                manager = new Manager(toplevel.workspace(), "Manager");
                toplevel.setManager(manager);
            }

            //set director for transparent composite actors?
            try {
                manager.preinitializeAndResolveTypes();
                returnValue = _generateCode(code);
            } finally {
                // We call wrapup here so that the state gets set to idle.
                // This makes it difficult to test the Exit actor.
                try {
                    long startTime = (new Date()).getTime();
                    manager.wrapup();
                    _printTimeAndMemory(startTime, "CodeGenerator: "
                            + "wrapup consumed: ");
                } catch (RuntimeException ex) {
                    // The Exit actor causes Manager.wrapup() to throw this.
                    if (!manager.isExitingAfterWrapup()) {
                        throw ex;
                    }
                }
            }
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            returnValue = _generateCode(code);
        }
        return returnValue;
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a program.
     *   In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {

        StringBuffer mainEntryCode = new StringBuffer();

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            mainEntryCode
                    .append(_eol
                            + _eol
                            + "public static void main(String [] args) throws Exception {"
                            + _eol + _sanitizedModelName + " model = new "
                            + _sanitizedModelName + "();" + _eol
                            + "model.run();" + _eol + "}" + _eol
                            + "public void run() throws Exception {" + _eol);
        } else {
            mainEntryCode.append(_eol + _eol + "public Object[] fire (" + _eol);

            Iterator<?> inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            boolean addComma = false;
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                if (addComma) {
                    mainEntryCode.append(", ");
                }
                
                String type = codeGenType(inputPort.getType());
                if (!type.equals("Token") && !isPrimitive(type)) {
                    type = "Token";
                }
                for (int i = 0; i < inputPort.getWidth(); i++) {
                    if (DFUtilities.getTokenConsumptionRate(inputPort) > 1) {
                        mainEntryCode.append(type + "[] " + inputPort.getName() + "_" + i);
                    } else {
                        mainEntryCode.append(type + " " + inputPort.getName() + "_" + i);
                    }
                }
                
                addComma = true;
            }
/*
            Iterator<?> outputPorts = ((Actor) getContainer()).outputPortList()
            .iterator();
            while (outputPorts.hasNext()) {
                TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
                if (addComma) {
                    mainEntryCode.append(", ");
                }
                
                String type = codeGenType(outputPort.getType());
                
                if (!type.equals("Token") && !isPrimitive(type)) {
                    type = "Token";
                }
                
                for (int i = 0; i < outputPort.getWidth(); i++) {
                    if (DFUtilities.getTokenConsumptionRate(outputPort) > 1) {
                        mainEntryCode.append(type + "[] " + outputPort.getName() + "_" + i);
                    } else {
                        mainEntryCode.append(type + " " + outputPort.getName() + "_" + i);
                    }
                }
                addComma = true;
            }
            */

            mainEntryCode.append(") {" + _eol);

        }

        return _processCode(mainEntryCode.toString());
    }


    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {

        if (_isTopLevel()) {
            return INDENT1 + "System.exit(0);" + _eol + "}" + _eol + "}" + _eol;
        } else {
            return INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                    + _eol + "}" + _eol;

        }
    }
}
