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
	int ACTORPARAM = 13;
	int RELATION = 14;
	int TRANSPARENT = 15;
	int COLON = 16;
	int DOT = 17;
	int IMPORT = 18;
	int TRUE = 19;
	int FALSE = 20;
	int IF = 21;
	int ELSE = 22;
	int IS = 23;
	int FOR = 24;
	int INITIALLY = 25;
	int NEXT = 26;
	int DANGLING_PORTS_OKAY = 27;
	int ASSIGN = 28;
	int RPAREN = 29;
	int COMMA = 30;
	int EXPRESSION = 31;
	int LPAREN = 32;
	int SEMI = 33;
	int COMMENT = 34;
	int LCURLY = 35;
	int RCURLY = 36;
	int TRUEBRANCH = 37;
	int FALSEBRANCH = 38;
	int QUALID = 39;
	int ATTRIBUTE = 40;
	int ACTOR_DECLARATION = 41;
	int ACTOR_DEFINITION = 42;
	int NEGATIVE_SIGN = 43;
	int POSITIVE_SIGN = 44;
	int ARITHMETIC_FACTOR = 45;
	int BOOLEAN_FACTOR = 46;
	int LOGICAL_BUFFER = 47;
	int ARITHMETIC_EXPRESSION = 48;
	int BOOLEAN_EXPRESSION = 49;
	int MULTIPORT = 50;
	int MULTIINPORT = 51;
	int MULTIOUTPORT = 52;
	int PARAM_EQUALS = 53;
	int ACTOR_EQUALS = 54;
	int SATISFIES = 55;
	int VARIABLE = 56;
	int DYNAMIC_NAME = 57;
	int ACTOR_LABEL = 58;
	int QUALIFIED_PORT = 59;
	int ESC = 60;
	int NUMBER_LITERAL = 61;
	int ATTRIBUTE_MARKER = 62;
	int STRING_LITERAL = 63;
	int WHITE_SPACE = 64;
}
