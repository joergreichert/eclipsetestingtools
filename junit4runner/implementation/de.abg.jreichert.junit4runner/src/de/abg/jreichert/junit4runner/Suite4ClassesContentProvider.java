package de.abg.jreichert.junit4runner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class Suite4ClassesContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object parent) {
		if (!(parent instanceof IPackageFragment))
			return new Object[0];
		IPackageFragment pack = (IPackageFragment) parent;
		if (!pack.exists())
			return new Object[0];
		return getTests(pack).toArray();
	}

	public Set getTests(IPackageFragment pack) {
		try {
			HashSet result = new HashSet();
			new JUnit4TestFinder().findTestsInContainer(pack, result, null);
			return result;
		} catch (CoreException e) {
			Activator.log(e);
			return Collections.EMPTY_SET;
		}
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
