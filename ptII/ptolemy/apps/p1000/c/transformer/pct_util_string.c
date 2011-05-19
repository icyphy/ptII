#include <string.h>
#include <stdlib.h>
#include "pct_util.h"
#include "pct_c_parser.h"

char error_buffer[255];

struct STRING_LIST_STRUCT* lex_files;
struct STRING_LIST_STRUCT* type_names;

// Semantic values starting from #258
#define STARTING_SEMANTIC_VALUE IDENTIFIER
#define ENDING_SEMANTIC_VALUE COMPOUND_STATEMENT
char* semantic_value_names[] = {
	"IDENTIFIER",
	"CONSTANT",
	"STRING_LITERAL",
	"SIZEOF",
	"PTR_OP",
	"INC_OP",
	"DEC_OP",
	"LEFT_OP",
	"RIGHT_OP",
	"LE_OP",
	"GE_OP",
	"EQ_OP",
	"NE_OP",
	"AND_OP",
	"OR_OP",
	"MUL_ASSIGN",
	"DIV_ASSIGN",
	"MOD_ASSIGN",
	"ADD_ASSIGN",
	"SUB_ASSIGN",
	"LEFT_ASSIGN",
	"RIGHT_ASSIGN",
	"AND_ASSIGN",
	"XOR_ASSIGN",
	"OR_ASSIGN",
	"TYPEDEF",
	"EXTERN",
	"STATIC",
	"AUTO",
	"REGISTER",
	"CHAR",
	"SHORT",
	"INT",
	"LONG",
	"SIGNED",
	"UNSIGNED",
	"FLOAT",
	"DOUBLE",
	"CONST",
	"VOLATILE",
	"VOID",
	"STRUCT",
	"UNION",
	"ENUM",
	"ELLIPSIS",
	"CASE",
	"DEFAULT",
	"IF",
	"ELSE",
	"SWITCH",
	"WHILE",
	"DO",
	"FOR",
	"GOTO",
	"CONTINUE",
	"BREAK",
	"RETURN",
	"LEXICAL_TOKEN",
	"AMBIGUOUS_TYPE_NAME",
	"COMPLEX",
	"IMAGINARY",
	"RESTRICT",
	"BOOLEAN",
	"INLINE",
	"ARRAY_ACCESS",
	"ARGUMENT_EXPRESSION_LIST",
	"PARAMETERIZED_EXPRESSION",
	"FIELD_ACCESS",
	"INC_DEC_EXPRESSION",
	"STRUCT_CONSTANT",
	"UNARY_EXPRESSION",
	"SIZEOF_EXPRESSION",
	"CAST_EXPRESSION",
	"BINARY_EXPRESSION",
	"CONDITIONAL_EXPRESSION",
	"COMMA_EXPRESSION",
	"DECLARATION",
	"STORAGE_CLASS_SPECIFIER",
	"STRUCT_OR_UNION",
	"STRUCT_DECLARATION",
	"STRUCT_DECLARATION_LIST",
	"PRIMITIVE_TYPE_SPECIFIER",
	"NAMED_TYPE_SPECIFIER",
	"DECLARATION_SPECIFIERS",
	"INIT_DECLARATOR_LIST",
	"INIT_DECLARATOR",
	"DECLARATOR",
	"SPECIFIER_QUALIFIER_LIST",
	"STRUCT_DECLARATOR_LIST",
	"STRUCT_DECLARATOR",
	"ENUM_SPECIFIER",
	"ENUMERATOR_LIST",
	"ENUMERATOR",
	"TYPE_QUALIFIER",
	"FUNCTION_SPECIFIER",
	"POINTER",
	"TYPE_QUALIFIER_LIST",
	"NAMED_DIRECT_DECLARATOR",
	"ARRAY_DIRECT_DECLARATOR",
	"PARAMETER_LIST",
	"PARAMETERIZED_DIRECT_DECLARATOR",
	"PARAMETER_DECLARATION",
	"IDENTIFIER_LIST",
	"TYPE_NAME",
	"ABSTRACT_DECLARATOR",
	"ARRAY_DIRECT_ABSTRACT_DECLARATOR",
	"PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR",
	"INITIALIZER_LIST",
	"DESIGNATOR_LIST",
	"ARRAY_DESIGNATOR",
	"DOT_DESIGNATOR",
	"IDENTIFIER_LABELED_STATEMENT",
	"CASE_LABELED_STATEMENT",
	"BLOCK_ITEM_LIST",
	"EXPRESSION_STATEMENT",
	"IF_STATEMENT",
	"SWITCH_STATEMENT",
	"WHILE_STATEMENT",
	"DO_WHILE_STATEMENT",
	"FOR_STATEMENT",
	"GOTO_STATEMENT",
	"LOOP_JUMP_STATEMENT",
	"RETURN_STATEMENT",
	"TRANSLATION_UNIT",
	"FUNCTION_DEFINITION",
	"DECLARATION_LIST",
	"COMPOUND_STATEMENT"
};

