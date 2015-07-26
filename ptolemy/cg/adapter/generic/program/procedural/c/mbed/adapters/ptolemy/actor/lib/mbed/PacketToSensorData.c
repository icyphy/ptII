/***preinitBlock***/
#define STREAM_FINGERS_QUATERNION 1
#define STREAM_QUATERNION 2
#define STREAM_FINGERS_RAW 3
#define STREAM_RAW 4
#define STREAM_FINGERS 5

int q0, q1, q2, q3;
double q00, q11, q22, q33;

float roll_value, pitch_value, yaw_value;

int iter, timeOfArrival, id, pkgtype;
char bcc;

// Stores the correction matrix
int glove_roll;
int glove_pitch;
int glove_yaw;
int glove_finger1;
int glove_finger2;
int glove_finger3;
int glove_finger4;
int glove_finger5;
int k, j;
double norm;
/**/

/***initBlock***/
k = 0;
j = 0;
/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.
Token * sensor_data = $get(dataPacket);

int array_size = sensor_data->payload.IntArray->size;

int *Buf = malloc(array_size * sizeof(int));

for (iter = 0; iter < array_size; iter++) {
    Buf[iter] = IntArray_get(sensor_data, iter);
}

if ((int) Buf[0] == 0x24) {
    if ((char) Buf[1] == 0x0a) {
        //timeOfArrival = (Buf[6] << 24) + (Buf[7] << 16) + (Buf[8] << 8) + Buf[9];
        //id = (Buf[4] << 8) + Buf[5];
        pkgtype = Buf[3];

        switch (pkgtype){
            case STREAM_FINGERS_QUATERNION:
            {
                k = 10;
                q0 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;
                q1 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;
                q2 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;
                q3 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;

                glove_finger1 = ((Buf[k] << 8) + Buf[k + 1]);
                k += 2;
                glove_finger2 = ((Buf[k] << 8) + Buf[k + 1]);
                k += 2;
                glove_finger3 = ((Buf[k] << 8) + Buf[k + 1]);
                k += 2;
                glove_finger4 = ((Buf[k] << 8) + Buf[k + 1]);
                k += 2;
                glove_finger5 = ((Buf[k] << 8) + Buf[k + 1]);
                k += 2;

                q00 = q0 / 32768.0;
                q11 = q1 / 32768.0;
                q22 = q2 / 32768.0;
                q33 = q3 / 32768.0;

                norm = sqrt(q00 * q00 + q11 * q11 + q22 * q22 + q33 * q33);

                roll_value = 180.0 * atan2(2 * (q00 * q11 + q22 * q33), 1 - 2 * (q11 * q11 + q22 * q22)) / 3.1415;
                pitch_value = 180.0 * asin(2 * (q00 * q22 - q33 * q11)) / 3.1415;
                yaw_value = 180.0 * atan2(2 * (q00 * q33 + q11 * q22), 1 - 2 * (q22 * q22 + q33 * q33)) / 3.1415;

                glove_roll = (int) roll_value;
                glove_pitch = (int) pitch_value;
                glove_yaw = (int) yaw_value;
            }
            break;
            case STREAM_QUATERNION:
            {
                k = 10;
                q0 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;
                q1 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;
                q2 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;
                q3 = (Buf[k] << 24) + (Buf[k+1] << 16) + (Buf[k+2] << 8) + Buf[k+3];
                k += 4;

                glove_finger1 = 0;
                glove_finger2 = 0;
                glove_finger3 = 0;
                glove_finger4 = 0;
                glove_finger5 = 0;

                q00 = q0 / 32768.0;
                q11 = q1 / 32768.0;
                q22 = q2 / 32768.0;
                q33 = q3 / 32768.0;

                norm = sqrt(q00 * q00 + q11 * q11 + q22 * q22 + q33 * q33);
                roll_value = 180.0 * atan2(2 * (q00 * q11 + q22 * q33), 1 - 2 * (q11 * q11 + q22 * q22)) / 3.1415;
                pitch_value = 180.0 * asin(2 * (q00 * q22 - q33 * q11)) / 3.1415;
                yaw_value = 180.0 * atan2(2 * (q00 * q33 + q11 * q22), 1 - 2 * (q22 * q22 + q33 * q33)) / 3.1415;

                glove_roll = (int) roll_value;
                glove_pitch = (int) pitch_value;
                glove_yaw = (int) yaw_value;
            }
            break;
            case STREAM_FINGERS_RAW:
            {
                k = 10;
                /*
                TODO: If you want to use the gyro, accel or magn data implement this.
                You will need to create new ports and send the data here to them.
                for (j = 0; j < 3; j++)
                {
                    gl.gyro[j] = (Buf[k] << 8) + (Buf[k+1]);
                    if (glove_gyro[j] > 0x7fff)
                        glove_gyro[j] -= 0x10000;
                    k += 2;
                }
                for (j = 0; j < 3; j++)
                {
                    gl.magn[j] = (Buf[k] << 8) + (Buf[k+1]);
                    if (glove_magn[j] > 0x7fff)
                        glove_magn[j] -= 0x10000;
                    k += 2;
                }
                for (j = 0; j < 3; j++)
                {
                    gl.accel[j] = (Buf[k] << 8) + (Buf[k+1]);
                    if (glove_accel[j]>0x7fff)
                        glove_accel[j] -= 0x10000;
                    k += 2;
                }
                */

                //Doing this to move over the gyro, accel and magn data
                for (j = 0; j < 9; j++) {
                    k += 2;
                }

                glove_finger1 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger2 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger3 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger4 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger5 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;

                glove_roll = 0;
                glove_pitch = 0;
                glove_yaw = 0;
            }
            break;
            case STREAM_RAW:
            {
                k = 10;
                /*
                TODO: If you want to use the gyro, accel or magn data implement this.
                You will need to create new ports and send the data here to them.
                for (j = 0; j < 3; j++){
                    gl.gyro[j] = (Buf[k] << 8) + (Buf[k+1]);
                    if (gl.gyro[j] > 0x7fff)
                        gl.gyro[j] -= 0x10000;
                    k += 2;
                }
                for (j = 0; j < 3; j++){
                    gl.magn[j] = (Buf[k] << 8) + (Buf[k+1]);
                    if (gl.magn[j] > 0x7fff)
                        gl.magn[j] -= 0x10000;
                    k += 2;
                }
                for (j = 0; j < 3; j++){
                    gl.accel[j] = (Buf[k] << 8) + (Buf[k+1]);
                    if (gl.accel[j] > 0x7fff)
                        gl.accel[j] -= 0x10000;
                    k += 2;
                }
                int y = 0;
                for (y = 0; y < 5; y++){
                    gl.fingers[y] = 0.0;
                }
                */

                glove_finger1 = 0;
                glove_finger2 = 0;
                glove_finger3 = 0;
                glove_finger4 = 0;
                glove_finger5 = 0;

                glove_roll = 0;
                glove_pitch = 0;
                glove_yaw = 0;
            }
            break;

            case STREAM_FINGERS:
            {
                k = 10;

                glove_finger1 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger2 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger3 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger4 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;
                glove_finger5 = ((Buf[k] << 8) + Buf[k+1]);
                k += 2;

                glove_roll = 0;
                glove_pitch = 0;
                glove_yaw = 0;
            }
            break;
        }
    }
}

IntArray_delete(sensor_data);
free(sensor_data);
free(Buf);

$put(finger1, glove_finger1);
$put(finger2, glove_finger2);
$put(finger3, glove_finger3);
$put(finger4, glove_finger4);
$put(finger5, glove_finger5);
$put(roll, glove_roll);
$put(pitch, glove_pitch);
$put(yaw, glove_yaw);
/**/

/***wrapupBlock***/
/**/
