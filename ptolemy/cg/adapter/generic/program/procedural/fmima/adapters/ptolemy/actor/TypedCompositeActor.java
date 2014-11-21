/* Code generator adapter for typed composite actor.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor;

import java.util.Iterator;
import java.util.List;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator adapter for typed composite actor.

 @author Man-Kit Leung, Bert Rodiers
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends FMIMACodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /** Generate FMIMA code.
     *  @return The generated FMIMA.
     *  @exception IllegalActionException If there is a problem getting the adapter, getting
     *  the director or generating FMIMA for the director.
     */
    @Override
    public String generateFMIMA() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Extending ProceduralCodeGenerator start.
        //if (getContainer() == null) {
        //    return "fmima: TypedCompositeActor: top level";
        //}
        //NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getComponent());

        code.append(getCodeGenerator()
                .comment(
                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/TypedCompositeActor.java start"
                                + _eol
                                + "   "
                                + adapter.getComponent().getName()
                                + " contains: "));
        // Extending ProceduralCodeGenerator end.

        // Extending GenericCodeGenerator start.
        //code.append(getComponent().getName() + " contains: ");
        // Extending GenericCodeGenerator end.

        Object director = getCodeGenerator().getAdapter(
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        Director directorAdapter = null;
        try {
            directorAdapter = (Director) director;
        } catch (ClassCastException ex) {
            throw new IllegalActionException(
            // Extending ProceduralCodeGenerator start.
                    adapter.getComponent(),
                    // Extending ProceduralCodeGenerator end.

                    // Extending GenericCodeGenerator start.
                    // /*adapter.*/getComponent(),
                    // Extending GenericCodeGenerator end
                    ex, "Failed to cast " + director + " of class "
                            + director.getClass().getName() + " to "
                            + Director.class.getName() + ".");
        }
        code.append(directorAdapter.generateFMIMA());

        code.append(getCodeGenerator()
                .comment(
                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/TypedCompositeActor.java end"));
        return /*processCode(code.toString())*/code.toString();
    }

    /**
     * Generate the preinitialize code. We do not call the super
     * method, because we have arguments to add here
     * This code contains the variable declarations
     *
     * @return The generated preinitialize code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        ptolemy.actor.CompositeActor topActor = (ptolemy.actor.CompositeActor) getComponent();
        
        List actorList = topActor.deepEntityList();
        
        codeStream.appendCodeBlock("variableDeclareBlock");        
                
        int portCount = 0;
        int fmuCount = 0;
        int connectionsCount = 0;
        
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
        	ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors.next();
        	
        	codeStream.append("#define " + actor.getName() + " "+ fmuCount++ + "\n");
        }
        
        actors = actorList.iterator();
        while (actors.hasNext()) {
        	ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors.next();        	
        	
        	for (TypedIOPort input : actor.inputPortList()) {
        		codeStream.append("#define " + input.getContainer().getName() + "_" + input.getName() + " " + portCount++ + "\n");
        		connectionsCount++;
        	}

        	for (TypedIOPort output : actor.outputPortList()) {
        		codeStream.append("#define " + output.getContainer().getName() + "_" + output.getName() + " " + portCount++ + "\n");
        	}
        }

        codeStream.append("#define NUMBER_OF_FMUS " + actorList.size() + "\n");
        codeStream.append("#define NUMBER_OF_EDGES " + connectionsCount + "\n");
                
        codeStream.appendCodeBlock("staticDeclareBlock");

        return processCode(codeStream.toString());
    }

}
