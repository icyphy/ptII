SAXON=/usr/local/bin/saxon
cartesianOut.xml: cartesianIn.xml cartesian.xsl
	$(SAXON) cartesianIn.xml cartesian.xsl > $@
	diff cartesianIn.xml $@
