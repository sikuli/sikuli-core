package org.sikuli.core.search;

public abstract class Collector<T> {
	abstract public void collect(T m);
	abstract public boolean hasEnough();
}