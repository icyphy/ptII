/*
 * Created on 8 juil. 2003
 *
 * @ProposedRating Yellow (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.vergil;

import java.net.URL;

import javax.swing.UIManager;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.gui.MessageHandler;
import ptolemy.vergil.VergilApplication;

import thales.actor.gui.SingleWindowHTMLViewer;

/**
 * <p>Titre : SingleWindowApplication</p>
 * <p>Description : Main entry point for the SingleWindow mode</p>
 * <p>Copyright : Copyright (c) 2003</p>
 * <p>Société : Thales Research and technology</p>
 * @author Jérôme Blanc & Benoit Masson
 * 12 nov. 2003
 */
public class SingleWindowApplication extends VergilApplication {

	//Main Frame
	public static SingleWindowHTMLViewer _mainFrame;

	/**
	 * @param args
	 * @throws Exception
	 */
	public SingleWindowApplication(String[] args) throws Exception {
		super(args);
	}

	public static void main(String[] args) {

		try {
			new SingleWindowApplication(args);
		} catch (Exception ex) {
			MessageHandler.error("Command failed", ex);
			System.exit(0);
		}

		// If the -test arg was set, then exit after 2 seconds.
		if (_test) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			System.exit(0);
		}
	}

	/* (non-Javadoc)
	 * @see ptolemy.actor.gui.MoMLApplication#_createDefaultConfiguration()
	 */
	protected Configuration _createDefaultConfiguration() throws Exception {
		return _readConfiguration(specToURL("thales/configs/singleWindow/SingleWindowConfiguration.xml"));
	}

	/* (non-Javadoc)
	 * @see ptolemy.actor.gui.MoMLApplication#_createEmptyConfiguration()
	 */
	protected Configuration _createEmptyConfiguration() throws Exception {
		Configuration configuration = _createDefaultConfiguration();

		try {
			UIManager.setLookAndFeel(System.getProperty("swing.defaultlaf"));
		} catch (Exception e) {
			// Ignore exceptions, which only result in the wrong look and feel.
		}

		// FIXME: This code is Dog slow for some reason.
		URL inurl = specToURL("thales/configs/singleWindow/SingleWindowWelcomeWindow.xml");
		_parser.reset();
		_parser.setContext(configuration);
		_parser.parse(inurl, inurl.openStream());
		Effigy doc = (Effigy) configuration.getEntity("directory.doc");
		URL idurl = specToURL("ptolemy/configs/full/intro.htm");
		doc.identifier.setExpression(idurl.toExternalForm());

		if (_mainFrame != null) {
			_mainFrame.setConfiguration(configuration);
		}

		return configuration;
	}

}
