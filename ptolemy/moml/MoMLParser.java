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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
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
import ptolemy.kernel.Prototype;
import ptolemy.kernel.Relation;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
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
fragments without top-level entity or inherited objects can be evaluated
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
This effectively implements an inheritance mechanism, where
a component inherits all the features of the master from which it
is cloned, but then extends the model with its own changes.
<p>
This class always processes MoML commands in the following
order within a "class" or "entity" element, irrespective of the order
in which they appear:
<ol>
<li> Create properties, entities, ports and relations; and
<li> Create links.
</ol>
Within each category, the order of actions depends on the order in
which the commands appear in the MoML text.
<p>
This class works closely with MoMLChangeRequest to implement another
feature of Ptolemy II hierarchy.  In particular, if an entity is cloned
from another that identifies itself as a "class", then any changes that
are made to the class via a MoMLChangeRequest are also made to the clone.
This parser ensures that those changes are <i>not</i> exported when
MoML is exported by the clone, because they will be exported when the
master exports MoML.

@see MoMLChangeRequest
@author Edward A. Lee, Steve Neuendorffer, John Reekie, Contributor: Christopher Hylands
@version $Id$
@since Ptolemy II 0.4
*/
public class MoMLParser extends HandlerBase implements ChangeListener {

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
        // The auto namespace is disabled while propogating, since
        // this would otherwise just result in chaotic names for
        // propagated changes.
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
    
