/* A parser for MoML (modeling markup language)

 Copyright (c) 1998-2003 The Regents of the University of California.
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import com.microstar.xml.HandlerBase;
import com.microstar.xml.XmlException;
import com.microstar.xml.XmlParser;


//////////////////////////////////////////////////////////////////////////
//// MoMLParser
/**
This class constructs Ptolemy II models from specifications
in MoML (modeling markup language), which is based on XML.
The class contains an instance of the Microstar &AElig;lfred XML
parser and implements callback methods to interpret the parsed XML.
The way to use this class is to call its parse() method.
The returned value is top-level composite entity of the model.
<p>
For convenience, there are several forms of the parse method.
Most of these take two arguments, a base, and some specification
of the MoML to parse (a stream or the text itself).  The base is
used to interpret relative URLs that might be present in the MoML.
For example, the base might be the document base of an applet.
An applet might use this class as follows:
<pre>
   MoMLParser parser = new MoMLParser();
   URL docBase = getDocumentBase();
   URL xmlFile = new URL(docBase, modelURL);
   NamedObj toplevel = parser.parse(docBase, xmlFile);
</pre>
If the first argument to parse() is null, then it is assumed that
all URLs in the MoML file are absolute.
<p>
It can be difficult to create an appropriate URL to give as a base,
particularly if what you have is a file or file name
in the directory that you want to use as a base.  The easiest
technique is to use the toURL() method of the File class.
Some of the URL constructors, for reasons we don't understand,
create URLs that do not work.
<p>
The MoML code given to a parse() method may be a fragment,
and does not need to include the "&lt;?xml ... &gt;" element nor
the DOCTYPE specification.  However, if the DOCTYPE specification
is not given, then the DTD will not be read.  The main consequence
of this, given the parser we are using, is that default values
for attributes will not be set.  This could cause errors.
The parser itself is not a validating parser, however, so it
makes very limited use of the DTD.  This may change in the future,
so it is best to give the DOCTYPE element.
<p>
The parse() methods can be used for incremental parsing.  After
creating an initial model using a call to parse(), further MoML
fragments without top-level entity or class elements can be evaluated
to modify the model.  You can specify the context in which the
MoML to be interpreted by calling setContext().  However, the
XML parser limits each fragment to one element.  So there always has
to be one top-level element.  If you wish to evaluate a group of
MoML elements in some context, set the context and then place your
MoML elements within a group element, as follows:
<pre>
    &lt;group&gt
        ... sequence of MoML elements ...
    &lt;/group&gt
</pre>
The group element is ignored, and just serves to aggregate the MoML
elements, unless it has a name attribute.  If it has a name attribute,
then the name becomes a prefix (separated by a colon) of all the names
of items immediately in the group element. If the value of the name
attribute is "auto", then the group is treated specially. Each item
immediately contained by the group (i.e. not deeply contained) will
be created with its specified name or a modified version of that name
that does not match a pre-existing object already contained by the
container.  That is, when name="auto" is specified, each item is
forced to be created with unique name, rather than possibly matching
a pre-existing item.
<p>
The parse methods throw a variety of exceptions if the parsed
data does not represent a valid MoML file or if the stream
cannot be read for some reason.
<p>
This parser supports the way Ptolemy II handles hierarchical models,
where components are instances cloned from reference models called
"classes." A model (a composite entity) is a "class" in Ptolemy II if
the elementName field of its MoMLInfo object is the string "class".  If a
component is cloned from a class, then when that component exports
MoML, it references the class from which it was cloned
and exports only differences from that class.  I.e., if further changes are
made to the component, it is important that when the component
exports MoML, that those changes are represented in the exported MoML.
This parser ensures that they are by creating an instance of
MoMLAttribute for each change that is made to the clone after cloning,
if necessary, or by marking the modified elements as not class elements.
The MoMLAttribute class exports a MoML description of a change.
This effectively implements an inheritance mechanism, where
a component inherits all the features of the master from which it
is cloned, but then extends the model with its own changes.
<p>
This class works closely with MoMLChangeRequest to implement another
feature of Ptolemy II hierarchy.  In particular, if an entity is cloned
from another that identifies itself as a "class", then any changes that
are made to the class via a MoMLChangeRequest are also made to the clone.
This parser ensures that those changes are <i>not</i> exported when
MoML is exported by the clone, because they will be exported when the
master exports MoML.

@see MoMLChangeRequest
@see MoMLAttribute
@author Edward A. Lee, Steve Neuendorffer, John Reekie, Contributor: Christopher Hylands
@version $Id$
@since Ptolemy II 0.4
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
     *  create a new workspace with an empty name.  Classes will be
     *  created using the classloader that created this class.
     *  @param workspace The workspace into which to place entities.
     */
    public MoMLParser(Workspace workspace) {
        super();
        if (workspace == null) {
            // NOTE: Workspace has no name, to ensure that full names
            // of enties conform to MoML standard of starting with a
            // leading period.
            workspace = new Workspace();
        }
        _workspace = workspace;
    }

    /** Construct a parser that creates entities in the specified workspace.
     *  If the workspace argument is null, then
     *  create a new workspace with an empty name. Classes will be
     *  created using the classloader that created this class.
     *  @param workspace The workspace into which to place entities.
     *  @param loader The class loader that will be used to create classes,
     *  or null if the the bootstrap class loader is to be used.
     */
    public MoMLParser(Workspace workspace, ClassLoader loader) {
        this(workspace);
        _classLoader = loader;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**  Add a MoMLFilter to the end of the list of MoMLFilters used
     *  to translate names.
     *  Note that this method is static.  The specified MoMLFilter
     *  will filter all MoML for any instances of this class.
     *  @param filter  The MoMLFilter to add to the list of MoMLFilters.
     *  @see #addMoMLFilters(List filterList)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList)
     */
    public void addMoMLFilter(MoMLFilter filter) {
        if (_filterList == null) {
            _filterList = new LinkedList();
        }
        _filterList.add(filter);
    }

    /**  Add a List of MoMLFilters to the end of the list of MoMLFilters used
     *  to translate names.
     *  Note that this method is static.  The specified MoMLFilter
     *  will filter all MoML for any instances of this class.
     *  @param filterList The list of MoMLFilters to add to the
     *  list of MoMLFilters to be used to translate names.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList)
     */
    public void addMoMLFilters(List filterList) {
        if (_filterList == null) {
            _filterList = new LinkedList();
        }
        _filterList.addAll(filterList);
    }

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
        if (name == null) throw new XmlException("Attribute has no name",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        // If the current namespace is _AUTO_NAMESPACE, then look up the
        // translation for the name. If there is a translation, then
        // this name has previously been converted in this group.
        // Otherwise, this name has not been previously converted,
        // so we convert it now.  To accomplish the conversion, if
        // we are not at the top level, then the converted name
        // is the result of calling the container's uniqueName()
        // method, passing it the specified name.
        if (_namespace == _AUTO_NAMESPACE
                && _current != null
                && (name.equals("name")
                        || name.equals("port")
                        || name.equals("relation")
                        || name.equals("vertex")
                        || name.equals("pathTo"))) {
            // See whether the name is in the translation table.
            // Note that the name might be compound, e.g. "Const.output",
            // in which case, we need to parse it and check to see whether
            // the first part of it is in the translation table.
            // NOTE: There is a remaining bug (or feature):
            // If the name is absolute, then no translation will be
            // performed.  This may be reasonable behavior.
            boolean nameSeenAlready = false;
            if (_namespaceTranslationTable != null) {
                // If the name contains a period, then it is a compound name.
                String prefix = value;
                String suffix = "";
                // NOTE: Paranoid coding, in case value is null.
                int period = -1;
                if (value != null) {
                    period = value.indexOf(".");
                }
                if (period >= 0) {
                    prefix = value.substring(0, period);
                    suffix = value.substring(period);
                }
                String replacement
                    = (String)_namespaceTranslationTable.get(prefix);
                if (replacement != null) {
                    // Replace name with translation.
                    value = replacement + suffix;
                    nameSeenAlready = true;
                }
            }
            if (!nameSeenAlready && name.equals("name")) {
                // We only convert "name" attributes, not "port" or
                // "relation", etc.
                String oldValue = value;
                value = _current.uniqueName(oldValue);
                _namespaceTranslationTable.put(oldValue, value);
            }
        } else {
            // If we have a non-default namespace, then prepend the namespace.
            // This needs to be done for every attribute whose value is a name.
            if (_namespace != _DEFAULT_NAMESPACE
                    && _namespace != _AUTO_NAMESPACE
                    && (name.equals("name")
                            || name.equals("port")
                            || name.equals("relation")
                            || name.equals("vertex")
                            || name.equals("pathTo"))) {
                value = _namespace + ":" + value;
            }
        }

        // Apply MoMLFilters here.
        // Filters can filter out graphical classes, or change
        // the names of ports to handle backward compatibility.
        if (_filterList != null) {
            Iterator filters = _filterList.iterator();
            String filteredValue = value;
            while (filters.hasNext()) {
                MoMLFilter filter = (MoMLFilter)filters.next();
                filteredValue =
                    filter.filterAttributeValue(_current, name,
                            filteredValue);
            }

            // Sometimes the value we pass in is null, so we only
            // want to skip if filterAttributeValue returns null
            // when passed a non-null value.
            if (value != null && filteredValue == null) {
                // If attribute() found an element to skip, then
                // the first time we startElement(), we do not
                // want to increment _skipElement again in
                // startElement() because we already did it in
                // attribute().
                _skipElementIsNew = true;
                _skipElementName = _parser.getCurrentElement();
                // Is there ever a case when _skipElement would not
                // be 0 here?  I'm not sure . . .
                _skipElement++;
            }
            value = filteredValue;
        }

        // NOTE: value may be null if attribute default is #IMPLIED.
        _attributes.put(name, value);
        _attributeNameList.add(name);
    }

    /** Handle character data.  In this implementation, the
     *  character data is accumulated in a buffer until the end element.
     *  Character data appears only in doc and configure elements.
     *  &AElig;lfred will call this method once for each chunk of
     *  character data found in the contents of elements.  Note that
     *  the parser may break up a long sequence of characters into
     *  smaller chunks and call this method once for each chunk.
     *  @param chars The character data.
     *  @param offset The starting position in the array.
     *  @param length The number of characters available.
     */
    public void charData(char[] chars, int offset, int length) {
        // If we haven't initialized _currentCharData, then we don't
        // care about character data, so we ignore it.
        if (_currentCharData != null) {
            _currentCharData.append(chars, offset, length);
        }
    }

    /** If a public ID is given, and is not that of MoML,
     *  then throw a CancelException, which causes the parse to abort
     *  and return null.  Note that the version number is not checked,
     *  so future versions of MoML should also work.
     *  @param name The name of the document type.
     *  @param publicID The public ID of the document type.
     *  @param systemID The system ID of the document type.
     *  @exception CancelException If the public ID is not that of MoML.
     */
    public void doctypeDecl(String name, String publicID, String systemID)
            throws CancelException {
        if (publicID != null
                && !publicID.trim().equals("")
                && !publicID.startsWith("-//UC Berkeley//DTD MoML")) {
            throw new CancelException(
                    "Public ID is not that of MoML version 1: " + publicID);
        }
    }

    /** End the document. The MoMLParser calls this method once, when
     *  it has finished parsing the complete XML document. It is
     *  guaranteed that this will be the last method called in the XML
     *  parsing process. As a consequence, it is guaranteed that all
     *  dependencies between parameters used in the XML description
     *  are resolved. This method executes any change requests that
     *  may have been made during the parsing process.
     *  @exception CancelException If an error occurs parsing one of the
     *   parameter values, and the user clicks on "cancel" to cancel the
     *   parse.
     */
    public void endDocument() throws Exception {
        if (_handler != null) {
            _handler.enableErrorSkipping(false);
        }
        // Tidy up the undo entry
        if (_undoEnabled && _undoContext != null && _undoContext.hasUndoMoML()) {
            String undoMoML = _undoContext.getUndoMoML();
            if (_undoDebug) {
                // Print out what has been generated
                System.out.println("=======================");
                System.out.println("Generated UNDO MoML: ");
                System.out.print(undoMoML);
                System.out.println("=======================");
            }

            // Create a new undo entry!
            // NOTE: we use the current context to undo the change. This is
            // because a change request sets the context before applying
            // some incremental MoML.
            NamedObj context = _current;
            if (context == null) {
                context = _toplevel;
            }
            MoMLUndoEntry newEntry = new MoMLUndoEntry(context, undoMoML);
            UndoStackAttribute undoInfo = UndoStackAttribute.getUndoInfo(context);
            // If we are in the middle of processing an undo, then this will
            // go onto the redo stack.
            undoInfo.push(newEntry);
            // Clear up the various MoML variables.
            _resetUndo();
        }
        // If there were any unrecognized elements, warn the user.
        if (_unrecognized != null) {
            StringBuffer warning = new StringBuffer(
                    "Warning: Unrecognized XML elements:");
            Iterator elements = _unrecognized.iterator();
            while (elements.hasNext()) {
                warning.append(" " + elements.next().toString());
            }
            try {
                MessageHandler.warning(warning.toString());
            } catch (CancelException ex) {
                // Ignore, since this is a one-time notification.
            }
        }
        // Force evaluation of parameters so that any listeners are notified.
        // This will also force evaluation of any parameter that this variable
        // depends on.
        Iterator parameters = _paramsToParse.iterator();
        while (parameters.hasNext()) {
            Settable param = (Settable)parameters.next();
            // NOTE: We used to catch exceptions here and issue
            // a warning only, but this has the side effect of blocking
            // the mechanism in PtolemyQuery that carefully prompts
            // the user for corrected parameter values.
            try {
                param.validate();
            } catch (Exception ex) {
                if (_handler != null) {
                    int reply = _handler.handleError(
                            "<param name=\""
                            + param.getName()
                            + "\" value=\""
                            + param.getExpression()
                            + "\"/>",
                            (NamedObj)param.getContainer(),
                            ex);
                    if (reply == ErrorHandler.CONTINUE) {
                        continue;
                    }
                }
                // No handler, or cancel button pushed.
                throw ex;
            }
        }
        // Execute any change requests that might have been queued
        // as a consequence of this change request.
        if (_toplevel != null) {
            // Set the top level back to the default
            // found in startDocument.
            _toplevel.setDeferChangeRequests(_previousDeferStatus);
            _toplevel.executeChangeRequests();
        }
    }

    /** End an element. This method pops the current container from
     *  the stack, if appropriate, and also adds specialized properties
     *  to the container, such as <i>_doc</i>, if appropriate.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {

        // Apply MoMLFilters here.
        if (_filterList != null) {
            Iterator filters = _filterList.iterator();
            String filteredElementName = elementName;
            while (filters.hasNext()) {
                MoMLFilter filter = (MoMLFilter)filters.next();
                filteredElementName =
                    filter.filterEndElement(_current, filteredElementName);
            }

            elementName = filteredElementName;
            if (elementName == null) {
                return;
            }
        }

        if (_skipElement <= 0) {
            // If we are not skipping an element, then adjust the
            // configuration nesting and doc nesting counts accordingly.
            // This was illustrated by having the RemoveGraphicalClasses
            // filter remove the SketchedSource from sources.xml,
            // which resulted in _docNesting being decremented from 0 to -1,
            // which caused problems with undo.
            // See test 1.4 in filter/test/RemoveGraphicalClasses.tcl

            // FIXME: Instead of doing string comparisons, do a hash lookup.
            if (elementName.equals("configure")) {
                // Count configure tags so that they can nest.
                _configureNesting--;
                if (_configureNesting < 0) {
                    throw new XmlException(
                            "Internal Error: _configureNesting is "
                            +  _configureNesting
                            + " which is <0, which indicates a nesting bug",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }
            } else if (elementName.equals("doc")) {
                // Count doc tags so that they can nest.
                _docNesting--;
                if (_docNesting < 0) {
                    throw new XmlException(
                            "Internal Error: _docNesting is " +  _docNesting
                            + " which is <0, which indicates a nesting bug",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }
            }

            if (_configureNesting > 0 || _docNesting > 0) {
                // Inside a configure or doc tag.
                // Simply replicate the element in the current
                // character buffer.
                _currentCharData.append("</");
                _currentCharData.append(elementName);
                _currentCharData.append(">");
                return;
            }
        }

        if ( _skipRendition ) {
            if (elementName.equals("rendition")) {
                _skipRendition = false;
            }
        } else if (_skipElement > 0) {
            if (elementName.equals(_skipElementName)) {
                // Nested element name.  Have to count so we properly
                // close the skipping.
                _skipElement--;
            }
        } else if (elementName.equals("configure")) {
            try {
                ((Configurable)_current).configure(
                        _base, _configureSource, _currentCharData.toString());
            } catch (NoClassDefFoundError e) {
                // If we are running without a display and diva.jar
                // is not in the classpath, then we may get"
                // "java.lang.NoClassDefFoundError: diva/canvas/Figure"
            }
        } else {
            // The doc and group used to be part of "else if" above but had
            // to move into here to handle undo
            if (elementName.equals("doc")) {
                // NOTE: for the undo of a doc element, all work is done here
                if (_currentDocName == null && _docNesting == 0) {
                    _currentDocName = "_doc";
                }
                // For undo need to know if a previous doc attribute with this
                // name existed
                Documentation previous =
                    (Documentation)_current.getAttribute(_currentDocName);
                String previousValue = null;
                if (previous != null) {
                    previousValue = previous.getValue();
                }

                // Set the doc value only if there is character data
                // and if it differs from the previous.
                // NOTE: This will replace any preexisting doc element with the
                // same name, since Documentation is a SigletonAttribute.
                if (_currentCharData.length() > 0
                        && !_currentCharData.equals(previousValue)) {
                    if (previous != null) {
                        previous.setExpression(_currentCharData.toString());
                    } else {
                        Documentation doc
                                = new Documentation(_current, _currentDocName);
                        doc.setValue(_currentCharData.toString());
                    }
                } else {
                    // Empty doc tag.  Remove previous doc element, if
                    // there is one.
                    if (previous != null) {
                        previous.setContainer(null);
                    }
                }
                if (_undoEnabled && _undoContext.isUndoable()) {
                    _undoContext.appendUndoMoML("<doc name=\"" +
                            _currentDocName + "\">");
                    if (previous != null) {
                        _undoContext.appendUndoMoML(previousValue);
                    }
                    _undoContext.appendUndoMoML("</doc>\n");
                }
                _currentDocName = null;
            }
            else if (elementName.equals("group")) {
                try {
                    _namespace = (String)_namespaces.pop();
                }
                catch (EmptyStackException ex) {
                    _namespace = _DEFAULT_NAMESPACE;
                }
            }
            else if (
                    elementName.equals("property")
                    || elementName.equals("class")
                    || elementName.equals("deleteEntity")
                    || elementName.equals("deletePort")
                    || elementName.equals("deleteProperty")
                    || elementName.equals("deleteRelation")
                    || elementName.equals("director")
                    || elementName.equals("entity")
                    || elementName.equals("model")
                    || elementName.equals("port")
                    || elementName.equals("relation")
                    || elementName.equals("rendition")
                    || elementName.equals("vertex")) {
                try {
                    _current = (NamedObj)_containers.pop();
                    _namespace = (String)_namespaces.pop();
                }
                catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _current = null;
                    _namespace = _DEFAULT_NAMESPACE;
                }
            }
        }

        // Handle the undoable aspect, if undo is enabled.
        // FIXME: How should _skipElement and _undoEnable interact?
        // If we are skipping an element, are we sure that we want
        // to add it to the undoContext?
        if (_undoEnabled && _isUndoableElement(elementName)) {
            try {
                // Get the result from this element, as we'll be pushing
                // it onto the stack of children MoML for the parent context
                String undoMoML = _undoContext.generateUndoEntry();
                if (_undoDebug) {
                    System.out.println("Completed element: " + elementName +
                            "\n" + _undoContext.getUndoMoML());
                }
                // Reset the undo context to the parent.
                // NOTE: if this is the top context, then doing a pop here
                // will cause the EmptyStackException
                _undoContext = (UndoContext)_undoContexts.pop();
                // Push the child's undo MoML on the stack of child
                // undo entries.
                _undoContext.pushUndoEntry(undoMoML);
            }
            catch (EmptyStackException ex) {
                // If get here typically means that we are back at the top
                // level, and the current _undoContext has the undo MoML
                // we want. Do nothing.
                if (_undoDebug) {
                    System.out.println("Reached top level of undo " +
                            "context stack");
                }
            }
        }
    }

    /** Handle the end of an external entity.  This pops the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemID The URI for the external entity.
     */
    public void endExternalEntity(String systemID) {
        _externalEntities.pop();
    }

    /** Indicate a fatal XML parsing error.
     *  &AElig;lfred will call this method whenever it encounters
     *  a serious error.  This method simply throws an XmlException.
     *  @param message The error message.
     *  @param systemID The URI of the entity that caused the error.
     *  @param line The approximate line number of the error.
     *  @param column The approximate column number of the error.
     *  @exception XmlException If called.
     */
    public void error(String message, String systemID,
            int line, int column) throws XmlException {
        String currentExternalEntity = "";
        try {
            // Error message methods should be very careful to handle
            // exceptions while trying to provide the user with information
            currentExternalEntity = _currentExternalEntity();
        } catch (java.util.EmptyStackException emptyStack) {
        }

        throw new XmlException(message, currentExternalEntity, line, column);
    }

    /** Get the error handler to handle parsing errors.
     *  Note that this method is static. The returned error handler
     *  will handle all errors for any instance of this class.
     *  @return The ErrorHandler currently handling errors.
     */
    public static ErrorHandler getErrorHandler() {
        return _handler;
    }

    /** Get the List of MoMLFilters used to translate names.
     *  Note that this method is static.  The returned MoMLFilters
     *  will filter all MoML for any instances of this class.
     *  @return The MoMLFilters currently filtering.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #setMoMLFilters(List filterList)
     */
    public List getMoMLFilters() {
        return _filterList;
    }

    /** Get the top-level entity associated with this parser, or null if none.
     *  @return The top-level associated with this parser.
     */
    public NamedObj getToplevel() {
        return _toplevel;
    }

    /** Return the value set by setModified(), or false if setModified()
     *  has yet not been called.
     *  @see #setModified(boolean)
     *  @return True if the data has been modified.
     */
    public static boolean isModified() {
        return _modified;
    }

    /** Parse the MoML file at the given URL, which may be a file
     *  on the local file system, using the specified base
     *  to expand any relative references within the MoML file.
     *  This method uses parse(URL, InputStream).
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model, or
     *   null if the file is not recognized as a MoML file.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, URL input)
            throws Exception {
        _xmlFile = input;
        try {
            return parse(base, input.openStream());
        } finally {
            _xmlFile = null;
        }
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  This method uses parse(URL, Reader).
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model, or
     *   null if the file is not recognized as a MoML file.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, InputStream input)
            throws Exception {
        return parse(base, new InputStreamReader(input));
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  The reader is wrapped in a BufferedReader before being used.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param reader The reader from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model, or
     *   null if the file is not recognized as a MoML file.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, Reader reader) throws Exception {
        _parser.setHandler(this);
        _base = base;
        Reader buffered = new BufferedReader(reader);
        try {
            if (base == null) {
                _parser.parse(null, null, buffered);
            } else {
                // If we have tmp.moml and tmp/tmp2.moml and tmp.moml
                // contains     <entity name="tmp2" class="tmp.tmp2">
                // then we want to be sure that we set _xmlFile properly

                // FIXME: I'm not sure if it is necessary to check to
                // see if _xmlFile is null before hand, but it seems
                // like it is safer to check before resetting it to null.
                boolean xmlFileWasNull = false;
                if (_xmlFile == null) {
                    xmlFileWasNull = true;
                    _xmlFile = new URL(base.toExternalForm());
                }
                try {
                    _parser.parse(base.toExternalForm(), null, buffered);
                } finally {
                    if (xmlFileWasNull) {
                        _xmlFile = null;
                    }
                }
            }
        } catch (CancelException ex) {
            // Parse operation cancelled.
            buffered.close();
            return null;
        }
        buffered.close();

        if (_toplevel == null) {
            // If we try to read a HSIF file but Ptolemy is not properly
            // configured, then we may end up here.
            throw new Exception(
                    "Toplevel was null?  Perhaps the xml does not contain "
                    + "a Ptolemy model?\n base ='" + base + "',\n reader = '"
                    + reader + "'");
        }

        // Add a parser attribute to the toplevel to indicate a parser
        // responsible for handling changes, unless there already is a
        // parser, in which case we just set the parser to this one.
        MoMLParser parser = ParserAttribute.getParser(_toplevel);
        if (parser != this) {
            // Force the parser to be this one.
            ParserAttribute parserAttribute = (ParserAttribute)
                    _toplevel.getAttribute("_parser", ParserAttribute.class);
            if (parserAttribute == null) {
                parserAttribute = new ParserAttribute(_toplevel, "_parser");
            }
            parserAttribute.setParser(this);
        }

        return _toplevel;
    }

    /** Parse the given string, which contains MoML.
     *  If there are external references in the MoML, they are interpreted
     *  relative to the current working directory.
     *  Note that this method attempts to read the user.dir system
     *  property, which is not generally available in applets.  Hence
     *  it is probably not a good idea to use this method in applet code,
     *  since it will probably fail outright.
     *  @param text The string from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     *  @exception SecurityException If the user.dir system property is
     *  not available.
     */
    public NamedObj parse(String text) throws Exception {
        URL base = null;
        // Use the current working directory as a base.
        String cwd = StringUtilities.getProperty("user.dir");
        if (cwd != null) {
            // We have to append a trailing / here for this to
            // work under Solaris.
            base = new URL("file", null, cwd + "/");
        }

        return parse(base, new StringReader(text));
    }

    /** Parse the given string, which contains MoML, using the specified
     *  base to evaluate relative references.
     *  This method uses parse(URL, Reader).
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param text The string from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, String text) throws Exception {
        return parse(base, new StringReader(text));
    }

    /** Parse the given file, which contains MoML.
     *  If there are external references in the MoML, they are interpreted
     *  relative to the current working directory.
     *
     *  <p>If you have an absolute pathname, rather than calling
     *  this method, you may want to try:
     *  <pre>
     *  CompositeActor toplevel = (CompositeActor) parser.parse(null,
     *           new File(xmlFilename).toURL());
     *  </pre>
     *
     *  <p>Note that this method attempts to read the user.dir system
     *  property, which is not generally available in applets.  Hence
     *  it is probably not a good idea to use this method in applet code,
     *  since it will probably fail outright.
     *
     *  @param filename The file name from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     *  @exception SecurityException If the user.dir system property is
     *  not available.
     */
    public NamedObj parseFile(String filename) throws Exception {
        URL base = null;
        // Use the current working directory as a base.
        String cwd = StringUtilities.getProperty("user.dir");
        if (cwd != null) {
            // We have to append a trailing / here for this to
            // work under Solaris.
            base = new URL("file", null, cwd + "/");
        }

        // Java's I/O is so lame that it can't find files in the current
        // working directory...
        File file = new File(new File(cwd), filename);
        FileReader input = new FileReader(file);
        _xmlFile = file.toURL();
        try {
            return parse(base, input);
        } finally {
            _xmlFile = null;
        }
    }

    /** Handle a processing instruction.  Processing instructions
     *  are allowed in doc and configure elements, and are passed through
     *  unchanged.  In the case of the doc element, they will be stored
     *  in the Documentation attribute.  In the case of the configure
     *  element, they will be passed to the configure() method
     *  of the parent object.
     *  @param target The name of the processing instruction.
     *  @param data The body of the processing instruction.
     */
    public void processingInstruction(String target, String data) {
        if (_currentCharData != null) {
            _currentCharData.append("<?");
            _currentCharData.append(target);
            _currentCharData.append(" ");
            _currentCharData.append(data);
            _currentCharData.append("?>");
        }
    }

    /** Reset the MoML parser, forgetting about any previously parsed
     *  models.
     */
    public void reset() {
        _attributes = new HashMap();
        _configureNesting = 0;
        _containers = new Stack();
        _current = null;
        _docNesting = 0;
        _externalEntities = new Stack();
        _modified = false;
        _namespace = _DEFAULT_NAMESPACE;
        _namespaces = new Stack();
        _namespaceTranslations = new Stack();
        _skipRendition = false;
        _skipElementIsNew = false;
        _skipElement = 0;
        _toplevel = null;
        // Reset undo specific members
        _resetUndo();
    }

    /** Resolve an external entity.  If the first argument is the
     *  name of the MoML PUBLIC DTD ("-//UC Berkeley//DTD MoML 1//EN"),
     *  then return a StringReader
     *  that will read the locally cached version of this DTD
     *  (the public variable MoML_DTD_1). Otherwise, return null,
     *  which has the effect of deferring to &AElig;lfred for
     *  resolution of the URI.  Derived classes may return a
     *  a modified URI (a string), an InputStream, or a Reader.
     *  In the latter two cases, the input character stream is
     *  provided.
     *  @param publicID The public identifier, or null if none was supplied.
     *  @param systemID The system identifier.
     *  @return Null, indicating to use the default system identifier.
     */
    public Object resolveEntity(String publicID, String systemID) {
        if (publicID != null &&
                publicID.equals(MoML_PUBLIC_ID_1)) {
            // This is the generic MoML DTD.
            return new StringReader(MoML_DTD_1);
        } else {
            return null;
        }
    }

    /** Given the name of a MoML class and a source URL, check to see
     * whether this class has already been instantiated, and if so,
     * return the previous instance.
     * @param name The name of the MoML class to search for.
     * @param source The URL source
     * @return If the class has already been instantiated, return
     * the previous instance.
     */
    public ComponentEntity searchForClass(String name, String source)
            throws XmlException {

        // If the name is absolute, the class may refer to an existing
        // entity.  Check to see whether there is one with a matching source.
        ComponentEntity candidate;
        if (name.startsWith(".")) {
            candidate = _searchForEntity(name);
            if (candidate != null) {
                // Check that it's a class.
                if (candidate.getMoMLInfo().elementName.equals("class")) {
                    // Check that its source matches.
                    String candidateSource = candidate.getMoMLInfo().source;

                    if (source == null && candidateSource == null) {
                        return candidate;
                    } else if (source != null
                            && source.equals(candidateSource)) {
                        return candidate;
                    }
                }
            }
        }
        if (_imports != null) {
            Iterator entries = _imports.iterator();
            while (entries.hasNext()) {
                Object possibleCandidate = entries.next();
                if (possibleCandidate instanceof ComponentEntity) {
                    candidate = (ComponentEntity)possibleCandidate;
                    String candidateClassName
                        = candidate.getMoMLInfo().className;
                    if (candidateClassName.equals(name)) {
                        String candidateSource = candidate.getMoMLInfo().source;
                        if (source == null && candidateSource == null) {
                            return candidate;
                        } else if (source != null
                                && source.equals(candidateSource)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Set the context for parsing.  This can be used to associate this
     *  parser with a pre-existing model, which can then be modified
     *  via incremental parsing.  This calls reset() and sets the top-level
     *  entity to the top-level of the specified object.
     *  @param context The context for parsing.
     */
    public void setContext(NamedObj context) {
        reset();
        _toplevel = context.toplevel();
        _current = context;
    }

    /** Set the error handler to handle parsing errors.
     *  Note that this method is static. The specified error handler
     *  will handle all errors for any instance of this class.
     *  @param handler The ErrorHandler to call.
     */
    public static void setErrorHandler(ErrorHandler handler) {
        _handler = handler;
    }

    /**  Set the list of MoMLFilters used to translate names.
     *  Note that this method is static.  The specified MoMLFilters
     *  will filter all MoML for any instances of this class.
     *  @param filterList The List of MoMLFilters.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #getMoMLFilters()
     */
    public void setMoMLFilters(List filterList) {
        _filterList = filterList;
    }

    /** Record whether the parsing of the moml modified the data.
     *  If a MoMLFilter modifies the model by returning a different
     *  value, then the MoMLFilter should call this method with a true
     *  argument.
     *  @param modified True if the data was modified while parsing.
     *  @see #isModified()
     *  @see MoMLFilter
     */
    public static void setModified(boolean modified) {
        // NOTE: To see who sets this true, uncomment this:
        // if (modified == true) (new Exception()).printStackTrace();

        _modified = modified;
    }

    /** Set the top-level entity.  This can be used to associate this
     *  parser with a pre-existing model, which can then be modified
     *  via incremental parsing.  This calls reset().
     *  @param toplevel The top-level to associate with this parser.
     */
    public void setToplevel(NamedObj toplevel) {
        reset();
        _toplevel = toplevel;
    }

    /**
     *  Set the current context as undoable. If set to true, the next MoML
     *  parsed will be able to be undone via a call to undo().
     *
     * @param  undoable  The new Undoable value
     * @since Ptolemy II 2.1
     */
    public void setUndoable(boolean undoable) {
        _undoEnabled = undoable;
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     *  In this implementation, this method resets some private variables,
     *  and if there is a top level model associated with this parser,
     *  sets it so that change requests are deferred rather than
     *  executed.  The change requests will be executed as a batch
     *  in endDocument().
     *  @see #endDocument()
     */
    public void startDocument() {
        _paramsToParse.clear();
        _unrecognized = null;
        // We assume that the data being parsed is MoML, unless we
        // get a publicID that doesn't match.

        // Authorize the user interface to offer the user the option
        // of skipping error reporting.
        if (_handler != null) {
            _handler.enableErrorSkipping(true);
        }
        if (_toplevel != null) {
            _previousDeferStatus = _toplevel.isDeferChangeRequests();
            _toplevel.setDeferChangeRequests(true);
        } else {
            // Make sure a default is provided.
            _previousDeferStatus = false;
        }
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
            if (_skipElement <= 0) {
                // If we are not skipping an element, then adjust the
                // configuration nesting and doc nesting counts accordingly.
                // This was illustrated by having the RemoveGraphicalClasses
                // filter remove the SketchedSource from sources.xml,
                // See test 1.3 in filter/test/RemoveGraphicalClasses.tcl
                if (_configureNesting > 0 || _docNesting > 0) {
                    // Inside a configure or doc tag.  First, check to see
                    // whether this is another configure or doc tag.
                    // Then simply replicate the element in the current
                    // character buffer.
                    if (elementName.equals("configure")) {
                        // Count configure tags so that they can nest.
                        _configureNesting++;
                    } else if (elementName.equals("doc")) {
                        // Count doc tags so that they can nest.
                        _docNesting++;
                    }
                    _currentCharData.append(_getCurrentElement(elementName));
                    _attributes.clear();
                    _attributeNameList.clear();
                    return;
                }
            }
            if (_skipRendition) {
                return;
            }

            // Handle the undo aspect of the parsing if enabled
            if (_undoEnabled && _isUndoableElement(elementName)) {
                // First push the current undo context if there is one.
                boolean childNodesUndoable = true;
                if (_undoContext != null) {
                    _undoContexts.push(_undoContext);
                    childNodesUndoable = _undoContext.hasUndoableChildren();
                }
                // Create a new current context
                _undoContext = new UndoContext(childNodesUndoable);
                if (_undoDebug) {
                    System.out.println("Current start element: " + elementName);
                }
            }

            if (_skipElement > 0) {
                if (elementName.equals(_skipElementName)) {
                    // If attribute() found an element to skip, then
                    // the first time we startElement(), we do not
                    // want to increment _skipElement again in
                    // startElement() because we already did it in
                    // attribute().
                    if (_skipElementIsNew) {
                        // After this, _skipElement no longer new.
                        _skipElementIsNew = false;
                    } else {
                        // Nested element name.  Have to count so we properly
                        // close the skipping.
                        _skipElement++;
                    }
                }
                return;
            }

            // NOTE: The elements are alphabetical below...
            // NOTE: I considered using reflection to invoke a set of
            // methods with names that match the element names.  However,
            // since we can't count on the XML parser to enforce the DTD,
            // this seems dangerous.  It could result in being able to write
            // an XML that would call methods of this class that are not
            // intended to be called, simply by putting in an element
            // whose name matches the method name.  So instead, we do
            // a dumb if...then...else.if... chain with string comparisons.
            // FIXME: Instead of doing all these string comparisons, do
            // a hash lookup.
            if (elementName.equals("class")) {
                String className = (String)_attributes.get("extends");
                _checkForNull(className,
                        "Missing \"extends\" attribute for element \"class\"");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"class\"");
                String source = (String)_attributes.get("source");

                NamedObj newEntity = _createEntity(
                        className, entityName, source);
                if (_current != null) {
                    _pushContext();
                } else if (_toplevel == null) {
                    // NOTE: Used to set _toplevel to newEntity, but
                    // this isn't quite right because the entity may have a
                    // composite name.
                    _toplevel = newEntity.toplevel();
                    
                    // Ensure that if any change requests occur as a
                    // consequence of adding items to this top level,
                    // that execution of those change requests is deferred
                    // until endDocument().
                    _toplevel.setDeferChangeRequests(true); 

                    // As early as possible, set URL attribute.
                    // This is needed in case any of the parameters
                    // refer to files whose location is relative
                    // to the URL location.
                    if (_xmlFile != null) {
                        // Add a URL attribute to the toplevel to
                        // indicate where it was read from.
                        URIAttribute attribute
                            = new URIAttribute(_toplevel, "_uri");
                        attribute.setURL(_xmlFile);
                    }
                }
                newEntity.getMoMLInfo().elementName = "class";

                // Adjust the classname and superclass of the object.
                newEntity.getMoMLInfo().className = newEntity.getFullName();
                newEntity.getMoMLInfo().superclass = className;

                _current = newEntity;
                _namespace = _DEFAULT_NAMESPACE;

            } else if (elementName.equals("configure")) {
                _checkClass(_current, Configurable.class,
                        "Element \"configure\" found inside an element that "
                        + "does not implement Configurable. It is: "
                        + _current);
                _configureSource = (String)_attributes.get("source");
                _currentCharData = new StringBuffer();
                // Count configure tags so that they can nest.
                _configureNesting++;
            } else if (elementName.equals("deleteEntity")) {
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName,
                        "No name for element \"deleteEntity\"");

                // NOTE: this also takes care of creating the undo
                // MoML is needed. This is because the deletion has side
                // effects so its not enough to generate the MoML after
                // the entity is deleted.
                NamedObj deletedEntity = _deleteEntity(entityName);

                // NOTE: This could occur at a top level, although it's
                // not clear what it means to delete a top-level entity.
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _pushContext();
                }
                _current = deletedEntity;
                _namespace = _DEFAULT_NAMESPACE;
                if (_undoEnabled && _undoContext.isUndoable()) {
                    // Note that nothing below a deleteEntity is undoable
                    _undoContext.setChildrenUndoable(false);
                }
            } else if (elementName.equals("deletePort")) {
                String portName = (String)_attributes.get("name");
                _checkForNull(portName,
                        "No name for element \"deletePort\"");
                // The entity attribute is optional
                String entityName = (String)_attributes.get("entity");

                // NOTE: this also takes care of creating the undo
                // MoML is needed. This is because the deletion has side
                // effects so its not enough to generate the MoML after
                // the port is deleted.
                NamedObj deletedPort = _deletePort(portName, entityName);
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _pushContext();
                }
                _current = deletedPort;
                _namespace = _DEFAULT_NAMESPACE;
                if (_undoEnabled && _undoContext.isUndoable()) {
                    // Note that nothing below a deletePort is undoable
                    _undoContext.setChildrenUndoable(false);
                }
            } else if (elementName.equals("deleteProperty")) {
                String propName = (String)_attributes.get("name");
                _checkForNull(propName,
                        "No name for element \"deleteProperty\"");
                NamedObj deletedProp = _deleteProperty(propName);
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _pushContext();
                }
                _current = deletedProp;
                _namespace = _DEFAULT_NAMESPACE;
                if (_undoEnabled && _undoContext.isUndoable()) {
                    // NOTE: do not need to move context as property deletions
                    // are with immedaitely contained properties
                    // NOTE: deleting a property does not have side effects,
                    // so it is ok to generate the undo MoML after the
                    // property has been deleted.
                    _undoContext.appendUndoMoML(deletedProp.exportMoML());
                    // Note that nothing below a deleteProperty is undoable
                    _undoContext.setChildrenUndoable(false);
                }
            } else if (elementName.equals("deleteRelation")) {
                String relationName = (String)_attributes.get("name");
                _checkForNull(relationName,
                        "No name for element \"deleteRelation\"");

                // NOTE: this also takes care of creating the undo
                // MoML is needed. This is because the deletion has side
                // effects so its not enough to generate the MoML after
                // the relation is deleted.
                NamedObj deletedRelation = _deleteRelation(relationName);
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _pushContext();
                }
                _current = deletedRelation;
                _namespace = _DEFAULT_NAMESPACE;

                if (_undoEnabled && _undoContext.isUndoable()) {
                    // Note that nothing below a deleteEntity is undoable
                    _undoContext.setChildrenUndoable(false);
                }
            } else if (elementName.equals("director")) {
                // NOTE: The director element is deprecated.
                // Use a property instead.  This is kept here so that
                // this parser can read older MoML files.
                // NOTE: We do not check for a previously existing director.
                // There is presumably no harm in just creating a new one.
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"director\"");
                String dirName = (String)_attributes.get("name");
                _checkForNull(dirName, "No name for element \"director\"");
                _checkClass(_current, CompositeActor.class,
                        "Element \"director\" found inside an element that "
                        + "is not a CompositeActor. It is: "
                        + _current);
                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = dirName;
                // NamedObj container = _current;
                _pushContext();
                Class newClass = Class.forName(className, true, _classLoader);
                _current = _createInstance(newClass, arguments);
                _namespace = _DEFAULT_NAMESPACE;

            } else if (elementName.equals("doc")) {
                _currentDocName = (String)_attributes.get("name");
                _currentCharData = new StringBuffer();
                // Count doc tags so that they can nest.
                _docNesting++;

            } else if (elementName.equals("entity")
                    || elementName.equals("model")) {
                // NOTE: The "model" element is deprecated.  It is treated
                // exactly as an entity.
                String className = (String)_attributes.get("class");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"entity\"");
                String source = (String)_attributes.get("source");
                // For undo purposes need to know if the entity existed
                // already
                ComponentEntity previous = _searchForEntity(entityName);
                boolean existedAlready = false;
                if (previous != null) {
                    existedAlready = true;
                }
                NamedObj newEntity = _createEntity(
                        className, entityName, source);
                // NOTE: The entity may be at the top level.
                if (_current != null) {
                    _pushContext();
                } else if (_toplevel == null) {
                    // NOTE: We used to set _toplevel to newEntity, but
                    // this isn't quite right because the entity may have a
                    // composite name.
                    _toplevel = newEntity.toplevel();
                    
                    // Ensure that if any change requests occur as a
                    // consequence of adding items to this top level,
                    // that execution of those change requests is deferred
                    // until endDocument().
                    _toplevel.setDeferChangeRequests(true); 

                    // As early as possible, set URL attribute.
                    // This is needed in case any of the parameters
                    // refer to files whose location is relative
                    // to the URL location.
                    if (_xmlFile != null) {
                        // Add a URL attribute to the toplevel to
                        // indicate where it was read from.
                        URIAttribute attribute
                            = new URIAttribute(_toplevel, "_uri");
                        attribute.setURL(_xmlFile);
                    }
                }
                _current = newEntity;
                _namespace = _DEFAULT_NAMESPACE;

                if (_undoEnabled && _undoContext.isUndoable()) {
                    // Handle the undo aspect.
                    if (existedAlready) {
                        _undoContext.appendUndoMoML("<entity name=\"" + entityName +
                                "\" >\n");
                        // Need to continue undoing and use an end tag
                        _undoContext.appendClosingUndoMoML("</entity>\n");
                        _undoContext.setChildrenUndoable(true);
                    }
                    else {
                        _undoContext.appendUndoMoML("<deleteEntity name=\"" + entityName +
                                "\" />\n");
                        // Do not need to continue generating undo MoML
                        // as the deleteEntity takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                    }
                }
            } else if (elementName.equals("group")) {
                String groupName = (String)_attributes.get("name");
                if (groupName != null) {
                    // Defining a namespace.
                    _namespaces.push(_namespace);
                    _namespaceTranslations.push(_namespaceTranslationTable);
                    if (groupName.equals("auto")) {
                        _namespace = _AUTO_NAMESPACE;
                        _namespaceTranslationTable = new HashMap();
                    } else {
                        _namespace = groupName;
                    }
                } else {
                    _namespaces.push(_DEFAULT_NAMESPACE);
                    _namespaceTranslations.push(_namespaceTranslationTable);
                    _namespace = _DEFAULT_NAMESPACE;
                }

                // Handle the undo aspect.
                if (_undoEnabled && _undoContext.isUndoable()) {
                    // NOTE: for groups with namespaces, rely on the names
                    // already being part of undo MoML names instead of
                    // tracking the namespace prefix
                    _undoContext.appendUndoMoML("<group>\n");
                    // Need to continue undoing and use an end tag
                    _undoContext.appendClosingUndoMoML("</group>\n");
                    _undoContext.setChildrenUndoable(true);
                }

            } else if (elementName.equals("input")) {
                String source = (String)_attributes.get("source");
                _checkForNull(source, "No source for element \"input\"");

                boolean skip = false;
                if ( inputFileNamesToSkip != null) {
                    // If inputFileNamesToSkip contains a string
                    // that matches the end of source, then skip
                    // parsing the source file.  We use this for testing
                    // configurations that have optional parts like
                    // Matlab or javacomm.
                    Iterator inputFileNames = inputFileNamesToSkip.iterator();
                    while (inputFileNames.hasNext()) {
                        String inputFileName = (String)inputFileNames.next();
                        if (source.endsWith(inputFileName)) {
                            skip = true;
                            break;
                        }
                    }
                }

                if (!skip) {
                    // NOTE: The base attribute has been deprecated.  Ignore.

                    // Read external file in the current context, but with
                    // a new parser.
                    MoMLParser newParser =
                        new MoMLParser(_workspace, _classLoader);

                    newParser.setContext(_current);
                    newParser._propagating = _propagating;
                    _parse(newParser, _base, source);
                }
            } else if (elementName.equals("link")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"link\"");
                // Relation attribute now optional
                String relationName = (String)_attributes.get("relation");
                String insertAtSpec = (String)_attributes.get("insertAt");
                String insertInsideAtSpec =
                    (String)_attributes.get("insertInsideAt");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);

                int countArgs = 0;
                // Check that one of the required arguments is given
                if (insertAtSpec != null) {
                    countArgs++;
                }
                if (insertInsideAtSpec != null) {
                    countArgs++;
                }
                if (relationName != null) {
                    countArgs++;
                }
                if (countArgs == 0) {
                    throw new XmlException(
                            "Element link requires at least one of "
                            + "an insertAt, an insertInsideAt, or a relation.",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }
                if (insertAtSpec != null && insertInsideAtSpec != null) {
                    throw new XmlException(
                            "Element link requires at most one of "
                            + "insertAt and insertInsideAt, not both.",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }

                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                ComponentPort port = _getPort(portName, context);

                // Save to help generate undo MoML
                int origNumOutsideLinks = port.numLinks();
                int origNumInsideLinks = port.numInsideLinks();

                // Get relation if given
                ComponentRelation relation = null;
                if (relationName != null) {
                    Relation tmpRelation = context.getRelation(relationName);
                    _checkForNull(tmpRelation, "No relation named \"" +
                            relationName + "\" in " + context.getFullName());
                    relation = (ComponentRelation)tmpRelation;
                }

                // Get the index if given
                int insertAt = -1;
                if (insertAtSpec != null) {
                    insertAt = Integer.parseInt(insertAtSpec);
                }
                // Get the inside index if given
                int insertInsideAt = -1;
                if (insertInsideAtSpec != null) {
                    insertInsideAt = Integer.parseInt(insertInsideAtSpec);
                }

                if (insertAtSpec != null) {
                    port.insertLink(insertAt, relation);
                }
                else if (insertInsideAtSpec != null) {
                    port.insertInsideLink(insertInsideAt, relation);
                }
                else {
                    port.link(relation);
                }

                // Handle the undo aspect.
                if (_undoEnabled && _undoContext.isUndoable()) {
                    // NOTE: always unlink using an index
                    // NOTE: do not use a relation name as that unlinks
                    // all links to that relation from the given port
                    if (relation == null) {
                        // Handle null links insertion first. Either an
                        // insertAt or an insertInsideAt must have been used.
                        // NOTE: we need to check if the number of links
                        // actually changed as a null link beyond the index of
                        // the first real link has no effect
                        if (insertAt != -1) {
                            if (port.numLinks() != origNumOutsideLinks) {
                                _undoContext.appendUndoMoML("<unlink port=\"" +
                                        portName + "\" index=\"" +
                                        insertAtSpec + "\" />\n");
                            }
                        }
                        else {
                            if (port.numInsideLinks() != origNumInsideLinks) {
                                _undoContext.appendUndoMoML("<unlink port=\"" +
                                        portName + "\" insideIndex=\"" +
                                        insertInsideAtSpec + "\" />\n");
                            }
                        }
                    }
                    else {
                        // The relation name was given, see if the link was
                        // added inside or outside
                        if (port.numInsideLinks() != origNumInsideLinks) {
                            if (insertInsideAt == -1) {
                                insertInsideAt = port.numInsideLinks() - 1;
                            }
                            _undoContext.appendUndoMoML("<unlink port=\"" +
                                    portName + "\" insideIndex=\"" +
                                    insertInsideAt + "\" />\n");
                        }
                        else if (port.numLinks() != origNumOutsideLinks) {
                            if (insertAt == -1) {
                                insertAt = port.numLinks() - 1;
                            }
                            _undoContext.appendUndoMoML("<unlink port=\"" +
                                    portName + "\" index=\"" +
                                    insertAt + "\" />\n");
                        }
                        else {
                            // No change so do not need to generate any undo MoML
                        }
                    }
                }

                // If the container is cloned from something, then
                // add to it a MoML description of the new link, so that
                // this new link will be persistent.
                _recordLink(context, portName, relationName, insertAtSpec);

            } else if (elementName.equals("port")) {
                String className = (String)_attributes.get("class");
                String portName = (String)_attributes.get("name");
                _checkForNull(portName, "No name for element \"port\"");

                _checkClass(_current, Entity.class,
                        "Element \"port\" found inside an element that "
                        + "is not an Entity. It is: "
                        + _current);
                Entity container = (Entity)_current;

                Class newClass = null;
                if (className != null && !className.trim().equals("")) {
                    newClass = Class.forName(className, true, _classLoader);
                }
                Port port = container.getPort(portName);
                // Flag used to generate correct undo MoML
                boolean alreadyExisted = (port != null);
                if (port != null) {
                    if (newClass != null) {
                        // Previously existing port with the specified name.
                        _checkClass(port, newClass,
                                "port named \"" + portName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                } else {
                    // No previously existing port with this name.
                    if (newClass == null) {
                        // Classname is not given.  Invoke newPort() on the
                        // container.
                        port = container.newPort(portName);
                    } else {
                        // Classname is given.
                        Object[] arguments = new Object[2];
                        arguments[0] = container;
                        arguments[1] = portName;
                        port = (Port)_createInstance(newClass, arguments);
                    }
                }
                _pushContext();
                _current = port;
                _namespace = _DEFAULT_NAMESPACE;

                // Handle the undo aspect if needed
                if (_undoEnabled && _undoContext.isUndoable()) {
                    if (alreadyExisted) {
                        // Simply create in the undo MoML the same port
                        _undoContext.appendUndoMoML("<port name=\"" +
                                portName + "\" ");
                        // Also add in the class if given
                        if (className != null) {
                            _undoContext.appendUndoMoML("class=\"" +
                                    className + "\" ");
                        }
                        _undoContext.appendUndoMoML(">\n");

                        // Need to continue undoing and use an end tag
                        _undoContext.appendClosingUndoMoML("</port>\n");
                        _undoContext.setChildrenUndoable(true);
                    }
                    else {
                        // Need to delete the port in the undo MoML
                        _undoContext.appendUndoMoML("<deletePort name=\"" +
                                portName + "\" />\n");
                        // Do not need to continue generating undo MoML
                        // as the deletePort takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                    }
                }

                // NOTE: The direction attribute is deprecated, but
                // supported nonetheless.
                if (port instanceof IOPort) {
                    String direction = (String)_attributes.get("direction");
                    if (direction != null) {
                        IOPort ioport = (IOPort)port;
                        ioport.setOutput(direction.equals("output")
                                || direction.equals("both"));
                        ioport.setInput(direction.equals("input")
                                || direction.equals("both"));
                    }
                }

            } else if (elementName.equals("property")) {
                String className = (String)_attributes.get("class");
                String propertyName = (String)_attributes.get("name");
                _checkForNull(propertyName,
                        "No name for element \"property\"");
                String value = (String)_attributes.get("value");

                // First handle special properties that are not translated
                // into Ptolemy II attributes.
                // Note that we have to push something on to the
                // stack so that we can pop it off later.
                // An xml version of the FSM ABP demo tickled this bug
                boolean isIOPort = (_current instanceof IOPort);
                if (propertyName.equals("multiport") && isIOPort) {
                    // FIXME: UNDO: Consider refactoring these clauses
                    // to remove the duplicate values
                    // The previous value is needed to generate undo MoML
                    boolean previousValue = false;
                    // Special properties that affect the behaviour of a port
                    IOPort currentIOPort = (IOPort)_current;

                    // The mere presense of a named property "multiport"
                    // makes the enclosing port a multiport, unless it
                    // has value false.

                    // Get the previous value to use when generating the
                    // undo MoML
                    previousValue = currentIOPort.isMultiport();
                    if (value == null
                            || value.trim().toLowerCase().equals("true")) {
                        ((IOPort)_current).setMultiport(true);
                    } else if (value.trim().toLowerCase().equals("false")) {
                        ((IOPort)_current).setMultiport(false);
                    }
                    _pushContext();
                    _current =  (Attribute)
                        _current.getAttribute(propertyName);
                    _namespace = _DEFAULT_NAMESPACE;

                    // Handle undo
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        _undoContext.appendUndoMoML("<property name=\"" +
                                propertyName +
                                "\" value=\"");
                        // Use what was there before.
                        _undoContext.appendUndoMoML(previousValue + "\" >\n");
                        // Continue undoing and also use an end tag as a
                        // property can contain other properties
                        _undoContext.setChildrenUndoable(true);
                        _undoContext.appendClosingUndoMoML("</property>\n");
                    }
                } else if (propertyName.equals("output") && isIOPort) {
                    // FIXME: UNDO: Consider refactoring these clauses
                    // to remove the duplicate values
                    // The previous value is needed to generate undo MoML
                    boolean previousValue = false;
                    // Special properties that affect the behaviour of a port
                    IOPort currentIOPort = (IOPort)_current;

                    // Get the previous value to use when generating the
                    // undo MoML
                    previousValue = currentIOPort.isOutput();
                    if (value == null
                            || value.trim().toLowerCase().equals("true")) {
                        ((IOPort)_current).setOutput(true);
                    } else if (value.trim().toLowerCase().equals("false")) {
                        ((IOPort)_current).setOutput(false);
                    }
                    _pushContext();
                    _current =  (Attribute)
                        _current.getAttribute(propertyName);
                    _namespace = _DEFAULT_NAMESPACE;

                    // Handle undo
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        _undoContext.appendUndoMoML("<property name=\"" +
                                propertyName +
                                "\" value=\"");
                        // Use what was there before
                        _undoContext.appendUndoMoML(previousValue + "\" >\n");
                        // Continue undoing and also use an end tag as a
                        // property can contain other properties
                        _undoContext.setChildrenUndoable(true);
                        _undoContext.appendClosingUndoMoML("</property>\n");
                    }
                } else if (propertyName.equals("input") && isIOPort) {
                    // FIXME: UNDO: Consider refactoring these clauses
                    // to remove the duplicate values
                    // The previous value is needed to generate undo MoML
                    boolean previousValue = false;
                    // Special properties that affect the behaviour of a port
                    IOPort currentIOPort = (IOPort)_current;

                    // Get the previous value to use when generating the
                    // undo MoML
                    previousValue = currentIOPort.isInput();
                    if (value == null
                            || value.trim().toLowerCase().equals("true")) {
                        ((IOPort)_current).setInput(true);
                    } else if (value.trim().toLowerCase().equals("false")) {
                        ((IOPort)_current).setInput(false);
                    }
                    _pushContext();
                    _current =  (Attribute)
                        _current.getAttribute(propertyName);
                    _namespace = _DEFAULT_NAMESPACE;

                    // Handle undo
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        _undoContext.appendUndoMoML("<property name=\"" +
                                propertyName +
                                "\" value=\"");
                        // Use what was there before
                        _undoContext.appendUndoMoML(previousValue + "\" >\n");
                        // Continue undoing and also use an end tag as a
                        // property can contain other properties
                        _undoContext.setChildrenUndoable(true);
                        _undoContext.appendClosingUndoMoML("</property>\n");
                    }
                } else {
                    // Ordinary attribute.
                    NamedObj property = (Attribute)
                        _current.getAttribute(propertyName);
                    Class newClass = null;
                    if (className != null) {
                        try {
                            newClass =
                                Class.forName(className, true, _classLoader);
                        } catch (NoClassDefFoundError ex) {
                            throw new XmlException("Failed to find class '"
                                    + className + "'",
                                    _currentExternalEntity(),
                                    _parser.getLineNumber(),
                                    _parser.getColumnNumber(), ex);
                        } catch (SecurityException ex) {
                            // An applet might throw this.
                            throw new XmlException("Failed to find class '"
                                    + className + "'",
                                    _currentExternalEntity(),
                                    _parser.getLineNumber(),
                                    _parser.getColumnNumber(), ex);
                        }
                    }

                    // If there is a previous property with this name
                    // (property is not null), then we check that the
                    // property is an instance of the specified class.
                    // If it is, then we set the value of the property.
                    // Otherwise, we try to replace it, something that
                    // will only work if it is a singleton (it might throw
                    // NameDuplicationException).
                    boolean createdNew = false;

                    // Also need the previous value, if any, to generate undo
                    // MoML. Also need to know if the property already existed
                    boolean previouslyExisted = (property != null);
                    String oldClassName = null;
                    String oldValue = null;
                    if (previouslyExisted) {
                        oldClassName = property.getClass().getName();
                        if (property instanceof Settable) {
                            Settable settable = (Settable)property;
                            oldValue = settable.getExpression();
                        }
                    }
                    // NOTE: This used to use the following code to
                    // match _exactly_ the specified class, rather
                    // than checking that it be an instance of it.
                    // The new behavior is more flexible, because, for
                    // example, it allows actors to replace parameters
                    // with a derived class of Parameter.  This was done,
                    // for example, to replace some instances of Parameter
                    // with ParameterPort.
                    // if (property == null || (className != null &&
                    //      !property.getClass().getName().equals(className))) {

                    if (property == null || (newClass != null
                            && !newClass.isInstance(property))) {
                        // The following will result in a
                        // NameDuplicationException if there is a previous
                        // property and it is not a singleton.
                        try {
                            // No previously existing attribute with this name,
                            // or the class name of the previous entity doesn't
                            // match.
                            if (newClass == null) {
                                newClass = Attribute.class;
                            }

                            // Invoke the constructor.
                            Object[] arguments = new Object[2];
                            arguments[0] = _current;
                            arguments[1] = propertyName;
                            property = _createInstance(newClass, arguments);

                            if (value != null) {
                                if (property == null) {
                                    throw new XmlException(
                                            "Property does not exist: "
                                            + propertyName
                                            + "\n",
                                            _currentExternalEntity(),
                                            _parser.getLineNumber(),
                                            _parser.getColumnNumber());
                                }
                                if (!(property instanceof Settable)) {
                                    throw new XmlException(
                                            "Property cannot be assigned a value: "
                                            + propertyName
                                            + "\n",
                                            _currentExternalEntity(),
                                            _parser.getLineNumber(),
                                            _parser.getColumnNumber());
                                }
                                Settable settable = (Settable)property;
                                settable.setExpression(value);
                                _paramsToParse.add(property);
                            }
                            createdNew = true;
                        } catch (NameDuplicationException ex) {
                            // Ignore, so we can try to set the value.
                            // The createdNew variable will still be false.
                        }
                    }
                    if (!createdNew) {
                        // Previously existing property with this name,
                        // whose class name exactly matches, or the class
                        // name does not match, but a NameDuplicationException
                        // was thrown (meaning the attribute was not
                        // a singleton).

                        // If value is null and the property already
                        // exists, then there is nothing to do.
                        if (value != null) {
                            if (!(property instanceof Settable)) {
                                throw new XmlException("Property is not an "
                                        + "instance of Settable, "
                                        + "so can't set the value.",
                                        _currentExternalEntity(),
                                        _parser.getLineNumber(),
                                        _parser.getColumnNumber());
                            }
                            Settable settable = (Settable)property;
                            settable.setExpression(value);
                            _paramsToParse.add(property);
                        }
                    }
                    _pushContext();
                    _current = property;
                    _namespace = _DEFAULT_NAMESPACE;

                    // Handle the undo aspect if needed
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        if (!previouslyExisted) {
                            // Need to delete the property in the undo MoML
                            _undoContext.appendUndoMoML("<deleteProperty name=\"" +
                                    propertyName + "\" />\n");
                            // Do not need to continue generating undo MoML
                            // as the deleteProperty takes care of all
                            // contained MoML
                            _undoContext.setChildrenUndoable(false);
                        }
                        else {
                            // Simply generate the same as was there before
                            // FIXME: this may have subtle issues which may
                            // require the use of the "createdNew" variable
                            _undoContext.appendUndoMoML("<property name=\"" +
                                    property.getName() + "\" ");
                            _undoContext.appendUndoMoML("class=\"" +
                                    oldClassName + "\" ");
                            if (oldValue != null) {

                                // Escape the value for xml so that if
                                // the property the user typed in was
                                // a Parameter "foo", we do not have
                                // problems.  To replicate this,
                                // create a Const with a value "foo"
                                // and then change it to 2 and then
                                // try undo.

                                _undoContext.appendUndoMoML("value=\"" +
                                        StringUtilities.escapeForXML(oldValue)
                                        + "\" ");
                            }
                            _undoContext.appendUndoMoML(">\n");
                            // Add the closing element
                            _undoContext.appendClosingUndoMoML("</property>\n");

                            // Need to continue generating undo MoML as a
                            // property can have other children
                            _undoContext.setChildrenUndoable(true);
                        }
                    }
                }

            } else if (elementName.equals("relation")) {
                String className = (String)_attributes.get("class");
                String relationName = (String)_attributes.get("name");
                _checkForNull(relationName, "No name for element \"relation\"");
                _checkClass(_current, CompositeEntity.class,
                        "Element \"relation\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);
                CompositeEntity container = (CompositeEntity)_current;
                Class newClass = null;
                if (className != null) {
                    newClass = Class.forName(className, true, _classLoader);
                }
                Relation relation = container.getRelation(relationName);
                // Flag used to generate correct undo MoML
                boolean alreadyExisted = (relation != null);
                if (relation == null) {
                    // No previous relation with this name.
                    NamedObj newRelation = null;
                    _pushContext();
                    if (newClass == null) {
                        // No classname. Use the newRelation() method.
                        newRelation = container.newRelation(relationName);
                    } else {
                        Object[] arguments = new Object[2];
                        arguments[0] = (CompositeEntity)_current;
                        arguments[1] = relationName;
                        newRelation = _createInstance(newClass, arguments);
                    }
                    _namespace = _DEFAULT_NAMESPACE;
                    _current = newRelation;

                } else {
                    // Previously existing relation with the specified name.
                    if (newClass != null) {
                        _checkClass(relation, newClass,
                                "relation named \"" + relationName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                    _pushContext();
                    _current = relation;
                    _namespace = _DEFAULT_NAMESPACE;
                }

                // Handle the undo aspect if needed
                if (_undoEnabled && _undoContext.isUndoable()) {
                    if (alreadyExisted) {
                        // Simply create in the undo MoML the same relation
                        _undoContext.appendUndoMoML("<relation name=\"" +
                                relationName + "\" ");
                        // Also add in the class if given
                        if (className != null) {
                            _undoContext.appendUndoMoML("class=\"" +
                                    className + "\" ");
                        }
                        _undoContext.appendUndoMoML(">\n");

                        // Need to continue undoing and use an end tag
                        _undoContext.appendClosingUndoMoML("</relation>\n");
                        _undoContext.setChildrenUndoable(true);
                    }
                    else {
                        // Need to delete the realtion in the undo MoML
                        _undoContext.appendUndoMoML("<deleteRelation name=\"" +
                                relationName + "\" />\n");
                        // Do not need to continue generating undo MoML
                        // as the deleteRelation takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                    }
                }

            } else if (elementName.equals("rename")) {
                String newName = (String)_attributes.get("name");
                _checkForNull(newName, "No new name for element \"rename\"");
                if (_current != null) {
                    String oldName = _current.getName();
                    _current.setName(newName);

                    // Handle the undo aspect if needed
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        // First try and rename in the parent context.
                        // NOTE: this is a bit of a hack but is the only way
                        // I could see of doing the rename without having to
                        // change the semantics or location of the rename
                        // element
                        try {
                            UndoContext parentContext =
                                (UndoContext)_undoContexts.peek();
                            parentContext.applyRename(newName);
                            // Simply create in the undo MoML another rename
                            _undoContext.appendUndoMoML("<rename name=\"" +
                                    oldName + "\" />\n");
                            // Do not need to continue generating undo MoML
                            // as rename does not have any child elements
                            _undoContext.setChildrenUndoable(false);
                        }
                        catch (Exception e) {
                            // FIXME: update to use a better exception
                            // Do nothing, should mean that no undo for this
                            // element is generated
                        }
                    }
                }

            } else if (elementName.equals("rendition")) {
                // NOTE: The rendition element is deprecated.
                // Use an icon property instead.
                // This ignores everything inside it.
                _skipRendition = true;

            } else if (elementName.equals("unlink")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"unlink\"");
                String relationName = (String)_attributes.get("relation");
                String indexSpec = (String)_attributes.get("index");
                String insideIndexSpec =
                    (String)_attributes.get("insideIndex");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"unlink\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);

                int countArgs = 0;
                // Check that one of the required arguments is given
                if (indexSpec != null) {
                    countArgs++;
                }
                if (insideIndexSpec != null) {
                    countArgs++;
                }
                if (relationName != null) {
                    countArgs++;
                }
                if (countArgs != 1) {
                    throw new XmlException(
                            "Element unlink requires exactly one of "
                            + "an index, an insideIndex, or a relation.",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }

                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                ComponentPort port = _getPort(portName, context);

                // Get relation if given
                if (relationName != null) {
                    Relation tmpRelation = context.getRelation(relationName);
                    _checkForNull(tmpRelation, "No relation named \"" +
                            relationName + "\" in " + context.getFullName());
                    ComponentRelation relation = (ComponentRelation)tmpRelation;

                    // Handle the undoable aspect
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        // Get the relation at the given index
                        List linkedRelations = port.linkedRelationList();
                        int index = linkedRelations.indexOf(tmpRelation);
                        if (index != -1) {
                            // Linked on the outside...
                            _undoContext.appendUndoMoML("<link port=\"" +
                                    portName + "\" insertAt=\"" +
                                    index + "\" relation=\"" +
                                    relationName + "\" />\n");
                        } else {
                            List insideLinkedRelations =
                                port.insideRelationList();
                            index = insideLinkedRelations.indexOf(tmpRelation);

                            // Linked on the inside.
                            _undoContext.appendUndoMoML("<link port=\"" +
                                    portName + "\" insertInsideAt=\"" +
                                    index + "\" relation=\"" +
                                    relationName + "\" />\n");
                        }
                    }
                    port.unlink(relation);
                } else if (indexSpec != null) {
                    // index is given.
                    int index = Integer.parseInt(indexSpec);

                    // Handle the undoable aspect  before doing the unlinking
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        // Get the relation at the given index
                        List linkedRelations = port.linkedRelationList();
                        Relation r = (Relation)linkedRelations.get(index);
                        // FIXME: need to worry about vertex?
                        _undoContext.appendUndoMoML("<link port=\"" +
                                portName + "\" insertAt=\"" +
                                indexSpec + "\" ");
                        // Only need to specify the relation if there was
                        // a relation at that index. Otherwise a null
                        // link is inserted
                        if (r != null) {
                            _undoContext.appendUndoMoML("relation=\"" +
                                    r.getName(context) + "\" ");
                        }
                        _undoContext.appendUndoMoML(" />\n");
                    }
                    port.unlink(index);
                } else {
                    // insideIndex is given.
                    int index = Integer.parseInt(insideIndexSpec);
                    // Handle the undoable aspect  before doing the unlinking
                    if (_undoEnabled && _undoContext.isUndoable()) {
                        // Get the relation at the given index
                        List linkedRelations = port.insideRelationList();
                        Relation r = (Relation)linkedRelations.get(index);
                        // FIXME: need to worry about vertex?
                        _undoContext.appendUndoMoML("<link port=\"" +
                                portName + "\" insertInsideAt=\"" +
                                index + "\" ");
                        // Only need to specify the relation if there was
                        // a relation at that index. Otherwise a null
                        // link is inserted
                        if (r != null) {
                            _undoContext.appendUndoMoML("relation=\"" +
                                    r.getName(context) + "\" ");
                        }
                        _undoContext.appendUndoMoML(" />\n");
                    }
                    port.unlinkInside(index);

                }

                // Do not need to worry about child elements
                if (_undoEnabled && _undoContext.isUndoable()) {
                    _undoContext.setChildrenUndoable(false);
                }

                // If the container is cloned from something, then
                // add to it a MoML description of the unlink, so that
                // this new entity will be persistent.
                _recordUnlink(context, portName, relationName,
                        indexSpec, insideIndexSpec);

            } else if (elementName.equals("vertex")) {
                String vertexName = (String)_attributes.get("name");
                _checkForNull(vertexName, "No name for element \"vertex\"");

                _checkClass(_current, Relation.class,
                        "Element \"vertex\" found inside an element that "
                        + "is not a Relation. It is: "
                        + _current);

                // For undo need to know if a previous vertex attribute
                // with this name existed, and if so its expression
                Vertex previous =
                    (Vertex)_current.getAttribute(vertexName);
                String previousValue = null;
                if (previous != null) {
                    previousValue = previous.getExpression();
                }

                // Note that vertexes are settable, but they are handled
                // separately.  This is probably the wrong way to do this.
                // Create an instance of Vertex and attach it to the Relation.
                Vertex vertex = new Vertex((Relation)_current, vertexName);

                // Deal with setting the location.
                String value = (String)_attributes.get("value");
                // If value is null and the property already
                // exists, then there is nothing to do.
                if (value != null) {
                    vertex.setExpression(value);
                    _paramsToParse.add(vertex);
                }

                _pushContext();
                _current = vertex;
                _namespace = _DEFAULT_NAMESPACE;

                if (_undoEnabled && _undoContext.isUndoable()) {
                    _undoContext.appendUndoMoML("<vertex name=\"" +
                            vertexName + "\" ");
                    if (previousValue != null) {
                        _undoContext.appendUndoMoML("value=\""  +
                                previousValue + "\" ");
                    }
                    _undoContext.appendUndoMoML(">\n");

                    // The Vertex element can have children
                    _undoContext.setChildrenUndoable(true);
                    _undoContext.appendClosingUndoMoML("</vertex>\n");
                }
            } else {
                // Unrecognized element name.  Collect it.
                if (_unrecognized == null) {
                    _unrecognized = new LinkedList();
                }
                _unrecognized.add(elementName);
            }
        } catch (InvocationTargetException ex) {
            // A constructor or method invoked via reflection has
            // triggered an exception.
            if (_handler != null) {
                int reply = _handler.handleError(
                        _getCurrentElement(elementName), _current,
                        ex.getTargetException());
                if (reply == ErrorHandler.CONTINUE) {
                    _attributes.clear();
                    _attributeNameList.clear();
                    _skipElement = 1;
                    _skipElementName = elementName;
                    return;
                } else if (reply == ErrorHandler.CANCEL) {
                    // NOTE: Since we have to throw an XmlException for
                    // the exception to be properly handled, we communicate
                    // that it is a user cancellation with the special
                    // string pattern "*** Canceled." in the message.
                    throw new XmlException(
                            "*** Canceled.",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }
            }
            // No handler.
            throw new XmlException(
                    "XML element \"" + elementName
                    + "\" triggers exception:\n  "
                    + ex.getTargetException(),
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber(), ex.getTargetException());

        } catch (Exception ex) {
            if (_handler != null) {
                int reply = _handler.handleError(
                        _getCurrentElement(elementName), _current, ex);
                if (reply == ErrorHandler.CONTINUE) {
                    _attributes.clear();
                    _attributeNameList.clear();
                    _skipElement = 1;
                    _skipElementName = elementName;
                    return;
                } else if (reply == ErrorHandler.CANCEL) {
                    // NOTE: Since we have to throw an XmlException for
                    // the exception to be properly handled, we communicate
                    // that it is a user cancellation with the special
                    // string pattern "*** Canceled." in the message.
                    throw new XmlException(
                            "*** Canceled.",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }
            }
            if (ex instanceof XmlException) {
                throw (XmlException)ex;
            } else {
                throw new XmlException(
                        "XML element \"" + elementName
                        + "\" triggers exception.",
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber(), ex);
            }
        }
        _attributes.clear();
        _attributeNameList.clear();
    }

    /** Handle the start of an external entity.  This pushes the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemID The URI for the external entity.
     */
    public void startExternalEntity(String systemID) {
        // NOTE: The Microstar XML parser incorrectly passes the
        // HTML file for the first external entity, rather than
        // XML file.  So error messages typically refer to the wrong file.
        _externalEntities.push(systemID);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The standard MoML DTD, represented as a string.  This is used
     *  to parse MoML data when a compatible PUBLIC DTD is specified.
     *  NOTE: This DTD includes a number of elements that are deprecated.
     *  They are included here for backward compatibility.  See the MoML
     *  chapter of the Ptolemy II design document for a view of the
     *  current (nondeprecated) DTD.
     */
    public static String MoML_DTD_1 = "<!ELEMENT model (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | property | relation | rename | rendition | unlink)*><!ATTLIST model name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT class (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | port | property | relation | rename | rendition | unlink)*><!ATTLIST class name CDATA #REQUIRED extends CDATA #IMPLIED source CDATA #IMPLIED><!ELEMENT configure (#PCDATA)><!ATTLIST configure source CDATA #IMPLIED><!ELEMENT deleteEntity EMPTY><!ATTLIST deleteEntity name CDATA #REQUIRED><!ELEMENT deletePort EMPTY><!ATTLIST deletePort name CDATA #REQUIRED><!ELEMENT deleteProperty EMPTY><!ATTLIST deleteProperty name CDATA #REQUIRED><!ELEMENT deleteRelation EMPTY><!ATTLIST deleteRelation name CDATA #REQUIRED><!ELEMENT director (configure | doc | property)*><!ATTLIST director name CDATA \"director\" class CDATA #REQUIRED><!ELEMENT doc (#PCDATA)><!ATTLIST doc name CDATA \"_doc\"><!ELEMENT entity (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | port | property | relation | rename | rendition | unlink)*><!ATTLIST entity name CDATA #REQUIRED class CDATA #IMPLIED source CDATA #IMPLIED><!ELEMENT group ANY><!ATTLIST group name CDATA #IMPLIED><!ELEMENT import EMPTY><!ATTLIST import source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT input EMPTY><!ATTLIST input source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT link EMPTY><!ATTLIST link insertAt CDATA #IMPLIED insertInsideAt CDATA #IMPLIED port CDATA #REQUIRED relation CDATA #IMPLIED vertex CDATA #IMPLIED><!ELEMENT location EMPTY><!ATTLIST location value CDATA #REQUIRED><!ELEMENT port (configure | doc | property | rename)*><!ATTLIST port class CDATA #IMPLIED name CDATA #REQUIRED><!ELEMENT property (configure | doc | property | rename)*><!ATTLIST property class CDATA #IMPLIED name CDATA #REQUIRED value CDATA #IMPLIED><!ELEMENT relation (configure | doc | property | rename | vertex)*><!ATTLIST relation name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT rename EMPTY><!ATTLIST rename name CDATA #REQUIRED><!ELEMENT rendition (configure | location | property)*><!ATTLIST rendition class CDATA #REQUIRED><!ELEMENT unlink EMPTY><!ATTLIST unlink index CDATA #IMPLIED insideIndex CDATA #IMPLIED port CDATA #REQUIRED relation CDATA #IMPLIED><!ELEMENT vertex (configure | doc | location | property | rename)*><!ATTLIST vertex name CDATA #REQUIRED pathTo CDATA #IMPLIED value CDATA #IMPLIED>";

    // NOTE: The master file for the above DTD is at
    // $PTII/ptolemy/moml/MoML_1.dtd.  If modified, it needs to be also
    // updated at ptweb/xml/dtd/MoML_1.dtd.

    /** The public ID for version 1 MoML. */
    public static String MoML_PUBLIC_ID_1 = "-//UC Berkeley//DTD MoML 1//EN";

    /** List of Strings that name files to be skipped.
     *  This variable is used primarily for testing configurations.
     *  The value of this variable is a List of Strings, where each
     *  element names a file name that should _not_ be loaded if
     *  it is encounted in an input statement.
     */
    public static List inputFileNamesToSkip = null;

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
    ////                         package friendly variables        ////

    // Indicator that the MoML currently being evaluated is the result
    // of propagating a change from a master to something that was cloned
    // from the master.  This is set by MoMLChangeRequest only.
    boolean _propagating = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // If the first argument is not an instance of the second,
    // throw an exception with the given message.
    private void _checkClass(Object object, Class correctClass, String msg)
            throws XmlException {
        if (!correctClass.isInstance(object)) {
            throw new XmlException(msg,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
    }

    // If the argument is null, throw an exception with the given message.
    private void _checkForNull(Object object, String message)
            throws XmlException {
        if (object == null) {
            throw new XmlException(message,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
    }

    // Create a new entity from the specified class name, give
    // it the specified entity name, and specify that its container
    // is the current container object.  If the current container
    // already contains an entity with the specified name and class,
    // then return that entity.  If the class name matches
    // a class that has been previously defined (by an absolute
    // or relative name), then that class is cloned.  Otherwise,
    // the class name is interpreted as a Java class name and we
    // attempt to construct the entity.  If instantiating a Java
    // class doesn't work, then we look for a MoML file on the
    // classpath that defines a class by this name.  The file
    // is assumed to be named "foo.xml", where "foo" is the name
    // of the class.  Moreover, the classname is assumed to have
    // no periods (since a MoML name does not allow periods,
    // this is reasonable). If _current is not an instance
    // of CompositeEntity, then an XML exception is thrown.
    // The third argument, if non-null, gives a URL to import
    // to create a reference class from which to instantiate this
    // entity.
    private NamedObj _createEntity(
            String className, String entityName, String source)
            throws Exception {

        if (_current != null && !(_current instanceof CompositeEntity)) {
            throw new XmlException("Cannot create an entity inside "
                    + "of another that is not a CompositeEntity "
                    + "(Container is '" + _current + "').",
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        CompositeEntity container = (CompositeEntity)_current;
        ComponentEntity previous = _searchForEntity(entityName);
        Class newClass = null;
        ComponentEntity reference = null;
        if (className != null) {
            // A class name is given.
            reference = searchForClass(className, source);
            if (reference == null) {
                // No previously defined class with this name.
                // First attempt to instantiate a Java class.

                // If we throw an error or exception be sure to save the
                // original error message before we go off and try to fix the
                // error.  Sometimes, the original error message is the true
                // cause of the problem, and we should always provide the user
                // with the cause of the original error in the unlikely event
                // that our error correction fails
                try {
                    newClass = Class.forName(className, true, _classLoader);
                } catch (Exception ex) {
                    // NOTE: Java sometimes throws ClassNotFoundException
                    // and sometimes NullPointerException when the class
                    // does not exist.  Hence the broad catch here.
                    try {
                        reference = _attemptToFindMoMLClass(className, source);
                    } catch (Exception ex2) {
                        // If we are running inside an applet, then
                        // we may end up getting a SecurityException,
                        // so we want to be sure to not throw away ex2
                        throw new IllegalActionException(null, ex2,
                                "Cannot find class: "
                                + className);
                    }
                } catch (Error error) {
                    // Java might throw a ClassFormatError, but
                    // we usually get and XmlException
                    // NOTE: The following error message is for
                    // the programmer, not for the user. EAL
                    StringBuffer errorMessage = new StringBuffer();

                    if (error instanceof ExceptionInInitializerError) {
                        // Running a Python applet may cause
                        // an ExceptionInInitializerError
                        
                        // There was a problem in the initializer, but
                        // we can get the original exception that was
                        // thrown.
                        Throwable staticThrowable =
                            ((ExceptionInInitializerError)error)
                            .getCause();

                        // I think we should report the cause and a stack
                        // trace for all the exceptions thrown here,
                        // but it sure makes the output ugly. 
                        // Instead, I just debug from here -cxh
                        errorMessage.append("ExceptionInInitializerError: "
                                + "Caused by:\n "
                                + KernelException.stackTraceToString(
                                        staticThrowable));
                    } else {
                        // If there is a class format error in the
                        // code generator, then we may end up obscuring
                        // that error, requiring debugging here.

                        // We use error.toString() here instead of
                        // error.getMessage() so that the name of the
                        // actual class that caused the error is reported.
                        // This is critical if the problem is a class not
                        // found error.  If we use error.getMessage()
                        // and try to open up
                        // actor/lib/comm/demo/SerialPort/SerialPort.xml
                        // when the Java Serial Comm API is not installed,
                        // we get
                        
                        // Error encounted in:
                        // <entity name="SerialComm" class="ptolemy.actor.lib...
                        // -- ptolemy.actor.lib.comm.SerialComm:
                        // javax/comm/SerialPortEventListener
                        // ptolemy.actor.lib.comm.SerialComm: XmlException:
                        // Could not find 'ptolemy/actor/lib/comm/SerialComm.xml'..

                        // If we use toString(), we get:
                        // Error encounted in:
                        // <entity name="SerialComm" class="ptolemy.actor.lib..
                        // -- ptolemy.actor.lib.comm.SerialComm:
                        // java.lang.NoClassDefFoundError: javax/comm/SerialPortEventListener
                        // ptolemy.actor.lib.comm.SerialComm: XmlException:
                        // Could not find 'ptolemy/actor/lib/comm/SerialComm.xml'..
                        
                        // It is critical that the error include the
                        // NoClassDefFoundError string -cxh

                        errorMessage.append(className + ": \n "
                                + error.toString() + "\n");
                    }

                    try {
                        reference = _attemptToFindMoMLClass(className, source);
                    } catch (XmlException ex2) {
                        throw new Exception(
                                "-- "
                                + errorMessage.toString()
                                + className
                                + ": XmlException:\n"
                                + ex2.getMessage());
                    } catch (ClassFormatError ex3) {
                        throw new Exception(
                                "-- :"
                                + errorMessage.toString()
                                + className
                                + ": ClassFormatError: "
                                + "found invalid Java class file.\n"
                                + ex3.getMessage());
                    } catch (Exception ex4) {
                        throw new Exception(
                                "-- "
                                + errorMessage.toString()
                                + className
                                + ": Exception:\n"
                                + ex4.getMessage());
                    }
                }
            }
        }
        if (previous != null) {
            if (newClass != null) {
                _checkClass(previous, newClass,
                        "entity named \"" + entityName
                        + "\" exists and is not an instance of "
                        + className);
            }
            return previous;
        }

        // No previous entity.  Class name is required.
        _checkForNull(className, "Cannot create entity without a class name.");

        // Next check to see whether the class extends a named entity.
        if (reference == null) {
            // Not a named entity. Instantiate a Java class.
            if (_current != null) {
                // Not a top-level entity.
                _checkClass(_current, CompositeEntity.class,
                        "Cannot create an entity inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);
                Object[] arguments = new Object[2];

                arguments[0] = _current;
                arguments[1] = entityName;
                NamedObj newEntity = _createInstance(newClass, arguments);
                return newEntity;
            } else {
                // Top-level entity.  Instantiate in the workspace.
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                NamedObj result = _createInstance(newClass, arguments);
                result.setName(entityName);
                return result;
            }
        } else {
            // Extending a previously defined entity.  Check to see that
            // it was defined to be a class.
            if (!reference.getMoMLInfo().elementName.equals("class")) {
                throw new XmlException("Attempt to extend an entity that "
                        + "is not a class: " + reference.getFullName(),
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }

            // Clone it into the workspace of the container, if there is one,
            // or the workspace of the reference if not.
            ComponentEntity newEntity = null;
            if (container == null) {
                newEntity = (ComponentEntity)
                    reference.clone(reference.workspace());
            } else {
                newEntity = (ComponentEntity)
                    reference.clone(container.workspace());
            }

            // Set up the new object to defer its MoML definition
            // to the original class.
            newEntity.setDeferMoMLDefinitionTo(reference);
            
            // Mark contents as being class elements.  EAL 12/03
            _markContentsClassElements(newEntity);

            // Set the name of the clone.
            // NOTE: The container is null, so there will be no
            // name conflict here.  If we were to set the name after
            // setting the container, we could get a spurious name conflict
            // when we set the container.
            newEntity.setName(entityName);

            // Set the class name as specified in this method call.
            // This overrides what NamedObj does.  The reason we want to
            // do that is that NamedObj uses the full name of the object
            // that we cloned as the classname.  But this may not provide
            // enough information to instantiate the class.
            newEntity.getMoMLInfo().className = className;

            // Set the container of the clone.
            newEntity.setContainer(container);

            // The master may have an entity name "class" (or "model"?), and
            // that name will be cloned, so we need to change this.
            // It may get changed back if we are inside a "class" element.
            newEntity.getMoMLInfo().elementName = "entity";

            return newEntity;
        }
    }

    // Attempt to find a MoML class.
    // If there is no source defined, then use the classpath.
    private ComponentEntity _attemptToFindMoMLClass(
            String className, String source) throws Exception {
        String classAsFile = null;
        String altClassAsFile = null;
        ComponentEntity reference = null;
        if (source == null) {
            // No source defined.  Use the classpath.

            // First, replace all periods in the class name
            // with slashes, and then append a ".xml".
            // NOTE: This should perhaps be handled by the
            // class loader, but it seems rather complicated to
            // do that.
            // Search for the .xml file before searching for the .moml
            // file.  .moml files are obsolete, and we should probably
            // not bother searching for them at all.
            classAsFile = className.replace('.', '/') + ".xml";
            // RIM uses .moml files, so leave them in.
            altClassAsFile = className.replace('.', '/') + ".moml";
        } else {
            // Source is given.
            classAsFile = source;
        }
        // Read external model definition in a new parser,
        // rather than in the current context.
        MoMLParser newParser = new MoMLParser(_workspace, _classLoader);

        NamedObj candidateReference = null;
        try {
            candidateReference =
                _parse(newParser, _base, classAsFile);
        } catch (Exception ex2) {
            // Try the alternate file, if it's not null.
            if (altClassAsFile != null) {
                try {
                    candidateReference =
                        _parse(newParser, _base, altClassAsFile);
                    classAsFile = altClassAsFile;
                } catch (Exception ex3) {
                    // Cannot find class definition.
                    // Unfortunately exception chaining does not work here
                    // since we really want to know what ex2 and ex3
                    // both were.
                    throw new XmlException("Could not find '"
                            + classAsFile + "' or '"
                            + altClassAsFile + "' using base '"
                            + _base + "': " ,
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber(), ex2);
                }
            } else {
                // No alternative. Rethrow exception.
                throw ex2;
            }
        }
        if (candidateReference instanceof ComponentEntity) {
            reference = (ComponentEntity)candidateReference;
        } else {
            throw new XmlException(
                    "File "
                    + classAsFile
                    + " does not define a ComponentEntity.",
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        // Check that the classname matches the name of the
        // reference.
        String referenceName = reference.getName();
        if (!className.equals(referenceName)
                && !className.endsWith("." + referenceName)) {
            throw new XmlException(
                    "File "
                    + classAsFile
                    + " does not define a class named "
                    + className,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        // Set the classname and source of the import.
        // NOTE: We have gone back and forth on whether this
        // is right, because previously, the className field
        // was overloaded to contain the class name in some
        // circumstances, and the base class name in others.
        // We now have a field for the base class, so this can
        // be done again.  Moreover, it is necessary, or
        // further instances of this class will not be cloned
        // from this same reference (which breaks look inside).
        reference.getMoMLInfo().className = className;

        // NOTE: This might be a relative file reference, which
        // won't be of much use if a MoML file is moved.
        reference.getMoMLInfo().source = source;

        // Record the import to avoid repeated reading
        if (_imports == null) {
            _imports = new LinkedList();
        }
        _imports.add(0, reference);
        return reference;
    }

    // Create an instance of the specified class name by finding a
    // constructor that matches the specified arguments.  The specified
    // class must be NamedObj or derived, or a ClassCastException will
    // be thrown.  NOTE: This mechanism does not support instantiation
    // of inner classes, since those take an additional argument (the
    // first argument), which is the enclosing class. Static inner
    // classes, however, work fine.
    // This method marks the contents of what it creates as class elements,
    // since they are defined in the Java code of the constructor.
    // @param newClass The class.
    // @param arguments The constructor arguments.
    // @exception Exception If no matching constructor is found, or if
    //  invoking the constructor triggers an exception.
    private NamedObj _createInstance(Class newClass, Object[] arguments)
            throws Exception {
        Constructor[] constructors = newClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != arguments.length) continue;
            boolean match = true;
            for (int j = 0; j < parameterTypes.length; j++) {
                if (!(parameterTypes[j].isInstance(arguments[j]))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                NamedObj newEntity = (NamedObj)constructor.newInstance(arguments);
                // Mark the contents of the new entity as being class elements.
                _markContentsClassElements(newEntity);
                return newEntity;
            }
        }
        // If we get here, then there is no matching constructor.

        // Generate a StringBuffer containing what we were looking for.
        StringBuffer argumentBuffer = new StringBuffer();
        for (int i = 0; i < arguments.length; i++) {
            argumentBuffer.append(arguments[i].getClass() + " = \""
                    + arguments[i].toString() + "\"" );
            if (i < arguments.length - 1) {
                argumentBuffer.append(", ");
            }
        }

        throw new XmlException("Cannot find a suitable constructor ("
                + arguments.length + " args) ("
                + argumentBuffer + ") for '"
                + newClass.getName()
                + "'",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
    }

    // Delete the entity after verifying that it is contained (deeply)
    // by the current environment.
    private NamedObj _deleteEntity(String entityName) throws Exception {
        ComponentEntity toDelete = _searchForEntity(entityName);
        if (toDelete == null) {
            throw new XmlException("No such entity to delete: " + entityName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }

        // NOTE: not enough to simply record the MoML of the deleted entity
        // as any links connected to its ports will also be deleted.
        if (_undoEnabled && _undoContext.isUndoable()) {
            _undoContext.appendUndoMoML("<group>\n");
            // May need to move context if the entity name given is not
            // immediate
            _undoContext.moveContextStart(_current, toDelete);

            // Add in the description
            _undoContext.appendUndoMoML(toDelete.exportMoML());

            // NOTE: cannot use the relationlist as returned as it is
            // unmodifiable and we need to add in the entity being deleted.
            ArrayList filter = new ArrayList(toDelete.linkedRelationList());
            filter.add(toDelete);

            // The parent container can do the filtering and generate the
            // MoML
            CompositeEntity container = (CompositeEntity)toDelete.getContainer();
            String replicaLinkMoML = container.exportLinks(0, filter);
            _undoContext.appendUndoMoML(replicaLinkMoML);

            // Finally move back to context if needed
            _undoContext.moveContextEnd(_current, toDelete);
            _undoContext.appendUndoMoML("</group>\n");

        }

        toDelete.setContainer(null);

        return toDelete;
    }

    // Delete the port after verifying that it is contained (deeply)
    // by the current environment.
    private Port _deletePort(String portName, String entityName)
            throws Exception {
        // NOTE: if the entity attribute is not used, then the
        // deletion of any links associated with this port will
        // not be undoable.
        // If the entity attribute is used, then the port name must be
        // immediate i.e. not contain a period.
        Port toDelete = null;
        Entity portContainer = null;
        boolean entityAttrUsed = false;
        if (entityName == null) {
            toDelete = _searchForPort(portName);
            entityAttrUsed = false;
        }
        else {
            entityAttrUsed = true;
            portContainer = _searchForEntity(entityName);
            if (portContainer == null) {
                throw new XmlException("No such entity (" + entityName +
                        ") to delete the port on: " + portName,
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }
            if (portName.indexOf(".") != -1) {
                throw new XmlException("Invalid port name: " + portName +
                        ", must be immediately contained if the entity " +
                        "attribute is used",
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }
            toDelete = portContainer.getPort(portName);
        }
        if (toDelete == null) {
            throw new XmlException("No such port to delete: "
                    + portName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }

        // NOTE: not enough to simply record the MoML of the deleted port
        // as any links connected to it will also be deleted.
        if (_undoEnabled && _undoContext.isUndoable()) {
            _undoContext.appendUndoMoML("<group>\n");
            // Need to work out the entity context to use if any
            if (!entityAttrUsed) {
                _undoContext.moveContextStart(_current, toDelete);
            }
            else {
                _undoContext.moveContextStart(_current, portContainer);
            }

            // Move to the entity context if the entity attribute was used.
            // NOTE: only the local name is used.
            if (entityAttrUsed) {
                _undoContext.appendUndoMoML("<entity name=\"" +
                        portContainer.getName() + "\" >\n");
            }

            // Add in the MoML description of the port
            _undoContext.appendUndoMoML(toDelete.exportMoML());

            // NOTE: only generate undo MoML for deleted links if the entity
            // attribute is used
            if (entityAttrUsed) {
                // First need to generate replicaLinkMoML for the internal
                // links if the port is a component port, then close of the
                // local entity MoML, then generate replicaLinkMoML for
                // the outside links.
                if (portContainer instanceof CompositeEntity) {
                    // Get the containing compositeEntity as this is
                    // needed for the generating the link MoML and
                    // level crossing links below
                    CompositeEntity composite = (CompositeEntity)portContainer;
                    ComponentPort compPort = (ComponentPort)toDelete;
                    List insideRelationsList = compPort.insideRelationList();
                    ArrayList filter = new ArrayList(insideRelationsList);
                    filter.add(compPort);
                    String replicaLinkMoML = composite.exportLinks(0, filter);
                    _undoContext.appendUndoMoML(replicaLinkMoML);
                }

                // Close off the local entity
                _undoContext.appendUndoMoML("</entity>\n");

                // Add in the replica links for outside
                List relationsList = toDelete.linkedRelationList();
                ArrayList filter = new ArrayList(relationsList);
                filter.add(toDelete);
                // Get the containing compositeEntity as this is needed for the
                // generating the link MoML and level crossing links below
                CompositeEntity composite =
                    (CompositeEntity)portContainer.getContainer();
                String replicaLinkMoML = composite.exportLinks(0, filter);
                _undoContext.appendUndoMoML(replicaLinkMoML);
            }


            // Finally move back to context if needed
            if (!entityAttrUsed) {
                _undoContext.moveContextEnd(_current, toDelete);
            }
            else {
                _undoContext.moveContextEnd(_current, portContainer);
            }
            _undoContext.appendUndoMoML("</group>\n");
        }
        toDelete.setContainer(null);

        return toDelete;
    }

    // Delete the property after verifying that it is contained (deeply)
    // by the current environment.
    private Attribute _deleteProperty(String propName) throws Exception {
        Attribute toDelete = _searchForAttribute(propName);
        if (toDelete == null) {
            throw new XmlException("No such property to delete: "
                    + propName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        toDelete.setContainer(null);

        return toDelete;
    }

    // Delete the relation after verifying that it is contained (deeply)
    // by the current environment.
    private Relation _deleteRelation(String relationName) throws Exception {
        ComponentRelation toDelete = _searchForRelation(relationName);
        if (toDelete == null) {
            throw new XmlException("No such relation to delete: "
                    + relationName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        // NOTE: not enough to simply record the MoML of the deleted entity
        // as any links connected to its ports will also be deleted.
        if (_undoEnabled && _undoContext.isUndoable()) {
            _undoContext.appendUndoMoML("<group>\n");
            // May need to move context if the entity name given is not
            // immediate
            _undoContext.moveContextStart(_current, toDelete);

            // Add in the relation description
            _undoContext.appendUndoMoML(toDelete.exportMoML());

            // NOTE: cannot use the relationlist as returned as it is
            // unmodifiable and we need to add in the entity being deleted.
            ArrayList filter = new ArrayList(toDelete.linkedPortList());
            filter.add(toDelete);

            // The parent container can do the filtering and generate the
            // MoML
            CompositeEntity container = (CompositeEntity)toDelete.getContainer();
            String replicaLinkMoML = container.exportLinks(0, filter);

            // Append it to the current element undo MoML
            _undoContext.appendUndoMoML(replicaLinkMoML.toString());

            // Finally move back to context if needed
            _undoContext.moveContextEnd(_current, toDelete);
            _undoContext.appendUndoMoML("</group>\n");
        }
        toDelete.setContainer(null);

        return toDelete;
    }

    // Construct a string representing the current XML element.
    private String _getCurrentElement(String elementName) {
        StringBuffer result = new StringBuffer();
        result.append("<");
        result.append(elementName);
        // Put the attributes into the character data.
        Iterator attributeNames = _attributeNameList.iterator();
        while (attributeNames.hasNext()) {
            String name = (String)attributeNames.next();
            String value = (String)_attributes.get(name);
            if (value != null) {
                // Note that we have to escape the value again,
                // so that it is properly parsed.
                result.append(" ");
                result.append(name);
                result.append("=\"");
                result.append(StringUtilities.escapeForXML(value));
                result.append("\"");
            }
        }
        result.append(">");
        return result.toString();
    }

    // Return the port corresponding to the specified port name in the
    // specified composite entity.  If the port belongs directly to the
    // composite entity, then the argument is a simple name.  If the
    // port belongs to a component entity, then the name is the entity
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

    /**
     *  Return whether or not the given element name is undoable. NOTE: we need
     *  this method as the list of actions on namespaces and _current does not
     *  apply to elements such as "link"
     *
     * @param  elementName  Description of Parameter
     * @return              Description of the Returned Value
     * @since Ptolemy 2.1
     */
    private boolean _isUndoableElement(String elementName) {
        // The following serves as documentation of sorts for which
        // elements usage is undoable
        // NOTE: property appears first for reasons of efficency.
        if (elementName.equals("property")
                || elementName.equals("doc")
                || elementName.equals("deleteEntity")
                || elementName.equals("deletePort")
                || elementName.equals("deleteProperty")
                || elementName.equals("deleteRelation")
                || elementName.equals("entity")
                || elementName.equals("group")
                || elementName.equals("link")
                || elementName.equals("port")
                || elementName.equals("relation")
                || elementName.equals("rename")
                || elementName.equals("unlink")
                || elementName.equals("vertex")) {
            return true;
        }
        return false;
    }
    
    /** Mark the contents as being class elements.
     *  This makes them not export MoML, and prohibits name and
     *  container changes. Normally, the argument is an Entity,
     *  but this method will accept any NamedObj.
     *  This method also validates all settables, something
     *  that used to be done when the settables were individually
     *  created in explicit MoML.
     *  @param entity The instance that is defined by a class.
     *  @exception IllegalActionException If the specified object
     *   has settables that cannot be validated.
     */
    private void _markContentsClassElements(NamedObj object)
            throws IllegalActionException {
        // NOTE: Added as part of big change in class handling. EAL 12/03.
        // NOTE: It is not necessary to mark objects deeply contained
        // because they will not be asked to export MoML.
        // However, we do it anyway in order to add any
        // deeply contained Settables to the _paramsToParse list.
        Iterator attributes = object.attributeList().iterator();
        while (attributes.hasNext()) {
            NamedObj attribute = (NamedObj)attributes.next();
            attribute.setClassElement(true);
            if (attribute instanceof Settable) {
                _paramsToParse.add(attribute);
            }
            _markContentsClassElements(attribute);
        }
        if (object instanceof Entity) {
            Iterator ports = ((Entity)object).portList().iterator();
            while (ports.hasNext()) {
                NamedObj port = (NamedObj)ports.next();
                port.setClassElement(true);
                _markContentsClassElements(port);
            }
        }
        if (object instanceof CompositeEntity) {
            Iterator entities
                    = ((CompositeEntity)object).entityList().iterator();
            while (entities.hasNext()) {
                NamedObj containedEntity = (NamedObj)entities.next();
                containedEntity.setClassElement(true);
                _markContentsClassElements(containedEntity);
            }            
            Iterator relations
                    = ((CompositeEntity)object).relationList().iterator();
            while (relations.hasNext()) {
                NamedObj relation = (NamedObj)relations.next();
                relation.setClassElement(true);
                _markContentsClassElements(relation);
            }            
        }
    }

    /** Use the specified parser to parse the file or URL,
     *  which contains MoML, using the specified base to find the URL.
     *  If the URL cannot be found relative to this base, then it
     *  is searched for relative to the current working directory
     *  (if this is permitted with the current security restrictions),
     *  and then relative to the classpath.
     *  @param parser The parser to use.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param source The URL from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    private NamedObj _parse(MoMLParser parser, URL base, String source)
            throws Exception {
        URL xmlFile = null;
        StringBuffer errorMessage = new StringBuffer();
        InputStream input = null;
        try {
            xmlFile = new URL(base, source);

            // Security concern here.  Warn if external source.
            // and we are not running within an applet.
            // The warning method will throw a CancelException if the
            // user clicks "Cancel".
            String protocol = xmlFile.getProtocol();
            if (protocol != null
                    && protocol.trim().toLowerCase().equals("http")) {
                SecurityManager security = System.getSecurityManager();
                boolean withinUntrustedApplet = false;
                if (security != null) {
                    try {
                        // This is sort of arbitrary, but seems to be the
                        // closest choice.
                        security.checkCreateClassLoader();
                    } catch (SecurityException securityException) {
                        // If we are running under an untrusted applet.
                        // then a SecurityException will be thrown,
                        // and we can rely on the Applet protection
                        // mechanism to protect the user against
                        // a wayward model.

                        // Note that if the jar files were signed
                        // for use with Web Start, then we are in a trusted
                        // applet, so the SecurityException will _not_
                        // be thrown and withinUntrustedApplet will be false.

                        withinUntrustedApplet = true;
                    }
                }
                if ((security == null || withinUntrustedApplet == false)
                        && !_approvedRemoteXmlFiles.contains(xmlFile)) {

                    // If the xmlFile and _base have a common root,
                    // then the code is ok.
                    String xmlFileBase =
                        xmlFile.toString() .substring(0,
                                xmlFile.toString().lastIndexOf("/"));

                    if (_base == null
                            || !xmlFileBase.startsWith(_base.toString())) {
                        MessageHandler.warning("Security concern:\n"
                                + "About to look for MoML from the "
                                + "net at address:\n"
                                + xmlFile.toExternalForm()
                                + "\nOK to proceed?");
                    }

                    // If we get to here, the the user did not hit cancel,
                    // so we cache the file
                    _approvedRemoteXmlFiles.add(xmlFile);
                }
            }
            input = xmlFile.openStream();
        } catch (IOException ioException) {
            errorMessage.append("-- " + ioException.getMessage() + "\n");
            // The error messages used to be more verbose, uncomment
            // the next line if you would like to know what failed and why
            // errorMessage.append(
            //        "\n    base: " + base
            //        + "\n    source: " + source
            //        + "\n    xmlFile: " + xmlFile
            //        + "\n" +KernelException.stackTraceToString(ioException));

            // That failed.  Try opening it relative to the classpath.
            xmlFile = _classLoader.getResource(source);
            if (xmlFile != null) {
                input = xmlFile.openStream();
            } else {
                errorMessage.append(
                        "-- XML file not found relative to classpath.\n");

                // Failed to open relative to the classpath.
                // Try relative to the current working directory.
                // NOTE: This is last because it will fail with a
                // security exception in applets.
                String cwd = StringUtilities.getProperty("user.dir");
                if (cwd != null) {
                    try {
                        // We have to append a trailing "/" here for this to
                        // work under Solaris.
                        base = new URL("file", null, cwd + File.pathSeparator);
                        xmlFile = new URL(base, source);
                        input = xmlFile.openStream();
                    } catch (Exception exception) {
                        errorMessage.append(
                                "-- "
                                + cwd
                                + File.pathSeparator
                                + source
                                + exception.getMessage()
                                + "\n");
                    }
                }
            }
        }
        if (input == null) {
            throw new XmlException(
                    errorMessage.toString(),
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        // If we get here, then xmlFile cannot possibly be null.
        try {
            _xmlFile = xmlFile;
            try {
                NamedObj toplevel = parser.parse(xmlFile, input);
                input.close();
                return toplevel;
            } finally {
                _xmlFile = null;
            }
        } catch (CancelException ex) {
            // Parse operation cancelled.
            input.close();
            return null;
        }
    }

    // Push the current context.
    private void _pushContext() {
        _containers.push(_current);
        _namespaces.push(_namespace);
        _namespaceTranslations.push(_namespaceTranslationTable);
    }

    // If a new link is added to a container, and this is not the
    // result of a propagating change from a master, then we need
    // to record the change so that it will be exported in any exported MoML.
    private void _recordLink(
            NamedObj container,
            String port,
            String relation,
            String insertAtSpec) {

        if (container.getMoMLInfo().deferTo != null && !_propagating) {
            try {
                MoMLAttribute attr = new MoMLAttribute(container,
                        container.uniqueName("_extension"));
                if (insertAtSpec == null) {
                    attr.appendMoMLDescription("<link port=\""
                            + port
                            + "\" relation=\""
                            + relation
                            + "\"/>");
                } else {
                    attr.appendMoMLDescription("<link port=\""
                            + port
                            + "\" relation=\""
                            + relation
                            + "\" insertAt=\""
                            + insertAtSpec
                            + "\"/>");
                }
            } catch (KernelException ex) {
                throw new InternalErrorException(container, ex,
                        "Unable to record extension to class!");
            }
        }

    }

    // If a link is deleted from a container, and this is not the
    // result of a propagating change from a master, then we need
    // to record the change so that it will be exported in any exported MoML.
    private void _recordUnlink(
            NamedObj container,
            String port,
            String relation,
            String indexSpec,
            String insideIndexSpec) {
        // NOTE: We permit unlinking of links defined in the class only because we have
        // no way to distinguish links defined by the class from links that were
        // added later, and clearly, links that were added later have to be able
        // to be deleted.  EAL 12/03
        if (container.getMoMLInfo().deferTo != null && !_propagating) {
            try {
                MoMLAttribute attr = new MoMLAttribute(container,
                        container.uniqueName("_extension"));
                if (relation != null) {
                    attr.appendMoMLDescription("<unlink port=\""
                            + port
                            + "\" relation=\""
                            + relation
                            + "\"/>");
                } else if (indexSpec != null) {
                    attr.appendMoMLDescription("<unlink port=\""
                            + port
                            + "\" index=\""
                            + indexSpec
                            + "\"/>");
                } else {
                    attr.appendMoMLDescription("<unlink port=\""
                            + port
                            + "\" insideIndex=\""
                            + insideIndexSpec
                            + "\"/>");
                }
            } catch (KernelException ex) {
                throw new InternalErrorException(container, ex,
                        "Unable to record extension to class!");
            }
        }
    }

    //  Reset the undo information to give a fresh setup for the next
    //  incremental change. NOTE: this resets all the undo information except
    //  for the UndoStackAttribute which is associated with the model.
    //  @since Ptolemy II 2.1
    private void _resetUndo() {
        _undoContext = null;
        _undoContexts = new Stack();
        _undoEnabled = false;
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find an attribute with that name.
    // Return null if it is not found.  The attribute is required to
    // be contained (deeply) by the current environment, or an XmlException
    // will be thrown.
    private Attribute _searchForAttribute(String name)
            throws XmlException {
        Attribute result = null;
        // If the name is absolute, strip the prefix.
        String currentName = "(no top level)";
        if (_current != null) {
            currentName = _current.getFullName();
        }
        if (_current != null && name.startsWith(currentName)) {
            int prefix = currentName.length();
            if (name.length() > prefix) {
                name = name.substring(prefix + 1);
            }
        }
        // Now we are assured that name is relative.
        result = _current.getAttribute(name);
        if (result == null) {
            throw new XmlException("No such property: " + name
                    + " in " + currentName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        return result;
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a component entity with that name.
    // Return null if it is not found
    private ComponentEntity _searchForEntity(String name) throws XmlException {

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
            // Search the current top level, if the name matches.
            if (_toplevel != null && _toplevel instanceof ComponentEntity
                    && topLevelName.equals(_toplevel.getName())) {
                if (nextPeriod < 1) {
                    if (_current != null
                            && _current != _toplevel) {
                        throw new XmlException(
                                "Reference to an existing entity: "
                                + _toplevel.getFullName()
                                + " in an inappropriate context: "
                                + _current.getFullName(),
                                _currentExternalEntity(),
                                _parser.getLineNumber(),
                                _parser.getColumnNumber());
                    }
                    return (ComponentEntity)_toplevel;
                } else {
                    if (name.length() > nextPeriod + 1) {
                        ComponentEntity result =
                            ((CompositeEntity)_toplevel).getEntity(
                                    name.substring(nextPeriod + 1));
                        if (result != null) {
                            if (_current != null
                                    && !_current.deepContains(result)) {
                                throw new XmlException(
                                        "Reference to an existing entity: "
                                        + result.getFullName()
                                        + " in an inappropriate context: "
                                        + _current.getFullName(),
                                        _currentExternalEntity(),
                                        _parser.getLineNumber(),
                                        _parser.getColumnNumber());
                            }
                            return result;
                        }
                    }
                }
            }
            return null;
        } else {
            // Name is relative.
            if (_current instanceof CompositeEntity) {
                ComponentEntity result =
                    ((CompositeEntity)_current).getEntity(name);
                if (result != null && !_current.deepContains(result)) {
                    throw new XmlException(
                            "Reference to an existing entity: "
                            + result.getFullName()
                            + " in an inappropriate context: "
                            + _current.getFullName(),
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }
                return result;
            }
            if (_current == null) {
                // The name might be a top-level name, but without
                // the leading period.
                return _searchForEntity("." + name);
            }
            return null;
        }
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a port with that name.
    // Return null if it is not found.  The port is required to
    // be contained (deeply) by the current environment, or an XmlException
    // will be thrown.
    private Port _searchForPort(String name)
            throws XmlException {
        Port result = null;
        // If the name is absolute, strip the prefix.
        String topLevelName = "(no top level)";
        if (_toplevel != null) {
            topLevelName = _toplevel.getFullName();
        }
        if (_toplevel != null && name.startsWith(topLevelName)) {
            int prefix = topLevelName.length();
            if (name.length() > prefix) {
                name = name.substring(1, name.length());
            }
        }
        // Now we are assured that name is relative.
        if (_current instanceof Entity) {
            result = ((Entity)_current).getPort(name);
        }
        if (result == null) {
            throw new XmlException("No such port: " + name
                    + " in " + topLevelName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        return result;
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a relation with that name.
    // Return null if it is not found.  The relation is required to
    // be contained (deeply) by the current environment, or an XmlException
    // will be thrown.
    private ComponentRelation _searchForRelation(String name)
            throws XmlException {
        ComponentRelation result = null;
        // If the name is absolute, strip the prefix.
        String topLevelName = "(no top level)";
        if (_toplevel != null) {
            topLevelName = _toplevel.getFullName();
        }
        if (_toplevel != null && name.startsWith(topLevelName)) {
            int prefix = topLevelName.length();
            if (name.length() > prefix) {
                name = name.substring(1, name.length());
            }
        }
        // Now we are assured that name is relative.
        if (_current instanceof CompositeEntity) {
            result = ((CompositeEntity)_current).getRelation(name);
        }
        if (result == null) {
            throw new XmlException("No such relation: " + name
                    + " in " + topLevelName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Remote xmlFiles that the user approved of when the security concern
    // dialog popped up.
    private static Set _approvedRemoteXmlFiles = new HashSet();

    // Attributes associated with an entity.
    private Map _attributes = new HashMap();

    // The list of attribute names, in the order they were parsed.
    private List _attributeNameList = new ArrayList(0);

    // The namespace for automatic naming.
    private static String _AUTO_NAMESPACE = "auto";

    // Base for relative URLs.
    private URL _base;

    // The class loader that will be used to instantiate objects.
    private ClassLoader _classLoader = getClass().getClassLoader();

    // Count of configure tags so that they can nest.
    private int _configureNesting = 0;

    // The source attribute specified by the configure element.
    private String _configureSource;

    // The stack of objects that contain the current one.
    private Stack _containers = new Stack();

    // The current object in the hierarchy.
    private NamedObj _current;

    // The current character data for the current element.
    private StringBuffer _currentCharData;

    // The name of the currently active doc element.
    private String _currentDocName;

    // The default namespace.
    private static String _DEFAULT_NAMESPACE = "";

    // Count of doc tags so that they can nest.
    private int _docNesting = 0;

    // The external entities being parsed.
    private Stack _externalEntities = new Stack();

    // ErrorHandler that handles parse errors.
    private static ErrorHandler _handler = null;

    // List of MoMLFilters to apply if non-null.  MoMLFilters translate MoML
    // elements.  These filters will filter all MoML for all instances
    // of this class.
    private static List _filterList = null;

    // List of top-level entities imported via import element.
    private List _imports;

    // Set to true if a MoMLFilter modified the model.
    private static boolean _modified = false;

    // The current namespace.
    private String _namespace = _DEFAULT_NAMESPACE;

    // The stack of name spaces.
    private Stack _namespaces = new Stack();

    // The current translation table for names.
    private Map _namespaceTranslationTable = null;

    // The stack of maps for name translations.
    private Stack _namespaceTranslations = new Stack();

    // A list of settable parameters specified in property tags.
    private List _paramsToParse = new LinkedList();

    // The parser.
    private XmlParser _parser = new XmlParser();
    
    // Status of the deferral of the top-level.
    private boolean _previousDeferStatus = false;

    // If greater than zero, skipping an element.
    private int _skipElement = 0;

    // If attribute() found an element to skip, then
    // the first time we startElement(), we do not
    // want to increment _skipElement again in
    // startElement() because we already did it in
    // attribute().
    private boolean _skipElementIsNew = false;

    // If skipping an element, then this is the name of the element.
    private String _skipElementName;

    // True if we are skipping a rendition body.  Rendition bodies
    // are skipped if the rendition class was not found.
    private boolean _skipRendition = false;

    // Top-level entity.
    private NamedObj _toplevel = null;

    // Holds information needed to generate undo MoML at this level
    private Stack _undoContexts = new Stack();
    
    // The current undo context. This contains information about the
    // the current undo environment
    private UndoContext _undoContext = null;

    // Set this to true to get debugging information for undo.
    private static boolean _undoDebug = false;

    // Flag indicating if the MoML currently being parsed should be
    // undoable. Primarily for incremental parsing.
    private boolean _undoEnabled = false;

    // The workspace for this model.
    // List of unrecognized elements.
    private List _unrecognized;

    // The workspace for this model.
    private Workspace _workspace;

    // The XML file being read, if any.
    private URL _xmlFile = null;
}
