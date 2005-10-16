/* Code generator helper for composite actor.

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
//// CompositeActor

/**
 Code generator helper for composite actor.
 
 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends CCodeGeneratorHelper {

    /** Construct the code generator helper associated
     *  with the given compositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }
    
    /** Generate the fire code of the associated composite actor.
     *  @param code
     *  @exception IllegalActionException 
     */  
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        super.generateFireCode(code);
        Director directorHelper = (Director) _getHelper
                ((NamedObj) ((ptolemy.actor.CompositeActor) 
                getComponent()).getDirector());
        
        Iterator inputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            directorHelper.generateTransferInputsCode(inputPort, code);
        }    
        
        directorHelper.generateFireCode(code);
        
        Iterator outputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            directorHelper.generateTransferOutputsCode(outputPort, code);
        }    
        
    }

    /** Generate the initialize code of the associated composite actor.
     *  Get the result of generateInitializeCode() method of the local 
     *  director helper. 
     *
     *  @return The initialize code of the associated composite actor.
     *  @exception IllegalActionException 
     */   
    public String generateInitializeCode() 
            throws IllegalActionException {
        
        Iterator actors = ((ptolemy.actor.CompositeActor) 
                getComponent()).deepEntityList().iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            CodeGeneratorHelper actorHelper 
                    = (CodeGeneratorHelper) _getHelper(actor);
            actorHelper.resetInputPortsOffset();
        }
        resetOutputPortsOffset();
        
        Director directorHelper = (Director) _getHelper
                ((NamedObj) ((ptolemy.actor.CompositeActor) 
                getComponent()).getDirector());
        return directorHelper.generateInitializeCode();
    }

    
    /** Create buffer and offset map. Then get the result of
     *  generatePreinitializeCode() method of the local director helper. 
     *
     *  @return The preinitialize code of the associated composite actor.
     *  @exception IllegalActionException 
     */
    public String generatePreinitializeCode() 
            throws IllegalActionException {
        
        StringBuffer result = new StringBuffer();
        result.append(super.generatePreinitializeCode());

        Director directorHelper = (Director) _getHelper
                ((NamedObj) ((ptolemy.actor.CompositeActor) 
                getComponent()).getDirector());
        result.append(directorHelper.generatePreinitializeCode());
        
        return result.toString();
    }


    /** Generate a set of the shared codes. 
     *  @return a set of shared codes.
     *  @exception IllegalActionException 
     */
    public Set generateSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        
        Iterator actors = ((ptolemy.actor.CompositeActor) 
                getComponent()).deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject 
                    =(CodeGeneratorHelper) _getHelper((NamedObj) actor);
            sharedCode.addAll(helperObject.generateSharedCode());
        }
        return sharedCode;
    }


    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @param code The given string buffer.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public void generateVariableDeclaration(StringBuffer code) 
            throws IllegalActionException {
        super.generateVariableDeclaration(code);
        
        Iterator actors = ((ptolemy.actor.CompositeActor) 
                getComponent()).deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject 
                    =(CodeGeneratorHelper) _getHelper((NamedObj) actor);
            helperObject.generateVariableDeclaration(code);
        }        
    }  
    
    /** Generate the wrapup code of the associated composite actor.
     *  @exception IllegalActionException 
     */   
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Director directorHelper = (Director) _getHelper
                ((NamedObj) ((ptolemy.actor.CompositeActor) 
                getComponent()).getDirector());
        code.append(directorHelper.generateWrapupCode());   
        return code.toString();
    }
    
    /** Get the files needed by the code generated from this helper class.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        
        Iterator actors = ((ptolemy.actor.CompositeActor) 
                getComponent()).deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject 
                    =(CodeGeneratorHelper) _getHelper((NamedObj) actor);
            files.addAll(helperObject.getHeaderFiles());
        }
        return files;
    }
    
    
    /** Reset the offsets of all inside channels of all output ports of the 
     *  associated actor to the default value of 0.
     */
    public void resetOutputPortsOffset() 
            throws IllegalActionException {
  
        Iterator outputPorts = ((Actor) getComponent()).outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();

            for (int i = 0; i < port.getWidthInside(); i++) {
                setReadOffset(port, i, new Integer(0));
                setWriteOffset(port, i, new Integer(0));
            }
        }
    }
    
  
    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////
       
    /** Create the buffer and offset map for each output port, which is 
     *  associated with this helper object. A key of the map is an IOPort 
     *  of the actor. The corresponding value is an array of Channel objects. 
     *  The i-th channel object corresponds to the i-th channel of that IOPort. 
     *  This method is used to maintain a internal HashMap of channels of the 
     *  actor. The channel objects in the map are used to keep track of the 
     *  offsets in their buffer.
     */
    protected void _createBufferAndOffsetMap() 
            throws IllegalActionException {
        
        super._createBufferAndOffsetMap();
        
        Iterator outputPorts = ((Actor) getComponent()).outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();
            int length = port.getWidthInside();

            int[] bufferSizes = new int[length];
            _bufferSizes.put(port, bufferSizes);
            Director directorHelper = (Director) _getHelper((NamedObj)
                    (((Actor) getComponent()).getDirector()));
            for (int i = 0; i < port.getWidthInside(); i++) {
                int bufferSize = directorHelper.getBufferSize(port, i);
                setBufferSize(port, i, bufferSize);
            }

            Object[] readOffsets = new Object[length];
            _readOffsets.put(port, readOffsets);
            
            Object[] writeOffsets = new Object[length];
            _writeOffsets.put(port, writeOffsets);
        }
    }

}
