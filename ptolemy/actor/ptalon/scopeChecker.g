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
:
	#(ASSIGN a:ID (ID | nested_actor_declaration[a.getText()]
	| arithmetic_expression | boolean_expression))
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
		| NUMBER_LITERAL | arithmetic_expression))
;

arithmetic_term throws PtalonScopeException
:
	#(STAR arithmetic_factor arithmetic_factor) |
	#(DIVIDE arithmetic_factor arithmetic_factor) |
	#(MOD arithmetic_factor arithmetic_factor) |
	arithmetic_factor
;

arithmetic_expression throws PtalonScopeException
:
	#(PLUS arithmetic_term arithmetic_term) |
	#(MINUS arithmetic_term arithmetic_term) |
	arithmetic_term
;

relational_expression throws PtalonScopeException
:
	#(EQUAL arithmetic_expression arithmetic_expression) | 
	#(NOT_EQUAL arithmetic_expression arithmetic_expression) | 
	#(LESS_THAN arithmetic_expression arithmetic_expression) | 
	#(GREATER_THAN arithmetic_expression arithmetic_expression) | 
	#(LESS_EQUAL arithmetic_expression arithmetic_expression) | 
	#(GREATER_EQUAL arithmetic_expression arithmetic_expression)
;

boolean_factor throws PtalonScopeException
:
	#(BOOLEAN_FACTOR (LOGICAL_NOT | LOGICAL_BUFFER)
		(boolean_expression | relational_expression | TRUE | FALSE | 
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

boolean_expression throws PtalonScopeException
:
	#(LOGICAL_OR boolean_term boolean_term) |
	boolean_term
;

atomic_statement throws PtalonScopeException
:
	(port_declaration | parameter_declaration |
		relation_declaration | actor_declaration)
;

conditional_statement throws PtalonScopeException
:
	#(IF 
	{
		info.pushIfStatement();
	}
	boolean_expression #(TRUEBRANCH (atomic_statement | conditional_statement)*)
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
