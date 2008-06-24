// $ANTLR 2.7.7 (2006-11-01): "parser.g" -> "PtalonLexer.java"$
/* Lexer/Parser for Ptalon.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
	int ITERATE = 14;
	int INITIALLY = 15;
	int NEXT = 16;
	int RELATION = 17;
	int TRANSPARENT = 18;
	int REFERENCE = 19;
	int COLON = 20;
	int DOT = 21;
	int IMPORT = 22;
	int TRUE = 23;
	int FALSE = 24;
	int IF = 25;
	int ELSE = 26;
	int IS = 27;
	int FOR = 28;
	int DANGLING_PORTS_OKAY = 29;
	int ATTACH_DANGLING_PORTS = 30;
	int ASSIGN = 31;
	int RPAREN = 32;
	int COMMA = 33;
	int EXPRESSION = 34;
	int LPAREN = 35;
	int SEMI = 36;
	int LCURLY = 37;
	int RCURLY = 38;
	int TRANSFORM = 39;
	int STAR = 40;
	int TRUEBRANCH = 41;
	int FALSEBRANCH = 42;
	int QUALID = 43;
	int ATTRIBUTE = 44;
	int ACTOR_DECLARATION = 45;
	int ACTOR_DEFINITION = 46;
	int TRANSFORMATION = 47;
	int NEGATIVE_SIGN = 48;
	int POSITIVE_SIGN = 49;
	int ARITHMETIC_FACTOR = 50;
	int BOOLEAN_FACTOR = 51;
	int LOGICAL_BUFFER = 52;
	int ARITHMETIC_EXPRESSION = 53;
	int BOOLEAN_EXPRESSION = 54;
	int MULTIPORT = 55;
	int MULTIINPORT = 56;
	int MULTIOUTPORT = 57;
	int PARAM_EQUALS = 58;
	int ACTOR_EQUALS = 59;
	int SATISFIES = 60;
	int VARIABLE = 61;
	int DYNAMIC_NAME = 62;
	int ACTOR_LABEL = 63;
	int QUALIFIED_PORT = 64;
	int ESC = 65;
	int NUMBER_LITERAL = 66;
	int STRING_LITERAL = 67;
	int WHITE_SPACE = 68;
	int LINE_COMMENT = 69;
	int COMMENT = 70;
}
