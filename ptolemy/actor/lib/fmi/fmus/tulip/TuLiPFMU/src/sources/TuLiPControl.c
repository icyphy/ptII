#include "TuLiPControl.h"

//not implemented yet
void abstract(Controller* controller) {
	return;
}

//compute the control input u(k) and store it in controller.u
void compute_control(Controller* controller)
{

	Polytope* currentRegion = cartesian_prod(regions[controller->dRegion],input_bound);
	Polytope* projGoalRegion = regions[controller->goal];
	//todo: check if the computation is successful
        get_input_helper(n, p, controller->nSteps, A, B, currentRegion->A, currentRegion->b, currentRegion->k, projGoalRegion->A, projGoalRegion->b, projGoalRegion->k,controller->hatx, projGoalRegion->center, controller->u);
	free_polytope(currentRegion);
}

Controller* instantiate_controller(){
	Controller *c = MALLOC(sizeof(Controller));
	int i;
	c->u = (pfloat*)MALLOC(p*sizeof(pfloat));
	c->y = (pfloat*)MALLOC(m*sizeof(pfloat));
	c->dInput = (idxint*)MALLOC(nInputVariable*sizeof(idxint));
	for (i = 0;i<nInputVariable;i++)
	{
		*(c->dInput+i) = 0;
	}
	c->hatx = (pfloat*)MALLOC(n*sizeof(pfloat));
	return c;
}

void init_controller(Controller* controller) {
	init_input_bound();
	init_region();
	printf("successful init input and ppp polytopes\n");
	fflush(stdout);
	int i;
	for(i=0;i<n;i++)
	{
		*(controller->hatx+i) = x0[i];
	}
	controller->dRegion = dRegion0;
	controller->nSteps = totalSteps;
	controller->fsm=init_fsm();
	printf("successful init fsm\n");
	fflush(stdout);
	controller->goal = fsm_transition(controller->fsm,controller->dInput);
	compute_control(controller);
	controller->nSteps--;
}

void free_controller(Controller* controller) {
	FREE(controller->u);
	FREE(controller->y);
	FREE(controller->hatx);
	free_fsm(controller->fsm);
	FREE(controller);
	free_region();
	free_input_bound();
}

void transition_function(Controller* controller) {
	estimate_function(controller);
	//arrives at the goal
	if (controller->nSteps<=0)
	{
		//todo:check if the goal is really reached 
		controller->dRegion = controller->goal;
		//reset nSteps
		controller->nSteps = totalSteps;
		controller->goal = fsm_transition(controller->fsm, controller->dInput);
	}
	compute_control(controller);
	controller->nSteps--;
	//display_controller(controller);
}


void output_function(Controller* controller, pfloat u[]){
	int i;
	for(i=0;i<p;i++)
	{
		u[i] = *(controller->u+i);
	}
}

void input_function(Controller* controller,const pfloat y[], const idxint dInput[]){
	int i;
	for(i=0;i<m;i++)
	{
		*(controller->y+i) = y[i];
	}
	for(i=0;i<nInputVariable;i++)
	{
		*(controller->dInput+i) = dInput[i];
	}
}

//assuming perfect full state observation
//possibly consider noisy observations in the future
void estimate_function(Controller* controller){
	int i;
	for(i=0;i<n;i++)
	{
		*(controller->hatx+i) = *(controller->y+i);
	}
}


void display_controller(Controller* controller){
	int i;
	printf("State Estimation: ");
	for(i=0;i<n;i++)
	{
		printf("%5.2f, ",*(controller->hatx+i));
	}
	printf("\nControl Inputs: ");
	for(i=0;i<p;i++)
	{
		printf("%5.2f, ",*(controller->u+i));
	}
	printf("\nSensor Measurements: ");
	for(i=0;i<m;i++)
	{
		printf("%5.2f, ",*(controller->y+i));
	}
	printf("\nState of the Mealy machine: %d",controller->fsm->currentState);
	printf("\nDiscrete Region: %d",controller->dRegion);
	printf("\nNext goal: %d",controller->goal);
	printf("\nSteps left to reach goal: %d\n",controller->nSteps);
	fflush(stdout);
	return;
}

