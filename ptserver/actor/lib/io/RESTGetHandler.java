/* RESTGetHandler receives incoming RESTful get requests and performs
 some action upon being fired.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptserver.actor.lib.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

/**
RESTGetHandler is an actor that takes a model and a RESTful string specifying a
model resource as inputs, and prints information about that resource to the
destination file in a combination of HTML + Javascript format.

In the future - should we separate actions (REST get) from actors?
I envision a "RESTHandler" which would take care of web connection settings,
and have (at least) four methods (get, post, put, delete) where classes could be
registered to support each method dynamically.  A default method can be inserted
if the operation is not supported (for example, delete).

@author Beth Latronico
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ltrnc)
@Pt.AcceptedRating Red (ltrnc)
@see ptolemy.vergil.basic.export.ExportModel
 */
public class RESTGetHandler extends TypedAtomicActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public RESTGetHandler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);

        destinationFileName = new FileParameter(this, "destinationFile");

        ignoreNamedObjsStartingWithUnderscores = new SharedParameter(this,
                "ignoreNamedObjsStartingWithUnderscores", getClass(), "false");
        ignoreNamedObjsStartingWithUnderscores.setTypeEquals(BaseType.BOOLEAN);

        outputToFile = new Parameter(this, "outputToFile");
        outputToFile.setTypeEquals(BaseType.BOOLEAN);

        sourceModel = new FileParameter(this, "sourceModel");

        resource = new StringParameter(this, "resource");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** The file name to write to.
     *  @see FileParameter
     */
    public FileParameter destinationFileName;

    /** True if NamedObjs that start with underscores should be ignored.
     *  The default is a boolean with value false, indicating that NamedObjs
     *  such as parameters like _vergilSize should be printed out.
     *  This parameters is primarily used for testing.
     */
    public SharedParameter ignoreNamedObjsStartingWithUnderscores;

    /** Output port for the HTML response.
     */
    public TypedIOPort output;

    /** True if the actor should output to the file specified by
     * destinationFileName.  Otherwise, actor sends output to output port.
     */
    public Parameter outputToFile;

    /** The resource to return.  This can be the whole model, any contained
     * entity, or a set of contained entities.  Please see the find() method
     * for a full explanation of the syntax.  Some examples:
     * modelName  - Returns the top-level entity (the whole model) and a list of
     * resources contained by this entity
     *
     * modelName/x  - Returns actor x whose parent is the top-level entity and a
     * list of resources contained by x
     *
     * modelName/x/y  - Returns actor y, whose parent is x, whose parent is the
     * top-level entity, and a list of resources contained by y
     *
     * modelName/ontology.name=unitSystem&amp;concept=Temperature -  Returns all
     * entities who have a port annotated with the concept "Temperature" from
     * the ontology "unitSystem"
     */
    public StringParameter resource;

    /** An optional string specifying the file name or URL of the model to read
     *  from.  If blank, the model containing this actor is used.
     *  @see FileParameter
     */
    public FileParameter sourceModel;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.  This will collect information about the given resource
     * and the named objects it contains, and output this information to either
     * the output port or a file depending on the user's choice.
     *
     *  @exception IllegalActionException If there is a problem finding the
     *  resource or writing to the file.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_readyToFire) {

            if (outputToFile != null && outputToFile.getToken() != null
                    && outputToFile.getToken().equals(BooleanToken.TRUE)) {
                // TODO:  Output to file
            } else {
                output.send(0, new StringToken(getHTML()));
            }
        }
    }

    /** Initialize this actor.  Check that the resource name is properly
     * formatted.  If an external model is given, check that this model can be
     * found and opened.  If an output file is given, check that this file can
     * be found and opened.
     *
     *  @exception IllegalActionException If the resource name is improperly
     *  formatted, if an external model is given but can't be opened, or if an
     *  output file is given but can't be opened.
     */

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Check that the resource is specified in a correctly formatted way.
        // Produce an error message if there is a problem and prohibit firing.
        // TODO:  Check for correct formatting of resource name

        // If an external model source is given, try to open that model.
        // Produce an error message if there is a problem and prohibit firing
        // TODO:  Implement this feature later

        // Open the destination file for writing.
        // Produce an error message if there is a problem and prohibit firing.
        // TODO:  More error checking
        if (outputToFile != null && outputToFile.getToken() != null
                && outputToFile.getToken().equals(BooleanToken.TRUE)) {
            // TODO:  Implement writing to file.  For now, throw an exception
            /*
            _destinationFile = destinationFileName.asFile();
            if (_destinationFile.exists()) {
                _readyToFire = true;
            }
             */
            throw new IllegalActionException(this, "File export is not yet "
                    + "implemented for RESTGetHandler.  Please uncheck the"
                    + "outputToFile parmater, and read the output from the "
                    + "output port.");
        } else {
            _readyToFire = true;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Find and return all named objects referenced by the resource parameter.
     *  Return an empty list if the request contained by the resource request is
     *  malformed.
     *
     * @return  A list of all named objects corresponding to the request
     * contained by the resource parameter.  Can be empty.  Will return an empty
     * list if the request is malformed.
     * @exception IllegalActionException If the resource is improperly formatted
     */
    protected List<NamedObj> findResources() throws IllegalActionException {
        Vector<NamedObj> containers = new Vector<NamedObj>();

        // Get the top-level container
        // TODO:  In future, allow other models to be referenced
        NamedObj container = this;

        while (container.getContainer() != null) {
            container = container.getContainer();
        }
        containers.add(container);

        // Parse the resource string
        // If blank, return the top-level container
        if (resource == null || resource.getExpression() == null) {
            throw new IllegalActionException(this, "Please specify "
                    + "a resource.");
        } else {
            String expression = resource.getExpression();

            StringTokenizer tokenizer = new StringTokenizer(expression);

            while (tokenizer.hasMoreTokens()) {
                String objectName = tokenizer.nextToken("/");

                // Check for case where there is just a / and no name
                // If that happens, query is malformed
                if (!objectName.isEmpty()) {

                    // Check for queries
                    if (objectName.contains(".")) {
                        // TODO:  Handle queries

                    } else {
                        int numContainers = containers.size();

                        // Add new objects
                        for (int i = 0; i < numContainers; i++) {
                            Iterator iterator = containers.get(i)
                                    .containedObjectsIterator();
                            while (iterator.hasNext()) {
                                NamedObj next = (NamedObj) iterator.next();
                                if (next.getName().equals(objectName)) {
                                    containers.add(next);
                                    // Break if object with matching name found
                                    // in current container.
                                    // Ptolemy objects have unique long names,
                                    // so there will not be two objects with the
                                    // same name in the same container.
                                    break;
                                }
                            }
                        }

                        // Delete parent objects
                        for (int i = 0; i < numContainers; i++) {
                            containers.remove(0);
                        }
                    }
                } else {
                    throw new IllegalActionException(this, "The resource "
                            + expression + " is not properly "
                            + "formatted.  Please enter a valid resource.");
                }
            }
        }

        return containers;
    }

    /** Construct and return the HTML response.  The HTML response includes
     * information about the selected entity or entities and links to contained
     * entities.  If no entities are selected, an empty HTML page is returned.
     * Uses HTML5 syntax.
     *
     * @return The HTML response.
     * @exception IllegalActionException If the resource is improperly formatted
     * @see ptolemy.vergil.basic.export.html.ExportHTMLAction
     */
    protected String getHTML() throws IllegalActionException {

        StringBuffer HTML = new StringBuffer();
        HTML.append("<html><head></head><body>");

        // Add information on each matching object and links to its contained
        // objects
        for (NamedObj obj : findResources()) {
            // Name.  Use the name of the ModalModel rather than the controller
            // if we have a model model
            String name = obj.getName();
            if (obj.getContainer() instanceof ModalModel) {
                name = obj.getContainer().getName();
            }

            HTML.append("<h1>" + name + "</h1>");
            HTML.append("http:/" + obj.getFullName().replace(".", "/"));
            HTML.append("<br/>");
            HTML.append(_getParameterTable(obj));

            HTML.append("<h1> Contained resources </h1> <ul>");
            for (String resourceURI : listContainedResources(obj)) {
                HTML.append("<li>" + resourceURI + "</li>");
            }
            HTML.append("</ul>");
        }

        HTML.append("</body></html>");

        return HTML.toString();
    }

    /** Return a list of URIs for all contained objects.  This allows an agent
     * that knows the URI of the main resource (e.g. the top-level model) to
     * discover contained resources (e.g. actors in the model).
     * <p>If ignoreNamedObjsStartingWithUnderscores is true and the name of the
     * containedObject begins with an underscore, then the name is not
     * added to the list.</p>
     *
     * @param obj The Ptolemy object to list contained resources for.
     * @return A list of URIs for all contained objects
     * @exception IllegalActionException If the ignoreNamedObjsStartingWithUnderscores
     * parmeter cannot be read.
     */
    protected List<String> listContainedResources(NamedObj obj)
            throws IllegalActionException {
        ArrayList<String> containedResources = new ArrayList();

        // The URI of an object is its full name with forward slashes instead
        // of periods.  (Periods are used as a query indicator in a resource
        // request instead of the conventional question marks, since an object's
        // short name cannot contain a period but it can contain a question
        // mark).
        Iterator objIterator = obj.containedObjectsIterator();
        while (objIterator.hasNext()) {
            NamedObj nextObj = (NamedObj) objIterator.next();
            if (!(((BooleanToken) ignoreNamedObjsStartingWithUnderscores
                    .getToken()).booleanValue() && nextObj.getName()
                    .startsWith("_"))) {
                containedResources.add("http:/"
                        + nextObj.getFullName().replace(".", "/"));
            }
        }

        return containedResources;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**Copied from ExportHTMLAction
     *
     *  Get an HTML table describing the parameters of the object.
     *  @param object The Ptolemy object to return a table for.
     *  @return An HTML table displaying the parameter values for the
     *   specified object, or the string "Has no parameters" if the
     *   object has no parameters.
     * @see ptolemy.vergil.basic.export.html.ExportHTMLAction
     */
    private String _getParameterTable(NamedObj object) {
        StringBuffer table = new StringBuffer();
        List<Settable> parameters = object.attributeList(Settable.class);
        boolean hasParameter = false;
        for (Settable parameter : parameters) {
            if (parameter.getVisibility().equals(Settable.FULL)) {
                hasParameter = true;
                table.append("<tr><td>");
                table.append(parameter.getName());
                table.append("</td><td>");
                String expression = parameter.getExpression();
                expression = StringUtilities.escapeForXML(expression);
                expression = expression.replaceAll("'", "\\\\'");
                if (expression.length() == 0) {
                    expression = "&nbsp;";
                }
                table.append(expression);
                table.append("</td><td>");
                String value = parameter.getValueAsString();
                value = StringUtilities.escapeForXML(value);
                value = value.replaceAll("'", "\\\\'");
                if (value.length() == 0) {
                    value = "&nbsp;";
                }
                table.append(value);
                table.append("</td></tr>");
            }
        }
        if (hasParameter) {
            table.insert(0, "<table border=&quot;1&quot;>"
                    + "<tr><td><b>Parameter</b></td>"
                    + "<td><b>Expression</b></td>"
                    + "<td><b>Value</b></td></tr>");
            table.append("</table>");
        } else {
            table.append("Has no parameters.");
        }
        return table.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private parameters                ////

    /** The file to write to.  */
    File _destinationFile;

    /**  A flag indicating if the actor is ready to fire.  True if the source
     * model is valid, the destination file is available for writing, and the
     * resource requested is specified in a valid way.  These are checked in
     * initialize().  False otherwise.
     */
    boolean _readyToFire = false;
}
