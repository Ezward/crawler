package com.lumpofcode.crawler;

import org.apache.log4j.PropertyConfigurator;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * @author Ed
 *
 * Master controller for the crawling process.
 * 
 */
public final class Controller
{
	public static final String ROOT = "http://www.babycenter.com/";	// root of site to crawl
	private static final String TEMP = "C:/Users/Ed/temp/crawl/root";	// temporary folder "/data/crawl/root";
	private static final int DEPTH = 3;			// depth of search
	private static final int DELAY = 1000;		// politeness delay between requests to a given server
	private static final int MAX_PAGES = 25;	// maximum pages to fetch.
	private static final int NUMBER_OF_CRAWLERS = 3;	// number of crawler instances
	
	private static SimpleSolrServer thisSolrServer = null;
	
	/**
	 * Lazy initialization of the server.
	 * 
	 * @return the initialized server.
	 */
	public static final SimpleSolrServer getSolrServer()
	{
		if(null == thisSolrServer)
		{
			//
			// create a connection to the Solr server
			//
			thisSolrServer = new SimpleSolrServer(60000);
		}
		return thisSolrServer;
	}
	
	/**
	 * Entry point.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		// Set up a simple configuration that logs on the console.
		//BasicConfigurator.configure();
		PropertyConfigurator.configure("log4j.properties");

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(TEMP);
		
        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(DELAY);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        config.setMaxDepthOfCrawling(DEPTH);

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(MAX_PAGES);

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);

		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first URLs that are fetched and then the crawler starts following links which are found
		 * in these pages
		 */
		controller.addSeed(ROOT);

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code will reach the line after this only when crawling is finished.
		 */
		controller.start(BabyCrawl.class, NUMBER_OF_CRAWLERS);
		
		// commit all documents
		getSolrServer().commit();
	}

}
