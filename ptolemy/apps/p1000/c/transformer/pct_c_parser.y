%{
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "pct_util.h"

void check_type_name_declaration(struct AST_NODE* declaration);
int c_error(char* const error);

extern int c_lex();
%}

%token IDENTIFIER CONSTANT STRING_LITERAL SIZEOF
%token PTR_OP INC_OP DEC_OP LEFT_OP RIGHT_OP LE_OP GE_OP EQ_OP NE_OP
%token AND_OP OR_OP MUL_ASSIGN DIV_ASSIGN MOD_ASSIGN ADD_ASSIGN
%token SUB_ASSIGN LEFT_ASSIGN RIGHT_ASSIGN AND_ASSIGN
%token XOR_ASSIGN OR_ASSIGN

%token TYPEDEF EXTERN STATIC AUTO REGISTER
%token CHAR SHORT INT LONG SIGNED UNSIGNED FLOAT DOUBLE CONST VOLATILE VOID
%token STRUCT UNION ENUM ELLIPSIS

%token CASE DEFAULT IF ELSE SWITCH WHILE DO FOR GOTO CONTINUE BREAK RETURN

/* Some extra tokens */
%token LEXICAL_TOKEN AMBIGUOUS_TYPE_NAME
%token COMPLEX IMAGINARY RESTRICT BOOLEAN INLINE
%token ARRAY_ACCESS ARGUMENT_EXPRESSION_LIST
%token PARAMETERIZED_EXPRESSION FIELD_ACCESS INC_DEC_EXPRESSION STRUCT_CONSTANT
	UNARY_EXPRESSION SIZEOF_EXPRESSION CAST_EXPRESSION BINARY_EXPRESSION
	CONDITIONAL_EXPRESSION COMMA_EXPRESSION
%token DECLARATION STORAGE_CLASS_SPECIFIER STRUCT_OR_UNION STRUCT_DECLARATION
	STRUCT_DECLARATION_LIST PRIMITIVE_TYPE_SPECIFIER NAMED_TYPE_SPECIFIER
	DECLARATION_SPECIFIERS INIT_DECLARATOR_LIST INIT_DECLARATOR DECLARATOR
	SPECIFIER_QUALIFIER_LIST STRUCT_DECLARATOR_LIST STRUCT_DECLARATOR
	ENUM_SPECIFIER ENUMERATOR_LIST ENUMERATOR TYPE_QUALIFIER FUNCTION_SPECIFIER
	POINTER TYPE_QUALIFIER_LIST NAMED_DIRECT_DECLARATOR ARRAY_DIRECT_DECLARATOR
	PARAMETER_LIST PARAMETERIZED_DIRECT_DECLARATOR PARAMETER_DECLARATION
	IDENTIFIER_LIST TYPE_NAME ABSTRACT_DECLARATOR
	ARRAY_DIRECT_ABSTRACT_DECLARATOR PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR
	INITIALIZER_LIST DESIGNATOR_LIST ARRAY_DESIGNATOR DOT_DESIGNATOR
%token IDENTIFIER_LABELED_STATEMENT CASE_LABELED_STATEMENT BLOCK_ITEM_LIST
	EXPRESSION_STATEMENT IF_STATEMENT SWITCH_STATEMENT WHILE_STATEMENT
	DO_WHILE_STATEMENT FOR_STATEMENT GOTO_STATEMENT LOOP_JUMP_STATEMENT
	RETURN_STATEMENT TRANSLATION_UNIT
%token FUNCTION_DEFINITION DECLARATION_LIST
%token COMPOUND_STATEMENT

%start translation_unit
%name-prefix="c_"

%%

string_literal	/* GCC extension */
	: STRING_LITERAL { $$ = $1; }
	| string_literal STRING_LITERAL {
		$$ = $1;
		$$->string_literal.text = (char*) realloc($$->string_literal.text,
			strlen($$->string_literal.text) + strlen($2->string_literal.text)
					- 1);
		strcpy($$->string_literal.text + strlen($$->string_literal.text) - 1,
			$2->string_literal.text + 1);
		move_and_append_comments($$, $2);
		free($2->string_literal.text);
		free($2);
	}

