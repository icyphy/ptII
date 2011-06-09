/* -------------------------------------------------------------------
 *
 * File derivatives.c
 *
 * The derivatives function is called by the numerical recipies
 * numerical integration function rk4. it calculates the derivative of
 * the next state which the calling function will integrate. Our state
 * vector must include the control vector since the derivative of the
 * next state depends on the control vector and current state vector.
 *
 * This function is a feed through function. It identifies what dx
 * equals by including the A matrix and B matrix in each line. It also
 * incorporates nonlinear behavior of px, py, pz and nonphi, nonth,
 * and nonpsi, although these quantities are not used internally to
 * determine state.  They are outputs and depend on the other current
 * state variables.
 *
 * The state y is given by: [px, py, pz, nonphi, nonth, nonpsi, u, v,
 * w, phi, th, psi, p, q, r,a, b, rfb] The input u is given by: [ua1,
 * ub1, uthm, uref] and is incorporated into y in order to allow the
 * control inputs to be used in calculating dydx.  The control inputs
 * are the last 4 components of y[] and are updated externally to this
 * function by the controller every time step.
 *
 * ---------------------------------------------------------------- */

#include <math.h>
#include "derivatives.h"

/* -------------------------------------------------------------------
 *
 * Function derivatives
 *
 * Feed through of the dy = f(y,u) lines.  px, py, pz, and phi are
 * commented out because they are not used to set dydx.  They are
 * calculated purely for output purposes and not used internally.
 *
 * Inputs: x is the current time, y is the state vector which here
 * includes the control vector, dydx is what is calculated to be used
 * by the calling function in the numerical methods function.
 *
 * ---------------------------------------------------------------- */

void derivatives(float x, float y[], float dydx[]) {
  /* float px     = y[1]; */
  /* float py     = y[2]; */
  /* float pz     = y[3]; */
  float nonphi = y[4];
  float nonth  = y[5];
  float nonpsi = y[6];
  float u      = y[7];
  float v      = y[8];
  float w      = y[9];
  float phi    = y[10];
  float th     = y[11];
  /* float psi    = y[12]; */
  float p      = y[13];
  float q      = y[14];
  float r      = y[15];
  float a      = y[16];
  float b      = y[17];
  float rfb    = y[18];
  /* inputs: */
  float ua1    = y[19];
  float ub1    = y[20];
  float uthm   = y[21];
  float uref   = y[22];

  /* set the outputs to equal f(x,u) where f = Ax + Bu */
  /* d(px)/dt - position x: */

  dydx[1] = (float) ((cos(nonth) * cos(nonpsi)) * u + (sin(nonphi) * sin(nonth) * cos(nonpsi) -
    cos(nonphi) * sin(nonpsi)) * v + (cos(nonphi) * sin(nonth) * cos(nonpsi) + sin(nonth) * sin(nonpsi))
    * w);

  /* d(py)/dt - position y: */
  dydx[2] = (float) ((cos(nonth) * sin(nonpsi)) * u + (sin(nonphi) * sin(nonth) * sin(nonpsi) +
    cos(nonphi) * cos(nonpsi)) * v + (cos(nonphi) * sin(nonth) * sin(nonpsi) - sin(nonphi) * cos(nonpsi))
    * w);

  /* d(pz)/dt - position z: */
  dydx[3] = (float) ((-sin(nonth)) * u + (sin(nonphi) * cos(nonth)) * v + (cos(nonphi) *
    cos(nonth)) * w);

  /* d(nonphi)/dt - nonlinear roll: */
  dydx[4] = (float) (p + (sin(nonphi)*tan(nonth)) * q + (cos(nonphi)*tan(nonth)) * r);

  /* d(nonth)/dt - nonlinear pitch: */
  dydx[5] = (float) (cos(nonphi) * q - (sin(nonphi) * r));

  /* d(nonpsi)/dt - nonlinear heading: */
  dydx[6] = (float) ((sin(nonphi) / cos(nonth)) * q + (cos(nonphi) / cos(nonth)) * r);

  /* d(u)/dt - body velocity x: */
  dydx[7] = (float) ((-.1257 * u) - (32 * th) - (32 * a));

  /* d(v)/dt - body velocity y: */
  dydx[8] = (float) ((-.4247 * v) + (32 * phi) + (32 * b));

  /* d(w)/dt - body velocity z: */
  dydx[9] = (float) ((-38.9954 * a) + (-.7598 * w) + (8.4231 * r) + (9.6401 * b) + (70.5041 *
    uthm));

  /* d(phi)/dt - roll: */
  dydx[10] = (float) p;

  /* d(th)/dt - pitch: */
  dydx[11] = (float) q;

  /* d(psi)/dt - heading: */
  dydx[12] = (float) r;

  /* d(p)/dt - roll rate: */
  dydx[13] = (float) ((-.1677 * u) + (.087 * v) + (36.705 * a) + (161.1087 * b));

  /* d(q)/dt - pitch rate: */
  dydx[14] = (float) ((-.0823 * u) + (-.0518 * v) + (63.5763 * a) +
    (-19.4931 * b));

  /* d(r)/dt - yaw(heading) rate: */
  dydx[15] = (float) ((.0566 * w) + (-1.33 * p) + (-5.5105 * r) +
    (-44.8734 * rfb) + (23.626 * uthm) + (44.8734 * uref));

  /* d(a)/dt - longitudinal flapping angle of the main rotor: */
  dydx[16] = (float) ((-q) + (-3.4436 * a) + (.8287 * b) + (-.8417 *
    ua1) + (2.8231 * ub1));

  /* d(b)/dt - lateral flapping angle of main rotor: */
  dydx[17] = (float) ((-p) + (.3611 * a) + (-3.4436 * b) + (-2.409 *
    ua1) + (-.3511 * ub1));

  /* d(rfb)/dt - reference feedback: */
  dydx[18] = (float) ((1.8157 * r) + (-11.021* rfb));

  /* for u's, set derivatives = 0.  this means last value of u's will
     be used, until new u's are received from controller.  in the
     future, we may want to use interpolation here. */

  dydx[19] = 0.0;
  dydx[20] = 0.0;
  dydx[21] = 0.0;
  dydx[22] = 0.0;

  return;
}

