#include <stdio.h>
#include "polytope.h"

void display_polytope(Polytope* p){
	int i;
	printf("Number of inequalities: %d, Dimension of the polytope: %d\n",p->k,p->l);
	printf("A matrix: ");
	for(i=0;i<((p->k)*(p->l));i++)
	{
		printf("%5.2f, ",*(p->A+i));
	}
	printf("\nb vector: ");
	for(i=0;i<p->k;i++)
	{
		printf("%5.2f, ",*(p->b+i));
	}
	printf("\ncenter: ");
	for(i=0;i<p->l;i++)
	{
		printf("%5.2f, ",*(p->center+i));
	}
	printf("\n");
}

int isInside(const Polytope* p, const pfloat x[]) {
	idxint k = p->k;
	idxint l = p->l;
	pfloat *A = p->A;
	int i,j;
	pfloat rowSum;
	for(j=0;j<k;j++)
	{
		rowSum = 0;
		for(i=0;i<l;i++)
		{
			rowSum+= *(A+i*k+j) * x[i];
		}
		if(rowSum < p->b[i])
		{
			return 0;
		}
	}
	return 1;
}

Polytope* from_box(idxint l,pfloat* upper,pfloat* lower){
	Polytope *p = (Polytope*)MALLOC(sizeof(Polytope));
	int i;
	idxint k = l*2;
	p->k = k;
	p->l = l;
	p->A = (pfloat*)MALLOC(sizeof(pfloat)*l*k);
	for(i=0;i< l*k;i++)
	{
		*(p->A+i) = 0;
	}
	p->b = (pfloat*)MALLOC(sizeof(pfloat)*k);
	p->center = (pfloat*)MALLOC(sizeof(pfloat)*l);
	for(i=0;i<l;i++)
	{
		*(p->A+i*k+2*i) = 1;	
		*(p->A+i*k+2*i+1) = -1;	
		*(p->b+2*i) = *(lower+i);
		*(p->b+2*i+1) =-1*(*(upper+i));
		*(p->center+i) = (*(upper+i) + *(lower+i))/2;
	}
	return p;
}

Polytope* create_poly(idxint k,idxint l,pfloat* A,pfloat* b,pfloat* center){
	Polytope *p = (Polytope*)MALLOC(sizeof(Polytope));
	p->k = k;
	p->l = l;
	p->A = A;
	p->b = b;
	p->center = center;
	return p;
}

Polytope* cartesian_prod(Polytope* p1, Polytope* p2)
{
	idxint k1 = p1->k;
	idxint k2 = p2->k;
	idxint l1 = p1->l;
	idxint l2 = p2->l;
	idxint k = k1+k2;
	idxint l = l1+l2;
	pfloat* A = (pfloat*) MALLOC(sizeof(pfloat)*k*l);
	pfloat* b = (pfloat*) MALLOC(sizeof(pfloat)*k);
	pfloat* center = (pfloat*) MALLOC(sizeof(pfloat)*l);
	int i,j;
	for(i=0;i<k;i++)
	{
		for(j=0;j<l;j++)
		{
			if(i<k1 && j<l1)
			{
				*(A+i+j*k) = *(p1->A+i+j*k1);
			}
			else if (i>=k1 && j>=l1)
			{
				*(A+i+j*k) = *(p2->A+(i-k1)+(j-l1)*k2);
			}
			else
			{
				*(A+i+j*k) = 0;
			}
		}
	}
	for(i=0;i<k;i++)
	{
		if (i < k1)
		{
			*(b+i) = *(p1->b+i);
		}
		else
		{
			*(b+i) = *(p2->b+i-k1);
		}
	}
	for(j=0;j<l;j++)
	{
		if (j < l1)
		{
			*(center+j) = *(p1->center+j);
		}
		else
		{
			*(center+j) = *(p2->center+j-l1);
		}

	}
	Polytope* p = create_poly(k,l,A,b,center);
	return p;
}

void free_polytope(Polytope* p)
{
	FREE(p->A);
	FREE(p->b);
	FREE(p->center);
	FREE(p);
}
