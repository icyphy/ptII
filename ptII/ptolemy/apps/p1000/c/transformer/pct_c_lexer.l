%option noyywrap
%option prefix="c_"
%option outfile="lex.yy.c"

%{
#include <stdio.h>
#include <string.h>
#include "pct_util.h"
#include "pct_c_parser.h"

void block_comment();
void change_source_file();
void check_comments(AST_NODE* ast_node);
void count();
AST_NODE* create_lexical_token(int token);
void ignore_attribute();
void line_comment();

extern int c_error(char* const error);

#define RETURN_LEXICAL_TOKEN(token) { \
	c_lval = create_lexical_token(token); \
	return token; \
}
%}

D				[0-9]
L				[a-zA-Z_]
H				[a-fA-F0-9]
E				[Ee][+-]?{D}+
FS				(f|F|l|L)
IS				(u|U|l|L)*

%%
"/*"			{ block_comment(); }
"#"				{ change_source_file(); }
"//"			{ line_comment(); }

"auto"			{ count(); RETURN_LEXICAL_TOKEN(AUTO); }
"_Bool"			{ count(); RETURN_LEXICAL_TOKEN(BOOLEAN); }
"break"			{ count(); RETURN_LEXICAL_TOKEN(BREAK); }
"case"			{ count(); RETURN_LEXICAL_TOKEN(CASE); }
"char"			{ count(); RETURN_LEXICAL_TOKEN(CHAR); }
"_Complex"		{ count(); RETURN_LEXICAL_TOKEN(COMPLEX); }
"const"			{ count(); RETURN_LEXICAL_TOKEN(CONST); }
"continue"		{ count(); RETURN_LEXICAL_TOKEN(CONTINUE); }
"default"		{ count(); RETURN_LEXICAL_TOKEN(DEFAULT); }
"do"			{ count(); RETURN_LEXICAL_TOKEN(DO); }
"double"		{ count(); RETURN_LEXICAL_TOKEN(DOUBLE); }
"else"			{ count(); RETURN_LEXICAL_TOKEN(ELSE); }
"enum"			{ count(); RETURN_LEXICAL_TOKEN(ENUM); }
"extern"		{ count(); RETURN_LEXICAL_TOKEN(EXTERN); }
"float"			{ count(); RETURN_LEXICAL_TOKEN(FLOAT); }
"for"			{ count(); RETURN_LEXICAL_TOKEN(FOR); }
"goto"			{ count(); RETURN_LEXICAL_TOKEN(GOTO); }
"if"			{ count(); RETURN_LEXICAL_TOKEN(IF); }
"_Imaginary"	{ count(); RETURN_LEXICAL_TOKEN(IMAGINARY); }
"inline"		{ count(); RETURN_LEXICAL_TOKEN(INLINE); }
"int"			{ count(); RETURN_LEXICAL_TOKEN(INT); }
"long"			{ count(); RETURN_LEXICAL_TOKEN(LONG); }
"register"		{ count(); RETURN_LEXICAL_TOKEN(REGISTER); }
"restrict"		{ count(); RETURN_LEXICAL_TOKEN(RESTRICT); }
"return"		{ count(); RETURN_LEXICAL_TOKEN(RETURN); }
"short"			{ count(); RETURN_LEXICAL_TOKEN(SHORT); }
"signed"		{ count(); RETURN_LEXICAL_TOKEN(SIGNED); }
"sizeof"		{ count(); RETURN_LEXICAL_TOKEN(SIZEOF); }
"static"		{ count(); RETURN_LEXICAL_TOKEN(STATIC); }
"struct"		{ count(); RETURN_LEXICAL_TOKEN(STRUCT); }
"switch"		{ count(); RETURN_LEXICAL_TOKEN(SWITCH); }
"typedef"		{ count(); RETURN_LEXICAL_TOKEN(TYPEDEF); }
"union"			{ count(); RETURN_LEXICAL_TOKEN(UNION); }
"unsigned"		{ count(); RETURN_LEXICAL_TOKEN(UNSIGNED); }
"void"			{ count(); RETURN_LEXICAL_TOKEN(VOID); }
"volatile"		{ count(); RETURN_LEXICAL_TOKEN(VOLATILE); }
"while"			{ count(); RETURN_LEXICAL_TOKEN(WHILE); }

	/* FIXME */
