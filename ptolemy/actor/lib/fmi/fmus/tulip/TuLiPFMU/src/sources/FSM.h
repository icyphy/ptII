#ifndef __FSM_H_
#define __FSM_H_

#include <stdlib.h>
#include "mealydata.h"

typedef struct FSM {
        int currentState;
} FSM;

FSM* init_fsm();

int fsm_transition(FSM* fsm, int input[]);

void free_fsm(FSM* fsm);
#endif
