# Ptolemy II to build Web Start JNLP files
#
# @Author: Christopher Brooks
# @Version: $Id$
#
# Copyright (c) 2001-2014 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the above
# copyright notice and the following two paragraphs appear in all copies
# of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
#						PT_COPYRIGHT_VERSION_2
#						COPYRIGHTENDKEY
##########################################################################

# Java Network Launch Protocol aka Web Start
#
# This makefile should be included from the bottom of $PTII/makefile
# It is a separate file so as to not clutter up $PTII/makefile

# We usually build installers using jnlp first because it is
# easier to update jar files and test.
# 
# To build with a self signed certificate, use:
#   make jnlp_all

# The usual procedure is to build the jnlp files, access them via
# a browser and then use the Ptolemy "about" facilty (part of the copyright
# links) to expand the configuration and view all the demos.
# In this way, we can be sure that we have all the files in the jar
# files _before_ building installers.

# To test a file, run:    make jnlp_run

# To display our key:
#   make key_list STOREPASSWORD="-storepass xxx" KEYSTORE=/users/ptII/adm/certs/ptkeystore
#   make key_list STOREPASSWORD="-storepass xxx" KEYSTORE=c:/cygwin/users/ptII/adm/certs/ptkeystore

# To sign using our key and update the website:
#   make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx" jnlp_dist

# To update the website:  make jnlp_dist_update

# To build a set of models for a paper, see http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport

################################
# Large jar file containing all the codedoc documentation.
# Comment this out for testing
DOC_CODEDOC_JAR = \
	doc/codeDoc.jar
#DOC_CODEDOC_JAR =

# We put the signed jar files in a separate subdirectory
# for two reasons
# 1) If a jar file is checked in to cvs, and we sign it, then
# cvs update will think that we need to update it.
# So, we copy the jar files to a different directory and then sign
# them.
#
# 2) If we run applets with jar files that have been signed, then the
# user gets a confusing message asking if they want to run the signed
# applets.  Since the Ptolemy II applets do not require signed jar
# files, this is unnecessary

SIGNED_DIR =		signed

# Linux Jars
lib/bcvtbLinux.jar:
	if [ -f lib/libbcvtb.so ]; then \
		(cd lib; \
	 	"$(JAR)" -cvf bcvtbLinux.jar libbcvtb.so); \
	else \
		echo "$$PTII/libbcvtb.so does not exist creating dummy jar"; \
		echo "$$PTII/lib/libbcvtb.so not found, see PTII/mk/jnlp.mk" \
			> lib/README_bcvtb.txt; \
		(cd lib; \
                "$(JAR)" -cvf bcvtbLinux.jar \
			README_bcvtb.txt); \
		rm -f lib/README_bcvtb.txt; \
	fi

# Mac OS X jars
lib/bcvtbMacOSX.jar:
	if [ -f lib/libbcvtb.jnilib ]; then \
		(cd lib; \
	 	"$(JAR)" -cvf bcvtbMacOSX.jar libbcvtb.jnilib); \
	else \
		echo "$$PTII/libbcvtb.jnilib does not exist creating dummy jar"; \
		echo "$$PTII/lib/libbcvtb.jnilib not found, see PTII/mk/jnlp.mk" \
			> lib/README_bcvtb.txt; \
		(cd lib; \
                "$(JAR)" -cvf bcvtbMacOSX.jar \
			README_bcvtb.txt); \
		rm -f lib/README_bcvtb.txt; \
	fi

lib/rxtxMacOSX.jar: 
	if [ -f /Library/Java/Extensions/RXTXcomm.jar ]; then \
		(cd /Library/Java/Extensions; \
	 	"$(JAR)" -cvf $(PTII)/lib/rxtxMacOSX.jar librxtxSerial.jnilib RXTXcomm.jar); \
	else \
		echo "/Library/Java/Extensions/RXTXcomm.jar, creating dummy jar"; \
		echo "/Library/Java/Extension/RXTXcomm.jar, not found, see PTII/mk/jnlp.mk" \
			> README_rxtx.txt; \
		(cd lib; \
		"$(JAR)" -cvf lib/rxtxMacOSX.jar \
			README_rxtx.txt); \
		rm -f README_rxtx.txt; \
	fi

# Jar file that contains win32com.dll for the Java Serial Communications API
lib/rxtxWindows.jar: 
	if [ -f vendors/misc/rxtx/Windows/i368-mingw32/rxtxParallel.dll ]; then \
		(cd vendors/misc/rxtx/Windows/i368-mingw32/
	 	"$(JAR)" -cvf $(PTII)/lib/rxtxWindows.jar rxtxParallel.dll rxtxSerial.dll); \
	else \
		echo "vendors/misc/rxtx/Windows/i368-mingw32/ not found, creating dummy jar"; \
		echo "vendors/misc/rxtx/Windows/i368-mingw32/ not found, see PTII/mk/jnlp.mk" \
			> README_comm.txt; \
		"$(JAR)" -cvf $(PTII)/lib/rxtxWindows.jar \
			README_comm.txt; \
		rm -f README_comm.txt; \
	fi

lib/joystickWindows.jar: 
	if [ -d vendors/misc/joystick/lib/ ]; then \
		(cd vendors/misc/joystick/lib/; \
	 	"$(JAR)" -cvf "$(PTII)/lib/joystickWindows.jar" jjstick.dll); \
	else \
		echo "vendors/misc/joystick not found, creating dummy jar"; \
		echo "vendors/misc/joystick/lib not found" \
			> README_joystick.txt; \
		"$(JAR)" -cvf "$(PTII)/lib/joystickWindows.jar" \
			README_joystick.txt; \
		rm -f README_joystick.txt; \
	fi


# NATIVE_SIGNED_LIB_JARS is a separate vaiable so that we can
# include it in ALL_JNLP_JARS
NATIVE_SIGNED_LIB_JARS = \
	lib/bcvtbMacOSX.jar \
	lib/joystickWindows.jar \
	lib/matlabMacOSX.jar \
	lib/matlabLinux.jar \
	lib/matlabSunOS.jar \
	lib/matlabWindows.jar \
	lib/rxtxMacOSX.jar \
	lib/rxtxWindows.jar 

SIGNED_LIB_JARS =	$(NATIVE_SIGNED_LIB_JARS) \
			lib/diva.jar \
			lib/guava.jar \
			lib/kieler.jar \
			lib/jasminclasses.jar \
			lib/jython.jar \
			lib/ptCal.jar \
			lib/sootclasses.jar

# Web Start can load jars either eagerly or lazily.
# This makefile variable gets passed to $PTII/bin/mkjnlp and determines
# the number of jars that are loaded eagerly.  Of course, for this to
# work, the jars you want to load eagerly need to be at the front of the
# list.  In general, large jars such as diva.jar and ptsupport.jar
# should be loaded eagerly.
#NUMBER_OF_JARS_TO_LOAD_EAGERLY = 11
NUMBER_OF_JARS_TO_LOAD_EAGERLY = 999

# Jar files that will appear in most Ptolemy II JNLP files.
# HyVisual has its own set of core jars

# Order matters here, include the most important jars first
# PTMATLAB_JARS is set by configure in $PTII/mk/ptII.mk
CORE_JNLP_JARS = \
	doc/docConfig.jar \
	lib/diva.jar \
	lib/ptliblicenses.jar \
	lib/guava.jar \
	lib/kieler.jar \
	ptolemy/vergil/basic/layout/layout.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar \
	ptolemy/domains/domains.jar \
	ptolemy/actor/parameters/demo/demo.jar \
	$(PTMATLAB_JARS)


#######
# DSP - The smallest runtime
#
# Jar files that will appear in a DSP only JNLP Ptolemy II Runtime.
#
# doc/design/usingVergil/usingVergil.jar is used in dsp, ptiny and full,
# but not hyvisual.
DSP_ONLY_JNLP_JARS = \
	doc/design/usingVergil/usingVergil.jar 

DSP_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/DSPApplication.jar

DSP_JNLP_JARS =	\
	$(DSP_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR)


#######
# CyPhySim: http://cyphysim.org
#
# Jar files that will appear in a CyPhySim only JNLP Ptolemy II Runtime.
#
# doc/design/usingVergil/usingVergil.jar is used in dsp, ptiny and full,
# but not hyvisual.

CYPHYSIM_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/CyPhySimApplication.jar

PTJAVASCRIPT_JARS = \
	$(PTOAUTHCLIENT_JAR) \
	$(PTOAUTHCOMMON_JAR) \
	$(PTSOCKETIO_JAR) \
	$(PTJAVASCRIPT_JAR) \

