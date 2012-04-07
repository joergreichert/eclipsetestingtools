package de.abg.reichert.joerg.eclipsetools.workingsets.internal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.CoreText;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.PlatformUI;

public class CustomGitProjectSetCapability extends ProjectSetCapability {

	private static final String SEPARATOR = ","; //$NON-NLS-1$
	private static final String VERSION = "1.0"; //$NON-NLS-1$

	private Shell shell;

	public CustomGitProjectSetCapability(Shell shell) {
		this.shell = shell;
	}

	private static final class ProjectReferenceComparator implements
			Comparator<ProjectReference>, Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(ProjectReference o1, ProjectReference o2) {
			final boolean reposEqual = o1.repository.equals(o2.repository);
			final boolean branchesEqual = o1.branch.equals(o2.branch);
			final boolean projectDirsEqual = o1.projectDir
					.equals(o2.projectDir);
			return reposEqual && branchesEqual && projectDirsEqual ? 0 : 1;
		}
	}

	private static final class ProjectReference {

		private static final String DEFAULT_BRANCH = Constants.MASTER;

		/**
		 * a relative path (from the repository root) to a project
		 */
		String projectDir;

		/**
		 * <code>repository</code> parameter
		 */
		URIish repository;

		/**
		 * the remote branch that will be checked out, see <code>--branch</code>
		 * option
		 */
		String branch = DEFAULT_BRANCH;

		@SuppressWarnings("boxing")
		ProjectReference(final String reference) throws URISyntaxException,
				IllegalArgumentException {
			final String[] tokens = reference.split(Pattern.quote(SEPARATOR));
			if (tokens.length != 4)
				throw new IllegalArgumentException("InvalidTokensCount: "
						+ tokens.length + " for " + tokens);

			this.repository = new URIish(tokens[1]);
			if (!"".equals(tokens[2])) //$NON-NLS-1$
				this.branch = tokens[2];
			this.projectDir = tokens[3];
		}
	}

	@Override
	public String[] asReference(IProject[] projects,
			ProjectSetSerializationContext context, IProgressMonitor monitor)
			throws TeamException {
		String[] references = new String[projects.length];
		for (int i = 0; i < projects.length; i++)
			references[i] = asReference(projects[i]);
		return references;
	}

	private String asReference(IProject project) throws TeamException {
		RepositoryMapping mapping = RepositoryMapping.getMapping(project);
		String branch;
		try {
			branch = mapping.getRepository().getBranch();
		} catch (IOException e) {
			throw new TeamException(NLS.bind(
					CoreText.GitProjectSetCapability_ExportCouldNotGetBranch,
					project.getName()));
		}
		StoredConfig config = mapping.getRepository().getConfig();
		String remote = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION,
				branch, ConfigConstants.CONFIG_KEY_REMOTE);
		String url = config.getString(ConfigConstants.CONFIG_REMOTE_SECTION,
				remote, ConfigConstants.CONFIG_KEY_URL);
		if (url == null)
			throw new TeamException(NLS.bind(
					CoreText.GitProjectSetCapability_ExportNoRemote,
					project.getName()));

		String projectPath = mapping.getRepoRelativePath(project);
		if (projectPath.equals("")) //$NON-NLS-1$
			projectPath = "."; //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();

		sb.append(VERSION);
		sb.append(SEPARATOR);
		sb.append(url);
		sb.append(SEPARATOR);
		sb.append(branch);
		sb.append(SEPARATOR);
		sb.append(projectPath);

		return sb.toString();
	}

	@Override
	public IProject[] addToWorkspace(final String[] referenceStrings,
			final ProjectSetSerializationContext context,
			final IProgressMonitor monitor) throws TeamException {
		final Map<URIish, Map<String, Set<ProjectReference>>> repositories = new LinkedHashMap<URIish, Map<String, Set<ProjectReference>>>();
		getRepositories(referenceStrings, repositories);
		final ArrayList<IProject> importedProjects = new ArrayList<IProject>();
		for (final Map.Entry<URIish, Map<String, Set<ProjectReference>>> entry : repositories
				.entrySet()) {
			final URIish gitUrl = entry.getKey();
			final Map<String, Set<ProjectReference>> branches = entry
					.getValue();

			for (final Map.Entry<String, Set<ProjectReference>> branchEntry : branches
					.entrySet()) {
				final String branch = branchEntry.getKey();
				final Set<ProjectReference> projects = branchEntry.getValue();

				try {
					final IPath workDir = getWorkingDir(gitUrl, branch,
							branches.keySet());
					boolean exists = isExistingWorkingDirectory(gitUrl,
							projects, workDir);

					int timeout = 60;
					String refName = Constants.R_HEADS + branch;

					clone(monitor, gitUrl, workDir, timeout, refName, exists);

					final File repositoryPath = getRepositoryPath(workDir);

					Activator.getDefault().getRepositoryUtil()
							.addConfiguredRepository(repositoryPath);

					// import projects from the current repository to workspace
					importProjects(workDir, projects, importedProjects,
							repositoryPath, monitor, exists);
				} catch (final InvocationTargetException e) {
					throw TeamException.asTeamException(e);
				} catch (final CoreException e) {
					throw TeamException.asTeamException(e);
				} catch (final InterruptedException e) {
					// was canceled by user
					return new IProject[0];
				}
			}
		}
		final IProject[] result = importedProjects
				.toArray(new IProject[importedProjects.size()]);
		return result;
	}

	protected File getRepositoryPath(final IPath workDir) {
		return workDir.append(Constants.DOT_GIT_EXT).toFile();
	}

	protected boolean isExistingWorkingDirectory(final URIish gitUrl,
			final Set<ProjectReference> projects, final IPath workDir)
			throws TeamException {
		return workDir.toFile().exists();
	}

	protected void importProjects(final IPath workDir,
			final Set<ProjectReference> projects,
			final ArrayList<IProject> importedProjects,
			final File repositoryPath, final IProgressMonitor monitor,
			boolean exists) throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot root = workspace.getRoot();
		for (final ProjectReference projectToImport : projects) {
			final IPath projectDir = workDir.append(projectToImport.projectDir);
			final IProjectDescription projectDescription = workspace
					.loadProjectDescription(projectDir
							.append(IProjectDescription.DESCRIPTION_FILE_NAME));
			final IProject project = root.getProject(projectDescription
					.getName());
			createProject(monitor, projectDescription, project, exists);
			importedProjects.add(project);

			project.open(monitor);
			connectProject(monitor, repositoryPath, project);
		}
	}

	protected void connectProject(final IProgressMonitor monitor,
			final File repositoryPath, final IProject project)
			throws CoreException {
		final ConnectProviderOperation connectProviderOperation = new ConnectProviderOperation(
				project, repositoryPath);
		connectProviderOperation.execute(monitor);
	}

	protected void createProject(final IProgressMonitor monitor,
			final IProjectDescription projectDescription,
			final IProject project, boolean exists) throws CoreException {
		project.create(projectDescription, monitor);
	}

	protected void clone(final IProgressMonitor monitor, final URIish gitUrl,
			final IPath workDir, int timeout, String refName, boolean exists)
			throws InvocationTargetException, InterruptedException {
		// TODO only clone if not exists
		if (!exists) {
			final CloneOperation cloneOperation = new CloneOperation(gitUrl,
					true, null, workDir.toFile(), refName,
					Constants.DEFAULT_REMOTE_NAME, timeout);
			cloneOperation.run(monitor);
		}
	}

	private void getRepositories(final String[] referenceStrings,
			final Map<URIish, Map<String, Set<ProjectReference>>> repositories)
			throws TeamException {
		for (final String reference : referenceStrings) {
			try {
				final ProjectReference projectReference = new ProjectReference(
						reference);
				Map<String, Set<ProjectReference>> repositoryBranches = repositories
						.get(projectReference.repository);
				if (repositoryBranches == null) {
					repositoryBranches = new HashMap<String, Set<ProjectReference>>();
					repositories.put(projectReference.repository,
							repositoryBranches);
				}
				Set<ProjectReference> projectReferences = repositoryBranches
						.get(projectReference.branch);
				if (projectReferences == null) {
					projectReferences = new TreeSet<ProjectReference>(
							new ProjectReferenceComparator());
					repositoryBranches.put(projectReference.branch,
							projectReferences);
				}

				projectReferences.add(projectReference);
			} catch (final IllegalArgumentException e) {
				throw new TeamException(reference, e);
			} catch (final URISyntaxException e) {
				throw new TeamException(reference, e);
			}
		}
	}

	/**
	 * @param gitUrl
	 * @param branch
	 *            the branch to check out
	 * @param allBranches
	 *            all branches which should be checked out for this gitUrl
	 * @return the directory where the project should be checked out
	 */

	private String absPath = null;

	private IPath getWorkingDir(URIish gitUrl, String branch,
			Set<String> allBranches) {
		absPath = null;
		if (getShell() != null) {
			getShell().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					DirectoryDialog directoryDialog = new DirectoryDialog(
							Display.getDefault().getActiveShell());
					absPath = directoryDialog.open();
				}
			});

		}
		final IPath workspaceLocation;
		if (absPath != null && new File(absPath).exists()) {
			workspaceLocation = Path.fromOSString(absPath);
		} else {
			workspaceLocation = ResourcesPlugin.getWorkspace().getRoot()
					.getRawLocation();
		}
		final String humanishName = gitUrl.getHumanishName();
		String extendedName;
		if (allBranches.size() == 1 || branch.equals(Constants.MASTER))
			extendedName = humanishName;
		else
			extendedName = humanishName + "_" + branch; //$NON-NLS-1$
		final IPath workDir = workspaceLocation.append(extendedName);
		return workDir;
	}

	protected Shell getShell() {
		return shell;
	}

}