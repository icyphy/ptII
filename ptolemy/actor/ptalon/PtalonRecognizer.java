// $ANTLR 2.7.6 (2005-12-22): "ptalonForPtolemy.g" -> "PtalonRecognizer.java"$
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

	import java.util.ArrayList;
	import ptolemy.actor.TypedAtomicActor;
	import ptolemy.actor.TypedIOPort;
	import ptolemy.actor.TypedIORelation;
	import ptolemy.data.expr.Parameter;
	import ptolemy.data.type.BaseType;
	import ptolemy.kernel.util.NameDuplicationException;
	import ptolemy.kernel.util.IllegalActionException;

public class PtalonRecognizer extends antlr.LLkParser       implements PtalonTokenTypes
 {

	/**
	 * Respective lists of all paramters, inports, outports,
	 * and relations generated in the actor being generated.
	 */
	private ArrayList<Parameter> _parameters = new ArrayList<Parameter>();
	private ArrayList<TypedIOPort> _inports = new ArrayList<TypedIOPort>();
	private ArrayList<TypedIOPort> _outports = new ArrayList<TypedIOPort>();
	private ArrayList<TypedIORelation> _relations = new ArrayList<TypedIORelation>();

protected PtalonRecognizer(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public PtalonRecognizer(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected PtalonRecognizer(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public PtalonRecognizer(TokenStream lexer) {
  this(lexer,2);
}

public PtalonRecognizer(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

/**
 * This parses this recognizer's input to see if it's a vailid
 * port declaration. In doing so, it will modify the input
 * <i>actor</i> to add port specified by the Ptalon model. 
 * @param actor The input actor.
 * @exception IllegalActionException If generated in trying to modify 
 * the Ptalon actor.
 * modify the Ptalon actor.  
 */
	public final void port_declaration(
		PtalonActor actor
	) throws RecognitionException, TokenStreamException, IllegalActionException {
		
		Token  a = null;
		
			String flow = "";
		
		
		{
		switch ( LA(1)) {
		case LITERAL_port:
		{
			match(LITERAL_port);
			break;
		}
		case LITERAL_inport:
		{
			match(LITERAL_inport);
			if ( inputState.guessing==0 ) {
				
							flow = "input";
						
			}
			break;
		}
		case LITERAL_outport:
		{
			match(LITERAL_outport);
			if ( inputState.guessing==0 ) {
				
							flow = "output";
						
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		a = LT(1);
		match(ID);
		if ( inputState.guessing==0 ) {
			
					TypedIOPort p;
					if (flow.equals("input")) {
						p = actor.addPort(a.getText(), PtalonActor.INPUT);
						_inports.add(p);
					} else if (flow.equals("output")) {
						p = actor.addPort(a.getText(), PtalonActor.OUTPUT);
						_outports.add(p);
					} else {
						p = actor.addPort(a.getText(), PtalonActor.BIDIRECTIONAL);
						_inports.add(p);
						_outports.add(p);
					}
				
		}
	}
	
/**
 * This parses this recognizer's input to see if it's a vailid
 * parameter declaration. In doing so, it will modify the input
 * <i>actor</i> to add parameters specified by the Ptalon model. 
 * @param actor The input actor.
 * @exception IllegalActionException If generated in trying to modify 
 * the Ptalon actor.
 * modify the Ptalon actor.  
 */
	public final void parameter_declaration(
		PtalonActor actor
	) throws RecognitionException, TokenStreamException, IllegalActionException {
		
		Token  a = null;
		
			String type = "";
		
		
		{
		switch ( LA(1)) {
		case LITERAL_parameter:
		{
			match(LITERAL_parameter);
			break;
		}
		case LITERAL_intparameter:
		{
			match(LITERAL_intparameter);
			if ( inputState.guessing==0 ) {
				
							type = "int";
						
			}
			break;
		}
		case LITERAL_boolparameter:
		{
			match(LITERAL_boolparameter);
			if ( inputState.guessing==0 ) {
				
							type = "boolean";
						
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		a = LT(1);
		match(ID);
		if ( inputState.guessing==0 ) {
			
					Parameter p = actor.addParameter(a.getText());
					if (type.equals("int")) {
						p.setTypeEquals(BaseType.INT);
					} else if (type.equals("boolean")) {
						p.setTypeEquals(BaseType.BOOLEAN);
					}
					_parameters.add(p);
				
		}
	}
	
/**
 * This parses this recognizer's input to see if it's a vailid
 * parameter declaration. In doing so, it will modify the input
 * <i>actor</i> to add parameters specified by the Ptalon model. 
 * @param actor The input actor.
 * @exception IllegalActionException If generated in trying to modify 
 * the Ptalon actor.
 * modify the Ptalon actor.  
 */
	public final void relation_declaration(
		PtalonActor actor
	) throws RecognitionException, TokenStreamException, IllegalActionException {
		
		Token  a = null;
		
		match(LITERAL_relation);
		a = LT(1);
		match(ID);
		if ( inputState.guessing==0 ) {
			
					TypedIORelation r = actor.addRelation(a.getText());
					_relations.add(r);
				
		}
	}
	
/**
 * Parses for a qualified identifier.
 * @return A String representation of this identifier.
 */
	public final String[]  qualified_identifier() throws RecognitionException, TokenStreamException {
		String[] s;
		
		Token  a = null;
		Token  b = null;
		
			s = new String[] {"", ""};
		
		
		a = LT(1);
		match(ID);
		if ( inputState.guessing==0 ) {
			
					s[0] = s[0].concat(a.getText());
					s[1] = a.getText();
				
		}
		{
		_loop8:
		do {
			if ((LA(1)==DOT)) {
				match(DOT);
				b = LT(1);
				match(ID);
				if ( inputState.guessing==0 ) {
					
							s[0] = s[0].concat("." + b.getText());
							s[1] = b.getText();
						
				}
			}
			else {
				break _loop8;
			}
			
		} while (true);
		}
		return s;
	}
	
/**
 * This parses this recognizer's input to see if it's a vailid
 * attribute declaration. If the attribute is <i>actorSource</i>
 * it will modify the input <i>actor</i> to add an instance of
 * the specified actor and make its ports and paramters visible.
 * @param actor The input actor.
 * @exception IllegalActionException If generated in trying to modify 
 * the Ptalon actor.
 * modify the Ptalon actor.  
 */
	public final void attribute(
		PtalonActor actor
	) throws RecognitionException, TokenStreamException, IllegalActionException {
		
		Token  a = null;
		
				String[] b;
			
		
		match(ATTRIBUTE_MARKER);
		a = LT(1);
		match(ID);
		b=qualified_identifier();
		match(ATTRIBUTE_MARKER);
		if ( inputState.guessing==0 ) {
			
					if (a.getText().equals("actorSource")) {
						TypedAtomicActor aa = actor.addAtomicActor(b[0], b[1]);
					}
				
		}
	}
	
	public final void assignment() throws RecognitionException, TokenStreamException {
		
		
		match(ID);
		match(ASSIGN);
		{
		boolean synPredMatched14 = false;
		if (((LA(1)==ID) && (LA(2)==RPAREN||LA(2)==COMMA))) {
			int _m14 = mark();
			synPredMatched14 = true;
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
				synPredMatched14 = false;
			}
			rewind(_m14);
inputState.guessing--;
		}
		if ( synPredMatched14 ) {
			match(ID);
		}
		else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2)))) {
			{
			boolean synPredMatched17 = false;
			if (((LA(1)==ID) && (LA(2)==LPAREN))) {
				int _m17 = mark();
				synPredMatched17 = true;
				inputState.guessing++;
				try {
					{
					match(ID);
					match(LPAREN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched17 = false;
				}
				rewind(_m17);
inputState.guessing--;
			}
			if ( synPredMatched17 ) {
				actor_declaration();
			}
			else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2)))) {
				{
				boolean synPredMatched20 = false;
				if (((_tokenSet_2.member(LA(1))) && (_tokenSet_3.member(LA(2))))) {
					int _m20 = mark();
					synPredMatched20 = true;
					inputState.guessing++;
					try {
						{
						arithmetic_expression();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched20 = false;
					}
					rewind(_m20);
inputState.guessing--;
				}
				if ( synPredMatched20 ) {
					arithmetic_expression();
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2)))) {
					boolean_expression();
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
	}
	
	public final void actor_declaration() throws RecognitionException, TokenStreamException {
		
		
		match(ID);
		match(LPAREN);
		{
		switch ( LA(1)) {
		case ID:
		{
			assignment();
			{
			_loop24:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					assignment();
				}
				else {
					break _loop24;
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
	}
	
	public final void arithmetic_expression() throws RecognitionException, TokenStreamException {
		
		
		arithmetic_term();
		{
		_loop36:
		do {
			if ((LA(1)==MINUS||LA(1)==PLUS)) {
				{
				switch ( LA(1)) {
				case PLUS:
				{
					match(PLUS);
					break;
				}
				case MINUS:
				{
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
			}
			else {
				break _loop36;
			}
			
		} while (true);
		}
	}
	
	public final void boolean_expression() throws RecognitionException, TokenStreamException {
		
		
		boolean_term();
		{
		_loop51:
		do {
			if ((LA(1)==LOGICAL_OR)) {
				match(LOGICAL_OR);
				boolean_term();
			}
			else {
				break _loop51;
			}
			
		} while (true);
		}
	}
	
	public final void arithmetic_factor() throws RecognitionException, TokenStreamException {
		
		
		{
		_loop27:
		do {
			if ((LA(1)==MINUS)) {
				match(MINUS);
			}
			else {
				break _loop27;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case ID:
		{
			match(ID);
			break;
		}
		case NUMBER_LITERAL:
		{
			match(NUMBER_LITERAL);
			break;
		}
		case LPAREN:
		{
			match(LPAREN);
			arithmetic_expression();
			match(RPAREN);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
	}
	
	public final void arithmetic_term() throws RecognitionException, TokenStreamException {
		
		
		arithmetic_factor();
		{
		_loop32:
		do {
			if (((LA(1) >= STAR && LA(1) <= MOD))) {
				{
				switch ( LA(1)) {
				case STAR:
				{
					match(STAR);
					break;
				}
				case DIVIDE:
				{
					match(DIVIDE);
					break;
				}
				case MOD:
				{
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
			}
			else {
				break _loop32;
			}
			
		} while (true);
		}
	}
	
	public final void relational_expression() throws RecognitionException, TokenStreamException {
		
		
		arithmetic_expression();
		{
		switch ( LA(1)) {
		case EQUAL:
		{
			match(EQUAL);
			break;
		}
		case NOT_EQUAL:
		{
			match(NOT_EQUAL);
			break;
		}
		case LESS_THAN:
		{
			match(LESS_THAN);
			break;
		}
		case GREATER_THAN:
		{
			match(GREATER_THAN);
			break;
		}
		case LESS_EQUAL:
		{
			match(LESS_EQUAL);
			break;
		}
		case GREATER_EQUAL:
		{
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
	}
	
	public final void boolean_factor() throws RecognitionException, TokenStreamException {
		
		
		{
		_loop41:
		do {
			if ((LA(1)==LOGICAL_NOT)) {
				match(LOGICAL_NOT);
			}
			else {
				break _loop41;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case LITERAL_true:
		{
			match(LITERAL_true);
			break;
		}
		case LITERAL_false:
		{
			match(LITERAL_false);
			break;
		}
		default:
			if ((_tokenSet_2.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
				{
				boolean synPredMatched45 = false;
				if (((LA(1)==LPAREN) && (_tokenSet_0.member(LA(2))))) {
					int _m45 = mark();
					synPredMatched45 = true;
					inputState.guessing++;
					try {
						{
						match(LPAREN);
						boolean_expression();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched45 = false;
					}
					rewind(_m45);
inputState.guessing--;
				}
				if ( synPredMatched45 ) {
					match(LPAREN);
					boolean_expression();
					match(RPAREN);
				}
				else if ((_tokenSet_2.member(LA(1))) && (_tokenSet_5.member(LA(2)))) {
					relational_expression();
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((LA(1)==ID) && (_tokenSet_6.member(LA(2)))) {
				match(ID);
			}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
	}
	
	public final void boolean_term() throws RecognitionException, TokenStreamException {
		
		
		boolean_factor();
		{
		_loop48:
		do {
			if ((LA(1)==LOGICAL_AND)) {
				match(LOGICAL_AND);
				boolean_factor();
			}
			else {
				break _loop48;
			}
			
		} while (true);
		}
	}
	
/**
 * This parses this recognizer's input to see if it's a vailid
 * atomic statement. In doing so, it will modify the input
 * <i>actor</i> to become the actor specified by the Ptalon model. 
 * @param actor The input actor.
 * @exception IllegalActionException If generated in trying to modify 
 * the Ptalon actor.
 * @exception NameDuplicationException If generated in trying to
 * modify the Ptalon actor. 
 */
	public final void atomic_statement(
		PtalonActor actor
	) throws RecognitionException, TokenStreamException, IllegalActionException,NameDuplicationException {
		
		
		switch ( LA(1)) {
		case LITERAL_port:
		case LITERAL_inport:
		case LITERAL_outport:
		case ID:
		case LITERAL_parameter:
		case LITERAL_intparameter:
		case LITERAL_boolparameter:
		case LITERAL_relation:
		{
			{
			{
			switch ( LA(1)) {
			case LITERAL_port:
			case LITERAL_inport:
			case LITERAL_outport:
			{
				port_declaration(actor);
				break;
			}
			case LITERAL_parameter:
			case LITERAL_intparameter:
			case LITERAL_boolparameter:
			{
				parameter_declaration(actor);
				break;
			}
			case LITERAL_relation:
			{
				relation_declaration(actor);
				break;
			}
			case ID:
			{
				actor_declaration();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(SEMI);
			}
			break;
		}
		case ATTRIBUTE_MARKER:
		{
			attribute(actor);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
	}
	
/**
 * This parses this recognizer's input to see if it's a vailid
 * conditional statement. In doing so, it will modify the input
 * <i>actor</i> to become the actor specified by the Ptalon model. 
 * @param actor The input actor.
 * @exception IllegalActionException If generated in trying to modify 
 * the Ptalon actor.
 * @exception NameDuplicationException If generated in trying to
 * modify the Ptalon actor. 
 */
	public final void conditional_statement(
		PtalonActor actor
	) throws RecognitionException, TokenStreamException, IllegalActionException,NameDuplicationException {
		
		
		match(LITERAL_if);
		boolean_expression();
		match(LCURLY);
		{
		switch ( LA(1)) {
		case LITERAL_port:
		case LITERAL_inport:
		case LITERAL_outport:
		case ID:
		case LITERAL_parameter:
		case LITERAL_intparameter:
		case LITERAL_boolparameter:
		case LITERAL_relation:
		case ATTRIBUTE_MARKER:
		{
			atomic_statement(actor);
			break;
		}
		case LITERAL_if:
		{
			conditional_statement(actor);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RCURLY);
		match(LITERAL_else);
		match(LCURLY);
		{
		switch ( LA(1)) {
		case LITERAL_port:
		case LITERAL_inport:
		case LITERAL_outport:
		case ID:
		case LITERAL_parameter:
		case LITERAL_intparameter:
		case LITERAL_boolparameter:
		case LITERAL_relation:
		case ATTRIBUTE_MARKER:
		{
			atomic_statement(actor);
			break;
		}
		case LITERAL_if:
		{
			conditional_statement(actor);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RCURLY);
	}
	
/**
 * This parses this recognizer's inputs to see if it's a valid actor 
 * definition. In doing so, it will modify the input <i>actor</i> to 
 * become the actor specified by the Ptalon model.
 * @param actor The input actor.
 * @exception IllegalActionException If generated in trying to modify 
 * the Ptalon actor.
 * @exception NameDuplicationException If generated in trying to
 * modify the Ptalon actor.
 * 
 */
	public final void actor_definition(
		PtalonActor actor
	) throws RecognitionException, TokenStreamException, IllegalActionException,NameDuplicationException {
		
		Token  a = null;
		
		try {      // for error handling
			a = LT(1);
			match(ID);
			match(LITERAL_is);
			match(LCURLY);
			{
			_loop60:
			do {
				switch ( LA(1)) {
				case LITERAL_port:
				case LITERAL_inport:
				case LITERAL_outport:
				case ID:
				case LITERAL_parameter:
				case LITERAL_intparameter:
				case LITERAL_boolparameter:
				case LITERAL_relation:
				case ATTRIBUTE_MARKER:
				{
					atomic_statement(actor);
					break;
				}
				case LITERAL_if:
				{
					conditional_statement(actor);
					break;
				}
				default:
				{
					break _loop60;
				}
				}
			} while (true);
			}
			match(RCURLY);
			if ( inputState.guessing==0 ) {
				
						try {
							actor.setName(a.getText());
						} catch (NameDuplicationException ex1) {
							actor.setName(actor.uniqueName(a.getText()));
						}
					
			}
		}
		catch (IllegalActionException ex) {
			if (inputState.guessing==0) {
				
					throw ex;
				
			} else {
				throw ex;
			}
		}
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
		"\"boolparameter\"",
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
		"LBRACKET",
		"RBRACKET",
		"BINARY_NOT",
		"ESC",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 7517110400L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 34359705728L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 917632L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 16744576L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 8589803648L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 1073610880L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 163208855552L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	
	}
