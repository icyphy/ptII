/***preinitBlock***/
#define MAX_COUNT 0
int counter;
int secondCounter;
bool flag;
double hue_val;
/**/

/***initBlock***/
counter = 0;
secondCounter = 0;
hue_val = 0; 
flag = false;
/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.
int finger1_val = $getAndFree(finger1);
int finger2_val = $getAndFree(finger2);
int finger3_val = $getAndFree(finger3);
int finger4_val = $getAndFree(finger4);
int finger5_val = $getAndFree(finger5);
double roll_val = $getAndFree(roll);
double pitch_val = $getAndFree(pitch);
double yaw_val = $getAndFree(yaw);

int deltaSize_val = 0;
int deltaX_val = 0;
int deltaY_val = 0;
int deltaZ_val = 0;

int fingerThreshold = 350;
int thumbThreshold = 200;

if (++counter >= MAX_COUNT) { 
    secondCounter++;
    bool fBent[5];

    fBent[0] = finger1_val > fingerThreshold;
    fBent[1] = finger2_val > fingerThreshold;
    fBent[2] = finger3_val > fingerThreshold;
    fBent[3] = finger4_val > fingerThreshold;
    fBent[4] = finger5_val > thumbThreshold;
    
    if (secondCounter > 3) {
        //printf("%d %d %d %d %d\r\n", fBent[0], fBent[1], fBent[2], fBent[3], fBent[4]);
        secondCounter = 0;
    }
    // Change size based on finger gesture  
    if (flag) {  
        if (fBent[0] && fBent[1] && fBent[2] && (!fBent[3]) && fBent[4]) 
            deltaSize_val = -1; 
        else if (fBent[0] && fBent[1] && (!fBent[2]) && (!fBent[3]) && fBent[4]) 
            deltaSize_val = 1; 
        flag = !flag;
    }
    else {
        flag = !flag;
    }
    
    if (fBent[0] && (fBent[1]) && (fBent[2]) && (!fBent[4]))
        hue_val = finger4_val/1000.0;  
    
    //Change position. Roll, pitch, and yaw are given as degrees. 
    if (roll_val > 10 )
        deltaX_val = -1;
    else if (roll_val < -10)
        deltaX_val = 1;
    if (pitch_val > 10)
        deltaZ_val = 1;
    else if (pitch_val < -10)
        deltaZ_val = -1;
    if (yaw_val > 10)
        deltaY_val = -1;
    else if (yaw_val < -10)
        deltaY_val = 1;
       
    counter = 0;
        
} 
$put(deltaSize, deltaSize_val);
$put(deltaX, deltaX_val);
$put(deltaY, deltaY_val);
$put(deltaZ, deltaZ_val);
$put(hue, hue_val);
/**/

/***wrapupBlock***/
/**/