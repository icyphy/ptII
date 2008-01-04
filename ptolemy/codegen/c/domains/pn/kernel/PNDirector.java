/* Code generator helper class associated with the PNDirector class.

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
package ptolemy.codegen.c.domains.pn.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////PNDirector

/**
 Code generator helper associated with the PNDirector class.
 This director initializes all the actors, then starts a thread
 for each actor that invokes the fire code for the actor in an
 infinite loop.

 FIXME: No communication between actors is implemented yet.

 FIXME: How to make it possible for executions to be finite?

 @author Edward A. Lee (based on SDFDirector helper class)
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class PNDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  PNDirector.
     *  @param pnDirector The associated
     *  ptolemy.domains.pn.kernel.PNDirector
     */
    public PNDirector(ptolemy.domains.pn.kernel.PNDirector pnDirector) {
        super(pnDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate code for declaring read and write offset variables if needed.
    *
    *  @return The generated code.
    *  @exception IllegalActionException If thrown while creating
    *  offset variables.
    */
   public String createOffsetVariablesIfNeeded() throws IllegalActionException {
       StringBuffer code = new StringBuffer();
       //code.append(_createOffsetVariablesIfNeeded());
       code.append(super.createOffsetVariablesIfNeeded());
       List actorList = 
           ((CompositeEntity) _director.getContainer()).deepEntityList();
       
       Iterator actors = actorList.iterator();
       while (actors.hasNext()) {
           Iterator ports = ((Entity) actors.next()).portList().iterator();
           while (ports.hasNext()) {
               code.append(_createDynamicOffsetVariables((IOPort) ports.next()));
           }
       }
       return code.toString();
   }
   
   /** Generate the body code that lies between variable declaration
    *  and wrapup.
    *  @return The generated body code.
    *  @exception IllegalActionException If the
    *  {@link #generateFireCode()} method throws the exceptions.
    */
   public String generateFireCode() throws IllegalActionException {
       StringBuffer code = new StringBuffer();
       CompositeActor compositeActor = 
           (CompositeActor) _director.getContainer();       
       
       code.append(_codeGenerator.comment("Create a thread for each actor."));
       code.append(_eol + "pthread_attr_t pthread_custom_attr;" + _eol);
       code.append("pthread_attr_init(&pthread_custom_attr);" + _eol + _eol);

       List actorList = compositeActor.deepEntityList();
       
       Iterator actors = actorList.iterator();
       while (actors.hasNext()) {
           Actor actor = (Actor) actors.next();
           
           code.append("pthread_create(");
           code.append("&thread_" + _getActorThreadLabel(actor));
           code.append(", &pthread_custom_attr, ");
           code.append(_getActorThreadLabel(actor));
           code.append(", NULL);" + _eol);
       }

       return code.toString();
   }

   /** Do nothing in generating fire function code. The fire code is
     *  wrapped in a for/while loop inside the thread function.
     *  The thread function is generated in 
     *  {@link #generatePreinitializeCode()} outside the main function. 
     *  @return An empty string.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        _generateThreadFunctionCode(code);
        return code.toString();
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException If something goes wrong.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("<pthread.h>");
        //files.add("<thread.h>");
        return files;
    }

    /** Return the libraries specified in the "libraries" blocks in the
     *  templates of the actors included in this CompositeActor.
     *  @return A Set of libraries.
     *  @exception IllegalActionException If thrown when gathering libraries.
     */
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new LinkedHashSet();
        libraries.add("pthread");
        //libraries.add("thread");
        return libraries;
    }
    
    /** Generate the initialize code for the associated PN director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        code.append(_codeGenerator
                .comment("Initialization code of the PNDirector."));

        // Generate the code to initialize all the actors.
        //code.append(super.generateInitializeCode());
        return code.toString();
    }
    
    /**
     * 
     */
    public String generateMainLoop()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
        .booleanValue();
        
        if (inline) {
            code.append(generateFireCode());
        } else {
            code.append(CodeGeneratorHelper.generateName(_director
                    .getContainer()) + "();" + _eol);
        }
        
        return code.toString();
    }

    /** Generate the preinitialize code for the associated PN director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = 
            new StringBuffer(super.generatePreinitializeCode());
        
        // Generate the global terminate flag which tells actor
        // whether or not to continue execution.
        code.append("boolean " + 
                _generateNotTerminateFlag() + " = true;" + _eol);
        
        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE) {
            _generateThreadFunctionCode(code);
        }
        
        return code.toString();
    }

    public String generatePostfireCode() throws IllegalActionException {
        return "";
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        return "";
    }
    
    /** Generate the wrapup code for the associated PN director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Note: We don't need to call the super class method nor
        // append the wrapup code for each of the containing actor.
        // Instead, the actor wrapup code resides in the actor 
        // thread function code.
        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();

        while (actors.hasNext()) {
            // Generate the thread pointer.
            Actor actor = (Actor) actors.next();
            
            code.append("pthread_join(");
            code.append("thread_" + _getActorThreadLabel(actor));
            code.append(", NULL);" + _eol);
        }
        return code.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Generate the thread function name for a given actor.
     * @param actor The given actor.
     * @return A unique label for the actor thread function.
     */
    private String _getActorThreadLabel(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor)
                + "_ThreadFunction";
    }

    /** Create offset variables for the channels of the given port.
     *  The offset variables are generated unconditionally.
     *
     *  @param port The port whose offset variables are generated.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    protected String _createDynamicOffsetVariables(IOPort port)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        int width;
        if (port.isInput()) {
            width = port.getWidth();
        } else {
            width = port.getWidthInside();
        }

        if (width != 0) {
            // Declare the read offset variable.
            String channelReadOffset = CodeGeneratorHelper.generateName(port);
            channelReadOffset += "_readOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                String offsetString = channelReadOffset + "_" + i;
                helper.setReadOffset(port, i, offsetString);
                code.append("static int " + offsetString + ";\n");
            }

            // Declare the write offset variable.
            String channelWriteOffset = CodeGeneratorHelper.generateName(port);

            channelWriteOffset += "_writeOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                String offsetString = channelReadOffset + "_" + i;
                helper.setWriteOffset(port, i, offsetString);
                code.append("static int " + offsetString + ";\n");
            }
        }
        return code.toString();
    }

    /** Generate the notTerminate flag variable for the associated PN director.
     * Generating notTerminate instead of terminate saves the negation in checking
     * the flag (e.g. "while (!terminate) ..."). 
     * @return The varaible label of the notTerminate flag.
     */
    protected String _generateNotTerminateFlag() {
        return CodeGeneratorHelper.generateName(_director) + "_notTerminate";
    }

    /** 
     * @param code The given code buffer.
     * @throws IllegalActionException
     */
    private void _generateThreadFunctionCode(StringBuffer code) throws IllegalActionException {
        List actorList = 
            ((CompositeActor) _director.getContainer()).deepEntityList();
        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
        .booleanValue();

        String notTerminateFlag = _generateNotTerminateFlag();
        
        Iterator actors = actorList.iterator();
        while (actors.hasNext()) {
            // Generate the thread pointer.
            Actor actor = (Actor) actors.next();
            code.append("pthread_t thread_");
            code.append(_getActorThreadLabel(actor));
            code.append(";" + _eol);
        }
        
        // Generate the function for each actor thread.
        actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helper = 
                (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            
            if (!inline) {
                code.append(helper.generateFireFunctionCode());
            } 

            code.append(_eol + "void* " + 
                    _getActorThreadLabel(actor) + "(void* arg) {" + _eol);

            // init
            code.append(helper.generateInitializeCode());
            code.append(helper.generateVariableInitialization());
            
            // mainLoop
            
            // Check if the actor is an opague CompositeActor. 
            // The actor is guaranteed to be opague from calling deepEntityList(),
            // so all we need to check whether or not it is a CompositeActor.
            if (actor instanceof CompositeActor) {
                Director directorHelper = (Director) _getHelper(actor.getDirector()); 

                // If so, it should contain a different Director.
                assert (directorHelper != this);
                
                code.append(directorHelper.generateMainLoop());                    
            } else {

                // if firingCountLimit exists, generate for loop.
                if (actor instanceof LimitedFiringSource) {
                    int firingCount = ((IntToken) ((LimitedFiringSource) actor)
                            .firingCountLimit.getToken()).intValue();
                    code.append("int i = 0;" + _eol);
                    code.append("for (; i < " + firingCount 
                            + " && " + notTerminateFlag + "; i++) {" + _eol);
                    
                } else {
                    code.append("while (" + notTerminateFlag + ") {" + _eol);                
                    //code.append("{" + _eol);
                }
                
                code.append(helper.generateFireCode());

                // If not inline, generateFireCode() would be a call
                // to the fire function which already includes the 
                // type conversion code.
                if (inline) {
                    code.append(helper.generateTypeConvertFireCode());
                }
                
                code.append(helper.generatePostfireCode());
                code.append("}" + _eol);
            }            

            // wrapup
            code.append(helper.generateWrapupCode());
            
            code.append("return NULL;" + _eol);
            code.append("}" + _eol);
        }
    }
}
