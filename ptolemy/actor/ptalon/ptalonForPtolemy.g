header {/* 

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
}

///////////////////////////////////////////////////////////////////
////                         Ptalon Parser                     ////

class PtalonRecognizer extends Parser;
options {
	exportVocab = Ptalon;
	k = 2;
	buildAST = true;
}

port_declaration:
	(PORT^ | INPORT^ | OUTPORT^) ID
;

parameter_declaration:
	(PARAMETER^ | INTPARAMETER^ | BOOLPARAMETER^) ID
;

relation_declaration:
	RELATION^ ID
;

qualified_identifier!
{
	String identifier = "";
}
:
	a:ID {
		identifier = identifier + a.getText();
	}
	(DOT b:ID
	{
		identifier = identifier + "." +  b.getText();
	}
	)*
	{
		#qualified_identifier = #[QUALID, identifier];
	}
;

attribute!
:
	ATTRIBUTE_MARKER! a:ID b:qualified_identifier ATTRIBUTE_MARKER!
	{
		#attribute = #([ATTRIBUTE, a.getText()], b);
	}
;

assignment:
	ID ASSIGN^ ((ID (RPAREN | COMMA)) => ID |
		((ID LPAREN) => actor_declaration | 
			((arithmetic_expression) =>
				arithmetic_expression |
				boolean_expression
			)
		)
	)
;

actor_declaration!
:
	a:ID
	{
		#a = #[ACTOR_DECLARATION, a.getText()];
		#actor_declaration = #(a);
	} 
	LPAREN! (
		b:assignment 
		{
			#actor_declaration.addChild(#b);
		}
		(COMMA! c:assignment
		{
			#actor_declaration.addChild(#c);
		}
		)*
	)? RPAREN!
;

arithmetic_factor!
{
	int sign = 1;
}
:
	(MINUS
	{
		sign = -sign;
	}
	)*
	{
		if (sign == 1) {
			#arithmetic_factor = #([ARITHMETIC_FACTOR, "arithmetic_factor"], 
				[POSITIVE_SIGN, "positive"]);
		} else {
			#arithmetic_factor = #([ARITHMETIC_FACTOR, "arithmetic_factor"], 
				[NEGATIVE_SIGN, "negative"]);
		}
	}
	(a:ID 
	{
		#arithmetic_factor.addChild(#a);
	}
	| b:NUMBER_LITERAL 
	{
		#arithmetic_factor.addChild(#b);
	}
	| LPAREN! c:arithmetic_expression RPAREN!
	{
		#arithmetic_factor.addChild(#c);
	}
	)
;

arithmetic_term:
	arithmetic_factor ((STAR^ | DIVIDE^ | MOD^) arithmetic_factor)*
;

arithmetic_expression:
	arithmetic_term ((PLUS^ | MINUS^) arithmetic_term)*
;

relational_expression:
	arithmetic_expression (
		EQUAL^ | NOT_EQUAL^ | LESS_THAN^ | GREATER_THAN^ |
		LESS_EQUAL^ | GREATER_EQUAL^
	) arithmetic_expression
;

boolean_factor!
{
	boolean sign = true;
}
:
	(LOGICAL_NOT
	{
		sign = !sign;
	}
	)* 
	{
		if (sign) {
			#boolean_factor = #([BOOLEAN_FACTOR, "boolean_factor"], 
			[LOGICAL_BUFFER, "!!"]);
		} else {
			#boolean_factor = #([BOOLEAN_FACTOR, "boolean_factor"],
			[LOGICAL_NOT, "!"]);
		}
	}
	( ( (LPAREN boolean_expression) => LPAREN! a:boolean_expression RPAREN! 
	{
		#boolean_factor.addChild(#a);	
	}
	| b:relational_expression 
    {
		#boolean_factor.addChild(#b);	
    }
	) | c:TRUE 
	{
		#boolean_factor.addChild(#c);	
	}
	| d:FALSE | 
	{
		#boolean_factor.addChild(#d);	
	}
	e:ID
	{
		#boolean_factor.addChild(#e);	
	}
	)
;

boolean_term:
	boolean_factor (LOGICAL_AND^ boolean_factor)*
;

boolean_expression:
	boolean_term (LOGICAL_OR^ boolean_term)*
;

atomic_statement : 
	((port_declaration | parameter_declaration |
	relation_declaration | actor_declaration) SEMI!) | attribute
;

conditional_statement:
	IF^ LPAREN! boolean_expression RPAREN! LCURLY! 
	(atomic_statement | conditional_statement)
	RCURLY! ELSE! LCURLY!
	(atomic_statement | conditional_statement)
	RCURLY!
;	

actor_definition!
:
	a:ID
	{
		#actor_definition = #([ACTOR_DEFINITION, a.getText()]);
	}
	IS! LCURLY! (b:atomic_statement 
	{
		#actor_definition.addChild(#b);
	}
	| c:conditional_statement
	{
		#actor_definition.addChild(#c);
	}
	)* RCURLY!

;

///////////////////////////////////////////////////////////////////
////                          Ptalon Lexer                     ////

class PtalonLexer extends Lexer;
options {
	exportVocab = Ptalon;
	testLiterals = false;
	k = 3;
}

tokens {
	PORT = "port";
	INPORT = "inport";
	OUTPORT = "outport";
	PARAMETER = "parameter";
	INTPARAMETER = "intparameter";
	OUTPARAMETER = "boolparameter";
	RELATION = "relation";
	TRUE = "true";
	FALSE = "false";
	IF = "if";
	ELSE = "else";
	IS = "is";
	QUALID;
	ATTRIBUTE;
	ACTOR_DECLARATION;
	ACTOR_DEFINITION;
	NEGATIVE_SIGN;
	POSITIVE_SIGN;
	ARITHMETIC_FACTOR;
	BOOLEAN_FACTOR;
	LOGICAL_BUFFER;
}


// Punctuation symbols
ASSIGN: ":=";

COMMA: ',';

DOT: '.';

LBRACKET: '[';

LCURLY: '{';

LPAREN: '(';

RBRACKET: ']';

RCURLY: '}';

RPAREN: ')';

SEMI: ';';

// Operators

LOGICAL_OR: "||";

LOGICAL_AND: "&&";

EQUAL: "==";

NOT_EQUAL: "!=";

LESS_THAN: '<';

GREATER_THAN: '>';

LESS_EQUAL: "<=";

GREATER_EQUAL: ">=";

PLUS: '+';

MINUS: '-';

STAR: '*';

DIVIDE: '/';

MOD: '%';

BINARY_NOT: '~';

LOGICAL_NOT: '!';

// Escape sequence
ESC:
	'\\' ('n' | 'r' | 't' | 'b' | 'f' | '"' | '\'')
;

// An identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
ID options { testLiterals=true; } :
	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
;

// Number literals
NUMBER_LITERAL:
	('0'..'9')+ ('.' ('0'..'9')+)?
;

ATTRIBUTE_MARKER :
	'$'
;

// String literals
STRING_LITERAL :
	'"' (ESC | ~('"'|'\\'|'\n'|'\r'))* '"'
;
	
// Whitespace -- ignored
WHITE_SPACE :
	(
		' '
		| '\t'
		| '\f'
		| '\r' '\n' { newline(); }
		| '\r' { newline(); }
		| '\n' { newline(); }
	)
	{ $setType(Token.SKIP); }
;
