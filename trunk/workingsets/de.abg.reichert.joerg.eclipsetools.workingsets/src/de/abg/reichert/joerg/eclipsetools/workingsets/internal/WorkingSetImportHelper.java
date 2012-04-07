package de.abg.reichert.joerg.eclipsetools.workingsets.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.UIProjectSetSerializationContext;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.XMLMemento;

public class WorkingSetImportHelper {
	private WorkingSetHelper wsHelper;
	private RepositoryHelper repositoryHelper;
	private Shell shell;

	public WorkingSetImportHelper(Shell shell) {
		this.shell = shell;
	}

	protected WorkingSetHelper getWorkingSetHelper() {
		if (wsHelper == null) {
			wsHelper = new WorkingSetHelper();
		}
		return wsHelper;
	}

	protected RepositoryHelper getRepositoryHelper(Shell shell) {
		if (repositoryHelper == null) {
			repositoryHelper = new RepositoryHelper(shell);
		}
		return repositoryHelper;
	}

	public void addToWorkspace(String filename, Shell shell,
			IProgressMonitor monitor, List newProjects,
			IProjectSetSerializer serializer) throws TeamException {
		IProject[] projects = serializer.addToWorkspace(new String[0],
				filename, shell, monitor);
		calculateNewProjects(newProjects, projects);
	}

	private void calculateNewProjects(List newProjects, IProject[] projects) {
		if (projects != null)
			newProjects.addAll(Arrays.asList(projects));
	}

	public List<String> handleWorkingSets(XMLMemento xmlMemento, Shell shell) {
		List<String> workingSetNames = new ArrayList<String>(); 
		// try working sets
		IMemento[] sets = xmlMemento.getChildren("workingSets"); //$NON-NLS-1$
		IWorkingSetManager wsManager = TeamUIPlugin.getPlugin().getWorkbench()
				.getWorkingSetManager();
		
		WorkingSetStatus status = new WorkingSetStatus();

		for (int i = 0; i < sets.length; i++) {
			IWorkingSet newWs = wsManager.createWorkingSet(sets[i]);
			if (newWs != null) {
				workingSetNames.add(newWs.getName());
				IWorkingSet oldWs = wsManager.getWorkingSet(newWs.getName());
				if (oldWs == null) {
					wsManager.addWorkingSet(newWs);
				} else if (status.isReplaceAll()) {
					getWorkingSetHelper().replaceWorkingSet(wsManager, newWs,
							oldWs);
				} else if (status.isMergeAll()) {
					getWorkingSetHelper().mergeWorkingSets(newWs, oldWs);
				} else if (!status.isSkipAll()) {
					getWorkingSetHelper().handleExistingWorkingSet(wsManager, oldWs, newWs, status, shell);
				}
			}
		}
		return workingSetNames;
	}

	public void handleErrors(List errors) throws TeamException {
		if (!errors.isEmpty()) {
			if (errors.size() == 1) {
				throw (TeamException) errors.get(0);
			} else {
				TeamException[] exceptions = (TeamException[]) errors
						.toArray(new TeamException[errors.size()]);
				IStatus[] status = new IStatus[exceptions.length];
				for (int i = 0; i < exceptions.length; i++) {
					status[i] = exceptions[i].getStatus();
				}
				throw new TeamException(new MultiStatus(TeamUIPlugin.ID, 0,
						status, TeamUIMessages.ProjectSetImportWizard_1, null));
			}
		}
	}

	public void handleRepository(IProgressMonitor monitor, List newProjects,
			UIProjectSetSerializationContext context, List errors,
			ArrayList referenceStrings, IMemento[] providers, int i) {
		try {
			RepositoryProviderType providerType = getRepositoryHelper(getShell())
					.getProviderType(providers, i);
			ProjectSetCapability serializer = getRepositoryHelper(getShell())
					.getSerializer(providerType);
			if (serializer != null) {
				IProject[] allProjects = getRepositoryHelper(getShell()).addToWorkspace(
						monitor, context, referenceStrings, serializer);
				calculateNewProjects(newProjects, allProjects);
			}
			referenceStrings.clear();
		} catch (TeamException e) {
			errors.add(e);
		}
	}

	protected Shell getShell() {
		return shell;
	}
}
