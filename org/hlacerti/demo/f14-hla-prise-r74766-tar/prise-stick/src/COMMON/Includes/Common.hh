#ifndef COMMON_HH_DEF
#define COMMON_HH_DEF

#define SYNCHRO_INITIALISATION 1
#define SYNCHRO_SIMULATION 1

/******* TIME_MANAGEMENT FLAG *********/
/* 0 => Data Flow execution mode */
/* 1 => Time Management execution mode  */
/* 2 => A completer : Time management avec NERA/TARA */
#define TIME_MANAGEMENT 1

#define TRACE_INIT 0
#define TRACE_SIMU 1
#define TRACE_CYCLES 0
#define JOYSTICK_ENABLED 1

#define NB_CYCLES_50_HZ 75000
#define NB_CYCLES_100_HZ 150000

/* Order Commands*/
#define ORDER_LENGTH 1000000

#define ORDER_STOP 499991
#define ORDER_START 499992

/* XML Parser*/
#include "prise_parameters_parser.hpp"

#endif // COMMON_HH_DEF
