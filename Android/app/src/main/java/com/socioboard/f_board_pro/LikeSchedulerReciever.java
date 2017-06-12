package com.socioboard.f_board_pro;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.socioboard.f_board_pro.adapter.CustomChoosePageLikeAdapter;
import com.socioboard.f_board_pro.database.util.F_Board_LocalData;
import com.socioboard.f_board_pro.database.util.JSONParseraa;
import com.socioboard.f_board_pro.database.util.MainSingleTon;
import com.socioboard.f_board_pro.database.util.Utilsss;
import com.socioboard.f_board_pro.fragments.AutoLiker;
import com.socioboard.f_board_pro.models.ChoosePageLikeModel;
import com.socioboard.f_board_pro.models.DetermineUserLike;
import com.socioboard.f_board_pro.models.HomeFeedModel;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;

public class LikeSchedulerReciever extends Service
{

	AutoLiker autoLiker = new AutoLiker();

	public static int i=0;
	public String id;

	RequestQueue requestQueue;

	public static HttpResponse response=null;

	private ArrayList<String> AdminAccessTokenPage;

	private List<ChoosePageLikeModel> likePageList = new ArrayList<ChoosePageLikeModel>();

	private CustomChoosePageLikeAdapter adapter;

	int complted_count;
	String userFBiD = null;
	String userFBaccesToken = null;
	String cheackResponseStatus = "null";
	int cheackBreak = 0;
	String type = null;
	int likesperminute = 0, totalhours = 0, perdaylikescount = 0;
	boolean error=false;
	F_Board_LocalData database;
	boolean isServicRunningFFF = false;
	private ArrayList<HomeFeedModel> mListItems;

	private ArrayList<String> AllfeedIds;

	private ArrayList<String> likedFeedIds;

	private ArrayList<String> notlikedIds;

	private ArrayList<HomeFeedModel> oldLikedlistItem;

	boolean isPagesAvailable = false, isFivePagesLoaded = false,
			isFivePagesLoaded1 = false;

	String cursor = null, cursor1 = null;

	int cursorcount = 0, cursorcount1 = 0;

	int randomtime = -3000;

	private ArrayList<HomeFeedModel> oldFeedlist;
	Timer timersa;

	SharedPreferences sharedPreferences;


	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

		sharedPreferences = getSharedPreferences("FacebookBoardAutoliker",
				Context.MODE_PRIVATE);
		database = new F_Board_LocalData(getApplicationContext());
		complted_count = sharedPreferences.getInt("completed_likes", 0);



		perdaylikescount = sharedPreferences.getInt("totallikerperday", 0);

		requestQueue = Volley.newRequestQueue(getApplicationContext());


		AdminAccessTokenPage = new ArrayList<>();

		mListItems = new ArrayList<HomeFeedModel>();

		oldLikedlistItem = new ArrayList<HomeFeedModel>();

		oldFeedlist = new ArrayList<HomeFeedModel>();

		AllfeedIds = new ArrayList<String>();

		likedFeedIds = new ArrayList<String>();

		notlikedIds = new ArrayList<String>();

		database.getAllUsersData();
		
		System.out.println("++++++++++++++++++++++++++++++++++  FboardScheduller  +++++++++++++++++++ getResponseCode");

