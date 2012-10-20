package org.sikuli.core.search;

import java.awt.Rectangle;

abstract public class Filter<T extends Match> {
	//  provides the documents which should be permitted or prohibited in search results
	abstract public boolean accept(T m);
	public boolean acceptMore(T m){
		return true;
	}
}