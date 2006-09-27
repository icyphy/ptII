// $ANTLR : "parser.g" -> "PtalonRecognizer.java"$
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

public class PtalonRecognizer extends antlr.LLkParser       implements PtalonTokenTypes
 {

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
		if ( inputState.guessing==0 ) {
			port_declaration_AST = (PtalonAST)currentAST.root;
			
					port_declaration_AST.addChild(d_AST);
				
		}
		returnAST = port_declaration_AST;
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
		PtalonAST e_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		Token  d = null;
		PtalonAST d_AST = null;
		PtalonAST q_AST = null;
		
			boolean addChild = true;
		
		
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
			case EQUALS:
			{
				PtalonAST tmp7_AST = null;
				tmp7_AST = (PtalonAST)astFactory.create(LT(1));
				match(EQUALS);
				expression();
				e_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					parameter_declaration_AST = (PtalonAST)currentAST.root;
					
							parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(3)).add((PtalonAST)astFactory.create(PARAM_EQUALS,"=")).add((PtalonAST)astFactory.make( (new ASTArray(2)).add(p_AST).add(c_AST))).add(e_AST));
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
							parameter_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add(p_AST).add(c_AST));
						}
					
				currentAST.root = parameter_declaration_AST;
				currentAST.child = parameter_declaration_AST!=null &&parameter_declaration_AST.getFirstChild()!=null ?
					parameter_declaration_AST.getFirstChild() : parameter_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case ACTOR:
		{
			a = LT(1);
			a_AST = (PtalonAST)astFactory.create(a);
			match(ACTOR);
			d = LT(1);
			d_AST = (PtalonAST)astFactory.create(d);
			match(ID);
			{
			switch ( LA(1)) {
			case EQUALS:
			{
				PtalonAST tmp8_AST = null;
				tmp8_AST = (PtalonAST)astFactory.create(LT(1));
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
 * Parse anything inside XML-like block
 * <p>&lt  /&gt
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
					out += full.substring(1, length - 2);
					expression_AST = (PtalonAST)astFactory.create(EXPRESSION,out);
				
			currentAST.root = expression_AST;
			currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
				expression_AST.getFirstChild() : expression_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = expression_AST;
	}
	
/**
 * Parse for statement
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
		
			String identifier = "";
		
		
		{
		if ((LA(1)==ID) && (LA(2)==COLON)) {
			p = LT(1);
			p_AST = (PtalonAST)astFactory.create(p);
			match(ID);
			PtalonAST tmp9_AST = null;
			tmp9_AST = (PtalonAST)astFactory.create(LT(1));
			match(COLON);
			if ( inputState.guessing==0 ) {
				
						identifier = identifier + p_AST.getText() + ":";
					
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
			
					identifier = identifier + a_AST.getText();
				
		}
		{
		_loop408:
		do {
			if ((LA(1)==DOT)) {
				PtalonAST tmp10_AST = null;
				tmp10_AST = (PtalonAST)astFactory.create(LT(1));
				match(DOT);
				keyword_or_identifier();
				b_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					
							identifier = identifier + "." +  b_AST.getText();
						
				}
			}
			else {
				break _loop408;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			qualified_identifier_AST = (PtalonAST)currentAST.root;
			
					qualified_identifier_AST = (PtalonAST)astFactory.create(QUALID,identifier);
				
			currentAST.root = qualified_identifier_AST;
			currentAST.child = qualified_identifier_AST!=null &&qualified_identifier_AST.getFirstChild()!=null ?
				qualified_identifier_AST.getFirstChild() : qualified_identifier_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = qualified_identifier_AST;
	}
	
/**
 * Parse for statement:
 * <p>relation <i>ID</i>
 * <p>Generate tree #(RELATION ID)
 */
	public final void relation_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relation_declaration_AST = null;
		
		PtalonAST tmp11_AST = null;
		tmp11_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp11_AST);
		match(RELATION);
		PtalonAST tmp12_AST = null;
		tmp12_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp12_AST);
		match(ID);
		relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = relation_declaration_AST;
	}
	
	public final void keyword_or_identifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST keyword_or_identifier_AST = null;
		
		switch ( LA(1)) {
		case ID:
		{
			PtalonAST tmp13_AST = null;
			tmp13_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp13_AST);
			match(ID);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case IMPORT:
		{
			PtalonAST tmp14_AST = null;
			tmp14_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp14_AST);
			match(IMPORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case PORT:
		{
			PtalonAST tmp15_AST = null;
			tmp15_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp15_AST);
			match(PORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case INPORT:
		{
			PtalonAST tmp16_AST = null;
			tmp16_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(INPORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case OUTPORT:
		{
			PtalonAST tmp17_AST = null;
			tmp17_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(OUTPORT);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case PARAMETER:
		{
			PtalonAST tmp18_AST = null;
			tmp18_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(PARAMETER);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ACTOR:
		{
			PtalonAST tmp19_AST = null;
			tmp19_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp19_AST);
			match(ACTOR);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case RELATION:
		{
			PtalonAST tmp20_AST = null;
			tmp20_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp20_AST);
			match(RELATION);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case TRUE:
		{
			PtalonAST tmp21_AST = null;
			tmp21_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp21_AST);
			match(TRUE);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case FALSE:
		{
			PtalonAST tmp22_AST = null;
			tmp22_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp22_AST);
			match(FALSE);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case IF:
		{
			PtalonAST tmp23_AST = null;
			tmp23_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp23_AST);
			match(IF);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case ELSE:
		{
			PtalonAST tmp24_AST = null;
			tmp24_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp24_AST);
			match(ELSE);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case IS:
		{
			PtalonAST tmp25_AST = null;
			tmp25_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp25_AST);
			match(IS);
			keyword_or_identifier_AST = (PtalonAST)currentAST.root;
			break;
		}
		case FOR:
		{
			PtalonAST tmp26_AST = null;
			tmp26_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp26_AST);
			match(FOR);
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
		
		PtalonAST tmp27_AST = null;
		tmp27_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp27_AST);
		match(ID);
		PtalonAST tmp28_AST = null;
		tmp28_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp28_AST);
		match(ASSIGN);
		{
		boolean synPredMatched414 = false;
		if (((LA(1)==ID) && (LA(2)==RPAREN||LA(2)==COMMA))) {
			int _m414 = mark();
			synPredMatched414 = true;
			inputState.guessing++;
			try {
				{
				match(ID);
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
				synPredMatched414 = false;
			}
			rewind(_m414);
inputState.guessing--;
		}
		if ( synPredMatched414 ) {
			PtalonAST tmp29_AST = null;
			tmp29_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp29_AST);
			match(ID);
		}
		else if ((LA(1)==ID||LA(1)==EXPRESSION) && (LA(2)==RPAREN||LA(2)==COMMA||LA(2)==LPAREN)) {
			{
			switch ( LA(1)) {
			case ID:
			{
				actor_declaration();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EXPRESSION:
			{
				expression();
				astFactory.addASTChild(currentAST, returnAST);
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
		assignment_AST = (PtalonAST)currentAST.root;
		returnAST = assignment_AST;
	}
	
/**
 * Parse statements of one of form:
 * <p><i>ID</i>(<i>assignment</i>, <i>assignment</i>, ...)
 * <p>Generate tree:
 * <p>#(ACTOR_DELCARATION <i>assignment</i> <i>assignment</i> ...)
 * <p>where the text for token ACTOR_DECLARATION is the leftmost
 * <i>ID</i> in the statement, or the name of the declared actor.
 */
	public final void actor_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_declaration_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST c_AST = null;
		
		a = LT(1);
		a_AST = (PtalonAST)astFactory.create(a);
		match(ID);
		if ( inputState.guessing==0 ) {
			actor_declaration_AST = (PtalonAST)currentAST.root;
			
					a_AST = (PtalonAST)astFactory.create(ACTOR_DECLARATION,a.getText());
					actor_declaration_AST = (PtalonAST)astFactory.make( (new ASTArray(1)).add(a_AST));
				
			currentAST.root = actor_declaration_AST;
			currentAST.child = actor_declaration_AST!=null &&actor_declaration_AST.getFirstChild()!=null ?
				actor_declaration_AST.getFirstChild() : actor_declaration_AST;
			currentAST.advanceChildToEnd();
		}
		match(LPAREN);
		{
		switch ( LA(1)) {
		case ID:
		{
			assignment();
			b_AST = (PtalonAST)returnAST;
			if ( inputState.guessing==0 ) {
				actor_declaration_AST = (PtalonAST)currentAST.root;
				
							actor_declaration_AST.addChild(b_AST);
						
			}
			{
			_loop420:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					assignment();
					c_AST = (PtalonAST)returnAST;
					if ( inputState.guessing==0 ) {
						actor_declaration_AST = (PtalonAST)currentAST.root;
						
									actor_declaration_AST.addChild(c_AST);
								
					}
				}
				else {
					break _loop420;
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
		match(RPAREN);
		returnAST = actor_declaration_AST;
	}
	
	public final void atomic_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST atomic_statement_AST = null;
		
		{
		switch ( LA(1)) {
		case PORT:
		case INPORT:
		case OUTPORT:
		{
			port_declaration();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case PARAMETER:
		case ACTOR:
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
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(SEMI);
		atomic_statement_AST = (PtalonAST)currentAST.root;
		returnAST = atomic_statement_AST;
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
		_loop425:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case ACTOR:
			case RELATION:
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
				break _loop425;
			}
			}
		} while (true);
		}
		match(RCURLY);
		match(ELSE);
		match(LCURLY);
		{
		_loop427:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case ACTOR:
			case RELATION:
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
					
							trueTree.addChild(i2_AST);
						
				}
				break;
			}
			default:
			{
				break _loop427;
			}
			}
		} while (true);
		}
		match(RCURLY);
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
		PtalonAST tmp39_AST = null;
		tmp39_AST = (PtalonAST)astFactory.create(LT(1));
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
		_loop430:
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
			case RELATION:
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
				break _loop430;
			}
			}
		} while (true);
		}
		PtalonAST tmp40_AST = null;
		tmp40_AST = (PtalonAST)astFactory.create(LT(1));
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
		Token  a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST c_AST = null;
		PtalonAST i_AST = null;
		
		if ( inputState.guessing==0 ) {
			actor_definition_AST = (PtalonAST)currentAST.root;
			
					actor_definition_AST = (PtalonAST)astFactory.create(ACTOR_DEFINITION);
				
			currentAST.root = actor_definition_AST;
			currentAST.child = actor_definition_AST!=null &&actor_definition_AST.getFirstChild()!=null ?
				actor_definition_AST.getFirstChild() : actor_definition_AST;
			currentAST.advanceChildToEnd();
		}
		a = LT(1);
		a_AST = (PtalonAST)astFactory.create(a);
		match(ID);
		if ( inputState.guessing==0 ) {
			actor_definition_AST = (PtalonAST)currentAST.root;
			
					actor_definition_AST.setText(a.getText());
				
		}
		match(IS);
		match(LCURLY);
		{
		_loop433:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case ACTOR:
			case RELATION:
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
				break _loop433;
			}
			}
		} while (true);
		}
		match(RCURLY);
		returnAST = actor_definition_AST;
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
		"\"if\"",
		"\"else\"",
		"\"is\"",
		"\"for\"",
		"ASSIGN",
		"RPAREN",
		"COMMA",
		"EXPRESSION",
		"LPAREN",
		"SEMI",
		"LCURLY",
		"RCURLY",
		"\"initially\"",
		"\"next\"",
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
		"ESC",
		"NUMBER_LITERAL",
		"ATTRIBUTE_MARKER",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 8337296L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	}