#define TEXT_BUFFER_INCREMENT 64
void append_text(char** text, char c, int* current_length, int* max_size)
{
	if (*max_size == 0) {
		*text = (char*) malloc(TEXT_BUFFER_INCREMENT);
		*max_size = TEXT_BUFFER_INCREMENT;
	}
	while (*current_length + 1 >= *max_size) {
		*max_size += TEXT_BUFFER_INCREMENT;
		*text = (char*) realloc(*text, *max_size);
	}
	(*text)[*current_length] = c;
	(*text)[++*current_length] = '\0';
}

char* create_error_string(const char* error, const char* file, int line,
	int column)
{
	int len = sprintf(error_buffer, "%s at %s%sl%dc%d", error,
		file == NULL ? "" : file, file == NULL ? "" : ":", line, column);
	if (len < 0) {
		return "unable to create error message";
	} else {
		return error_buffer;
	}
}

const char* get_operator_string(int operator) {
	switch (operator) {
		case RIGHT_ASSIGN:
			return ">>=";
		case LEFT_ASSIGN:
			return "<<=";
		case ADD_ASSIGN:
			return "+=";
		case SUB_ASSIGN:
			return "-=";
		case MUL_ASSIGN:
			return "*=";
		case DIV_ASSIGN:
			return "/=";
		case MOD_ASSIGN:
			return "%=";
		case AND_ASSIGN:
			return "&=";
		case XOR_ASSIGN:
			return "^=";
		case OR_ASSIGN:
			return "|=";
		case RIGHT_OP:
			return ">>";
		case LEFT_OP:
			return "<<";
		case INC_OP:
			return "++";
		case DEC_OP:
			return "--";
		case PTR_OP:
			return "->";
		case AND_OP:
			return "&&";
		case OR_OP:
			return "||";
		case LE_OP:
			return "<=";
		case GE_OP:
			return ">=";
		case EQ_OP:
			return "==";
		case NE_OP:
			return "!=";
		default:
			return NULL;	
	}
}

const char* get_semantic_value_name(int token)
{
	if (token >= STARTING_SEMANTIC_VALUE && token <= ENDING_SEMANTIC_VALUE)
		return semantic_value_names[token - STARTING_SEMANTIC_VALUE];
	else
		return NULL;
}

char* strclone(const char* src)
{
	char* des = (char*) malloc(strlen(src) + 1);
	strcpy(des, src);
	return des;
}

int strlist_add(struct STRING_LIST_STRUCT* list, const char* string)
{
	int max_index = -1;
	while (list->next != NULL) {	// while not the end of the list
		max_index = list->index;
		if (strcmp(list->string, string) == 0) {
			list->count++;
			return list->index;
		} else
			list = list->next;
	}
	list->string = strclone(string);
	list->count = 1;
	list->index = max_index + 1;
	list->next =
		(struct STRING_LIST_STRUCT*) malloc(sizeof(struct STRING_LIST_STRUCT));
	*list->next = (struct STRING_LIST_STRUCT) { NULL, 0, 0, list, NULL };
	return list->index;
}

const char* strlist_get(const struct STRING_LIST_STRUCT* list, int index)
{
	while (list->next != NULL) {	// while not the end of the list
		if (list->index == index)
			return list->string;
		else
			list = list->next;
	}
	return NULL;
}

int strlist_lookup(const struct STRING_LIST_STRUCT* list, const char* string)
{
	while (list->next != NULL) {	// while not the end of the list
		if (strcmp(list->string, string) == 0)
			return list->index;
		else
			list = list->next;
	}
	return -1;
}

BOOL strlist_remove(struct STRING_LIST_STRUCT** list, const char* string)
{
	while ((*list)->next != NULL) {	// while not the end of the list
		if (strcmp((*list)->string, string) == 0) {
			(*list)->count--;
			if ((*list)->count == 0) {
				free((*list)->string);
				if ((*list)->previous != NULL)
					(*list)->previous->next = (*list)->next;
				else
					*list = (*list)->next;
				(*list)->next->previous = (*list)->previous;
				free(*list);
			}
			return 1;
		}
	}
	return 0;
}
