package de.abg.reichert.joerg.eclipsetools.workingsets;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.wizards.ImportProjectSetMainPage;
import org.eclipse.team.internal.ui.wizards.PsfFilenameStore;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.xml.sax.SAXException;

public class CustomProjectSetImportWizard extends Wizard implements IImportWizard {
	ImportProjectSetMainPage mainPage;
	private CustomImportProjectSetOperation customImportProjectSetOperation = null;
	private CustomProjectSetImporter customProjectSetImporter = null;

	public CustomProjectSetImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(TeamUIMessages.ProjectSetImportWizard_Project_Set_1); 
	}
	
	public void addPages() {
		mainPage = new ImportProjectSetMainPage("projectSetMainPage", TeamUIMessages.ProjectSetImportWizard_Import_a_Project_Set_3, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_PROJECTSET_IMPORT_BANNER)); //$NON-NLS-1$ 
		addPage(mainPage);
	}

	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			CustomImportProjectSetOperation op;
			if (mainPage.getInputType() == ImportProjectSetMainPage.InputType_URL) {
				String psfContent = mainPage.getURLContents();
				if(psfContent==null){
					return false;
				}
				op = getImportProjectSetOperation(
						mainPage.isRunInBackgroundOn() ? null : getContainer(),
						psfContent, mainPage.getUrl(), mainPage.getWorkingSets());
			} else {
				op = getImportProjectSetOperation(
						mainPage.isRunInBackgroundOn() ? null : getContainer(),
						mainPage.getFileName(), mainPage.getWorkingSets());
			}
			op.run();
			result[0] = true;
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof TeamException) {
				ErrorDialog.openError(getShell(), null, null, ((TeamException)target).getStatus());
				return false;
			}
			if (target instanceof RuntimeException) {
				throw (RuntimeException)target;
			}
			if (target instanceof Error) {
				throw (Error)target;
			}
			if (target instanceof SAXException) {
			    ErrorDialog.openError(getShell(), null, null, new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(TeamUIMessages.ProjectSetImportWizard_2, new String[] { target.getMessage() }), target)); 
			    return false;
			}
			ErrorDialog.openError(getShell(), null, null, new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(TeamUIMessages.ProjectSetImportWizard_3, new String[] { target.getMessage() }), target)); 
		}
		return result[0];
	}
		
	private CustomImportProjectSetOperation getImportProjectSetOperation(
			IWizardContainer iWizardContainer, String psfContent, String url,
			IWorkingSet[] workingSets) {
		if(customImportProjectSetOperation == null) {
			customImportProjectSetOperation = new CustomImportProjectSetOperation(iWizardContainer, psfContent, url,
				workingSets, getCustomProjectSetImporter());
		}
		return customImportProjectSetOperation;
	}
	
	private CustomImportProjectSetOperation getImportProjectSetOperation(
			IWizardContainer iWizardContainer, String fileName,
			IWorkingSet[] workingSets) {
		if(customImportProjectSetOperation == null) {
			customImportProjectSetOperation = new CustomImportProjectSetOperation(iWizardContainer, fileName,
					workingSets, getCustomProjectSetImporter());
		}
		return customImportProjectSetOperation;
	}

	private CustomProjectSetImporter getCustomProjectSetImporter() {
		if(customProjectSetImporter == null) {
			customProjectSetImporter = new CustomProjectSetImporter(getShell());
		}
		return customProjectSetImporter;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// The code that finds "selection" is broken (it is always empty), so we
		// must dig for the selection in the workbench.
		PsfFilenameStore.getInstance().setDefaultFromSelection(workbench);
	}
}