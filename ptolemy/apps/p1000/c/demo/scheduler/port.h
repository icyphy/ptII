#ifndef PORT_H_
#define PORT_H_

#include "types.h"

typedef struct DEPENDENCY_LINK {
	struct PORT* port;
	double dependency;
	struct DEPENDENCY_LINK* prev;
	struct DEPENDENCY_LINK* next;
} DEPENDENCY_LINK;

void DEPENDENCY_LINK_init(DEPENDENCY_LINK* link, struct PORT* port,
	double dependency);
	
typedef struct PORT {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	struct PORT* connected_port;
	struct ACTOR* container;
	int has_token : 1;
	int is_realtime : 1;
	DEPENDENCY_LINK* last_link;
	struct PORT* prev;
	struct PORT* next;
} PORT;

void PORT_init(PORT* port, void* actual_ref, struct ACTOR* container);
void PORT_connect(PORT* port, PORT* to_port);

#endif /*PORT_H_*/
