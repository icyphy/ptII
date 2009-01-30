#ifndef _ARSMAIN_H_
#define __ARSMAIN_H__

extern double *speed, *front_angle;
extern int simStep;


void appDispatcher(void);
int startUp(void);
void motorController(void);
void dispatcherIRS(void);


#endif                                  /* _ARSMAIN_H_ */
