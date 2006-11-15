#ifndef __PTOLEMY_CODE_TRANSFORMER_UTIL__
#define __PTOLEMY_CODE_TRANSFORMER_UTIL__

#include <stdio.h>

extern int lex_line, lex_column, lex_file_index;
extern char* lex_file;
extern struct COMMENT_LIST_STRUCT *comment_list, *current_comment_ptr;
extern struct AST_NODE* c_translation_unit;
extern char error_buffer[];

#define GCC_BUILTIN_TYPE_NUM 1
extern char* gcc_builtin_types[GCC_BUILTIN_TYPE_NUM];

/* Comment list struct */
typedef struct COMMENT_LIST_STRUCT {
	int line;
	int column;
	int file;
	enum {
		BLOCK_COMMENT, LINE_COMMENT
	} comment_type;
	char* comment;
	struct AST_NODE* ast_node;
	struct COMMENT_LIST_STRUCT* previous;
	struct COMMENT_LIST_STRUCT* next;
} COMMENT_LIST_STRUCT;

/* Identifier struct */
typedef struct IDENTIFIER_STRUCT {
	char* id;
} IDENTIFIER_STRUCT;

/* Constant struct */
typedef struct CONSTANT_STRUCT {
	enum {
		CONSTANT_HEX,
		CONSTANT_OCT,
		CONSTANT_DEC,
		CONSTANT_CHR,
		CONSTANT_FLT
	} constant_type;
	char* text;
} CONSTANT_STRUCT;

/* String literal struct */
typedef struct STRING_LITERAL_STRUCT {
	char* text;
} STRING_LITERAL_STRUCT;

/* Array access struct */
typedef struct ARRAY_ACCESS_STRUCT {
	struct AST_NODE* array;
	struct AST_NODE* index;
} ARRAY_ACCESS_STRUCT;

/* Argument list struct */
typedef struct ARGUMENT_EXPRESSION_LIST_STRUCT {
	struct AST_NODE* left_argument_expression_list;
	struct AST_NODE* assignment_expression;
} ARGUMENT_EXPRESSION_LIST_STRUCT;

/* Parameterized expression struct */
typedef struct PARAMETERIZED_EXPRESSION_STRUCT {
	struct AST_NODE* expression;
	struct AST_NODE* argument_list;
} PARAMETERIZED_EXPRESSION_STRUCT;

/* Field access struct */
typedef struct FIELD_ACCESS_STRUCT {
	int is_pointer_access : 1;
	struct AST_NODE* expression;
	char* identifier;
} FIELD_ACCESS_STRUCT;

/* Inc/Dec expression struct */
typedef struct INC_DEC_EXPRESSION_STRUCT {
	enum {
		POSTFIX_INC,
		POSTFIX_DEC,
		PREFIX_INC,
		PREFIX_DEC
	} inc_dec_type;
	struct AST_NODE* operand;
} INC_DEC_EXPRESSION_STRUCT;

/* Struct constant struct */
typedef struct STRUCT_CONSTANT_STRUCT {
	struct AST_NODE* struct_type;
	struct AST_NODE* initializer_list;
} STRUCT_CONSTANT_STRUCT;

/* Unary expression struct */
typedef struct UNARY_EXPRESSION_STRUCT {
	int operator;
	struct AST_NODE* operand;
} UNARY_EXPRESSION_STRUCT;

/* Sizeof expression struct */
typedef struct SIZEOF_EXPRESSION_STRUCT {
	int mode : 1;
	struct AST_NODE* expression_or_type;
} SIZEOF_EXPRESSION_STRUCT;

/* Case expression struct */
typedef struct CAST_EXPRESSION_STRUCT {
	struct AST_NODE* cast_type;
	struct AST_NODE* operand;
} CAST_EXPRESSION_STRUCT;

/* Binary expression struct */
typedef struct BINARY_EXPRESSION_STRUCT {
	struct AST_NODE* left_operand;
	int operator;
	struct AST_NODE* right_operand;
} BINARY_EXPRESSION_STRUCT;

/* Ternary expression struct */
typedef struct CONDITIONAL_EXPRESSION_STRUCT {
	struct AST_NODE* operand1;
	struct AST_NODE* operand2;
	struct AST_NODE* operand3;
} CONDITIONAL_EXPRESSION_STRUCT;

