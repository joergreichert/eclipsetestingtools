package de.abg.reichert.joerg.eclipsetools.workingsets;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.UIProjectSetSerializationContext;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.progress.UIJob;

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
		XMLMemento xmlMemento = getXMLMementoHelper().stringToXMLMemento(
				psfContents);
		return importProjectSet(xmlMemento, filename, shell, monitor);
	}

	public IProject[] importProjectSet(String filename, Shell shell,
			IProgressMonitor monitor) throws InvocationTargetException {
		XMLMemento xmlMemento = getXMLMementoHelper().filenameToXMLMemento(
				filename);
		return importProjectSet(xmlMemento, filename, shell, monitor);
	}

	private IProject[] importProjectSet(XMLMemento xmlMemento, String filename,
			Shell shell, IProgressMonitor monitor)
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
			getWorkingSetImportHelper().addToWorkspace(filename, shell,
					monitor, newProjects, serializer);
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
				referenceStrings.add(projects[j].getString("reference")); //$NON-NLS-1$
			}
			getWorkingSetImportHelper().handleRepository(monitor, newProjects,
					context, errors, referenceStrings, providers, i);
		}
		getWorkingSetImportHelper().handleErrors(errors);
		final List<String> workingSetNames = getWorkingSetImportHelper()
				.handleWorkingSets(xmlMemento, shell);

		UIJob job = new UIJob("Switch and sort working sets") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				switchPackageExplorerToWorksetMode(workingSetNames);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@SuppressWarnings("restriction")
	protected void switchPackageExplorerToWorksetMode(
			List<String> workingSetNames) {
		IWorkbenchPage page = getActivePage();
		String viewId = "org.eclipse.jdt.ui.PackageExplorer"; // defined by you
		IViewReference ref = page.findViewReference(viewId);
		if (ref != null) {
			IWorkbenchPart part = ref.getPart(true);
			if (part instanceof PackageExplorerPart) {
				PackageExplorerPart explorer = (PackageExplorerPart) part;
				explorer.rootModeChanged(PackageExplorerPart.WORKING_SETS_AS_ROOTS);
				sortWorkingSets(workingSetNames, explorer);
			}
		}
	}

	@SuppressWarnings("restriction")	
	private IWorkbenchPage getActivePage() {
		IWorkbenchPage page = null;
		int timeOut = 20000;
		int time = 0;
		while(page == null && time<=timeOut) {
			page = Workbench.getInstance()
					.getActiveWorkbenchWindow().getActivePage();
			if(page == null) {
				time += 1000;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return page;
	}

	@SuppressWarnings("restriction")
	protected void sortWorkingSets(List<String> workingSetNames,
			PackageExplorerPart explorer) {
		IWorkingSet[] workingSets = explorer.getWorkingSetModel()
				.getAllWorkingSets();
		Map<String, IWorkingSet> nameToSet = new HashMap<String, IWorkingSet>();
		for(IWorkingSet ws : workingSets) {
			nameToSet.put(ws.getLabel(), ws);
		}
		IWorkingSet[] sortedWorkingSets = new IWorkingSet[workingSets.length];
		List<String> visited = new ArrayList<String>();
		int counter = 0;
		IWorkingSet ws;
		for(String workingSetName : workingSetNames) {
			ws = nameToSet.get(workingSetName);
			if(ws != null) {
				sortedWorkingSets[counter] = ws;
				counter++;
				visited.add(workingSetName);
			}
		}
		for(IWorkingSet workingSet : workingSets) {
			if(!visited.contains(workingSet.getLabel()) && counter < sortedWorkingSets.length) {
				sortedWorkingSets[counter] = workingSet;
				counter++;
				visited.add(workingSet.getLabel());
			}
		}
		boolean sort = false;
		explorer.getWorkingSetModel().setWorkingSets(sortedWorkingSets, sort,
				sortedWorkingSets);
	}
}
