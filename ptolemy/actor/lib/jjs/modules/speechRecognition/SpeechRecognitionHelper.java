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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// SpeechRecognitionHelper

/**
   A helper class for speech recognition.  This class uses the CMU Sphinx-4 recognition engine.
   Note that speech synthesis (text-to-speech) is a separate capability not covered by Sphinx.
   https://github.com/cmusphinx/sphinx4

   This helper only handles live speech at the moment.  Sphinx also supports reading speech from 
   a file.  This could be added in the future.
   
   Sphinx4 requires some large .jar files which must be downloaded separately.  Please see 
   ptolemy/vendors/misc/sphinx/README.txt
   for instructions.
   
   Sphinx works much better with a custom dictionary and language model.  These can be generated from a file of 
   sentences with an online tool.  Please see:
   https://www.icyphy.org/accessors/wiki/Version1/SpeechRecognition
   
   A sample dictionary, language model and sentence file from Sphinx4 are included with the demo:
   $PTII/ptolemy/actor/lib/jjs/modules/speechRecognition/demo/SpeechRecognition 
   weather.dic, weather.lm and weather.samples
   
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
	public SpeechRecognitionHelper(Object actor, ScriptObjectMirror helping, ScriptObjectMirror options) throws IllegalActionException {
		super(actor, helping);
		
		if (options.containsKey("continuous")) {
			_continuous = (Boolean) options.get("continuous");
		} else {
			_continuous = true;
		}
		
		File testFile;
		
		// Set any language model and dictionary.  Check if they exist, since Sphinx fails silent if they don't.
		if (options.containsKey("languageModelPath") && !options.get("languageModelPath").toString().isEmpty()) {
			testFile = new File((String) options.get("languageModelPath"));
			if(!testFile.exists() || !testFile.isFile()) { 
				_currentObj.callMember("emit", "onerror", "SpeechRecognition:  Requested language model file does not "
						+ "exist: " + (String) options.get("languageModelPath"));
				_languageModelPath = _defaultLanguageModelPath;
			}
			_languageModelPath = (String) options.get("languageModelPath");
		} else {
			_languageModelPath = _defaultLanguageModelPath;
		}
		
		if (options.containsKey("dictionaryPath") && !options.get("dictionaryPath").toString().isEmpty()) {
			_dictionaryPath = (String) options.get("dictionaryPath");
			testFile = new File((String) options.get("dictionaryPath"));
			if(!testFile.exists() || !testFile.isFile()) { 
				_currentObj.callMember("emit", "onerror", "SpeechRecognition:  Requested dictionary file does not "
						+ "exist: " + (String) options.get("dictionaryPath"));
				_dictionaryPath = _defaultDictionaryPath;
			}
		} else {
			_dictionaryPath = _defaultDictionaryPath;
		}
		
		 _worker = null;
		 
		 // Disable Sphinx console log messages.  Sphinx produces a lot of them.
		 /*
	     Logger cmRootLogger = Logger.getLogger("default.config");
	     cmRootLogger.setLevel(java.util.logging.Level.OFF);
	     String conFile = System.getProperty("java.util.logging.config.file");
	     if (conFile == null) {
	           System.setProperty("java.util.logging.config.file", "ignoreAllSphinx4LoggingOutput");
	     }
	     */
		 
		 // Load speech recognizer by reflection since .jar files are not included with Ptolemy due to size.
 			
	     try {
			File jarFile = new File("vendors/misc/sphinx/sphinx4-core-5prealpha.jar");
			URL urls[] = { new URL("jar:file:" + jarFile.getCanonicalPath() + "!/") };
			
			URLClassLoader loader = URLClassLoader.newInstance(urls);
			_configurationClass = loader.loadClass("edu.cmu.sphinx.api.Configuration");
			_recognizerClass = loader.loadClass("edu.cmu.sphinx.api.LiveSpeechRecognizer");
			
			try {
		        _configuration = _configurationClass.getConstructor().newInstance();
		        
		        Class<?>[] booleanType = {boolean.class};
		        Class<?>[] stringType = {String.class};
		        Class<?>[] configurationType = {_configuration.getClass()};
		        
		        _configuration.getClass().getMethod("setAcousticModelPath", stringType).invoke(_configuration, _acousticModel);
		        _configuration.getClass().getMethod("setDictionaryPath", stringType).invoke(_configuration, _dictionaryPath);
		        _configuration.getClass().getMethod("setLanguageModelPath", stringType).invoke(_configuration, _languageModelPath);
		        _configuration.getClass().getMethod("setUseGrammar", booleanType).invoke(_configuration, false);
		        
		        _recognizer = _recognizerClass.getConstructor(configurationType).newInstance(_configuration);

	       } catch(InvocationTargetException e) {
		        _currentObj.callMember("emit", "onerror", "Failed to instantiate speech recognizer: " + e.getMessage());
	       } catch(IllegalAccessException e2) {
		        _currentObj.callMember("emit", "onerror", "Failed to instantiate speech recognizer: " + e2.getMessage());
	       } catch(NoSuchMethodException e3) {
		        _currentObj.callMember("emit", "onerror", "Failed to instantiate speech recognizer: " + e3.getMessage());
	       } catch(InstantiationException e4) {
	    	   _currentObj.callMember("emit", "onerror", "Failed to instantiate speech recognizer: " + e4.getMessage());
	       }
			
	     } catch(MalformedURLException e) {
	    	 _currentObj.callMember("emit", "onerror", "Failed to load speech recognition .jar file.  This file must be downloaded separately. " + e.getMessage());
	     } catch(IOException e2) {
	    	 _currentObj.callMember("emit", "onerror", "Failed to load speech recognition .jar file.  This file must be downloaded separately. " + e2.getMessage());
	     } catch(ClassNotFoundException e3) {
	    	 _currentObj.callMember("emit", "onerror", "Failed to load speech recognition classes.  The .jar file must be downloaded separately. " + e3.getMessage());
	     }
	}
	
	/** Start speech recognition.  The recognizer runs in a separate thread, since it runs continuously.
	 */
	
	public void start() {
		Class<?>[] booleanType = {boolean.class};

		if (_recognizer == null) {
			_currentObj.callMember("emit", "onerror", "Cannot start speech recognizer.  Speech recognizer failed to initialize.");
			return;
		}
		
		if (_worker == null) {
			
	        // Start a thread to transcribe speech until stopped.
	        _worker = new Thread() {
	            public void run() {
	    			try {
	    				_recognizer.getClass().getMethod("startRecognition", booleanType).invoke(_recognizer, true);
	    			} catch(InvocationTargetException e) {
	    				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to start: " + e.getMessage());
	    			} catch(NoSuchMethodException e2) {
	    				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to start: " + e2.getMessage());
	    			} catch(IllegalAccessException e3) {
	    				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to start: " + e3.getMessage());
	    			}
	    			
	            	while (true) {
		            	if (Thread.interrupted()) {
		        			try {
		        				_recognizer.getClass().getMethod("stopRecognition").invoke(_recognizer);
		        				_worker = null;
		        			} catch(InvocationTargetException e) {
		        				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to stop: " + e.getMessage());
		        			} catch(NoSuchMethodException e2) {
		        				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to stop: " + e2.getMessage());
		        			} catch(IllegalAccessException e3) {
		        				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to stop: " + e3.getMessage());
		        			}
		            		return;
		            	} 
		            	
		            	String utterance;
		            	
		    			try {
		    				_speechResult = _recognizer.getClass().getMethod("getResult").invoke(_recognizer);
		    				
			    			if (_speechResult != null) {
			    				utterance = (String) _speechResult.getClass().getMethod("getHypothesis").invoke(_speechResult);
			    				// Sphinx lmtool generates dictionaries with all-caps words.  
			    				// Convert to lowercase to be less annoying.
			    				if (utterance.length() > 0) {
			    					_currentObj.callMember("emit", "result", utterance.toLowerCase());
			    				}
			    				if (!_continuous) {
			    					_worker.interrupt();
			    				}
			    			}
		    			} catch(InvocationTargetException e) {
		    				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to start: " + e.getMessage());
		    				_worker.interrupt();
		    			} catch(NoSuchMethodException e2) {
		    				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to start: " + e2.getMessage());
		    				_worker.interrupt();
		    			} catch(IllegalAccessException e3) {
		    				_currentObj.callMember("emit", "onerror", "Speech recognizer failed to start: " + e3.getMessage());
		    				_worker.interrupt();
		    			}
	            	}
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
	
	/** The instance of edu.cmu.sphinx.api.Configuration. */
	private Object _configuration;
	
	/** The class of edu.cmu.sphinx.api.Configuration. */
	private Class<?> _configurationClass;
	
	/** True if speech should be recognized continuously; false to stop after first utterance. */
	private boolean _continuous;
	
	/** The default dictionary for the speech recognizer.  Here, US English. */
	private String _defaultDictionaryPath = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
	
	/** The default language model for the speech recognizer.  Here, US English.  */
	private String _defaultLanguageModelPath = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
	
	/** The dictionary for the speech recognizer.  */
	private String _dictionaryPath = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
	
	/** The language model for the speech recognizer.  */
	private String _languageModelPath = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
	
	/** The instance of edu.cmu.sphinx.api.LiveSpeechRecognizer. */
	private Object _recognizer;
	
	/** The class of edu.cmu.sphinx.api.LiveSpeechRecognizer. */
	private Class<?> _recognizerClass;
	
	/** The instance of edu.cmu.sphinx.api.SpeechResult. */
	private Object _speechResult;
	
	/** A separate thread to run the speech recognizer in.  */
	private Thread _worker;
}
