package com.google.gwt.mytwitter.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

public class Tweet implements Serializable {

	private static final long serialVersionUID = 1L;
	private String tweetId;
	private String userId;
	private long time;
	private String tweetString;
	private TreeMap<String, Object> V;
	private HashMap<String, Object> E;

	public Tweet(){

	}

	public Tweet(String userId, String tweetString) {
		this.userId = userId;
		this.time = System.currentTimeMillis();
		this.tweetId = this.time  + "." + this.userId;
		this.tweetString = tweetString;
		this.V = new TreeMap<String, Object>();
		this.E = new HashMap<String, Object>();
		this.V.put(tweetId, tweetString);
	}

	public Tweet(String tweetId, Map<String, Object> v, Map<String, Object> e) {
		this.tweetId = tweetId;
		this.time = Long.parseLong(tweetId.substring(0, tweetId.indexOf(".")));
		this.userId = tweetId.substring(tweetId.indexOf(".")+1);
		this.tweetString = (String)v.get(tweetId);
		this.V = new TreeMap<String, Object>(v);
		this.E = new HashMap<String, Object>(e);
	}

	public String getTweetId() {
		return tweetId;
	}

	public String getUserId() {
		return userId;
	}

	public long getTime() {
		return time;
	}

	public String getTweetString() {
		return tweetString;
	}

	public Map<String, Object> getV() {
		return V;
	}

	public Map<String, Object> getE() {
		return E;
	}

	public void addComment(String commentId, String commentString) {
		V.put(commentId, commentString);
	}

	public void addHyperLink(SortedSet<String> u, SortedSet<String> v, String obj) {
		E.put(flat(u) + ":" + flat(v), obj);
	}

	public void removeComment(String commentId) {
		V.remove(commentId);
	}

	public void removeHyperLinkFrom(String w) {
		String edge = getEdgeFrom(w);
		if (edge != null) {
			E.remove(edge);
		}
	}

	public String getEdgeFrom(String w) {
		for (String e : E.keySet()) {
			if (e.startsWith(w + ":")) {
				return e;
			}
		}
		return null;
	}

	public Set<String> getAncestors(String w) {
		Set<String> ancestors = new HashSet<String>();
		for (String e : E.keySet()) {
			if (e.startsWith(w + ":")) {
				String[] parents = e.substring(e.indexOf(':') + 1).split(",");
				for(String p : parents){
					ancestors.add(p);
				}
				for (String e1 : E.keySet()) {
					for(String p : parents){
						if (e1.startsWith(p + ":")) {
							String[] gparents = e1.substring(e1.indexOf(':') + 1).split(",");
							for(String g : gparents){
								ancestors.add(g);
							}
							continue;
						}
					}
				}
				return ancestors;
			}
		}
		return null;
	}

	private String flat(SortedSet<String> u) {
		String str = "";
		for (String s : u) {
			str = str + s + ",";
		}
		return str.substring(0, str.length() - 1);
	}

	@Override
	public String toString() {
		return userId + ": " + tweetString;
	}

}
