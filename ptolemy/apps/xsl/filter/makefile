# $Id$

# For Unix
SAXON_JAR =	/users/ptII/vendors/saxon/saxon7-1/saxon7.jar
SAXON = 	java -jar $(SAXON_JAR)

# For Windows
#SAXON_BIN =	/usr/local/bin/saxon
#SAXON = 	$(SAXON_BIN)

MODEL =		$(PTII)/ptolemy/domains/sdf/demo/Expression/Expression.xml
FILTER_OUTPUT =	filterOutput.xml
FILTER =	expressionFix.xsl 	

$(FILTER_OUTPUT): $(FILTER)
	$(SAXON) $(MODEL) $(FILTER) > $@
	diff $(MODEL) $@
