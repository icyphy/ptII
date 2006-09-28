// $ANTLR : "populator.g" -> "PtalonPopulator.java"$
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

public interface PtalonPopulatorTokenTypes {
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
	int COLON = 14;
	int DOT = 15;
	int IMPORT = 16;
	int TRUE = 17;
	int FALSE = 18;
	int IF = 19;
	int ELSE = 20;
	int IS = 21;
	int FOR = 22;
	int INITIALLY = 23;
	int NEXT = 24;
	int ASSIGN = 25;
	int RPAREN = 26;
	int COMMA = 27;
	int EXPRESSION = 28;
	int LPAREN = 29;
	int SEMI = 30;
	int LCURLY = 31;
	int RCURLY = 32;
	int TRUEBRANCH = 33;
	int FALSEBRANCH = 34;
	int QUALID = 35;
	int ATTRIBUTE = 36;
	int ACTOR_DECLARATION = 37;
	int ACTOR_DEFINITION = 38;
	int NEGATIVE_SIGN = 39;
	int POSITIVE_SIGN = 40;
	int ARITHMETIC_FACTOR = 41;
	int BOOLEAN_FACTOR = 42;
	int LOGICAL_BUFFER = 43;
	int ARITHMETIC_EXPRESSION = 44;
	int BOOLEAN_EXPRESSION = 45;
	int MULTIPORT = 46;
	int MULTIINPORT = 47;
	int MULTIOUTPORT = 48;
	int PARAM_EQUALS = 49;
	int ACTOR_EQUALS = 50;
	int SATISFIES = 51;
	int VARIABLE = 52;
	int DYNAMIC_NAME = 53;
	int ESC = 54;
	int NUMBER_LITERAL = 55;
	int ATTRIBUTE_MARKER = 56;
	int STRING_LITERAL = 57;
	int WHITE_SPACE = 58;
}
