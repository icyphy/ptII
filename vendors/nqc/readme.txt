NQC 2.1 r2 Readme
-----------------

Please send bug reports to dbaum@enteract.com.  Be sure to include
details about what platform you are running nqc on and a sample
file that demonstrates the bug if possible.

For updates and additional documentation, visit the NQC Web Site:
http://www.enteract.com/~dbaum/nqc


Getting started
---------------

Download the appropriate compiler (nqc or nqc.exe) and put it where
your shell can find it as a command.

The IR tower should be connected to your modem port (macintosh) or COM1
(Win32/Linux). The IR tower should be set for "near" mode (small triangle).
The RCX should also be set for this mode, and firmware must already be
downloaded.

Compile and download the test file using the following command line:

nqc -d test.nqc

The test program assumes theres a motor on output A and a touch sensor on
input 1.  It turns on the motor and waits for the switch to be pressed,
then it turns off the motor and plays a sound.

If you want to use a different serial port, you can set the RCX_PORT
environment variable to the serial port name.  Here are some examples:

Mac/MPW printer port:
	set -e RCX_PORT b

Win32 COM2 port:
	set RCX_PORT=COM2

Linux:
	The syntax for setting environment variables is shell specific.  By
	default nqcc uses "/dev/ttyS0" as the device name for the serial port.
	If you are using the second serial port, then "/dev/ttyS1" should
	work.  Other device drivers may or may not work depending on if they
	implement the expected ioctl's to setup the baud rate, parity, etc.


Note that NQC no longer requires the RCX API include file (rcx.nqh).  However,
for reference purposes copies of the 1.3 and 2.0 api files (rcx1.nqh and
rcx2.nqh) are included in the NQC distribution.

version 2.1 r2
--------------

* disabled duplicate reduction code in rcxlib when sending RCX messages.  This
should only affect people using the rcxlib sources for their own projects - nqc
never directly used this behavior.

* added an error for recursive function calls

* fixed a bug in local variable allocation when one inline function calls a
second one

* orphan subs (those never invoked from any task) are now allowed to allocate
variables from the main task's pool (previously they couldn't allocate any
variables)


version 2.1 r1
--------------

* code generation for repeat() has been improved quite a bit
   - correctly implemented for Scout
   - nested repeats() are now legal
   - the repeat count can be greater than 255
   - the RCX loop counter is used whenever possible (non-nested
     repeat with compile time constant count of 0-255), otherwise
     a temporary variable is claimed to do the repeat.  Note that this
     will break code that used every single variable and then had a repeat
     count that came from a variable.  In previous versions of NQC, the
     repeat would use the built-in loop counter even though the repeat
     count (in the variable) may have exceeded 255.  The current version
     of NQC is more paranoid, and will not use the built-in loop counter
     in this case, thus a temporary variable needs to be allocated.

* total bytecode size is included in the listing


version 2.1 b3 (beta 3)
-----------------------

* Fixed bug where __SCOUT was not defined properly

* Output files now default to being placed in the current directory rather
that next to the source file.

* Trailing directory delimiter (e.g. / for Unix) is now optional with the
-I option.

* For WIN32, command line escaping of quotes (e.g. \") is disabled.  This
is a temporary measure for RcxCC compatability until RcxCC can be updated.


version 2.1 b2 (beta 2)
-----------------------

* Added the NQC_OPTIONS environment variable, which can be used to specify
extra options to be inserted into the command line.  For example, setting
NQC_OPTIONS to "-TScout" would cause nqc to target the Scout by default.

* A 'switch' statement was added. The generated code is reasonably good
considering the limitations of the RCX bytecodes.  However, some optimizations
(such as surpressing a 'break' in the last case) are not implemented [yet].
  
* Expressions are now coerced into conditions where appropriate.  For example,
you can do this:

  int x;
  
  while(x)
  {
  }
  

* Improved Scout support - battery level is checked on download, API file
merged into compiler so "scout.nqh" no longer needs to be included, PlayTone()
now supports both constant and variable argument for frequency.

* Switched over to official Lego mnemonics for those operations listed in the
Scout SDK.  Bytecodes not present on Scout still use the older NQC mnemonics,
but will change eventually.

* __type() operator added.  This is just a nasty low-level hack to
allow an inline function to call two different bytecodes depending on the
type of the argument passed in.  Used for PlayTone() when targeting the Scout.

* fixed a bug introduced in 2.1 b1 that caused problems using include files
with DOS line endings.


version 2.1 b1 (beta 1)
-----------------------

* Added preliminary support for Scout.  See "scout.txt" for more information
on Scout support in NQC.

* Added support for faster firmware downloading.  Firmware will download
about 4 times as fast with this option, but it requires the IR tower to
be in "near" mode.  If you have trouble getting the fast download to work,
please revert to the older (and slower) method.

  Fast:  nqc -firmfast <filename>
  Slow:  nqc -firmware <filename>

* The -o, -e, and -s options have been removed (they were deprecated quite
a while back).  If you still use these options, please change to the -O,
-E, and -S variant.

* The -c option (cybermaster support) has been deprectaed.  NQC now has a
more general option to specify the target: use -TCM for cybermaster:

  nqc -TCM -d test.nqc

Other targets include -TScout and -TRCX (the default).

* A couple of bugs relating to compiling stdin were fixed.



version 2.0.2
-------------

Fixed a bug which caused NQC to crash when compiling programs containing
functions with certain errors in them.


version 2.0.1
-------------

Fixed a bug that caused the compiler to crash when more than 32 variables
were used in a program.

The Win32 version no longer aborts due to serial driver errors - the retry
algorithm will remain in effect.  This makes download of very long programs
much more reliable (especially under WinNT).

The retry algorithm is now more forgiving if the IR tower doesn't echo the
serial data properly.  This makes very long downloads a little more reliable.


version 2.0
-----------

First official release of NQC 2.0
