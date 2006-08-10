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
class PtalonPopulator extends TreeParser;
options {
	importVocab = Ptalon;
	buildAST = true;
	defaultErrorHandler = false;
	ASTLabelType = "PtalonAST";
}

{
	private CodeManager info;

	public CodeManager getCodeManager() {
		return info;
	}
	
	private String scopeName;
}

import_declaration throws PtalonRuntimeException
:
	#(IMPORT qualified_identifier)
;

port_declaration throws PtalonRuntimeException
:
	#(PORT a:ID
	{
		if (info.isReady() && !info.isCreated(a.getText())) {
			info.addPort(a.getText());
		}
	}
	) | #(INPORT b:ID
	{
		if (info.isReady() && !info.isCreated(b.getText())) {
			info.addInPort(b.getText());
		}
	}
	) | #(OUTPORT c:ID
	{
		if (info.isReady() && !info.isCreated(c.getText())) {
			info.addOutPort(c.getText());
		}
	}
	)
;

parameter_declaration throws PtalonRuntimeException
:
	#(PARAMETER a:ID
	{
		if (info.isReady() && !info.isCreated(a.getText())) {
			info.addParameter(a.getText());
		}
	}
	) | #(INTPARAMETER b:ID
	{
		if (info.isReady() && !info.isCreated(b.getText())) {
			info.addIntParameter(b.getText());
		}
	}
	) | #(BOOLPARAMETER c:ID
	{
		if (info.isReady() && !info.isCreated(c.getText())) {
			info.addBoolParameter(c.getText());
		}
	}
	)
;

relation_declaration throws PtalonRuntimeException
:
	#(RELATION a:ID
	{
		if (info.isReady() && !info.isCreated(a.getText())) {
			info.addRelation(a.getText());
		}
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

assignment throws PtalonRuntimeException
:
	#(ASSIGN ID (ID | actor_declaration | arithmetic_expression | boolean_expression))
;

actor_declaration throws PtalonRuntimeException
:
	#(a:ACTOR_DECLARATION (assignment)*)
;

arithmetic_factor throws PtalonRuntimeException
:
	#(ARITHMETIC_FACTOR (POSITIVE_SIGN | NEGATIVE_SIGN) 
		(a:ID | NUMBER_LITERAL | arithmetic_expression))
;

arithmetic_term throws PtalonRuntimeException
:
	#(STAR arithmetic_factor arithmetic_factor) |
	#(DIVIDE arithmetic_factor arithmetic_factor) |
	#(MOD arithmetic_factor arithmetic_factor) |
	arithmetic_factor
;

arithmetic_expression throws PtalonRuntimeException
:
	#(PLUS arithmetic_term arithmetic_term) |
	#(MINUS arithmetic_term arithmetic_term) |
	arithmetic_term
;

relational_expression throws PtalonRuntimeException
:
	#(EQUAL arithmetic_expression arithmetic_expression) | 
	#(NOT_EQUAL arithmetic_expression arithmetic_expression) | 
	#(LESS_THAN arithmetic_expression arithmetic_expression) | 
	#(GREATER_THAN arithmetic_expression arithmetic_expression) | 
	#(LESS_EQUAL arithmetic_expression arithmetic_expression) | 
	#(GREATER_EQUAL arithmetic_expression arithmetic_expression)
;

boolean_factor throws PtalonRuntimeException
:
	#(BOOLEAN_FACTOR (LOGICAL_NOT | LOGICAL_BUFFER)
		(boolean_expression | relational_expression | TRUE | FALSE | 
		a:ID))
;

boolean_term throws PtalonRuntimeException
:
	#(LOGICAL_AND boolean_factor boolean_factor) |
	boolean_factor
;

boolean_expression throws PtalonRuntimeException
:
	#(LOGICAL_OR boolean_term boolean_term) |
	boolean_term
;

atomic_statement throws PtalonRuntimeException
:
	(port_declaration | parameter_declaration |
		relation_declaration | actor_declaration)
;

conditional_statement throws PtalonRuntimeException
:
	#(a:IF 
	{
		info.enterIfScope(a.getText());
	}
	boolean_expression #(TRUEBRANCH (atomic_statement | conditional_statement)*)
		#(FALSEBRANCH (atomic_statement | conditional_statement)*))
	{
		info.exitIfScope();
	}
;	

actor_definition[CodeManager info] throws PtalonRuntimeException
{
	this.info = info;
	this.info.startAtTop();
}
:
	#(a:ACTOR_DEFINITION 
	{
		if (!this.info.isActorSet()) {
			this.info.setActor(a.getText());
		}
	}
	(import_declaration)* ((atomic_statement | 
		conditional_statement)* | attribute))
;
