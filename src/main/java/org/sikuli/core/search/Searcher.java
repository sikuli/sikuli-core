package org.sikuli.core.search;

import java.util.List;

import com.google.common.collect.Lists;

abstract public class Searcher<T extends Match> {
	
	
	abstract protected SearchAlgorithm<T> getAlgorithm(Query query);
	
	protected void explain(Query query, List<T> outputs){	
	}

	public List<T> search(Query query, Filter<T> filter, int n) {
		TopMatchesCollector<T> results = new TopMatchesCollector<T>(n); 
		search(query, filter, results);
		return results.topMatches();
	}

	public List<T> search(Query query, Filter<T> filter, int n, SearchAlgorithm<T> alg) {
		TopMatchesCollector<T> results = new TopMatchesCollector<T>(n); 
		search(query, filter, results, alg);
		return results.topMatches();
	}
	
	public List<T> search(Query query, int n) {
		TopMatchesCollector<T> results = new TopMatchesCollector<T>(n); 
		search(query, null, results);
		return results.topMatches();
	}

	
	private void search(Query query, Filter<T> filter, Collector<T> collector) {
		search(query, filter, collector, getAlgorithm(query));	
	}	
	
	
	private void search(Query query, Filter<T> filter, Collector<T> collector, SearchAlgorithm<T> alg) {		
		
		alg.execute();
		
		final List<T> matches = Lists.newArrayList();		
		while(!collector.hasEnough()){
			T m = alg.fetchNext();		
			if (filter == null || filter.accept(m)){
				collector.collect(m);				
				matches.add(m);
			}						
			if (filter != null && !filter.acceptMore(m))
				break;			
		}
		explain(query, matches);		
	}	
}