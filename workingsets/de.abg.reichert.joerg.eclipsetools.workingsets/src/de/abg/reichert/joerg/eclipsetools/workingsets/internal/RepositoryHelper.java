package de.abg.reichert.joerg.eclipsetools.workingsets.internal;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.GitProjectSetCapability;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.TeamCapabilityHelper;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.UIProjectSetSerializationContext;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

public class RepositoryHelper {
	private Shell shell;

	public RepositoryHelper(Shell shell) {
		this.shell = shell;
	}

	IProject[] addToWorkspace(IProgressMonitor monitor,
			UIProjectSetSerializationContext context,
			ArrayList referenceStrings, ProjectSetCapability serializer)
			throws TeamException {
		if(serializer instanceof org.eclipse.egit.core.GitProjectSetCapability) {
			serializer = new CustomGitProjectSetCapability(getShell());
		}
		
		IProject[] allProjects = serializer
				.addToWorkspace(
						(String[]) referenceStrings
								.toArray(new String[referenceStrings
										.size()]), context,
						monitor);
		return allProjects;
	}

	protected Shell getShell() {
		return shell;
	}

	ProjectSetCapability getSerializer(
			RepositoryProviderType providerType) {
		ProjectSetCapability serializer = providerType
				.getProjectSetCapability();
		ProjectSetCapability.ensureBackwardsCompatible(
				providerType, serializer);
		return serializer;
	}

	RepositoryProviderType getProviderType(IMemento[] providers, int i)
			throws TeamException {
		String id = providers[i].getString("id"); //$NON-NLS-1$
		TeamCapabilityHelper.getInstance().processRepositoryId(
				id,
				PlatformUI.getWorkbench().getActivitySupport());
		RepositoryProviderType providerType = RepositoryProviderType
				.getProviderType(id);
		if (providerType == null) {
			// The provider type is absent. Perhaps there is
			// another provider that can import this type
			providerType = TeamPlugin.getAliasType(id);
		}
		if (providerType == null) {
			throw new TeamException(
					new Status(
							IStatus.ERROR,
							TeamUIPlugin.ID,
							0,
							NLS.bind(
									TeamUIMessages.ProjectSetImportWizard_0,
									new String[] { id }), null));
		}
		return providerType;
	}
}
