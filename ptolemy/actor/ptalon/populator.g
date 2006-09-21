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
	) | #(MULTIPORT d:ID
	{
		if (info.isReady() && !info.isCreated(d.getText())) {
			info.addPort(d.getText());
		}
	}
	) | #(MULTIINPORT e:ID
	{
		if (info.isReady() && !info.isCreated(e.getText())) {
			info.addInPort(e.getText());
		}
	}
	) | #(MULTIOUTPORT f:ID
	{
		if (info.isReady() && !info.isCreated(f.getText())) {
			info.addOutPort(f.getText());
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
	) | #(ACTOR b:ID
	{
		if (info.isReady() && !info.isCreated(b.getText())) {
			info.addActorParameter(b.getText());
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
:
	#(ASSIGN a:ID (b:ID	| nested_actor_declaration
	| EXPRESSION))
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
	e:EXPRESSION 
	{
		if (ready) {
			info.setActiveBranch(info.evaluateBoolean(e.getText()));
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
