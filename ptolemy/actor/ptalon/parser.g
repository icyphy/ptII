header {/* Lexer/Parser for Ptalon.

 Copyright (c) 2006-2009 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
////                         Ptalon Parser                     ////
{
/** 
  PtalonRecognizer.java generated from populator.g by ANTLR.

  @author Adam Cataldo, Elaine Cheong, Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 7.0
  @Pt.ProposedRating Red (celaine)
  @Pt.AcceptedRating Red (celaine)
*/
@SuppressWarnings("unused")
}
class PtalonRecognizer extends Parser;
options {
    exportVocab = Ptalon;
    k = 2;
    buildAST = true;
    defaultErrorHandler = false;
    ASTLabelType = "PtalonAST";
}

{
    private boolean _gtExtension = false;
    
    private boolean _isInTransformation = false;
    
    public void enableGTExtension(boolean enable) {
        _gtExtension = enable;
    }
}

/**
 * Parse for statement:
 * <p><i>portType</i> <i>ID</i>
 * <p>where portType is either "port", "inport", or "outport".
 * Generate corresponding tree #(PORT ID), #(INPORT ID), or #(OUTPORT ID).
 */
port_declaration!
{
    boolean dynamic_name = false;
}
:
    (a:PORT
    {
        #port_declaration = #a;
    }
    (! LBRACKET RBRACKET
    {
        #port_declaration = #[MULTIPORT, "multiport"];
    }    
    )? | b:INPORT
    {
        #port_declaration = #b;
    }
    (! LBRACKET RBRACKET
    {
        #port_declaration = #[MULTIINPORT, "multiinport"];
    }
    )? | c:OUTPORT
    {
        #port_declaration = #c;
    }
    (! LBRACKET RBRACKET
    {
        #port_declaration = #[MULTIOUTPORT, "multioutport"];
    }
    )? ) d:ID (e:expression
    {
        dynamic_name = true;
    }
    )?
    {
        if (dynamic_name) {
            #port_declaration.addChild(#([DYNAMIC_NAME, "dynamic"], d, e));
        } else {
            #port_declaration.addChild(#d);
        }
    }
;

/**
 * Parse for one of:
 * <p>parameter <i>ID</i>
 * <p>actor <i>ID</i>
 * <p>where parameterType is either "parameter", "intparameter", or 
 * "outparameter".
 * Generate corresponding tree #(PARAMETER ID), #(INTPARAMETER ID), or 
 * #(BOOLPARAMETER ID).
 */
parameter_declaration!
{
    boolean addChild = true;
    boolean dynamic_name = false;
}
:
    (p:PARAMETER c:ID (n:expression
    {
        dynamic_name = true;
    }
    )? (EQUALS e:expression
    {
        if (dynamic_name) {
            #parameter_declaration = #([PARAM_EQUALS, "="], (p, 
                ([DYNAMIC_NAME, "dynamic"], c, n)), e);
        } else {
            #parameter_declaration = #([PARAM_EQUALS, "="], (p, c), e);
        }
        addChild = false;
    }
    )? 
    {
        if (addChild) {
            if (dynamic_name) {
                #parameter_declaration = #(p, ([DYNAMIC_NAME, "dynamic"], c, n));
            } else {
                #parameter_declaration = #(p, c);
            }
        }
    }
    | (a:ACTOR | ACTORPARAM
    {
        #a = #[ACTOR, "actor"];
    }
    ) d:ID (EQUALS q:qualified_identifier
    {
        #parameter_declaration = #([ACTOR_EQUALS, "="], (a, d), q);
        addChild = false;
    }
    )?
    {
        if (addChild) {
            #parameter_declaration = #(a, d);
        }
    }
    )
;

/**
 * Parse relation declaration:
 * <p>relation <i>ID</i>
 * <p>Generate tree #(RELATION ID)
 */
relation_declaration
:
    r:RELATION^ i:ID (! e:expression
    {
        #relation_declaration = #(r, ([DYNAMIC_NAME, "dynamic"], i, e));
    }
    )?
;

