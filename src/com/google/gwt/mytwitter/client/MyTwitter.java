package com.google.gwt.mytwitter.client;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.mytwitter.shared.Tweet;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

class StringComparator implements Comparator<String>
{
	public int compare(String s1,String s2)
	{
		return -s1.compareTo(s2);
	}
}

public class MyTwitter implements EntryPoint {  
	//private static final int REFRESH_INTERVAL = 30000; // ms
	private String USER_ID = "root";
	private VerticalPanel mainPanel = new VerticalPanel();
	private DecoratorPanel tweetDecPanel = new DecoratorPanel();
	private FlexTable tweetsFlexTable = new FlexTable();
	private HorizontalPanel tweetPanel = new HorizontalPanel();
	private TextArea tweetTextArea = new TextArea();
	private Button tweetButton = new Button("Post");
	private Button refreshButton = new Button("Refresh");
	private Label lastUpdatedLabel = new Label();
	private Map<String, Tweet> tweets = new TreeMap<String, Tweet>(new StringComparator());
	private Map<String, FlexTable> commentsTableList = new HashMap<String, FlexTable>();
	private MyTwitterServiceAsync myTwitterSvc = GWT.create(MyTwitterService.class);

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {

		// Assemble tweetPanel.
		tweetPanel.add(tweetTextArea);
		tweetPanel.add(tweetButton);
		tweetPanel.add(refreshButton);
		tweetPanel.addStyleName("tweetPanel");
		tweetPanel.setSpacing(10);

		tweetDecPanel.add(tweetPanel);

		tweetsFlexTable.setCellSpacing(20);

		// Assemble mainPanel.
		mainPanel.add(tweetDecPanel);
		mainPanel.add(lastUpdatedLabel);
		mainPanel.add(tweetsFlexTable);
		mainPanel.addStyleName("mainPanel");
		mainPanel.setSpacing(10);

		// Associate the mainPanel with the HTML host page.
		RootPanel.get("tweetList").add(mainPanel);

		// Move cursor focus to the input box.
		tweetTextArea.setFocus(true);
		tweetTextArea.addStyleName("tweetText");

		tweetButton.addStyleDependentName("tweet");
		refreshButton.addStyleDependentName("tweet");


		// Setup timer to refresh list automatically.
//		Timer refreshTimer = new Timer() {
//			@Override
//			public void run() {
//				tweetsFlexTable.removeAllRows();
//				loadTweets();
//			}
//		};
//		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

		refreshButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				tweetsFlexTable.removeAllRows();
				loadTweets();				
			}
		});

		// Listen for mouse events on the tweet button
		tweetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addTweet();
			}
		});

		tweetTextArea.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER){
					addTweet();
				}
			}
		});

		getUserId();

	}

	private void getUserId(){
		final DialogBox dialogBox = new DialogBox();
		VerticalPanel userIdPanel = new VerticalPanel();
		dialogBox.setWidget(userIdPanel);

		Label userIdLabel = new Label("User Id");
		final TextBox userIdTextBox = new TextBox();

		userIdTextBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER){
					USER_ID = userIdTextBox.getText().trim();

					dialogBox.hide();

					// Load the tweetsFlexTable
					loadTweets();
				}

			}
		});

		// Add a close button at the bottom of the dialog
		Button sumbmitButton = new Button("Submit", new ClickHandler() {
			public void onClick(ClickEvent event) {

				USER_ID = userIdTextBox.getText().trim();

				dialogBox.hide();

				// Load the tweetsFlexTable
				loadTweets();
			}
		});

		userIdPanel.add(userIdLabel);
		userIdPanel.add(userIdTextBox);
		userIdPanel.add(sumbmitButton);

		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);
		dialogBox.center();
		dialogBox.show();
	}

	private void loadTweets() {
		// Initialize the service proxy.
		if (myTwitterSvc == null) {
			myTwitterSvc = GWT.create(MyTwitterService.class);
		}

		// Set up the callback object.
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(String result) {
				parseTweetList(result);
			}
		};

		// Make the call to the stock price service.
		myTwitterSvc.getTweetList(callback);
	}

	private void parseTweetList(String messageXml){
		try {
			// parse the XML document into a DOM
			Document messageDom = XMLParser.parse(messageXml);

			// find the sender's display name in an attribute of the <from> tag
			NodeList itemNodes = messageDom.getElementsByTagName("items").item(0).getChildNodes();

			for(int i=0; i<itemNodes.getLength(); i++){
				NodeList iList = itemNodes.item(i).getChildNodes();
				String itemnm = iList.item(0).getFirstChild().getNodeValue();
				loadTweet(itemnm);
			}

		} catch (DOMException e) {
			Window.alert("Could not parse XML document.");
		}
	}

	private void loadTweet(final String itemnm){
		// Initialize the service proxy.
		if (myTwitterSvc == null) {
			myTwitterSvc = GWT.create(MyTwitterService.class);
		}

		// Set up the callback object.
		AsyncCallback<Tweet> callback = new AsyncCallback<Tweet>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Tweet tweet) {
				String tweetId = itemnm.substring(itemnm.indexOf('/') + 1); 
				tweets.put(tweetId, tweet);
				FlexTable t = new FlexTable();
				t.setCellSpacing(2);
				t.getColumnFormatter().setWidth(1, "340px");
				commentsTableList.put(tweetId, t);
				drawTweet(tweetId, -1);
			}
		};

		// Make the call to the stock price service.
		myTwitterSvc.getTweet(itemnm, callback);
	}

