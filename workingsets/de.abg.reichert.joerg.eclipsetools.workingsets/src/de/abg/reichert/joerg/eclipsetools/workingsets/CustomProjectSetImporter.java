package de.abg.reichert.joerg.eclipsetools.workingsets;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.UIProjectSetSerializationContext;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

import de.abg.reichert.joerg.eclipsetools.workingsets.internal.WorkingSetHelper;
import de.abg.reichert.joerg.eclipsetools.workingsets.internal.WorkingSetImportHelper;
import de.abg.reichert.joerg.eclipsetools.workingsets.internal.XMLMementoHelper;

public class CustomProjectSetImporter {
	private XMLMementoHelper xmlMementoHelper;
	private WorkingSetImportHelper importHelper;
	private Shell shell; 

	public CustomProjectSetImporter(Shell shell) {
		this.shell = shell;
	}

	protected XMLMementoHelper getXMLMementoHelper() {
		if (xmlMementoHelper == null) {
			xmlMementoHelper = new XMLMementoHelper();
		}
		return xmlMementoHelper;
	}
	
	protected WorkingSetImportHelper getWorkingSetImportHelper() {
		if (importHelper == null) {
			importHelper = new WorkingSetImportHelper(getShell());
		}
		return importHelper;
	}

	protected Shell getShell() {
		return shell;
	}

	public IProject[] importProjectSetFromString(String psfContents,
			String filename, Shell shell, IProgressMonitor monitor)
			throws InvocationTargetException {
		XMLMemento xmlMemento = getXMLMementoHelper().stringToXMLMemento(psfContents);
		return importProjectSet(xmlMemento, filename, shell, monitor);
	}

	public IProject[] importProjectSet(String filename, Shell shell,
			IProgressMonitor monitor) throws InvocationTargetException {
		XMLMemento xmlMemento = getXMLMementoHelper().filenameToXMLMemento(filename);
		return importProjectSet(xmlMemento, filename, shell, monitor);
	}

	private IProject[] importProjectSet(XMLMemento xmlMemento,
			String filename, Shell shell, IProgressMonitor monitor)
			throws InvocationTargetException {
		try {
			String version = xmlMemento.getString("version"); //$NON-NLS-1$
			List newProjects = new ArrayList();
			if (version.equals("1.0")) { //$NON-NLS-1$
				handleVersionOne(filename, shell, monitor, newProjects);
			} else {
				handleNewerVersion(xmlMemento, filename, shell, monitor,
						newProjects);
			}
			return (IProject[]) newProjects.toArray(new IProject[newProjects
					.size()]);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	private void handleVersionOne(String filename, Shell shell,
			IProgressMonitor monitor, List newProjects) throws TeamException {
		IProjectSetSerializer serializer = Team
				.getProjectSetSerializer("versionOneSerializer"); //$NON-NLS-1$
		if (serializer != null) {
			getWorkingSetImportHelper().addToWorkspace(filename, shell, monitor, newProjects,
					serializer);
		}
	}

	private void handleNewerVersion(XMLMemento xmlMemento, String filename,
			Shell shell, IProgressMonitor monitor, List newProjects)
			throws TeamException {
		UIProjectSetSerializationContext context = new UIProjectSetSerializationContext(
				shell, filename);
		List errors = new ArrayList();
		ArrayList referenceStrings = new ArrayList();
		IMemento[] providers = xmlMemento.getChildren("provider"); //$NON-NLS-1$
		for (int i = 0; i < providers.length; i++) {
			IMemento[] projects = providers[i].getChildren("project"); //$NON-NLS-1$
			for (int j = 0; j < projects.length; j++) {
				referenceStrings
						.add(projects[j].getString("reference")); //$NON-NLS-1$
			}
			getWorkingSetImportHelper().handleRepository(monitor, newProjects, context, errors,
					referenceStrings, providers, i);
		}
		getWorkingSetImportHelper().handleErrors(errors);
		getWorkingSetImportHelper().handleWorkingSets(xmlMemento, shell);
	}
}
