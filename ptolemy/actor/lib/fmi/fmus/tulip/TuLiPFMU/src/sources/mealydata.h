#ifndef __MEALY_H_
#define __MEALY_H_
extern int nInputVariable;
extern int nInputValue[];
#define NUM_INPUT 2
#define NUM_STATE 24
extern int initState;
extern int transition[NUM_STATE][NUM_INPUT];
extern int output[NUM_STATE][NUM_INPUT];
int value2index(int inputValue[]);
#endif