package com.lumpofcode.crawler;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.solr.common.SolrInputDocument;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Ed
 * 
 * Web Crawler for documents in www.babycenter.com
 *
 */
public final class BabyCrawl extends WebCrawler
{

	private final static Pattern	FILTERS	= Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
													+ "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf"
													+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	private final String			thisRoot;
	private final SimpleSolrServer thisSolrServer;

	/**
	 * construct with defaults
	 */
	public BabyCrawl()
	{
		super();
		thisRoot = Controller.ROOT;
		thisSolrServer = Controller.getSolrServer();
	}

	/**
	 * Construct with a given server and document root.
	 * 
	 * TODO: Autowire the parameters so we can inject them.
	 * 
	 * @param theSolrServer
	 * @param theRoot
	 */
	public BabyCrawl(final SimpleSolrServer theSolrServer, final String theRoot)
	{
		super();
		
		if(null == theSolrServer) throw new IllegalArgumentException();
		if ((null == theRoot) || theRoot.isEmpty()) throw new IllegalArgumentException();
		
		thisSolrServer = theSolrServer;
		thisRoot = theRoot;
	}

	/**
	 * You should implement this function to specify whether the given url should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url)
	{
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && href.startsWith(thisRoot);
	}

	/**
	 * This function is called when a page is fetched and ready to be processed by your program.
	 */
	@Override
	public void visit(Page page)
	{
		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();
		String domain = page.getWebURL().getDomain();
		String path = page.getWebURL().getPath();
		String subDomain = page.getWebURL().getSubDomain();
		String parentUrl = page.getWebURL().getParentUrl();
		String anchor = page.getWebURL().getAnchor();

		System.out.println("Docid: " + docid);
		System.out.println("URL: " + url);
		System.out.println("Domain: '" + domain + "'");
		System.out.println("Sub-domain: '" + subDomain + "'");
		System.out.println("Path: '" + path + "'");
		System.out.println("Parent page: " + parentUrl);
		System.out.println("Anchor text: " + anchor);
		System.out.println("ContentType: " + page.getContentType());


		//
		// submit to SOLR server
		//
		submit(page);

		Header[] responseHeaders = page.getFetchResponseHeaders();
		if (responseHeaders != null)
		{
			System.out.println("Response headers:");
			for (Header header : responseHeaders)
			{
				System.out.println("\t" + header.getName() + ": " + header.getValue());
			}
		}

		System.out.println("=============");
	}

	/**
	 * Submit a document to the solr server.
	 * 
	 * @param thePage may be of any document type.
	 */
	private void submit(final Page thePage)
	{
		if(null == thePage) throw new IllegalArgumentException();
		
		final WebURL theWebUrl = thePage.getWebURL();
		final int theDocId = theWebUrl.getDocid();
		final String theUrl = theWebUrl.getURL();
		final String theContentType = thePage.getContentType();

		//
		// submit to SOLR server as a Solr document
		//
		final SolrInputDocument theDocument = new SolrInputDocument();
		theDocument.addField("id", theDocId);
		theDocument.addField("url", theUrl);
		theDocument.addField("content_type", theContentType);	
		theDocument.addField("category", categoryFromContentType(theContentType));
		
		if(thePage.getParseData() instanceof HtmlParseData)
		{
			final HtmlParseData theHtmlParseData = (HtmlParseData) thePage.getParseData();
			final String theTitle = theHtmlParseData.getTitle();
			final String theText = theHtmlParseData.getText();
			final String theHtml = theHtmlParseData.getHtml();
			final List<WebURL> theLinks = theHtmlParseData.getOutgoingUrls();
			
			System.out.println("HTML Resource");
			System.out.println("Text length: " + theText.length());
			System.out.println("Html length: " + theHtml.length());
			System.out.println("Number of outgoing links: " + theLinks.size());
			System.out.println("Title: " + theTitle);
			// System.out.println("Text: " + text);
			
				
			theDocument.addField("title", theTitle);
			theDocument.addField("text", theText);
		}
		else if(thePage.getParseData() instanceof TextParseData)
		{
			final TextParseData theParseData = (TextParseData)thePage.getParseData();
											
			System.out.println("Text Resource");

			theDocument.addField("text", theParseData.getTextContent());
		}
		else //if(thePage.getParseData() instanceof BinaryParseData)
		{
//				AutoDetectParser theAutoParser = new AutoDetectParser();
//				final ContentHandler theBodyHandler = new BodyContentHandler();
//				final Metadata theMetaData = new Metadata();
//				final ParseContext theContext = new ParseContext();
//				theAutoParser.parse(new ByteArrayInputStream(thePage.), theBodyHandler, theContext);
			
			System.out.println("Binary Resource");
		}
		
		//
		// add the document to the server
		//
		thisSolrServer.add(theDocument);
	}
	
	private String categoryFromContentType(final String theContentType)
	{
		if((null != theContentType) && !theContentType.isEmpty())
		{
			final int theSlash = theContentType.indexOf('/');
			return (theSlash >= 0) ? theContentType.substring(0, theSlash) : theContentType;
		}
		return "";
	}
}
