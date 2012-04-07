package de.abg.reichert.joerg.eclipsetools.workingsets.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

public class WorkingSetHelper {

	public void mergeWorkingSets(IWorkingSet newWs, IWorkingSet oldWs) {
		IAdaptable[] oldElements = oldWs.getElements();
		IAdaptable[] newElements = newWs.getElements();

		Set combinedElements = new HashSet();
		combinedElements.addAll(Arrays.asList(oldElements));
		combinedElements.addAll(Arrays.asList(newElements));

		oldWs.setElements((IAdaptable[]) combinedElements
				.toArray(new IAdaptable[0]));
	}

	public void replaceWorkingSet(IWorkingSetManager wsManager,
			IWorkingSet newWs, IWorkingSet oldWs) {
		if (oldWs != null)
			wsManager.removeWorkingSet(oldWs);
		wsManager.addWorkingSet(newWs);
	}
	
	void handleExistingWorkingSet(IWorkingSetManager wsManager, IWorkingSet oldWs, IWorkingSet newWs, WorkingSetStatus status, Shell shell) {
		// a working set with the same name has been found
		final AdviceDialog dialog = new AdviceDialog(shell, newWs);

		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		handleDialogReturnType(wsManager, oldWs, newWs, status, dialog);
	}

	private void handleDialogReturnType(IWorkingSetManager wsManager,
			IWorkingSet oldWs, IWorkingSet newWs, WorkingSetStatus status,
			final AdviceDialog dialog) {
		switch (dialog.getReturnCode()) {
		case 0: // overwrite
			replaceWorkingSet(wsManager, newWs, oldWs);
			status.setReplaceAll(dialog.isApplyToAll());
			break;
		case 1: // combine
			mergeWorkingSets(newWs, oldWs);
			status.setMergeAll(dialog.isApplyToAll());
			break;
		case 2: // skip
			status.setSkipAll(dialog.isApplyToAll());
			break;
		case 3: // cancel
		default:
			throw new OperationCanceledException();
		}
	}	
}
