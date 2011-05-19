#include <stdio.h>
#include "pct_util.h"
#include "pct_c_parser.h"

#define TAB "    "

struct PRINT_TREE_DATA {
	FILE* stream;
	int tab_num;
};

AST_HANDLER_RESULT ast_print_tree_pre_handler(struct AST_NODE* ast_node,
	void* data)
{
	struct PRINT_TREE_DATA* print_tree_data = (struct PRINT_TREE_DATA*) data;
	int tab;
	const char* file = strlist_get(lex_files, ast_node->file);
	
	print_comments(print_tree_data->stream, ast_node->comment, TAB,
		print_tree_data->tab_num);

	for (tab = 0; tab < print_tree_data->tab_num; tab++)
		fprintf(print_tree_data->stream, TAB);
	fprintf(print_tree_data->stream, "%s@(%s%sl%dc%d)\n",
			get_semantic_value_name(ast_node->type),
			file == NULL ? "" : file, file == NULL ? "" : ":",  ast_node->line,
			ast_node->column);
	print_tree_data->tab_num++;
	return AST_VISIT_CONTINUE;
}

AST_HANDLER_RESULT ast_print_tree_post_handler(struct AST_NODE* ast_node,
	void* data)
{
	struct PRINT_TREE_DATA* print_tree_data = (struct PRINT_TREE_DATA*) data;
	struct COMMENT_LIST_STRUCT* remaining_comments;
	print_tree_data->tab_num--;
	
	if (ast_node->type == TRANSLATION_UNIT) {
		remaining_comments = ast_node->translation_unit.remaining_comments;
		if (remaining_comments != NULL)
			print_comments(print_tree_data->stream, remaining_comments, NULL,
				0);
	}
	return AST_VISIT_CONTINUE;
}

BOOL ast_print_tree(FILE* stream, struct AST_NODE* ast_node,
		ast_error_handler error_handler)
{
	struct PRINT_TREE_DATA print_tree_data = (struct PRINT_TREE_DATA) {
		stream, 0
	};
	return visit(ast_node, ast_print_tree_pre_handler,
			ast_print_tree_post_handler, error_handler, &print_tree_data);
}

struct AST_INITIAL_CHECK_DATA {
	struct AST_NODE* parent;
};

AST_HANDLER_RESULT ast_initial_check_pre_handler(struct AST_NODE* ast_node,
	void* data)
{
	struct AST_INITIAL_CHECK_DATA* ast_initial_check_data =
		(struct AST_INITIAL_CHECK_DATA*) data;
	ast_node->parent = ast_initial_check_data->parent;
	ast_initial_check_data->parent = ast_node;
	return AST_VISIT_CONTINUE;
}

AST_HANDLER_RESULT ast_initial_check_post_handler(struct AST_NODE* ast_node,
	void* data)
{
	struct AST_INITIAL_CHECK_DATA* ast_initial_check_data =
		(struct AST_INITIAL_CHECK_DATA*) data;
	ast_initial_check_data->parent = ast_node->parent;
	return AST_VISIT_CONTINUE;
}

BOOL ast_initial_check(struct AST_NODE* ast_node,
	ast_error_handler error_handler)
{
	struct AST_INITIAL_CHECK_DATA ast_initial_check_data =
		(struct AST_INITIAL_CHECK_DATA) { NULL };
	return visit(ast_node, ast_initial_check_pre_handler,
			ast_initial_check_post_handler, error_handler,
			&ast_initial_check_data);
}

struct SOURCE_PRINTER_FORMAT_STRUCT default_source_printer_format =
	(SOURCE_PRINTER_FORMAT_STRUCT) {
		0,	// block_curly_on_new_line
		0,	// do_while_statement_always_in_block
		1,	// empty_line_between_declarations
		1,	// enum_item_on_new_line
		0,	// for_statement_always_in_block
		1,	// function_curly_on_new_line
		0,	// if_branch_always_in_block
		1,	// initializer_on_new_line
		0,	// parenthesize_return_statement
		1,	// space_between_arguments
		1,	// space_between_declarators
		0,	// space_for_abstract_declarator
		0,	// space_for_array_access
		1,	// space_for_binary_operation
		1,	// space_for_block_curly
		1,	// space_for_comma_expression
		0,	// space_for_dot_field_access
		1,	// space_for_field_size
		1,	// space_for_initializer
		0,	// space_for_pointer
		0,	// space_for_pointer_field_access
		0,	// space_for_parameterized_expression
		1,	// space_for_parenthesized_expression
		1,	// space_for_ternary_expression
		1,	// space_for_type_cast
		0,	// space_for_type_name
		0,	// space_for_unary_operation
		0,	// tab_in_formal_parameter_declaration
		1,	// tab_in_switch_case
		0,	// while_statement_always_in_block
		TAB	// tab
	};

struct SOURCE_PRINTER_DATA_STRUCT {
	FILE* stream;
	const struct SOURCE_PRINTER_FORMAT_STRUCT* format;
	ast_error_handler error_handler;
	int tab_num;
	int is_on_new_line : 1;
	int comment_printed : 1;
	int need_space_before : 1;
};

void repeat_print(int num, FILE* stream, char* string) {
	int count;
	for (count = 0; count < num; count++)
		fprintf(stream, string);
}

AST_HANDLER_RESULT ast_print_source_pre_handler(struct AST_NODE* ast_node,
	void* data)
{
	
#define OUTPUT(...) \
	(source_printer_data->is_on_new_line \
			? repeat_print(source_printer_data->tab_num, stream, format->tab) \
			: NULL, \
	source_printer_data->need_space_before \
			? (fprintf(stream, " "), \
					source_printer_data->need_space_before = 0) \
			: NULL, \
	fprintf(stream, __VA_ARGS__), \
	source_printer_data->is_on_new_line = 0)

#define SUB_VISIT(ast_node) { \
	if (ast_node != NULL) \
		if (!visit(ast_node, ast_print_source_pre_handler, NULL, \
				source_printer_data->error_handler, data)) \
			return AST_VISIT_CANCEL; \
}

#define SUB_VISIT_EXPRESSION(child) { \
	if (get_operator_precedence(ast_node) < get_operator_precedence(child)) { \
		if (child->comment != NULL) { \
			if (!source_printer_data->is_on_new_line) { \
				OUTPUT("\n"); \
				print_comments(stream, child->comment, format->tab, \
					source_printer_data->tab_num); \
			} else \
				print_comments(stream, child->comment, format->tab, \
					source_printer_data->tab_num); \
			source_printer_data->is_on_new_line = 1; \
		} \
		OUTPUT("("); \
		if (format->space_for_parenthesized_expression) \
			source_printer_data->need_space_before = 1; \
		source_printer_data->comment_printed = 1; \
	} \
	SUB_VISIT(child); \
	if (get_operator_precedence(ast_node) < get_operator_precedence(child)) \
		OUTPUT("%s)", SPACE(space_for_parenthesized_expression)); \
}

#define POST_VISIT() {}

