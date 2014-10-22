/* An attribute providing ontology analysis in the event of an exception.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

package ptolemy.data.ontologies;

import java.util.ArrayList;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.lib.ExceptionManagerModel;
import ptolemy.actor.lib.ExceptionSubscriber;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ExceptionAnalyzer

/**
An attribute that analyzes a model in the event of an exception.  To be used 
with {@link ptolemy.actor.lib.ExceptionManager}. 

@author Elizabeth Latronico
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (beth)
@Pt.AcceptedRating Red (beth)
*/

public class ExceptionAnalyzer extends Attribute implements ExceptionSubscriber{

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ExceptionAnalyzer(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
            super(container, name, true);
            
            statusMessage = new StringParameter(this, "statusMessage");
            statusMessage.setExpression("No analysis performed yet");
            statusMessage.setVisibility(Settable.NOT_EDITABLE);
            
            _annotations = new ArrayList();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////
    
    /** A status message reflecting the success or failure of actions taken
     * upon occurrence of an exception.  Implemented as a public parameter so
     * the message can be displayed in the icon.
     */
    public StringParameter statusMessage;  
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ExceptionAnalyzer newObject = 
                (ExceptionAnalyzer) super.clone(workspace);
        newObject._annotations = new ArrayList<OntologyAnnotationAttribute>();
        return newObject;
    }
    
    // TODO:  Analyze input ports also?  Allow forward and backward analysis?
    // (Look to solver to see which to do)
    /** Handle an exception according to the specified policy:
     *  analyze:  Determine the source actor(s) and annotate output ports
     *   with error constraints.
     *  
     *  others:  Delegate to parent class
     *  
     * @param policy The policy
     * @param exception The exception that occurred
     * @return true if the solver completed successfully.
     */
    @Override
    public boolean exceptionOccurred(String policy, Throwable exception) {
        NamedObj toplevel;
        
        // If contained by an ExceptionManager, get toplevel of container
        if (getContainer() != null && 
                getContainer() instanceof ExceptionManagerModel) {
            toplevel = ((ExceptionManagerModel) getContainer())
                    .getModelContainer().toplevel();
        } else {
            toplevel = toplevel();
        }
       
        // Clear any existing OntologyAnnotationAttributes
        // Check that each exists in case user deleted them manually
        for (OntologyAnnotationAttribute annotation : _annotations) {
            if (toplevel.getAttribute(annotation.getName()) != null) {
                annotation.getContainer().removeAttribute(annotation);
            }
        }
        _annotations.clear();
            
        // Look for a constraint solver in the model called
        // ErrorOntologySolver
        // FIXME:  Better way to do this than by name?
                
        Iterator iterator = toplevel.containedObjectsIterator();
        Object object;
        LatticeOntologySolver solver = null;
            
        while (iterator.hasNext()) {
            object = iterator.next();
            if (object instanceof LatticeOntologySolver &&
                   ((NamedObj) object).getName().equals("ErrorOntologySolver")){
                solver = (LatticeOntologySolver) object;
            }
        }
                

        if (solver == null) {
            // Return false if the ontology solver cannot be found, 
            // since this attribute did not resolve the exception.
            statusMessage.setExpression("Cannot analyze: No ontology solver " 
                    + "found");
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e) {
                // Should not happen since expression is legal
            };
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
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e) {
                // Should not happen since expression is legal
            };
            return false;
        }
                    
        Nameable nameable1 = null, nameable2 = null;
                    
        nameable1 = ((KernelException) exception).getNameable1();
        nameable2 = ((KernelException) exception).getNameable2();
                       
        // TODO:  Handle other objects that can be assigned ontology 
        // concepts, such as Parameter?
        if (!((nameable1 != null && nameable1 instanceof Actor) ||
                (nameable2 != null && nameable2 instanceof Actor))) {
            statusMessage.setExpression("Cannot analyze: No source actor " 
                + "identified");
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e) {
                // Should not happen since expression is legal
            };
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
        try {
            solver.invokeSolver();
        }catch(IllegalActionException e){
            statusMessage.setExpression("Failed to invoke solver.");
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e2) {
                // Should not happen since expression is legal
            };
            return false;
        }
                       
        statusMessage.setExpression("Model successfully analyzed");

        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
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
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e) {
                // Should not happen since expression is legal
            };
            return false;
        }
            
        for (Object portObject : actor.outputPortList()) {
            if (portObject instanceof Port) {
                Port port = (Port) portObject;
                
                // The constraint text must refer to the actorname.portname
                // Derive this from the actor's full name, which
                // is of the form .nameOfModel.actorName
                // Remove all characters through the second period
                String constraintText = nameable.getFullName();
                int period = constraintText.indexOf('.');
                if ((period != -1) && (period != constraintText.length() - 1)) {
                     period = constraintText.indexOf('.', period + 1);
                }
                    
                if ((period != -1) &&(period != constraintText.length() - 1)) {
                     constraintText = constraintText.substring(period + 1);
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
                            attribute.setExpression(constraintText + "." 
                                    + port.getName() + ">= Error");
                             _annotations.add(attribute);
                        } catch (IllegalActionException e2) {
                            statusMessage.setExpression("Cannot analyze: " 
                                    + "Cannot annotate port " 
                                    + port.getContainer().getFullName() 
                                    + "." + port.getName());
                            try { 
                                statusMessage.validate();
                            } catch(IllegalActionException e3) {
                                // Should not happen since expression is legal
                            };
                                    return false;
                        }
                   } else {
                       statusMessage.setExpression("Cannot analyze: Cannot " 
                               + "annotate port " 
                               + port.getContainer().getFullName() + "." 
                               + port.getName());
                       try { 
                           statusMessage.validate();
                       } catch(IllegalActionException e2) {
                           // Should not happen since expression is legal
                       };
                       return false;
                   }      
                } catch (IllegalActionException e) {
                    statusMessage.setExpression("Cannot create annotations.");
                    try { 
                        statusMessage.validate();
                    } catch(IllegalActionException e2) {
                        // Should not happen since expression is legal
                    };
                    return false;
                }
            }
        }
        return true;
    }


    /** Nothing to do after exception is handled. 
     * 
     * return true always, since there is nothing to do after handling */
    @Override
    public boolean exceptionHandled(boolean succesful, String message) {
        return true;
    }
   
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** A list of the OntologyAnnotationAttributes created by this 
     *  attribute.  Stored as a list so they can easily be deleted
     *  when a new analysis is calculated.
     */
    private ArrayList<OntologyAnnotationAttribute> _annotations;

}
