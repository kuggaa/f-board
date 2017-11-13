package com.socioboard.f_board_pro.fragments;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.socioboard.f_board_pro.LikeSchedulerReciever;
import com.socioboard.f_board_pro.R;
import com.socioboard.f_board_pro.adapter.CustomChoosePageLikeAdapter;
import com.socioboard.f_board_pro.adapter.HomeFeedAdapter;
import com.socioboard.f_board_pro.database.util.F_Board_LocalData;
import com.socioboard.f_board_pro.database.util.JSONParseraa;
import com.socioboard.f_board_pro.database.util.MainSingleTon;
import com.socioboard.f_board_pro.database.util.ModelUserDatas;
import com.socioboard.f_board_pro.models.ChoosePageLikeModel;
import com.socioboard.f_board_pro.models.GroupModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.facebook.FacebookSdk.getApplicationContext;

public class AutoLiker extends Fragment
{
	Handler handler;
	public String adminpageacesstoken;
	View rooview;
	AlarmManager alarmManagers;
	TextView totalcount, likedcount;
	ImageView button1Search;
	HomeFeedAdapter feedAdapter;
	EditText likesperminute, totalhours;
	RelativeLayout timerRLt2, service_runningRlt, unkonw_user, scheduler,
			cancel_schedulerRlt, showlikeresponseRlt;
	boolean error;

	TextView warningtext, totallikerperday, completed_likes, pending_likes,
			total_hours_assigned, textViewCount,total_selected_links,clear_total_links;
	ImageView refreshImage;
	F_Board_LocalData database;
	ArrayList<GroupModel> groupList;
	boolean isServicRunning = false;
	ArrayList<ModelUserDatas> navDrawerItems;

	Context context;
	ProgressDialog progressDialog;
	public Timer timersa, refreshTmer = new Timer();
	public SparseBooleanArray sparseBooleanArray;
	int count = 0;
	int countadminpage;
	RelativeLayout account_Rlt;


