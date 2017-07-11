$Id: README.txt 67777 2013-10-26 01:51:06Z cxh $

Sphinx is a speech recognition library from CMU.  The .jar files are large
(around 35MB total 

This directory is for the sphinx .jar files from: 

https://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=net.sf.phat&a=sphinx4-core&v=5prealpha&e=jar
https://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=net.sf.phat&a=sphinx4-data&v=5prealpha&e=jar

linked to from the download page referenced by the sphinx tutorial, 
https://cmusphinx.github.io/wiki/tutorialsphinx4/  

To use Sphinx, download these file to this directory, then run
cd $PTII
./configure

 sphinx4-core-5prealpha.jar and sphinx4-data-5prealpha.jar are used by the speech recognition accessor, 
 ptolemy/actor/lib/jjs/modules/speechRecognition/SpeechRecognitionHelper.java

See also ptolemy/actor/lib/jjs/modules/speechRecognition/sphinx-license.htm




