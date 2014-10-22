/* An aggregation of typed actors, specified by a Ptalon program.

 Copyright (c) 2006-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.actor.ptalon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

import com.microstar.xml.XmlParser;

///////////////////////////////////////////////////////////////////
////PtalonActor

/**
 An aggregation of typed actors, specified by a Ptalon program.

 <p>A TypedCompositeActor is an aggregation of typed actors.  A
 PtalonActor is a TypedCompositeActor whose aggregation is specified
 by a Ptalon program in an external file.  This file is loaded during
 initialization, and is specified in a FileParameter called
 <i>ptalonCodeLocation</i>.

 <p>
 @author Adam Cataldo, Elaine Cheong, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (celaine)
 */
public class PtalonActor extends TypedCompositeActor implements Configurable {

    /** Construct a PtalonActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PtalonActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _initializePtalonActor();

        setClassName("ptolemy.actor.ptalon.PtalonActor");
        ptalonCodeLocation = new FileParameter(this, "ptalonCodeLocation");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by a
     *  contained attribute when its value changes.  This initially
     *  responds to changes in the <i>ptalonCodeLocation</i> parameter
     *  by calling a method which starts the parsing of the Ptalon
     *  code.  Later, this method responds to changes in parameters
     *  specified in the Ptalon code itself: (1) If the parameter was
     *  not previously assigned, then update the internal variables;
     *  (2) If the parameter was previously assigned, then update the
     *  internal variables, and also update the rest of the
     *  PtalonActor based on the new value of the parameter (we
     *  currently choose the reparse and regenerate the entire
     *  PtalonActor from scratch).
     *
     *  <p>We choose to implement this in attributeChanged() instead
     *  if preinitialize() because we want changes to be reflected
     *  immediately, since preinitialize() is only processed after the
     *  model starts to run.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not
     *  acceptable to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == ptalonCodeLocation) {
            // The location of the Ptalon file has been set, so start
            // the Ptalon compiler.
            _initializePtalonCodeLocation();
        } else if (attribute instanceof PtalonParameter) {
            PtalonParameter p = (PtalonParameter) attribute;
            if (_isValueChanged(p)) {
                try {
                    /*if (p.getVisibility().equals(Settable.NONE)) {
                        // Store parameters that are not settable by
                        // the user so that they do not get exported
                        // to MoML.
                        // @see #_exportMoMLContents(Writer output, int depth)
                        if (!_unsettablePtalonParameters.contains(p)) {
                            _unsettablePtalonParameters.add(p);
                        } else {
                            return;
                        }
                    }*/

                    // Check if the parameter was not previously assigned.
                    if (!_assignedPtalonParametersCopy.containsKey(p.getName())) {
                        // The value of the parameter was not
                        // previously assigned, so add it to
                        // _assignedPtalonParameters and save a copy
                        // by creating a clone.
                        _assignedPtalonParameters.add(p);

                        _assignedPtalonParametersCopy.put(p.getName(),
                                (PtalonParameter) p.clone(null));
                        _assignedPtalonParametersCopyValues.put(p.getName(),
                                p.getToken());
                    } else {
                        // The value of the parameter was previously
                        // assigned, and therefore the parameter value
                        // must have changed (we checked this at the
                        // beginning of this method).

                        // Store a new copy of the parameter and its
                        // token value.
                        _assignedPtalonParametersCopy.put(p.getName(),
                                (PtalonParameter) p.clone(null));
                        _assignedPtalonParametersCopyValues.put(p.getName(),
                                p.getToken());

                        _removeContents();

                        // Reset this PtalonActor and reparse the
                        // Ptalon code in order to regenerate the
                        // inside of this actor.
                        _initializePtalonActor();
                        _initializePtalonCodeLocation();
                        return;
                    }