CYPHYSIM_JNLP_JARS =	\
	org/json/json.jar \
	org/ptolemy/qss/qss.jar \
	org/ptolemy/machineLearning/machineLearning.jar \
	org/ptolemy/optimization/optimization.jar \
	com/cureos/cureos.jar \
	ptolemy/actor/lib/aspect/aspect.jar \
	ptolemy/actor/lib/colt/colt.jar \
	lib/ptcolt.jar \
	ptolemy/actor/lib/conversions/json/json.jar \
	ptolemy/actor/lib/io/comm/comm.jar \
	vendors/misc/rxtx/RXTXcomm.jar \
	ptolemy/actor/lib/jai/jai.jar \
	ptolemy/actor/lib/jjs/jjs.jar \
	ptolemy/actor/lib/jmf/jmf.jar \
	ptolemy/actor/lib/js/js.jar \
	ptolemy/actor/lib/mail/mail.jar \
	$(PTJAVAMAIL_JARS) \
	$(PTMATLAB_JARS) \
	ptolemy/data/ontologies/ontologies.jar \
	ptolemy/vergil/ontologies/ontologies.jar \
	ptolemy/domains/algebraic/algebraic.jar \
	ptolemy/domains/algebraic/demo/demo.jar \
	ptolemy/domains/gr/gr.jar \
	ptolemy/domains/gr/lib/quicktime/quicktime.jar \
	ptolemy/domains/qss/qss.jar \
	ptolemy/domains/qss/demo/demo.jar \
	ptolemy/domains/sdf/lib/vq/vq.jar \
	ptolemy/vergil/basic/export/html/jsoup/jsoup.jar \
	$(CYPHYSIM_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(EXPORT_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(PDFRENDERER_JARS) \
	org/ptolemy/ptango/ptango.jar \
	$(PTANGO_JAR_FILES) \
	$(PTFMI_JARS) \
	$(PTJAVASCRIPT_JARS) \
	$(PTLBNL_JARS) \
	$(PTMATLAB_JARS) \
	$(PTDATABASE_JNLP_JARS) \
	$(WIRELESS_JARS) \

#######
# Building Controls Virtual Test Bed (https://gaia.lbl.gov/bcvtb)
#
BCVTB_ONLY_JNLP_JARS = \
	doc/codeDocBcvtb.jar

SDF_DEMO_JARS = \
	ptolemy/actor/lib/comm/demo/demo.jar \
	ptolemy/actor/lib/hoc/demo/demo.jar \
	ptolemy/actor/lib/io/demo/demo.jar \
	ptolemy/actor/lib/javasound/demo/demo.jar \
	ptolemy/data/type/demo/demo.jar \
	ptolemy/data/unit/demo/demo.jar \
	ptolemy/moml/demo/demo.jar \
	ptolemy/vergil/kernel/attributes/demo/demo.jar

PTANGO_JAR_FILES = \
	lib/jetty-all-8.1.5-v20120716.jar \
	lib/javax.servlet-api-3.0.1.jar \
	lib/jsoup-1.7.3.jar \
	lib/smack.jar \
	lib/smackx.jar

# Actors that use the jsoup html parser.
JSOUP_JARS = \
	$(PTJSOUP_JAR) \
	ptolemy/vergil/basic/export/html/jsoup/jsoup.jar

PTFMI_JARS = \
	lib/jna.jar \
	lib/jna-4.0.0-variadic.jar \
	org/ptolemy/fmi/fmi.jar \
	ptolemy/actor/lib/fmi/fmi.jar \
	ptolemy/actor/lib/fmi/demo/demo.jar \
	ptolemy/vergil/basic/imprt/fmu/fmu.jar

BCVTB_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/BCVTBApplication.jar

# PTLBNL_JARS is set by configure in ptII/mk/ptII.mk if libexpat was found
# PTMATLAB_JARS is set by configure in $PTII/mk/ptII.mk
BCVTB_JNLP_JARS =	\
	$(BCVTB_MAIN_JAR) \
	$(BCVTB_ONLY_JNLP_JARS) \
	$(JYTHON_JARS) \
	$(PTFMI_JARS) \
	$(PTLBNL_JARS) \
	doc/design/usingVergil/usingVergil.jar \
	doc/docConfig.jar \
	lib/diva.jar \
	lib/kieler.jar \
	ptolemy/vergil/basic/layout/layout.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar \
	ptolemy/domains/algebraic/algebraic.jar \
	ptolemy/domains/algebraic/demo/demo.jar \
	ptolemy/domains/continuous/continuous.jar \
	ptolemy/domains/continuous/demo/demo.jar \
	ptolemy/domains/sdf/sdf.jar \
	ptolemy/domains/sdf/demo/demo.jar \
	ptolemy/domains/modal/modal.jar \
	ptolemy/domains/modal/demo/demo.jar \
	ptolemy/actor/parameters/demo/demo.jar \
	$(PTMATLAB_JARS) \
	$(SDF_DEMO_JARS) \
	$(PTDATABASE_JNLP_JARS)


#######
# HyVisual - HybridSystems
#
# Jar files that will appear in a HyVisual only JNLP Ptolemy II Runtime.
# This list is used to create the ptII/signed directory, so each
# jar file should be named once in one of the *ONLY_JNLP_JARS
#  - rather than including domains.jar, we include only ct.jar, fsm.jar
#  - hybrid/configure.xml includes actor/lib/math.xml which includes
#    sdf.lib.DotProduct
#  - hybrid/configure.xml includes
#    actor/lib/conversions/conversions.xml
#    which includes
#    sdf.lib.BitsToInt
#    sdf.lib.IntToBits
#
# The full version of Vergil should not include any of the jar files below
# because the hsif conversion does not work here
HYBRID_SYSTEMS_ONLY0_JNLP_JARS = \
	doc/design/hyvisual.jar \
	doc/codeDocHyVisual.jar \
	ptolemy/domains/continuous/continuous.jar \
	ptolemy/domains/de/de.jar \
	ptolemy/domains/gr/gr.jar \
	ptolemy/domains/gr/demo/demo.jar \
	ptolemy/domains/modal/modal.jar \
	ptolemy/domains/sdf/lib/lib.jar \
	ptolemy/domains/sdf/kernel/kernel.jar \
	ptolemy/domains/sr/sr.jar


HYBRID_SYSTEMS_ONLY_JNLP_JARS = \
	$(HYBRID_SYSTEMS_ONLY0_JNLP_JARS) \
	lib/saxon8.jar \
	lib/saxon8-dom.jar

HYBRID_SYSTEMS_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/HyVisualApplication.jar

HYBRID_SYSTEMS_DEMO_AND_DOC_JARS = \
	ptolemy/domains/continuous/demo/demo.jar \
	ptolemy/domains/continuous/doc/doc.jar \
	ptolemy/domains/modal/doc/doc.jar \
	ptolemy/domains/modal/demo/demo.jar \
	ptolemy/domains/sdf/demo/demo.jar \
	ptolemy/domains/sdf/doc/doc.jar

# PTMATLAB_JARS is set by configure in $PTII/mk/ptII.mk
HYBRID_SYSTEMS_JNLP_JARS =	\
	$(HYBRID_SYSTEMS_MAIN_JAR) \
	$(HYBRID_SYSTEMS_ONLY_JNLP_JARS) \
	$(HYBRID_SYSTEMS_DEMO_AND_DOC_JARS) \
	doc/docConfig.jar \
	lib/diva.jar \
	lib/kieler.jar \
	ptolemy/vergil/basic/layout/layout.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar \
	ptolemy/domains/gr/lib/quicktime/quicktime.jar \
	$(PTMATLAB_JARS) 

HYVISUAL_JNLP_JARS = $(HYBRID_SYSTEMS_JNLP_JARS)


PTALON_JARS = \
	ptolemy/actor/ptalon/antlr/antlr.jar \
	ptolemy/actor/ptalon/demo/demo.jar \
	ptolemy/actor/ptalon/ptalon.jar

JYTHON_JARS = \
	lib/jython.jar \
        ptolemy/actor/lib/python/python.jar \
        ptolemy/actor/lib/python/demo/demo.jar

#######
# Ptiny
#
# Jar files that will appear in a smaller (Ptiny) JNLP Ptolemy II Runtime.
PTINY_ONLY_JNLP_JARS = \
	lib/ptcolt.jar \
	ptolemy/actor/lib/colt/colt.jar \
	ptolemy/actor/lib/colt/demo/demo.jar \
        ptolemy/actor/lib/security/demo/demo.jar \
	$(SDF_DEMO_JARS) \
	$(PTALON_JARS) \
	$(JYTHON_JARS) \
	$(HYBRID_SYSTEMS_DEMO_AND_DOC_JARS) \
	ptolemy/domains/ddf/demo/demo.jar \
	ptolemy/domains/ddf/doc/doc.jar \
	ptolemy/domains/de/demo/demo.jar \
	ptolemy/domains/de/doc/doc.jar \
	ptolemy/domains/hdf/demo/demo.jar \
	ptolemy/domains/hdf/doc/doc.jar \
	ptolemy/domains/pn/demo/demo.jar \
	ptolemy/domains/pn/doc/doc.jar \
	ptolemy/domains/rendezvous/demo/demo.jar \
	ptolemy/domains/rendezvous/doc/doc.jar \
	ptolemy/domains/sr/demo/demo.jar \
	ptolemy/domains/sr/doc/doc.jar

PTINY_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinyApplication.jar

PTINY_JNLP_JARS = \
	$(PTINY_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)

PTINY_KEPLER_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinyKeplerApplication.jar

PTINY_KEPLER_JNLP_JARS = \
	$(PTINY_KEPLER_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)

PTINY_SANDBOX_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinySandboxApplication.jar

PTINY_SANDBOX_JNLP_JARS = \
	$(PTINY_SANDBOX_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)


#######
# Full
#

COPERNICUS_JARS = \
	lib/jasminclasses.jar \
	lib/sootclasses.jar \
	ptolemy/copernicus/copernicus.jar


BACKTRACK_JARS = 

#BACKTRACK_JARS = \
#	ptolemy/backtrack/backtrack.jar \
#	ptolemy/backtrack/demo/demo.jar


EXEC_JARS = 	ptolemy/actor/gui/exec/exec.jar

EXPORT_JARS = \
	ptolemy/vergil/basic/export/html/html.jar \
	ptolemy/vergil/basic/export/export.jar \
	ptolemy/vergil/basic/export/web/web.jar \


PDFRENDERER_JARS = ptolemy/vergil/pdfrenderer/pdfrenderer.jar \
		lib/PDFRenderer.jar

PTERA_JARS = \
	ptolemy/domains/ptera/ptera.jar \
	ptolemy/domains/ptera/demo/demo.jar \
	ptolemy/domains/ptera/doc/doc.jar \
	ptolemy/vergil/ptera/ptera.jar


PTJACL_JARS =	ptolemy/actor/gui/ptjacl/ptjacl.jar \
		lib/ptjacl.jar

# Do not include PTJACL for size reasons
PTJACL_JARS =

# Jars for configurable run control panel
RUN_JARS = \
	com/jgoodies/jgoodies.jar \
	lib/bsh-2.0b4.jar \
	org/mlc/mlc.jar \
	ptolemy/actor/gui/run/run.jar

WIRELESS_JARS = \
	ptolemy/domains/wireless/wireless.jar \
	ptolemy/domains/wireless/demo/demo.jar \
	ptolemy/domains/wireless/doc/doc.jar

FULL_8_1_JARS = \
	ptolemy/cg/cg.jar \
	ptolemy/data/ontologies/ontologies.jar \
	ptolemy/data/ontologies/demo/demo.jar \
	ptolemy/vergil/ontologies/ontologies.jar \
	ptolemy/domains/sequence/sequence.jar \
	ptolemy/domains/pthales/pthales.jar \
	ptolemy/domains/pthales/demo/demo.jar

METROII_JARS = \
	lib/protobuf-java-2.4.1.jar \
	ptolemy/domains/metroII/metroII.jar \
	ptolemy/domains/metroII/demo/demo.jar

FULL_10_0_JARS = \
	com/cureos/cureos.jar \
	org/ptolemy/faultModels/faultModels.jar \
	org/ptolemy/machineImprovisation/machineImprovisation.jar \
	org/ptolemy/machineImprovisation/demo/demo.jar \
	org/ptolemy/machineLearning/machineLearning.jar \
	org/ptolemy/optimization/optimization.jar \
	org/ptolemy/osc/osc.jar \
	$(PTOSCP5_JAR) \
	org/ptolemy/ssm/ssm.jar \
	$(METROII_JARS) \
	ptolemy/domains/openmodelica/openmodelica.jar \
	ptolemy/domains/scr/scr.jar \
	ptolemy/domains/scr/demo/demo.jar \
	ptolemy/vergil/scr/scr.jar \
	ptolemy/vergil/basic/imprt/accessor/accessor.jar \
	$(PTG4LTL_JAR) \
	ptolemy/vergil/basic/imprt/g4ltl/g4ltl.jar \
	ptolemy/vergil/basic/imprt/g4ltl/demo/demo.jar

FULL_11_0_JARS = \
	org/ptolemy/qss/qss.jar \
	org/ptolemy/qss/demo/demo.jar \
	ptolemy/actor/lib/jjs/modules/localStorage/localStorage.jar \
	ptolemy/actor/lib/jjs/modules/mqtt/mqtt.jar \
	ptolemy/actor/lib/jjs/modules/vertxBus/demo/demo.jar \
	ptolemy/actor/lib/jjs/modules/vertxBus/vertxBus.jar \
	ptolemy/actor/lib/jjs/modules/webSocket/webSocket.jar \
	ptolemy/domains/qss/qss.jar \
	$(PTPAHO_MQTT_JAR) \
	$(PTVERTX_JARS)


# Jar files that will appear in a full JNLP Ptolemy II Runtime
# ptolemy/domains/sdf/lib/vq/data/data.jar contains images for HTVQ demo
FULL_ONLY_JNLP_JARS = \
	$(COPERNICUS_JARS) \
	contrib/contrib.jar \
	doc/books/systems/vergil/vergil.jar \
	doc/design/design.jar \
	doc/img/img.jar \
	doc/papers/papers.jar \
	$(EXPORT_JARS) \
	$(PTANGO_JAR_FILES) \
	org/terraswarm/terraswarm.jar \
	org/terraswarm/demo/demo.jar \
	org/ptolemy/ptango/ptango.jar \
	org/ptolemy/ptango/demo/demo.jar \
	org/ptolemy/qss/qss.jar \
	org/ptolemy/qss/demo/demo.jar \
	$(PTFMI_JARS) \
	$(PTJACL_JARS) \
	ptolemy/actor/gt/gt.jar \
	ptolemy/actor/gt/demo/demo.jar \
	org/json/json.jar \
	ptolemy/actor/lib/aspect/aspect.jar \
	ptolemy/actor/lib/aspect/demo/demo.jar \
	ptolemy/actor/lib/conversions/json/json.jar \
	ptolemy/actor/lib/fmi/demo/demo.jar \
	ptolemy/actor/lib/io/comm/comm.jar \
	ptolemy/actor/lib/io/comm/demo/demo.jar \
	vendors/misc/rxtx/RXTXcomm.jar \
	ptolemy/actor/lib/jai/demo/demo.jar \
	ptolemy/actor/lib/jjs/jjs.jar \
	ptolemy/actor/lib/jjs/demo/demo.jar \
	ptolemy/actor/lib/jmf/jmf.jar \
	ptolemy/actor/lib/jmf/demo/demo.jar \
	ptolemy/actor/lib/joystick/joystick.jar \
	vendors/misc/joystick/Joystick.jar \
	ptolemy/actor/lib/mail/mail.jar \
	ptolemy/actor/lib/mail/demo/demo.jar \
	$(PTJAVAMAIL_JARS) \
	ptolemy/actor/lib/js/js.jar \
	ptolemy/actor/lib/js/demo/demo.jar \
	$(PTJAVASCRIPT_JARS) \
	ptolemy/actor/ptalon/gt/gt.jar \
	ptolemy/actor/ptalon/gt/demo/demo.jar \
	lib/ptCal.jar \
	lib/saxon8.jar \
	lib/saxon8-dom.jar \
	lib/java_cup.jar \
	ptolemy/backtrack/backtrack.jar \
	ptolemy/backtrack/demo/demo.jar \
	ptolemy/caltrop/caltrop.jar \
	ptolemy/caltrop/demo/demo.jar \
	ptolemy/distributed/distributed.jar \
	ptolemy/distributed/domains/sdf/sdf.jar \
	ptolemy/distributed/demo/demo.jar \
	ptolemy/demo/demo.jar \
	ptolemy/domains/experimentalDomains.jar \
	ptolemy/domains/algebraic/demo/demo.jar \
	ptolemy/domains/ca/demo/demo.jar \
	ptolemy/domains/ci/demo/demo.jar \
	ptolemy/domains/ci/doc/doc.jar \
	ptolemy/domains/csp/demo/demo.jar \
	ptolemy/domains/csp/doc/doc.jar \
	ptolemy/domains/curriculum/curriculum.jar \
	ptolemy/domains/dde/demo/demo.jar \
	ptolemy/domains/dde/doc/doc.jar \
	ptolemy/domains/dt/demo/demo.jar \
	ptolemy/domains/dt/doc/doc.jar \
	ptolemy/domains/giotto/demo/demo.jar \
	ptolemy/domains/giotto/doc/doc.jar \
	ptolemy/domains/gr/demo/demo.jar \
	ptolemy/domains/gr/doc/doc.jar \
	ptolemy/domains/gr/lib/quicktime/quicktime.jar \
	ptolemy/domains/petrinet/demo/demo.jar \
	ptolemy/domains/psdf/psdf.jar \
	ptolemy/domains/psdf/demo/demo.jar \
	ptolemy/domains/psdf/demo/BlindCommunication/BlindCommunication.jar \
	ptolemy/domains/psdf/doc/doc.jar \
	lib/mapss.jar \
	ptolemy/domains/ptides/ptides.jar \
	ptolemy/domains/ptides/demo/demo.jar \
	ptolemy/domains/scr/scr.jar \
	ptolemy/domains/scr/demo/demo.jar \
	ptolemy/domains/sdf/lib/vq/vq.jar \
	ptolemy/domains/sdf/lib/vq/data/data.jar \
	ptolemy/domains/sdf/optimize/optimize.jar \
	ptolemy/domains/sdf/optimize/demo/demo.jar \
	ptolemy/domains/taskpt/taskpt.jar \
	ptolemy/domains/taskpt/demo/demo.jar \
	ptolemy/domains/taskpt/doc/doc.jar \
	ptolemy/domains/tdl/tdl.jar \
	ptolemy/domains/tdl/demo/demo.jar \
	ptolemy/domains/tester/tester.jar \
	ptolemy/domains/tm/demo/demo.jar \
	ptolemy/domains/tm/doc/doc.jar \
	ptolemy/domains/tm/tm.jar \
	ptolemy/verification/verification.jar \
	ptolemy/verification/demo/demo.jar \
	$(PTERA_JARS) \
	$(PDFRENDERER_JARS) \
	ptolemy/vergil/basic/export/exportFull.jar \
	$(JSOUP_JARS) \
	ptolemy/vergil/modal/fmv/fmv.jar \
	ptolemy/vergil/gt/gt.jar \
	ptolemy/vergil/scr/scr.jar \
	ptolemy/vergil/tdl/tdl.jar \
	$(FULL_8_1_JARS) \
	$(FULL_10_0_JARS) \
	$(FULL_11_0_JARS) \
	$(PTDATABASE_JNLP_JARS) \
	$(RUN_JARS) \
	$(WIRELESS_JARS)

FULL_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/FullApplication.jar

FULL_JNLP_JARS = \
	$(FULL_MAIN_JAR) \
	$(PTLBNL_JARS) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(FULL_ONLY_JNLP_JARS)

#######
# Space
#
# DOP Center Seating Chart exe
# See http://embedded.eecs.berkeley.edu/dopcenter/roster/index.htm
# To build dopseating.ex, run:
# cd $PTII;
# make install
# make jnlp_all
# # Install launch4j from http://launch4j.sourceforge.net/ 
# # set L4J_DIR above if you install Launch4j anywhere other than 
# # ptII/vendors/launch4j
# make dopseating.exe

# Jar files specific to the space domain
SPACE_ONLY_JNLP_JARS = \
	$(PTDATABASE_JNLP_JARS)

SPACE_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/SpaceApplication.jar

# Jar files used by the DOP Center Seating Chart exe
SPACE_JNLP_JARS = \
	$(SPACE_MAIN_JAR) \
	$(SPACE_ONLY_JNLP_JARS) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR)



#######
# Viptos
#
# Jar files that will appear in a Viptos only JNLP Ptolemy II Runtime.
# ct, fsm, de, sdf

# FIXME: experimentalDomains.jar also includes wireless.jar
# Jar files that are only used in JNLP
VIPTOS_ONLY_JNLP_JARS = \
	doc/codeDocViptos.jar \
	ptolemy/domains/ptinyos/ptinyos.jar \
	ptolemy/domains/ptinyos/demo/demo.jar \
	ptolemy/domains/ptinyos/doc/doc.jar

#	doc/design/viptos.jar 

VIPTOS_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/ViptosApplication.jar

VIPTOS_JNLP_JARS =	\
	$(VIPTOS_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(WIRELESS_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(VIPTOS_ONLY_JNLP_JARS)

#######
# VisualSense
#
# Jar files that will appear in a VisualSense only JNLP Ptolemy II Runtime.
# ct, fsm, de, sdf

# FIXME: experimentalDomains.jar also includes wireless.jar
# Jar files that are only used in JNLP
VISUAL_SENSE_ONLY_JNLP_JARS = \
	doc/design/visualsense.jar \
	doc/codeDocVisualSense.jar

VISUAL_SENSE_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/VisualSenseApplication.jar

# wireless/demo/Intersections/Intersections.xml uses Ptera
VISUAL_SENSE_JNLP_JARS =	\
	$(VISUAL_SENSE_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(WIRELESS_JARS) \
	$(PTERA_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(VISUAL_SENSE_ONLY_JNLP_JARS)


#########

# All the JNLP Jar files except the application jars,
# hopefully without duplicates so that  we don't sign jars twice.
# We include plotapplication.jar so that the ptplot and histogram
# commands will work.
ALL_NON_APPLICATION_JNLP_JARS = \
	$(NATIVE_SIGNED_LIB_JARS) \
	$(BCVTB_ONLY_JNLP_JARS) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(EXPORT_HTML_JARS) \
	$(FULL_ONLY_JNLP_JARS) \
	$(HYBRID_SYSTEMS_ONLY_JNLP_JARS) \
	$(HYBRID_SYSTEMS_DEMO_AND_DOC_JARS) \
	$(SDF_DEMO_JARS) \
	$(PTLBNL_JARS) \
	$(SPACE_ONLY_JNLP_JARS) \
	$(VIPTOS_ONLY_JNLP_JARS) \
	$(VISUAL_SENSE_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS) \
	$(APPLET_DOMAIN_JARS) \
	ptolemy/plot/plotapplication.jar

# The classpath for vergil is more than 8192 chars, so
# creating vergil.exe fails with this message:
# launch4j: Classpath exceeds the maximum length of 8192 characters.
# The workaround is to create a jar file that has a Class-Path:
#  manifest.  See $PTII/bin/mkl4j
ALL_L4J_JARS = \
	vergil_l4j.jar

# All the jar files, include the application jars
ALL_JNLP_JARS = \
	$(ALL_L4J_JARS) \
	$(ALL_NON_APPLICATION_JNLP_JARS) \
	$(CYPHY_MAIN_JAR) \
	$(DSP_MAIN_JAR) \
	$(BCVTB_MAIN_JAR) \
	$(HYBRID_SYSTEMS_MAIN_JAR) \
	$(PTINY_MAIN_JAR) \
	$(PTINY_KEPLER_MAIN_JAR) \
	$(PTINY_SANDBOX_MAIN_JAR) \
	$(FULL_MAIN_JAR) \
	$(SPACE_MAIN_JAR) \
	$(VIPTOS_MAIN_JAR)

# Makefile variables used to set up keys for jar signing.
# To use Web Start, we have to sign the jars.
KEYDNAME = "CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, S=Your State, C=US "
KEYSTORE = ptKeystore
KEYALIAS = ptolemy
# The password should not be stored in a makefile, for production
# purposes, run something like:
#
# make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx" jnlp_all
#
# Note that there is chaos with using full paths like
# "/users/ptII/adm/certs/ptkeystore"
# Cygwin and make think this file is c:/cygwin/users/ptII/adm/certs/ptkeystore
# Java thinks it is c:/users/ptII/adm/certs/ptkeystore
# Thus, you should copy the same file to both locations.
# Then try viewing the keystore:
# make key_list STOREPASSWORD="-storepass xxx" KEYSTORE=/users/ptII/adm/certs/ptkeystore
# make key_list STOREPASSWORD="-storepass xxx" KEYSTORE=c:/cygwin/users/ptII/adm/certs/ptkeystore
#

STOREPASSWORD = -storepass this.is.the.storePassword,change.it
KEYPASSWORD = -keypass this.is.the.keyPassword,change.it

# The keytool binary is found by configure, it will be in $(PTJAVA_DIR)/bin/keytool
# or $(PTJAVA_DIR)/Commands/keytool
#KEYTOOL = $(PTJAVA_DIR)/bin/keytool

# Script to update a *.jnlp file with the proper jar files
MKJNLP =		$(PTII)/bin/mkjnlp

# JNLP files that do the actual installation
JNLPS =	vergilCyPhySim.jnlp \
	vergilBCVTB.jnlp \
	vergilDSP.jnlp \
	vergilHyVisual.jnlp \
	vergilPtiny.jnlp \
	vergilPtinyKepler.jnlp \
	vergilPtinySandbox.jnlp \
	vergilVisualSense.jnlp \
	vergilSpace.jnlp \
	vergil.jnlp 

##############################
# "make jnlp_all" - Main entry point for building with a self-signed certificate
# Create $PTII/ptKeystore, copy jar files to $PTII/signed
# and create the .jnlp files
#
jnlp_all: $(KEYSTORE) $(SIGNED_LIB_JARS) jnlp_sign $(JNLPS) 
	@echo "To run the jnlp file, run \"make jnlp_run\""

#jnlps: $(SIGNED_LIB_JARS) $(JNLPS)
jnlp_clean: 
	rm -rf $(JNLPS) $(SIGNED_DIR)
jnlp_distclean: jnlp_clean
	rm -f  $(ALL_JNLP_JARS) 

# Rule to run the jnlp file
PTJNLP = vergil.jnlp
jnlp_run:
	"$(PTJAVA_HOME)/bin/javaws" -wait $(PTJNLP)


$(SIGNED_DIR):
	if [ ! -d $(SIGNED_DIR) ]; then \
		mkdir -p $(SIGNED_DIR); \
	fi

$(KEYSTORE): 
	if [ ! -f "$(KEYSTORE)" ]; then \
	   "$(KEYTOOL)" -genkey \
		-dname $(KEYDNAME) \
		-keystore "$(KEYSTORE)" \
		-alias "$(KEYALIAS)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD); \
	   "$(KEYTOOL)" -selfcert \
		-keystore "$(KEYSTORE)" \
		-alias "$(KEYALIAS)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD); \
	   "$(KEYTOOL)" -list \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD); \
	fi

# In 2014, signed jar files need a manifest that has a Permissions attribute.
# See http://docs.oracle.com/javase/tutorial/deployment/jar/secman.html
# http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#permissions
# http://docs.oracle.com/javase/tutorial/deployment/jar/modman.html
JNLP_MANIFEST = jnlp_manifest.txt
$(JNLP_MANIFEST):
	echo "Application-Name: Ptolemy II" > $@
	echo "Permissions: all-permissions" >> $@

JNLP_SANDBOX_MANIFEST = jnlp_sandbox_manifest.txt
$(JNLP_SANDBOX_MANIFEST):
	echo "Application-Name: Ptolemy II" > $@
	echo "Permissions: sandbox" >> $@


# Web Start: CyPhySim version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilCyPhySim.jnlp: vergilCyPhySim.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilCyPhySim.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(CYPHYSIM_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(CYPHYSIM_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(CYPHYSIM_MAIN_JAR)`; \
		cp -p $(CYPHYSIM_MAIN_JAR) `dirname $(SIGNED_DIR)/$(CYPHYSIM_MAIN_JAR)`;\
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(CYPHYSIM_MAIN_JAR) \
		`echo $(CYPHYSIM_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(CYPHYSIM_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(CYPHYSIM_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(CYPHYSIM_MAIN_JAR)`; \
	cp -p $(CYPHYSIM_MAIN_JAR) `dirname $(SIGNED_DIR)/$(CYPHYSIM_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(CYPHYSIM_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(CYPHYSIM_MAIN_JAR)" "$(KEYALIAS)"

# Web Start: BCVTB version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilBCVTB.jnlp: vergilBCVTB.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilBCVTB.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(BCVTB_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(BCVTB_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(BCVTB_MAIN_JAR)`; \
		cp -p $(BCVTB_MAIN_JAR) `dirname $(SIGNED_DIR)/$(BCVTB_MAIN_JAR)`;\
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(BCVTB_MAIN_JAR) \
		`echo $(BCVTB_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(BCVTB_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(BCVTB_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(BCVTB_MAIN_JAR)`; \
	cp -p $(BCVTB_MAIN_JAR) `dirname $(SIGNED_DIR)/$(BCVTB_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(BCVTB_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(BCVTB_MAIN_JAR)" "$(KEYALIAS)"

# Web Start: DSP version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilDSP.jnlp: vergilDSP.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilDSP.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(DSP_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(DSP_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(DSP_MAIN_JAR)`; \
		cp -p $(DSP_MAIN_JAR) `dirname $(SIGNED_DIR)/$(DSP_MAIN_JAR)`;\
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(DSP_MAIN_JAR) \
		`echo $(DSP_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(DSP_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(DSP_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(DSP_MAIN_JAR)`; \
	cp -p $(DSP_MAIN_JAR) `dirname $(SIGNED_DIR)/$(DSP_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(DSP_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(DSP_MAIN_JAR)" "$(KEYALIAS)"


# Web Start: HyVisual version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilHyVisual.jnlp: vergilHyVisual.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilHyVisual.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(HYBRID_SYSTEMS_MAIN_JAR)`; \
		cp -p $(HYBRID_SYSTEMS_MAIN_JAR) \
			`dirname $(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(HYBRID_SYSTEMS_MAIN_JAR) \
		`echo $(HYBRID_SYSTEMS_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(HYBRID_SYSTEMS_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(HYBRID_SYSTEMS_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(HYBRID_SYSTEMS_MAIN_JAR)`; \
	cp -p $(HYBRID_SYSTEMS_MAIN_JAR) `dirname $(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR)" "$(KEYALIAS)"

# Web Start: Ptiny version of Vergil - No sources or build env.
vergilPtiny.jnlp: vergilPtiny.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilPtiny.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(PTINY_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(PTINY_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_MAIN_JAR)`; \
		cp -p $(PTINY_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(PTINY_MAIN_JAR) \
		`echo $(PTINY_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(PTINY_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_MAIN_JAR)`; \
	cp -p $(PTINY_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(PTINY_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(PTINY_MAIN_JAR)" "$(KEYALIAS)"

# Web Start: Ptiny version of Vergil for Kepler
vergilPtinyKepler.jnlp: vergilPtinyKepler.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilPtinyKepler.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(PTINY_KEPLER_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(PTINY_KEPLER_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_KEPLER_MAIN_JAR)`; \
		cp -p $(PTINY_KEPLER_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_KEPLER_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(PTINY_KEPLER_MAIN_JAR) \
		`echo $(PTINY_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_KEPLER_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(PTINY_KEPLER_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_KEPLER_MAIN_JAR)`; \
	cp -p $(PTINY_KEPLER_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_KEPLER_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(PTINY_KEPLER_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(PTINY_KEPLER_MAIN_JAR)" "$(KEYALIAS)"


# Web Start: Ptiny version of Vergil - No sources or build env., in a sandbox
vergilPtinySandbox.jnlp: vergilPtinySandbox.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_SANDBOX_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilPtinySandbox.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_SANDBOX_MAIN_JAR)`; \
		cp -p $(PTINY_SANDBOX_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(PTINY_SANDBOX_MAIN_JAR) \
		`echo $(PTINY_SANDBOX_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_SANDBOX_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_SANDBOX_MANIFEST) $(PTINY_SANDBOX_JAR) JNLP-INF/APPLICATION.JNLP
	"$(JAR)" -uf $(PTINY_SANDBOX_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_SANDBOX_MAIN_JAR)`; \
	cp -p $(PTINY_SANDBOX_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR)" "$(KEYALIAS)"


# Web Start: Space version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilSpace.jnlp: vergilSpace.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilSpace.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(SPACE_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(SPACE_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(SPACE_MAIN_JAR)`; \
		cp -p $(SPACE_MAIN_JAR) \
			`dirname $(SIGNED_DIR)/$(SPACE_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(SPACE_MAIN_JAR) \
		`echo $(SPACE_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(SPACE_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(SPACE_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(SPACE_MAIN_JAR)`; \
	cp -p $(SPACE_MAIN_JAR) `dirname $(SIGNED_DIR)/$(SPACE_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(SPACE_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(SPACE_MAIN_JAR)" "$(KEYALIAS)"

# Web Start: VisualSense version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilVisualSense.jnlp: vergilVisualSense.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergilVisualSense.jnlp.in > $@
	if [ ! -f $(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(VISUAL_SENSE_MAIN_JAR)`; \
		cp -p $(VISUAL_SENSE_MAIN_JAR) \
			`dirname $(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(VISUAL_SENSE_MAIN_JAR) \
		`echo $(VISUAL_SENSE_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(VISUAL_SENSE_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(VISUAL_SENSE_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(VISUAL_SENSE_MAIN_JAR)`; \
	cp -p $(VISUAL_SENSE_MAIN_JAR) `dirname $(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR)`
	@echo "# Signing $(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR)" "$(KEYALIAS)"

# Web Start: Full Runtime version of Vergil - No sources or build env.
vergil.jnlp: vergil.jnlp.in $(SIGNED_DIR) $(KEYSTORE) $(JNLP_MANIFEST) $(JNLP_MANIFEST)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			vergil.jnlp.in > $@
	ls -l $@
	if [ ! -f $(SIGNED_DIR)/$(FULL_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)/$(FULL_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(FULL_MAIN_JAR)`; \
		cp -p $(FULL_MAIN_JAR) `dirname $(SIGNED_DIR)/$(FULL_MAIN_JAR)`;\
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	ls -l $@
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(FULL_MAIN_JAR) \
		`echo $(FULL_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`
	ls -l $@
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(FULL_MAIN_JAR) contains the main class"
	"$(JAR)" -umf $(PTII)/$(JNLP_MANIFEST) $(FULL_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(FULL_MAIN_JAR)`; \
	cp -p $(FULL_MAIN_JAR) `dirname $(SIGNED_DIR)/$(FULL_MAIN_JAR)`
	ls -l $@
	@echo "# Signing $(SIGNED_DIR)/$(FULL_MAIN_JAR)"
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(SIGNED_DIR)/$(FULL_MAIN_JAR)" "$(KEYALIAS)"
	ls -l $@

# We first copy the jars, then sign them so as to avoid
# problems with cvs and applets.
jnlp_sign: jnlp_sign1 $(JNLPS) $(KEYSTORE)
jnlp_sign1: $(SIGNED_DIR) $(NATIVE_SIGNED_LIB_JARS) $(JNLP_MANIFEST)
	# Remove $(PTII)/ for files like /var/lib/hudson/jobs/ptII/workspace/vendors/oracle/javamail/mail.jar
	# Replace the CLASSPATHSEPARATOR with a space.
	@set `echo $(ALL_NON_APPLICATION_JNLP_JARS) | sed 's/$(CLASSPATHSEPARATOR)/ /g' | sed 's/C:/c:/g' | sed "s@$(PTII)/@@g"`; \
	for x do \
		if [ ! -f $$x ]; then \
			echo "Warning: $$x does not exist, skipping."; \
			continue; \
		fi; \
		jarfile=`echo $$x | sed "s@$(PTII)/@@"`; \
		if [ ! -f $(SIGNED_DIR)/$$jarfile ]; then \
			echo "#  Copying $$jarfile to $(SIGNED_DIR)/"; \
			mkdir -p $(SIGNED_DIR)/`dirname $$jarfile`; \
			cp -p $$jarfile `dirname $(SIGNED_DIR)/$$jarfile`; \
		fi; \
		echo "# Updating the manifest of $(SIGNED_DIR)/$$jarfile with the Permissions: attribute."; \
		$(JAR) -umf $(PTII)/$(JNLP_MANIFEST) $(SIGNED_DIR)/$$jarfile; \
		echo "# Signing $(SIGNED_DIR)/$$jarfile"; \
		"$(JARSIGNER)" \
			-keystore "$(KEYSTORE)" \
			$(STOREPASSWORD) \
			$(KEYPASSWORD) \
			$(SIGNED_DIR)/$$jarfile $(KEYALIAS); \
	done;

sign_jar: 
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		"$(JARFILE)" "$(KEYALIAS)"

# The jnlp_test rule can be used to build, copy, and sign a jar file.
# For example:
#   make jnlp_test JARSRC=ptolemy/actor/ptalon/ptalon.jar
jnlp_test:
	(cd `dirname $(JARSRC)`; make `basename $(JARSRC)`)
	cp $(JARSRC) signed/$(JARSRC)
	$(MAKE) sign_jar JARFILE=signed/$(JARSRC)
	$(MAKE) jnlp_run

JAR_DIST_DIR = jar_dist

$(JAR_DIST_DIR): $(NATIVE_SIGNED_LIB_JARS)
	if [ ! -d $(JAR_DIST_DIR) ]; then \
		mkdir -p $(JAR_DIST_DIR); \
	fi
	# Remove $(PTII)/ for files like /var/lib/hudson/jobs/ptII/workspace/vendors/oracle/javamail/mail.jar
	# Replace the CLASSPATHSEPARATOR with a space.
	@set `echo $(ALL_JNLP_JARS) | sed 's/$(CLASSPATHSEPARATOR)/ /g' | sed 's/C:/c:/g' | sed "s@$(PTII)/@@g"`; \
	for x do \
		jarfile=`echo $$x | sed "s@$(PTII)/@@"`; \
		if [ ! -f $(JAR_DIST_DIR)/$$jarfile ]; then \
			echo "#  Copying $$jarfile to $(JAR_DIST_DIR)/"; \
			mkdir -p $(JAR_DIST_DIR)/`dirname $$jarfile`; \
			cp -p $$jarfile `dirname $(JAR_DIST_DIR)/$$jarfile`; \
		fi; \
	done;

# Jarfiles used by JNLP WebStart code generation in copernicus/applet.
APPLET_DOMAIN_JARS = \
	ptolemy/domains/algebraic/algebraic.jar \
	ptolemy/domains/ca/ca.jar \
	ptolemy/domains/ci/ci.jar \
	ptolemy/domains/continuous/continuous.jar \
	ptolemy/domains/csp/csp.jar \
	ptolemy/domains/dde/dde.jar \
	ptolemy/domains/ddf/ddf.jar \
	ptolemy/domains/de/de.jar \
	ptolemy/domains/dt/dt.jar \
	ptolemy/domains/giotto/giotto.jar \
	ptolemy/domains/gr/gr.jar \
	ptolemy/domains/hdf/hdf.jar \
	ptolemy/domains/modal/modal.jar \
	ptolemy/domains/pn/pn.jar \
	ptolemy/domains/petrinet/petrinet.jar \
	ptolemy/domains/ptides/ptides.jar \
	ptolemy/domains/rendezvous/rendezvous.jar \
	ptolemy/domains/scr/scr.jar \
	ptolemy/domains/sdf/sdf.jar \
	ptolemy/domains/sr/sr.jar \
	ptolemy/domains/wireless/wireless.jar \
	ptolemy/vergil/vergilApplet.jar 

UNJAR_JARS = \
	ptolemy/actor/gui/jnlp/jnlp.jar \
	$(APPLET_DOMAIN_JARS) \
	$(ALL_NON_APPLICATION_JNLP_JARS)


UNJAR_DIST_DIR = unjar_dist

$(UNJAR_DIST_DIR):
	if [ ! -d $(UNJAR_DIST_DIR) ]; then \
		mkdir -p $(UNJAR_DIST_DIR); \
		mkdir -p $(UNJAR_DIST_DIR)/lib; \
		mkdir -p $(UNJAR_DIST_DIR)/doc; \
	fi
	mkdir -p $(UNJAR_DIST_DIR)/ptolemy/vergil
	cp ptolemy/vergil/vergilApplet.jar $(UNJAR_DIST_DIR)/ptolemy/vergil
	# Replace the CLASSPATHSEPARATOR with a space.
	@set `echo $(UNJAR_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`; \
	for x do \
		echo $$x; \
		case "$$x" in \
			lib/*) \
			   echo "  Copying to lib"; \
			   cp $$x $(UNJAR_DIST_DIR)/lib;; \
			doc/codeDoc*) \
			   echo "  Copying to doc"; \
			   cp $$x $(UNJAR_DIST_DIR)/doc;; \
			ptolemy/actor/gui/jnlp/jnlp.jar) \
			   echo "  Copying jar to ptolemy/actor/gui/jnlp"; \
			   mkdir -p $(UNJAR_DIST_DIR)/ptolemy/actor/gui/jnlp; \
			   cp $$x $(UNJAR_DIST_DIR)/ptolemy/actor/gui/jnlp; \
			  (cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
			ptolemy/ptsupport.jar) \
			   echo "  Copying to ptolemy"; \
			   cp $$x $(UNJAR_DIST_DIR)/ptolemy;; \
			ptolemy/domains/*/*.jar) \
			   echo "Copying to domains specific jars for cg "; \
			   mkdir -p $(UNJAR_DIST_DIR)/`dirname $$x`; \
			   cp $$x `dirname $(UNJAR_DIST_DIR)/$$x`; \
			  (cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
			*)(cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
	        esac; \
	done;
	# Remove jars lie pn/demo/demo.jar, but leave pn/pn.jar
	rm $(UNJAR_DIST_DIR)/ptolemy/domains/*/*/*.jar
	# Fix for quicktime.jar
	rm $(UNJAR_DIST_DIR)/ptolemy/domains/*/*/*/*.jar

# Verify the jar files.  This is useful for debugging if you are
# getting errors about unsigned applications
 
jnlp_verify:
	(cd signed; \
	@set `echo $(ALL_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`; \
	for x do \
		echo "$$x"; \
		"$(JARSIGNER)" -verify -verbose -certs $$x; \
	done;)

# Use this to verify that the key is ok
key_list:
	   "$(KEYTOOL)" -list -v \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD)

# Update a location with the files necessary to download
DIST_BASE = ptolemyII/ptII11.0/jnlp-$(PTVERSION)
DIST_DIR = /home/www/ptweb/$(DIST_BASE)
DIST_URL = http://ptolemy.eecs.berkeley.edu/$(DIST_BASE)
OTHER_FILES_TO_BE_DISTED = doc/img/PtolemyIISmall.gif \
	ptolemy/configs/hyvisual/hyvisualPlanet.gif \

KEYSTORE2=/users/ptII/adm/certs/ptkeystore
KEYALIAS2=ptolemy
# make jnlp_dist STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx"
# make DIST_DIR=c:/cxh/hyv DIST_URL=file:///c:/cxh/hyv jnlp_dist KEYSTORE2=ptKeystore KEYALIAS2=claudius

jnlp_dist: jnlp_dist_clean jnlp_dist_1 jnlp_dist_update
jnlp_dist_clean:
	rm -rf $(JNLPS) $(SIGNED_DIR)
jnlp_dist_1:
	$(MAKE) KEYSTORE="$(KEYSTORE2)" \
		KEYALIAS="$(KEYALIAS2)" \
		PTII_LOCALURL="$(DIST_URL)" jnlp_sign

# Change this if your user name on the webserver is different than your username on the local machine.
WEBSERVER_USER=$(USER)

# We use a non-fully qualified domain name to avoid people accidentally hitting our webserver
WEBSERVER=moog

jnlp_dist_update:
	-ssh $(WEBSERVER_USER)@$(WEBSERVER) chgrp -R cvs $(DIST_DIR)
	-ssh $(WEBSERVER_USER)@$(WEBSERVER) chmod -R g+rwX $(DIST_DIR)
	-tar -cf - $(SIGNED_DIR) $(JNLPS) \
		$(OTHER_FILES_TO_BE_DISTED) | \
		ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); tar -xvmf -"
	-ssh $(WEBSERVER_USER)@$(WEBSERVER) chgrp -R cvs $(DIST_DIR)
	-ssh $(WEBSERVER_USER)@$(WEBSERVER) chmod -R g+rwX $(DIST_DIR)
	-ssh $(WEBSERVER_USER)@$(WEBSERVER) mkdir -p $(DIST_DIR)/doc
	scp doc/webStartHelp.htm $(WEBSERVER_USER)@$(WEBSERVER):$(DIST_DIR)/doc

jnlp_dist_nightly:
	gmake STOREPASSWORD="-storepass `cat $(HOME)/.certpw`" KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYPASSWORD="-keypass `cat $(HOME)/.certpw`" KEYSTORE2=/users/ptII/adm/certs/ptkeystore jnlp_dist

# Used to update gr and codeDoc.jar
DIST_JAR=/home/www/ptweb/ptolemyII/ptII11.0/$(PTVERSION)
update_gr_codeDoc:
	scp ptolemy/domains/gr/gr.jar $(WEBSERVER_USER)@$(WEBSERVER):$(DIST_JAR)/ptolemy/domains/gr
	ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_JAR)/doc; jar -xf ../../jnlp-$(PTVERSION)/signed/doc/codeDoc.jar"

APPLET_FILES_TO_BE_UPDATED = \
	$(APPLET_DOMAIN_JARS) \
	doc/codeDoc.jar \
	doc/webStartHelp.htm \
	lib/diva.jar \
	ptolemy/domains/ptides/ptides.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergilApplet.jar \
	ptolemy/gui/demo/*.class

update_applet_files:
	tar -cf - $(APPLET_FILES_TO_BE_UPDATED) | ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_JAR); tar -xvf -"
	ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_JAR)/doc; jar -xf codeDoc.jar; mv doc/codeDoc .; rmdir doc"

#make KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx" KEYSTORE=ptkeystore PTII_LOCALURL=http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII4.0/jnlp-4.0 jnlp_sign

jnlp_dist_update_remote:
	scp doc/webStartHelp.htm $(WEBSERVER_USER)@$(WEBSERVER):$(DIST_DIR)
	tar -cf - $(SIGNED_DIR) $(JNLPS) \
		$(OTHER_FILES_TO_BE_DISTED) | \
		ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); tar -xpf -"


sign_jar_dist: 
	"$(JARSIGNER)" \
		-keystore "$(KEYSTORE2)" \
		"$(JARFILE)" "$(KEYALIAS2)"

sign_jar_dist_update_remote: sign_jar_dist
	scp $(JARFILE) $(WEBSERVER_USER)@$(WEBSERVER):$(DIST_DIR)/$(JARFILE)

################################################################
################################################################
################################################################

# Launch4j rules
# We use Launch4j http://launch4j.sourceforge.net/ to create
# .exe files that run Vergil etc. and then use
# IzPack (http://www.izforge.com/izpack/) to set up the start menu.
# To build vergil.exe:                                                                         
# 1. Download Launch4j http://launch4j.sourceforge.net/                                        
# 2. Expand Launch4j in $PTII/vendors:                                                         
#      cd $PTII/vendors                                                                        
#      tar -zxf ~/Downloads/launch4j-3.0.1-macosx.tgz                                          
# 3. Create the jar files:                                                                     
#      cd $PTII; make install                                                                  
# 4. Create the .exe file:                                                                     
#      make vergil.exe                                                                         
# vergil.exe and the makefiles listed in vergil_l4j.xml      

# mkl4j is a script that generates an xml file that is then read by Launch4j 
MKL4J = $(ROOT)/bin/mkl4j

# Location of Launch4J, see http://launch4j.sourceforge.net/
#L4J_DIR=c:/Program Files/Launch4j
L4J_DIR=$(PTII)/vendors/launch4j

# Cygpath command
#PTCYGPATH=cygpath --windows -a
PTCYGPATH=$(ROOT)/bin/ptcygpath

# Launch4J console application that reads in .xml files and creates .exe files.
#L4JC=$(L4J_DIR)/launch4jc.exe
L4JC=$(L4J_DIR)/launch4j

# .exe files to be created by Launch4J
L4J_DOC_EXES = 		ptbook.exe
L4J_PTOLEMY_EXES = 	hyvisual.exe ptiny.exe vergil.exe \
				visualsense.exe
L4J_PTPLOT_EXES = 	histogram.exe ptplot.exe

L4J_EXES =		$(L4J_DOC_EXES) $(L4J_PTOLEMY_EXES) $(L4J_PTPLOT_EXES)

# .xml files used to create .exe files.  
# These files are created by $(MKL4J)
L4J_CONFIGS =		$(L4J_EXES:%.exe=%_l4j.xml)

# Create all the .exe files
exes: $(L4J_CONFIGS) $(L4J_EXES)

# Remove the .exe files and the .xml files used to create the .exe files
clean_exes:
	rm -f $(L4J_EXES)
	rm -f $(L4J_CONFIGS)

DOC_JNLP_JARS = \
	ptolemy/ptsupport.jar

bcvtb_l4j.xml: $(MKL4J)
	$(MKL4J) bcvtb ptolemy.vergil.VergilApplication \
		doc/img/ptiny.ico \
		-bcvtb \
		`echo $(BCVTB_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'` > $@

bcvtb.exe: bcvtb_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) bcvtb_l4j.xml`

PTBOOK_PDF=PtolemyII_DigitalV1_02.pdf
ptbook_l4j.xml: doc/books/systems/$(PTBOOK_PDF)
	$(MKL4J) ptbook ptolemy.actor.gui.BrowserLauncher \
		 doc/img/pdf.ico \
		 doc/books/systems/$(PTBOOK_PDF) $(DOC_JNLP_JARS) > $@
	chmod a+x doc/books/systems/$(PTBOOK_PDF)
ptbook.exe: ptbook_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) ptbook_l4j.xml`

doc/books/systems/$(PTBOOK_PDF):
	wget http://ptolemy.eecs.berkeley.edu/books/Systems/$(PTBOOK_PDF)
	mv $(PTBOOK_PDF) $@
	chmod a+x doc/books/systems/$(PTBOOK_PDF)

DOPCenterModel=ptolemy/domains/space/demo/DOPCenter/DOPCenter.xml
dopseating_l4j.xml:
	$(MKL4J) dopseating ptolemy.vergil.VergilApplication \
		doc/img/vergil.ico \
		"-space $(DOPCenterModel)" \
		`echo $(SPACE_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'` > $@

dopseating.exe: dopseating_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) dopseating_l4j.xml`

histogram_l4j.xml:
	$(MKL4J) histogram ptolemy.plot.plotml.HistogramMLApplication \
		doc/img/histogram.ico \
		"" ptolemy/plot/plotapplication.jar > $@
histogram.exe: histogram_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) histogram_l4j.xml`

hyvisual_l4j.xml:
	$(MKL4J) hyvisual ptolemy.vergil.VergilApplication \
		doc/img/hyvisual.ico \
		-hyvisual \
		`echo $(HYBRID_SYSTEMS_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'` > $@

hyvisual.exe: hyvisual_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) $^`

hyvisualdoc_l4j.xml:
	$(MKL4J) hyvisualdoc ptolemy.actor.gui.BrowserLauncher \
		doc/img/pdf.ico \
		doc/design/hyvisual.pdf $(DOC_JNLP_JARS) > $@
	chmod a+x doc/design/hyvisual.pdf
hyvisualdoc.exe: hyvisualdoc_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) hyvisualdoc_l4j.xml`

ptiny_l4j.xml: $(MKL4J)
	$(MKL4J) ptiny ptolemy.vergil.VergilApplication \
		doc/img/ptiny.ico \
		-ptiny \
		`echo $(PTINY_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'` > $@

ptiny.exe: ptiny_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) ptiny_l4j.xml`

ptplot_l4j.xml:
	$(MKL4J) ptplot ptolemy.plot.plotml.EditablePlotMLApplication \
		doc/img/ptplot.ico \
		"" ptolemy/plot/plotapplication.jar > $@
ptplot.exe: ptplot_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) ptplot_l4j.xml`


vergil_l4j.xml:
	# Remove $(PTII)/ for files like /var/lib/hudson/jobs/ptII/workspace/adm/dists/ptII11.0devel/vendors/oracle/javamail/javax.mail.jar and /home/hudson/jobs/ptII/workspace/adm/dists/ptII11.0.devel/lib/jsoup-1.7.3.jar
	$(MKL4J) vergil ptolemy.vergil.VergilApplication \
		doc/img/vergil.ico \
		"" \
		`echo $(FULL_JNLP_JARS) | sed 's/$(CLASSPATHSEPARATOR)/ /g' | sed 's/C:/c:/g' | sed "s@$(PTII)/@@g" ` > $@

vergil.exe: vergil_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) vergil_l4j.xml`

vergil.jar:
	mkdir $(PTJAR_TMPDIR)
	for jar in `echo $(FULL_JNLP_JARS) | sed 's@$(PTII)/@@g' | sed 's/$(CLASSPATHSEPARATOR)/ /g'` do \
		echo "Unjarring $$jar"; \
		(cd $(PTJAR_TMPDIR); "$(JAR)" $(JAR_FLAGS) -xf ../$$jar); \
	done
	rm -rf $(PTJAR_TMPDIR)/META-INF
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@

viptos_l4j.xml:
	$(MKL4J) viptos ptolemy.vergil.VergilApplication \
		doc/img/viptos.ico \
		-viptos \
		`echo $(VIPTOS_JNLP_JARS) | sed 's@$(PTII)/@@g' | sed 's/$(CLASSPATHSEPARATOR)/ /g'` > $@

viptos.exe: viptos_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) viptos_l4j.xml`

visualsense_l4j.xml:
	$(MKL4J) visualsense ptolemy.vergil.VergilApplication \
		doc/img/visualsense.ico \
		-visualsense \
		`echo $(VISUAL_SENSE_JNLP_JARS) | sed 's@$(PTII)/@@g' | sed 's/$(CLASSPATHSEPARATOR)/ /g'` > $@

visualsense.exe: visualsense_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) visualsense_l4j.xml`

visualsensedoc_l4j.xml:
	$(MKL4J) visualsensedoc ptolemy.actor.gui.BrowserLauncher \
		doc/img/pdf.ico $(DOC_JNLP_JARS) > $@
	chmod a+x doc/design/visualsense.pdf
visualsensedoc.exe: visualsensedoc_l4j.xml
	"$(L4JC)" `$(PTCYGPATH) visualsensedoc_l4j.xml`

################################################################
################################################################
################################################################
# We use IzPack (http://www.izforge.com/izpack/) to set up the start menu.

# Used to build installers in adm/gen-X.Y

# Echo the jar files in a format suitable for izpack. 
# Certain jar files from the doc/ directory are not echoed.
# For example:  make echo_jars JARS=PTINY_JNLP_JARS
echo_jars:
	@echo $($(JARS)) | grep -v "(doc/codeDoc|doc/design/hyvisual.jar|doc/design/design.jar|doc/design/visualsense.jar)" |  awk '{for(i=1;i<=NF;i++){ print "            <file src=\"../../jar_dist/" $$i "\""; ns = split($$i, f, "/"); dir = ""; for(s=1;s<ns;s++) {dir = dir "/" f[s]}  print "                  targetdir=\"$$INSTALL_PATH" dir "\"/>"  } }'


# The echo_plist_jars rule is used by $PTII/bin/makeapp to create Contents/Info.plist.
# sed is used to make the paths relative for things like lib/js.jar
echo_plist_jars:
	@echo $($(JARS)) | grep -v "(doc/codeDoc|doc/design/hyvisual.jar|doc/design/design.jar|doc/design/visualsense.jar)" | sed 's@$(PTII)/@@g'

# make echo_classpath_jars JARS=PTINY_JNLP_JARS
echo_classpath_jars:
	@echo $($(JARS)) | grep -v "(doc/codeDoc|doc/design/hyvisual.jar|doc/design/design.jar|doc/design/visualsense.jar)" | awk '{for(i=1;i<=NF;i++) {printf("${CLASSPATHSEPARATOR}"); if ($$i !~ /^\//) {printf("${PTII}/")} printf("%s", $$i)} printf("${CLASSPATHSEPARATOR}\n")}'

# How to run from the jars:
# make vergil_run_full
# We run in the /tmp directory to avoid looking in $PTII
vergil_run:
	(cd /tmp; $(JAVA) -Xmx4000m $(JAVAFLAGS) -classpath `(cd ${PTII}; make echo_classpath_jars JARS=${CONFIGURATION_JARS})` ptolemy.vergil.VergilApplication $(CONFIGURATION))
vergil_run_bcvtb:
	$(MAKE) vergil_run CONFIGURATION_JARS=BCVTB_JNLP_JARS CONFIGURATION=-bcvtb
vergil_run_full:
	$(MAKE) vergil_run CONFIGURATION_JARS=FULL_JNLP_JARS
vergil_run_hyvisual:
	$(MAKE) vergil_run CONFIGURATION_JARS=HYVISUAL_JNLP_JARS CONFIGURATION=-hyvisual
vergil_run_ptiny:
	$(MAKE) vergil_run CONFIGURATION_JARS=PTINY_JNLP_JARS CONFIGURATION=-ptiny 
vergil_run_visualsense:
	$(MAKE) vergil_run CONFIGURATION_JARS=VISUAL_SENSE_JNLP_JARS CONFIGURATION=-visualsense


vergil_run_signed:
	(cd /tmp; $(JAVA) $(JAVAFLAGS) -classpath `(cd $(PTII); make echo_classpath_jars JARS=PTINY_JNLP_JARS)` ptolemy.vergil.VergilApplication $(CONFIGURATION))


################################################################
################################################################
################################################################
# Bootstrapping OSGi bundles
MKOSGI=$(ROOT)/bin/mkosgi
OSGI_TARGET_DIRECTORY=~/tmp/triq

KORE_JARS = \
	ptolemy/data/data.jar \
	ptolemy/graph/graph.jar \
	ptolemy/kernel/kernel.jar \
	ptolemy/math/math.jar \
	ptolemy/util/util.jar \


ACTOR_KORE_JARS = \
	ptolemy/actor/actor.jar

ACTOR_LIB_KORE_JARS = \
	ptolemy/actor/lib/libKore.jar

ACTOR_LIB_GUI_KORE_JARS = \
	ptolemy/actor/lib/gui/gui.jar

MOML_JARS = \
	ptolemy/moml/moml.jar

# Microstar is used by moml and ptplot, but ptplot does not use moml
MICROSTAR_JARS = \
	com/microstar/xml/xml.jar

# Simple MoML App with no gui
OSGI_PTOLEMY_JARS = \
	$(KORE_JARS) \
	$(ACTOR_KORE_JARS) \
	$(ACTOR_LIB_KORE_JARS) \
	$(MOML_JARS) \
	$(MICROSTAR_JARS)

OSGI_SR_TEST_JARS = \
	$(OSGI_PTOLEMY_JARS) \
	ptolemy/domains/sr/sr.jar 

osgi_sr_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.sr.example $(OSGI_SR_TEST_JARS)

OSGI_CT_TEST_JARS = \
	$(OSGI_PTOLEMY_JARS)

osgi_ct_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.ct.example $(OSGI_CT_TEST_JARS)

OSGI_ACTOR_GUI_JARS = \
	ptolemy/actor/gui/gui.jar

OSGI_ACTOR_LIB_GUI_JARS = \
	ptolemy/actor/lib/gui/gui.jar \
	ptolemy/actor/lib/image/image.jar \
	ptolemy/actor/lib/javasound/javasound.jar \
	ptolemy/domains/sdf/lib/vq/vq.jar \
	ptolemy/media/media.jar

OSGI_GUI_JARS = \
	ptolemy/gui/gui.jar

OSGI_PLOT_JARS = \
	ptolemy/plot/plot.jar

OSGI_PTINY_DOMAINS_JARS =  \
	ptolemy/domains/domains.jar \
	ptolemy/domains/demo.jar

OSGI_VERGIL_JARS = \
	diva/diva.jar \
	ptolemy/vergil/vergil.jar

OSGI_PTINY_JARS = \
	$(OSGI_ACTOR_GUI_JARS) \
	ptolemy/configs/configs.jar \
	ptolemy/doc/docConfig.jar \
	$(OSGI_ACTOR_LIB_GUI_JARS) \
	$(OSGI_GUI_JARS) \
	$(OSGI_PLOT_JARS) \
	$(OSGI_PTOLEMY_JARS) \
	$(OSGI_PTINY_DOMAINS_JARS) \
	$(OSGI_VERGIL_JARS) \
	$(PTINY_ONLY_JNLP_JARS)

osgi_ptiny_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.ptiny $(OSGI_PTINY_JARS)

osgi_gui_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.guiKore \
	diva/diva.jar \
	ptolemy/actor/actor.jar \
	ptolemy/actor/gui.jar \
	ptolemy/data/data.jar \
	ptolemy/gui/gui.jar \
	ptolemy/kernel/kernel.jar \
	ptolemy/plot/plot.jar

osgi_image_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.actorImageKore \
	ptolemy/actor/gui/gui.jar \
	ptolemy/actor/lib/lib.jar \
	ptolemy/data/data.jar \
	ptolemy/gui/gui.jar \
	ptolemy/kernel/kernel.jar \
	ptolemy/media/media.jar \
	ptolemy/moml/moml.jar \
	ptolemy/actor/lib/image/image.jar

osgi_colt_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.actorLibColtKore \
	com/microstar/xml/xml.jar \
	ptolemy/actor/actor.jar \
	ptolemy/actor/lib/colt/colt.jar \
	ptolemy/actor/lib/libKore.jar \
	ptolemy/data/data.jar \
	ptolemy/graph/graph.jar \
	ptolemy/kernel/kernel.jar \
	ptolemy/math/math.jar \
	ptolemy/moml/moml.jar \
	ptolemy/util/util.jar

osgi_codegen_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.codgenKore \
	com/microstar/xml/xml.jar \
	ptolemy/actor/actor.jar \
	ptolemy/actor/gui/gui.jar \
	ptolemy/actor/lib/colt/colt.jar \
	ptolemy/actor/lib/gui/gui.jar \
	ptolemy/actor/lib/libKore.jar \
	ptolemy/actor/lib/javasound/javasound.jar \
	ptolemy/data/data.jar \
	ptolemy/domains/hdf/hdf.jar \
	ptolemy/domains/pn/pn.jar \
	ptolemy/domains/sdf/sdf.jar \
	ptolemy/graph/graph.jar \
	ptolemy/gui/gui.jar \
	ptolemy/kernel/kernel.jar \
	ptolemy/math/math.jar \
	ptolemy/media/media.jar \
	ptolemy/moml/moml.jar \
	ptolemy/plot/plot.jar \
	ptolemy/util/util.jar

osgi_demo_test:
	rm -rf $(OSGI_TARGET_DIRECTORY)/*
	$(MKOSGI) $(PTII) $(OSGI_TARGET_DIRECTORY) ptolemy.domains.demo \
	com/microstar/xml/xml.jar \
	ptolemy/actor/actor.jar \
	ptolemy/actor/lib/colt/colt.jar \
	ptolemy/actor/lib/libKore.jar \
	ptolemy/data/data.jar \
	ptolemy/graph/graph.jar \
	ptolemy/kernel/kernel.jar \
	ptolemy/math/math.jar \
	ptolemy/moml/moml.jar \
	ptolemy/util/util.jar \
	ptolemy/domains/demo/demo.jar \
	ptolemy/domains/domains.jar

################################################################################
# Rules used to to create jnlp files for ptolemy/demo/11/modularSemantics
#
################################################################################
# IMPORTANT:  See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport
#             Below is probably out of date
################################################################################
#
# To build the complete set of signed jars:
# * you will need access to our key, which is in /users/ptII/adm/certs/ptkeystore on $(WEBSERVER)
# * you need access to the password, which /users/ptII/adm/certs/.pw on $(WEBSERVER) 
#
# 1. To build all the jars and copy them to the webserver:
#   First: create the directory on moog.  You must have an ssh account on moog
#   and be in the ptolemy group:
#     ssh moog "mkdir ~www/ptweb/ptolemyII/ptII11.0/jnlp-modularSemantics"
#   Then, run these commands:
#     cd $PTII
#     ant build javadoc
#     ant -f jars.xml jars
#     rm -rf signed
#   
#   make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxxxxx" KEYPASSWORD="-keypass xxxxxx" DIST_BASE=ptolemyII/ptII11.0/jnlp-modularSemantics jnlp_dist

# This will create /home/www/ptweb/ptolemyII/ptII11.0/jnlp-modularSemantics
#
# 2. Set up ptII/ptKeystore.properties to contain the path to the keystore,
# the passwords and the alias.  This file is used by copernicus to create signed jars.
# 
# 3. If you are using Server Side Includes (SSI) on the server, copy the *-ssi.htm.in files:
#   cd $PTII/ptolemy/copernicus/applet
#   cp modelJnlp-ssi.htm.in modelJnlp.htm.in 
#   cp modelVergil-ssi.htm.in modelVergil.htm.in 
#   cd $PTII
#
# 4. Clean up any previous work for a model:
#   make book_real_clean JNLP_MODEL_DIRECTORY=ptolemy/demo/FuelSystem JNLP_MODEL=FuelSystem
#
# 5. To create a JNLP file for one model and upload it:
#   make JNLP_MODEL=FuelSystem JNLP_MODEL_DIRECTORY=ptolemy/demo/FuelSystem KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxxxxx" KEYPASSWORD="-keypass xxxxx" DIST_BASE=ptolemyII/ptII11.0/jnlp-modularSemantics book_dist_update
#
# 6. To create JNLP files for all the models listed in the $(EXAMPLE_MODELS) makefile variable:
#   cd $PTII/doc/books/11/modularSemantics
#   make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxxxxx" KEYPASSWORD="-keypass xxxxx" DIST_BASE=ptolemyII/ptII11.0/jnlp-modularSemantics jnlps


# The name of the model, without the .xml extension.
JNLP_MODEL =		CapriciousThermostat

# The path, relative from ptII that contains the model.
JNLP_MODEL_DIRECTORY =  doc/books/system

################################################################### 
# For jnlp files, you should not need to modify anything below here
################################################################### 

# The .xml file that contains the model, relative to $PTII.
JNLP_MODEL_FILE =	$(JNLP_MODEL_DIRECTORY)/$(JNLP_MODEL).xml

# The directory that is created by exporting HTML of the model
JNLP_HTML_EXPORT =		$(JNLP_MODEL_DIRECTORY)/$(JNLP_MODEL)

# The .jnlp file that is produced by $PTII/bin/copernicus.
JNLP_FILE =		$(JNLP_MODEL_DIRECTORY)/$(JNLP_MODEL).jnlp

# The main .htm file that is produced by $PTII/bin/copernicus that has links to the other files.
JNLP_HTML =		$(JNLP_MODEL_DIRECTORY)/$(JNLP_MODEL).htm

# The main .htm file that is produced by $PTII/bin/copernicus that has the applet.
JNLP_VERGIL_HTM =		$(JNLP_MODEL_DIRECTORY)/$(JNLP_MODEL)Vergil.htm

# A fixed version of the .jnlp file that has the URL updated.
JNLP_FILE_FIXED	=	$(JNLP_FILE).fixed

# The signed jar file to be created that contains a copy of the .jnlp file
JNLP_JAR =		$(JNLP_MODEL_DIRECTORY)/signed_$(JNLP_MODEL).jar

# Create the .jnlp file by running copernicus on the .xml file that contains the model.
$(JNLP_FILE): $(JNLP_MODEL_FILE)
	@echo "#"
	@echo "# mk/jnlp.mk: creating $(JNLP_FILE) by invoking copernicus"
	@echo "#"
	# Create any jar files by running make install.
	(cd $(JNLP_MODEL_DIRECTORY); $(MAKE) install);
	(cd $(JNLP_MODEL_DIRECTORY); JAVAFLAGS=-Dptolemy.ptII.ptKeystore=$(HOME)/ptKeystore.properties $(PTII)/bin/copernicus -codeGenerator applet -run false -targetPath $(JNLP_MODEL_DIRECTORY) $(JNLP_MODEL).xml)

# Shortcut to create the jnlp file.  Try "make book"
book: $(JNLP_FILE_FIXED)
book_clean:
	rm -f $(JNLP_FILE_FIXED) 
book_real_clean:
	rm -f $(JNLP_FILE_FIXED) $(JNLP_FILE) $(JNLP_HTML) $(JNLP_VERGIL_HTM) $(JNLP_JAR)
	rm -rf $(JNLP_HTML_EXPORT)

# Create the .jnlp file, but don't fix it yet:
# make -n JNLP_MODEL_DIRECTORY=doc/papers/y12/designContracts JNLP_MODEL=DCMotorTol KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass `cat $HOME/.certpw`" KEYPASSWORD="-storepass `cat $HOME/.certpw`" DIST_BASE=ptolemyII/ptII8.1/jnlp-modularSemantics jnlp_file
jnlp_file: $(JNLP_FILE)

# Fix the jnlp file by substituting in the proper URL
jnlp_file_fixed: $(JNLP_FILE_FIXED)
$(JNLP_FILE_FIXED): $(JNLP_FILE) $(JNLP_MANIFEST)
	#
	# mk/jnlp.mk: Fix the JNLP File
	#
	sed -e "s@<jnlp codebase=\".*\"@<jnlp codebase=\"$(DIST_URL)\"@" \
	    -e "s@\(^ *href=\"\).*\(/[^/]*\)@\1$(DIST_URL)/$(JNLP_MODEL_DIRECTORY)\2@" \
	    $(JNLP_FILE) > $(JNLP_FILE_FIXED)
	#-diff $(JNLP_FILE) $(JNLP_FILE_FIXED)
	# Updating the jar file with the fixed JNLP file
	rm -rf tmpjar
	mkdir tmpjar
	(cd tmpjar; \
		"$(JAR)" -xf ../$(JNLP_JAR); \
		rm -rf JNLP-INF META-INF; \
		mkdir JNLP-INF; \
		cp ../$@ JNLP-INF/APPLICATION.JNLP; \
		"$(JAR)" -cmf $(PTII)/$(JNLP_MANIFEST) ../$(JNLP_JAR) .)
	rm -rf tmpjar
	# Signing the jar file
	@"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(JNLP_JAR) "$(KEYALIAS)"
	#"$(JARSIGNER)" -verify -verbose -certs $(JNLP_JAR)

######################################################
#
# Export an HTML version of the model
# See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport
#
# The default is to run the model before exporting so that we capture the plot. 
# However, the PrintingPress model has no plots.
EXPORT_HTML_RUN=-run

# The directory that contains the html version of the model,
# generated by ptolemy.vergil.basic.export.ExportModel -run htm
HTML_MODEL =           $(JNLP_MODEL_DIRECTORY)/$(JNLP_MODEL)
html_model: $(HTML_MODEL)
$(HTML_MODEL):
	#
	# mk/jnlp.mk: HTML_MODEL rule. Exporting $(JNLP_MODEL).xml to html.
	#
	(cd $(JNLP_MODEL_DIRECTORY); $(PTII)/bin/ptinvoke -Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true ptolemy.vergil.basic.export.ExportModel $(EXPORT_HTML_RUN) -whiteBackground -openComposites htm $(JNLP_MODEL).xml; find $(JNLP_MODEL) -name "*.htm*" -exec chmod a+x {} \;)

# Update the website, create links.
update_html_model: html_model
	(cd $(JNLP_MODEL_DIRECTORY); tar -cf - $(JNLP_MODEL)) | ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd /home/www/ptweb/ptolemyII/ptII11.0/jnlp-ptides/$(JNLP_MODEL_DIRECTORY); rm -rf $(JNLP_MODEL); tar -xf -; cd $(JNLP_MODEL); ln -s $(JNLP_MODEL).htm index.htm; ln -s $(JNLP_MODEL).htm index.html"

# We have two sets (!) of jar files: unsigned for applets and signed for JNLP Web Start
#
# Set #1: Update applet jar files
book_dist_applet_update: $(JNLP_FILE_FIXED)
	APPLET_JARS=`grep jar $(JNLP_FILE_FIXED) | awk -F \" '{print $$2}' | grep -v signed_ | sed  's@signed/@@'`; \
	tar -cf - $$APPLET_JARS | \
		ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); tar -xvpf -"

# Set #2: Update jnlp jar files.  Usually don't need to run this as make ... jnlp_dist will do it.
book_dist_jnlp_update: $(JNLP_FILE_FIXED)
	JNLP_JARS=`grep jar $(JNLP_FILE_FIXED) | awk -F \" '{print $$2}' | grep -v signed_`; \
	tar -cf - $$JNLP_JARS | \
		ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); tar -xvpf -"

# Update the website.

# Files to be updated, including the .htm files
# index.html and toc.htm are created by rules in $PTII/mk/ptcommon.mk
INDEX_CHAPTER =           $(JNLP_MODEL_DIRECTORY)/index.html
TOC_CHAPTER =           $(JNLP_MODEL_DIRECTORY)/toc.htm
JNLP_FILES_TO_BE_UPDATED =  $(JNLP_MODEL_FILE) $(JNLP_JAR) $(JNLP_FILE_FIXED) $(JNLP_HTML) $(JNLP_VERGIL_HTM) doc/deployJava.js $(HTML_MODEL) $(INDEX_CHAPTER) $(TOC_CHAPTER)

# Files to be updated, not including the .htm files
#JNLP_FILES_TO_BE_UPDATED =  $(JNLP_MODEL_FILE) $(JNLP_JAR) $(JNLP_FILE_FIXED) doc/deployJava.js doc/deployJava.txt

# The jnlps_index rule is in $PTII/mk/ptcommon.mk
book_dist_update: $(JNLP_FILE_FIXED) $(HTML_MODEL) jnlps_index
	@echo "#"
	@echo "# mk/jnlp.mk:  book_dist_update target."
	@echo "#"
	pwd
	tar -cf - $(JNLP_FILES_TO_BE_UPDATED) | ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); tar -xpf -"
	ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); mv $(JNLP_FILE_FIXED) $(JNLP_FILE)"
	# Copy over the model and submodel files
	(cd $(JNLP_MODEL_DIRECTORY); tar -cf - `make --no-print-directory --silent echo_models` | ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR)/$(JNLP_MODEL_DIRECTORY); tar -xpf -")
	# Replace link to applet with link to image.  Some files have a .htm file that link to the applet
	ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); sed -e 's@$(JNLP_MODEL)Vergil.htm@$(JNLP_MODEL)/index.html@' -e 's@>applet</a>@>HTML Version</a> - browsable only, not executable@' $(JNLP_HTML) > $(JNLP_HTML).tmp"
	#-ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); diff $(JNLP_HTML).tmp $(JNLP_HTML)"
	ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd $(DIST_DIR); mv $(JNLP_HTML).tmp $(JNLP_HTML)"
	# Make the html file executable so that Server Side Includes (SSIs) work.
	ssh $(WEBSERVER_USER)@$(WEBSERVER) "chmod a+x $(DIST_DIR)/$(JNLP_HTML) $(DIST_DIR)/$(JNLP_VERGIL_HTM) $(DIST_DIR)/$(HTML_MODEL)/index.html $(DIST_DIR)/$(INDEX_CHAPTER) $(DIST_DIR)/$(TOC_CHAPTER) $(DIST_DIR)/$(INDEX_CHAPTER) $(DIST_DIR)/$(TOC_CHAPTER)"
	#ssh $(WEBSERVER_USER)@$(WEBSERVER) "chgrp -R ptolemy $(DIST_DIR)"
	#
	# Update the Web Start help.  Not the most portable command :-(
	svn update doc/webStartHelp.htm
	scp doc/webStartHelp.htm $(WEBSERVER_USER)@$(WEBSERVER):~www/ptweb/ptolemyII/ptIIlatest/ptII/doc/


# Update the 11.0 tree.
# To use: cd $PTII; make JNLP_MODEL_DIRECTORY=ptolemy/domains/sdf/demo/MaximumEntropySpectrum JNLP_MODEL=MaximumEntropySpectrum 11_0_update_model
11_0_update_model:
	$(MAKE) KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass `cat ${HOME}/.certpw`" KEYPASSWORD="-storepass `cat ${HOME}/.certpw`" DIST_BASE=ptolemyII/ptII11.0/jnlp-11.0.devel book_dist_update

11_0_update_all_models: ptolemy/configs/doc/models.txt
	sed 's@\$$CLASSPATH/@@' ptolemy/configs/doc/models.txt | \
	egrep -v '(/cg/|/codegen/|/ExecDemos/|lbnl/demo/|/matlab/|SMVLegacyCodeActor|/SystemLevelType/|/taskpt/|/verification/)' | \
	awk -F / '{dir=$$1; for(i = 2; i < (NF); i++) {dir = dir "/" $$i}; print "make JNLP_MODEL_DIRECTORY=" dir " JNLP_MODEL=" substr($$NF, 0, length($$NF)-4) " 9_0_update_model"}'


####################################################
# Generate ant code to build ALL_JNLP_JARS except for files in lib/

ant.header:
	cat adm/ant/jnlpHeader.xml

# Echo the jar files in a format suitable for ant
# Certain jar files from the doc/ directory are not echoed.
# For example:  make ant.jnlpjar JARS=PTINY_JNLP_JARS
ant.jnlpjar:
	@echo $($(JARS)) | $(PTII)/adm/ant/jnlpjars $(JARS)


ant.jars.full:
	make ant.jnlpjar JARS=FULL_JNLP_JARS

ant.jars.ptiny:
	make ant.jnlpjar JARS=PTINY_JNLP_JARS

ant.jars.space:
	make ant.jnlpjar JARS=SPACE_JNLP_JARS

# make --quiet ant.java.full  >& full.xml; ant -v -f full.xml full
ant.java.full: ant.header ant.jars.full ant.footer 

# make --quiet ant.java.ptiny  >& ptiny.xml; ant -v -f ptiny.xml ptiny
ant.java.ptiny: ant.header ant.jars.ptiny ant.footer 

# make --quiet ant.java.space >& space.xml; ant -v -f space.xml space
ant.java.space: ant.header ant.jars.space ant.footer 


ant.jars: ant.header ant.jars.body ant.footer
ant.jars.body:
	@echo "  <target name=\"jars\">"
	@set `echo $(ALL_JNLP_JARS) | sed "s@$(PTII)/@@g" | sed 's/$(CLASSPATHSEPARATOR)/ /g'`; \
	for x do \
		if [ -z `echo $$x | grep '^lib/'` ]; then \
			echo ""; \
			echo "<!-- $$x -->"; \
			$(PTII)/adm/ant/jar2ant $$x; \
		fi; \
        done
	@echo "  </target>"

ant.footer:
	@echo "</project>"


# Someone with very little understanding of Java removed the certificates from the
# Java 3D jar files on download.java.net
# Web Start files that use Java3D will now fail with:
#
# exception: Found unsigned entry in resource: http://download.java.net/media/java3d/webstart/release/j3d/1.5.2/j3dcore.jar.
# com.sun.deploy.net.JARSigningException: Found unsigned entry in resource: http://download.java.net/media/java3d/webstart/release/j3d/1.5.2/j3dcore.jar
#
# See http://www.java.net/forum/topic/javadesktop/java-desktop-technologies/java-3d/unsigned-entry-j3dcorejar#comment-818570
#
# A solution is documented at the above link.
# make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx" sign_j3d
#
# Unfortunately, under Mac OS X 10.7, the Java 3D 1.5.2 jar files fail with:
#   Caused by: java.lang.ClassNotFoundException: apple.awt.CGraphicsDevice
# So, we fall back to 1.5.0


JNLP_J3D_MANIFEST = jnlp_j3d_manifest.txt
$(JNLP_J3D_MANIFEST):
	echo "Application-Name: Java3D, via the Ptolemy website" > $@
	echo "Permissions: all-permissions" >> $@

# Use $(ROOT) here instead of $(PTII) or $(HOME)
# and avoid target pattern contains no `%'.
WEBSTART=$(ROOT)/webstart
JAVA3D=$(WEBSTART)/java3d
sign_j3d: $(JAVA3D) $(JNLP_J3D_MANIFEST)
	@set `find $(JAVA3D) -name "*.jar"`; \
	for x do \
		echo $$x; \
		echo "# Updating the manifest of $$x with the Permissions: attribute."; \
		$(JAR) -umf $(PTII)/$(JNLP_J3D_MANIFEST) $$x; \
		"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$$x $(KEYALIAS); \
	done

JAVA3D_VERSION=1.5.0
java3d: $(JAVA3D)
$(JAVA3D):
	wget -m -np http://download.java.net/media/java3d/webstart/release/j3d/$(JAVA3D_VERSION)/
	mkdir -p $(JAVA3D);
	mv download.java.net/media/java3d/webstart/release/j3d/ $(JAVA3D)
	rm -rf download.java.net
	wget http://download.java.net/media/java3d/webstart/release/vecmath/$(JAVA3D_VERSION)/vecmath.jar
	mkdir -p $(JAVA3D)/vecmath/$(JAVA3D_VERSION)
	mv vecmath.jar $(JAVA3D)/vecmath/$(JAVA3D_VERSION)


JNLP_JAI_MANIFEST = jnlp_jai_manifest.txt
$(JNLP_JAI_MANIFEST):
	echo "Application-Name: Java Advance Imaging (JAI), via the Ptolemy website" > $@
	echo "Permissions: all-permissions" >> $@
JAI = $(WEBSTART)/jai
sign_jai: $(JAI) $(JNLP_JAI_MANIFEST)
	@set `find $(JAI) -name "*.jar"`; \
	for x do \
		echo $$x; \
		echo "# Updating the manifest of $$x with the Permissions: attribute."; \
		$(JAR) -umf $(PTII)/$(JNLP_JAI_MANIFEST) $$x; \
		"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$$x $(KEYALIAS); \
	done

jai: $(JAI)
$(JAI):
	wget -m -np http://download.java.net/media/jai/webstart/release/1.1.3/
	mkdir $(JAI)
	mv download.java.net/media/jai/webstart/release/1.1.3 $(JAI)
jai_update:
	(cd $(JAI)/..; tar -cf - jai) | ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd ~www/ptweb/ptolemyII; tar -xf -"

# make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx" sign_jogl

JNLP_JOGL_MANIFEST = jnlp_jogl_manifest.txt
$(JNLP_JOGL_MANIFEST):
	echo "Application-Name: Java OpenGL, via the Ptolemy website" > $@
	echo "Permissions: all-permissions" >> $@

JOGL = $(WEBSTART)/jogl
sign_jogl: $(JOGL) $(JNLP_JOGL_MANIFEST)
	set `find $(JOGL) -name "*.jar"`; \
	for x do \
		echo $$x; \
		echo "# Updating the manifest of $$x with the Permissions: attribute."; \
		$(JAR) -umf $(PTII)/$(JNLP_JOGL_MANIFEST) $$x; \
		"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$$x $(KEYALIAS); \
	done

jogl: $(JOGL)/jogl-natives-windows-i586.jar
$(JOGL): 
	mkdir -p $(JOGL)

$(JOGL)/jogl.jnlp: $(JOGL)
	if [ ! -f $@ ]; then \
		echo "Downloading jogl.jnlp"; \
		cd $(JOGL); wget http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl.jnlp; \
	fi

$(JOGL)/jogl-natives-windows-i586.jar: $(JOGL)/jogl.jnlp
	set `grep .jar $(JOGL)/jogl.jnlp | awk -F \" '{print $$2}' | sort | uniq`; \
	for x do \
		cd $(JOGL); \
		echo $$x; \
		wget http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/$$x; \
        done

jogl_update:
	(cd $(JOGL)/..; tar -cf - jogl) | ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd ~www/ptweb/ptolemyII; tar -xf -"


# make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx" sign_gluegen-rt
JNLP_GLUEGEN-RT_MANIFEST = jnlp_gluegen-rt_manifest.txt
$(JNLP_GLUEGEN-RT_MANIFEST):
	echo "Application-Name: Gluegen-RT, via the Ptolemy website" > $@
	echo "Permissions: all-permissions" >> $@
GLUEGEN_RT = $(WEBSTART)/gluegen-rt
sign_gluegen-rt: $(GLUEGEN_RT) $(JNLP_GLUEGEN-RT_MANIFEST)
	set `find $(GLUEGEN_RT) -name "*.jar"`; \
	for x do \
		echo $$x; \
		echo "# Updating the manifest of $$x with the Permissions: attribute."; \
		$(JAR) -umf $(PTII)/$(JNLP_GLUEGEN-RT_MANIFEST) $$x; \
		"$(JARSIGNER)" \
		-keystore "$(KEYSTORE)" \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$$x $(KEYALIAS); \
	done

gluegen-rt: $(GLUEGEN_RT)/gluegen-rt-natives-windows-i586.jar
$(GLUEGEN_RT): 
	mkdir -p $(GLUEGEN_RT)

$(GLUEGEN_RT)/gluegen-rt.jnlp: $(GLUEGEN_RT)
	if [ ! -f $@ ]; then \
		echo "Downloading gluegen-rt.jnlp"; \
		cd $(GLUEGEN_RT); wget http://download.java.net/media/gluegen/webstart/gluegen-rt.jnlp; \
	fi

$(GLUEGEN_RT)/gluegen-rt-natives-windows-i586.jar: $(GLUEGEN_RT)/gluegen-rt.jnlp
	set `grep .jar $(GLUEGEN_RT)/gluegen-rt.jnlp | awk -F \" '{print $$2}' | sort | uniq`; \
	for x do \
		cd $(GLUEGEN_RT); \
		echo $$x; \
		wget http://download.java.net/media/gluegen/webstart/$$x; \
        done

gluegen-rt_update:
	(cd $(GLUEGEN_RT)/..; tar -cf - gluegen-rt) | ssh $(WEBSERVER_USER)@$(WEBSERVER) "cd ~www/ptweb/ptolemyII; tar -xf -"

