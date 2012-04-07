package de.abg.reichert.joerg.eclipsetools.workingsets;

import org.eclipse.team.internal.ui.ProjectSetImporter;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.wizards.ImportProjectSetMainPage;
import org.eclipse.team.internal.ui.wizards.PsfUrlStore;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

public class Main {

	public static void main(String[] args) {
		org.eclipse.ui.IWorkingSet ws = null;
		IWorkingSetManager manager = WorkbenchPlugin.getDefault()
				.getWorkingSetManager();
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
				.getWorkingSetRegistry();
		WorkingSetDescriptor descriptor = null;
		registry.addWorkingSetDescriptor(descriptor);
		
		ImportProjectSetMainPage ip;
		
		PsfUrlStore.getInstance();
		
		ProjectSetImporter importer;
		
	  	IWorkingSetManager wsManager = TeamUIPlugin.getPlugin().getWorkbench().getWorkingSetManager();
	  	
	  	WorkbenchPlugin.getDefault()
		.getImportWizardRegistry().getRootCategory();

	}
}
