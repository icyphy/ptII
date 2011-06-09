#include <stdio.h>
#include <stdlib.h>  /* Get malloc() declaration under Solaris. */
#include <string.h>
#include "pct_util.h"
#include "pct_c_parser.h"

int lex_line, lex_column, lex_file_index;
AST_NODE* c_translation_unit = NULL;
struct COMMENT_LIST_STRUCT *comment_list = NULL, *current_comment_ptr = NULL;

char* gcc_builtin_types[GCC_BUILTIN_TYPE_NUM] = {
	"__builtin_va_list"
};

AST_NODE* create_node(int type, struct AST_NODE* first_child)
{
	AST_NODE* node = (AST_NODE*) malloc(sizeof(AST_NODE));
	node->type = type;
	node->line = first_child->line;
	node->column = first_child->column;
	node->file = first_child->file;
	node->comment = NULL;
	node->parent = NULL; // To be set later.
	return node;
}

AST_NODE* create_node2(int type, int line, int column, int file)
{
	AST_NODE* node = (AST_NODE*) malloc(sizeof(AST_NODE));
	node->type = type;
	node->line = line;
	node->column = column;
	node->file = file;
	node->comment = NULL;
	node->parent = NULL; // To be set later.
	return node;
}

int get_operator_precedence(const struct AST_NODE* expression) {
	switch (expression->type) {
		case IDENTIFIER:
		case CONSTANT:
		case STRING_LITERAL:
			return 0;
		case ARRAY_ACCESS:
		case PARAMETERIZED_EXPRESSION:
		case FIELD_ACCESS:
		case STRUCT_CONSTANT:
			return 1;
		case INC_DEC_EXPRESSION:
			if (expression->inc_dec_expression.inc_dec_type == POSTFIX_INC
					|| expression->inc_dec_expression.inc_dec_type
							== POSTFIX_DEC)
				return 1;
			else
				return 2;
		case UNARY_EXPRESSION:
		case SIZEOF_EXPRESSION:
			return 2;
		case CAST_EXPRESSION:
			return 3;
		case BINARY_EXPRESSION:
			if (expression->binary_expression.operator == '*'
					|| expression->binary_expression.operator == '/'
					|| expression->binary_expression.operator == '%')
				return 4;
			else if (expression->binary_expression.operator == '+'
					|| expression->binary_expression.operator == '-')
				return 5;
			else if (expression->binary_expression.operator == LEFT_OP
					|| expression->binary_expression.operator == RIGHT_OP)
				return 6;
			else if (expression->binary_expression.operator == '<'
					|| expression->binary_expression.operator == '>'
					|| expression->binary_expression.operator == LE_OP
					|| expression->binary_expression.operator == GE_OP)
				return 7;
			else if (expression->binary_expression.operator == EQ_OP
					|| expression->binary_expression.operator == NE_OP)
				return 8;
			else if (expression->binary_expression.operator == '&')
				return 9;
			else if (expression->binary_expression.operator == '^')
				return 10;
			else if (expression->binary_expression.operator == '|')
				return 11;
			else if (expression->binary_expression.operator == AND_OP)
				return 12;
			else if (expression->binary_expression.operator == OR_OP)
				return 13;
			else if (expression->binary_expression.operator == '='
					|| expression->binary_expression.operator == MUL_ASSIGN
					|| expression->binary_expression.operator == DIV_ASSIGN
					|| expression->binary_expression.operator == MOD_ASSIGN
					|| expression->binary_expression.operator == ADD_ASSIGN
					|| expression->binary_expression.operator == SUB_ASSIGN
					|| expression->binary_expression.operator == LEFT_ASSIGN
					|| expression->binary_expression.operator == RIGHT_ASSIGN
					|| expression->binary_expression.operator == AND_ASSIGN
					|| expression->binary_expression.operator == XOR_ASSIGN
					|| expression->binary_expression.operator == OR_ASSIGN)
				return 15;
			else
				return -1;
		case CONDITIONAL_EXPRESSION:
			return 14;
		case COMMA_EXPRESSION:
			return 16;
		default:
			return -1;
	}
}

void free_comments(struct COMMENT_LIST_STRUCT* comments_end, BOOL free_text,
	BOOL recursive_forward)
{
	if (comments_end != NULL) {
		if (free_text)
			free(comments_end->comment);
		if (recursive_forward)
			free_comments(comments_end->previous, free_text, recursive_forward);
		comments_end->previous = NULL;
		comments_end->next = NULL;
		free(comments_end);
	}
}

