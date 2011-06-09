# Makefile stub for Giotto Codegen
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
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
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

# This makefile stub has rules for Giotto code generation.
# This makefile is usually included in the bottom of other makefiles
# This makefile expects the PTPACKAGE variable to be defined to name the model

GIOTTO_DIR=c:/Program Files/Giotto/giotto1.0.1
demo_gdk: compile_driver gdk

$(PTPACKAGE).giotto:
	if [ ! -f $(PTPACKAGE).giotto ]; then \
	    error "$(PTPACKAGE).giotto does not exist!, run make demo_vergil and click on the Giotto Code Generator Icon to generate $(PTPACKAGE).giotto"; \
        fi

$(PTPACKAGE): $(PTPACKAGE).giotto
	@echo "# Creating Java stub files"
	$(ROOT)/ptolemy/domains/giotto/kernel/mkGiottoCGJava $(PTPACKAGE).giotto

compile_driver: $(PTPACKAGE)
	@echo "# Compiling Java stub files"
	(cd $(PTPACKAGE); "$(JAVAC)" -classpath "$(GIOTTO_DIR)/gdk.jar" *.java)

gdk:
	@echo "# After GDK comes up, perform the following steps"
	@echo "# 1. File -> open $(PTPACKAGE).giotto"
	@echo "# 2. Change the package from giotto.functionality.code.Hovercraft to $(PTPACKAGE)"
	@echo "# 3. Hit the Compile button"
	@echo "# 4. Run the example by doing E Code -> run E Code"
	@echo "# See standard out for the output."
	"$(JAVA)" \
		-classpath ".$(CLASSPATHSEPARATOR)$(GIOTTO_DIR)" \
		giotto.gdk.Start $(PTPACKAGE).giotto
