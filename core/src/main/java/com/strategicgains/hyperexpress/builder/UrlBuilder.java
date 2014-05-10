/*
    Copyright 2014, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.strategicgains.hyperexpress.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.strategicgains.hyperexpress.util.MapStringFormat;
import com.strategicgains.hyperexpress.util.Strings;

/**
 * Build URL strings from a URL pattern, binding URL tokens to actual values.
 * This class is not thread safe, as the token bindings are fully mutable.
 * <p/>
 * There is also building of dynamic query strings. By calling addQuery() with a
 * tokenized query-string segment (e.g. "limit={limit}"), the builder will
 * include query-string arguments that get fully populated. If there is no
 * parmater bound to a particular query-string segment, it is not included in
 * the constructed URL string.
 * 
 * @author toddf
 * @since Jan 10, 2014
 */
public class UrlBuilder
{
	private static final MapStringFormat FORMATTER = new MapStringFormat();

	private String baseUrl;
	private String urlPattern;
	private List<String> queries = new ArrayList<String>();
	private Map<String, String> tokenBindings = new HashMap<String, String>();

	/**
	 * Create an empty UrlBuilder, with no URL pattern. Using this constructor
	 * mandates that you MUST use the build(String) form of build instead of the
	 * parameterless, build(), as the latter will throw IllegalStateException in
	 * this state.
	 */
	public UrlBuilder()
	{
		super();
	}

	/**
	 * Create a new UrlBuilder, passing in the URL pattern in which to
	 * substitute tokens. This URL pattern may represent the entire URL or just
	 * the path portion (relative path).
	 * <p/>
	 * The URL pattern is templated, in that it contains tokens to be later
	 * substituted for actual values. The tokens are delimited with beginning
	 * and trailing curly-braces (e.g. '{token}').
	 * <p/>
	 * If used in conjunction with baseUrl(), this URL pattern must be just the
	 * path portion of the URL and should be prefixed with a leading slash
	 * ('/').
	 * <p/>
	 * For example: '/users/{userId}' or
	 * 'http://www.example.com/api/users/{userId}'
	 * 
	 * @param urlPattern a URL path with optional tokens of the form '{tokenName}'
	 */
	public UrlBuilder(String urlPattern)
	{
		super();
		this.urlPattern = urlPattern;
	}

