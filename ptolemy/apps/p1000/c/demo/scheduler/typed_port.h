#ifndef TYPED_PORT_H_
#define TYPED_PORT_H_

#include "types.h"
#include "port.h"
#include "event.h"

typedef struct TYPED_PORT {
	DECLARE_SUPER_TYPE(PORT)
	
	EVENT* first_event;
	EVENT* last_event;
} TYPED_PORT;

void TYPED_PORT_init(TYPED_PORT* typed_port, void* actual_ref,
	struct ACTOR* container);
void TYPED_PORT_send(TYPED_PORT* typed_port, const EVENT* event);

#endif /*TYPED_PORT_H_*/
