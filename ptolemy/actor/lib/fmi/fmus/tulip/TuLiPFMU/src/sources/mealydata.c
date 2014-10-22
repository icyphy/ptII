#include "mealydata.h"

int nInputVariable = 1;
int nInputValue[] = {2,};
int initState = 23;
int transition[NUM_STATE][NUM_INPUT]={
{10,9,},
{0,9,},
{0,13,},
{2,1,},
{2,15,},
{6,5,},
{4,3,},
{6,5,},
{6,5,},
{10,9,},
{8,7,},
{10,9,},
{10,9,},
{12,11,},
{12,13,},
{14,13,},
{14,13,},
{16,15,},
{16,15,},
{20,19,},
{18,17,},
{20,19,},
{20,19,},
{21,7,},
};

int output[NUM_STATE][NUM_INPUT]={
{0,2,},
{2,2,},
{2,8,},
{5,5,},
{5,4,},
{5,2,},
{1,1,},
{5,2,},
{5,2,},
{0,2,},
{2,2,},
{0,2,},
{0,2,},
{18,18,},
{18,8,},
{8,8,},
{8,8,},
{4,4,},
{4,4,},
{4,8,},
{16,16,},
{4,8,},
{4,8,},
{8,2,},
};

int value2index(int inputValue[]){
        int result = 0;
        int tmp = 1;
        int valuation;
        int i;
        for(i=0;i<nInputVariable;i++)
        {
                valuation = inputValue[i]%nInputValue[i];
                result += valuation * tmp;
                tmp *= nInputValue[i];
        }
        return result;
};