void move_and_append_comments(struct AST_NODE* des_node,
	struct AST_NODE* src_node)
{
	struct COMMENT_LIST_STRUCT *des_node_ptr, *src_node_ptr;
	if (src_node->comment != NULL) {
		src_node_ptr = src_node->comment;
		if (des_node->comment == NULL) {
			des_node->comment = src_node_ptr;
		} else {
			while (src_node_ptr->previous != NULL)
				src_node_ptr = src_node_ptr->previous;
			des_node_ptr = des_node->comment;
			while (des_node_ptr->next != NULL)
				des_node_ptr = des_node_ptr->next;
			des_node_ptr->next = src_node_ptr;
			src_node_ptr->previous = des_node_ptr;
			des_node->comment = src_node->comment;
		}
		src_node->comment = NULL;
	}
}

void move_and_prepend_comments(struct AST_NODE* des_node,
	struct AST_NODE* src_node)
{
	struct COMMENT_LIST_STRUCT *des_node_ptr, *src_node_ptr;
	if (src_node->comment != NULL) {
		src_node_ptr = src_node->comment;
		if (des_node->comment == NULL) {
			des_node->comment = src_node_ptr;
		} else {
			while (src_node_ptr->next != NULL)
				src_node_ptr = src_node_ptr->next;
			des_node_ptr = des_node->comment;
			while (des_node_ptr->previous != NULL)
				des_node_ptr = des_node_ptr->previous;
			des_node_ptr->previous = src_node_ptr;
			src_node_ptr->next = des_node_ptr;
		}
		src_node->comment = NULL;
	}
}

void print_comments(FILE* stream, const struct COMMENT_LIST_STRUCT* end,
	const char* tab, int tab_num)
{
	int i;
	if (end != NULL) {
		print_comments(stream, end->previous, tab, tab_num);
		if (tab != NULL)
			for (i = 0; i < tab_num; i++)
				fprintf(stream, tab);
		fprintf(stream, "%s\n", end->comment);
	}
}

extern int c_parse();
struct AST_NODE* parse()
{
	struct AST_NODE* translation_unit;
	struct COMMENT_LIST_STRUCT* comment_ptr;
	struct COMMENT_LIST_STRUCT* node_comment_ptr = NULL;
	
	reset();
	
	if (c_parse() != 0)
		return NULL;
	
	translation_unit = c_translation_unit;
	translation_unit->translation_unit.is_top_level = 1;
	
	comment_ptr = current_comment_ptr;
	while (comment_ptr != NULL && comment_ptr->ast_node == NULL) {
		if (node_comment_ptr == NULL) {
			node_comment_ptr =
				(COMMENT_LIST_STRUCT*) malloc(sizeof(COMMENT_LIST_STRUCT));
			*node_comment_ptr = (COMMENT_LIST_STRUCT) {
				comment_ptr->line,
				comment_ptr->column,
				comment_ptr->file,
				comment_ptr->comment_type,
				comment_ptr->comment,
				NULL,
				NULL,
				NULL
			};
			translation_unit->translation_unit.remaining_comments =
				node_comment_ptr;
		} else {
			node_comment_ptr->previous =
				(COMMENT_LIST_STRUCT*) malloc(sizeof(COMMENT_LIST_STRUCT));
			*node_comment_ptr->previous = (COMMENT_LIST_STRUCT) {
				comment_ptr->line,
				comment_ptr->column,
				comment_ptr->file,
				comment_ptr->comment_type,
				comment_ptr->comment,
				NULL,
				NULL,
				node_comment_ptr
			};
			node_comment_ptr = node_comment_ptr->previous;
		}
		comment_ptr = comment_ptr->previous;
	}
	
	free_comments(current_comment_ptr, 0, 1);
	current_comment_ptr = NULL;
	
	if (!ast_initial_check(translation_unit, NULL))
		return NULL;
	
	return translation_unit;
}

void reset()
{
	int i;
	lex_line = 1;
	lex_column = 0;
	lex_file_index = -1;
	c_translation_unit = NULL;
	comment_list = NULL;
	current_comment_ptr = NULL;
	
	lex_files =
		(struct STRING_LIST_STRUCT*) malloc(sizeof(struct STRING_LIST_STRUCT));
	type_names =
		(struct STRING_LIST_STRUCT*) malloc(sizeof(struct STRING_LIST_STRUCT));
	*type_names = *lex_files = (struct STRING_LIST_STRUCT) {
		NULL, 0, 0, NULL, NULL
	};
	for (i = 0; i < GCC_BUILTIN_TYPE_NUM; i++)
		strlist_add(type_names, gcc_builtin_types[i]);
}

void c_default_error_handler(char* const error)
{
	fflush(stderr);
	fprintf(stderr, "ERROR: %s.\n", create_error_string(error,
		strlist_get(lex_files, lex_file_index), lex_line, lex_column));
}
