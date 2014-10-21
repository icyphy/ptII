#ifndef __TULIPCONTROL_H__
#define __TULIPCONTROL_H__

#include <stdio.h>
#include <ecos.h>
#include "polytope.h"
#include "data.h"
#include "pppdata.h"
#include "FSM.h"
#include "mealydata.h"

typedef struct Controller {
    pfloat *u;            // The output of the feedback controller
    pfloat *y;            // The input of the feedback controller
    pfloat *hatx;         // The state estimates 
    idxint dRegion;       // The discrete region of the system
    idxint goal;          // The next discrete region of the system 
    idxint nSteps;        // The number of steps left to reach the goal
    idxint *dInput;       // The discrete input
    FSM *fsm;		
}Controller;

/*Methods-----------------------------------------*/

/* Compute dRegion based on hatx */
void abstract(Controller* controller);

/*Compute the control input u * */

void compute_control(Controller* controller);

Controller* instantiate_controller();

void init_controller(Controller* controller);

void free_controller(Controller* controller);

void transition_function(Controller* controller);

void output_function(Controller* controller, pfloat u[]); 

void input_function(Controller* controller, const pfloat y[], const idxint dInput[]); 

void estimate_function(Controller* controller);

void display_controller(Controller* controller);

/* Compute the MPC input given by the following optimization problem:
 * min u(0)'u(0) + ... + u(N-1)'u(N-1) + (x(N)-xc)'(x(N)-xc)
 * such that x(0) = x0
 * x(k+1) = Ax(k) + B u(k)
 * [x(k),u(k)] belongs to polytope 1 (A1*x >= b1) for k=0,...,N-1
 * x(N) belongs to polytope 2 (A2*x >= b2)
 *
 * n is the dimension of the state x(k)
 * p is the dimension of the input u(k)
 * N is the window size
 * l1 is the number of inequalities describing polytope 1
 * l2 is the number of inequalities describing polytope 2
 * u0 is the input at time 0, which is the solution of the optimization problem 
 * the entries of any n by m matrix X is assumed to be arranged as[X11,X21,...,Xn1,X12,...,Xn2,...,X1m,...,Xnm]
 */
idxint get_input_helper(const idxint n, const idxint p, const idxint N, const pfloat* A, const pfloat* B, 
		const pfloat* A1, const pfloat* b1, const idxint l1, 
		const pfloat* A2, const pfloat* b2, const idxint l2,
	       	const pfloat* x0, const pfloat* xc, pfloat* u0);

#endif
