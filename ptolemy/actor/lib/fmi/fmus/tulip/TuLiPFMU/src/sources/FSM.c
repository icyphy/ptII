#include "FSM.h"

FSM* init_fsm(){
	FSM* fsm =(FSM*) malloc(sizeof(FSM));
	fsm->currentState = initState;
	return fsm;
};

int fsm_transition(FSM* fsm, int input[]){
	int currentState = fsm->currentState;	
	int index = value2index(input);
	fsm->currentState = transition[currentState][index];
	return (output[currentState][index]);
};

void free_fsm(FSM* fsm){
	free(fsm);
};