//	private void drawTweets() {
//		tweetsFlexTable.removeAllRows();
//		int i = 0;
//		for(final String tweetId : tweets.keySet()){
//			drawTweet(tweetId, i);
//			i++;
//		}
//	}

	private void drawTweet(final String tweetId, int i) {

		final Tweet tweet = tweets.get(tweetId);

		HorizontalPanel tweetHPanel = new HorizontalPanel();
		if(i != -1){
			tweetsFlexTable.setWidget(i, 0, tweetHPanel);
		} else{
			tweetsFlexTable.insertRow(0);
			tweetsFlexTable.setWidget(0, 0, tweetHPanel);
		}

		VerticalPanel tweetVPanel = new VerticalPanel();
		tweetVPanel.addStyleName("tweetVPanel");

		Button tweetRemoveButton = new Button("x");
		if(USER_ID.equals(tweet.getUserId())){
			tweetRemoveButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					removeTweet(tweet);
				}
			});
		} else {
			tweetRemoveButton.setVisible(false);
		}

		tweetHPanel.add(tweetVPanel);
		tweetHPanel.add(tweetRemoveButton);

		VerticalPanel tweetTextPanel = new VerticalPanel();
		tweetTextPanel.addStyleName("tweetTextPanel");
		Label tweetLabel = new Label(tweet.toString());
		Label tweetTimeLabel = new Label(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date(tweet.getTime())));
		tweetTimeLabel.addStyleName("time");
		tweetTextPanel.add(tweetLabel);
		tweetTextPanel.add(tweetTimeLabel);
		tweetVPanel.add(tweetTextPanel);

		final Map<String, Object> comments = tweet.getV();

		final Set<String> commentKeys = comments.keySet();

		final FlexTable commentsTable = commentsTableList.get(tweetId);
		commentsTable.getColumnFormatter().addStyleName(1, "comment");



		commentsTable.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(event!=null){

					Cell c = commentsTable.getCellForEvent(event);
					if(c.getCellIndex() == 1){
						for(int i=0;i<commentsTable.getRowCount()-1;i++){
							commentsTable.getWidget(i, 1).setStyleName("commentNotSelected");
						}
						int row = c.getRowIndex();
						Widget w = commentsTable.getWidget(row, 1);
						w.addStyleName("commentSelected");					
						String[] commentsArr = commentKeys.toArray(new String[1]);
						String currComment = commentsArr[row+1];
						String edge = tweet.getEdgeFrom(currComment);
						String parentList = edge.substring(edge.indexOf(':')+1);
						Set<String> ancestors = tweet.getAncestors(currComment);
						for(int i=0;i<row;i++){
							if(parentList.indexOf(commentsArr[i+1] + ",") != -1 
									|| parentList.indexOf("," + commentsArr[i+1]) != -1){
								commentsTable.getWidget(i, 1).addStyleName("commentParent");
							} else if (ancestors.contains(commentsArr[i+1])){
								commentsTable.getWidget(i, 1).addStyleName("commentAncestors");
							}
						}
					}

				}
			}
		});

		commentsTable.removeAllRows();
		tweetVPanel.add(commentsTable);


		int j = 0;
		for(final String k :commentKeys){
			if(!k.equals(tweetId)){
				final int index = j;

				CheckBox commentCheckBox = new CheckBox();
				commentCheckBox.setValue(true);	
				commentsTable.setWidget(j, 0, commentCheckBox);

				VerticalPanel commentTextPanel = new VerticalPanel();
				commentTextPanel.setWidth("340px");
				String user = k.substring(k.indexOf('.') + 1);
				Label commentLabel = new Label(user + ": " + (String)comments.get(k));
				Label commentTimeLabel = new Label(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date(Long.parseLong(k.substring(0, k.indexOf('.'))))));
				commentTimeLabel.addStyleName("time");
				commentTextPanel.add(commentLabel);
				commentTextPanel.add(commentTimeLabel);

				commentsTable.setWidget(j, 1, commentTextPanel);
				commentsTable.getCellFormatter().addStyleName(j, 1, "commentList");

				Button removeCommentButton = new Button("x");
				if(USER_ID.equals(user)){
					removeCommentButton.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							removeComment(tweet, k, index);
						}
					});
				} else {
					removeCommentButton.setVisible(false);
				}
				commentsTable.setWidget(j, 2, removeCommentButton);

				j++;
			}
		}
		commentsTable.setWidget(j, 0, new Label(""));

		DisclosurePanel commentDisclosurePanel = new DisclosurePanel("Comment");

		HorizontalPanel postCommentPanel = new HorizontalPanel();

		final TextArea postCommentTextArea = new TextArea();
		postCommentTextArea.addStyleName("commentText");
		postCommentTextArea.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER){
					addComment(tweetId, postCommentTextArea);
				}
			}
		});
		postCommentPanel.add(postCommentTextArea);

		Button postCommentButton = new Button("Comment");
		postCommentButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addComment(tweetId, postCommentTextArea);
			}
		});
		postCommentPanel.add(postCommentButton);

		commentDisclosurePanel.setContent(postCommentPanel);
		commentDisclosurePanel.setAnimationEnabled(true);
		tweetVPanel.add(commentDisclosurePanel);

	}

	private void removeTweet(Tweet tweet) {
		// Remove the tweet from the tweets list
		tweets.remove(tweet.getTweetId());
		commentsTableList.get(tweet.getTweetId());
		Widget w = commentsTableList.get(tweet.getTweetId()).getParent().getParent();
		w.removeFromParent();
		commentsTableList.remove(tweet.getTweetId());
		// Update Server 
		removeTweetServer(tweet);
	}

	private void removeTweetServer(Tweet tweet) {
		// Initialize the service proxy.
		if (myTwitterSvc == null) {
			myTwitterSvc = GWT.create(MyTwitterService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myTwitterSvc.removeTweet(tweet, callback);		
	}

	private void addTweet() {

		final String tweetText = tweetTextArea.getText().trim();
		tweetTextArea.setFocus(true);
		tweetTextArea.setText("");

		Tweet tweet = new Tweet(USER_ID, tweetText);

		// Add the tweet to the tweets list
		tweets.put(tweet.getTweetId(), tweet);
		commentsTableList.put(tweet.getTweetId(), new FlexTable());

		// Draw the Tweets
		drawTweet(tweet.getTweetId(), -1);

		// Display timestamp showing last Tweet.
		lastUpdatedLabel.setText("Last update : "
				+ DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()));

		// Update Server
		addTweetServer(tweet);

	}

	private void addTweetServer(Tweet tweet){
		// Initialize the service proxy.
		if (myTwitterSvc == null) {
			myTwitterSvc = GWT.create(MyTwitterService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myTwitterSvc.createTweet(tweet, callback);
	}

	private void addComment(String tweetId, TextArea commentTextArea) {
		String commentString = commentTextArea.getText().trim();
		commentTextArea.setText("");
		commentTextArea.setFocus(true);

		final Tweet tweet = tweets.get(tweetId);

		Map<String, Object> comments = tweet.getV();
		FlexTable commentsTable = commentsTableList.get(tweetId);

		Set<String> commentKeys = comments.keySet();

		Set<String> commentIdList = new TreeSet<String>(commentKeys);

		int i = 0;
		for(String k : comments.keySet()){
			if(!k.equals(tweetId)){
				CheckBox c = (CheckBox)commentsTable.getWidget(i, 0);
				if(c.getValue() == false){
					commentIdList.remove(k);
				}
				i++;
			}
		}

		final String commentId = 	System.currentTimeMillis() + "." + USER_ID;

		CheckBox commentCheckBox = new CheckBox();
		commentCheckBox.setValue(true);	

		final int index = commentsTable.insertRow(commentsTable.getRowCount()-1);
		commentsTable.setWidget(index, 0, commentCheckBox);

		VerticalPanel commentTextPanel = new VerticalPanel();
		commentTextPanel.setWidth("340px");
		Label commentLabel = new Label(USER_ID + ": " + commentString);
		Label commentTimeLabel = new Label(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date(Long.parseLong(commentId.substring(0, commentId.indexOf('.'))))));
		commentTimeLabel.addStyleName("time");
		commentTextPanel.add(commentLabel);
		commentTextPanel.add(commentTimeLabel);
		commentsTable.setWidget(index, 1, commentTextPanel);
		commentsTable.getCellFormatter().addStyleName(index, 1, "commentList");

		Button removeCommentButton = new Button("x");
		removeCommentButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				removeComment(tweet, commentId, index);
			}
		});
		commentsTable.setWidget(index, 2, removeCommentButton);

		tweet.addComment(commentId, commentString);

		SortedSet<String> u = new TreeSet<String>();
		u.add(commentId);
		SortedSet<String> v = new TreeSet<String>();
		v.addAll(commentIdList);
		tweet.addHyperLink(u, v, null);

		// Update the server
		addCommentServer(tweet, commentId);
	}


	private void addCommentServer(Tweet tweet, String commentId){
		// Initialize the service proxy.
		if (myTwitterSvc == null) {
			myTwitterSvc = GWT.create(MyTwitterService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myTwitterSvc.commitTweet(tweet, commentId, callback);
	}

	private void removeComment(Tweet tweet, String commentId, int index) {
		// Remove the tweet from the tweets list
		tweet.removeComment(commentId);

		// Draw the Tweets
		commentsTableList.get(tweet.getTweetId()).getRowFormatter().setVisible(index, false);

		// TODO Update Server 
		removeCommentServer(tweet, commentId);
	}

	private void removeCommentServer(Tweet tweet, String commentId) {
		// Initialize the service proxy.
		if (myTwitterSvc == null) {
			myTwitterSvc = GWT.create(MyTwitterService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myTwitterSvc.removeComment(tweet, commentId, callback);
	}

}
