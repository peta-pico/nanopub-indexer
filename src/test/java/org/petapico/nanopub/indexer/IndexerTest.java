package org.petapico.nanopub.indexer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IndexerTest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public IndexerTest( String testName ) {
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite( IndexerTest.class );
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue( true );
	}

}
