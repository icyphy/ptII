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
 * <p>import <i>qualified_identifier</i>;
 * <p>Generate tree #(IMPORT <i>qualified_identifier</i>).
 */
	public final void import_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST import_declaration_AST = null;
		
		{
		PtalonAST tmp1_AST = null;
		tmp1_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp1_AST);
		match(IMPORT);
		qualified_identifier();
		astFactory.addASTChild(currentAST, returnAST);
		match(SEMI);
		}
		import_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = import_declaration_AST;
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
		Token  a = null;
		PtalonAST a_AST = null;
		Token  b = null;
		PtalonAST b_AST = null;
		
			String identifier = "";
		
		
		a = LT(1);
		a_AST = (PtalonAST)astFactory.create(a);
		match(ID);
		if ( inputState.guessing==0 ) {
			
					identifier = identifier + a.getText();
				
		}
		{
		_loop10:
		do {
			if ((LA(1)==DOT)) {
				PtalonAST tmp3_AST = null;
				tmp3_AST = (PtalonAST)astFactory.create(LT(1));
				match(DOT);
				b = LT(1);
				b_AST = (PtalonAST)astFactory.create(b);
				match(ID);
				if ( inputState.guessing==0 ) {
					
							identifier = identifier + "." +  b.getText();
						
				}
			}
			else {
				break _loop10;
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
 * <p><i>portType</i> <i>ID</i>
 * <p>where portType is either "port", "inport", or "outport".
 * Generate corresponding tree #(PORT ID), #(INPORT ID), or #(OUTPORT ID).
 */
	public final void port_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST port_declaration_AST = null;
		
		{
		switch ( LA(1)) {
		case PORT:
		{
			PtalonAST tmp4_AST = null;
			tmp4_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp4_AST);
			match(PORT);
			break;
		}
		case INPORT:
		{
			PtalonAST tmp5_AST = null;
			tmp5_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp5_AST);
			match(INPORT);
			break;
		}
		case OUTPORT:
		{
			PtalonAST tmp6_AST = null;
			tmp6_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp6_AST);
			match(OUTPORT);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		PtalonAST tmp7_AST = null;
		tmp7_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp7_AST);
		match(ID);
		port_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = port_declaration_AST;
	}
	
/**
 * Parse for statement:
 * <p><i>parameterType</i> <i>ID</i>
 * <p>where parameterType is either "parameter", "intparameter", or 
 * "outparameter".
 * Generate corresponding tree #(PARAMETER ID), #(INTPARAMETER ID), or 
 * #(BOOLPARAMETER ID).
 */
	public final void parameter_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST parameter_declaration_AST = null;
		
		{
		switch ( LA(1)) {
		case PARAMETER:
		{
			PtalonAST tmp8_AST = null;
			tmp8_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp8_AST);
			match(PARAMETER);
			break;
		}
		case INTPARAMETER:
		{
			PtalonAST tmp9_AST = null;
			tmp9_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp9_AST);
			match(INTPARAMETER);
			break;
		}
		case BOOLPARAMETER:
		{
			PtalonAST tmp10_AST = null;
			tmp10_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp10_AST);
			match(BOOLPARAMETER);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		PtalonAST tmp11_AST = null;
		tmp11_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp11_AST);
		match(ID);
		parameter_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = parameter_declaration_AST;
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
		
		PtalonAST tmp12_AST = null;
		tmp12_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp12_AST);
		match(RELATION);
		PtalonAST tmp13_AST = null;
		tmp13_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp13_AST);
		match(ID);
		relation_declaration_AST = (PtalonAST)currentAST.root;
		returnAST = relation_declaration_AST;
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
 * <p>#(ASSIGN ID <i>arithmetic_expression</i>)
 * <p>#(ASSIGN ID <i>boolean_expression</i>)
 */
	public final void assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		
		PtalonAST tmp14_AST = null;
		tmp14_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp14_AST);
		match(ID);
		PtalonAST tmp15_AST = null;
		tmp15_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp15_AST);
		match(ASSIGN);
		{
		boolean synPredMatched15 = false;
		if (((LA(1)==ID) && (LA(2)==RPAREN||LA(2)==COMMA))) {
			int _m15 = mark();
			synPredMatched15 = true;
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
				synPredMatched15 = false;
			}
			rewind(_m15);
inputState.guessing--;
		}
		if ( synPredMatched15 ) {
			PtalonAST tmp16_AST = null;
			tmp16_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(ID);
		}
		else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2)))) {
			{
			boolean synPredMatched18 = false;
			if (((LA(1)==ID) && (LA(2)==LPAREN))) {
				int _m18 = mark();
				synPredMatched18 = true;
				inputState.guessing++;
				try {
					{
					match(ID);
					match(LPAREN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched18 = false;
				}
				rewind(_m18);
inputState.guessing--;
			}
			if ( synPredMatched18 ) {
				actor_declaration();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2)))) {
				{
				boolean synPredMatched21 = false;
				if (((_tokenSet_2.member(LA(1))) && (_tokenSet_3.member(LA(2))))) {
					int _m21 = mark();
					synPredMatched21 = true;
					inputState.guessing++;
					try {
						{
						arithmetic_expression();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched21 = false;
					}
					rewind(_m21);
inputState.guessing--;
				}
				if ( synPredMatched21 ) {
					arithmetic_expression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2)))) {
					boolean_expression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
			_loop25:
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
					break _loop25;
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
	
	public final void arithmetic_expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_expression_AST = null;
		
		arithmetic_term();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop37:
		do {
			if ((LA(1)==MINUS||LA(1)==PLUS)) {
				{
				switch ( LA(1)) {
				case PLUS:
				{
					PtalonAST tmp20_AST = null;
					tmp20_AST = (PtalonAST)astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp20_AST);
					match(PLUS);
					break;
				}
				case MINUS:
				{
					PtalonAST tmp21_AST = null;
					tmp21_AST = (PtalonAST)astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp21_AST);
					match(MINUS);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				arithmetic_term();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop37;
			}
			
		} while (true);
		}
		arithmetic_expression_AST = (PtalonAST)currentAST.root;
		returnAST = arithmetic_expression_AST;
	}
	
	public final void boolean_expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_expression_AST = null;
		
		boolean_term();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop52:
		do {
			if ((LA(1)==LOGICAL_OR)) {
				PtalonAST tmp22_AST = null;
				tmp22_AST = (PtalonAST)astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp22_AST);
				match(LOGICAL_OR);
				boolean_term();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop52;
			}
			
		} while (true);
		}
		boolean_expression_AST = (PtalonAST)currentAST.root;
		returnAST = boolean_expression_AST;
	}
	
	public final void arithmetic_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_factor_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		Token  b = null;
		PtalonAST b_AST = null;
		PtalonAST c_AST = null;
		
			int sign = 1;
		
		
		{
		_loop28:
		do {
			if ((LA(1)==MINUS)) {
				PtalonAST tmp23_AST = null;
				tmp23_AST = (PtalonAST)astFactory.create(LT(1));
				match(MINUS);
				if ( inputState.guessing==0 ) {
					
							sign = -sign;
						
				}
			}
			else {
				break _loop28;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			arithmetic_factor_AST = (PtalonAST)currentAST.root;
			
					if (sign == 1) {
						arithmetic_factor_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(ARITHMETIC_FACTOR,"arithmetic_factor")).add((PtalonAST)astFactory.create(POSITIVE_SIGN,"positive")));
					} else {
						arithmetic_factor_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(ARITHMETIC_FACTOR,"arithmetic_factor")).add((PtalonAST)astFactory.create(NEGATIVE_SIGN,"negative")));
					}
				
			currentAST.root = arithmetic_factor_AST;
			currentAST.child = arithmetic_factor_AST!=null &&arithmetic_factor_AST.getFirstChild()!=null ?
				arithmetic_factor_AST.getFirstChild() : arithmetic_factor_AST;
			currentAST.advanceChildToEnd();
		}
		{
		switch ( LA(1)) {
		case ID:
		{
			a = LT(1);
			a_AST = (PtalonAST)astFactory.create(a);
			match(ID);
			if ( inputState.guessing==0 ) {
				arithmetic_factor_AST = (PtalonAST)currentAST.root;
				
						arithmetic_factor_AST.addChild(a_AST);
					
			}
			break;
		}
		case NUMBER_LITERAL:
		{
			b = LT(1);
			b_AST = (PtalonAST)astFactory.create(b);
			match(NUMBER_LITERAL);
			if ( inputState.guessing==0 ) {
				arithmetic_factor_AST = (PtalonAST)currentAST.root;
				
						arithmetic_factor_AST.addChild(b_AST);
					
			}
			break;
		}
		case LPAREN:
		{
			match(LPAREN);
			arithmetic_expression();
			c_AST = (PtalonAST)returnAST;
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				arithmetic_factor_AST = (PtalonAST)currentAST.root;
				
						arithmetic_factor_AST.addChild(c_AST);
					
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = arithmetic_factor_AST;
	}
	
	public final void arithmetic_term() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST arithmetic_term_AST = null;
		
		arithmetic_factor();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop33:
		do {
			if (((LA(1) >= STAR && LA(1) <= MOD))) {
				{
				switch ( LA(1)) {
				case STAR:
				{
					PtalonAST tmp26_AST = null;
					tmp26_AST = (PtalonAST)astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp26_AST);
					match(STAR);
					break;
				}
				case DIVIDE:
				{
					PtalonAST tmp27_AST = null;
					tmp27_AST = (PtalonAST)astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp27_AST);
					match(DIVIDE);
					break;
				}
				case MOD:
				{
					PtalonAST tmp28_AST = null;
					tmp28_AST = (PtalonAST)astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp28_AST);
					match(MOD);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				arithmetic_factor();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop33;
			}
			
		} while (true);
		}
		arithmetic_term_AST = (PtalonAST)currentAST.root;
		returnAST = arithmetic_term_AST;
	}
	
	public final void relational_expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST relational_expression_AST = null;
		
		arithmetic_expression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case EQUAL:
		{
			PtalonAST tmp29_AST = null;
			tmp29_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp29_AST);
			match(EQUAL);
			break;
		}
		case NOT_EQUAL:
		{
			PtalonAST tmp30_AST = null;
			tmp30_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp30_AST);
			match(NOT_EQUAL);
			break;
		}
		case LESS_THAN:
		{
			PtalonAST tmp31_AST = null;
			tmp31_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp31_AST);
			match(LESS_THAN);
			break;
		}
		case GREATER_THAN:
		{
			PtalonAST tmp32_AST = null;
			tmp32_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp32_AST);
			match(GREATER_THAN);
			break;
		}
		case LESS_EQUAL:
		{
			PtalonAST tmp33_AST = null;
			tmp33_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp33_AST);
			match(LESS_EQUAL);
			break;
		}
		case GREATER_EQUAL:
		{
			PtalonAST tmp34_AST = null;
			tmp34_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp34_AST);
			match(GREATER_EQUAL);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		arithmetic_expression();
		astFactory.addASTChild(currentAST, returnAST);
		relational_expression_AST = (PtalonAST)currentAST.root;
		returnAST = relational_expression_AST;
	}
	
	public final void boolean_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_factor_AST = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		Token  c = null;
		PtalonAST c_AST = null;
		Token  d = null;
		PtalonAST d_AST = null;
		Token  e = null;
		PtalonAST e_AST = null;
		
			boolean sign = true;
		
		
		{
		_loop42:
		do {
			if ((LA(1)==LOGICAL_NOT)) {
				PtalonAST tmp35_AST = null;
				tmp35_AST = (PtalonAST)astFactory.create(LT(1));
				match(LOGICAL_NOT);
				if ( inputState.guessing==0 ) {
					
							sign = !sign;
						
				}
			}
			else {
				break _loop42;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			boolean_factor_AST = (PtalonAST)currentAST.root;
			
					if (sign) {
						boolean_factor_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(BOOLEAN_FACTOR,"boolean_factor")).add((PtalonAST)astFactory.create(LOGICAL_BUFFER,"!!")));
					} else {
						boolean_factor_AST = (PtalonAST)astFactory.make( (new ASTArray(2)).add((PtalonAST)astFactory.create(BOOLEAN_FACTOR,"boolean_factor")).add((PtalonAST)astFactory.create(LOGICAL_NOT,"!")));
					}
				
			currentAST.root = boolean_factor_AST;
			currentAST.child = boolean_factor_AST!=null &&boolean_factor_AST.getFirstChild()!=null ?
				boolean_factor_AST.getFirstChild() : boolean_factor_AST;
			currentAST.advanceChildToEnd();
		}
		{
		switch ( LA(1)) {
		case TRUE:
		{
			c = LT(1);
			c_AST = (PtalonAST)astFactory.create(c);
			match(TRUE);
			if ( inputState.guessing==0 ) {
				boolean_factor_AST = (PtalonAST)currentAST.root;
				
						boolean_factor_AST.addChild(c_AST);	
					
			}
			break;
		}
		case FALSE:
		{
			d = LT(1);
			d_AST = (PtalonAST)astFactory.create(d);
			match(FALSE);
			if ( inputState.guessing==0 ) {
				boolean_factor_AST = (PtalonAST)currentAST.root;
				
						boolean_factor_AST.addChild(d_AST);	
					
			}
			break;
		}
		default:
			if ((_tokenSet_2.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
				{
				boolean synPredMatched46 = false;
				if (((LA(1)==LPAREN) && (_tokenSet_0.member(LA(2))))) {
					int _m46 = mark();
					synPredMatched46 = true;
					inputState.guessing++;
					try {
						{
						match(LPAREN);
						boolean_expression();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched46 = false;
					}
					rewind(_m46);
inputState.guessing--;
				}
				if ( synPredMatched46 ) {
					match(LPAREN);
					boolean_expression();
					a_AST = (PtalonAST)returnAST;
					match(RPAREN);
					if ( inputState.guessing==0 ) {
						boolean_factor_AST = (PtalonAST)currentAST.root;
						
								boolean_factor_AST.addChild(a_AST);	
							
					}
				}
				else if ((_tokenSet_2.member(LA(1))) && (_tokenSet_5.member(LA(2)))) {
					relational_expression();
					b_AST = (PtalonAST)returnAST;
					if ( inputState.guessing==0 ) {
						boolean_factor_AST = (PtalonAST)currentAST.root;
						
								boolean_factor_AST.addChild(b_AST);	
						
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((LA(1)==ID) && (_tokenSet_6.member(LA(2)))) {
				e = LT(1);
				e_AST = (PtalonAST)astFactory.create(e);
				match(ID);
				if ( inputState.guessing==0 ) {
					boolean_factor_AST = (PtalonAST)currentAST.root;
					
							boolean_factor_AST.addChild(e_AST);	
						
				}
			}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = boolean_factor_AST;
	}
	
	public final void boolean_term() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST boolean_term_AST = null;
		
		boolean_factor();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop49:
		do {
			if ((LA(1)==LOGICAL_AND)) {
				PtalonAST tmp38_AST = null;
				tmp38_AST = (PtalonAST)astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp38_AST);
				match(LOGICAL_AND);
				boolean_factor();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop49;
			}
			
		} while (true);
		}
		boolean_term_AST = (PtalonAST)currentAST.root;
		returnAST = boolean_term_AST;
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
		case INTPARAMETER:
		case BOOLPARAMETER:
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
		PtalonAST a2_AST = null;
		PtalonAST c2_AST = null;
		
			AST trueTree = null;
			AST falseTree = null;
		
		
		i = LT(1);
		i_AST = (PtalonAST)astFactory.create(i);
		match(IF);
		match(LPAREN);
		boolean_expression();
		b_AST = (PtalonAST)returnAST;
		match(RPAREN);
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
		_loop57:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
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
			default:
			{
				break _loop57;
			}
			}
		} while (true);
		}
		match(RCURLY);
		match(ELSE);
		match(LCURLY);
		{
		_loop59:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
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
			default:
			{
				break _loop59;
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
	
	public final void actor_definition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST actor_definition_AST = null;
		PtalonAST i_AST = null;
		Token  a = null;
		PtalonAST a_AST = null;
		PtalonAST b_AST = null;
		PtalonAST c_AST = null;
		
		if ( inputState.guessing==0 ) {
			actor_definition_AST = (PtalonAST)currentAST.root;
			
					actor_definition_AST = (PtalonAST)astFactory.create(ACTOR_DEFINITION);
				
			currentAST.root = actor_definition_AST;
			currentAST.child = actor_definition_AST!=null &&actor_definition_AST.getFirstChild()!=null ?
				actor_definition_AST.getFirstChild() : actor_definition_AST;
			currentAST.advanceChildToEnd();
		}
		{
		_loop62:
		do {
			if ((LA(1)==IMPORT)) {
				import_declaration();
				i_AST = (PtalonAST)returnAST;
				if ( inputState.guessing==0 ) {
					actor_definition_AST = (PtalonAST)currentAST.root;
					
							actor_definition_AST.addChild(i_AST);
						
				}
			}
			else {
				break _loop62;
			}
			
		} while (true);
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
		_loop64:
		do {
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
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
			default:
			{
				break _loop64;
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
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 15034221056L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 68719411712L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 1835520L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 33489408L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 17179607552L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 2147222016L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 51539804160L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	
	}