"__extension__"	{ count(); /* Ignore. */ }
"__attribute__"	{ ignore_attribute(); /* Ignore. */ }
"__inline__"	{ count(); /* Ignore. */ }
"__const"		{ count(); /* Ignore. */ }

	/* Identifier */
{L}({L}|{D})* {
	count();
	c_lval = create_node2(IDENTIFIER, lex_line, lex_column - strlen(yytext) + 1,
		lex_file_index);
	c_lval->identifier = (IDENTIFIER_STRUCT) { strclone(yytext) };
	check_comments(c_lval);
	if (strlist_lookup(type_names, yytext) >= 0)
		return AMBIGUOUS_TYPE_NAME;
	else
		return IDENTIFIER;
}
	/* Constant */
(0[xX]{H}+{IS}?) {
	count();
	c_lval = create_node2(CONSTANT, lex_line, lex_column - strlen(yytext) + 1,
		lex_file_index);
	c_lval->constant = (CONSTANT_STRUCT) { CONSTANT_HEX, strclone(yytext) };
	check_comments(c_lval);
	return c_lval->type;
}

(0{D}+{IS}?) {
	count();
	c_lval = create_node2(CONSTANT, lex_line, lex_column - strlen(yytext) + 1,
		lex_file_index);
	c_lval->constant = (CONSTANT_STRUCT) { CONSTANT_OCT, strclone(yytext) };
	check_comments(c_lval);
	return c_lval->type;
}

({D}+{IS}?) {
	count();
	c_lval = create_node2(CONSTANT, lex_line, lex_column - strlen(yytext) + 1,
		lex_file_index);
	c_lval->constant = (CONSTANT_STRUCT) { CONSTANT_DEC, strclone(yytext) };
	check_comments(c_lval);
	return c_lval->type;
}

(L?'(\\.|[^\\'])+') {
	count();
	c_lval = create_node2(CONSTANT, lex_line, lex_column - strlen(yytext) + 1,
		lex_file_index);
	c_lval->constant = (CONSTANT_STRUCT) { CONSTANT_CHR, strclone(yytext) };
	check_comments(c_lval);
	return c_lval->type;
}

({D}+{E}{FS}?)|({D}*"."{D}+({E})?{FS}?)|({D}+"."{D}*({E})?{FS}?) {
	count();
	c_lval = create_node2(CONSTANT, lex_line, lex_column - strlen(yytext) + 1,
		lex_file_index);
	c_lval->constant = (CONSTANT_STRUCT) { CONSTANT_FLT, strclone(yytext) };
	check_comments(c_lval);
	return c_lval->type;
}

	/* String Literal */
L?\"(\\.|[^\\"])*\" {
	count();
	c_lval = create_node2(STRING_LITERAL, lex_line,
		lex_column - strlen(yytext) + 1, lex_file_index);
	c_lval->string_literal = (STRING_LITERAL_STRUCT) { strclone(yytext) };
	check_comments(c_lval);
	return c_lval->type;
}

"..."			{ count(); RETURN_LEXICAL_TOKEN(ELLIPSIS); }
">>="			{ count(); RETURN_LEXICAL_TOKEN(RIGHT_ASSIGN); }
"<<="			{ count(); RETURN_LEXICAL_TOKEN(LEFT_ASSIGN); }
"+="			{ count(); RETURN_LEXICAL_TOKEN(ADD_ASSIGN); }
"-="			{ count(); RETURN_LEXICAL_TOKEN(SUB_ASSIGN); }
"*="			{ count(); RETURN_LEXICAL_TOKEN(MUL_ASSIGN); }
"/="			{ count(); RETURN_LEXICAL_TOKEN(DIV_ASSIGN); }
"%="			{ count(); RETURN_LEXICAL_TOKEN(MOD_ASSIGN); }
"&="			{ count(); RETURN_LEXICAL_TOKEN(AND_ASSIGN); }
"^="			{ count(); RETURN_LEXICAL_TOKEN(XOR_ASSIGN); }
"|="			{ count(); RETURN_LEXICAL_TOKEN(OR_ASSIGN); }
">>"			{ count(); RETURN_LEXICAL_TOKEN(RIGHT_OP); }
"<<"			{ count(); RETURN_LEXICAL_TOKEN(LEFT_OP); }
"++"			{ count(); RETURN_LEXICAL_TOKEN(INC_OP); }
"--"			{ count(); RETURN_LEXICAL_TOKEN(DEC_OP); }
"->"			{ count(); RETURN_LEXICAL_TOKEN(PTR_OP); }
"&&"			{ count(); RETURN_LEXICAL_TOKEN(AND_OP); }
"||"			{ count(); RETURN_LEXICAL_TOKEN(OR_OP); }
"<="			{ count(); RETURN_LEXICAL_TOKEN(LE_OP); }
">="			{ count(); RETURN_LEXICAL_TOKEN(GE_OP); }
"=="			{ count(); RETURN_LEXICAL_TOKEN(EQ_OP); }
"!="			{ count(); RETURN_LEXICAL_TOKEN(NE_OP); }
";"				{ count(); RETURN_LEXICAL_TOKEN(';'); }
("{"|"<%")		{ count(); RETURN_LEXICAL_TOKEN('{'); }
("}"|"%>")		{ count(); RETURN_LEXICAL_TOKEN('}'); }
","				{ count(); RETURN_LEXICAL_TOKEN(','); }
":"				{ count(); RETURN_LEXICAL_TOKEN(':'); }
"="				{ count(); RETURN_LEXICAL_TOKEN('='); }
"("				{ count(); RETURN_LEXICAL_TOKEN('('); }
")"				{ count(); RETURN_LEXICAL_TOKEN(')'); }
("["|"<:")		{ count(); RETURN_LEXICAL_TOKEN('['); }
("]"|":>")		{ count(); RETURN_LEXICAL_TOKEN(']'); }
"."				{ count(); RETURN_LEXICAL_TOKEN('.'); }
"&"				{ count(); RETURN_LEXICAL_TOKEN('&'); }
"!"				{ count(); RETURN_LEXICAL_TOKEN('!'); }
"~"				{ count(); RETURN_LEXICAL_TOKEN('~'); }
"-"				{ count(); RETURN_LEXICAL_TOKEN('-'); }
"+"				{ count(); RETURN_LEXICAL_TOKEN('+'); }
"*"				{ count(); RETURN_LEXICAL_TOKEN('*'); }
"/"				{ count(); RETURN_LEXICAL_TOKEN('/'); }
"%"				{ count(); RETURN_LEXICAL_TOKEN('%'); }
"<"				{ count(); RETURN_LEXICAL_TOKEN('<'); }
">"				{ count(); RETURN_LEXICAL_TOKEN('>'); }
"^"				{ count(); RETURN_LEXICAL_TOKEN('^'); }
"|"				{ count(); RETURN_LEXICAL_TOKEN('|'); }
"?"				{ count(); RETURN_LEXICAL_TOKEN('?'); }

[ \t\v\n\f]		{ count(); }
.				{ /* ignore bad characters */ }

%%

void block_comment()
{
	char c, prev = 0;
	char* text;
	COMMENT_LIST_STRUCT* prev_comment;
	int current_length = 0, max_size = 0;
	
	count();
	append_text(&text, '/', &current_length, &max_size);
	append_text(&text, '*', &current_length, &max_size);
	while ((c = input()) != 0) {	/* (EOF maps to 0) */
		append_text(&text, c, &current_length, &max_size);
		if (c == '\n') {
			lex_line++;
			lex_column = 0;
		} else
			lex_column++;
		if (c == '/' && prev == '*')
		{
			if (comment_list == NULL) {
				comment_list =
					(COMMENT_LIST_STRUCT*) malloc(sizeof(COMMENT_LIST_STRUCT));
				current_comment_ptr = comment_list;
				prev_comment = NULL;
			} else {
				current_comment_ptr->next =
					(COMMENT_LIST_STRUCT*) malloc(sizeof(COMMENT_LIST_STRUCT));
				prev_comment = current_comment_ptr;
				current_comment_ptr = current_comment_ptr->next;
			}
			*current_comment_ptr = (COMMENT_LIST_STRUCT) {
				lex_line, lex_column, lex_file_index, BLOCK_COMMENT, text, NULL, prev_comment,
				NULL
			};
			return;
		}
		prev = c;
	}
	c_error("unterminated comment");
}

