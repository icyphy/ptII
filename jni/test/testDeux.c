#include <stdio.h>
double testDeux(int in, float inout) {
  printf("testDeux.c: %d, %d\n", in, inout);
  //in= 0 -> on fait rien                                                 
  if(in==0) {
    inout =-1;
    return -2.0;
  }
  //in = 1 -> on essaie                                                       
  if(in==1) {
    //inout=0.0-> on fait rien                                      
    if(inout==0.0)
      return -1.0;
    //inout=1.0-> on fait                                           
    if(inout==1.0){
      inout = 2.0;
    }
    return 0.0;
  }
  return 0.0;
}
