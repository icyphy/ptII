#! /bin/csh
# sample Ptolemy II .cshrc
# $Id$

# By default, create files that are writable by only the user
# Locally, we override this in ucb-.cshrc
umask 022

# Home directory of the Ptolemy II installation
if (! $?PTII ) setenv PTII $HOME

# Try to set up for Java.
if ( ! $?PTJAVA_HOME ) then
	# Search the likely places
	if ( ! $?PTJAVA_HOME && -d /opt/jdk1.2latest ) then
		setenv PTJAVA_HOME /opt/jdk1.2latest
	endif
	if ( ! $?PTJAVA_HOME && -d /usr/java ) then
		setenv PTJAVA_HOME /usr/java
	endif
	if ( ! $?PTJAVA_HOME && -d /opt/jdk1.1.6 ) then
		setenv PTJAVA_HOME /opt/jdk1.1.6
	endif
	if ( ! $?PTJAVA_HOME && -d /opt/java) then
		setenv PTJAVA_HOME /opt/java
	endif
	if ( ! $?PTJAVA_HOME && -d /usr/eesww/lang/jdk1.1latest) then
		setenv PTJAVA_HOME /usr/eesww/lang/jdk1.1latest
	endif
	if ( ! $?PTJAVA_HOME && -d /usr/local/java) then
		setenv PTJAVA_HOME /usr/local/java
	endif
	if ( ! $?PTJAVA_HOME && -d /usr/sww/X11/bin/java) then
		setenv PTJAVA_HOME /usr/sww/X11/bin/java
	endif
endif

set javapath
if ( $?PTJAVA_HOME ) then
	# The user has PTJAVA_HOME set, so they must know what they are doing.
	set javapath=$PTJAVA_HOME/bin
	if ( ! $?JAVAHOME ) then
		setenv JAVAHOME $PTJAVA_HOME
	endif
endif

# Path may need adjusting, especially for accessing X programs.
set path = ( \
	$PTII/bin \
	$javapath \
	$PTII/vendors/sun/JavaScope/bin \
	/usr/X11/bin \
	/usr/openwin/bin \
	/usr/local/bin \
	/opt/SUNWspro/bin \
	/usr/ucb /usr/bin /bin \
	/usr/bsd \
	/usr/bin/X11 \
	/users/ptdesign/gnu/sol2.5/bin \
	/usr/ccs/bin \
	/usr/sww/bin /usr/sww/X11/bin . )

if ($?prompt) then
	# Set automatic file completion
	set filec
	set notify noclobber
	set history=100

	set shorthost=` hostname | sed -e 's/\..*$//'`
	# hppa does not seem to set $user
	set myusername=`whoami`
	set prompt="$myusername@$shorthost \!% "

	# Useful on the hppa
	unset autologout

	# some useful aliases
	alias ls ls -CF
	alias h history
	alias j jobs -l
endif

# This is required because of the upgrade to X11R5 on our system 
# causes the LD_LIBRARY_PATH to be mis-set as far as THOR is concerned.
# the following corrects for this.
# if (! $?LD_LIBRARY_PATH) setenv LD_LIBRARY_PATH ""
setenv LD_LIBRARY_PATH /usr/ccs/lib:/usr/lib:/usr/openwin/lib:/usr/sww/X11/lib:/usr/sww/sunos-X11R5/lib

# Source UCB local modifications
if (-r ~/ucb-.cshrc) then
	source ~/ucb-.cshrc
endif
