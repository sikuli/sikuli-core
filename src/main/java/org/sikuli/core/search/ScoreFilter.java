package org.sikuli.core.search;

public class ScoreFilter<T extends Match> extends Filter<T>{

	double minScore = 0;
	double maxScore = Double.MAX_VALUE;
	
	public ScoreFilter(double minScore, double maxScore){
		this.minScore = minScore;
		this.maxScore = maxScore;
	}
	
	public ScoreFilter(double minScore){
		this.minScore = minScore;
	}
	
	@Override
	public boolean accept(T m) {
		return m.getScore() >= minScore && m.getScore() <= maxScore;
	}
	
	@Override
	public boolean acceptMore(T m){
		// TODO: if scores are decsending
		return m.getScore() >= minScore;
	}
	
}