primary_expression
	: IDENTIFIER { $$ = $1; }
	| CONSTANT { $$ = $1; }
	| string_literal { $$ = $1; }
	| '(' expression ')' {
		$$ = $2;
		move_and_prepend_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	;

postfix_expression
	: primary_expression { $$ = $1; }
	| postfix_expression '[' expression ']' {
		$$ = create_node(ARRAY_ACCESS, $1);
		$$->array_access = (ARRAY_ACCESS_STRUCT) { $1, $3 };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| postfix_expression '(' ')' {
		$$ = create_node(PARAMETERIZED_EXPRESSION, $1);
		$$->parameterized_expression = (PARAMETERIZED_EXPRESSION_STRUCT) {
			$1, NULL
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
	}
	| postfix_expression '(' argument_expression_list ')' {
		$$ = create_node(PARAMETERIZED_EXPRESSION, $1);
		$$->parameterized_expression = (PARAMETERIZED_EXPRESSION_STRUCT) {
			$1, $3
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| postfix_expression '.' IDENTIFIER {
		$$ = create_node(FIELD_ACCESS, $1);
		$$->field_access = (FIELD_ACCESS_STRUCT) { 0, $1, $3->identifier.id };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
	}
	| postfix_expression PTR_OP IDENTIFIER {
		$$ = create_node(FIELD_ACCESS, $1);
		$$->field_access = (FIELD_ACCESS_STRUCT) { 1, $1, $3->identifier.id };
		move_and_append_comments($1, $2);
		free($2);
	}
	| postfix_expression INC_OP {
		$$ = create_node(INC_DEC_EXPRESSION, $1);
		$$->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) {
			POSTFIX_INC, $1
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| postfix_expression DEC_OP {
		$$ = create_node(INC_DEC_EXPRESSION, $1);
		$$->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) {
			POSTFIX_DEC, $1
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| '(' type_name ')' '{' initializer_list '}' {
		$$ = create_node(STRUCT_CONSTANT, $1);
		$$->struct_constant = (STRUCT_CONSTANT_STRUCT) { $2, $5 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
		move_and_append_comments($2, $4);
		free($4);
		move_and_append_comments($5, $6);
		free($6);
	}
	| '(' type_name ')' '{' initializer_list ',' '}' {
		$$ = create_node(STRUCT_CONSTANT, $1);
		$$->struct_constant = (STRUCT_CONSTANT_STRUCT) { $2, $5 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
		move_and_append_comments($2, $4);
		free($4);
		move_and_append_comments($5, $6);
		free($6);
		move_and_append_comments($5, $7);
		free($7);
	}
	;

argument_expression_list
	: assignment_expression {
		$$ = create_node(ARGUMENT_EXPRESSION_LIST, $1);
		$$->argument_expression_list = (ARGUMENT_EXPRESSION_LIST_STRUCT) {
			NULL, $1
		};
	}
	| argument_expression_list ',' assignment_expression {
		$$ = create_node(ARGUMENT_EXPRESSION_LIST, $1);
		$$->argument_expression_list = (ARGUMENT_EXPRESSION_LIST_STRUCT) {
			$1, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

unary_expression
	: postfix_expression { $$ = $1; }
	| INC_OP unary_expression {
		$$ = create_node(INC_DEC_EXPRESSION, $1);
		$$->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) { PREFIX_INC, $2 };
		move_and_append_comments($$, $1);
		free($1);
	}
	| DEC_OP unary_expression {
		$$ = create_node(INC_DEC_EXPRESSION, $1);
		$$->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) { PREFIX_DEC, $2 };
		move_and_append_comments($$, $1);
		free($1);
	}
	| unary_operator cast_expression {
		$$ = create_node(UNARY_EXPRESSION, $1);
		$$->unary_expression = (UNARY_EXPRESSION_STRUCT) {
			$1->lexical_token, $2
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| SIZEOF unary_expression {
		$$ = create_node(SIZEOF_EXPRESSION, $1);
		$$->sizeof_expression = (SIZEOF_EXPRESSION_STRUCT) { 0, $2 };
		move_and_append_comments($$, $1);
		free($1);
	}
	| SIZEOF '(' type_name ')' {
		$$ = create_node(SIZEOF_EXPRESSION, $1);
		$$->sizeof_expression = (SIZEOF_EXPRESSION_STRUCT) { 1, $3 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	;

unary_operator
	: '&' { $$ = $1; }
	| '*' { $$ = $1; }
	| '+' { $$ = $1; }
	| '-' { $$ = $1; }
	| '~' { $$ = $1; }
	| '!' { $$ = $1; }
	;

cast_expression
	: unary_expression { $$ = $1; }
	| '(' type_name ')' cast_expression {
		$$ = create_node(CAST_EXPRESSION, $1);
		$$->cast_expression = (CAST_EXPRESSION_STRUCT) { $2, $4 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	;

multiplicative_expression
	: cast_expression { $$ = $1; }
	| multiplicative_expression '*' cast_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| multiplicative_expression '/' cast_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3 
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| multiplicative_expression '%' cast_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

additive_expression
	: multiplicative_expression { $$ = $1; }
	| additive_expression '+' multiplicative_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| additive_expression '-' multiplicative_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

shift_expression
	: additive_expression { $$ = $1; }
	| shift_expression LEFT_OP additive_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| shift_expression RIGHT_OP additive_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

relational_expression
	: shift_expression { $$ = $1; }
	| relational_expression '<' shift_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| relational_expression '>' shift_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| relational_expression LE_OP shift_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| relational_expression GE_OP shift_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

equality_expression
	: relational_expression { $$ = $1; }
	| equality_expression EQ_OP relational_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	| equality_expression NE_OP relational_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

and_expression
	: equality_expression { $$ = $1; }
	| and_expression '&' equality_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

exclusive_or_expression
	: and_expression { $$ = $1; }
	| exclusive_or_expression '^' and_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

inclusive_or_expression
	: exclusive_or_expression { $$ = $1; }
	| inclusive_or_expression '|' exclusive_or_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

logical_and_expression
	: inclusive_or_expression { $$ = $1; }
	| logical_and_expression AND_OP inclusive_or_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

logical_or_expression
	: logical_and_expression { $$ = $1; }
	| logical_or_expression OR_OP logical_and_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

conditional_expression
	: logical_or_expression { $$ = $1; }
	| logical_or_expression '?' expression ':' conditional_expression {
		$$ = create_node(CONDITIONAL_EXPRESSION, $1);
		$$->conditional_expression = (CONDITIONAL_EXPRESSION_STRUCT) {
			$1, $3, $5
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	;

assignment_expression
	: conditional_expression { $$ = $1; }
	| unary_expression assignment_operator assignment_expression {
		$$ = create_node(BINARY_EXPRESSION, $1);
		$$->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			$1, $2->lexical_token, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

assignment_operator
	: '=' { $$ = $1; }
	| MUL_ASSIGN { $$ = $1; }
	| DIV_ASSIGN { $$ = $1; }
	| MOD_ASSIGN { $$ = $1; }
	| ADD_ASSIGN { $$ = $1; }
	| SUB_ASSIGN { $$ = $1; }
	| LEFT_ASSIGN { $$ = $1; }
	| RIGHT_ASSIGN { $$ = $1; }
	| AND_ASSIGN { $$ = $1; }
	| XOR_ASSIGN { $$ = $1; }
	| OR_ASSIGN { $$ = $1; }
	;

expression
	: assignment_expression { $$ = $1; }
	| expression ',' assignment_expression {
		$$ = create_node(COMMA_EXPRESSION, $1);
		$$->comma_expression = (COMMA_EXPRESSION_STRUCT) { $1, $3 };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

constant_expression
	: conditional_expression { $$ = $1; }
	;

declaration
	: declaration_specifiers ';' {
		$$ = create_node(DECLARATION, $1);
		$$->declaration = (DECLARATION_STRUCT) { $1, NULL };
		move_and_append_comments($1, $2);
		free($2);
	}
	| declaration_specifiers init_declarator_list ';' {
		$$ = create_node(DECLARATION, $1);
		$$->declaration = (DECLARATION_STRUCT) { $1, $2 };
		check_type_name_declaration($$);
		move_and_append_comments($2, $3);
		free($3);
	}
	;

declaration_specifiers
	: storage_class_specifier {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			$1, NULL
		};
	}
	| storage_class_specifier declaration_specifiers {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { $1, $2 };
	}
	| type_specifier {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			$1, NULL
		};
	}
	| type_specifier declaration_specifiers {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { $1, $2 };
	}
	| type_qualifier {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			$1, NULL
		};
	}
	| type_qualifier declaration_specifiers {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { $1, $2 };
	}
	| function_specifier {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			$1, NULL
		};
	}
	| function_specifier declaration_specifiers {
		$$ = create_node(DECLARATION_SPECIFIERS, $1);
		$$->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { $1, $2 };
	}
	;

init_declarator_list
	: init_declarator {
		$$ = create_node(INIT_DECLARATOR_LIST, $1);
		$$->init_declarator_list = (INIT_DECLARATOR_LIST_STRUCT) { NULL, $1 };
	}
	| init_declarator_list ',' init_declarator {
		$$ = create_node(INIT_DECLARATOR_LIST, $1);
		$$->init_declarator_list = (INIT_DECLARATOR_LIST_STRUCT) { $1, $3 };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

init_declarator
	: declarator {
		$$ = create_node(INIT_DECLARATOR, $1);
		$$->init_declarator = (INIT_DECLARATOR_STRUCT) { $1, NULL };
	}
	| declarator '=' initializer {
		$$ = create_node(INIT_DECLARATOR, $1);
		$$->init_declarator = (INIT_DECLARATOR_STRUCT) { $1, $3 };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

storage_class_specifier
	: TYPEDEF {
		$$ = create_node(STORAGE_CLASS_SPECIFIER, $1);
		$$->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| EXTERN {
		$$ = create_node(STORAGE_CLASS_SPECIFIER, $1);
		$$->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| STATIC {
		$$ = create_node(STORAGE_CLASS_SPECIFIER, $1);
		$$->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| AUTO {
		$$ = create_node(STORAGE_CLASS_SPECIFIER, $1);
		$$->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| REGISTER {
		$$ = create_node(STORAGE_CLASS_SPECIFIER, $1);
		$$->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	;

type_specifier
	: VOID {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| CHAR {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| SHORT {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| INT {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| LONG {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| FLOAT {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| DOUBLE {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| SIGNED {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| UNSIGNED {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| BOOLEAN {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| COMPLEX {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| IMAGINARY {
		$$ = create_node(PRIMITIVE_TYPE_SPECIFIER, $1);
		$$->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| struct_or_union_specifier { $$ = $1; }
	| enum_specifier { $$ = $1; }
	| AMBIGUOUS_TYPE_NAME {
		$$ = create_node(NAMED_TYPE_SPECIFIER, $1);
		$$->named_type_specifier = (NAMED_TYPE_SPECIFIER_STRUCT) {
			$1->identifier.id
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	;

struct_or_union_specifier
	: struct_or_union IDENTIFIER '{' struct_declaration_list '}' {
		$$ = create_node(STRUCT_OR_UNION, $1);
		$$->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			$1->lexical_token == STRUCT, $2->identifier.id, $4
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($$, $3);
		free($3);
		move_and_append_comments($4, $5);
		free($5);
	}
	| struct_or_union '{' struct_declaration_list '}' {
		$$ = create_node(STRUCT_OR_UNION, $1);
		$$->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			$1->lexical_token == STRUCT, NULL, $3
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| struct_or_union IDENTIFIER {
		$$ = create_node(STRUCT_OR_UNION, $1);
		$$->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			$1->lexical_token == STRUCT, $2->identifier.id, NULL
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| struct_or_union AMBIGUOUS_TYPE_NAME {
		$$ = create_node(STRUCT_OR_UNION, $1);
		$$->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			$1->lexical_token == STRUCT, $2->identifier.id, NULL
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	;

struct_or_union
	: STRUCT { $$ = $1; }
	| UNION { $$ = $1; }
	;

struct_declaration_list
	: struct_declaration {
		$$ = create_node(STRUCT_DECLARATION_LIST, $1);
		$$->struct_declaration_list = (STRUCT_DECLARATION_LIST_STRUCT) {
			NULL, $1
		};
	}
	| struct_declaration_list struct_declaration {
		$$ = create_node(STRUCT_DECLARATION_LIST, $1);
		$$->struct_declaration_list = (STRUCT_DECLARATION_LIST_STRUCT) {
			$1, $2
		};
	}
	;

struct_declaration
	: specifier_qualifier_list struct_declarator_list ';' {
		$$ = create_node(STRUCT_DECLARATION, $1);
		$$->struct_declaration = (STRUCT_DECLARATION_STRUCT) { $1, $2 };
		move_and_append_comments($2, $3);
		free($3);
	}
	| specifier_qualifier_list ';' {	/* GCC extension */
		$$ = create_node(STRUCT_DECLARATION, $1);
		$$->struct_declaration = (STRUCT_DECLARATION_STRUCT) { $1, NULL };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

specifier_qualifier_list
	: type_specifier specifier_qualifier_list {
		$$ = create_node(SPECIFIER_QUALIFIER_LIST, $1);
		$$->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			$1, $2
		};
	}
	| type_specifier {
		$$ = create_node(SPECIFIER_QUALIFIER_LIST, $1);
		$$->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			$1, NULL
		};
	}
	| type_qualifier specifier_qualifier_list {
		$$ = create_node(SPECIFIER_QUALIFIER_LIST, $1);
		$$->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			$1, $2
		};
	}
	| type_qualifier {
		$$ = create_node(SPECIFIER_QUALIFIER_LIST, $1);
		$$->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			$1, NULL
		};
	}
	;

struct_declarator_list
	: struct_declarator {
		$$ = create_node(STRUCT_DECLARATOR_LIST, $1);
		$$->struct_declarator_list = (STRUCT_DECLARATOR_LIST_STRUCT) {
			$1, NULL
		};
	}
	| struct_declarator_list ',' struct_declarator {
		$$ = create_node(STRUCT_DECLARATOR_LIST, $1);
		$$->struct_declarator_list = (STRUCT_DECLARATOR_LIST_STRUCT) {
			$1, $3
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

struct_declarator
	: declarator {
		$$ = create_node(STRUCT_DECLARATOR, $1);
		$$->struct_declarator = (STRUCT_DECLARATOR_STRUCT) { $1, NULL };
	}
	| ':' constant_expression {
		$$ = create_node(STRUCT_DECLARATOR, $1);
		$$->struct_declarator = (STRUCT_DECLARATOR_STRUCT) { NULL, $2 };
		move_and_append_comments($$, $1);
		free($1);
	}
	| declarator ':' constant_expression {
		$$ = create_node(STRUCT_DECLARATOR, $1);
		$$->struct_declarator = (STRUCT_DECLARATOR_STRUCT) { $1, $3 };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

enum_specifier
	: ENUM '{' enumerator_list '}' {
		$$ = create_node(ENUM_SPECIFIER, $1);
		$$->enum_specifier = (ENUM_SPECIFIER_STRUCT) { NULL, $3 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| ENUM IDENTIFIER '{' enumerator_list '}' {
		$$ = create_node(ENUM_SPECIFIER, $1);
		$$->enum_specifier = (ENUM_SPECIFIER_STRUCT) { $2->identifier.id, $4 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($$, $3);
		free($3);
		move_and_append_comments($4, $5);
		free($5);
	}
	| ENUM '{' enumerator_list ',' '}' {
		$$ = create_node(ENUM_SPECIFIER, $1);
		$$->enum_specifier = (ENUM_SPECIFIER_STRUCT) { NULL, $3 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
		move_and_append_comments($3, $5);
		free($5);
	}
	| ENUM IDENTIFIER '{' enumerator_list ',' '}' {
		$$ = create_node(ENUM_SPECIFIER, $1);
		$$->enum_specifier = (ENUM_SPECIFIER_STRUCT) { $2->identifier.id, $4 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($$, $3);
		free($3);
		move_and_append_comments($4, $5);
		free($5);
		move_and_append_comments($4, $6);
		free($6);
	}
	| ENUM IDENTIFIER {
		$$ = create_node(ENUM_SPECIFIER, $1);
		$$->enum_specifier = (ENUM_SPECIFIER_STRUCT) {
			$2->identifier.id, NULL
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| ENUM AMBIGUOUS_TYPE_NAME {
		$$ = create_node(ENUM_SPECIFIER, $1);
		$$->enum_specifier = (ENUM_SPECIFIER_STRUCT) {
			$2->identifier.id, NULL
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	;

enumerator_list
	: enumerator {
		$$ = create_node(ENUMERATOR_LIST, $1);
		$$->enumerator_list = (ENUMERATOR_LIST_STRUCT) { NULL, $1 };
	}
	| enumerator_list ',' enumerator {
		$$ = create_node(ENUMERATOR_LIST, $1);
		$$->enumerator_list = (ENUMERATOR_LIST_STRUCT) { $1, $3 };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

enumerator
	: IDENTIFIER {
		$$ = create_node(ENUMERATOR, $1);
		$$->enumerator = (ENUMERATOR_STRUCT) { $1->identifier.id, NULL };
		move_and_append_comments($$, $1);
		free($1);
	}
	| IDENTIFIER '=' constant_expression {
		$$ = create_node(ENUMERATOR, $1);
		$$->enumerator = (ENUMERATOR_STRUCT) { $1->identifier.id, $3 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	;

type_qualifier
	: CONST {
		$$ = create_node(TYPE_QUALIFIER, $1);
		$$->type_qualifier = (TYPE_QUALIFIER_STRUCT) { $1->lexical_token };
		move_and_append_comments($$, $1);
		free($1);
	}
	| RESTRICT {
		$$ = create_node(TYPE_QUALIFIER, $1);
		$$->type_qualifier = (TYPE_QUALIFIER_STRUCT) { $1->lexical_token };
		move_and_append_comments($$, $1);
		free($1);
	}
	| VOLATILE {
		$$ = create_node(TYPE_QUALIFIER, $1);
		$$->type_qualifier = (TYPE_QUALIFIER_STRUCT) { $1->lexical_token };
		move_and_append_comments($$, $1);
		free($1);
	}
	;

function_specifier
	: INLINE {
		$$ = create_node(FUNCTION_SPECIFIER, $1);
		$$->function_specifier = (FUNCTION_SPECIFIER_STRUCT) {
			$1->lexical_token
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	;

declarator
	: pointer direct_declarator {
		$$ = create_node(DECLARATOR, $1);
		$$->declarator = (DECLARATOR_STRUCT) { $1, $2 };
	}
	| direct_declarator {
		if ($1->type == DECLARATOR)
			$$ = $1;
		else {
			$$ = create_node(DECLARATOR, $1);
			$$->declarator = (DECLARATOR_STRUCT) { NULL, $1 };
		}
	}
	;

direct_declarator
	: IDENTIFIER {
		$$ = create_node(NAMED_DIRECT_DECLARATOR, $1);
		$$->named_direct_declarator = (NAMED_DIRECT_DECLARATOR_STRUCT) {
			$1->identifier.id
		};
		move_and_append_comments($$, $1);
		free($1);
	}
	| '(' declarator ')' {
		$$ = $2;
		move_and_prepend_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	| direct_declarator '[' type_qualifier_list assignment_expression ']' {
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 0, $3, 0, $4, 0
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($4, $5);
		free($5);
	}
	| direct_declarator '[' type_qualifier_list ']' {
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 0, $3, 0, NULL, 0
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| direct_declarator '[' assignment_expression ']' {
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 0, NULL, 0, $3, 0
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| direct_declarator '[' STATIC type_qualifier_list assignment_expression ']'
	{
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 1, $4, 0, $5, 0
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
		move_and_append_comments($5, $6);
		free($6);
	}
	| direct_declarator '[' type_qualifier_list STATIC assignment_expression ']'
	{
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 0, $3, 1, $5, 0
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
		move_and_append_comments($5, $6);
		free($6);
	}
	| direct_declarator '[' type_qualifier_list '*' ']' {
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 0, $3, 0, NULL, 1
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
		move_and_append_comments($3, $5);
		free($5);
	}
	| direct_declarator '[' '*' ']' {
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 0, NULL, 0, NULL, 1
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
		move_and_append_comments($1, $4);
		free($4);
	}
	| direct_declarator '[' ']' {
		$$ = create_node(ARRAY_DIRECT_DECLARATOR, $1);
		$$->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			$1, 0, NULL, 0, NULL, 0
		};
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
	}
	| direct_declarator '(' parameter_type_list ')' {
		$$ = create_node(PARAMETERIZED_DIRECT_DECLARATOR, $1);
		$$->parameterized_direct_declarator =
			(PARAMETERIZED_DIRECT_DECLARATOR_STRUCT) { $1, $3, NULL };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| direct_declarator '(' identifier_list ')' {
		$$ = create_node(PARAMETERIZED_DIRECT_DECLARATOR, $1);
		$$->parameterized_direct_declarator =
			(PARAMETERIZED_DIRECT_DECLARATOR_STRUCT) { $1, NULL, $3 };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| direct_declarator '(' ')' {
		$$ = create_node(PARAMETERIZED_DIRECT_DECLARATOR, $1);
		$$->parameterized_direct_declarator =
			(PARAMETERIZED_DIRECT_DECLARATOR_STRUCT) { $1, NULL, NULL };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
	}
	;

pointer
	: '*' {
		$$ = create_node(POINTER, $1);
		$$->pointer = (POINTER_STRUCT) { NULL, NULL };
		move_and_append_comments($$, $1);
		free($1);
	}
	| '*' type_qualifier_list {
		$$ = create_node(POINTER, $1);
		$$->pointer = (POINTER_STRUCT) { $2, NULL };
		move_and_append_comments($$, $1);
		free($1);
	}
	| '*' pointer {
		$$ = create_node(POINTER, $1);
		$$->pointer = (POINTER_STRUCT) { NULL, $2 };
		move_and_append_comments($$, $1);
		free($1);
	}
	| '*' type_qualifier_list pointer {
		$$ = create_node(POINTER, $1);
		$$->pointer = (POINTER_STRUCT) { $2, $3 };
		move_and_append_comments($$, $1);
		free($1);
	}
	;

type_qualifier_list
	: type_qualifier {
		$$ = create_node(TYPE_QUALIFIER_LIST, $1);
		$$->type_qualifier_list = (TYPE_QUALIFIER_LIST_STRUCT) { NULL, $1 };
	};
	| type_qualifier_list type_qualifier {
		$$ = create_node(TYPE_QUALIFIER_LIST, $1);
		$$->type_qualifier_list = (TYPE_QUALIFIER_LIST_STRUCT) { $1, $2 };
	};
	;

parameter_type_list
	: parameter_list { $$ = $1; };
	| parameter_list ',' ELLIPSIS {
		$$ = $1;
		$$->parameter_list.has_ellipsis = 1;
	}
	;

parameter_list
	: parameter_declaration {
		$$ = create_node(PARAMETER_LIST, $1);
		$$->parameter_list = (PARAMETER_LIST_STRUCT) { NULL, $1, 0 };
	}
	| parameter_list ',' parameter_declaration {
		$$ = create_node(PARAMETER_LIST, $1);
		$$->parameter_list = (PARAMETER_LIST_STRUCT) { $1, $3, 0 };
		free($2);
	}
	;

parameter_declaration
	: declaration_specifiers declarator {
		$$ = create_node(PARAMETER_DECLARATION, $1);
		$$->parameter_declaration = (PARAMETER_DECLARATION_STRUCT) {
			$1, $2, NULL
		};
	}
	| declaration_specifiers abstract_declarator {
		$$ = create_node(PARAMETER_DECLARATION, $1);
		$$->parameter_declaration = (PARAMETER_DECLARATION_STRUCT) {
			$1, NULL, $2
		};
	}
	| declaration_specifiers {
		$$ = create_node(PARAMETER_DECLARATION, $1);
		$$->parameter_declaration = (PARAMETER_DECLARATION_STRUCT) {
			$1, NULL, NULL
		};
	}
	;

identifier_list
	: IDENTIFIER {
		$$ = create_node(IDENTIFIER_LIST, $1);
		$$->identifier_list = (IDENTIFIER_LIST_STRUCT) {
			NULL, $1->identifier.id
		};
	}
	| identifier_list ',' IDENTIFIER {
		$$ = create_node(IDENTIFIER_LIST, $1);
		$$->identifier_list = (IDENTIFIER_LIST_STRUCT) {
			$1, $3->identifier.id
		};
		move_and_append_comments($1, $2);
		free($2);
	}
	;

type_name
	: specifier_qualifier_list {
		$$ = create_node(TYPE_NAME, $1);
		$$->type_name = (TYPE_NAME_STRUCT) { $1, NULL };
	}
	| specifier_qualifier_list abstract_declarator {
		$$ = create_node(TYPE_NAME, $1);
		$$->type_name = (TYPE_NAME_STRUCT) { $1, $2 };
	}
	;

abstract_declarator
	: pointer {
		$$ = create_node(ABSTRACT_DECLARATOR, $1);
		$$->abstract_declarator = (ABSTRACT_DECLARATOR_STRUCT) { $1, NULL };
	}
	| direct_abstract_declarator {
		$$ = create_node(ABSTRACT_DECLARATOR, $1);
		$$->abstract_declarator = (ABSTRACT_DECLARATOR_STRUCT) { NULL, $1 };
	}
	| pointer direct_abstract_declarator {
		$$ = create_node(ABSTRACT_DECLARATOR, $1);
		$$->abstract_declarator = (ABSTRACT_DECLARATOR_STRUCT) { $1, $2 };
	}
	;

direct_abstract_declarator
	: '(' abstract_declarator ')' {
		$$ = $2;
		move_and_prepend_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	| '[' ']' {
		$$ = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, NULL, 0 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| '[' assignment_expression ']' {
		$$ = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, $2, 0 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	| direct_abstract_declarator '[' ']' {
		$$ = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { $1, NULL, 0 };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
	}
	| direct_abstract_declarator '[' assignment_expression ']' {
		$$ = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { $1, $3, 0 };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| '[' '*' ']' {
		$$ = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, NULL, 1 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($$, $3);
		free($3);
	}
	| direct_abstract_declarator '[' '*' ']' {
		$$ = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { $1, NULL, 1 };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
		move_and_append_comments($1, $4);
		free($4);
	}
	| '(' ')' {
		$$ = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, NULL };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| '(' parameter_type_list ')' {
		$$ = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, $2 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	| direct_abstract_declarator '(' ')' {
		$$ = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { $1, NULL };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($1, $3);
		free($3);
	}
	| direct_abstract_declarator '(' parameter_type_list ')' {
		$$ = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, $1);
		$$->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { $1, $3 };
		move_and_append_comments($1, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	;

initializer
	: assignment_expression { $$ = $1; }
	| '{' initializer_list '}' {
		$$ = $2;
		move_and_prepend_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	| '{' initializer_list ',' '}' {
		$$ = $2;
		move_and_prepend_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
		move_and_append_comments($2, $4);
		free($4);
	}
	;

initializer_list
	: initializer {
		$$ = create_node(INITIALIZER_LIST, $1);
		$$->initializer_list = (INITIALIZER_LIST_STRUCT) { NULL, NULL, $1 };
	}
	| designation initializer {
		$$ = create_node(INITIALIZER_LIST, $1);
		$$->initializer_list = (INITIALIZER_LIST_STRUCT) { NULL, $1, $2 };
	}
	| initializer_list ',' initializer {
		$$ = create_node(INITIALIZER_LIST, $1);
		$$->initializer_list = (INITIALIZER_LIST_STRUCT) { $1, NULL, $3 };
		move_and_append_comments($1, $2);
		free($2);
	}
	| initializer_list ',' designation initializer {
		$$ = create_node(INITIALIZER_LIST, $1);
		$$->initializer_list = (INITIALIZER_LIST_STRUCT) { $1, $3, $4 };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

designation
	: designator_list '=' {
		$$ = $1;
		move_and_append_comments($1, $2);
		free($2);
	}
	;

designator_list
	: designator {
		$$ = create_node(DESIGNATOR_LIST, $1);
		$$->designator_list = (DESIGNATOR_LIST_STRUCT) { NULL, $1 };
	}
	| designator_list designator {
		$$ = create_node(DESIGNATOR_LIST, $1);
		$$->designator_list = (DESIGNATOR_LIST_STRUCT) { $1, $2 };
	}
	;

designator
	: '[' constant_expression ']' {
		$$ = create_node(ARRAY_DESIGNATOR, $1);
		$$->array_designator = (ARRAY_DESIGNATOR_STRUCT) { $2 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	| '.' IDENTIFIER {
		$$ = create_node(DOT_DESIGNATOR, $1);
		$$->dot_designator = (DOT_DESIGNATOR_STRUCT) { $2->identifier.id };
		move_and_append_comments($$, $1);
		free($1);
	}
	;

statement
	: labeled_statement { $$ = $1; }
	| compound_statement { $$ = $1; }
	| expression_statement { $$ = $1; }
	| selection_statement { $$ = $1; }
	| iteration_statement { $$ = $1; }
	| jump_statement { $$ = $1; }
	;

labeled_statement
	: IDENTIFIER ':' statement {
		$$ = create_node(IDENTIFIER_LABELED_STATEMENT, $1);
		$$->identifier_labeled_statement =
			(IDENTIFIER_LABELED_STATEMENT_STRUCT) { $1->identifier.id, $3 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| CASE constant_expression ':' statement {
		$$ = create_node(CASE_LABELED_STATEMENT, $1);
		$$->case_labeled_statement = (CASE_LABELED_STATEMENT_STRUCT) {
			0, $2, $4
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	| DEFAULT ':' statement {
		$$ = create_node(CASE_LABELED_STATEMENT, $1);
		$$->case_labeled_statement = (CASE_LABELED_STATEMENT_STRUCT) {
			1, NULL, $3
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	;

compound_statement
	: '{' '}' {
		$$ = create_node(COMPOUND_STATEMENT, $1);
		$$->compound_statement = (COMPOUND_STATEMENT_STRUCT) { NULL };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| '{' block_item_list '}' {
		$$ = create_node(COMPOUND_STATEMENT, $1);
		$$->compound_statement = (COMPOUND_STATEMENT_STRUCT) { $2 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
	}
	;

block_item_list
	: block_item {
		$$ = create_node(BLOCK_ITEM_LIST, $1);
		$$->block_item_list = (BLOCK_ITEM_LIST_STRUCT) { NULL, $1 };
	}
	| block_item_list block_item {
		$$ = create_node(BLOCK_ITEM_LIST, $1);
		$$->block_item_list = (BLOCK_ITEM_LIST_STRUCT) { $1, $2 };
	}
	;

block_item
	: declaration { $$ = $1; }
	| statement { $$ = $1; }
	;

expression_statement
	: ';' {
		$$ = create_node(EXPRESSION_STATEMENT, $1);
		$$->expression_statement = (EXPRESSION_STATEMENT_STRUCT) { NULL };
		move_and_append_comments($$, $1);
		free($1);
	}
	| expression ';'  {
		$$ = create_node(EXPRESSION_STATEMENT, $1);
		$$->expression_statement = (EXPRESSION_STATEMENT_STRUCT) { $1 };
		move_and_append_comments($1, $2);
		free($2);
	}
	;

selection_statement
	: IF '(' expression ')' statement {
		$$ = create_node(IF_STATEMENT, $1);
		$$->if_statement = (IF_STATEMENT_STRUCT) { $3, $5, NULL };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| IF '(' expression ')' statement ELSE statement {
		$$ = create_node(IF_STATEMENT, $1);
		$$->if_statement = (IF_STATEMENT_STRUCT) { $3, $5, $7 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
		move_and_append_comments($5, $6);
		free($6);
	}
	| SWITCH '(' expression ')' statement {
		$$ = create_node(SWITCH_STATEMENT, $1);
		$$->switch_statement = (SWITCH_STATEMENT_STRUCT) { $3, $5 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	;

iteration_statement
	: WHILE '(' expression ')' statement {
		$$ = create_node(WHILE_STATEMENT, $1);
		$$->while_statement = (WHILE_STATEMENT_STRUCT) { $3, $5 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($3, $4);
		free($4);
	}
	| DO statement WHILE '(' expression ')' ';' {
		$$ = create_node(DO_WHILE_STATEMENT, $1);
		$$->do_while_statement = (DO_WHILE_STATEMENT_STRUCT) { $2, $5 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($2, $3);
		free($3);
		move_and_append_comments($2, $4);
		free($4);
		move_and_append_comments($5, $6);
		free($6);
		move_and_append_comments($5, $7);
		free($7);
	}
	| FOR '(' expression_statement expression_statement ')' statement {
		$$ = create_node(FOR_STATEMENT, $1);
		$$->for_statement = (FOR_STATEMENT_STRUCT) { $3, $4, NULL, $6 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($4, $5);
		free($5);
	}
	| FOR '(' expression_statement expression_statement expression ')' statement
	{
		$$ = create_node(FOR_STATEMENT, $1);
		$$->for_statement = (FOR_STATEMENT_STRUCT) { $3, $4, $5, $7 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($5, $6);
		free($6);
	}
	| FOR '(' declaration expression_statement ')' statement {
		$$ = create_node(FOR_STATEMENT, $1);
		$$->for_statement = (FOR_STATEMENT_STRUCT) { $3, $4, NULL, $6 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($4, $5);
		free($5);
	}
	| FOR '(' declaration expression_statement expression ')' statement {
		$$ = create_node(FOR_STATEMENT, $1);
		$$->for_statement = (FOR_STATEMENT_STRUCT) { $3, $4, $5, $7 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($5, $6);
		free($6);
	}
	;

jump_statement
	: GOTO IDENTIFIER ';' {
		$$ = create_node(GOTO_STATEMENT, $1);
		$$->goto_statement = (GOTO_STATEMENT_STRUCT) { $2->identifier.id };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
		move_and_append_comments($$, $3);
		free($3);
	}
	| CONTINUE ';' {
		$$ = create_node(LOOP_JUMP_STATEMENT, $1);
		$$->loop_jump_statement = (LOOP_JUMP_STATEMENT_STRUCT) {
			CONTINUE_STATEMENT
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| BREAK ';' {
		$$ = create_node(LOOP_JUMP_STATEMENT, $1);
		$$->loop_jump_statement = (LOOP_JUMP_STATEMENT_STRUCT) {
			BREAK_STATEMENT
		};
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| RETURN ';' {
		$$ = create_node(RETURN_STATEMENT, $1);
		$$->return_statement = (RETURN_STATEMENT_STRUCT) { NULL };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($2);
	}
	| RETURN expression ';' {
		$$ = create_node(RETURN_STATEMENT, $1);
		$$->return_statement = (RETURN_STATEMENT_STRUCT) { $2 };
		move_and_append_comments($$, $1);
		free($1);
		move_and_append_comments($$, $2);
		free($3);
	}
	;

translation_unit
	: external_declaration {
		$$ = create_node(TRANSLATION_UNIT, $1);
		$$->translation_unit = (TRANSLATION_UNIT_STRUCT) { NULL, $1, 0, NULL};
		c_translation_unit = $$;
	}
	| translation_unit external_declaration {
		$$ = create_node(TRANSLATION_UNIT, $1);
		$$->translation_unit = (TRANSLATION_UNIT_STRUCT) { $1, $2, 0, NULL };
		c_translation_unit = $$;
	}
	;

external_declaration
	: function_definition { $$ = $1; }
	| declaration { $$ = $1; }
	;

function_definition
	: declarator declaration_list compound_statement {
		$$ = create_node(FUNCTION_DEFINITION, $1);
		$$->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			NULL, $1, $2, $3
		};
	} 
	| declarator compound_statement {
		$$ = create_node(FUNCTION_DEFINITION, $1);
		$$->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			NULL, $1, NULL, $2
		};
	}
	| declaration_specifiers declarator declaration_list compound_statement {
		$$ = create_node(FUNCTION_DEFINITION, $1);
		$$->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			$1, $2, $3, $4
		};
	}
	| declaration_specifiers declarator compound_statement {
		$$ = create_node(FUNCTION_DEFINITION, $1);
		$$->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			$1, $2, NULL, $3
		};
	}
	;

declaration_list
	: declaration {
		$$ = create_node(DECLARATION_LIST, $1);
		$$->declaration_list = (DECLARATION_LIST_STRUCT) { NULL, $1 };
	}
	| declaration_list declaration {
		$$ = create_node(DECLARATION_LIST, $1);
		$$->declaration_list = (DECLARATION_LIST_STRUCT) { $1, $2 };
	}
	;

%%

/* Check the following patterns:
 *   ... typedef ... id;
 *   ... typedef ... (*id)(...);
 *   etc
 * If found, "id" will be recognized as a type name in the remainder of the
 * source; otherwise, "id" will be reported as an ordinary identifier.
 */
void check_type_name_declaration(struct AST_NODE* declaration)
{
	BOOL has_typedef = 0;
	struct AST_NODE* init_declarator;
	struct AST_NODE* declarator;
	struct AST_NODE* direct_declarator;
	struct AST_NODE* declaration_specifiers;
	struct AST_NODE* list;
	char* identifier;
	
	if (declaration->type == DECLARATION) {
		declaration_specifiers =
			declaration->declaration.declaration_specifiers;
		while (declaration_specifiers != NULL) {
			if (declaration_specifiers->declaration_specifiers
					.specifier_or_qualifier != NULL
					&& declaration_specifiers->declaration_specifiers
					.specifier_or_qualifier->type == STORAGE_CLASS_SPECIFIER
					&& declaration_specifiers->declaration_specifiers
					.specifier_or_qualifier->storage_class_specifier
					.storage_class_specifier == TYPEDEF) {
				has_typedef = 1;
				break;
			} else
				declaration_specifiers =
					declaration_specifiers->declaration_specifiers
							.more_declaration_specifiers;
		}		
	}

	if (has_typedef) {
		list = declaration->declaration.init_declarator_list;
		while (list != NULL) {
			init_declarator = list->init_declarator_list.init_declarator;
			if (init_declarator != NULL) {
				declarator = init_declarator->init_declarator.declarator;
				if (declarator != NULL) {
					direct_declarator =
						declarator->declarator.direct_declarator;
					
					while (direct_declarator != NULL && direct_declarator->type
							!= NAMED_DIRECT_DECLARATOR)
						if (direct_declarator->type == ARRAY_DIRECT_DECLARATOR)
							direct_declarator = direct_declarator
									->array_direct_declarator.direct_declarator;
						else if (direct_declarator->type
								== PARAMETERIZED_DIRECT_DECLARATOR)
							direct_declarator = direct_declarator
									->parameterized_direct_declarator
											.direct_declarator;
						else if (direct_declarator->type == DECLARATOR)
							direct_declarator = direct_declarator
									->declarator.direct_declarator;
						else
							c_error("unknown direct declarator type");
					
					if (direct_declarator != NULL) {
						identifier = direct_declarator
								->named_direct_declarator.identifier;
						strlist_add(type_names, identifier);
					}
				}
			}
			list = list->init_declarator_list.left_init_declarator_list;
		}
	}
}

int c_error(char* const error)
{
	c_default_error_handler(error);
	return 0;
}
