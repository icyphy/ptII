header {/* 

 Copyright (c) 2006-2008 The Regents of the University of California.
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
}

{
/** 
  PtalonScopeChecker.java generated from populator.g by ANTLR.

  @author Adam Cataldo, Elaine Cheong, Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 7.0
  @Pt.ProposedRating Red (celaine)
  @Pt.AcceptedRating Red (celaine)
*/
}
class PtalonScopeChecker extends TreeParser;
options {
    importVocab = Ptalon;
    buildAST = true;
    defaultErrorHandler  = false;
    ASTLabelType = "PtalonAST";
 // http://www.antlr.org:8080/pipermail/antlr-interest/2006-October/018084.html
 // says:
 // > Just delete that line.  ANTLR 2 treewalkers are effectively k=1.
 //    k = 2;
}

{
    private PtalonEvaluator info;

    public PtalonEvaluator getCodeManager() {
        return info;
    }

    private String scopeName;
}

port_declaration throws PtalonScopeException
:
    #(PORT (a:ID
    {
        info.addSymbol(a.getText(), "port");
    }
    | #(DYNAMIC_NAME ID EXPRESSION))) | #(INPORT (b:ID
    {
        info.addSymbol(b.getText(), "inport");
    }
    | #(DYNAMIC_NAME ID EXPRESSION))) | #(OUTPORT (c:ID
    {
        info.addSymbol(c.getText(), "outport");
    }
    | #(DYNAMIC_NAME ID EXPRESSION))) | #(MULTIPORT (d:ID
    {
        info.addSymbol(d.getText(), "multiport");
    }
    | #(DYNAMIC_NAME ID EXPRESSION))) | #(MULTIINPORT (e:ID
    {
        info.addSymbol(e.getText(), "multiinport");
    }
    | #(DYNAMIC_NAME ID EXPRESSION))) | #(MULTIOUTPORT (f:ID
    {
        info.addSymbol(f.getText(), "multioutport");
    }
    | #(DYNAMIC_NAME ID EXPRESSION)))
;

parameter_declaration throws PtalonScopeException
:
    #(PARAMETER (a:ID
    {
        info.addSymbol(a.getText(), "parameter");
    } | #(DYNAMIC_NAME ID EXPRESSION)))
    | #(ACTOR b:ID
    {
        info.addSymbol(b.getText(), "actorparameter");
    }
    )
;

assigned_parameter_declaration throws PtalonScopeException
:
    #(PARAM_EQUALS #(PARAMETER (a:ID
    {
        info.addSymbol(a.getText(), "parameter");
    }
    | #(DYNAMIC_NAME ID EXPRESSION))) EXPRESSION) | 
    #(ACTOR_EQUALS #(ACTOR b:ID
    {
        info.addSymbol(b.getText(), "actorparameter");
    }
    ) QUALID)
;

relation_declaration throws PtalonScopeException
:
    #(RELATION (a:ID
    {
        info.addSymbol(a.getText(), "relation");
    }
    | #(DYNAMIC_NAME ID EXPRESSION)))
;

transparent_relation_declaration throws PtalonScopeException
:
    #(TRANSPARENT (a:ID
    {
        info.addSymbol(a.getText(), "transparent");
    }
    | #(DYNAMIC_NAME ID EXPRESSION)))
;

assignment throws PtalonScopeException
{
    boolean leftDynamic = false;
}
:
    #(ASSIGN (a:ID | #(DYNAMIC_NAME left:ID leftExp:EXPRESSION
    {
        leftDynamic = true;
        info.addUnknownLeftSide(left.getText(), leftExp.getText());
    }
    )) ((b:ID 
    {
        if (!leftDynamic) {
            info.addPortAssign(a.getText(), b.getText());
        }
    }
    | #(DYNAMIC_NAME c:ID d:EXPRESSION)
    {
        if (!leftDynamic) {
            info.addPortAssign(a.getText(), c.getText(), d.getText());
        }
    }
    )| nested_actor_declaration[a.getText()]
    | e:EXPRESSION
    {
        if (!leftDynamic) {
            info.addParameterAssign(a.getText(), e.getText());
        }
    }
    ))
;

actor_declaration throws PtalonScopeException    
:
    #(a:ACTOR_DECLARATION 
    {
        info.pushActorDeclaration(a.getText());
    }
    (#(b:ACTOR_ID (c:EXPRESSION)?))?
    (assignment)*)
    {
        String uniqueName = info.popActorDeclaration();
        #actor_declaration.setText(uniqueName);
        
    	if (b != null && c == null) {
    		info.addSymbol(b.getText(), "actor");
    	}
    }    
;

nested_actor_declaration [String paramValue] throws PtalonScopeException    
:
    #(a:ACTOR_DECLARATION
    {
        info.pushActorDeclaration(a.getText());
        info.setActorParameter(paramValue);
    }
    (b:assignment)*)
    {
        String uniqueName = info.popActorDeclaration();
        #nested_actor_declaration.setText(uniqueName);
    }    
;

atomic_statement throws PtalonScopeException
:
    (port_declaration
    | parameter_declaration
    | assigned_parameter_declaration
    | relation_declaration
    | transparent_relation_declaration
    | actor_declaration
    | transformation_declaration)
;

transformation_declaration throws PtalonScopeException
:
	(#(NEGATE (ID | #(DYNAMIC_NAME ID EXPRESSION)))
	|#(OPTIONAL (ID | #(DYNAMIC_NAME ID EXPRESSION)))
	|#(REMOVE (ID | #(DYNAMIC_NAME ID EXPRESSION)))
	|#(PRESERVE (ID | #(DYNAMIC_NAME ID EXPRESSION))))
;

conditional_statement throws PtalonScopeException
:
    #(IF 
    {
        info.pushIfStatement();
    }
    EXPRESSION 
        #(TRUEBRANCH 
        {
            info.setCurrentBranch(true);
        }
            (atomic_statement | conditional_statement | iterative_statement)*)
        #(FALSEBRANCH 
        {
            info.setCurrentBranch(false);
        }
            (atomic_statement | conditional_statement | iterative_statement)*))
    {
        #conditional_statement.setText(info.popIfStatement());
    }
;    

iterative_statement throws PtalonScopeException
:
    #(FOR #(VARIABLE a:ID) #(INITIALLY b:EXPRESSION) #(SATISFIES c:EXPRESSION)
    {
        info.pushForStatement(a.getText(), b.getText(), c.getText());
    }
        (atomic_statement | conditional_statement | iterative_statement)*
        #(NEXT n:EXPRESSION
        {
            info.setNextExpression(n.getText());
        }
        ))
    {
        #iterative_statement.setText(info.popForStatement());
    }
;

transformation throws PtalonScopeException
:
    #(TRANSFORMATION
    {
        info._setPreservingTransformation(false);
    }
    (PLUS
    {
        info._setPreservingTransformation(true);
    })?
    (atomic_statement | conditional_statement | iterative_statement)*)
;

actor_definition [PtalonEvaluator manager] throws PtalonScopeException
{
    info = manager;
}
:
    #(a:ACTOR_DEFINITION 
    {
        info.setActorSymbol(a.getText());
        info.setDanglingPortsOkay(true);
    }
    (DANGLING_PORTS_OKAY)? (ATTACH_DANGLING_PORTS 
    {
        info.setDanglingPortsOkay(false);
    }
    )?
    (atomic_statement | conditional_statement | iterative_statement)*
    (transformation)?)
;
