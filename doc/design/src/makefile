# Makefile for the Ptolemy II Design doc framemaker source
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-2000 The Regents of the University of California.
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

# See the README file for information on how to convert the
# Ptolemy II Design Doc to HTML


ME =		ptII/doc/design/src

# Sub directories to run make in.
DIRS =

# Root of Ptolemy II directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

EXTRA_SRCS = \
	actor.fm \
	actorLib.fm \
	csp.fm \
	ct.fm \
	data.fm \
	dde.fm \
	de.fm \
	design.book \
	design.wdt \
	designIX.fm \
	designTOC.fm \
	glossary.fm \
	graph.fm \
	introduction.fm \
	kernel.fm \
	math.fm \
	models.fm \
	plot.fm \
	pn.fm \
	ptfilter.fm \
	references.fm \
	sdf.fm \
	template.fm \
	templateFontImpaired.fm \
	title.fm \
	type.fm

MISC_FILES =	figures

WPUBLISH_OUTPUT_DIR = Output

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(WPUBLISH_OUTPUT_DIR)

DESIGNDOC =	/users/ptII/doc/design/src

all: $(WPUBLISH_OUTPUT_DIR) $(WPUBLISH_OUTPUT_DIR)/introduction.html
install: all

$(WPUBLISH_OUTPUT_DIR):
	if [ ! -d $@ ]; then echo "Creating $@"; mkdir $@; fi

design.pdf: $(wildcard $(DESIGNDOC)/*.fm)
	-chmod g+w $^
	rm -f $@
	fmprint2pdf $(DESIGNDOC)/design.book

# Copy over chars for the title
CHARS=	alpha.gif \
	beta.gif \
	gamma.gif \
	logic_or.gif \
	logicand.gif \
	lt_equal.gif


SUPPORT_HTMLS = \
	$(WPUBLISH_OUTPUT_DIR)/index.html \
	$(WPUBLISH_OUTPUT_DIR)/support/bottom.gif \
	$(WPUBLISH_OUTPUT_DIR)/idx.html \
	$(WPUBLISH_OUTPUT_DIR)/content.html \
	$(WPUBLISH_OUTPUT_DIR)/search.html

support_htmls:	$(SUPPORT_HTMLS)
	-mkdir $(WPUBLISH_OUTPUT_DIR)/chars
	for file in $(CHARS); \
	do \
		cp /usr/tools/tools2/www/quadralay/lib/chars/$$file $(WPUBLISH_OUTPUT_DIR)/chars; \
	done

$(WPUBLISH_OUTPUT_DIR)/introduction.html: $(wildcard $(DESIGNDOC)/*.fm) support_htmls

	@if [ `whoami` != "ptuser" ]; then \
		echo "wpbatch only works for ptuser"; \
	else \
		echo "You must run this as ptuser and allow ptuser"; \
		echo "access to your X display"; \
		echo "Now running wpbatch, this could take a moment"; \
		wpbatch -fileset \
			/users/ptdesign/public_html/java/design.wdt; \
	fi

$(WPUBLISH_OUTPUT_DIR)/index.html:
	rm -f $@
	(cd $(WPUBLISH_OUTPUT_DIR); ln -s title.html index.html)


$(WPUBLISH_OUTPUT_DIR)/bottom.gif:
	-cp -f /users/ptdesign/doc/lib/buttons/* $(WPUBLISH_OUTPUT_DIR)

$(WPUBLISH_OUTPUT_DIR)/support/bottom.gif:
	-mkdir $(WPUBLISH_OUTPUT_DIR)/support
	-cp -f /users/ptdesign/doc/lib/support/* $(WPUBLISH_OUTPUT_DIR)/support


$(WPUBLISH_OUTPUT_DIR)/idx.html:
	rm -f $@
	(cd $(WPUBLISH_OUTPUT_DIR); ln -s designix.html idx.html)

$(WPUBLISH_OUTPUT_DIR)/content.html:
	rm -f $@
	(cd $(WPUBLISH_OUTPUT_DIR); ln -s designtoc.html content.html)

$(WPUBLISH_OUTPUT_DIR)/search.html:
	echo "<html>" >> $@
	echo "<head>" >> $@
	echo "<title>Search</title>" >> $@
	echo "</head>" >> $@
	echo '<body bgcolor="#faf0e6">' >> $@
	echo "</head>" >> $@
	echo "Sorry, no search yet" >> $@
	echo "</body>" >> $@
	echo "</html>" >> $@

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk

