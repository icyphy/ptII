#! /bin/csh
# sample Ptolemy II .cshrc
# @(#).cshrc	1.2 06/24/98

set cdpath= ($HOME/src/domains $HOME/src)

# Set auto. file completion
set filec
set notify noclobber
set history=100

# By default, create files that are writable by only the user
# Locally, we override this in ucb-.cshrc
umask 022

# Home directory of the Ptolemy installation
if (! $?PTOLEMY) setenv PTOLEMY ~ptdesign
if (! $?PT_DEBUG) setenv PT_DEBUG ptgdb

# The arch script figures out what type of machine we are on.
if (! $?ARCH) setenv ARCH ` $PTOLEMY/bin/ptarch`
if (! $?PTARCH) setenv PTARCH ` $PTOLEMY/bin/ptarch`
if (! $?PTARCH) setenv PTARCH ` $PTOLEMY/bin/ptarch`
if (! $?PTII ) setenv PTII $HOME

# Try to set up for Java.

if ( ! $?JAVA_HOME ) then
	# Search the likely places
	if ( ! $?JAVA_HOME && -d /usr/java ) then
		setenv JAVA_HOME /usr/java
	endif
	if ( ! $?JAVA_HOME && -d /opt/jdk1.1.6 ) then
		setenv JAVA_HOME /opt/jdk1.1.6
	endif
	if ( ! $?JAVA_HOME && -d /opt/java) then
		setenv JAVA_HOME /opt/java
	endif
	if ( ! $?JAVA_HOME && -d /usr/eesww/lang/jdk1.1latest) then
		setenv JAVA_HOME /usr/eesww/lang/jdk1.1latest
	endif
	if ( ! $?JAVA_HOME && -d /usr/local/java) then
		setenv JAVA_HOME /usr/local/java
	endif
	if ( ! $?JAVA_HOME && -d /usr/sww/X11/bin/java) then
		setenv JAVA_HOME /usr/sww/X11/bin/java
	endif
endif

set javapath
if ( $?JAVA_HOME ) then
	# The user has JAVA_HOME set, so they must know what they are doing.
	set javapath=$JAVA_HOME/bin
	if ( ! $?JAVAHOME ) then
		setenv JAVAHOME $JAVA_HOME
	endif
endif

# Path may need adjusting, especially for accessing X programs.
# /usr/ccs/bin and /opt/SUNWspro/bin are necessary for Solaris2.x -cxh
# Leave /usr/tools/gnu/bin out of the path here, or the build may break -cxh
set path = ( \
	$PTOLEMY/bin \
	$PTOLEMY/bin.$PTARCH \
	$PTOLEMY/bin.$ARCH \
	$PTOLEMY/vendors/bin \
	$javapath \
	/usr/X11/bin \
	/usr/openwin/bin \
	/usr/local/bin \
	/opt/SUNWspro/bin \
	/usr/ucb /usr/bin /bin \
	/usr/bsd \
	/usr/bin/X11 \
	/usr/ccs/bin \
	/usr/sww/bin /usr/sww/X11/bin . )

if ($?prompt) then
	stty erase ^H
	set cdpath= ($HOME/src/domains $HOME/src)

	set shorthost=` hostname | sed -e 's/\..*$//'`
	# hppa does not seem to set $user
	set myusername=`whoami`
	set	prompt="$myusername@$shorthost \!% "

	set history = 22

	# Useful on the hppa
	unset autologout

	# some useful aliases
	alias ls ls -CF
	alias oops source .login
	alias term 'xterm -fn 9x15 -bg lightblue -fg black -bd violetred &'
	alias h history
	alias j jobs -l
	alias f 'fg %\!*'

	# The following aliases are useful for development of Ptolemy code:
	alias srcdir 'cd `echo $PWD | sed "s?/obj.$PTARCH/?/src/?"`'
	alias objdir 'cd `echo $PWD | sed "s?/src/?/obj.$PTARCH/?"`'
	alias cdo 'cd \!* >& /dev/null ; objdir; echo $PWD'

endif

# this should point to whatever your printer is named
setenv PRINTER lw

# This is required because of the upgrade to X11R5 on our system 
# causes the LD_LIBRARY_PATH to be mis-set as far as THOR is concerned.
# the following corrects for this.
# if (! $?LD_LIBRARY_PATH) setenv LD_LIBRARY_PATH ""
setenv LD_LIBRARY_PATH /usr/lib:/usr/openwin/lib:/usr/sww/X11/lib:/usr/sww/sunos-X11R5/lib

# If the following variables are set, then the gnu compiler will
# work, even if the distribution is not installed in /users/ptolemy:
#setenv GCC_EXEC_PREFIX $PTOLEMY/gnu/$PTARCH/lib/gcc-lib/$PTARCH/egcs-2.90.27/

# This variable is needed to find the libraries
#if ($PTARCH =~ hppa*) then
#	setenv SHLIB_PATH $PTOLEMY/lib.${PTARCH}:$PTOLEMY/octtools/lib.${PTARCH}:$PTOLEMY/gnu/$PTARCH/lib:$PTOLEMY/tcltk/itcl.{$PTARCH}/lib/itcl
#else
#	setenv LD_LIBRARY_PATH $PTOLEMY/lib.${PTARCH}:$PTOLEMY/octtools/lib.${PTARCH}:$PTOLEMY/gnu/$PTARCH/lib:$PTOLEMY/tcltk/itcl.{$PTARCH}/lib/itcl
#endif

# tycho -java will fail to find libjtk.sl if LD_LIBRARY_PATH is set.
if ($PTARCH =~ hppa?*) then
	unsetenv LD_LIBRARY_PATH
endif

# For VHDL Synopsys demos, uncomment the following:
# setenv SYNOPSYS /usr/tools/synopsys
# setenv SIM_ARCH sparcOS5
# set path = ( $path $SYNOPSYS/$SIM_ARCH/syn/bin $SYNOPSYS/$SIM_ARCH/sim/bin)

# For Motorola S56x card demos on the Sparc, you will need something like:
# setenv S56DSP /users/ptdesign/vendors/s56dsp
# setenv QCKMON qckMon5
# setenv LD_LIBRARY_PATH ${LD_LIBRARY_PATH}:${S56DSP}/lib


# To use the X11 pxgraph, follow the instructions
# in $PTOLEMY/src/pxgraph/README.txt and uncomment the next line.
#setenv PT_USE_X11_PXGRAPH yes

# Source UCB local modifications
if (-r ~/ucb-.cshrc) then
	source ~/ucb-.cshrc
endif
