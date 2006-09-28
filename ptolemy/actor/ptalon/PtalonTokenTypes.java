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
	int DANGLING_PORTS_OKAY = 25;
	int ASSIGN = 26;
	int RPAREN = 27;
	int COMMA = 28;
	int EXPRESSION = 29;
	int LPAREN = 30;
	int SEMI = 31;
	int COMMENT = 32;
	int LCURLY = 33;
	int RCURLY = 34;
	int TRUEBRANCH = 35;
	int FALSEBRANCH = 36;
	int QUALID = 37;
	int ATTRIBUTE = 38;
	int ACTOR_DECLARATION = 39;
	int ACTOR_DEFINITION = 40;
	int NEGATIVE_SIGN = 41;
	int POSITIVE_SIGN = 42;
	int ARITHMETIC_FACTOR = 43;
	int BOOLEAN_FACTOR = 44;
	int LOGICAL_BUFFER = 45;
	int ARITHMETIC_EXPRESSION = 46;
	int BOOLEAN_EXPRESSION = 47;
	int MULTIPORT = 48;
	int MULTIINPORT = 49;
	int MULTIOUTPORT = 50;
	int PARAM_EQUALS = 51;
	int ACTOR_EQUALS = 52;
	int SATISFIES = 53;
	int VARIABLE = 54;
	int DYNAMIC_NAME = 55;
	int ESC = 56;
	int NUMBER_LITERAL = 57;
	int ATTRIBUTE_MARKER = 58;
	int STRING_LITERAL = 59;
	int WHITE_SPACE = 60;
}
