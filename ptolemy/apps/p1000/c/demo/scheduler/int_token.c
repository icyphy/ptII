#include "int_token.h"

void INT_TOKEN_init(INT_TOKEN* int_token, void* actual_ref)
{
	INIT_SUPER_TYPE(INT_TOKEN, TOKEN, int_token, actual_ref, NULL);
	
	// Very important: to tell the allocators how much memory is needed for this
	// token type. For instance t of type T inherited from TOKEN, do:
	//     UPCAST(t, TOKEN)->size = sizeof(T);
	UPCAST(int_token, TOKEN)->size = sizeof(INT_TOKEN);
	
	int_token->value = 0;
}
