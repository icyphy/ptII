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

class PtalonScopeChecker extends TreeParser;
options {
	importVocab = Ptalon;
	buildAST = true;
}

{
	private PtalonCompilerInfo info = new PtalonCompilerInfo();	

	public PtalonCompilerInfo getCompilerInfo() {
		return info;
	}
	
	private String scopeName;
}


port_declaration throws PtalonScopeException
:
	#(PORT a:ID
	{
		info.addSymbol(a.getText(), "port");
	}
	) | #(INPORT b:ID
	{
		info.addSymbol(a.getText(), "inport");
	}
	) | #(OUTPORT c:ID
	{
		info.addSymbol(a.getText(), "outport");
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
		info.addSymbol(a.getText(), "intparameter");
	}
	) | #(BOOLPARAMETER c:ID
	{
		info.addSymbol(a.getText(), "boolparameter");
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
	a:QUALID
;

attribute
:
	#(a:ATTRIBUTE c:qualified_identifier)
;

assignment throws PtalonScopeException
:
	#(ASSIGN ID (ID | actor_declaration | arithmetic_expression | boolean_expression))
;

actor_declaration throws PtalonScopeException
:
	#(a:ACTOR_DECLARATION 
	{
		String type = info.getType(a.getText());
		if (!type.equals("parameter")) {
			throw new PtalonScopeException(a.getText() + 
				" should have type parameter, but instead has type " + type);
		}
	}
	(assignment)*)
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
		relation_declaration | actor_declaration |
		attribute)
;

conditional_statement throws PtalonScopeException
:
	#(a:IF 
	{
		info.pushIfStatement();
	}
	boolean_expression (atomic_statement | conditional_statement)
		(atomic_statement | conditional_statement))
	{
		a.setText(info.popIfStatement());
	}
;	

actor_definition throws PtalonScopeException
:
	#(a:ACTOR_DEFINITION (atomic_statement | 
		conditional_statement)*)
;
