/* Catch exceptions and handle them with the specified policy.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.Manager;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.OntologyAnnotationAttribute;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ExceptionHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// CatchExceptionAttribute

/**
 This actor tests for exceptions that are expected to occur when
 running a test model. When an exception is
 thrown by the model, this actor is invoked. It has two
 working modes, training mode and non-training mode. If in training mode,
 this actor handles an exception by recording the exception message. If
 not in training mode, this actor first compares the previously stored
 (assumed correct) message to the exception message and then throws an
 exception if the two messages are not the same.
 Also, if a test runs to completion without throwing an exception, this actor
 throws an exception in its wrapup() method. An exception is expected.

 @author Edward A. Lee, Elizabeth Latronico
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class CatchExceptionAttribute extends AbstractInitializableAttribute
        implements ExceptionHandler {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public CatchExceptionAttribute(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        policy = new StringParameter(this, "policy");
        policy.setExpression("throw");
        
        policy.addChoice("analyze");
        policy.addChoice("continue");
        policy.addChoice("throw");
        policy.addChoice("restart");
        policy.addChoice("stop");

        exceptionMessage = new StringParameter(this, "exceptionMessage");
        exceptionMessage.setExpression("No exceptions encountered");
        exceptionMessage.setVisibility(Settable.NOT_EDITABLE); 
        
        statusMessage = new StringParameter(this, "statusMessage");
        statusMessage.setExpression("No exceptions encountered");
        statusMessage.setVisibility(Settable.NOT_EDITABLE);

        _annotations = new ArrayList<OntologyAnnotationAttribute>();
        _resetMessages = true;
        _initialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** The exception message from the caught exception. */
    public StringParameter exceptionMessage;
    
    /** The error handling policy to apply if an exception occurs
     * 
     * One of:  Analyze, Continue, Throw, Restart, Quit
     * TODO:  Is analyze really a policy?  Should be able to combine with 
     * other things
     */
    public StringParameter policy;
    
    /** The latest action, if any, taken by the CatchExceptionAttribute.  
     *  For example, a notification that the model has restarted.   
     *  It offers a way to provide feedback to the user.
     */
    public StringParameter statusMessage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // TODO:  Analyze input ports also? 
    // TODO:  Figure out what makes sense for continue (if anything)
    
    /** Handle an exception according to the specified policy:
     *  analyze:  Determine the source actor(s) and annotate output ports
     *   with error constraints.
     *  
     *  continue: Not implemented yet  
     *   Consume the exception and return control to the director.
     *   Could be valuable for domains like DE or modal models when new
     *   events will arrive.  Probably not appropriate for domains like SDF
     *   where the director follows a predefined schedule based on data flow
     *   (since the actor throwing the exception no longer provides output to 
     *   the next actor).
     *   
     *  throw:  Do not catch the exception.  
     *  
     *  restart:  Stop and restart the model.  Does not apply to exceptions
     *   generated during initialize().
     *   
     *  stop:  Stop the model.
     *   
     *  @param context The object in which the error occurred.
     *  @param exception The exception to be handled.
     *  @return true if the exception is handled; false if this attribute 
     *   did not handle it
     *  @exception IllegalActionException If thrown by the parent
     */
    
    // FIXME:  Appears wrapup() is called just prior to this automatically; 
    // still possible to set parameter values?
    public boolean handleException(NamedObj context, Throwable exception)
            throws IllegalActionException {
        
         // Save the exception message.  Only informational at the moment.
         exceptionMessage.setExpression(exception.getMessage());
        
         Date date = new Date(System.currentTimeMillis());
         SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
         
         // Clear any existing OntologyAnnotationAttributes
         // Check that each exists in case user deleted them manually
         for (OntologyAnnotationAttribute annotation : _annotations) {
             if (toplevel().getAttribute(annotation.getName()) != null) {
                 annotation.getContainer().removeAttribute(annotation);
             }
         }
         _annotations.clear();
         
         // Handle the exception according to the specified policy 
         // TODO:  Apply different policies depending on the type of exception.
         // How would the policy be specified then?
         
         String policyValue = policy.stringValue();
         
         if (policyValue.equals("analyze")) {
             // Look for a constraint solver in the model called
             // ErrorOntologySolver
             // FIXME:  Better way to do this than by name?
             // TODO:  This introduces a dependency on the ontologies package.  
             // Is that OK?
             
             Iterator iterator = toplevel().containedObjectsIterator();
             Object object;
             LatticeOntologySolver solver = null;
             
             while (iterator.hasNext()) {
                 object = iterator.next();
                 if (object instanceof LatticeOntologySolver &&
                         ((NamedObj) object).getName()
                             .equals("ErrorOntologySolver")) {
                     
                     solver = (LatticeOntologySolver) object;
                 }
             }
             
             if (solver == null) {
                 
                 // Return false if the ontology solver cannot be found, since 
                 // this attribute did not resolve the exception.
                 statusMessage.setExpression("Cannot analyze: No ontology " +
                 		"solver found");
                 return false;
             }
             
             // "context" is not always the object that caused the exception
             // (for example, in a divide by zero error, the context is 
             // the top level object i.e. the model container)
             // Instead, check for an instance of KernelException and get the 
             // objects implicated there.  
             // If any, check if object(s) are actors, find any output ports, 
             // and annotate these with an "error" concept
             
             if (!(exception instanceof KernelException)) {
                 statusMessage.setExpression("Cannot analyze: Not a " +
                 		"KernelException or subclass");
                 return false;
             }
             
             Nameable nameable1 = null, nameable2 = null;
             
             nameable1 = ((KernelException) exception).getNameable1();
             nameable2 = ((KernelException) exception).getNameable2();
           
                 
             // TODO:  Handle other objects that can be assigned ontology 
             // concepts, such as Parameter?
             if (!((nameable1 != null && nameable1 instanceof Actor) ||
                     (nameable2 != null && nameable2 instanceof Actor))) {
                 statusMessage.setExpression("Cannot analyze: " +
                         "No source actor identified");
                 return false;
             }
             
             if (nameable1 != null) {
                 // Return false from this method if _annoateActor encounters
                 // a problem
                 if (!_annotateActor(nameable1)){
                     return false;
                 }
             }
             
             if (nameable2 != null) {
                 // Return false from this method if _annoateActor encounters
                 // a problem
                 if (!_annotateActor(nameable2)){
                     return false;
                 }
             }
                 
             // Invoke the solver
             solver.invokeSolver();
                
             statusMessage.setExpression("Model successfully analyzed");
       
         } else if (policyValue.equals("continue")) {
             statusMessage.setExpression("Execution continued at " 
                     + dateFormat.format(date));
             
             // FIXME:  Is continue possible?  Looks like wrapup() is called
             // automatically before handleException()
         } else if (policyValue.equals("throw")) {
             statusMessage.setExpression("Exception thrown at " +
                     dateFormat.format(date));
             
             // Return false if an exception is thrown, since this attribute 
             // did not resolve the exception.
             return false;
             
         } else if (policyValue.equals("restart")){
             // Restarts the model in a new thread
             
             // Check if the model made it through initialize().  If not, return
             // false (thereby leaving exception unhandled)
             if (!_initialized) {
                 
                 // Return false if an exception is thrown, since this attribute 
                 // did not resolve the exception.
                 statusMessage.setExpression("Cannot restart: Error " +
                 		"before or during intialize()");
                 return false;
             }
             
             // Find an actor in the model at the same hierarchy level.  
             // Use the actor to get the manager.
             Manager manager = null;
             
             if (getContainer() != null) {
                 Iterator iterator = getContainer().containedObjectsIterator();
                 while (iterator.hasNext()) {
                     Object obj = iterator.next();
                     if (obj instanceof Actor) {
                         manager = ((Actor) obj).getManager();
                     }
                 }
             }
             
             if (manager != null) {
                 // End execution
                 manager.finish();
                 
                 // Start a new execution in a new thread
                 manager.startRun();
                 
                 // Can it be that easy???
                 
                 statusMessage.setExpression("Model restarted at " +
                         dateFormat.format(date));
                 
                 // Do NOT reset messages in the event of a restart
                 // This way, user can see that model was restarted
                 _resetMessages = false;
                
             } else {
                 throw new IllegalActionException(this, "Cannot restart model" +
                 		" since there is no model Manager.  Perhaps " +
                 		"the model has no actors?");
             }
 
         } else if (policyValue.equals("stop")) {
             statusMessage.setExpression("Model stopped at " +
                     dateFormat.format(date));
             
             // Call validate() to notify listeners of these changes
             exceptionMessage.validate();
             statusMessage.validate();

             // wrapup() is automatically called prior to handleException(), 
             // so don't need to call it again
         } else {
             statusMessage.setExpression("Illegal policy encountered at: "
                     + dateFormat.format(date));
             
             throw new IllegalActionException(this, 
                     "Illegal exception handling policy.");
         }
         
         // Call validate() to notify listeners of these changes
         exceptionMessage.validate();
         statusMessage.validate();
         
         _resetMessages = false;
         
         // Set _initialized to false here, instead of in wrapup(), since 
         // wrapup() is called prior to handleException()
         _initialized = false;
         
        return true;
    }
    
    /** Initialize exceptionMessage and statusMessage.
     *  @exception IllegalActionException If the parent class throws it
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        if (_resetMessages) {
            exceptionMessage.setExpression("No exceptions encountered");
            statusMessage.setExpression("No exceptions encountered");
            
            // Call validate() to notify listeners of these changes
            exceptionMessage.validate();
            statusMessage.validate();
        }
        
        _resetMessages = true;
        _initialized = true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Annotate an actor with error constraints.
     * 
     * @return True if actor was successfully annotated; false otherwise
     */
    private boolean _annotateActor(Nameable nameable) {
        // Find the output ports on this actor, if any, and generate
        // error constraints for them.  Constraints are added to the
        // top level model.

        NamedObj toplevel = toplevel();
        Actor actor = null;
        
        if (nameable instanceof Actor) { 
            actor = (Actor) nameable;
        } else {
            statusMessage.setExpression("Cannot analyze: Source of exception "
                    + nameable.getName() + " is not an actor");
            return false;
        }
        
        for (Object portObject : actor.outputPortList()) {
            if (portObject instanceof Port) {
                
                Port port = (Port) portObject;
                
                // The constraint text must refer to the
                // actorname.portname
                // Derive this from the actor's full name, which
                // is of the form .nameOfModel.actorName
                // Remove all characters through the second period
                String constraintText = nameable.getFullName();
                int period = constraintText.indexOf('.');
                if ((period != -1) && 
                    (period != constraintText.length() - 1)) {
                 period = constraintText.indexOf('.', period + 1);
                }
                
                if ((period != -1) &&
                     (period != constraintText.length() - 1)) {
                    constraintText = 
                            constraintText.substring(period + 1);
                }
                
                // The constraint itself is named after the actor
                // to avoid name duplications and to help with 
                // traceability.  Underscores replace periods.
                String actorName = constraintText.replace('.','_');
                
                try {    
                    OntologyAnnotationAttribute attribute = 
                        new OntologyAnnotationAttribute(toplevel, 
                           "ErrorOntologySolver::" + actorName + "_" +  
                           port.getName());
                    
                    attribute.setExpression(constraintText + "." + 
                           port.getName() + ">= Error");
                    _annotations.add(attribute); 
                } catch(NameDuplicationException e) {
                    // If one exists already, assume the previous one
                    // can be overwritten.  This can occur if the model
                    // is saved after an exception is caught.
                    OntologyAnnotationAttribute attribute = 
                            (OntologyAnnotationAttribute) toplevel()
                            .getAttribute("ErrorOntologySolver::" + 
                            actorName + "_" + port.getName());
                    
                    if (attribute != null) {
                        try {
                        attribute.setExpression(constraintText + "." + 
                                port.getName() + ">= Error");
                         _annotations.add(attribute);
                        } catch (IllegalActionException e2) {
                            statusMessage.setExpression("Cannot analyze: "+
                                    "Cannot annotate port " + 
                                    port.getContainer().getFullName() + "." + 
                                    port.getName());
                            return false;
                        }
                    } else {
                        statusMessage.setExpression("Cannot analyze: "+
                            "Cannot annotate port " + 
                            port.getContainer().getFullName() + "." + 
                            port.getName());
                        
                        return false;
                    }
                } catch(IllegalActionException e3) {
                    statusMessage.setExpression("Cannot analyze: "+
                            "Cannot annotate port " + 
                            port.getContainer().getFullName() + "." + 
                            port.getName());
                    return false;
                }
               
            }
        }
        
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** A list of the OntologyAnnotationAttributes created by this 
     *  attribute.  Stored as a list so they can easily be deleted
     *  when a new analysis is calculated.
     */
    private ArrayList<OntologyAnnotationAttribute> _annotations;
    
    /** True if the model has been initialized but not yet wrapped up; 
     *  false otherwise.  Some policies (e.g. restart) are desirable only 
     *  for run-time exceptions.
     */
    private boolean _initialized;
    
    /** True if the model has been started externally (e.g. by a user);
     * false if the model has been restarted by this attribute.
     */
    private boolean _resetMessages;
}
