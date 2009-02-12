/***preinitBlock***/
//preinit code should be here
printf("preinit code should go here");
/**/


/***initBlock***/
//init code should be here
 
/**/

/***configureBlock($text)***/
//configure code should be here
printf("configure code should go here");
/**/

/***plotBlock($channel)***/

//printf("plotBlock code should go here for %d",$ref(input#$channel));

/**/


/***plotBlock1($channel)***/

PlotPoint1(8,$ref(input#$channel));

/**/

/***plotBlock2($channel)***/

PlotPoint2(x,$ref(input#$channel));

/**/





/***sharedBlock***/
void PlotPoint(int x,int y)
{

 int myx;
 int myy;
 int mymaxy;
 int myminx;
 int dotsize;
 int axissize;

   mymaxy = 89;
   myminx = 0;
   dotsize = 3;
   axissize = 5;
   myy = mymaxy-(y*8);

   myx = myminx +(x*10);

   RIT128x96x4StringDraw(".",   myx,myy,20);
   

}


void PlotPoint1(int x,int y)
{

 int myx;
 int myy;
 int mymaxy;
 int myminx;
 int dotsize;
 int axissize;

   mymaxy = 89;
   myminx = 0;
   dotsize = 3;
   axissize = 5;
   myy = mymaxy-(y*8);

   myx = myminx +(x*10);

   RIT128x96x4StringDraw(".",   myx,myy,20);
   

}
void PlotPoint2(int x,int y)
{

 int myx;
 int myy;
 int mymaxy;
 int myminx;
 int dotsize;
 int axissize;

   mymaxy = 47;
   myminx = 0;
   dotsize = 3;
   axissize = 5;
   myy = mymaxy-(y*8);

   myx = myminx +(x*10);

   RIT128x96x4StringDraw(".",   myx,myy,20);
   

}
/**/