    /** React to a change request has been successfully executed.
     *  This method is called after a change request
     *  has been executed successfully. It does nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to a change request has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution an exception was thrown.
     *  Presumably the change was a propagation request.
     *  If there is a registered error handler, then the
     *  error is delegated to that handler.  Otherwise,
     *  the error is reported to stderr.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        if (_handler != null) {
            int reply = _handler.handleError(
                    change.toString(),
                    _toplevel,
                    exception);
            if (reply == ErrorHandler.CONTINUE) {
                return;
            }
        }
        // No handler, or cancel button pushed.
        // FIXME: What is the right thing to do here?
        System.err.println(exception.toString());
        exception.printStackTrace();
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
        
        // Execute any change requests that might have been queued
        // as a consequence of this change request.
        // NOTE: This used to be done after validating parameters,
        // but now these change requests might add to the list
        // of parameters to validate (because of propagation).
        // EAL 3/04
        if (_toplevel != null) {
            // Set the top level back to the default
            // found in startDocument.
            _toplevel.setDeferringChangeRequests(_previousDeferStatus);
            _toplevel.executeChangeRequests();
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
                        
                // Propagate to instances and derived classes.
                Iterator heritage
                        = _current.getShadowedHeritageList().iterator();
                while (heritage.hasNext()) {
                    Configurable inherited
                            = (Configurable)heritage.next();
                    inherited.configure(
                            _base, _configureSource, _currentCharData.toString());
                    // The above sets the modified field true, which
                    // need to reverse because we are propagating.
                    ((NamedObj)inherited).setModifiedHeritage(false);
                }

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
                        
                        // Propagate to instances and derived classes.
                        Iterator heritage
                                = previous.getShadowedHeritageList().iterator();
                        while (heritage.hasNext()) {
                            Documentation inherited
                                    = (Documentation)heritage.next();
                            inherited.setExpression(
                                    _currentCharData.toString());
                            // The above sets the modified field true, which
                            // need to reverse because we are propagating.
                            inherited.setModifiedHeritage(false);
                        }
                        
                    } else {
                        Documentation doc
                                = new Documentation(_current, _currentDocName);
                        doc.setValue(_currentCharData.toString());
                        
                        // Propagate to instances and derived classes.
                        Iterator heritage
                                = _current.getShadowedHeritageList().iterator();
                        while (heritage.hasNext()) {
                            NamedObj inherited = (NamedObj)heritage.next();
                            Documentation newDoc = new Documentation(
                                    inherited, _currentDocName);
                            newDoc.setValue(_currentCharData.toString());
                            // The above sets the modified field true, which
                            // need to reverse because we are propagating.
                            newDoc.setInherited(true);
                            newDoc.setModifiedHeritage(false);
                        }
                    }
                } else {
                    // Empty doc tag.  Remove previous doc element, if
                    // there is one.
                    if (previous != null) {
                        // Propagate to instances and derived classes.
                        Iterator heritage
                                = previous.getShadowedHeritageList().iterator();
                        while (heritage.hasNext()) {
                            Documentation inherited
                                    = (Documentation)heritage.next();
                            inherited.setContainer(null);
                        }

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
                
            } else if (elementName.equals("group")) {
                try {
                    _namespace = (String)_namespaces.pop();
                }
                catch (EmptyStackException ex) {
                    _namespace = _DEFAULT_NAMESPACE;
                }
                
            } else if (
                    elementName.equals("class")
                    || elementName.equals("entity")
                    || elementName.equals("model")) {
                // Process link requests that have accumulated in
                // this element.
                if (_linkRequests != null) {
                    Iterator requests = _linkRequests.iterator();
                    while (requests.hasNext()) {
                        LinkRequest request = (LinkRequest)requests.next();
                        // Be sure to use the handler if these fail so that
                        // we continue to the next link requests.
                        try {
                            request.execute();
                        } catch (Exception ex) {
                            if (_handler != null) {
                                int reply = _handler.handleError(
                                        request.toString(), _current, ex);
                                if (reply == ErrorHandler.CONTINUE) {
                                    continue;
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
                            } else {
                                // No handler.  Throw the original exception.
                                throw ex;
                            }
                        }
                    }
                }
                try {
                    _current = (NamedObj)_containers.pop();
                    _namespace = (String)_namespaces.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _current = null;
                    _namespace = _DEFAULT_NAMESPACE;
                }
                // Use a separate try-catch for more robustness
                // against malformed XML.
                try {
                    _linkRequests = (List)_linkRequestStack.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _linkRequests = null;
                }
            } else if (
                    elementName.equals("property")
                    || elementName.equals("deleteEntity")
                    || elementName.equals("deletePort")
                    || elementName.equals("deleteProperty")
                    || elementName.equals("deleteRelation")
                    || elementName.equals("director")
                    || elementName.equals("port")
                    || elementName.equals("relation")
                    || elementName.equals("rendition")
                    || elementName.equals("vertex")) {
                try {
                    _current = (NamedObj)_containers.pop();
                    _namespace = (String)_namespaces.pop();
                } catch (EmptyStackException ex) {
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
    
    /** Given a file name or URL description, find a URL on which
     *  openStream() will work. If a base is given, the URL can
     *  be found relative to that base.
     *  If the URL cannot be found relative to this base, then it
     *  is searched for relative to the current working directory
     *  (if this is permitted with the current security restrictions),
     *  and then relative to the classpath. If the URL is external
     *  and is not relative to a previously defined base, then the
     *  user will be warned about a security concern and given the
     *  opportunity to cancel.
     *  <p>
     *  NOTE: This may trigger a dialog with the user (about
     *  security concerns), and hence should be called in the event
     *  thread.
     *  @param source A file name or URL description.
     *  @param base The base URL for relative references, or null if
     *   there is none.
     *  @return A URL on which openStream() will succeed.
     *  @exception Exception If the file or URL cannot be found or
     *   if the user cancels on being warned of a security concern.
     */
    public URL fileNameToURL(String source, URL base)
            throws Exception {
        URL result = null;
        StringBuffer errorMessage = new StringBuffer();
        InputStream input = null;
        try {
            result = new URL(base, source);

            // Security concern here.  Warn if external source.
            // and we are not running within an applet.
            // The warning method will throw a CancelException if the
            // user clicks "Cancel".
            String protocol = result.getProtocol();
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
                        && !_approvedRemoteXmlFiles.contains(result)) {

                    // If the result and _base have a common root,
                    // then the code is ok.
                    String resultBase =
                        result.toString() .substring(0,
                                result.toString().lastIndexOf("/"));

                    if (_base == null
                            || !resultBase.startsWith(_base.toString())) {
                        MessageHandler.warning("Security concern:\n"
                                + "About to look for MoML from the "
                                + "net at address:\n"
                                + result.toExternalForm()
                                + "\nOK to proceed?");
                    }

                    // If we get to here, the the user did not hit cancel,
                    // so we cache the file
                    _approvedRemoteXmlFiles.add(result);
                }
            }
            input = result.openStream();
        } catch (IOException ioException) {
            errorMessage.append("-- " + ioException.getMessage() + "\n");
            // The error messages used to be more verbose, uncomment
            // the next line if you would like to know what failed and why
            // errorMessage.append(
            //        "\n    base: " + base
            //        + "\n    source: " + source
            //        + "\n    result: " + result
            //        + "\n" +KernelException.stackTraceToString(ioException));

            // That failed.  Try opening it relative to the classpath.
            result = _classLoader.getResource(source);
            if (result != null) {
                input = result.openStream();
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
                        result = new URL(base, source);
                        input = result.openStream();
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
        // If we get here, then result cannot possibly be null.
        // Close the open stream, which was used only to make
        // sure it would work.
        input.close();
        return result;
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
     *  If the <i>input</i> URL has already been parsed, then
     *  return the model that previously parsed.  Note that this
     *  means that an application that opens and then closes
     *  a model and expects to re-parse the XML when re-opening
     *  must call purgeModelRecord() when closing it.
     *  This method uses parse(URL, InputStream).
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model, or
     *   null if the file is not recognized as a MoML file.
     *  @exception Exception If the parser fails.
     *  @see #purgeModelRecord(URL)
     */
    public NamedObj parse(URL base, URL input)
            throws Exception {
        _xmlFile = input;
        try {
            if (_imports == null) {
                _imports = new HashMap();
            } else {
                WeakReference reference 
                        = (WeakReference)_imports.get(input);
                NamedObj previous = null;
                if (reference != null) {
                    previous = (NamedObj)reference.get();
                    if (previous == null) {
                        _imports.remove(input);
                    }
                } 
                if (previous != null) {
                    // NOTE: In theory, we should not even have to
                    // check whether the file has been updated, because
                    // if changes were made to model since it was loaded,
                    // they should have been propagated.
                    return previous;
                }
            }
            NamedObj result = parse(base, input.openStream());
            _imports.put(input, new WeakReference(result));
            return result;
        } finally {
            _xmlFile = null;
        }
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  This method uses parse(URL, Reader).  Note that this
     *  bypasses the mechanism of parse(URL, URL) that returns
     *  a previously parsed model. This method will always re-parse
     *  using data from the stream.
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
     *  Note that this
     *  bypasses the mechanism of parse(URL, URL) that returns
     *  a previously parsed model. This method will always re-parse
     *  using data from the stream.
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

                // NOTE: I'm not sure if it is necessary to check to
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

    /** Parse the file with the given name, which contains MoML.
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
     *  <p>If the file has already been parsed, then
     *  return the model that previously parsed.  Note that this
     *  means that an application that opens and then closes
     *  a model and expects to re-parse the XML when re-opening
     *  should call purgeModelRecord() when closing it.
     *
     *  @param filename The file name from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     *  @exception SecurityException If the user.dir system property is
     *  not available.
     *  @see #purgeModelRecord(String)
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
        return parse(base, file.toURL());
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

    /** Purge any record of a model opened from the specified
     *  URL.
     *  @param url The URL.
     *  @see #parse(URL, URL)
     */
    public static void purgeModelRecord(URL url) {
        if (_imports != null) {
            _imports.remove(url);
        }
    }
    
    /** Purge any record of a model opened from the specified
     *  file name.
     *
     *  <p>Note that this method attempts to read the user.dir system
     *  property, which is not generally available in applets.  Hence
     *  it is probably not a good idea to use this method in applet code,
     *  since it will probably fail outright.
     * 
     *  @param filename The file name from which to read MoML.
     *  @exception MalformedURLException If the file name cannot be converted to a URL.
     *  @exception SecurityException If the user.dir system property is
     *   not available.
     *  @see #parse(URL, String)
     */
    public static void purgeModelRecord(String filename)
            throws MalformedURLException {
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
        purgeModelRecord(file.toURL());
    }
    
    /** Reset the MoML parser, forgetting about any previously parsed
     *  models.
     */
    public void reset() {
        _attributes = new HashMap();
        _configureNesting = 0;
        _containers = new Stack();
        _linkRequestStack = new Stack();
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
     *  whether this class has already been instantiated, and if so,
     *  return the previous instance.  If the source is non-null, then
     *  this finds an instance that has been previously opened by this
     *  application from the same URL.  If the source is null and
     *  the class name is absolute (starting with a period), then
     *  look for the class in the current top level. If the source
     *  is null and the class name is relative, then look for
     *  the class relative to the current context.
     *  @param name The name of the MoML class to search for.
     *  @param source The URL source
     *  @return If the class has already been instantiated, return
     *   the previous instance, otherwise return null.
     */
    public ComponentEntity searchForClass(String name, String source)
            throws Exception {

        // Use a canonical form of the source.
        URL sourceURL = null;
        if (source != null) {
            sourceURL = fileNameToURL(source, _base);
        }

        if (_imports != null && source != null) {
            WeakReference reference = (WeakReference)_imports.get(source);
            Object possibleCandidate = null;
            if (reference != null) {
                possibleCandidate = reference.get();
                if (possibleCandidate == null) {
                    _imports.remove(source);
                }
            } 
            if (possibleCandidate instanceof ComponentEntity) {
                ComponentEntity candidate = (ComponentEntity)possibleCandidate;
                // Check that the candidate is a class.
                if (candidate.isClassDefinition()) {
                    // Check that the class name matches.
                    // Only the last part, after any periods has to match.
                    String realClassName = name;
                    int lastPeriod = name.lastIndexOf(".");
                    if (lastPeriod >= 0 && (name.length() > lastPeriod + 1)) {
                        realClassName = name.substring(lastPeriod + 1);
                    }
                
                    String candidateClassName = candidate.getClassName();
                    lastPeriod = candidateClassName.lastIndexOf(".");
                    if (lastPeriod >= 0
                            && (candidateClassName.length() > lastPeriod + 1)) {
                        candidateClassName
                                = candidateClassName.substring(lastPeriod + 1);
                    }
                    if (candidateClassName.equals(realClassName)) {
                        return candidate;
                    }
                }
            }
        }

        // Source has not been previously loaded.
        // Only if the source is null can we have a matching previous instance.
        if (source == null) {
            ComponentEntity candidate = _searchForEntity(name, _current);
            if (candidate != null) {
                // Check that it's a class.
                if (candidate.isClassDefinition()) {
                    return candidate;
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
            _previousDeferStatus = _toplevel.isDeferringChangeRequests();
            _toplevel.setDeferringChangeRequests(true);
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
            
            boolean undoEnabled = _undoEnabled && _isUndoableElement(elementName);

            // Handle the undo aspect of the parsing if enabled
            if (undoEnabled) {
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
            
            //////////////////////////////////////////////////////////////
            //// class
            
            if (elementName.equals("class")) {
                String className = (String)_attributes.get("extends");
                String entityName = (String)_attributes.get("name");
                String source = (String)_attributes.get("source");

                _checkForNull(entityName, "No name for element \"class\"");

                // For undo purposes need to know if the entity existed
                // already
                Entity entity = _searchForEntity(entityName, _current);
                boolean existedAlready = (entity != null);
                if (!existedAlready) {
                    NamedObj candidate = _createEntity(className, entityName, source);
                    if (candidate instanceof Entity) {
                        entity = (Entity)candidate;
                    } else {
                        throw new IllegalActionException(_current,
                        "Attempt to create a class named "
                        + entityName
                        + " from a class that "
                        + "is not a subclass of Entity: "
                        + className);
                    }
                }
                // NOTE: The entity may be at the top level.
                if (_linkRequests != null) {
                    _linkRequestStack.push(_linkRequests);
                }
                _linkRequests = new LinkedList();
                if (_current != null) {
                    _pushContext();
                } else if (_toplevel == null) {
                    // NOTE: Used to set _toplevel to newEntity, but
                    // this isn't quite right because the entity may have a
                    // composite name.
                    _toplevel = entity.toplevel();
                    
                    // Ensure that if any change requests occur as a
                    // consequence of adding items to this top level,
                    // that execution of those change requests is deferred
                    // until endDocument().
                    _toplevel.setDeferringChangeRequests(true); 

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
                boolean converted = false;
                if (!existedAlready) {
                    entity.setClassDefinition(true);
                    // Adjust the classname and superclass of the object.
                    // NOTE: This used to set the class name to entity.getFullName(),
                    // and superclass to className.  Now that we've consolidated
                    // these, we set the class name to the value of "extends"
                    // attribute that was used to create this.
                    entity.setClassName(className);
                } else {
                    // If the object is not already a class, then convert
                    // it to one.
                    if (!entity.isClassDefinition()) {
                        entity.setClassDefinition(true);
                        converted = true;
                    }
                }
                
                _current = entity;
                _namespace = _DEFAULT_NAMESPACE;

                if (undoEnabled && _undoContext.isUndoable()) {
                    // Handle the undo aspect.
                    if (existedAlready) {
                        if (!converted) {
                            _undoContext.appendUndoMoML("<class name=\"" + entityName +
                                    "\" >\n");
                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</class>\n");
                        } else {
                            // Converting from entity to class, so reverse this.
                            _undoContext.appendUndoMoML("<entity name=\"" + entityName +
                                    "\" >\n");
                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</entity>\n");
                        }
                        _undoContext.setChildrenUndoable(true);
                    } else {
                        _undoContext.appendUndoMoML("<deleteEntity name=\"" + entityName +
                                "\" />\n");
                        // Do not need to continue generating undo MoML
                        // as the deleteEntity takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                    }
                }
                
            //////////////////////////////////////////////////////////////
            //// configure

            } else if (elementName.equals("configure")) {
                _checkClass(_current, Configurable.class,
                        "Element \"configure\" found inside an element that "
                        + "does not implement Configurable. It is: "
                        + _current);
                _configureSource = (String)_attributes.get("source");
                _currentCharData = new StringBuffer();
                // Count configure tags so that they can nest.
                _configureNesting++;
                
            //////////////////////////////////////////////////////////////
            //// deleteEntity

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
                if (undoEnabled && _undoContext.isUndoable()) {
                    // Note that nothing below a deleteEntity is undoable
                    _undoContext.setChildrenUndoable(false);
                }

            //////////////////////////////////////////////////////////////
            //// deletePort

            } else if (elementName.equals("deletePort")) {
                String portName = (String)_attributes.get("name");
                _checkForNull(portName,
                        "No name for element \"deletePort\"");
                // The entity attribute is optional.
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
                if (undoEnabled && _undoContext.isUndoable()) {
                    // Note that nothing below a deletePort is undoable
                    _undoContext.setChildrenUndoable(false);
                }

            //////////////////////////////////////////////////////////////
            //// deleteProperty

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
                if (undoEnabled && _undoContext.isUndoable()) {
                    // Note that nothing below a deleteProperty is undoable
                    _undoContext.setChildrenUndoable(false);
                }

            //////////////////////////////////////////////////////////////
            //// deleteRelation

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

                if (undoEnabled && _undoContext.isUndoable()) {
                    // Note that nothing below a deleteEntity is undoable
                    _undoContext.setChildrenUndoable(false);
                }

            //////////////////////////////////////////////////////////////
            //// director

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
                // NOTE: No propagation occurs here... Hopefully, deprecated
                // elements are not used with class structures.
                _current = _createInstance(newClass, arguments);
                _namespace = _DEFAULT_NAMESPACE;

            //////////////////////////////////////////////////////////////
            //// doc

            } else if (elementName.equals("doc")) {
                _currentDocName = (String)_attributes.get("name");
                _currentCharData = new StringBuffer();
                // Count doc tags so that they can nest.
                _docNesting++;

            //////////////////////////////////////////////////////////////
            //// entity

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
                Entity entity = _searchForEntity(entityName, _current);
                boolean existedAlready = (entity != null);
                boolean converted = false;
                if (existedAlready) {
                    // Check whether it was previously a class, in which case
                    // it is being converted to an entity.
                    if (entity.isClassDefinition()) {
                        entity.setClassDefinition(false);
                        converted = true;
                    }
                } else {
                    NamedObj candidate = _createEntity(className, entityName, source);
                    if (candidate instanceof Entity) {
                        entity = (Entity)candidate;
                        entity.setClassName(className);
                    } else {
                        throw new IllegalActionException(_current,
                        "Attempt to create an entity named "
                        + entityName
                        + " from a class that "
                        + "is not a subclass of Entity: "
                        + className);
                    }
                }
                // NOTE: The entity may be at the top level.
                if (_linkRequests != null) {
                    _linkRequestStack.push(_linkRequests);
                }
                _linkRequests = new LinkedList();

                if (_current != null) {
                    _pushContext();
                } else if (_toplevel == null) {
                    // NOTE: We used to set _toplevel to newEntity, but
                    // this isn't quite right because the entity may have a
                    // composite name.
                    _toplevel = entity.toplevel();
                    
                    // Ensure that if any change requests occur as a
                    // consequence of adding items to this top level,
                    // that execution of those change requests is deferred
                    // until endDocument().
                    _toplevel.setDeferringChangeRequests(true); 

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
                _current = entity;
                _namespace = _DEFAULT_NAMESPACE;

                if (undoEnabled && _undoContext.isUndoable()) {
                    // Handle the undo aspect.
                    if (existedAlready) {
                        if (!converted) {
                            _undoContext.appendUndoMoML("<entity name=\"" + entityName +
                                    "\" >\n");
                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</entity>\n");
                        } else {
                            // Converted from a class to an entity, so reverse this.
                            _undoContext.appendUndoMoML("<class name=\"" + entityName +
                                    "\" >\n");
                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</class>\n");
                        }
                        _undoContext.setChildrenUndoable(true);
                    } else {
                        _undoContext.appendUndoMoML("<deleteEntity name=\"" + entityName +
                                "\" />\n");
                        // Do not need to continue generating undo MoML
                        // as the deleteEntity takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                    }
                }
                
            //////////////////////////////////////////////////////////////
            //// group

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
                if (undoEnabled && _undoContext.isUndoable()) {
                    // NOTE: for groups with namespaces, rely on the names
                    // already being part of undo MoML names instead of
                    // tracking the namespace prefix
                    _undoContext.appendUndoMoML("<group>\n");
                    // Need to continue undoing and use an end tag
                    _undoContext.appendClosingUndoMoML("</group>\n");
                    _undoContext.setChildrenUndoable(true);
                }

            //////////////////////////////////////////////////////////////
            //// input

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
                    _parse(newParser, _base, source);
                }

            //////////////////////////////////////////////////////////////
            //// link

            } else if (elementName.equals("link")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"link\"");
                // Relation attribute now optional
                String relationName = (String)_attributes.get("relation");
                String insertAtSpec = (String)_attributes.get("insertAt");
                String insertInsideAtSpec =
                    (String)_attributes.get("insertInsideAt");

                // Link is stored and processed last.
                LinkRequest request = new LinkRequest(
                        portName,
                        relationName,
                        insertAtSpec,
                        insertInsideAtSpec);
                if (_linkRequests != null) {
                    _linkRequests.add(request);
                } else {
                    // Very likely, the context is null, in which
                    // case the following will throw an exception.
                    // We defer to it in case somehow a link request
                    // is being made at the top level with a non-null
                    // context (e.g. via a change request).
                    request.execute();
                }

            //////////////////////////////////////////////////////////////
            //// port

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
                    
                    // First check that there will be no name collision
                    // when this is propagated. Note that we need to
                    // include all heritage objects, irrespective of whether
                    // they are locally changed.
                    List heritageList = container.getHeritageList();
                    Iterator heritage = heritageList.iterator();
                    while (heritage.hasNext()) {
                        Entity inherited = (Entity)heritage.next();
                        if (inherited.getPort(portName) != null) {
                            throw new IllegalActionException(container,
                            "Cannot create port because a subclass or instance "
                            + "contains a port with the same name: "
                            + inherited.getPort(portName).getFullName());
                        }
                    }

                    if (newClass == null) {
                        // Classname is not given.  Invoke newPort() on the
                        // container.
                        port = container.newPort(portName);
                        
                        // Propagate to instances and derived classes.
                        heritage = heritageList.iterator();
                        while (heritage.hasNext()) {
                            Entity inherited = (Entity)heritage.next();
                            Port newPort = inherited.newPort(portName);
                            newPort.setInherited(true);
                        }
                    } else {
                        // Classname is given.
                        Object[] arguments = new Object[2];
                        arguments[0] = container;
                        arguments[1] = portName;
                        port = (Port)_createInstance(newClass, arguments);

                        // Propagate to instances and derived classes.
                        heritage = heritageList.iterator();
                        while (heritage.hasNext()) {
                            Entity inherited = (Entity)heritage.next();
                            // Invoke the constructor.
                            arguments[0] = inherited;
                            NamedObj propagatedPort
                                    = _createInstance(newClass, arguments);
                            propagatedPort.setInherited(true);
                        }
                    }                   
                }
                _pushContext();
                _current = port;
                _namespace = _DEFAULT_NAMESPACE;

                // Handle the undo aspect if needed
                if (undoEnabled && _undoContext.isUndoable()) {
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
                // supported nonetheless. This is not propagated, but
                // hopefully deprecated attributes are not used with
                // the class mechanism.
                if (port instanceof IOPort) {
                    String direction = (String)_attributes.get("direction");
                    if (direction != null) {
                        IOPort ioport = (IOPort)port;
                        boolean isOutput = direction.equals("output")
                                || direction.equals("both");
                        boolean isInput = direction.equals("input")
                                || direction.equals("both");
                        // If this object is an inherited object, then its I/O status
                        // cannot be changed.  EAL 1/04.
                        if (alreadyExisted
                                &&  ioport.isInherited()) {
                            if (ioport.isInput() != isInput
                                    || ioport.isOutput() != isOutput) {
                                throw new IllegalActionException(ioport,
                                    "Cannot change whether this port is " +
                                    "an input or output. That property is " +
                                    "fixed by the class definition.");
                            }
                        }
                        ioport.setOutput(isOutput);
                        ioport.setInput(isInput);
                    }
                }

            //////////////////////////////////////////////////////////////
            //// property

            } else if (elementName.equals("property")) {
                String className = (String)_attributes.get("class");
                String propertyName = (String)_attributes.get("name");
                _checkForNull(propertyName,
                        "No name for element \"property\"");
                String value = (String)_attributes.get("value");

                _handlePropertyElement(className, propertyName, value);

            //////////////////////////////////////////////////////////////
            //// relation

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
                    
                    // First check that there will be no name collision
                    // when this is propagated. Note that we need to
                    // include all heritage objects, irrespective of whether
                    // they are locally changed.
                    List heritageList = container.getHeritageList();
                    Iterator heritage = heritageList.iterator();
                    while (heritage.hasNext()) {
                        CompositeEntity inherited = (CompositeEntity)heritage.next();
                        if (inherited.getRelation(relationName) != null) {
                            throw new IllegalActionException(container,
                            "Cannot create relation because a subclass or instance "
                            + "contains a relation with the same name: "
                            + inherited.getRelation(relationName).getFullName());
                        }
                    }

                    NamedObj newRelation = null;
                    _pushContext();
                    if (newClass == null) {
                        // No classname. Use the newRelation() method.
                        newRelation = container.newRelation(relationName);

                        // Propagate to instances and derived classes.
                        heritage = heritageList.iterator();
                        while (heritage.hasNext()) {
                            CompositeEntity inherited = (CompositeEntity)heritage.next();
                            Relation propagatedRelation
                                    = inherited.newRelation(relationName);
                            propagatedRelation.setInherited(true);
                        }
                    } else {
                        Object[] arguments = new Object[2];
                        arguments[0] = (CompositeEntity)_current;
                        arguments[1] = relationName;
                        newRelation = _createInstance(newClass, arguments);
 
                        // Propagate to instances and derived classes.
                        heritage = heritageList.iterator();
                        while (heritage.hasNext()) {
                            CompositeEntity inherited = (CompositeEntity)heritage.next();
                            // Invoke the constructor.
                            arguments[0] = inherited;
                            NamedObj propagatedRelation
                                    = _createInstance(newClass, arguments);
                            propagatedRelation.setInherited(true);
                        }
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
                if (undoEnabled && _undoContext.isUndoable()) {
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

            //////////////////////////////////////////////////////////////
            //// rename

            } else if (elementName.equals("rename")) {
                String newName = (String)_attributes.get("name");
                _checkForNull(newName, "No new name for element \"rename\"");
                if (_current != null) {
                    String oldName = _current.getName();
                    
                    // NOTE: Added to ensure that inherited objects aren't changed.
                    // EAL 1/04.
                    if (!oldName.equals(newName) 
                            && _current.isInherited()) { 
                        throw new IllegalActionException(_current,
                            "Cannot change the name to "
                            + newName
                            + ". The name is fixed by the class definition.");
                    }
                    // Propagate.  Note that a rename in a derived class
                    // could cause a NameDuplicationException.  We have to
                    // be able to unroll the changes if that occurs.
                    Iterator heritage = _current.getHeritageList().iterator();
                    Set changedName = new HashSet();
                    HashMap changedClassName = new HashMap();
                    NamedObj inherited = null;
                    try {
                        while (heritage.hasNext()) {
                            inherited = (NamedObj)heritage.next();
                            // If the inherited object has the same
                            // name as the old name, then we assume it
                            // should change.
                            if (inherited.getName().equals(oldName)) {
                                inherited.setName(newName);
                                changedName.add(inherited);
                            }
                            // Also need to modify the class name of
                            // the instance or derived class if the
                            // class or base class changes its name.
                            if (inherited instanceof Instantiable) {
                                Instantiable parent = ((Instantiable)inherited)
                                        .getParent();
                                // This relies on the depth-first search
                                // order of the getHeritageList() method
                                // to be sure that the base class will
                                // already be in the changedName set if 
                                // its name will change.
                                if (parent != null
                                        && (parent == _current 
                                        || changedName.contains(parent))) {
                                    String previousClassName
                                            = inherited.getClassName();
                                    int last = previousClassName
                                            .lastIndexOf(oldName);
                                    if (last < 0) {
                                        throw new InternalErrorException(
                                        "Expected instance "
                                        + inherited.getFullName()
                                        + " to have class name ending with "
                                        + oldName
                                        + " but its class name is "
                                        + previousClassName);
                                    }
                                    String newClassName = newName;
                                    if (last > 0) {
                                        newClassName
                                                = previousClassName
                                                .substring(0, last)
                                                + newName;
                                    }
                                    inherited.setClassName(newClassName);
                                    changedClassName.put(
                                            inherited, previousClassName);
                                }
                            }                                
                        }
                    } catch (NameDuplicationException ex) {
                        // Unravel the name changes before
                        // rethrowing the exception.
                        Iterator toUndo = changedName.iterator();
                        while (toUndo.hasNext()) {
                            NamedObj revert = (NamedObj)toUndo.next();
                            revert.setName(oldName);
                        }
                        Iterator classNameFixes = changedClassName.entrySet().iterator();
                        while (classNameFixes.hasNext()) {
                            Map.Entry revert = (Map.Entry)classNameFixes.next();
                            NamedObj toFix = (NamedObj)revert.getKey();
                            String previousClassName = (String)revert.getValue();
                            toFix.setClassName(previousClassName);
                        }
                        throw new IllegalActionException(_current, ex,
                        "Propagation to instance and/or derived class causes" +
                        "name duplication: " + inherited.getFullName());
                    }

                    _current.setName(newName);

                    // Handle the undo aspect if needed
                    if (undoEnabled && _undoContext.isUndoable()) {
                        // First try and rename in the parent context.
                        // NOTE: this is a bit of a hack but is the only way
                        // I could see of doing the rename without having to
                        // change the semantics or location of the rename
                        // element
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
                    
                    // If _current is a class definition, then find
                    // subclasses and instances and propagate the
                    // change to the name of the
                    // object they refer to.
                    if ((_current instanceof Instantiable)
                            && ((Instantiable)_current).isClassDefinition()) {
                        List deferredFrom
                                = ((Instantiable)_current).getChildren();
                        if (deferredFrom != null) {
                            Iterator deferrers = deferredFrom.iterator();
                            while (deferrers.hasNext()) {
                                WeakReference reference
                                        = (WeakReference)deferrers.next();
                                Prototype deferrer = (Prototype)reference.get();
                                if (deferrer != null) {
                                // Got a live one.
                                // Need to determine whether the name is
                                // absolute or relative.
                                String replacementName = newName;
                                    if (deferrer.getClassName().startsWith(".")) {
                                        replacementName = _current.getFullName();
                                    }
                                    deferrer.setClassName(replacementName);
                                }
                            }
                        }
                    }
                }

            //////////////////////////////////////////////////////////////
            //// rendition

            } else if (elementName.equals("rendition")) {
                // NOTE: The rendition element is deprecated.
                // Use an icon property instead.
                // This ignores everything inside it.
                _skipRendition = true;

            //////////////////////////////////////////////////////////////
            //// unlink

            } else if (elementName.equals("unlink")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"unlink\"");
                String relationName = (String)_attributes.get("relation");
                String indexSpec = (String)_attributes.get("index");
                String insideIndexSpec =
                    (String)_attributes.get("insideIndex");

                // Unlink is stored and processed last.
                UnlinkRequest request = new UnlinkRequest(
                        portName,
                        relationName,
                        indexSpec,
                        insideIndexSpec);
                if (_linkRequests != null) {
                    _linkRequests.add(request);
                } else {
                    // Very likely, the context is null, in which
                    // case the following will throw an exception.
                    // We defer to it in case somehow a link request
                    // is being made at the top level with a non-null
                    // context (e.g. via a change request).
                    request.execute();
                }

            //////////////////////////////////////////////////////////////
            //// vertex

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
                
                // No need to check for name collision on propagated
                // objects because Vertex is a singleton.

                Vertex vertex = previous;
                // Create a new vertex only if it didn't previously exist.
                if (vertex == null) {
                    vertex = new Vertex((Relation)_current, vertexName);
                    
                    // Propagate to instances and derived classes.
                    Iterator heritage = _current.getHeritageList().iterator();
                    while (heritage.hasNext()) {
                        Relation inherited = (Relation)heritage.next();
                        Vertex propagatedVertex
                                = new Vertex(inherited, vertexName);
                        propagatedVertex.setInherited(true);
                    }
                }
                
                // Deal with setting the location.
                String value = (String)_attributes.get("value");
                // If value is null or same as before, then there is
                // nothing to do.
                if (value != null && !value.equals(previousValue)) {
                    vertex.setExpression(value);
                    vertex.setModifiedHeritage(true);
                    _paramsToParse.add(vertex);

                    // Propagate to instances and derived classes.
                    Iterator heritage
                            = vertex.getShadowedHeritageList().iterator();
                    while (heritage.hasNext()) {
                        Vertex inherited = (Vertex)heritage.next();
                        inherited.setExpression(value);
                        // The above sets the modified field true, which
                        // need to reverse because we are propagating.
                        inherited.setModifiedHeritage(false);
                        _paramsToParse.add(inherited);
                    }
                }
                
                _pushContext();
                _current = vertex;
                _namespace = _DEFAULT_NAMESPACE;

                if (undoEnabled && _undoContext.isUndoable()) {
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
            
        //////////////////////////////////////////////////////////////
        //// failure

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
    ////                         private methods                   ////

    /** Attempt to find a MoML class from an external file.
     *  If there is no source defined, then search for the file
     *  relative to the classpath.
     *  @param className The class name.
     *  @param source The source as specified in the XML.
     */
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
            candidateReference
                    = _findOrParse(newParser, _base, classAsFile,
                    className, source);
        } catch (Exception ex2) {
            // Try the alternate file, if it's not null.
            if (altClassAsFile != null) {
                try {
                    candidateReference
                            = _findOrParse(newParser, _base,
                            altClassAsFile, className, source);
                    classAsFile = altClassAsFile;
                } catch (Exception ex3) {
                    // Cannot find a class definition.
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
        
        // Load an associated icon, if there is one.
        _loadIconForClass(className, reference);
        
        return reference;
    }
    
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

    /** Create a new entity from the specified class name, give
     *  it the specified entity name, and specify that its container
     *  is the current container object.  If the current container
     *  already contains an entity with the specified name and class,
     *  then return that entity.  If the class name matches
     *  a class that has been previously defined in the scope
     *  (or with an absolute name), then that class is instantiated.
     *  Otherwise, the class name is interpreted as a Java class name
     *  and we attempt to construct the entity.  If instantiating a Java
     *  class doesn't work, then we look for a MoML file on the
     *  classpath that defines a class by this name.  The file
     *  is assumed to be named "foo.xml", where "foo" is the name
     *  of the class.  Moreover, the classname is assumed to have
     *  no periods (since a MoML name does not allow periods,
     *  this is reasonable). If _current is not an instance
     *  of CompositeEntity, then an XML exception is thrown.
     *  If an object is created and we are propagating, then that
     *  object is marked as an inherited object.
     *  The third argument, if non-null, gives a URL to import
     *  to create a reference class from which to instantiate this
     *  entity.
     * 
     * @param className
     * @param entityName
     * @param source
     * @return
     * @throws Exception
     */
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
        ComponentEntity previous = _searchForEntity(entityName, _current);
        Class newClass = null;
        ComponentEntity reference = null;
        if (className != null) {
            // A class name is given.
            reference = searchForClass(className, source);
            if (reference == null) {
                // Allow the class name to be local in the current context
                // or defined in scope. Search for a class definition that
                // matches in the current context.
                reference = _searchForClassInContext(className, source);
            }
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
                
                // First check that there will be no name collision
                // when this is propagated. Note that we need to
                // include all heritage objects, irrespective of whether
                // they are locally changed.
                List heritageList = container.getHeritageList();
                Iterator heritage = heritageList.iterator();
                while (heritage.hasNext()) {
                    CompositeEntity inherited = (CompositeEntity)heritage.next();
                    if (inherited.getEntity(entityName) != null) {
                        throw new IllegalActionException(container,
                        "Cannot create entity because a subclass or instance "
                        + "contains an entity with the same name: "
                        + inherited.getEntity(entityName).getFullName());
                    }
                }
                
                _checkClass(_current, CompositeEntity.class,
                        "Cannot create an entity inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);
                Object[] arguments = new Object[2];

                arguments[0] = _current;
                arguments[1] = entityName;
                NamedObj newEntity = _createInstance(newClass, arguments);
                _loadIconForClass(className, newEntity);
                
                // Propagate to instances and derived classes.
                heritage = heritageList.iterator();
                while (heritage.hasNext()) {
                    CompositeEntity inherited = (CompositeEntity)heritage.next();
                    // Invoke the constructor.
                    arguments[0] = inherited;
                    NamedObj propagatedEntity = _createInstance(newClass, arguments);
                    _loadIconForClass(className, propagatedEntity);
                    propagatedEntity.setInherited(true);
                    _markContentsInherited(propagatedEntity);
                }

                return newEntity;
            } else {
                // Top-level entity.  Instantiate in the workspace.
                // Note that there cannot possibly be any propagation here.
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                NamedObj result = _createInstance(newClass, arguments);
                result.setName(entityName);
                _loadIconForClass(className, result);
                return result;
            }
        } else {
            // Extending a previously defined entity.  Check to see that
            // it was defined to be a class definition.
            if (!reference.isClassDefinition()) {
                throw new XmlException("Attempt to extend an entity that "
                        + "is not a class: " + reference.getFullName(),
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }
            
            // First check that there will be no name collision
            // when this is propagated. Note that we need to
            // include all heritage objects, irrespective of whether
            // they are locally changed.
            // If the container is null, then we can't possibly get
            // a name collision.
            List heritageList = null;
            if (container != null) {
                heritageList = container.getHeritageList();
                Iterator heritage = heritageList.iterator();
                while (heritage.hasNext()) {
                    CompositeEntity inherited = (CompositeEntity)heritage.next();
                    if (inherited.getEntity(entityName) != null) {
                        throw new IllegalActionException(container,
                        "Cannot create entity because a subclass or instance "
                        + "contains an entity with the same name: "
                        + inherited.getEntity(entityName).getFullName());
                    }
                }
            }

            // Instantiate it.
            ComponentEntity newEntity = (ComponentEntity)reference.instantiate(
                    container, entityName);
            
            // The original reference object may have had a URIAttribute,
            // but the new one should not. The clone would have copied
            // it.  The URIAttribute refers to the file in which the
            // component is defined, but the new entity is defined
            // in whatever file its container is defined. Leaving the
            // URIAttribute in the clone results in "look inside"
            // opening the clone but making it look as if it is the
            // original.
            URIAttribute modelURI = (URIAttribute)newEntity.getAttribute(
                    "_uri", URIAttribute.class);
            if (modelURI != null) {
                modelURI.setContainer(null);
            }
            
            // Mark contents as being inherited objects.  EAL 12/03
            // FIXME: Probably this needs to indicate the level
            // at which they are inherited... (depth)...
            _markContentsInherited(newEntity);
            
            // Set the class name as specified in this method call.
            // This overrides what Prototype does.  The reason we want to
            // do that is that Prototype uses the name of the object
            // that we cloned as the classname.  But this may not provide
            // enough information to instantiate the class.
            newEntity.setClassName(className);
            
            // Propagate to instances and derived classes.
            if (container != null) {
                Iterator heritage = heritageList.iterator();
                while (heritage.hasNext()) {
                    CompositeEntity inherited = (CompositeEntity)heritage.next();
                    ComponentEntity propagatedEntity
                            = (ComponentEntity)reference.instantiate(
                            inherited, entityName);
                    _markContentsInherited(propagatedEntity);
                    propagatedEntity.setInherited(true);
                    propagatedEntity.setClassName(className);
                    URIAttribute propagatedURI
                            = (URIAttribute)propagatedEntity.getAttribute(
                            "_uri", URIAttribute.class);
                    if (propagatedURI != null) {
                        propagatedURI.setContainer(null);
                    }
                }
            }

            return newEntity;
        }
    }
    
    /** Create an instance of the specified class name by finding a
     *  constructor that matches the specified arguments.  The specified
     *  class must be NamedObj or derived, or a ClassCastException will
     *  be thrown.  NOTE: This mechanism does not support instantiation
     *  of inner classes, since those take an additional argument (the
     *  first argument), which is the enclosing class. Static inner
     *  classes, however, work fine.
     *  This method marks the contents of what it creates as inherited objects,
     *  since they are defined in the Java code of the constructor.
     *  If we are currently propagating, then it also marks the new
     *  instance itself as an inherited object.
     *  @param newClass The class.
     *  @param arguments The constructor arguments.
     *  @exception Exception If no matching constructor is found, or if
     *   invoking the constructor triggers an exception.
     */
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
                // Mark the contents of the new entity as being inherited objects.
                _markContentsInherited(newEntity);
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

    /** Delete the entity after verifying that it is contained (deeply)
     *  by the current environment.
     *  @param entityName The relative or absolute name of the
     *   entity to delete.
     *  @return The deleted object.
     *  @throws Exception If there is no such entity or if the entity
     *   is defined in the class definition.
     */
    private NamedObj _deleteEntity(String entityName) throws Exception {
        ComponentEntity toDelete = _searchForEntity(entityName, _current);
        if (toDelete == null) {
            throw new XmlException("No such entity to delete: " + entityName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }

        // Ensure that inherited objects aren't changed.
        if (toDelete.isInherited()) {
            throw new IllegalActionException(toDelete,
                    "Cannot delete. This entity is part of the class definition.");
        }

        // NOTE: not enough to simply record the MoML of the deleted entity
        // as any links connected to its ports will also be deleted.
        // Construct the undo MoML as we go to ensure: (1) that
        // the undo occurs in the opposite order of all deletions, and
        // (2) that if a failure to delete occurs at any point, then
        // the current undo only represents as far as the failure got.
        StringBuffer undoMoML = new StringBuffer();

        // Propagate. The name might be absolute and have
        // nothing to do with the current context.  So
        // we look for its heritage, not the context's heritage.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        // Note that deletion and undo need to occur in the opposite
        // order, so we first create the undo in the order given
        // by the heritage list, then do the deletion in the
        // opposite order.
        try {
            Iterator heritage = toDelete.getHeritageList().iterator();
            // NOTE: Deletion needs to occur in the reverse order from
            // what appears in the heritage list. So first we construct
            // a reverse order list.
            List reverse = new LinkedList();
            while (heritage.hasNext()) {
                reverse.add(0, heritage.next());
            }
            heritage = reverse.iterator();
            while (heritage.hasNext()) {
                ComponentEntity inherited = (ComponentEntity)heritage.next();
                // Have to get this _before_ deleting.
                String toUndo = _getUndoForDeleteEntity(inherited);
                inherited.setContainer(null);
                // Put at the _start_ of the undo MoML, to ensure
                // reverse order from the deletion.
                undoMoML.insert(0, toUndo);
            }
            // Have to get this _before_ deleting.
            String toUndo = _getUndoForDeleteEntity(toDelete);
            toDelete.setContainer(null);
            undoMoML.insert(0, toUndo);
        } finally {
            if (_undoEnabled && _undoContext.isUndoable()) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
            }
        }
        return toDelete;
    }

    /** Delete the port after verifying that it is contained (deeply)
     *  by the current environment.
     *  @param portName The relative or absolute name of the
     *   port to delete.
     *  @param entityName Optional name of the entity that contains
     *   the port (or null to use the current context).
     *  @return The deleted object.
     *  @throws Exception If there is no such port or if the port
     *   is defined in the class definition.
     */
    private Port _deletePort(String portName, String entityName)
            throws Exception {
        Port toDelete = null;
        Entity portContainer = null;
        if (entityName == null) {
            toDelete = _searchForPort(portName);
            if (toDelete != null) {
                portContainer = (Entity)toDelete.getContainer();
            }
        } else {
            portContainer = _searchForEntity(entityName, _current);
            if (portContainer != null) {
                toDelete = portContainer.getPort(portName);
            }
        }
        if (toDelete == null) {
            throw new XmlException("No such port to delete: "
                    + portName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        if (portContainer == null) {
            throw new XmlException("No container for the port: "
                    + portName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }

        // Ensure that inherited objects aren't changed.
        if (toDelete.isInherited()) {
            throw new IllegalActionException(toDelete,
                    "Cannot delete. This port is part of the class definition.");
        }

        // Propagate and generate undo MoML.
        // NOTE: not enough to simply record the MoML of the deleted port
        // as any links connected to it will also be deleted
        // and derived ports will have to have similar undo MoML
        // so that connections get remade.
        // Construct the undo MoML as we go to ensure: (1) that
        // the undo occurs in the opposite order of all deletions, and
        // (2) that if a failure to delete occurs at any point, then
        // the current undo only represents as far as the failure got.
        StringBuffer undoMoML = new StringBuffer();

        // Propagate. The name might be absolute and have
        // nothing to do with the current context.  So
        // we look for its heritage, not the context's heritage.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        // Note that deletion and undo need to occur in the opposite
        // order.
        try {
            Iterator heritage = toDelete.getHeritageList().iterator();
            // NOTE: Deletion needs to occur in the reverse order from
            // what appears in the heritage list. So first we construct
            // a reverse order list.
            List reverse = new LinkedList();
            while (heritage.hasNext()) {
                reverse.add(0, heritage.next());
            }
            heritage = reverse.iterator();
            while (heritage.hasNext()) {
                Port inherited = (Port)heritage.next();
                // Create the undo MoML.
                // Have to get this _before_ deleting.
                // Put at the _start_ of the undo MoML, to ensure
                // reverse order from the deletion.
                // NOTE: This describes links to the
                // inherited port.  Amazingly, the order
                // seems to be exactly right so that links
                // that will propagate on undo are no longer
                // present. So it seems to generate exactly
                // the right undo to not end up with duplicate connections!
                String toUndo = _getUndoForDeletePort(inherited);
                inherited.setContainer(null);
                undoMoML.insert(0, toUndo);
            }
            // Have to get this _before_ deleting.
            String toUndo = _getUndoForDeletePort(toDelete);
            toDelete.setContainer(null);
            undoMoML.insert(0, toUndo);
        } finally {
            if (_undoEnabled && _undoContext.isUndoable()) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
            }
        }
        return toDelete;
    }

    /** Delete an attribute after verifying that it is contained (deeply)
     *  by the current environment.
     *  @param attributeName The relative or absolute name of the
     *   attribute to delete.
     *  @return The deleted object.
     *  @throws Exception If there is no such attribute or if the attribute
     *   is defined in the class definition.
     */
    private Attribute _deleteProperty(String attributeName) throws Exception {
        Attribute toDelete = _searchForAttribute(attributeName);
        if (toDelete == null) {
            throw new XmlException("No such property to delete: "
                    + attributeName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        // Ensure that inherited objects aren't changed.
         if (toDelete.isInherited()) {
            throw new IllegalActionException(toDelete,
                    "Cannot delete. This attribute is part of the class definition.");
        }
        
        // Propagate and generate undo MoML.
        // NOTE: not enough to simply record the MoML of the deleted attribute
        // as derived attributes may have overridden the values.
        // Construct the undo MoML as we go to ensure: (1) that
        // the undo occurs in the opposite order of all deletions, and
        // (2) that if a failure to delete occurs at any point, then
        // the current undo only represents as far as the failure got.
        StringBuffer undoMoML = new StringBuffer();

        // Propagate. The name might be absolute and have
        // nothing to do with the current context.  So
        // we look for its heritage, not the context's heritage.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        try {
            // NOTE: Deletion can occur in the the same order as
            // what appears in the heritage list.
            Iterator heritage = toDelete.getHeritageList().iterator();

            String toUndo = _getUndoForDeleteAttribute(toDelete);
            toDelete.setContainer(null);
            undoMoML.append(toUndo);

            while (heritage.hasNext()) {
                Attribute inherited = (Attribute)heritage.next();

                if (inherited.isModifiedHeritage()) {
                    toUndo = _getUndoForDeleteAttribute(inherited);
                    inherited.setContainer(null);
                    undoMoML.append(toUndo);
                } else {
                    // No need for undo code.
                    // Propagation will take care of it.
                    inherited.setContainer(null);
                }
            }
        } finally {
            if (_undoEnabled && _undoContext.isUndoable()) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
                // FIXME
                System.out.println("***********************\n" + undoMoML.toString() + "************************\n");
            }
        }
        return toDelete;
    }

    /** Delete the relation after verifying that it is contained (deeply)
     *  by the current environment.
     *  @param relationName The relative or absolute name of the
     *   relation to delete.
     *  @return The deleted object.
     *  @throws Exception If there is no such relation or if the relation
     *   is defined in the class definition.
     */
    private Relation _deleteRelation(String relationName) throws Exception {
        ComponentRelation toDelete = _searchForRelation(relationName);
        if (toDelete == null) {
            throw new XmlException("No such relation to delete: "
                    + relationName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }

        if (toDelete.isInherited()) {
            throw new IllegalActionException(toDelete,
                    "Cannot delete. This relation is part of the class definition.");
        }

        // Propagate and generate undo MoML.
        // NOTE: not enough to simply record the MoML of the deleted relation
        // as any links connected to it will also be deleted
        // and derived relations will have to have similar undo MoML
        // so that connections get remade.
        // Construct the undo MoML as we go to ensure: (1) that
        // the undo occurs in the opposite order of all deletions, and
        // (2) that if a failure to delete occurs at any point, then
        // the current undo only represents as far as the failure got.
        StringBuffer undoMoML = new StringBuffer();

        // Propagate. The name might be absolute and have
        // nothing to do with the current context.  So
        // we look for its heritage, not the context's heritage.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        // Note that deletion and undo need to occur in the opposite
        // order.
        try {
            Iterator heritage = toDelete.getHeritageList().iterator();
            // NOTE: Deletion needs to occur in the reverse order from
            // what appears in the heritage list. So first we construct
            // a reverse order list.
            List reverse = new LinkedList();
            while (heritage.hasNext()) {
                reverse.add(0, heritage.next());
            }
            heritage = reverse.iterator();
            while (heritage.hasNext()) {
                ComponentRelation inherited 
                        = (ComponentRelation)heritage.next();
                // Since the Relation can't be a
                // class itself (currently), it has derived objects
                // only if its container has derived objects.
                // Thus, we do not need to create undo MoML
                // for the derived objects. The undo MoML for
                // the principal relation will propagate when
                // executed.
                inherited.setContainer(null);
            }
            // Have to get this _before_ deleting.
            String toUndo = _getUndoForDeleteRelation(toDelete);
            toDelete.setContainer(null);
            undoMoML.insert(0, toUndo);
        } finally {
            if (_undoEnabled && _undoContext.isUndoable()) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
            }
        }
        return toDelete;
    }
    
    /** Use the specified parser to parse the file or URL,
     *  which contains MoML, using the specified base to find the URL.
     *  If the URL has been previously parsed by this application,
     *  then return the instance that was the result of the previous
     *  parse.
     *  If the URL cannot be found relative to this base, then it
     *  is searched for relative to the current working directory
     *  (if this is permitted with the current security restrictions),
     *  and then relative to the classpath.
     *  @param parser The parser to use.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param file The file or URL from which to read MoML.
     *  @param className The class name to assign if the file is
     *   parsed anew.
     *  @param source The source file to assign if the file is
     *   parsed anew, or null to not assign one.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    private NamedObj _findOrParse(
            MoMLParser parser,
            URL base, 
            String file, 
            String className, 
            String source)
            throws Exception {
        URL previousXmlFile = parser._xmlFile;
        parser._xmlFile = fileNameToURL(file, base);
        try {
            NamedObj toplevel 
                    = parser.parse(parser._xmlFile, parser._xmlFile);

            // NOTE: This might be a relative file reference, which
            // won't be of much use if a MoML file is moved.
            // But we don't want absolute file names wired in
            // either.  So we record the source as originally
            // specified, since it was sufficient to find this.
            // Note that the source may be null.
            toplevel.setSource(source);

            // Record the import to avoid repeated reading
            if (_imports == null) {
                _imports = new HashMap();
            }
            // NOTE: The index into the HashMap is the URL, not
            // its string representation. The URL class overrides
            // equal() so that it returns true if two URLs refer
            // to the same file, regardless of whether they have
            // the same string representation.
            // NOTE: The value in the HashMap is a weak reference
            // so that we don't keep all models ever created just
            // because of this _imports field. If there are no
            // references to the model other than the one in
            // _imports, it can be garbage collected.
            _imports.put(parser._xmlFile, new WeakReference(toplevel));

            return toplevel;
        } catch (CancelException ex) {
            // Parse operation cancelled.
            return null;
        } finally {
            parser._xmlFile = previousXmlFile;
        }
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

    /** Return the port corresponding to the specified port name in the
     *  specified composite entity.  If the port belongs directly to the
     *  composite entity, then the argument is a simple name.  If the
     *  port belongs to a component entity, then the name is the entity
     *  name, a period, and the port name.
     *  Throw an exception if there is no such port.
     *  The returned value is never null.
     *  @param portspec The relative port name.
     *  @param context The context in which to look for the port.
     *  @return The port.
     *  @throws XmlException If no such port is found.
     */
    private ComponentPort _getPort(String portspec, CompositeEntity context)
            throws XmlException {
        ComponentPort port = (ComponentPort)context.getPort(portspec);
        _checkForNull(port, "No port named \"" + portspec
                + "\" in " + context.getFullName());
        return (ComponentPort)port;
    }

    /** Return the MoML commands to undo deleting the specified attribute
     *  from the current context.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeleteAttribute(Attribute toDelete)
            throws IOException {

        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(
                UndoContext.moveContextStart(_current, toDelete));
        
        // Add in the description.
        moml.append(toDelete.exportMoML());
        
        // Finally move back to context if needed
        moml.append(UndoContext.moveContextEnd(_current, toDelete));
        
        return moml.toString();
    }

    /** Return the MoML commands to undo deleting the specified entity
     *  from the current context.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeleteEntity(ComponentEntity toDelete)
            throws IOException {

        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(
                UndoContext.moveContextStart(_current, toDelete));
        
        // Add in the description.
        moml.append(toDelete.exportMoML());
                
        // Now create the undo that will recreate any links
        // the are deleted as a side effect.
        // NOTE: cannot use the relationlist as returned as it is
        // unmodifiable and we need to add in the entity being deleted.
        ArrayList filter = new ArrayList(toDelete.linkedRelationList());
        filter.add(toDelete);
        
        // The parent container can do the filtering and generate the MoML.
        CompositeEntity container = (CompositeEntity)toDelete.getContainer();
        moml.append(container.exportLinks(0, filter));
        
        // Finally move back to context if needed
        moml.append(UndoContext.moveContextEnd(_current, toDelete));
        
        return moml.toString();
    }
    
    /** Return the MoML commands to undo deleting the specified port
     *  from the current context.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeletePort(Port toDelete)
            throws IOException {
                
        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(
                UndoContext.moveContextStart(_current, toDelete));
        
        // Add in the description.
        moml.append(toDelete.exportMoML());
                
        // Now create the undo that will recreate any links
        // the are deleted as a side effect.
        ArrayList filter = new ArrayList(toDelete.linkedRelationList());
        if (toDelete instanceof ComponentPort) {
            filter.addAll(((ComponentPort)toDelete).insideRelationList());
        }
        filter.add(toDelete);
        
        NamedObj container = toDelete.getContainer();
        
        // Generate the undo MoML for the inside links, if there are any.
        if (container instanceof CompositeEntity) {
            moml.append(((CompositeEntity)container).exportLinks(0, filter));
        }

        // Move back to context if needed.
        moml.append(UndoContext.moveContextEnd(_current, toDelete));

        // The undo MoML for the outside links is trickier.
        // We have to move up in the hierarchy, so we need to generate
        // an absolute context.
        if (container != null) {
            NamedObj containerContainer = container.getContainer();
            if (containerContainer instanceof CompositeEntity) {
                // Set the context to the container's container.
                moml.append(UndoContext.moveContextStart(_current, container));
                moml.append(((CompositeEntity)containerContainer)
                        .exportLinks(0, filter));
                moml.append(UndoContext.moveContextEnd(_current, container));
            }
        }
        return moml.toString();
    }

    /** Return the MoML commands to undo deleting the specified relation
     *  from the current context.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeleteRelation(ComponentRelation toDelete)
            throws IOException {
                
        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(
                UndoContext.moveContextStart(_current, toDelete));
        
        // Add in the description.
        moml.append(toDelete.exportMoML());
        
        // NOTE: cannot use the relationlist as returned as it is
        // unmodifiable and we need to add in the relation being deleted.
        ArrayList filter = new ArrayList(toDelete.linkedPortList());
        filter.add(toDelete);

        // The parent container can do the filtering and generate the
        // MoML.
        CompositeEntity container = (CompositeEntity)toDelete.getContainer();
        moml.append(container.exportLinks(0, filter));
        
        // Move back to context if needed.
        moml.append(UndoContext.moveContextEnd(_current, toDelete));

       return moml.toString();
    }
    
    /** Create a property and/or set its value.
     *  @param className The class name field, if present.
     *  @param propertyName The property name field.
     *  @param value The value, if present.
     *  @throws Exception If something goes wrong.
     */
    private void _handlePropertyElement(
            String className,
            String propertyName,
            final String value)
            throws Exception {
        // First handle special properties that are not translated
        // into Ptolemy II attributes.
        // Note that we have to push something on to the
        // stack so that we can pop it off later.
        // An xml version of the FSM ABP demo tickled this bug
        boolean isIOPort = (_current instanceof IOPort);
        if (propertyName.equals("multiport") && isIOPort) {
            // Special properties that affect the behaviour of a port
        
            // NOTE: UNDO: Consider refactoring these clauses
            // to remove the duplicate values
            // The previous value is needed to generate undo MoML
        
            IOPort currentIOPort = (IOPort)_current;
        
            // The mere presense of a named property "multiport"
            // makes the enclosing port a multiport, unless it
            // has value false.
        
            // Get the previous value to use when generating the
            // undo MoML
            boolean previousValue = currentIOPort.isMultiport();
            
            // Default for new value is true, unless it is explicitly false.
            boolean newValue = true;
            if (value != null && value.trim().toLowerCase().equals("false")) {
                newValue = false;
            }
            // If this object is an inherited object, then its I/O status
            // cannot be changed.  EAL 1/04.
            if (_current.isInherited()
                    && ((IOPort)_current).isMultiport() != newValue) {
                throw new IllegalActionException(_current,
                        "Cannot change whether this port is " +
                        "a multiport. That property is fixed by " +
                        "the class definition.");
            }                
            ((IOPort)_current).setMultiport(newValue);

            // Propagate.
            Iterator heritage = _current.getHeritageList().iterator();
            while (heritage.hasNext()) {
                IOPort inherited = (IOPort)heritage.next();
                inherited.setMultiport(newValue);
            }
        
            _pushContext();
            _current =  (Attribute)_current.getAttribute(propertyName);
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
            // Special properties that affect the behaviour of a port
        
            // NOTE: UNDO: Consider refactoring these clauses
            // to remove the duplicate values
            // The previous value is needed to generate undo MoML
        
            IOPort currentIOPort = (IOPort)_current;
        
            // Get the previous value to use when generating the
            // undo MoML
            boolean previousValue = currentIOPort.isOutput();
            
            // Default for new value is true, unless it is explicitly false.
            boolean newValue = true;
            if (value != null && value.trim().toLowerCase().equals("false")) {
                newValue = false;
            }
            // If this object is an inherited object, then its I/O status
            // cannot be changed.  EAL 1/04.
            if (_current.isInherited()
                    && ((IOPort)_current).isOutput() != newValue) {
                throw new IllegalActionException(_current,
                        "Cannot change whether this port is " +
                        "an output. That property is fixed by " +
                        "the class definition.");
            }
    
            ((IOPort)_current).setOutput(newValue);

            // Propagate.
            Iterator heritage = _current.getHeritageList().iterator();
            while (heritage.hasNext()) {
                IOPort inherited = (IOPort)heritage.next();
                inherited.setOutput(newValue);
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
            // Special properties that affect the behaviour of a port
        
            // NOTE: UNDO: Consider refactoring these clauses
            // to remove the duplicate values
            // The previous value is needed to generate undo MoML
        
            IOPort currentIOPort = (IOPort)_current;
        
            // Get the previous value to use when generating the
            // undo MoML
            boolean previousValue = currentIOPort.isInput();
            
            // Default for new value is true, unless it is explicitly false.
            boolean newValue = true;
            if (value != null && value.trim().toLowerCase().equals("false")) {
                newValue = false;
            }
            // If this object is an inherited object, then its I/O status
            // cannot be changed.  EAL 1/04.
            if (_current.isInherited()
                    && ((IOPort)_current).isInput() != newValue) {
                throw new IllegalActionException(_current,
                        "Cannot change whether this port is " +
                        "an input. That property is fixed by " +
                        "the class definition.");
            }
    
            ((IOPort)_current).setInput(newValue);

            // Propagate.
            Iterator heritage = _current.getHeritageList().iterator();
            while (heritage.hasNext()) {
                IOPort inherited = (IOPort)heritage.next();
                inherited.setInput(newValue);
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
            NamedObj property = null;
            if (_current != null) {
                property = (Attribute)_current
                        .getAttribute(propertyName);
            }
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
            boolean previouslyExisted = (property != null);
            
            // Even if the object previously existed, if the
            // class does not match, we may create a new object.
            boolean createdNew = false;
        
            // Also need the previous value, if any, to generate undo MoML.
            String oldClassName = null;
            String oldValue = null;
            if (previouslyExisted) {
                oldClassName = property.getClass().getName();
                if (property instanceof Settable) {
                    Settable settable = (Settable)property;
                    oldValue = settable.getExpression();
                }
            }
        
            if (!previouslyExisted || (newClass != null
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
                    
                    // An attribute cannot be a top-level element.
                    if (_current == null) {
                        throw new IllegalActionException(
                        "Attempt to create an attribute with no container: "
                        + propertyName);
                    }
                            
                    // First check that there will be no name collision
                    // when this is propagated. Note that we need to
                    // include all heritage objects, irrespective of whether
                    // they are locally changed.
                    List heritageList = _current.getHeritageList();
                    Iterator heritage = heritageList.iterator();
                    while (heritage.hasNext()) {
                        NamedObj inherited = (NamedObj)heritage.next();
                        if (inherited.getAttribute(propertyName) != null) {
                            throw new IllegalActionException(_current,
                            "Cannot create attribute because a subclass or instance "
                            + "contains an attribute with the same name: "
                            + inherited.getAttribute(propertyName).getFullName());
                        }
                    }
                    // Invoke the constructor.
                    Object[] arguments = new Object[2];
                    arguments[0] = _current;
                    arguments[1] = propertyName;
                    property = _createInstance(newClass, arguments);

                    // Check that the result is an instance of Attribute.
                    if (!(property instanceof Attribute)) {
                        // NOTE: Need to get rid of the object.
                        // Unfortunately, setContainer() is not defined,
                        // so we have to find the right class.
                        if (property instanceof ComponentEntity) {
                            ((ComponentEntity)property).setContainer(null);
                        } else if (property instanceof Port) {
                            ((Port)property).setContainer(null);
                        } else if (property instanceof ComponentRelation) {
                            ((ComponentRelation)property).setContainer(null);
                        }
                        throw new XmlException("Property is not an "
                                + "instance of Attribute. ",
                                _currentExternalEntity(),
                                _parser.getLineNumber(),
                                _parser.getColumnNumber());
                    }
        
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
                                    + property.getFullName()
                                    + " (instance of "
                                    + property.getClass().toString()
                                    + ")\n",
                                    _currentExternalEntity(),
                                    _parser.getLineNumber(),
                                    _parser.getColumnNumber());
                        }
                        Settable settable = (Settable)property;
                        // NOTE: Since this property is being now created,
                        // we do not have to worry about whether we are
                        // propagating a change that is shadowed by a
                        // change from class definition.
                        settable.setExpression(value);
                        _paramsToParse.add(property);
                    }
                    createdNew = true;
                    
                    // Propagate to instances and derived classes.
                    heritage = heritageList.iterator();
                    while (heritage.hasNext()) {
                        NamedObj inherited = (NamedObj)heritage.next();
                        // Invoke the constructor.
                        arguments[0] = inherited;
                        NamedObj newAttribute = _createInstance(newClass, arguments);
                        // It would be redundant to perform the same checks as above.
                        if (value != null) {
                            ((Settable)newAttribute).setExpression(value);
                            _paramsToParse.add(newAttribute);
                        }
                        newAttribute.setInherited(true);
                    }
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
                    
                    // Propagate, if appropriate.
                    Iterator propagated
                            = property.getShadowedHeritageList().iterator();
                    while (propagated.hasNext()) {
                        // The following cast is safe because property is known
                        // to be of type Attribute.
                        final Attribute attribute = (Attribute)propagated.next();
                        // Use a change request because we can't be sure
                        // that the change is applicable now.
                        ChangeRequest request = new ChangeRequest(
                                this, "Propagating change from "
                                + property.getFullName()) {
                            protected void _execute() throws IllegalActionException {
                                ((Settable)attribute).setExpression(value);
                                // Indicate that the current value is propagated in.
                                attribute.setModifiedHeritage(false);
                            }
                        };
                        request.addChangeListener(this);
                        attribute.requestChange(request);
                        // Need to make sure that the above attribute is validated
                        // after the change request is processed. That is taken
                        // care of in endDocument().
                        _paramsToParse.add(attribute);
                    }
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
    }
    
    /** Return true if the link between the specified port and
     *  relation is part of the class definition. It is part of the
     *  class definition if either the port and the relation are
     *  at the same level of hierarchy and are both inherited objects, or if
     *  the relation and the container of the port are both class
     *  elements. If the relation is null, then this return true
     *  if the port and its container are inherited objects.
     *  NOTE: This is not perfect, since a link could have been
     *  created between these elements in a subclass.
     *  @param context The context containing the link.
     *  @param port The port.
     *  @param relation The relation.
     *  @return True if the link is part of the class definition.
     */
    private boolean _isLinkInClass(NamedObj context, Port port, Relation relation) {
        boolean portIsInClass = (port.getContainer()
                == context)
                ? (port.isInherited() )
                : (((NamedObj)port.getContainer()).isInherited());
        return (portIsInClass && (relation == null || relation.isInherited()));
    }
    
    /** Return whether or not the given element name is undoable. NOTE: we need
     *  this method as the list of actions on namespaces and _current does not
     *  apply to elements such as "link"
     *  @param  elementName  Description of Parameter
     *  @return              Description of the Returned Value
     *  @since Ptolemy 2.1
     */
    private boolean _isUndoableElement(String elementName) {
        // The following serves as documentation of sorts for which
        // elements usage is undoable
        // NOTE: property appears first for reasons of efficency.
        if (elementName.equals("property")
                || elementName.equals("class")
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
    
    /** If the file with the specified name exists, parse it in
     *  the context of the specified instance. If it does not
     *  exist, do nothing.  If the file creates an attribute
     *  named "_icon", then that attribute is marked as a class
     *  element with the same depth as the context.
     *  @param fileName The file name.
     *  @param context The context into which to load the file.
     *  @return True if a file was found.
     *  @throws Exception If the file exists but cannot be read
     *   for some reason.
     */
    private boolean _loadFileInContext(String fileName, NamedObj context)
            throws Exception {
        URL xmlFile = _classLoader.getResource(fileName);
        if (xmlFile == null) {
            return false;
        }
        InputStream input = xmlFile.openStream();
        // Read the external file in the current context, but with
        // a new parser.  I'm not sure why the new parser is needed,
        // but the "input" element handler does the same thing.
        // NOTE: Should we keep the parser to re-use?
        MoMLParser newParser = new MoMLParser(_workspace, _classLoader);
        newParser.setContext(context);
        NamedObj result = newParser.parse(_base, input);
        
        // Have to mark the contents inherited objects, so that
        // the icon is not exported with the MoML export.
        // Unfortunately, we can't be sure what contents
        // were added to the context, so we just mark the
        // "_icon" attribute.
        // FIXME: Instead of doing this, which only work for
        // attributes named "_icon", we could set a private
        // variable on the parser to indicate that any new
        // objects it creates should be marked inherited objects.
        Attribute icon = context.getAttribute("_icon");
        if (icon != null) {
            _markContentsInherited(icon);
        }
        
        return true;
    }

    /** Look for a MoML file associated with the specified class
     *  name, and if it exists, parse it in the context of the
     *  specified instance. The file name is constructed from
     *  the class name by replacing periods with file separators
     *  ("/") and appending "Icon.xml".  So, for example, for
     *  the class name "ptolemy.actor.lib.Ramp", if there is a
     *  file "ptolemy/actor/lib/RampIcon.xml" in the classpath
     *  then that file be read.
     *  @param className The class name.
     *  @param context The context into which to load the file.
     *  @return True if a file was found.
     *  @throws Exception If the file exists but cannot be read
     *   for some reason.
     */
    private boolean _loadIconForClass(String className, NamedObj context)
            throws Exception {
        String fileName = className.replace('.', '/') + "Icon.xml";
        return _loadFileInContext(fileName, context);
    }

    /** Mark the contents as being inherited objects.
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
    private void _markContentsInherited(NamedObj object)
            throws IllegalActionException {
        // NOTE: Added as part of big change in class handling. EAL 12/03.
        // NOTE: It is necessary to mark objects deeply contained
        // so that we can disable deletion and name changes.
        // While we are at it, we add any
        // deeply contained Settables to the _paramsToParse list.
        Iterator objects = object.containedObjectsIterator();
        while (objects.hasNext()) {
            NamedObj containedObject = (NamedObj)objects.next();
            containedObject.setInherited(true);
            if (containedObject instanceof Settable) {
                _paramsToParse.add(containedObject);
            }
            _markContentsInherited(containedObject);
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
        _xmlFile = fileNameToURL(source, base);
        InputStream input = _xmlFile.openStream();
        try {
            NamedObj toplevel = parser.parse(_xmlFile, input);
            input.close();
            return toplevel;
        } catch (CancelException ex) {
            // Parse operation cancelled.
            return null;
        } finally {
            input.close();
            _xmlFile = null;
        }
    }

    /** Process a link command.
     *  @param portName The port name.
     *  @param relationName The relation name.
     *  @param insertAtSpec The place to insert.
     *  @param insertInsideAtSpec The place to insert inside.
     *  @throws XmlException
     *  @throws IllegalActionException
     */
    private void _processLink(
            String portName,
            String relationName,
            String insertAtSpec,
            String insertInsideAtSpec)
            throws XmlException, IllegalActionException {
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
        
        // NOTE: Added to ensure that inherited objects aren't changed.
        // EAL 1/04. We have to prohit adding links between class
        // elements because this operation cannot be undone, and
        // it will not be persistent.
        if (_isLinkInClass(context, port, relation)) {
            throw new IllegalActionException(port,
                    "Cannot link a port to a relation when both" +
                    " are part of the class definition.");
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
        } else if (insertInsideAtSpec != null) {
            port.insertInsideLink(insertInsideAt, relation);
        } else {
            port.link(relation);
        }
        
        // Propagate. Get the heritage list for the relation,
        // then use its container as the context in which to
        // find the port. NOTE: The relation can be null
        // (to insert an empty link in a multiport), so
        // we have two cases to consider.
        if (relation != null) {
            Iterator heritage = relation.getHeritageList().iterator();
            while (heritage.hasNext()) {
                ComponentRelation inheritedRelation
                        = (ComponentRelation)heritage.next();
                CompositeEntity inheritedContext
                        = (CompositeEntity)inheritedRelation.getContainer();
                ComponentPort inheritedPort
                        = _getPort(portName, inheritedContext);
                // NOTE: Duplicate the above logic exactly.
                if (insertAtSpec != null) {
                    inheritedPort.insertLink(insertAt, inheritedRelation);
                } else if (insertInsideAtSpec != null) {
                    inheritedPort.insertInsideLink(insertInsideAt, inheritedRelation);
                } else {
                    inheritedPort.link(inheritedRelation);
                }
            }
        } else {
            Iterator heritage = port.getHeritageList().iterator();
            while (heritage.hasNext()) {
                ComponentPort inheritedPort
                        = (ComponentPort)heritage.next();
                // NOTE: Duplicate the above logic exactly.
                if (insertAtSpec != null) {
                    inheritedPort.insertLink(insertAt, null);
                } else if (insertInsideAtSpec != null) {
                    inheritedPort.insertInsideLink(insertInsideAt, null);
                } else {
                    // This one probably shouldn't occur.
                    inheritedPort.link(null);
                }
            }  
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
            } else {
                // The relation name was given, see if the link was
                // added inside or outside
                if (port.numInsideLinks() != origNumInsideLinks) {
                    if (insertInsideAt == -1) {
                        insertInsideAt = port.numInsideLinks() - 1;
                    }
                    _undoContext.appendUndoMoML("<unlink port=\"" +
                            portName + "\" insideIndex=\"" +
                            insertInsideAt + "\" />\n");
                } else if (port.numLinks() != origNumOutsideLinks) {
                    if (insertAt == -1) {
                        insertAt = port.numLinks() - 1;
                    }
                    _undoContext.appendUndoMoML("<unlink port=\"" +
                            portName + "\" index=\"" +
                            insertAt + "\" />\n");
                } else {
                    // No change so do not need to generate any undo MoML
                }
            }
        }
    }

    /** Process an unlink request.
     *  @param portName The port name.
     *  @param relationName The relation name.
     *  @param indexSpec The index of the channel.
     *  @param insideIndexSpec The index of the inside channel.
     *  @throws XmlException If something goes wrong.
     *  @throws IllegalActionException If the link is part of a class definition.
     */
    private void _processUnlink(
            String portName,
            String relationName,
            String indexSpec,
            String insideIndexSpec)
            throws XmlException, IllegalActionException {

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
            
            // NOTE: Added to ensure that inherited objects aren't changed.
            // EAL 1/04.
            if (_isLinkInClass(context, port, relation)) {
                throw new IllegalActionException(port,
                        "Cannot unlink a port from a relation when both" +
                        " are part of the class definition.");
            }
        
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

            // Propagate. Get the heritage list for the relation,
            // then use its container as the context in which to
            // find the port.
            Iterator heritage = relation.getHeritageList().iterator();
            while (heritage.hasNext()) {
                ComponentRelation inheritedRelation
                        = (ComponentRelation)heritage.next();
                CompositeEntity inheritedContext
                        = (CompositeEntity)inheritedRelation.getContainer();
                ComponentPort inheritedPort
                        = _getPort(portName, inheritedContext);
                inheritedPort.unlink(inheritedRelation);
            }

            port.unlink(relation);
            
        } else if (indexSpec != null) {
            // index is given.
            int index = Integer.parseInt(indexSpec);
        
            // NOTE: Added to ensure that inherited objects aren't changed.
            // EAL 1/04.  Unfortunately, getting the relation is fairly
            // expensive.
            List relationList = port.linkedRelationList();
            Relation relation = (Relation)relationList.get(index);
            if (_isLinkInClass(context, port, relation)) {
                throw new IllegalActionException(port,
                        "Cannot unlink a port from a relation when both" +
                        " are part of the class definition.");
            }

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
            
            // Propagate.
            Iterator heritage = port.getHeritageList().iterator();
            while (heritage.hasNext()) {
                ComponentPort inheritedPort
                        = (ComponentPort)heritage.next();
                inheritedPort.unlink(index);
            }

            port.unlink(index);
            
        } else {
            // insideIndex is given.
            int index = Integer.parseInt(insideIndexSpec);
            
            // NOTE: Added to ensure that inherited objects aren't changed.
            // EAL 1/04.  Unfortunately, getting the relation is fairly
            // expensive.
            List relationList = port.insideRelationList();
            Relation relation = (Relation)relationList.get(index);
            if (_isLinkInClass(context, port, relation)) {
                throw new IllegalActionException(port,
                        "Cannot unlink a port from a relation when both" +
                        " are part of the class definition.");
            }

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
            // Propagate.
            Iterator heritage = port.getHeritageList().iterator();
            while (heritage.hasNext()) {
                ComponentPort inheritedPort
                        = (ComponentPort)heritage.next();
                inheritedPort.unlinkInside(index);
            }

            port.unlinkInside(index);
        }
        
        // Do not need to worry about child elements
        if (_undoEnabled && _undoContext.isUndoable()) {
            _undoContext.setChildrenUndoable(false);
        }
    }

    /** Push the current context.
     */
    private void _pushContext() {
        _containers.push(_current);
        _namespaces.push(_namespace);
        _namespaceTranslations.push(_namespaceTranslationTable);
    }

    /** Reset the undo information to give a fresh setup for the next
     *  incremental change. NOTE: this resets all the undo information except
     *  for the UndoStackAttribute which is associated with the model.
     */
    private void _resetUndo() {
        _undoContext = null;
        _undoContexts = new Stack();
        _undoEnabled = false;
    }

    /** Given a name that is either absolute (with a leading period)
     *  or relative to _current, find an attribute with that name.
     *  The attribute is required to
     *  be contained (deeply) by the current environment, or an XmlException
     *  will be thrown.
     *  @param name The name of the attribute, relative or absolute.
     *  @return The attribute.
     *  @throws XmlException If the attribute is not found.
     */
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

    /** Search for a class definition in the current context or
     *  anywhere above it in the hierarchy.
     *  If a instance of a MoML class with a matching name and
     *  source is found, then return it. Otherwise, return null.
     *  @param name The name of the class.
     *  @param source The source for the class.
     *  @return A class, if it exists.
     *  @throws Exception If a source is specified and it cannot
     *   be opened.
     */
    private ComponentEntity _searchForClassInContext(
            String name, String source) 
            throws Exception {
        ComponentEntity candidate = _searchForEntity(name, _current);
        // Search upwards in the hierarchy if necessary.
        NamedObj context = _current;
        // Make sure we get a real candidate, which is a
        // class definition. The second term in the if will
        // cause the search to continue up the hierarchy.
        // NOTE: There is still an oddness, in that
        // the class scoping results in a subtle (and
        // maybe incomprehensible) identification of
        // the base class, particularly when pasting
        // an instance or subclass into a new context.
        while ((candidate == null || !candidate.isClassDefinition())
                && context != null) {
            context = (NamedObj)context.getContainer();
            if (context instanceof CompositeEntity) {
                candidate = ((CompositeEntity)context).getEntity(name);
            }
        }
        if (candidate != null) {
            // Check that its source matches.
            String candidateSource = candidate.getSource();

            if (source == null && candidateSource == null) {
                return candidate;
            } else if (source != null && candidateSource != null) {
                // Have to convert to a URL to check whether the
                // same file is being specified.
                URL sourceURL = fileNameToURL(source, _base);
                URL candidateSourceURL = fileNameToURL(candidateSource, _base);
                if (sourceURL.equals(candidateSourceURL)) {
                    return candidate;
                }
            }
        }
        return null;
    }
    
    /** Given a name that is either absolute (with a leading period)
     *  or relative to the specified context, find a component entity
     *  with that name.  Return null if it is not found.
     *  @param name The name of the entity.
     *  @param context The context in which to search.
     *  @return An entity with the specified name, or null if none is found.
     *  @throws XmlException If the name refers to an entity in an
     *   innappropriate context or if the context is not an instance
     *   of CompositeEntity.
     */
    private ComponentEntity _searchForEntity(String name, NamedObj context)
            throws XmlException {

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
                    /* NOTE: This is too restrictive.
                     * With an absolute name, there is no
                     * reason to disallow setting the context.
                     * This is useful in particular when deleting
                     * ports to make sure undo works.  The undo
                     * code has to execute in a context that may
                     * be higher than that in which the port
                     * was deleted in order for the connections
                     * to be re-established.
                     */
                    /*
                    if (context != null
                            && context != _toplevel) {
                        throw new XmlException(
                                "Reference to an existing entity: "
                                + _toplevel.getFullName()
                                + " in an inappropriate context: "
                                + context.getFullName(),
                                _currentExternalEntity(),
                                _parser.getLineNumber(),
                                _parser.getColumnNumber());
                    }
                    */
                    return (ComponentEntity)_toplevel;
                } else {
                    if (name.length() > nextPeriod + 1) {
                        ComponentEntity result =
                            ((CompositeEntity)_toplevel).getEntity(
                                    name.substring(nextPeriod + 1));
                        if (result != null) {
                            /* NOTE: This is too restrictive.
                             * With an absolute name, there is no
                             * reason to disallow setting the context.
                             * This is useful in particular when deleting
                             * ports to make sure undo works.  The undo
                             * code has to execute in a context that may
                             * be higher than that in which the port
                             * was deleted in order for the connections
                             * to be re-established.
                             */
                            /*
                            if (context != null
                                    && !context.deepContains(result)) {
                                throw new XmlException(
                                        "Reference to an existing entity: "
                                        + result.getFullName()
                                        + " in an inappropriate context: "
                                        + context.getFullName(),
                                        _currentExternalEntity(),
                                        _parser.getLineNumber(),
                                        _parser.getColumnNumber());
                            }
                            */
                            return result;
                        }
                    }
                }
            }
            return null;
        } else {
            // Name is relative.
            if (context instanceof CompositeEntity) {
                ComponentEntity result
                        = ((CompositeEntity)context).getEntity(name);
                return result;
            }
            if (context == null) {
                // The name might be a top-level name, but without
                // the leading period.
                return _searchForEntity("." + name, context);
            }
            return null;
        }
    }

    /** Given a name that is either absolute (with a leading period)
     *  or relative to _current, find a port with that name.
     *  The port is required to
     *  be contained (deeply) by the current environment, or an XmlException
     *  will be thrown.
     *  @param name The name of the port.
     *  @return The port.
     *  @throws XmlException If the port is not found.
     */
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

    /** Given a name that is either absolute (with a leading period)
     *  or relative to _current, find a relation with that name.
     *  The relation is required to
     *  be contained (deeply) by the current environment, or an XmlException
     *  will be thrown.
     *  @param name The name of the relation.
     *  @return The relation.
     *  @throws XmlException If the relation is not found.
     */
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

    // List of weak references to
    // top-level entities imported via import element,
    // of MoML classes loaded in order to instantiate them,
    // and of models that have been parsed.
    private static Map _imports;
    
    // List of link or unlink requests.
    private List _linkRequests;
    
    // Stack of lists of link or unlink requests.
    private Stack _linkRequestStack = new Stack();

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
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Class that records a link request.
    private class LinkRequest {
        public LinkRequest(
                String portName,
                String relationName,
                String insertAtSpec,
                String insertInsideAtSpec) {
            _portName = portName;
            _relationName = relationName;
            _indexSpec = insertAtSpec;
            _insideIndexSpec = insertInsideAtSpec;
        }
        public void execute() throws IllegalActionException, XmlException {
            _processLink(_portName, _relationName,
                    _indexSpec, _insideIndexSpec);
        }
        public String toString() {
            return "link " + _portName + " to " + _relationName;
        }
        protected String _portName, _relationName,
                _indexSpec, _insideIndexSpec;
    }
    
    // Class that records a link request.
    private class UnlinkRequest extends LinkRequest {
        public UnlinkRequest(
                String portName,
                String relationName,
                String indexSpec,
                String insideIndexSpec) {
            super(portName, relationName, indexSpec, insideIndexSpec);
        }
        public void execute() throws IllegalActionException, XmlException {
            _processUnlink(_portName, _relationName,
                    _indexSpec, _insideIndexSpec);
        }
        public String toString() {
            return "unlink " + _portName + " from " + _relationName;
        }
    }
}
