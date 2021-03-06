package de.abg.jreichert.junit4runner;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.BasicElementLabels;
import org.eclipse.jdt.internal.junit.Messages;
import org.eclipse.jdt.internal.junit.wizards.JUnitWizard;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;

/**
 * A wizard for creating test suites.
 */
public class NewTestSuite4CreationWizard extends JUnitWizard {

	private NewTestSuite4WizardPage fPage;

	public NewTestSuite4CreationWizard() {
		super();
		setWindowTitle(WizardMessages.Wizard_title_new_testsuite);
		initDialogSettings();
	}

	/*
	 * @see Wizard#createPages
	 */
	public void addPages() {
		super.addPages();
		fPage = new NewTestSuite4WizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}

	/*
	 * @see Wizard#performFinish
	 */
	public boolean performFinish() {
		IPackageFragment pack = fPage.getPackageFragment();
		String filename = fPage.getTypeName() + ".java"; //$NON-NLS-1$
		ICompilationUnit cu = pack.getCompilationUnit(filename);
		if (cu.exists()) {
			IEditorPart cu_ep = EditorUtility.isOpenInEditor(cu);
			if (cu_ep != null && cu_ep.isDirty()) {
				boolean saveUnsavedChanges = MessageDialog
						.openQuestion(
								fPage.getShell(),
								WizardMessages.NewTestSuiteWiz_unsavedchangesDialog_title,
								Messages.format(
										WizardMessages.NewTestSuiteWiz_unsavedchangesDialog_message,
										BasicElementLabels
												.getResourceName(filename)));
				if (saveUnsavedChanges) {
					try {
						getContainer()
								.run(false, false, getRunnableSave(cu_ep));
					} catch (Exception e) {
						Activator.log(e);
					}
				}
			}
			IType suiteType = cu.getType(fPage.getTypeName());
			boolean hasTestAnnotatedMethod = false;
			try {

				for (IMethod method : suiteType.getMethods()) {
					for (IAnnotation anno : method.getAnnotations()) {
						if ("Test".equals(anno.getElementName())) {
							hasTestAnnotatedMethod = true;
							break;
						}
					}
					if (hasTestAnnotatedMethod) {
						break;
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			if(hasTestAnnotatedMethod) {
				MessageDialog.openConfirm(this.getShell(), "title2", "message");
			}
			IMethod suiteMethod = suiteType.getMethod("suite", new String[] {}); //$NON-NLS-1$
			if (suiteMethod.exists()) {
				try {
					ISourceRange range = suiteMethod.getSourceRange();
					IBuffer buf = cu.getBuffer();
					String originalContent = buf.getText(range.getOffset(),
							range.getLength());
					if (UpdateTestSuite4
							.getTestSuiteClassListRange(originalContent) == null) {
						cannotUpdateSuiteError();
						return false;
					}
				} catch (JavaModelException e) {
					Activator.log(e);
					return false;
				}
			}
		}

		if (finishPage(fPage.getRunnable())) {
			if (!fPage.hasUpdatedExistingClass())
				postCreatingType();
			return true;
		}

		return false;
	}

	private void cannotUpdateSuiteError() {
		MessageDialog
				.openError(
						getShell(),
						WizardMessages.NewTestSuiteWizPage_cannotUpdateDialog_title,
						Messages.format(
								WizardMessages.NewTestSuiteWizPage_cannotUpdateDialog_message,
								new String[] {
										NewTestSuite4WizardPage.START_MARKER,
										NewTestSuite4WizardPage.END_MARKER }));

	}

	protected void postCreatingType() {
		IType newClass = fPage.getCreatedType();
		if (newClass == null)
			return;
		ICompilationUnit cu = newClass.getCompilationUnit();
		IResource resource = cu.getResource();
		if (resource != null) {
			selectAndReveal(resource);
			openResource(resource);
		}
	}

	public NewTestSuite4WizardPage getPage() {
		return fPage;
	}

	protected void initializeDefaultPageImageDescriptor() {
		setDefaultPageImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "wizban/newtest_wiz.png")); //$NON-NLS-1$
	}

	public IRunnableWithProgress getRunnableSave(final IEditorPart cu_ep) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				if (monitor == null) {
					monitor = new NullProgressMonitor();
				}
				cu_ep.doSave(monitor);
			}
		};
	}
}