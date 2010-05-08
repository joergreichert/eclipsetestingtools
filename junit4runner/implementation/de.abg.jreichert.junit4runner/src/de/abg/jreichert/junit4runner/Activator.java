package de.abg.jreichert.junit4runner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.abg.jreichert.junit4runner"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}
	
	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow workBenchWindow= getActiveWorkbenchWindow();
		if (workBenchWindow == null)
			return null;
		return workBenchWindow.getShell();
	}
	
	/**
	 * Returns the active workbench window
	 *
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (plugin == null)
			return null;
		IWorkbench workBench= plugin.getWorkbench();
		if (workBench == null)
			return null;
		return workBench.getActiveWorkbenchWindow();
	}
}
