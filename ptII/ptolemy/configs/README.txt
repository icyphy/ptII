$Id$

How to run a different configuration
------------------------------------

$PTII/bin/vergil runs $PTII/ptolemy/vergil/VergilApplication.
If you pass an argument that begins with a dash "-", then
VergilApplication will look for a directory with that name
in $PTII/ptolemy/vergil/configs and if the directory contains
the filed named configuration.xml and intro.htm, it will try
to use that configuration.

For example, $PTII/bin/vergil -ptiny will look for
$PTII/ptolemy/vergil/configs/ptiny/configuration.xml
and
$PTII/ptolemy/vergil/configs/ptiny/intro.htm
and if those files are found, then vergil will use them as
the configuration

$PTII/bin/vergil -help looks for the configuration.xml
and intro.htm files and reads in their configurations
and prints out the top level documentation for each configuration.


How to add a configuration
--------------------------
1. Edit $PTII/ptolemy/configs/makefile
   a.  Add your configuration name to the DIRS line in alphabetical order.
       For example, if the configuration I was adding was 'myconfig'
DIRS =		doc dsp full hyvisual jxta myconfig ptiny test
   b.  Add a jar file entry to the value of PTCLASSALLJARS.
       This is required if your configuration is to be included in
       the distribution when we ship it.  For example:
PTCLASSALLJARS = \
		doc/doc.jar \
		dsp/dsp.jar \
		full/full.jar \
		hyvisual/hyvisual.jar \
		jxta/jxta.jar \
		myconfig/myconfig.jar \
		ptiny/ptiny.jar
 
2. Copy an existing configuration
   cp -r $PTII/ptolemy/configs/full $PTII/ptolemy/configs/myconfig
 
3. Edit $PTII/ptolemy/configs/myconfig/makefile
   a.  Change the first line of the makefile to be a one line description
       of your configuration.  For example:
# Makefile for the Ptolemy II my configuration runtime configuration

   b.  Change the value of ME to point to your configuration:
ME =		ptolemy/configs/me
     
   c.  Change the value of PTPACKAGE.  If you do not change PTPACKAGE,
       then the jar file will be created with the wrong name and your
       configuration will not ship with the distribution
PTPACKAGE = 	me

4. Edit $PTII/ptolemy/configs/myconfig/welcomeWindow.xml and change
   the name of the url property to point to your intro.htm

      <property name="url" value="ptolemy/configs/myconfig/intro.htm"/>

5. Customize $PTII/ptolemy/configs/myconfig/intro.htm
   and $PTII/ptolemy/configs/myconfig/configuration.xml
   as you see fit.
   If you change the contents of the <doc> ... </doc> tag in
   configuration.xml, then vergil -help will use that value in its output.

6. Test out your configuration with:
   $PTII/bin/vergil -myconfig

7. If you are a Ptolemy II developer with CVS read/write access, then
   add your configuration to the tree with
   cvs add $PTII/ptolemy/configs/myconfig
   cvs add $PTII/ptolemy/configs/myconfig/{configuration.xml,intro.htm,makefile,welcomeWindow.xml}
   cvs commit -m "Added myconfig"

