#include "token.h"

void TOKEN_init(TOKEN* token, void* actual_ref)
{
	INIT_SUPER_TYPE(TOKEN, GENERAL_TYPE, token, actual_ref, NULL);
	
	// Subclasses must override this size with the actual size of that token
	// type.
	token->size = sizeof(TOKEN);
	
	token->prev = token->next = NULL;
}
