// $ANTLR : "populator.g" -> "PtalonPopulator.java"$
/* 

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

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

	import java.util.StringTokenizer;


public class PtalonPopulator extends antlr.TreeParser       implements PtalonPopulatorTokenTypes
 {

	private NestedActorManager info;

	public NestedActorManager getCodeManager() {
		return info;
	}
	
	private String scopeName;
	
	private boolean evalBool = false;
	
public PtalonPopulator() {
	tokenNames = _tokenNames;
}

	public final void import_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST import_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST import_declaration_AST = null;
		
		AST __t350 = _t;
		PtalonAST tmp1_AST = null;
		PtalonAST tmp1_AST_in = null;
		tmp1_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp1_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp1_AST);
		ASTPair __currentAST350 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IMPORT);
		_t = _t.getFirstChild();
		qualified_identifier(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST350;
		_t = __t350;
		_t = _t.getNextSibling();
		import_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = import_declaration_AST;
		_retTree = _t;
	}
	
	public final void qualified_identifier(AST _t) throws RecognitionException {
		
		PtalonAST qualified_identifier_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST qualified_identifier_AST = null;
		
		PtalonAST tmp2_AST = null;
		PtalonAST tmp2_AST_in = null;
		tmp2_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp2_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp2_AST);
		match(_t,QUALID);
		_t = _t.getNextSibling();
		qualified_identifier_AST = (PtalonAST)currentAST.root;
		returnAST = qualified_identifier_AST;
		_retTree = _t;
	}
	
	public final void port_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST port_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST port_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		PtalonAST c = null;
		PtalonAST c_AST = null;
		PtalonAST d = null;
		PtalonAST d_AST = null;
		PtalonAST e = null;
		PtalonAST e_AST = null;
		PtalonAST f = null;
		PtalonAST f_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PORT:
		{
			AST __t352 = _t;
			PtalonAST tmp3_AST = null;
			PtalonAST tmp3_AST_in = null;
			tmp3_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp3_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			ASTPair __currentAST352 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PORT);
			_t = _t.getFirstChild();
			a = (PtalonAST)_t;
			PtalonAST a_AST_in = null;
			a_AST = (PtalonAST)astFactory.create(a);
			astFactory.addASTChild(currentAST, a_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(a.getText())) {
						info.addPort(a.getText());
					}
				
			currentAST = __currentAST352;
			_t = __t352;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			AST __t353 = _t;
			PtalonAST tmp4_AST = null;
			PtalonAST tmp4_AST_in = null;
			tmp4_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp4_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST353 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,INPORT);
			_t = _t.getFirstChild();
			b = (PtalonAST)_t;
			PtalonAST b_AST_in = null;
			b_AST = (PtalonAST)astFactory.create(b);
			astFactory.addASTChild(currentAST, b_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(b.getText())) {
						info.addInPort(b.getText());
					}
				
			currentAST = __currentAST353;
			_t = __t353;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			AST __t354 = _t;
			PtalonAST tmp5_AST = null;
			PtalonAST tmp5_AST_in = null;
			tmp5_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp5_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST354 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,OUTPORT);
			_t = _t.getFirstChild();
			c = (PtalonAST)_t;
			PtalonAST c_AST_in = null;
			c_AST = (PtalonAST)astFactory.create(c);
			astFactory.addASTChild(currentAST, c_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(c.getText())) {
						info.addOutPort(c.getText());
					}
				
			currentAST = __currentAST354;
			_t = __t354;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIPORT:
		{
			AST __t355 = _t;
			PtalonAST tmp6_AST = null;
			PtalonAST tmp6_AST_in = null;
			tmp6_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp6_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp6_AST);
			ASTPair __currentAST355 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MULTIPORT);
			_t = _t.getFirstChild();
			d = (PtalonAST)_t;
			PtalonAST d_AST_in = null;
			d_AST = (PtalonAST)astFactory.create(d);
			astFactory.addASTChild(currentAST, d_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(d.getText())) {
						info.addPort(d.getText());
					}
				
			currentAST = __currentAST355;
			_t = __t355;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIINPORT:
		{
			AST __t356 = _t;
			PtalonAST tmp7_AST = null;
			PtalonAST tmp7_AST_in = null;
			tmp7_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp7_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp7_AST);
			ASTPair __currentAST356 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MULTIINPORT);
			_t = _t.getFirstChild();
			e = (PtalonAST)_t;
			PtalonAST e_AST_in = null;
			e_AST = (PtalonAST)astFactory.create(e);
			astFactory.addASTChild(currentAST, e_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(e.getText())) {
						info.addInPort(e.getText());
					}
				
			currentAST = __currentAST356;
			_t = __t356;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIOUTPORT:
		{
			AST __t357 = _t;
			PtalonAST tmp8_AST = null;
			PtalonAST tmp8_AST_in = null;
			tmp8_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp8_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp8_AST);
			ASTPair __currentAST357 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MULTIOUTPORT);
			_t = _t.getFirstChild();
			f = (PtalonAST)_t;
			PtalonAST f_AST_in = null;
			f_AST = (PtalonAST)astFactory.create(f);
			astFactory.addASTChild(currentAST, f_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(f.getText())) {
						info.addOutPort(f.getText());
					}
				
			currentAST = __currentAST357;
			_t = __t357;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = port_declaration_AST;
		_retTree = _t;
	}
	
	public final void parameter_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST parameter_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST parameter_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PARAMETER:
		{
			AST __t359 = _t;
			PtalonAST tmp9_AST = null;
			PtalonAST tmp9_AST_in = null;
			tmp9_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp9_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp9_AST);
			ASTPair __currentAST359 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PARAMETER);
			_t = _t.getFirstChild();
			a = (PtalonAST)_t;
			PtalonAST a_AST_in = null;
			a_AST = (PtalonAST)astFactory.create(a);
			astFactory.addASTChild(currentAST, a_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(a.getText())) {
						info.addParameter(a.getText());
					}
				
			currentAST = __currentAST359;
			_t = __t359;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ACTOR:
		{
			AST __t360 = _t;
			PtalonAST tmp10_AST = null;
			PtalonAST tmp10_AST_in = null;
			tmp10_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp10_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp10_AST);
			ASTPair __currentAST360 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ACTOR);
			_t = _t.getFirstChild();
			b = (PtalonAST)_t;
			PtalonAST b_AST_in = null;
			b_AST = (PtalonAST)astFactory.create(b);
			astFactory.addASTChild(currentAST, b_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					if (info.isReady() && !info.isCreated(b.getText())) {
						info.addActorParameter(b.getText());
					}
				
			currentAST = __currentAST360;
			_t = __t360;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = parameter_declaration_AST;
		_retTree = _t;
	}
	
	public final void relation_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST relation_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relation_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
		AST __t362 = _t;
		PtalonAST tmp11_AST = null;
		PtalonAST tmp11_AST_in = null;
		tmp11_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp11_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp11_AST);
		ASTPair __currentAST362 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,RELATION);
		_t = _t.getFirstChild();
		a = (PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		match(_t,ID);
		_t = _t.getNextSibling();
		
				if (info.isReady() && !info.isCreated(a.getText())) {
					info.addRelation(a.getText());
				}
			
		currentAST = __currentAST362;
		_t = __t362;
		_t = _t.getNextSibling();
		relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = relation_declaration_AST;
		_retTree = _t;
	}
	
	public final void assignment(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST assignment_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		
		AST __t365 = _t;
		PtalonAST tmp12_AST = null;
		PtalonAST tmp12_AST_in = null;
		tmp12_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp12_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp12_AST);
		ASTPair __currentAST365 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ASSIGN);
		_t = _t.getFirstChild();
		a = (PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		match(_t,ID);
		_t = _t.getNextSibling();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case ID:
		{
			b = (PtalonAST)_t;
			PtalonAST b_AST_in = null;
			b_AST = (PtalonAST)astFactory.create(b);
			astFactory.addASTChild(currentAST, b_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			break;
		}
		case ACTOR_DECLARATION:
		{
			nested_actor_declaration(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EXPRESSION:
		{
			PtalonAST tmp13_AST = null;
			PtalonAST tmp13_AST_in = null;
			tmp13_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp13_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp13_AST);
			match(_t,EXPRESSION);
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST365;
		_t = __t365;
		_t = _t.getNextSibling();
		assignment_AST = (PtalonAST)currentAST.root;
		returnAST = assignment_AST;
		_retTree = _t;
	}
	
/**
 * In this case we do not add any actors, but rather
 * defer this decision to any generated actors.
 */
	public final void nested_actor_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST nested_actor_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST nested_actor_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		
		AST __t372 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST372 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.enterActorDeclaration(a.getText());
			
		{
		_loop374:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==ASSIGN)) {
				b = _t==ASTNULL ? null : (PtalonAST)_t;
				assignment(_t);
				_t = _retTree;
				b_AST = (PtalonAST)returnAST;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop374;
			}
			
		} while (true);
		}
		
				info.exitActorDeclaration();
			
		currentAST = __currentAST372;
		_t = __t372;
		_t = _t.getNextSibling();
		nested_actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = nested_actor_declaration_AST;
		_retTree = _t;
	}
	