	private ArrayList<String> AdminAccessTokenPage = new ArrayList<>();
	private List<ChoosePageLikeModel> likePageList = new ArrayList<ChoosePageLikeModel>();
	private CustomChoosePageLikeAdapter adapter;
	RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
	String userFBaccesToken;
	public static String adminPageAcessToken;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{

		rooview = inflater.inflate(R.layout.liker_layout, container, false);

		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		adapter = new CustomChoosePageLikeAdapter(AutoLiker.this,likePageList);

		userFBaccesToken = MainSingleTon.accesstoken;

		//LoadAd();

		database = new F_Board_LocalData(getActivity());
		unkonw_user = (RelativeLayout) rooview.findViewById(R.id.unkonw_user);
		groupList = new ArrayList<GroupModel>();
		service_runningRlt = (RelativeLayout) rooview
				.findViewById(R.id.service_running);
		showlikeresponseRlt = (RelativeLayout)rooview.findViewById(R.id.showLikeResponse);

		totallikerperday = (TextView) rooview
				.findViewById(R.id.totallikerperday);
		completed_likes = (TextView) rooview.findViewById(R.id.completed_likes);
		pending_likes = (TextView) rooview.findViewById(R.id.pending_likes);
		total_hours_assigned = (TextView) rooview
				.findViewById(R.id.total_hours_assigned);
		total_selected_links = (TextView)rooview.findViewById(R.id.total_selected_links);
		clear_total_links = (TextView)rooview.findViewById(R.id.clear_total_links);

		alarmManagers = (AlarmManager) getActivity().getSystemService(
				getActivity().ALARM_SERVICE);
		likesperminute = (EditText) rooview.findViewById(R.id.likesperminute);
		totalhours = (EditText) rooview.findViewById(R.id.totalhoure);
		timerRLt2 = (RelativeLayout) rooview.findViewById(R.id.timerRLt2);
		scheduler = (RelativeLayout) rooview.findViewById(R.id.scheduler);
		cancel_schedulerRlt = (RelativeLayout) rooview
				.findViewById(R.id.cancel_schedulerRlt);


		account_Rlt = (RelativeLayout) rooview.findViewById(R.id.account_Rlt);
		textViewCount = (TextView) rooview.findViewById(R.id.textView1Counted);

		scheduler.setVisibility(View.INVISIBLE);
		context = getActivity();
		timersa = new Timer();

		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setMessage("Authorizing.........");
		progressDialog.setCancelable(false);
		progressDialog.setIndeterminate(true);

		refreshImage = (ImageView) rooview.findViewById(R.id.refreshImage);

		handler = new Handler();


		SharedPreferences lifesharedpref = getActivity().getSharedPreferences(
				"FacebookBoardAutoliker", Context.MODE_PRIVATE);

		isServicRunning = lifesharedpref.getBoolean("isServicRunning", false);

		navDrawerItems = database.getAllUsersDataArlist();

		sparseBooleanArray = new SparseBooleanArray(navDrawerItems.size());

		
		account_Rlt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				selectAdminPage();
			}
		});
		
		for (int i = 0; i < navDrawerItems.size(); ++i) {

			sparseBooleanArray.put(i, false);

			if (navDrawerItems.get(i).getUserid()
					.contains(MainSingleTon.userid)) {

				sparseBooleanArray.put(i, true);

				count++;
			}

		}


		if (!isServicRunning) {
			progressDialog.show();

			new GetGroupsads().execute();

			service_runningRlt.setVisibility(View.GONE);

			timerRLt2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					int likesperminuteInt, totalhoursInt;

					if (!isServicRunning) {

						if (!likesperminute.getText().toString().isEmpty()
								&& !totalhours.getText().toString().isEmpty()) {
							likesperminuteInt = Integer.parseInt(likesperminute
									.getText().toString());
							totalhoursInt = Integer.parseInt(totalhours
									.getText().toString());

							if (likesperminuteInt >= 1
									&& likesperminuteInt <= 5) {
								if (totalhoursInt >= 1 && totalhoursInt <= 24) {


									if(MainSingleTon.autoLikerPageList.size()==0)
									{
										Toast.makeText(getActivity(),"Select page",Toast.LENGTH_SHORT).show();
									}
									else {

										if(MainSingleTon.selectAdmingPage)
										{
											if (isServicRunning) {
												Toast.makeText(getActivity(),
														"Like schedular is running",
														Toast.LENGTH_SHORT).show();

											} else {
												setAlarmThisPost(MainSingleTon.userid,
														likesperminuteInt,
														totalhoursInt);
											}

											System.out.println("STTTARTT="
													+ MainSingleTon.userid + "***"
													+ likesperminuteInt + "***"
													+ totalhoursInt);
										}
										else {
											Toast.makeText(getApplicationContext(),"Select Admin Page",Toast.LENGTH_SHORT).show();
										}
									}
								}
								else {
									totalhours
											.setError("try with new number between 1 to 24");
								}
							} else {
								likesperminute
										.setError("try with new number between 1 to 5");

							}

						} else {
							Toast.makeText(
									getActivity(),
									"Please enter likes count and execution hours",
									Toast.LENGTH_SHORT).show();
						}


					} else {
						Toast.makeText(getActivity(),
								"Like schedular is running", Toast.LENGTH_SHORT)
								.show();
					}
				}
			});

		} else {
			unkonw_user.setVisibility(View.INVISIBLE);
			// service's are running
			service_runningRlt.setVisibility(View.VISIBLE);

			totallikerperday.setText("Total likes assigned :  "
					+ lifesharedpref.getInt("totallikerperday", 0));
			completed_likes.setText("Liked :   "
					+ lifesharedpref.getInt("completed_likes", 0));
			pending_likes.setText("Pending likes :   "
					+ lifesharedpref.getString("pending_likes", "0"));
			total_hours_assigned.setText("Total hours to finish task :   "
					+ lifesharedpref.getInt("totalhoursInt", 0) + "Hr");

		}



		total_selected_links.setText("Total selected pages :  "+MainSingleTon.autoLikerPageList.size());
		clear_total_links.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainSingleTon.autoLikerPageList.clear();
				total_selected_links.setText("Total selected pages :  "+MainSingleTon.autoLikerPageList.size());
			}
		});

		refreshImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("REFRESIS SERVIC IS RUNNING**********************"
								+ isMyServiceRunning(LikeSchedulerReciever.class));
				SharedPreferences lisharedpref = getActivity()
						.getSharedPreferences("FacebookBoardAutoliker",
								Context.MODE_PRIVATE);

				isServicRunning = lisharedpref.getBoolean("isServicRunning",
						false);

				if (isServicRunning) {
					service_runningRlt.setVisibility(View.VISIBLE);

					totallikerperday.setText("Total likes assigned :  "
							+ lisharedpref.getInt("totallikerperday", 0));
					completed_likes.setText("Liked :   "
							+ lisharedpref.getInt("completed_likes", 0));
					pending_likes.setText("Pending likes :   "
							+ lisharedpref.getString("pending_likes", "0"));
					total_hours_assigned
							.setText("Total hours to finish task :   "
									+ lisharedpref.getInt("totalhoursInt", 0)
									+ "Hr");

				} else {
					service_runningRlt.setVisibility(View.GONE);
				}

			}
		});

		cancel_schedulerRlt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				cancelService();

			}
		});

		schedullerInner();

		return rooview;
	}


	public void test()
	{
		service_runningRlt.setVisibility(View.INVISIBLE);
	}

