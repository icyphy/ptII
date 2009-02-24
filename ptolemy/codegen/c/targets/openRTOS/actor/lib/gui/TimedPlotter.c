/***preinitBlock***/
static int $actorSymbol(xvalue);
/**/


/***initBlock***/
$actorSymbol(xvalue) = 0;
//printf("init code should go here");
{
     int mlc,i;
     mlc = 0;
     //RIT128x96x4StringDraw("TimedPlotterTest",25,  0, 15);
     for(i = 0; i < 128; i+= 5)
     {
       RIT128x96x4StringDraw("_",   i,89,5);
     }
     for(i = 0; i < 96; i +=8)
     {
       RIT128x96x4StringDraw("|",   0,i,5);
     }
     for(i = 0; i < 128; i+= 5)
     {
       RIT128x96x4StringDraw("_",   i,41,5);
     }
}
 
/**/

/***configureBlock($text)***/
//configure code should be here
/**/

/***plotBlock($channel)***/
//printf("plotBlock code should go here for %d",$ref(input#$channel));

/**/


/***plotBlock1($channel)***/

PlotPoint1($actorSymbol(xvalue),$ref(input#$channel));
$actorSymbol(xvalue)++;
/**/

/***plotBlock2($channel)***/

PlotPoint2($actorSymbol(xvalue),$ref(input#$channel));
$actorSymbol(xvalue)++;

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