idxint get_input_helper(const idxint n, const idxint p, const idxint N, const pfloat* A, const pfloat* B, 
		const pfloat* A1, const pfloat* b1, const idxint l1, 
		const pfloat* A2, const pfloat* b2, const idxint l2,
		const pfloat* x0, const pfloat* xc, pfloat* u0) {
	idxint i, j, k, l, sparsepr;

	idxint exitflag = ECOS_FATAL;
	pwork* mywork;
	/*the state is [x0,u0,x1,u1,...,uN-1,xN,a,b,t], where a,b,t are auxiliary variables.
	 * a,b,t satifies the following equalities and inequality:
	 * a = (t-1)/2, b = (t+1)/2
	 * norm([u0,...,uN-1,xN,a],2)<= b
	 * these equalities and inequality implies that u0^2+...+(uN-1)^2 + xN^2<=t*/
	idxint ecos_n = n*(N+1) + p*N + 3;	
	idxint ecos_m = l1 * N + l2 + p*N + n + 2;
	idxint ecos_p = n*(N+1) + 2;
	idxint ecos_l = l1 * N + l2;
	idxint ecos_ncones = 1;
	idxint ecos_q[1] = {p*N + n + 2};

	/*generating matrix G of ECOS*/	
	idxint nnzG = N*p + n + N*(n+p)*l1 + n*l2 + 2;
	pfloat *ecos_Gpr = (pfloat*)MALLOC(sizeof(pfloat)*nnzG);
	if (ecos_Gpr == NULL)
		return ECOS_FATAL;
	idxint *ecos_Gjc = (idxint*)MALLOC(sizeof(idxint)*(ecos_n+1));
	if (ecos_Gjc == NULL)
		return ECOS_FATAL;
	idxint *ecos_Gir = (idxint*)MALLOC(sizeof(idxint)*nnzG);
	if (ecos_Gir == NULL)
		return ECOS_FATAL;
	sparsepr = 0;

	for(k=0; k<N; k++)
	{
		for(j = 0; j < n+p; j++)
		{
			*(ecos_Gjc+j+k*(n+p)) = sparsepr;
			for (i=0; i<l1;i++)
			{
				*(ecos_Gpr+sparsepr) = -1*(*(A1+j*l1+i));
				*(ecos_Gir+sparsepr) = i+k*l1;
				sparsepr++;
			}
			if (j >= n)
			{
				*(ecos_Gpr+sparsepr) = -1;
				*(ecos_Gir+sparsepr) = j-n+k*p+N*l1+l2+2;
				sparsepr++;
			}
		}
	}

	for(j=N*(n+p); j<N*(n+p)+n;j++)
	{
		*(ecos_Gjc+j) = sparsepr;
		for(i=0;i<l2;i++)
		{
			*(ecos_Gpr+sparsepr) = -1*(*(A2+(j-N*(n+p))*l2+i));
			*(ecos_Gir+sparsepr) = i+N*l1;
			sparsepr++;
		}
		*(ecos_Gpr+sparsepr) = -1;
		*(ecos_Gir+sparsepr) = j-N*(n+p)+N*l1+l2+2+N*p;
		sparsepr++;
	}

	*(ecos_Gjc+ecos_n-3) = nnzG-2;
	*(ecos_Gpr+nnzG-2) = -1;
	*(ecos_Gir+nnzG-2) = N*l1+l2+1;

	*(ecos_Gjc+ecos_n-2) = nnzG-1;
	*(ecos_Gpr+nnzG-1) = -1;
	*(ecos_Gir+nnzG-1) = N*l1+l2;

	*(ecos_Gjc+ecos_n-1) = nnzG;
	*(ecos_Gjc+ecos_n) = nnzG;

	/*generating matrix A of ECOS*/	
	idxint nnzA = (N+1)*n + N*n*n + N*n*p + 4;
	/*The number of none zero entries in the block [I,0;-A,-B]*/
	idxint nnzAblock = n*n + n*p + n;
	pfloat *ecos_Apr = (pfloat*)MALLOC(sizeof(pfloat)*nnzA);
	if (ecos_Apr == NULL)
		return ECOS_FATAL;
	idxint *ecos_Ajc = (idxint*)MALLOC(sizeof(idxint)*(ecos_n+1));
	if (ecos_Ajc == NULL)
		return ECOS_FATAL;
	idxint *ecos_Air = (idxint*)MALLOC(sizeof(pfloat)*nnzA);
	if (ecos_Air == NULL)
		return ECOS_FATAL;

	sparsepr = 0;

	for(j = 0; j < n; j++)
	{
		*(ecos_Ajc+j)=sparsepr;
		*(ecos_Apr + sparsepr) = 1;
		*(ecos_Air + sparsepr) = j;
		sparsepr++;
		for (i = 0;i < n; i++)
		{
			*(ecos_Apr+sparsepr) = -1*(*(A+j*n+i));
			*(ecos_Air+sparsepr) = n+i;
			sparsepr++;
		}
	}

	for (j = n; j < n+p; j++)
	{
		*(ecos_Ajc+j)=sparsepr;
		for (i = 0;i < n; i++)
		{
			*(ecos_Apr + sparsepr) = -1*(*(B + (j-n)*n + i));
			*(ecos_Air + sparsepr) = n+i;
			sparsepr++;
		}
	}

	*(ecos_Ajc+n+p) = sparsepr;
	for (k = 1; k < N; k++)
	{
		for (l = 1; l <= n+p; l++)
		{
			*(ecos_Ajc+k*(n+p)+l) = *(ecos_Ajc + l) + *(ecos_Ajc + k*(n+p));
		}
		for (l = 0; l < nnzAblock; l++)
		{
			*(ecos_Apr+sparsepr) = *(ecos_Apr+sparsepr%nnzAblock);
			*(ecos_Air+sparsepr) = *(ecos_Air+sparsepr%nnzAblock) + k*n;
			sparsepr++;
		}
	}

	for(j=(n+p)*N;j<(n+p)*N+n;j++)
	{
		*(ecos_Ajc+j) = sparsepr;
		*(ecos_Apr+sparsepr) = 1;
		*(ecos_Air+sparsepr) = N*n+j-(n+p)*N;
		sparsepr++;
	}

	*(ecos_Ajc+ecos_n-3) = nnzA-4;
	*(ecos_Apr+nnzA-4) = 1;
	*(ecos_Air+nnzA-4) = (N+1)*n;

	*(ecos_Ajc+ecos_n-2) = nnzA-3;
	*(ecos_Apr+nnzA-3) = 1;
	*(ecos_Air+nnzA-3) = (N+1)*n+1;

	*(ecos_Ajc+ecos_n-1) = nnzA-2;
	*(ecos_Apr+nnzA-2) = -0.5;
	*(ecos_Air+nnzA-2) = (N+1)*n;
	*(ecos_Apr+nnzA-1) = -0.5;
	*(ecos_Air+nnzA-1) = (N+1)*n+1;

	*(ecos_Ajc+ecos_n) = nnzA;

	/*The objective function is equivalent to -2xc'*xN + t + xc'*xc
	 * (the constant xc'*xc is omitted )*/
	pfloat *ecos_c = (pfloat*)MALLOC(sizeof(pfloat)*ecos_n);
	if (ecos_c == NULL)
		return ECOS_FATAL;
	for(i = 0; i < ecos_n; i++)
	{
		if (i == ecos_n - 1)
		{
			*(ecos_c + i) = 1;
		}
		else if (i >= (n+p)*N && i< (n+p)*N+n)
		{
			*(ecos_c + i) = -2*(*(xc + i - (n+p)*N));
		}
		else
		{
			*(ecos_c+i) = 0;
		}
	}

	/* h is of the form [-b1,...,-b1,-b2,0,...,0] */
	pfloat *ecos_h = (pfloat*)MALLOC(sizeof(pfloat)*ecos_m);
	if (ecos_h == NULL)
		return ECOS_FATAL;
	for (i = 0; i < ecos_m; i++)
	{
		if (i < l1*N)
		{
			*(ecos_h + i) = -1 * (*(b1 + i%l1));
		}
		else if (i < l1*N + l2)
		{
			*(ecos_h + i) = -1 * (*(b2 + i - l1*N));
		}
		else
		{
			*(ecos_h + i) = 0;
		}
	}

	/*b is of the form [x0,0,...,0,-0.5,0.5]*/
	pfloat *ecos_b = (pfloat*)MALLOC(sizeof(pfloat)*ecos_p);
	if (ecos_b == NULL)
		return ECOS_FATAL;
	for (i = 0; i < ecos_p; i++)
	{
		if (i < n)
		{
			*(ecos_b + i) = *(x0 + i);
		}
		else
		{
			*(ecos_b + i) = 0;
		}
	}
	*(ecos_b + ecos_p - 2) = -0.5;
	*(ecos_b + ecos_p - 1) = 0.5;

	mywork = ECOS_setup(ecos_n, ecos_m, ecos_p, ecos_l, ecos_ncones, ecos_q, ecos_Gpr, ecos_Gjc, ecos_Gir, ecos_Apr, ecos_Ajc, ecos_Air, ecos_c, ecos_h, ecos_b);
	if( mywork != NULL ){
		mywork->stgs->verbose = 0;
		// solve 	
		exitflag = ECOS_solve(mywork);

		for (i = 0; i < p; i++)
		{
			*(u0+i) = mywork->x[i+n];
		}
		ECOS_cleanup(mywork, 0);
	}

	FREE(ecos_Gpr);
	FREE(ecos_Gjc);
	FREE(ecos_Gir);
	FREE(ecos_Apr);
	FREE(ecos_Ajc);
	FREE(ecos_Air);
	FREE(ecos_c);
	FREE(ecos_h);
	FREE(ecos_b);

	return exitflag;
}

