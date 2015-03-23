package com.coderscode.twitter;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

/**
 * Provides a basic search service for Twitter.
 * 
 * @author Graeme Sutherland
 */
public class TwitterActivity extends ListActivity
{
	/** ID for the progress dialogue. */
	private final static int PROGRESS_DIALOGUE = 0;

	/** Regular expression to split the search string up on. */
	private final static String SPLIT_REGEX = "\\s"; //$NON-NLS-1$

	/** The {@link ArrayAdapter} used to display the search results. */
	private ArrayAdapter <String> adapter;

	/** The {@link EditText} used to enter the search criteria. */
	private EditText criteria;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedState Persisted data used by the activity when it's restarted
	 */
	@Override
	public void onCreate (final Bundle savedState)
	{
		super.onCreate (savedState);
		setContentView (R.layout.main);

		this.criteria = (EditText) findViewById (R.id.criteria);
		this.adapter = new ArrayAdapter <String> (this,
				android.R.layout.simple_list_item_1);

		getListView ().setAdapter (this.adapter);
	}

	/**
	 * Handles the user clicking on the "Search" button.
	 * 
	 * @param view The {@link View} that was clicked on
	 */
	public void invokeSearch (@SuppressWarnings ("unused") final View view)
	{
		final Editable text = this.criteria.getText ();

		if (text != null)
		{
			final String search = text.toString ();

			if (search != null && !search.isEmpty ())
			{
				final SearchTask task = new SearchTask (this);

				showDialog (PROGRESS_DIALOGUE);

				task.execute (search.split (SPLIT_REGEX));
			}
		}
	}

	/**
	 * Displays the received list of Tweets.
	 * 
	 * @param tweets The strings containing the Tweets to be displayed
	 */
	public void displayTweets (final String... tweets)
	{
		this.adapter.clear ();

		// The results have to be added individually because the addAll ()
		// method wasn't introduced until Honeycomb.
		for (final String tweet : tweets)
		{
			this.adapter.add (tweet);
		}

		this.adapter.notifyDataSetChanged ();

		removeDialog (PROGRESS_DIALOGUE);
	}

	/**
	 * Creates the dialogues used by the Twitter Activity. At the present time
	 * only a progress message is displayed.
	 * 
	 * @param id The ID of the dialogue to be created
	 * @return The newly created dialogue
	 */
	@Override
	protected Dialog onCreateDialog (final int id)
	{
		final Dialog dialogue;

		if (id == PROGRESS_DIALOGUE)
		{
			dialogue = ProgressDialog.show (this,
					getString (R.string.progress),
					getString (R.string.downloading), true);
		}

		else
		{
			dialogue = null;
		}

		return dialogue;
	}
}