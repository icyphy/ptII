// $ANTLR 2.7.6 (2005-12-22): "parser.g" -> "PtalonRecognizer.java"$
/* Lexer/Parser for Ptalon.

 Copyright (c) 2006-2009 The Regents of the University of California.
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

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

/** 
  PtalonRecognizer.java generated from populator.g by ANTLR.

  @author Adam Cataldo, Elaine Cheong, Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 7.0
  @Pt.ProposedRating Red (celaine)
  @Pt.AcceptedRating Red (celaine)
*/
@SuppressWarnings("unused")

public class PtalonRecognizer extends antlr.LLkParser       implements PtalonTokenTypes
 {

    private boolean _gtExtension = false;
    
    private boolean _isInTransformation = false;
    
    public void enableGTExtension(boolean enable) {
        _gtExtension = enable;
    }

protected PtalonRecognizer(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public PtalonRecognizer(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected PtalonRecognizer(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public PtalonRecognizer(TokenStream lexer) {
  this(lexer,2);
}

public PtalonRecognizer(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

/**
 * Parse for statement:
 * <p><i>portType</i> <i>ID</i>
 * <p>where portType is either "port", "inport", or "outport".
 * Generate corresponding tree #(PORT ID), #(INPORT ID), or #(OUTPORT ID).
 */
	public final void port_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST port_declaration_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		Token  b = null;
		PtalonAST b_AST = null;
		Token  c = null;
		PtalonAST c_AST = null;
		Token  d = null;
		PtalonAST d_AST = null;
		PtalonAST e_AST = null;
		
		boolean dynamic_name = false;
		
		
		{
		switch ( LA(1)) {
		case PORT:
		{
			a = LT(1);
			a_AST = (PtalonAST)astFactory.create(a);
			match(PORT);
			if ( inputState.guessing==0 ) {
				port_declaration_AST = (PtalonAST)currentAST.root;
				
				port_declaration_AST = a_AST;
				
				currentAST.root = port_declaration_AST;
				currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
					port_declaration_AST.getFirstChild() : port_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case LBRACKET:
			{
				PtalonAST tmp1_AST = null;
				tmp1_AST = (PtalonAST)astFactory.create(LT(1));
				match(LBRACKET);
				PtalonAST tmp2_AST = null;
				tmp2_AST = (PtalonAST)astFactory.create(LT(1));
				match(RBRACKET);
				if ( inputState.guessing==0 ) {
					port_declaration_AST = (PtalonAST)currentAST.root;
					
					port_declaration_AST = (PtalonAST)astFactory.create(MULTIPORT,"multiport");
					
					currentAST.root = port_declaration_AST;
					currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
						port_declaration_AST.getFirstChild() : port_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case INPORT:
		{
			b = LT(1);
			b_AST = (PtalonAST)astFactory.create(b);
			match(INPORT);
			if ( inputState.guessing==0 ) {
				port_declaration_AST = (PtalonAST)currentAST.root;
				
				port_declaration_AST = b_AST;
				
				currentAST.root = port_declaration_AST;
				currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
					port_declaration_AST.getFirstChild() : port_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case LBRACKET:
			{
				PtalonAST tmp3_AST = null;
				tmp3_AST = (PtalonAST)astFactory.create(LT(1));
				match(LBRACKET);
				PtalonAST tmp4_AST = null;
				tmp4_AST = (PtalonAST)astFactory.create(LT(1));
				match(RBRACKET);
				if ( inputState.guessing==0 ) {
					port_declaration_AST = (PtalonAST)currentAST.root;
					
					port_declaration_AST = (PtalonAST)astFactory.create(MULTIINPORT,"multiinport");
					
					currentAST.root = port_declaration_AST;
					currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
						port_declaration_AST.getFirstChild() : port_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case OUTPORT:
		{
			c = LT(1);
			c_AST = (PtalonAST)astFactory.create(c);
			match(OUTPORT);
			if ( inputState.guessing==0 ) {
				port_declaration_AST = (PtalonAST)currentAST.root;
				
				port_declaration_AST = c_AST;
				
				currentAST.root = port_declaration_AST;
				currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
					port_declaration_AST.getFirstChild() : port_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case LBRACKET:
			{
				PtalonAST tmp5_AST = null;
				tmp5_AST = (PtalonAST)astFactory.create(LT(1));
				match(LBRACKET);
				PtalonAST tmp6_AST = null;
				tmp6_AST = (PtalonAST)astFactory.create(LT(1));
				match(RBRACKET);
				if ( inputState.guessing==0 ) {
					port_declaration_AST = (PtalonAST)currentAST.root;
					
					port_declaration_AST = (PtalonAST)astFactory.create(MULTIOUTPORT,"multioutport");
					
					currentAST.root = port_declaration_AST;
					currentAST.child = port_declaration_AST!=null &&port_declaration_AST.getFirstChild()!=null ?
						port_declaration_AST.getFirstChild() : port_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		d = LT(1);
		d_AST = (PtalonAST)astFactory.create(d);
		match(ID);
		{
		switch ( LA(1)) {
		case EXPRESSION:
		{
			expression();
			e_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				
				dynamic_name = true;
				
			}
			break;
		}
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			port_declaration_AST = (PtalonAST)currentAST.root;
			
			if (dynamic_name) {
			port_declaration_AST.addChild((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(d_AST).add(e_AST)));
			} else {
			port_declaration_AST.addChild(d_AST);
			}
			
		}
		returnAST = port_declaration_AST;
	}
	
/**
 * Parse anything inside XML-like block
 * <p>&lt;  /&gt;
 * <p>Generate the tree
 * <p>#(EXPRESSION)
 * <p>where the text of the token EXPRESSION is the expression
 * inside the XML-like block.
 * 
 */
	public final void expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST expression_AST = null;
		Token  b = null;
		PtalonAST b_AST = null;
		
		String out = "";
		
		
		b = LT(1);
		b_AST = (PtalonAST)astFactory.create(b);
		match(EXPRESSION);
		if ( inputState.guessing==0 ) {
			expression_AST = (PtalonAST)currentAST.root;
			
			String full = b.getText();
			int length = full.length();
			out += full.substring(2, length - 2);
			expression_AST = (PtalonAST)astFactory.create(EXPRESSION,out);
			
			currentAST.root = expression_AST;
			currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
				expression_AST.getFirstChild() : expression_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = expression_AST;
	}
	
/**
 * Parse for one of:
 * <p>parameter <i>ID</i>
 * <p>actor <i>ID</i>
 * <p>where parameterType is either "parameter", "intparameter", or 
 * "outparameter".
 * Generate corresponding tree #(PARAMETER ID), #(INTPARAMETER ID), or 
 * #(BOOLPARAMETER ID).
 */
	public final void parameter_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST parameter_declaration_AST = null;
		Token  p = null;
		PtalonAST p_AST = null;
		Token  c = null;
		PtalonAST c_AST = null;
		PtalonAST n_AST = null;
		PtalonAST e_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		Token  d = null;
		PtalonAST d_AST = null;
		PtalonAST q_AST = null;
		
		boolean addChild = true;
		boolean dynamic_name = false;
		
		
		{
		switch ( LA(1)) {
		case PARAMETER:
		{
			p = LT(1);
			p_AST = (PtalonAST)astFactory.create(p);
			match(PARAMETER);
			c = LT(1);
			c_AST = (PtalonAST)astFactory.create(c);
			match(ID);
			{
			switch ( LA(1)) {
			case EXPRESSION:
			{
				expression();
				n_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					
					dynamic_name = true;
					
				}
				break;
			}
			case EQUALS:
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case EQUALS:
			{
				PtalonAST tmp7_AST = null;
				tmp7_AST = (PtalonAST)astFactory.create(LT(1));
				match(EQUALS);
				expression();
				e_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					parameter_declaration_AST = (PtalonAST)currentAST.root;
					
					if (dynamic_name) {
					parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(PARAM_EQUALS,"=")).add((PtalonAST)astFactory.make( (new ASTArray(2)).add(p_AST).add((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(c_AST).add(n_AST))))).add(e_AST));
					} else {
					parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(PARAM_EQUALS,"=")).add((PtalonAST)astFactory.make( (new ASTArray(2)).add(p_AST).add(c_AST))).add(e_AST));
					}
					addChild = false;
					
					currentAST.root = parameter_declaration_AST;
					currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
						parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				parameter_declaration_AST = (PtalonAST)currentAST.root;
				
				if (addChild) {
				if (dynamic_name) {
				parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add(p_AST).add((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(c_AST).add(n_AST))));
				} else {
				parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add(p_AST).add(c_AST));
				}
				}
				
				currentAST.root = parameter_declaration_AST;
				currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
					parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case ACTOR:
		case ACTORPARAM:
		{
			{
			switch ( LA(1)) {
			case ACTOR:
			{
				a = LT(1);
				a_AST = (PtalonAST)astFactory.create(a);
				match(ACTOR);
				break;
			}
			case ACTORPARAM:
			{
				PtalonAST tmp8_AST = null;
				tmp8_AST = (PtalonAST)astFactory.create(LT(1));
				match(ACTORPARAM);
				if ( inputState.guessing==0 ) {
					
					a_AST = (PtalonAST)astFactory.create(ACTOR,"actor");
					
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			d = LT(1);
			d_AST = (PtalonAST)astFactory.create(d);
			match(ID);
			{
			switch ( LA(1)) {
			case EQUALS:
			{
				PtalonAST tmp9_AST = null;
				tmp9_AST = (PtalonAST)astFactory.create(LT(1));
				match(EQUALS);
				qualified_identifier();
				q_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					parameter_declaration_AST = (PtalonAST)currentAST.root;
					
					parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(ACTOR_EQUALS,"=")).add((PtalonAST)astFactory.make( (new ASTArray(2)).add(a_AST).add(d_AST))).add(q_AST));
					addChild = false;
					
					currentAST.root = parameter_declaration_AST;
					currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
						parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				parameter_declaration_AST = (PtalonAST)currentAST.root;
				
				if (addChild) {
				parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add(a_AST).add(d_AST));
				}
				
				currentAST.root = parameter_declaration_AST;
				currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
					parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = parameter_declaration_AST;
	}
	
/**
 * Parse qualified identifier:
 * <p><i>ID</i>
 * <p>or
 * <p><i>ID</i>.qualified_identifier
 * <p>Generate tree #(QUALID)
 */
	public final void qualified_identifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST qualified_identifier_AST = null;
		Token  p = null;
		PtalonAST p_AST = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		
		StringBuffer identifier = new StringBuffer();
		
		
		{
		if ((LA(1)==ID) && (LA(2)==COLON)) {
			p = LT(1);
			p_AST = (PtalonAST)astFactory.create(p);
			match(ID);
			PtalonAST tmp10_AST = null;
			tmp10_AST = (PtalonAST)astFactory.create(LT(1));
			match(COLON);
			if ( inputState.guessing==0 ) {
				
				identifier.append(p_AST.getText() + ":");
				
			}
		}
		else if ((_tokenSet_0.member(LA(1))) && (LA(2)==DOT||LA(2)==SEMI)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		keyword_or_identifier();
		a_AST = (PtalonAST)returnAST;
		if ( inputState.guessing==0 ) {
			
			identifier.append(a_AST.getText());
			
		}
		{
		_loop21:
		do {
			if ((LA(1)==DOT)) {
				PtalonAST tmp11_AST = null;
				tmp11_AST = (PtalonAST)astFactory.create(LT(1));
				match(DOT);
				keyword_or_identifier();
				b_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					
					identifier.append("." +  b_AST.getText());
					
				}
			}
			else {
				break _loop21;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			qualified_identifier_AST = (PtalonAST)currentAST.root;
			
			qualified_identifier_AST = (PtalonAST)astFactory.create(QUALID,identifier.toString());
			
			currentAST.root = qualified_identifier_AST;
			currentAST.child = qualified_identifier_AST!=null &&qualified_identifier_AST.getFirstChild()!=null ?
				qualified_identifier_AST.getFirstChild() : qualified_identifier_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = qualified_identifier_AST;
	}
	
/**
 * Parse relation declaration:
 * <p>relation <i>ID</i>
 * <p>Generate tree #(RELATION ID)
 */
	public final void relation_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relation_declaration_AST = null;
		Token  r = null;
		PtalonAST r_AST = null;
		Token  i = null;
		PtalonAST i_AST = null;
		PtalonAST e_AST = null;
		
		r = LT(1);
		r_AST = (PtalonAST)astFactory.create(r);
		astFactory.makeASTRoot(currentAST, r_AST);
		match(RELATION);
		i = LT(1);
		i_AST = (PtalonAST)astFactory.create(i);
		astFactory.addASTChild(currentAST, i_AST);
		match(ID);
		{
		switch ( LA(1)) {
		case EXPRESSION:
		{
			expression();
			e_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				relation_declaration_AST = (PtalonAST)currentAST.root;
				
				relation_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add(r_AST).add((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(i_AST).add(e_AST))));
				
				currentAST.root = relation_declaration_AST;
				currentAST.child = relation_declaration_AST!=null &&relation_declaration_AST.getFirstChild()!=null ?
					relation_declaration_AST.getFirstChild() : relation_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = relation_declaration_AST;
	}
	
	public final void transparent_relation_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST transparent_relation_declaration_AST = null;
		Token  t = null;
		PtalonAST t_AST = null;
		Token  r = null;
		PtalonAST r_AST = null;
		Token  i = null;
		PtalonAST i_AST = null;
		PtalonAST e_AST = null;
		
		boolean created = false;
		
		
		{
		switch ( LA(1)) {
		case TRANSPARENT:
		{
			t = LT(1);
			t_AST = (PtalonAST)astFactory.create(t);
			match(TRANSPARENT);
			PtalonAST tmp12_AST = null;
			tmp12_AST = (PtalonAST)astFactory.create(LT(1));
			match(RELATION);
			break;
		}
		case PORT:
		{
			PtalonAST tmp13_AST = null;
			tmp13_AST = (PtalonAST)astFactory.create(LT(1));
			match(PORT);
			r = LT(1);
			r_AST = (PtalonAST)astFactory.create(r);
			match(REFERENCE);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		i = LT(1);
		i_AST = (PtalonAST)astFactory.create(i);
		match(ID);
		{
		switch ( LA(1)) {
		case EXPRESSION:
		{
			expression();
			e_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				transparent_relation_declaration_AST = (PtalonAST)currentAST.root;
				
				transparent_relation_declaration_AST = 
				(PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(TRANSPARENT,"transparent")).add((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(i_AST).add(e_AST))));
				created = true;
				
				currentAST.root = transparent_relation_declaration_AST;
				currentAST.child = transparent_relation_declaration_AST!=null &&transparent_relation_declaration_AST.getFirstChild()!=null ?
					transparent_relation_declaration_AST.getFirstChild() : transparent_relation_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			transparent_relation_declaration_AST = (PtalonAST)currentAST.root;
			
			if (!created) {
			transparent_relation_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(TRANSPARENT,"transparent")).add(i_AST));
			}
			
			currentAST.root = transparent_relation_declaration_AST;
			currentAST.child = transparent_relation_declaration_AST!=null &&transparent_relation_declaration_AST.getFirstChild()!=null ?
				transparent_relation_declaration_AST.getFirstChild() : transparent_relation_declaration_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = transparent_relation_declaration_AST;
	}
	
	public final void keyword_or_identifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST keyword_or_identifier_AST = null;
		
		switch ( LA(1)) {
		case ID:
		{
			PtalonAST tmp14_AST = null;
			tmp14_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp14_AST);
			match(ID);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case IMPORT:
		{
			PtalonAST tmp15_AST = null;
			tmp15_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp15_AST);
			match(IMPORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case PORT:
		{
			PtalonAST tmp16_AST = null;
			tmp16_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(PORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			PtalonAST tmp17_AST = null;
			tmp17_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(INPORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			PtalonAST tmp18_AST = null;
			tmp18_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(OUTPORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case PARAMETER:
		{
			PtalonAST tmp19_AST = null;
			tmp19_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp19_AST);
			match(PARAMETER);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ACTOR:
		{
			PtalonAST tmp20_AST = null;
			tmp20_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp20_AST);
			match(ACTOR);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case RELATION:
		{
			PtalonAST tmp21_AST = null;
			tmp21_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp21_AST);
			match(RELATION);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case TRUE:
		{
			PtalonAST tmp22_AST = null;
			tmp22_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp22_AST);
			match(TRUE);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case FALSE:
		{
			PtalonAST tmp23_AST = null;
			tmp23_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp23_AST);
			match(FALSE);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case IF:
		{
			PtalonAST tmp24_AST = null;
			tmp24_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp24_AST);
			match(IF);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ELSE:
		{
			PtalonAST tmp25_AST = null;
			tmp25_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp25_AST);
			match(ELSE);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case IS:
		{
			PtalonAST tmp26_AST = null;
			tmp26_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp26_AST);
			match(IS);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case FOR:
		{
			PtalonAST tmp27_AST = null;
			tmp27_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp27_AST);
			match(FOR);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INITIALLY:
		{
			PtalonAST tmp28_AST = null;
			tmp28_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp28_AST);
			match(INITIALLY);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case NEXT:
		{
			PtalonAST tmp29_AST = null;
			tmp29_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp29_AST);
			match(NEXT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case DANGLING_PORTS_OKAY:
		{
			PtalonAST tmp30_AST = null;
			tmp30_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp30_AST);
			match(DANGLING_PORTS_OKAY);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ATTACH_DANGLING_PORTS:
		{
			PtalonAST tmp31_AST = null;
			tmp31_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp31_AST);
			match(ATTACH_DANGLING_PORTS);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case TRANSPARENT:
		{
			PtalonAST tmp32_AST = null;
			tmp32_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp32_AST);
			match(TRANSPARENT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case REFERENCE:
		{
			PtalonAST tmp33_AST = null;
			tmp33_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp33_AST);
			match(REFERENCE);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = keyword_or_identifier_AST;
	}
	
/**
 * Parse statements of one of form:
 * <p><i>ID</i> := <i>ID</i>
 * <p><i>ID</i> := <i>actor_declaration</i>
 * <p><i>ID</i> := <i>arithmetic_expression</i>
 * <p><i>ID</i> := <i>boolean_expression</i>
 * <p>with preference given in that order.  Generate corresponding
 * tree:
 * <p>#(ASSIGN ID ID)
 * <p>#(ASSIGN ID <i>actor_declaration</i>)
 * <p>#(ASSIGN ID expression)
 */
	public final void assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		Token  l = null;
		PtalonAST l_AST = null;
		PtalonAST lExp_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		Token  r = null;
		PtalonAST r_AST = null;
		PtalonAST e_AST = null;
		PtalonAST d_AST = null;
		PtalonAST f_AST = null;
		
		boolean dynamic_name = false;
		boolean dynamic_left = false;
		
		
		l = LT(1);
		l_AST = (PtalonAST)astFactory.create(l);
		match(ID);
		{
		switch ( LA(1)) {
		case EXPRESSION:
		{
			expression();
			lExp_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				
				dynamic_left = true;
				
			}
			break;
		}
		case ASSIGN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		a = LT(1);
		a_AST = (PtalonAST)astFactory.create(a);
		match(ASSIGN);
		{
		boolean synPredMatched29 = false;
		if (((LA(1)==ID) && ((LA(2) >= RPAREN && LA(2) <= EXPRESSION)))) {
			int _m29 = mark();
			synPredMatched29 = true;
			inputState.guessing++;
			try {
				{
				match(ID);
				{
				switch ( LA(1)) {
				case EXPRESSION:
				{
					expression();
					break;
				}
				case RPAREN:
				case COMMA:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case RPAREN:
				{
					match(RPAREN);
					break;
				}
				case COMMA:
				{
					match(COMMA);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched29 = false;
			}
			rewind(_m29);
inputState.guessing--;
		}
		if ( synPredMatched29 ) {
			r = LT(1);
			r_AST = (PtalonAST)astFactory.create(r);
			match(ID);
			{
			switch ( LA(1)) {
			case EXPRESSION:
			{
				expression();
				e_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					
					dynamic_name = true;
					
				}
				break;
			}
			case RPAREN:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				assignment_AST = (PtalonAST)currentAST.root;
				
				PtalonAST left;
				if (dynamic_left) {
				left = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(l_AST).add(lExp_AST));
				} else {
				left = l_AST;
				}
				if (dynamic_name) {
				assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add(a_AST).add(left).add((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(r_AST).add(e_AST))));
				} else {
				assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add(a_AST).add(left).add(r_AST));
				}
				
				currentAST.root = assignment_AST;
				currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
					assignment_AST.getFirstChild() : assignment_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else if ((LA(1)==ID||LA(1)==EXPRESSION) && ((LA(2) >= ASSIGN && LA(2) <= LPAREN))) {
			{
			switch ( LA(1)) {
			case ID:
			{
				actor_declaration();
				d_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					assignment_AST = (PtalonAST)currentAST.root;
					
					assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add(a_AST).add(l_AST).add(d_AST));
					
					currentAST.root = assignment_AST;
					currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
						assignment_AST.getFirstChild() : assignment_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case EXPRESSION:
			{
				expression();
				f_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					assignment_AST = (PtalonAST)currentAST.root;
					
					assignment_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add(a_AST).add(l_AST).add(f_AST));
					
					currentAST.root = assignment_AST;
					currentAST.child = assignment_AST!=null &&assignment_AST.getFirstChild()!=null ?
						assignment_AST.getFirstChild() : assignment_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		returnAST = assignment_AST;
	}
	
/**
 * Parse statements of one of form:
 * <p><i>ID</i>(<i>assignment</i>, <i>assignment</i>, ...)
 * <p>Generate tree:
 * <p>#(ACTOR_DECLARATION <i>assignment</i> <i>assignment</i> ...)
 * <p>where the text for token ACTOR_DECLARATION is the leftmost
 * <i>ID</i> in the statement, or the name of the declared actor.
 */
	public final void actor_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_declaration_AST = null;
		PtalonAST a_AST = null;
		Token  b = null;
		PtalonAST b_AST = null;
		PtalonAST c_AST = null;
		PtalonAST d_AST = null;
		
		{
		if ((LA(1)==ID) && (LA(2)==ASSIGN||LA(2)==EXPRESSION)) {
			actor_id();
			a_AST = (PtalonAST)returnAST;
			PtalonAST tmp34_AST = null;
			tmp34_AST = (PtalonAST)astFactory.create(LT(1));
			match(ASSIGN);
		}
		else if ((LA(1)==ID) && (LA(2)==LPAREN)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		b = LT(1);
		b_AST = (PtalonAST)astFactory.create(b);
		match(ID);
		PtalonAST tmp35_AST = null;
		tmp35_AST = (PtalonAST)astFactory.create(LT(1));
		match(LPAREN);
		if ( inputState.guessing==0 ) {
			actor_declaration_AST = (PtalonAST)currentAST.root;
			
			b_AST = (PtalonAST)astFactory.create(ACTOR_DECLARATION,b.getText());
			actor_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(1)).add(b_AST));
			if (a_AST != null) {
				actor_declaration_AST.addChild(a_AST);
			}
			
			currentAST.root = actor_declaration_AST;
			currentAST.child = actor_declaration_AST!=null &&actor_declaration_AST.getFirstChild()!=null ?
				actor_declaration_AST.getFirstChild() : actor_declaration_AST;
			currentAST.advanceChildToEnd();
		}
		{
		switch ( LA(1)) {
		case ID:
		{
			assignment();
			c_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				actor_declaration_AST = (PtalonAST)currentAST.root;
				
				actor_declaration_AST.addChild(c_AST);
				
			}
			{
			_loop37:
			do {
				if ((LA(1)==COMMA)) {
					PtalonAST tmp36_AST = null;
					tmp36_AST = (PtalonAST)astFactory.create(LT(1));
					match(COMMA);
					assignment();
					d_AST = (PtalonAST)returnAST;
					if ( inputState.guessing==0 ) {
						actor_declaration_AST = (PtalonAST)currentAST.root;
						
						actor_declaration_AST.addChild(d_AST);
						
					}
				}
				else {
					break _loop37;
				}
				
			} while (true);
			}
			break;
		}
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		PtalonAST tmp37_AST = null;
		tmp37_AST = (PtalonAST)astFactory.create(LT(1));
		match(RPAREN);
		returnAST = actor_declaration_AST;
	}
	
	public final void actor_id() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_id_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		
		a = LT(1);
		a_AST = (PtalonAST)astFactory.create(a);
		match(ID);
		if ( inputState.guessing==0 ) {
			actor_id_AST = (PtalonAST)currentAST.root;
			
					actor_id_AST = (PtalonAST)astFactory.create(ACTOR_ID,a.getText());
				
			currentAST.root = actor_id_AST;
			currentAST.child = actor_id_AST!=null &&actor_id_AST.getFirstChild()!=null ?
				actor_id_AST.getFirstChild() : actor_id_AST;
			currentAST.advanceChildToEnd();
		}
		{
		switch ( LA(1)) {
		case EXPRESSION:
		{
			expression();
			b_AST = (PtalonAST)returnAST;
			break;
		}
		case ASSIGN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			actor_id_AST = (PtalonAST)currentAST.root;
			
					actor_id_AST.addChild(b_AST);
				
		}
		returnAST = actor_id_AST;
	}
	
	public final void atomic_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST atomic_statement_AST = null;
		
		{
		switch ( LA(1)) {
		case PARAMETER:
		case ACTOR:
		case ACTORPARAM:
		{
			parameter_declaration();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RELATION:
		{
			relation_declaration();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case ID:
		{
			actor_declaration();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case NEGATE:
		case OPTIONAL:
		case REMOVE:
		case PRESERVE:
		{
			transformation_declaration();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
			if ((LA(1)==PORT||LA(1)==INPORT||LA(1)==OUTPORT) && (LA(2)==LBRACKET||LA(2)==ID)) {
				port_declaration();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((LA(1)==PORT||LA(1)==TRANSPARENT) && (LA(2)==RELATION||LA(2)==REFERENCE)) {
				transparent_relation_declaration();
				astFactory.addASTChild(currentAST, returnAST);
			}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(SEMI);
		atomic_statement_AST = (PtalonAST)currentAST.root;
		returnAST = atomic_statement_AST;
	}
	
	public final void transformation_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST transformation_declaration_AST = null;
		Token  n = null;
		PtalonAST n_AST = null;
		Token  o = null;
		PtalonAST o_AST = null;
		Token  r = null;
		PtalonAST r_AST = null;
		Token  p = null;
		PtalonAST p_AST = null;
		Token  d = null;
		PtalonAST d_AST = null;
		PtalonAST e_AST = null;
		
		boolean dynamic_name = false;
		
		
		{
		switch ( LA(1)) {
		case NEGATE:
		case OPTIONAL:
		{
			{
			if (!(!_isInTransformation))
			  throw new SemanticException("!_isInTransformation");
			{
			switch ( LA(1)) {
			case NEGATE:
			{
				n = LT(1);
				n_AST = (PtalonAST)astFactory.create(n);
				match(NEGATE);
				if ( inputState.guessing==0 ) {
					transformation_declaration_AST = (PtalonAST)currentAST.root;
					transformation_declaration_AST = n_AST;
					currentAST.root = transformation_declaration_AST;
					currentAST.child = transformation_declaration_AST!=null &&transformation_declaration_AST.getFirstChild()!=null ?
						transformation_declaration_AST.getFirstChild() : transformation_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case OPTIONAL:
			{
				o = LT(1);
				o_AST = (PtalonAST)astFactory.create(o);
				match(OPTIONAL);
				if ( inputState.guessing==0 ) {
					transformation_declaration_AST = (PtalonAST)currentAST.root;
					transformation_declaration_AST = o_AST;
					currentAST.root = transformation_declaration_AST;
					currentAST.child = transformation_declaration_AST!=null &&transformation_declaration_AST.getFirstChild()!=null ?
						transformation_declaration_AST.getFirstChild() : transformation_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
			break;
		}
		case REMOVE:
		case PRESERVE:
		{
			{
			if (!(_isInTransformation))
			  throw new SemanticException("_isInTransformation");
			{
			switch ( LA(1)) {
			case REMOVE:
			{
				r = LT(1);
				r_AST = (PtalonAST)astFactory.create(r);
				match(REMOVE);
				if ( inputState.guessing==0 ) {
					transformation_declaration_AST = (PtalonAST)currentAST.root;
					transformation_declaration_AST = r_AST;
					currentAST.root = transformation_declaration_AST;
					currentAST.child = transformation_declaration_AST!=null &&transformation_declaration_AST.getFirstChild()!=null ?
						transformation_declaration_AST.getFirstChild() : transformation_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case PRESERVE:
			{
				p = LT(1);
				p_AST = (PtalonAST)astFactory.create(p);
				match(PRESERVE);
				if ( inputState.guessing==0 ) {
					transformation_declaration_AST = (PtalonAST)currentAST.root;
					transformation_declaration_AST = p_AST;
					currentAST.root = transformation_declaration_AST;
					currentAST.child = transformation_declaration_AST!=null &&transformation_declaration_AST.getFirstChild()!=null ?
						transformation_declaration_AST.getFirstChild() : transformation_declaration_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		d = LT(1);
		d_AST = (PtalonAST)astFactory.create(d);
		match(ID);
		{
		switch ( LA(1)) {
		case EXPRESSION:
		{
			expression();
			e_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				
				dynamic_name = true;
				
			}
			break;
		}
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			transformation_declaration_AST = (PtalonAST)currentAST.root;
			
			if (dynamic_name) {
			transformation_declaration_AST.addChild((PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(DYNAMIC_NAME,"dynamic")).add(d_AST).add(e_AST)));
			} else {
			transformation_declaration_AST.addChild(d_AST);
			}
			
		}
		returnAST = transformation_declaration_AST;
	}
	
	public final void conditional_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST conditional_statement_AST = null;
		Token  i = null;
		PtalonAST i_AST = null;
		PtalonAST b_AST = null;
		PtalonAST a1_AST = null;
		PtalonAST c1_AST = null;
		PtalonAST i1_AST = null;
		PtalonAST a2_AST = null;
		PtalonAST c2_AST = null;
		PtalonAST i2_AST = null;
		
		AST trueTree = null;
		AST falseTree = null;
		
		
		i = LT(1);
		i_AST = (PtalonAST)astFactory.create(i);
		match(IF);
		expression();
		b_AST = (PtalonAST)returnAST;
		if ( inputState.guessing==0 ) {
			conditional_statement_AST = (PtalonAST)currentAST.root;
			
			conditional_statement_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add(i_AST).add(b_AST));
			trueTree = (PtalonAST)astFactory.create(TRUEBRANCH,"true branch");
			falseTree = (PtalonAST)astFactory.create(FALSEBRANCH,"false branch");
			
			currentAST.root = conditional_statement_AST;
			currentAST.child = conditional_statement_AST!=null &&conditional_statement_AST.getFirstChild()!=null ?
				conditional_statement_AST.getFirstChild() : conditional_statement_AST;
			currentAST.advanceChildToEnd();
		}
		match(LCURLY);
		{
		_loop51:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case ACTOR:
			case ACTORPARAM:
			case RELATION:
			case TRANSPARENT:
			case NEGATE:
			case OPTIONAL:
			case REMOVE:
			case PRESERVE:
			{
				atomic_statement();
				a1_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					
					trueTree.addChild(a1_AST);
					
				}
				break;
			}
			case IF:
			{
				conditional_statement();
				c1_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					
					trueTree.addChild(c1_AST);
					
				}
				break;
			}
			case FOR:
			{
				iterative_statement();
				i1_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					
					trueTree.addChild(i1_AST);
					
				}
				break;
			}
			default:
			{
				break _loop51;
			}
			}
		} while (true);
		}
		match(RCURLY);
		{
		switch ( LA(1)) {
		case ELSE:
		{
			match(ELSE);
			match(LCURLY);
			{
			_loop54:
			do {
				switch ( LA(1)) {
				case PORT:
				case INPORT:
				case OUTPORT:
				case ID:
				case PARAMETER:
				case ACTOR:
				case ACTORPARAM:
				case RELATION:
				case TRANSPARENT:
				case NEGATE:
				case OPTIONAL:
				case REMOVE:
				case PRESERVE:
				{
					atomic_statement();
					a2_AST = (PtalonAST)returnAST;
					if ( inputState.guessing==0 ) {
						
						falseTree.addChild(a2_AST);
						
					}
					break;
				}
				case IF:
				{
					conditional_statement();
					c2_AST = (PtalonAST)returnAST;
					if ( inputState.guessing==0 ) {
						
						falseTree.addChild(c2_AST);
						
					}
					break;
				}
				case FOR:
				{
					iterative_statement();
					i2_AST = (PtalonAST)returnAST;
					if ( inputState.guessing==0 ) {
						
						falseTree.addChild(i2_AST);
						
					}
					break;
				}
				default:
				{
					break _loop54;
				}
				}
			} while (true);
			}
			match(RCURLY);
			break;
		}
		case PORT:
		case INPORT:
		case OUTPORT:
		case ID:
		case PARAMETER:
		case ACTOR:
		case ACTORPARAM:
		case RELATION:
		case TRANSPARENT:
		case IF:
		case FOR:
		case NEGATE:
		case OPTIONAL:
		case REMOVE:
		case PRESERVE:
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			conditional_statement_AST = (PtalonAST)currentAST.root;
			
			conditional_statement_AST.addChild(trueTree);
			conditional_statement_AST.addChild(falseTree);
			
		}
		returnAST = conditional_statement_AST;
	}
	
	public final void iterative_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST iterative_statement_AST = null;
		Token  f = null;
		PtalonAST f_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		Token  b = null;
		PtalonAST b_AST = null;
		PtalonAST init_AST = null;
		PtalonAST sat_AST = null;
		PtalonAST it_AST = null;
		PtalonAST cond_AST = null;
		PtalonAST at_AST = null;
		Token  c = null;
		PtalonAST c_AST = null;
		PtalonAST next_AST = null;
		
		f = LT(1);
		f_AST = (PtalonAST)astFactory.create(f);
		match(FOR);
		a = LT(1);
		a_AST = (PtalonAST)astFactory.create(a);
		match(ID);
		b = LT(1);
		b_AST = (PtalonAST)astFactory.create(b);
		match(INITIALLY);
		expression();
		init_AST = (PtalonAST)returnAST;
		expression();
		sat_AST = (PtalonAST)returnAST;
		PtalonAST tmp44_AST = null;
		tmp44_AST = (PtalonAST)astFactory.create(LT(1));
		match(LCURLY);
		if ( inputState.guessing==0 ) {
			iterative_statement_AST = (PtalonAST)currentAST.root;
			
			iterative_statement_AST = (PtalonAST)astFactory.make( (new ASTArray(4)).add(f_AST).add((PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(VARIABLE,"variable")).add(a_AST))).add((PtalonAST)astFactory.make( (new ASTArray(2)).add(b_AST).add(init_AST))).add((PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(SATISFIES,"satisfies")).add(sat_AST))));
			
			currentAST.root = iterative_statement_AST;
			currentAST.child = iterative_statement_AST!=null &&iterative_statement_AST.getFirstChild()!=null ?
				iterative_statement_AST.getFirstChild() : iterative_statement_AST;
			currentAST.advanceChildToEnd();
		}
		{
		_loop57:
		do {
			switch ( LA(1)) {
			case FOR:
			{
				iterative_statement();
				it_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					iterative_statement_AST = (PtalonAST)currentAST.root;
					
					iterative_statement_AST.addChild(it_AST);
					
				}
				break;
			}
			case IF:
			{
				conditional_statement();
				cond_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					iterative_statement_AST = (PtalonAST)currentAST.root;
					
					iterative_statement_AST.addChild(cond_AST);
					
				}
				break;
			}
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case ACTOR:
			case ACTORPARAM:
			case RELATION:
			case TRANSPARENT:
			case NEGATE:
			case OPTIONAL:
			case REMOVE:
			case PRESERVE:
			{
				atomic_statement();
				at_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					iterative_statement_AST = (PtalonAST)currentAST.root;
					
					iterative_statement_AST.addChild(at_AST);
					
				}
				break;
			}
			default:
			{
				break _loop57;
			}
			}
		} while (true);
		}
		PtalonAST tmp45_AST = null;
		tmp45_AST = (PtalonAST)astFactory.create(LT(1));
		match(RCURLY);
		c = LT(1);
		c_AST = (PtalonAST)astFactory.create(c);
		match(NEXT);
		expression();
		next_AST = (PtalonAST)returnAST;
		if ( inputState.guessing==0 ) {
			iterative_statement_AST = (PtalonAST)currentAST.root;
			
			iterative_statement_AST.addChild((PtalonAST)astFactory.make( (new ASTArray(2)).add(c_AST).add(next_AST)));
			
		}
		returnAST = iterative_statement_AST;
	}
	
	public final void actor_definition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_definition_AST = null;
		PtalonAST d_AST = null;
		PtalonAST at_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST c_AST = null;
		PtalonAST i_AST = null;
		PtalonAST t_AST = null;
		
		boolean danglingPortsOkay = false;
		boolean attachDanglingPorts = false;
		
		
		if ( inputState.guessing==0 ) {
			actor_definition_AST = (PtalonAST)currentAST.root;
			
			actor_definition_AST = (PtalonAST)astFactory.create(ACTOR_DEFINITION);
			_isInTransformation = false;
			
			currentAST.root = actor_definition_AST;
			currentAST.child = actor_definition_AST!=null &&actor_definition_AST.getFirstChild()!=null ?
				actor_definition_AST.getFirstChild() : actor_definition_AST;
			currentAST.advanceChildToEnd();
		}
		{
		switch ( LA(1)) {
		case DANGLING_PORTS_OKAY:
		{
			danglingPortsOkay();
			d_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				
				danglingPortsOkay = true;
				
			}
			break;
		}
		case ID:
		case ATTACH_DANGLING_PORTS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case ATTACH_DANGLING_PORTS:
		{
			attachDanglingPorts();
			at_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				
				attachDanglingPorts = true;
				
			}
			break;
		}
		case ID:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		a = LT(1);
		a_AST = (PtalonAST)astFactory.create(a);
		match(ID);
		if ( inputState.guessing==0 ) {
			actor_definition_AST = (PtalonAST)currentAST.root;
			
			actor_definition_AST.setText(a.getText());
			if (danglingPortsOkay) {
			actor_definition_AST.addChild(d_AST);
			}
			if (attachDanglingPorts) {
			actor_definition_AST.addChild(at_AST);
			}
			
		}
		match(IS);
		match(LCURLY);
		{
		_loop62:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case ACTOR:
			case ACTORPARAM:
			case RELATION:
			case TRANSPARENT:
			case NEGATE:
			case OPTIONAL:
			case REMOVE:
			case PRESERVE:
			{
				atomic_statement();
				b_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					actor_definition_AST = (PtalonAST)currentAST.root;
					
					actor_definition_AST.addChild(b_AST);
					
				}
				break;
			}
			case IF:
			{
				conditional_statement();
				c_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					actor_definition_AST = (PtalonAST)currentAST.root;
					
					actor_definition_AST.addChild(c_AST);
					
				}
				break;
			}
			case FOR:
			{
				iterative_statement();
				i_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					actor_definition_AST = (PtalonAST)currentAST.root;
					
					actor_definition_AST.addChild(i_AST);
					
				}
				break;
			}
			default:
			{
				break _loop62;
			}
			}
		} while (true);
		}
		match(RCURLY);
		{
		if ((LA(1)==EOF)) {
		}
		else if (((LA(1)==TRANSFORM))&&(_gtExtension)) {
			transformation();
			t_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				actor_definition_AST = (PtalonAST)currentAST.root;
				
				actor_definition_AST.addChild(t_AST);
				
			}
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		returnAST = actor_definition_AST;
	}
	
	public final void danglingPortsOkay() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST danglingPortsOkay_AST = null;
		
		PtalonAST tmp49_AST = null;
		tmp49_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp49_AST);
		match(DANGLING_PORTS_OKAY);
		match(SEMI);
		danglingPortsOkay_AST = (PtalonAST)currentAST.root;
		returnAST = danglingPortsOkay_AST;
	}
	
	public final void attachDanglingPorts() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST attachDanglingPorts_AST = null;
		
		PtalonAST tmp51_AST = null;
		tmp51_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp51_AST);
		match(ATTACH_DANGLING_PORTS);
		match(SEMI);
		attachDanglingPorts_AST = (PtalonAST)currentAST.root;
		returnAST = attachDanglingPorts_AST;
	}
	
	public final void transformation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST transformation_AST = null;
		Token  s = null;
		PtalonAST s_AST = null;
		PtalonAST b_AST = null;
		PtalonAST c_AST = null;
		PtalonAST i_AST = null;
		
		if ( inputState.guessing==0 ) {
			transformation_AST = (PtalonAST)currentAST.root;
			
			transformation_AST = (PtalonAST)astFactory.create(TRANSFORMATION);
				_isInTransformation = true;
			
			currentAST.root = transformation_AST;
			currentAST.child = transformation_AST!=null &&transformation_AST.getFirstChild()!=null ?
				transformation_AST.getFirstChild() : transformation_AST;
			currentAST.advanceChildToEnd();
		}
		PtalonAST tmp53_AST = null;
		tmp53_AST = (PtalonAST)astFactory.create(LT(1));
		match(TRANSFORM);
		{
		switch ( LA(1)) {
		case PLUS:
		{
			s = LT(1);
			s_AST = (PtalonAST)astFactory.create(s);
			match(PLUS);
			if ( inputState.guessing==0 ) {
				transformation_AST = (PtalonAST)currentAST.root;
				
				transformation_AST.addChild(s_AST);
				
			}
			break;
		}
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(LCURLY);
		{
		_loop67:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case ACTOR:
			case ACTORPARAM:
			case RELATION:
			case TRANSPARENT:
			case NEGATE:
			case OPTIONAL:
			case REMOVE:
			case PRESERVE:
			{
				atomic_statement();
				b_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					transformation_AST = (PtalonAST)currentAST.root;
					
					transformation_AST.addChild(b_AST);
					
				}
				break;
			}
			case IF:
			{
				conditional_statement();
				c_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					transformation_AST = (PtalonAST)currentAST.root;
					
					transformation_AST.addChild(c_AST);
					
				}
				break;
			}
			case FOR:
			{
				iterative_statement();
				i_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					transformation_AST = (PtalonAST)currentAST.root;
					
					transformation_AST.addChild(i_AST);
					
				}
				break;
			}
			default:
			{
				break _loop67;
			}
			}
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			
				_isInTransformation = false;
			
		}
		match(RCURLY);
		returnAST = transformation_AST;
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
		"\"actorparameter\"",
		"\"relation\"",
		"\"transparent\"",
		"\"reference\"",
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
		"\"attachDanglingPorts\"",
		"ASSIGN",
		"RPAREN",
		"COMMA",
		"EXPRESSION",
		"LPAREN",
		"SEMI",
		"\"negate\"",
		"\"optional\"",
		"\"remove\"",
		"\"preserve\"",
		"LCURLY",
		"RCURLY",
		"TRANSFORM",
		"PLUS",
		"TRUEBRANCH",
		"FALSEBRANCH",
		"QUALID",
		"ATTRIBUTE",
		"ACTOR_DECLARATION",
		"ACTOR_DEFINITION",
		"TRANSFORMATION",
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
		"ACTOR_ID",
		"ESC",
		"NUMBER_LITERAL",
		"STRING_LITERAL",
		"WHITE_SPACE",
		"LINE_COMMENT",
		"COMMENT"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 1073338256L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	}
