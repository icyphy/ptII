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

	private CodeManager info;

	public CodeManager getCodeManager() {
		return info;
	}
	
	private String scopeName;
	
	private boolean evalBool = false;
	
	/**
	 * This becomes true if this populator needs
	 * to be called again due to nested actor
	 * declarations.
	 */ 
	public boolean walkAgain = false;
	
	private boolean settingContainer = false;
	
public PtalonPopulator() {
	tokenNames = _tokenNames;
}

	public final void import_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST import_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST import_declaration_AST = null;
		
		AST __t1908 = _t;
		PtalonAST tmp1_AST = null;
		PtalonAST tmp1_AST_in = null;
		tmp1_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp1_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp1_AST);
		ASTPair __currentAST1908 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IMPORT);
		_t = _t.getFirstChild();
		qualified_identifier(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST1908;
		_t = __t1908;
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
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PORT:
		{
			AST __t1910 = _t;
			PtalonAST tmp3_AST = null;
			PtalonAST tmp3_AST_in = null;
			tmp3_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp3_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			ASTPair __currentAST1910 = currentAST.copy();
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
				
			currentAST = __currentAST1910;
			_t = __t1910;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			AST __t1911 = _t;
			PtalonAST tmp4_AST = null;
			PtalonAST tmp4_AST_in = null;
			tmp4_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp4_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST1911 = currentAST.copy();
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
				
			currentAST = __currentAST1911;
			_t = __t1911;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			AST __t1912 = _t;
			PtalonAST tmp5_AST = null;
			PtalonAST tmp5_AST_in = null;
			tmp5_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp5_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST1912 = currentAST.copy();
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
				
			currentAST = __currentAST1912;
			_t = __t1912;
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
		PtalonAST c = null;
		PtalonAST c_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PARAMETER:
		{
			AST __t1914 = _t;
			PtalonAST tmp6_AST = null;
			PtalonAST tmp6_AST_in = null;
			tmp6_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp6_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp6_AST);
			ASTPair __currentAST1914 = currentAST.copy();
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
				
			currentAST = __currentAST1914;
			_t = __t1914;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INTPARAMETER:
		{
			AST __t1915 = _t;
			PtalonAST tmp7_AST = null;
			PtalonAST tmp7_AST_in = null;
			tmp7_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp7_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp7_AST);
			ASTPair __currentAST1915 = currentAST.copy();
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
			
					if (info.isReady() && !info.isCreated(b.getText())) {
						info.addIntParameter(b.getText());
					}
				
			currentAST = __currentAST1915;
			_t = __t1915;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case BOOLPARAMETER:
		{
			AST __t1916 = _t;
			PtalonAST tmp8_AST = null;
			PtalonAST tmp8_AST_in = null;
			tmp8_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp8_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp8_AST);
			ASTPair __currentAST1916 = currentAST.copy();
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
			
					if (info.isReady() && !info.isCreated(c.getText())) {
						info.addBoolParameter(c.getText());
					}
				
			currentAST = __currentAST1916;
			_t = __t1916;
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
		
		AST __t1918 = _t;
		PtalonAST tmp9_AST = null;
		PtalonAST tmp9_AST_in = null;
		tmp9_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp9_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp9_AST);
		ASTPair __currentAST1918 = currentAST.copy();
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
			
		currentAST = __currentAST1918;
		_t = __t1918;
		_t = _t.getNextSibling();
		relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = relation_declaration_AST;
		_retTree = _t;
	}
	
	public final void attribute(AST _t) throws RecognitionException {
		
		PtalonAST attribute_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST attribute_AST = null;
		
		AST __t1921 = _t;
		PtalonAST tmp10_AST = null;
		PtalonAST tmp10_AST_in = null;
		tmp10_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp10_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp10_AST);
		ASTPair __currentAST1921 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ATTRIBUTE);
		_t = _t.getFirstChild();
		qualified_identifier(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST1921;
		_t = __t1921;
		_t = _t.getNextSibling();
		attribute_AST = (PtalonAST)currentAST.root;
		returnAST = attribute_AST;
		_retTree = _t;
	}
	
	public final void assignment(AST _t,
		String actorName, boolean set
	) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST assignment_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		
			int x;
			boolean y;
			String c;
		
		
		AST __t1923 = _t;
		PtalonAST tmp11_AST = null;
		PtalonAST tmp11_AST_in = null;
		tmp11_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp11_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp11_AST);
		ASTPair __currentAST1923 = currentAST.copy();
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
			
					if (set) {
						info.assign(actorName, a.getText(), b.getText());
					}
				
			break;
		}
		case ACTOR_DECLARATION:
		{
			nested_actor_declaration(_t,actorName, a.getText(), set);
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
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LOGICAL_AND:
		case LOGICAL_OR:
		case BOOLEAN_FACTOR:
		{
			y=boolean_expression(_t);
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
		currentAST = __currentAST1923;
		_t = __t1923;
		_t = _t.getNextSibling();
		assignment_AST = (PtalonAST)currentAST.root;
		returnAST = assignment_AST;
		_retTree = _t;
	}
	
/**
 * This is used for actor declarations declared in
 * another actor declaration.
 */
	public final void nested_actor_declaration(AST _t,
		String containingActor, String paramName, boolean set
	) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST nested_actor_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST nested_actor_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		
			String actorName = "";
		
		
		AST __t1933 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST1933 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				if (set) {
					actorName = info.assignActor(containingActor, paramName, a.getText());
					settingContainer = true;
				} else {
					actorName = a.getText();
				}
			
		{
		_loop1935:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==ASSIGN)) {
				b = _t==ASTNULL ? null : (PtalonAST)_t;
				nested_assignment(_t,actorName);
				_t = _retTree;
				b_AST = (PtalonAST)returnAST;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1935;
			}
			
		} while (true);
		}
		
				settingContainer = false;
			
		currentAST = __currentAST1933;
		_t = __t1933;
		_t = _t.getNextSibling();
		nested_actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = nested_actor_declaration_AST;
		_retTree = _t;
	}
	
	public final int  arithmetic_expression(AST _t) throws RecognitionException, PtalonRuntimeException {
		int i;
		
		PtalonAST arithmetic_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_expression_AST = null;
		
			i = 0;
			int x, y;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PLUS:
		{
			AST __t1945 = _t;
			PtalonAST tmp12_AST = null;
			PtalonAST tmp12_AST_in = null;
			tmp12_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp12_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp12_AST);
			ASTPair __currentAST1945 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS);
			_t = _t.getFirstChild();
			x=arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = x + y;
					}
				
			currentAST = __currentAST1945;
			_t = __t1945;
			_t = _t.getNextSibling();
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MINUS:
		{
			AST __t1946 = _t;
			PtalonAST tmp13_AST = null;
			PtalonAST tmp13_AST_in = null;
			tmp13_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp13_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp13_AST);
			ASTPair __currentAST1946 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS);
			_t = _t.getFirstChild();
			x=arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = x - y;
					}
				
			currentAST = __currentAST1946;
			_t = __t1946;
			_t = _t.getNextSibling();
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case STAR:
		case DIVIDE:
		case MOD:
		case ARITHMETIC_FACTOR:
		{
			x=arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = x;
					}
				
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = arithmetic_expression_AST;
		_retTree = _t;
		return i;
	}
	
	public final boolean  boolean_expression(AST _t) throws RecognitionException, PtalonRuntimeException {
		boolean b;
		
		PtalonAST boolean_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_expression_AST = null;
		
			b = false;
			boolean x, y;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_OR:
		{
			AST __t1961 = _t;
			PtalonAST tmp14_AST = null;
			PtalonAST tmp14_AST_in = null;
			tmp14_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp14_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp14_AST);
			ASTPair __currentAST1961 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LOGICAL_OR);
			_t = _t.getFirstChild();
			x=boolean_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=boolean_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = x || y;
					}
				
			currentAST = __currentAST1961;
			_t = __t1961;
			_t = _t.getNextSibling();
			boolean_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LOGICAL_AND:
		case BOOLEAN_FACTOR:
		{
			x=boolean_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = x;
					}
				
			boolean_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = boolean_expression_AST;
		_retTree = _t;
		return b;
	}
	
	public final void actor_declaration(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST actor_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST b = null;
		
			String actorName = "";
			boolean set = false;
		
		
		AST __t1926 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		ASTPair __currentAST1926 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		actor_declaration_AST = (PtalonAST)currentAST.root;
		
				String name = a.getText();
				try {
					if (info.isReady() && info.getType(name).equals("import")) {
					actorName = info.addActor(name);
					actor_declaration_AST = (PtalonAST)astFactory.create(ACTOR_DECLARATION,actorName);
					StringTokenizer tokenizer = new StringTokenizer(actorName, "+");
					try {
						tokenizer.nextToken();
						actorName = tokenizer.nextToken();
					} catch (NullPointerException e) {
						throw new PtalonRuntimeException("Bad name " + actorName + " given as name");
					}
					set = true;
				} else if (info.isReady() && info.getType(name).equals("parameter")) {
					if (info.paramHasValue(name)) {    				
					actorName = info.addActor(name);
					actor_declaration_AST = (PtalonAST)astFactory.create(ACTOR_DECLARATION,actorName);
					StringTokenizer tokenizer = new StringTokenizer(actorName, "+");
					try {
						tokenizer.nextToken();
						actorName = tokenizer.nextToken();
					} catch (NullPointerException e) {
						throw new PtalonRuntimeException("Bad name " + actorName + " given as name");
					}
					set = true;
					} else {
						actor_declaration_AST = (PtalonAST)astFactory.create(ACTOR_DECLARATION,name);
					}
					} else {
					actor_declaration_AST = (PtalonAST)astFactory.create(ACTOR_DECLARATION,name);
				}
				} catch (Exception e) {
					throw new PtalonRuntimeException("Problem with actor declaration " + name, e);
				}
			
		currentAST.root = actor_declaration_AST;
		currentAST.child = actor_declaration_AST!=null &&actor_declaration_AST.getFirstChild()!=null ?
			actor_declaration_AST.getFirstChild() : actor_declaration_AST;
		currentAST.advanceChildToEnd();
		{
		_loop1928:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==ASSIGN)) {
				b = _t==ASTNULL ? null : (PtalonAST)_t;
				assignment(_t,actorName, set);
				_t = _retTree;
				b_AST = (PtalonAST)returnAST;
				actor_declaration_AST = (PtalonAST)currentAST.root;
				
						actor_declaration_AST.addChild(b_AST);
					
			}
			else {
				break _loop1928;
			}
			
		} while (true);
		}
		currentAST = __currentAST1926;
		_t = __t1926;
		_t = _t.getNextSibling();
		returnAST = actor_declaration_AST;
		_retTree = _t;
	}
	
	public final void nested_assignment(AST _t,
		String actorName
	) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST nested_assignment_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST nested_assignment_AST = null;
		PtalonAST eq = null;
		PtalonAST eq_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		
			int x;
			boolean y;
			String c;
			boolean set = false;
		
		
		AST __t1930 = _t;
		eq = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST eq_AST_in = null;
		eq_AST = (PtalonAST)astFactory.create(eq);
		astFactory.addASTChild(currentAST, eq_AST);
		ASTPair __currentAST1930 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ASSIGN);
		_t = _t.getFirstChild();
		
				set = eq_AST.walkMe;
			
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
			
					if (settingContainer) {
						walkAgain = true;
						eq_AST.walkMe = true;
					} else if (set) {
						info.assign(actorName, a.getText(), b.getText());
					}
				
			break;
		}
		case ACTOR_DECLARATION:
		{
			nested_actor_declaration(_t,actorName, a.getText(), set);
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
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LOGICAL_AND:
		case LOGICAL_OR:
		case BOOLEAN_FACTOR:
		{
			y=boolean_expression(_t);
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
		
				if (set) {
					eq_AST.walkMe = false;			
				}
			
		currentAST = __currentAST1930;
		_t = __t1930;
		_t = _t.getNextSibling();
		nested_assignment_AST = (PtalonAST)currentAST.root;
		returnAST = nested_assignment_AST;
		_retTree = _t;
	}
	
	public final int  arithmetic_factor(AST _t) throws RecognitionException, PtalonRuntimeException {
		int i;
		
		PtalonAST arithmetic_factor_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_factor_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		
			i = 0;
			int x;
			int sign = 1;
		
		
		AST __t1937 = _t;
		PtalonAST tmp15_AST = null;
		PtalonAST tmp15_AST_in = null;
		tmp15_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp15_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp15_AST);
		ASTPair __currentAST1937 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ARITHMETIC_FACTOR);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case POSITIVE_SIGN:
		{
			PtalonAST tmp16_AST = null;
			PtalonAST tmp16_AST_in = null;
			tmp16_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp16_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(_t,POSITIVE_SIGN);
			_t = _t.getNextSibling();
			break;
		}
		case NEGATIVE_SIGN:
		{
			PtalonAST tmp17_AST = null;
			PtalonAST tmp17_AST_in = null;
			tmp17_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp17_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(_t,NEGATIVE_SIGN);
			_t = _t.getNextSibling();
			
					sign = -1;
				
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
			
					if (evalBool) {
						i = sign * info.getIntValueOf(a.getText());
					}
				
			break;
		}
		case NUMBER_LITERAL:
		{
			b = (PtalonAST)_t;
			PtalonAST b_AST_in = null;
			b_AST = (PtalonAST)astFactory.create(b);
			astFactory.addASTChild(currentAST, b_AST);
			match(_t,NUMBER_LITERAL);
			_t = _t.getNextSibling();
			
					if (evalBool) {
						i = sign * (new Integer(b.getText()));
					}
				
			break;
		}
		case MINUS:
		case STAR:
		case DIVIDE:
		case MOD:
		case PLUS:
		case ARITHMETIC_FACTOR:
		{
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = sign * x;
					}
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST1937;
		_t = __t1937;
		_t = _t.getNextSibling();
		arithmetic_factor_AST = (PtalonAST)currentAST.root;
		returnAST = arithmetic_factor_AST;
		_retTree = _t;
		return i;
	}
	
	public final int  arithmetic_term(AST _t) throws RecognitionException, PtalonRuntimeException {
		int i;
		
		PtalonAST arithmetic_term_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_term_AST = null;
		
			i = 0;
			int x, y;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case STAR:
		{
			AST __t1941 = _t;
			PtalonAST tmp18_AST = null;
			PtalonAST tmp18_AST_in = null;
			tmp18_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp18_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp18_AST);
			ASTPair __currentAST1941 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,STAR);
			_t = _t.getFirstChild();
			x=arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = x * y;
					}
				
			currentAST = __currentAST1941;
			_t = __t1941;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case DIVIDE:
		{
			AST __t1942 = _t;
			PtalonAST tmp19_AST = null;
			PtalonAST tmp19_AST_in = null;
			tmp19_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp19_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp19_AST);
			ASTPair __currentAST1942 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DIVIDE);
			_t = _t.getFirstChild();
			x=arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = x / y;
					}
				
			currentAST = __currentAST1942;
			_t = __t1942;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MOD:
		{
			AST __t1943 = _t;
			PtalonAST tmp20_AST = null;
			PtalonAST tmp20_AST_in = null;
			tmp20_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp20_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp20_AST);
			ASTPair __currentAST1943 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MOD);
			_t = _t.getFirstChild();
			x=arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = x % y;
					}
				
			currentAST = __currentAST1943;
			_t = __t1943;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ARITHMETIC_FACTOR:
		{
			x=arithmetic_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						i = x;
					}
				
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
		return i;
	}
	
	public final boolean  relational_expression(AST _t) throws RecognitionException, PtalonRuntimeException {
		boolean b;
		
		PtalonAST relational_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relational_expression_AST = null;
		
			b = false;
			int x,y;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EQUAL:
		{
			AST __t1948 = _t;
			PtalonAST tmp21_AST = null;
			PtalonAST tmp21_AST_in = null;
			tmp21_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp21_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp21_AST);
			ASTPair __currentAST1948 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,EQUAL);
			_t = _t.getFirstChild();
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = (x == y);
					}
				
			currentAST = __currentAST1948;
			_t = __t1948;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case NOT_EQUAL:
		{
			AST __t1949 = _t;
			PtalonAST tmp22_AST = null;
			PtalonAST tmp22_AST_in = null;
			tmp22_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp22_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp22_AST);
			ASTPair __currentAST1949 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NOT_EQUAL);
			_t = _t.getFirstChild();
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = (x != y);
					}
				
			currentAST = __currentAST1949;
			_t = __t1949;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LESS_THAN:
		{
			AST __t1950 = _t;
			PtalonAST tmp23_AST = null;
			PtalonAST tmp23_AST_in = null;
			tmp23_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp23_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp23_AST);
			ASTPair __currentAST1950 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LESS_THAN);
			_t = _t.getFirstChild();
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = (x < y);
					}
				
			currentAST = __currentAST1950;
			_t = __t1950;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case GREATER_THAN:
		{
			AST __t1951 = _t;
			PtalonAST tmp24_AST = null;
			PtalonAST tmp24_AST_in = null;
			tmp24_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp24_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp24_AST);
			ASTPair __currentAST1951 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GREATER_THAN);
			_t = _t.getFirstChild();
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = (x > y);
					}
				
			currentAST = __currentAST1951;
			_t = __t1951;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LESS_EQUAL:
		{
			AST __t1952 = _t;
			PtalonAST tmp25_AST = null;
			PtalonAST tmp25_AST_in = null;
			tmp25_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp25_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp25_AST);
			ASTPair __currentAST1952 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LESS_EQUAL);
			_t = _t.getFirstChild();
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = (x <= y);
					}
				
			currentAST = __currentAST1952;
			_t = __t1952;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case GREATER_EQUAL:
		{
			AST __t1953 = _t;
			PtalonAST tmp26_AST = null;
			PtalonAST tmp26_AST_in = null;
			tmp26_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp26_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp26_AST);
			ASTPair __currentAST1953 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GREATER_EQUAL);
			_t = _t.getFirstChild();
			x=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = (x >= y);
					}
				
			currentAST = __currentAST1953;
			_t = __t1953;
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
		return b;
	}
	
	public final boolean  boolean_factor(AST _t) throws RecognitionException, PtalonRuntimeException {
		boolean b;
		
		PtalonAST boolean_factor_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_factor_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			boolean x;
			b = false;
			boolean sign = true;
		
		
		AST __t1955 = _t;
		PtalonAST tmp27_AST = null;
		PtalonAST tmp27_AST_in = null;
		tmp27_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp27_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp27_AST);
		ASTPair __currentAST1955 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,BOOLEAN_FACTOR);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_NOT:
		{
			PtalonAST tmp28_AST = null;
			PtalonAST tmp28_AST_in = null;
			tmp28_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp28_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp28_AST);
			match(_t,LOGICAL_NOT);
			_t = _t.getNextSibling();
			
					sign = false;
				
			break;
		}
		case LOGICAL_BUFFER:
		{
			PtalonAST tmp29_AST = null;
			PtalonAST tmp29_AST_in = null;
			tmp29_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp29_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp29_AST);
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
			x=boolean_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = !(sign ^ x);
					}
				
			break;
		}
		case EQUAL:
		case NOT_EQUAL:
		case LESS_THAN:
		case GREATER_THAN:
		case LESS_EQUAL:
		case GREATER_EQUAL:
		{
			x=relational_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = !(sign ^ x);
					}
				
			break;
		}
		case TRUE:
		{
			PtalonAST tmp30_AST = null;
			PtalonAST tmp30_AST_in = null;
			tmp30_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp30_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp30_AST);
			match(_t,TRUE);
			_t = _t.getNextSibling();
			
					if (evalBool) {
						b = !(sign ^ true);
					}
				
			break;
		}
		case FALSE:
		{
			PtalonAST tmp31_AST = null;
			PtalonAST tmp31_AST_in = null;
			tmp31_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp31_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp31_AST);
			match(_t,FALSE);
			_t = _t.getNextSibling();
			
					if (evalBool) {
						b = !(sign ^ false);
					}
				
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
			
					if (evalBool) {
						b = !(sign ^ info.getBooleanValueOf(a.getText()));
					}
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST1955;
		_t = __t1955;
		_t = _t.getNextSibling();
		boolean_factor_AST = (PtalonAST)currentAST.root;
		returnAST = boolean_factor_AST;
		_retTree = _t;
		return b;
	}
	
	public final boolean  boolean_term(AST _t) throws RecognitionException, PtalonRuntimeException {
		boolean b;
		
		PtalonAST boolean_term_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_term_AST = null;
		
			boolean x, y;
			b = false;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_AND:
		{
			AST __t1959 = _t;
			PtalonAST tmp32_AST = null;
			PtalonAST tmp32_AST_in = null;
			tmp32_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp32_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp32_AST);
			ASTPair __currentAST1959 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LOGICAL_AND);
			_t = _t.getFirstChild();
			x=boolean_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			y=boolean_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = x && y;
					}
				
			currentAST = __currentAST1959;
			_t = __t1959;
			_t = _t.getNextSibling();
			boolean_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case BOOLEAN_FACTOR:
		{
			x=boolean_factor(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			
					if (evalBool) {
						b = x;
					}
				
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
		return b;
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
	
	public final void conditional_statement(AST _t) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST conditional_statement_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST conditional_statement_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			boolean b;
			boolean ready;
		
		
		AST __t1965 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST1965 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IF);
		_t = _t.getFirstChild();
		
				info.enterIfScope(a.getText());
				ready = info.isIfReady();
				if (ready) {
					evalBool = true;
				}
			
		b=boolean_expression(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		
				if (ready) {
					info.setActiveBranch(b);
					evalBool = false;
				}
			
		AST __t1966 = _t;
		PtalonAST tmp33_AST = null;
		PtalonAST tmp33_AST_in = null;
		tmp33_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp33_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp33_AST);
		ASTPair __currentAST1966 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TRUEBRANCH);
		_t = _t.getFirstChild();
		
				if (ready) {
					info.setCurrentBranch(true);
				}
			
		{
		_loop1968:
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
				break _loop1968;
			}
			}
		} while (true);
		}
		currentAST = __currentAST1966;
		_t = __t1966;
		_t = _t.getNextSibling();
		AST __t1969 = _t;
		PtalonAST tmp34_AST = null;
		PtalonAST tmp34_AST_in = null;
		tmp34_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp34_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp34_AST);
		ASTPair __currentAST1969 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FALSEBRANCH);
		_t = _t.getFirstChild();
		
				if (ready) {
					info.setCurrentBranch(false);
				}
			
		{
		_loop1971:
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
				break _loop1971;
			}
			}
		} while (true);
		}
		currentAST = __currentAST1969;
		_t = __t1969;
		_t = _t.getNextSibling();
		currentAST = __currentAST1965;
		_t = __t1965;
		_t = _t.getNextSibling();
		
				info.exitIfScope();
			
		conditional_statement_AST = (PtalonAST)currentAST.root;
		returnAST = conditional_statement_AST;
		_retTree = _t;
	}
	
	public final void actor_definition(AST _t,
		CodeManager info
	) throws RecognitionException, PtalonRuntimeException {
		
		PtalonAST actor_definition_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_definition_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			this.info = info;
			this.info.startAtTop();
		
		
		AST __t1973 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST1973 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DEFINITION);
		_t = _t.getFirstChild();
		
				if (!this.info.isActorSet()) {
					this.info.setActor(a.getText());
				}
			
		{
		_loop1975:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==IMPORT)) {
				import_declaration(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1975;
			}
			
		} while (true);
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case 3:
		case PORT:
		case INPORT:
		case OUTPORT:
		case PARAMETER:
		case INTPARAMETER:
		case BOOLPARAMETER:
		case RELATION:
		case IF:
		case ACTOR_DECLARATION:
		{
			{
			_loop1978:
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
					break _loop1978;
				}
				}
			} while (true);
			}
			break;
		}
		case ATTRIBUTE:
		{
			attribute(_t);
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
		currentAST = __currentAST1973;
		_t = __t1973;
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
		"BOOLPARAMETER",
		"\"relation\"",
		"DOT",
		"ATTRIBUTE_MARKER",
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
		"\"boolparameter\"",
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
		"LBRACKET",
		"RBRACKET",
		"BINARY_NOT",
		"ESC",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	}
	
