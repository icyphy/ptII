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
	int IMPORT = 4;
	int SEMI = 5;
	int PORT = 6;
	int LBRACKET = 7;
	int RBRACKET = 8;
	int INPORT = 9;
	int OUTPORT = 10;
	int ID = 11;
	int PARAMETER = 12;
	int EQUALS = 13;
	int ACTOR = 14;
	int RELATION = 15;
	int DOT = 16;
	int ASSIGN = 17;
	int RPAREN = 18;
	int COMMA = 19;
	int EXPRESSION = 20;
	int LPAREN = 21;
	int IF = 22;
	int LCURLY = 23;
	int RCURLY = 24;
	int ELSE = 25;
	int IS = 26;
	int TRUE = 27;
	int TRUEBRANCH = 28;
	int FALSE = 29;
	int FALSEBRANCH = 30;
	int QUALID = 31;
	int ATTRIBUTE = 32;
	int ACTOR_DECLARATION = 33;
	int ACTOR_DEFINITION = 34;
	int NEGATIVE_SIGN = 35;
	int POSITIVE_SIGN = 36;
	int ARITHMETIC_FACTOR = 37;
	int BOOLEAN_FACTOR = 38;
	int LOGICAL_BUFFER = 39;
	int ARITHMETIC_EXPRESSION = 40;
	int BOOLEAN_EXPRESSION = 41;
	int MULTIPORT = 42;
	int MULTIINPORT = 43;
	int MULTIOUTPORT = 44;
	int PARAM_EQUALS = 45;
	int ACTOR_EQUALS = 46;
	int ESC = 47;
	int NUMBER_LITERAL = 48;
	int ATTRIBUTE_MARKER = 49;
	int STRING_LITERAL = 50;
	int WHITE_SPACE = 51;
}
