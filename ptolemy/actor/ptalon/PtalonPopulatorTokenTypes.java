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
	int ASSIGN = 19;
	int RPAREN = 20;
	int COMMA = 21;
	int EXPRESSION = 22;
	int LPAREN = 23;
	int SEMI = 24;
	int IF = 25;
	int LCURLY = 26;
	int RCURLY = 27;
	int ELSE = 28;
	int IS = 29;
	int TRUEBRANCH = 30;
	int FALSEBRANCH = 31;
	int QUALID = 32;
	int ATTRIBUTE = 33;
	int ACTOR_DECLARATION = 34;
	int ACTOR_DEFINITION = 35;
	int NEGATIVE_SIGN = 36;
	int POSITIVE_SIGN = 37;
	int ARITHMETIC_FACTOR = 38;
	int BOOLEAN_FACTOR = 39;
	int LOGICAL_BUFFER = 40;
	int ARITHMETIC_EXPRESSION = 41;
	int BOOLEAN_EXPRESSION = 42;
	int MULTIPORT = 43;
	int MULTIINPORT = 44;
	int MULTIOUTPORT = 45;
	int PARAM_EQUALS = 46;
	int ACTOR_EQUALS = 47;
	int ESC = 48;
	int NUMBER_LITERAL = 49;
	int ATTRIBUTE_MARKER = 50;
	int STRING_LITERAL = 51;
	int WHITE_SPACE = 52;
}
