/* An aggregation of typed actors, specified by a Ptalon model.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.actor.ptalon;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.LinkedList;

import antlr.debug.misc.ASTFrame;

import com.microstar.xml.XmlParser;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;


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
        astCreated = new ExpertParameter(this, "astCreated");
        astCreated.setTypeEquals(BaseType.BOOLEAN);
        astCreated.setExpression("false");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
        
    
    
    
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
            if ((p.hasValue()) && (!p.getVisibility().equals(Settable.NOT_EDITABLE))) {
                try {
                    p.setVisibility(Settable.NOT_EDITABLE);
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
                        populator.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
                        populator.actor_definition(_ast, _codeManager);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
    public void configure(URL base, String source, String text) throws Exception {
        try {
            if (base != null) {
                _configureSource = base.toExternalForm();
            }
            if ((text != null) && (!text.trim().equals(""))) {
                XmlParser parser = new XmlParser();
                PtalonMLHandler handler = new PtalonMLHandler(this);
                parser.setHandler(handler);
                parser.parse(_configureSource, null, new StringReader(text));
                _ast = handler.getAST();
                _codeManager = handler.getCodeManager();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
    ///////////////////////////////////////////////////////////////////
    ////                        public members                     ////
    
    
    /**
     * An invisible parameter whose value is true if the 
     * AST has been created.  We use this instead of a boolean
     * as a way for the value to persist when a PtalonActor is
     * saved and reopened elsewhere.
     */
    public ExpertParameter astCreated;
    
    /**
     * The location of the Ptalon code.
     */
    public FileParameter ptalonCodeLocation;
    
    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                    ////

    /**
     * Add the attribute, and if attribute is a PtalonParameter,
     * add it to a list of Ptalon parameters.
     * @throws NameDuplicationException If the superclass throws it.
     * @thrwos IllegalActionException If the superclass throws it.
     */
    protected void _addAttribute(Attribute p) throws NameDuplicationException, IllegalActionException {
        super._addAttribute(p);
        if (p instanceof PtalonParameter) {
            _ptalonParameters.add((PtalonParameter)p);
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
            super._exportMoMLContents(output, depth);
            output.write(_getIndentPrefix(depth) + "<property class=\"ptolemy.data.expr.ExpertParameter\" name=\"astCreated\"value =\"" 
                    + astCreated.getExpression() + "\">\n" + _getIndentPrefix(depth) + "</property>\n");
            output.write(_getIndentPrefix(depth) + "<configure>\n");
            output.write(_getIndentPrefix(depth + 1) + "<ptaloninfo>\n");
            if (_ast != null) {
                _ast.xmlSerialize(output, depth + 2);
            }
            if (_codeManager != null) {
                _codeManager.xmlSerialize(output, depth + 2);
            }
            output.write(_getIndentPrefix(depth + 1) + "</ptaloninfo>\n");
            output.write(_getIndentPrefix(depth) + "</configure>\n");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////
    
    
    
    /**
     * This helper method is used to begin the Ptalon compiler
     * if the ptalonCodeLocation attribute has been updated.
     * @throws IllegalActionException If any exception is thrown.
     */
    private void _initializePtalonCodeLocation() throws IllegalActionException {
        try {
            if (astCreated.getExpression().equals("true")) {
                ptalonCodeLocation.setVisibility(Settable.NOT_EDITABLE);
                return;
            }
            File inputFile = ptalonCodeLocation.asFile();
            if (inputFile == null)  {
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
            _codeManager = new CodeManager(this);
            checker.actor_definition(_ast, _codeManager);
            _ast = (PtalonAST) checker.getAST();
            PtalonPopulator populator;
            populator = new PtalonPopulator();
            populator.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
            _codeManager = checker.getCodeManager();
            populator.actor_definition(_ast, _codeManager);
            _ast = (PtalonAST) populator.getAST();
            astCreated.setExpression("true");
            ptalonCodeLocation.setVisibility(Settable.NOT_EDITABLE);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }
    
    /**
     * The abstract syntax tree for the PtalonActor.
     */
    private PtalonAST _ast;
    
    /**
     * Information generated about the Ptalon code that is used by the
     * compiler.
     */
    private CodeManager _codeManager;
    
    /**
     * The text representation of the URL for this object.
     */
    private String _configureSource;
    
    /**
     * A list of all ptalon parameters for this actor.
     */
    private LinkedList<PtalonParameter> _ptalonParameters = new 
        LinkedList<PtalonParameter>();


}
