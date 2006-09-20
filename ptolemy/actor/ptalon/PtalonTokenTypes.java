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
	int INPORT = 7;
	int OUTPORT = 8;
	int ID = 9;
	int PARAMETER = 10;
	int INTPARAMETER = 11;
	int BOOLPARAMETER = 12;
	int RELATION = 13;
	int DOT = 14;
	int ASSIGN = 15;
	int RPAREN = 16;
	int COMMA = 17;
	int LPAREN = 18;
	int MINUS = 19;
	int NUMBER_LITERAL = 20;
	int STAR = 21;
	int DIVIDE = 22;
	int MOD = 23;
	int PLUS = 24;
	int EQUAL = 25;
	int NOT_EQUAL = 26;
	int LESS_THAN = 27;
	int GREATER_THAN = 28;
	int LESS_EQUAL = 29;
	int GREATER_EQUAL = 30;
	int LOGICAL_NOT = 31;
	int TRUE = 32;
	int FALSE = 33;
	int LOGICAL_AND = 34;
	int LOGICAL_OR = 35;
	int IF = 36;
	int LCURLY = 37;
	int RCURLY = 38;
	int ELSE = 39;
	int IS = 40;
	int TRUEBRANCH = 41;
	int FALSEBRANCH = 42;
	int QUALID = 43;
	int ATTRIBUTE = 44;
	int ACTOR_DECLARATION = 45;
	int ACTOR_DEFINITION = 46;
	int NEGATIVE_SIGN = 47;
	int POSITIVE_SIGN = 48;
	int ARITHMETIC_FACTOR = 49;
	int BOOLEAN_FACTOR = 50;
	int LOGICAL_BUFFER = 51;
	int ARITHMETIC_EXPRESSION = 52;
	int BOOLEAN_EXPRESSION = 53;
	int LBRACKET = 54;
	int RBRACKET = 55;
	int BINARY_NOT = 56;
	int ESC = 57;
	int ATTRIBUTE_MARKER = 58;
	int STRING_LITERAL = 59;
	int WHITE_SPACE = 60;
}