transparent_relation_declaration!
{
    boolean created = false;
}
:
    (t:TRANSPARENT RELATION | PORT r:REFERENCE
    ) i:ID (! e:expression
    {
        #transparent_relation_declaration = 
            #([TRANSPARENT, "transparent"], ([DYNAMIC_NAME, "dynamic"], i, e));
        created = true;
    }
    )?
    {
        if (!created) {
            #transparent_relation_declaration = #([TRANSPARENT, "transparent"], i);
        }
    }
;

/**
 * Parse qualified identifier:
 * <p><i>ID</i>
 * <p>or
 * <p><i>ID</i>.qualified_identifier
 * <p>Generate tree #(QUALID)
 */
qualified_identifier!
{
    StringBuffer identifier = new StringBuffer();
}
:
    (p:ID COLON
    {
        identifier.append(#p.getText() + ":");
    }
    )?
    a:keyword_or_identifier 
    {
        identifier.append(#a.getText());
    } 
    (DOT b:keyword_or_identifier
    {
        identifier.append("." +  #b.getText());
    }
    )*
    {
        #qualified_identifier = #[QUALID, identifier.toString()];
    }
;

keyword_or_identifier:
    ID | IMPORT | PORT | INPORT | OUTPORT | PARAMETER 
    | ACTOR | RELATION | TRUE | FALSE | IF | ELSE | IS | FOR |
    INITIALLY | NEXT | DANGLING_PORTS_OKAY | ATTACH_DANGLING_PORTS |
    TRANSPARENT | REFERENCE
;

/**
 * Parse statements of one of form:
 * <p><i>ID</i> := <i>ID</i>
 * <p><i>ID</i> := <i>actor_declaration</i>
 * <p><i>ID</i> := <i>arithmetic_expression</i>
 * <p><i>ID</i> := <i>boolean_expression</i>
 * <p>with preference given in that order.  Generate corresponding
 * tree:
 * <p>#(ASSIGN ID ID)
 * <p>#(ASSIGN ID <i>actor_declaration</i>)
 * <p>#(ASSIGN ID expression)
 */
assignment!
{
    boolean dynamic_name = false;
    boolean dynamic_left = false;
}
:
    l:ID (lExp:expression
    {
        dynamic_left = true;
    }
    )? a:ASSIGN ((ID (expression)? (RPAREN | COMMA)) => r:ID (e:expression
    {
        dynamic_name = true;
    }
    )? 
    {
        PtalonAST left;
        if (dynamic_left) {
            left = #([DYNAMIC_NAME, "dynamic"], l, lExp);
        } else {
            left = #l;
        }
        if (dynamic_name) {
            #assignment = #(a, left, ([DYNAMIC_NAME, "dynamic"], r, e));
        } else {
            #assignment = #(a, left, r);
        }
    }
    |
        (d:actor_declaration
        {
            #assignment = #(a, l, d);
        }        
        | f:expression
        {
            #assignment = #(a, l, f);
        }        
        )
    )
;

/**
 * Parse anything inside XML-like block
 * <p>&lt;  /&gt;
 * <p>Generate the tree
 * <p>#(EXPRESSION)
 * <p>where the text of the token EXPRESSION is the expression
 * inside the XML-like block.
 * 
 */
expression!
{
    String out = "";
}
:
    b:EXPRESSION
    {
        String full = b.getText();
        int length = full.length();
        out += full.substring(2, length - 2);
        #expression = #[EXPRESSION, out];
    }
;

/**
 * Parse statements of one of form:
 * <p><i>ID</i>(<i>assignment</i>, <i>assignment</i>, ...)
 * <p>Generate tree:
 * <p>#(ACTOR_DECLARATION <i>assignment</i> <i>assignment</i> ...)
 * <p>where the text for token ACTOR_DECLARATION is the leftmost
 * <i>ID</i> in the statement, or the name of the declared actor.
 */
actor_declaration!
:
	(a:actor_id ASSIGN)?
    b:ID LPAREN 
    {
        #b = #[ACTOR_DECLARATION, b.getText()];
        #actor_declaration = #(b);
        if (#a != null) {
        	#actor_declaration.addChild(#a);
        }
    }
    (c:assignment 
    {
        #actor_declaration.addChild(#c);
    }
    (COMMA d:assignment
    {
        #actor_declaration.addChild(#d);
    }
    )*)? RPAREN
;

actor_id!
:
	a:ID
	{
		#actor_id = #[ACTOR_ID, a.getText()];
	}
	(b:expression)?
	{
		#actor_id.addChild(#b);
	}
;

atomic_statement : 
    (port_declaration
    | parameter_declaration
    | relation_declaration
    | transparent_relation_declaration 
    | actor_declaration
    | transformation_declaration) SEMI!
;

transformation_declaration!
{
    boolean dynamic_name = false;
}
:
	(({!_isInTransformation}? (n:NEGATE { #transformation_declaration = #n; }
		| o:OPTIONAL { #transformation_declaration = #o; }))
	| ({_isInTransformation}? (r:REMOVE { #transformation_declaration = #r; }
		| p:PRESERVE { #transformation_declaration = #p; })))
	d:ID (e:expression
    {
        dynamic_name = true;
    }
    )?
    {
        if (dynamic_name) {
            #transformation_declaration.addChild(#([DYNAMIC_NAME, "dynamic"], d, e));
        } else {
            #transformation_declaration.addChild(#d);
        }
    }
;

conditional_statement!
{
    AST trueTree = null;
    AST falseTree = null;
}
:
    i:IF b:expression 
    {
        #conditional_statement = #(i, b);
        trueTree = #[TRUEBRANCH, "true branch"];
        falseTree = #[FALSEBRANCH, "false branch"];
    }
    LCURLY! (a1:atomic_statement 
    {
        trueTree.addChild(#a1);
    }
    | c1:conditional_statement
    {
        trueTree.addChild(#c1);
    }
    | i1:iterative_statement
    {
        trueTree.addChild(#i1);
    }
    )* RCURLY! (ELSE! LCURLY! (a2:atomic_statement 
    {
        falseTree.addChild(#a2);
    }
    | c2:conditional_statement
    {
        falseTree.addChild(#c2);
    }
    | i2:iterative_statement
    {
        falseTree.addChild(#i2);
    }
    )* RCURLY!)?
    {
        #conditional_statement.addChild(trueTree);
        #conditional_statement.addChild(falseTree);
    }
;

iterative_statement!
:
    f:FOR a:ID b:INITIALLY init:expression sat:expression LCURLY
    {
        #iterative_statement = #(f, ([VARIABLE, "variable"], a), (b, init), 
        ([SATISFIES, "satisfies"], sat));
    }
    (it:iterative_statement 
    {
        #iterative_statement.addChild(#it);
    }
    | cond:conditional_statement 
    {
        #iterative_statement.addChild(#cond);
    }
    | at:atomic_statement
    {
        #iterative_statement.addChild(#at);
    }
    )*
    RCURLY c:NEXT next:expression
    {
        #iterative_statement.addChild(#(c, next));
    }
;

actor_definition!
{
    boolean danglingPortsOkay = false;
    boolean attachDanglingPorts = false;
}
:
    {
        #actor_definition = #[ACTOR_DEFINITION];
        _isInTransformation = false;
    }
    (d:danglingPortsOkay
    {
        danglingPortsOkay = true;
    }
    )? (at:attachDanglingPorts
    {
        attachDanglingPorts = true;
    }
    )?
    a:ID
    {
        #actor_definition.setText(a.getText());
        if (danglingPortsOkay) {
            #actor_definition.addChild(#d);
        }
        if (attachDanglingPorts) {
            #actor_definition.addChild(#at);
        }
    }
    IS! LCURLY! (b:atomic_statement 
    {
        #actor_definition.addChild(#b);
    }
    | c:conditional_statement
    {
        #actor_definition.addChild(#c);
    }
    | i:iterative_statement
    {
        #actor_definition.addChild(#i);
    }
    )* RCURLY!
    (| {_gtExtension}? t:transformation
    {
       #actor_definition.addChild(#t);
    })
;

transformation! :
    {
        #transformation = #[TRANSFORMATION];
    	_isInTransformation = true;
    }
    TRANSFORM (s:PLUS
    {
        #transformation.addChild(#s);
    })? LCURLY! (b:atomic_statement 
    {
        #transformation.addChild(#b);
    }
    | c:conditional_statement
    {
        #transformation.addChild(#c);
    }
    | i:iterative_statement
    {
        #transformation.addChild(#i);
    }
    )*
    {
    	_isInTransformation = false;
    }
    RCURLY!
;

danglingPortsOkay :
    DANGLING_PORTS_OKAY SEMI!
;

attachDanglingPorts :
    ATTACH_DANGLING_PORTS SEMI!
;

///////////////////////////////////////////////////////////////////
////                          Ptalon Lexer                     ////
{
/** 
  PtalonLexer.java generated from populator.g by ANTLR.

  @author Adam Cataldo, Elaine Cheong, Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 7.0
  @Pt.ProposedRating Red (celaine)
  @Pt.AcceptedRating Red (celaine)
*/
}
class PtalonLexer extends Lexer;
options {
    exportVocab = Ptalon;
    testLiterals = false;
    k = 3;
}

tokens {
    IMPORT = "import";
    PORT = "port";
    INPORT = "inport";
    OUTPORT = "outport";
    PARAMETER = "parameter";
    ACTOR = "actor";
    ACTORPARAM = "actorparameter";
    RELATION = "relation";
    TRUE = "true";
    FALSE = "false";
    IF = "if";
    ELSE = "else";
    IS = "is";
    FOR = "for";
    INITIALLY = "initially";
    NEXT = "next";
    DANGLING_PORTS_OKAY = "danglingPortsOkay";
    ATTACH_DANGLING_PORTS = "attachDanglingPorts";
    TRANSPARENT = "transparent";
    REFERENCE = "reference";
    TRUEBRANCH;
    FALSEBRANCH;
    QUALID;
    ATTRIBUTE;
    ACTOR_DECLARATION;
    ACTOR_DEFINITION;
    TRANSFORMATION;
    NEGATIVE_SIGN;
    POSITIVE_SIGN;
    ARITHMETIC_FACTOR;
    BOOLEAN_FACTOR;
    LOGICAL_BUFFER;
    ARITHMETIC_EXPRESSION;
    BOOLEAN_EXPRESSION;
    MULTIPORT;
    MULTIINPORT;
    MULTIOUTPORT;
    PARAM_EQUALS;
    ACTOR_EQUALS;
    SATISFIES;
    VARIABLE;
    DYNAMIC_NAME;
    ACTOR_LABEL;
    QUALIFIED_PORT;
    NEGATE = "negate";
    OPTIONAL = "optional";
    REMOVE = "remove";
    PRESERVE = "preserve";
    ACTOR_ID;
}


// Punctuation symbols
ASSIGN: ":=";

COMMA: ',';

DOT: '.';

LBRACKET: '[';

LCURLY: '{';

LPAREN: '(';

RBRACKET: ']';

RCURLY: '}';

RPAREN: ')';

SEMI: ';';

EQUALS: '=';

COLON: ':';

TRANSFORM: "=>";

PLUS: '+';

// Escape sequence
ESC:
    '\\' ('n' | 'r' | 't' | 'b' | 'f' | '"' | '\'')
;

// An identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifier.
ID options { testLiterals=true; } :
    ('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
;

// Number literals
NUMBER_LITERAL:
    ('0'..'9')+ ('.' ('0'..'9')+)?
;

// String literals
STRING_LITERAL :
    '"' (ESC | ~('"'|'\\'|'\n'|'\r'))* '"'
;
    
// Whitespace -- ignored
WHITE_SPACE :
    (
        ' '
        | '\t'
        | '\f'
        | ('\r' '\n' | '\r' | '\n') { newline(); }
    )
    { $setType(Token.SKIP); }
;

LINE_COMMENT :
    "//" (~('\n' | '\r'))* ((('\r' '\n') | '\r' | '\n') { newline(); })?
    {
        $setType(Token.SKIP);
    }
;

EXPRESSION :
    '[' '[' (
        options {
            greedy = false;
        } :
            .
        )* 
    ']' ']'
;

COMMENT :
    '/' '*' (
        options {
            greedy = false;
        } :
            .
        )*
    '*' '/'
    { $setType(Token.SKIP); }
;
