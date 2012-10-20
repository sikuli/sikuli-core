package org.sikuli.core.search.index;



import org.sikuli.core.search.ImageQuery;
import org.sikuli.core.search.Query;
import org.sikuli.core.search.SearchAlgorithm;
import org.sikuli.core.search.Searcher;

 
public class ImageIndexSearcher extends Searcher<LabeledImageMatch> {
	
	final private ImageIndex index; 

	public ImageIndexSearcher(ImageIndex index) {
		super();
		this.index = index;
	}

	@Override
	protected SearchAlgorithm<LabeledImageMatch> getAlgorithm(Query query) {		
		return index.getAlgorithm(((ImageQuery)query).getImage());
	}

}
