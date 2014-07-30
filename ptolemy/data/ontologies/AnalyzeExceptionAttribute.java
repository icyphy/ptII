/* An extension of CatchExceptionAttribute providing ontology analysis.

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.lib.CatchExceptionAttribute;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////AnalyzeExceptionAttribute

/**
An extension of CatchExceptionAttribute, this attribute catches exceptions and
offers ontology analysis.  If the exception cannot be handled, the attribute
indicates this to the Manager.  Status messages may be logged to a file.

@author Elizabeth Latronico
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (beth)
@Pt.AcceptedRating Red (beth)
 */

public class AnalyzeExceptionAttribute extends CatchExceptionAttribute {

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
    public AnalyzeExceptionAttribute(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        policy.addChoice("analyze");

        _annotations = new ArrayList<OntologyAnnotationAttribute>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AnalyzeExceptionAttribute newObject = (AnalyzeExceptionAttribute) super
                .clone(workspace);
        newObject._annotations = new ArrayList<OntologyAnnotationAttribute>();
        return newObject;
    }

    // TODO:  Analyze input ports also?
    /** Handle an exception according to the specified policy:
     *  analyze:  Determine the source actor(s) and annotate output ports
     *   with error constraints.
     *
     *  others:  Delegate to parent class
     *
     *  @exception IllegalActionException If there is a problem writing to
     *   the log file
     */

    @Override
    public boolean handleException(NamedObj context, Throwable exception)
            throws IllegalActionException {

        if (!policy.stringValue().equals("analyze")) {
            return super.handleException(context, exception);
        } else {
            // Clear any existing OntologyAnnotationAttributes
            // Check that each exists in case user deleted them manually
            for (OntologyAnnotationAttribute annotation : _annotations) {
                if (toplevel().getAttribute(annotation.getName()) != null) {
                    annotation.getContainer().removeAttribute(annotation);
                }
            }
            _annotations.clear();

            // Look for a constraint solver in the model called
            // ErrorOntologySolver
            // FIXME:  Better way to do this than by name?

            Iterator iterator = toplevel().containedObjectsIterator();
            Object object;
            LatticeOntologySolver solver = null;

            while (iterator.hasNext()) {
                object = iterator.next();
                if (object instanceof LatticeOntologySolver
                        && ((NamedObj) object).getName().equals(
                                "ErrorOntologySolver")) {
                    solver = (LatticeOntologySolver) object;
                }
            }

            // try/catch for IOException from file writer
            try {
                if (solver == null) {

                    // Return false if the ontology solver cannot be found,
                    // since this attribute did not resolve the exception.
                    _writeMessage("Cannot analyze: No ontology solver found");
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
                    _writeMessage("Cannot analyze: Not a KernelException or "
                            + "subclass");
                    return false;
                }

                Nameable nameable1 = null, nameable2 = null;

                nameable1 = ((KernelException) exception).getNameable1();
                nameable2 = ((KernelException) exception).getNameable2();

                // TODO:  Handle other objects that can be assigned ontology
                // concepts, such as Parameter?
                if (!((nameable1 != null && nameable1 instanceof Actor) || (nameable2 != null && nameable2 instanceof Actor))) {
                    _writeMessage("Cannot analyze: No source actor identified");
                    return false;
                }

                if (nameable1 != null) {
                    // Return false from this method if _annoateActor encounters
                    // a problem
                    if (!_annotateActor(nameable1)) {
                        return false;
                    }
                }

                if (nameable2 != null) {
                    // Return false from this method if _annoateActor encounters
                    // a problem
                    if (!_annotateActor(nameable2)) {
                        return false;
                    }
                }

                // Invoke the solver
                solver.invokeSolver();

                _writeMessage("Model successfully analyzed");

            } catch (IOException e) {
                statusMessage.setExpression("Error:  Cannot write to file.");
                return false;
            }
        }

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

        // try / catch block for IOException
        try {
            NamedObj toplevel = toplevel();
            Actor actor = null;

            if (nameable instanceof Actor) {
                actor = (Actor) nameable;
            } else {
                _writeMessage("Cannot analyze: Source of exception "
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
                    if ((period != -1)
                            && (period != constraintText.length() - 1)) {
                        period = constraintText.indexOf('.', period + 1);
                    }

                    if ((period != -1)
                            && (period != constraintText.length() - 1)) {
                        constraintText = constraintText.substring(period + 1);
                    }

                    // The constraint itself is named after the actor
                    // to avoid name duplications and to help with
                    // traceability.  Underscores replace periods.
                    String actorName = constraintText.replace('.', '_');

                    try {
                        OntologyAnnotationAttribute attribute = new OntologyAnnotationAttribute(
                                toplevel, "ErrorOntologySolver::" + actorName
                                        + "_" + port.getName());

                        attribute.setExpression(constraintText + "."
                                + port.getName() + ">= Error");
                        _annotations.add(attribute);
                    } catch (NameDuplicationException e) {
                        // If one exists already, assume the previous one
                        // can be overwritten.  This can occur if the model
                        // is saved after an exception is caught.
                        OntologyAnnotationAttribute attribute = (OntologyAnnotationAttribute) toplevel()
                                .getAttribute(
                                        "ErrorOntologySolver::" + actorName
                                                + "_" + port.getName());

                        if (attribute != null) {
                            try {
                                attribute.setExpression(constraintText + "."
                                        + port.getName() + ">= Error");
                                _annotations.add(attribute);
                            } catch (IllegalActionException e2) {
                                _writeMessage("Cannot analyze: "
                                        + "Cannot annotate port "
                                        + port.getContainer().getFullName()
                                        + "." + port.getName());
                                return false;
                            }
                        } else {
                            _writeMessage("Cannot analyze: "
                                    + "Cannot annotate port "
                                    + port.getContainer().getFullName() + "."
                                    + port.getName());

                            return false;
                        }
                    } catch (IllegalActionException e3) {
                        _writeMessage("Cannot analyze: "
                                + "Cannot annotate port "
                                + port.getContainer().getFullName() + "."
                                + port.getName());
                        return false;
                    }
                }
            }
        } catch (IOException ioe) {
            statusMessage.setExpression("Error:  Cannot write to file.");
            return false;
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

}
