/* Base class for code generators.

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

package ptolemy.codegen.kernel;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// CodeGenerator

/** FIXME
 * 
 *  @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Rachel Zhou
 *  @version $Id$
 *  @since Ptolemy II 5.0
 *  @Pt.ProposedRating Red (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class CodeGenerator extends Attribute implements ComponentCodeGenerator {

	/** Create a new instance of the C code generator.
	 *  @param container The container.
	 *  @param name The name.
	 *  @throws IllegalActionException
	 *  @throws NameDuplicationException
	 */
	public CodeGenerator(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
        
        codeDirectory = new FileParameter(this, "codeDirectory");
        codeDirectory.setExpression("$HOME/codegen");
        new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);
        
        generatorPackage = new StringParameter(this, "generatorPackage");
        generatorPackage.setExpression("ptolemy.codegen.c");
        
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");
        
        // FIXME: We may not want this GUI dependency here...
        // This attibute could be put in the MoML in the library instead
        // of here in the Java code.
        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");
	}
    
    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////
    
    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/codegen.
     */
    public FileParameter codeDirectory;
    
    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to 
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Return a formatted comment containing the
     *  specified string. In this base class, the
     *  comments is a C-style comment, which begins with
     *  "\/*" and ends with "*\/". Subclasses may override this
     *  produce comments that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
        return "/* " + comment + " */\n";
    }
    
    /** Generate the body code that lies between initialize and wrapup.
     *  In this base class, nothing is generated.
     */
    public String generateBodyCode() throws IllegalActionException {
        return "";
    }

    /** Generate code.  This is the main entry point.
     *  @exception 
     *  FIXME: more
     */
    public void generateCode(StringBuffer code) throws KernelException {
        generateInitializeCode(code);
        String bodyCode = generateBodyCode();
        generateVariableDeclarations(code);
        code.append(bodyCode);
        generateWrapupCode(code);
        
        // Write the code to the file specified by codeDirectory.
        try {
            // Check if needs to overwrite.
            if (codeDirectory.asFile().exists()) {
                if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                        + " exists. OK to overwrite?")) {
                    throw new IllegalActionException(this,
                            "Please select another file name.");        
                }
            }
            Writer writer = codeDirectory.openForWriting();
            writer.write(code.toString());
            codeDirectory.close();
        } catch(Exception ex) {
            throw new IllegalActionException(this, ex.getMessage());
        }
    }
    

    /** Generate the code associated with initialization of the
     *  container composite actor. This is created by stringing
     *  together the intialization code for actors contained by
     *  the container of this attribute (in arbitrary order).
     */
    public void generateInitializeCode(StringBuffer code) 
            throws IllegalActionException {
        code.append(comment(
                "Initialize " + getContainer().getFullName()));
        Iterator actors = ((CompositeActor)getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            ComponentCodeGenerator helperObject = _getHelper((NamedObj)actor);
            helperObject.generateInitializeCode(code);
        }
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     */
    public void generateVariableDeclarations(StringBuffer code)
            throws IllegalActionException {
        code.append(comment("Variable Declarations "
                + getContainer().getFullName()));
        Iterator actors = ((CompositeActor)getContainer())
            .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            // Generate variable declarations for referenced parameters.
            CodeGeneratorHelper helper
                = (CodeGeneratorHelper)_getHelper((NamedObj)actor);
            HashSet parameterSet = helper.getReferencedParameter();
            if (parameterSet != null) {
                Iterator parameters = parameterSet.iterator();
                while (parameters.hasNext()) {
                    Parameter parameter= (Parameter)parameters.next();
                    String type = parameter.getType().toString();
                    boolean isArrayType = false;
                    if (type.equals("boolean")) {
                        type = "bool";
                    } else if (type.charAt(0) == '{') {
                        // This should be an ArrayType.
                        StringTokenizer tokenizer
                            = new StringTokenizer("{}");
                        type = tokenizer.nextToken();
                    }
                    code.append(type);
                    code.append(" ");
                    code.append(parameter.getFullName().replace('.', '_'));
                    if (isArrayType) {
                        code.append("[ ]");
                    }
                    code.append(" = ");
                    code.append(parameter.getToken().toString());
                    code.append(";\n"); 
                }
            }
            // Generate variable declarations for input ports.
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort)inputPorts.next();
                if (inputPort.getWidth() == 0) {
                    break;
                }
                code.append(inputPort.getType().toString());
                code.append(" ");
                code.append(inputPort.getFullName().replace('.', '_'));
                
                if (inputPort.isMultiport()) {
                    code.append("[");
                    code.append((new Integer(inputPort.getWidth())).toString());
                    code.append("]");
                }
                // For multi-rate SDF. 
                // This should move to StaticSchedulingCodeGenerator.
                /*
                code.append("[");
                int bufferCapacity = (inputPort.getReceivers())[0].length;
                code.append(new Integer(bufferCapacity).toString());
                code.append("]");
                */
                code.append(";\n");
            }
            // Generate variable declarations for output ports.
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                TypedIOPort outputPort = (TypedIOPort)outputPorts.next();
                // Only generate declarations for those output ports with
                // port width zero. 
                if (outputPort.getWidth() == 0) {
                    // The buffer size of this port is always 1.
                    code.append(outputPort.getType().toString());
                    code.append(" ");
                    code.append(outputPort.getFullName().replace('.', '_'));
                    code.append(";\n");
                }
            }
        }
    }
    
    /** Generate into the specified code stream the code associated
     *  with wrapping up the container composite actor. This is
     *  created by stringing together the wrapup code for the actors
     *  contained by the container of this attribute (in arbitrary
     *  order).
     *  @param code The code stream into which to generate the code.
     */
    public void generateWrapupCode(StringBuffer code) 
            throws IllegalActionException {
        code.append(comment(
                "Wrapup " + getContainer().getFullName()));
        Iterator actors = ((CompositeActor)getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            ComponentCodeGenerator helperObject = _getHelper((NamedObj)actor);
            helperObject.generateWrapupCode(code);
        }
    }
    
    /** Return the associated component, which is always the container.
     *  @return The component for which this is a helper to generate code.
     */
    public NamedObj getComponent() {
    	return getContainer();
    }
    
    public void setContainer(NamedObj container) 
            throws IllegalActionException, NameDuplicationException {
        if (container != null && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
                    "CodeGenerator can only be contained"
                    + " by CompositeEntity");
        }  
        super.setContainer(container);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
           
        String packageName = generatorPackage.stringValue();
        
        String componentClassName = component.getClass().getName();
        String helperClassName = componentClassName
                .replaceFirst("ptolemy", packageName);
    
        Class helperClass = null;
        try {
            helperClass = Class.forName(helperClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, e, 
                    "Cannot find helper class " 
                    + helperClassName);   
        }
    
        Constructor constructor = null;
        try {
            constructor = helperClass
                    .getConstructor(new Class[]{component.getClass()});
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(this, e,
                    "There is no constructor in " 
                    + helperClassName
                    + " which accepts an instance of "
                    + componentClassName
                    + " as the argument.");
        }
    
        Object helperObject = null;
        try {
            helperObject = constructor.newInstance(new Object[]{component});
        } catch (Exception e) {
            throw new IllegalActionException((NamedObj)component, e,
                    "Failed to create helper class code generator.");
        }
    
        if (!(helperObject instanceof ComponentCodeGenerator)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this component: "
                    + component
                    + ". Its helper class does not"
                    + " implement componentCodeGenerator.");
        }
        ComponentCodeGenerator castHelperObject 
                = (ComponentCodeGenerator)helperObject;
        
        return castHelperObject;
    }   
    
}
