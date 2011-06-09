AC_INIT([p1000], [0.1])
AM_INIT_AUTOMAKE([-Wall -Werror foreign])

AC_PROG_CC
AC_PROG_CXX
AC_PROG_YACC
AC_PROG_LEX
AC_PROG_RANLIB

AC_CHECK_LIB([pthread], [pthread_create], [THREAD_LIBS="-lpthread"],
	[AC_MSG_ERROR([No pthread library.])])

AC_CONFIG_HEADERS([config.h])
AC_CONFIG_FILES([
	Makefile
	c/Makefile
	c/common/Makefile
	c/demo/Makefile
	c/demo/get_time/Makefile
	c/demo/scheduler/Makefile
	c/demo/sensor_actuator/Makefile
	c/transformer/Makefile
	c/transformer/test/Makefile
	cpp/Makefile
])
AC_OUTPUT
