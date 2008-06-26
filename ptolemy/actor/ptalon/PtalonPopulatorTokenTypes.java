// $ANTLR 2.7.7 (2006-11-01): "populator.g" -> "PtalonPopulator.java"$
/* ANTLR TreeParser that populates a PtalonActor using a PtalonEvaluator.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
	int REFERENCE = 16;
	int COLON = 17;
	int DOT = 18;
	int IMPORT = 19;
	int TRUE = 20;
	int FALSE = 21;
	int IF = 22;
	int ELSE = 23;
	int IS = 24;
	int FOR = 25;
	int INITIALLY = 26;
	int NEXT = 27;
	int DANGLING_PORTS_OKAY = 28;
	int ATTACH_DANGLING_PORTS = 29;
	int ASSIGN = 30;
	int RPAREN = 31;
	int COMMA = 32;
	int EXPRESSION = 33;
	int LPAREN = 34;
	int SEMI = 35;
	int LCURLY = 36;
	int RCURLY = 37;
	int TRANSFORM = 38;
	int PLUS = 39;
	int TRUEBRANCH = 40;
	int FALSEBRANCH = 41;
	int QUALID = 42;
	int ATTRIBUTE = 43;
	int ACTOR_DECLARATION = 44;
	int ACTOR_DEFINITION = 45;
	int TRANSFORMATION = 46;
	int NEGATIVE_SIGN = 47;
	int POSITIVE_SIGN = 48;
	int ARITHMETIC_FACTOR = 49;
	int BOOLEAN_FACTOR = 50;
	int LOGICAL_BUFFER = 51;
	int ARITHMETIC_EXPRESSION = 52;
	int BOOLEAN_EXPRESSION = 53;
	int MULTIPORT = 54;
	int MULTIINPORT = 55;
	int MULTIOUTPORT = 56;
	int PARAM_EQUALS = 57;
	int ACTOR_EQUALS = 58;
	int SATISFIES = 59;
	int VARIABLE = 60;
	int DYNAMIC_NAME = 61;
	int ACTOR_LABEL = 62;
	int QUALIFIED_PORT = 63;
	int ESC = 64;
	int NUMBER_LITERAL = 65;
	int STRING_LITERAL = 66;
	int WHITE_SPACE = 67;
	int LINE_COMMENT = 68;
	int COMMENT = 69;
}
