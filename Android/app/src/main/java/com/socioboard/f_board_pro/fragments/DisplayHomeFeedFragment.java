
package com.socioboard.f_board_pro.fragments;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.socioboard.f_board_pro.MainActivity;
import com.socioboard.f_board_pro.R;
import com.socioboard.f_board_pro.adapter.CommentAdapter;
import com.socioboard.f_board_pro.database.util.JSONParseraa;
import com.socioboard.f_board_pro.database.util.MainSingleTon;
import com.socioboard.f_board_pro.database.util.Utilsss;
import com.socioboard.f_board_pro.models.CommentModel;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayHomeFeedFragment extends ListFragment
{
	View rootView;
	TextView peopleLikedYou,noComments;
	ArrayList<CommentModel> commentList;
	ProgressBar progressbar;
	ListView mHomeCommentList;
	EditText userComment;
	ImageButton enter;
	ImageView like;
	boolean likestatus=false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.display_group_feed, container,false);

		userComment=(EditText) rootView.findViewById(R.id.usercomment);
		enter=(ImageButton) rootView.findViewById(R.id.enter);


		progressbar=(ProgressBar) rootView.findViewById(R.id.progressBar1);

		progressbar.setVisibility(View.VISIBLE);

		noComments=(TextView) rootView.findViewById(R.id.no_comments);
		noComments.setText("No Comments");
		noComments.setVisibility(View.INVISIBLE);

		like=(ImageView) rootView.findViewById(R.id.like);
		like.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v)
			{

				if(likestatus)//Undo()    new CallToFbUnLike().execute(); 
					new CallToFbUnLike().execute(); 
				else
					new CallToFbLike().execute();
			}
		});
		userComment.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				userComment.requestFocus();

			}
		});
		enter.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{

				final String comment=userComment.getText().toString();
				if(comment.length()>0)
				{
					new CallToFbComment().execute();

				}
				else
				{

					getActivity().runOnUiThread(new Runnable() 
					{

						@Override
						public void run() 
						{

							Toast.makeText(getActivity(), "Please enter your comment...!", Toast.LENGTH_SHORT).show();
						}
					});

				}

			}
		});

		peopleLikedYou=(TextView) rootView.findViewById(R.id.people_liked_you);

		peopleLikedYou.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				MainActivity.fragmentManager =getFragmentManager();
				MainActivity.swipeFragment(new DisplayLikes());							
			}
		});
		commentList=new ArrayList<CommentModel>();

		checkLike();
		commentList.clear();
		new GetHomeFeed().execute();

		return rootView;
	}
	public void Undo() 
	{
		Bundle params = new Bundle();
		params.putString(AccessToken.ACCESS_TOKEN_KEY, MainSingleTon.accesstoken);

		new GraphRequest(MainSingleTon.dummyAccesstoken, MainSingleTon.selectedGroupFeed+"/likes", params, HttpMethod.GET, new GraphRequest.Callback()
		{

			@Override
			public void onCompleted(GraphResponse response) 
			{
				System.out.println("Responce in new delete"+response);


			}
		}).executeAsync();


	}
	public void  checkLike() 
	{
		if(MainSingleTon.selectedHomeFeed.getLikes()>0)
		{
			peopleLikedYou.setText(MainSingleTon.selectedHomeFeed.getLikes()+" people like this");
			like.setBackgroundResource(R.drawable.unlike);
			for (int i = 0; i < MainSingleTon.userLikedFeedList.size(); i++) 
			{
				if(MainSingleTon.userLikedFeedList.get(i).getFeedId().equalsIgnoreCase(MainSingleTon.selectedHomeFeed.getFeedId()))
				{
					if(MainSingleTon.userLikedFeedList.get(i).isLike())
					{
						like.setBackgroundResource(R.drawable.like);

						likestatus=true;

						if(MainSingleTon.selectedHomeFeed.getLikes()==1)
						{
							peopleLikedYou.setText("You like this ");
						}
						else if(MainSingleTon.selectedHomeFeed.getLikes()==2)
						{
							peopleLikedYou.setText("You and other "+(MainSingleTon.selectedHomeFeed.getLikes()-1)+" person like this");
						}
						else
						{
							peopleLikedYou.setText("You and other "+(MainSingleTon.selectedHomeFeed.getLikes()-1)+" people like this");
						}
					}
					else
					{
						likestatus=false;
						peopleLikedYou.setText(""+MainSingleTon.selectedHomeFeed.getLikes()+" people like this");
					}
				}
			}
		}
		else
		{
			like.setBackgroundResource(R.drawable.unlike);
			peopleLikedYou.setText("Be the first to like this");

		}
	}

	public void setUnlike() 
	{
		if(MainSingleTon.selectedHomeFeed.getLikes()>0)
		{
			like.setBackgroundResource(R.drawable.unlike);
			for (int i = 0; i < MainSingleTon.userLikedFeedList.size(); i++) 
			{
				if(MainSingleTon.userLikedFeedList.get(i).getFeedId().equalsIgnoreCase(MainSingleTon.selectedHomeFeed.getFeedId()))
				{
					MainSingleTon.selectedHomeFeed.setLikes(MainSingleTon.selectedHomeFeed.getLikes()-1);
					MainSingleTon.userLikedFeedList.get(i).setLike(false);
					likestatus=false;
				}
			}
		}		
	}
	public void setLike()
	{
		like.setBackgroundResource(R.drawable.like);
		for (int i = 0; i < MainSingleTon.userLikedFeedList.size(); i++) 
		{
			if(MainSingleTon.userLikedFeedList.get(i).getFeedId().equalsIgnoreCase(MainSingleTon.selectedHomeFeed.getFeedId()))
			{

				MainSingleTon.selectedHomeFeed.setLikes(MainSingleTon.selectedHomeFeed.getLikes()+1);
				MainSingleTon.userLikedFeedList.get(i).setLike(true);
				likestatus=true;
			}
		}

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{

		mHomeCommentList = getListView();

		mHomeCommentList.setVisibility(View.INVISIBLE);
	}

	public class GetHomeFeed extends AsyncTask<Void, Void, String>
	{
		String homeFeedId =null;
		String userFBaccesToken = null;
		String type = null;

		@Override
		protected String doInBackground(Void... params) 
		{

			userFBaccesToken = MainSingleTon.accesstoken;
			homeFeedId = MainSingleTon.selectedHomeFeed.getFeedId();
			commentList.clear();

			String tokenURL = "https://graph.facebook.com/"+homeFeedId+"/comments?access_token="+userFBaccesToken;

			JSONParseraa jsonParser = new JSONParseraa();

			JSONObject jsonObject = jsonParser.getJSONFromUrl(tokenURL);

			try {

				JSONArray jsonArray =  jsonObject.getJSONArray("data");

				for(int i = 0; i<jsonArray.length();i++)
				{
					CommentModel commentModel=new CommentModel();
					JSONObject jsonObject2 = jsonArray.getJSONObject(i);

					if(jsonObject2.has("from"))
					{

						JSONObject jsonObject3 = jsonObject2.getJSONObject("from");
						if(jsonObject3.has("name"))
						{
							System.out.println("name "+jsonObject3.getString("name"));
							commentModel.setName(jsonObject3.getString("name"));								
						}

						if(jsonObject3.has("id"))
						{
							String id=null;
							commentModel.setFromID(id=jsonObject3.getString("id"));	

							commentModel.setProfilePic("https://graph.facebook.com/"+id+"/picture?type=small");


						}
					}
					if(jsonObject2.has("message"))
					{
						commentModel.setComment(jsonObject2.getString("message"));	
					}
					if(jsonObject2.has("created_time"))
					{
						commentModel.setDateTime(Utilsss.GetLocalDateStringFromUTCString(jsonObject2.getString("created_time")));	
					}
					commentList.add(commentModel);
				} 
			}
			catch (JSONException e) 
			{

				e.printStackTrace();
			}

			System.out.println("----------------------------------------------");
			return null;
		}
		@Override
		protected void onPostExecute(String result)
		{
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if(commentList.size()>0)
			{
				CommentAdapter commentAdapter = new CommentAdapter(getActivity(), commentList);
				setListAdapter(commentAdapter);
				commentAdapter.notifyDataSetChanged();
				progressbar.setVisibility(View.INVISIBLE);
				mHomeCommentList.setVisibility(View.VISIBLE);
				noComments.setVisibility(View.INVISIBLE);
			}
			else
			{
				progressbar.setVisibility(View.INVISIBLE);
				mHomeCommentList.setVisibility(View.INVISIBLE);
				noComments.setVisibility(View.VISIBLE);
			}

		}

	}

	/*class to comment*/
	public class CallToFbComment extends AsyncTask<String, Void, String> 
	{
		String comment=null;
		CallToFbComment()
		{
			comment=userComment.getText().toString();
		}
		HttpResponse response;

		@Override
		protected String doInBackground(String... params) 
		{


			HttpClient httpclient = new DefaultHttpClient();


			String URL = "https://graph.facebook.com/"+MainSingleTon.selectedHomeFeed.getFeedId() +"/comments";


			HttpPost httppost = new HttpPost(URL);


			try
			{
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("access_token",MainSingleTon.accesstoken));
				nameValuePairs.add(new BasicNameValuePair("message",comment));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				response = httpclient.execute(httppost);


			}
			catch (ClientProtocolException e)
			{
				// TODO Auto-generated catch block
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);

			if (response.getStatusLine().toString().equals("HTTP/1.1 200 OK"))
			{
				Toast.makeText(getActivity(), "Comment success", Toast.LENGTH_SHORT).show();
				userComment.clearFocus();
				userComment.setText("");

				new GetHomeFeed().execute();
				progressbar.setVisibility(View.VISIBLE);
				mHomeCommentList.setVisibility(View.INVISIBLE);
			}
			else
			{
				Toast.makeText(getActivity(), "Comment failed", Toast.LENGTH_SHORT).show();
			}

		}
	}

	/*class to like a post*/
	public class CallToFbLike extends AsyncTask<String, Void, String>
	{

		HttpResponse response;

		@Override
		protected String doInBackground(String... params)
		{


			HttpClient httpclient = new DefaultHttpClient();

			String URL = "https://graph.facebook.com/"+ MainSingleTon.selectedHomeFeed.getFeedId()+ "/likes";

			HttpPost httppost = new HttpPost(URL);

			try 
			{
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("access_token",MainSingleTon.accesstoken));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				response = httpclient.execute(httppost);
				System.out.println("response unlke......"+response.getStatusLine());	

			} 
			catch (ClientProtocolException e)
			{
				// TODO Auto-generated catch block
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (response.getStatusLine().toString().equals("HTTP/1.1 200 OK"))
			{
				Toast.makeText(getActivity(), "you like this", Toast.LENGTH_SHORT).show();
				setLike();
				checkLike();
			}
			else
			{
				Toast.makeText(getActivity(), "try later", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public class CallToFbUnLike extends AsyncTask<String, Void, String>
	{

		HttpResponse response;

		@Override
		protected String doInBackground(String... params)
		{

			HttpClient httpclient = new DefaultHttpClient();

			String URL = "https://graph.facebook.com/"+ MainSingleTon.selectedHomeFeed.getFeedId()+ "/likes";

			HttpPost httppost = new HttpPost(URL);

			try 
			{
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("access_token",MainSingleTon.accesstoken));
				nameValuePairs.add(new BasicNameValuePair("method","DELETE"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


				// Execute HTTP Post Request
				response = httpclient.execute(httppost);

				System.out.println("response unlke......"+response.getStatusLine());

			} 
			catch (ClientProtocolException e)
			{
				// TODO Auto-generated catch block
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (response.getStatusLine().toString().equals("HTTP/1.1 200 OK"))
			{
				Toast.makeText(getActivity(), "you unlike this", Toast.LENGTH_SHORT).show();
				setUnlike();
				checkLike();

			}
			else
			{
				Toast.makeText(getActivity(), "please try after some time", Toast.LENGTH_SHORT).show();
			}
		}
	}

}