		getSharedPrefData(getApplicationContext());
		return START_STICKY;
	}


	
	public void myprint(Object msg) {

		System.out.println(msg.toString());

	}

	public void getSharedPrefData(Context context) {

		SharedPreferences lifesharedpref = context.getSharedPreferences("FacebookBoardAutoliker", Context.MODE_PRIVATE);

		userFBiD = lifesharedpref.getString("likescheduler_id", null);
		userFBiD = MainSingleTon.userid;
		System.out.println("userFbid======="+userFBiD);

		userFBaccesToken = lifesharedpref.getString("likeScheluerAccesstoken", null);
		userFBaccesToken = MainSingleTon.accesstoken;
		System.out.println("userFBaccessToken======="+userFBaccesToken);

		//new getAdminPage().execute();
		new GetFeeds().execute(userFBaccesToken);

		likesperminute = lifesharedpref.getInt("likesperminuteInt", 0);
		totalhours = lifesharedpref.getInt("totalhoursInt", 0);

		perdaylikescount = totalhours * 60 * likesperminute;

		
		
//		if (userFBiD != null) {
//
//			for (int i = 0; i < MainSingleTon.useraccesstokenlist.size(); i++) {
//				new GetFeeds().execute(MainSingleTon.useraccesstokenlist.get(i));
//			}
//		}

	}





	public class GetFeeds extends AsyncTask<String, Void, String>
	{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			int j=0;

			if(i<MainSingleTon.autoLikerPageList.size())
			{
				if(j>0)
				{
					i++;
					id = MainSingleTon.likePgID.get(i);
					j=0;
				}
				id = MainSingleTon.likePgID.get(i);
				i++;
			}
			else {
				i=0;
				id = MainSingleTon.likePgID.get(i);
				j++;
			}

			//String tokenURL = "https://graph.facebook.com/me/home?access_token=" + userFBaccesToken;
			String coverUrl = "https://graph.facebook.com/" + id + "/feed?access_token=" + MainSingleTon.accesstoken;
			System.out.println("coverUrl========"+coverUrl);

			//System.out.println("HOME FEED url ======== " + tokenURL);

			JSONParseraa jsonParser = new JSONParseraa();

			JSONObject jsonObject = jsonParser.getJSONFromUrl(coverUrl);

			mListItems.clear();
			try {

				JSONArray jsonArray = jsonObject.getJSONArray("data");

				if (jsonArray.length() != 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						HomeFeedModel feedModel = new HomeFeedModel();
						DetermineUserLike userLikes = new DetermineUserLike();
						JSONObject jsonObject2 = jsonArray.getJSONObject(i);
						if (jsonObject2.has("id")) {
							feedModel.setFeedId(jsonObject2.getString("id"));
							userLikes.setFeedId(jsonObject2.getString("id"));
						}
						AllfeedIds.add(feedModel.getFeedId());

					}

					System.out.println("AllfeedsIds======="+AllfeedIds);


					if (jsonObject.has("paging")) {
						JSONObject js56 = jsonObject.getJSONObject("paging");

						if (js56.has("next")) {
							cursor = js56.getString("next");
						} else {

						}
					} else {
						cursor = null;
					}
				} else {
					cursor = null;
				}

			} catch (JSONException e) {

				e.printStackTrace();

				SharedPreferences sharedPreferences = getSharedPreferences(
						"FacebookBoardAutoliker", Context.MODE_PRIVATE);

				sharedPreferences.edit().clear().commit();

				LikeSchedulerReciever.this.stopSelf();
				LikeSchedulerReciever.this.onDestroy();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			if (AllfeedIds.size() != 0)
			{
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {

						// Check if feeds AllfeedIdsList has same as
						// likedFeedIdsList

						Set<String> uniqueset = new HashSet<String>();

						uniqueset.addAll(AllfeedIds);

						AllfeedIds.clear();

						AllfeedIds.addAll(uniqueset);

						notlikedIds.clear();

						notlikedIds.addAll(differenciate(AllfeedIds, likedFeedIds));

						// Clear all total ids now add only not liked ids to
						// Allids
						AllfeedIds.clear();

						AllfeedIds.addAll(notlikedIds);

						if (AllfeedIds.size() > likesperminute) {
							for (int i = 0; i < likesperminute; i++)
							{

								if (!cheackResponseStatus.equalsIgnoreCase("HTTP/1.1 400 Bad Request"))
								{
									try {

										if (isNetworkAvailable(getApplicationContext())) {
											SharedPreferences lifesharedpref1 = getApplication()
													.getSharedPreferences(
															"FacebookBoardAutoliker",
															Context.MODE_PRIVATE);

											boolean isServicRunning = lifesharedpref1
													.getBoolean("isServicRunning",
															false);

											if (isServicRunning) {
												new CallToFbLike()
														.execute(AllfeedIds.get(i));
											} else {
												stopSelf();
												LikeSchedulerReciever.this
														.stopSelf();
												LikeSchedulerReciever.this
														.onDestroy();
											}

										} else {
											SharedPreferences sharedPreferences = getSharedPreferences(
													"FacebookBoardAutoliker",
													Context.MODE_PRIVATE);

											sharedPreferences.edit().putBoolean(
													"isServicRunning", false);

											LikeSchedulerReciever.this.stopSelf();
											LikeSchedulerReciever.this.onDestroy();
										}

										int sleepInMiliseconds = (60 / likesperminute)
												* 1000 - (randomtime);

										Thread.sleep(sleepInMiliseconds);

										if (i == 0) {
											randomtime = 3000;
										}
										if (i == 2) {
											randomtime = -3000;
										}

									} catch (InterruptedException e) {

										e.printStackTrace();
									}
								}
								else
								{
									System.out.println("Response========="+response.getStatusLine());
									cheackBreak=1;
									break;
								}

							}


							notlikedIds.clear();

							notlikedIds.addAll(differenciate(AllfeedIds,
									likedFeedIds));

							// Clear all total ids now add only not liked ids to
							// Allids
							AllfeedIds.clear();

							AllfeedIds.addAll(notlikedIds);

							Set<String> uniqueset1 = new HashSet<String>();

							uniqueset1.addAll(likedFeedIds);

							likedFeedIds.clear();

							likedFeedIds.addAll(uniqueset1);

							if (perdaylikescount == complted_count) {
								System.out.println("*************DONE***********");

								SharedPreferences sharedPreferences = getSharedPreferences(
										"FacebookBoardAutoliker",
										Context.MODE_PRIVATE);

								sharedPreferences.edit().putBoolean(
										"isServicRunning", false);

								LikeSchedulerReciever.this.stopSelf();
								LikeSchedulerReciever.this.onDestroy();

							} else {
								if (isNetworkAvailable(getApplicationContext())) {

									new GetFeeds().execute(userFBaccesToken);


								} else {
									SharedPreferences sharedPreferences = getSharedPreferences("FacebookBoardAutoliker",
											Context.MODE_PRIVATE);

									sharedPreferences.edit().putBoolean(
											"isServicRunning", false);

									LikeSchedulerReciever.this.stopSelf();
									onDestroy();
								}

								System.out
										.println(perdaylikescount
												+ "=perdaylikescount************************presentcounter="
												+ complted_count);
							}

						} else {
							System.out
									.println("*****************NO FEEEDS to Fetch old feeds********************");

							if (cursor != null) {
								if (isNetworkAvailable(getApplicationContext())) {
									new LoadMoreSearchPeopleAys1()
											.execute(cursor);

								} else {
									SharedPreferences sharedPreferences = getSharedPreferences(
											"FacebookBoardAutoliker",
											Context.MODE_PRIVATE);

									sharedPreferences.edit().putBoolean(
											"isServicRunning", false);
									LikeSchedulerReciever.this.stopSelf();
									LikeSchedulerReciever.this.onDestroy();
								}

							}
						}

					}
				});

				thread.start();
			} else if (AllfeedIds.size() == 0) {
				if (isNetworkAvailable(getApplicationContext())) {
					new LoadMoreSearchPeopleAys1().execute(cursor);

				} else {
					SharedPreferences sharedPreferences = getSharedPreferences(
							"FacebookBoardAutoliker", Context.MODE_PRIVATE);

					sharedPreferences.edit().putBoolean("isServicRunning",
							false);
					LikeSchedulerReciever.this.stopSelf();
					LikeSchedulerReciever.this.onDestroy();
				}
			}

			super.onPostExecute(result);

		}

	}




	public List<String> differenciate(List<String> a, List<String> b) {

		// difference a-b
		List<String> c = new ArrayList<String>(a.size());
		c.addAll(a);
		c.removeAll(b);

		return c;
	}

	public void runThread(ArrayList<HomeFeedModel> list)
	{

		if (mListItems.size() != 0) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {

					if (mListItems.size() > likesperminute) {
						for (int i = 0; i < likesperminute; i++) {
							try {

								SharedPreferences lifesharedpref1 = getApplication()
										.getSharedPreferences(
												"FacebookBoardAutoliker",
												Context.MODE_PRIVATE);

								boolean isServicRunning = lifesharedpref1
										.getBoolean("isServicRunning", false);

								if (isServicRunning) {

									new CallToFbLike().execute(mListItems
											.get(i).getFeedId());

									int sleepInMiliseconds = (60 / likesperminute) * 1000;

									Thread.sleep(sleepInMiliseconds);

								} else {
									stopSelf();
									LikeSchedulerReciever.this.stopSelf();
									LikeSchedulerReciever.this.onDestroy();
								}

							} catch (InterruptedException e) {

								e.printStackTrace();
							}

						}

						if (perdaylikescount != complted_count) {

							new GetFeeds().execute(userFBaccesToken);

							System.out
									.println(perdaylikescount
											+ "=perdaylikescount************************presentcounter="
											+ complted_count);

						} else {
							System.out.println("*************DONE***********");

							SharedPreferences sharedPreferences = getSharedPreferences(
									"FacebookBoardAutoliker",
									Context.MODE_PRIVATE);

							sharedPreferences.edit().clear().commit();

							LikeSchedulerReciever.this.stopSelf();
							LikeSchedulerReciever.this.onDestroy();
						}

					} else {
						System.out
								.println("*****************NO FEEEDS********************");
					}

				}
			});

			thread.start();
		}

	}


	public class CallToFbLike extends AsyncTask<String, Void, String> {
		String adminPageAcessToken;

		public CallToFbLike()
		{
			adminPageAcessToken = AutoLiker.adminPageAcessToken;

			System.out.println("adminPageAcessToken = "+adminPageAcessToken);
			System.out.println("Call to CallToFBLike");
		}


		@Override
		protected String doInBackground(String... params) {

			HttpClient httpclient = new DefaultHttpClient();

			String feedID = params[0];

			likedFeedIds.add(feedID);

			String URL;


			complted_count++;
			System.out.println("new Feed Id-------------"+feedID);
			if (oldLikedlistItem.size() != 0) {
				for (int i = 0; i < oldLikedlistItem.size(); i++) {
					if (oldLikedlistItem.get(i).getFeedId().equalsIgnoreCase(feedID)) {
						System.out.println(feedID+ "_______________Already liked________________");
						System.out.println("new Feed Id-------------"+feedID);
					} else {
						URL = "https://graph.facebook.com/" + feedID + "/likes";
						System.out.println("new Feed Id-------------"+feedID);
					}

				}
			} else {
				URL = "https://graph.facebook.com/" + feedID + "/likes";
				System.out.println("new Feed Id-------------"+feedID);
			}
			URL = "https://graph.facebook.com/" + feedID + "/likes";
			System.out.println("new Feed Id-------------"+feedID);

			HttpPost httppost = new HttpPost(URL);

			try {

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("access_token",adminPageAcessToken));
				System.out.println(".............LikeSchedulerReceiver.........."+adminPageAcessToken);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				response = httpclient.execute(httppost);

				if(response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 400 Bad Request"))
				{
					cheackResponseStatus = response.getStatusLine().toString();
					error=true;
					System.out.println("Facebook API Limitation has cross. It is block for some time....");
				}
				System.out.println("response like......"+ response.getStatusLine() + feedID);


				sharedPreferences.edit().putInt("totallikerperday", perdaylikescount).commit();
				sharedPreferences.edit().putInt("completed_likes", complted_count).commit();
				sharedPreferences.edit().putString("pending_likes",(perdaylikescount - complted_count) + "").commit();
				sharedPreferences.edit().putBoolean("error_likes", error).commit();

			} catch (ClientProtocolException e) {
				System.out.println(e);

			} catch (IOException e) {

			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

		}
	}

	private class LoadMoreSearchPeopleAys1 extends
			AsyncTask<String, Void, Void> {
		String type = null;

		@Override
		protected Void doInBackground(String... params) {

			String hitNextUrl = params[0];

			JSONParseraa jsonParser = new JSONParseraa();

			JSONObject jsonObject = null;

			jsonObject = jsonParser.getJSONFromUrl(hitNextUrl);

			try {

				JSONArray jsonArray = jsonObject.getJSONArray("data");

				if (jsonArray.length() != 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						HomeFeedModel feedModel = new HomeFeedModel();
						DetermineUserLike userLikes = new DetermineUserLike();
						JSONObject jsonObject2 = jsonArray.getJSONObject(i);
						if (jsonObject2.has("id")) {
							feedModel.setFeedId(jsonObject2.getString("id"));
							userLikes.setFeedId(jsonObject2.getString("id"));
						}
						if (jsonObject2.has("likes")) {
							JSONObject jsonObjectlikes = jsonObject2
									.getJSONObject("likes");
							JSONArray jsonArraylikes = jsonObjectlikes
									.getJSONArray("data");
							feedModel.setLikes(jsonArraylikes.length());
							for (int j = 0; j < jsonArraylikes.length(); j++) {
								JSONObject jsonObjectlike = jsonArraylikes
										.getJSONObject(j);
								if (jsonObjectlike.has("id")) {
									String id = jsonObjectlike.getString("id");
									if (id.equalsIgnoreCase(MainSingleTon.userid)) {
										userLikes.setLike(true);
									} else {
										userLikes.setLike(false);
									}
								} else {
								}
							}
						} else {
							feedModel.setLikes(0);
							userLikes.setLike(false);
						}
						if (jsonObject2.has("comments")) {
							JSONObject jsonObjectcomments = jsonObject2
									.getJSONObject("comments");
							JSONArray jsonArraycomments = jsonObjectcomments
									.getJSONArray("data");
							feedModel.setComments(jsonArraycomments.length());
						} else {
							feedModel.setComments(0);
						}
						if (jsonObject2.has("shares")) {
							JSONObject jsonObjectshare = jsonObject2
									.getJSONObject("shares");
							System.out.println("jsonObjectshare  "
									+ jsonObjectshare);

							if (jsonObjectshare.has("count")) {
								feedModel.setShares(jsonObjectshare
										.getInt("count"));
								System.out.println("sharessss  "
										+ jsonObjectshare.getInt("count"));
							} else {
								feedModel.setShares(0);
							}
						} else {
							feedModel.setShares(0);
						}
						if (jsonObject2.has("type")) {
							type = jsonObject2.getString("type");

							if (type.equalsIgnoreCase("link")) {
								if (jsonObject2.has("id")) {
									feedModel.setFeedId(jsonObject2
											.getString("id"));

								}
								if (jsonObject2.has("description")) {
									feedModel.setDescription(jsonObject2
											.getString("description"));
								}

								feedModel
										.setDateTime(Utilsss
												.GetLocalDateStringFromUTCString(jsonObject2
														.getString("created_time")));

								if (jsonObject2.has("picture")) {
									feedModel.setPicture(jsonObject2
											.getString("picture"));
								}
								if (jsonObject2.has("from")) {
									JSONObject jsonObject3 = jsonObject2
											.getJSONObject("from");
									if (jsonObject3.has("name")) {
										feedModel.setFrom(jsonObject3
												.getString("name"));
									}
									if (jsonObject3.has("id")) {
										feedModel.setFromID(jsonObject3
												.getString("id"));
									}
									feedModel
											.setProfilePic("https://graph.facebook.com/"
													+ jsonObject3
															.getString("id")
													+ "/picture?type=small");
								}
								if (jsonObject2.has("message")) {
									feedModel.setMessage(jsonObject2
											.getString("message"));
								} else if (jsonObject2.has("name")) {
									feedModel.setMessage(jsonObject2
											.getString("name"));
								}

							}
							if (type.equalsIgnoreCase("status")) {

								if (jsonObject2.has("id")) {
									feedModel.setFeedId(jsonObject2
											.getString("id"));

								}
								if (jsonObject2.has("description")) {
									feedModel.setDescription(jsonObject2
											.getString("description"));
								} else if (jsonObject2.has("story")) {
									feedModel.setDescription(jsonObject2
											.getString("story"));
								}

								feedModel
										.setDateTime(Utilsss
												.GetLocalDateStringFromUTCString(jsonObject2
														.getString("created_time")));
								if (jsonObject2.has("picture")) {
									feedModel.setPicture(jsonObject2
											.getString("picture"));
								}
								if (jsonObject2.has("from")) {
									JSONObject jsonObject3 = jsonObject2
											.getJSONObject("from");
									if (jsonObject3.has("name")) {
										feedModel.setFrom(jsonObject3
												.getString("name"));
									}
									if (jsonObject3.has("id")) {
										feedModel.setFromID(jsonObject3
												.getString("id"));
									}
									feedModel
											.setProfilePic("https://graph.facebook.com/"
													+ jsonObject3
															.getString("id")
													+ "/picture?type=small");

								}
								if (jsonObject2.has("message")) {
									feedModel.setMessage(jsonObject2
											.getString("message"));
								} else if (jsonObject2.has("name")) {
									feedModel.setMessage(jsonObject2
											.getString("name"));
								}

							}
							if (type.equalsIgnoreCase("photo")) {

								if (jsonObject2.has("id")) {
									feedModel.setFeedId(jsonObject2
											.getString("id"));

								}
								if (jsonObject2.has("description")) {
									feedModel.setDescription(jsonObject2
											.getString("description"));
								} else if (jsonObject2.has("story")) {
									feedModel.setDescription(jsonObject2
											.getString("story"));
								}

								feedModel
										.setDateTime(Utilsss
												.GetLocalDateStringFromUTCString(jsonObject2
														.getString("created_time")));
								if (jsonObject2.has("picture")) {
									feedModel.setPicture(jsonObject2
											.getString("picture"));
								}
								if (jsonObject2.has("from")) {
									JSONObject jsonObject3 = jsonObject2
											.getJSONObject("from");
									if (jsonObject3.has("name")) {
										feedModel.setFrom(jsonObject3
												.getString("name"));
									}
									if (jsonObject3.has("id")) {
										feedModel.setFromID(jsonObject3
												.getString("id"));
									}
									feedModel
											.setProfilePic("https://graph.facebook.com/"
													+ jsonObject3
															.getString("id")
													+ "/picture?type=small");

								}
								if (jsonObject2.has("message")) {
									feedModel.setMessage(jsonObject2
											.getString("message"));
								} else if (jsonObject2.has("name")) {
									feedModel.setMessage(jsonObject2
											.getString("name"));
								}
							}
							if (type.equalsIgnoreCase("video")) {

								if (jsonObject2.has("id")) {
									feedModel.setFeedId(jsonObject2
											.getString("id"));

								}
								if (jsonObject2.has("description")) {
									feedModel.setDescription(jsonObject2
											.getString("description"));
								} else if (jsonObject2.has("story")) {
									feedModel.setDescription(jsonObject2
											.getString("story"));
								}

								feedModel
										.setDateTime(Utilsss
												.GetLocalDateStringFromUTCString(jsonObject2
														.getString("created_time")));
								if (jsonObject2.has("picture")) {
									feedModel.setPicture(jsonObject2
											.getString("picture"));
								}
								if (jsonObject2.has("from")) {
									JSONObject jsonObject3 = jsonObject2
											.getJSONObject("from");
									if (jsonObject3.has("name")) {
										feedModel.setFrom(jsonObject3
												.getString("name"));
									}
									if (jsonObject3.has("id")) {
										feedModel.setFromID(jsonObject3
												.getString("id"));
									}
									feedModel
											.setProfilePic("https://graph.facebook.com/"
													+ jsonObject3
															.getString("id")
													+ "/picture?type=small");

								}
								if (jsonObject2.has("message")) {
									feedModel.setMessage(jsonObject2
											.getString("message"));
								} else if (jsonObject2.has("name")) {
									feedModel.setMessage(jsonObject2
											.getString("name"));
								}
							}

							// oldFeedlist.add(feedModel);

							AllfeedIds.add(feedModel.getFeedId());

						} else {

						}
					}

					if (jsonObject.has("paging")) {
						JSONObject js56 = jsonObject.getJSONObject("paging");

						isFivePagesLoaded1 = true;

						cursorcount1++;

						cursor1 = js56.getString("next");
					} else {
						cursor1 = null;
						isFivePagesLoaded1 = false;
					}
				} else {
					cursor1 = null;
					isFivePagesLoaded1 = false;
				}

			} catch (JSONException e) {

				e.printStackTrace();
			}

			return null;
		}





		@Override
		protected void onPostExecute(Void result)
		{


			if (isFivePagesLoaded1) {

				if (cursorcount1 <= (perdaylikescount / 2))
				{
					if (cursor1 != null)
					{

						likeIFnoID();

						System.out.println(cursorcount1
								+ "___________cursorcount2 ________"
								+ AllfeedIds.size());

					} else
					{
						System.out.println("****************FB STOpPED HITS**********************");
					}

				}
				else {
					System.out
							.println("___________GOT FIRST 20 *25 values________"
									+ oldFeedlist.size());

					new GetFeeds().execute(userFBaccesToken);

				}

			} else {
				System.out.println("****************FB STOpPED HITS**********************");
				System.out.println("*************DONE***********");

				SharedPreferences sharedPreferences = getSharedPreferences(
						"FacebookBoardAutoliker", Context.MODE_PRIVATE);

				sharedPreferences.edit().clear().commit();

				LikeSchedulerReciever.this.stopSelf();
				LikeSchedulerReciever.this.onDestroy();
			}

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

	}

	public void likeIFnoID() {

		if (AllfeedIds.size() != 0)
		{

			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run() {

					// Check if feeds AllfeedIdsList has same as
					// likedFeedIdsList

					Set<String> uniqueset = new HashSet<String>();

					uniqueset.addAll(AllfeedIds);

					AllfeedIds.clear();

					AllfeedIds.addAll(uniqueset);

					notlikedIds.clear();

					notlikedIds.addAll(differenciate(AllfeedIds, likedFeedIds));

					System.out.println("AllfeedIds=" + AllfeedIds.size());
					System.out.println("likedFeedIds=" + likedFeedIds.size());
					// Clear all total ids now add only not liked ids to Allids
					AllfeedIds.clear();

					AllfeedIds.addAll(notlikedIds);

					if (AllfeedIds.size() > likesperminute)
					{

						System.out.println("AllfeedIds.size()="
								+ AllfeedIds.size());

						for (int i = 0; i < likesperminute; i++) {
							System.out.println("likesperminute I=" + i);

							try {

								if (isNetworkAvailable(getApplicationContext())) {
									SharedPreferences lifesharedpref1 = getApplication()
											.getSharedPreferences(
													"FacebookBoardAutoliker",
													Context.MODE_PRIVATE);

									boolean isServicRunning = lifesharedpref1
											.getBoolean("isServicRunning",
													false);

									if (isServicRunning) {
										new CallToFbLike().execute(AllfeedIds
												.get(i));
									} else {
										stopSelf();
										LikeSchedulerReciever.this.stopSelf();
										LikeSchedulerReciever.this.onDestroy();
									}

								} else {
									SharedPreferences sharedPreferences = getSharedPreferences(
											"FacebookBoardAutoliker",
											Context.MODE_PRIVATE);

									sharedPreferences.edit().putBoolean(
											"isServicRunning", false);

									LikeSchedulerReciever.this.stopSelf();
									LikeSchedulerReciever.this.onDestroy();
								}

							} catch (ArrayIndexOutOfBoundsException e) {

								e.printStackTrace();

								System.out
										.println("ArrayIndexOutOfBoundsException"
												+ e);

								System.out.println("likesperminute I=" + i);
								System.out.println("AllfeedIds.size()="
										+ AllfeedIds.size());

								break;
							}

							int sleepInMiliseconds = (60 / likesperminute) * 1000;

							try {

								System.out.println("SLEEEEEEEEPING now I====="
										+ i);

								System.out.println("AllfeedIds.size()="
										+ AllfeedIds.size());

								Thread.sleep(sleepInMiliseconds);

								System.out
										.println("complted SLEEEEEEEEPING now I ====="
												+ i);

								System.out.println("AllfeedIds.size()="
										+ AllfeedIds.size());

							} catch (InterruptedException e)
							{

								e.printStackTrace();

							}

						}

						System.out.println("&&&&&&&&&&&&&&&&&FOR LOOP completed&&&&&&&&&&&&&&&&&&&&&");

						notlikedIds.clear();

						notlikedIds.addAll(differenciate(
								(ArrayList<String>) AllfeedIds.clone(),
								(ArrayList<String>) likedFeedIds.clone()));

						// Clear all total ids now add only not liked ids to
						// Allids
						AllfeedIds.clear();

						AllfeedIds.addAll(notlikedIds);

						Set<String> uniqueset1 = new HashSet<String>();

						uniqueset1.addAll(likedFeedIds);

						likedFeedIds.clear();

						likedFeedIds.addAll(uniqueset1);

						if (perdaylikescount == complted_count) {
							System.out.println("*************DONE***********");

							SharedPreferences sharedPreferences = getSharedPreferences(
									"FacebookBoardAutoliker",
									Context.MODE_PRIVATE);

							sharedPreferences.edit().clear().commit();

							LikeSchedulerReciever.this.stopSelf();

						} else {

							new GetFeeds().execute(userFBaccesToken);

							if (cursor1 != null) {
								new LoadMoreSearchPeopleAys1().execute(cursor1);
							}
							System.out
									.println(perdaylikescount + "=perdaylikescount************************presentcounter=" + complted_count);
						}

					} else {
						System.out
								.println("*****************NO FEEEDS to Fetch old feeds********************");

					}

				}
			});

			thread.start();
		}

	}

	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();

			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					Log.i("Class", info[i].getState().toString());
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
