/***preinitBlock***/
static int $actorSymbol(xvalue);
/**/


/***initBlock***/
$actorSymbol(xvalue) = 0;
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
PlotPoint($actorSymbol(xvalue),$ref(input#$channel));
$actorSymbol(xvalue)++;
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
char buff[50];

   mymaxy = 89;
   myminx = 0;
   dotsize = 3;
   axissize = 5;
   myy = mymaxy-(y*8);

   myx = myminx +(x*10);

   //RIT128x96x4StringDraw(".",   myx,myy,20);
   sprintf(buff,"xv: %d yv: %d",x,y);
    RIT128x96x4StringDraw(buff,   0,50,20);


}


void PlotPoint1(int x,int y)
{

 int myx;
 int myy;
 int mymaxy;
 int myminx;
 int dotsize;
 int axissize;
 char buff[20];

   mymaxy = 89;
   myminx = 0;
   dotsize = 3;
   axissize = 5;
   myy = mymaxy-(y*8);

   myx = myminx +(x*10);

   //RIT128x96x4StringDraw(".",   myx,myy,20);
   sprintf(buff,"xv: %d yv: %d",x,y);
    RIT128x96x4StringDraw(buff,   0,60,20);
}



void PlotPoint2(int x,int y)
{

 int myx;
 int myy;
 int mymaxy;
 int myminx;
 int dotsize;
 int axissize;
 char buff[20];

   mymaxy = 47;
   myminx = 0;
   dotsize = 3;
   axissize = 5;
   myy = mymaxy-(y*8);

   myx = myminx +(x*10);
   // RIT128x96x4StringDraw(".",   myx,myy,20);
        sprintf(buff,"xv: %d yv: %d",x,y);
    RIT128x96x4StringDraw(buff,   0,70,20);


}
/**/