//	void LoadAd()
//	{
//		MobileAds.initialize(getActivity(), getString(R.string.adMob_app_id));
//		AdView mAdView = (AdView) rooview.findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
//
//	}

	public void selectAdminPage()
	{
		new getAdminPage().execute();

		final Dialog dialog;

		dialog = new Dialog(AutoLiker.this.context);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setContentView(R.layout.choosepage_fragment);

		final ListView listData = (ListView)dialog.findViewById(R.id.list);
		listData.setAdapter(adapter);

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		Window window = dialog.getWindow();

		lp.copyFrom(window.getAttributes());

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;

		lp.height = WindowManager.LayoutParams.MATCH_PARENT;

		window.setAttributes(lp);

		dialog.setCancelable(true);

		listData.setClickable(true);

		listData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView data = (TextView)view.findViewById(R.id.accessToken);
				adminPageAcessToken = data.getText().toString();
				System.out.println("adminPageAcessTokenOnClick"+adminPageAcessToken);
				TextView pageName = (TextView)view.findViewById(R.id.pageNAme);
				textViewCount.setText("Selected account : " + pageName.getText().toString());

				MainSingleTon.selectAdmingPage = true;

				dialog.dismiss();
			}
		});


		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {

				dialog.show();

				System.out.println("dfdfsfsfs======"+likePageList.size());
				if(likePageList.size()==0)
				{
					Toast.makeText(getContext(),"There is no Admin Page",Toast.LENGTH_SHORT).show();
				}

			}
		},1000);
	}

	public class getAdminPage extends AsyncTask<String,Void,String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			likePageList.clear();
		}

		@Override
		protected String doInBackground(String... params)
		{
			System.out.println("inside getPage class=======");
			//String tokenURL = "https://graph.facebook.com/me/likes?access_token=" + userFBaccesToken;
			String tokenURL = "https://graph.facebook.com/me/accounts?access_token=" + userFBaccesToken;

			System.out.println("GetAdminPage url=1234 " + tokenURL);


			//This Volly is use to access only admin page of facebook

			final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, tokenURL, null, new Response.Listener<JSONObject>() {
				public void onResponse(JSONObject response) {

					//    System.out.println(""+response);
					try {
						JSONArray jsonArray = response.getJSONArray("data");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject1 = jsonArray.getJSONObject(i);

							ChoosePageLikeModel choosePageLikeModel = new ChoosePageLikeModel();

							choosePageLikeModel.setAccess_token(jsonObject1.getString("access_token"));

							choosePageLikeModel.setCategory(jsonObject1.getString("category"));

							choosePageLikeModel.setName(jsonObject1.getString("name"));

							choosePageLikeModel.setId(jsonObject1.getString("id"));

							AdminAccessTokenPage.add(choosePageLikeModel.getAccess_token());

							likePageList.add(choosePageLikeModel);

						}
						System.out.println("AdminAccessTokenPage====1234" + AdminAccessTokenPage);

						adapter.notifyDataSetChanged();

					} catch (JSONException e) {
						e.printStackTrace();
					}
					//listData.setAdapter(adapter);
				}
			}, new Response.ErrorListener() {
				public void onErrorResponse(VolleyError error) {
					Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
//			jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
//					0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			requestQueue.add(jsonObjReq);
			System.out.println("Inside dialoag================");
			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
		}
	}

	
	

	void setAlarmThisPost(String likescheduler_id, int likesperminuteInt,
			int totalhoursInt) {

		Intent myIntent = new Intent(getActivity(), LikeSchedulerReciever.class);

		// myIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

		myIntent.putExtra("respcode", 111);

		getActivity().startService(myIntent);

		SharedPreferences lifesharedpref = getActivity().getSharedPreferences(
				"FacebookBoardAutoliker", Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = lifesharedpref.edit();

		editor.putString("likescheduler_id", likescheduler_id);

		editor.putInt("likesperminuteInt", likesperminuteInt);

		editor.putInt("totalhoursInt", totalhoursInt);

		editor.putString("likeScheluerAccesstoken", MainSingleTon.accesstoken);

		editor.putBoolean("isServicRunning", true);

		editor.commit();

		SharedPreferences lifesharedpref1 = getActivity().getSharedPreferences(
				"FacebookBoardAutoliker", Context.MODE_PRIVATE);

		isServicRunning = lifesharedpref1.getBoolean("isServicRunning", false);

		if (isServicRunning) {
			service_runningRlt.setVisibility(View.VISIBLE);

			totallikerperday.setText("Total likes assigned :  "
					+ lifesharedpref1.getInt("totallikerperday", 0));
			completed_likes.setText("Liked :   "
					+ lifesharedpref1.getInt("completed_likes", 0));
			pending_likes.setText("Pending likes :   "
					+ lifesharedpref1.getString("pending_likes", "0"));
			total_hours_assigned.setText("Total hours to finish task :   "
					+ lifesharedpref1.getInt("totalhoursInt", 0) + "Hr");

		} else {
			// Not started
		}
		// **************************************

	}

	public  void cancelService()
	{

		Intent myIntent = new Intent(getActivity(), LikeSchedulerReciever.class);

		myIntent.putExtra("respcode", 111);

		getActivity().stopService(myIntent);

		SharedPreferences lifesharedpref = getActivity().getSharedPreferences(
				"FacebookBoardAutoliker", Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = lifesharedpref.edit();
		/*
		 * editor.putString("likescheduler_id", 0+"");
		 * 
		 * editor.putInt("likesperminuteInt", 0);
		 * 
		 * editor.putInt("totalhoursInt", 0);
		 * 
		 * editor.putString("likeScheluerAccesstoken",
		 * MainSingleTon.accesstoken);
		 * 
		 * editor.putBoolean("isServicRunning", false);
		 */

		editor.clear();

		/*
		 * final NotificationManager notificationManager = (NotificationManager)
		 * getActivity().getSystemService( getActivity().NOTIFICATION_SERVICE);
		 * 
		 * notificationManager.cancel(42);
		 */
		editor.commit();

		SharedPreferences lifesharedpref1 = getActivity().getSharedPreferences(
				"FacebookBoardAutoliker", Context.MODE_PRIVATE);

		isServicRunning = lifesharedpref1.getBoolean("isServicRunning", false);

		if (isServicRunning) {
			service_runningRlt.setVisibility(View.VISIBLE);

			System.out.println("STILL RUNNING");

			totallikerperday.setText("Total likes assigned :  "
					+ lifesharedpref.getInt("totallikerperday", 0));
			completed_likes.setText("Liked :   "
					+ lifesharedpref.getInt("completed_likes", 0));
			pending_likes.setText("Pending likes :   "
					+ lifesharedpref.getString("pending_likes", "0"));
			total_hours_assigned.setText("Total hours to finish task :   "
					+ lifesharedpref.getInt("totalhoursInt", 0) + "Hr");

		} else {
			// Not started
			service_runningRlt.setVisibility(View.GONE);
			System.out.println("STILL NOOOOOT RUNNING");
		}
		// **************************************

	}



	public  void badRequest()
	{
			service_runningRlt.setVisibility(View.INVISIBLE);
			showlikeresponseRlt.setVisibility(View.VISIBLE);

	}

	public class GetGroupsads extends AsyncTask<Void, Void, String> {
		String userFBiD = null;
		String userFBaccesToken = null;
		String type = null;

		@Override
		protected String doInBackground(Void... params) {
			userFBaccesToken = MainSingleTon.accesstoken;

			userFBiD = MainSingleTon.userid;

			groupList.clear();

			String tokenURL = "https://graph.facebook.com/" + userFBiD
					+ "/groups?access_token=" + userFBaccesToken;

			JSONParseraa jsonParser = new JSONParseraa();

			System.out.println("Check Valid user=" + tokenURL);

			JSONObject jsonObject = jsonParser.getJSONFromUrl(tokenURL);

			try {

				JSONArray jsonArray = jsonObject.getJSONArray("data");

				for (int i = 0; i < jsonArray.length(); i++) {
					GroupModel groupModel = new GroupModel();

					JSONObject jsonObject2 = jsonArray.getJSONObject(i);

					if (jsonObject2.has("id")) {
						groupModel.setGroupId(jsonObject2.getString("id"));
					}
					if (jsonObject2.has("name")) {
						groupModel.setGroupName(jsonObject2.getString("name"));
					}

					if (jsonObject2.has("unread")) {
						groupModel.setGroupUnread(jsonObject2.getInt("unread"));
					}
					groupList.add(groupModel);

				}

				JSONObject jsonObject2 = jsonObject.getJSONObject("paging");

			} catch (JSONException e) {

				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (groupList.size() > 0) {/*
										 * getActivity().runOnUiThread(new
										 * Runnable() {
										 * 
										 * @Override public void run() { // TODO
										 * Auto-generated method stub
										 */
				unkonw_user.setVisibility(View.GONE);
				scheduler.setVisibility(View.VISIBLE);

				progressDialog.dismiss();
				/*
				 * } });
				 */

			} else {

				/*
				 * getActivity().runOnUiThread(new Runnable() {
				 * 
				 * @Override public void run() { // TODO Auto-generated method
				 * stub
				 */unkonw_user.setVisibility(View.VISIBLE);
				scheduler.setVisibility(View.VISIBLE);
				progressDialog.dismiss();
				;
				/*
				 * } });
				 */
			}

		}

	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {

		ActivityManager manager = (ActivityManager) getActivity()
				.getSystemService(Context.ACTIVITY_SERVICE);

		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {

			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}

		}

		return false;
	}



	@Override
	public void onDestroy() {
		super.onDestroy();

		refreshTmer.cancel();
	}

	@Override
	public void onResume() {

		super.onResume();
		schedullerInner();
	}

	void schedullerInner() {

		refreshTmer.cancel();
		refreshTmer = new Timer();
		refreshTmer.schedule(new TimerTask() {

			@Override
			public void run() {

				handler.post(new Runnable() {

					@Override
					public void run() {
						System.out
								.println("REFRESIS SERVIC IS RUNNING**********************"
										+ isMyServiceRunning(LikeSchedulerReciever.class));
						SharedPreferences lisharedpref = context
								.getSharedPreferences("FacebookBoardAutoliker",
										Context.MODE_PRIVATE);

						isServicRunning = lisharedpref.getBoolean(
								"isServicRunning", false);

						if (isServicRunning) {
							service_runningRlt.setVisibility(View.VISIBLE);

							if (lisharedpref.getBoolean("error_likes",false))
							{
								System.out.println("data"+lisharedpref.getBoolean("error_likes",false));
								cancelService();
								showlikeresponseRlt.setVisibility(View.VISIBLE);
								scheduler.setVisibility(View.INVISIBLE);
							}
							totallikerperday.setText("Total likes assigned :  "
									+ lisharedpref
											.getInt("totallikerperday", 0));
							completed_likes.setText("Liked :   " + lisharedpref.getInt("completed_likes", 0));
							pending_likes.setText("Pending likes :   "
									+ lisharedpref.getString("pending_likes",
											"0"));
							total_hours_assigned
									.setText("Total hours to finish task :   "
											+ lisharedpref.getInt(
													"totalhoursInt", 0) + "Hr");

						} else {
							service_runningRlt.setVisibility(View.GONE);
						}

					}
				});

			}
		}, 0, 1000);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}
}
