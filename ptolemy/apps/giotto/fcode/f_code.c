/*

 Copyright (c) 2001-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

*/

/*

  Author: Christoph Kirsch, cm@eecs.berkeley.edu
          Slobodan Matic, matic@eecs.berkeley.edu

*/

#include "f_code.h"

void GPS_device_driver_fire(double &GPS) {
    // Low-level driver code.
    GPS = 10.0;
}

void motor_device_driver_fire(double &motor) {
    // Low-level motor driver code.
}

void init_function_name_A(double &port) {
    // This needs to be synthesized by Ptolemy II.
    port = 0.0;
}

void copy_double(double &local_A_out, double &global_A_out) {
    // This needs to be synthesized by Ptolemy II.
    global_A_out = local_A_out;
}

void init_function_name_B(double &port) {
    // This needs to be synthesized by Ptolemy II.
    port = 0.0;
}

// This coult be a private port in Giotto, but not yet supported
// in the compiler.
double A_state = 0.0;
void A_fire(double &Ain1, double &Ain2, double &local_A_out) {
    // Fire method.
    A_state += 1.0;
    local_A_out = Ain1 + Ain2 + A_state;
}    

void B_fire(double &Bin1, double &local_B_out) {
    // Fire method.
    local_B_out = Bin1 * 2.0;
}    

unsigned motor_guard(double &out_port) {
  return 1;
}

void motor_transferOutputs(double &out_port, double &motor_port) {
  motor_port = out_port;
}

unsigned A_guard(double &GPS_port, double &out_port) {
  return 1;
}

void A_transferInputs(double &GPS_port,
                      double &out_port,
                      double &Ain1_port,
                      double &Ain2_port) {
  Ain1_port = GPS_port;
  Ain2_port = out_port;
}

unsigned B_guard(double &GPS_port) {
  return 1;
}

void B_transferInputs(double &GPS_port, double &Bin1_port) {
  Bin1_port = GPS_port;
}

/* The following needs to be kept verbatim. */

void giotto_timer_enable_code(e_machine_type e_machine, int relative_time) {
}

int giotto_timer_save_code(void) {
  int current_time = get_time();

  return current_time;
}

unsigned giotto_timer_trigger_code(int initial_time, int relative_time) {
  int current_time = get_time();

  return (current_time == (initial_time + relative_time) % get_time_overflow());
}