#define SPACE(format_space_field) (format->format_space_field ? " " : "")

	struct SOURCE_PRINTER_DATA_STRUCT* source_printer_data =
		(struct SOURCE_PRINTER_DATA_STRUCT*) data;
	FILE* stream = source_printer_data->stream;
	const struct SOURCE_PRINTER_FORMAT_STRUCT* format =
		source_printer_data->format;
	const char* str;
	
	if (ast_node->comment != NULL && !source_printer_data->comment_printed) {
		source_printer_data->need_space_before = 0;
		if (!source_printer_data->is_on_new_line) {
			OUTPUT("\n");
			print_comments(stream, ast_node->comment, format->tab,
				source_printer_data->tab_num);
		} else
			print_comments(stream, ast_node->comment, format->tab,
				source_printer_data->tab_num);
		source_printer_data->is_on_new_line = 1;
	}
	source_printer_data->comment_printed = 0;
	
	switch (ast_node->type) {
		case IDENTIFIER:
			OUTPUT(ast_node->identifier.id);
			break;
		case CONSTANT:
			OUTPUT(ast_node->constant.text);
			break;
		case STRING_LITERAL:
			OUTPUT("%s", ast_node->string_literal.text);
			break;
		case ARRAY_ACCESS:
			SUB_VISIT_EXPRESSION(ast_node->array_access.array);
			OUTPUT("[");
			source_printer_data->need_space_before =
				format->space_for_array_access;
			SUB_VISIT_EXPRESSION(ast_node->array_access.index);
			OUTPUT("%s]", SPACE(space_for_array_access));
			break;
		case ARGUMENT_EXPRESSION_LIST:
			SUB_VISIT(ast_node->argument_expression_list
					.left_argument_expression_list);
			if (ast_node->argument_expression_list.left_argument_expression_list
					!= NULL) {
				OUTPUT(",");
				source_printer_data->need_space_before =
					format->space_between_arguments;
			}
			SUB_VISIT(ast_node->argument_expression_list.assignment_expression);
			break;
		case PARAMETERIZED_EXPRESSION:
			SUB_VISIT_EXPRESSION(ast_node->parameterized_expression.expression);
			OUTPUT("(");
			source_printer_data->need_space_before =
				format->space_for_parameterized_expression;
			SUB_VISIT(ast_node->parameterized_expression.argument_list);
			OUTPUT("%s)", SPACE(space_for_parameterized_expression));
			break;
		case FIELD_ACCESS:
			SUB_VISIT_EXPRESSION(ast_node->field_access.expression);
			if (ast_node->field_access.is_pointer_access) {
				OUTPUT("%s->", SPACE(space_for_pointer_field_access));
				source_printer_data->need_space_before =
					format->space_for_pointer_field_access;
			} else {
				OUTPUT("%s.", SPACE(space_for_dot_field_access));
				source_printer_data->need_space_before =
					format->space_for_dot_field_access;
			}
			OUTPUT(ast_node->field_access.identifier);
			break;
		case INC_DEC_EXPRESSION:
			if (ast_node->inc_dec_expression.inc_dec_type == PREFIX_INC) {
				OUTPUT("++");
				source_printer_data->need_space_before =
					format->space_for_unary_operation;
			} else if (ast_node->inc_dec_expression.inc_dec_type
					== PREFIX_DEC) {
				OUTPUT("--");
				source_printer_data->need_space_before =
					format->space_for_unary_operation;
			}
			SUB_VISIT_EXPRESSION(ast_node->inc_dec_expression.operand);
			if (ast_node->inc_dec_expression.inc_dec_type == POSTFIX_INC)
				OUTPUT("%s++", SPACE(space_for_unary_operation));
			else if (ast_node->inc_dec_expression.inc_dec_type == POSTFIX_DEC)
				OUTPUT("%s--", SPACE(space_for_unary_operation));
			break;
		case STRUCT_CONSTANT:
			OUTPUT("(");
			source_printer_data->need_space_before =
				format->space_for_type_name;
			SUB_VISIT(ast_node->struct_constant.struct_type);
			OUTPUT("%s)", SPACE(space_for_type_name));
			if (format->block_curly_on_new_line) {
				OUTPUT("\n");
				source_printer_data->is_on_new_line = 1;
				OUTPUT("{\n");
			} else
				OUTPUT("%s{\n", SPACE(space_for_type_cast));
			source_printer_data->is_on_new_line = 1;
			source_printer_data->tab_num++;
			SUB_VISIT(ast_node->struct_constant.initializer_list);
			source_printer_data->tab_num--;
			OUTPUT("\n");
			source_printer_data->is_on_new_line = 1;
			OUTPUT("}");
			break;
		case UNARY_EXPRESSION:
			OUTPUT("%c", ast_node->unary_expression.operator);
			source_printer_data->need_space_before =
				format->space_for_unary_operation;
			SUB_VISIT_EXPRESSION(ast_node->unary_expression.operand);
			break;
		case SIZEOF_EXPRESSION:
			if (ast_node->sizeof_expression.mode) {
				OUTPUT("sizeof%s(", SPACE(space_for_type_name));
				source_printer_data->need_space_before =
					format->space_for_type_name;
			} else {
				OUTPUT("sizeof");
				source_printer_data->need_space_before = 1;
			}
			SUB_VISIT_EXPRESSION(
				ast_node->sizeof_expression.expression_or_type);
			if (ast_node->sizeof_expression.mode)
				OUTPUT("%s)", SPACE(space_for_type_name));
			break;
		case CAST_EXPRESSION:
			OUTPUT("(");
			source_printer_data->need_space_before =
				format->space_for_type_name;
			SUB_VISIT(ast_node->cast_expression.cast_type);
			OUTPUT("%s)", SPACE(space_for_type_name));
			source_printer_data->need_space_before =
				format->space_for_type_cast;
			SUB_VISIT_EXPRESSION(ast_node->cast_expression.operand);
			break;
		case BINARY_EXPRESSION:
			SUB_VISIT_EXPRESSION(ast_node->binary_expression.left_operand);
			source_printer_data->need_space_before =
				format->space_for_binary_operation;
			str = get_operator_string(ast_node->binary_expression.operator);
			if (str == NULL)
				OUTPUT("%c", ast_node->binary_expression.operator);
			else
				OUTPUT(str);
			source_printer_data->need_space_before =
				format->space_for_binary_operation;
			SUB_VISIT_EXPRESSION(ast_node->binary_expression.right_operand);
			break;
		case CONDITIONAL_EXPRESSION:
			SUB_VISIT_EXPRESSION(ast_node->conditional_expression.operand1);
			OUTPUT("%s?", SPACE(space_for_comma_expression));
			source_printer_data->need_space_before =
				format->space_for_comma_expression;
			SUB_VISIT_EXPRESSION(ast_node->conditional_expression.operand2);
			OUTPUT("%s:", SPACE(space_for_comma_expression));
			source_printer_data->need_space_before =
				format->space_for_comma_expression;
			SUB_VISIT_EXPRESSION(ast_node->conditional_expression.operand3);
			break;
		case COMMA_EXPRESSION:
			SUB_VISIT_EXPRESSION(ast_node->comma_expression.left_expression);
			OUTPUT(",");
			source_printer_data->need_space_before =
				format->space_for_comma_expression;
			SUB_VISIT_EXPRESSION(
				ast_node->comma_expression.assignment_expression);
			break;
		case DECLARATION:
			SUB_VISIT(ast_node->declaration.declaration_specifiers);
			source_printer_data->need_space_before =
				ast_node->declaration.init_declarator_list != NULL;
			SUB_VISIT(ast_node->declaration.init_declarator_list);
			OUTPUT(";");
			if (ast_node->parent->type != FOR_STATEMENT
					|| ast_node != ast_node->parent->for_statement
					.declaration_or_expression_statement) {
				OUTPUT("\n");
				source_printer_data->is_on_new_line = 1;
			}
			break;
		case STORAGE_CLASS_SPECIFIER:
			switch (ast_node->storage_class_specifier.storage_class_specifier) {
				case TYPEDEF:
					OUTPUT("typedef");
					break;
				case EXTERN:
					OUTPUT("extern");
					break;
				case STATIC:
					OUTPUT("static");
					break;
				case AUTO:
					OUTPUT("auto");
					break;
				case REGISTER:
					OUTPUT("register");
					break;
			}
			break;
		case STRUCT_OR_UNION:
			if (ast_node->struct_or_union.is_struct)
				OUTPUT("struct");
			else
				OUTPUT("union");
			if (ast_node->struct_or_union.identifier != NULL)
				OUTPUT(" %s", ast_node->struct_or_union.identifier);
			if (ast_node->struct_or_union.struct_declaration_list != NULL) {
				if (format->block_curly_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
					OUTPUT("{\n");
				} else
					OUTPUT(" {\n");
				source_printer_data->is_on_new_line = 1;
				source_printer_data->tab_num++;
				SUB_VISIT(ast_node->struct_or_union.struct_declaration_list);
				source_printer_data->tab_num--;
				OUTPUT("}");
			}
			break;
		case STRUCT_DECLARATION_LIST:
			SUB_VISIT(ast_node->struct_declaration_list
				.left_struct_declaration_list);
			SUB_VISIT(ast_node->struct_declaration_list.struct_declaration);
			break;
		case STRUCT_DECLARATION:
			SUB_VISIT(ast_node->struct_declaration.struct_qualifier_list);
			source_printer_data->need_space_before =
				ast_node->struct_declaration.struct_declaration_list != NULL;
			SUB_VISIT(ast_node->struct_declaration.struct_declaration_list);
			OUTPUT(";\n");
			source_printer_data->is_on_new_line = 1;
			break;
		case PRIMITIVE_TYPE_SPECIFIER:
			switch (ast_node->primitive_type_specifier.primitive_type) {
				case VOID:
					OUTPUT("void");
					break;
				case CHAR:
					OUTPUT("char");
					break;
				case SHORT:
					OUTPUT("short");
					break;
				case INT:
					OUTPUT("int");
					break;
				case LONG:
					OUTPUT("long");
					break;
				case FLOAT:
					OUTPUT("float");
					break;
				case DOUBLE:
					OUTPUT("double");
					break;
				case SIGNED:
					OUTPUT("signed");
					break;
				case UNSIGNED:
					OUTPUT("unsigned");
					break;
				case BOOLEAN:
					OUTPUT("_Bool");
					break;
				case COMPLEX:
					OUTPUT("_Complex");
					break;
				case IMAGINARY:
					OUTPUT("_Imaginary");
					break;
			}
			break;
		case NAMED_TYPE_SPECIFIER:
			OUTPUT(ast_node->named_type_specifier.identifier);
			break;
		case DECLARATION_SPECIFIERS:
			SUB_VISIT(ast_node->declaration_specifiers.specifier_or_qualifier);
			source_printer_data->need_space_before =
				ast_node->declaration_specifiers.more_declaration_specifiers
					!= NULL;
			SUB_VISIT(
				ast_node->declaration_specifiers.more_declaration_specifiers);
			break;
		case INIT_DECLARATOR_LIST:
			SUB_VISIT(ast_node->init_declarator_list.left_init_declarator_list);
			if (ast_node->init_declarator_list.left_init_declarator_list
					!= NULL) {
				OUTPUT(",");
				source_printer_data->need_space_before =
					format->space_between_declarators;
			}
			SUB_VISIT(ast_node->init_declarator_list.init_declarator);
			break;
		case INIT_DECLARATOR:
			SUB_VISIT(ast_node->init_declarator.declarator);
			if (ast_node->init_declarator.initializer != NULL) {
				OUTPUT("%s=", SPACE(space_for_initializer));
				source_printer_data->need_space_before =
					format->space_for_initializer;
			}
			SUB_VISIT(ast_node->init_declarator.initializer);
			break;
		case SPECIFIER_QUALIFIER_LIST:
			SUB_VISIT(
				ast_node->specifier_qualifier_list.specifier_or_qualifier);
			source_printer_data->need_space_before =
				ast_node->specifier_qualifier_list
						.right_specifier_qualifier_list != NULL;
			SUB_VISIT(ast_node->specifier_qualifier_list
					.right_specifier_qualifier_list);
			break;
		case STRUCT_DECLARATOR_LIST:
			SUB_VISIT(ast_node->struct_declarator_list.struct_declarator);
			if (ast_node->struct_declarator_list.right_struct_declarator_list
					!= NULL) {
				OUTPUT(",");
				source_printer_data->need_space_before =
					format->space_between_declarators;
			}
			SUB_VISIT(ast_node->struct_declarator_list
					.right_struct_declarator_list);
			break;
		case STRUCT_DECLARATOR:
			SUB_VISIT(ast_node->struct_declarator.declarator);
			if (ast_node->struct_declarator.constant_expression != NULL) {
				OUTPUT("%s:", SPACE(space_for_field_size));
				source_printer_data->need_space_before =
					format->space_for_field_size;
			}
			SUB_VISIT(ast_node->struct_declarator.constant_expression);
			break;
		case ENUM_SPECIFIER:
			OUTPUT("enum");
			if (ast_node->enum_specifier.identifier != NULL)
				OUTPUT(" %s", ast_node->enum_specifier.identifier);
			if (ast_node->enum_specifier.enumerator_list != NULL) {
				if (format->block_curly_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
					OUTPUT("{\n");
				} else
					OUTPUT(" {\n");
				source_printer_data->is_on_new_line = 1;
				source_printer_data->tab_num++;
				SUB_VISIT(ast_node->enum_specifier.enumerator_list);
				source_printer_data->tab_num--;
				if (format->block_curly_on_new_line
						|| format->enum_item_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
				} else
					source_printer_data->need_space_before = 1;
				OUTPUT("}");
			}
			break;
		case ENUMERATOR_LIST:
			SUB_VISIT(ast_node->enumerator_list.left_enumerator_list);
			if (ast_node->enumerator_list.left_enumerator_list != NULL) {
				OUTPUT(",");
				if (format->enum_item_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
				} else
					source_printer_data->need_space_before =
						format->space_between_declarators;
			}
			SUB_VISIT(ast_node->enumerator_list.enumerator);
			break;
		case ENUMERATOR:
			OUTPUT(ast_node->enumerator.identifier);
			if (ast_node->enumerator.constant_expression != NULL) {
				OUTPUT("%s=", SPACE(space_for_initializer));
				source_printer_data->need_space_before =
					format->space_for_initializer;
			}
			SUB_VISIT(ast_node->enumerator.constant_expression);
			break;
		case TYPE_QUALIFIER:
			switch (ast_node->type_qualifier.qualifier) {
				case CONST:
					OUTPUT("const");
					break;
				case RESTRICT:
					OUTPUT("restrict");
					break;
				case VOLATILE:
					OUTPUT("volatile");
					break;
			}
			break;
		case FUNCTION_SPECIFIER:
			switch (ast_node->function_specifier.specifier) {
				case INLINE:
					OUTPUT("inline");
					break;
			}
			break;
		case POINTER:
			OUTPUT("*");
			source_printer_data->need_space_before =
				ast_node->pointer.type_qualifier_list != NULL
						&& format->space_for_pointer;
			SUB_VISIT(ast_node->pointer.type_qualifier_list);
			source_printer_data->need_space_before =
				ast_node->pointer.right_pointer != NULL
						&& (ast_node->pointer.type_qualifier_list != NULL
								|| format->space_for_pointer);
			SUB_VISIT(ast_node->pointer.right_pointer);
			break;
		case TYPE_QUALIFIER_LIST:
			SUB_VISIT(ast_node->type_qualifier_list.left_type_qualifier_list);
			source_printer_data->need_space_before =
				ast_node->type_qualifier_list.left_type_qualifier_list != NULL;
			SUB_VISIT(ast_node->type_qualifier_list.type_qualifier);
			break;
		case DECLARATOR:
			SUB_VISIT(ast_node->declarator.pointer);
			SUB_VISIT(ast_node->declarator.direct_declarator);
			break;
		case NAMED_DIRECT_DECLARATOR:
			OUTPUT(ast_node->named_direct_declarator.identifier);
			break;
		case ARRAY_DIRECT_DECLARATOR:
			if (ast_node->array_direct_declarator.direct_declarator
					!= NULL && ast_node->array_direct_declarator
					.direct_declarator->type == DECLARATOR) {
				OUTPUT("(");
				source_printer_data->need_space_before =
					format->space_for_parenthesized_expression;
			}
			SUB_VISIT(ast_node->array_direct_declarator.direct_declarator);
			if (ast_node->array_direct_declarator.direct_declarator
					!= NULL && ast_node->array_direct_declarator
					.direct_declarator->type == DECLARATOR)
				OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			
			OUTPUT("[");
			source_printer_data->need_space_before =
				format->space_for_array_access;
			if (ast_node->array_direct_declarator.is_type_qualifier_static) {
				OUTPUT("static");
				source_printer_data->need_space_before = 1;
			}
			SUB_VISIT(ast_node->array_direct_declarator.type_qualifier_list);
			
			source_printer_data->need_space_before =
				ast_node->array_direct_declarator.type_qualifier_list != NULL
						&& ast_node->array_direct_declarator
								.assignment_expression != NULL;
			
			if (ast_node->array_direct_declarator
					.is_assignment_expression_static) {
				OUTPUT("static");
				source_printer_data->need_space_before = 1;
			}
			SUB_VISIT(ast_node->array_direct_declarator.assignment_expression);
			
			if (ast_node->array_direct_declarator.has_pointer)
				OUTPUT("*");
			
			source_printer_data->need_space_before =
				format->space_for_array_access
						&& (ast_node->array_direct_declarator
						.type_qualifier_list != NULL
						|| ast_node->array_direct_declarator
						.assignment_expression != NULL
						|| ast_node->array_direct_declarator.has_pointer);
			OUTPUT("]");
			break;
		case PARAMETERIZED_DIRECT_DECLARATOR:
			if (ast_node->parameterized_direct_declarator.direct_declarator
					!= NULL && ast_node->parameterized_direct_declarator
					.direct_declarator->type == DECLARATOR) {
				OUTPUT("(");
				source_printer_data->need_space_before =
					format->space_for_parenthesized_expression;
			}
			SUB_VISIT(ast_node->parameterized_direct_declarator
					.direct_declarator);
			if (ast_node->parameterized_direct_declarator.direct_declarator
					!= NULL && ast_node->parameterized_direct_declarator
					.direct_declarator->type == DECLARATOR)
				OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			
			OUTPUT("(");
			source_printer_data->need_space_before =
				format->space_for_parameterized_expression;
			SUB_VISIT(ast_node->parameterized_direct_declarator
					.parameter_type_list);
			SUB_VISIT(ast_node->parameterized_direct_declarator
					.identifier_list);
			source_printer_data->need_space_before =
				format->space_for_parameterized_expression
						&& (ast_node->parameterized_direct_declarator
						.parameter_type_list != NULL
						|| ast_node->parameterized_direct_declarator
						.identifier_list != NULL);
			OUTPUT(")");
			break;
		case PARAMETER_LIST:
			SUB_VISIT(ast_node->parameter_list.left_parameter_list);
			if (ast_node->parameter_list.left_parameter_list != NULL) {
				OUTPUT(",");
				source_printer_data->need_space_before =
					format->space_between_arguments;
			}
			SUB_VISIT(ast_node->parameter_list.parameter_declaration);
			if (ast_node->parameter_list.has_ellipsis)
				OUTPUT(",%s...", SPACE(space_between_arguments));
			break;
		case PARAMETER_DECLARATION:
			SUB_VISIT(ast_node->parameter_declaration.declaration_specifiers);
			if (ast_node->parameter_declaration.declarator != NULL
					|| (ast_node->parameter_declaration.abstract_declarator
							!= NULL && format->space_for_abstract_declarator))
				source_printer_data->need_space_before = 1;
			SUB_VISIT(ast_node->parameter_declaration.declarator);
			SUB_VISIT(ast_node->parameter_declaration.abstract_declarator);
			break;
		case IDENTIFIER_LIST:
			SUB_VISIT(ast_node->identifier_list.left_identifier_list);
			if (ast_node->identifier_list.left_identifier_list) {
				OUTPUT(",");
				source_printer_data->need_space_before =
					format->space_between_declarators;
			}
			OUTPUT(ast_node->identifier_list.identifier);
			break;
		case TYPE_NAME:
			SUB_VISIT(ast_node->type_name.specifier_qualifier_list);
			if (ast_node->type_name.abstract_declarator != NULL
					&& format->space_for_abstract_declarator)
				source_printer_data->need_space_before = 1;
			SUB_VISIT(ast_node->type_name.abstract_declarator);
			break;
		case ABSTRACT_DECLARATOR:
			SUB_VISIT(ast_node->abstract_declarator.pointer);
			if (ast_node->abstract_declarator.pointer != NULL
					&& ast_node->abstract_declarator.direct_abstract_declarator
							!= NULL
					&& format->space_for_abstract_declarator)
				source_printer_data->need_space_before = 1;
			if (ast_node->abstract_declarator.direct_abstract_declarator
					!= NULL
					&& ast_node->abstract_declarator.direct_abstract_declarator
							->type == ABSTRACT_DECLARATOR) {
				OUTPUT("(");
				source_printer_data->need_space_before =
					format->space_for_parenthesized_expression;
			}
			SUB_VISIT(ast_node->abstract_declarator.direct_abstract_declarator);
			if (ast_node->abstract_declarator.direct_abstract_declarator
					!= NULL
					&& ast_node->abstract_declarator.direct_abstract_declarator
							->type == ABSTRACT_DECLARATOR)
				OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			break;
		case ARRAY_DIRECT_ABSTRACT_DECLARATOR:
			if (ast_node->array_direct_abstract_declarator
					.direct_abstract_declarator != NULL
					&& ast_node->array_direct_abstract_declarator
							.direct_abstract_declarator->type
							== ABSTRACT_DECLARATOR) {
				OUTPUT("(");
				source_printer_data->need_space_before =
					format->space_for_parenthesized_expression;
			}
			SUB_VISIT(ast_node->array_direct_abstract_declarator
					.direct_abstract_declarator);
			if (ast_node->array_direct_abstract_declarator
					.direct_abstract_declarator != NULL
					&& ast_node->array_direct_abstract_declarator
							.direct_abstract_declarator->type
							== ABSTRACT_DECLARATOR)
				OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
					
			OUTPUT("[");
			source_printer_data->need_space_before =
				format->space_for_array_access;
			SUB_VISIT(ast_node->array_direct_abstract_declarator
					.assignment_expression);
			if (ast_node->array_direct_abstract_declarator.has_pointer)
				OUTPUT("*");
			if (format->space_for_array_access
					&& (ast_node->array_direct_abstract_declarator.has_pointer
					|| ast_node->array_direct_abstract_declarator
					.assignment_expression != NULL))
				source_printer_data->need_space_before = 1;
			OUTPUT("]");
			break;
		case PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR:
			if (ast_node->parameterized_direct_abstract_declarator
					.direct_abstract_declarator != NULL
					&& ast_node->array_direct_abstract_declarator
							.direct_abstract_declarator->type
							== ABSTRACT_DECLARATOR) {
				OUTPUT("(");
				source_printer_data->need_space_before =
					format->space_for_parenthesized_expression;
			}
			SUB_VISIT(ast_node->parameterized_direct_abstract_declarator
					.direct_abstract_declarator);
			if (ast_node->parameterized_direct_abstract_declarator
					.direct_abstract_declarator != NULL
					&& ast_node->array_direct_abstract_declarator
							.direct_abstract_declarator->type
							== ABSTRACT_DECLARATOR)
				OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			
			OUTPUT("(");
			source_printer_data->need_space_before =
				format->space_for_parameterized_expression;
			SUB_VISIT(ast_node->parameterized_direct_abstract_declarator
					.parameter_type_list);
			if (format->space_for_parameterized_expression
					&& ast_node->parameterized_direct_abstract_declarator
					.parameter_type_list != NULL)
				source_printer_data->need_space_before = 1;
			OUTPUT("%s)", SPACE(space_for_parameterized_expression));
			break;
		case INITIALIZER_LIST:
			SUB_VISIT(ast_node->initializer_list.left_initializer_list);
			if (ast_node->initializer_list.left_initializer_list != NULL) {
				if (format->initializer_on_new_line) {
					OUTPUT(",\n");
					source_printer_data->is_on_new_line = 1;
				} else {
					OUTPUT(",");
					source_printer_data->need_space_before = 1;
				}
			}
			
			SUB_VISIT(ast_node->initializer_list.designation);
			if (ast_node->initializer_list.designation != NULL) {
				OUTPUT("%s=", SPACE(space_for_initializer));
				source_printer_data->need_space_before =
					format->space_for_initializer;
			}
			SUB_VISIT(ast_node->initializer_list.initializer);
			break;
		case DESIGNATOR_LIST:
			SUB_VISIT(ast_node->designator_list.left_designator_list);
			source_printer_data->need_space_before =
				ast_node->designator_list.left_designator_list != NULL;
			SUB_VISIT(ast_node->designator_list.designator);
			break;
		case ARRAY_DESIGNATOR:
			OUTPUT("[");
			source_printer_data->need_space_before =
				format->space_for_array_access;
			SUB_VISIT(ast_node->array_designator.constant_expression);
			OUTPUT("%s]", SPACE(space_for_array_access));
			break;
		case DOT_DESIGNATOR:
			OUTPUT(".%s", ast_node->dot_designator.identifier);
			break;
		case IDENTIFIER_LABELED_STATEMENT:
			OUTPUT("%s:\n", ast_node->identifier_labeled_statement.identifier);
			source_printer_data->is_on_new_line = 1;
			SUB_VISIT(ast_node->identifier_labeled_statement.statement);
			break;
		case CASE_LABELED_STATEMENT:
			source_printer_data->tab_num--;
			if (ast_node->case_labeled_statement.constant_expression == NULL)
				OUTPUT("default:\n");
			else {
				OUTPUT("case");
				source_printer_data->need_space_before = 1;
				SUB_VISIT(ast_node->case_labeled_statement.constant_expression);
				OUTPUT(":\n");
			}
			source_printer_data->tab_num++;
			source_printer_data->is_on_new_line = 1;
			SUB_VISIT(ast_node->case_labeled_statement.statement);
			break;
		case BLOCK_ITEM_LIST:
			SUB_VISIT(ast_node->block_item_list.left_block_item_list);
			SUB_VISIT(ast_node->block_item_list.declaration_or_statement);
			break;
		case EXPRESSION_STATEMENT:
			SUB_VISIT(ast_node->expression_statement.expression);
			OUTPUT(";");
			if (ast_node->parent->type != FOR_STATEMENT
					|| (ast_node != ast_node->parent->for_statement
					.declaration_or_expression_statement
					&& ast_node != ast_node->parent->for_statement
					.expression_statement)) {
				OUTPUT("\n");
				source_printer_data->is_on_new_line = 1;
			}
			break;
		case IF_STATEMENT:
			OUTPUT("if (");
			source_printer_data->need_space_before =
				format->space_for_parenthesized_expression;
			SUB_VISIT(ast_node->if_statement.expression);
			OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			
			if (ast_node->if_statement.if_statement->type == COMPOUND_STATEMENT
					|| format->if_branch_always_in_block) {
				if (format->block_curly_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
				} else {
					source_printer_data->need_space_before =
						format->space_for_block_curly;
				}
				OUTPUT("{\n");
			} else
				OUTPUT("\n");
			source_printer_data->is_on_new_line = 1;
			source_printer_data->tab_num++;
			SUB_VISIT(ast_node->if_statement.if_statement);
			source_printer_data->tab_num--;
			
			if (ast_node->if_statement.if_statement->type == COMPOUND_STATEMENT
					|| format->if_branch_always_in_block) {
				OUTPUT("}");
				if (ast_node->if_statement.else_statement == NULL) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
				}
			}
			
			if (ast_node->if_statement.else_statement != NULL) {
				if (ast_node->if_statement.if_statement->type
						== COMPOUND_STATEMENT
						|| format->if_branch_always_in_block) {
					if (format->block_curly_on_new_line) {
						OUTPUT("\n");
						source_printer_data->is_on_new_line = 1;
						OUTPUT("else\n");
						source_printer_data->is_on_new_line = 1;
						if (ast_node->if_statement.else_statement->type
								== COMPOUND_STATEMENT
								|| format->if_branch_always_in_block) {
							OUTPUT("{\n");
							source_printer_data->is_on_new_line = 1;
						}
					} else {
						OUTPUT(" else");
						if (ast_node->if_statement.else_statement->type
								== COMPOUND_STATEMENT
								|| format->if_branch_always_in_block)
							OUTPUT(" {\n");
						else
							OUTPUT("\n");
						source_printer_data->is_on_new_line = 1;
					}
				} else {
					if (ast_node->if_statement.if_statement->type
							== COMPOUND_STATEMENT
							|| format->if_branch_always_in_block) {
						if (format->block_curly_on_new_line) {
							OUTPUT("\n");
							source_printer_data->is_on_new_line = 1;
							OUTPUT("else");
						} else
							OUTPUT(" else");
					} else
						OUTPUT("else");
					if (ast_node->if_statement.else_statement->type
							== COMPOUND_STATEMENT
							|| format->if_branch_always_in_block) {
						if (format->block_curly_on_new_line) {
							OUTPUT("\n");
							source_printer_data->is_on_new_line = 1;
						} else
							source_printer_data->need_space_before = 1;
						OUTPUT("{\n");
					} else
						OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
				}
				source_printer_data->tab_num++;
				SUB_VISIT(ast_node->if_statement.else_statement);
				source_printer_data->tab_num--;
				if (ast_node->if_statement.else_statement->type
						== COMPOUND_STATEMENT
						|| format->if_branch_always_in_block) {
					OUTPUT("}\n");
					source_printer_data->is_on_new_line = 1;
				}
			}
			break;
		case SWITCH_STATEMENT:
			OUTPUT("switch (");
			source_printer_data->need_space_before =
				format->space_for_parenthesized_expression;
			SUB_VISIT(ast_node->switch_statement.expression);
			OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			
			if (format->block_curly_on_new_line) {
				OUTPUT("\n");
				source_printer_data->is_on_new_line = 1;
				OUTPUT("{\n");
			} else
				OUTPUT(" {\n");
			source_printer_data->is_on_new_line = 1;
			source_printer_data->tab_num++;
			if (format->tab_in_switch_case)
				source_printer_data->tab_num++;
			SUB_VISIT(ast_node->switch_statement.statement);
			source_printer_data->tab_num--;
			if (format->tab_in_switch_case)
				source_printer_data->tab_num--;
			OUTPUT("}\n");
			source_printer_data->is_on_new_line = 1;
			break;
		case WHILE_STATEMENT:
			OUTPUT("while (");
			source_printer_data->need_space_before =
				format->space_for_parenthesized_expression;
			SUB_VISIT(ast_node->while_statement.expression);
			OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			
			if (ast_node->while_statement.statement->type == COMPOUND_STATEMENT
					|| format->while_statement_always_in_block) {
				if (format->block_curly_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
					OUTPUT("{\n");
				} else
					OUTPUT(" {\n");
			} else
				OUTPUT("\n");
			source_printer_data->is_on_new_line = 1;
			source_printer_data->tab_num++;
			SUB_VISIT(ast_node->while_statement.statement);
			source_printer_data->tab_num--;
			if (ast_node->while_statement.statement->type == COMPOUND_STATEMENT
					|| format->while_statement_always_in_block) {
				OUTPUT("}\n");
				source_printer_data->is_on_new_line = 1;
			}
			break;
		case DO_WHILE_STATEMENT:
			OUTPUT("do");
			
			if (ast_node->do_while_statement.statement->type
					== COMPOUND_STATEMENT
					|| format->do_while_statement_always_in_block) {
				if (format->block_curly_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
					OUTPUT("{\n");
				} else
					OUTPUT(" {\n");
			} else
				OUTPUT("\n");
			source_printer_data->is_on_new_line = 1;
			source_printer_data->tab_num++;
			SUB_VISIT(ast_node->do_while_statement.statement);
			source_printer_data->tab_num--;
			if (ast_node->do_while_statement.statement->type
					== COMPOUND_STATEMENT
					|| format->do_while_statement_always_in_block) {
				OUTPUT("}");
				source_printer_data->need_space_before = 1;
			}
			OUTPUT("while (");
			source_printer_data->need_space_before =
				format->space_for_parenthesized_expression;
			SUB_VISIT(ast_node->do_while_statement.expression);
			OUTPUT("%s);\n", SPACE(space_for_parenthesized_expression));
			source_printer_data->is_on_new_line = 1;
			break;
		case FOR_STATEMENT:
			OUTPUT("for (");
			source_printer_data->need_space_before =
				format->space_for_parenthesized_expression;
			SUB_VISIT(ast_node->for_statement
					.declaration_or_expression_statement);
			source_printer_data->need_space_before = 1;
			SUB_VISIT(ast_node->for_statement.expression_statement);
			source_printer_data->need_space_before = 1;
			SUB_VISIT(ast_node->for_statement.expression);
			if (ast_node->for_statement.expression != NULL)
				OUTPUT("%s)", SPACE(space_for_parenthesized_expression));
			else
				OUTPUT(")");
			if (ast_node->for_statement.statement->type == COMPOUND_STATEMENT
					|| format->for_statement_always_in_block) {
				if (format->block_curly_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
					OUTPUT("{\n");
				} else
					OUTPUT(" {\n");
			} else
				OUTPUT("\n");
			source_printer_data->is_on_new_line = 1;
			source_printer_data->tab_num++;
			SUB_VISIT(ast_node->for_statement.statement);
			source_printer_data->tab_num--;
			if (ast_node->for_statement.statement->type == COMPOUND_STATEMENT
					|| format->for_statement_always_in_block) {
				OUTPUT("}\n");
				source_printer_data->is_on_new_line = 1;
			}
			break;
		case GOTO_STATEMENT:
			OUTPUT("goto %s;\n", ast_node->goto_statement.identifier);
			source_printer_data->is_on_new_line = 1;
			break;
		case LOOP_JUMP_STATEMENT:
			OUTPUT("%s;\n", ast_node->loop_jump_statement.loop_jump_type
					== CONTINUE_STATEMENT ? "continue" : "break");
			source_printer_data->is_on_new_line = 1;
			break;
		case RETURN_STATEMENT:
			OUTPUT("return");
			if (ast_node->return_statement.expression != NULL) {
				if (format->parenthesize_return_statement) {
					OUTPUT("(");
					source_printer_data->need_space_before =
						format->space_for_parameterized_expression;
				} else
					source_printer_data->need_space_before = 1;
				SUB_VISIT(ast_node->return_statement.expression);
				if (format->parenthesize_return_statement)
					OUTPUT("%s)", SPACE(space_for_parameterized_expression));
			}
			OUTPUT(";\n");
			source_printer_data->is_on_new_line = 1;
			break;
		case TRANSLATION_UNIT:
			SUB_VISIT(ast_node->translation_unit.left_translation_unit);
			if (ast_node->translation_unit.left_translation_unit != NULL)
				if (format->empty_line_between_declarations) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
				}
			SUB_VISIT(ast_node->translation_unit.external_declaration);
			break;
		case FUNCTION_DEFINITION:
			SUB_VISIT(ast_node->function_definition.declaration_specifiers);
			source_printer_data->need_space_before =
				ast_node->function_definition.declaration_specifiers != NULL;
			SUB_VISIT(ast_node->function_definition.declarator);
			if (ast_node->function_definition.declaration_list != NULL) {
				OUTPUT("\n");
				source_printer_data->is_on_new_line = 1;
			}

			if (format->tab_in_formal_parameter_declaration)
				source_printer_data->tab_num++;
			SUB_VISIT(ast_node->function_definition.declaration_list);
			if (format->tab_in_formal_parameter_declaration)
				source_printer_data->tab_num--;
			
			if (ast_node->function_definition.declaration_list == NULL) {
				if (format->function_curly_on_new_line) {
					OUTPUT("\n");
					source_printer_data->is_on_new_line = 1;
				} else
					source_printer_data->need_space_before = 1;
				OUTPUT("{\n");
				source_printer_data->is_on_new_line = 1;
			}
			source_printer_data->tab_num++;
			SUB_VISIT(ast_node->function_definition.compound_statement);
			source_printer_data->tab_num--;
			OUTPUT("}");
			OUTPUT("\n");
			source_printer_data->is_on_new_line = 1;
			
			if (format->empty_line_between_declarations) {
				OUTPUT("\n");
				source_printer_data->is_on_new_line = 1;
			}
			break;
		case DECLARATION_LIST:
			SUB_VISIT(ast_node->declaration_list.left_declaration_list);
			SUB_VISIT(ast_node->declaration_list.declaration);
			break;
		case COMPOUND_STATEMENT:
			if (ast_node->parent->type != FUNCTION_DEFINITION
					&& ast_node->parent->type != IF_STATEMENT
					&& ast_node->parent->type != SWITCH_STATEMENT
					&& ast_node->parent->type != WHILE_STATEMENT
					&& ast_node->parent->type != DO_WHILE_STATEMENT
					&& ast_node->parent->type != FOR_STATEMENT) {
				OUTPUT("{\n");
				source_printer_data->is_on_new_line = 1;
				source_printer_data->tab_num++;
			}
			SUB_VISIT(ast_node->compound_statement.block_item_list);
			if (ast_node->parent->type != FUNCTION_DEFINITION
					&& ast_node->parent->type != IF_STATEMENT
					&& ast_node->parent->type != SWITCH_STATEMENT
					&& ast_node->parent->type != WHILE_STATEMENT
					&& ast_node->parent->type != DO_WHILE_STATEMENT
					&& ast_node->parent->type != FOR_STATEMENT) {
				source_printer_data->tab_num--;
				OUTPUT("}\n");
				source_printer_data->is_on_new_line = 1;
			}
			break;
	}
	POST_VISIT();
	return AST_VISIT_CANCEL_NODE;

