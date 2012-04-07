package de.abg.reichert.joerg.eclipsetools.workingsets;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.wizards.PsfFilenameStore;
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

public class CustomImportProjectSetOperation extends TeamOperation {
	
	private String psfFileContents;
	private String urlString;
	private String psfFile;
	private IWorkingSet[] workingSets;
	private CustomProjectSetImporter customProjectSetImporter = null;

	
	
	/**
	 * Operation for importing a Team Project Set stored in a String
	 * 
	 * @param context
	 *            a runnable context,
	 *            <code>null</null> for running in background
	 * @param psfFileContents
	 *            the psf file content to load
	 * @param urlString
	 *            url or path to file loaded into <code>psfFileContents</code>
	 * @param workingSets
	 *            an array of working sets where imported project should be
	 *            added. If a working set doesn't exist it will be created. The
	 *            array cannot be <code>null</code>, pass an empty array if you
	 *            don't want to add projects to any working set.
	 */
	public CustomImportProjectSetOperation(IRunnableContext context,
			String psfFileContents, String urlString, IWorkingSet[] workingSets, CustomProjectSetImporter customProjectSetImporter) {
		super(context);
		this.psfFileContents = psfFileContents;
		this.workingSets = workingSets;
		this.urlString = urlString;
		this.customProjectSetImporter = customProjectSetImporter;
	}

	/**
	 * Operation for importing a Team Project Set file
	 * 
	 * @param context
	 *            a runnable context
	 * @param psfFile
	 *            a psf file name
	 * @param workingSets
	 *            an array of working sets where imported project should be
	 *            added. If a working set doesn't exist it will be created. The
	 *            array cannot be <code>null</code>, pass an empty array if you
	 *            don't want to add projects to any working set.
	 */
	public CustomImportProjectSetOperation(IRunnableContext context, String psfFile,
			IWorkingSet[] workingSets, CustomProjectSetImporter customProjectSetImporter) {
		super(context);
		this.psfFile = psfFile;
		this.workingSets = workingSets;
		this.customProjectSetImporter = customProjectSetImporter;
	}
	
	private void runForStringContent(IProgressMonitor monitor) throws InvocationTargetException{
		IProject[] newProjects = getProjectSetImporter(getShell()).importProjectSetFromString(
				psfFileContents, urlString, getShell(), monitor);
		createWorkingSet(workingSets, newProjects);
	}
	
	private void runForFile(IProgressMonitor monitor) throws InvocationTargetException{
		PsfFilenameStore.getInstance().remember(psfFile);
		IProject[] newProjects = getProjectSetImporter(getShell()).importProjectSet(psfFile,
				getShell(), monitor);
		createWorkingSet(workingSets, newProjects);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException{
		if (psfFileContents != null) {
			runForStringContent(monitor);
		} else {
			runForFile(monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.TeamOperation#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.TeamOperation#getJobName()
	 */
	protected String getJobName() {
		return TeamUIMessages.ImportProjectSetMainPage_jobName;
	}
	
	private void createWorkingSet(IWorkingSet[] workingSets, IProject[] projects) {
		IWorkingSetManager manager = TeamUIPlugin.getPlugin().getWorkbench().getWorkingSetManager();
		String workingSetName;
		for (int i = 0; i < workingSets.length; i++) {
			workingSetName = workingSets[i].getName();
			IWorkingSet oldSet = manager.getWorkingSet(workingSetName);
			if (oldSet == null) {
				IWorkingSet newSet = manager.createWorkingSet(workingSetName, projects);
				manager.addWorkingSet(newSet);
			} else {
				//don't overwrite the old elements
				IAdaptable[] tempElements = oldSet.getElements();
				IAdaptable[] adaptedProjects = oldSet.adaptElements(projects);
				IAdaptable[] finalElementList = new IAdaptable[tempElements.length + adaptedProjects.length];
				System.arraycopy(tempElements, 0, finalElementList, 0, tempElements.length);
				System.arraycopy(adaptedProjects, 0,finalElementList, tempElements.length, adaptedProjects.length);
				oldSet.setElements(finalElementList);
			}	
		}
	}
	
	protected CustomProjectSetImporter getProjectSetImporter(Shell shell) {
		if(customProjectSetImporter == null) {
			customProjectSetImporter = new CustomProjectSetImporter(shell);
		}
		return customProjectSetImporter;
	}
}