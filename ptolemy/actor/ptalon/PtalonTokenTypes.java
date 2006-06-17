// $ANTLR 2.7.6 (2005-12-22): "ptalonForPtolemy.g" -> "PtalonLexer.java"$
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
	int LITERAL_port = 4;
	int LITERAL_inport = 5;
	int LITERAL_outport = 6;
	int ID = 7;
	int LITERAL_parameter = 8;
	int LITERAL_intparameter = 9;
	int LITERAL_boolparameter = 10;
	int LITERAL_relation = 11;
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
	int LITERAL_true = 31;
	int LITERAL_false = 32;
	int LOGICAL_AND = 33;
	int LOGICAL_OR = 34;
	int SEMI = 35;
	int LITERAL_if = 36;
	int LCURLY = 37;
	int RCURLY = 38;
	int LITERAL_else = 39;
	int LITERAL_is = 40;
	int LBRACKET = 41;
	int RBRACKET = 42;
	int BINARY_NOT = 43;
	int ESC = 44;
	int STRING_LITERAL = 45;
	int WHITE_SPACE = 46;
}