/* Comma expression struct */
typedef struct COMMA_EXPRESSION_STRUCT {
	struct AST_NODE* left_expression;
	struct AST_NODE* assignment_expression;
} COMMA_EXPRESSION_STRUCT;

/* Declaration struct */
typedef struct DECLARATION_STRUCT {
	struct AST_NODE* declaration_specifiers;
	struct AST_NODE* init_declarator_list;
} DECLARATION_STRUCT;

/* Storage class specifier struct */
typedef struct STORAGE_CLASS_SPECIFIER_STRUCT {
	int storage_class_specifier;
} STORAGE_CLASS_SPECIFIER_STRUCT;

/* Struct or union struct */
typedef struct STRUCT_OR_UNION_STRUCT {
	int is_struct : 1;
	char* identifier;
	struct AST_NODE* struct_declaration_list;
} STRUCT_OR_UNION_STRUCT;

/* Struct declaration list struct */
typedef struct STRUCT_DECLARATION_LIST_STRUCT {
	struct AST_NODE* left_struct_declaration_list;
	struct AST_NODE* struct_declaration;
} STRUCT_DECLARATION_LIST_STRUCT;

/* Struct declaration struct */
typedef struct STRUCT_DECLARATION_STRUCT {
	struct AST_NODE* struct_qualifier_list;
	struct AST_NODE* struct_declaration_list;
} STRUCT_DECLARATION_STRUCT;

/* Primitive type specifier struct */
typedef struct PRIMITIVE_TYPE_SPECIFIER_STRUCT {
	int primitive_type;
} PRIMITIVE_TYPE_SPECIFIER_STRUCT;

/* Named type specifier struct */
typedef struct NAMED_TYPE_SPECIFIER_STRUCT {
	char* identifier;
} NAMED_TYPE_SPECIFIER_STRUCT;

/* Declaration specifiers struct */
typedef struct DECLARATION_SPECIFIERS_STRUCT {
	struct AST_NODE* specifier_or_qualifier;
	struct AST_NODE* more_declaration_specifiers;
} DECLARATION_SPECIFIERS_STRUCT;

/* Init declarator list struct */
typedef struct INIT_DECLARATOR_LIST_STRUCT {
	struct AST_NODE* left_init_declarator_list;
	struct AST_NODE* init_declarator;
} INIT_DECLARATOR_LIST_STRUCT;

/* Init declarator struct */
typedef struct INIT_DECLARATOR_STRUCT {
	struct AST_NODE* declarator;
	struct AST_NODE* initializer;
} INIT_DECLARATOR_STRUCT;

/* Specifier qualifier list struct */
typedef struct SPECIFIER_QUALIFIER_LIST_STRUCT {
	struct AST_NODE* specifier_or_qualifier;
	struct AST_NODE* right_specifier_qualifier_list;
} SPECIFIER_QUALIFIER_LIST_STRUCT;

/* Struct declarator list struct */
typedef struct STRUCT_DECLARATOR_LIST_STRUCT {
	struct AST_NODE* struct_declarator;
	struct AST_NODE* right_struct_declarator_list;
} STRUCT_DECLARATOR_LIST_STRUCT;

/* Struct declarator struct */
typedef struct STRUCT_DECLARATOR_STRUCT {
	struct AST_NODE* declarator;
	struct AST_NODE* constant_expression;
} STRUCT_DECLARATOR_STRUCT;

/* Enum specifier struct */
typedef struct ENUM_SPECIFIER_STRUCT {
	char* identifier;
	struct AST_NODE* enumerator_list;
} ENUM_SPECIFIER_STRUCT;

/* Enumerator list struct */
typedef struct ENUMERATOR_LIST_STRUCT {
	struct AST_NODE* left_enumerator_list;
	struct AST_NODE* enumerator;
} ENUMERATOR_LIST_STRUCT;

/* Enumerator struct */
typedef struct ENUMERATOR_STRUCT {
	char* identifier;
	struct AST_NODE* constant_expression;
} ENUMERATOR_STRUCT;

/* Type qualifier struct */
typedef struct TYPE_QUALIFIER_STRUCT {
	int qualifier;
} TYPE_QUALIFIER_STRUCT;

/* Function specifier struct */
typedef struct FUNCTION_SPECIFIER_STRUCT {
	int specifier;
} FUNCTION_SPECIFIER_STRUCT;

