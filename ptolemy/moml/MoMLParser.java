/* A parser for MoML (model markup language)

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.moml;

// Ptolemy imports.
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.expr.Variable;

// Java imports.
import java.awt.Panel;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Stack;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.net.URL;

// XML imports.
import com.microstar.xml.*;


//////////////////////////////////////////////////////////////////////////
//// MoMLParser
/**
This class constructs Ptolemy II models from specifications
in MoML (model markup language), which is based on XML.
The class contains an instance of the Microstar &AElig;lfred XML
parser and implements callback methods to interpret the parsed XML.
The way to use this class is to call its parse() method.
The returned value is top-level composite entity of the model.

@author Edward A. Lee, Steve Neuendorffer, John Reekie
@version $Id$
*/
public class MoMLParser extends HandlerBase {

    /** Construct a parser that creates a new workspace into which to
     *  put the entities created by the parse() method.
     */
    public MoMLParser() {
        this(null);
    }

    /** Construct a parser that creates entities
     *  in the specified workspace.  If the argument is null,
     *  create a new workspace with an empty name.
     *  @param workspace The workspace into which to place entities.
     */
    public MoMLParser(Workspace workspace) {
        this(workspace, null);
    }

    /** Construct a parser that creates entities
     *  in the specified workspace, and if any of these entities implements
     *  the Placeable interface, then places the entity in the specified
     *  panel.  If the workspace argument is null, then
     *  create a new workspace with an empty name.
     *  If the panel argument is null, then entities implenting Placeable
     *  are not placed.
     *  @param workspace The workspace into which to place entities.
     *  @param panel The panel into which to place Placeable entities.
     */
    public MoMLParser(Workspace workspace, Panel panel) {
        super();
        if (workspace == null) {
            // NOTE: Workspace has no name, to ensure that full names
            // of enties conform to MoML standard of starting with a
            // leading period.
            workspace = new Workspace();
        }
        _workspace = workspace;
        _panel = panel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle an attribute assignment that is part of an XML element.
     *  This method is called prior to the corresponding startElement()
     *  call, so it simply accumulates attributes in a hashtable for
     *  use by startElement().
     *  @param name The name of the attribute.
     *  @param value The value of the attribute, or null if the attribute
     *   is <code>#IMPLIED</code> and not specified.
     *  @param specified True if the value is specified, false if the
     *   value comes from the default value in the DTD rather than from
     *   the XML file.
     *  @exception XmlException If the name or value is null.
     */
    public void attribute(String name, String value, boolean specified)
            throws XmlException {
        if(name == null) throw new XmlException("Attribute has no name",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        // NOTE: value may be null if attribute default is #IMPLIED.
        _attributes.put(name, value);
    }

    /** Handle character data.  In this implementation, the
     *  character data is accumulated in a buffer until the
     *  end element.  Character data appears only in doc elements.
     *  &AElig;lfred will call this method once for each chunk of
     *  character data found in the contents of elements.  Note that
     *  the parser may break up a long sequence of characters into
     *  smaller chunks and call this method once for each chunk.
     *  @param chars The character data.
     *  @param offset The starting position in the array.
     *  @param length The number of characters available.
     */
    public void charData(char[] chars, int offset, int length) {
        _currentCharData.append(chars, offset, length);
    }

    /** End the document.  In this implementation, do nothing.
     *  &AElig;lfred will call this method once, when it has
     *  finished parsing the XML document.
     *  It is guaranteed that this will be the last method called.
     */
    public void endDocument() throws Exception {
    }

    /** End an element. This method pops the current container from
     *  the stack, if appropriate, and also adds specialized attributes
     *  to the container, such as <i>_doc</i>, if appropriate.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        if (elementName.equals("configure")) {
            // NOTE: There doesn't appear to be any more direct way to
            // do this in Java (!).
            byte[] bytes = _currentCharData.toString().getBytes();
            InputStream stream = new ByteArrayInputStream(bytes);
            ((Configurable)_current).configure(_base, stream);

        } else if (elementName.equals("doc")) {
            // Use the special attribute name "_doc" to contain the text.
            // If the attribute already exists, append to its value rather
            // creating a new attribute.
            _checkClass(_current, NamedObj.class,
                    "Element \"doc\" found inside an element that "
                    + "is not a NamedObj. It is: "
                    + _current.toString());
            NamedObj current = (NamedObj)_current;
            Documentation previous
                   = (Documentation)current.getAttribute("_doc");
            if (previous == null) {
                Documentation doc
                        = new Documentation((NamedObj)_current, "_doc");
                doc.setValue(_currentCharData.toString());
            } else {
                previous.setValue(previous.getValue()
                        + "\n" + _currentCharData.toString());
            }
        } else if (
                elementName.equals("attribute")
                || elementName.equals("class")
                || elementName.equals("director")
                || elementName.equals("entity")
                || elementName.equals("port")
                || elementName.equals("relation")
                || elementName.equals("rendition")
                || elementName.equals("vertex")) {
            _current = _containers.pop();
        }
    }

    /** Handle the end of an external entity.  This pops the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemId The URI for the external entity.
     */
    public void endExternalEntity (String systemId) {
        _externalEntities.pop();
    }

    /** Indicate a fatal XML parsing error.
     *  &AElig;lfred will call this method whenever it encounters
     *  a serious error.  This method simply throws an XmlException.
     *  @param message The error message.
     *  @param systemId The URI of the tntity that caused the error.
     *  @param line The approximate line number of the error.
     *  @param column The approximate column number of the error.
     *  @exception XmlException If called.
     */
    public void error(String message, String sysid,
            int line, int column) throws XmlException {
        throw new XmlException(message, _currentExternalEntity(), line, column);
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  That is, relative URLs are interpreted relative to the
     *  first argument. For example, it might be the document
     *  base of an applet.  For example, an applet might use
     *  this method as follows:
     *  <pre>
     *     MoMLParser parser = new MoMLParser();
     *     URL docBase = getDocumentBase();
     *     URL xmlFile = new URL(docBase, modelURL);
     *     TypedCompositeActor toplevel =
     *             parser.parse(docBase, xmlFile.openStream());
     *  </pre>
     *  If the first argument is null, then it is assumed that
     *  all URLs are absolute.
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent a valid MoML file.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception If the parser fails.
     */
    public TypedCompositeActor parse(URL base, InputStream input)
            throws Exception {
        _parser.setHandler(this);
        _base = base;
        if (base == null) {
            _parser.parse(null, null, input, null);
        } else {
            _parser.parse(base.toExternalForm(), null, input, null);
        }
        return _toplevel;
    }

    /** Resolve an external entity.  This method returns null,
     *  which has the effect of defering to &AElig;lfred for
     *  resolution of the URI.  Derived classes may return a
     *  a modified URI (a string), an InputStream, or a Reader.
     *  In the latter two cases, the input character stream is
     *  provided.
     *  @param publicId The public identifier, or null if none was supplied.
     *  @param systemId The system identifier.
     *  @return Null, indicating to use the default system identifier.
     */
    public Object resolveEntity(String publicID, String systemID) {
        return null;
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     */
    public void startDocument() {
        _attributes = new HashMap();
        _toplevel = null;
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     *  @exception XmlException If the element produces an error
     *   in constructing the model.
     */
    public void startElement(String elementName) throws XmlException {
        try {
            // NOTE: The elements are alphabetical below...
            // NOTE: I considered using reflection to invoke a set of
            // methods with names that match the element names.  However,
            // since we can't count on the XML parser to enforce the DTD,
            // this seems dangerous.  It could result in being able to write
            // an XML that would call methods of this class that are not
            // intended to be called, simply by putting in an element
            // whose name matches the method name.  So instead, we do
            // a dumb if...then...elseif... chain with string comparisons.
            if (elementName.equals("attribute")) {
                String attributeName = (String)_attributes.get("name");
                _checkForNull(attributeName,
                        "No name for element \"attribute\"");
                String value = (String)_attributes.get("value");
                _checkClass(_current, NamedObj.class,
                        "Element \"attribute\" found inside an element that "
                        + "is not a NamedObj. It is: "
                        + _current.toString());
                Object attribute = (Attribute)
                        ((NamedObj)_current).getAttribute(attributeName);

                if (attribute == null) {
                    String className = (String)_attributes.get("class");
                    _checkForNull(className,
                            "No class for element \"attribute\"");

                    // Get a constructor for the attribute.
                    Class attributeClass = Class.forName(className);
                    Class[] argTypes = new Class[2];
                    argTypes[0] = NamedObj.class;
                    argTypes[1] = String.class;
                    Constructor attributeConstructor
                            = attributeClass.getConstructor(argTypes);
                    
                    // Invoke the constructor.
                    Object[] arguments = new Object[2];
                    arguments[0] = _current;
                    arguments[1] = attributeName;
                    attribute = attributeConstructor.newInstance(arguments);

                    if (value != null) {
                        if (!(attribute instanceof Variable)) {
                            throw new XmlException("Attribute is not an "
                            + "instance of Variable, so can't set value.",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                        }
                        ((Variable)attribute).setExpression(value);
                    }
                } else {
                    // If value is null and the attribute already
                    // exists, then there is nothing to do.
                    if (value != null) {
                        _checkClass(attribute, Variable.class,
                               "attribute element named \"" + attributeName
                               + "\" is not an instance of Variable.");
                        ((Variable)attribute).setExpression(value);
                    }
                 }
                 _containers.push(_current);
                 _current = attribute;

             } else if (elementName.equals("class")) {
                String className = (String)_attributes.get("extends");
                _checkForNull(className,
                        "No extends attribute for element \"class\"");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"class\"");
                _checkClass(_current, TypedCompositeActor.class,
                       "Element \"class\" found inside an element that "
                       + "is not a TypedCompositeActor. It is: "
                       + _current.toString());

                Object newEntity = _createEntity(className, entityName);
                _containers.push(_current);
                _current = newEntity;

            } else if (elementName.equals("configure")) {
                String source = (String)_attributes.get("source");
                _checkClass(_current, Configurable.class,
                        "Element \"configure\" found inside an element that "
                        + "does not implement Configurable. It is: "
                        + _current.toString());
                if (source != null) {
                    URL xmlFile = new URL(_base, source);
                    InputStream stream = xmlFile.openStream();
                    ((Configurable)_current).configure(_base, stream);
                }
                _currentCharData = new StringBuffer();

            } else if (elementName.equals("director")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"director\"");
                String dirName = (String)_attributes.get("name");
                _checkForNull(dirName, "No name for element \"director\"");
                Class dirClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = TypedCompositeActor.class;
                argTypes[1] = String.class;
                Constructor dirConstructor
                        = dirClass.getConstructor(argTypes);
                Object[] arguments = new Object[2];
                _checkClass(_current, CompositeActor.class,
                        "Element \"director\" found inside an element that "
                        + "is not a CompositeActor. It is: "
                        + _current.toString());
                arguments[0] = _current;
                arguments[1] = dirName;
                _containers.push(_current);
                _current = dirConstructor.newInstance(arguments);

            } else if (elementName.equals("doc")) {
                _currentCharData = new StringBuffer();

            } else if (elementName.equals("entity")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"entity\"");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"entity\"");
                _checkClass(_current, TypedCompositeActor.class,
                        "Element \"entity\" found inside an element that "
                        + "is not a TypedCompositeActor. It is: "
                        + _current.toString());
                Object newEntity = _createEntity(className, entityName);
                if (_panel != null && newEntity instanceof Placeable) {
                    ((Placeable)newEntity).setPanel(_panel);
                }
                _containers.push(_current);
                _current = newEntity;

            } else if (elementName.equals("import")) {
                String source = (String)_attributes.get("source");
                _checkForNull(source, "No source for element \"import\"");

                // Read external model definition and then clone it.
                MoMLParser newParser = new MoMLParser(_workspace);
                URL xmlFile = new URL(_base, source);
                TypedCompositeActor reference =
                        newParser.parse(_base, xmlFile.openStream());
                if (_imports == null) {
                    _imports = new LinkedList();
                }
                // Put the entity at the top of the list so that if there
                // are name duplications in the imports, the most recent
                // import prevails.
                _imports.add(0, reference);

            } else if (elementName.equals("link")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"link\"");
                String relationName = (String)_attributes.get("relation");
                _checkForNull(relationName,
                        "No relation for element \"link\"");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current.toString());
                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                ComponentPort port = _getPort(portName, context);

                // Get relation
                Relation tmpRelation = context.getRelation(relationName);
                _checkForNull(tmpRelation, "No relation named \"" +
                        relationName + "\" in " + context.getFullName());
                ComponentRelation relation = (ComponentRelation)tmpRelation;

                port.link(relation);

             } else if (elementName.equals("location")) {
                String xSpec = (String)_attributes.get("x");
                String ySpec = (String)_attributes.get("x");
                String zSpec = (String)_attributes.get("x");
                _checkForNull(xSpec, "No x attribute for element \"location\"");

                _checkClass(_current, Locatable.class,
                       "Element \"location\" found inside an element that "
                       + "is not Locatable. It is: "
                       + _current.toString());

                int x = Integer.parseInt(xSpec);
                if (ySpec != null) {
                    int y = Integer.parseInt(ySpec);
                    if (zSpec != null) {
                        // Have three dimensions.
                        int z = Integer.parseInt(zSpec);
                        int[] location = {x, y, z};
                        ((Locatable)_current).setLocation(location);
                    } else {
                        // Have two dimensions.
                        int[] location = {x, y};
                        ((Locatable)_current).setLocation(location);
                    }
                } else {
                    // Have one dimension.
                    int[] location = {x};
                    ((Locatable)_current).setLocation(location);
                }

            } else if (elementName.equals("model")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"model\"");
                String modelName = (String)_attributes.get("name");
                _checkForNull(modelName, "No name for element \"model\"");

                Class toplevelClass = Class.forName(className);
                Class[] argTypes = new Class[1];
                argTypes[0] = Workspace.class;
                Constructor toplevelConstructor
                        = toplevelClass.getConstructor(argTypes);
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                _toplevel = (TypedCompositeActor)
                        toplevelConstructor.newInstance(arguments);
                _manager = new Manager(_workspace, "manager");
                _toplevel.setName(modelName);
                _toplevel.setManager(_manager);
                _current = _toplevel;

            } else if (elementName.equals("port")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"port\"");
                String portName = (String)_attributes.get("name");
                _checkForNull(portName, "No name for element \"port\"");

                // Get a constructor for the port.
                Class portClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = ComponentEntity.class;
                argTypes[1] = String.class;
                Constructor portConstructor
                        = portClass.getConstructor(argTypes);

                // Invoke the constructor.
                Object[] arguments = new Object[2];
                _checkClass(_current, ComponentEntity.class,
                        "Element \"port\" found inside an element that "
                        + "is not a ComponentEntity. It is: "
                        + _current.toString());
                arguments[0] = (ComponentEntity)_current;
                arguments[1] = portName;
                _containers.push(_current);
                IOPort port = (IOPort)portConstructor.newInstance(arguments);
                _current = port;

                String direction = (String)_attributes.get("direction");
                _checkForNull(direction, "No direction for element \"port\"");
                port.setOutput(direction.equals("output")
                        || direction.equals("both"));
                port.setInput(direction.equals("input")
                        || direction.equals("both"));                    

            } else if (elementName.equals("relation")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"relation\"");
                String relationName = (String)_attributes.get("name");
                _checkForNull(relationName, "No name for element \"relation\"");
                _checkClass(_current, TypedCompositeActor.class,
                        "Element \"relation\" found inside an element that "
                        + "is not a TypedCompositeActor. It is: "
                        + _current.toString());
                // Get a constructor for the relation.
                Class relationClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = TypedCompositeActor.class;
                argTypes[1] = String.class;
                Constructor relationConstructor
                        = relationClass.getConstructor(argTypes);
            
