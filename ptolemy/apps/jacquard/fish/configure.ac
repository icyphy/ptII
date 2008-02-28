#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
#
# Originally (?) from E. Jason Riedy's Spring 2004 CS 267 code.
#

AC_PREREQ(2.50)
AC_INIT(fish, sp05, yozo@cs.berkeley.edu)
AC_CONFIG_AUX_DIR(config)
AM_INIT_AUTOMAKE
AC_CONFIG_SRCDIR([aquarium.in])
AC_CONFIG_HEADER([config.h])
AC_CONFIG_MACRO_DIR([m4])

# Checks for programs.
AC_PROG_CC([icc ecc xlc_r xlc gcc cc])
AC_ISC_POSIX
AC_PROG_RANLIB
AC_ARG_VAR([PYTHON])
AC_PATH_PROG([PYTHON],[python python2.3 python2.2 python2.1],[/usr/bin/env python])

# Checks for libraries.
AC_CHECK_LIB([m], [sqrt])
ACX_PTHREAD([HAVE_PTHREADS=true])
AM_CONDITIONAL(COND_HAVE_PTHREADS, [test x$HAVE_PTHREADS = xtrue])
ACX_MPI([HAVE_MPI=true])
AM_CONDITIONAL(COND_HAVE_MPI,[test x$HAVE_MPI = xtrue])
ACX_C_OPENMP([HAVE_OPENMP=true])
AM_CONDITIONAL(COND_HAVE_OPENMP,[test x$HAVE_OPENMP = xtrue])
AC_CHECK_FUNCS(fmax)
AC_SUBST(HAVE_FMAX)
AC_CHECK_FUNCS(isnan)
AC_SUBST(HAVE_ISNAN)

dnl AC_ARG_VAR(UPCC,[UPC compiler])
dnl if test x"$UPCC" = x ; then
dnl   AC_PATH_PROGS([UPCC],[/usr/common/ftg/upc/bin/upcc],[echo])
dnl fi
dnl AM_CONDITIONAL(COND_HAVE_UPC,[test x$UPCC != xecho])

dnl AC_ARG_VAR(TCBUILD,[Titanium compiler])
dnl if test x"$TCBUILD" = x ; then
dnl   AC_PATH_PROGS([TCBUILD],[/usr/common/ftg/titanium/dist/bin/tcbuild /project/cs/titanium/b/temp/citris/dist/bin/tcbuild /project/cs/titanium/srs/i686-pc-linux-gnu/bin/tcbuild],[echo])
dnl fi
dnl AM_CONDITIONAL(COND_HAVE_TITANIUM,[test x$TCBUILD != xecho])

AM_CONDITIONAL(VAMPIR_ACTIVE,[test "x$VTLIB" != x])

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([float.h stdlib.h string.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_C_INLINE
AC_C_RESTRICT

AC_CONFIG_FILES([Makefile fish-lib/Makefile sequential/Makefile pthreads/Makefile])
AC_CONFIG_FILES([mpi/Makefile openmp/Makefile])
dnl AC_CONFIG_FILES([upc/Makefile titanium/Makefile])
AC_OUTPUT

echo ""
echo "Will compile: sequential"
if test x$HAVE_PTHREADS = xtrue; then
  echo  "              pthread"
fi
if test x$HAVE_MPI = xtrue; then
  echo  "              mpi"
  if test "x$VTLIB" != x; then
    echo  "              mpi with vampir"
  fi
fi
if test x$HAVE_OPENMP = xtrue; then
  echo  "              openmp"
fi

