/* A parser for MoML (modeling markup language)

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.moml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.undo.UndoActionsList;
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
import ptolemy.kernel.util.ScopeExtender;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Singleton;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.CancelException;
import ptolemy.util.ClassUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import com.microstar.xml.HandlerBase;
import com.microstar.xml.XmlException;
import com.microstar.xml.XmlParser;

///////////////////////////////////////////////////////////////////
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
 fragments without top-level entity or derived objects can be evaluated
 to modify the model.  You can specify the context in which the
 MoML to be interpreted by calling setContext().  However, the
 XML parser limits each fragment to one element.  So there always has
 to be one top-level element.  If you wish to evaluate a group of
 MoML elements in some context, set the context and then place your
 MoML elements within a group element, as follows:
 <pre>
 &lt;group&gt;
 ... sequence of MoML elements ...
 &lt;/group&gt;
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
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
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
        _ifElementStack = new Stack();
        _ifElementStack.push(Integer.valueOf(0));
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
        if (loader != null) {
            _classLoader = loader;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // FIXME: For all propagations, there
    // are two problems that we haven't dealt with.
    // 1) The changes are not atomic. If a failure occurs
    //    halfway through propagation, then the model is
    //    corrupted, and no further editing is reliably possible.
    //    For example, the derivation invariant may not be
    //    satisfied.
    // 2) If the original change was in a class definition
    //    and the objects to which the change propagates
    //    are in different models, then it is not
    //    safe to make changes in those models. They may
    //    be executing for example. One way to find out
    //    whether it is safe is to ask them
    //    isDeferringChangeRequests(), but if we use
    //    ChangeRequests to propagate we take a severe
    //    performance hit and make it essentially
    //    impossible to make the change atomic (see
    //    (1) above).

    /**  Add a MoMLFilter to the end of the list of MoMLFilters used
     *  to translate names.  If the list of MoMLFilters already contains
     *  the filter, then the filter is not added again.
     *  Note that this method is static.  The specified MoMLFilter
     *  will filter all MoML for any instances of this class.
     *
     *  <p>To avoid leaking memory, if addMoMLFilter(), addMoMLFilters()
     *  or setMoMLFilters()  is called, then call setMoMLFilters(null).</p>
     *
     *  <p>To avoid leaking memory, it is best if the MoMLParser is
     *  created in a separate Workspace and this method is not called, instead
     *  call {@link #addMoMLFilter(MoMLFilter, Workspace)}:</p>
     *  <pre>
     *  Workspace workspace = new Workspace("MyWorkspace");
     *  MoMLParser parser = new MoMLParser(workspace);
     *  MoMLFilter myFilter = new ptolemy.moml.filter.ClassChanges();
     *  MoMLParser.addMoMLFilter(myfilter, workspace);
     *  </pre>
     *
     *  @param filter  The MoMLFilter to add to the list of MoMLFilters.
     *  @see #addMoMLFilters(List filterList, Workspace workspace)
     *  @see #addMoMLFilters(List filterList)
     *  @see #addMoMLFilters(List filterList, Workspace workspace)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList)
     *  @see #setMoMLFilters(List filterList, Workspace workspace)
     */
    public static void addMoMLFilter(MoMLFilter filter) {
        MoMLParser.addMoMLFilter(filter, new Workspace("MoMLFilter"));
    }

    /**  Add a MoMLFilter to the end of the list of MoMLFilters used
     *  to translate names.  If the list of MoMLFilters already contains
     *  the filter, then the filter is not added again.
     *  Note that this method is static.  The specified MoMLFilter
     *  will filter all MoML for any instances of this class.
     *
     *  <p>To avoid leaking memory, if addMoMLFilter(), addMoMLFilters()
     *  or setMoMLFilters()  is called, then call setMoMLFilters(null).</p>
     *
     *  <p>To avoid leaking memory, it is best if the MoMLParser is
     *  created in a separate Workspace:</p>
     *  <pre>
     *  Workspace workspace = new Workspace("MyWorkspace");
     *  MoMLParser parser = new MoMLParser(workspace);
     *  MoMLFilter myFilter = new ptolemy.moml.filter.ClassChanges();
     *  MoMLParser.addMoMLFilter(myfilter, workspace);
     *  </pre>
     *
     *  @param filter  The MoMLFilter to add to the list of MoMLFilters.
     *  @param workspace MoMLFilters are passed a MoMLParser that is optionally
     *  used by a filter.  This parameter determines the Workspace in which
     *  that MoMLFilter is created.  To avoid memory leaks, typically the
     *  MoMLFilter that is used to parse a model is created in a new workspace.
     *  The MoMLFilters are static, so we need to pass in the Workspace from
     *  the top level MoMLFilter.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #addMoMLFilters(List filterList)
     *  @see #addMoMLFilters(List filterList, Workspace workspace)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList)
     *  @see #setMoMLFilters(List filterList, Workspace workspace)
     */
    public static void addMoMLFilter(MoMLFilter filter, Workspace workspace) {
        if (_filterList == null) {
            _filterList = new LinkedList();
        }
        if (_filterMoMLParser == null) {
            _filterMoMLParser = new MoMLParser(workspace);
        }
        if (!_filterList.contains(filter)) {
            _filterList.add(filter);
        }
    }

    /**  Add a List of MoMLFilters to the end of the list of MoMLFilters used
     *  to translate names.  The argument list of filters is added even if
     *  the current list already contains some of the filters in the argument
     *  list.
     *  Note that this method is static.  The specified MoMLFilter
     *  will filter all MoML for any instances of this class.
     *
     *  <p>To avoid leaking memory, if addMoMLFilter(), addMoMLFilters()
     *  or setMoMLFilters()  is called, then call setMoMLFilters(null).</p>
     *
     *  <p>To avoid leaking memory, it is best if the MoMLParser is
     *  created in a separate Workspace and this method is not called, instead
     *  call {@link #addMoMLFilters(List, Workspace)}:</p>
     *  <pre>
     *  Workspace workspace = new Workspace("MyWorkspace");
     *  MoMLParser parser = new MoMLParser(workspace);
     *  List myFilters = BackwardCompatibility.allFilters();
     *  MoMLParser.addMoMLFilters(myfilter, workspace);
     *  </pre>
     *
     *  @param filterList The list of MoMLFilters to add to the
     *  list of MoMLFilters to be used to translate names.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #addMoMLFilter(MoMLFilter filter, Workspace workspace)
     *  @see #addMoMLFilters(List filterList, Workspace workspace)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList)
     *  @see #setMoMLFilters(List filterList, Workspace workspace)
     */
    public static void addMoMLFilters(List filterList) {
        MoMLParser.addMoMLFilters(filterList, new Workspace("MoMLFilter"));
    }

    /**  Add a List of MoMLFilters to the end of the list of MoMLFilters used
     *  to translate names.  The argument list of filters is added even if
     *  the current list already contains some of the filters in the argument
     *  list.
     *  Note that this method is static.  The specified MoMLFilter
     *  will filter all MoML for any instances of this class.
     *
     *  <p>To avoid leaking memory, if addMoMLFilter(), addMoMLFilters()
     *  or setMoMLFilters()  is called, then call setMoMLFilters(null).</p>
     *
     *  <p>To avoid leaking memory, it is best if the MoMLParser is
     *  created in a separate Workspace:</p>
     *  <pre>
     *  Workspace workspace = new Workspace("MyWorkspace");
     *  MoMLParser parser = new MoMLParser(workspace);
     *  List myFiltersList = ...
     *  MoMLParser.addMoMLFilters(myFilterList, workspace);
     *  </pre>
     *
     *  @param filterList The list of MoMLFilters to add to the
     *  list of MoMLFilters to be used to translate names.
     *  @param workspace MoMLFilters are passed a MoMLParser that is optionally
     *  used by a filter.  This parameter determines the Workspace in which
     *  that MoMLFilter is created.  To avoid memory leaks, typically the
     *  MoMLFilter that is used to parse a model is created in a new workspace.
     *  The MoMLFilters are static, so we need to pass in the Workspace from
     *  the top level MoMLFilter.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #addMoMLFilter(MoMLFilter filter, Workspace workspace)
     *  @see #addMoMLFilters(List filterList)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList)
     *  @see #setMoMLFilters(List filterList, Workspace workspace)
     */
    public static void addMoMLFilters(List filterList, Workspace workspace) {
        if (_filterList == null) {
            _filterList = new LinkedList();
        }
        if (_filterMoMLParser == null) {
            _filterMoMLParser = new MoMLParser(workspace);
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
    @Override
    public void attribute(String name, String value, boolean specified)
            throws XmlException {
        if (name == null) {
            throw new XmlException("Attribute has no name",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        // If the current namespace is _AUTO_NAMESPACE, then look up the
        // translation for the name. If there is a translation, then
        // this name has previously been converted in this group.
        // Otherwise, this name has not been previously converted,
        // so we convert it now.  To accomplish the conversion, if
        // we are not at the top level, then the converted name
        // is the result of calling the container's uniqueName()
        // method, passing it the specified name.
        // The auto namespace is disabled while propagating, since
        // this would otherwise just result in chaotic names for
        // propagated changes.
        if (_namespace.equals(_AUTO_NAMESPACE)
                && _current != null
                && (name.equals("name") || name.equals("port")
                        || name.startsWith("relation") || name.equals("vertex") || name
                        .equals("pathTo"))) {
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

                String replacement = (String) _namespaceTranslationTable
                        .get(prefix);

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
                // If we have seen a createIfNecessary="true",
                // then skip if there already is an element with that name.
                String createIfNecessary = (String) _attributes
                        .get("createIfNecessary");
                // uniqueName() is not a good test for whether oldValue is
                // present because uniqueName() strips off any numeric suffix
                // before testing for existence.  Thus, if q1 exists, then
                // uniqueName(q1) will return q if there is no q.
                value = _current.uniqueName(oldValue);
                if (createIfNecessary != null
                        && createIfNecessary.equals("true")) {
                    // Check value against oldValue so that MoMLVariableChecker-2.3.4 passes.
                    // Check for oldValue in _current so that MoMLVariableChecker-3.2 passes.
                    if (!value.equals(oldValue)
                            || _current.getAttribute(oldValue) == null) {
                        // Needed to find Parameters that are up scope.
                        // FIXME: does this check ScopeExtendingAttributes?
                        // Should this use ModelScope.getScopedVariable()?
                        Attribute masterAttribute = null;
                        NamedObj searchContainer = _current;
                        while (searchContainer != null
                                && masterAttribute == null) {
                            masterAttribute = searchContainer
                                    .getAttribute(oldValue);
                            searchContainer = searchContainer.getContainer();
                        }
                        if (!value.equals(oldValue) || masterAttribute != null) {
                            // There already is something with that name, so we skip.
                            String currentElement = _xmlParser
                                    .getCurrentElement();

                            // FIXME: increment _skipElement or set it to 1?
                            _skipElement++;
                            _skipElementIsNew = true;
                            _skipElementName = currentElement;
                            return;
                        }
                    }
                }
                if (_namespaceTranslationTable == null) {
                    throw new InternalErrorException(
                            "_namespaceTranslationTable was null, which should not happen.");
                } else {
                    _namespaceTranslationTable.put(oldValue, value);
                }
            }
        } else {
            // If we have a non-default namespace, then prepend the namespace.
            // This needs to be done for every attribute whose value is a name.
            if (!_namespace.equals(_DEFAULT_NAMESPACE)
                    && !_namespace.equals(_AUTO_NAMESPACE)
                    && (name.equals("name") || name.equals("port")
                            || name.equals("relation") || name.equals("vertex") || name
                            .equals("pathTo"))) {
                value = _namespace + ":" + value;
            }
        }

        // Apply MoMLFilters here.
        // Filters can filter out graphical classes, or change
        // the names of ports to handle backward compatibility.
        if (_filterList != null) {
            // FIXME: There is a slight risk of xmlParser being null here.
            if (_xmlParser == null) {
                throw new InternalErrorException(
                        "_xmlParser is null? This can occur "
                                + " when parse(URL, String, Reader)"
                                + " calls itself because that method"
                                + " sets _xmlParser to null while exiting. "
                                + "name: " + name + " value: " + value);
            }
            String currentElement = _xmlParser.getCurrentElement();
            Iterator filters = _filterList.iterator();
            String filteredValue = value;

            while (filters.hasNext()) {
                MoMLFilter filter = (MoMLFilter) filters.next();
                filteredValue = filter.filterAttributeValue(_current,
                        currentElement, name, filteredValue, _xmlFileName,
                        _filterMoMLParser);
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
                _skipElementName = currentElement;

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
    @Override
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
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        if (_handler != null) {
            int reply = _handler.handleError(change.toString(), _toplevel,
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
    @Override
    public void charData(char[] chars, int offset, int length) {
        // If we haven't initialized _currentCharData, then we don't
        // care about character data, so we ignore it.
        if (_currentCharData != null) {
            _currentCharData.append(chars, offset, length);
        }
    }

    /** Clear the top objects list. The top objects list
     *  is a list of top-level objects that this parser has
     *  created.
     *  @see #topObjectsCreated()
     */
    public void clearTopObjectsList() {
        if (_topObjectsCreated == null) {
            _topObjectsCreated = new LinkedList();
        } else {
            _topObjectsCreated.clear();
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
    @Override
    public void doctypeDecl(String name, String publicID, String systemID)
            throws CancelException {
        if (publicID != null && !publicID.trim().equals("")
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
    @Override
    public void endDocument() throws Exception {
        // If link or delete requests are issued at the top level,
        // then they must be processed here.
        _processPendingRequests();

        // Push the undo entry.
        // Note that it is not correct here to check _undoEnabled because
        // undo might have been disabled part way through the current element.
        if (_undoContext != null && _undoContext.hasUndoMoML()) {
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

            UndoAction newEntry = new MoMLUndoEntry(context, undoMoML);
            UndoStackAttribute undoInfo = UndoStackAttribute
                    .getUndoInfo(context);

            // If we have additional undo actions that have to execute
            // in a different context, bundle those together with this one
            // before pushing on the undo stack.
            if (_undoForOverrides.size() > 0) {
                newEntry = new UndoActionsList(newEntry);
                for (UndoAction action : _undoForOverrides) {
                    ((UndoActionsList) newEntry).add(action);
                }
            }

            // If we are in the middle of processing an undo, then this will
            // go onto the redo stack.
            undoInfo.push(newEntry);

            // Clear up the various MoML variables.
            _resetUndo();
        }

        // See https://projects.ecoinformatics.org/ecoinfo/issues/6587: summarize missing actors
        if (_missingClasses != null) {
            StringBuffer warning = new StringBuffer();
            for (String missingClass : _missingClasses) {
                warning.append(missingClass + ", ");
            }
            // Get rid of the trailing comma and space.
            warning.delete(warning.length() - 2, warning.length());

            // Adding another dialog is annoying, so we print out the warning.
            System.err.println("Warning: Missing Classes: " + warning);

            // MessageHandler(String) is not selectable, so we use MessageHandler(String, Throwable).
            //MessageHandler.warning("Missing Classes", new Exception(warning.toString()));
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

        try {
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

            // Before evaluating parameters, expand all ScopeExtenders.
            // Scope extenders will create parameters that other parameters may depend
            // on, and these parameters were not seen during parsing.
            _expandScopeExtenders();

            // Force evaluation of parameters so that any listeners are notified.
            // This will also force evaluation of any parameter that this variable
            // depends on.
            Iterator parameters = _paramsToParse.iterator();

            // As an optimization, if there are multiple instances of
            // SharedParameter in the list that are shared, we only
            // validate the first of these. This prevents a square-law
            // increase in complexity, because each validation of an
            // instance of SharedParameter causes validation of all
            // its shared instances. EAL 9/10/06.
            HashSet parametersValidated = new HashSet();
            while (parameters.hasNext()) {
                Settable param = (Settable) parameters.next();

                if (parametersValidated.contains(param)) {
                    continue;
                }

                // NOTE: We used to catch exceptions here and issue
                // a warning only, but this has the side effect of blocking
                // the mechanism in PtolemyQuery that carefully prompts
                // the user for corrected parameter values.
                try {
                    param.validate();

                    // Also validate derived objects.
                    Iterator derivedParams = ((NamedObj) param)
                            .getDerivedList().iterator();

                    while (derivedParams.hasNext()) {
                        Settable derivedParam = (Settable) derivedParams.next();
                        derivedParam.validate();
                        parametersValidated.add(derivedParam);
                    }

                    if (param instanceof SharedParameter) {
                        parametersValidated.addAll(((SharedParameter) param)
                                .sharedParameterSet());
                    }
                } catch (Exception ex) {
                    if (_handler != null) {
                        int reply = _handler.handleError(
                                "<param name=\"" + param.getName()
                                + "\" value=\"" + param.getExpression()
                                + "\"/>", param.getContainer(), ex);

                        if (reply == ErrorHandler.CONTINUE) {
                            continue;
                        }
                    }

                    // No handler, or cancel button pushed.
                    throw ex;
                }
            }
        } finally {
            if (_handler != null) {
                _handler.enableErrorSkipping(false);
            }
        }
    }

    /** End an element. This method pops the current container from
     *  the stack, if appropriate, and also adds specialized properties
     *  to the container, such as <i>_doc</i>, if appropriate.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     *  @exception Exception If thrown while adding properties.
     */
    @Override
    public void endElement(String elementName) throws Exception {
        // Apply MoMLFilters here.
        // FIXME: Why is this done first?  Perhaps it should be
        // done last?
        if (_filterList != null) {
            Iterator filters = _filterList.iterator();

            while (filters.hasNext()) {
                MoMLFilter filter = (MoMLFilter) filters.next();
                filter.filterEndElement(_current, elementName,
                        _currentCharData, _xmlFileName, _filterMoMLParser);
            }
        }

        if (((Integer) _ifElementStack.peek()).intValue() > 1) {
            _ifElementStack
            .push(((Integer) _ifElementStack.pop()).intValue() - 1);
        } else if (_skipElement <= 0) {
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
                                    + _configureNesting
                                    + " which is <0, which indicates a nesting bug",
                                    _currentExternalEntity(), _getLineNumber(),
                                    _getColumnNumber());
                }
            } else if (elementName.equals("doc")) {
                // Count doc tags so that they can nest.
                _docNesting--;

                if (_docNesting < 0) {
                    throw new XmlException("Internal Error: _docNesting is "
                            + _docNesting
                            + " which is <0, which indicates a nesting bug",
                            _currentExternalEntity(), _getLineNumber(),
                            _getColumnNumber());
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

        if (_skipRendition) {
            if (elementName.equals("rendition")) {
                _skipRendition = false;
            }
        } else if (_skipElement > 0) {
            if (elementName.equals(_skipElementName)) {
                // Nested element name.  Have to count so we properly
                // close the skipping.
                _skipElement--;
            }
        } else if (((Integer) _ifElementStack.peek()).intValue() > 1) {
        } else if (elementName.equals("if")
                && ((Integer) _ifElementStack.peek()).intValue() == 1) {
            _ifElementStack.pop();
        } else if (elementName.equals("configure")) {
            try {
                Configurable castCurrent = (Configurable) _current;
                String previousSource = castCurrent.getConfigureSource();
                String previousText = castCurrent.getConfigureText();
                castCurrent.configure(_base, _configureSource,
                        _currentCharData.toString());

                // Propagate to derived classes and instances.
                try {
                    // This has the side effect of marking the value
                    // overridden.
                    _current.propagateValue();
                } catch (IllegalActionException ex) {
                    // Propagation failed. Restore previous value.
                    castCurrent.configure(_base, previousSource, previousText);
                    throw ex;
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
                Documentation previous = (Documentation) _current
                        .getAttribute(_currentDocName);
                String previousValue = null;

                if (previous != null) {
                    previousValue = previous.getValueAsString();
                }

                // Set the doc value only if it differs from the previous.
                // FIXME: Is this the right thing to do w.r.t. propagation?
                // In effect, this means that if the value is the same as
                // the previous, then this will revert to not being an
                // override when the model is next opened.
                // Cf. What is done with parameter values.
                if (!_currentCharData.toString().equals(previousValue)) {
                    if (previous != null) {
                        String newString = _currentCharData.toString();

                        // If the newString is an empty list, then
                        // this will have the side effect of deleting
                        // the doc element by calling setContainer(null)
                        // in a change request. Since this is done in
                        // a change request, it will be done after
                        // propagation, as it should be.
                        previous.setExpression(newString);

                        // Propagate to derived classes and instances.
                        // If the new string is empty, this will remove
                        // the doc tag from any derived object that has
                        // not overridden the value of the doc tag.
                        try {
                            // This has the side effect of marking the
                            // value overridden.
                            previous.propagateValue();
                        } catch (IllegalActionException ex) {
                            // Propagation failed. Restore previous value.
                            previous.setExpression(previousValue);
                            throw ex;
                        }
                    } else {
                        Documentation doc = new Documentation(_current,
                                _currentDocName);
                        doc.setValue(_currentCharData.toString());

                        // Propagate. This has the side effect of marking
                        // the object overridden from its class definition.
                        doc.propagateExistence();

                        // Propagate value. This has the side effect of marking
                        // the object overridden from its class definition.
                        doc.propagateValue();
                    }
                }

                if (_undoEnabled) {
                    _undoContext.appendUndoMoML("<doc name=\""
                            + _currentDocName + "\">");

                    if (previous != null) {
                        _undoContext.appendUndoMoML(previousValue);
                    }

                    _undoContext.appendUndoMoML("</doc>\n");
                }

                _currentDocName = null;
            } else if (elementName.equals("group")) {
                // Process link requests that have accumulated in
                // this element.
                _processPendingRequests();

                try {
                    _namespace = (String) _namespaces.pop();

                    _namespaceTranslationTable = (Map) _namespaceTranslations
                            .pop();
                } catch (EmptyStackException ex) {
                    _namespace = _DEFAULT_NAMESPACE;
                }

                try {
                    _linkRequests = (List) _linkRequestStack.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _linkRequests = null;
                }

                try {
                    _deleteRequests = (List) _deleteRequestStack.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _deleteRequests = null;
                }
            } else if (elementName.equals("class")
                    || elementName.equals("entity")
                    || elementName.equals("model")) {
                // Process link requests that have accumulated in
                // this element.
                _processPendingRequests();

                try {
                    _current = (NamedObj) _containers.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _current = null;

                }

                try {
                    _namespace = (String) _namespaces.pop();

                    _namespaceTranslationTable = (Map) _namespaceTranslations
                            .pop();
                } catch (EmptyStackException ex) {
                    _namespace = _DEFAULT_NAMESPACE;
                }

                try {
                    _linkRequests = (List) _linkRequestStack.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _linkRequests = null;
                }

                try {
                    _deleteRequests = (List) _deleteRequestStack.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _deleteRequests = null;
                }
            } else if (elementName.equals("property")
                    || elementName.equals("director")
                    || elementName.equals("port")
                    || elementName.equals("relation")
                    || elementName.equals("rendition")
                    || elementName.equals("vertex")) {
                try {
                    _current = (NamedObj) _containers.pop();
                } catch (EmptyStackException ex) {
                    // We are back at the top level.
                    _current = null;
                }

                try {
                    _namespace = (String) _namespaces.pop();

                    _namespaceTranslationTable = (Map) _namespaceTranslations
                            .pop();
                } catch (EmptyStackException ex) {
                    _namespace = _DEFAULT_NAMESPACE;
                }
            }
        }

        // Handle the undoable aspect, if undo is enabled.
        // FIXME: How should _skipElement and _undoEnable interact?
        // If we are skipping an element, are we sure that we want
        // to add it to the undoContext?
        String undoMoML = null;
        // Note that it is not correct here to check _undoEnabled because
        // undo might have been disabled part way through the current element.
        if (_undoContext != null && _undoContext.hasUndoMoML()) {
            // Get the result from this element, as we'll be pushing
            // it onto the stack of children MoML for the parent context
            undoMoML = _undoContext.generateUndoEntry();

            if (_undoDebug) {
                System.out.println("Completed element: " + elementName + "\n"
                        + _undoContext.getUndoMoML());
            }
        }
        // Have to pop even if undo is not enabled!
        // Otherwise, we don't restore undo at the next level up.
        try {
            // Reset the undo context to the parent.
            // NOTE: if this is the top context, then doing a pop here
            // will cause the EmptyStackException
            _undoContext = (UndoContext) _undoContexts.pop();
            _undoEnabled = _undoContext.isUndoable();
        } catch (EmptyStackException ex) {
            // At the top level. The current _undoContext has the undo
            // that we want to preserve.
            if (_undoDebug) {
                System.out.println("Reached top level of undo "
                        + "context stack");
            }
        }
        // Push the child's undo MoML on the stack of child
        // undo entries.
        if (undoMoML != null) {
            _undoContext.pushUndoEntry(undoMoML);
        }
    }

    /** Handle the end of an external entity.  This pops the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemID The URI for the external entity.
     */
    @Override
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
    @Override
    public void error(String message, String systemID, int line, int column)
            throws XmlException {
        String currentExternalEntity = "";

        try {
            // Error message methods should be very careful to handle
            // exceptions while trying to provide the user with information
            currentExternalEntity = _currentExternalEntity();

            // Restore the status of change requests.
            // Execute any change requests that might have been queued
            // as a consequence of this change request.
            if (_toplevel != null) {
                // Set the top level back to the default
                // found in startDocument.
                _toplevel.setDeferringChangeRequests(_previousDeferStatus);
                _toplevel.executeChangeRequests();
            }
        } catch (Throwable throwable) {
            // Ignore any exceptions here.
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
    public URL fileNameToURL(String source, URL base) throws Exception {
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
                    && protocol.trim().toLowerCase(Locale.getDefault())
                    .equals("http")) {
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
                    String resultBase = result.toString().substring(0,
                            result.toString().lastIndexOf("/"));

                    if (_base == null
                            || !resultBase.startsWith(_base.toString())) {
                        MessageHandler.warning("Security concern:\n"
                                + "About to look for MoML from the "
                                + "net at address:\n" + result.toExternalForm()
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
            result = _classLoader.getResource(source);

            if (result != null) {
                input = result.openStream();
            } else {
                errorMessage
                .append("-- XML file not found relative to classpath.\n");

                // Failed to open relative to the classpath.
                // Try relative to the current working directory.
                // NOTE: This is last because it will fail with a
                // security exception in applets.
                String cwd = StringUtilities.getProperty("user.dir");
                try {
                    base = new File(cwd).toURI().toURL();
                    result = new URL(base, source);
                    input = result.openStream();
                } catch (Throwable throwable) {
                    errorMessage.append("-- " + cwd + File.separator + source
                            + "\n" + throwable.getMessage() + "\n");
                }
            }
        }

        if (input == null) {
            throw new XmlException(errorMessage.toString(),
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
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
     *  @see #setErrorHandler(ErrorHandler)
     */
    public static ErrorHandler getErrorHandler() {
        return _handler;
    }

    /** Get the icon loader for all MoMLParsers.
     *  @return The IconLoader for all MoMLParsers.
     *  @see #setIconLoader(IconLoader)
     */
    public static IconLoader getIconLoader() {
        return _iconLoader;
    }

    /** Get the List of MoMLFilters used to translate names.
     *  Note that this method is static.  The returned MoMLFilters
     *  will filter all MoML for any instances of this class.
     *  @return The MoMLFilters currently filtering.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #addMoMLFilter(MoMLFilter filter, Workspace workspace)
     *  @see #setMoMLFilters(List filterList)
     *  @see #setMoMLFilters(List filterList, Workspace workspace)
     */
    public static List getMoMLFilters() {
        return _filterList;
    }

    /** Get the top-level entity associated with this parser, or null if none.
     *  @return The top-level associated with this parser.
     *  @see #setToplevel(NamedObj)
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
     *  return the model that was previously parsed.  Note that this
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
     *  @see #purgeAllModelRecords()
     */
    public NamedObj parse(URL base, URL input) throws Exception {
        _setXmlFile(input);

        try {
            if (_imports == null) {
                _imports = new HashMap();
            } else {
                WeakReference reference = (WeakReference) _imports.get(input);
                NamedObj previous = null;

                if (reference != null) {
                    previous = (NamedObj) reference.get();

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

            InputStream inputStream = null;

            try {
                try {
                    inputStream = input.openStream();
                } catch (Exception ex) {
                    // Try opening it up as a Jar URL.
                    // vergilPtiny.jnlp needs this.
                    URL jarURL = ClassUtilities.jarURLEntryResource(input
                            .toExternalForm());

                    if (jarURL != null) {
                        inputStream = jarURL.openStream();
                    } else {
                        throw ex;
                    }
                }
                // Pass the input URL in case we need it for an error message.
                // See test MoMLParser-31.1
                NamedObj result = parse(base, input.toString(), inputStream);
                // Note that the parse()  call above can parse a model that
                // call parseMoML() in the expression language which calls
                // resetAll(), which sets _imports to null.
                if (_imports == null) {
                    _imports = new HashMap();
                }
                _imports.put(input, new WeakReference(result));
                return result;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } finally {
            _setXmlFile(null);
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
     *  @deprecated Use
     *  {@link #parse(URL base, String systemID, InputStream input)}
     *  for better error messages that include the name of the file being
     *  read.
     */
    @Deprecated
    public NamedObj parse(URL base, InputStream input) throws Exception {
        return parse(base, new InputStreamReader(input));
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  This method uses parse(URL, Reader).  Note that this
     *  bypasses the mechanism of parse(URL, URL) that returns
     *  a previously parsed model. This method will always re-parse
     *  using data from the stream.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param systemID The URI of the document.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model, or
     *   null if the file is not recognized as a MoML file.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, String systemID, InputStream input)
            throws Exception {
        return parse(base, systemID, new InputStreamReader(input));
    }

    /** Parse the given stream, using the specified url as the base
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
     *  @deprecated Use {@link #parse(URL base, String systemID, Reader reader)}
     *  for better error messages that include the name of the file being
     *  read.
     */
    @Deprecated
    public NamedObj parse(URL base, Reader reader) throws Exception {
        return parse(base, null, reader);
    }

    /** Parse the given stream, using the specified url as the base
     *  The reader is wrapped in a BufferedReader before being used.
     *  Note that this
     *  bypasses the mechanism of parse(URL, URL) that returns
     *  a previously parsed model. This method will always re-parse
     *  using data from the stream.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param systemID The URI of the document.
     *  @param reader The reader from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model, or
     *   null if the file is not recognized as a MoML file.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, String systemID, Reader reader)
            throws Exception {
        _base = base;

        Reader buffered = new BufferedReader(reader);

        try {
            // We allocate a new XmlParser each time so as to avoid leaks.
            _xmlParser = new XmlParser();
            _xmlParser.setHandler(this);
            if (base == null) {
                _xmlParser.parse(systemID, null, buffered);
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
                    _setXmlFile(new URL(base.toExternalForm()));
                }

                try {
                    _xmlParser.parse(base.toExternalForm(), null, buffered);
                } finally {
                    if (xmlFileWasNull) {
                        _setXmlFile(null);
                    }
                }
            }
        } catch (CancelException ex) {
            // Parse operation cancelled.
            return null;
        } catch (Exception ex) {
            // If you change this code, try running
            // ptolemy.moml.test.MoMLParserLeak with the heap profiler
            // and look for leaks.
            if (_toplevel != null && _toplevel instanceof ComponentEntity) {
                try {
                    ((ComponentEntity) _toplevel).setContainer(null);
                } catch (Throwable throwable2) {
                    // Ignore.  setContainer(null) might throw an exception
                    // if there are deferrables, but we don't want to hide
                    // the original exception.
                    // This problem comes up with tests in
                    // actor/gui/test/UserActorLibrary.tcl.
                }
                // Since the container is probably already null, then
                // the setContainer(null) call probably did not do anything.
                // so, we remove the object from the workspace so it
                // can get gc'd.
                // FIXME: perhaps we should do more of what
                // ComponentEntity.setContainer() does and remove the ports?
                try {
                    _workspace.getWriteAccess();
                    _workspace.remove(_toplevel);
                } finally {
                    _workspace.doneWriting();
                }
                _toplevel = null;
            }

            _paramsToParse.clear();
            if (_scopeExtenders != null) {
                _scopeExtenders.clear();
            }
            reset();
            if (base != null) {
                purgeModelRecord(base);
            }
            throw ex;
        } finally {
            // Avoid memory leaks
            _xmlParser = null;
            buffered.close();
        }

        if (_toplevel == null) {
            // If we try to read a HSIF file but Ptolemy is not properly
            // configured, then we may end up here.
            throw new Exception(
                    "Toplevel was null?  Perhaps the xml does not contain "
                            + "a Ptolemy model?\n base ='" + base
                            + "',\n reader = '" + reader + "'");
        }

        // Add a parser attribute to the toplevel to indicate a parser
        // responsible for handling changes, unless there already is a
        // parser, in which case we just set the parser to this one.
        MoMLParser parser = ParserAttribute.getParser(_toplevel);

        if (parser != this) {
            // Force the parser to be this one.
            ParserAttribute parserAttribute = (ParserAttribute) _toplevel
                    .getAttribute("_parser", ParserAttribute.class);

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
        // Use the current working directory as a base.
        String cwd = StringUtilities.getProperty("user.dir");

        URL base = new File(cwd).toURI().toURL();

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
     *  @see #purgeAllModelRecords()
     */
    public NamedObj parseFile(String filename) throws Exception {
        // Use the current working directory as a base.
        String cwd = StringUtilities.getProperty("user.dir");

        URL base = new File(cwd).toURI().toURL();

        // Java's I/O is so lame that it can't find files in the current
        // working directory...
        File file = new File(filename);
        if (!file.exists()) {
            file = new File(new File(cwd), filename);
        }
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file \"" + filename
                    + "\", also tried \"" + file + "\"");
        }
        return parse(base, file.toURI().toURL());
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
    @Override
    public void processingInstruction(String target, String data) {
        if (_currentCharData != null) {
            _currentCharData.append("<?");
            _currentCharData.append(target);
            _currentCharData.append(" ");
            _currentCharData.append(data);
            _currentCharData.append("?>");
        }
    }

    /** Purge all records of models opened. This is here
     *  for testing only.
     *  @see #purgeModelRecord(URL)
     *  @see #resetAll()
     */
    public static void purgeAllModelRecords() {
        _imports = null;
    }

    /** Purge any record of a model opened from the specified
     *  URL.  The record will not be purged if the model is
     *  a class definition that has child instances.
     *  Note that you may also need to call {@link #reset()} so
     *  that the _toplevel is reset on any parser.
     *  @param url The URL.
     *  @see #parse(URL, URL)
     *  @see #purgeAllModelRecords()
     */
    public static void purgeModelRecord(URL url) {
        if (_imports != null && url != null) {
            // Don't do this if the url is of a class
            // and there are instances!!!!
            WeakReference reference = (WeakReference) _imports.get(url);
            if (reference != null) {
                Object modelToPurge = reference.get();
                // Check to see whether the model to
                // purge is a class with instances.
                if (modelToPurge instanceof Instantiable) {
                    boolean keepTheModel = false;
                    List children = ((Instantiable) modelToPurge).getChildren();
                    if (children != null) {
                        Iterator childrenIterator = children.iterator();
                        while (childrenIterator.hasNext()) {
                            WeakReference child = (WeakReference) childrenIterator
                                    .next();
                            if (child != null && child.get() != null) {
                                keepTheModel = true;
                                break;
                            }
                        }
                    }
                    if (keepTheModel) {
                        return;
                    }
                }
            }

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
     *  <p> Note that you may also need to call {@link #reset()} so
     *  that the _toplevel is reset.
     *
     *  @param filename The file name from which to read MoML.
     *  @exception MalformedURLException If the file name cannot be converted to a URL.
     *  @exception SecurityException If the user.dir system property is
     *   not available.
     *  @see #parse(URL, String)
     *  @see #purgeAllModelRecords()
     */
    public static void purgeModelRecord(String filename)
            throws MalformedURLException {
        // Use the current working directory as a base.
        String cwd = StringUtilities.getProperty("user.dir");

        // Java's I/O is so lame that it can't find files in the current
        // working directory...
        File file = new File(new File(cwd), filename);
        purgeModelRecord(file.toURI().toURL());
    }

    /** Reset the MoML parser.
     *  Note that this method does not purge records of the models
     *  that have been parsed.  To completely reset the MoMLParser,
     *  see {@link #resetAll()}.
     *  @see #resetAll()
     *  @see #purgeModelRecord(String)
     *  @see #purgeAllModelRecords()
     */
    public void reset() {
        _attributes = new HashMap();
        _configureNesting = 0;
        _containers = new Stack();
        _linkRequests = null;
        _linkRequestStack = new Stack();
        _deleteRequests = null;
        _deleteRequestStack = new Stack();
        _current = null;
        _docNesting = 0;
        _externalEntities = new Stack();
        _modified = false;
        _namespace = _DEFAULT_NAMESPACE;
        _namespaces = new Stack();
        _namespaceTranslations = new Stack();
        _skipRendition = false;
        _skipElementIsNew = false;
        _ifElementStack.clear();
        _ifElementStack.add(Integer.valueOf(0));
        _skipElement = 0;
        _toplevel = null;

        // Reset undo specific members
        _resetUndo();
    }

    /** Reset the MoML parser, forgetting about any previously parsed
     *  models.  This method differs from {@link #reset()} in that
     *  this method does as complete a reset of the MoMLParser
     *  as possible.  Note that the static MoMLFilters are not reset,
     *  but the MoMLParser optionally used by the filters is reset.
     *  @see #purgeModelRecord(String)
     *  @see #purgeAllModelRecords()
     */
    public void resetAll() {
        purgeAllModelRecords();
        reset();
        _workspace = new Workspace();
        if (_filterMoMLParser != null) {
            MoMLParser.purgeAllModelRecords();
            _filterMoMLParser.reset();
            _filterMoMLParser = new MoMLParser(_workspace);
        }
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
    @Override
    public Object resolveEntity(String publicID, String systemID) {
        if (publicID != null && publicID.equals(MoML_PUBLIC_ID_1)) {
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
     *  @exception Exception If thrown while searching for the class
     */
    public ComponentEntity searchForClass(String name, String source)
            throws Exception {
        if (_imports != null && source != null) {
            WeakReference reference = (WeakReference) _imports.get(source);
            Object possibleCandidate = null;

            if (reference != null) {
                possibleCandidate = reference.get();

                if (possibleCandidate == null) {
                    _imports.remove(source);
                }
            }

            if (possibleCandidate instanceof ComponentEntity) {
                ComponentEntity candidate = (ComponentEntity) possibleCandidate;

                // Check that the candidate is a class.
                if (candidate.isClassDefinition()) {
                    // Check that the class name matches.
                    // Only the last part, after any periods has to match.
                    String realClassName = name;
                    int lastPeriod = name.lastIndexOf(".");

                    if (lastPeriod >= 0 && name.length() > lastPeriod + 1) {
                        realClassName = name.substring(lastPeriod + 1);
                    }

                    String candidateClassName = candidate.getClassName();
                    lastPeriod = candidateClassName.lastIndexOf(".");

                    if (lastPeriod >= 0
                            && candidateClassName.length() > lastPeriod + 1) {
                        candidateClassName = candidateClassName
                                .substring(lastPeriod + 1);
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
     *  <p>
     *  Callers should be careful about calling this method and resetting
     *  the modified flag to false when parsing moml that has been modified
     *  by the backward compatibility filter.
     *  It is safer to do something like:
     *  <pre>
     *   boolean modified = isModified();
     *   MoMLParser newParser = new MoMLParser(...);
     *   newParser.setContext(context);
     *   setModified(modified);
     *  </pre>
     *  @param context The context for parsing.
     */
    public void setContext(NamedObj context) {
        reset();
        _toplevel = context.toplevel();
        _current = context;
        _originalContext = context;
    }

    /** Set the error handler to handle parsing errors.
     *  Note that this method is static. The specified error handler
     *  will handle all errors for any instance of this class.
     *  @param handler The ErrorHandler to call.
     *  @see #getErrorHandler()
     */
    public static void setErrorHandler(ErrorHandler handler) {
        _handler = handler;
    }

    /** Set the icon loader for all MoMLParsers.
     *  @param loader The IconLoader for all MoMLParsers.
     *  @see #getIconLoader()
     */
    public static void setIconLoader(IconLoader loader) {
        _iconLoader = loader;
    }

    /**  Set the list of MoMLFilters used to translate names.
     *  Note that this method is static.  The specified MoMLFilters
     *  will filter all MoML for any instances of this class.
     *
     *  <p>To avoid leaking memory, if addMoMLFilter(), addMoMLFilters()
     *  or setMoMLFilters()  is called, then call setMoMLFilters(null).</p>
     *
     *  <p>To avoid leaking memory, it is best if the MoMLParser is
     *  created in a separate Workspace and this method is not called, instead
     *  call {@link #setMoMLFilters(List, Workspace)}:</p>
     *  <pre>
     *  Workspace workspace = new Workspace("MyWorkspace");
     *  MoMLParser parser = new MoMLParser(workspace);
     *  List myFilters = BackwardCompatibility.allFilters();
     *  MoMLParser.setMoMLFilters(myFilters, workspace);
     *  </pre>
     *
     *  @param filterList The List of MoMLFilters.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #addMoMLFilter(MoMLFilter filter, Workspace workspace)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList, Workspace workspace)
     */
    public static void setMoMLFilters(List filterList) {
        MoMLParser.setMoMLFilters(filterList, new Workspace("MoMLFilter"));
    }

    /**  Set the list of MoMLFilters used to translate names.
     *  Note that this method is static.  The specified MoMLFilters
     *  will filter all MoML for any instances of this class.
     *
     *  <p>To avoid leaking memory, if addMoMLFilter(), addMoMLFilters()
     *  or setMoMLFilters()  is called, then call setMoMLFilters(null).</p>
     *
     *  <p>To avoid leaking memory, it is best if the MoMLParser is
     *  created in a separate Workspace:</p>
     *  <pre>
     *  Workspace workspace = new Workspace("MyWorkspace");
     *  MoMLParser parser = new MoMLParser(workspace);
     *  List myFilters = BackwardCompatibility.allFilters();
     *  MoMLParser.setMoMLFilters(myFilters, workspace);
     *  </pre>
     *
     *  @param filterList The List of MoMLFilters.
     *  @param workspace MoMLFilters are passed a MoMLParser that is optionally
     *  used by a filter.  This parameter determines the Workspace in which
     *  that MoMLFilter is created.  To avoid memory leaks, typically the
     *  MoMLFilter that is used to parse a model is created in a new workspace.
     *  The MoMLFilters are static, so we need to pass in the Workspace from
     *  the top level MoMLFilter.
     *  @see #addMoMLFilter(MoMLFilter filter)
     *  @see #addMoMLFilter(MoMLFilter filter, Workspace workspace)
     *  @see #getMoMLFilters()
     *  @see #setMoMLFilters(List filterList)
     */
    public static void setMoMLFilters(List filterList, Workspace workspace) {
        _filterList = filterList;
        if (_filterList == null) {
            _filterMoMLParser = null;
        } else {
            if (_filterMoMLParser == null) {
                // FIXME: it seems like the MoMLParser should be created
                // in a particular Workspace, not just the default workspace
                // so that we can unload the Workspace?  However, since
                // the filters are static, we are probably ok.
                _filterMoMLParser = new MoMLParser(workspace);
            }
        }
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
        //if (modified == true) (new Exception()).printStackTrace();
        _modified = modified;
    }

    /** Set the top-level entity.  This can be used to associate this
     *  parser with a pre-existing model, which can then be modified
     *  via incremental parsing.  This calls reset().
     *  @param toplevel The top-level to associate with this parser.
     *  @see #getToplevel()
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
    @Override
    public void startDocument() {
        _paramsToParse.clear();
        _missingClasses = null;
        if (_scopeExtenders != null) {
            _scopeExtenders.clear();
        }
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

        _linkRequests = null;
        _deleteRequests = null;
        _linkRequestStack.clear();
        _deleteRequestStack.clear();
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
    @Override
    public void startElement(String elementName) throws XmlException {
        boolean pushedLinkRequests = false;
        boolean pushedDeleteRequests = false;
        boolean pushedUndoContexts = false;
        boolean exceptionThrown = false;
        _namespacesPushed = false;

        try {
            if (((Integer) _ifElementStack.peek()).intValue() > 1) {
                _ifElementStack.push(((Integer) _ifElementStack.pop())
                        .intValue() + 1);
            } else if (_skipElement <= 0) {
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

            _undoEnabled = _undoEnabled && _isUndoableElement(elementName);
            if (_undoContext != null) {
                _undoContexts.push(_undoContext);
                pushedUndoContexts = true;
                _undoEnabled = _undoEnabled
                        && _undoContext.hasUndoableChildren();
            }

            // Create a new current context
            _undoContext = new UndoContext(_undoEnabled);

            if (_undoDebug) {
                System.out.println("Current start element: " + elementName);
            }

            if (_skipElement > 0
                    || ((Integer) _ifElementStack.peek()).intValue() > 1) {
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
                String className = (String) _attributes.get("extends");
                String entityName = (String) _attributes.get("name");
                String source = (String) _attributes.get("source");

                _checkForNull(entityName, "No name for element \"class\"");

                // For undo purposes need to know if the entity existed
                // already
                Entity entity = _searchForEntity(entityName, _current);
                boolean existedAlready = entity != null;

                if (!existedAlready) {
                    NamedObj candidate = _createEntity(className, entityName,
                            source, true);

                    if (candidate instanceof Entity) {
                        entity = (Entity) candidate;
                    } else {
                        throw new IllegalActionException(_current,
                                "Attempt to create a class named " + entityName
                                + " from a class that "
                                + "is not a subclass of Entity: "
                                + className);
                    }
                }

                // NOTE: The entity may be at the top level, in
                // which case _deleteRequests is null.
                if (_deleteRequests != null) {
                    _deleteRequestStack.push(_deleteRequests);
                    pushedDeleteRequests = true;
                }

                _deleteRequests = new LinkedList();

                // NOTE: The entity may be at the top level, in
                // which case _linkRequests is null.
                if (_linkRequests != null) {
                    _linkRequestStack.push(_linkRequests);
                    pushedLinkRequests = true;
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
                        URIAttribute attribute = new URIAttribute(_toplevel,
                                "_uri");
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

                if (_undoEnabled) {
                    // Handle the undo aspect.
                    if (existedAlready) {
                        if (!converted) {
                            _undoContext.appendUndoMoML("<class name=\""
                                    + entityName + "\" >\n");

                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</class>\n");
                        } else {
                            // Converting from entity to class, so reverse this.
                            _undoContext.appendUndoMoML("<entity name=\""
                                    + entityName + "\" >\n");

                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</entity>\n");
                        }

                        _undoContext.setChildrenUndoable(true);
                    } else {
                        _undoContext.appendUndoMoML("<deleteEntity name=\""
                                + entityName + "\" />\n");

                        // Do not need to continue generating undo MoML
                        // as the deleteEntity takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                        _undoContext.setUndoable(false);

                        // Prevent any further undo entries for this context.
                        _undoEnabled = false;
                    }
                }

                //////////////////////////////////////////////////////////////
                //// configure
            } else if (elementName.equals("configure")) {
                _checkClass(_current, Configurable.class,
                        "Element \"configure\" found inside an element that "
                                + "does not implement Configurable. It is: "
                                + _current);
                _configureSource = (String) _attributes.get("source");
                _currentCharData = new StringBuffer();

                // Count configure tags so that they can nest.
                _configureNesting++;

                //////////////////////////////////////////////////////////////
                //// deleteEntity
            } else if (elementName.equals("deleteEntity")) {
                String entityName = (String) _attributes.get("name");
                _checkForNull(entityName,
                        "No name for element \"deleteEntity\"");

                // Link is stored and processed last, but before deletions.
                DeleteRequest request = new DeleteRequest(_DELETE_ENTITY,
                        entityName, null);

                // Only defer if we are in a class, entity, or model context,
                // which is equivalent to the _current being an instance of
                // InstantiableNamedObj.
                if (_deleteRequests != null
                        && _current instanceof InstantiableNamedObj) {
                    _deleteRequests.add(request);
                } else {
                    // Very likely, the context is null, in which
                    // case the following will throw an exception.
                    // We defer to it in case somehow a link request
                    // is being made at the top level with a non-null
                    // context (e.g. via a change request).
                    request.execute();
                }

                // NOTE: deleteEntity is not supposed to have anything
                // inside it, so we do not push the context.
                //////////////////////////////////////////////////////////////
                //// deletePort
            } else if (elementName.equals("deletePort")) {
                String portName = (String) _attributes.get("name");
                _checkForNull(portName, "No name for element \"deletePort\"");

                // The entity attribute is optional.
                String entityName = (String) _attributes.get("entity");

                // Delete the corresponding ParameterPort, if any.
                Port toDelete = null;
                try {
                    toDelete = _searchForPort(portName);
                } catch (XmlException ex) {
                    // Ignore, there is no port by that name.
                }
                // Find the corresponding ParameterPort and delete it
                if (toDelete != null) {
                    NamedObj container = toDelete.getContainer();
                    if (container != null && container instanceof Entity) {
                        Attribute attribute = ((Entity) container)
                                .getAttribute(portName);
                        if (attribute != null
                                && attribute instanceof PortParameter) {
                            DeleteRequest request = new DeleteRequest(
                                    _DELETE_PROPERTY, attribute.getName(), null);
                            // Only defer if we are in a class, entity, or
                            // model context, which is equivalent to the
                            // _current being an instance of
                            // InstantiableNamedObj.
                            if (_deleteRequests != null
                                    && _current instanceof InstantiableNamedObj) {
                                _deleteRequests.add(request);
                            } else {
                                // Very likely, the context is null, in which
                                // case the following will throw an exception.
                                // We defer to it in case somehow a link request
                                // is being made at the top level with a non-null
                                // context (e.g. via a change request).
                                request.execute();
                            }
                        }
                    }
                }

                // Link is stored and processed last, but before deletions.
                DeleteRequest request = new DeleteRequest(_DELETE_PORT,
                        portName, entityName);

                // Only defer if we are in a class, entity, or model context,
                // which is equivalent to the _current being an instance of
                // InstantiableNamedObj.
                if (_deleteRequests != null
                        && _current instanceof InstantiableNamedObj) {
                    _deleteRequests.add(request);
                } else {
                    // Very likely, the context is null, in which
                    // case the following will throw an exception.
                    // We defer to it in case somehow a link request
                    // is being made at the top level with a non-null
                    // context (e.g. via a change request).
                    request.execute();
                }

                // NOTE: deletePort is not supposed to have anything
                // inside it, so we do not push the context.
                //////////////////////////////////////////////////////////////
                //// deleteProperty
            } else if (elementName.equals("deleteProperty")) {
                String propName = (String) _attributes.get("name");
                _checkForNull(propName,
                        "No name for element \"deleteProperty\"");

                // Link is stored and processed last, but before deletions.
                DeleteRequest request = new DeleteRequest(_DELETE_PROPERTY,
                        propName, null);

                // We use toDelete to find any PortParameters
                Attribute toDelete = _searchForAttribute(propName);

                // Only defer if we are in a class, entity, or model context,
                // which is equivalent to the _current being an instance of
                // InstantiableNamedObj.
                if (_deleteRequests != null
                        && _current instanceof InstantiableNamedObj) {
                    _deleteRequests.add(request);
                } else {
                    // Very likely, the context is null, in which
                    // case the following will throw an exception.
                    // We defer to it in case somehow a link request
                    // is being made at the top level with a non-null
                    // context (e.g. via a change request).
                    request.execute();
                }

                // Find the corresponding PortParameter and delete it
                NamedObj container = toDelete.getContainer();
                if (container != null && container instanceof Entity) {
                    Port port = ((Entity) container).getPort(propName);
                    if (port != null && port instanceof ParameterPort) {
                        request = new DeleteRequest(_DELETE_PORT,
                                port.getName(), container.getFullName());
                        // Only defer if we are in a class, entity, or
                        // model context, which is equivalent to the
                        // _current being an instance of
                        // InstantiableNamedObj.
                        if (_deleteRequests != null
                                && _current instanceof InstantiableNamedObj) {
                            _deleteRequests.add(request);
                        } else {
                            // Very likely, the context is null, in which
                            // case the following will throw an exception.
                            // We defer to it in case somehow a link request
                            // is being made at the top level with a non-null
                            // context (e.g. via a change request).
                            request.execute();
                        }
                    }
                }

                // NOTE: deleteProperty is not supposed to have anything
                // inside it, so we do not push the context.
                //////////////////////////////////////////////////////////////
                //// deleteRelation
            } else if (elementName.equals("deleteRelation")) {
                String relationName = (String) _attributes.get("name");
                _checkForNull(relationName,
                        "No name for element \"deleteRelation\"");

                // Link is stored and processed last, but before deletions.
                DeleteRequest request = new DeleteRequest(_DELETE_RELATION,
                        relationName, null);

                // Only defer if we are in a class, entity, or model context,
                // which is equivalent to the _current being an instance of
                // InstantiableNamedObj.
                if (_deleteRequests != null
                        && _current instanceof InstantiableNamedObj) {
                    _deleteRequests.add(request);
                } else {
                    // Very likely, the context is null, in which
                    // case the following will throw an exception.
                    // We defer to it in case somehow a link request
                    // is being made at the top level with a non-null
                    // context (e.g. via a change request).
                    request.execute();
                }

                // NOTE: deleteRelation is not supposed to have anything
                // inside it, so we do not push the context.

                //////////////////////////////////////////////////////////////
                //// director
            } else if (elementName.equals("director")) {
                // NOTE: The director element is deprecated.
                // Use a property instead.  This is kept here so that
                // this parser can read older MoML files.
                // NOTE: We do not check for a previously existing director.
                // There is presumably no harm in just creating a new one.
                String className = (String) _attributes.get("class");
                _checkForNull(className, "No class for element \"director\"");

                String dirName = (String) _attributes.get("name");
                _checkForNull(dirName, "No name for element \"director\"");
                _checkClass(_current, CompositeActor.class,
                        "Element \"director\" found inside an element that "
                                + "is not a CompositeActor. It is: " + _current);

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
                //// display
            } else if (elementName.equals("display")) {
                String displayName = (String) _attributes.get("name");
                if (_current != null) {

                    // Propagate.
                    Iterator derivedObjects = _current.getDerivedList()
                            .iterator();
                    String currentName = _current.getName();
                    while (derivedObjects.hasNext()) {
                        NamedObj derived = (NamedObj) derivedObjects.next();

                        // If the derived object has the same
                        // name as the old name, then we assume it
                        // should change.
                        if (derived.getName().equals(currentName)) {
                            if (displayName != null) {
                                if (displayName.equals(currentName)) {
                                    // The displayName is the same as the
                                    // name, so it should be reset to null.
                                    derived.setDisplayName(null);
                                } else {
                                    derived.setDisplayName(displayName);
                                }
                            }
                        }
                    }

                    // Now change the display name.
                    String oldDisplayName = _current.getDisplayName();
                    if (displayName != null) {
                        if (displayName.equals(currentName)
                                || displayName.equals("")) {
                            // The displayName is the same as the
                            // name, so it should be reset to null.
                            _current.setDisplayName(null);
                        } else {
                            _current.setDisplayName(displayName);
                        }

                        // Handle the undo aspect if needed
                        if (_undoEnabled) {
                            // Simply create in the undo MoML another display element.
                            _undoContext.appendUndoMoML("<display name=\""
                                    + StringUtilities
                                    .escapeForXML(oldDisplayName)
                                    + "\"/>\n");

                            // Do not need to continue generating undo MoML
                            // as rename does not have any child elements
                            _undoContext.setChildrenUndoable(false);
                        }
                    }
                }

                //////////////////////////////////////////////////////////////
                //// doc
            } else if (elementName.equals("doc")) {
                _currentDocName = (String) _attributes.get("name");
                _currentCharData = new StringBuffer();

                // Count doc tags so that they can nest.
                _docNesting++;

                //////////////////////////////////////////////////////////////
                //// entity
            } else if (elementName.equals("entity")
                    || elementName.equals("model")) {
                // NOTE: The "model" element is deprecated.  It is treated
                // exactly as an entity.
                String className = (String) _attributes.get("class");
                String entityName = (String) _attributes.get("name");
                _checkForNull(entityName, "No name for element \"entity\"");

                String source = (String) _attributes.get("source");

                // For undo purposes need to know if the entity existed
                // already
                Entity entity = _searchForEntity(entityName, _current);
                boolean existedAlready = entity != null;
                boolean converted = false;

                if (existedAlready) {
                    // Check whether it was previously a class, in which case
                    // it is being converted to an entity.
                    if (entity.isClassDefinition()) {
                        entity.setClassDefinition(false);
                        converted = true;
                    }
                } else {
                    NamedObj candidate = _createEntity(className, entityName,
                            source, false);

                    if (candidate instanceof Entity) {
                        entity = (Entity) candidate;
                        entity.setClassName(className);
                    } else {
                        throw new IllegalActionException(_current,
                                "Attempt to create an entity named "
                                        + entityName + " from a class that "
                                        + "is not a subclass of Entity: "
                                        + className);
                    }
                }

                // NOTE: The entity may be at the top level, in
                // which case _deleteRequests is null.
                if (_deleteRequests != null) {
                    _deleteRequestStack.push(_deleteRequests);
                    pushedDeleteRequests = true;
                }

                _deleteRequests = new LinkedList();

                // NOTE: The entity may be at the top level, in
                // which case _linkRequests is null.
                if (_linkRequests != null) {
                    _linkRequestStack.push(_linkRequests);
                    pushedLinkRequests = true;
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
                        URIAttribute attribute = new URIAttribute(_toplevel,
                                "_uri");
                        attribute.setURL(_xmlFile);
                    }
                }

                _current = entity;

                _namespace = _DEFAULT_NAMESPACE;

                if (_undoEnabled) {
                    // Handle the undo aspect.
                    if (existedAlready) {
                        if (!converted) {
                            _undoContext.appendUndoMoML("<entity name=\""
                                    + entityName + "\" >\n");

                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</entity>\n");
                        } else {
                            // Converted from a class to an entity, so reverse this.
                            _undoContext.appendUndoMoML("<class name=\""
                                    + entityName + "\" >\n");

                            // Need to continue undoing and use an end tag
                            _undoContext.appendClosingUndoMoML("</class>\n");
                        }

                        _undoContext.setChildrenUndoable(true);
                    } else {
                        _undoContext.appendUndoMoML("<deleteEntity name=\""
                                + entityName + "\" />\n");

                        // Do not need to continue generating undo MoML
                        // as the deleteEntity takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                        _undoContext.setUndoable(false);

                        // Prevent any further undo entries for this context.
                        _undoEnabled = false;
                    }
                }

                //////////////////////////////////////////////////////////////
                //// group
            } else if (elementName.equals("group")) {
                String groupName = (String) _attributes.get("name");

                if (groupName != null) {
                    // Defining a namespace.
                    _namespaces.push(_namespace);
                    _namespaceTranslations.push(_namespaceTranslationTable);
                    _namespacesPushed = true;

                    if (groupName.equals("auto")) {
                        _namespace = _AUTO_NAMESPACE;
                        _namespaceTranslationTable = new HashMap();
                    } else {
                        _namespace = groupName;
                    }
                } else {
                    _namespaces.push(_DEFAULT_NAMESPACE);
                    _namespaceTranslations.push(_namespaceTranslationTable);
                    _namespacesPushed = true;
                    _namespace = _DEFAULT_NAMESPACE;
                    _namespaceTranslationTable = new HashMap();
                }

                // Link and unlink requests are processed when the
                // group closes.
                // NOTE: The entity may be at the top level, in
                // which case _deleteRequests is null.
                if (_deleteRequests != null) {
                    _deleteRequestStack.push(_deleteRequests);
                    pushedDeleteRequests = true;
                }

                _deleteRequests = new LinkedList();

                // NOTE: The entity may be at the top level, in
                // which case _linkRequests is null.
                if (_linkRequests != null) {
                    _linkRequestStack.push(_linkRequests);
                    pushedLinkRequests = true;
                }

                _linkRequests = new LinkedList();

                // Handle the undo aspect.
                if (_undoEnabled) {
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
                String source = (String) _attributes.get("source");
                _checkForNull(source, "No source for element \"input\"");
                // Uncomment this to trace outputs
                //System.out.println("MoMLParser: input: " + source);

                boolean skip = false;

                if (source
                        .equals("ptolemy/configs/properties/propertiesAttributeLibrary.xml")) {
                    // Certain models such as the ee149 models like
                    // eecs149/src/reading/io/Models/TimerInterrupt.xml
                    // had a StateLibrary entity that included
                    // '<input source="ptolemy/configs/properties/propertiesAttributeLibrary.xml"...'
                    // This file is part of the properties work that was removed.
                    // It is a bit of a bug that FSM Editors have a StateLibrary entity that
                    // does inputs at all.

                    // Our fix here is to skip the input and mark this as modified.
                    skip = true;
                    // FIXME: this does not seem to have any effect?
                    setModified(true);
                }

                if (inputFileNamesToSkip != null) {
                    // If inputFileNamesToSkip contains a string
                    // that matches the end of source, then skip
                    // parsing the source file.  We use this for testing
                    // configurations that have optional parts like
                    // Matlab or javacomm.
                    Iterator inputFileNames = inputFileNamesToSkip.iterator();

                    while (inputFileNames.hasNext()) {
                        String inputFileName = (String) inputFileNames.next();

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
                    boolean modified = isModified();
                    MoMLParser newParser = new MoMLParser(_workspace,
                            _classLoader);

                    newParser.setContext(_current);
                    setModified(modified);

                    _parse(newParser, _base, source);
                }

                //////////////////////////////////////////////////////////////
                //// link
            } else if (elementName.equals("link")) {
                String portName = (String) _attributes.get("port");

                // Port can be null if we are linking two vertices.
                // _checkForNull(portName, "No port for element \"link\"");
                // Relation attribute now optional
                String relationName = (String) _attributes.get("relation");
                String insertAtSpec = (String) _attributes.get("insertAt");
                String insertInsideAtSpec = (String) _attributes
                        .get("insertInsideAt");

                // Link is stored and processed last, but before deletions.
                LinkRequest request;

                if (portName != null) {
                    request = new LinkRequest(portName, relationName,
                            insertAtSpec, insertInsideAtSpec);
                } else {
                    String relation1Name = (String) _attributes
                            .get("relation1");
                    String relation2Name = (String) _attributes
                            .get("relation2");
                    request = new LinkRequest(relation1Name, relation2Name);
                }

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
                String className = (String) _attributes.get("class");
                String portName = (String) _attributes.get("name");
                _checkForNull(portName, "No name for element \"port\"");

                _checkClass(_current, Entity.class,
                        "Element \"port\" found inside an element that "
                                + "is not an Entity. It is: " + _current);

                Entity container = (Entity) _current;

                Class newClass = null;

                if (className != null && !className.trim().equals("")) {
                    newClass = Class.forName(className, true, _classLoader);
                }

                Port port = container.getPort(portName);

                // Flag used to generate correct undo MoML
                boolean alreadyExisted = port != null;

                if (port != null) {
                    if (newClass != null) {
                        // Previously existing port with the specified name.
                        _checkClass(port, newClass, "port named \"" + portName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                } else {
                    // No previously existing port with this name.
                    // First check that there will be no name collision
                    // when this is propagated. Note that we need to
                    // include all derived objects, irrespective of whether
                    // they are locally changed.
                    List derivedList = container.getDerivedList();
                    Iterator derivedObjects = derivedList.iterator();

                    while (derivedObjects.hasNext()) {
                        Entity derived = (Entity) derivedObjects.next();

                        if (derived.getPort(portName) != null) {
                            throw new IllegalActionException(
                                    container,
                                    "Cannot create port because a subclass or instance "
                                            + "contains a port with the same name: "
                                            + derived.getPort(portName)
                                            .getFullName());
                        }
                    }

                    if (newClass == null) {
                        // Classname is not given.  Invoke newPort() on the
                        // container.
                        port = container.newPort(portName);

                        if (_topObjectsCreated != null
                                && container == _originalContext) {
                            _topObjectsCreated.add(port);
                        }

                        // Propagate.
                        // NOTE: Propagated ports will not use newPort(),
                        // but rather will use clone. Classes that override
                        // newPort() to perform special actions will no longer
                        // work, possibly!
                        port.propagateExistence();
                    } else {
                        // Classname is given.
                        Object[] arguments = new Object[2];
                        arguments[0] = container;
                        arguments[1] = portName;
                        port = (Port) _createInstance(newClass, arguments);

                        // Propagate.
                        port.propagateExistence();
                    }
                }

                _pushContext();
                _current = port;

                _namespace = _DEFAULT_NAMESPACE;

                // Handle the undo aspect if needed
                if (_undoEnabled) {
                    if (alreadyExisted) {
                        // Simply create in the undo MoML the same port
                        _undoContext.appendUndoMoML("<port name=\"" + portName
                                + "\" ");

                        // Also add in the class if given
                        if (className != null) {
                            _undoContext.appendUndoMoML("class=\"" + className
                                    + "\" ");
                        }

                        _undoContext.appendUndoMoML(">\n");

                        // Need to continue undoing and use an end tag
                        _undoContext.appendClosingUndoMoML("</port>\n");
                        _undoContext.setChildrenUndoable(true);
                    } else {
                        // Need to delete the port in the undo MoML
                        _undoContext.appendUndoMoML("<deletePort name=\""
                                + portName + "\" />\n");

                        // Do not need to continue generating undo MoML
                        // as the deletePort takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                        _undoContext.setUndoable(false);

                        // Prevent any further undo entries for this context.
                        _undoEnabled = false;
                    }
                }

                // NOTE: The direction attribute is deprecated, but
                // supported nonetheless. This is not propagated, but
                // hopefully deprecated attributes are not used with
                // the class mechanism.
                if (port instanceof IOPort) {
                    String direction = (String) _attributes.get("direction");

                    if (direction != null) {
                        IOPort ioport = (IOPort) port;
                        boolean isOutput = direction.equals("output")
                                || direction.equals("both");
                        boolean isInput = direction.equals("input")
                                || direction.equals("both");

                        // If this object is a derived object, then its I/O status
                        // cannot be changed.
                        if (alreadyExisted
                                && ioport.getDerivedLevel() < Integer.MAX_VALUE) {
                            if (ioport.isInput() != isInput
                                    || ioport.isOutput() != isOutput) {
                                throw new IllegalActionException(
                                        ioport,
                                        "Cannot change whether this port is "
                                                + "an input or output. That property is "
                                                + "fixed by the class definition.");
                            }
                        }

                        ioport.setOutput(isOutput);
                        ioport.setInput(isInput);
                    }
                }

                //////////////////////////////////////////////////////////////
                //// property
            } else if (elementName.equals("property")) {
                String createIfNecessary = (String) _attributes
                        .get("createIfNecessary");
                String className = (String) _attributes.get("class");
                String propertyName = (String) _attributes.get("name");
                _checkForNull(propertyName, "No name for element \"property\"");
                if (createIfNecessary != null
                        && createIfNecessary.equals("true") && _current != null
                        && propertyName != null
                        && _current.getAttribute(propertyName) != null) {
                    // The createIfNecessary="true" and the property
                    // already exists
                } else {
                    // If the createIfNecessary property is true and
                    // there already is a property with this name, we
                    // don't handle this property
                    String value = (String) _attributes.get("value");
                    if (propertyName == null) {
                        throw new InternalErrorException(
                                "FindBugs: propertyName must not be null when calling _handlePropertyName()");
                    } else {
                        _handlePropertyElement(className, propertyName, value);
                    }
                }

                //////////////////////////////////////////////////////////////
                //// relation
            } else if (elementName.equals("relation")) {
                String className = (String) _attributes.get("class");
                String relationName = (String) _attributes.get("name");
                _checkForNull(relationName, "No name for element \"relation\"");
                _checkClass(_current, CompositeEntity.class,
                        "Element \"relation\" found inside an element that "
                                + "is not a CompositeEntity. It is: "
                                + _current);

                CompositeEntity container = (CompositeEntity) _current;
                Class newClass = null;

                if (className != null) {
                    newClass = Class.forName(className, true, _classLoader);
                }

                Relation relation = container.getRelation(relationName);

                // Flag used to generate correct undo MoML
                boolean alreadyExisted = relation != null;

                if (relation == null) {
                    // No previous relation with this name.
                    // First check that there will be no name collision
                    // when this is propagated. Note that we need to
                    // include all derived objects, irrespective of whether
                    // they are locally changed.
                    List derivedList = container.getDerivedList();
                    Iterator derivedObjects = derivedList.iterator();

                    while (derivedObjects.hasNext()) {
                        CompositeEntity derived = (CompositeEntity) derivedObjects
                                .next();

                        if (derived.getRelation(relationName) != null) {
                            throw new IllegalActionException(
                                    container,
                                    "Cannot create relation because a subclass or instance "
                                            + "contains a relation with the same name: "
                                            + derived.getRelation(relationName)
                                            .getFullName());
                        }
                    }

                    NamedObj newRelation = null;
                    _pushContext();

                    if (newClass == null) {
                        // No classname. Use the newRelation() method.
                        newRelation = container.newRelation(relationName);

                        // Mark the contents of the new entity as being derived objects.
                        // If we wouldn't do this the default attributes would be saved.
                        _markContentsDerived(newRelation, 0);

                        if (_topObjectsCreated != null
                                && container == _originalContext) {
                            _topObjectsCreated.add(newRelation);
                        }

                        // Propagate.
                        // NOTE: Propagated relations will not use newRelation(),
                        // but rather will use clone. Classes that rely
                        // on newRelation(), will no longer work, possibly!
                        newRelation.propagateExistence();
                    } else {
                        Object[] arguments = new Object[2];
                        arguments[0] = _current;
                        arguments[1] = relationName;
                        newRelation = _createInstance(newClass, arguments);

                        // Propagate.
                        newRelation.propagateExistence();
                    }

                    _namespace = _DEFAULT_NAMESPACE;
                    _current = newRelation;

                } else {
                    // Previously existing relation with the specified name.
                    if (newClass != null) {
                        _checkClass(relation, newClass, "relation named \""
                                + relationName
                                + "\" exists and is not an instance of "
                                + className);
                    }

                    _pushContext();

                    _current = relation;
                    _namespace = _DEFAULT_NAMESPACE;
                }

                // Handle the undo aspect if needed
                if (_undoEnabled) {
                    if (alreadyExisted) {
                        // Simply create in the undo MoML the same relation
                        _undoContext.appendUndoMoML("<relation name=\""
                                + relationName + "\" ");

                        // Also add in the class if given
                        if (className != null) {
                            _undoContext.appendUndoMoML("class=\"" + className
                                    + "\" ");
                        }

                        _undoContext.appendUndoMoML(">\n");

                        // Need to continue undoing and use an end tag
                        _undoContext.appendClosingUndoMoML("</relation>\n");
                        _undoContext.setChildrenUndoable(true);
                    } else {
                        // Need to delete the realtion in the undo MoML
                        _undoContext.appendUndoMoML("<deleteRelation name=\""
                                + relationName + "\" />\n");

                        // Do not need to continue generating undo MoML
                        // as the deleteRelation takes care of all
                        // contained MoML
                        _undoContext.setChildrenUndoable(false);
                        _undoContext.setUndoable(false);

                        // Prevent any further undo entries for this context.
                        _undoEnabled = false;
                    }
                }

                //////////////////////////////////////////////////////////////
                //// rename
            } else if (elementName.equals("rename")) {
                String newName = (String) _attributes.get("name");
                _checkForNull(newName, "No new name for element \"rename\"");

                if (_current != null) {
                    String oldName = _current.getName();

                    // Ensure that derived objects aren't changed.
                    if (!oldName.equals(newName)
                            && _current.getDerivedLevel() < Integer.MAX_VALUE) {
                        throw new IllegalActionException(
                                _current,
                                "Cannot change the name to "
                                        + newName
                                        + ". The name is fixed by the class definition.");
                    }

                    // Propagate.  Note that a rename in a derived class
                    // could cause a NameDuplicationException.  We have to
                    // be able to unroll the changes if that occurs.
                    Iterator derivedObjects = _current.getDerivedList()
                            .iterator();
                    Set changedName = new HashSet();
                    HashMap changedClassName = new HashMap();
                    NamedObj derived = null;

                    try {
                        while (derivedObjects.hasNext()) {
                            derived = (NamedObj) derivedObjects.next();

                            // If the derived object has the same
                            // name as the old name, then we assume it
                            // should change.
                            if (derived.getName().equals(oldName)) {
                                derived.setName(newName);
                                changedName.add(derived);
                            }

                            // Also need to modify the class name of
                            // the instance or derived class if the
                            // class or base class changes its name.
                            if (derived instanceof Instantiable) {
                                Instantiable parent = ((Instantiable) derived)
                                        .getParent();

                                // This relies on the depth-first search
                                // order of the getDerivedList() method
                                // to be sure that the base class will
                                // already be in the changedName set if
                                // its name will change.
                                if (parent != null
                                        && (parent == _current || changedName
                                        .contains(parent))) {
                                    String previousClassName = derived
                                            .getClassName();
                                    int last = previousClassName
                                            .lastIndexOf(oldName);

                                    if (last < 0) {
                                        throw new InternalErrorException(
                                                "Expected instance "
                                                        + derived.getFullName()
                                                        + " to have class name ending with "
                                                        + oldName
                                                        + " but its class name is "
                                                        + previousClassName);
                                    }

                                    String newClassName = newName;

                                    if (last > 0) {
                                        newClassName = previousClassName
                                                .substring(0, last) + newName;
                                    }

                                    derived.setClassName(newClassName);
                                    changedClassName.put(derived,
                                            previousClassName);
                                }
                            }
                        }
                    } catch (NameDuplicationException ex) {
                        // Unravel the name changes before
                        // rethrowing the exception.
                        Iterator toUndo = changedName.iterator();

                        while (toUndo.hasNext()) {
                            NamedObj revert = (NamedObj) toUndo.next();
                            revert.setName(oldName);
                        }

                        Iterator classNameFixes = changedClassName.entrySet()
                                .iterator();

                        while (classNameFixes.hasNext()) {
                            Map.Entry revert = (Map.Entry) classNameFixes
                                    .next();
                            NamedObj toFix = (NamedObj) revert.getKey();
                            String previousClassName = (String) revert
                                    .getValue();
                            toFix.setClassName(previousClassName);
                        }

                        throw new IllegalActionException(_current, ex,
                                "Propagation to instance and/or derived class causes"
                                        + "name duplication: "
                                        + derived.getFullName());
                    }

                    _current.setName(newName);

                    // Handle the undo aspect if needed
                    if (_undoEnabled) {
                        // First try and rename in the parent context.
                        // NOTE: this is a bit of a hack but is the only way
                        // I could see of doing the rename without having to
                        // change the semantics or location of the rename
                        // element
                        UndoContext parentContext = (UndoContext) _undoContexts
                                .peek();
                        parentContext.applyRename(newName);

                        // Simply create in the undo MoML another rename
                        _undoContext.appendUndoMoML("<rename name=\"" + oldName
                                + "\" />\n");

                        // Do not need to continue generating undo MoML
                        // as rename does not have any child elements
                        _undoContext.setChildrenUndoable(false);
                    }

                    // If _current is a class definition, then find
                    // subclasses and instances and propagate the
                    // change to the name of the
                    // object they refer to.
                    if (_current instanceof Instantiable
                            && ((Instantiable) _current).isClassDefinition()) {
                        List deferredFrom = ((Instantiable) _current)
                                .getChildren();

                        if (deferredFrom != null) {
                            Iterator deferrers = deferredFrom.iterator();

                            while (deferrers.hasNext()) {
                                WeakReference reference = (WeakReference) deferrers
                                        .next();
                                InstantiableNamedObj deferrer = (InstantiableNamedObj) reference
                                        .get();

                                if (deferrer != null) {
                                    // Got a live one.
                                    // Need to determine whether the name is
                                    // absolute or relative.
                                    String replacementName = newName;

                                    if (deferrer.getClassName().startsWith(".")) {
                                        replacementName = _current
                                                .getFullName();
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
                String portName = (String) _attributes.get("port");

                // Port may not be specified if we are unlinking two vertices.
                // _checkForNull(portName, "No port for element \"unlink\"");
                String relationName = (String) _attributes.get("relation");
                String indexSpec = (String) _attributes.get("index");
                String insideIndexSpec = (String) _attributes
                        .get("insideIndex");

                // Unlink is stored and processed last.
                UnlinkRequest request;

                if (portName != null) {
                    request = new UnlinkRequest(portName, relationName,
                            indexSpec, insideIndexSpec);
                } else {
                    String relation1Name = (String) _attributes
                            .get("relation1");
                    String relation2Name = (String) _attributes
                            .get("relation2");
                    request = new UnlinkRequest(relation1Name, relation2Name);
                }

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
                String vertexName = (String) _attributes.get("name");
                _checkForNull(vertexName, "No name for element \"vertex\"");

                _checkClass(_current, Relation.class,
                        "Element \"vertex\" found inside an element that "
                                + "is not a Relation. It is: " + _current);

                // For undo need to know if a previous vertex attribute
                // with this name existed, and if so its expression
                Vertex previous = (Vertex) _current.getAttribute(vertexName);
                String previousValue = null;

                if (previous != null) {
                    previousValue = previous.getExpression();
                }

                // No need to check for name collision on propagated
                // objects because Vertex is a singleton.
                Vertex vertex = previous;

                // Create a new vertex only if it didn't previously exist.
                if (vertex == null) {
                    vertex = new Vertex((Relation) _current, vertexName);

                    // Propagate.
                    vertex.propagateExistence();
                }

                // Deal with setting the location.
                String value = (String) _attributes.get("value");

                // If value is null or same as before, then there is
                // nothing to do.
                if (value != null && !value.equals(previousValue)) {
                    vertex.setExpression(value);

                    // Propagate to derived classes and instances.
                    try {
                        // Propagate. This has the side effect of marking the
                        // object overridden.
                        vertex.propagateValue();
                        _paramsToParse.add(vertex);
                    } catch (IllegalActionException ex) {
                        // Propagation failed. Restore previous value.
                        vertex.setExpression(previousValue);
                        throw ex;
                    }
                }

                _pushContext();

                _current = vertex;
                _namespace = _DEFAULT_NAMESPACE;

                if (_undoEnabled) {
                    _undoContext.appendUndoMoML("<vertex name=\"" + vertexName
                            + "\" ");

                    if (previousValue != null) {
                        _undoContext.appendUndoMoML("value=\"" + previousValue
                                + "\" ");
                    }

                    _undoContext.appendUndoMoML(">\n");

                    // The Vertex element can have children
                    _undoContext.setChildrenUndoable(true);
                    _undoContext.appendClosingUndoMoML("</vertex>\n");
                }

                //////////////////////////////////////////////////////////////
                //// if
            } else if (elementName.equals("if")) {
                String name = _current.uniqueName("_tempVariable");
                Class variableClass = Class
                        .forName("ptolemy.data.expr.Variable");
                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = name;
                Settable variable = (Settable) _createInstance(variableClass,
                        arguments);

                String expression = (String) _attributes.get("test");
                if (expression == null) {
                    throw new IllegalActionException(_current,
                            "<if> element must have the \"test\" property "
                                    + "which specifies the expression to test.");
                }
                variable.setExpression(expression);

                Object token = variableClass
                        .getMethod("getToken", new Class[0]).invoke(variable,
                                new Object[0]);
                Class tokenClass = token.getClass();
                Boolean value = (Boolean) tokenClass.getMethod("booleanValue",
                        new Class[0]).invoke(token, new Object[0]);

                ((Attribute) variable).setContainer(null);

                if (!value.booleanValue()) {
                    _ifElementStack.push(Integer.valueOf(2));
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
            exceptionThrown = true;

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
                    throw new XmlException("*** Canceled.",
                            _currentExternalEntity(), _getLineNumber(),
                            _getColumnNumber());
                }
            }

            // No handler.
            throw new XmlException("XML element \"" + elementName
                    + "\" triggers exception:\n  " + ex.getTargetException(),
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber(), ex.getTargetException());
        } catch (Exception ex) {
            exceptionThrown = true;

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
                    // Restore the status of change requests.
                    // Execute any change requests that might have been queued
                    // as a consequence of this change request.
                    if (_toplevel != null) {
                        // Set the top level back to the default
                        // found in startDocument.
                        _toplevel
                        .setDeferringChangeRequests(_previousDeferStatus);
                        _toplevel.executeChangeRequests();
                    }

                    // NOTE: Since we have to throw an XmlException for
                    // the exception to be properly handled, we communicate
                    // that it is a user cancellation with the special
                    // string pattern "*** Canceled." in the message.
                    throw new XmlException("*** Canceled.",
                            _currentExternalEntity(), _getLineNumber(),
                            _getColumnNumber());
                }
            }

            // There is no handler.
            // Restore the status of change requests.
            // Execute any change requests that might have been queued
            // as a consequence of this change request.
            if (_toplevel != null) {
                // Set the top level back to the default
                // found in startDocument.
                _toplevel.setDeferringChangeRequests(_previousDeferStatus);
                _toplevel.executeChangeRequests();
            }

            if (ex instanceof XmlException) {
                throw (XmlException) ex;
            } else {
                throw new XmlException("XML element \"" + elementName
                        + "\" triggers exception.", _currentExternalEntity(),
                        _getLineNumber(), _getColumnNumber(), ex);
            }
        } finally {
            _attributes.clear();
            _attributeNameList.clear();

            // If an exception was thrown, then restore all stacks
            // by popping off them anything that was pushed.
            if (exceptionThrown) {
                if (pushedDeleteRequests) {
                    try {
                        _deleteRequests = (List) _deleteRequestStack.pop();
                    } catch (EmptyStackException ex) {
                        // We are back at the top level.
                        _deleteRequests = null;
                    }
                }

                if (pushedLinkRequests) {
                    try {
                        _linkRequests = (List) _linkRequestStack.pop();
                    } catch (EmptyStackException ex) {
                        // We are back at the top level.
                        _linkRequests = null;
                    }
                }

                if (_namespacesPushed) {
                    try {
                        _namespace = (String) _namespaces.pop();

                        _namespaceTranslationTable = (Map) _namespaceTranslations
                                .pop();
                    } catch (EmptyStackException ex) {
                        _namespace = _DEFAULT_NAMESPACE;
                    }
                }

                if (pushedUndoContexts) {
                    try {
                        _undoContext = (UndoContext) _undoContexts.pop();
                    } catch (EmptyStackException ex) {
                        // This should not occur, but if it does,
                        // we don't want _undoContext set to null.
                        // Leave it as it is so we don't lose undo
                        // information.
                    }
                }
            }
        }
    }

    /** Handle the start of an external entity.  This pushes the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemID The URI for the external entity.
     */
    @Override
    public void startExternalEntity(String systemID) {
        // NOTE: The Microstar XML parser incorrectly passes the
        // HTML file for the first external entity, rather than
        // XML file.  So error messages typically refer to the wrong file.
        _externalEntities.push(systemID);
    }

    /** Get the top objects list. The top objects list
     *  is a list of top-level objects that this parser has
     *  created.
     *  @return The list of top objects created since
     *   clearTopObjectsList() was called, or null if it has
     *   not been called.
     *  @see #clearTopObjectsList()
     */
    public List topObjectsCreated() {
        return _topObjectsCreated;
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
    public static String MoML_DTD_1 = "<!ELEMENT model (class | configure | deleteEntity | deletePort | deleteRelation | director | display | doc | entity | group | import | input | link | property | relation | rename | rendition | unlink)*><!ATTLIST model name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT class (class | configure | deleteEntity | deletePort | deleteRelation | director | display | doc | entity | group | import | input | link | port | property | relation | rename | rendition | unlink)*><!ATTLIST class name CDATA #REQUIRED extends CDATA #IMPLIED source CDATA #IMPLIED><!ELEMENT configure (#PCDATA)><!ATTLIST configure source CDATA #IMPLIED><!ELEMENT deleteEntity EMPTY><!ATTLIST deleteEntity name CDATA #REQUIRED><!ELEMENT deletePort EMPTY><!ATTLIST deletePort name CDATA #REQUIRED><!ELEMENT deleteProperty EMPTY><!ATTLIST deleteProperty name CDATA #REQUIRED><!ELEMENT deleteRelation EMPTY><!ATTLIST deleteRelation name CDATA #REQUIRED><!ELEMENT director (configure | doc | property)*><!ATTLIST director name CDATA \"director\" class CDATA #REQUIRED><!ELEMENT display EMPTY><!ATTLIST display name CDATA #REQUIRED><!ELEMENT doc (#PCDATA)><!ATTLIST doc name CDATA #IMPLIED><!ELEMENT entity (class | configure | deleteEntity | deletePort | deleteRelation | director | display | doc | entity | group | import | input | link | port | property | relation | rename | rendition | unlink)*><!ATTLIST entity name CDATA #REQUIRED class CDATA #IMPLIED source CDATA #IMPLIED><!ELEMENT group ANY><!ATTLIST group name CDATA #IMPLIED><!ELEMENT import EMPTY><!ATTLIST import source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT input EMPTY><!ATTLIST input source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT link EMPTY><!ATTLIST link insertAt CDATA #IMPLIED insertInsideAt CDATA #IMPLIED port CDATA #IMPLIED relation CDATA #IMPLIED relation1 CDATA #IMPLIED relation2 CDATA #IMPLIED vertex CDATA #IMPLIED><!ELEMENT location EMPTY><!ATTLIST location value CDATA #REQUIRED><!ELEMENT port (configure | display | doc | property | rename)*><!ATTLIST port class CDATA #IMPLIED name CDATA #REQUIRED><!ELEMENT property (configure | display | doc | property | rename)*><!ATTLIST property class CDATA #IMPLIED name CDATA #REQUIRED value CDATA #IMPLIED><!ELEMENT relation (configure | display | doc | property | rename | vertex)*><!ATTLIST relation name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT rename EMPTY><!ATTLIST rename name CDATA #REQUIRED><!ELEMENT rendition (configure | location | property)*><!ATTLIST rendition class CDATA #REQUIRED><!ELEMENT unlink EMPTY><!ATTLIST unlink index CDATA #IMPLIED insideIndex CDATA #IMPLIED port CDATA #REQUIRED relation CDATA #IMPLIED><!ELEMENT vertex (configure | display | doc | location | property | rename)*><!ATTLIST vertex name CDATA #REQUIRED pathTo CDATA #IMPLIED value CDATA #IMPLIED>";

    // NOTE: The master file for the above DTD is at
    // $PTII/ptolemy/moml/MoML_1.dtd.  If modified, it needs to be also
    // updated at ptweb/xml/dtd/MoML_1.dtd.

    /** The public ID for version 1 MoML. */
    public static String MoML_PUBLIC_ID_1 = "-//UC Berkeley//DTD MoML 1//EN";

    /** List of Strings that name files to be skipped.
     *  This variable is used primarily for testing configurations.
     *  The value of this variable is a List of Strings, where each
     *  element names a file name that should _not_ be loaded if
     *  it is encountered in an input statement.
     */
    public static List inputFileNamesToSkip = null;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the the URI for the current external entity.
     *  @return A string giving the URI of the external entity being read,
     *   or null if none.
     */
    protected String _currentExternalEntity() {
        try {
            return (String) _externalEntities.peek();
        } catch (EmptyStackException ex) {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add all (deeply) contained instances of Settable to the
     *  _paramsToParse list, which will ensure that they get validated.
     *  @param object The object to be scanned for Settables.
     */
    private void _addParamsToParamsToParse(NamedObj object) {
        Iterator objects = object.lazyContainedObjectsIterator();

        while (objects.hasNext()) {
            NamedObj containedObject = (NamedObj) objects.next();

            if (containedObject instanceof Settable) {
                _paramsToParse.add((Settable) containedObject);
            }

            _addParamsToParamsToParse(containedObject);
        }
    }

    /** Attempt to find a MoML class from an external file.
     *  If there is no source defined, then search for the file
     *  relative to the classpath.
     *  @param className The class name.
     *  @param source The source as specified in the XML.
     *  @return The class definition.
     */
    private ComponentEntity _attemptToFindMoMLClass(String className,
            String source) throws Exception {
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
        // First check to see whether the object has been previously loaded.
        URL url = null;
        try {
            url = fileNameToURL(classAsFile, _base);
            if (_imports != null) {
                WeakReference possiblePrevious = (WeakReference) _imports
                        .get(url);
                NamedObj previous = null;
                if (possiblePrevious != null) {
                    previous = (NamedObj) possiblePrevious.get();
                    if (previous == null) {
                        _imports.remove(url);
                    }
                }
                if (previous instanceof ComponentEntity) {
                    // NOTE: In theory, we should not even have to
                    // check whether the file has been updated, because
                    // if changes were made to model since it was loaded,
                    // they should have been propagated.
                    return (ComponentEntity) previous;
                }
            }
        } catch (Exception ex) {
            // An exception will be thrown if the class is not
            // found under the specified file name. Below we
            // will try again under the alternate file name.
        }

        // Read external model definition in a new parser,
        // rather than in the current context.
        MoMLParser newParser = new MoMLParser(_workspace, _classLoader);

        NamedObj candidateReference = null;

        try {
            candidateReference = _findOrParse(newParser, _base, classAsFile,
                    className, source);
        } catch (Exception ex2) {
            url = null;
            // Try the alternate file, if it's not null.
            if (altClassAsFile != null) {
                try {
                    url = fileNameToURL(altClassAsFile, _base);
                } catch (Exception ex) {
                    // Throw the original exception, which is likely to have
                    // the real reason that the file is missing.
                    // For example, if loading a .xml file fails because of
                    // a missing class, then we should report that message
                    // instead of looking for a .moml file.
                    // See test 32.1 in test/MoMLParser.tcl that reads in
                    // test/AltFileNameExceptionTest.xml
                    // It would be nice to have both messages displayed, but
                    // it would be ugly.

                    // FIXME: The tricky question is: if loading both the .xml
                    // and the .moml file fail, then what should be reported?
                    // If the .xml file is present and loading it fails, then
                    // any errors associated with the loading should be reported.
                    // If the .xml file is not present and the .moml file is
                    // present, then any errors associated with loading the
                    // .moml file should be reported.
                    // If neither file is present, then a FileNotFoundException
                    // should be thrown that lists both files.
                    throw ex2;
                }
                // First check to see whether the object has been previously loaded.
                if (_imports != null) {
                    WeakReference possiblePrevious = (WeakReference) _imports
                            .get(url);
                    NamedObj previous = null;
                    if (possiblePrevious != null) {
                        previous = (NamedObj) possiblePrevious.get();
                        if (previous == null) {
                            _imports.remove(url);
                        }
                    }
                    if (previous instanceof ComponentEntity) {
                        // NOTE: In theory, we should not even have to
                        // check whether the file has been updated, because
                        // if changes were made to model since it was loaded,
                        // they should have been propagated.
                        return (ComponentEntity) previous;
                    }
                }
                try {
                    candidateReference = _findOrParse(newParser, _base,
                            altClassAsFile, className, source);
                    classAsFile = altClassAsFile;
                } catch (Exception ex3) {
                    // Cannot find a class definition.
                    // Unfortunately exception chaining does not work here
                    // since we really want to know what ex2 and ex3
                    // both were.
                    throw new XmlException("Could not find '" + classAsFile
                            + "' or '" + altClassAsFile + "' using base '"
                            + _base + "': ", _currentExternalEntity(),
                            _getLineNumber(), _getColumnNumber(), ex2);
                }
            } else {
                // No alternative. Rethrow exception.
                throw ex2;
            }
        }

        if (candidateReference instanceof ComponentEntity) {
            reference = (ComponentEntity) candidateReference;
        } else {
            throw new XmlException("File " + classAsFile
                    + " does not define a ComponentEntity.",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        // Check that the classname matches the name of the
        // reference.
        String referenceName = reference.getName();

        if (!className.equals(referenceName)
                && !className.endsWith("." + referenceName)) {
            // The className might reference an inner class defined in
            // the reference.  Try to find that.
            if (reference instanceof CompositeEntity) {
                if (className.startsWith(referenceName + ".")) {
                    reference = ((CompositeEntity) reference)
                            .getEntity(className.substring(referenceName
                                    .length() + 1));
                } else {
                    reference = null;
                }
            } else {
                reference = null;
            }
            if (reference == null) {
                throw new XmlException("File " + classAsFile
                        + " does not define a class named " + className,
                        _currentExternalEntity(), _getLineNumber(),
                        _getColumnNumber());
            }
        }

        // Load an associated icon, if there is one.
        _loadIconForClass(className, reference);

        // Record the import to avoid repeated reading.
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
        _imports.put(url, new WeakReference(reference));

        return reference;
    }

    // If the first argument is not an instance of the second,
    // throw an exception with the given message.
    private void _checkClass(Object object, Class correctClass, String msg)
            throws XmlException {
        if (!correctClass.isInstance(object)) {
            throw new XmlException(msg, _currentExternalEntity(),
                    _getLineNumber(), _getColumnNumber());
        }
    }

    // If the argument is null, throw an exception with the given message.
    private void _checkForNull(Object object, String message)
            throws XmlException {
        if (object == null) {
            throw new XmlException(message, _currentExternalEntity(),
                    _getLineNumber(), _getColumnNumber());
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
     *  object is marked as a derived object.
     *  The third argument, if non-null, gives a URL to import
     *  to create a reference class from which to instantiate this
     *  entity.
     *
     * @param className
     * @param entityName
     * @param source
     * @param isClass True to create a class definition, false to create
     *  an instance.
     * @return The created NamedObj
     * @exception Exception If anything goes wrong.
     */
    private NamedObj _createEntity(String className, String entityName,
            String source, boolean isClass) throws Exception {
        if (_current != null && !(_current instanceof CompositeEntity)) {
            throw new XmlException("Cannot create an entity inside "
                    + "of another that is not a CompositeEntity "
                    + "(Container is '" + _current + "').",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        CompositeEntity container = (CompositeEntity) _current;
        ComponentEntity previous = _searchForEntity(entityName, _current);
        Class newClass = null;
        ComponentEntity reference = null;

        if (className != null) {
            // A class name is given.
            reference = searchForClass(className, source);

            // If no source is specified and no reference was found,
            // search for a class definition in context.
            if (reference == null && source == null) {
                // Allow the class name to be local in the current context
                // or defined in scope. Search for a class definition that
                // matches in the current context.
                reference = _searchForClassInContext(className, /* source*/
                        null);
            }

            if (reference == null || !reference.isClassDefinition()) {
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
                        _updateMissingClasses(className);
                        throw new IllegalActionException(null, ex2,
                                "Cannot find class: " + className);
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
                        Throwable staticThrowable = ((ExceptionInInitializerError) error)
                                .getCause();

                        String causeDescription = "";
                        if (staticThrowable == null) {
                            // The SerialComm actor may have a null cause
                            causeDescription = KernelException
                                    .stackTraceToString(error);
                        } else {
                            causeDescription = KernelException
                                    .stackTraceToString(staticThrowable);
                        }
                        // I think we should report the cause and a stack
                        // trace for all the exceptions thrown here,
                        // but it sure makes the output ugly.
                        // Instead, I just debug from here -cxh
                        errorMessage.append("ExceptionInInitializerError: "
                                + "Caused by:\n " + causeDescription);
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
                        // Error encountered in:
                        // <entity name="SerialComm" class="ptolemy.actor.lib...
                        // -- ptolemy.actor.lib.comm.SerialComm:
                        // javax/comm/SerialPortEventListener
                        // ptolemy.actor.lib.comm.SerialComm: XmlException:
                        // Could not find 'ptolemy/actor/lib/comm/SerialComm.xml'..
                        // If we use toString(), we get:
                        // Error encountered in:
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
                        _updateMissingClasses(className);
                        throw new Exception("-- " + errorMessage.toString()
                                + className + ": XmlException:\n"
                                + ex2.getMessage());
                    } catch (ClassFormatError ex3) {
                        _updateMissingClasses(className);
                        throw new Exception("-- :" + errorMessage.toString()
                                + className + ": ClassFormatError: "
                                + "found invalid Java class file.\n"
                                + ex3.getMessage());
                    } catch (Exception ex4) {
                        _updateMissingClasses(className);
                        throw new Exception("-- " + errorMessage.toString()
                                + className + ": Exception:\n"
                                + ex4.getMessage());
                    }
                }
            }
        }

        if (previous != null) {
            if (newClass != null) {
                _checkClass(previous, newClass, "entity named \"" + entityName
                        + "\" exists and is not an instance of " + className);
            }

            return previous;
        }

        // No previous entity.  Class name is required.
        _checkForNull(className, "Cannot create entity without a class name.");

        // Next check to see whether the class extends a named entity.
        if (reference == null || !reference.isClassDefinition()
                && newClass != null) {

            // Not a named entity. Instantiate a Java class.
            if (_current != null) {
                // Not a top-level entity.
                // First check that there will be no name collision
                // when this is propagated. Note that we need to
                // include all derived objects, irrespective of whether
                // they are locally changed.
                List derivedList = container.getDerivedList();
                Iterator derivedObjects = derivedList.iterator();

                while (derivedObjects.hasNext()) {
                    CompositeEntity derived = (CompositeEntity) derivedObjects
                            .next();

                    // The following call, if derived is lazy, will trigger
                    // its expansion. However, derived may contain an instance
                    // of the same class we are now trying to define, so
                    // the populate will delegate to this class definition,
                    // which will result in the class getting defined,
                    // and the collidingEntity being non-null.
                    // Entity collidingEntity = derived.getEntity(entityName);
                    // Hence, we have to scroll through the list of entities
                    // lazily, avoiding populating.
                    if (derived.getEntity(entityName) != null) {
                        // If the derived is within an EntityLibrary,
                        // then don't throw an exception.  To
                        // replicate this, create an EntityLibrary
                        // within the UserLibrary, save and then try
                        // to edit the UserLibrary.
                        boolean derivedIsNotWithinEntityLibrary = true;
                        CompositeEntity derivedContainer = derived;
                        while (derivedContainer != null
                                && (derivedIsNotWithinEntityLibrary = !(derivedContainer instanceof EntityLibrary))) {
                            derivedContainer = (CompositeEntity) derivedContainer
                                    .getContainer();
                        }
                        if (derivedIsNotWithinEntityLibrary) {
                            throw new IllegalActionException(
                                    container,
                                    "Cannot create entity named \""
                                            + entityName
                                            + "\" because a subclass or instance in \""
                                            + container.getFullName()
                                            + "\" contains an entity with the same name \""
                                            + derived.getEntity(entityName)
                                            .getFullName()
                                            + "\".  Note that this can happen when actor oriented class "
                                            + "definitions are LazyTypedCompositeActors.");
                        }
                    }

                    // Here's a possible solution to the above
                    // See actor/lib/test/auto/LazyAOCTestLazy.xml and LazyAOCTestNonLazy.xml
                    //                     List<ComponentEntity> possibleCollidingEntities = derived.lazyEntityList();
                    //                     for (ComponentEntity possibleCollidingEntity : possibleCollidingEntities) {
                    //                         if (possibleCollidingEntity.getName().equals(entityName)) {
                    //                             previous = _searchForEntity(entityName, _current);
                    //                             throw new IllegalActionException(
                    //                                     container,
                    //                                     "Cannot create entity named \"" + entityName
                    //                                             + "\" because a subclass or instance in \""
                    //                                             + container.getFullName()
                    //                                             + "\" contains an entity with the same name \""
                    //                                             + derived.getEntity(entityName).getFullName() + "\".");
                    //                         }
                    //                     }
                }

                _checkClass(_current, CompositeEntity.class,
                        "Cannot create an entity inside an element that "
                                + "is not a CompositeEntity. It is: "
                                + _current);

                Object[] arguments = new Object[2];

                arguments[0] = _current;
                arguments[1] = entityName;

                NamedObj newEntity = _createInstance(newClass, arguments);

                // Propagate existence, and then mark each newly created object as a class
                // if this is a class.
                List<InstantiableNamedObj> impliedObjects = newEntity
                        .propagateExistence();
                if (isClass) {
                    for (InstantiableNamedObj impliedObject : impliedObjects) {
                        impliedObject.setClassDefinition(true);
                    }
                }

                _loadIconForClass(className, newEntity);

                _addParamsToParamsToParse(newEntity);

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
                throw new MissingClassException(
                        "Attempt to extend an entity that "
                                + "is not a class: " + reference.getFullName()
                                + " className: " + className + " entityName: "
                                + entityName + " source: " + source,
                                reference.getFullName(), _currentExternalEntity(),
                                _getLineNumber(), _getColumnNumber());
            }

            // First check that there will be no name collision
            // when this is propagated. Note that we need to
            // include all derived objects, irrespective of whether
            // they are locally changed.
            // If the container is null, then we can't possibly get
            // a name collision.
            List derivedList = null;

            if (container != null) {
                derivedList = container.getDerivedList();

                Iterator derivedObjects = derivedList.iterator();

                while (derivedObjects.hasNext()) {
                    CompositeEntity derived = (CompositeEntity) derivedObjects
                            .next();

                    // The following call, if derived is lazy, will trigger
                    // its expansion. However, derived may contain an instance
                    // of the same class we are now trying to define, so
                    // the populate will delegate to this class definition,
                    // which will result in the class getting defined,
                    // and the collidingEntity being non-null.
                    // Entity collidingEntity = derived.getEntity(entityName);
                    // Hence, we have to scroll through the list of entities
                    // lazily, avoiding populating.
                    if (derived.getEntity(entityName) != null) {
                        throw new IllegalActionException(
                                container,
                                "Cannot create entity named \""
                                        + entityName
                                        + "\" because a subclass or instance in \""
                                        + container.getFullName()
                                        + "\" contains an entity with the same name \""
                                        + derived.getEntity(entityName)
                                        .getFullName()
                                        + "\".  Note that this can happen when actor oriented class "
                                        + "definitions are LazyTypedCompositeActors.");
                    }
                    // Here's a possible solution to the above
                    // See actor/lib/test/auto/LazyAOCTestLazy.xml and LazyAOCTestNonLazy.xml
                    //                     List<ComponentEntity> possibleCollidingEntities = derived.lazyEntityList();
                    //                     for (ComponentEntity possibleCollidingEntity : possibleCollidingEntities) {
                    //                         if (possibleCollidingEntity.getName().equals(entityName)) {
                    //                             previous = _searchForEntity(entityName, _current);
                    //                             throw new IllegalActionException(
                    //                                     container,
                    //                                     "Cannot create entity named \"" + entityName
                    //                                             + "\" because a subclass or instance in \""
                    //                                             + container.getFullName()
                    //                                             + "\" contains an entity with the same name \""
                    //                                             + derived.getEntity(entityName).getFullName() + "\".");
                    //                         }
                    //                     }
                }
            }

            // Instantiate it.
            ComponentEntity newEntity = (ComponentEntity) reference
                    .instantiate(container, entityName);

            // If we are keeping track of objects created...
            if (_topObjectsCreated != null && container == _originalContext) {
                _topObjectsCreated.add(newEntity);
            }

            // The original reference object may have had a URIAttribute,
            // but the new one should not. The clone would have copied
            // it.  The URIAttribute refers to the file in which the
            // component is defined, but the new entity is defined
            // in whatever file its container is defined. Leaving the
            // URIAttribute in the clone results in "look inside"
            // opening the clone but making it look as if it is the
            // original.
            // FIXME: This violates the derivation invariant.
            URIAttribute modelURI = (URIAttribute) newEntity.getAttribute(
                    "_uri", URIAttribute.class);

            if (modelURI != null) {
                modelURI.setContainer(null);
            }

            // Mark contents as needing evaluation.
            _markParametersToParse(newEntity);

            // Set the class name as specified in this method call.
            // This overrides what InstantiableNamedObj does.  The reason
            // we want to do that is that InstantiableNamedObj uses the
            // name of the object that we cloned as the classname.
            //  But this may not provide enough information to
            // instantiate the class.
            newEntity.setClassName(className);

            // Propagate.
            Iterator propagatedInstances = newEntity.propagateExistence()
                    .iterator();

            while (propagatedInstances.hasNext()) {
                ComponentEntity propagatedEntity = (ComponentEntity) propagatedInstances
                        .next();

                // If this is a class definition, then newly created instances should be too.
                if (isClass) {
                    propagatedEntity.setClassDefinition(true);
                }
                // Get rid of URI attribute that may have been cloned.
                // FIXME: Should that be done in the clone method
                // for URIAttribute?  Doesn't this violate the
                // derivation invariant?
                URIAttribute propagatedURI = (URIAttribute) propagatedEntity
                        .getAttribute("_uri", URIAttribute.class);

                if (propagatedURI != null) {
                    propagatedURI.setContainer(null);
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
     *  This method marks the contents of what it creates as derived objects,
     *  since they are defined in the Java code of the constructor.
     *  If we are currently propagating, then it also marks the new
     *  instance itself as a derived object.
     *  @param newClass The class.
     *  @param arguments The constructor arguments.
     *  @exception Exception If no matching constructor is found, or if
     *   invoking the constructor triggers an exception.
     */
    private NamedObj _createInstance(Class newClass, Object[] arguments)
            throws Exception {
        Constructor[] constructors = newClass.getConstructors();

        for (Constructor constructor : constructors) {
            Class[] parameterTypes = constructor.getParameterTypes();

            if (parameterTypes.length != arguments.length) {
                continue;
            }

            boolean match = true;

            for (int j = 0; j < parameterTypes.length; j++) {
                if (!parameterTypes[j].isInstance(arguments[j])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                NamedObj newEntity = (NamedObj) constructor
                        .newInstance(arguments);

                // Mark the contents of the new entity as being derived objects.
                _markContentsDerived(newEntity, 0);

                // If we are keeping track of objects created...
                if (_topObjectsCreated != null
                        && arguments[0] == _originalContext) {
                    _topObjectsCreated.add(newEntity);
                }

                // If the entity implements ScopeExtender, then add it to the list.
                if (newEntity instanceof ScopeExtender) {
                    if (_scopeExtenders == null) {
                        _scopeExtenders = new LinkedList<ScopeExtender>();
                    }
                    _scopeExtenders.add((ScopeExtender) newEntity);
                }

                return newEntity;
            }
        }

        // If we get here, then there is no matching constructor.
        // Generate a StringBuffer containing what we were looking for.
        StringBuffer argumentBuffer = new StringBuffer();

        for (int i = 0; i < arguments.length; i++) {
            argumentBuffer.append(arguments[i].getClass() + " = \""
                    + arguments[i].toString() + "\"");

            if (i < arguments.length - 1) {
                argumentBuffer.append(", ");
            }
        }

        throw new XmlException("Cannot find a suitable constructor ("
                + arguments.length + " args) (" + argumentBuffer + ") for '"
                + newClass.getName() + "'", _currentExternalEntity(),
                _getLineNumber(), _getColumnNumber());
    }

    /** Delete the entity after verifying that it is contained (deeply)
     *  by the current environment.  If no object is found, then do
     *  nothing and return null.  This is because deletion of a class
     *  may result in deletion of other objects that make this particular
     *  delete call redundant.
     *  @param entityName The relative or absolute name of the
     *   entity to delete.
     *  @return The deleted object, or null if none was found.
     *  @exception Exception If there is no such entity or if the entity
     *   is defined in the class definition.
     */
    private NamedObj _deleteEntity(String entityName) throws Exception {
        ComponentEntity toDelete = _searchForEntity(entityName, _current);

        if (toDelete == null) {
            return null;
        }

        // Ensure that derived objects aren't changed.
        if (toDelete.getDerivedLevel() < Integer.MAX_VALUE) {
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
        // we look for its derived objects, not the context's derived objects.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        // Note that deletion and undo need to occur in the opposite
        // order, so we first create the undo in the order given
        // by the derived list, then do the deletion in the
        // opposite order.
        try {
            Iterator derivedObjects = toDelete.getDerivedList().iterator();

            // NOTE: Deletion needs to occur in the reverse order from
            // what appears in the derived list. So first we construct
            // a reverse order list. The reason for this is that subclasses
            // appear before instances in the derived list, and we
            // need to delete the instances before we delete the
            // the subclasses.
            List reverse = new LinkedList();

            while (derivedObjects.hasNext()) {
                reverse.add(0, derivedObjects.next());
            }

            derivedObjects = reverse.iterator();

            while (derivedObjects.hasNext()) {
                ComponentEntity derived = (ComponentEntity) derivedObjects
                        .next();

                // Generate Undo commands.
                // Note that deleting an entity inside a class definition
                // may generate deletions in derived classes or instances.
                // The undo does not need to restore these (they are implied),
                // but if the deleted entities have overridden parameters,
                // then have to restore the overrides.  If the derived class
                // or instance is inside a different top level model, then
                // the MoML to do this cannot be in the same MoML with the
                // main undo because its execution context needs to be
                // different.  Hence, we collection additional MoMLUndoEntry
                // instances to carry out these overrides.
                // Note that we have to be careful to not re-establish
                // links, as these will duplicate the original links
                // (and, in fact, will throw an exception saying
                // that establishing links between ports defined
                // in the base class is not allowed). The following method
                // takes care of that.
                // Have to get this _before_ deleting.
                String toUndo = _getUndoForDeleteEntity(derived);
                NamedObj derivedToplevel = derived.toplevel();

                // Remove the derived object.
                derived.setContainer(null);

                // If the top level for the derived object is the same
                // as the top level for the object being deleted, then
                // we can simply insert this into the undo MoML.
                // Otherwise, it has to execute in a different context.
                if (derivedToplevel == _current.toplevel()) {
                    // Undo needs to occur in the opposite order because when
                    // we redo we need to create subclasses before instances.
                    undoMoML.insert(0, toUndo);
                } else {
                    MoMLUndoEntry entry = new MoMLUndoEntry(derivedToplevel,
                            toUndo);
                    _undoForOverrides.add(0, entry);
                }
            }

            // Have to get this _before_ deleting.
            String toUndo = _getUndoForDeleteEntity(toDelete);
            toDelete.setContainer(null);
            undoMoML.insert(0, toUndo);
        } finally {
            if (_undoEnabled) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
            }
        }

        return toDelete;
    }

    /** Delete the port after verifying that it is contained (deeply)
     *  by the current environment. If no object is found, then do
     *  nothing and return null.  This is because deletion of a class
     *  may result in deletion of other objects that make this particular
     *  delete call redundant.
     *  @param portName The relative or absolute name of the
     *   port to delete.
     *  @param entityName Optional name of the entity that contains
     *   the port (or null to use the current context).
     *  @return The deleted object, or null if none is found.
     *  @exception Exception If there is no such port or if the port
     *   is defined in the class definition.
     */
    private Port _deletePort(String portName, String entityName)
            throws Exception {
        Port toDelete = null;
        Entity portContainer = null;

        if (entityName == null) {
            toDelete = _searchForPort(portName);

            if (toDelete != null) {
                portContainer = (Entity) toDelete.getContainer();
            }
        } else {
            portContainer = _searchForEntity(entityName, _current);

            if (portContainer != null) {
                toDelete = portContainer.getPort(portName);
            }
        }

        if (toDelete == null) {
            return null;
        }

        if (portContainer == null) {
            throw new XmlException("No container for the port: " + portName,
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        // Ensure that derived objects aren't changed.
        if (toDelete.getDerivedLevel() < Integer.MAX_VALUE) {
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
        // we look for its derived objects, not the context's derived objects.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        // Note that deletion and undo need to occur in the opposite
        // order.  This is because connections need to removed in the instances
        // before the subclasses.
        try {
            Iterator derivedObjects = toDelete.getDerivedList().iterator();

            // NOTE: Deletion needs to occur in the reverse order from
            // what appears in the derived list. So first we construct
            // a reverse order list.
            List reverse = new LinkedList();

            while (derivedObjects.hasNext()) {
                reverse.add(0, derivedObjects.next());
            }

            derivedObjects = reverse.iterator();

            while (derivedObjects.hasNext()) {
                Port derived = (Port) derivedObjects.next();

                // Create the undo MoML.
                // Note that this is necessary because the ports
                // will have customized connections to the instances.
                // Have to get this _before_ deleting.
                // Put at the _start_ of the undo MoML, to ensure
                // reverse order from the deletion.
                // NOTE: This describes links to the
                // derived port.  Amazingly, the order
                // seems to be exactly right so that links
                // that will propagate on undo are no longer
                // present. So it seems to generate exactly
                // the right undo to not end up with duplicate connections!
                // Some care is needed, however.  If the implied port
                // is inside a different top level model, then
                // the MoML to do this cannot be in the same MoML with the
                // main undo because its execution context needs to be
                // different.  Hence, we collection additional MoMLUndoEntry
                // instances to carry out these overrides.
                // Have to get this before doing the deletion.
                // FIXME: Actually, this does seem to generate duplicate connections!
                // To replicate: In a new model, create an instance of Sinewave.
                // Look inside and delete the output port. Then undo.
                // This triggers an exception because it tries to recreate
                // the link in the derived object.
                String toUndo = _getUndoForDeletePort(derived);
                NamedObj derivedToplevel = derived.toplevel();

                derived.setContainer(null);

                // If the top level for the derived object is the same
                // as the top level for the object being deleted, then
                // we can simply insert this into the undo MoML.
                // Otherwise, it has to execute in a different context.
                if (derivedToplevel == _current.toplevel()) {
                    undoMoML.insert(0, toUndo);
                } else {
                    MoMLUndoEntry entry = new MoMLUndoEntry(derivedToplevel,
                            toUndo);
                    _undoForOverrides.add(0, entry);
                }
            }

            // Have to get this _before_ deleting.
            String toUndo = _getUndoForDeletePort(toDelete);
            toDelete.setContainer(null);
            undoMoML.insert(0, toUndo);
        } finally {
            if (_undoEnabled) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
            }
        }

        return toDelete;
    }

    /** Delete an attribute after verifying that it is contained (deeply)
     *  by the current environment. If no object is found, then do
     *  nothing and return null.  This is because deletion of a class
     *  may result in deletion of other objects that make this particular
     *  delete call redundant.
     *  @param attributeName The relative or absolute name of the
     *   attribute to delete.
     *  @return The deleted object, or null if none is found.
     *  @exception Exception If there is no such attribute or if the attribute
     *   is defined in the class definition.
     */
    private Attribute _deleteProperty(String attributeName) throws Exception {
        Attribute toDelete = _searchForAttribute(attributeName);

        if (toDelete == null) {
            return null;
        }

        // Ensure that derived objects aren't changed.
        if (toDelete.getDerivedLevel() < Integer.MAX_VALUE) {
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
        // we look for its derived objects, not the context's derived objects.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        try {
            // NOTE: Deletion can occur in the the same order as
            // what appears in the derived list. This is because attributes
            // cannot be classes, so there are no subclasses, and there are
            // no connections to remove.
            Iterator derivedObjects = toDelete.getDerivedList().iterator();

            String toUndo = _getUndoForDeleteAttribute(toDelete);

            NamedObj container = toDelete.getContainer();
            container.attributeDeleted(toDelete);
            toDelete.setContainer(null);
            undoMoML.append(toUndo);
            while (derivedObjects.hasNext()) {
                Attribute derived = (Attribute) derivedObjects.next();

                // Generate Undo commands.
                // Note that deleting an attribute inside a class definition
                // may generate deletions in derived classes or instances.
                // The undo does not need to restore these (they are implied),
                // but if the deleted attributes have overridden parameters,
                // then we have to restore the overrides.  If the derived class
                // or instance is inside a different top level model, then
                // the MoML to do this cannot be in the same MoML with the
                // main undo because its execution context needs to be
                // different.  Hence, we collection additional MoMLUndoEntry
                // instances to carry out these overrides.
                // Have to get this _before_ deleting.
                toUndo = _getUndoForDeleteAttribute(derived);
                NamedObj derivedToplevel = derived.toplevel();

                // Remove the derived object.
                derived.setContainer(null);

                // If the top level for the derived object is the same
                // as the top level for the object being deleted, then
                // we can simply insert this into the undo MoML.
                // Otherwise, it has to execute in a different context.
                if (derivedToplevel == _current.toplevel()) {
                    // Put at the _start_ of the undo MoML, to ensure
                    // reverse order from the deletion.
                    undoMoML.append(toUndo);
                } else {
                    MoMLUndoEntry entry = new MoMLUndoEntry(derivedToplevel,
                            toUndo);
                    _undoForOverrides.add(entry);
                }

            }
        } finally {
            if (_undoEnabled) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
            }
        }

        return toDelete;
    }

    /** Delete the relation after verifying that it is contained (deeply)
     *  by the current environment. If no object is found, then do
     *  nothing and return null.  This is because deletion of a class
     *  may result in deletion of other objects that make this particular
     *  delete call redundant.
     *  @param relationName The relative or absolute name of the
     *   relation to delete.
     *  @return The deleted object, or null if none is found.
     *  @exception Exception If there is no such relation or if the relation
     *   is defined in the class definition.
     */
    private Relation _deleteRelation(String relationName) throws Exception {
        ComponentRelation toDelete = _searchForRelation(relationName);

        if (toDelete == null) {
            return null;
        }

        if (toDelete.getDerivedLevel() < Integer.MAX_VALUE) {
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
        // we look for its derived objects, not the context's derived objects.
        // We have to do this before actually deleting it,
        // which also has the side effect of triggering errors
        // before the deletion.
        // Note that deletion and undo need to occur in the opposite
        // order.
        try {
            Iterator derivedObjects = toDelete.getDerivedList().iterator();

            // NOTE: Deletion needs to occur in the reverse order from
            // what appears in the derived objects list. So first we construct
            // a reverse order list. This is because connections in
            // instances need to be removed before connections
            // in subclasses.
            List reverse = new LinkedList();

            while (derivedObjects.hasNext()) {
                reverse.add(0, derivedObjects.next());
            }

            derivedObjects = reverse.iterator();

            while (derivedObjects.hasNext()) {
                ComponentRelation derived = (ComponentRelation) derivedObjects
                        .next();

                // Create the undo MoML.
                // Note that this is necessary because the relations
                // will have customized connections to the instances
                // and may have parameters that are overridden.
                // Put at the _start_ of the undo MoML, to ensure
                // reverse order from the deletion.
                // Some care is needed, however.  If the implied port
                // is inside a different top level model, then
                // the MoML to do this cannot be in the same MoML with the
                // main undo because its execution context needs to be
                // different.  Hence, we collection additional MoMLUndoEntry
                // instances to carry out these overrides.
                // Have to get this before doing the deletion.
                // FIXME: Actually, this seems to generate duplicate connections!
                // To replicate: In a new model, create an instance of Sinewave.
                // Look inside and delete the output port. Then undo.
                // This triggers an exception because it tries to recreate
                // the link in the derived object.
                // Have to get this _before_ deleting.
                String toUndo = _getUndoForDeleteRelation(derived);
                NamedObj derivedToplevel = derived.toplevel();

                derived.setContainer(null);

                // If the top level for the derived object is the same
                // as the top level for the object being deleted, then
                // we can simply insert this into the undo MoML.
                // Otherwise, it has to execute in a different context.
                if (derivedToplevel == _current.toplevel()) {
                    undoMoML.insert(0, toUndo);
                } else {
                    MoMLUndoEntry entry = new MoMLUndoEntry(derivedToplevel,
                            toUndo);
                    _undoForOverrides.add(0, entry);
                }
            }

            // Have to get this _before_ deleting.
            String toUndo = _getUndoForDeleteRelation(toDelete);
            toDelete.setContainer(null);
            undoMoML.insert(0, toUndo);
        } finally {
            if (_undoEnabled) {
                undoMoML.insert(0, "<group>");
                undoMoML.append("</group>\n");
                _undoContext.appendUndoMoML(undoMoML.toString());
            }
        }

        return toDelete;
    }

    /** Expand all the scope extenders that were encountered during parsing.
     *  Then, after expanding them all, validate them all.
     */
    private void _expandScopeExtenders() throws IllegalActionException {
        if (_scopeExtenders != null) {
            for (ScopeExtender extender : _scopeExtenders) {
                extender.expand();
            }
            // The above will create the parameters of the scope extender, but
            // not evaluate their expressions.
            // The following evaluates their expressions.
            // This has to be done as a separate pass because a scope extender
            // may have parameters whose values depend on parameters in another
            // scope extender.
            for (ScopeExtender extender : _scopeExtenders) {
                extender.validate();
            }
        }
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
    private NamedObj _findOrParse(MoMLParser parser, URL base, String file,
            String className, String source) throws Exception {
        URL previousXmlFile = parser._xmlFile;

        // Cache the modified flag so that if the file
        // we are opening is modified we don't accidentally
        // mark container file as modified.
        // Wireless SmartParking.xml had this problem because
        // LotSensor.xml has backward compat changes
        boolean modified = isModified();
        parser._setXmlFile(fileNameToURL(file, base));

        try {
            NamedObj toplevel = parser.parse(parser._xmlFile, parser._xmlFile);

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
            parser._setXmlFile(previousXmlFile);
            setModified(modified);
        }
    }

    /** Return the column number from the XmlParser.
     *  @return the column number from the XmlParser.  Return -1 if
     *  _xmlParser is null.
     */
    private int _getColumnNumber() {
        // _parser can be null if a method is called outside of a callback
        // for XmlParser.  All calls should go through parser(URL, Reader)
        if (_xmlParser == null) {
            return -1;
        }
        return _xmlParser.getColumnNumber();
    }

    // Construct a string representing the current XML element.
    private String _getCurrentElement(String elementName) {
        StringBuffer result = new StringBuffer();
        result.append("<");
        result.append(elementName);

        // Put the attributes into the character data.
        Iterator attributeNames = _attributeNameList.iterator();

        while (attributeNames.hasNext()) {
            String name = (String) attributeNames.next();
            String value = (String) _attributes.get(name);

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

    /** Return the line number from the XmlParser.
     *  @return the line number from the XmlParser.  Return -1 if
     *  _xmlParser is null.
     */
    private int _getLineNumber() {
        // _xmlParser can be null if a method is called outside of a callback
        // for XmlParser.  All calls should go through parser(URL, Reader)
        if (_xmlParser == null) {
            return -1;
        }
        return _xmlParser.getLineNumber();
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
     *  @exception XmlException If no such port is found.
     */
    private ComponentPort _getPort(String portspec, CompositeEntity context)
            throws XmlException {
        ComponentPort port = (ComponentPort) context.getPort(portspec);
        _checkForNull(
                port,
                "No port named \"" + portspec + "\" in "
                        + context.getFullName());
        return port;
    }

    /** Return the MoML commands to undo deleting the specified attribute
     *  from the current context.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeleteAttribute(Attribute toDelete)
            throws IOException {
        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(UndoContext.moveContextStart(
                _current, toDelete));

        int depth = toDelete.depthInHierarchy() - _current.depthInHierarchy();

        // Is it possible that _current doesn't contain toDelete?
        if (!_current.deepContains(toDelete)) {
            depth = 0;
        }

        // Add in the description.
        // Need to use the depth to determine how much MoML to export.
        StringWriter buffer = new StringWriter();
        toDelete.exportMoML(buffer, depth);
        moml.append(buffer.toString());

        // Finally move back to context if needed
        moml.append(UndoContext.moveContextEnd(_current, toDelete));

        // If there is no body, don't return the context either.
        if (buffer.toString().trim().equals("")) {
            return "";
        }

        return moml.toString();
    }

    /** Return the MoML commands to undo deleting the specified entity
     *  from the current context.  Unless the container implements
     *  the marker interface HandlesInternalLinks, this will generate
     *  undo MoML that takes care of re-creating any links that get
     *  broken as a result of deleting the specified entity.
     *  If both ends of the link are defined in the base class,
     *  however, then the link is not reported.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeleteEntity(ComponentEntity toDelete)
            throws IOException {
        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(UndoContext.moveContextStart(
                _current, toDelete));

        int depth = toDelete.depthInHierarchy() - _current.depthInHierarchy();

        // It possible that _current doesn't contain toDelete.
        // In particular, _current may be the container of an object
        // from which toDelete is derived. If this is the case, we still
        // want to generate the undo MoML as if toDelete were contained
        // by _current because, presumably, we will also be generating
        // undo MoML for the deletion of the master object inside _current.
        // Thus, we want the MoML generated here to only include overrides.
        if (depth < 0) {
            depth = 0;
        }

        // Add in the description.
        // Need to use the depth to determine how much MoML to export.
        StringWriter buffer = new StringWriter();
        toDelete.exportMoML(buffer, depth);
        moml.append(buffer.toString());

        CompositeEntity container = (CompositeEntity) toDelete.getContainer();

        if (container instanceof HandlesInternalLinks) {
            // If there is no body, don't return the context either.
            if (buffer.toString().trim().equals("")) {
                return "";
            }
            moml.append(UndoContext.moveContextEnd(_current, toDelete));
            return moml.toString();
        }

        // Now create the undo that will recreate any links
        // the are deleted as a side effect.
        // This is a little tricky, because if the entity
        // is defined in the base class, then we only want
        // to include links that override the base class.
        HashSet filter = new HashSet();
        if (toDelete.getDerivedLevel() == Integer.MAX_VALUE) {
            // The entity is not derived, so all links should be included.
            // NOTE: cannot use the relationlist as returned as it is
            // unmodifiable and we need to add in the entity being deleted.
            filter.addAll(toDelete.linkedRelationList());
        } else {
            // The entity is derived, so we should only include
            // relations that are not derived.
            Iterator relations = toDelete.linkedRelationList().iterator();
            while (relations.hasNext()) {
                Relation relation = (Relation) relations.next();
                if (relation != null) {
                    if (relation.getDerivedLevel() == Integer.MAX_VALUE) {
                        filter.add(relation);
                    }
                }
            }
        }
        filter.add(toDelete);

        String links = container.exportLinks(0, filter);
        // The parent container can do the filtering and generate the MoML.
        moml.append(links);

        // Finally move back to context if needed
        moml.append(UndoContext.moveContextEnd(_current, toDelete));

        // If there is no body, don't return the context either.
        if (buffer.toString().trim().equals("") && links.trim().equals("")) {
            return "";
        }

        return moml.toString();
    }

    /** Return the MoML commands to undo deleting the specified port
     *  from the current context. Unless the container implements
     *  the marker interface HandlesInternalLinks, this will generate
     *  undo MoML that takes care of re-creating any inside links that get
     *  broken as a result of deleting the specified port. Unless the
     *  container of the container implements HandlesInternalLinks,
     *  this will also generate undo to recreate the outside links.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeletePort(Port toDelete) throws IOException {
        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(UndoContext.moveContextStart(
                _current, toDelete));

        int depth = toDelete.depthInHierarchy() - _current.depthInHierarchy();

        // Is it possible that _current doesn't contain toDelete?
        if (!_current.deepContains(toDelete)) {
            depth = 0;
        }
        // Keep track of whether any moml code is actually generated.
        boolean bodyEmpty = true;

        // Add in the description.
        // Need to use the depth to determine how much MoML to export.
        StringWriter buffer = new StringWriter();
        toDelete.exportMoML(buffer, depth);
        if (!buffer.toString().trim().equals("")) {
            moml.append(buffer.toString());
            bodyEmpty = false;
        }

        NamedObj container = toDelete.getContainer();

        // Now create the undo that will recreate any links
        // the are deleted as a side effect.
        HashSet filter = new HashSet();
        // NOTE: cannot use the relationlist as returned as it is
        // unmodifiable and we need to add in the entity being deleted.
        filter.addAll(toDelete.linkedRelationList());
        if (container != null && !(container instanceof HandlesInternalLinks)) {
            if (toDelete instanceof ComponentPort) {
                filter.addAll(((ComponentPort) toDelete).insideRelationList());
            }
        }
        filter.add(toDelete);

        // Generate the undo MoML for the inside links, if there are any.
        if (container instanceof CompositeEntity) {
            String links = ((CompositeEntity) container).exportLinks(depth,
                    filter);
            if (!links.trim().equals("")) {
                moml.append(links);
                bodyEmpty = false;
            }
        }

        // Move back to context if needed.
        moml.append(UndoContext.moveContextEnd(_current, toDelete));

        // The undo MoML for the outside links is trickier.
        // We have to move up in the hierarchy, so we need to generate
        // an absolute context.
        if (container != null) {
            NamedObj containerContainer = container.getContainer();

            if (containerContainer instanceof CompositeEntity
                    && !(containerContainer instanceof HandlesInternalLinks)) {
                // Set the context to the container's container.
                moml.append(UndoContext.moveContextStart(_current, container));
                // In theory, depth should not be zero, but just in case...
                if (depth == 0) {
                    depth = 1;
                }
                String links = ((CompositeEntity) containerContainer)
                        .exportLinks(depth - 1, filter);
                if (!links.trim().equals("")) {
                    moml.append(links);
                    bodyEmpty = false;
                }
                moml.append(UndoContext.moveContextEnd(_current, container));
            }
        }
        // If there is no body, don't return MoML with just the context.
        if (bodyEmpty) {
            return "";
        }

        return moml.toString();
    }

    /** Return the MoML commands to undo deleting the specified relation
     *  from the current context. Unless the container implements
     *  the marker interface HandlesInternalLinks, this will generate
     *  undo MoML that takes care of re-creating any links that get
     *  broken as a result of deleting the specified relation.
     *  @param toDelete The component to delete.
     */
    private String _getUndoForDeleteRelation(ComponentRelation toDelete)
            throws IOException {
        // Set the context to the immediate container.
        StringBuffer moml = new StringBuffer(UndoContext.moveContextStart(
                _current, toDelete));

        // Add in the description.
        // By specifying the depth to be the depth in the hierarchy,
        // we ensure that if the object is implied, then no MoML
        // is exported, unless to override parameter values.
        int depth = toDelete.depthInHierarchy() - _current.depthInHierarchy();

        // Is it possible that _current doesn't contain toDelete?
        if (!_current.deepContains(toDelete)) {
            depth = 0;
        }

        // Add in the description.
        // Need to use the depth to determine how much MoML to export.
        StringWriter buffer = new StringWriter();
        toDelete.exportMoML(buffer, depth);
        moml.append(buffer.toString());

        CompositeEntity container = (CompositeEntity) toDelete.getContainer();

        if (container instanceof HandlesInternalLinks) {
            // If there is no body, don't return the context either.
            if (buffer.toString().trim().equals("")) {
                return "";
            }
            moml.append(UndoContext.moveContextEnd(_current, toDelete));
            return moml.toString();
        }

        // Generate undo to recreate the links.
        // This is a little tricky, because if the relation
        // is defined in the base class, then we only want
        // to include links that override the base class.
        HashSet filter = new HashSet();
        if (toDelete.getDerivedLevel() == Integer.MAX_VALUE) {
            // The entity is not derived, so all links should be included.
            // NOTE: cannot use the relationlist as returned as it is
            // unmodifiable and we need to add in the entity being deleted.
            filter.addAll(toDelete.linkedObjectsList());
        } else {
            // The relation is derived, so we should only include
            // relations that are not derived.
            Iterator objects = toDelete.linkedObjectsList().iterator();
            while (objects.hasNext()) {
                NamedObj portOrRelation = (NamedObj) objects.next();
                if (portOrRelation != null) {
                    if (portOrRelation.getDerivedLevel() == Integer.MAX_VALUE) {
                        filter.add(portOrRelation);
                    }
                }
            }
        }
        filter.add(toDelete);

        // The parent container can do the filtering and generate the
        // MoML.
        String links = container.exportLinks(0, filter);
        moml.append(links);
        if (buffer.toString().trim().equals("") && links.trim().equals("")) {
            // No body.
            return "";
        }

        // Move back to context if needed.
        moml.append(UndoContext.moveContextEnd(_current, toDelete));

        return moml.toString();
    }

    /** Create a property and/or set its value.
     *  @param className The class name field, if present.
     *  @param propertyName The property name field.
     *  @param value The value, if present.
     *  @exception Exception If something goes wrong.
     */
    private void _handlePropertyElement(String className, String propertyName,
            final String value) throws Exception {
        // First handle special properties that are not translated
        // into Ptolemy II attributes.
        // Note that we have to push something on to the
        // stack so that we can pop it off later.
        // An xml version of the FSM ABP demo tickled this bug
        boolean isIOPort = _current instanceof IOPort;

        if (propertyName.equals("multiport") && isIOPort) {
            // Special properties that affect the behaviour of a port
            // NOTE: UNDO: Consider refactoring these clauses
            // to remove the duplicate values
            // The previous value is needed to generate undo MoML
            IOPort currentIOPort = (IOPort) _current;

            // The mere presense of a named property "multiport"
            // makes the enclosing port a multiport, unless it
            // has value false.
            // Get the previous value to use when generating the
            // undo MoML
            boolean previousValue = currentIOPort.isMultiport();

            // Default for new value is true, unless it is explicitly false.
            boolean newValue = true;

            if (value != null
                    && value.trim().toLowerCase(Locale.getDefault())
                    .equals("false")) {
                newValue = false;
            }

            // If this object is a derived object, then its I/O status
            // cannot be changed.
            if (_current.getDerivedLevel() < Integer.MAX_VALUE
                    // FindBugs reports a problem with the cast, but
                    // isIOPort is set by checking whether _current is
                    // an instanceof IOPort, so the warning is superfluous
                    && ((IOPort) _current).isMultiport() != newValue) {
                throw new IllegalActionException(_current,
                        "Cannot change whether this port is "
                                + "a multiport. That property is fixed by "
                                + "the class definition.");
            }

            // FindBugs reports a problem with the cast, but
            // isIOPort is set by checking whether _current is
            // an instanceof IOPort, so the warning is superfluous
            ((IOPort) _current).setMultiport(newValue);

            // Propagate.
            Iterator derivedObjects = _current.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                IOPort derived = (IOPort) derivedObjects.next();
                derived.setMultiport(newValue);
            }

            _pushContext();
            _current = _current.getAttribute(propertyName);
            _namespace = _DEFAULT_NAMESPACE;

            // Handle undo
            if (_undoEnabled) {
                _undoContext.appendUndoMoML("<property name=\"" + propertyName
                        + "\" value=\"");

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
            IOPort currentIOPort = (IOPort) _current;

            // Get the previous value to use when generating the
            // undo MoML
            boolean previousValue = currentIOPort.isOutput();

            // Default for new value is true, unless it is explicitly false.
            boolean newValue = true;

            if (value != null
                    && value.trim().toLowerCase(Locale.getDefault())
                    .equals("false")) {
                newValue = false;
            }

            // If this object is a derived object, then its I/O status
            // cannot be changed.
            if (_current.getDerivedLevel() < Integer.MAX_VALUE
                    // FindBugs reports a problem with the cast, but
                    // isIOPort is set by checking whether _current is
                    // an instanceof IOPort, so the warning is superfluous
                    && ((IOPort) _current).isOutput() != newValue) {
                throw new IllegalActionException(_current,
                        "Cannot change whether this port is "
                                + "an output. That property is fixed by "
                                + "the class definition.");
            }

            // FindBugs reports a problem with the cast, but
            // isIOPort is set by checking whether _current is
            // an instanceof IOPort, so the warning is superfluous
            ((IOPort) _current).setOutput(newValue);

            // Propagate.
            Iterator derivedObjects = _current.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                IOPort derived = (IOPort) derivedObjects.next();
                derived.setOutput(newValue);
            }

            _pushContext();

            _current = _current.getAttribute(propertyName);
            _namespace = _DEFAULT_NAMESPACE;

            // Handle undo
            if (_undoEnabled) {
                _undoContext.appendUndoMoML("<property name=\"" + propertyName
                        + "\" value=\"");

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
            IOPort currentIOPort = (IOPort) _current;

            // Get the previous value to use when generating the
            // undo MoML
            boolean previousValue = currentIOPort.isInput();

            // Default for new value is true, unless it is explicitly false.
            boolean newValue = true;

            if (value != null
                    && value.trim().toLowerCase(Locale.getDefault())
                    .equals("false")) {
                newValue = false;
            }

            // If this object is a derived object, then its I/O status
            // cannot be changed.
            if (_current.getDerivedLevel() < Integer.MAX_VALUE
                    // FindBugs reports a problem with the cast, but
                    // isIOPort is set by checking whether _current is
                    // an instanceof IOPort, so the warning is superfluous
                    && ((IOPort) _current).isInput() != newValue) {
                throw new IllegalActionException(_current,
                        "Cannot change whether this port is "
                                + "an input. That property is fixed by "
                                + "the class definition.");
            }

            // FindBugs reports a problem with the cast, but
            // isIOPort is set by checking whether _current is
            // an instanceof IOPort, so the warning is superfluous
            ((IOPort) _current).setInput(newValue);

            // Propagate.
            Iterator derivedObjects = _current.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                IOPort derived = (IOPort) derivedObjects.next();
                derived.setInput(newValue);
            }

            _pushContext();
            _current = _current.getAttribute(propertyName);
            _namespace = _DEFAULT_NAMESPACE;

            // Handle undo
            if (_undoEnabled) {
                _undoContext.appendUndoMoML("<property name=\"" + propertyName
                        + "\" value=\"");

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
                property = _current.getAttribute(propertyName);
            }

            Class newClass = null;

            if (className != null) {
                try {
                    newClass = Class.forName(className, true, _classLoader);
                } catch (ClassNotFoundException ex) {
                    throw new XmlException("Failed to find class '" + className
                            + "'", _currentExternalEntity(), _getLineNumber(),
                            _getColumnNumber(), ex);
                } catch (NoClassDefFoundError ex) {
                    throw new XmlException("Failed to find class '" + className
                            + "'", _currentExternalEntity(), _getLineNumber(),
                            _getColumnNumber(), ex);
                } catch (SecurityException ex) {
                    // An applet might throw this.
                    throw new XmlException("Failed to find class '" + className
                            + "'", _currentExternalEntity(), _getLineNumber(),
                            _getColumnNumber(), ex);
                }
            }

            // If there is a previous property with this name
            // (property is not null), then we check that the
            // property is an instance of the specified class.
            // If it is, then we set the value of the property.
            // Otherwise, we try to replace it, something that
            // will only work if it is a singleton (it might throw
            // NameDuplicationException).
            boolean previouslyExisted = property != null;

            // Even if the object previously existed, if the
            // class does not match, we may create a new object.
            boolean createdNew = false;

            // Also need the previous value, if any, to generate undo MoML.
            String oldClassName = null;
            String oldValue = null;

            if (previouslyExisted) {
                oldClassName = property.getClass().getName();

                if (property instanceof Settable) {
                    Settable settable = (Settable) property;
                    oldValue = settable.getExpression();
                }
            }

            if (!previouslyExisted || newClass != null
                    && !newClass.isInstance(property)) {
                // The following will result in a
                // NameDuplicationException if there is a previous
                // property and it is not a singleton.
                try {
                    // No previously existing attribute with this name,
                    // or the class name of the previous entity doesn't
                    // match.
                    boolean isNewClassSetToAttribute = false;
                    if (newClass == null) {
                        isNewClassSetToAttribute = true;
                        newClass = Attribute.class;
                    }

                    // An attribute is not usually a top-level element,
                    // but it might be (e.g. when editing icons).
                    if (_current == null) {
                        // If we want to be able to edit icons, we
                        // have to allow this.
                        // Invoke the constructor.
                        Object[] arguments = new Object[2];
                        arguments[0] = _workspace;
                        arguments[1] = propertyName;
                        property = _createInstance(newClass, arguments);
                        _toplevel = property;
                    } else {
                        // First check that there will be no name collision
                        // when this is propagated. Note that we need to
                        // include all derived objects, irrespective of whether
                        // they are locally changed.
                        List derivedList = _current.getDerivedList();
                        Iterator derivedObjects = derivedList.iterator();

                        while (derivedObjects.hasNext()) {
                            NamedObj derived = (NamedObj) derivedObjects.next();
                            Attribute other = derived
                                    .getAttribute(propertyName);

                            if (other != null && !(other instanceof Singleton)) {
                                // If the derived is within an EntityLibrary,
                                // then don't throw an exception.  To
                                // replicate this, create an EntityLibrary
                                // within the UserLibrary, save and then try
                                // to edit the UserLibrary.
                                boolean derivedIsNotWithinEntityLibrary = true;
                                NamedObj derivedContainer = derived;
                                while (derivedContainer != null
                                        && (derivedIsNotWithinEntityLibrary = !(derivedContainer instanceof EntityLibrary))) {
                                    derivedContainer = derivedContainer
                                            .getContainer();
                                }
                                if (derivedIsNotWithinEntityLibrary) {
                                    throw new IllegalActionException(
                                            _current,
                                            "Cannot create attribute because a subclass or instance "
                                                    + "contains an attribute with the same name: "
                                                    + derived.getAttribute(
                                                            propertyName)
                                                            .getFullName());
                                }
                            }
                        }

                        // Invoke the constructor.
                        Object[] arguments = new Object[2];
                        arguments[0] = _current;
                        arguments[1] = propertyName;
                        property = _createInstance(newClass, arguments);

                        // Why was this restricted to Directors?? Efficiency?
                        // If this creates an efficiency problem, then we should
                        // create a marker interface that Director and WebServer,
                        // at least, implement.
                        // if (property instanceof ptolemy.actor.Director) {
                        if (className != null) {
                            // className can be null, to replicate:
                            // (cd $PTII/doc; make test)
                            _loadIconForClass(className, property);
                        }
                        // Check that the result is an instance of Attribute.
                        if (!(property instanceof Attribute)) {
                            // NOTE: Need to get rid of the object.
                            // Unfortunately, setContainer() is not defined,
                            // so we have to find the right class.
                            if (property instanceof ComponentEntity) {
                                ((ComponentEntity) property).setContainer(null);
                            } else if (property instanceof Port) {
                                ((Port) property).setContainer(null);
                            } else if (property instanceof ComponentRelation) {
                                ((ComponentRelation) property)
                                .setContainer(null);
                            }

                            throw new XmlException(
                                    "Property \""
                                            + property.getFullName()
                                            + "\" is not an "
                                            + "instance of Attribute, it is a \""
                                            + property.getClass().getName()
                                            + "\"."
                                            + (property instanceof Entity ? "The property is an instance "
                                                    + "of the Entity class, "
                                                    + "so if you were using "
                                                    + "the GUI, try \"Instantiate"
                                                    + "Entity\" or \"Instantiate"
                                                    + "Component\"."
                                                    : "")
                                                    + (isNewClassSetToAttribute ? " The class \""
                                                            + className
                                                            + "\" was not found in the "
                                                            + "classpath."
                                                            : ""),
                                                            _currentExternalEntity(), _getLineNumber(),
                                                            _getColumnNumber());
                        }

                        // Propagate.
                        property.propagateExistence();
                    }

                    if (value != null) {
                        if (property == null) {
                            throw new XmlException("Property does not exist: "
                                    + propertyName + "\n",
                                    _currentExternalEntity(), _getLineNumber(),
                                    _getColumnNumber());
                        }

                        if (!(property instanceof Settable)) {
                            throw new XmlException(
                                    "Property cannot be assigned a value: "
                                            + property.getFullName()
                                            + " (instance of "
                                            + property.getClass().toString()
                                            + ")\n", _currentExternalEntity(),
                                            _getLineNumber(), _getColumnNumber());
                        }

                        Settable settable = (Settable) property;
                        settable.setExpression(value);
                        _paramsToParse.add(settable);

                        // Propagate. This has the side effect of marking
                        // the object overridden.
                        property.propagateValue();
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
                                _currentExternalEntity(), _getLineNumber(),
                                _getColumnNumber());
                    }

                    Settable settable = (Settable) property;
                    //String previousValue = settable.getExpression();

                    // NOTE: It is not correct to do nothing even
                    // if the value is not changed.  If the value of
                    // of an instance parameter is explicitly set,
                    // and that value happens to be the same as the
                    // value in the base class, then it should keep
                    // that value even if the base class later changes.
                    // if (!value.equals(previousValue)) {
                    settable.setExpression(value);

                    // Propagate. This has the side effect of marking
                    // the object overridden.
                    property.propagateValue();

                    _paramsToParse.add(settable);
                }
            }

            _pushContext();
            _current = property;

            _namespace = _DEFAULT_NAMESPACE;

            // Handle the undo aspect if needed
            if (_undoEnabled) {
                if (!previouslyExisted) {
                    // Need to delete the property in the undo MoML
                    _undoContext.appendUndoMoML("<deleteProperty name=\""
                            + propertyName + "\" />\n");

                    // Do not need to continue generating undo MoML
                    // as the deleteProperty takes care of all
                    // contained MoML
                    _undoContext.setChildrenUndoable(false);
                    _undoContext.setUndoable(false);

                    // Prevent any further undo entries for this context.
                    _undoEnabled = false;
                } else {
                    // Simply generate the same as was there before.
                    _undoContext.appendUndoMoML("<property name=\""
                            + property.getName() + "\" ");
                    _undoContext.appendUndoMoML("class=\"" + oldClassName
                            + "\" ");

                    if (oldValue != null) {
                        // Escape the value for xml so that if
                        // the property the user typed in was
                        // a Parameter "foo", we do not have
                        // problems.  To replicate this,
                        // create a Const with a value "foo"
                        // and then change it to 2 and then
                        // try undo.
                        _undoContext.appendUndoMoML("value=\""
                                + StringUtilities.escapeForXML(oldValue)
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
     *  at the same level of hierarchy and are both derived objects, or if
     *  the relation and the container of the port are both class
     *  elements. If the relation is null, then this return true
     *  if the port and its container are derived objects.
     *  NOTE: This is not perfect, since a link could have been
     *  created between these elements in a subclass.
     *  @param context The context containing the link.
     *  @param port The port.
     *  @param relation The relation.
     *  @return True if the link is part of the class definition.
     */
    private boolean _isLinkInClass(NamedObj context, Port port,
            Relation relation) {
        // If the context is the container of the port, then
        // this is an inside link. In that case, even if this
        // is a level-crossing link, the port itself has to be a
        // derived object. Otherwise, we check to see whether the
        // container of the port is a derived object.
        int portContainerLevel = port.getContainer().getDerivedLevel();
        int portLevel = port.getDerivedLevel();

        // The decision is slightly different if the port is immediately
        // contained by the context because in that case we presume
        // it is an inside link. Since an inside link can only occur
        // with a relation deeply contained by the container of the
        // port, there is no need to check whether both the relation
        // and the link are derived by the same container. If they are
        // both derived, then they are both derived.
        if (port.getContainer() == context) {
            return portLevel < Integer.MAX_VALUE
                    && (relation == null || relation.getDerivedLevel() < Integer.MAX_VALUE);
        }
        boolean portIsInClass = port.getContainer() == context ? portLevel < Integer.MAX_VALUE
                : portContainerLevel < Integer.MAX_VALUE;
        if (portIsInClass) {
            if (relation == null) {
                // Inserting a blank link in a multiport.
                // NOTE: This used to return true, which would
                // trigger an exception. But why would this be disallowed?
                return false;
            }
            int relationLevel = relation.getDerivedLevel();
            if (relationLevel < Integer.MAX_VALUE) {
                // Check that the container above at which these two objects
                // are implied is the same container.
                NamedObj relationContainer = relation;
                while (relationLevel > 0) {
                    relationContainer = relationContainer.getContainer();
                    relationLevel--;
                    // It's not clear to me how this occur, but if relationCaontiner
                    // is null, then clearly there is no common container that
                    // implies the two objects.
                    if (relationContainer == null) {
                        return false;
                    }
                }
                NamedObj portContainer = port;
                // Handle inside links slightly differently from outside links.
                if (port.getContainer() == context) {
                    // Inside link.
                    while (portLevel > 0) {
                        portContainer = portContainer.getContainer();
                        portLevel--;
                        // It's not clear to me how this occur, but if relationCaontiner
                        // is null, then clearly there is no common container that
                        // implies the two objects.
                        if (portContainer == null) {
                            return false;
                        }
                    }
                } else {
                    // Outside link.
                    portContainer = port.getContainer();
                    while (portContainerLevel > 0) {
                        // It's not clear to me how this occur, but if relationCaontiner
                        // is null, then clearly there is no common container that
                        // implies the two objects.
                        if (portContainer == null) {
                            return false;
                        }
                        portContainer = portContainer.getContainer();
                        portContainerLevel--;
                    }
                }
                if (relationContainer == portContainer) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return true if the link between the specified relations
     *  is part of the class definition. It is part of the
     *  class definition if both are derived objects, and the
     *  container whose parent-child relationship makes them
     *  derived is the same container.
     *  NOTE: This is not perfect, since a link could have been
     *  created between these elements in a subclass.
     *  @param context The context containing the link.
     *  @param relation1 The first relation.
     *  @param relation2 The second relation.
     *  @return True if the link is part of the class definition.
     */
    private boolean _isLinkInClass(NamedObj context, Relation relation1,
            Relation relation2) {
        // Careful: It is possible for both relations to be derived
        // but the link not be defined within the same class definition.
        int relation1Level = relation1.getDerivedLevel();
        int relation2Level = relation2.getDerivedLevel();
        if (relation1Level < Integer.MAX_VALUE
                && relation2Level < Integer.MAX_VALUE) {
            // Check that the container above at which these two objects
            // are implied is the same container.
            NamedObj relation1Container = relation1;
            while (relation1Level > 0) {
                relation1Container = relation1Container.getContainer();
                relation1Level--;
                // It's not clear to me how this occur, but if relationCaontiner
                // is null, then clearly there is no common container that
                // implies the two objects.
                if (relation1Container == null) {
                    return false;
                }
            }
            NamedObj relation2Container = relation2;
            while (relation2Level > 0) {
                relation2Container = relation2Container.getContainer();
                relation2Level--;
                // It's not clear to me how this occur, but if relationCaontiner
                // is null, then clearly there is no common container that
                // implies the two objects.
                if (relation2Container == null) {
                    return false;
                }
            }
            if (relation1Container == relation2Container) {
                return true;
            }
        }
        return false;
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
        if (elementName.equals("property") || elementName.equals("class")
                || elementName.equals("doc")
                || elementName.equals("deleteEntity")
                || elementName.equals("deletePort")
                || elementName.equals("deleteProperty")
                || elementName.equals("deleteRelation")
                || elementName.equals("display")
                || elementName.equals("entity") || elementName.equals("group")
                || elementName.equals("link") || elementName.equals("port")
                || elementName.equals("relation")
                || elementName.equals("rename") || elementName.equals("unlink")
                || elementName.equals("vertex")) {
            return true;
        }

        return false;
    }

    /** If the file with the specified name exists, parse it in
     *  the context of the specified instance. If it does not
     *  exist, do nothing.  If the file creates new objects,
     *  then mark those objects as a class
     *  element with the same depth as the context.
     *  @param fileName The file name.
     *  @param context The context into which to load the file.
     *  @return True if a file was found.
     *  @exception Exception If the file exists but cannot be read
     *   for some reason.
     */
    private boolean _loadFileInContext(String fileName, NamedObj context)
            throws Exception {
        if (_classLoader == null) {
            throw new InternalErrorException(
                    "_classloader is null? "
                            + "If you are using Eclipse, then perhaps the ptII project is in the boothpath? "
                            + "Check Run -> Run Configurations... -> Classpath and be sure that the ptII project "
                            + "is not in the Bootstrap Entries section.");
        }
        URL xmlFile = _classLoader.getResource(fileName);

        if (xmlFile == null) {
            return false;
        }

        InputStream input = xmlFile.openStream();

        // Read the external file in the current context, but with
        // a new parser.  I'm not sure why the new parser is needed,
        // but the "input" element handler does the same thing.
        // NOTE: Should we keep the parser to re-use?
        boolean modified = isModified();
        MoMLParser newParser = new MoMLParser(_workspace, _classLoader);

        // setContext() calls reset(), which sets the modified
        // flag to false.  Thus, we cache the value of the modified
        // flag.
        // See test 13.2 in
        // $PTII/ptolemy/moml/filter/test/BackwardCompatibility.tcl
        // which has a backward compatibility problem and loads a filter.
        newParser.setContext(context);
        setModified(modified);

        // Create a list to keep track of objects created.
        newParser._topObjectsCreated = new LinkedList();

        // We don't need the results of the parse because
        // the context for the parser has been set so the
        // objects are already in the hierarchy.
        /*NamedObj result = */newParser.parse(_base, input);

        // Have to mark the contents derived objects, so that
        // the icon is not exported with the MoML export.
        Iterator objects = newParser._topObjectsCreated.iterator();

        while (objects.hasNext()) {
            NamedObj newObject = (NamedObj) objects.next();
            newObject.setDerivedLevel(1);
            _markContentsDerived(newObject, 1);
        }

        return true;
    }

    /** Look for a MoML file associated with the specified class
     *  name, and if it exists, parse it in the context of the
     *  specified instance.
     *  If {@link #setIconLoader(IconLoader)} has been called with
     *  a non-null argument, then invoke the
     *  {@link ptolemy.moml.IconLoader#loadIconForClass(String, NamedObj)}
     *  method.  If {@link #setIconLoader(IconLoader)} has <b>not</b>
     *  been called, or was called with a null argument, then
     *  The file name is constructed from
     *  the class name by replacing periods with file separators
     *  ("/") and appending "Icon.xml".  So, for example, for
     *  the class name "ptolemy.actor.lib.Ramp", if there is a
     *  file "ptolemy/actor/lib/RampIcon.xml" in the classpath
     *  then that file be read.
     *  @param className The class name.
     *  @param context The context into which to load the file.
     *  @return True if a file was found.
     *  @exception Exception If the file exists but cannot be read
     *   for some reason or if there is some other problem loading
     *   the icon.
     *  @see #setIconLoader(IconLoader)
     *  @see ptolemy.moml.IconLoader
     */
    private boolean _loadIconForClass(String className, NamedObj context)
            throws Exception {
        if (_iconLoader != null) {
            return _iconLoader.loadIconForClass(className, context);
        } else {
            // Default behavior if no icon loader has been specified.
            String fileName = className.replace('.', '/') + "Icon.xml";
            return _loadFileInContext(fileName, context);
        }
    }

    /** Mark the contents as being derived objects at a depth
     *  one greater than the depth argument, and then recursively
     *  mark their contents derived.
     *  This makes them not export MoML, and prohibits name and
     *  container changes. Normally, the argument is an Entity,
     *  but this method will accept any NamedObj.
     *  This method also adds all (deeply) contained instances
     *  of Settable to the _paramsToParse list, which ensures
     *  that they will be validated.
     *  @param object The instance that is defined by a class.
     *  @param depth The depth (normally 0).
     */
    private void _markContentsDerived(NamedObj object, int depth) {
        // NOTE: It is necessary to mark objects deeply contained
        // so that we can disable deletion and name changes.
        // While we are at it, we add any
        // deeply contained Settables to the _paramsToParse list.
        Iterator objects = object.lazyContainedObjectsIterator();

        while (objects.hasNext()) {
            NamedObj containedObject = (NamedObj) objects.next();
            containedObject.setDerivedLevel(depth + 1);

            if (containedObject instanceof Settable) {
                _paramsToParse.add((Settable) containedObject);
            }

            _markContentsDerived(containedObject, depth + 1);
        }
    }

    /** Mark deeply contained parameters as needing validation.
     *  This method adds all (deeply) contained instances
     *  of Settable to the _paramsToParse list, which ensures
     *  that they will be validated.
     *  @param object The instance to mark.
     */
    private void _markParametersToParse(NamedObj object) {
        Iterator objects = object.lazyContainedObjectsIterator();

        while (objects.hasNext()) {
            NamedObj containedObject = (NamedObj) objects.next();

            if (containedObject instanceof Settable) {
                _paramsToParse.add((Settable) containedObject);
            }

            _markParametersToParse(containedObject);
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
        _setXmlFile(fileNameToURL(source, base));

        InputStream input = null;

        try {
            input = _xmlFile.openStream();

            try {
                NamedObj toplevel = parser.parse(_xmlFile, input);
                input.close();
                return toplevel;
            } catch (CancelException ex) {
                // Parse operation cancelled.
                return null;
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable throwable) {
                    System.out.println("Ignoring failure to close stream "
                            + "on " + _xmlFile);
                    throwable.printStackTrace();
                }
            }

            _setXmlFile(null);
        }
    }

    /** Process a link command between two relations.
     *  @param relation1Name The first relation name.
     *  @param relation2Name The second relation name.
     *  @exception XmlException
     *  @exception IllegalActionException
     */
    private void _processLink(String relation1Name, String relation2Name)
            throws XmlException, IllegalActionException {
        _checkClass(_current, CompositeEntity.class,
                "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity. It is: " + _current);

        // Check that required arguments are given
        if (relation1Name == null || relation2Name == null) {
            throw new XmlException("Element link requires two relations.",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        CompositeEntity context = (CompositeEntity) _current;

        // Get relations.
        ComponentRelation relation1 = context.getRelation(relation1Name);
        _checkForNull(relation1, "No relation named \"" + relation1Name
                + "\" in " + context.getFullName());

        // Get relations.
        ComponentRelation relation2 = context.getRelation(relation2Name);
        _checkForNull(relation2, "No relation named \"" + relation2Name
                + "\" in " + context.getFullName());

        // Ensure that derived objects aren't changed.
        // We have to prohit adding links between class
        // elements because this operation cannot be undone, and
        // it will not be persistent.
        if (_isLinkInClass(context, relation1, relation2)) {
            throw new IllegalActionException(relation1, relation2,
                    "Cannot link relations when both"
                            + " are part of the class definition.");
        }

        relation1.link(relation2);

        // Propagate. Get the derived list for relation1,
        // then use its container as the context in which to
        // find relation2.
        Iterator derivedObjects = relation1.getDerivedList().iterator();

        while (derivedObjects.hasNext()) {
            ComponentRelation derivedRelation1 = (ComponentRelation) derivedObjects
                    .next();
            CompositeEntity derivedContext = (CompositeEntity) derivedRelation1
                    .getContainer();
            ComponentRelation derivedRelation2 = derivedContext
                    .getRelation(relation2Name);
            derivedRelation1.link(derivedRelation2);
        }

        // Handle the undo aspect.
        if (_undoEnabled) {
            // Generate a link in the undo only if one or the other relation is
            // not derived. If they are both derived, then the link belongs to
            // the class definition and should not be undone in undo.
            if (relation1.getDerivedLevel() == Integer.MAX_VALUE
                    || relation2.getDerivedLevel() == Integer.MAX_VALUE) {
                // Enclose in a group to prevent deferral on undo.
                _undoContext.appendUndoMoML("<group><unlink relation1=\""
                        + relation1Name + "\" relation2=\"" + relation2Name
                        + "\" /></group>\n");
            }
        }
    }

    /** Process a link command between a port and a relation.
     *  @param portName The port name.
     *  @param relationName The relation name.
     *  @param insertAtSpec The place to insert.
     *  @param insertInsideAtSpec The place to insert inside.
     *  @exception XmlException
     *  @exception IllegalActionException
     */
    private void _processLink(String portName, String relationName,
            String insertAtSpec, String insertInsideAtSpec)
                    throws XmlException, IllegalActionException {
        _checkClass(_current, CompositeEntity.class,
                "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity. It is: " + _current);

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
            throw new XmlException("Element link requires at least one of "
                    + "an insertAt, an insertInsideAt, or a relation.",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        if (insertAtSpec != null && insertInsideAtSpec != null) {
            throw new XmlException("Element link requires at most one of "
                    + "insertAt and insertInsideAt, not both.",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        CompositeEntity context = (CompositeEntity) _current;

        // Parse port
        ComponentPort port = _getPort(portName, context);

        // Save to help generate undo MoML
        int origNumOutsideLinks = port.numLinks();
        int origNumInsideLinks = port.numInsideLinks();

        // Get relation if given
        ComponentRelation relation = null;

        if (relationName != null) {
            Relation tmpRelation = context.getRelation(relationName);
            _checkForNull(tmpRelation, "No relation named \"" + relationName
                    + "\" in " + context.getFullName());
            relation = (ComponentRelation) tmpRelation;
        }

        // Ensure that derived objects aren't changed.
        // We have to prohibit adding links between class
        // elements because this operation cannot be undone, and
        // it will not be persistent.
        if (_isLinkInClass(context, port, relation)) {
            throw new IllegalActionException(port,
                    "Cannot link a port to a relation when both"
                            + " are part of the class definition.");
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

        // Propagate. Get the derived list for the relation,
        // then use its container as the context in which to
        // find the port. NOTE: The relation can be null
        // (to insert an empty link in a multiport), so
        // we have two cases to consider.
        if (relation != null) {
            Iterator derivedObjects = relation.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                ComponentRelation derivedRelation = (ComponentRelation) derivedObjects
                        .next();
                CompositeEntity derivedContext = (CompositeEntity) derivedRelation
                        .getContainer();
                ComponentPort derivedPort = _getPort(portName, derivedContext);

                // NOTE: Duplicate the above logic exactly.
                if (insertAtSpec != null) {
                    derivedPort.insertLink(insertAt, derivedRelation);
                } else if (insertInsideAtSpec != null) {
                    derivedPort.insertInsideLink(insertInsideAt,
                            derivedRelation);
                } else {
                    derivedPort.link(derivedRelation);
                }
            }
        } else {
            Iterator derivedObjects = port.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                ComponentPort derivedPort = (ComponentPort) derivedObjects
                        .next();

                // NOTE: Duplicate the above logic exactly.
                if (insertAtSpec != null) {
                    derivedPort.insertLink(insertAt, null);
                } else if (insertInsideAtSpec != null) {
                    derivedPort.insertInsideLink(insertInsideAt, null);
                } else {
                    // This one probably shouldn't occur.
                    derivedPort.link(null);
                }
            }
        }

        // Handle the undo aspect.
        if (_undoEnabled) {
            // NOTE: always unlink using an index
            // NOTE: do not use a relation name as that unlinks
            // all links to that relation from the given port
            if (relation == null) {
                // Generate a link in the undo only if the port is
                // not derived. If it is derived, then the link belongs to
                // the class definition and should not be undone in undo.
                if (port.getDerivedLevel() == Integer.MAX_VALUE) {
                    // Handle null links insertion first. Either an
                    // insertAt or an insertInsideAt must have been used.
                    // NOTE: we need to check if the number of links
                    // actually changed as a null link beyond the index of
                    // the first real link has no effect
                    if (insertAt != -1) {
                        if (port.numLinks() != origNumOutsideLinks) {
                            // Enclose in a group to prevent deferral on undo.
                            _undoContext
                            .appendUndoMoML("<group><unlink port=\""
                                    + portName + "\" index=\""
                                    + insertAtSpec + "\" /></group>\n");
                        }
                    } else {
                        if (port.numInsideLinks() != origNumInsideLinks) {
                            // Enclose in a group to prevent deferral on undo.
                            _undoContext
                            .appendUndoMoML("<group><unlink port=\""
                                    + portName + "\" insideIndex=\""
                                    + insertInsideAtSpec
                                    + "\" /></group>\n");
                        }
                    }
                }
            } else {
                // Generate a link in the undo only if the port or relation is
                // not derived. If they are both derived, then the link belongs to
                // the class definition and should not be undone in undo.
                if (port.getDerivedLevel() == Integer.MAX_VALUE
                        || relation.getDerivedLevel() == Integer.MAX_VALUE) {
                    // The relation name was given, see if the link was
                    // added inside or outside

                    if (port.numInsideLinks() != origNumInsideLinks) {
                        if (insertInsideAt == -1) {
                            insertInsideAt = port.numInsideLinks() - 1;
                        }
                        // Enclose in a group to prevent deferral on undo.
                        // Handle deleting links in reverse order so that if
                        // we copy and paste the undo/redo works out
                        _undoContext
                        .appendClosingUndoMoML("<group><unlink port=\""
                                + portName + "\" insideIndex=\""
                                + insertInsideAt + "\" /></group>"
                                + "\n");
                    } else if (port.numLinks() != origNumOutsideLinks) {
                        if (insertAt == -1) {
                            insertAt = port.numLinks() - 1;
                        }
                        // Enclose in a group to prevent deferral on undo.
                        // Handle deleting links in reverse order so that if
                        // we copy and paste the undo/redo works out
                        _undoContext
                        .appendClosingUndoMoML("<group><unlink port=\""
                                + portName + "\" index=\"" + insertAt
                                + "\" /></group>" + "\n");
                    } else {
                        // No change so do not need to generate any undo MoML
                    }
                }
            }
        }
    }

    /** Process pending link and delete requests, if any.
     *  @exception Exception If something goes wrong.
     */
    private void _processPendingRequests() throws Exception {
        if (_linkRequests != null) {
            Iterator requests = _linkRequests.iterator();

            while (requests.hasNext()) {
                LinkRequest request = (LinkRequest) requests.next();

                // Be sure to use the handler if these fail so that
                // we continue to the next link requests.
                try {
                    request.execute();
                } catch (Exception ex) {
                    if (_handler != null) {
                        int reply = _handler.handleError(request.toString(),
                                _current, ex);

                        if (reply == ErrorHandler.CONTINUE) {
                            continue;
                        } else if (reply == ErrorHandler.CANCEL) {
                            // NOTE: Since we have to throw an XmlException for
                            // the exception to be properly handled, we communicate
                            // that it is a user cancellation with the special
                            // string pattern "*** Canceled." in the message.
                            throw new XmlException("*** Canceled.",
                                    _currentExternalEntity(), _getLineNumber(),
                                    _getColumnNumber());
                        }
                    } else {
                        // No handler.  Throw the original exception.
                        throw ex;
                    }
                }
            }
        }

        // Process delete requests that have accumulated in
        // this element.
        if (_deleteRequests != null) {
            Iterator requests = _deleteRequests.iterator();

            while (requests.hasNext()) {
                DeleteRequest request = (DeleteRequest) requests.next();

                // Be sure to use the handler if these fail so that
                // we continue to the next link requests.
                try {
                    request.execute();
                } catch (Exception ex) {
                    if (_handler != null) {
                        int reply = _handler.handleError(request.toString(),
                                _current, ex);

                        if (reply == ErrorHandler.CONTINUE) {
                            continue;
                        } else if (reply == ErrorHandler.CANCEL) {
                            // NOTE: Since we have to throw an XmlException for
                            // the exception to be properly handled, we communicate
                            // that it is a user cancellation with the special
                            // string pattern "*** Canceled." in the message.
                            throw new XmlException("*** Canceled.",
                                    _currentExternalEntity(), _getLineNumber(),
                                    _getColumnNumber());
                        }
                    } else {
                        // No handler.  Throw the original exception.
                        throw ex;
                    }
                }
            }
        }
    }

    /** Process an unlink request between two relations.
     *  @param relation1Name The first relation name.
     *  @param relation2Name The second relation name.
     *  @exception XmlException If something goes wrong.
     *  @exception IllegalActionException If the link is part of a class definition.
     */
    private void _processUnlink(String relation1Name, String relation2Name)
            throws XmlException, IllegalActionException {
        // Check that required arguments are given
        if (relation1Name == null || relation2Name == null) {
            throw new XmlException("Element unlink requires two relations.",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        CompositeEntity context = (CompositeEntity) _current;

        // Get relations.
        ComponentRelation relation1 = context.getRelation(relation1Name);
        _checkForNull(relation1, "No relation named \"" + relation1Name
                + "\" in " + context.getFullName());

        // Get relations.
        ComponentRelation relation2 = context.getRelation(relation2Name);
        _checkForNull(relation2, "No relation named \"" + relation2Name
                + "\" in " + context.getFullName());

        // Ensure that derived objects aren't changed.
        // We have to prohit adding links between class
        // elements because this operation cannot be undone, and
        // it will not be persistent.
        if (_isLinkInClass(context, relation1, relation2)) {
            throw new IllegalActionException(relation1, relation2,
                    "Cannot unlink relations when both"
                            + " are part of the class definition.");
        }

        relation1.unlink(relation2);

        // Propagate. Get the derived list for relation1,
        // then use its container as the context in which to
        // find relation2.
        Iterator derivedObjects = relation1.getDerivedList().iterator();

        while (derivedObjects.hasNext()) {
            ComponentRelation derivedRelation1 = (ComponentRelation) derivedObjects
                    .next();
            CompositeEntity derivedContext = (CompositeEntity) derivedRelation1
                    .getContainer();
            ComponentRelation derivedRelation2 = derivedContext
                    .getRelation(relation2Name);
            derivedRelation1.unlink(derivedRelation2);
        }

        // Handle the undo aspect.
        if (_undoEnabled) {
            // Generate a link in the undo only if one or the other relation is
            // not derived. If they are both derived, then the link belongs to
            // the class definition and should not be recreated in undo.
            if (relation1.getDerivedLevel() == Integer.MAX_VALUE
                    || relation2.getDerivedLevel() == Integer.MAX_VALUE) {
                _undoContext.appendUndoMoML("<link relation1=\""
                        + relation1Name + "\" relation2=\"" + relation2Name
                        + "\" />\n");
            }
        }
    }

    /** Process an unlink request between a port and relation.
     *  @param portName The port name.
     *  @param relationName The relation name.
     *  @param indexSpec The index of the channel.
     *  @param insideIndexSpec The index of the inside channel.
     *  @exception XmlException If something goes wrong.
     *  @exception IllegalActionException If the link is part of a class definition.
     */
    private void _processUnlink(String portName, String relationName,
            String indexSpec, String insideIndexSpec) throws XmlException,
            IllegalActionException {
        _checkClass(_current, CompositeEntity.class,
                "Element \"unlink\" found inside an element that "
                        + "is not a CompositeEntity. It is: " + _current);

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
            throw new XmlException("Element unlink requires exactly one of "
                    + "an index, an insideIndex, or a relation.",
                    _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        CompositeEntity context = (CompositeEntity) _current;

        // Parse port
        ComponentPort port = _getPort(portName, context);

        // Get relation if given
        if (relationName != null) {
            Relation tmpRelation = context.getRelation(relationName);
            _checkForNull(tmpRelation, "No relation named \"" + relationName
                    + "\" in " + context.getFullName());

            ComponentRelation relation = (ComponentRelation) tmpRelation;

            // Ensure that derived objects aren't changed.
            if (_isLinkInClass(context, port, relation)) {
                throw new IllegalActionException(port,
                        "Cannot unlink a port from a relation when both"
                                + " are part of the class definition.");
            }

            // Handle the undoable aspect.
            // Generate a link in the undo only if one or the other relation is
            // not derived. If they are both derived, then the link belongs to
            // the class definition and should not be recreated in undo.
            if (_undoEnabled
                    && (port.getDerivedLevel() == Integer.MAX_VALUE || relation
                    .getDerivedLevel() == Integer.MAX_VALUE)) {
                // Get the relation at the given index
                List linkedRelations = port.linkedRelationList();
                int index = linkedRelations.indexOf(tmpRelation);

                if (index != -1) {
                    // Linked on the outside...
                    _undoContext.appendUndoMoML("<link port=\"" + portName
                            + "\" insertAt=\"" + index + "\" relation=\""
                            + relationName + "\" />\n");
                } else {
                    List insideLinkedRelations = port.insideRelationList();
                    index = insideLinkedRelations.indexOf(tmpRelation);

                    // Linked on the inside.
                    _undoContext.appendUndoMoML("<link port=\"" + portName
                            + "\" insertInsideAt=\"" + index + "\" relation=\""
                            + relationName + "\" />\n");
                }
            }

            // Propagate. Get the derived list for the relation,
            // then use its container as the context in which to
            // find the port.
            Iterator derivedObjects = relation.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                ComponentRelation derivedRelation = (ComponentRelation) derivedObjects
                        .next();
                CompositeEntity derivedContext = (CompositeEntity) derivedRelation
                        .getContainer();
                ComponentPort derivedPort = _getPort(portName, derivedContext);
                derivedPort.unlink(derivedRelation);
            }

            port.unlink(relation);
        } else if (indexSpec != null) {
            // index is given.
            int index = Integer.parseInt(indexSpec);

            // Ensure that derived objects aren't changed.
            // Unfortunately, getting the relation is fairly
            // expensive.
            List relationList = port.linkedRelationList();
            if (relationList.size() <= index) {
                throw new IllegalActionException(port, "Cannot unlink index "
                        + indexSpec + ", because there is no such link.");
            }
            Relation relation = (Relation) relationList.get(index);

            if (_isLinkInClass(context, port, relation)) {
                throw new IllegalActionException(port,
                        "Cannot unlink a port from a relation when both"
                                + " are part of the class definition.");
            }

            // Handle the undoable aspect before doing the unlinking.
            // Generate a link in the undo only if one or the other relation is
            // not derived. If they are both derived, then the link belongs to
            // the class definition and should not be recreated in undo.
            if (_undoEnabled) {
                // Get the relation at the given index.
                List linkedRelations = port.linkedRelationList();
                Relation r = (Relation) linkedRelations.get(index);
                // Generate undo moml only if either the port is
                // not derived or there is a relation and it is not derived.
                if (port.getDerivedLevel() == Integer.MAX_VALUE || r != null
                        && r.getDerivedLevel() == Integer.MAX_VALUE) {
                    // FIXME: need to worry about vertex?
                    _undoContext.appendUndoMoML("<link port=\"" + portName
                            + "\" insertAt=\"" + indexSpec + "\" ");

                    // Only need to specify the relation if there was
                    // a relation at that index. Otherwise a null
                    // link is inserted
                    if (r != null) {
                        _undoContext.appendUndoMoML("relation=\""
                                + r.getName(context) + "\" ");
                    }
                    _undoContext.appendUndoMoML(" />\n");
                }
            }

            // Propagate.
            Iterator derivedObjects = port.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                ComponentPort derivedPort = (ComponentPort) derivedObjects
                        .next();
                derivedPort.unlink(index);
            }

            port.unlink(index);
        } else {
            // insideIndex is given.
            int index = Integer.parseInt(insideIndexSpec);

            // Ensure that derived objects aren't changed.
            // Unfortunately, getting the relation is fairly
            // expensive.
            List relationList = port.insideRelationList();
            Relation relation = (Relation) relationList.get(index);

            if (_isLinkInClass(context, port, relation)) {
                throw new IllegalActionException(port,
                        "Cannot unlink a port from a relation when both"
                                + " are part of the class definition.");
            }

            // Handle the undoable aspect  before doing the unlinking
            if (_undoEnabled) {
                // Get the relation at the given index
                List linkedRelations = port.insideRelationList();
                Relation r = (Relation) linkedRelations.get(index);
                // Generate undo moml only if either the port is
                // not derived or there is a relation and it is not derived.
                if (port.getDerivedLevel() == Integer.MAX_VALUE || r != null
                        && r.getDerivedLevel() == Integer.MAX_VALUE) {
                    // FIXME: need to worry about vertex?
                    _undoContext.appendUndoMoML("<link port=\"" + portName
                            + "\" insertInsideAt=\"" + index + "\" ");

                    // Only need to specify the relation if there was
                    // a relation at that index. Otherwise a null
                    // link is inserted
                    if (r != null) {
                        _undoContext.appendUndoMoML("relation=\""
                                + r.getName(context) + "\" ");
                    }
                    _undoContext.appendUndoMoML(" />\n");
                }
            }

            // Propagate.
            Iterator derivedObjects = port.getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                ComponentPort derivedPort = (ComponentPort) derivedObjects
                        .next();
                derivedPort.unlinkInside(index);
            }

            port.unlinkInside(index);
        }
    }

    /** Push the current context.
     */
    private void _pushContext() {
        _containers.push(_current);
        _namespaces.push(_namespace);
        _namespace = _DEFAULT_NAMESPACE;
        _namespaceTranslations.push(_namespaceTranslationTable);
        _namespaceTranslationTable = new HashMap();
        _namespacesPushed = true;
    }

    /** Reset the undo information to give a fresh setup for the next
     *  incremental change. NOTE: this resets all the undo information except
     *  for the UndoStackAttribute which is associated with the model.
     */
    private void _resetUndo() {
        _undoContext = null;
        _undoContexts = new Stack();
        _undoEnabled = false;
        _undoForOverrides.clear();
    }

    /** Given a name that is either absolute (with a leading period)
     *  or relative to _current, find an attribute with that name.
     *  The attribute is required to
     *  be contained (deeply) by the current environment, or an XmlException
     *  will be thrown.
     *  @param name The name of the attribute, relative or absolute.
     *  @return The attribute.
     *  @exception XmlException If the attribute is not found.
     */
    private Attribute _searchForAttribute(String name) throws XmlException {
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

        if (_current != null) {
            // Now we are assured that name is relative.
            result = _current.getAttribute(name);
        } else {
            // Findbugs suggests checking for null
            throw new XmlException("The current object in the hierarchy "
                    + "is null? Could not find property: " + name + " in "
                    + currentName, _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        if (result == null) {
            throw new XmlException("No such property: " + name + " in "
                    + currentName, _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
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
     *  @exception Exception If a source is specified and it cannot
     *   be opened.
     */
    private ComponentEntity _searchForClassInContext(String name, String source)
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
            context = context.getContainer();

            if (context instanceof CompositeEntity) {
                candidate = ((CompositeEntity) context).getEntity(name);
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
                URI sourceURI = fileNameToURL(source, _base).toURI();
                URI candidateSourceURI = fileNameToURL(candidateSource, _base)
                        .toURI();

                // FIXME: URL.equals() is very expensive?  See:
                // http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html
                if (sourceURI.equals(candidateSourceURI)) {
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
     *  @exception XmlException If the name refers to an entity in an
     *   inappropriate context or if the context is not an instance
     *   of CompositeEntity.
     * @exception IllegalActionException If the name is ambiguous in that
     *  more than one entity in workspace matches. This will only occur
     *  if the name is absolute and is not inside the current model.
     */
    private ComponentEntity _searchForEntity(String name, NamedObj context)
            throws XmlException, IllegalActionException {
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
                     _getLineNumber(),
                     _getColumnNumber());
                     }
                     */
                    return (ComponentEntity) _toplevel;
                } else {
                    if (name.length() > nextPeriod + 1) {
                        ComponentEntity result = ((CompositeEntity) _toplevel)
                                .getEntity(name.substring(nextPeriod + 1));

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
                             _getLineNumber(),
                             _getColumnNumber());
                             }
                             */
                            return result;
                        }
                    }
                }
            } else {
                // Name of the top level doesn't match.
                // NOTE: It is tempting to try to find the object within the workspace.
                // However, the names of objects in the workspace are not unique,
                // so if there is more than one with a matching name, which should be
                // returned? Hence, we don't permit MoML to reach into another toplevel
                // through the absolute naming mechanism.  Just return null.
            }

            return null;
        } else {
            // Name is relative.
            if (context instanceof CompositeEntity) {
                ComponentEntity result = ((CompositeEntity) context)
                        .getEntity(name);
                return result;
            }

            if (context == null) {
                // The name might be a top-level name, but without
                // the leading period.
                return _searchForEntity("." + name, /* context*/null);
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
     *  @exception XmlException If the port is not found.
     */
    private Port _searchForPort(String name) throws XmlException {
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
            result = ((Entity) _current).getPort(name);
        }

        if (result == null) {
            throw new XmlException("No such port: " + name + " in "
                    + topLevelName, _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
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
     *  @exception XmlException If the relation is not found.
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
            result = ((CompositeEntity) _current).getRelation(name);
        }

        if (result == null) {
            throw new XmlException("No such relation: " + name + " in "
                    + topLevelName, _currentExternalEntity(), _getLineNumber(),
                    _getColumnNumber());
        }

        return result;
    }

    /** Store the value of the file being read.  We do this for performance
     *  reasons because URL.toString() is expensive.  For large models,
     *  caching results in a 2x speed-up in opening time under Mac OS X.
     */
    private void _setXmlFile(URL xmlFile) {
        _xmlFile = xmlFile;
        _xmlFileName = _xmlFile != null ? _xmlFile.toString() : null;
    }

    /** Add a class name to the list of missing classes.
     *  @param className the class name to be added.
     */
    private void _updateMissingClasses(String className) {
        if (_missingClasses == null) {
            _missingClasses = new HashSet<String>();
        }
        _missingClasses.add(className);
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

    // Indentification of what is being deleted for the _delete() method.
    private static int _DELETE_ENTITY = 0;

    private static int _DELETE_PORT = 1;

    private static int _DELETE_PROPERTY = 2;

    private static int _DELETE_RELATION = 3;

    // List of delete requests.
    private List _deleteRequests;

    // Stack of lists of delete requests.
    private Stack _deleteRequestStack = new Stack();

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

    /** MoMLParser to use with the MoMLFilters.  We share one
     *  MoMLParser between the filters, _filterMoMLParser is passed as
     *  an argument.  Each filter should call setContext(container) on
     *  the passed-in filter.  Since setContext() calls reset(), we
     *  don't want to pass in the main MoMLParser.  We share one
     *  MoMLParser so as to avoid the expense of constructing one each
     *  time we read an attribute.
     */
    private static MoMLParser _filterMoMLParser = null;

    /** IconLoader used to load icons. */
    private static IconLoader _iconLoader;

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

    // Indicator that namespaces have been pushed on the stack.
    private boolean _namespacesPushed = false;

    // The current translation table for names.
    private Map _namespaceTranslationTable = null;

    // The stack of maps for name translations.
    private Stack _namespaceTranslations = new Stack();

    // The original context set by setContext().
    private NamedObj _originalContext = null;

    // A set of settable parameters specified in property tags.
    private Set<Settable> _paramsToParse = new HashSet<Settable>();

    /** A list of scope extenders encountered while parsing. */
    private List<ScopeExtender> _scopeExtenders;

    /** The XmlParser. */
    private XmlParser _xmlParser;

    // Status of the deferral of the top-level.
    private boolean _previousDeferStatus = false;

    // The stack of integers that represent the state of <if> elements. The top
    // integer always represent the state of the last <if> element, or the
    // initial state if no <if> element has been reached. If it is > 1, then the
    // elements within the <if> block should be ignored. If it is 1, then the
    // next input must be </if>, which closes the <if> block. If it is 0, then
    // all the elements are processed normally.
    private Stack _ifElementStack;

    // List of missing actors.
    private Set<String> _missingClasses;

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

    // List of top-level objects created by a parse operation.
    // If this is list is non-null when parse() is called, it will
    // be populated with a list of instance of NamedObj that are
    // created at the top level of the parse.  Note that these
    // may not be top-level objects.
    private List _topObjectsCreated = null;

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

    // Holds additional undo commands that have to execute in a
    // different context.
    private List<UndoAction> _undoForOverrides = new LinkedList<UndoAction>();

    // List of unrecognized elements.
    private List _unrecognized;

    // The workspace for this model.
    private Workspace _workspace;

    /** The XML file being read, if any.  Do not set _xmlFile directly,
     *  instead call _setXmlFile().
     */
    private URL _xmlFile = null;

    /** The name of the XMLFile, which we cache for performance reasons. */
    private String _xmlFileName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // Class to record a deletion request.
    // The first argument to the constructor should be one of
    // _DELETE_ENTITY, _DELETE_PORT, _DELETE_PROPERTY, or _DELETE_RELATION.
    // The second should be the name of the object to delete.
    // The third (optional) argument should be the name of the context,
    // or null to use the current context (only valid for _DELETE_PORT).
    private class DeleteRequest {
        public DeleteRequest(int type, String name, String context) {
            _type = type;
            _name = name;
            _context = context;
        }

        public NamedObj execute() throws Exception {
            if (_type == _DELETE_ENTITY) {
                return _deleteEntity(_name);
            } else if (_type == _DELETE_PORT) {
                return _deletePort(_name, _context);
            } else if (_type == _DELETE_PROPERTY) {
                return _deleteProperty(_name);
            } else {
                return _deleteRelation(_name);
            }
        }

        private int _type;

        private String _name;

        private String _context;
    }

    // Class that records a link request.
    private class LinkRequest {
        // This constructor is used to link two relations into
        // the same relation group.
        public LinkRequest(String relation1Name, String relation2Name) {
            _relationName = relation1Name;
            _relation2Name = relation2Name;
        }

        // This constructor is used to link a port and a relation.
        public LinkRequest(String portName, String relationName,
                String insertAtSpec, String insertInsideAtSpec) {
            _portName = portName;
            _relationName = relationName;
            _indexSpec = insertAtSpec;
            _insideIndexSpec = insertInsideAtSpec;
        }

        public void execute() throws IllegalActionException, XmlException {
            if (_portName != null) {
                _processLink(_portName, _relationName, _indexSpec,
                        _insideIndexSpec);
            } else {
                _processLink(_relationName, _relation2Name);
            }
        }

        @Override
        public String toString() {
            if (_portName != null) {
                return "link " + _portName + " to " + _relationName;
            } else {
                return "link " + _relationName + " to " + _relation2Name;
            }
        }

        protected String _portName;

        protected String _relationName;

        protected String _relation2Name;

        protected String _indexSpec;

        protected String _insideIndexSpec;
    }

    // Class that records a link request.
    private class UnlinkRequest extends LinkRequest {
        public UnlinkRequest(String portName, String relationName,
                String indexSpec, String insideIndexSpec) {
            super(portName, relationName, indexSpec, insideIndexSpec);
        }

        // This constructor is used to link two relations into
        // the same relation group.
        public UnlinkRequest(String relation1Name, String relation2Name) {
            super(relation1Name, relation2Name);
        }

        @Override
        public void execute() throws IllegalActionException, XmlException {
            if (_portName != null) {
                _processUnlink(_portName, _relationName, _indexSpec,
                        _insideIndexSpec);
            } else {
                _processUnlink(_relationName, _relation2Name);
            }
        }

        @Override
        public String toString() {
            return "unlink " + _portName + " from " + _relationName;
        }
    }
}
