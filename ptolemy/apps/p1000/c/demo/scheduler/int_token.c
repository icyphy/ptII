#include "int_token.h"

void INT_TOKEN_init(INT_TOKEN* int_token, void* actual_ref)
{
	INIT_SUPER_TYPE(INT_TOKEN, TOKEN, int_token, actual_ref, NULL);
	
	int_token->value = 0;
}
