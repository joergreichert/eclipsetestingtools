package de.abg.reichert.joerg.eclipsetools.workingsets.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class XMLMementoHelper {

	public XMLMemento stringToXMLMemento(String stringContents)
			throws InvocationTargetException {
		StringReader reader = null;
		try {
			reader = new StringReader(stringContents);
			return XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	public XMLMemento filenameToXMLMemento(String filename) throws InvocationTargetException {
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(filename), "UTF-8"); //$NON-NLS-1$
			return XMLMemento.createReadRoot(reader);
		} catch (UnsupportedEncodingException e) {
			throw new InvocationTargetException(e);
		} catch (FileNotFoundException e) {
			throw new InvocationTargetException(e);
		} catch (WorkbenchException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}
			}
		}
	}
}
