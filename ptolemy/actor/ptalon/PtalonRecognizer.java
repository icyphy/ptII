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
		_loop82:
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
				break _loop82;
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
				PtalonAST tmp4_AST = null;
				tmp4_AST = (PtalonAST)astFactory.create(LT(1));
				match(LBRACKET);
				PtalonAST tmp5_AST = null;
				tmp5_AST = (PtalonAST)astFactory.create(LT(1));
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
				PtalonAST tmp6_AST = null;
				tmp6_AST = (PtalonAST)astFactory.create(LT(1));
				match(LBRACKET);
				PtalonAST tmp7_AST = null;
				tmp7_AST = (PtalonAST)astFactory.create(LT(1));
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
				PtalonAST tmp8_AST = null;
				tmp8_AST = (PtalonAST)astFactory.create(LT(1));
				match(LBRACKET);
				PtalonAST tmp9_AST = null;
				tmp9_AST = (PtalonAST)astFactory.create(LT(1));
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
		
		{
		switch ( LA(1)) {
		case PARAMETER:
		{
			PtalonAST tmp10_AST = null;
			tmp10_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp10_AST);
			match(PARAMETER);
			break;
		}
		case ACTOR:
		{
			PtalonAST tmp11_AST = null;
			tmp11_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp11_AST);
			match(ACTOR);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		PtalonAST tmp12_AST = null;
		tmp12_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp12_AST);
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
		
		PtalonAST tmp13_AST = null;
		tmp13_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp13_AST);
		match(RELATION);
		PtalonAST tmp14_AST = null;
		tmp14_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp14_AST);
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
 * <p>#(ASSIGN ID expression)
 */
	public final void assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		PtalonAST assignment_AST = null;
		
		PtalonAST tmp15_AST = null;
		tmp15_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp15_AST);
		match(ID);
		PtalonAST tmp16_AST = null;
		tmp16_AST = (PtalonAST)astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp16_AST);
		match(ASSIGN);
		{
		boolean synPredMatched87 = false;
		if (((LA(1)==ID) && (LA(2)==RPAREN||LA(2)==COMMA))) {
			int _m87 = mark();
			synPredMatched87 = true;
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
				synPredMatched87 = false;
			}
			rewind(_m87);
inputState.guessing--;
		}
		if ( synPredMatched87 ) {
			PtalonAST tmp17_AST = null;
			tmp17_AST = (PtalonAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp17_AST);
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
			_loop93:
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
					break _loop93;
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
		PtalonAST a2_AST = null;
		PtalonAST c2_AST = null;
		
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
		_loop98:
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
			default:
			{
				break _loop98;
			}
			}
		} while (true);
		}
		match(RCURLY);
		match(ELSE);
		match(LCURLY);
		{
		_loop100:
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
			default:
			{
				break _loop100;
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
		_loop103:
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
				break _loop103;
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
		_loop105:
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
			default:
			{
				break _loop105;
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
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	
	}
