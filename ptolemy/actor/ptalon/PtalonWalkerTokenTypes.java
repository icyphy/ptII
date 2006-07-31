// $ANTLR 2.7.6 (2005-12-22): "ptolemyTreeWalker.g" -> "PtalonWalker.java"$
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

public interface PtalonWalkerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int PORT = 4;
	int INPORT = 5;
	int OUTPORT = 6;
	int ID = 7;
	int PARAMETER = 8;
	int INTPARAMETER = 9;
	int BOOLPARAMETER = 10;
	int RELATION = 11;
	int DOT = 12;
	int ATTRIBUTE_MARKER = 13;
	int ASSIGN = 14;
	int RPAREN = 15;
	int COMMA = 16;
	int LPAREN = 17;
	int MINUS = 18;
	int NUMBER_LITERAL = 19;
	int STAR = 20;
	int DIVIDE = 21;
	int MOD = 22;
	int PLUS = 23;
	int EQUAL = 24;
	int NOT_EQUAL = 25;
	int LESS_THAN = 26;
	int GREATER_THAN = 27;
	int LESS_EQUAL = 28;
	int GREATER_EQUAL = 29;
	int LOGICAL_NOT = 30;
	int TRUE = 31;
	int FALSE = 32;
	int LOGICAL_AND = 33;
	int LOGICAL_OR = 34;
	int SEMI = 35;
	int IF = 36;
	int LCURLY = 37;
	int RCURLY = 38;
	int ELSE = 39;
	int IS = 40;
	int OUTPARAMETER = 41;
	int QUALID = 42;
	int ATTRIBUTE = 43;
	int ACTOR_DECLARATION = 44;
	int ACTOR_DEFINITION = 45;
	int NEGATIVE_SIGN = 46;
	int POSITIVE_SIGN = 47;
	int ARITHMETIC_FACTOR = 48;
	int BOOLEAN_FACTOR = 49;
	int LOGICAL_BUFFER = 50;
	int LBRACKET = 51;
	int RBRACKET = 52;
	int BINARY_NOT = 53;
	int ESC = 54;
	int STRING_LITERAL = 55;
	int WHITE_SPACE = 56;
}
