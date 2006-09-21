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
	int LBRACKET = 7;
	int RBRACKET = 8;
	int INPORT = 9;
	int OUTPORT = 10;
	int ID = 11;
	int PARAMETER = 12;
	int ACTOR = 13;
	int RELATION = 14;
	int DOT = 15;
	int ASSIGN = 16;
	int RPAREN = 17;
	int COMMA = 18;
	int EXPRESSION = 19;
	int LPAREN = 20;
	int IF = 21;
	int LCURLY = 22;
	int RCURLY = 23;
	int ELSE = 24;
	int IS = 25;
	int TRUE = 26;
	int TRUEBRANCH = 27;
	int FALSE = 28;
	int FALSEBRANCH = 29;
	int QUALID = 30;
	int ATTRIBUTE = 31;
	int ACTOR_DECLARATION = 32;
	int ACTOR_DEFINITION = 33;
	int NEGATIVE_SIGN = 34;
	int POSITIVE_SIGN = 35;
	int ARITHMETIC_FACTOR = 36;
	int BOOLEAN_FACTOR = 37;
	int LOGICAL_BUFFER = 38;
	int ARITHMETIC_EXPRESSION = 39;
	int BOOLEAN_EXPRESSION = 40;
	int MULTIPORT = 41;
	int MULTIINPORT = 42;
	int MULTIOUTPORT = 43;
	int ESC = 44;
	int NUMBER_LITERAL = 45;
	int ATTRIBUTE_MARKER = 46;
	int STRING_LITERAL = 47;
	int WHITE_SPACE = 48;
}
