package edu.rit.csh.googlebooks;

public interface APIQuery {
	/**
	 * @return a String URL target that when queried will return results for this APIQuery
	 */
	public String getRequest();
}
