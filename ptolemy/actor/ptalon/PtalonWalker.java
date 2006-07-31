// $ANTLR 2.7.6 (2005-12-22): "ptolemyTreeWalker.g" -> "PtalonWalker.java"$
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

	import java.lang.reflect.Method;
	import java.util.List;
	import java.util.ArrayList;
	import java.util.StringTokenizer;
	import ptolemy.data.expr.Parameter;
	import ptolemy.actor.TypedAtomicActor;
	import ptolemy.actor.TypedIORelation;
	import ptolemy.actor.TypedIOPort;
	import ptolemy.data.expr.FileParameter;
	import ptolemy.data.type.BaseType;
	import ptolemy.kernel.util.NameDuplicationException;
	import ptolemy.kernel.util.IllegalActionException;
	import ptolemy.kernel.util.Settable;


public class PtalonWalker extends antlr.TreeParser       implements PtalonWalkerTokenTypes
 {

	/**
	 * Respective lists of all paramters, inports, outports,
	 * and relations generated in the actor being generated.
	 */
	private ArrayList<PtalonParameter> _parameters = new ArrayList<PtalonParameter>();
	private ArrayList<PtalonPort> _ports = new ArrayList<PtalonPort>();
	private ArrayList<TypedIORelation> _relations = new ArrayList<TypedIORelation>();
	
	/**
	 * Returns true if the given string corresponds to a parameter.
	 * @param name The name to test.
	 * @return True if the name is a parameter.
	 */
	private boolean isParameter(String name) {
		for (int i=0; i < _parameters.size(); i++) {
			if (_parameters.get(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the given string corresponds to a port.
	 * @param name The name to test.
	 * @return True if the name is a port.
	 */
	private boolean isPort(String name) {
		for (int i=0; i < _ports.size(); i++) {
			if (_ports.get(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the given string corresponds to a relation.
	 * @param name The name to test.
	 * @return True if the name is a relation.
	 */
	private boolean isRelation(String name) {
		for (int i=0; i < _relations.size(); i++) {
			if (_relations.get(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * True when inside a conditional block.
	 */
	private boolean inConditional = false;

public PtalonWalker() {
	tokenNames = _tokenNames;
}

	public final void port_declaration(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException {
		
		AST port_declaration_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST port_declaration_AST = null;
		AST a = null;
		AST a_AST = null;
		AST b = null;
		AST b_AST = null;
		AST c = null;
		AST c_AST = null;
		AST d = null;
		AST d_AST = null;
		AST e = null;
		AST e_AST = null;
		AST f = null;
		AST f_AST = null;
		
			String flow = "";
		
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			{
				AST __t2 = _t;
				a = _t==ASTNULL ? null :(AST)_t;
				AST a_AST_in = null;
				a_AST = astFactory.create(a);
				ASTPair __currentAST2 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,PORT);
				_t = _t.getFirstChild();
				b = (AST)_t;
				AST b_AST_in = null;
				b_AST = astFactory.create(b);
				match(_t,ID);
				_t = _t.getNextSibling();
				currentAST = __currentAST2;
				_t = __t2;
				_t = _t.getNextSibling();
				port_declaration_AST = (AST)currentAST.root;
				
						if (!inConditional) {
						PtalonPort p;
						p = actor.addPort(b.getText(), PtalonActor.BIDIRECTIONAL);
						_ports.add(p);
						} else {
							port_declaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(a_AST).add(b_AST));
						}
					
				currentAST.root = port_declaration_AST;
				currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
					port_declaration_AST.getFirstChild() : port_declaration_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case INPORT:
			{
				AST __t3 = _t;
				c = _t==ASTNULL ? null :(AST)_t;
				AST c_AST_in = null;
				c_AST = astFactory.create(c);
				ASTPair __currentAST3 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,INPORT);
				_t = _t.getFirstChild();
				d = (AST)_t;
				AST d_AST_in = null;
				d_AST = astFactory.create(d);
				match(_t,ID);
				_t = _t.getNextSibling();
				currentAST = __currentAST3;
				_t = __t3;
				_t = _t.getNextSibling();
				port_declaration_AST = (AST)currentAST.root;
				
						if (!inConditional) {
						PtalonPort p;
						p = actor.addPort(d.getText(), PtalonActor.INPUT);
						_ports.add(p);
						} else {
							port_declaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(c_AST).add(d_AST));
						}
					
				currentAST.root = port_declaration_AST;
				currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
					port_declaration_AST.getFirstChild() : port_declaration_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case OUTPORT:
			{
				AST __t4 = _t;
				e = _t==ASTNULL ? null :(AST)_t;
				AST e_AST_in = null;
				e_AST = astFactory.create(e);
				ASTPair __currentAST4 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OUTPORT);
				_t = _t.getFirstChild();
				f = (AST)_t;
				AST f_AST_in = null;
				f_AST = astFactory.create(f);
				match(_t,ID);
				_t = _t.getNextSibling();
				currentAST = __currentAST4;
				_t = __t4;
				_t = _t.getNextSibling();
				port_declaration_AST = (AST)currentAST.root;
				
						if (!inConditional) {
						PtalonPort p;
						p = actor.addPort(f.getText(), PtalonActor.OUTPUT);
						_ports.add(p);
						} else {
							port_declaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(e_AST).add(f_AST));
						}
					
				currentAST.root = port_declaration_AST;
				currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
					port_declaration_AST.getFirstChild() : port_declaration_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = port_declaration_AST;
		_retTree = _t;
	}
	
	public final void parameter_declaration(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException {
		
		AST parameter_declaration_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameter_declaration_AST = null;
		AST a = null;
		AST a_AST = null;
		AST b = null;
		AST b_AST = null;
		AST c = null;
		AST c_AST = null;
		AST d = null;
		AST d_AST = null;
		AST e = null;
		AST e_AST = null;
		AST f = null;
		AST f_AST = null;
		
			String type = "";
		
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PARAMETER:
			{
				AST __t6 = _t;
				a = _t==ASTNULL ? null :(AST)_t;
				AST a_AST_in = null;
				a_AST = astFactory.create(a);
				ASTPair __currentAST6 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,PARAMETER);
				_t = _t.getFirstChild();
				b = (AST)_t;
				AST b_AST_in = null;
				b_AST = astFactory.create(b);
				match(_t,ID);
				_t = _t.getNextSibling();
				currentAST = __currentAST6;
				_t = __t6;
				_t = _t.getNextSibling();
				parameter_declaration_AST = (AST)currentAST.root;
				
						if (!inConditional) {
							PtalonParameter p = actor.addParameter(b.getText());
							p.setStringMode(true);
							_parameters.add(p);
						} else {
							parameter_declaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(a_AST).add(b_AST));
						}
					
				currentAST.root = parameter_declaration_AST;
				currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
					parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case INTPARAMETER:
			{
				AST __t7 = _t;
				c = _t==ASTNULL ? null :(AST)_t;
				AST c_AST_in = null;
				c_AST = astFactory.create(c);
				ASTPair __currentAST7 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,INTPARAMETER);
				_t = _t.getFirstChild();
				d = (AST)_t;
				AST d_AST_in = null;
				d_AST = astFactory.create(d);
				match(_t,ID);
				_t = _t.getNextSibling();
				currentAST = __currentAST7;
				_t = __t7;
				_t = _t.getNextSibling();
				parameter_declaration_AST = (AST)currentAST.root;
				
						if (!inConditional) {
							PtalonParameter p = actor.addParameter(d.getText());
							p.setTypeEquals(BaseType.INT);
							_parameters.add(p);
						} else {
							parameter_declaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(c_AST).add(d_AST));
						}
					
				currentAST.root = parameter_declaration_AST;
				currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
					parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case BOOLPARAMETER:
			{
				AST __t8 = _t;
				e = _t==ASTNULL ? null :(AST)_t;
				AST e_AST_in = null;
				e_AST = astFactory.create(e);
				ASTPair __currentAST8 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,BOOLPARAMETER);
				_t = _t.getFirstChild();
				f = (AST)_t;
				AST f_AST_in = null;
				f_AST = astFactory.create(f);
				match(_t,ID);
				_t = _t.getNextSibling();
				currentAST = __currentAST8;
				_t = __t8;
				_t = _t.getNextSibling();
				parameter_declaration_AST = (AST)currentAST.root;
				
						if (!inConditional) {
							PtalonParameter p = actor.addParameter(f.getText());
							p.setTypeEquals(BaseType.BOOLEAN);
							_parameters.add(p);
						} else {
							parameter_declaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(e_AST).add(f_AST));
						}
					
				currentAST.root = parameter_declaration_AST;
				currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
					parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = parameter_declaration_AST;
		_retTree = _t;
	}
	
	public final void relation_declaration(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException {
		
		AST relation_declaration_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relation_declaration_AST = null;
		AST a = null;
		AST a_AST = null;
		AST b = null;
		AST b_AST = null;
		
		try {      // for error handling
			AST __t10 = _t;
			a = _t==ASTNULL ? null :(AST)_t;
			AST a_AST_in = null;
			a_AST = astFactory.create(a);
			ASTPair __currentAST10 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RELATION);
			_t = _t.getFirstChild();
			b = (AST)_t;
			AST b_AST_in = null;
			b_AST = astFactory.create(b);
			match(_t,ID);
			_t = _t.getNextSibling();
			currentAST = __currentAST10;
			_t = __t10;
			_t = _t.getNextSibling();
			relation_declaration_AST = (AST)currentAST.root;
			
					if (!inConditional) {
					PtalonRelation r = actor.addRelation(b.getText());
					_relations.add(r);
					} else {
						relation_declaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(a_AST).add(b_AST));
					}
				
			currentAST.root = relation_declaration_AST;
			currentAST.child = relation_declaration_AST!=null &&relation_declaration_AST.getFirstChild()!=null ?
				relation_declaration_AST.getFirstChild() : relation_declaration_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = relation_declaration_AST;
		_retTree = _t;
	}
	
	public final String[]  qualified_identifier(AST _t) throws RecognitionException {
		String[] s;
		
		AST qualified_identifier_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST qualified_identifier_AST = null;
		AST a = null;
		AST a_AST = null;
		
			s = new String[] {"", ""};
		
		
		try {      // for error handling
			a = (AST)_t;
			AST a_AST_in = null;
			a_AST = astFactory.create(a);
			astFactory.addASTChild(currentAST, a_AST);
			match(_t,QUALID);
			_t = _t.getNextSibling();
			
					s[0] = a.getText();
					StringTokenizer tokenizer = new StringTokenizer(s[0], ".");
					while (tokenizer.hasMoreTokens()) {
						s[1] = tokenizer.nextToken();
					}
				
			qualified_identifier_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = qualified_identifier_AST;
		_retTree = _t;
		return s;
	}
	
	public final void attribute(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException {
		
		AST attribute_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST attribute_AST = null;
		AST a = null;
		AST a_AST = null;
		AST c_AST = null;
		AST c = null;
		
			String[] b;
		
		
		try {      // for error handling
			AST __t13 = _t;
			a = _t==ASTNULL ? null :(AST)_t;
			AST a_AST_in = null;
			a_AST = astFactory.create(a);
			ASTPair __currentAST13 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ATTRIBUTE);
			_t = _t.getFirstChild();
			c = _t==ASTNULL ? null : (AST)_t;
			b=qualified_identifier(_t);
			_t = _retTree;
			c_AST = (AST)returnAST;
			currentAST = __currentAST13;
			_t = __t13;
			_t = _t.getNextSibling();
			attribute_AST = (AST)currentAST.root;
			
				if (!inConditional) {
					if (a.getText().equals("actorSource")) {
						TypedAtomicActor aa = actor.addAtomicActor(b[0], b[1]);
					}
				} else {
					attribute_AST = (AST)astFactory.make( (new ASTArray(2)).add(a_AST).add(c_AST));
				}
			
			currentAST.root = attribute_AST;
			currentAST.child = attribute_AST!=null &&attribute_AST.getFirstChild()!=null ?
				attribute_AST.getFirstChild() : attribute_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = attribute_AST;
		_retTree = _t;
	}
	
	public final void assignment(AST _t,
		PtalonActor actor, String actorName
	) throws RecognitionException, IllegalActionException {
		
		AST assignment_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignment_AST = null;
		AST a = null;
		AST a_AST = null;
		AST b = null;
		AST b_AST = null;
		AST c = null;
		AST c_AST = null;
		AST d_AST = null;
		AST d = null;
		AST e_AST = null;
		AST e = null;
		AST f_AST = null;
		AST f = null;
		
			PtalonParameter param = null;
			String [] s;
			String l, r;
			Class actorClass = PtalonActor.class;
			Class[] parameterClass = null;
			ArrayList<PtalonObject> list = null;
			PtalonActor newActor = null;
		
		
		try {      // for error handling
			AST __t15 = _t;
			a = _t==ASTNULL ? null :(AST)_t;
			AST a_AST_in = null;
			a_AST = astFactory.create(a);
			ASTPair __currentAST15 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ASSIGN);
			_t = _t.getFirstChild();
			b = (AST)_t;
			AST b_AST_in = null;
			b_AST = astFactory.create(b);
			match(_t,ID);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				c = (AST)_t;
				AST c_AST_in = null;
				c_AST = astFactory.create(c);
				match(_t,ID);
				_t = _t.getNextSibling();
				assignment_AST = (AST)currentAST.root;
				
						if (!inConditional) {
							try {
								l = a.getText();
								r = b.getText();
								if (isParameter(actorName)) { 
									if (isPort(l) && isPort(r)) {
										Method portMethod = actorClass.getMethod("connectPorts", parameterClass);
										ArrayList<PtalonObject> list2 = new ArrayList<PtalonObject>();
										list2.add(param);
										PtalonParameter portParam = new PtalonParameter(actor, actor.uniqueName("bar"));
										portParam.setExpression(l);
										portParam.setVisibility(Settable.NONE);
										list2.add(portParam);
										PtalonPort port = (PtalonPort)actor.getPort(r);
										list2.add(port);
										param.addMethod(portMethod, list2);
									} else if (isPort(l) && isRelation(r)) {
										Method portMethod = actorClass.getMethod("linkToRelation", parameterClass);
										ArrayList list2 = new ArrayList<PtalonParameter>();
										list2.add(param);
										PtalonParameter portParam = new PtalonParameter(actor, actor.uniqueName("bar"));
										portParam.setExpression(l);
										portParam.setVisibility(Settable.NONE);
										list2.add(portParam);
										PtalonRelation relation = (PtalonRelation)actor.getRelation(r);
										list2.add(relation);
										param.addMethod(portMethod, list2);
									}
								} else {
									if (isPort(l) && isPort(r)) {
										TypedIORelation rel = new TypedIORelation(actor, actor.uniqueName("r"));
										PtalonPort lPort = (PtalonPort) newActor.getPort(l);
										PtalonPort rPort = (PtalonPort) actor.getPort(r);
										lPort.link(rel);
										rPort.link(rel);
									} else if (isPort(l) && isRelation(r)) {
										PtalonRelation rel = (PtalonRelation) actor.getRelation(r);
										PtalonPort lPort = (PtalonPort) actor.getPort(l);
										lPort.link(rel);
									}
								}
							} catch (Exception ex) {
								throw new IllegalActionException(actor, ex, ex.getMessage());
							}
						} else {
							assignment_AST = (AST)astFactory.make( (new ASTArray(3)).add(a_AST).add(b_AST).add(c_AST));
						}
					
				currentAST.root = assignment_AST;
				currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
					assignment_AST.getFirstChild() : assignment_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case ACTOR_DECLARATION:
			{
				d = _t==ASTNULL ? null : (AST)_t;
				actor_declaration(_t,actor);
				_t = _retTree;
				d_AST = (AST)returnAST;
				assignment_AST = (AST)currentAST.root;
				
						if (inConditional) {
							assignment_AST = (AST)astFactory.make( (new ASTArray(3)).add(a_AST).add(b_AST).add(d_AST));
						}
					
				currentAST.root = assignment_AST;
				currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
					assignment_AST.getFirstChild() : assignment_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case MINUS:
			case STAR:
			case DIVIDE:
			case MOD:
			case PLUS:
			case ARITHMETIC_FACTOR:
			{
				e = _t==ASTNULL ? null : (AST)_t;
				arithmetic_expression(_t);
				_t = _retTree;
				e_AST = (AST)returnAST;
				assignment_AST = (AST)currentAST.root;
				
						if (inConditional) {
							assignment_AST = (AST)astFactory.make( (new ASTArray(3)).add(a_AST).add(b_AST).add(e_AST));
						}
					
				currentAST.root = assignment_AST;
				currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
					assignment_AST.getFirstChild() : assignment_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LOGICAL_AND:
			case LOGICAL_OR:
			case BOOLEAN_FACTOR:
			{
				f = _t==ASTNULL ? null : (AST)_t;
				boolean_expression(_t);
				_t = _retTree;
				f_AST = (AST)returnAST;
				assignment_AST = (AST)currentAST.root;
				
						if (inConditional) {
							assignment_AST = (AST)astFactory.make( (new ASTArray(3)).add(a_AST).add(b_AST).add(f_AST));
						}
					
				currentAST.root = assignment_AST;
				currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
					assignment_AST.getFirstChild() : assignment_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST15;
			_t = __t15;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = assignment_AST;
		_retTree = _t;
	}
	
	public final void actor_declaration(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException {
		
		AST actor_declaration_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST actor_declaration_AST = null;
		AST a = null;
		AST a_AST = null;
		AST b_AST = null;
		AST b = null;
		
		try {      // for error handling
			AST __t18 = _t;
			a = _t==ASTNULL ? null :(AST)_t;
			AST a_AST_in = null;
			a_AST = astFactory.create(a);
			ASTPair __currentAST18 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ACTOR_DECLARATION);
			_t = _t.getFirstChild();
			actor_declaration_AST = (AST)currentAST.root;
			
					if (inConditional) {
						actor_declaration_AST = a_AST;
					}
				
			currentAST.root = actor_declaration_AST;
			currentAST.child = actor_declaration_AST!=null &&actor_declaration_AST.getFirstChild()!=null ?
				actor_declaration_AST.getFirstChild() : actor_declaration_AST;
			currentAST.advanceChildToEnd();
			{
			_loop20:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==ASSIGN)) {
					b = _t==ASTNULL ? null : (AST)_t;
					assignment(_t,actor, a.getText());
					_t = _retTree;
					b_AST = (AST)returnAST;
					actor_declaration_AST = (AST)currentAST.root;
					
							if (inConditional) {
								actor_declaration_AST.addChild(b_AST);
							}
						
				}
				else {
					break _loop20;
				}
				
			} while (true);
			}
			currentAST = __currentAST18;
			_t = __t18;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = actor_declaration_AST;
		_retTree = _t;
	}
	
	public final void arithmetic_expression(AST _t) throws RecognitionException {
		
		AST arithmetic_expression_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arithmetic_expression_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PLUS:
			{
				AST __t30 = _t;
				AST tmp1_AST = null;
				AST tmp1_AST_in = null;
				tmp1_AST = astFactory.create((AST)_t);
				tmp1_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp1_AST);
				ASTPair __currentAST30 = currentAST.copy();
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
				currentAST = __currentAST30;
				_t = __t30;
				_t = _t.getNextSibling();
				arithmetic_expression_AST = (AST)currentAST.root;
				break;
			}
			case MINUS:
			{
				AST __t31 = _t;
				AST tmp2_AST = null;
				AST tmp2_AST_in = null;
				tmp2_AST = astFactory.create((AST)_t);
				tmp2_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp2_AST);
				ASTPair __currentAST31 = currentAST.copy();
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
				currentAST = __currentAST31;
				_t = __t31;
				_t = _t.getNextSibling();
				arithmetic_expression_AST = (AST)currentAST.root;
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
				arithmetic_expression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = arithmetic_expression_AST;
		_retTree = _t;
	}
	
	public final void boolean_expression(AST _t) throws RecognitionException {
		
		AST boolean_expression_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_expression_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LOGICAL_OR:
			{
				AST __t46 = _t;
				AST tmp3_AST = null;
				AST tmp3_AST_in = null;
				tmp3_AST = astFactory.create((AST)_t);
				tmp3_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp3_AST);
				ASTPair __currentAST46 = currentAST.copy();
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
				currentAST = __currentAST46;
				_t = __t46;
				_t = _t.getNextSibling();
				boolean_expression_AST = (AST)currentAST.root;
				break;
			}
			case LOGICAL_AND:
			case BOOLEAN_FACTOR:
			{
				boolean_term(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				boolean_expression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = boolean_expression_AST;
		_retTree = _t;
	}
	
	public final void arithmetic_factor(AST _t) throws RecognitionException {
		
		AST arithmetic_factor_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arithmetic_factor_AST = null;
		
		try {      // for error handling
			AST __t22 = _t;
			AST tmp4_AST = null;
			AST tmp4_AST_in = null;
			tmp4_AST = astFactory.create((AST)_t);
			tmp4_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST22 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ARITHMETIC_FACTOR);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case POSITIVE_SIGN:
			{
				AST tmp5_AST = null;
				AST tmp5_AST_in = null;
				tmp5_AST = astFactory.create((AST)_t);
				tmp5_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp5_AST);
				match(_t,POSITIVE_SIGN);
				_t = _t.getNextSibling();
				break;
			}
			case NEGATIVE_SIGN:
			{
				AST tmp6_AST = null;
				AST tmp6_AST_in = null;
				tmp6_AST = astFactory.create((AST)_t);
				tmp6_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp6_AST);
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
				AST tmp7_AST = null;
				AST tmp7_AST_in = null;
				tmp7_AST = astFactory.create((AST)_t);
				tmp7_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp7_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				break;
			}
			case NUMBER_LITERAL:
			{
				AST tmp8_AST = null;
				AST tmp8_AST_in = null;
				tmp8_AST = astFactory.create((AST)_t);
				tmp8_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp8_AST);
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
			currentAST = __currentAST22;
			_t = __t22;
			_t = _t.getNextSibling();
			arithmetic_factor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = arithmetic_factor_AST;
		_retTree = _t;
	}
	
	public final void arithmetic_term(AST _t) throws RecognitionException {
		
		AST arithmetic_term_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arithmetic_term_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STAR:
			{
				AST __t26 = _t;
				AST tmp9_AST = null;
				AST tmp9_AST_in = null;
				tmp9_AST = astFactory.create((AST)_t);
				tmp9_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp9_AST);
				ASTPair __currentAST26 = currentAST.copy();
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
				currentAST = __currentAST26;
				_t = __t26;
				_t = _t.getNextSibling();
				arithmetic_term_AST = (AST)currentAST.root;
				break;
			}
			case DIVIDE:
			{
				AST __t27 = _t;
				AST tmp10_AST = null;
				AST tmp10_AST_in = null;
				tmp10_AST = astFactory.create((AST)_t);
				tmp10_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp10_AST);
				ASTPair __currentAST27 = currentAST.copy();
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
				currentAST = __currentAST27;
				_t = __t27;
				_t = _t.getNextSibling();
				arithmetic_term_AST = (AST)currentAST.root;
				break;
			}
			case MOD:
			{
				AST __t28 = _t;
				AST tmp11_AST = null;
				AST tmp11_AST_in = null;
				tmp11_AST = astFactory.create((AST)_t);
				tmp11_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp11_AST);
				ASTPair __currentAST28 = currentAST.copy();
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
				currentAST = __currentAST28;
				_t = __t28;
				_t = _t.getNextSibling();
				arithmetic_term_AST = (AST)currentAST.root;
				break;
			}
			case ARITHMETIC_FACTOR:
			{
				arithmetic_factor(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				arithmetic_term_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = arithmetic_term_AST;
		_retTree = _t;
	}
	
	public final void relational_expression(AST _t) throws RecognitionException {
		
		AST relational_expression_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relational_expression_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUAL:
			{
				AST __t33 = _t;
				AST tmp12_AST = null;
				AST tmp12_AST_in = null;
				tmp12_AST = astFactory.create((AST)_t);
				tmp12_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp12_AST);
				ASTPair __currentAST33 = currentAST.copy();
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
				currentAST = __currentAST33;
				_t = __t33;
				_t = _t.getNextSibling();
				relational_expression_AST = (AST)currentAST.root;
				break;
			}
			case NOT_EQUAL:
			{
				AST __t34 = _t;
				AST tmp13_AST = null;
				AST tmp13_AST_in = null;
				tmp13_AST = astFactory.create((AST)_t);
				tmp13_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp13_AST);
				ASTPair __currentAST34 = currentAST.copy();
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
				currentAST = __currentAST34;
				_t = __t34;
				_t = _t.getNextSibling();
				relational_expression_AST = (AST)currentAST.root;
				break;
			}
			case LESS_THAN:
			{
				AST __t35 = _t;
				AST tmp14_AST = null;
				AST tmp14_AST_in = null;
				tmp14_AST = astFactory.create((AST)_t);
				tmp14_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp14_AST);
				ASTPair __currentAST35 = currentAST.copy();
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
				currentAST = __currentAST35;
				_t = __t35;
				_t = _t.getNextSibling();
				relational_expression_AST = (AST)currentAST.root;
				break;
			}
			case GREATER_THAN:
			{
				AST __t36 = _t;
				AST tmp15_AST = null;
				AST tmp15_AST_in = null;
				tmp15_AST = astFactory.create((AST)_t);
				tmp15_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp15_AST);
				ASTPair __currentAST36 = currentAST.copy();
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
				currentAST = __currentAST36;
				_t = __t36;
				_t = _t.getNextSibling();
				relational_expression_AST = (AST)currentAST.root;
				break;
			}
			case LESS_EQUAL:
			{
				AST __t37 = _t;
				AST tmp16_AST = null;
				AST tmp16_AST_in = null;
				tmp16_AST = astFactory.create((AST)_t);
				tmp16_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp16_AST);
				ASTPair __currentAST37 = currentAST.copy();
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
				currentAST = __currentAST37;
				_t = __t37;
				_t = _t.getNextSibling();
				relational_expression_AST = (AST)currentAST.root;
				break;
			}
			case GREATER_EQUAL:
			{
				AST __t38 = _t;
				AST tmp17_AST = null;
				AST tmp17_AST_in = null;
				tmp17_AST = astFactory.create((AST)_t);
				tmp17_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp17_AST);
				ASTPair __currentAST38 = currentAST.copy();
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
				currentAST = __currentAST38;
				_t = __t38;
				_t = _t.getNextSibling();
				relational_expression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = relational_expression_AST;
		_retTree = _t;
	}
	
	public final void boolean_factor(AST _t) throws RecognitionException {
		
		AST boolean_factor_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_factor_AST = null;
		
		try {      // for error handling
			AST __t40 = _t;
			AST tmp18_AST = null;
			AST tmp18_AST_in = null;
			tmp18_AST = astFactory.create((AST)_t);
			tmp18_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp18_AST);
			ASTPair __currentAST40 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,BOOLEAN_FACTOR);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LOGICAL_NOT:
			{
				AST tmp19_AST = null;
				AST tmp19_AST_in = null;
				tmp19_AST = astFactory.create((AST)_t);
				tmp19_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp19_AST);
				match(_t,LOGICAL_NOT);
				_t = _t.getNextSibling();
				break;
			}
			case LOGICAL_BUFFER:
			{
				AST tmp20_AST = null;
				AST tmp20_AST_in = null;
				tmp20_AST = astFactory.create((AST)_t);
				tmp20_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp20_AST);
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
				AST tmp21_AST = null;
				AST tmp21_AST_in = null;
				tmp21_AST = astFactory.create((AST)_t);
				tmp21_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp21_AST);
				match(_t,TRUE);
				_t = _t.getNextSibling();
				break;
			}
			case FALSE:
			{
				AST tmp22_AST = null;
				AST tmp22_AST_in = null;
				tmp22_AST = astFactory.create((AST)_t);
				tmp22_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp22_AST);
				match(_t,FALSE);
				_t = _t.getNextSibling();
				break;
			}
			case ID:
			{
				AST tmp23_AST = null;
				AST tmp23_AST_in = null;
				tmp23_AST = astFactory.create((AST)_t);
				tmp23_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp23_AST);
				match(_t,ID);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST40;
			_t = __t40;
			_t = _t.getNextSibling();
			boolean_factor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = boolean_factor_AST;
		_retTree = _t;
	}
	
	public final void boolean_term(AST _t) throws RecognitionException {
		
		AST boolean_term_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_term_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LOGICAL_AND:
			{
				AST __t44 = _t;
				AST tmp24_AST = null;
				AST tmp24_AST_in = null;
				tmp24_AST = astFactory.create((AST)_t);
				tmp24_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp24_AST);
				ASTPair __currentAST44 = currentAST.copy();
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
				currentAST = __currentAST44;
				_t = __t44;
				_t = _t.getNextSibling();
				boolean_term_AST = (AST)currentAST.root;
				break;
			}
			case BOOLEAN_FACTOR:
			{
				boolean_factor(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				boolean_term_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = boolean_term_AST;
		_retTree = _t;
	}
	
	public final void atomic_statement(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException {
		
		AST atomic_statement_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST atomic_statement_AST = null;
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			{
				port_declaration(_t,actor);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			{
				parameter_declaration(_t,actor);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RELATION:
			{
				relation_declaration(_t,actor);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case ACTOR_DECLARATION:
			{
				actor_declaration(_t,actor);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case ATTRIBUTE:
			{
				attribute(_t,actor);
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
			atomic_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = atomic_statement_AST;
		_retTree = _t;
	}
	
	public final void conditional_statement(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException {
		
		AST conditional_statement_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conditional_statement_AST = null;
		
		try {      // for error handling
			AST __t50 = _t;
			AST tmp25_AST = null;
			AST tmp25_AST_in = null;
			tmp25_AST = astFactory.create((AST)_t);
			tmp25_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp25_AST);
			ASTPair __currentAST50 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,IF);
			_t = _t.getFirstChild();
			
					inConditional = true;
				
			boolean_expression(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			case ATTRIBUTE:
			case ACTOR_DECLARATION:
			{
				atomic_statement(_t,actor);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			{
				conditional_statement(_t,actor);
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
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			case ATTRIBUTE:
			case ACTOR_DECLARATION:
			{
				atomic_statement(_t,actor);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			{
				conditional_statement(_t,actor);
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
			currentAST = __currentAST50;
			_t = __t50;
			_t = _t.getNextSibling();
			
					inConditional = false;
				
			conditional_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = conditional_statement_AST;
		_retTree = _t;
	}
	
	public final void actor_definition(AST _t,
		PtalonActor actor
	) throws RecognitionException, IllegalActionException,NameDuplicationException {
		
		AST actor_definition_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST actor_definition_AST = null;
		AST a = null;
		AST a_AST = null;
		
		try {      // for error handling
			AST __t54 = _t;
			a = _t==ASTNULL ? null :(AST)_t;
			AST a_AST_in = null;
			a_AST = astFactory.create(a);
			astFactory.addASTChild(currentAST, a_AST);
			ASTPair __currentAST54 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ACTOR_DEFINITION);
			_t = _t.getFirstChild();
			{
			_loop56:
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
				case ATTRIBUTE:
				case ACTOR_DECLARATION:
				{
					atomic_statement(_t,actor);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case IF:
				{
					conditional_statement(_t,actor);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					break _loop56;
				}
				}
			} while (true);
			}
			currentAST = __currentAST54;
			_t = __t54;
			_t = _t.getNextSibling();
			
					try {
						actor.setName(a.getText());
					} catch (NameDuplicationException ex1) {
						actor.setName(actor.getContainer().uniqueName(a.getText()));
					}
				
			actor_definition_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = actor_definition_AST;
		_retTree = _t;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
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
		"SEMI",
		"\"if\"",
		"LCURLY",
		"RCURLY",
		"\"else\"",
		"\"is\"",
		"\"boolparameter\"",
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
	
