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

#ifndef _F_CODE_
#define _F_CODE_

#include <stdio.h>
#include <stdlib.h>
#include <sys/ioctl.h>

#include "f_table.h"
#include "f_spec.h"
#include "f_os_interface.h"

#define MAXDISPLAY 20

typedef int c_int;

typedef unsigned c_bool;

typedef char c_string[MAXDISPLAY];

void c_connect_sensor_to_return_key(c_bool *);

void c_connect_sensor_to_random_generator(c_int *);

void c_connect_actuator_to_display(c_string *);

void c_empty_string(c_string *);

void copy_c_string(c_string *, c_string *);

void c_one(c_int *);

void copy_c_int(c_int *, c_int *);

void c_control_task(c_int *, c_string *);

void c_navigation_task(c_int *, c_int *, c_int *);

unsigned c_true();

void c_string_to_string(c_string *, c_string *);

unsigned c_key_pressed(c_bool *);

void c_switch_mode(c_int *);

void c_int_to_int(c_int *, c_int *);

void c_int_int_to_int_int(c_int *, c_int *, c_int *, c_int *);

void giotto_timer_enable_code(e_machine_type, int);

int giotto_timer_save_code(void);

unsigned giotto_timer_trigger_code(int, int);

#endif