/* Pointer struct */
typedef struct POINTER_STRUCT {
	struct AST_NODE* type_qualifier_list;
	struct AST_NODE* right_pointer;
} POINTER_STRUCT;

/* Type qualifier list */
typedef struct TYPE_QUALIFIER_LIST_STRUCT {
	struct AST_NODE* left_type_qualifier_list;
	struct AST_NODE* type_qualifier;
} TYPE_QUALIFIER_LIST_STRUCT;

/* Declarator struct */
typedef struct DECLARATOR_STRUCT {
	struct AST_NODE* pointer;
	struct AST_NODE* direct_declarator;
} DECLARATOR_STRUCT;

/* Named direct declarator struct */
typedef struct NAMED_DIRECT_DECLARATOR_STRUCT {
	char* identifier;
} NAMED_DIRECT_DECLARATOR_STRUCT;

/* Array direct declarator struct */
typedef struct ARRAY_DIRECT_DECLARATOR_STRUCT {
	struct AST_NODE* direct_declarator;
	int is_type_qualifier_static : 1;
	struct AST_NODE* type_qualifier_list;
	int is_assignment_expression_static : 1;
	struct AST_NODE* assignment_expression;
	int has_pointer : 1;
} ARRAY_DIRECT_DECLARATOR_STRUCT;

/* Parameterized direct declarator struct */
typedef struct PARAMETERIZED_DIRECT_DECLARATOR_STRUCT {
	struct AST_NODE* direct_declarator;
	struct AST_NODE* parameter_type_list;
	struct AST_NODE* identifier_list;
} PARAMETERIZED_DIRECT_DECLARATOR_STRUCT;

/* Parameter list struct */
typedef struct PARAMETER_LIST_STRUCT {
	struct AST_NODE* left_parameter_list;
	struct AST_NODE* parameter_declaration;
	int has_ellipsis : 1;
} PARAMETER_LIST_STRUCT;

/* Parameter declaration struct */
typedef struct PARAMETER_DECLARATION_STRUCT {
	struct AST_NODE* declaration_specifiers;
	struct AST_NODE* declarator;
	struct AST_NODE* abstract_declarator;
} PARAMETER_DECLARATION_STRUCT;

/* Identifier list struct */
typedef struct IDENTIFIER_LIST_STRUCT {
	struct AST_NODE* left_identifier_list;
	char* identifier;
} IDENTIFIER_LIST_STRUCT;

/* Type name struct */
typedef struct TYPE_NAME_STRUCT {
	struct AST_NODE* specifier_qualifier_list;
	struct AST_NODE* abstract_declarator;
} TYPE_NAME_STRUCT;

/* Abstract declarator struct */
typedef struct ABSTRACT_DECLARATOR_STRUCT {
	struct AST_NODE* pointer;
	struct AST_NODE* direct_abstract_declarator;
} ABSTRACT_DECLARATOR_STRUCT;

/* Array direct abstract declarator struct */
typedef struct ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT {
	struct AST_NODE* direct_abstract_declarator;
	struct AST_NODE* assignment_expression;
	int has_pointer : 1;
} ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT;

/* Parameterized direct abstract declarator struct */
typedef struct PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT {
	struct AST_NODE* direct_abstract_declarator;
	struct AST_NODE* parameter_type_list;
} PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT;

/* Initializer list struct */
typedef struct INITIALIZER_LIST_STRUCT {
	struct AST_NODE* left_initializer_list;
	struct AST_NODE* designation;
	struct AST_NODE* initializer;
} INITIALIZER_LIST_STRUCT;

/* Designator list struct */
typedef struct DESIGNATOR_LIST_STRUCT {
	struct AST_NODE* left_designator_list;
	struct AST_NODE* designator;
} DESIGNATOR_LIST_STRUCT;

/* Array designator struct */
typedef struct ARRAY_DESIGNATOR_STRUCT {
	struct AST_NODE* constant_expression;
} ARRAY_DESIGNATOR_STRUCT;

/* Dot designator struct */
typedef struct DOT_DESIGNATOR_STRUCT {
	char* identifier;
} DOT_DESIGNATOR_STRUCT;

/* Identifier labeled statement struct */
typedef struct IDENTIFIER_LABELED_STATEMENT_STRUCT {
	char* identifier;
	struct AST_NODE* statement;
} IDENTIFIER_LABELED_STATEMENT_STRUCT;

