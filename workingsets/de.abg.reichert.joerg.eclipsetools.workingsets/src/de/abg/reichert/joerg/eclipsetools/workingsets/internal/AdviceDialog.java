package de.abg.reichert.joerg.eclipsetools.workingsets.internal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.ui.IWorkingSet;

public class AdviceDialog extends MessageDialog {
	private static final String TITLE = TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_title;
	private static final String[] BUTTONS = new String[] {
			TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_replace,
			TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_merge,
			TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_skip,
			IDialogConstants.CANCEL_LABEL };
	private boolean applyToAll;

	public AdviceDialog(Shell parentShell, IWorkingSet newWs) {
		this(parentShell, getTitle(), null, getMsg(newWs),
				MessageDialog.QUESTION, getButtons(), 0);
	}

	private static String getTitle() {
		return TITLE;
	}

	private static String[] getButtons() {
		return BUTTONS;
	}

	private static String getMsg(IWorkingSet newWs) {
		return NLS
				.bind(TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_message,
						newWs.getName());
	}

	public AdviceDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
	}

	protected Control createCustomArea(Composite parent) {
		final Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_applyToAll);
		checkBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setApplyToAll(checkBox.getSelection());
			}
		});
		return checkBox;
	}

	public boolean isApplyToAll() {
		return applyToAll;
	}

	public void setApplyToAll(boolean applyToAll) {
		this.applyToAll = applyToAll;
	}
}