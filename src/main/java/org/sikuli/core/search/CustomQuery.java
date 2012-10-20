package org.sikuli.core.search;

abstract public class CustomQuery<S extends Searcher<?>, T extends Match> extends Query{
	public abstract SearchAlgorithm<T> getAlgorithm(S searcher);
}
