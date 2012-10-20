package org.sikuli.core.search;

import java.awt.Rectangle;

public class SearchAlgorithmOutput<T extends Match>{
	final private T match;
	final private Scorer scorer;
	public SearchAlgorithmOutput(T match, Scorer scorer) {
		super();
		this.match = match;
		this.scorer = scorer;
	}
	
	public SearchAlgorithmOutput(T match, final float score) {
		super();
		this.match = match;
		this.scorer = new Scorer(null){
			@Override
			public float score() {
				return score;
			}			
		};
	}

	
	public Scorer getScorer() {
		return scorer;
	}

	public T getMatch() {
		return match;
	}	
}