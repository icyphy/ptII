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
	import java.util.StringTokenizer;
}
class PtalonPopulator extends TreeParser;
options {
	importVocab = Ptalon;
	buildAST = true;
	defaultErrorHandler = false;
	ASTLabelType = "PtalonAST";
}

{
	private NestedActorManager info;

	public NestedActorManager getCodeManager() {
		return info;
	}
	
	private String scopeName;
	
	private boolean evalBool = false;
	
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

assignment throws PtalonRuntimeException
{
	int x;
	boolean y;
}
:
	#(ASSIGN a:ID (b:ID	| nested_actor_declaration
	| x=arithmetic_expression | y=boolean_expression))
;

/**
 * This is for a top level actor declaration, which 
 * requires seperate treatement from a nested actor
 * declaration.
 */
actor_declaration throws PtalonRuntimeException
{
	boolean oldEvalBool = false;
}
:
	#(a:ACTOR_DECLARATION 
	{
		info.enterActorDeclaration(a.getText());
		if (info.isActorReady()) {
			oldEvalBool = evalBool;
			evalBool = true;
		}
	}
	(b:assignment)*
	{
		if (info.isActorReady()) {
			evalBool = oldEvalBool;
			info.addActor(a.getText());
		}
		info.exitActorDeclaration();
	}
	)
;

/**
 * In this case we do not add any actors, but rather
 * defer this decision to any generated actors.
 */
nested_actor_declaration throws PtalonRuntimeException
:
	#(a:ACTOR_DECLARATION 
	{
		info.enterActorDeclaration(a.getText());
	}
	(b:assignment)*
	{
		info.exitActorDeclaration();
	}
	)
;


arithmetic_factor returns [int i] throws PtalonRuntimeException
{
	i = 0;
	int x;
	int sign = 1;
}
:
	#(ARITHMETIC_FACTOR (POSITIVE_SIGN | NEGATIVE_SIGN
	{
		sign = -1;
	}
	) (a:ID 
	{
		if (evalBool) {
			i = sign * info.getIntValueOf(a.getText());
		}
	}
	| b:NUMBER_LITERAL 
	{
		if (evalBool) {
			i = sign * (new Integer(b.getText()));
		}
	}
	| x=arithmetic_expression
	{
		if (evalBool) {
			i = sign * x;
		}
	}	
	))
;

arithmetic_term returns [int i] throws PtalonRuntimeException
{
	i = 0;
	int x, y;
}
:
	#(STAR x=arithmetic_factor y=arithmetic_factor
	{
		if (evalBool) {
			i = x * y;
		}
	}	
	) | #(DIVIDE x=arithmetic_factor y=arithmetic_factor
	{
		if (evalBool) {
			i = x / y;
		}
	}		
	) | #(MOD x=arithmetic_factor y=arithmetic_factor
	{
		if (evalBool) {
			i = x % y;
		}
	}		
	) | x=arithmetic_factor
	{
		if (evalBool) {
			i = x;
		}
	}		
;

arithmetic_expression returns [int i] throws PtalonRuntimeException
{
	i = 0;
	int x, y;
}
:
	#(a:ARITHMETIC_EXPRESSION (#(PLUS x=arithmetic_term y=arithmetic_term
	{
		if (evalBool) {
			i = x + y;
		}
	}			
	) | #(MINUS x=arithmetic_term y=arithmetic_term
	{
		if (evalBool) {
			i = x - y;
		}
	}		
	) |	x=arithmetic_term
	{
		if (evalBool) {
			i = x;
		}
	}
	)
	{
		if (evalBool) {
			info.setArithExpr(a.getText(), i);
		}
	}
	)
;

relational_expression returns [boolean b] throws PtalonRuntimeException
{
	b = false;
	int x,y;
}
:
	#(EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (evalBool) {
			b = (x == y);
		}
	}			
	) | #(NOT_EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (evalBool) {
			b = (x != y);
		}
	}			
	) | #(LESS_THAN x=arithmetic_expression y=arithmetic_expression
	{
		if (evalBool) {
			b = (x < y);
		}
	}				
	) | #(GREATER_THAN x=arithmetic_expression y=arithmetic_expression
	{
		if (evalBool) {
			b = (x > y);
		}
	}			
	) | #(LESS_EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (evalBool) {
			b = (x <= y);
		}
	}			
	) | #(GREATER_EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (evalBool) {
			b = (x >= y);
		}
	}			
	)
;

boolean_factor returns [boolean b] throws PtalonRuntimeException
{
	boolean x;
	b = false;
	boolean sign = true;
}
:
	#(BOOLEAN_FACTOR (LOGICAL_NOT 
	{
		sign = false;
	}
	| LOGICAL_BUFFER) (x=boolean_expression 
	{
		if (evalBool) {
			b = !(sign ^ x);
		}
	}			
	| x=relational_expression 
	{
		if (evalBool) {
			b = !(sign ^ x);
		}
	}				
	| TRUE 
	{
		if (evalBool) {
			b = !(sign ^ true);
		}
	}			
	| FALSE 
	{
		if (evalBool) {
			b = !(sign ^ false);
		}
	}			
	| a:ID
	{
		if (evalBool) {
			b = !(sign ^ info.getBooleanValueOf(a.getText()));
		}
	}			
	))
;

boolean_term returns [boolean b] throws PtalonRuntimeException
{
	boolean x, y;
	b = false;
}
:
	#(LOGICAL_AND x=boolean_factor y=boolean_factor
	{
		if (evalBool) {
			b = x && y;
		}
	}			
	) |	x=boolean_factor
	{
		if (evalBool) {
			b = x;
		}
	}			
;

boolean_expression returns [boolean b] throws PtalonRuntimeException
{
	b = false;
	boolean x, y;
}
:
	#(e:BOOLEAN_EXPRESSION (#(LOGICAL_OR x=boolean_term y=boolean_term
	{
		if (evalBool) {
			b = x || y;
		}
	}			
	) |	x=boolean_term
	{
		if (evalBool) {
			b = x;
		}
	}
	)
	{
		if (evalBool) {
			info.setBoolExpr(e.getText(), b);
		}	
	}
	)
;

atomic_statement throws PtalonRuntimeException
:
	(port_declaration | parameter_declaration |
		relation_declaration | actor_declaration)
;

conditional_statement throws PtalonRuntimeException
{
	boolean b;
	boolean ready;
}
:
	#(a:IF 
	{
		info.enterIfScope(a.getText());
		ready = info.isIfReady();
		if (ready) {
			evalBool = true;
		}
	}
	b=boolean_expression 
	{
		if (ready) {
			info.setActiveBranch(b);
			evalBool = false;
		}
	}
	#(TRUEBRANCH 
	{
		if (ready) {
			info.setCurrentBranch(true);
		}
	}
	(atomic_statement | conditional_statement)*) #(FALSEBRANCH
	{
		if (ready) {
			info.setCurrentBranch(false);
		}
	}
	(atomic_statement | conditional_statement)*))
	{
		info.exitIfScope();
	}
;	

actor_definition[NestedActorManager info] throws PtalonRuntimeException
{
	this.info = info;
	this.info.startAtTop();
}
:
	#(a:ACTOR_DEFINITION (import_declaration)* (atomic_statement | 
		conditional_statement)*)
;
