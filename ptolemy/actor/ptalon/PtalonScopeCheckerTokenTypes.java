// $ANTLR : "scopeChecker.g" -> "PtalonScopeChecker.java"$
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

public interface PtalonScopeCheckerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int IMPORT = 4;
	int SEMI = 5;
	int PORT = 6;
	int INPORT = 7;
	int OUTPORT = 8;
	int ID = 9;
	int PARAMETER = 10;
	int ACTOR = 11;
	int RELATION = 12;
	int DOT = 13;
	int ASSIGN = 14;
	int RPAREN = 15;
	int COMMA = 16;
	int EXPRESSION = 17;
	int LPAREN = 18;
	int IF = 19;
	int LCURLY = 20;
	int RCURLY = 21;
	int ELSE = 22;
	int IS = 23;
	int TRUE = 24;
	int TRUEBRANCH = 25;
	int FALSE = 26;
	int FALSEBRANCH = 27;
	int QUALID = 28;
	int ATTRIBUTE = 29;
	int ACTOR_DECLARATION = 30;
	int ACTOR_DEFINITION = 31;
	int NEGATIVE_SIGN = 32;
	int POSITIVE_SIGN = 33;
	int ARITHMETIC_FACTOR = 34;
	int BOOLEAN_FACTOR = 35;
	int LOGICAL_BUFFER = 36;
	int ARITHMETIC_EXPRESSION = 37;
	int BOOLEAN_EXPRESSION = 38;
	int LBRACKET = 39;
	int RBRACKET = 40;
	int LOGICAL_OR = 41;
	int LOGICAL_AND = 42;
	int EQUAL = 43;
	int NOT_EQUAL = 44;
	int LESS_THAN = 45;
	int GREATER_THAN = 46;
	int LESS_EQUAL = 47;
	int GREATER_EQUAL = 48;
	int PLUS = 49;
	int MINUS = 50;
	int STAR = 51;
	int DIVIDE = 52;
	int MOD = 53;
	int BINARY_NOT = 54;
	int LOGICAL_NOT = 55;
	int ESC = 56;
	int NUMBER_LITERAL = 57;
	int ATTRIBUTE_MARKER = 58;
	int STRING_LITERAL = 59;
	int WHITE_SPACE = 60;
}
