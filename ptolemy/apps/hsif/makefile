
demo :
	java -classpath ".;../saxon7.jar" net.sf.saxon.Transform data/books.xml styles/books.xsl >output.html

shsif :
	java -classpath ".;../saxon7.jar" net.sf.saxon.Transform data/shsif.xml styles/hsif.xsl >shsif.xml

thsif :
	java -classpath ".;../saxon7.jar" net.sf.saxon.Transform data/thsif.xml styles/hsif.xsl >thsif.xml

phsif :
	java -classpath ".;../saxon7.jar" net.sf.saxon.Transform data/phsif.xml styles/hsif.xsl >phsif.xml

all:	shsif thsif phsif

