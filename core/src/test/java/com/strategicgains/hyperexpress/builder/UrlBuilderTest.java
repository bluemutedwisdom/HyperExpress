package com.strategicgains.hyperexpress.builder;

import static org.junit.Assert.*;

import org.junit.Test;

import com.strategicgains.hyperexpress.builder.UrlBuilder;

public class UrlBuilderTest
{
	private static final String URL_PATTERN = "/{id}";
	private static final String URL_PATTERN2 = "/{rootId}/{secondaryId}/{id}";

	@Test
	public void shouldBuildSimpleUrl()
	{
		assertEquals("/todd:was,here", new UrlBuilder(URL_PATTERN)
			.bindToken("id", "todd:was,here")
			.build());
	}

	@Test
	public void shouldBuildComplexUrl()
	{
		assertEquals("/something/else/12345", new UrlBuilder(URL_PATTERN2)
			.bindToken("rootId", "something")
			.bindToken("secondaryId", "else")
			.bindToken("id", "12345")
			.build());	
	}

	@Test
	public void shouldBuildMultipleUrls()
	{
		UrlBuilder b = new UrlBuilder(URL_PATTERN2);
		assertEquals("/something/else/12345", b
			.bindToken("rootId", "something")
			.bindToken("secondaryId", "else")
			.bindToken("id", "12345")
			.build());	

		assertEquals("/anything/maybe/54321", b
			.bindToken("rootId", "anything")
			.bindToken("secondaryId", "maybe")
			.bindToken("id", "54321")
			.build());	

		assertEquals("/anything/wonderful/54321", b
			.bindToken("secondaryId", "wonderful")
			.build());	
	}

	@Test
	public void shouldExcludeQueryString()
	{
		assertEquals("/something/else/12345", new UrlBuilder(URL_PATTERN2)
			.addQuery("limit={selfLimit}")
			.addQuery("offset={selfOffset}")
			.bindToken("rootId", "something")
			.bindToken("secondaryId", "else")
			.bindToken("id", "12345")
			.build());	
	}

	@Test
	public void shouldIncludeEntireQueryString()
	{
		assertEquals("/something/else/12345?limit=20&offset=40", new UrlBuilder(URL_PATTERN2)
			.addQuery("limit={selfLimit}")
			.addQuery("offset={selfOffset}")
			.bindToken("rootId", "something")
			.bindToken("secondaryId", "else")
			.bindToken("id", "12345")
			.bindToken("selfLimit", "20")
			.bindToken("selfOffset", "40")
			.build());	
	}

	@Test
	public void shouldIncludeQueryString()
	{
		assertEquals("/something/else/12345?offset=40", new UrlBuilder(URL_PATTERN2)
			.addQuery("limit={selfLimit}")
			.addQuery("offset={selfOffset}")
			.bindToken("rootId", "something")
			.bindToken("secondaryId", "else")
			.bindToken("id", "12345")
			.bindToken("selfOffset", "40")
			.build());	
	}
}
