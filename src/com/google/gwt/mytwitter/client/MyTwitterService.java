package com.google.gwt.mytwitter.client;

import com.google.gwt.mytwitter.shared.Tweet;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("myTwitterService")
public interface MyTwitterService extends RemoteService {
	String getTweetList() ;
	Tweet getTweet(String tweetId);
	Boolean createTweet(Tweet tweet);
	Boolean removeTweet(Tweet tweet);
	Boolean commitTweet(Tweet tweet, String commentId);
	Boolean removeComment(Tweet tweet, String commentId);

}

