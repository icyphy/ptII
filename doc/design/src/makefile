# Makefile for the Ptolemy II Design doc framemaker source
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-1999 The Regents of the University of California.
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

########################################################
# Convert the Ptolemy II Design Doc to HTML
#
# To convert design.fm, do the following:
# 1. As yourself, do xhost carson
# 2. Become ptuser on carson
# 3. As ptuser, set your DISPLAY to carson:0
# 4. As ptuser, run cd ~ptdesign/public_html/java; make

DESIGNDOC=/users/ptII/doc/design/src

all: wmwork/Output/introduction.html
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

support_htmls:	wmwork/Output/index.html wmwork/Output/support/bottom.gif \
		wmwork/Output/idx.html wmwork/Output/content.html wmwork/Output/search.html
	-mkdir wmwork/Output/chars
	for file in $(CHARS); \
	do \
		cp /usr/tools/tools2/www/quadralay/lib/chars/$$file wmwork/Output/chars; \
	done

wmwork/Output/introduction.html: $(wildcard $(DESIGNDOC)/*.fm) support_htmls

	@if [ `whoami` != "ptuser" ]; then \
		echo "wpbatch only works for ptuser"; \
	else \
		echo "You must run this as ptuser and allow ptuser"; \
		echo "access to your X display"; \
		echo "Now running wpbatch, this could take a moment"; \
		wpbatch -fileset \
			/users/ptdesign/public_html/java/design.wdt; \
	fi

wmwork/Output/index.html:
	rm -f $@
	(cd wmwork/Output; ln -s title.html index.html)


wmwork/Output/bottom.gif:
	-cp -f /users/ptdesign/doc/lib/buttons/* wmwork/Output

wmwork/Output/support/bottom.gif:
	-mkdir wmwork/Output/support
	-cp -f /users/ptdesign/doc/lib/support/* wmwork/Output/support


wmwork/Output/idx.html:
	rm -f $@
	(cd wmwork/Output; ln -s designix.html idx.html)

wmwork/Output/content.html:
	rm -f $@
	(cd wmwork/Output; ln -s designtoc.html content.html)

wmwork/Output/search.html:
	echo "<html>" >> $@
	echo "<head>" >> $@
	echo "<title>Search</title>" >> $@
	echo "</head>" >> $@
	echo '<body bgcolor="#faf0e6">' >> $@
	echo "</head>" >> $@
	echo "Sorry, no search yet" >> $@
	echo "</body>" >> $@
	echo "</html>" >> $@