void change_source_file()
{
	char c;
	char* text = NULL;
	int current_length = 0, max_size = 0;
	int line_num = -1;
	BOOL file_name_started = 0;
	count();
	while ((c = input()) != 0 && c != '\n') {	/* (EOF maps to 0) */
		if (line_num == -1) {
			if (c == ' ' || c == '\t') {
				if (text != NULL) {
					line_num = atoi(text);
					free(text);
					text = NULL;
					current_length = 0;
					max_size = 0;
				}
			} else if (c >= '0' && c <= '9') {
				append_text(&text, c, &current_length, &max_size);
			} else {
				break;
			}
		} else {
			if (c == '\"') {
				if (!file_name_started && text == NULL) {
					file_name_started = 1;
				} else if (file_name_started && text != NULL) {
					file_name_started = 0;
				} else {
					break;
				}
			} else {
				if (file_name_started) {
					append_text(&text, c, &current_length, &max_size);
				} else if (c == ' ' || c == '\t'
						|| (c >= '0' && c <='9' && text != NULL)) {
					// Numbers after the source file name.
				} else {
					break;
				}
			}
		}
	}
	if ((c != 0 && c != '\n') || line_num < 0 || text == NULL
			|| file_name_started) {
		while (c != 0 && c != '\n') {
			c = input();
		}
		if (text != NULL)
			free(text);
		c_error("unable to parse source file redirective");
	} else {
		lex_file_index = strlist_add(lex_files, text);
		lex_line = line_num;
		lex_column = 0;
		free(text);
	}
}

void check_comments(AST_NODE* ast_node)
{
	COMMENT_LIST_STRUCT* comment_ptr = current_comment_ptr;
	COMMENT_LIST_STRUCT* node_comment_ptr = NULL;
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
				ast_node,
				NULL,
				NULL
			};
			ast_node->comment = node_comment_ptr;
		} else {
			node_comment_ptr->previous =
				(COMMENT_LIST_STRUCT*) malloc(sizeof(COMMENT_LIST_STRUCT));
			*node_comment_ptr->previous = (COMMENT_LIST_STRUCT) {
				comment_ptr->line,
				comment_ptr->column,
				comment_ptr->file,
				comment_ptr->comment_type,
				comment_ptr->comment,
				ast_node,
				NULL,
				node_comment_ptr
			};
			node_comment_ptr = node_comment_ptr->previous;
		}
		comment_ptr->ast_node = ast_node;
		comment_ptr = comment_ptr->previous;
	}
}

void count()
{
	int i;
	for (i = 0; yytext[i] != '\0'; i++)
		if (yytext[i] == '\n') {
			lex_line++;
			lex_column = 0;
		} else if (yytext[i] == '\t')
			lex_column += 8 - (lex_column % 8);
		else
			lex_column++;
}

AST_NODE* create_lexical_token(int token)
{
	AST_NODE* node = create_node2(LEXICAL_TOKEN, lex_line,
		lex_column - strlen(yytext) + 1, lex_file_index);
	node->lexical_token = token;
	check_comments(node);
	return node;
}

/* Ignore __attribute__((...)).
 */
void ignore_attribute()
{
	int paren = 0;
	BOOL started = 0;
	char c;
	count();
	while ((!started || paren > 0) && (c = input()) != 0) {
		if (c == '\n') {
			lex_line++;
			lex_column = 0;
		} else {
			lex_column++;
			if (c == '(') {
				paren++;
				started = 1;
			} else if (c == ')')
				paren--;
		}
	}
}

void line_comment()
{
	char c, prev = 0;
	char* text;
	COMMENT_LIST_STRUCT* prev_comment;
	int current_length = 0, max_size = 0;
	
	count();
	append_text(&text, '/', &current_length, &max_size);
	append_text(&text, '/', &current_length, &max_size);
	while ((c = input()) != 0) {	/* (EOF maps to 0) */
		if (c == '\n') {
			lex_line++;
			lex_column = 0;
			if (comment_list == NULL) {
				comment_list =
					(COMMENT_LIST_STRUCT*) malloc(sizeof(COMMENT_LIST_STRUCT));
				current_comment_ptr = comment_list;
				prev_comment = NULL;
			} else {
				current_comment_ptr->next =
					(COMMENT_LIST_STRUCT*) malloc(sizeof(COMMENT_LIST_STRUCT));
				prev_comment = current_comment_ptr;
				current_comment_ptr = current_comment_ptr->next;
			}
			*current_comment_ptr = (COMMENT_LIST_STRUCT) {
				lex_line, lex_column, lex_file_index, LINE_COMMENT, text, NULL, prev_comment,
				NULL
			};
			return;
		} else
			lex_column++;
		append_text(&text, c, &current_length, &max_size);
		prev = c;
	}
	c_error("unterminated comment");
}
