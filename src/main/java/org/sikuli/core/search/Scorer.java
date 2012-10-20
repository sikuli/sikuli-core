package org.sikuli.core.search;

public abstract class Scorer {
	final private Similarity similarity;
	
	protected Scorer(Similarity similarity){
		this.similarity = similarity;
	}
	//  Returns the score of the current document matching the query.
	abstract public float score();
	
	public Similarity getSimilarity() {
		return similarity;
	}
}