package com.google.gwt.mytwitter.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.mytwitter.client.MyTwitterService;
import com.google.gwt.mytwitter.shared.Tweet;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.itemstore.types.ItemStoreException;
import com.itemstore.types.ItemStoreHypergraph;
import com.itemstore.types.ItemStoreUtil;
import com.itemstore.types.Operation;

public class MyTwitterServiceImpl extends RemoteServiceServlet implements MyTwitterService {

	private static final long serialVersionUID = 1L;

	@Override
	public Boolean createTweet(Tweet tweet) {
		ItemStoreHypergraph hg = new ItemStoreHypergraph(tweet.getUserId() + "/" +tweet.getTweetId(), "mytwitter", "usr1", null,tweet.getV(), tweet.getE());
		try {
			ItemStoreUtil.commitItem(hg);
		} catch (ItemStoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public Boolean commitTweet(Tweet tweet, String commentId) {
		ItemStoreHypergraph hg = new ItemStoreHypergraph(tweet.getUserId() + "/" +tweet.getTweetId(), "mytwitter", "usr1", null);
		hg.log.setMethod("PUT");
		Map<String, String> params = new HashMap<String, String>(1);
		params.put("v", commentId);
		hg.log.getOps().add(new Operation("addVer", params, (Serializable)tweet.getV().get(commentId)));
		params = new HashMap<String, String>(2);
		String edge = tweet.getEdgeFrom(commentId);
		String[] ver = edge.split(":");
		params.put("u", ver[0]);
		params.put("v", ver[1]);
		hg.log.getOps().add(new Operation("addEdge", params, (Serializable)tweet.getE().get(edge)));

		try {
			ItemStoreUtil.commitItem(hg);
		} catch (ItemStoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getTweetList() {
		String xml = "";
		try {
			xml = ItemStoreUtil.getItems("mytwitter", "usr1");
		} catch (ItemStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xml;
	}

	@Override
	public Tweet getTweet(String itemnm) {
		ItemStoreHypergraph hg = null;
		try {
			hg = (ItemStoreHypergraph) ItemStoreUtil.getItem(itemnm, "mytwitter", "usr1", "hg");
		} catch (ItemStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  new Tweet(itemnm.substring(itemnm.indexOf('/')+1, itemnm.length()), hg.getVertices(), hg.getEdges());
	}

	@Override
	public Boolean removeTweet(Tweet tweet) {
		ItemStoreHypergraph hg = new ItemStoreHypergraph(tweet.getUserId() + "/" +tweet.getTweetId(), "mytwitter", "usr1", null,tweet.getV(), tweet.getE());
		try {
			ItemStoreUtil.deleteItem(hg);
		} catch (ItemStoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public Boolean removeComment(Tweet tweet, String commentId) {
		ItemStoreHypergraph hg = new ItemStoreHypergraph(tweet.getUserId() + "/" +tweet.getTweetId(), "mytwitter", "usr1", null);
		hg.log.setMethod("PUT");
		Map<String, String> params = new HashMap<String, String>(2);
		String edge = tweet.getEdgeFrom(commentId);
		String[] ver = edge.split(":");
		params.put("u", ver[0]);
		params.put("v", ver[1]);
		hg.log.getOps().add(new Operation("removeEdge", params, (Serializable)tweet.getE().get(edge)));
		params = new HashMap<String, String>(1);
		params.put("v", commentId);
		hg.log.getOps().add(new Operation("removeVer", params, (Serializable)tweet.getV().get(commentId)));

		try {
			ItemStoreUtil.commitItem(hg);
		} catch (ItemStoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}