                // Invoke the constructor.
                Object[] arguments = new Object[2];
                arguments[0] = (TypedCompositeActor)_current;
                arguments[1] = relationName;
                _containers.push(_current);
                _current = relationConstructor.newInstance(arguments);

            } else if (elementName.equals("rendition")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"rendition\"");

                _checkClass(_current, NamedObj.class,
                        "Element \"rendition\" found inside an element that "
                        + "is not a NamedObj. It is: "
                        + _current.toString());

                // Get a constructor for the rendition.
                Class renditionClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = NamedObj.class;
                argTypes[1] = String.class;
                Constructor renditionConstructor
                      = renditionClass.getConstructor(argTypes);
            
                // Invoke the constructor.
                Object[] arguments = new Object[2];
                arguments[0] = (NamedObj)_current;
                arguments[1] = "_icon";

                _containers.push(_current);
                _current = renditionConstructor.newInstance(arguments);

            } else if (elementName.equals("vertex")) {
                String vertexName = (String)_attributes.get("name");
                _checkForNull(vertexName, "No name for element \"vertex\"");

                _checkClass(_current, Relation.class,
                        "Element \"vertex\" found inside an element that "
                        + "is not a Relation. It is: "
                        + _current.toString());

                // Create an instance of Vertex and attach it to the Relation.
                Vertex vertex = new Vertex((Relation)_current, vertexName);

                _containers.push(_current);
                _current = vertex;

            }
        } catch (InvocationTargetException ex) {
            // A constructor or method invoked via reflection has
            // triggered an exception.
            String msg = "XML element \"" + elementName
                    + "\" triggers exception:\n  "
                    + ex.getTargetException().toString();
            throw new XmlException(msg,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                throw (XmlException)ex;
            } else {
                String msg = "XML element \"" + elementName
                        + "\" triggers exception:\n  " + ex.toString();
                throw new XmlException(msg,
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }
        }
        _attributes.clear();
    }

    /** Handle the start of an external entity.  This pushes the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemId The URI for the external entity.
     */
    public void startExternalEntity (String systemId) {
        _externalEntities.push(systemId);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the the URI for the current external entity.
     *  @return A string giving the URI of the external entity being read,
     *   or null if none.
     */
    protected String _currentExternalEntity() {
        return (String)_externalEntities.peek();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // If the first argument is not an instance of the second,
    // throw an exception with the given message.
    private void _checkClass(Object object, Class correctClass, String msg)
            throws XmlException {
        if(!correctClass.isInstance(object)) {
            throw new XmlException(msg,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // If the argument is null, throw an exception with the given message.
    private void _checkForNull(Object object, String message)
            throws XmlException {
        if(object == null) {
            throw new XmlException(message,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // Create a new entity from the specified class name and give
    // it the specified entity name.  If the class name matches
    // an entity that has been previously created (by an absolute
    // or relative name), then that entity is cloned.  Otherwise,
    // the class name is interpreted as a Java class name and we
    // attempt to construct the entity.
    private Object _createEntity(String className, String entityName)
                 throws Exception {
        // First check to see if the class extends a named entity.
        ComponentEntity reference = _searchForEntity(className);
        if (reference == null) {
            // Not a named entity. Invoke the class loader.
            // Get a constructor for the entity.
            Class entityClass = Class.forName(className);
            Class[] argTypes = new Class[2];
            argTypes[0] = TypedCompositeActor.class;
            argTypes[1] = String.class;
            Constructor entityConstructor
                   = entityClass.getConstructor(argTypes);
            
            // Invoke the constructor.
            Object[] arguments = new Object[2];
            arguments[0] = (TypedCompositeActor)_current;
            arguments[1] = entityName;
            return entityConstructor.newInstance(arguments);
        } else {
            // Extending a previously defined entity.  Clone it.
            ComponentEntity newActor = (ComponentEntity)reference.clone();
            
            // Set the name of the clone.
            // NOTE: The container is null, so there will be no
            // name conflict here.  If we were to set the name after
            // setting the container, we could get a spurious name conflict.
            newActor.setName(entityName);

            // Set the container of the clone.
            newActor.setContainer((TypedCompositeActor)_current);
            
            return newActor;
        }
    }

    // Return the port corresponding to the specified port name in the
    // specified composite actor.  If the port belongs directly to the
    // composite actor, then the argument is a simple name.  If the
    // port belongs to a component actor, then the name is the actor
    // name, a period, and the port name.
    // Throw an exception if there is no such port.
    // The returned value is never null.
    private ComponentPort _getPort(String portspec, CompositeEntity context)
            throws XmlException {
        ComponentPort port = (ComponentPort)context.getPort(portspec);
        _checkForNull(port, "No port named \"" + portspec
                + "\" in " + context.getFullName());
        return (ComponentPort)port;
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a component entity with that name.
    // Return null if it is not found.
    private ComponentEntity _searchForEntity(String name) {
        // If the name is absolute, we first have to find a
        // name from the imports that matches.
        if (name.startsWith(".")) {
            // Name is absolute.
            String topLevelName;
            int nextPeriod = name.indexOf(".", 1);
            if (nextPeriod < 1) {
                topLevelName = name.substring(1);
            } else {
                topLevelName = name.substring(1, nextPeriod);
            }
            Iterator entries = _imports.iterator();
            while (entries.hasNext()) {
                TypedCompositeActor candidate
                        = (TypedCompositeActor)entries.next();
                if (candidate.getName().equals(topLevelName)) {
                    if (nextPeriod < 1) {
                        // Found a match.
                        return candidate;
                    } else {
                        ComponentEntity result = candidate.getEntity(
                                name.substring(nextPeriod + 1));
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
            return null;
        } else {
            // Name is relative.
            if (_current instanceof CompositeEntity) {
                return ((CompositeEntity)_current).getEntity(name);
            }
            return null;
        }
    }
           
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Attributes associated with an entity.
    private Map _attributes;

    // Base for relative URLs.
    private URL _base;

    // The stack of objects that contain the current one.
    private Stack _containers = new Stack();

    // The current object in the hierarchy.
    private Object _current;

    // The current character data for the current element.
    private StringBuffer _currentCharData;

    // The relation for the currently active connection.
    private ComponentRelation _currentConnection;

    // List of top-level entities imported via import element.
    private List _imports;

    // The manager for this model.
    private Manager _manager;

    // The panel into which to place Placeable entities.
    private Panel _panel;

    // The parser.
    private XmlParser _parser = new XmlParser();

    // Top-level entity.
    private TypedCompositeActor _toplevel = null;

    // The workspace for this model.
    private Workspace _workspace;

    // The external entities being parsed.
    private Stack _externalEntities = new Stack();
}