/* Case labeled statement struct */
typedef struct CASE_LABELED_STATEMENT_STRUCT {
	int is_default : 1;
	struct AST_NODE* constant_expression;
	struct AST_NODE* statement;
} CASE_LABELED_STATEMENT_STRUCT;

/* Block item list struct */
typedef struct BLOCK_ITEM_LIST_STRUCT {
	struct AST_NODE* left_block_item_list;
	struct AST_NODE* declaration_or_statement;
} BLOCK_ITEM_LIST_STRUCT;

/* Expression statement struct */
typedef struct EXPRESSION_STATEMENT_STRUCT {
	struct AST_NODE* expression;
} EXPRESSION_STATEMENT_STRUCT;

/* If statement struct */
typedef struct IF_STATEMENT_STRUCT {
	struct AST_NODE* expression;
	struct AST_NODE* if_statement;
	struct AST_NODE* else_statement;
} IF_STATEMENT_STRUCT;

/* Switch statement struct */
typedef struct SWITCH_STATEMENT_STRUCT {
	struct AST_NODE* expression;
	struct AST_NODE* statement;
} SWITCH_STATEMENT_STRUCT;

/* While statement struct */
typedef struct WHILE_STATEMENT_STRUCT {
	struct AST_NODE* expression;
	struct AST_NODE* statement;
} WHILE_STATEMENT_STRUCT;

/* Do while statement struct */
typedef struct DO_WHILE_STATEMENT_STRUCT {
	struct AST_NODE* statement;
	struct AST_NODE* expression;
} DO_WHILE_STATEMENT_STRUCT;

/* For statement struct */
typedef struct FOR_STATEMENT_STRUCT {
	struct AST_NODE* declaration_or_expression_statement;
	struct AST_NODE* expression_statement;
	struct AST_NODE* expression;
	struct AST_NODE* statement;
} FOR_STATEMENT_STRUCT;

/* Goto statement struct */
typedef struct GOTO_STATEMENT_STRUCT {
	char* identifier;
} GOTO_STATEMENT_STRUCT;

/* Loop jump statement struct */
typedef struct LOOP_JUMP_STATEMENT_STRUCT {
	enum {
		CONTINUE_STATEMENT,
		BREAK_STATEMENT
	} loop_jump_type;
} LOOP_JUMP_STATEMENT_STRUCT;

/* Return statement struct */
typedef struct RETURN_STATEMENT_STRUCT {
	struct AST_NODE* expression;
} RETURN_STATEMENT_STRUCT;

/* Translation unit struct */
typedef struct TRANSLATION_UNIT_STRUCT {
	struct AST_NODE* left_translation_unit;
	struct AST_NODE* external_declaration;
	int is_top_level : 1;
	struct COMMENT_LIST_STRUCT* remaining_comments;
} TRANSLATION_UNIT_STRUCT;

/* Function declaration struct */
typedef struct FUNCTION_DEFINITION_STRUCT {
	struct AST_NODE* declaration_specifiers;
	struct AST_NODE* declarator;
	struct AST_NODE* declaration_list;
	struct AST_NODE* compound_statement;
} FUNCTION_DEFINITION_STRUCT;

/* Declaration list struct */
typedef struct DECLARATION_LIST_STRUCT {
	struct AST_NODE* left_declaration_list;
	struct AST_NODE* declaration;
} DECLARATION_LIST_STRUCT;

/* Compound statement struct */
typedef struct COMPOUND_STATEMENT_STRUCT {
	struct AST_NODE* block_item_list;
} COMPOUND_STATEMENT_STRUCT;

