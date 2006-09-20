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

	public final void import_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST import_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST import_declaration_AST = null;
		PtalonAST a_AST = null;
		PtalonAST a = null;
		
		AST __t181 = _t;
		PtalonAST tmp1_AST = null;
		PtalonAST tmp1_AST_in = null;
		tmp1_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp1_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp1_AST);
		ASTPair __currentAST181 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IMPORT);
		_t = _t.getFirstChild();
		a = _t==ASTNULL ? null : (PtalonAST)_t;
		qualified_identifier(_t);
		_t = _retTree;
		a_AST = (PtalonAST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		
				info.addImport(a.getText());
			
		currentAST = __currentAST181;
		_t = __t181;
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
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PORT:
		{
			AST __t183 = _t;
			PtalonAST tmp3_AST = null;
			PtalonAST tmp3_AST_in = null;
			tmp3_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp3_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			ASTPair __currentAST183 = currentAST.copy();
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
				
			currentAST = __currentAST183;
			_t = __t183;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			AST __t184 = _t;
			PtalonAST tmp4_AST = null;
			PtalonAST tmp4_AST_in = null;
			tmp4_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp4_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST184 = currentAST.copy();
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
				
			currentAST = __currentAST184;
			_t = __t184;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			AST __t185 = _t;
			PtalonAST tmp5_AST = null;
			PtalonAST tmp5_AST_in = null;
			tmp5_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp5_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST185 = currentAST.copy();
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
				
			currentAST = __currentAST185;
			_t = __t185;
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
		PtalonAST c = null;
		PtalonAST c_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PARAMETER:
		{
			AST __t187 = _t;
			PtalonAST tmp6_AST = null;
			PtalonAST tmp6_AST_in = null;
			tmp6_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp6_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp6_AST);
			ASTPair __currentAST187 = currentAST.copy();
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
				
			currentAST = __currentAST187;
			_t = __t187;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INTPARAMETER:
		{
			AST __t188 = _t;
			PtalonAST tmp7_AST = null;
			PtalonAST tmp7_AST_in = null;
			tmp7_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp7_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp7_AST);
			ASTPair __currentAST188 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,INTPARAMETER);
			_t = _t.getFirstChild();
			b = (PtalonAST)_t;
			PtalonAST b_AST_in = null;
			b_AST = (PtalonAST)astFactory.create(b);
			astFactory.addASTChild(currentAST, b_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					info.addSymbol(b.getText(), "intparameter");
				
			currentAST = __currentAST188;
			_t = __t188;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case BOOLPARAMETER:
		{
			AST __t189 = _t;
			PtalonAST tmp8_AST = null;
			PtalonAST tmp8_AST_in = null;
			tmp8_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp8_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp8_AST);
			ASTPair __currentAST189 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,BOOLPARAMETER);
			_t = _t.getFirstChild();
			c = (PtalonAST)_t;
			PtalonAST c_AST_in = null;
			c_AST = (PtalonAST)astFactory.create(c);
			astFactory.addASTChild(currentAST, c_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
					info.addSymbol(c.getText(), "boolparameter");
				
			currentAST = __currentAST189;
			_t = __t189;
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
	
	public final void relation_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST relation_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relation_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
		AST __t191 = _t;
		PtalonAST tmp9_AST = null;
		PtalonAST tmp9_AST_in = null;
		tmp9_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp9_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp9_AST);
		ASTPair __currentAST191 = currentAST.copy();
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
			
		currentAST = __currentAST191;
		_t = __t191;
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
		
			String arith, bool;
		
		
		AST __t194 = _t;
		PtalonAST tmp10_AST = null;
		PtalonAST tmp10_AST_in = null;
		tmp10_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp10_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp10_AST);
		ASTPair __currentAST194 = currentAST.copy();
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
			match(_t,ID);
			_t = _t.getNextSibling();
			assignment_AST = (PtalonAST)currentAST.root;
			
					if (info.getType(b.getText()).equals("intparameter")) {
						String arithmetic_label = info.getNextArithExpr();
						PtalonAST temp = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(ARITHMETIC_EXPRESSION,arithmetic_label)).add((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(ARITHMETIC_FACTOR,"arithmetic_factor")).add((PtalonAST)astFactory.create(POSITIVE_SIGN,"positive")).add(b_AST))));
						assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(ASSIGN)).add(a_AST).add(temp));
						info.addArithParam(a.getText(), arithmetic_label);
						info.putIntParamInScope(b.getText());
					} else if (info.getType(b.getText()).equals("boolparameter")) {
						String boolean_label = info.getNextBoolExpr();
						PtalonAST temp = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(BOOLEAN_EXPRESSION,boolean_label)).add((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(BOOLEAN_FACTOR,"boolean_factor")).add((PtalonAST)astFactory.create(LOGICAL_BUFFER,"!!")).add(b_AST))));
						assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(ASSIGN)).add(a_AST).add(temp));
						info.addBoolParam(a.getText(), boolean_label);
						info.putBoolParamInScope(b.getText());
					} else if (info.getType(b.getText()).endsWith("port")) {
						info.addPortAssign(a.getText(), b.getText());
						assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(ASSIGN)).add(a_AST).add(b_AST));
					} else if (info.getType(b.getText()).equals("relation")) {
						info.addPortAssign(a.getText(), b.getText());
						assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(ASSIGN)).add(a_AST).add(b_AST));
					}
				
			currentAST.root = assignment_AST;
			currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
				assignment_AST.getFirstChild() : assignment_AST;
			currentAST.advanceChildToEnd();
			break;
		}
		case ACTOR_DECLARATION:
		{
			nested_actor_declaration(_t,a.getText());
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case MINUS:
		case STAR:
		case DIVIDE:
		case MOD:
		case PLUS:
		case ARITHMETIC_FACTOR:
		{
			arith=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					info.addArithParam(a.getText(), arith);
				
			break;
		}
		case LOGICAL_AND:
		case LOGICAL_OR:
		case BOOLEAN_FACTOR:
		{
			bool=boolean_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					info.addBoolParam(a.getText(), bool);
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST194;
		_t = __t194;
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
		
		AST __t201 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST201 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.pushActorDeclaration(a.getText());
				info.setActorParameter(paramValue);
			
		{
		_loop203:
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
				break _loop203;
			}
			
		} while (true);
		}
		currentAST = __currentAST201;
		_t = __t201;
		_t = _t.getNextSibling();
		nested_actor_declaration_AST = (PtalonAST)currentAST.root;
		
				String uniqueName = info.popActorDeclaration();
				nested_actor_declaration_AST.setText(uniqueName);
			
		nested_actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = nested_actor_declaration_AST;
		_retTree = _t;
	}
	
	public final String  arithmetic_expression(AST _t) throws RecognitionException, PtalonScopeException {
		String expressionLabel;
		
		PtalonAST arithmetic_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_expression_AST = null;
		PtalonAST p = null;
		PtalonAST p_AST = null;
		PtalonAST a_AST = null;
		PtalonAST a = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		PtalonAST m = null;
		PtalonAST m_AST = null;
		PtalonAST c_AST = null;
		PtalonAST c = null;
		PtalonAST d_AST = null;
		PtalonAST d = null;
		PtalonAST e_AST = null;
		PtalonAST e = null;
		
			expressionLabel = "";
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PLUS:
		{
			AST __t213 = _t;
			p = _t==ASTNULL ? null :(PtalonAST)_t;
			PtalonAST p_AST_in = null;
			p_AST = (PtalonAST)astFactory.create(p);
			ASTPair __currentAST213 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS);
			_t = _t.getFirstChild();
			a = _t==ASTNULL ? null : (PtalonAST)_t;
			arithmetic_term(_t);
			_t = _retTree;
			a_AST = (PtalonAST)returnAST;
			b = _t==ASTNULL ? null : (PtalonAST)_t;
			arithmetic_term(_t);
			_t = _retTree;
			b_AST = (PtalonAST)returnAST;
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			
					expressionLabel = info.getNextBoolExpr();
					arithmetic_expression_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(ARITHMETIC_EXPRESSION,expressionLabel)).add((PtalonAST)astFactory.make( (new ASTArray(3)).add(p_AST).add(a_AST).add(b_AST))));
				
			currentAST.root = arithmetic_expression_AST;
			currentAST.child = arithmetic_expression_AST!=null &&arithmetic_expression_AST.getFirstChild()!=null ?
				arithmetic_expression_AST.getFirstChild() : arithmetic_expression_AST;
			currentAST.advanceChildToEnd();
			currentAST = __currentAST213;
			_t = __t213;
			_t = _t.getNextSibling();
			break;
		}
		case MINUS:
		{
			AST __t214 = _t;
			m = _t==ASTNULL ? null :(PtalonAST)_t;
			PtalonAST m_AST_in = null;
			m_AST = (PtalonAST)astFactory.create(m);
			ASTPair __currentAST214 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS);
			_t = _t.getFirstChild();
			c = _t==ASTNULL ? null : (PtalonAST)_t;
			arithmetic_term(_t);
			_t = _retTree;
			c_AST = (PtalonAST)returnAST;
			d = _t==ASTNULL ? null : (PtalonAST)_t;
			arithmetic_term(_t);
			_t = _retTree;
			d_AST = (PtalonAST)returnAST;
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			
					expressionLabel = info.getNextBoolExpr();
					arithmetic_expression_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(ARITHMETIC_EXPRESSION,expressionLabel)).add((PtalonAST)astFactory.make( (new ASTArray(3)).add(m_AST).add(c_AST).add(d_AST))));
				
			currentAST.root = arithmetic_expression_AST;
			currentAST.child = arithmetic_expression_AST!=null &&arithmetic_expression_AST.getFirstChild()!=null ?
				arithmetic_expression_AST.getFirstChild() : arithmetic_expression_AST;
			currentAST.advanceChildToEnd();
			currentAST = __currentAST214;
			_t = __t214;
			_t = _t.getNextSibling();
			break;
		}
		case STAR:
		case DIVIDE:
		case MOD:
		case ARITHMETIC_FACTOR:
		{
			e = _t==ASTNULL ? null : (PtalonAST)_t;
			arithmetic_term(_t);
			_t = _retTree;
			e_AST = (PtalonAST)returnAST;
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			
					expressionLabel = info.getNextBoolExpr();
					arithmetic_expression_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(ARITHMETIC_EXPRESSION,expressionLabel)).add(e_AST));
				
			currentAST.root = arithmetic_expression_AST;
			currentAST.child = arithmetic_expression_AST!=null &&arithmetic_expression_AST.getFirstChild()!=null ?
				arithmetic_expression_AST.getFirstChild() : arithmetic_expression_AST;
			currentAST.advanceChildToEnd();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = arithmetic_expression_AST;
		_retTree = _t;
		return expressionLabel;
	}
	
	public final String  boolean_expression(AST _t) throws RecognitionException, PtalonScopeException {
		String expressionLabel;
		
		PtalonAST boolean_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_expression_AST = null;
		PtalonAST l = null;
		PtalonAST l_AST = null;
		PtalonAST a_AST = null;
		PtalonAST a = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		PtalonAST c_AST = null;
		PtalonAST c = null;
		
			expressionLabel = "";
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_OR:
		{
			AST __t229 = _t;
			l = _t==ASTNULL ? null :(PtalonAST)_t;
			PtalonAST l_AST_in = null;
			l_AST = (PtalonAST)astFactory.create(l);
			ASTPair __currentAST229 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LOGICAL_OR);
			_t = _t.getFirstChild();
			a = _t==ASTNULL ? null : (PtalonAST)_t;
			boolean_term(_t);
			_t = _retTree;
			a_AST = (PtalonAST)returnAST;
			b = _t==ASTNULL ? null : (PtalonAST)_t;
			boolean_term(_t);
			_t = _retTree;
			b_AST = (PtalonAST)returnAST;
			currentAST = __currentAST229;
			_t = __t229;
			_t = _t.getNextSibling();
			break;
		}
		case LOGICAL_AND:
		case BOOLEAN_FACTOR:
		{
			boolean_expression_AST = (PtalonAST)currentAST.root;
			
					expressionLabel = info.getNextBoolExpr();
					boolean_expression_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(BOOLEAN_EXPRESSION,expressionLabel)).add((PtalonAST)astFactory.make( (new ASTArray(3)).add(l_AST).add(a_AST).add(b_AST))));
				
			currentAST.root = boolean_expression_AST;
			currentAST.child = boolean_expression_AST!=null &&boolean_expression_AST.getFirstChild()!=null ?
				boolean_expression_AST.getFirstChild() : boolean_expression_AST;
			currentAST.advanceChildToEnd();
			c = _t==ASTNULL ? null : (PtalonAST)_t;
			boolean_term(_t);
			_t = _retTree;
			c_AST = (PtalonAST)returnAST;
			boolean_expression_AST = (PtalonAST)currentAST.root;
			
					expressionLabel = info.getNextBoolExpr();
					boolean_expression_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(BOOLEAN_EXPRESSION,expressionLabel)).add(c_AST));
				
			currentAST.root = boolean_expression_AST;
			currentAST.child = boolean_expression_AST!=null &&boolean_expression_AST.getFirstChild()!=null ?
				boolean_expression_AST.getFirstChild() : boolean_expression_AST;
			currentAST.advanceChildToEnd();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = boolean_expression_AST;
		_retTree = _t;
		return expressionLabel;
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
		
		AST __t197 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST197 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.pushActorDeclaration(a.getText());
			
		{
		_loop199:
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
				break _loop199;
			}
			
		} while (true);
		}
		currentAST = __currentAST197;
		_t = __t197;
		_t = _t.getNextSibling();
		actor_declaration_AST = (PtalonAST)currentAST.root;
		
				String uniqueName = info.popActorDeclaration();
				actor_declaration_AST.setText(uniqueName);
			
		actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = actor_declaration_AST;
		_retTree = _t;
	}
	
	public final void arithmetic_factor(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST arithmetic_factor_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_factor_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			String foo;
		
		
		AST __t205 = _t;
		PtalonAST tmp11_AST = null;
		PtalonAST tmp11_AST_in = null;
		tmp11_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp11_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp11_AST);
		ASTPair __currentAST205 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ARITHMETIC_FACTOR);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case POSITIVE_SIGN:
		{
			PtalonAST tmp12_AST = null;
			PtalonAST tmp12_AST_in = null;
			tmp12_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp12_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp12_AST);
			match(_t,POSITIVE_SIGN);
			_t = _t.getNextSibling();
			break;
		}
		case NEGATIVE_SIGN:
		{
			PtalonAST tmp13_AST = null;
			PtalonAST tmp13_AST_in = null;
			tmp13_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp13_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp13_AST);
			match(_t,NEGATIVE_SIGN);
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case ID:
		{
			a = (PtalonAST)_t;
			PtalonAST a_AST_in = null;
			a_AST = (PtalonAST)astFactory.create(a);
			astFactory.addASTChild(currentAST, a_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
						String type = info.getType(a.getText());
						if (!type.equals("intparameter")) {
							throw new PtalonScopeException(a.getText() + 
								" should have type intparameter, but instead has type " + type);
						}
						info.putIntParamInScope(a.getText());
					
			break;
		}
		case NUMBER_LITERAL:
		{
			PtalonAST tmp14_AST = null;
			PtalonAST tmp14_AST_in = null;
			tmp14_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp14_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp14_AST);
			match(_t,NUMBER_LITERAL);
			_t = _t.getNextSibling();
			break;
		}
		case MINUS:
		case STAR:
		case DIVIDE:
		case MOD:
		case PLUS:
		case ARITHMETIC_FACTOR:
		{
			foo=arithmetic_expression(_t);
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
		currentAST = __currentAST205;
		_t = __t205;
		_t = _t.getNextSibling();
		arithmetic_factor_AST = (PtalonAST)currentAST.root;
		returnAST = arithmetic_factor_AST;
		_retTree = _t;
	}
	
	public final void arithmetic_term(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST arithmetic_term_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_term_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case STAR:
		{
			AST __t209 = _t;
			PtalonAST tmp15_AST = null;
			PtalonAST tmp15_AST_in = null;
			tmp15_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp15_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp15_AST);
			ASTPair __currentAST209 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,STAR);
			_t = _t.getFirstChild();
			arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST209;
			_t = __t209;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case DIVIDE:
		{
			AST __t210 = _t;
			PtalonAST tmp16_AST = null;
			PtalonAST tmp16_AST_in = null;
			tmp16_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp16_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp16_AST);
			ASTPair __currentAST210 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DIVIDE);
			_t = _t.getFirstChild();
			arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST210;
			_t = __t210;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MOD:
		{
			AST __t211 = _t;
			PtalonAST tmp17_AST = null;
			PtalonAST tmp17_AST_in = null;
			tmp17_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp17_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp17_AST);
			ASTPair __currentAST211 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MOD);
			_t = _t.getFirstChild();
			arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST211;
			_t = __t211;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ARITHMETIC_FACTOR:
		{
			arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = arithmetic_term_AST;
		_retTree = _t;
	}
	
	public final void relational_expression(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST relational_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relational_expression_AST = null;
		
			String foo;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EQUAL:
		{
			AST __t216 = _t;
			PtalonAST tmp18_AST = null;
			PtalonAST tmp18_AST_in = null;
			tmp18_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp18_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp18_AST);
			ASTPair __currentAST216 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,EQUAL);
			_t = _t.getFirstChild();
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST216;
			_t = __t216;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case NOT_EQUAL:
		{
			AST __t217 = _t;
			PtalonAST tmp19_AST = null;
			PtalonAST tmp19_AST_in = null;
			tmp19_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp19_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp19_AST);
			ASTPair __currentAST217 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NOT_EQUAL);
			_t = _t.getFirstChild();
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST217;
			_t = __t217;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LESS_THAN:
		{
			AST __t218 = _t;
			PtalonAST tmp20_AST = null;
			PtalonAST tmp20_AST_in = null;
			tmp20_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp20_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp20_AST);
			ASTPair __currentAST218 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LESS_THAN);
			_t = _t.getFirstChild();
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST218;
			_t = __t218;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case GREATER_THAN:
		{
			AST __t219 = _t;
			PtalonAST tmp21_AST = null;
			PtalonAST tmp21_AST_in = null;
			tmp21_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp21_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp21_AST);
			ASTPair __currentAST219 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GREATER_THAN);
			_t = _t.getFirstChild();
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST219;
			_t = __t219;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LESS_EQUAL:
		{
			AST __t220 = _t;
			PtalonAST tmp22_AST = null;
			PtalonAST tmp22_AST_in = null;
			tmp22_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp22_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp22_AST);
			ASTPair __currentAST220 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LESS_EQUAL);
			_t = _t.getFirstChild();
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST220;
			_t = __t220;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case GREATER_EQUAL:
		{
			AST __t221 = _t;
			PtalonAST tmp23_AST = null;
			PtalonAST tmp23_AST_in = null;
			tmp23_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp23_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp23_AST);
			ASTPair __currentAST221 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GREATER_EQUAL);
			_t = _t.getFirstChild();
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			foo=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST221;
			_t = __t221;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = relational_expression_AST;
		_retTree = _t;
	}
	
	public final void boolean_factor(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST boolean_factor_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_factor_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			String foo;
		
		
		AST __t223 = _t;
		PtalonAST tmp24_AST = null;
		PtalonAST tmp24_AST_in = null;
		tmp24_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp24_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp24_AST);
		ASTPair __currentAST223 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,BOOLEAN_FACTOR);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_NOT:
		{
			PtalonAST tmp25_AST = null;
			PtalonAST tmp25_AST_in = null;
			tmp25_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp25_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp25_AST);
			match(_t,LOGICAL_NOT);
			_t = _t.getNextSibling();
			break;
		}
		case LOGICAL_BUFFER:
		{
			PtalonAST tmp26_AST = null;
			PtalonAST tmp26_AST_in = null;
			tmp26_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp26_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp26_AST);
			match(_t,LOGICAL_BUFFER);
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_AND:
		case LOGICAL_OR:
		case BOOLEAN_FACTOR:
		{
			foo=boolean_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EQUAL:
		case NOT_EQUAL:
		case LESS_THAN:
		case GREATER_THAN:
		case LESS_EQUAL:
		case GREATER_EQUAL:
		{
			relational_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case TRUE:
		{
			PtalonAST tmp27_AST = null;
			PtalonAST tmp27_AST_in = null;
			tmp27_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp27_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp27_AST);
			match(_t,TRUE);
			_t = _t.getNextSibling();
			break;
		}
		case FALSE:
		{
			PtalonAST tmp28_AST = null;
			PtalonAST tmp28_AST_in = null;
			tmp28_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp28_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp28_AST);
			match(_t,FALSE);
			_t = _t.getNextSibling();
			break;
		}
		case ID:
		{
			a = (PtalonAST)_t;
			PtalonAST a_AST_in = null;
			a_AST = (PtalonAST)astFactory.create(a);
			astFactory.addASTChild(currentAST, a_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			
						String type = info.getType(a.getText());
						if (!type.equals("boolparameter")) {
							throw new PtalonScopeException(a.getText() + 
								" should have type boolparameter, but instead has type " + type);
						}
						info.putBoolParamInScope(a.getText());
					
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST223;
		_t = __t223;
		_t = _t.getNextSibling();
		boolean_factor_AST = (PtalonAST)currentAST.root;
		returnAST = boolean_factor_AST;
		_retTree = _t;
	}
	
	public final void boolean_term(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST boolean_term_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_term_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_AND:
		{
			AST __t227 = _t;
			PtalonAST tmp29_AST = null;
			PtalonAST tmp29_AST_in = null;
			tmp29_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp29_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp29_AST);
			ASTPair __currentAST227 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LOGICAL_AND);
			_t = _t.getFirstChild();
			boolean_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			boolean_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST227;
			_t = __t227;
			_t = _t.getNextSibling();
			boolean_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case BOOLEAN_FACTOR:
		{
			boolean_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			boolean_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = boolean_term_AST;
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
		{
			port_declaration(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case PARAMETER:
		case INTPARAMETER:
		case BOOLPARAMETER:
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
	
	public final void conditional_statement(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST conditional_statement_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST conditional_statement_AST = null;
		
			String foo;
		
		
		AST __t233 = _t;
		PtalonAST tmp30_AST = null;
		PtalonAST tmp30_AST_in = null;
		tmp30_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp30_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp30_AST);
		ASTPair __currentAST233 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IF);
		_t = _t.getFirstChild();
		
				info.pushIfStatement();
			
		foo=boolean_expression(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		AST __t234 = _t;
		PtalonAST tmp31_AST = null;
		PtalonAST tmp31_AST_in = null;
		tmp31_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp31_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp31_AST);
		ASTPair __currentAST234 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TRUEBRANCH);
		_t = _t.getFirstChild();
		{
		_loop236:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			case ACTOR_DECLARATION:
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
				break _loop236;
			}
			}
		} while (true);
		}
		currentAST = __currentAST234;
		_t = __t234;
		_t = _t.getNextSibling();
		AST __t237 = _t;
		PtalonAST tmp32_AST = null;
		PtalonAST tmp32_AST_in = null;
		tmp32_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp32_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp32_AST);
		ASTPair __currentAST237 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FALSEBRANCH);
		_t = _t.getFirstChild();
		{
		_loop239:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			case ACTOR_DECLARATION:
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
				break _loop239;
			}
			}
		} while (true);
		}
		currentAST = __currentAST237;
		_t = __t237;
		_t = _t.getNextSibling();
		currentAST = __currentAST233;
		_t = __t233;
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
		
		
		AST __t241 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST241 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DEFINITION);
		_t = _t.getFirstChild();
		
				info.setActorSymbol(a.getText());
			
		{
		_loop243:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==IMPORT)) {
				import_declaration(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop243;
			}
			
		} while (true);
		}
		{
		_loop245:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			case ACTOR_DECLARATION:
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
				break _loop245;
			}
			}
		} while (true);
		}
		currentAST = __currentAST241;
		_t = __t241;
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
		"\"inport\"",
		"\"outport\"",
		"ID",
		"\"parameter\"",
		"\"intparameter\"",
		"\"boolparameter\"",
		"\"relation\"",
		"DOT",
		"ASSIGN",
		"RPAREN",
		"COMMA",
		"LPAREN",
		"MINUS",
		"NUMBER_LITERAL",
		"STAR",
		"DIVIDE",
		"MOD",
		"PLUS",
		"EQUAL",
		"NOT_EQUAL",
		"LESS_THAN",
		"GREATER_THAN",
		"LESS_EQUAL",
		"GREATER_EQUAL",
		"LOGICAL_NOT",
		"\"true\"",
		"\"false\"",
		"LOGICAL_AND",
		"LOGICAL_OR",
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
		"LBRACKET",
		"RBRACKET",
		"BINARY_NOT",
		"ESC",
		"ATTRIBUTE_MARKER",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	}
	
