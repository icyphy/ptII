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

assignment [String actorName, boolean set] throws PtalonRuntimeException
{
	int x;
	boolean y;
}
:
	#(ASSIGN a:ID (b:ID 
	{
		if (set) {
			info.assign(actorName, a.getText(), b.getText());
		}
	}
	| actor_declaration | x=arithmetic_expression | y=boolean_expression))
;

actor_declaration! throws PtalonRuntimeException
{
	String actorName = "";
	boolean set = false;
}
:
	#(a:ACTOR_DECLARATION 
	{
		if (info.isReady() && !info.isCreated(a.getText())) {
			actorName = info.addActor(a.getText());
			#actor_declaration = #[ACTOR_DECLARATION, actorName];
			StringTokenizer tokenizer = new StringTokenizer(actorName, "+");
			try {
				tokenizer.nextToken();
				actorName = tokenizer.nextToken();
			} catch (NullPointerException e) {
				throw new PtalonRuntimeException("Bad name " + actorName + " given as name");
			}
			set = true;
		} else {
			#actor_declaration = #[ACTOR_DECLARATION, a.getText()];
		}
	}
	(b:assignment[actorName, set]
	{
		#actor_declaration.addChild(#b);
	}
	)*)
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
		if (info.isReady()) {
			i = sign * info.getIntValueOf(a.getText());
		}
	}
	| b:NUMBER_LITERAL 
	{
		if (info.isReady()) {
			i = sign * (new Integer(b.getText()));
		}
	}
	| x=arithmetic_expression
	{
		if (info.isReady()) {
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
		if (info.isReady()) {
			i = x * y;
		}
	}	
	) | #(DIVIDE x=arithmetic_factor y=arithmetic_factor
	{
		if (info.isReady()) {
			i = x / y;
		}
	}		
	) | #(MOD x=arithmetic_factor y=arithmetic_factor
	{
		if (info.isReady()) {
			i = x % y;
		}
	}		
	) | x=arithmetic_factor
	{
		if (info.isReady()) {
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
	#(PLUS x=arithmetic_term y=arithmetic_term
	{
		if (info.isReady()) {
			i = x + y;
		}
	}			
	) | #(MINUS x=arithmetic_term y=arithmetic_term
	{
		if (info.isReady()) {
			i = x - y;
		}
	}		
	) |	x=arithmetic_term
	{
		if (info.isReady()) {
			i = x;
		}
	}		
;

relational_expression returns [boolean b] throws PtalonRuntimeException
{
	b = false;
	int x,y;
}
:
	#(EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (info.isReady()) {
			b = (x == y);
		}
	}			
	) | #(NOT_EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (info.isReady()) {
			b = (x != y);
		}
	}			
	) | #(LESS_THAN x=arithmetic_expression y=arithmetic_expression
	{
		if (info.isReady()) {
			b = (x < y);
		}
	}				
	) | #(GREATER_THAN x=arithmetic_expression y=arithmetic_expression
	{
		if (info.isReady()) {
			b = (x > y);
		}
	}			
	) | #(LESS_EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (info.isReady()) {
			b = (x <= y);
		}
	}			
	) | #(GREATER_EQUAL x=arithmetic_expression y=arithmetic_expression
	{
		if (info.isReady()) {
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
		if (info.isReady()) {
			b = !(sign ^ x);
		}
	}			
	| x=relational_expression 
	{
		if (info.isReady()) {
			b = !(sign ^ x);
		}
	}				
	| TRUE 
	{
		if (info.isReady()) {
			b = !(sign ^ true);
		}
	}			
	| FALSE 
	{
		if (info.isReady()) {
			b = !(sign ^ false);
		}
	}			
	| a:ID
	{
		if (info.isReady()) {
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
		if (info.isReady()) {
			b = x && y;
		}
	}			
	) |	x=boolean_factor
	{
		if (info.isReady()) {
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
	#(LOGICAL_OR x=boolean_term y=boolean_term
	{
		if (info.isReady()) {
			b = x || y;
		}
	}			
	) |	x=boolean_term
	{
		if (info.isReady()) {
			b = x;
		}
	}			
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
		ready = info.isReady();
		info.enterIfScope(a.getText());
	}
	b=boolean_expression 
	{
		if (ready) {
			info.setActiveBranch(b);
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
