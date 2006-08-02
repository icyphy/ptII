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

import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
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

public class PtalonActor extends TypedCompositeActor {

    /** Construct a PtalonActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  
     *  FIXME: There is an issue with persistence that has yet to be
     *  solved for this actor.  In particular, if I create an
     *  instance of this actor and then save it...
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
        _ptalonParameters = new ArrayList<PtalonParameter>();
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
        if (att == ptalonCodeLocation) {
            _initializePtalonCodeLocation();
        } else if (att instanceof PtalonParameter) {
        } else {
            super.attributeChanged(att);
        }
    }
    
    /**
     * The location of the Ptalon code.
     */
    public FileParameter ptalonCodeLocation;
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////
    
    
    /**
     * Collect all the PtalonParameters created into the
     * _ptalonParameters list.
     */
    private void _collectPtalonParameters() {
        List attributes = attributeList();
        Attribute attribute;
        PtalonParameter parameter;
        for (int i = 0; i < attributes.size(); i++) {
            attribute = (Attribute) attributes.get(i);
            if (attribute instanceof PtalonParameter) {
                parameter = (PtalonParameter) attribute;
                if (!parameter.getVisibility().equals(Settable.NONE)) {
                    _ptalonParameters.add(parameter);
                }
            }
        }
    }
    
    /**
     * This helper method is used to begin the Ptalon compiler
     * if the ptalonCodeLocation attribute has been updated.
     * @throws IllegalActionException If any exception is thrown.
     */
    private void _initializePtalonCodeLocation() throws IllegalActionException {
        try {
            File inputFile = ptalonCodeLocation.asFile();
            if (inputFile == null)  {
                return;
            }
            FileReader reader = new FileReader(inputFile);
            PtalonLexer lex = new PtalonLexer(reader);
            PtalonRecognizer rec = new PtalonRecognizer(lex);
            rec.actor_definition();
            AST ast = rec.getAST();
            PtalonScopeChecker checker = new PtalonScopeChecker();
            checker.actor_definition(ast);
            ast = checker.getAST();
            PtalonPopulator populator = new PtalonPopulator();
            populator.actor_definition(ast, checker.getCompilerInfo(), this);
            ptalonCodeLocation.setExpression("");
            ptalonCodeLocation.setContainer(null);
            _collectPtalonParameters();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }
    
    /**
     * Returns true if all the PtalonParamters have values and
     * are therefore ready to have their methods called.  Their
     * methods will populate this actor with a hierarchical network of
     * interconnected actors.
     * @return True when all parameters are ready. 
     */
    private boolean _readyToCallMethods() {
        PtalonParameter p;
        for (int i = 0; i < _ptalonParameters.size(); i++) {
            p = _ptalonParameters.get(i);
            if (!(p.hasValue())) {
                return false;
            } else if (p.getVisibility().equals(Settable.NONE)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * List of all PtalonParameters associated with this actor.
     */
    private ArrayList<PtalonParameter> _ptalonParameters;
    

}
