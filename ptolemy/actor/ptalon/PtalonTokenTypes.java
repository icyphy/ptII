// $ANTLR : "parser.g" -> "PtalonLexer.java"$
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
	int RELATION = 13;
	int TRANSPARENT = 14;
	int COLON = 15;
	int DOT = 16;
	int IMPORT = 17;
	int TRUE = 18;
	int FALSE = 19;
	int IF = 20;
	int ELSE = 21;
	int IS = 22;
	int FOR = 23;
	int INITIALLY = 24;
	int NEXT = 25;
	int DANGLING_PORTS_OKAY = 26;
	int ASSIGN = 27;
	int RPAREN = 28;
	int COMMA = 29;
	int EXPRESSION = 30;
	int LPAREN = 31;
	int SEMI = 32;
	int COMMENT = 33;
	int LCURLY = 34;
	int RCURLY = 35;
	int TRUEBRANCH = 36;
	int FALSEBRANCH = 37;
	int QUALID = 38;
	int ATTRIBUTE = 39;
	int ACTOR_DECLARATION = 40;
	int ACTOR_DEFINITION = 41;
	int NEGATIVE_SIGN = 42;
	int POSITIVE_SIGN = 43;
	int ARITHMETIC_FACTOR = 44;
	int BOOLEAN_FACTOR = 45;
	int LOGICAL_BUFFER = 46;
	int ARITHMETIC_EXPRESSION = 47;
	int BOOLEAN_EXPRESSION = 48;
	int MULTIPORT = 49;
	int MULTIINPORT = 50;
	int MULTIOUTPORT = 51;
	int PARAM_EQUALS = 52;
	int ACTOR_EQUALS = 53;
	int SATISFIES = 54;
	int VARIABLE = 55;
	int DYNAMIC_NAME = 56;
	int ACTOR_LABEL = 57;
	int QUALIFIED_PORT = 58;
	int ESC = 59;
	int NUMBER_LITERAL = 60;
	int ATTRIBUTE_MARKER = 61;
	int STRING_LITERAL = 62;
	int WHITE_SPACE = 63;
}
