/* Code generator helper for typed composite actor.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.codegen.c.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator helper for typed composite actor.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends CCodeGeneratorHelper {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }
    
    /** Create read and write offset variables if needed for the associated 
     *  composite actor. It delegates to the director helper of the local 
     *  director.
     *  @return A string containing declared read and write offset variables. 
     *  @exception IllegalActionException If the helper class cannot be found
     *   or the director helper throws it.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();    
        
        Director directorHelper = (Director) _getHelper((NamedObj) 
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        code.append(directorHelper.createOffsetVariablesIfNeeded()); 
        return code.toString();
    }

    /** Generate the fire code of the associated composite actor. It first 
     *  generates code for transferring any data from the input ports of this 
     *  composite to the ports connected on the inside by calling the 
     *  generateTransferInputsCode() method of the local director helper. It 
     *  then invokes the generateFireCode() method of its local director helper.
     *  After the generateFireCode() method of the director helper returns, 
     *  generate code for transferring any output data created by calling the 
     *  local director helper's generateTransferOutputsCode() method.
     * 
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor, or
     *   the director helper throws it while generating code for transferring
     *   data.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        Director directorHelper = (Director) _getHelper((NamedObj) 
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());

        // Transfer the data to the inside.
        Iterator inputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            directorHelper.generateTransferInputsCode(inputPort, code);
        }

        // Generate the fire code by the director helper.
        code.append(directorHelper.generateFireCode());
 
        // Transfer the data to the outside. 
        Iterator outputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            directorHelper.generateTransferOutputsCode(outputPort, code);
        }
        
        return code.toString();
    }

    /** Generate the initialize code of the associated composite actor. It 
     *  first resets the read and write offset of all input ports of all
     *  contained actors and all output ports. It then gets the result of 
     *  generateInitializeCode() method of the local director helper.
     *
     *  @return The initialize code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor
     *   or while resetting read and write offset.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer initializeCode = new StringBuffer();
        
        // Reset the offset for all of the contained actors' input ports.
        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(actor);
            initializeCode.append(actorHelper.resetInputPortsOffset());
        }

        // Reset the offset for all of the output ports.
        initializeCode.append(resetOutputPortsOffset());

        Director directorHelper = (Director) _getHelper((NamedObj) 
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        
        // Generate the initialize code by the director helper.
        initializeCode.append(directorHelper.generateInitializeCode());
        
        return initializeCode.toString();
    }

    /** Generate mode transition code. It delegates to the director helper 
     *  of the local director. The mode transition code generated in this 
     *  method is executed after each global iteration, e.g., in HDF model. 
     * 
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the director helper throws it 
     *   while generating mode transition code. 
     */
    public void generateModeTransitionCode(StringBuffer code) 
            throws IllegalActionException {
        Director directorHelper = (Director) _getHelper((NamedObj) 
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        directorHelper.generateModeTransitionCode(code);       
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
        result.append(super.generatePreinitializeCode());

        Director directorHelper = (Director) _getHelper((NamedObj) 
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        result.append(directorHelper.generatePreinitializeCode());

        return result.toString();
    }

    /** Generate a set of shared codes of the associated composite actor.
     *  It returns the result of calling generateSharedCode() method of 
     *  the helpers of all contained actors.
     *  
     *  @return a set of shared codes.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating shared code for the actor.
     */
    public Set generateSharedCode() throws IllegalActionException {
        
        Set sharedCode = new HashSet();
        sharedCode.addAll(super.generateSharedCode());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) 
                    _getHelper((NamedObj) actor);
            sharedCode.addAll(helperObject.generateSharedCode());
        }

        return sharedCode;
    }

    /** Generate variable declarations for input ports, output ports and 
     *  parameters if necessary.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating variable declarations for 
     *   the actor.
     */
    public void generateVariableDeclaration(StringBuffer code)
            throws IllegalActionException {
        super.generateVariableDeclaration(code);

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) 
                    _getHelper((NamedObj) actor);
            helperObject.generateVariableDeclaration(code);
        }
    }

    /** Generate the wrapup code of the associated composite actor. It returns
     *  the result of generateWrapupCode() method of the local director helper.
     * 
     *  @return The wrapup code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating wrapup code for the actor.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Director directorHelper = (Director) _getHelper((NamedObj) 
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        code.append(directorHelper.generateWrapupCode());
        return code.toString();
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
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) 
                    _getHelper((NamedObj) actor);
            files.addAll(helperObject.getHeaderFiles());
        }

        return files;
    }
    
    /** Return an int array of firings per global iteration. For each internal 
     *  configuration of this composite actor, the array contains a corresponding 
     *  element representing the number of firings of this composite actor per 
     *  global iteration.  
     * 
     *  @return An int array of firings per global iteration.
     */
    public int[] getFiringsPerGlobalIteration() {
        return _firingsPerGlobalIteration;    
    }

    /** Return a set of parameters that will be modified during the execution
     *  of the model. These parameters are those returned by getModifiedVariables()
     *  method of directors or actors that implement ExplicitChangeContext
     *  interface. 
     * 
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If the helper associated with an actor 
     *   or director throws it while getting modified variables. 
     */
    public Set getModifiedVariables() throws IllegalActionException {
        Set set = new HashSet();
        set.addAll(super.getModifiedVariables());

        Director directorHelper = (Director) _getHelper((NamedObj) 
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        set.addAll(directorHelper.getModifiedVariables());
        return set;
    }
    
    /** Return a two-dimensional int array of rates of this actor. For each internal 
     *  configuration of this composite actor, the array contains a corresponding 
     *  one-dimensional int array representing the rates of all ports of this 
     *  composite actor. It returns null when there is only one internal 
     *  configuration, e.g., when the internal model is an SDF model.
     * 
     *  @return A two-dimensional int array of rates of this actor or null.
     */
    public int[][] getRates() {
        return _rates;
    }

    /** Reset the offsets of all inside buffers of all output ports of the
     *  associated composite actor to the default value of 0.
     * 
     *  @return The reset code of the associated composite actor.
     *  @exception IllegalActionException If thrown while getting or
     *   setting the offset.
     */
    public String resetOutputPortsOffset() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();

            for (int i = 0; i < port.getWidthInside(); i++) {
                Object readOffset = getReadOffset(port, i);              
                if (readOffset instanceof Integer) {
                    // Read offset is a number.
                    setReadOffset(port, i, new Integer(0));
                } else {
                    // Read offset is a variable.
                    code.append(((String) readOffset) + " = 0;\n");         
                }
                Object writeOffset = getWriteOffset(port, i);
                if (writeOffset instanceof Integer) {
                    // Write offset is a number.
                    setWriteOffset(port, i, new Integer(0));
                } else {
                    // Write offset is a variable.
                    code.append(((String) writeOffset) + " = 0;\n");         
                }
            }
        }
        return code.toString();
    }

    /** Set the int array of firings per global iteration. For each internal 
     *  configuration of this composite actor, the array contains a corresponding 
     *  element representing the number of firings of this composite actor 
     *  per global iteration.  
     * 
     *  @param firingsPerGlobalIteration An int array of firings per 
     *   global iteration
     */
    public void setFiringsPerGlobalIteration(int[] firingsPerGlobalIteration) {
        _firingsPerGlobalIteration = firingsPerGlobalIteration;    
    }
    
    /** Set the two-dimensional int array of rates of this actor. For each internal 
     *  configuration of this composite actor, the array contains a corresponding 
     *  one-dimensional int array representing the rates of all ports of this 
     *  composite actor.  
     * 
     *  @param rates A two-dimensional int array of rates of this actor.
     */
    public void setRates(int[][] rates) {
        _rates = rates;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Create and initialize the buffer size and offset maps for this 
     *  composite actor. A key of the map is an IOPort of the actor. 
     *  The corresponding value is an array of buffer sizes or an 
     *  array of offsets. The i-th element in the array corresponds to 
     *  the i-th channel of that IOPort.

     * @exception IllegalActionException If thrown while getting helper
     *  or buffer size.
     */
    protected void _createBufferSizeAndOffsetMap() throws IllegalActionException {
        super._createBufferSizeAndOffsetMap();

        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();
            int length = port.getWidthInside();

            int[] bufferSizes = new int[length];
            _bufferSizes.put(port, bufferSizes);

            Director directorHelper = (Director) _getHelper((NamedObj) 
                    (((Actor) getComponent()).getDirector()));

            for (int i = 0; i < port.getWidthInside(); i++) {
                // If the local director is an SDF director, then the buffer
                // size got from the director helper is final. Otherwise
                // the buffer size will be updated later on with the maximum 
                // for all possible schedules.
                int bufferSize = directorHelper.getBufferSize(port, i);
                setBufferSize(port, i, bufferSize);
            }

            Object[] readOffsets = new Object[length];
            _readOffsets.put(port, readOffsets);

            Object[] writeOffsets = new Object[length];
            _writeOffsets.put(port, writeOffsets);
            
            for (int i = 0; i < length; i++) {
                setReadOffset(port, i, new Integer(0));
                setWriteOffset(port, i, new Integer(0));
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** An int array of firings per global iteration.
     */
    private int[] _firingsPerGlobalIteration;

    /** A two-dimensional int array of rates of this actor.
     */
    private int[][] _rates;
}
