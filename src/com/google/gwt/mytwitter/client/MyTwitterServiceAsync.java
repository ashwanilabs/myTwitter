package com.google.gwt.mytwitter.client;

import com.google.gwt.mytwitter.shared.Tweet;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MyTwitterServiceAsync {

	void getTweetList(AsyncCallback<String> callback);
	void getTweet(String tweetId, AsyncCallback<Tweet> callback);
	void createTweet(Tweet tweet, AsyncCallback<Boolean> callback);
	void removeTweet(Tweet tweet, AsyncCallback<Boolean> callback);
	void commitTweet(Tweet tweet, String commentId, AsyncCallback<Boolean> callback);
	void removeComment(Tweet tweet, String commentId, AsyncCallback<Boolean> callback);

}
