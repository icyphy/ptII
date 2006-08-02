// $ANTLR : "ptalonForPtolemy.g" -> "PtalonLexer.java"$
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
	int ATTRIBUTE_MARKER = 15;
	int ASSIGN = 16;
	int RPAREN = 17;
	int COMMA = 18;
	int LPAREN = 19;
	int MINUS = 20;
	int NUMBER_LITERAL = 21;
	int STAR = 22;
	int DIVIDE = 23;
	int MOD = 24;
	int PLUS = 25;
	int EQUAL = 26;
	int NOT_EQUAL = 27;
	int LESS_THAN = 28;
	int GREATER_THAN = 29;
	int LESS_EQUAL = 30;
	int GREATER_EQUAL = 31;
	int LOGICAL_NOT = 32;
	int TRUE = 33;
	int FALSE = 34;
	int LOGICAL_AND = 35;
	int LOGICAL_OR = 36;
	int IF = 37;
	int LCURLY = 38;
	int RCURLY = 39;
	int ELSE = 40;
	int IS = 41;
	int OUTPARAMETER = 42;
	int QUALID = 43;
	int ATTRIBUTE = 44;
	int ACTOR_DECLARATION = 45;
	int ACTOR_DEFINITION = 46;
	int NEGATIVE_SIGN = 47;
	int POSITIVE_SIGN = 48;
	int ARITHMETIC_FACTOR = 49;
	int BOOLEAN_FACTOR = 50;
	int LOGICAL_BUFFER = 51;
	int LBRACKET = 52;
	int RBRACKET = 53;
	int BINARY_NOT = 54;
	int ESC = 55;
	int STRING_LITERAL = 56;
	int WHITE_SPACE = 57;
}
