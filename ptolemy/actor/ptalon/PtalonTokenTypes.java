// $ANTLR 2.7.6 (2005-12-22): "parser.g" -> "PtalonLexer.java"$
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

public interface PtalonTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int PORT = 4;
	int LBRACKET = 5;
	int RBRACKET = 6;
	int INPORT = 7;
	int OUTPORT = 8;
	int ID = 9;
	int PARAMETER = 10;
	int EQUALS = 11;
	int ACTOR = 12;
	int ACTORPARAM = 13;
	int RELATION = 14;
	int TRANSPARENT = 15;
	int REFERENCE = 16;
	int COLON = 17;
	int DOT = 18;
	int IMPORT = 19;
	int TRUE = 20;
	int FALSE = 21;
	int IF = 22;
	int ELSE = 23;
	int IS = 24;
	int FOR = 25;
	int INITIALLY = 26;
	int NEXT = 27;
	int DANGLING_PORTS_OKAY = 28;
	int ATTACH_DANGLING_PORTS = 29;
	int ASSIGN = 30;
	int RPAREN = 31;
	int COMMA = 32;
	int EXPRESSION = 33;
	int LPAREN = 34;
	int SEMI = 35;
	int NEGATE = 36;
	int OPTIONAL = 37;
	int REMOVE = 38;
	int PRESERVE = 39;
	int LCURLY = 40;
	int RCURLY = 41;
	int TRANSFORM = 42;
	int PLUS = 43;
	int TRUEBRANCH = 44;
	int FALSEBRANCH = 45;
	int QUALID = 46;
	int ATTRIBUTE = 47;
	int ACTOR_DECLARATION = 48;
	int ACTOR_DEFINITION = 49;
	int TRANSFORMATION = 50;
	int NEGATIVE_SIGN = 51;
	int POSITIVE_SIGN = 52;
	int ARITHMETIC_FACTOR = 53;
	int BOOLEAN_FACTOR = 54;
	int LOGICAL_BUFFER = 55;
	int ARITHMETIC_EXPRESSION = 56;
	int BOOLEAN_EXPRESSION = 57;
	int MULTIPORT = 58;
	int MULTIINPORT = 59;
	int MULTIOUTPORT = 60;
	int PARAM_EQUALS = 61;
	int ACTOR_EQUALS = 62;
	int SATISFIES = 63;
	int VARIABLE = 64;
	int DYNAMIC_NAME = 65;
	int ACTOR_LABEL = 66;
	int QUALIFIED_PORT = 67;
	int ACTOR_ID = 68;
	int ESC = 69;
	int NUMBER_LITERAL = 70;
	int STRING_LITERAL = 71;
	int WHITE_SPACE = 72;
	int LINE_COMMENT = 73;
	int COMMENT = 74;
}
