#include <stdio.h>
#include "../pct_util.h"

int main()
{
	AST_NODE* translation_unit = parse();
	if (translation_unit == NULL)
		return 1;
	
	//ast_print_tree(stdout, translation_unit, NULL);
	ast_print_source(stdout, translation_unit, NULL,
		&default_source_printer_format); 
	
	return 0;
}
