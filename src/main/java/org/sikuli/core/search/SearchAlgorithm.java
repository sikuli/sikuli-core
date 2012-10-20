package org.sikuli.core.search;

public interface SearchAlgorithm<T extends Match> {	
	void execute();
	/**
	 * @return next match or null if there is no such match
	 */
	T fetchNext();
}