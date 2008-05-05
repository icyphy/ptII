/***fileDependencies***/
oi.h
iRobotFunctions.c
/**/

/***preinitBlock***/
// Include the functions file if it has not already been included.
#ifndef I_ROBOT_FUNCTIONS
#define I_ROBOT_FUNCTIONS
#include "iRobotFunctions.c"
#endif
/**/

/***initBlock***/
  // Initialize the microcontroller
  initializeRobot();

  // Turn on the Create power if off
  powerOnRobot();

  // Start the open interface
  byteTx(CmdStart);

  // Change to 28800 baud
  baud28k();

  // Take full control of the Create
  byteTx(CmdFull);

  // Get rid of unwanted data in the serial port receiver
  flushRx();
/**/

/***fireBlock***/
/**/

/***wrapupBlock***/
/**/

