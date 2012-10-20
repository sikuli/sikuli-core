package org.sikuli.core.search;

import java.util.List;
import com.google.common.collect.Lists;

public class TopMatchesCollector<T extends Match> extends Collector<T> {
	
	final private List<T> matches = Lists.newArrayList();
	final private int numberOfMatchesToCollect;

	TopMatchesCollector(int numberOfMatchesToCollect){
		this.numberOfMatchesToCollect = numberOfMatchesToCollect;
	}
	
	public List<T> topMatches(){		
//		TopMatches<T> topMatches = new TopMatches<T>();
//		topMatches.matches = Lists.newArrayList(matches);
//		topMatches.totalHits = matches.size();
		return matches;
	}
	
	public boolean hasEnough(){
		return matches.size() == numberOfMatchesToCollect;
	}

	@Override
	public void collect(T m) {
		matches.add(m);
	}

}