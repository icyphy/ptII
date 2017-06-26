/* A helper class for the device discovery accessor.

   Copyright (c) 2015-2017 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

 */

package ptolemy.actor.lib.jjs.modules.speechRecognition;

import java.io.IOException;
import java.util.logging.Logger;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;

///////////////////////////////////////////////////////////////////
//// SpeechRecognitionHelper

/**
   A helper class for speech recognition.  This class uses the CMU Sphinx-4 recognition engine.
   Note that speech synthesis (text-to-speech) is a separate capability not covered by Sphinx.
   https://github.com/cmusphinx/sphinx4

   This helper only handles live speech at the moment.  Sphinx also supports reading speech from 
   a file.  This could be added in the future.
   
   @author Elizabeth Osyk
   @version $Id: SpeechRecognitionHelper.java 75909 2017-04-11 03:01:21Z beth@berkeley.edu $
   @since Ptolemy II 11.0
   @Pt.ProposedRating Red (ltrnc)
   @Pt.AcceptedRating Red (ltrnc)
 */
public class SpeechRecognitionHelper extends HelperBase {
	
	/** Construct a SpeechRecognitionHelper.
	 * 
	 * @param actor  The PtolemyII actor associated with this helper.
	 * @param helping  The Javascript object this helper is helping.
	 */
	public SpeechRecognitionHelper(Object actor, ScriptObjectMirror helping) {
		super(actor, helping);
		 _worker = null;
	     Configuration configuration = new Configuration();
	     configuration.setAcousticModelPath(_acousticModel);
	     configuration.setDictionaryPath(_dictionaryPath);
	     configuration.setLanguageModelPath(_languageModel);
	     
	     configuration.setUseGrammar(false);
	     
	     // Disable Sphinx logging to console.
	     // See:  https://stackoverflow.com/questions/35560969/disable-console-mess-in-cmusphinx4
	     Logger cmRootLogger = Logger.getLogger("default.config");
	     cmRootLogger.setLevel(java.util.logging.Level.OFF);
	     String conFile = System.getProperty("java.util.logging.config.file");
	     if (conFile == null) {
	           System.setProperty("java.util.logging.config.file", "ignoreAllSphinx4LoggingOutput");
	     }
	     
	     try {
	    	 _recognizer = new LiveSpeechRecognizer(configuration);
	     } catch(IOException e) {
	    	 _currentObj.callMember("emit", "onerror", "SpeechRecognitionHelper failed to instantiate: " + e.getMessage());
	     }
	}
	
	/** Start speech recognition.  The recognizer runs in a separate thread, since it runs continuously.
	 */
	public void start() {
		if (_worker == null) {
			_recognizer.startRecognition(true);
			
	        // Start a thread to transcribe speech until stopped.
	        _worker = new Thread() {
	            public void run() {
	            	while (true) {
		            	if (Thread.interrupted()) {
		            		_recognizer.stopRecognition();
		            		_worker = null;
		            		break;
		            	} 
		            	
			            String utterance = _recognizer.getResult().getHypothesis();
			            _currentObj.callMember("emit", "result", utterance);
	            	}
	            	return;
	            }
	        };
	        
	        _worker.start();
		}
	}
	
	/** Stop speech recognition.  
	 */
	public void stop() {
		if (_worker != null) {
			_worker.interrupt();
		}
	}
	
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////
	
	/** The acoustic model for the speech recognizer.  Here, US English.  */
	private String _acousticModel = "resource:/edu/cmu/sphinx/models/en-us/en-us";
	
	/** The dictionary for the speech recognizer.  Here, US English. */
	private String _dictionaryPath = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
	
	/** The language model for the speech recognizer.  Here, US English.  */
	private String _languageModel = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
	
	/** The speech recognizing engine. */
	private LiveSpeechRecognizer _recognizer;
	
	/** A separate thread to run the speech recognizer in.  */
	private Thread _worker;
}
