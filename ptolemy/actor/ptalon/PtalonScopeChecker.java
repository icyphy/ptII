// $ANTLR : "scopeChecker.g" -> "PtalonScopeChecker.java"$
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

	import java.util.LinkedList;


public class PtalonScopeChecker extends antlr.TreeParser       implements PtalonScopeCheckerTokenTypes
 {

	private NestedActorManager info;

	public NestedActorManager getCodeManager() {
		return info;
	}
	
	private String scopeName;
public PtalonScopeChecker() {
	tokenNames = _tokenNames;
}

	public final void port_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
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
			AST __t317 = _t;
			PtalonAST tmp1_AST = null;
			PtalonAST tmp1_AST_in = null;
			tmp1_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp1_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp1_AST);
			ASTPair __currentAST317 = currentAST.copy();
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
			
					info.addSymbol(a.getText(), "port");
				
			currentAST = __currentAST317;
			_t = __t317;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			AST __t318 = _t;
			PtalonAST tmp2_AST = null;
			PtalonAST tmp2_AST_in = null;
			tmp2_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp2_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp2_AST);
			ASTPair __currentAST318 = currentAST.copy();
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
			
					info.addSymbol(b.getText(), "inport");
				
			currentAST = __currentAST318;
			_t = __t318;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			AST __t319 = _t;
			PtalonAST tmp3_AST = null;
			PtalonAST tmp3_AST_in = null;
			tmp3_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp3_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			ASTPair __currentAST319 = currentAST.copy();
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
			
					info.addSymbol(c.getText(), "outport");
				
			currentAST = __currentAST319;
			_t = __t319;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIPORT:
		{
			AST __t320 = _t;
			PtalonAST tmp4_AST = null;
			PtalonAST tmp4_AST_in = null;
			tmp4_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp4_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST320 = currentAST.copy();
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
			
					info.addSymbol(d.getText(), "multiport");
				
			currentAST = __currentAST320;
			_t = __t320;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIINPORT:
		{
			AST __t321 = _t;
			PtalonAST tmp5_AST = null;
			PtalonAST tmp5_AST_in = null;
			tmp5_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp5_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST321 = currentAST.copy();
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
			
					info.addSymbol(e.getText(), "multiinport");
				
			currentAST = __currentAST321;
			_t = __t321;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIOUTPORT:
		{
			AST __t322 = _t;
			PtalonAST tmp6_AST = null;
			PtalonAST tmp6_AST_in = null;
			tmp6_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp6_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp6_AST);
			ASTPair __currentAST322 = currentAST.copy();
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
			
					info.addSymbol(f.getText(), "multioutport");
				
			currentAST = __currentAST322;
			_t = __t322;
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
	
	public final void parameter_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
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
			AST __t324 = _t;
			PtalonAST tmp7_AST = null;
			PtalonAST tmp7_AST_in = null;
			tmp7_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp7_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp7_AST);
			ASTPair __currentAST324 = currentAST.copy();
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
			
					info.addSymbol(a.getText(), "parameter");
				
			currentAST = __currentAST324;
			_t = __t324;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ACTOR:
		{
			AST __t325 = _t;
			PtalonAST tmp8_AST = null;
			PtalonAST tmp8_AST_in = null;
			tmp8_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp8_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp8_AST);
			ASTPair __currentAST325 = currentAST.copy();
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
			
					info.addSymbol(b.getText(), "actorparameter");
				
			currentAST = __currentAST325;
			_t = __t325;
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
	
	public final void assigned_parameter_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST assigned_parameter_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assigned_parameter_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST e = null;
		PtalonAST e_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		PtalonAST q_AST = null;
		PtalonAST q = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PARAM_EQUALS:
		{
			AST __t327 = _t;
			PtalonAST tmp9_AST = null;
			PtalonAST tmp9_AST_in = null;
			tmp9_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp9_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp9_AST);
			ASTPair __currentAST327 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PARAM_EQUALS);
			_t = _t.getFirstChild();
			AST __t328 = _t;
			PtalonAST tmp10_AST = null;
			PtalonAST tmp10_AST_in = null;
			tmp10_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp10_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp10_AST);
			ASTPair __currentAST328 = currentAST.copy();
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
			currentAST = __currentAST328;
			_t = __t328;
			_t = _t.getNextSibling();
			e = (PtalonAST)_t;
			PtalonAST e_AST_in = null;
			e_AST = (PtalonAST)astFactory.create(e);
			astFactory.addASTChild(currentAST, e_AST);
			match(_t,EXPRESSION);
			_t = _t.getNextSibling();
			
					info.addSymbol(a.getText(), "parameter");
				
			currentAST = __currentAST327;
			_t = __t327;
			_t = _t.getNextSibling();
			assigned_parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ACTOR_EQUALS:
		{
			AST __t329 = _t;
			PtalonAST tmp11_AST = null;
			PtalonAST tmp11_AST_in = null;
			tmp11_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp11_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp11_AST);
			ASTPair __currentAST329 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ACTOR_EQUALS);
			_t = _t.getFirstChild();
			AST __t330 = _t;
			PtalonAST tmp12_AST = null;
			PtalonAST tmp12_AST_in = null;
			tmp12_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp12_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp12_AST);
			ASTPair __currentAST330 = currentAST.copy();
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
			currentAST = __currentAST330;
			_t = __t330;
			_t = _t.getNextSibling();
			q = _t==ASTNULL ? null : (PtalonAST)_t;
			qualified_identifier(_t);
			_t = _retTree;
			q_AST = (PtalonAST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			
					info.addSymbol(b.getText(), "actorparameter");
				
			currentAST = __currentAST329;
			_t = __t329;
			_t = _t.getNextSibling();
			assigned_parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = assigned_parameter_declaration_AST;
		_retTree = _t;
	}
	
	public final void qualified_identifier(AST _t) throws RecognitionException {
		
		PtalonAST qualified_identifier_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST qualified_identifier_AST = null;
		
		PtalonAST tmp13_AST = null;
		PtalonAST tmp13_AST_in = null;
		tmp13_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp13_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp13_AST);
		match(_t,QUALID);
		_t = _t.getNextSibling();
		qualified_identifier_AST = (PtalonAST)currentAST.root;
		returnAST = qualified_identifier_AST;
		_retTree = _t;
	}
	
	public final void relation_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST relation_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relation_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
		AST __t332 = _t;
		PtalonAST tmp14_AST = null;
		PtalonAST tmp14_AST_in = null;
		tmp14_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp14_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp14_AST);
		ASTPair __currentAST332 = currentAST.copy();
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
		
				info.addSymbol(a.getText(), "relation");
			
		currentAST = __currentAST332;
		_t = __t332;
		_t = _t.getNextSibling();
		relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = relation_declaration_AST;
		_retTree = _t;
	}
	
	public final void assignment(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST assignment_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		PtalonAST e = null;
		PtalonAST e_AST = null;
		
			String arith, bool;
		
		
		AST __t335 = _t;
		PtalonAST tmp15_AST = null;
		PtalonAST tmp15_AST_in = null;
		tmp15_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp15_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp15_AST);
		ASTPair __currentAST335 = currentAST.copy();
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
			
					if (info.getType(b.getText()).endsWith("port")) {
						info.addPortAssign(a.getText(), b.getText());
					} else if (info.getType(b.getText()).equals("relation")) {
						info.addPortAssign(a.getText(), b.getText());
					}
				
			break;
		}
		case ACTOR_DECLARATION:
		{
			nested_actor_declaration(_t,a.getText());
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EXPRESSION:
		{
			e = (PtalonAST)_t;
			PtalonAST e_AST_in = null;
			e_AST = (PtalonAST)astFactory.create(e);
			astFactory.addASTChild(currentAST, e_AST);
			match(_t,EXPRESSION);
			_t = _t.getNextSibling();
			
					info.addParameterAssign(a.getText(), e.getText());
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST335;
		_t = __t335;
		_t = _t.getNextSibling();
		assignment_AST = (PtalonAST)currentAST.root;
		returnAST = assignment_AST;
		_retTree = _t;
	}
	
	public final void nested_actor_declaration(AST _t,
		String paramValue
	) throws RecognitionException, PtalonScopeException {
		
		PtalonAST nested_actor_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST nested_actor_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		
		AST __t342 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST342 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.pushActorDeclaration(a.getText());
				info.setActorParameter(paramValue);
			
		{
		_loop344:
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
				break _loop344;
			}
			
		} while (true);
		}
		currentAST = __currentAST342;
		_t = __t342;
		_t = _t.getNextSibling();
		nested_actor_declaration_AST = (PtalonAST)currentAST.root;
		
				String uniqueName = info.popActorDeclaration();
				nested_actor_declaration_AST.setText(uniqueName);
			
		nested_actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = nested_actor_declaration_AST;
		_retTree = _t;
	}
	
	public final void actor_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST actor_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		
		AST __t338 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST338 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.pushActorDeclaration(a.getText());
			
		{
		_loop340:
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
				break _loop340;
			}
			
		} while (true);
		}
		currentAST = __currentAST338;
		_t = __t338;
		_t = _t.getNextSibling();
		actor_declaration_AST = (PtalonAST)currentAST.root;
		
				String uniqueName = info.popActorDeclaration();
				actor_declaration_AST.setText(uniqueName);
			
		actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = actor_declaration_AST;
		_retTree = _t;
	}
	
	public final void atomic_statement(AST _t) throws RecognitionException, PtalonScopeException {
		
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
		case PARAM_EQUALS:
		case ACTOR_EQUALS:
		{
			assigned_parameter_declaration(_t);
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
	
	public final void conditional_statement(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST conditional_statement_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST conditional_statement_AST = null;
		
		AST __t348 = _t;
		PtalonAST tmp16_AST = null;
		PtalonAST tmp16_AST_in = null;
		tmp16_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp16_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp16_AST);
		ASTPair __currentAST348 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IF);
		_t = _t.getFirstChild();
		
				info.pushIfStatement();
			
		PtalonAST tmp17_AST = null;
		PtalonAST tmp17_AST_in = null;
		tmp17_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp17_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp17_AST);
		match(_t,EXPRESSION);
		_t = _t.getNextSibling();
		AST __t349 = _t;
		PtalonAST tmp18_AST = null;
		PtalonAST tmp18_AST_in = null;
		tmp18_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp18_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp18_AST);
		ASTPair __currentAST349 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TRUEBRANCH);
		_t = _t.getFirstChild();
		{
		_loop351:
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
			case PARAM_EQUALS:
			case ACTOR_EQUALS:
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
				break _loop351;
			}
			}
		} while (true);
		}
		currentAST = __currentAST349;
		_t = __t349;
		_t = _t.getNextSibling();
		AST __t352 = _t;
		PtalonAST tmp19_AST = null;
		PtalonAST tmp19_AST_in = null;
		tmp19_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp19_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp19_AST);
		ASTPair __currentAST352 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FALSEBRANCH);
		_t = _t.getFirstChild();
		{
		_loop354:
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
			case PARAM_EQUALS:
			case ACTOR_EQUALS:
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
				break _loop354;
			}
			}
		} while (true);
		}
		currentAST = __currentAST352;
		_t = __t352;
		_t = _t.getNextSibling();
		currentAST = __currentAST348;
		_t = __t348;
		_t = _t.getNextSibling();
		conditional_statement_AST = (PtalonAST)currentAST.root;
		
				conditional_statement_AST.setText(info.popIfStatement());
			
		conditional_statement_AST = (PtalonAST)currentAST.root;
		returnAST = conditional_statement_AST;
		_retTree = _t;
	}
	
	public final void actor_definition(AST _t,
		NestedActorManager manager
	) throws RecognitionException, PtalonScopeException {
		
		PtalonAST actor_definition_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_definition_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			info = manager;
		
		
		AST __t356 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST356 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DEFINITION);
		_t = _t.getFirstChild();
		
				info.setActorSymbol(a.getText());
			
		{
		_loop358:
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
			case PARAM_EQUALS:
			case ACTOR_EQUALS:
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
				break _loop358;
			}
			}
		} while (true);
		}
		currentAST = __currentAST356;
		_t = __t356;
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
		"\"port\"",
		"LBRACKET",
		"RBRACKET",
		"\"inport\"",
		"\"outport\"",
		"ID",
		"\"parameter\"",
		"EQUALS",
		"\"actor\"",
		"\"relation\"",
		"COLON",
		"DOT",
		"\"import\"",
		"\"true\"",
		"\"false\"",
		"ASSIGN",
		"RPAREN",
		"COMMA",
		"EXPRESSION",
		"LPAREN",
		"SEMI",
		"\"if\"",
		"LCURLY",
		"RCURLY",
		"\"else\"",
		"\"is\"",
		"TRUEBRANCH",
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
		"PARAM_EQUALS",
		"ACTOR_EQUALS",
		"ESC",
		"NUMBER_LITERAL",
		"ATTRIBUTE_MARKER",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	}
	
