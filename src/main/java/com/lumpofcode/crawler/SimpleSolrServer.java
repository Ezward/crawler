package com.lumpofcode.crawler;

import java.io.IOException;
import java.util.Collection;

import org.apache.http.annotation.ThreadSafe;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author Ed
 *
 * Adapter that simplifies and limits the api to the SolrServer instance and makes 
 * it completely thread-safe by hiding non-thread-safe methods.
 */
@ThreadSafe
public final class SimpleSolrServer
{
	private static final String DEFAULT_URL = "http://localhost:8983/solr/";
	private final HttpSolrServer thisServer;
	private final int thisDefaultCommitDelayMs;
	
	/**
	 * Construct using default localhost url.
	 * 
	 * @param theDefaultCommitDelayMs
	 */
	public SimpleSolrServer(final int theDefaultCommitDelayMs)
	{
		this(DEFAULT_URL, theDefaultCommitDelayMs);
	}

	/**
	 * Construct given the server url.
	 * 
	 * @param theServerUrl
	 * @param theDefaultCommitDelayMs
	 */
	public SimpleSolrServer(final String theServerUrl, final int theDefaultCommitDelayMs)
	{
		if((null == theServerUrl) || theServerUrl.isEmpty()) throw new IllegalArgumentException();
		if(theDefaultCommitDelayMs < 0) throw new IllegalArgumentException();
		
		thisServer = new HttpSolrServer(theServerUrl);
		thisDefaultCommitDelayMs = theDefaultCommitDelayMs;
	}
	
	/**
	 * Add a single document to the server.
	 * 
	 * @param theDocument
	 * @return this SolrServer for call chaining purposes;
	 */
	public SimpleSolrServer add(final SolrInputDocument theDocument)
	{
		try
		{
			thisServer.add(theDocument, thisDefaultCommitDelayMs);
		}
		catch (SolrServerException | IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return this;	// for call chaining
	}
	
	/**
	 * Add a collection of documents to the server.
	 * 
	 * @param theDocuments
	 * @return this SolrServer for call chaining purposes;
	 */
	public SimpleSolrServer add(final Collection<SolrInputDocument> theDocuments)
	{
		try
		{
			thisServer.add(theDocuments, thisDefaultCommitDelayMs);
		}
		catch (SolrServerException | IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return this;	// for call chaining
	}
	
	/**
	 * Commit any pending docs on the server.
	 * 
	 * @return this SolrServer for call chaining purposes;
	 */
	public SimpleSolrServer commit()
	{
		try
		{
			thisServer.commit();
		}
		catch (SolrServerException | IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return this;	// for call chaining
	}

}
