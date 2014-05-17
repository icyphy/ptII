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
import java.util.Date;
import java.util.Iterator;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ExceptionHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;

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

        _resetMessages = true;
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

    /** Handle an exception according to the specified policy:
     *  analyze:
     *  continue:  Consume the exception and return control to the director.
     *   Could be valuable for domains like DE or modal models when new
     *   events will arrive.  Probably not appropriate for domains like SDF
     *   where the director follows a predefined schedule based on data flow
     *   (since the actor throwing the exception no longer provides output to 
     *   the next actor).
     *   
     *   Allow option to clear schedule??
     *   
     *  If in training
     *  mode, simply record the exception message. If not in training mode,
     *  first compare the stored good message against the exception message.
     *  If they are the same, do nothing. Otherwise, throw the exception again.
     *  @param context The object in which the error occurred.
     *  @param exception The exception to be handled.
     *  @return True since the exception has been handled
     *  @exception IllegalActionException If the policy is "throw"
     */
    
    // FIXME:  Appears wrapup() is called just prior to this automatically; 
    // still possible to set parameter values?
    public boolean handleException(NamedObj context, Throwable exception)
            throws IllegalActionException {
        
        // Save the exception message.  Only informational at the moment.
         exceptionMessage.setExpression(exception.getMessage());
        
         Date date = new Date(System.currentTimeMillis());
         SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                 // new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");

         // Handle the exception according 
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
                 throw new IllegalActionException(this, "No " +
                   "LatticeOntologySolver found.  A LatticeOntologySolver " +
                   "using an Error ontology is required for the analyze " +
                   "option.");
             }
             
             // Figure out which actor threw the exception
             // exception.  How to do this?
             
             // Add an error constraint to the model for every output port
             // of that actor
       
         } else if (policyValue.equals("continue")) {
             statusMessage.setExpression("Execution continued at " 
                     + dateFormat.format(date));
             
             // FIXME:  Is continue possible?  Looks like wrapup() is called
             // automatically before handleException()
         } else if (policyValue.equals("throw")) {
             statusMessage.setExpression("Exception re-thrown at " +
                     dateFormat.format(date));
             throw new IllegalActionException(this, exception.getMessage());
         } else if (policyValue.equals("restart")){
             // Restarts the model in a new thread
             
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
    }
    
    /** Do nothing if the policy is continue; else, wrapup.  wrapup() needs
     *  to be overridden since it is automatically called prior to 
     *  handleException()
     *  @exception IllegalActionException If the parent class throws it
     */
    // FIXME:  Can override wrapup() here but it will still be called on all 
    // the other actors.... what to do?  Throw an exception upon trying wrapup
    // if policy is to continue?  Some actors might successfully process
    // their wrapups though.
    
    /*
    public void wrapup() throws IllegalActionException {
        if (policy == null || !policy.stringValue().equals("continue")) {
            super.wrapup();
        }
    }
    */
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** True if the model has been started externally (e.g. by a user);
     * false if the model has been restarted by this attribute.
     */
    private boolean _resetMessages;
}
