header {/* 

 Copyright (c) 2006 The Regents of the University of California.
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
	import java.util.LinkedList;
}
class PtalonScopeChecker extends TreeParser;
options {
	importVocab = Ptalon;
	buildAST = true;
	defaultErrorHandler  = false;
	ASTLabelType = "PtalonAST";
}

{
	private NestedActorManager info;

	public NestedActorManager getCodeManager() {
		return info;
	}
	
	private String scopeName;
}

import_declaration throws PtalonScopeException
:
	#(IMPORT a:qualified_identifier
	{
		info.addImport(a.getText());
	}
	)
;

port_declaration throws PtalonScopeException
:
	#(PORT a:ID
	{
		info.addSymbol(a.getText(), "port");
	}
	) | #(INPORT b:ID
	{
		info.addSymbol(b.getText(), "inport");
	}
	) | #(OUTPORT c:ID
	{
		info.addSymbol(c.getText(), "outport");
	}
	)
;

parameter_declaration throws PtalonScopeException
:
	#(PARAMETER a:ID
	{
		info.addSymbol(a.getText(), "parameter");
	}
	) | #(INTPARAMETER b:ID
	{
		info.addSymbol(b.getText(), "intparameter");
	}
	) | #(BOOLPARAMETER c:ID
	{
		info.addSymbol(c.getText(), "boolparameter");
	}
	)
;

relation_declaration throws PtalonScopeException
:
	#(RELATION a:ID
	{
		info.addSymbol(a.getText(), "relation");
	}
	)
;

qualified_identifier
:
	QUALID
;

attribute
:
	#(ATTRIBUTE qualified_identifier)
;

assignment throws PtalonScopeException
{
	String arith, bool;
}
:
	#(ASSIGN a:ID (! b:ID 
	{
		if (info.getType(b.getText()).equals("intparameter")) {
			String arithmetic_label = info.getNextArithExpr();
			PtalonAST temp = #([ARITHMETIC_EXPRESSION, arithmetic_label],
			([ARITHMETIC_FACTOR, "arithmetic_factor"],
			[POSITIVE_SIGN, "positive"], b));
			#assignment = #([ASSIGN], a, temp);
			info.addArithParam(a.getText(), arithmetic_label);
		} else if (info.getType(b.getText()).equals("boolparameter")) {
			String boolean_label = info.getNextBoolExpr();
			PtalonAST temp = #([BOOLEAN_EXPRESSION, boolean_label],
			([BOOLEAN_FACTOR, "boolean_factor"],
			[LOGICAL_BUFFER, "!!"], b));
			#assignment = #([ASSIGN], a, temp);
			info.addBoolParam(a.getText(), boolean_label);
		} else if (info.getType(b.getText()).endsWith("port")) {
			info.addPortAssign(a.getText(), b.getText());
			#assignment = #([ASSIGN], a, b);
		} else if (info.getType(b.getText()).equals("relation")) {
			info.addPortAssign(a.getText(), b.getText());
			#assignment = #([ASSIGN], a, b);
		}
	}
	| nested_actor_declaration[a.getText()]
	| arith=arithmetic_expression 
	{
		info.addArithParam(a.getText(), arith);
	}
	| bool=boolean_expression
	{
		info.addBoolParam(a.getText(), bool);
	}
	))
;

actor_declaration throws PtalonScopeException	
:
	#(a:ACTOR_DECLARATION 
	{
		info.pushActorDeclaration(a.getText());
	}
	(b:assignment)*)
	{
		String uniqueName = info.popActorDeclaration();
		#actor_declaration.setText(uniqueName);
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


arithmetic_factor throws PtalonScopeException
{
	String foo;
}
:
	#(ARITHMETIC_FACTOR (POSITIVE_SIGN | NEGATIVE_SIGN) 
		(a:ID
		{
			String type = info.getType(a.getText());
			if (!type.equals("intparameter")) {
				throw new PtalonScopeException(a.getText() + 
					" should have type intparameter, but instead has type " + type);
			}
		}
		| NUMBER_LITERAL | foo=arithmetic_expression))
;

arithmetic_term throws PtalonScopeException
:
	#(STAR arithmetic_factor arithmetic_factor) |
	#(DIVIDE arithmetic_factor arithmetic_factor) |
	#(MOD arithmetic_factor arithmetic_factor) |
	arithmetic_factor
;

arithmetic_expression! returns [String expressionLabel] throws PtalonScopeException
{
	expressionLabel = "";
}
:
	#(p:PLUS a:arithmetic_term b:arithmetic_term
	{
		expressionLabel = info.getNextBoolExpr();
		#arithmetic_expression = #([ARITHMETIC_EXPRESSION, expressionLabel],
		(p, a, b));
	}
	) | #(m:MINUS c:arithmetic_term d:arithmetic_term
	{
		expressionLabel = info.getNextBoolExpr();
		#arithmetic_expression = #([ARITHMETIC_EXPRESSION, expressionLabel],
		(m, c, d));
	}	
	) | e:arithmetic_term
	{
		expressionLabel = info.getNextBoolExpr();
		#arithmetic_expression = #([ARITHMETIC_EXPRESSION, expressionLabel],
		e);
	}
	
;

relational_expression throws PtalonScopeException
{
	String foo;
}
:
	#(EQUAL foo=arithmetic_expression foo=arithmetic_expression) | 
	#(NOT_EQUAL foo=arithmetic_expression foo=arithmetic_expression) | 
	#(LESS_THAN foo=arithmetic_expression foo=arithmetic_expression) | 
	#(GREATER_THAN foo=arithmetic_expression foo=arithmetic_expression) | 
	#(LESS_EQUAL foo=arithmetic_expression foo=arithmetic_expression) | 
	#(GREATER_EQUAL foo=arithmetic_expression foo=arithmetic_expression)
;

boolean_factor throws PtalonScopeException
{
	String foo;
}
:
	#(BOOLEAN_FACTOR (LOGICAL_NOT | LOGICAL_BUFFER)
		(foo=boolean_expression | relational_expression | TRUE | FALSE | 
		a:ID
		{
			String type = info.getType(a.getText());
			if (!type.equals("boolparameter")) {
				throw new PtalonScopeException(a.getText() + 
					" should have type boolparameter, but instead has type " + type);
			}
		}
		))
;

boolean_term throws PtalonScopeException
:
	#(LOGICAL_AND boolean_factor boolean_factor) |
	boolean_factor
;

boolean_expression! returns [String expressionLabel] throws PtalonScopeException
{
	expressionLabel = "";
}
:
	#(l:LOGICAL_OR a:boolean_term b:boolean_term) |
	{
		expressionLabel = info.getNextBoolExpr();
		#boolean_expression = #([BOOLEAN_EXPRESSION, expressionLabel],
		(l, a, b));
	}
	c:boolean_term
	{
		expressionLabel = info.getNextBoolExpr();
		#boolean_expression = #([BOOLEAN_EXPRESSION, expressionLabel],
		c);
	}
;

atomic_statement throws PtalonScopeException
:
	(port_declaration | parameter_declaration |
		relation_declaration | actor_declaration)
;

conditional_statement throws PtalonScopeException
{
	String foo;
}
:
	#(IF 
	{
		info.pushIfStatement();
	}
	foo=boolean_expression #(TRUEBRANCH (atomic_statement | conditional_statement)*)
		#(FALSEBRANCH (atomic_statement | conditional_statement)*))
	{
		#conditional_statement.setText(info.popIfStatement());
	}
;	

actor_definition [NestedActorManager manager] throws PtalonScopeException
{
	info = manager;
}
:
	#(a:ACTOR_DEFINITION 
	{
		info.setActorSymbol(a.getText());
	}
	(import_declaration)* ((atomic_statement | 
		conditional_statement)* | attribute))
;
