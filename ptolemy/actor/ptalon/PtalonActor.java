/* An aggregation of typed actors, specified by a Ptalon model.

 @Copyright (c) 1998-2006 The Regents of the University of California.
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
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

import com.microstar.xml.XmlParser;

//////////////////////////////////////////////////////////////////////////
////PtalonActor

/**
 A TypedCompositeActor is an aggregation of typed actors.  A PtalonActor
 is a TypedCompositeActor whose aggregation is specified by a Ptalon
 model in an external file.  This file is specified in a FileParameter, 
 and it is loaded during initialization.
 <p>

 @author Adam Cataldo
 @Pt.ProposedRating Red (acataldo)
 @Pt.AcceptedRating Red (acataldo)
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
        setClassName("ptolemy.actor.ptalon.PtalonActor");
        ptalonCodeLocation = new FileParameter(this, "ptalonCodeLocation");
        astCreated = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Set the parameter with paramName to have value
     * actorName.
     * 
     * @param paramName The parameter name.
     * @param actorName The contained actor name.
     * @throws PtalonRuntimeException If the parameter or actor
     * do not exist.
     */
    public String addActorParameter(String paramName, String actorName)
            throws PtalonRuntimeException {
        PtalonActor actor = (PtalonActor) getEntity(actorName);
        if (actor == null) {
            throw new PtalonRuntimeException("No such actor " + actorName);
        }
        PtalonParameter param = (PtalonParameter) getAttribute(paramName);
        if (param == null) {
            throw new PtalonRuntimeException("No such parameter " + paramName);
        }
        try {
            param.setToken(new StringToken(actorName));
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("Not able to set parameter "
                    + paramName + " to actor " + actorName, e);
        }
        param.setVisibility(Settable.NOT_EDITABLE);
        return "";
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This initally responds
     *  to changes in the <i>ptalonCode</i> parameter.  Later it responds
     *  to changes in parameters specified in the Ptalon code itself.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute att) throws IllegalActionException {
        super.attributeChanged(att);
        if (att == ptalonCodeLocation) {
            _initializePtalonCodeLocation();
        } else if (att instanceof PtalonParameter) {
            PtalonParameter p = (PtalonParameter) att;
            if ((p.hasValue())
                    && (!p.getVisibility().equals(Settable.NOT_EDITABLE))) {
                try {
                    if (p.getVisibility().equals(Settable.FULL)) {
                        p.setVisibility(Settable.NOT_EDITABLE);
                    } else if (p.getVisibility().equals(Settable.NONE)) {
                        if (_unsettablePtalonParameters.contains(p)) {
                            return;
                        } else {
                            _unsettablePtalonParameters.add(p);
                        }
                    }
                    _assignedPtalonParameters.add(p);
                    if ((_ast == null) || (_codeManager == null)) {
                        return;
                    }
                    boolean ready = true;
                    for (PtalonParameter param : _ptalonParameters) {
                        if (!param.hasValue()) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalActionException(this, e, e.getMessage());

                }
            }
        }
    }

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The object should interpret the
     *  source first, if it is specified, followed by the literal text,
     *  if that is specified.  The new configuration should usually
     *  override any old configuration wherever possible, in order to
     *  ensure that the current state can be successfully retrieved.
     *  <p>
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
        try {
            if (base != null) {
                _configureSource = base.toExternalForm();
            }
            if ((text != null) && (!text.trim().equals(""))) {
                XmlParser parser = new XmlParser();
                PtalonMLHandler handler = new PtalonMLHandler(this);
                parser.setHandler(handler);
                parser.parse(_configureSource, null, new StringReader(text));
                _removeEntity(null);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create a nested actor with respect to this code manager's
     * actor.
     * @param container The actor that will contain the created actor, which
     * should be a decendant of this code manager's actor.
     * @param uniqueName The unqique name for the nested actor declaration
     * this actor refers to.
     * @return The created actor.
     * @exception PtalonRuntimeException If there is any trouble creating this actor.
     */
    public ComponentEntity createNestedActor(PtalonActor container,
            String uniqueName) throws PtalonRuntimeException {
        return _codeManager.createNestedActor(container, uniqueName);
    }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL, or null if the
     *  no source has been used to configure this object, or null if no
     *  external source need be used to configure this object.
     */
    public String getConfigureSource() {
        return _configureSource;
    }

    public NestedActorManager getActorManager() {
        return _codeManager;
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    public String getConfigureText() {
        return null;
    }

    /**
     * Get the unique name for the symbol in the PtalonActor. 
     * @param ptalonName The symbol to test.
     * @return The unique name.
     * @exception PtalonRuntimeException If no such symbol exists.
     */
    public String getMappedName(String ptalonName)
            throws PtalonRuntimeException {
        return _codeManager.getMappedName(ptalonName);
    }

    /**
     * Return the depth of this actor declaration
     * with respect to its creator.  If this is 
     * not created by another PtalonActor's code, then the
     * depth will be zero.  If however, this actor is named
     * Bar in some PtalonCode, and it is created with
     * Foo(a := Bar()), then it's depth will be 2, and the
     * corresponding Foo container will have depth 1.
     * @return The depth of this actor declaration
     * with respect to its creator. 
     */
    public int getNestedDepth() {
        return _nestedDepth;
    }

    /**
     * Get the PtalonParameter with the name specified in
     * the Ptalon code.
     * @param name The name of the parameter in the Ptalon code,
     * which may be a prefix of the actual parameter's name.
     * @return The PtalonParameter
     * @exception PtalonRuntimeException If no such PtalonParameter exists.
     */
    public PtalonParameter getPtalonParameter(String name)
            throws PtalonRuntimeException {
        try {
            String uniqueName = _codeManager.getMappedName(name);
            PtalonParameter param = (PtalonParameter) getAttribute(uniqueName);
            return param;
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to access parameter "
                    + name, e);
        }
    }

    /**
     * Set the depth of this actor declaration
     * with respect to its creator.  If this is 
     * not created by another PtalonActor's code, then the
     * depth will be zero.  If however, this actor is named
     * Bar in some PtalonCode, and it is created with
     * Foo(a := Bar()), then it's depth will be 2, and the
     * corresponding Foo container will have depth 1.
     * @param depth The of this actor declaration
     * with respect to its creator. 
     */
    public void setNestedDepth(int depth) {
        _nestedDepth = depth;
    }

    /** Return a name that is guaranteed to not be the name of
     *  any contained attribute, port, class, entity, or relation.
     *  In this implementation, the argument
     *  is stripped of any numeric suffix, and then a numeric suffix
     *  is appended and incremented until a name is found that does not
     *  conflict with a contained attribute, port, class, entity, or relation.
     *  If this composite entity or any composite entity that it contains
     *  defers its MoML definition (i.e., it is an instance of a class or
     *  a subclass), then the prefix gets appended with "_<i>n</i>_",
     *  where <i>n</i> is the depth of this deferral. That is, if the object
     *  deferred to also defers, then <i>n</i> is incremented.  This differs
     *  from the superclass in that the first "_<i>n</i>_" appended to the
     *  prefix is "1" rather than "2".
     *  @param prefix A prefix for the name.
     *  @return A unique name.
     */
    public String uniqueName(String prefix) {
        if (prefix == null) {
            prefix = "null";
        }

        String candidate = prefix;

        // NOTE: The list returned by getPrototypeList() has
        // length equal to the number of containers of this object
        // that return non-null to getParent(). That number is
        // assured to be at least one greater than the corresponding
        // number for any of the parents returned by getParent().
        // Hence, we can use that number to minimize the likelyhood
        // of inadvertent capture.
        try {
            int depth = getPrototypeList().size();

            if (depth > 0) {
                prefix = prefix + "_" + depth + "_";
            }
        } catch (IllegalActionException e) {
            // Derivation invariant is not satisified.
            throw new InternalErrorException(e);
        }

        int uniqueNameIndex = 1;

        while ((getAttribute(candidate) != null)
                || (getPort(candidate) != null)
                || (getEntity(candidate) != null)
                || (getRelation(candidate) != null)) {
            candidate = prefix + uniqueNameIndex++;
        }

        return candidate;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public members                     ////

    /**
     * An boolean whose value is true if the 
     * AST has been created.
     */
    public boolean astCreated;

    /**
     * The location of the Ptalon code.
     */
    public FileParameter ptalonCodeLocation;

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                    ////

    /**
     * Add the attribute, and if attribute is a PtalonParameter,
     * add it to a list of Ptalon parameters.
     * @exception NameDuplicationException If the superclass throws it.
     * @throws IllegalActionException If the superclass throws it.
     */
    protected void _addAttribute(Attribute p) throws NameDuplicationException,
            IllegalActionException {
        super._addAttribute(p);
        if (p instanceof PtalonParameter) {
            _ptalonParameters.add((PtalonParameter) p);
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class is the configuration information. This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        try {
            for (Object att : attributeList()) {
                if (!(att instanceof Parameter)) {
                    Attribute attribute = (Attribute) att;
                    attribute.exportMoML(output, depth);
                }
            }
            if (astCreated) {
                String filename;
                try {
                    filename = ptalonCodeLocation.asFile().toURI().toString();
                } catch (IllegalActionException e) {
                    throw new IOException("Unable to get valid file name.");
                }
                if (filename.startsWith("file:/")) {
                    filename = filename.substring(5);
                }
                if (filename.startsWith("/")) {
                    filename = filename.substring(1);
                }
                String ptiiDir = StringUtilities
                        .getProperty("ptolemy.ptII.dir");
                File ptiiDirFile = new File(ptiiDir);
                String prefix = ptiiDirFile.toURI().toString();
                if (prefix.startsWith("file:/")) {
                    prefix = prefix.substring(5);
                }
                if (prefix.startsWith("/")) {
                    prefix = prefix.substring(1);
                }
                String ptiiFilename = filename.substring(prefix.length());
                String unPtlnName = ptiiFilename.substring(0, ptiiFilename
                        .length() - 5);
                String displayName = unPtlnName.replace('/', '.');
                output.write(_getIndentPrefix(depth) + "<configure>\n");
                output.write(_getIndentPrefix(depth + 1) + "<ptalon file=\""
                        + displayName + "\">\n");
                for (PtalonParameter param : _assignedPtalonParameters) {
                    if (!_unsettablePtalonParameters.contains(param)) {
                        if (param instanceof PtalonExpressionParameter) {
                            String expression = param.getExpression();
                            expression = expression.replaceAll("\"",
                                    "\\&quot\\;");
                            output.write(_getIndentPrefix(depth + 2)
                                    + "<ptalonExpressionParameter name=\""
                                    + param.getName() + "\" value=\""
                                    + expression + "\"/>\n");
                        } else if (param instanceof PtalonParameter) {
                            output.write(_getIndentPrefix(depth + 2)
                                    + "<ptalonParameter name=\""
                                    + param.getName() + "\" value=\""
                                    + param.getExpression() + "\"/>\n");
                        }
                    }
                }
                output.write(_getIndentPrefix(depth + 1) + "</ptalon>\n");
                output.write(_getIndentPrefix(depth) + "</configure>\n");
            }

        } catch (IOException e) {
            //e.printStackTrace();
            throw e;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /**
     * This helper method is used to begin the Ptalon compiler
     * if the ptalonCodeLocation attribute has been updated.
     * @exception IllegalActionException If any exception is thrown.
     */
    private void _initializePtalonCodeLocation() throws IllegalActionException {
        try {
            if (astCreated) {
                ptalonCodeLocation.setVisibility(Settable.NONE);
                return;
            }
            File inputFile = ptalonCodeLocation.asFile();
            if (inputFile == null) {
                return;
            }
            FileReader reader = new FileReader(inputFile);
            PtalonLexer lex = new PtalonLexer(reader);
            PtalonRecognizer rec = new PtalonRecognizer(lex);
            rec.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
            rec.actor_definition();
            _ast = (PtalonAST) rec.getAST();
            PtalonScopeChecker checker = new PtalonScopeChecker();
            checker.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
            _codeManager = new NestedActorManager(this);
            checker.actor_definition(_ast, _codeManager);
            _ast = (PtalonAST) checker.getAST();
            _codeManager = checker.getCodeManager();
            PtalonPopulator populator = new PtalonPopulator();
            populator.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
            populator.actor_definition(_ast, _codeManager);
            _ast = (PtalonAST) populator.getAST();
            astCreated = true;
            ptalonCodeLocation.setVisibility(Settable.NOT_EDITABLE);
            _codeManager.assignInternalParameters();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }

    /**
     * A list of all ptalon paramters who have been assinged a value.
     */
    private List<PtalonParameter> _assignedPtalonParameters = new LinkedList<PtalonParameter>();

    /**
     * The abstract syntax tree for the PtalonActor.
     */
    private PtalonAST _ast;

    /**
     * Information generated about the Ptalon code that is used by the
     * compiler.
     */
    private NestedActorManager _codeManager;

    /**
     * The text representation of the URL for this object.
     */
    private String _configureSource;

    /**
     * The depth for this actor with respect to nested
     * actor declarations.
     */
    private int _nestedDepth = 0;

    /**
     * A list of all ptalon parameters for this actor.
     */
    private List<PtalonParameter> _ptalonParameters = new LinkedList<PtalonParameter>();

    /**
     * A list of all ptalon paramters who are not settable by the user.
     */
    private List<PtalonParameter> _unsettablePtalonParameters = new LinkedList<PtalonParameter>();

}
