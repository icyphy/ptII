/* Code generator helper for typed composite actor.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.vhdl.actor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.codegen.vhdl.kernel.VHDLCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.math.Precision;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator helper for typed composite actor.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends VHDLCodeGeneratorHelper {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /**
     * For each actor in this typed composite actor, determine which ports
     * need type conversion.
     * @exception IllegalActionException Thrown if any of the helpers of the
     * inside actors is unavailable.
     * @see ptolemy.codegen.kernel.CodeGeneratorHelper#analyzeTypeConvert
     */
    public void analyzeTypeConvert() throws IllegalActionException {
        super.analyzeTypeConvert();
    }

    /** Create read and write offset variables if needed for the associated 
     *  composite actor. It delegates to the director helper of the local 
     *  director.
     *  @return A string containing declared read and write offset variables. 
     *  @exception IllegalActionException If the helper class cannot be found
     *   or the director helper throws it.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        return directorHelper.createOffsetVariablesIfNeeded();
    }

    /** Generate the fire code of the associated composite actor. This method
     *  first generates code for transferring any data from the input
     *  ports of this composite to the ports connected on the inside
     *  by calling the generateTransferInputsCode() method of the
     *  local director helper. It then invokes the generateFireCode()
     *  method of its local director helper.  After the
     *  generateFireCode() method of the director helper returns,
     *  generate code for transferring any output data created by
     *  calling the local director helper's
     *  generateTransferOutputsCode() method.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated
     *  with an actor throws it while generating fire code for the
     *  actor, or the director helper throws it while generating code
     *  for transferring data.
     */
    public String generateFireCode() throws IllegalActionException {
        _codeStream.clear();
        _codeStream.append(super.generateFireCode());
        ArrayList args = new ArrayList();
        args.add("");
        args.add("");
        _codeStream.appendCodeBlock("fireBlock", args);
        return processCode(_codeStream.toString());
    }

    /** Generate the preinitialize code of the associated composite actor.
     *  It first creates buffer size and offset map for its input ports and 
     *  output ports. It then gets the result of generatePreinitializeCode() 
     *  method of the local director helper.
     *
     *  @return The preinitialize code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor
     *   or while creating buffer size and offset map.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        StringBuffer signalCode = new StringBuffer();
        StringBuffer componentCode = new StringBuffer();
        StringBuffer instantiateCode = new StringBuffer();
        HashSet componentSet = new HashSet();
        
        result.append(super.generatePreinitializeCode());

        CompositeActor composite = 
            (ptolemy.actor.CompositeActor) getComponent();

        Iterator actors = composite.entityList().iterator();
            
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            Iterator outputPorts = actor.outputPortList().iterator();
            
            while (outputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) outputPorts.next();
                Precision precision = new Precision(_getPortPrecision(port));
                int msb = precision.getMostSignificantBitPosition();
                int lsb = precision.getLeastSignificantBitPosition();
                signalCode.append("    signal ");
                
                signalCode.append(((CodeGeneratorHelper) _getHelper(
                        port.getContainer())).getReference(
                                port.getName() + "#" + 0));
                
                signalCode.append(" : sfixed (" + msb + 
                        " DOWNTO " + lsb + ");\n");
            }
            
            CodeGeneratorHelper helper = 
                (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            instantiateCode.append(helper.generateFireCode());

            componentSet.addAll(helper.getSharedCode());
            
            if (actor instanceof CompositeActor) {
                result.append(helper.generatePreinitializeCode());
            }
        }    
        
        Iterator iterator = componentSet.iterator();
        
        while(iterator.hasNext()) {
            componentCode.append(iterator.next().toString());
        }
        
        _codeStream.clear();
        ArrayList args = new ArrayList();
        args.add("");
        args.add(signalCode);
        args.add(componentCode);
        args.add(instantiateCode);
        _codeStream.appendCodeBlock("preinitBlock", args);
        result.append(_codeStream.toString());
        
        return processCode(result.toString());
    }

    
    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Generate variable declarations for input ports.
        
        // Generate variable declarations for output ports.

        return processCode(code.toString());
    }

    /** Get the header files needed by the code generated from this helper 
     *  class. It returns the result of calling getHeaderFiles() method of 
     *  the helpers of all contained actors.
     * 
     *  @return A set of strings that are header files. 
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating header files for the actor.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.addAll(super.getHeaderFiles());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            files.addAll(helperObject.getHeaderFiles());
        }

        return files;
    }
}