/**
 * This is for a top level actor declaration, which 
 * requires seperate treatement from a nested actor
 * declaration.
 */
	public final void actor_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST actor_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		
			boolean oldEvalBool = false;
		
		
		AST __t368 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST368 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.enterActorDeclaration(a.getText());
				if (info.isActorReady()) {
					oldEvalBool = evalBool;
					evalBool = true;
				}
			
		{
		_loop370:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==ASSIGN)) {
				b = _t==ASTNULL ? null : (PtalonAST)_t;
				assignment(_t);
				_t = _retTree;
				b_AST = (PtalonAST)returnAST;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop370;
			}
			
		} while (true);
		}
		
				if (info.isActorReady()) {
					evalBool = oldEvalBool;
					info.addActor(a.getText());
				}
				info.exitActorDeclaration();
			
		currentAST = __currentAST368;
		_t = __t368;
		_t = _t.getNextSibling();
		actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = actor_declaration_AST;
		_retTree = _t;
	}
	
	public final void atomic_statement(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST atomic_statement_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST atomic_statement_AST = null;
		
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PORT:
		case INPORT:
		case OUTPORT:
		case MULTIPORT:
		case MULTIINPORT:
		case MULTIOUTPORT:
		{
			port_declaration(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case PARAMETER:
		case ACTOR:
		{
			parameter_declaration(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RELATION:
		{
			relation_declaration(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case ACTOR_DECLARATION:
		{
			actor_declaration(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		atomic_statement_AST = (PtalonAST)currentAST.root;
		returnAST = atomic_statement_AST;
		_retTree = _t;
	}
	
	public final void conditional_statement(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST conditional_statement_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST conditional_statement_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST e = null;
		PtalonAST e_AST = null;
		
			boolean b;
			boolean ready;
		
		
		AST __t378 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST378 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IF);
		_t = _t.getFirstChild();
		
				info.enterIfScope(a.getText());
				ready = info.isIfReady();
				if (ready) {
					evalBool = true;
				}
			
		e = (PtalonAST)_t;
		PtalonAST e_AST_in = null;
		e_AST = (PtalonAST)astFactory.create(e);
		astFactory.addASTChild(currentAST, e_AST);
		match(_t,EXPRESSION);
		_t = _t.getNextSibling();
		
				if (ready) {
					info.setActiveBranch(info.evaluateBoolean(e.getText()));
					evalBool = false;
				}
			
		AST __t379 = _t;
		PtalonAST tmp14_AST = null;
		PtalonAST tmp14_AST_in = null;
		tmp14_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp14_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp14_AST);
		ASTPair __currentAST379 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TRUEBRANCH);
		_t = _t.getFirstChild();
		
				if (ready) {
					info.setCurrentBranch(true);
				}
			
		{
		_loop381:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case ACTOR:
			case RELATION:
			case ACTOR_DECLARATION:
			case MULTIPORT:
			case MULTIINPORT:
			case MULTIOUTPORT:
			{
				atomic_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			{
				conditional_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop381;
			}
			}
		} while (true);
		}
		currentAST = __currentAST379;
		_t = __t379;
		_t = _t.getNextSibling();
		AST __t382 = _t;
		PtalonAST tmp15_AST = null;
		PtalonAST tmp15_AST_in = null;
		tmp15_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp15_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp15_AST);
		ASTPair __currentAST382 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FALSEBRANCH);
		_t = _t.getFirstChild();
		
				if (ready) {
					info.setCurrentBranch(false);
				}
			
		{
		_loop384:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case ACTOR:
			case RELATION:
			case ACTOR_DECLARATION:
			case MULTIPORT:
			case MULTIINPORT:
			case MULTIOUTPORT:
			{
				atomic_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			{
				conditional_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop384;
			}
			}
		} while (true);
		}
		currentAST = __currentAST382;
		_t = __t382;
		_t = _t.getNextSibling();
		currentAST = __currentAST378;
		_t = __t378;
		_t = _t.getNextSibling();
		
				info.exitIfScope();
			
		conditional_statement_AST = (PtalonAST)currentAST.root;
		returnAST = conditional_statement_AST;
		_retTree = _t;
	}
	
	public final void actor_definition(AST _t,
		NestedActorManager info
	) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST actor_definition_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_definition_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			this.info = info;
			this.info.startAtTop();
		
		
		AST __t386 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST386 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DEFINITION);
		_t = _t.getFirstChild();
		{
		_loop388:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==IMPORT)) {
				import_declaration(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop388;
			}
			
		} while (true);
		}
		{
		_loop390:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case ACTOR:
			case RELATION:
			case ACTOR_DECLARATION:
			case MULTIPORT:
			case MULTIINPORT:
			case MULTIOUTPORT:
			{
				atomic_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			{
				conditional_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop390;
			}
			}
		} while (true);
		}
		currentAST = __currentAST386;
		_t = __t386;
		_t = _t.getNextSibling();
		actor_definition_AST = (PtalonAST)currentAST.root;
		returnAST = actor_definition_AST;
		_retTree = _t;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"import\"",
		"SEMI",
		"\"port\"",
		"LBRACKET",
		"RBRACKET",
		"\"inport\"",
		"\"outport\"",
		"ID",
		"\"parameter\"",
		"\"actor\"",
		"\"relation\"",
		"DOT",
		"ASSIGN",
		"RPAREN",
		"COMMA",
		"EXPRESSION",
		"LPAREN",
		"\"if\"",
		"LCURLY",
		"RCURLY",
		"\"else\"",
		"\"is\"",
		"\"true\"",
		"TRUEBRANCH",
		"\"false\"",
		"FALSEBRANCH",
		"QUALID",
		"ATTRIBUTE",
		"ACTOR_DECLARATION",
		"ACTOR_DEFINITION",
		"NEGATIVE_SIGN",
		"POSITIVE_SIGN",
		"ARITHMETIC_FACTOR",
		"BOOLEAN_FACTOR",
		"LOGICAL_BUFFER",
		"ARITHMETIC_EXPRESSION",
		"BOOLEAN_EXPRESSION",
		"MULTIPORT",
		"MULTIINPORT",
		"MULTIOUTPORT",
		"ESC",
		"NUMBER_LITERAL",
		"ATTRIBUTE_MARKER",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	}
	