#undef OUTPUT
#undef SUB_VISIT
#undef SUB_VISIT_EXPRESSION
#undef POST_VISIT
#undef SPACE
}

BOOL ast_print_source(FILE* stream, struct AST_NODE* ast_node,
	ast_error_handler error_handler,
	const struct SOURCE_PRINTER_FORMAT_STRUCT* format)
{
	struct SOURCE_PRINTER_DATA_STRUCT source_printer_data =
		(struct SOURCE_PRINTER_DATA_STRUCT) {
			stream, format, error_handler, 0, 1, 0, 0
		};
	return visit(ast_node, ast_print_source_pre_handler, NULL, error_handler,
			&source_printer_data);
}

BOOL visit(struct AST_NODE* ast_node, ast_node_handler pre_handler,
	ast_node_handler post_handler, ast_error_handler error_handler, void* data)
{
	char* error_string;

#define SUB_VISIT(ast_node) \
	if (ast_node != NULL && \
			!visit(ast_node, pre_handler, post_handler, error_handler, data)) \
		return 0;

	if (pre_handler != NULL)
		switch (pre_handler(ast_node, data)) {
			case AST_VISIT_CONTINUE:
				break;
			case AST_VISIT_CANCEL_NODE:
				return 1;
			case AST_VISIT_CANCEL:
				return 0;
		}

	switch (ast_node->type) {
		case IDENTIFIER:
		case CONSTANT:
		case STRING_LITERAL:
			break;
		case ARRAY_ACCESS:
			SUB_VISIT(ast_node->array_access.array);
			SUB_VISIT(ast_node->array_access.index);
			break;
		case ARGUMENT_EXPRESSION_LIST:
			SUB_VISIT(ast_node->argument_expression_list
					.left_argument_expression_list);
			SUB_VISIT(ast_node->argument_expression_list.assignment_expression);
			break;
		case PARAMETERIZED_EXPRESSION:
			SUB_VISIT(ast_node->parameterized_expression.expression);
			SUB_VISIT(ast_node->parameterized_expression.argument_list);
			break;
		case FIELD_ACCESS:
			SUB_VISIT(ast_node->field_access.expression);
			break;
		case INC_DEC_EXPRESSION:
			SUB_VISIT(ast_node->inc_dec_expression.operand);
			break;
		case STRUCT_CONSTANT:
			SUB_VISIT(ast_node->struct_constant.struct_type);
			SUB_VISIT(ast_node->struct_constant.initializer_list);
			break;
		case UNARY_EXPRESSION:
			SUB_VISIT(ast_node->unary_expression.operand);
			break;
		case SIZEOF_EXPRESSION:
			SUB_VISIT(ast_node->sizeof_expression.expression_or_type);
			break;
		case CAST_EXPRESSION:
			SUB_VISIT(ast_node->cast_expression.cast_type);
			SUB_VISIT(ast_node->cast_expression.operand);
			break;
		case BINARY_EXPRESSION:
			SUB_VISIT(ast_node->binary_expression.left_operand);
			SUB_VISIT(ast_node->binary_expression.right_operand);
			break;
		case CONDITIONAL_EXPRESSION:
			SUB_VISIT(ast_node->conditional_expression.operand1);
			SUB_VISIT(ast_node->conditional_expression.operand2);
			SUB_VISIT(ast_node->conditional_expression.operand3);
			break;
		case COMMA_EXPRESSION:
			SUB_VISIT(ast_node->comma_expression.left_expression);
			SUB_VISIT(ast_node->comma_expression.assignment_expression);
			break;
		case DECLARATION:
			SUB_VISIT(ast_node->declaration.declaration_specifiers);
			SUB_VISIT(ast_node->declaration.init_declarator_list);
			break;
		case STORAGE_CLASS_SPECIFIER:
			break;
		case STRUCT_OR_UNION:
			SUB_VISIT(ast_node->struct_or_union.struct_declaration_list);
			break;
		case STRUCT_DECLARATION_LIST:
			SUB_VISIT(ast_node->struct_declaration_list
					.left_struct_declaration_list);
			SUB_VISIT(ast_node->struct_declaration_list.struct_declaration);
			break;
		case STRUCT_DECLARATION:
			SUB_VISIT(ast_node->struct_declaration.struct_qualifier_list);
			SUB_VISIT(ast_node->struct_declaration.struct_declaration_list);
			break;
		case PRIMITIVE_TYPE_SPECIFIER:
			break;
		case NAMED_TYPE_SPECIFIER:
			break;
		case DECLARATION_SPECIFIERS:
			SUB_VISIT(ast_node->declaration_specifiers.specifier_or_qualifier);
			SUB_VISIT(ast_node->declaration_specifiers.more_declaration_specifiers);
			break;
		case INIT_DECLARATOR_LIST:
			SUB_VISIT(ast_node->init_declarator_list.left_init_declarator_list);
			SUB_VISIT(ast_node->init_declarator_list.init_declarator);
			break;
		case INIT_DECLARATOR:
			SUB_VISIT(ast_node->init_declarator.declarator);
			SUB_VISIT(ast_node->init_declarator.initializer);
			break;
		case SPECIFIER_QUALIFIER_LIST:
			SUB_VISIT(ast_node->specifier_qualifier_list.specifier_or_qualifier);
			SUB_VISIT(ast_node->specifier_qualifier_list
					.right_specifier_qualifier_list);
			break;
		case STRUCT_DECLARATOR_LIST:
			SUB_VISIT(ast_node->struct_declarator_list.struct_declarator);
			SUB_VISIT(ast_node->struct_declarator_list
					.right_struct_declarator_list);
			break;
		case STRUCT_DECLARATOR:
			SUB_VISIT(ast_node->struct_declarator.declarator);
			SUB_VISIT(ast_node->struct_declarator.constant_expression);
			break;
		case ENUM_SPECIFIER:
			SUB_VISIT(ast_node->enum_specifier.enumerator_list);
			break;
		case ENUMERATOR_LIST:
			SUB_VISIT(ast_node->enumerator_list.left_enumerator_list);
			SUB_VISIT(ast_node->enumerator_list.enumerator);
			break;
		case ENUMERATOR:
			SUB_VISIT(ast_node->enumerator.constant_expression);
			break;
		case TYPE_QUALIFIER:
			break;
		case FUNCTION_SPECIFIER:
			break;
		case POINTER:
			SUB_VISIT(ast_node->pointer.type_qualifier_list);
			SUB_VISIT(ast_node->pointer.right_pointer);
			break;
		case TYPE_QUALIFIER_LIST:
			SUB_VISIT(ast_node->type_qualifier_list.left_type_qualifier_list);
			SUB_VISIT(ast_node->type_qualifier_list.type_qualifier);
			break;
		case DECLARATOR:
			SUB_VISIT(ast_node->declarator.pointer);
			SUB_VISIT(ast_node->declarator.direct_declarator);
			break;
		case NAMED_DIRECT_DECLARATOR:
			break;
		case ARRAY_DIRECT_DECLARATOR:
			SUB_VISIT(ast_node->array_direct_declarator.direct_declarator);
			SUB_VISIT(ast_node->array_direct_declarator.type_qualifier_list);
			SUB_VISIT(ast_node->array_direct_declarator.assignment_expression);
			break;
		case PARAMETERIZED_DIRECT_DECLARATOR:
			SUB_VISIT(ast_node->parameterized_direct_declarator.direct_declarator);
			SUB_VISIT(ast_node->parameterized_direct_declarator
					.parameter_type_list);
			SUB_VISIT(ast_node->parameterized_direct_declarator.identifier_list);
			break;
		case PARAMETER_LIST:
			SUB_VISIT(ast_node->parameter_list.left_parameter_list);
			SUB_VISIT(ast_node->parameter_list.parameter_declaration);
			break;
		case PARAMETER_DECLARATION:
			SUB_VISIT(ast_node->parameter_declaration.declaration_specifiers);
			SUB_VISIT(ast_node->parameter_declaration.declarator);
			SUB_VISIT(ast_node->parameter_declaration.abstract_declarator);
			break;
		case IDENTIFIER_LIST:
			SUB_VISIT(ast_node->identifier_list.left_identifier_list);
			break;
		case TYPE_NAME:
			SUB_VISIT(ast_node->type_name.specifier_qualifier_list);
			SUB_VISIT(ast_node->type_name.abstract_declarator);
			break;
		case ABSTRACT_DECLARATOR:
			SUB_VISIT(ast_node->abstract_declarator.pointer);
			SUB_VISIT(ast_node->abstract_declarator.direct_abstract_declarator);
			break;
		case ARRAY_DIRECT_ABSTRACT_DECLARATOR:
			SUB_VISIT(ast_node->array_direct_abstract_declarator
					.direct_abstract_declarator);
			SUB_VISIT(ast_node->array_direct_abstract_declarator
					.assignment_expression);
			break;
		case PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR:
			SUB_VISIT(ast_node->parameterized_direct_abstract_declarator
					.direct_abstract_declarator);
			SUB_VISIT(ast_node->parameterized_direct_abstract_declarator
					.parameter_type_list);
			break;
		case INITIALIZER_LIST:
			SUB_VISIT(ast_node->initializer_list.left_initializer_list);
			SUB_VISIT(ast_node->initializer_list.designation);
			SUB_VISIT(ast_node->initializer_list.initializer);
			break;
		case DESIGNATOR_LIST:
			SUB_VISIT(ast_node->designator_list.left_designator_list);
			SUB_VISIT(ast_node->designator_list.designator);
			break;
		case ARRAY_DESIGNATOR:
			SUB_VISIT(ast_node->array_designator.constant_expression);
			break;
		case DOT_DESIGNATOR:
			break;
		case IDENTIFIER_LABELED_STATEMENT:
			SUB_VISIT(ast_node->identifier_labeled_statement.statement);
			break;
		case CASE_LABELED_STATEMENT:
			SUB_VISIT(ast_node->case_labeled_statement.constant_expression);
			SUB_VISIT(ast_node->case_labeled_statement.statement);
			break;
		case BLOCK_ITEM_LIST:
			SUB_VISIT(ast_node->block_item_list.left_block_item_list);
			SUB_VISIT(ast_node->block_item_list.declaration_or_statement);
			break;
		case EXPRESSION_STATEMENT:
			SUB_VISIT(ast_node->expression_statement.expression);
			break;
		case IF_STATEMENT:
			SUB_VISIT(ast_node->if_statement.expression);
			SUB_VISIT(ast_node->if_statement.if_statement);
			SUB_VISIT(ast_node->if_statement.else_statement);
			break;
		case SWITCH_STATEMENT:
			SUB_VISIT(ast_node->switch_statement.expression);
			SUB_VISIT(ast_node->switch_statement.statement);
			break;
		case WHILE_STATEMENT:
			SUB_VISIT(ast_node->while_statement.expression);
			SUB_VISIT(ast_node->while_statement.statement);
			break;
		case DO_WHILE_STATEMENT:
			SUB_VISIT(ast_node->do_while_statement.statement);
			SUB_VISIT(ast_node->do_while_statement.expression);
			break;
		case FOR_STATEMENT:
			SUB_VISIT(ast_node->for_statement.declaration_or_expression_statement);
			SUB_VISIT(ast_node->for_statement.expression_statement);
			SUB_VISIT(ast_node->for_statement.expression);
			SUB_VISIT(ast_node->for_statement.statement);
			break;
		case GOTO_STATEMENT:
			break;
		case LOOP_JUMP_STATEMENT:
			break;
		case RETURN_STATEMENT:
			SUB_VISIT(ast_node->return_statement.expression);
			break;
		case TRANSLATION_UNIT:
			SUB_VISIT(ast_node->translation_unit.left_translation_unit);
			SUB_VISIT(ast_node->translation_unit.external_declaration);
			break;
		case FUNCTION_DEFINITION:
			SUB_VISIT(ast_node->function_definition.declaration_specifiers);
			SUB_VISIT(ast_node->function_definition.declarator);
			SUB_VISIT(ast_node->function_definition.declaration_list);
			SUB_VISIT(ast_node->function_definition.compound_statement);
			break;
		case DECLARATION_LIST:
			SUB_VISIT(ast_node->declaration_list.left_declaration_list);
			SUB_VISIT(ast_node->declaration_list.declaration);
			break;
		case COMPOUND_STATEMENT:
			SUB_VISIT(ast_node->compound_statement.block_item_list);
			break;
		default:
			error_string = create_error_string("unknown AST node type",
				strlist_get(lex_files, ast_node->file), ast_node->line,
				ast_node->column);
			if (error_handler == NULL) {
				fflush(stderr);
				fprintf(stderr, "ERROR: %s.\n", error_string);
				return 0;
			}
			else
				return error_handler(error_string);
	}
	
	if (post_handler != NULL)
		switch (post_handler(ast_node, data)) {
			case AST_VISIT_CONTINUE:
				break;
			case AST_VISIT_CANCEL_NODE:
				return 1;
			case AST_VISIT_CANCEL:
				return 0;
		}
	
	return 1;
	
#undef SUB_VISIT
}
