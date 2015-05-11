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
package ptolemy.cg.adapter.generic.program.procedural.fmimahybrid.adapters.ptolemy.actor;

import java.util.Iterator;
import java.util.List;

import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.util.StringUtilities;


//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator adapter for typed composite actor.

 @author Man-Kit Leung, Bert Rodiers
 @version $Id: TypedCompositeActor.java 71660 2015-02-26 16:00:53Z f.cremona $
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
     * Here the number of FMUs and the number of connections are
     * determined and declared as #define inside the MA C code
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

        int fmuCount = 0;
        int connectionsCount = 0;

        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors
                    .next();

            codeStream.append("#define " + actor.getName() + " " + fmuCount++
                    + "\n");
        }

        actors = actorList.iterator();
        while (actors.hasNext()) {
            ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors
                    .next();

            //for (TypedIOPort input : actor.inputPortList()) {
            //	connectionsCount++;
            //}
            connectionsCount += actor.inputPortList().size();

            //for (TypedIOPort output : actor.outputPortList()) {
            //}
        }

        codeStream.append("#define NUMBER_OF_FMUS " + actorList.size() + "\n");
        codeStream.append("#define NUMBER_OF_EDGES " + connectionsCount + "\n");
        codeStream
                .append("#define MODEL_NAME \"" + topActor.getName() + "\"\n");

        actors = actorList.iterator();
        codeStream.append("const char* NAMES_OF_FMUS[] = {");
        while (actors.hasNext()) {
            ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors
                    .next();
            codeStream.append("\"" + actor.getName() + "\"");
            if (actors.hasNext())
                codeStream.append(",");
        }
        codeStream.append("};\n" + _eol);
                
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getComponent());
        actors =  topActor.deepEntityList().iterator();
        codeStream.append("int static i = 0;\n");
		codeStream.append("static void setupParameters(FMU *fmu) {\n");
		
		codeStream.append("fmi2Status fmi2Flag = fmu->enterInitializationMode(fmu->component);\n");
		codeStream.append("if (fmi2Flag > fmi2Warning) {\n");
		codeStream.append("error(\"could not initialize model; failed FMI enter initialization mode\");\n");
		codeStream.append("}\n");
		codeStream.append("printf(\"initialization mode entered\\n\");\n");
		
		
		codeStream.append("int _vr = 0;\n");
		codeStream.append("fmi2Integer isPresent = 0;\n");
		codeStream.append("switch(i) {\n");
		int i = 0;
		while (actors.hasNext()) {			
			ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors
					.next();
			
			codeStream.append("case(" + i + "): {\n");
			int j = 0;
			for ( FMIScalarVariable scalar : actor.getScalarVariables()) {
				
				if (scalar.causality.equals(Causality.parameter)) {
					String sanitizedName = StringUtilities
	                        .sanitizeName(scalar.name);
	                Parameter parameter = (Parameter) actor.getAttribute(sanitizedName,
	                        Parameter.class);
	                
                        // Coverity Scan indicates that getAttribute could return null, so we check.
                        if (parameter == null) {
                            throw new InternalErrorException(actor, null, "Could not find parameter \""
                                    + sanitizedName + "\" in " + actor.getFullName());
                        }

	                if (scalar.type instanceof FMIBooleanType) {
	                	codeStream.append("fmi2Boolean tmp_" + i + "_" + j + " = " + parameter.getToken() + ";\n");
	                	codeStream.append("_vr = " + scalar.valueReference + ";\n");
	                	codeStream.append("fmu->setHybridBoolean(fmu->component, &_vr, 1, &tmp_" + i + "_" + j + ", &isPresent);" + _eol);
	                } else if (scalar.type instanceof FMIIntegerType) {
	                	codeStream.append("_vr = " + scalar.valueReference + ";\n");
	                	codeStream.append("fmi2Integer tmp_" + i + "_" + j + " = " + parameter.getToken() + ";\n");
	                	codeStream.append("fmu->setHybridInteger(fmu->component, &_vr, 1, &tmp_" + i + "_" + j + ", &isPresent);" + _eol);
	                } else if (scalar.type instanceof FMIRealType) {
	                	codeStream.append("_vr = " + scalar.valueReference + ";\n");
	                	codeStream.append("fmi2Real tmp_" + i + "_" + j + " = " + parameter.getToken() + ";\n");
	                	codeStream.append("fmu->setHybridReal(fmu->component, &_vr, 1, &tmp_" + i + "_" + j + ", &isPresent);" + _eol);
	                } else if (scalar.type instanceof FMIStringType) {
	                	codeStream.append("_vr = " + scalar.valueReference + ";\n");
	                	codeStream.append("fmi2String tmp_" + i + "_" + j + " = " + parameter.getToken() + ";\n");
	                	codeStream.append("fmu->setHybridString(fmu->component, &_vr, 1, &tmp_" + i + "_" + j + ", &isPresent);" + _eol);
	                }
				}
				j++;				
			}
			i++;
			codeStream.append("break;\n}\n");			
		}
		
		codeStream.append("}\n");		
		codeStream.append("i++;\n");
		
		codeStream.append("fmi2Flag = fmu->exitInitializationMode(fmu->component);\n");
		codeStream.append("printf(\"successfully initialized.\\n\");\n");
		codeStream.append("if (fmi2Flag > fmi2Warning) {\n");
		codeStream.append("error(\"could not initialize model; failed FMI exit initialization mode\");\n");
		codeStream.append("}\n");
		
		codeStream.append("}\n");
        
        codeStream.appendCodeBlock("staticDeclareBlock");

        return processCode(codeStream.toString());
    }

}
