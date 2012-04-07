package de.abg.reichert.joerg.eclipsetools.workingsets.internal;

public class WorkingSetStatus {
	boolean replaceAll = false;
	boolean mergeAll = false;
	boolean skipAll = false;

	public boolean isReplaceAll() {
		return replaceAll;
	}

	public void setReplaceAll(boolean replaceAll) {
		this.replaceAll = replaceAll;
	}

	public boolean isMergeAll() {
		return mergeAll;
	}

	public void setMergeAll(boolean mergeAll) {
		this.mergeAll = mergeAll;
	}

	public boolean isSkipAll() {
		return skipAll;
	}

	public void setSkipAll(boolean skipAll) {
		this.skipAll = skipAll;
	}
}
