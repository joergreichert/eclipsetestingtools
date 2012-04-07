package de.abg.reichert.joerg.eclipsetools.workingsets;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.statushandlers.StatusManager;

public class CustomImportProjectSetAction extends ActionDelegate implements IObjectActionDelegate {

	private IStructuredSelection fSelection;
	private CustomProjectSetImporter customProjectSetImporter = null;
	private CustomImportProjectSetOperation customImportProjectSetOperation = null;

	public void run(IAction action) {
		final Shell shell= Display.getDefault().getActiveShell();
		try {
			new ProgressMonitorDialog(shell).run(true, true, new WorkspaceModifyOperation(null) {
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
					Iterator iterator= fSelection.iterator();
					while (iterator.hasNext()) {
						IFile file = (IFile) iterator.next();
						if (isRunInBackgroundPreferenceOn()) {
							CustomImportProjectSetOperation op = getImportProjectSetOperation(file.getLocation().toString(), getProjectSetImporter(shell), shell);
							op.run();
						} else { 
							getProjectSetImporter(shell).importProjectSet(file.getLocation().toString(), shell, monitor);
						}
					}
				}
			});
		} catch (InvocationTargetException exception) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, TeamUIPlugin.PLUGIN_ID,
							IStatus.ERROR,
							TeamUIMessages.ImportProjectSetAction_0,
							exception.getTargetException()),
					StatusManager.LOG | StatusManager.SHOW);
		} catch (InterruptedException exception) {
		}
	}
	
	protected CustomProjectSetImporter getProjectSetImporter(Shell shell) {
		if(customProjectSetImporter == null) {
			customProjectSetImporter = new CustomProjectSetImporter(shell);
		}
		return customProjectSetImporter;
	}
	
	private CustomImportProjectSetOperation getImportProjectSetOperation(String location,
			CustomProjectSetImporter projectSetImporter, Shell shell) {
		if(customImportProjectSetOperation == null) {
			customImportProjectSetOperation = new CustomImportProjectSetOperation(null, location, new IWorkingSet[0], getProjectSetImporter(shell));
		}
		return customImportProjectSetOperation;
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	private static boolean isRunInBackgroundPreferenceOn() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(
				IPreferenceIds.RUN_IMPORT_IN_BACKGROUND);
	}
}
