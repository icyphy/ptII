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

	private CodeManager info;

	public CodeManager getCodeManager() {
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
		
		AST __t438 = _t;
		PtalonAST tmp1_AST = null;
		PtalonAST tmp1_AST_in = null;
		tmp1_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp1_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp1_AST);
		ASTPair __currentAST438 = currentAST.copy();
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
			
		currentAST = __currentAST438;
		_t = __t438;
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
			AST __t440 = _t;
			PtalonAST tmp3_AST = null;
			PtalonAST tmp3_AST_in = null;
			tmp3_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp3_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			ASTPair __currentAST440 = currentAST.copy();
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
				
			currentAST = __currentAST440;
			_t = __t440;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			AST __t441 = _t;
			PtalonAST tmp4_AST = null;
			PtalonAST tmp4_AST_in = null;
			tmp4_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp4_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST441 = currentAST.copy();
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
				
			currentAST = __currentAST441;
			_t = __t441;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			AST __t442 = _t;
			PtalonAST tmp5_AST = null;
			PtalonAST tmp5_AST_in = null;
			tmp5_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp5_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST442 = currentAST.copy();
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
				
			currentAST = __currentAST442;
			_t = __t442;
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
			AST __t444 = _t;
			PtalonAST tmp6_AST = null;
			PtalonAST tmp6_AST_in = null;
			tmp6_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp6_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp6_AST);
			ASTPair __currentAST444 = currentAST.copy();
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
				
			currentAST = __currentAST444;
			_t = __t444;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INTPARAMETER:
		{
			AST __t445 = _t;
			PtalonAST tmp7_AST = null;
			PtalonAST tmp7_AST_in = null;
			tmp7_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp7_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp7_AST);
			ASTPair __currentAST445 = currentAST.copy();
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
				
			currentAST = __currentAST445;
			_t = __t445;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case BOOLPARAMETER:
		{
			AST __t446 = _t;
			PtalonAST tmp8_AST = null;
			PtalonAST tmp8_AST_in = null;
			tmp8_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp8_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp8_AST);
			ASTPair __currentAST446 = currentAST.copy();
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
				
			currentAST = __currentAST446;
			_t = __t446;
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
		
		AST __t448 = _t;
		PtalonAST tmp9_AST = null;
		PtalonAST tmp9_AST_in = null;
		tmp9_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp9_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp9_AST);
		ASTPair __currentAST448 = currentAST.copy();
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
			
		currentAST = __currentAST448;
		_t = __t448;
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
		
		AST __t451 = _t;
		PtalonAST tmp10_AST = null;
		PtalonAST tmp10_AST_in = null;
		tmp10_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp10_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp10_AST);
		ASTPair __currentAST451 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ATTRIBUTE);
		_t = _t.getFirstChild();
		qualified_identifier(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST451;
		_t = __t451;
		_t = _t.getNextSibling();
		attribute_AST = (PtalonAST)currentAST.root;
		returnAST = attribute_AST;
		_retTree = _t;
	}
	
	public final void assignment(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST assignment_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		
		AST __t453 = _t;
		PtalonAST tmp11_AST = null;
		PtalonAST tmp11_AST_in = null;
		tmp11_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp11_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp11_AST);
		ASTPair __currentAST453 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ASSIGN);
		_t = _t.getFirstChild();
		PtalonAST tmp12_AST = null;
		PtalonAST tmp12_AST_in = null;
		tmp12_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp12_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp12_AST);
		match(_t,ID);
		_t = _t.getNextSibling();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case ID:
		{
			PtalonAST tmp13_AST = null;
			PtalonAST tmp13_AST_in = null;
			tmp13_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp13_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp13_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			break;
		}
		case ACTOR_DECLARATION:
		{
			actor_declaration(_t);
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
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LOGICAL_AND:
		case LOGICAL_OR:
		case BOOLEAN_FACTOR:
		{
			boolean_expression(_t);
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
		currentAST = __currentAST453;
		_t = __t453;
		_t = _t.getNextSibling();
		assignment_AST = (PtalonAST)currentAST.root;
		returnAST = assignment_AST;
		_retTree = _t;
	}
	
	public final void actor_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST actor_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
		AST __t456 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST456 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				String type = info.getType(a.getText());
				LinkedList<String> types = new LinkedList<String>();
				types.add("parameter");
				types.add("import");
				types.add("this");
				if (!types.contains(type)) {
					throw new PtalonScopeException(a.getText() + 
						" should have type parameter, import or this, but instead has type " + type);
				}
			
		{
		_loop458:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==ASSIGN)) {
				assignment(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop458;
			}
			
		} while (true);
		}
		currentAST = __currentAST456;
		_t = __t456;
		_t = _t.getNextSibling();
		actor_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = actor_declaration_AST;
		_retTree = _t;
	}
	
	public final void arithmetic_expression(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST arithmetic_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_expression_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PLUS:
		{
			AST __t468 = _t;
			PtalonAST tmp14_AST = null;
			PtalonAST tmp14_AST_in = null;
			tmp14_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp14_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp14_AST);
			ASTPair __currentAST468 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS);
			_t = _t.getFirstChild();
			arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST468;
			_t = __t468;
			_t = _t.getNextSibling();
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MINUS:
		{
			AST __t469 = _t;
			PtalonAST tmp15_AST = null;
			PtalonAST tmp15_AST_in = null;
			tmp15_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp15_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp15_AST);
			ASTPair __currentAST469 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS);
			_t = _t.getFirstChild();
			arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST469;
			_t = __t469;
			_t = _t.getNextSibling();
			arithmetic_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case STAR:
		case DIVIDE:
		case MOD:
		case ARITHMETIC_FACTOR:
		{
			arithmetic_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
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
	}
	
	public final void boolean_expression(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST boolean_expression_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_expression_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_OR:
		{
			AST __t484 = _t;
			PtalonAST tmp16_AST = null;
			PtalonAST tmp16_AST_in = null;
			tmp16_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp16_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp16_AST);
			ASTPair __currentAST484 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LOGICAL_OR);
			_t = _t.getFirstChild();
			boolean_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			boolean_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST484;
			_t = __t484;
			_t = _t.getNextSibling();
			boolean_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LOGICAL_AND:
		case BOOLEAN_FACTOR:
		{
			boolean_term(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
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
	}
	
	public final void arithmetic_factor(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST arithmetic_factor_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_factor_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
		AST __t460 = _t;
		PtalonAST tmp17_AST = null;
		PtalonAST tmp17_AST_in = null;
		tmp17_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp17_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp17_AST);
		ASTPair __currentAST460 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ARITHMETIC_FACTOR);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case POSITIVE_SIGN:
		{
			PtalonAST tmp18_AST = null;
			PtalonAST tmp18_AST_in = null;
			tmp18_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp18_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(_t,POSITIVE_SIGN);
			_t = _t.getNextSibling();
			break;
		}
		case NEGATIVE_SIGN:
		{
			PtalonAST tmp19_AST = null;
			PtalonAST tmp19_AST_in = null;
			tmp19_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp19_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp19_AST);
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
					
			break;
		}
		case NUMBER_LITERAL:
		{
			PtalonAST tmp20_AST = null;
			PtalonAST tmp20_AST_in = null;
			tmp20_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp20_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp20_AST);
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
			arithmetic_expression(_t);
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
		currentAST = __currentAST460;
		_t = __t460;
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
			AST __t464 = _t;
			PtalonAST tmp21_AST = null;
			PtalonAST tmp21_AST_in = null;
			tmp21_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp21_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp21_AST);
			ASTPair __currentAST464 = currentAST.copy();
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
			currentAST = __currentAST464;
			_t = __t464;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case DIVIDE:
		{
			AST __t465 = _t;
			PtalonAST tmp22_AST = null;
			PtalonAST tmp22_AST_in = null;
			tmp22_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp22_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp22_AST);
			ASTPair __currentAST465 = currentAST.copy();
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
			currentAST = __currentAST465;
			_t = __t465;
			_t = _t.getNextSibling();
			arithmetic_term_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MOD:
		{
			AST __t466 = _t;
			PtalonAST tmp23_AST = null;
			PtalonAST tmp23_AST_in = null;
			tmp23_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp23_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp23_AST);
			ASTPair __currentAST466 = currentAST.copy();
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
			currentAST = __currentAST466;
			_t = __t466;
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
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EQUAL:
		{
			AST __t471 = _t;
			PtalonAST tmp24_AST = null;
			PtalonAST tmp24_AST_in = null;
			tmp24_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp24_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp24_AST);
			ASTPair __currentAST471 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,EQUAL);
			_t = _t.getFirstChild();
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST471;
			_t = __t471;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case NOT_EQUAL:
		{
			AST __t472 = _t;
			PtalonAST tmp25_AST = null;
			PtalonAST tmp25_AST_in = null;
			tmp25_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp25_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp25_AST);
			ASTPair __currentAST472 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NOT_EQUAL);
			_t = _t.getFirstChild();
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST472;
			_t = __t472;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LESS_THAN:
		{
			AST __t473 = _t;
			PtalonAST tmp26_AST = null;
			PtalonAST tmp26_AST_in = null;
			tmp26_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp26_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp26_AST);
			ASTPair __currentAST473 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LESS_THAN);
			_t = _t.getFirstChild();
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST473;
			_t = __t473;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case GREATER_THAN:
		{
			AST __t474 = _t;
			PtalonAST tmp27_AST = null;
			PtalonAST tmp27_AST_in = null;
			tmp27_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp27_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp27_AST);
			ASTPair __currentAST474 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GREATER_THAN);
			_t = _t.getFirstChild();
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST474;
			_t = __t474;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case LESS_EQUAL:
		{
			AST __t475 = _t;
			PtalonAST tmp28_AST = null;
			PtalonAST tmp28_AST_in = null;
			tmp28_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp28_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp28_AST);
			ASTPair __currentAST475 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LESS_EQUAL);
			_t = _t.getFirstChild();
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST475;
			_t = __t475;
			_t = _t.getNextSibling();
			relational_expression_AST = (PtalonAST)currentAST.root;
			break;
		}
		case GREATER_EQUAL:
		{
			AST __t476 = _t;
			PtalonAST tmp29_AST = null;
			PtalonAST tmp29_AST_in = null;
			tmp29_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp29_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp29_AST);
			ASTPair __currentAST476 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GREATER_EQUAL);
			_t = _t.getFirstChild();
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			arithmetic_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST476;
			_t = __t476;
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
		
		AST __t478 = _t;
		PtalonAST tmp30_AST = null;
		PtalonAST tmp30_AST_in = null;
		tmp30_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp30_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp30_AST);
		ASTPair __currentAST478 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,BOOLEAN_FACTOR);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LOGICAL_NOT:
		{
			PtalonAST tmp31_AST = null;
			PtalonAST tmp31_AST_in = null;
			tmp31_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp31_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp31_AST);
			match(_t,LOGICAL_NOT);
			_t = _t.getNextSibling();
			break;
		}
		case LOGICAL_BUFFER:
		{
			PtalonAST tmp32_AST = null;
			PtalonAST tmp32_AST_in = null;
			tmp32_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp32_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp32_AST);
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
			boolean_expression(_t);
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
			PtalonAST tmp33_AST = null;
			PtalonAST tmp33_AST_in = null;
			tmp33_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp33_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp33_AST);
			match(_t,TRUE);
			_t = _t.getNextSibling();
			break;
		}
		case FALSE:
		{
			PtalonAST tmp34_AST = null;
			PtalonAST tmp34_AST_in = null;
			tmp34_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp34_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp34_AST);
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
					
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST478;
		_t = __t478;
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
			AST __t482 = _t;
			PtalonAST tmp35_AST = null;
			PtalonAST tmp35_AST_in = null;
			tmp35_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp35_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp35_AST);
			ASTPair __currentAST482 = currentAST.copy();
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
			currentAST = __currentAST482;
			_t = __t482;
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
		
		AST __t488 = _t;
		PtalonAST tmp36_AST = null;
		PtalonAST tmp36_AST_in = null;
		tmp36_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp36_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp36_AST);
		ASTPair __currentAST488 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IF);
		_t = _t.getFirstChild();
		
				info.pushIfStatement();
			
		boolean_expression(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		AST __t489 = _t;
		PtalonAST tmp37_AST = null;
		PtalonAST tmp37_AST_in = null;
		tmp37_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp37_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp37_AST);
		ASTPair __currentAST489 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TRUEBRANCH);
		_t = _t.getFirstChild();
		{
		_loop491:
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
				break _loop491;
			}
			}
		} while (true);
		}
		currentAST = __currentAST489;
		_t = __t489;
		_t = _t.getNextSibling();
		AST __t492 = _t;
		PtalonAST tmp38_AST = null;
		PtalonAST tmp38_AST_in = null;
		tmp38_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp38_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp38_AST);
		ASTPair __currentAST492 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FALSEBRANCH);
		_t = _t.getFirstChild();
		{
		_loop494:
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
				break _loop494;
			}
			}
		} while (true);
		}
		currentAST = __currentAST492;
		_t = __t492;
		_t = _t.getNextSibling();
		currentAST = __currentAST488;
		_t = __t488;
		_t = _t.getNextSibling();
		conditional_statement_AST = (PtalonAST)currentAST.root;
		
				conditional_statement_AST.setText(info.popIfStatement());
			
		conditional_statement_AST = (PtalonAST)currentAST.root;
		returnAST = conditional_statement_AST;
		_retTree = _t;
	}
	
	public final void actor_definition(AST _t,
		CodeManager manager
	) throws RecognitionException, PtalonScopeException {
		
		PtalonAST actor_definition_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_definition_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
			info = manager;
		
		
		AST __t496 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST496 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DEFINITION);
		_t = _t.getFirstChild();
		
				info.addSymbol(a.getText(), "this");
			
		{
		_loop498:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==IMPORT)) {
				import_declaration(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop498;
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
			_loop501:
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
					break _loop501;
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
		currentAST = __currentAST496;
		_t = __t496;
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
	
