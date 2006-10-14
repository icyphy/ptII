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
			AST __t94 = _t;
			PtalonAST tmp1_AST = null;
			PtalonAST tmp1_AST_in = null;
			tmp1_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp1_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp1_AST);
			ASTPair __currentAST94 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PORT);
			_t = _t.getFirstChild();
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
				
						info.addSymbol(a.getText(), "port");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t96 = _t;
				PtalonAST tmp2_AST = null;
				PtalonAST tmp2_AST_in = null;
				tmp2_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp2_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp2_AST);
				ASTPair __currentAST96 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp3_AST = null;
				PtalonAST tmp3_AST_in = null;
				tmp3_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp3_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp3_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp4_AST = null;
				PtalonAST tmp4_AST_in = null;
				tmp4_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp4_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp4_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST96;
				_t = __t96;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST94;
			_t = __t94;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			AST __t97 = _t;
			PtalonAST tmp5_AST = null;
			PtalonAST tmp5_AST_in = null;
			tmp5_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp5_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST97 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,INPORT);
			_t = _t.getFirstChild();
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
				
						info.addSymbol(b.getText(), "inport");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t99 = _t;
				PtalonAST tmp6_AST = null;
				PtalonAST tmp6_AST_in = null;
				tmp6_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp6_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp6_AST);
				ASTPair __currentAST99 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp7_AST = null;
				PtalonAST tmp7_AST_in = null;
				tmp7_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp7_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp7_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp8_AST = null;
				PtalonAST tmp8_AST_in = null;
				tmp8_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp8_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp8_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST99;
				_t = __t99;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST97;
			_t = __t97;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			AST __t100 = _t;
			PtalonAST tmp9_AST = null;
			PtalonAST tmp9_AST_in = null;
			tmp9_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp9_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp9_AST);
			ASTPair __currentAST100 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,OUTPORT);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				c = (PtalonAST)_t;
				PtalonAST c_AST_in = null;
				c_AST = (PtalonAST)astFactory.create(c);
				astFactory.addASTChild(currentAST, c_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				
						info.addSymbol(c.getText(), "outport");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t102 = _t;
				PtalonAST tmp10_AST = null;
				PtalonAST tmp10_AST_in = null;
				tmp10_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp10_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp10_AST);
				ASTPair __currentAST102 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp11_AST = null;
				PtalonAST tmp11_AST_in = null;
				tmp11_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp11_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp11_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp12_AST = null;
				PtalonAST tmp12_AST_in = null;
				tmp12_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp12_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp12_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST102;
				_t = __t102;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST100;
			_t = __t100;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIPORT:
		{
			AST __t103 = _t;
			PtalonAST tmp13_AST = null;
			PtalonAST tmp13_AST_in = null;
			tmp13_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp13_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp13_AST);
			ASTPair __currentAST103 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MULTIPORT);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				d = (PtalonAST)_t;
				PtalonAST d_AST_in = null;
				d_AST = (PtalonAST)astFactory.create(d);
				astFactory.addASTChild(currentAST, d_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				
						info.addSymbol(d.getText(), "multiport");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t105 = _t;
				PtalonAST tmp14_AST = null;
				PtalonAST tmp14_AST_in = null;
				tmp14_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp14_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp14_AST);
				ASTPair __currentAST105 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp15_AST = null;
				PtalonAST tmp15_AST_in = null;
				tmp15_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp15_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp15_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp16_AST = null;
				PtalonAST tmp16_AST_in = null;
				tmp16_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp16_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp16_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST105;
				_t = __t105;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST103;
			_t = __t103;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIINPORT:
		{
			AST __t106 = _t;
			PtalonAST tmp17_AST = null;
			PtalonAST tmp17_AST_in = null;
			tmp17_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp17_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp17_AST);
			ASTPair __currentAST106 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MULTIINPORT);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				e = (PtalonAST)_t;
				PtalonAST e_AST_in = null;
				e_AST = (PtalonAST)astFactory.create(e);
				astFactory.addASTChild(currentAST, e_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				
						info.addSymbol(e.getText(), "multiinport");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t108 = _t;
				PtalonAST tmp18_AST = null;
				PtalonAST tmp18_AST_in = null;
				tmp18_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp18_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp18_AST);
				ASTPair __currentAST108 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp19_AST = null;
				PtalonAST tmp19_AST_in = null;
				tmp19_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp19_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp19_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp20_AST = null;
				PtalonAST tmp20_AST_in = null;
				tmp20_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp20_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp20_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST108;
				_t = __t108;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST106;
			_t = __t106;
			_t = _t.getNextSibling();
			port_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case MULTIOUTPORT:
		{
			AST __t109 = _t;
			PtalonAST tmp21_AST = null;
			PtalonAST tmp21_AST_in = null;
			tmp21_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp21_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp21_AST);
			ASTPair __currentAST109 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MULTIOUTPORT);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				f = (PtalonAST)_t;
				PtalonAST f_AST_in = null;
				f_AST = (PtalonAST)astFactory.create(f);
				astFactory.addASTChild(currentAST, f_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				
						info.addSymbol(f.getText(), "multioutport");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t111 = _t;
				PtalonAST tmp22_AST = null;
				PtalonAST tmp22_AST_in = null;
				tmp22_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp22_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp22_AST);
				ASTPair __currentAST111 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp23_AST = null;
				PtalonAST tmp23_AST_in = null;
				tmp23_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp23_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp23_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp24_AST = null;
				PtalonAST tmp24_AST_in = null;
				tmp24_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp24_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp24_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST111;
				_t = __t111;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST109;
			_t = __t109;
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
			AST __t113 = _t;
			PtalonAST tmp25_AST = null;
			PtalonAST tmp25_AST_in = null;
			tmp25_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp25_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp25_AST);
			ASTPair __currentAST113 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PARAMETER);
			_t = _t.getFirstChild();
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
				
						info.addSymbol(a.getText(), "parameter");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t115 = _t;
				PtalonAST tmp26_AST = null;
				PtalonAST tmp26_AST_in = null;
				tmp26_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp26_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp26_AST);
				ASTPair __currentAST115 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp27_AST = null;
				PtalonAST tmp27_AST_in = null;
				tmp27_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp27_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp27_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp28_AST = null;
				PtalonAST tmp28_AST_in = null;
				tmp28_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp28_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp28_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST115;
				_t = __t115;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST113;
			_t = __t113;
			_t = _t.getNextSibling();
			parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ACTOR:
		{
			AST __t116 = _t;
			PtalonAST tmp29_AST = null;
			PtalonAST tmp29_AST_in = null;
			tmp29_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp29_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp29_AST);
			ASTPair __currentAST116 = currentAST.copy();
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
				
			currentAST = __currentAST116;
			_t = __t116;
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
		PtalonAST b = null;
		PtalonAST b_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PARAM_EQUALS:
		{
			AST __t118 = _t;
			PtalonAST tmp30_AST = null;
			PtalonAST tmp30_AST_in = null;
			tmp30_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp30_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp30_AST);
			ASTPair __currentAST118 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PARAM_EQUALS);
			_t = _t.getFirstChild();
			AST __t119 = _t;
			PtalonAST tmp31_AST = null;
			PtalonAST tmp31_AST_in = null;
			tmp31_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp31_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp31_AST);
			ASTPair __currentAST119 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PARAMETER);
			_t = _t.getFirstChild();
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
				
						info.addSymbol(a.getText(), "parameter");
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t121 = _t;
				PtalonAST tmp32_AST = null;
				PtalonAST tmp32_AST_in = null;
				tmp32_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp32_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp32_AST);
				ASTPair __currentAST121 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				PtalonAST tmp33_AST = null;
				PtalonAST tmp33_AST_in = null;
				tmp33_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp33_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp33_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				PtalonAST tmp34_AST = null;
				PtalonAST tmp34_AST_in = null;
				tmp34_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp34_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp34_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST121;
				_t = __t121;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST119;
			_t = __t119;
			_t = _t.getNextSibling();
			PtalonAST tmp35_AST = null;
			PtalonAST tmp35_AST_in = null;
			tmp35_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp35_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp35_AST);
			match(_t,EXPRESSION);
			_t = _t.getNextSibling();
			currentAST = __currentAST118;
			_t = __t118;
			_t = _t.getNextSibling();
			assigned_parameter_declaration_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ACTOR_EQUALS:
		{
			AST __t122 = _t;
			PtalonAST tmp36_AST = null;
			PtalonAST tmp36_AST_in = null;
			tmp36_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp36_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp36_AST);
			ASTPair __currentAST122 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ACTOR_EQUALS);
			_t = _t.getFirstChild();
			AST __t123 = _t;
			PtalonAST tmp37_AST = null;
			PtalonAST tmp37_AST_in = null;
			tmp37_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp37_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp37_AST);
			ASTPair __currentAST123 = currentAST.copy();
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
				
			currentAST = __currentAST123;
			_t = __t123;
			_t = _t.getNextSibling();
			PtalonAST tmp38_AST = null;
			PtalonAST tmp38_AST_in = null;
			tmp38_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp38_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp38_AST);
			match(_t,QUALID);
			_t = _t.getNextSibling();
			currentAST = __currentAST122;
			_t = __t122;
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
	
	public final void relation_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST relation_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relation_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
		AST __t125 = _t;
		PtalonAST tmp39_AST = null;
		PtalonAST tmp39_AST_in = null;
		tmp39_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp39_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp39_AST);
		ASTPair __currentAST125 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,RELATION);
		_t = _t.getFirstChild();
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
			
					info.addSymbol(a.getText(), "relation");
				
			break;
		}
		case DYNAMIC_NAME:
		{
			AST __t127 = _t;
			PtalonAST tmp40_AST = null;
			PtalonAST tmp40_AST_in = null;
			tmp40_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp40_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp40_AST);
			ASTPair __currentAST127 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DYNAMIC_NAME);
			_t = _t.getFirstChild();
			PtalonAST tmp41_AST = null;
			PtalonAST tmp41_AST_in = null;
			tmp41_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp41_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp41_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			PtalonAST tmp42_AST = null;
			PtalonAST tmp42_AST_in = null;
			tmp42_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp42_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp42_AST);
			match(_t,EXPRESSION);
			_t = _t.getNextSibling();
			currentAST = __currentAST127;
			_t = __t127;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST125;
		_t = __t125;
		_t = _t.getNextSibling();
		relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = relation_declaration_AST;
		_retTree = _t;
	}
	
	public final void transparent_relation_declaration(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST transparent_relation_declaration_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST transparent_relation_declaration_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		
		AST __t129 = _t;
		PtalonAST tmp43_AST = null;
		PtalonAST tmp43_AST_in = null;
		tmp43_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp43_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp43_AST);
		ASTPair __currentAST129 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TRANSPARENT);
		_t = _t.getFirstChild();
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
			
					info.addSymbol(a.getText(), "transparent");
				
			break;
		}
		case DYNAMIC_NAME:
		{
			AST __t131 = _t;
			PtalonAST tmp44_AST = null;
			PtalonAST tmp44_AST_in = null;
			tmp44_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp44_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp44_AST);
			ASTPair __currentAST131 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DYNAMIC_NAME);
			_t = _t.getFirstChild();
			PtalonAST tmp45_AST = null;
			PtalonAST tmp45_AST_in = null;
			tmp45_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp45_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp45_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			PtalonAST tmp46_AST = null;
			PtalonAST tmp46_AST_in = null;
			tmp46_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp46_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp46_AST);
			match(_t,EXPRESSION);
			_t = _t.getNextSibling();
			currentAST = __currentAST131;
			_t = __t131;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST129;
		_t = __t129;
		_t = _t.getNextSibling();
		transparent_relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = transparent_relation_declaration_AST;
		_retTree = _t;
	}
	
	public final void assignment(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST assignment_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST left = null;
		PtalonAST left_AST = null;
		PtalonAST leftExp = null;
		PtalonAST leftExp_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		PtalonAST c = null;
		PtalonAST c_AST = null;
		PtalonAST d = null;
		PtalonAST d_AST = null;
		PtalonAST e = null;
		PtalonAST e_AST = null;
		
			boolean leftDynamic = false;
		
		
		AST __t133 = _t;
		PtalonAST tmp47_AST = null;
		PtalonAST tmp47_AST_in = null;
		tmp47_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp47_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp47_AST);
		ASTPair __currentAST133 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ASSIGN);
		_t = _t.getFirstChild();
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
			break;
		}
		case DYNAMIC_NAME:
		{
			AST __t135 = _t;
			PtalonAST tmp48_AST = null;
			PtalonAST tmp48_AST_in = null;
			tmp48_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp48_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp48_AST);
			ASTPair __currentAST135 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DYNAMIC_NAME);
			_t = _t.getFirstChild();
			left = (PtalonAST)_t;
			PtalonAST left_AST_in = null;
			left_AST = (PtalonAST)astFactory.create(left);
			astFactory.addASTChild(currentAST, left_AST);
			match(_t,ID);
			_t = _t.getNextSibling();
			leftExp = (PtalonAST)_t;
			PtalonAST leftExp_AST_in = null;
			leftExp_AST = (PtalonAST)astFactory.create(leftExp);
			astFactory.addASTChild(currentAST, leftExp_AST);
			match(_t,EXPRESSION);
			_t = _t.getNextSibling();
			
					leftDynamic = true;
					info.addUnknownLeftSide(left.getText(), leftExp.getText());
				
			currentAST = __currentAST135;
			_t = __t135;
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
		case DYNAMIC_NAME:
		{
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
				
						if (!leftDynamic) {
							info.addPortAssign(a.getText(), b.getText());
						}
					
				break;
			}
			case DYNAMIC_NAME:
			{
				AST __t138 = _t;
				PtalonAST tmp49_AST = null;
				PtalonAST tmp49_AST_in = null;
				tmp49_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
				tmp49_AST_in = (PtalonAST)_t;
				astFactory.addASTChild(currentAST, tmp49_AST);
				ASTPair __currentAST138 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DYNAMIC_NAME);
				_t = _t.getFirstChild();
				c = (PtalonAST)_t;
				PtalonAST c_AST_in = null;
				c_AST = (PtalonAST)astFactory.create(c);
				astFactory.addASTChild(currentAST, c_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				d = (PtalonAST)_t;
				PtalonAST d_AST_in = null;
				d_AST = (PtalonAST)astFactory.create(d);
				astFactory.addASTChild(currentAST, d_AST);
				match(_t,EXPRESSION);
				_t = _t.getNextSibling();
				currentAST = __currentAST138;
				_t = __t138;
				_t = _t.getNextSibling();
				
						if (!leftDynamic) {
							info.addPortAssign(a.getText(), c.getText(), d.getText());
						}
					
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
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
			
					if (!leftDynamic) {
						info.addParameterAssign(a.getText(), e.getText());
					}
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST133;
		_t = __t133;
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
		
		AST __t144 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST144 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.pushActorDeclaration(a.getText());
				info.setActorParameter(paramValue);
			
		{
		_loop146:
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
				break _loop146;
			}
			
		} while (true);
		}
		currentAST = __currentAST144;
		_t = __t144;
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
		
		AST __t140 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST140 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DECLARATION);
		_t = _t.getFirstChild();
		
				info.pushActorDeclaration(a.getText());
			
		{
		_loop142:
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
				break _loop142;
			}
			
		} while (true);
		}
		currentAST = __currentAST140;
		_t = __t140;
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
		case TRANSPARENT:
		{
			transparent_relation_declaration(_t);
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
		
		AST __t150 = _t;
		PtalonAST tmp50_AST = null;
		PtalonAST tmp50_AST_in = null;
		tmp50_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp50_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp50_AST);
		ASTPair __currentAST150 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,IF);
		_t = _t.getFirstChild();
		
				info.pushIfStatement();
			
		PtalonAST tmp51_AST = null;
		PtalonAST tmp51_AST_in = null;
		tmp51_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp51_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp51_AST);
		match(_t,EXPRESSION);
		_t = _t.getNextSibling();
		AST __t151 = _t;
		PtalonAST tmp52_AST = null;
		PtalonAST tmp52_AST_in = null;
		tmp52_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp52_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp52_AST);
		ASTPair __currentAST151 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TRUEBRANCH);
		_t = _t.getFirstChild();
		
					info.setCurrentBranch(true);
				
		{
		_loop153:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case ACTOR:
			case RELATION:
			case TRANSPARENT:
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
			case FOR:
			{
				iterative_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop153;
			}
			}
		} while (true);
		}
		currentAST = __currentAST151;
		_t = __t151;
		_t = _t.getNextSibling();
		AST __t154 = _t;
		PtalonAST tmp53_AST = null;
		PtalonAST tmp53_AST_in = null;
		tmp53_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp53_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp53_AST);
		ASTPair __currentAST154 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FALSEBRANCH);
		_t = _t.getFirstChild();
		
					info.setCurrentBranch(false);
				
		{
		_loop156:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case ACTOR:
			case RELATION:
			case TRANSPARENT:
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
			case FOR:
			{
				iterative_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop156;
			}
			}
		} while (true);
		}
		currentAST = __currentAST154;
		_t = __t154;
		_t = _t.getNextSibling();
		currentAST = __currentAST150;
		_t = __t150;
		_t = _t.getNextSibling();
		conditional_statement_AST = (PtalonAST)currentAST.root;
		
				conditional_statement_AST.setText(info.popIfStatement());
			
		conditional_statement_AST = (PtalonAST)currentAST.root;
		returnAST = conditional_statement_AST;
		_retTree = _t;
	}
	
	public final void iterative_statement(AST _t) throws RecognitionException, PtalonScopeException {
		
		PtalonAST iterative_statement_AST_in = (_t == ASTNULL) ? null : (PtalonAST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST iterative_statement_AST = null;
		PtalonAST a = null;
		PtalonAST a_AST = null;
		PtalonAST b = null;
		PtalonAST b_AST = null;
		PtalonAST c = null;
		PtalonAST c_AST = null;
		PtalonAST n = null;
		PtalonAST n_AST = null;
		
		AST __t158 = _t;
		PtalonAST tmp54_AST = null;
		PtalonAST tmp54_AST_in = null;
		tmp54_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp54_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp54_AST);
		ASTPair __currentAST158 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FOR);
		_t = _t.getFirstChild();
		AST __t159 = _t;
		PtalonAST tmp55_AST = null;
		PtalonAST tmp55_AST_in = null;
		tmp55_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp55_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp55_AST);
		ASTPair __currentAST159 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,VARIABLE);
		_t = _t.getFirstChild();
		a = (PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		match(_t,ID);
		_t = _t.getNextSibling();
		currentAST = __currentAST159;
		_t = __t159;
		_t = _t.getNextSibling();
		AST __t160 = _t;
		PtalonAST tmp56_AST = null;
		PtalonAST tmp56_AST_in = null;
		tmp56_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp56_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp56_AST);
		ASTPair __currentAST160 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,INITIALLY);
		_t = _t.getFirstChild();
		b = (PtalonAST)_t;
		PtalonAST b_AST_in = null;
		b_AST = (PtalonAST)astFactory.create(b);
		astFactory.addASTChild(currentAST, b_AST);
		match(_t,EXPRESSION);
		_t = _t.getNextSibling();
		currentAST = __currentAST160;
		_t = __t160;
		_t = _t.getNextSibling();
		AST __t161 = _t;
		PtalonAST tmp57_AST = null;
		PtalonAST tmp57_AST_in = null;
		tmp57_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp57_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp57_AST);
		ASTPair __currentAST161 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SATISFIES);
		_t = _t.getFirstChild();
		c = (PtalonAST)_t;
		PtalonAST c_AST_in = null;
		c_AST = (PtalonAST)astFactory.create(c);
		astFactory.addASTChild(currentAST, c_AST);
		match(_t,EXPRESSION);
		_t = _t.getNextSibling();
		currentAST = __currentAST161;
		_t = __t161;
		_t = _t.getNextSibling();
		
				info.pushForStatement(a.getText(), b.getText(), c.getText());
			
		{
		_loop163:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case ACTOR:
			case RELATION:
			case TRANSPARENT:
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
			case FOR:
			{
				iterative_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop163;
			}
			}
		} while (true);
		}
		AST __t164 = _t;
		PtalonAST tmp58_AST = null;
		PtalonAST tmp58_AST_in = null;
		tmp58_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
		tmp58_AST_in = (PtalonAST)_t;
		astFactory.addASTChild(currentAST, tmp58_AST);
		ASTPair __currentAST164 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,NEXT);
		_t = _t.getFirstChild();
		n = (PtalonAST)_t;
		PtalonAST n_AST_in = null;
		n_AST = (PtalonAST)astFactory.create(n);
		astFactory.addASTChild(currentAST, n_AST);
		match(_t,EXPRESSION);
		_t = _t.getNextSibling();
		
					info.setNextExpression(n.getText());
				
		currentAST = __currentAST164;
		_t = __t164;
		_t = _t.getNextSibling();
		currentAST = __currentAST158;
		_t = __t158;
		_t = _t.getNextSibling();
		iterative_statement_AST = (PtalonAST)currentAST.root;
		
				iterative_statement_AST.setText(info.popForStatement());
			
		iterative_statement_AST = (PtalonAST)currentAST.root;
		returnAST = iterative_statement_AST;
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
		
		
		AST __t166 = _t;
		a = _t==ASTNULL ? null :(PtalonAST)_t;
		PtalonAST a_AST_in = null;
		a_AST = (PtalonAST)astFactory.create(a);
		astFactory.addASTChild(currentAST, a_AST);
		ASTPair __currentAST166 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ACTOR_DEFINITION);
		_t = _t.getFirstChild();
		
				info.setActorSymbol(a.getText());
			
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case DANGLING_PORTS_OKAY:
		{
			PtalonAST tmp59_AST = null;
			PtalonAST tmp59_AST_in = null;
			tmp59_AST = (PtalonAST)astFactory.create((PtalonAST)_t);
			tmp59_AST_in = (PtalonAST)_t;
			astFactory.addASTChild(currentAST, tmp59_AST);
			match(_t,DANGLING_PORTS_OKAY);
			_t = _t.getNextSibling();
			
					info.setDanglingPortsOkay(true);
				
			break;
		}
		case 3:
		case PORT:
		case INPORT:
		case OUTPORT:
		case PARAMETER:
		case ACTOR:
		case RELATION:
		case TRANSPARENT:
		case IF:
		case FOR:
		case ACTOR_DECLARATION:
		case MULTIPORT:
		case MULTIINPORT:
		case MULTIOUTPORT:
		case PARAM_EQUALS:
		case ACTOR_EQUALS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		{
		_loop169:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case ACTOR:
			case RELATION:
			case TRANSPARENT:
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
			case FOR:
			{
				iterative_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop169;
			}
			}
		} while (true);
		}
		currentAST = __currentAST166;
		_t = __t166;
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
		"\"transparent\"",
		"COLON",
		"DOT",
		"\"import\"",
		"\"true\"",
		"\"false\"",
		"\"if\"",
		"\"else\"",
		"\"is\"",
		"\"for\"",
		"\"initially\"",
		"\"next\"",
		"\"danglingPortsOkay\"",
		"ASSIGN",
		"RPAREN",
		"COMMA",
		"EXPRESSION",
		"LPAREN",
		"SEMI",
		"COMMENT",
		"LCURLY",
		"RCURLY",
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
		"SATISFIES",
		"VARIABLE",
		"DYNAMIC_NAME",
		"ACTOR_LABEL",
		"QUALIFIED_PORT",
		"ESC",
		"NUMBER_LITERAL",
		"ATTRIBUTE_MARKER",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	}
	
