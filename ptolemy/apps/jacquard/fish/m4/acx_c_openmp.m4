AC_DEFUN([ACX_C_OPENMP], [
AC_PREREQ(2.50)

AC_REQUIRE([AC_PROG_CC])
AC_ARG_VAR(OPENMP_CFLAGS,[OpenMP C compiler flags])
acx_c_openmp_flags="-openmp -qsmp=omp"
acx_c_openmp_ok="no"

if test x"$OPENMP_CFLAGS" != x ; then
  save_CFLAGS="$CFLAGS"
  CFLAGS="$OPENMP_CFLAGS $CFLAGS"
  AC_MSG_CHECKING([for OpenMP C flags $OPENMP_CFLAGS])
  AC_COMPILE_IFELSE([
#if !defined(_OPENMP)
#error "No OpenMP here!"
#endif
], [acx_c_openmp_ok=yes])
  AC_MSG_RESULT($acx_c_openmp_ok)
  if test x"$acx_c_openmp_ok" = xno ; then
    OPENMP_CFLAGS=""
  fi
  LIBS="$save_LIBS"
  CFLAGS="$save_CFLAGS"
fi

if test x"$acx_c_openmp_ok" != xyes ; then
	AC_MSG_CHECKING([for OpenMP C compiler flags])

	for flag in $acx_c_openmp_flags ; do
	        save_CFLAGS="$CFLAGS"
		OPENMP_CFLAGS="$flag"
		CFLAGS="$OPENMP_CFLAGS $save_CFLAGS"

		AC_COMPILE_IFELSE([
#if !defined(_OPENMP)
#error "No OpenMP here!"
#endif
], [acx_c_openmp_ok=yes])
	
	        CFLAGS="$save_CFLAGS"

        	if test "x$acx_c_openmp_ok" = xyes; then 
        		break;
	        fi
		OPENMP_CFLAGS=""
	done
       	if test "x$acx_c_openmp_ok" = xyes; then 
	        AC_MSG_RESULT($OPENMP_CFLAGS)
	else
	        AC_MSG_RESULT($acx_c_openmp_ok)
        fi
	AC_SUBST(OPENMP_CFLAGS)
fi

# Finally, execute ACTION-IF-FOUND/ACTION-IF-NOT-FOUND:
if test x = x"$OPENMP_CFLAGS"; then
        $2
        :
else
	$1
        :
fi
])dnl ACX_C_OPENMP
