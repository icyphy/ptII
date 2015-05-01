/***preinitBlock***/
#define initTime 50
#define alpha 0.5
unsigned int count;
// Stores the correction matrix
double correction_roll;
double correction_pitch;
double correction_yaw;
int correction_finger1;
int correction_finger2;
int correction_finger3;
int correction_finger4;
int correction_finger5;

// Stores the corrected glove values
double corrected_roll;
double corrected_pitch;
double corrected_yaw;
int corrected_finger1;
int corrected_finger2;
int corrected_finger3;
int corrected_finger4;
int corrected_finger5;
/**/

/***initBlock***/
count = 0;
//correction = new Glove;
//corrected = new Glove;
//Initialize correction values to 0
correction_roll = 0.0;
correction_pitch = 0.0;
correction_yaw = 0.0;
correction_finger1 = 0;
correction_finger2 = 0;
correction_finger3 = 0;
correction_finger4 = 0;
correction_finger5 = 0;

corrected_roll = 0.0;
corrected_pitch = 0.0;
corrected_yaw = 0.0;
corrected_finger1 = 0;
corrected_finger2 = 0;
corrected_finger3 = 0;
corrected_finger4 = 0;
corrected_finger5 = 0;
/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.
int gloveData_finger1 = $getAndFree(finger1Input);
int gloveData_finger2 = $getAndFree(finger2Input);
int gloveData_finger3 = $getAndFree(finger3Input);
int gloveData_finger4 = $getAndFree(finger4Input);
int gloveData_finger5 = $getAndFree(finger5Input);
double gloveData_roll = $getAndFree(rollInput);
double gloveData_pitch = $getAndFree(pitchInput);
double gloveData_yaw = $getAndFree(yawInput);

int iter;

// Normalize sensor data for initTime
if (count < initTime) {
    count++;
    correction_roll = (correction_roll*count + gloveData_roll)/(count+1);
    correction_pitch = (correction_pitch*count + gloveData_pitch)/(count+1);
    correction_yaw = (correction_yaw*count + gloveData_yaw)/(count+1);

    correction_finger1 = (correction_finger1*count + gloveData_finger1)/(count+1);
    correction_finger2 = (correction_finger2*count + gloveData_finger2)/(count+1);
    correction_finger3 = (correction_finger3*count + gloveData_finger3)/(count+1);
    correction_finger4 = (correction_finger4*count + gloveData_finger4)/(count+1);
    correction_finger5 = (correction_finger5*count + gloveData_finger5)/(count+1);
}
else if (count < 2*initTime) {
    count++;
}
else {
    corrected_roll = alpha*(gloveData_roll - correction_roll) + (1.0-alpha)*corrected_roll;
    corrected_pitch = alpha*(gloveData_pitch - correction_pitch) + (1.0-alpha)*corrected_pitch;
    corrected_yaw = alpha*(gloveData_yaw - correction_yaw) + (1.0-alpha)*corrected_yaw;

    corrected_finger1 = alpha*(gloveData_finger1 - correction_finger1) + (1.0-alpha)*corrected_finger1;
    corrected_finger2 = alpha*(gloveData_finger2 - correction_finger2) + (1.0-alpha)*corrected_finger2;
    corrected_finger3 = alpha*(gloveData_finger3 - correction_finger3) + (1.0-alpha)*corrected_finger3;
    corrected_finger4 = alpha*(gloveData_finger4 - correction_finger4) + (1.0-alpha)*corrected_finger4;
    corrected_finger5 = alpha*(gloveData_finger5 - correction_finger5) + (1.0-alpha)*corrected_finger5;
}

$put(finger1Output, corrected_finger1);
$put(finger2Output, corrected_finger2);
$put(finger3Output, corrected_finger3);
$put(finger4Output, corrected_finger4);
$put(finger5Output, corrected_finger5);
$put(rollOutput, corrected_roll);
$put(pitchOutput, corrected_pitch);
$put(yawOutput, corrected_yaw);
/**/

/***wrapupBlock***/
/**/

