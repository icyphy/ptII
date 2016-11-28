/** Output sensor data.
 *  FIXME: explain the sensor data.
 *  FIXME: Can't have more than one of this actor.
 *  E.g.,
 *    - distance: An integer giving the total accumulated travel distance.
 *    - angle:
 */
/***preinitBlock***/
volatile int16_t distance = 0;
volatile int16_t angle = 0;
volatile uint8_t sensors[Sen0Size];
/**/

/***initBlock***/
  // Set the sensor data to be all zero.
  // This initializes the buffer that gets
  // filled by the interrupt service routine that
  // reads from the serial port.
  for(int i = 0; i < Sen6Size; i++) {
     sensors[i] = 0x0;
  }
/**/

/***fireBlock***/
  // Request Sensors Packet 2
  byteTx(CmdSensors);
  // Request packet 0, which has 26 bytes of information.
  byteTx(0);

  for(int i = 0; i < Sen0Size; i++) {
    sensors[i] = byteRx();
  }

  // Output aggregated data for wheel drops and bumps.
  $ref(wheelDrop) = sensors[SenBumpDrop] & WheelDropAll;
  $ref(bump) = sensors[SenBumpDrop] & BumpEither;
  $ref(wall) = sensors[SenWall];
  $ref(cliffLeft) = sensors[SenCliffL];
  $ref(cliffFrontLeft) = sensors[SenCliffFL];
  $ref(cliffFrontRight) = sensors[SenCliffFR];
  $ref(cliffRight) = sensors[SenCliffR];
  $ref(virtualWall) = sensors[SenVWall];
  $ref(advanceButton) = sensors[SenButton] & ButtonAdvance;
  $ref(playButton) = sensors[SenButton] & ButtonPlay;
  // Update accumulated distance and angle.
  distance += (int)((sensors[SenDist1] << 8) | sensors[SenDist0]);
  $ref(distance) = distance;
  angle += (int)((sensors[SenAng1] << 8) | sensors[SenAng0]);
  $ref(angle) = angle;
/* FIXME: Skipped the battery state
#define SenChargeState  16
#define SenVolt1        17
#define SenVolt0        18
#define SenCurr1        19
#define SenCurr0        20
#define SenTemp         21
#define SenCharge1      22
#define SenCharge0      23
#define SenCap1         24
#define SenCap0         25
*/

/**/

/***wrapupBlock***/
/**/