/* Struct for AST nodes */
typedef struct AST_NODE {
	int type;
	int line;
	int column;
	int file;
	struct COMMENT_LIST_STRUCT* comment;
	struct AST_NODE* parent;
	union {
		struct IDENTIFIER_STRUCT identifier;
		struct CONSTANT_STRUCT constant;
		struct STRING_LITERAL_STRUCT string_literal;
		struct ARRAY_ACCESS_STRUCT array_access;
		struct ARGUMENT_EXPRESSION_LIST_STRUCT argument_expression_list;
		struct PARAMETERIZED_EXPRESSION_STRUCT parameterized_expression;
		struct FIELD_ACCESS_STRUCT field_access;
		struct INC_DEC_EXPRESSION_STRUCT inc_dec_expression;
		struct STRUCT_CONSTANT_STRUCT struct_constant;
		struct UNARY_EXPRESSION_STRUCT unary_expression;
		struct SIZEOF_EXPRESSION_STRUCT sizeof_expression;
		struct CAST_EXPRESSION_STRUCT cast_expression;
		struct BINARY_EXPRESSION_STRUCT binary_expression;
		struct CONDITIONAL_EXPRESSION_STRUCT conditional_expression;
		struct COMMA_EXPRESSION_STRUCT comma_expression;
		struct DECLARATION_STRUCT declaration;
		struct STORAGE_CLASS_SPECIFIER_STRUCT storage_class_specifier;
		struct STRUCT_OR_UNION_STRUCT struct_or_union;
		struct STRUCT_DECLARATION_LIST_STRUCT struct_declaration_list;
		struct STRUCT_DECLARATION_STRUCT struct_declaration;
		struct PRIMITIVE_TYPE_SPECIFIER_STRUCT primitive_type_specifier;
		struct NAMED_TYPE_SPECIFIER_STRUCT named_type_specifier;
		struct DECLARATION_SPECIFIERS_STRUCT declaration_specifiers;
		struct INIT_DECLARATOR_LIST_STRUCT init_declarator_list;
		struct INIT_DECLARATOR_STRUCT init_declarator;
		struct SPECIFIER_QUALIFIER_LIST_STRUCT specifier_qualifier_list;
		struct STRUCT_DECLARATOR_LIST_STRUCT struct_declarator_list;
		struct STRUCT_DECLARATOR_STRUCT struct_declarator;
		struct ENUM_SPECIFIER_STRUCT enum_specifier;
		struct ENUMERATOR_LIST_STRUCT enumerator_list;
		struct ENUMERATOR_STRUCT enumerator;
		struct TYPE_QUALIFIER_STRUCT type_qualifier;
		struct FUNCTION_SPECIFIER_STRUCT function_specifier;
		struct POINTER_STRUCT pointer;
		struct TYPE_QUALIFIER_LIST_STRUCT type_qualifier_list;
		struct DECLARATOR_STRUCT declarator;
		struct NAMED_DIRECT_DECLARATOR_STRUCT named_direct_declarator;
		struct ARRAY_DIRECT_DECLARATOR_STRUCT array_direct_declarator;
		struct PARAMETERIZED_DIRECT_DECLARATOR_STRUCT
				parameterized_direct_declarator;
		struct PARAMETER_LIST_STRUCT parameter_list;
		struct PARAMETER_DECLARATION_STRUCT parameter_declaration;
		struct IDENTIFIER_LIST_STRUCT identifier_list;
		struct TYPE_NAME_STRUCT type_name;
		struct ABSTRACT_DECLARATOR_STRUCT abstract_declarator;
		struct ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT
				array_direct_abstract_declarator;
		struct PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT
				parameterized_direct_abstract_declarator;
		struct INITIALIZER_LIST_STRUCT initializer_list;
		struct DESIGNATOR_LIST_STRUCT designator_list;
		struct ARRAY_DESIGNATOR_STRUCT array_designator;
		struct DOT_DESIGNATOR_STRUCT dot_designator;
		struct IDENTIFIER_LABELED_STATEMENT_STRUCT identifier_labeled_statement;
		struct CASE_LABELED_STATEMENT_STRUCT case_labeled_statement;
		struct BLOCK_ITEM_LIST_STRUCT block_item_list;
		struct EXPRESSION_STATEMENT_STRUCT expression_statement;
		struct IF_STATEMENT_STRUCT if_statement;
		struct SWITCH_STATEMENT_STRUCT switch_statement;
		struct WHILE_STATEMENT_STRUCT while_statement;
		struct DO_WHILE_STATEMENT_STRUCT do_while_statement;
		struct FOR_STATEMENT_STRUCT for_statement;
		struct GOTO_STATEMENT_STRUCT goto_statement;
		struct LOOP_JUMP_STATEMENT_STRUCT loop_jump_statement;
		struct RETURN_STATEMENT_STRUCT return_statement;
		struct TRANSLATION_UNIT_STRUCT translation_unit;
		struct FUNCTION_DEFINITION_STRUCT function_definition;
		struct DECLARATION_LIST_STRUCT declaration_list;
		struct COMPOUND_STATEMENT_STRUCT compound_statement;
		
		int lexical_token; // not in AST
	};
} AST_NODE;

