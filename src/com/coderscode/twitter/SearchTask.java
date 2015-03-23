package com.coderscode.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;

/**
 * An {@link AsyncTask} used to perform a basic search using the Twitter API. It
 * pulls the data down from the web, and passes it back to the invoking
 * {@link TwitterActivity} to be displayed in the appropriate list view.
 * <p>
 * This example uses the {@link HttpURLConnection}, rather than the Apache HTTP
 * client, as this is now the recommended approach for Android applications.
 * See:
 * </p>
 * <p>
 * <a href=
 * "http://android-developers.blogspot.co.uk/2011/09/androids-http-clients.html"
 * > http://android-developers.blogspot.co.uk/2011/09/androids-http-clients.html
 * </a>
 * </p>
 * <p>
 * The search API is described at:
 * </p>
 * <p>
 * <a href="https://dev.twitter.com/docs/using-search">
 * https://dev.twitter.com/docs/using-search</a>
 * </p>
 * 
 * @author Graeme Sutherland
 */
public class SearchTask extends AsyncTask <String, Void, String []>
{
	/** Error code from the Twitter API. */
	private final static String ERROR = "error"; //$NON-NLS-1$

	/** Results field in the response from the Twitter API. */
	private final static String RESULTS = "results"; //$NON-NLS-1$

	/** Text of the Tweet in the JSON returned by the Twitter API. */
	private final static String TEXT = "text"; //$NON-NLS-1$

	/** {@link Uri} pointing at the Twitter search API. */
	private final Uri uri = Uri.parse ("http://search.twitter.com/search.json"); //$NON-NLS-1$

	/** The {@link TwitterActivity} used to display the results of the search. */
	private final TwitterActivity activity;

	/**
	 * Constructor. Creates an instance of the search task.
	 * 
	 * @param anActivity The {@link TwitterActivity} to pass the results to
	 */
	public SearchTask (final TwitterActivity anActivity)
	{
		super ();

		this.activity = anActivity;
	}

	/**
	 * Processes the search request.
	 * <p>
	 * This is invoked on a separate thread, and so doesn't block the UI from
	 * functioning whilst it completes. The result is passed back to the main
	 * thread via the <code>onPostExecute</code> method.
	 * </p>
	 * 
	 * @param params The search parameters to pass onto Twitter
	 * @return A {@link String} array containing the requested Tweets
	 */
	@Override
	protected String [] doInBackground (final String... params)
	{
		HttpURLConnection connection = null;
		String result[] = null;

		try
		{
			final URL url = buildUrl (params);
			connection = (HttpURLConnection) url.openConnection ();

			connection.connect ();

			result = getTweets (handleResponse (connection.getInputStream ()));
		}

		catch (final Exception e)
		{
			result = new String [] { e.getMessage () };
			e.printStackTrace ();
		}

		finally
		{
			if (connection != null)
			{
				connection.disconnect ();
			}
		}

		return result;
	}

	/**
	 * Handles the response from the task. This is performed in the main UI
	 * thread.
	 * 
	 * @param results An array containing the Tweets received from the web
	 */
	@Override
	protected void onPostExecute (final String [] results)
	{
		this.activity.displayTweets (results);
	}

	/**
	 * Builds the {@link URL} used to access the Twitter search API.
	 * 
	 * @param params The search terms to be incorporated into the query
	 * @return The {@link URL} to access the Twitter API through
	 * @throws MalformedURLException If the URL is incorrectly formated
	 */
	private URL buildUrl (final String... params) throws MalformedURLException
	{
		final Builder builder = this.uri.buildUpon ();

		if (params.length > 0)
		{
			final StringBuilder query = new StringBuilder ();

			for (int i = 0; i < params.length; i++)
			{
				query.append (params[i]);

				if (i < params.length - 1)
				{
					query.append (' ');
				}
			}

			builder.appendQueryParameter ("q", query.toString ()); //$NON-NLS-1$
		}

		return new URL (builder.build ().toString ());
	}

	/**
	 * Reads the response from the HTTP connection, and parses it into a
	 * {@link String}.
	 * 
	 * @param stream The {@link InputStream} from the HTTP connection
	 * @return A {@link String} containing the server's response
	 * @throws IOException If there is an IO error
	 */
	private String handleResponse (final InputStream stream) throws IOException
	{
		final BufferedReader reader = new BufferedReader (
				new InputStreamReader (stream));
		final StringBuffer buffer = new StringBuffer ();

		try
		{
			String line;

			while ((line = reader.readLine ()) != null)
			{
				buffer.append (line);
			}
		}

		finally
		{
			reader.close ();
		}

		return buffer.toString ();
	}

	/**
	 * Pulls the text of individual Tweets out of the JSON results returned by
	 * the Twitter API.
	 * 
	 * @param response The response from the Twitter web service
	 * @return An array of type {@link String} containing the Tweets
	 * @throws JSONException If the parsing of the JSON fails
	 */
	private String [] getTweets (final String response) throws JSONException
	{
		final JSONObject object = new JSONObject (response);
		final String [] result;

		if (object.has (ERROR))
		{
			result = new String [] { object.getString (ERROR) };
		}

		else
		{
			result = extractTweets (object);
		}

		return result;
	}

	/**
	 * Extracts the Tweets encoded in the response from the Twitter API.
	 * 
	 * @param object The {@link JSONObject} received from Twitter
	 * @return A {@link String} array containing the Tweets
	 * @throws JSONException
	 */
	private String [] extractTweets (final JSONObject object)
			throws JSONException
	{
		final ArrayList <String> tweets = new ArrayList <String> ();
		final JSONArray results = object.getJSONArray (RESULTS);
		final int length = results.length ();

		for (int i = 0; i < length; i++)
		{
			final JSONObject result = results.getJSONObject (i);
			final String tweet = result.getString (TEXT);

			tweets.add (tweet);
		}

		return tweets.toArray (new String [tweets.size ()]);
	}
}