	/**
	 * Set the prefix portion of the URL which is to be pre-pended to the URL
	 * pattern.
	 * <p/>
	 * Optional, as the URL pattern may contain everything. However, this is
	 * provided as a convenience so consumers don't have to perform their own
	 * concatenation to pass in the entire URL pattern string.
	 * <p/>
	 * For example: 'http://www.example.com:8080'
	 * 
	 * @param baseUrl the string that will prefix the URL pattern
	 * @return this UrlBuilder instance to facilitate method chaining.
	 */
	public UrlBuilder baseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
		return this;
	}

	/**
	 * Add an optional query-string segment to this UrlBuilder. If all of the
	 * tokens in the query-string are bound, the segment is included in the
	 * generated URL string during build().  However, if there are unbound
	 * tokens in the resulting query-string segment, it is not included in the
	 * generated URL string.
	 * <p/>
	 * Do not include any question mark ("?") or ampersand ("&") in the query-string
	 * segment.
	 * 
	 * @param query a query-string segment to optionally include.
	 * @return this UrlBuilder instance to facilitate method chaining.
	 */
	public UrlBuilder withQuery(String query)
	{
		queries.add(query);
		return this;
	}

	/**
	 * Remove the query-string segments from this UrlBuilder.
	 */
	public void clearQueries()
	{
		queries.clear();
	}

	/**
	 * Set a value to be substituted for a token in the URL pattern. While
	 * tokens in the URL pattern are delimited with curly-braces, the token name
	 * does not contain the braces. The value is any URL-safe string value.
	 * 
	 * @param tokenName the name of a token in the URL pattern.
	 * @param value the URL-safe string value to substitute for the token name in the URL pattern.
	 * @return this UrlBuilder instance to facilitate method chaining.
	 */
	public UrlBuilder bindToken(String tokenName, String value)
	{
		if (value == null)
		{
			tokenBindings.remove(tokenName);
		}
		else
		{
			tokenBindings.put(tokenName, value);
		}

		return this;
	}

	/**
	 * Remove all the token bindings.
	 */
	public void clearTokenBindings()
	{
		tokenBindings.clear();
	}

	/**
	 * 'Unbind' a named substitution value from a token name.
	 * 
	 * @param tokenName the name of a previously-bound token name.
	 */
	public void removeBinding(String tokenName)
	{
		tokenBindings.remove(tokenName);
	}

	/**
	 * Build a new URL from the URL pattern, utilizing the current token
	 * bindings. If any tokens are not bound, they will not be substituted,
	 * remaining as URL template parameters in the output string.
	 * 
	 * @return a URL string utilizing the current token bindings.
	 * @throws IllegalStateException if no URL pattern is present (it is null).
	 */
	public String build()
	{
		return build(buildFullUrlPattern());
	}

	/**
	 * Build a new URL from the give URL pattern, utilizing the current token
	 * bindings.
	 * 
	 * @param urlPattern a URL pattern to bind, instead of using the one associated 
	 * with this builder. May not be null.
	 * @return a URL string.
	 */
	public String build(String urlPattern)
	{
		String url = FORMATTER.format(urlPattern, tokenBindings);
		return appendQueryString(url);
	}

	/**
	 * Sometimes, it is useful to bind *most* (all but one) of the tokens in a
	 * URL then, given a collection of IDs, build a collection of URLs for each
	 * of those IDs, binding them in individually.
	 * <p/>
	 * This method builds a collection of URL strings, one for each ID given in
	 * the 'ids' array.
	 * 
	 * @param tokenName the name of a token in the URL pattern.
	 * @param values variable-length array of URL-safe string values to substitute
	 * for the token name in the URL pattern.
	 * @return a Collection of URL strings, matching the size of the 'values'
	 * array.
	 */
	public Collection<String> build(String tokenName, String... values)
	{
		return build(tokenName, Arrays.asList(values));
	}

	/**
	 * This method builds a collection of URL strings, one for each ID given in
	 * the 'ids' collection.
	 * 
	 * @param tokenName the name of a token in the URL pattern.
	 * @param values variable-length array of URL-safe string values to substitute
	 * for the token name in the URL pattern.
	 * @return a Collection of URL strings, matching the size of the 'values'
	 * collection.
	 */
	public Collection<String> build(String tokenName, Collection<String> values)
	{
		if (values == null) return null;

		Collection<String> urls = new ArrayList<String>(values.size());
		String href = buildFullUrlPattern();

		for (String id : values)
		{
			bindToken(tokenName, id);
			urls.add(build(href));
		}

		removeBinding(tokenName);
		return urls;
	}

	private String buildFullUrlPattern()
	{
		if (urlPattern == null)
		    throw new IllegalStateException("Null URL pattern");

		String path = FORMATTER.format(urlPattern, tokenBindings);
		return (baseUrl == null ? path : baseUrl + path);
	}

	private String appendQueryString(String url)
	{
		if (queries.isEmpty()) return url;

		StringBuilder sb = new StringBuilder(url);
		boolean hasQuery = false;

		for (String query : queries)
		{
			String boundQuery = FORMATTER.format(query, tokenBindings);

			if (!Strings.hasToken(boundQuery))
			{
				if (hasQuery)
				{
					sb.append("&");
				}
				else
				{
					sb.append("?");
				}

				sb.append(boundQuery);
				hasQuery = true;
			}
		}

		return (hasQuery ? sb.toString() : url);
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append(this.getClass().getSimpleName());
		s.append(": [baseUrl: ");
		s.append(baseUrl == null ? "null" : baseUrl);
		s.append(", urlPattern: ");
		s.append(urlPattern == null ? "null" : urlPattern);
        printBindings(s);
		printQueryStrings(s);
		return s.toString();
	}

	private void printBindings(StringBuilder s)
    {
	    s.append(", bindings: {");
		boolean isFirst = true;

		for (Entry<String, String> entry : tokenBindings.entrySet())
		{
			if (!isFirst)
			{
				s.append(", ");
			}
			else
			{
				isFirst = false;
			}

			s.append(entry.getKey());
			s.append("=");
			s.append(entry.getValue());
		}
    }

	private void printQueryStrings(StringBuilder s)
    {
	    boolean isFirst = true;
		s.append("}, query-strings: {");

		for (String query : queries)
		{
			if (!isFirst)
			{
				s.append(", ");
			}
			else
			{
				isFirst = false;
			}

			s.append(query);
		}

		s.append("}]");
    }
}