                    // Check to see if all of the PtalonParameters for
                    // this actor have been assigned values.
                    boolean ready = true;
                    for (PtalonParameter param : _ptalonParameters) {
                        if (!param.hasValue()) {
                            // There is a PtalonParameter whose value
                            // is unassigned.
                            ready = false;
                            break;
                        }
                    }
                    if (ready) {
                        PtalonPopulator populator = new PtalonPopulator();
                        populator
                        .setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
                        populator.actor_definition(_ast, _codeManager);
                        _ast = (PtalonAST) populator.getAST();
                        _codeManager.assignInternalParameters();
                    }
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to process attribute change to "
                                    + "PtalonParameter, whose value is \""
                                    + attribute + "\".");
                }
            }
        }
    }

    /** Read the saved XML for this PtalonActor.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong.
     */
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
        try {
            if (base != null) {
                _configureSource = base.toExternalForm();
            }
            if (text != null && !text.trim().equals("")) {
                XmlParser parser = new XmlParser();
                PtalonMLHandler handler = new PtalonMLHandler(this);
                parser.setHandler(handler);
                parser.parse(_configureSource, null, new StringReader(text));
                _removeEntity(null);
            }
        } catch (Exception ex) {
            //e.printStackTrace();
            throw ex;
        }
    }

    /** Return the input source that was specified the last time the
     *  configure() method was called.
     *  @return The string representation of the input URL, or null if
     *  the no source has been used to configure this object, or null
     *  if no external source need be used to configure this object.
     */
    @Override
    public String getConfigureSource() {
        return _configureSource;
    }

    /** Override the default behavior to always return null.
     *  @return null.
     */
    @Override
    public String getConfigureText() {
        return null;
    }

    /** Get the stored unique name for a symbol in the PtalonActor.
     *  The unique name comes from a call to uniqueName().
     *  @param ptalonName The symbol.
     *  @return The unique name.
     *  @exception PtalonRuntimeException If no such symbol exists.
     */
    public String getMappedName(String ptalonName)
            throws PtalonRuntimeException {
        return _codeManager.getMappedName(ptalonName);
    }

    /** Return the depth of this PtalonActor declaration with respect
     *  to its creator.  If this PtalonActor is not created by another
     *  PtalonActor's code, then the depth is zero.  If, however, this
     *  PtalonActor is named Bar in some PtalonCode, and it is created
     *  with Foo(actorparameter0 := Bar()), then its depth will be 2,
     *  and the corresponding Foo container will have depth 1.
     *  @return The depth of this actor declaration with respect to
     *  its creator.
     *  @see #setNestedDepth
     */
    public int getNestedDepth() {
        return _nestedDepth;
    }

    /** Find the parameter in the Ptalon code with the specified name,
     *  and return the Ptolemy parameter (java) that was created.
     *  @param name The name of the parameter in the Ptalon code,
     *  which may be a prefix of the actual parameter's name.
     *  @return The PtalonParameter.
     *  @exception PtalonRuntimeException If no such PtalonParameter
     *  exists.
     */
    public PtalonParameter getPtalonParameter(String name)
            throws PtalonRuntimeException {
        try {
            String uniqueName = _codeManager.getMappedName(name);
            PtalonParameter param = (PtalonParameter) getAttribute(uniqueName);
            return param;
        } catch (Exception ex) {
            throw new PtalonRuntimeException("Unable to access parameter "
                    + name, ex);
        }
    }

    /** Set the depth of this PtalonActor declaration with respect to
     *  its creator.  If this PtalonActor is not created by another
     *  PtalonActor's code, then the depth will be zero.  If, however,
     *  this PtalonActor is named Bar in some PtalonCode, and it is
     *  created with Foo(actorparameter0 := Bar()), then it's depth
     *  will be 2, and the corresponding Foo container will have depth
     *  1.
     *  @param depth The of this actor declaration with respect to its
     *  creator.
     *  @see #getNestedDepth
     */
    public void setNestedDepth(int depth) {
        _nestedDepth = depth;
    }

    /** Return a name that is guaranteed to not be the name of any
     *  contained attribute, port, class, entity, or relation.  In
     *  this implementation, the argument is stripped of any numeric
     *  suffix, and then a numeric suffix is appended and incremented
     *  until a name is found that does not conflict with a contained
     *  attribute, port, class, entity, or relation.  If this
     *  composite entity or any composite entity that it contains
     *  defers its MoML definition (i.e., it is an instance of a class
     *  or a subclass), then the prefix gets appended with
     *  "_<i>n</i>_", where <i>n</i> is the depth of this
     *  deferral. That is, if the object deferred to also defers, then
     *  <i>n</i> is incremented.  This differs from the superclass in
     *  that a number is always appended in this class.  This is
     *  mainly important for visualization of the created actors and
     *  their displayed names.
     *  @param prefix A prefix for the name.
     *  @return A unique name.
     */
    @Override
    public String uniqueName(String prefix) {
        if (prefix == null) {
            prefix = "null";
        }

        String candidate = prefix;

        // NOTE: The list returned by getPrototypeList() has length
        // equal to the number of containers of this object that
        // return non-null to getParent(). That number is assured to
        // be at least one greater than the corresponding number for
        // any of the parents returned by getParent().  Hence, we can
        // use that number to minimize the likelyhood of inadvertent
        // capture.
        try {
            int depth = getPrototypeList().size();

            if (depth > 0) {
                prefix = prefix + "_" + depth + "_";
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Derivation invariant is not satisfied.");
        }

        int uniqueNameIndex = 1;

        while (getAttribute(candidate) != null || getPort(candidate) != null
                || getEntity(candidate) != null
                || getRelation(candidate) != null) {
            candidate = prefix + uniqueNameIndex++;
        }

        return candidate;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public members                     ////

    /** The location of the Ptalon code.
     */
    public FileParameter ptalonCodeLocation;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add the attribute, and if the attribute is a PtalonParameter,
     *  add it to a list of Ptalon parameters.
     *  @param attribute The attribute to be added.
     *  @exception NameDuplicationException If the superclass throws it.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    protected void _addAttribute(Attribute attribute)
            throws NameDuplicationException, IllegalActionException {
        super._addAttribute(attribute);
        if (attribute instanceof PtalonParameter) {
            _ptalonParameters.add((PtalonParameter) attribute);
        }
    }

    protected PtalonEvaluator _createPtalonEvaluator(PtalonActor actor) {
        return new PtalonEvaluator(actor);
    }

    protected PtalonPopulator _createPtalonPopulator() {
        return new PtalonPopulator();
    }

    /** Create Ptalon parser. This function simply constructs a {@link
     *  PtalonRecognizer} instance with the given lexer. Subclasses may override
     *  this function to return a customized parser.
     *
     *  @param lexer The lexer.
     *  @return The Ptalon parser.
     */
    protected PtalonRecognizer _createPtalonRecognizer(PtalonLexer lexer) {
        return new PtalonRecognizer(lexer);
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class is the configuration information. This method is
     *  called by exportMoML().  Each description is indented
     *  according to the specified depth and terminated with a newline
     *  character.  Note that this only saves the values of the Ptalon
     *  parameters; it does not save other entities generated as a
     *  result of parsing the Ptalon file.
     *  @param output The output stream for writing.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        for (Object att : attributeList()) {
            if (!(att instanceof Parameter)) {
                Attribute attribute = (Attribute) att;
                attribute.exportMoML(output, depth);
            }
        }
        if (_astCreated) {
            // Get the name of the Ptalon file.
            String filename;
            try {
                filename = ptalonCodeLocation.asURL().toString();
            } catch (IllegalActionException ex) {
                IOException ex2 = new IOException(
                        "Unable to get valid file name "
                                + "from ptalonCodeLocation: \""
                                + ptalonCodeLocation + "\".");
                ex2.initCause(ex);
                throw ex2;
            }

            // Strip postfix.
            if (!filename.toLowerCase(Locale.getDefault()).endsWith(".ptln")) {
                throw new IOException("Ptalon file does not end with "
                        + "postfix .ptln.");
            }
            filename = filename.substring(0, filename.length() - 5);

            if (filename.startsWith("file:/")) {
                filename = filename.substring(5);
            }
            if (filename.startsWith("$CLASSPATH")) {
                filename = filename.substring(9);
            }
            String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
            File ptIIDirFile = new File(ptIIDir);
            String prefix = ptIIDirFile.toURI().toString();
            if (prefix.startsWith("file:/")) {
                prefix = prefix.substring(5);
            }
            String displayName;
            if (filename.toLowerCase(Locale.getDefault()).startsWith(
                    prefix.toLowerCase(Locale.getDefault()))) {
                int i = 0;
                while (filename.startsWith("/")) {
                    filename = filename.substring(1);
                    i++;
                }
                displayName = filename.substring(prefix.length() - i);
            } else {
                displayName = filename;
            }
            displayName = displayName.replace('/', '.');

            // Write the name of the Ptalon file.
            output.write(_getIndentPrefix(depth) + "<configure>\n");
            output.write(_getIndentPrefix(depth + 1) + "<ptalon file=\""
                    + displayName + "\">\n");

            // Write the name and value of all Ptalon parameters.
            for (PtalonParameter param : _assignedPtalonParameters) {
                if (!_unsettablePtalonParameters.contains(param)) {
                    if (param instanceof PtalonExpressionParameter) {
                        String expression = param.getExpression();
                        expression = expression.replaceAll("\"", "\\&quot\\;");
                        output.write(_getIndentPrefix(depth + 2)
                                + "<ptalonExpressionParameter name=\""
                                + param.getName() + "\" value=\"" + expression
                                + "\"/>\n");
                    } else {
                        output.write(_getIndentPrefix(depth + 2)
                                + "<ptalonParameter name=\"" + param.getName()
                                + "\" value=\"" + param.getExpression()
                                + "\"/>\n");
                    }
                }
            }
            output.write(_getIndentPrefix(depth + 1) + "</ptalon>\n");
            output.write(_getIndentPrefix(depth) + "</configure>\n");
        }
    }

    /** Initialize this PtalonActor.  This method may be called when
     *  the PtalonActor is first constructed or if any of its
     *  parameter values are changed, so we only initialize
     *  variables that do not need to be saved when reparsing the
     *  Ptalon file.
     */
    protected void _initializePtalonActor() {
        _astCreated = false;
        _ast = null;
        _codeManager = null;
        _nestedDepth = 0;
    }

    /** This helper method is used to begin the Ptalon compiler if the
     *  ptalonCodeLocation attribute has been updated.
     * @exception IllegalActionException If any exception is thrown.
     */
    protected void _initializePtalonCodeLocation()
            throws IllegalActionException {
        try {
            if (_astCreated) {
                ptalonCodeLocation.setVisibility(Settable.NONE);
                return;
            }
            // Open the code location.  Use urls for WebStart and jar files.
            PtalonLexer lex = null;
            PtalonRecognizer rec = null;
            InputStream inputStream = null;
            URL inputURL = null;
            try {
                inputURL = ptalonCodeLocation.asURL();
                if (inputURL == null) {
                    // We are probably just starting up
                    return;
                }
            } catch (IllegalActionException ex) {
                // We might be under WebStart, try it as a jar URL
                inputURL = Thread.currentThread().getContextClassLoader()
                        .getResource(ptalonCodeLocation.getExpression());
                if (inputURL == null) {
                    throw new IllegalActionException(this, ex,
                            "Failed to open " + "\""
                                    + ptalonCodeLocation.getExpression()
                                    + "\" as a file or jar URL");
                }
            }

            try {
                inputStream = inputURL.openStream();
                lex = new PtalonLexer(inputStream);

                rec = _createPtalonRecognizer(lex);
                rec.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
                rec.actor_definition();
            } catch (IOException ex2) {
                throw new IllegalActionException(this, ex2, "Failed to open \""
                        + inputURL + "\".");
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex4) {
                        // Ignored, but print anyway
                        ex4.printStackTrace();
                    }
                }
            }
            _ast = (PtalonAST) rec.getAST();
            PtalonScopeChecker checker = new PtalonScopeChecker();
            checker.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
            _codeManager = _createPtalonEvaluator(this);
            checker.actor_definition(_ast, _codeManager);
            _ast = (PtalonAST) checker.getAST();
            _codeManager = checker.getCodeManager();
            PtalonPopulator populator = _createPtalonPopulator();
            populator.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
            try {
                populator.actor_definition(_ast, _codeManager);
                _ast = (PtalonAST) populator.getAST();
            } catch (PtalonRuntimeException e) {
                if (_codeManager.hasUnassignedParameters()) {
                    // Ignore because we will set the parameters next and
                    // re-populate the Ptalon actor. At that time, the model may
                    // be valid.
                } else {
                    throw e;
                }
            }
            _astCreated = true;
            ptalonCodeLocation.setVisibility(Settable.NOT_EDITABLE);
            _codeManager.assignInternalParameters();
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to process " + "the ptalonCodeLocation \""
                            + ptalonCodeLocation + "\"");
        }
    }

    protected void _removeContents() {
        // Delete the inside of this PtalonActor by
        // removing all entities and relations.  We do
        // not remove ports, but they become unlinked
        // when the attached entities and relations
        // are removed.
        removeAllEntities();
        removeAllRelations();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the value of the given PtalonParameter has
     *  changed.  This method checks for newly assigned values (where
     *  the value was not previously assigned), changed expression
     *  values, and changed token values (as a result of a change to
     *  the value of the underlying expression).
     *  @see #attributeChanged(Attribute)
     *  @param p The PtalonParameter to check.
     *  @return true If value has changed.
     *  @exception IllegalActionException If unable to compare the token
     *  value of p with its previously stored value.
     */
    private boolean _isValueChanged(PtalonParameter p)
            throws IllegalActionException {
        if (!p.hasValue()) {
            // The parameter does not have a value.
            return true;
        } else if (_assignedPtalonParametersCopy.containsKey(p.getName())) {
            // The value of the parameter was previously assigned.
            // Check to see if value has actually changed.
            PtalonParameter oldParameter = _assignedPtalonParametersCopy.get(p
                    .getName());
            if (!oldParameter.getExpression().equals(p.getExpression())) {
                // The parameter expressions are different.
                return true;
            } else {
                // The parameter expressions are the same.  Check to
                // see if the token values are the same.
                Token oldToken = _assignedPtalonParametersCopyValues.get(p
                        .getName());
                if (oldToken == null) {
                    // The old token value of the parameter is null
                    // because the token value has not yet been
                    // stored, since the call to this method in this
                    // case occurs as a result of a call to getToken()
                    // to store a copy of the parameter token value (a
                    // reentrant call).  The value has not changed, so
                    // we return false.
                    return false;
                } else if (p.getToken().isEqualTo(oldToken).booleanValue()) {
                    // The token values has not changed.
                    return false;
                } else {
                    // The token value has changed.
                    return true;
                }
            }
        } else {
            // The parameter has a value, but it was not already
            // stored.
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private members                      ////

    /** A list of all ptalon parameters that have been assigned a
     *  value.  This is a subset of _ptalonParameters.
     */
    private List<PtalonParameter> _assignedPtalonParameters = new LinkedList<PtalonParameter>();

    /** A table of copies of all the elements in
     *  _assignedPtalonParameters.  In each entry of this table, the
     *  value is a copy of an element in _assignedPtalonParameters (a
     *  PtalonParameter) and the key is the name returned by
     *  getName().  This table is used to compare newly assigned
     *  expression values against previously assigned expression
     *  values.
     */
    private Hashtable<String, PtalonParameter> _assignedPtalonParametersCopy = new Hashtable<String, PtalonParameter>();

    /** A table of copies of token values of all of the elements in
     *  _assignedPtalonParameters.  In each entry of this table, the
     *  value is the token value of an element in
     *  _assignedPtalonParameters (a PtalonParameter) and the key is
     *  the name returned by getName().  This table is used to compare
     *  current token values against previous token values.
     */
    private Hashtable<String, Token> _assignedPtalonParametersCopyValues = new Hashtable<String, Token>();

    /** The abstract syntax tree for the PtalonActor.
     */
    private PtalonAST _ast;

    /** A boolean whose value is true if the AST has been created.
     */
    private boolean _astCreated;

    /** Information generated about the Ptalon code that is used by
     *  the compiler.
     */
    private PtalonEvaluator _codeManager;

    /** The text representation of the URL for this object.
     */
    private String _configureSource;

    /** The depth for this actor with respect to nested actor
     *  declarations.
     */
    private int _nestedDepth;

    /** The list of all ptalon parameters for this actor.
     */
    private List<PtalonParameter> _ptalonParameters = new LinkedList<PtalonParameter>();

    /** The list of all ptalon parameters who are not settable by the
     *  user.  This is a subset of _ptalonParameters.
     */
    private List<PtalonParameter> _unsettablePtalonParameters = new LinkedList<PtalonParameter>();;
}
