# Berkeley-local stuff
# $Id$

if (-x /usr/bin/X11) set path = ($path /usr/bin/X11)
# Make files group writable.  The .cshrc we distribute should not
# have umask set to 2, it should be set to 22.
umask 02


# Get kerberos in path before sun rsh and kinit
# Get software warehouse in the path, however this will mean that
#  gcc uses gnm from sww/bin, so when building a ptolemy to distribute
#  don't include gcc in the path
set path = ( 	/usr/krb5/bin \
		$path \
		/usr/sww/bin )


# Setup that is specific to certain architectures is in this section

if ( "$PTARCH" =~ sol?* ) then
	# Needed for SUN CC, may interfere with Synopsys
	setenv LM_LICENSE_FILE /opt/lm/lmgrd.key

	# Synopsys needs these
	# Old version 3.4a
	# setenv SYNOPSYS /vol/markov/markov1/synopsys
	# New version 3.4b
	setenv SYNOPSYS /usr/tools/synopsys
	setenv SIM_ARCH sparcOS5
	# vhdlan which is run in utils/ptvhdlsim requires that $ARCH be set
	setenv ARCH $PTARCH

	# Get Synopsys in path
	set path = ( $path \
		$SYNOPSYS/$SIM_ARCH/syn/bin \
                $SYNOPSYS/$SIM_ARCH/sim/bin \
                $SYNOPSYS/$SIM_ARCH/sge/bin )

	# These are for accessing online documentation
	# through "iview" and SOLV-IT On-Line through "sos".
	set path = ( $path \
		$SYNOPSYS/worldview/bin \
                $SYNOPSYS/sos/bin )

	# Needed for Synopsys to find libCLI.so
	setenv LD_LIBRARY_PATH \
		${LD_LIBRARY_PATH}:${SYNOPSYS}/${SIM_ARCH}/sim/lib

	# Only include /usr/tools/bin in our path if we are running under
	# Solaris otherwise the sun4 build will fail because
	# /usr/tools/mathematica is Solaris 
	set path = ($path /usr/tools/bin)
endif

if ( "$PTARCH" =~ hppa* ) then
	# HPPA needs these
	setenv PT_DISPLAY "xterm -e vi %s"
else
	# For Purify: allow 128 files to be open at the same time
	# HPPA does not have a limit command
	limit descriptors 128
endif


# Setup that is generic to all platforms at UCB is below here

if ($?prompt) then
	# The alias below depends on stuff local to UCB, so it should
	# not be shipped
#	if ($TERM == "xterm") then
#		alias cd 'cd \!* ; (echo -n "]2;ptuser:`$HOME/adm/ptuser/scwd`]1;PT:`$HOME/adm/ptuser/scwd short`" &)'
#	endif
endif

# Modify the printer variable
setenv PRINTER sp524

# For FrameMaker
setenv FMHOME /opt/frame-5.5
setenv FM_FLS_HOST brahe.eecs.berkeley.edu
set path = ($path $FMHOME/bin /usr/sww/urt/bin)

# For Quadralay, which converts frame files to html
setenv QUADRALAYHOME /usr/tools/tools2/www/quadralay
set path = ($path $QUADRALAYHOME/bin)

# Needed for s56x demos which are meant for Sun workstations
setenv S56DSP /users/ptdesign/vendors/s56dsp
setenv QCKMON qckMon5
setenv VSDKHOME /opt/SUNWvsdk
setenv INCASHOME /opt/SUNWincas
if ( $?LD_LIBRARY_PATH ) then
       setenv LD_LIBRARY_PATH ${LD_LIBRARY_PATH}:${S56DSP}/lib
endif