typedef struct AST_NODE* YYSTYPE;
#define YYSTYPE_IS_DECLARED

#ifdef NULL
#undef NULL
#endif
#define NULL 0

typedef enum {
	AST_VISIT_CONTINUE, AST_VISIT_CANCEL_NODE, AST_VISIT_CANCEL
} AST_HANDLER_RESULT;
typedef AST_HANDLER_RESULT (*ast_node_handler)(struct AST_NODE* ast_node,
	void* data);
typedef AST_HANDLER_RESULT (*ast_error_handler)(const char* message);
typedef short int BOOL;

void append_text(char** text, char c, int* current_length, int* max_size);
char* create_error_string(const char* error, const char* file, int line,
	int column);
AST_NODE* create_node(int type, struct AST_NODE* first_child);
AST_NODE* create_node2(int type, int line, int column, int file);
int get_operator_precedence(const struct AST_NODE* expression);
const char* get_operator_string(int operator);
const char* get_semantic_value_name(int token);
void move_and_append_comments(struct AST_NODE* des_node,
	struct AST_NODE* src_node);
void move_and_prepend_comments(struct AST_NODE* des_node,
	struct AST_NODE* src_node);
struct AST_NODE* parse();
void print_comments(FILE* stream, const struct COMMENT_LIST_STRUCT* end,
	const char* tab, int tab_num);
void reset();
char* strclone(const char* src);
BOOL visit(struct AST_NODE* ast_node, ast_node_handler pre_handler,
	ast_node_handler post_handler, ast_error_handler error_handler, void* data);
void c_default_error_handler(char* const error);

BOOL ast_print_tree(FILE* stream, struct AST_NODE* ast_node,
	ast_error_handler error_handler);
BOOL ast_initial_check(struct AST_NODE* ast_node,
	ast_error_handler error_handler);
	
typedef struct SOURCE_PRINTER_FORMAT_STRUCT {
	int block_curly_on_new_line : 1;
	int do_while_statement_always_in_block : 1;
	int empty_line_between_declarations : 1;
	int enum_item_on_new_line : 1;
	int for_statement_always_in_block : 1;
	int function_curly_on_new_line : 1;
	int if_branch_always_in_block : 1;
	int initializer_on_new_line : 1;
	int parenthesize_return_statement : 1;
	int space_between_arguments : 1;
	int space_between_declarators : 1;
	int space_for_abstract_declarator : 1;
	int space_for_array_access : 1;
	int space_for_binary_operation : 1;
	int space_for_block_curly : 1;
	int space_for_comma_expression : 1;
	int space_for_dot_field_access : 1;
	int space_for_field_size : 1;
	int space_for_initializer : 1;
	int space_for_pointer : 1;
	int space_for_pointer_field_access : 1;
	int space_for_parameterized_expression : 1;
	int space_for_parenthesized_expression : 1;
	int space_for_ternary_expression : 1;
	int space_for_type_cast : 1;
	int space_for_type_name : 1;
	int space_for_unary_operation : 1;
	int tab_in_formal_parameter_declaration : 1;
	int tab_in_switch_case : 1;
	int while_statement_always_in_block : 1;
	char* tab;
} SOURCE_PRINTER_FORMAT_STRUCT;
extern struct SOURCE_PRINTER_FORMAT_STRUCT default_source_printer_format;

BOOL ast_print_source(FILE* stream, struct AST_NODE* ast_node,
	ast_error_handler error_handler,
	const struct SOURCE_PRINTER_FORMAT_STRUCT* format);

typedef struct STRING_LIST_STRUCT {
	char* string;
	int index;
	int count;
	struct STRING_LIST_STRUCT* previous;
	struct STRING_LIST_STRUCT* next;
} STRING_LIST_STRUCT;

int strlist_add(struct STRING_LIST_STRUCT* list, const char* string);
const char* strlist_get(const struct STRING_LIST_STRUCT* list, int index);
int strlist_lookup(const struct STRING_LIST_STRUCT* list, const char* string);
BOOL strlist_remove(struct STRING_LIST_STRUCT** list, const char* string);

extern struct STRING_LIST_STRUCT* lex_files;
extern struct STRING_LIST_STRUCT* type_names;

void free_comments(struct COMMENT_LIST_STRUCT* comments_end, BOOL free_text,
	BOOL recursive_forward);

#endif