#if 0
void derivatives(float x, float y[], float dydx[]) {
        /* float px     = y[1]; */
        /* float py     = y[2]; */
        /* float pz     = y[3]; */
        float nonphi = y[4];
        float nonth  = y[5];
        float nonpsi = y[6];
        float u      = y[7];
        float v      = y[8];
        float w      = y[9];
        float phi    = y[10];
        float th     = y[11];
        /* float psi    = y[12]; */
        float p      = y[13];
        float q      = y[14];
        float r      = y[15];
        float a      = y[16];
        float b      = y[17];
        float rfb    = y[18];
        /* inputs: */
        float ua1    = y[19];
        float ub1    = y[20];
        float uthm   = y[21];
        float uref   = y[22];

        /* set the outputs to equal f(x,u) where f = Ax + Bu */
        /* d(px)/dt - position x: */

        dydx[1] = (float) ((cos(nonth) * cos(nonpsi)) * u + (sin(nonphi) * sin(nonth) * cos(nonpsi) -
             cos(nonphi) * sin(nonpsi)) * v + (cos(nonphi) * sin(nonth) * cos(nonpsi) + sin(nonth) * sin(nonpsi))
                           * w);

        /* d(py)/dt - position y: */
        dydx[2] = (float) ((cos(nonth) * sin(nonpsi)) * u + (sin(nonphi) * sin(nonth) * sin(nonpsi) +
             cos(nonphi) * cos(nonpsi)) * v + (cos(nonphi) * sin(nonth) * sin(nonpsi) - sin(nonphi) * cos(nonpsi))
                           * w);

        /* d(pz)/dt - position z: */
        dydx[3] = (float) ((-sin(nonth)) * u + (sin(nonphi) * cos(nonth)) * v + (cos(nonphi) *
                                                                                 cos(nonth)) * w);

        /* d(nonphi)/dt - nonlinear roll: */
        dydx[4] = (float) (p + (sin(nonphi)*tan(nonth)) * q + (cos(nonphi)*tan(nonth)) * r);

        /* d(nonth)/dt - nonlinear pitch: */
        dydx[5] = (float) (cos(nonphi) * q - (sin(nonphi) * r));

        /* d(nonpsi)/dt - nonlinear heading: */
        dydx[6] = (float) ((sin(nonphi) / cos(nonth)) * q + (cos(nonphi) / cos(nonth)) * r);

        /* d(u)/dt - body velocity x: */
        dydx[7] = (float) ((-.1257 * u) - (32 * th) - (32 * a));

        /* d(v)/dt - body velocity y: */
        dydx[8] = (float) ((-.4247 * v) + (32 * phi) + (32 * b));

        /* d(w)/dt - body velocity z: */
        dydx[9] = (float) ((-38.9954 * a) + (-.7598 * w) + (8.4231 * r) + (9.6401 * b) + (70.5041 * uthm));

        /* d(phi)/dt - roll: */
        dydx[10] = (float) p;

        /* d(th)/dt - pitch: */
        dydx[11] = (float) q;

        /* d(psi)/dt - heading: */
        dydx[12] = (float) r;

        /* d(p)/dt - roll rate: */
        dydx[13] = (float) ((-.1677 * u) + (.087 * v) + (36.705 * a) + (161.1087 * b));

        /* d(q)/dt - pitch rate: */
        dydx[14] = (float) ((-.0823 * u) + (-.0518 * v) + (63.5763 * a) + (-19.4931 * b));

        /* d(r)/dt - yaw(heading) rate: */
        dydx[15] = (float) ((.0566 * w) + (-1.33 * p) + (-5.5105 * r) +
                            (-44.8734 * rfb) + (23.626 * uthm) + (44.8734 * uref));

        /* d(a)/dt - longitudinal flapping angle of the main rotor: */
        dydx[16] = (float) ((-q) + (-3.4436 * a) + (.8287 * b) + (-.8417 * ua1) + (2.8231 * ub1));

        /* d(b)/dt - lateral flapping angle of main rotor: */
        dydx[17] = (float) ((-p) + (.3611 * a) + (-3.4436 * b) + (-2.409 * ua1) + (-.3511 * ub1));

        /* d(rfb)/dt - reference feedback: */
        dydx[18] = (float) ((1.8157 * r) + (-11.021* rfb));

        /* for u's, set derivatives = 0.  this means last value of u's
           will be used, until new u's are received from controller.
           in the future, we may want to use interpolation here. */

        dydx[19] = 0.0;
        dydx[20] = 0.0;
        dydx[21] = 0.0;
        dydx[22] = 0.0;

        return;
}
#endif
