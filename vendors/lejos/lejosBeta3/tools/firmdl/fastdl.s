/*
 *  fastdl.s
 *
 *  A hack so to get the ROM running in a state with a doubled serial
 *  baud rate, at least until it is turned off and back on again.
 *
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS"
 *  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 *  License for the specific language governing rights and limitations
 *  under the License.
 *
 *  The Original Code is Librcx code, released February 9, 1999.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  Contributor(s): Kekoa Proudfoot <kekoa@graphics.stanford.edu>
 */

    .lsym rom_init_handlers, 0x0688
    .lsym init_timer, 0x3b9a

    .lsym rom_main_loop_state, 0xee5e
    .lsym rom_dispatch_struct, 0xee64
    .lsym rom_counter_struct, 0xee74
    .lsym rom_power_off_minutes, 0xee80
    .lsym rom_update_function_state, 0xef06
    .lsym rom_use_complements_flag, 0xef51
	
    .global __start

__start:

    ; Set up as if ROM were waking RCX up

    mov.w   #15,r6
    mov.w   r6,@rom_power_off_minutes

    mov.w   #rom_dispatch_struct,r6
    push    r6
    mov.w   #rom_counter_struct,r6
    jsr     @init_timer
    adds.w  #2,r7

    mov.w   #rom_main_loop_state,r6
    jsr     @rom_init_handlers

    ; Double the baud rate

    mov.b   #103,r6l
    mov.b   r6l,@0xd9:8

    ; Turn off parity

    bclr    #5,@0xd8:8
	
    ; Turn off complements

    sub.b   r6l,r6l
    mov.b   r6l,@rom_use_complements_flag
	
    ; Hack the update function state so we don't hear two beeps

    mov.b   #2,r6l
    mov.b   r6l,@rom_update_function_state

    ; Hack the main loop state so we reenter ROM main loop correctly

    mov.b   #13,r6l
    mov.b   r6l,@rom_main_loop_state

    ; Return control back to ROM

    rts

    ; String needed for new firmware

    .string "Do you byte, when I knock?"
