/** Send a command to the iRobot Create to drive at
 *  the specified velocity, in mm per second, and at the
 *  specified angle, in mm.  The velocity can range from
 *  -500 to 500, and the radius from -2000 to 2000.

FIXME: Use portParameters.
FIXME: ExpertParameter STRAIGHT, etc.
FIXME: Details from the manual below:

The longer radii make Create drive straighter, while
the shorter radii make Create turn more. The radius is
measured from the center of the turning circle to the center
of Create. A Drive command with a positive velocity and a
positive radius makes Create drive forward while turning
toward the left. A negative radius makes Create turn toward
the right. Special cases for the radius make Create turn
in place or drive straight, as specified below. A negative
velocity makes Create drive backward.
NOTE: Internal and environmental restrictions may prevent
Create from accurately carrying out some drive commands.
For example, it may not be possible for Create to drive at
full speed in an arc with a large radius of curvature.
* Serial sequence: [137] [Velocity high byte] [Velocity low byte]
[Radius high byte] [Radius low byte]
* Available in modes: Safe or Full
* Changes mode to: No Change
* Drive data byte 1: Velocity (-500 - 500 mm/s)
* Drive data byte 2: Radius (-2000 - 2000 mm)
Special cases:
Straight = 32768 or 32767 = hex 8000 or 7FFF
Turn in place clockwise = hex FFFF
Turn in place counter-clockwise = hex 0001

 */

/***preinitBlock***/
// Send Create drive commands in terms of velocity and radius
#ifndef _DRIVE_DRIVE
#define _DRIVE_DRIVE
void drive(int16_t velocity, int16_t radius)
{
  byteTx(CmdDrive);
  byteTx((uint8_t)((velocity >> 8) & 0x00FF));
  byteTx((uint8_t)(velocity & 0x00FF));
  byteTx((uint8_t)((radius >> 8) & 0x00FF));
  byteTx((uint8_t)(radius & 0x00FF));
}
#endif
/**/

/***initBlock***/
  // As a precaution, stop driving.
  // RadStraight = 32768, the code for no turns.
  drive(0, RadStraight);
/**/

/***fireBlock***/
  drive($ref(velocity), $ref(radius));
  $ref(done) = true;
/**/

/***wrapupBlock***/
/**/

