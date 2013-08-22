package com.example.stanjenacestah;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends Activity {
	private ListView textView;

	public static Context cont;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		cont = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView = (ListView) findViewById(R.id.listView1);
	}

	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		         factory.setNamespaceAware(true);
		         XmlPullParser xpp = factory.newPullParser();
		         xpp.setInput( new StringReader ( result ) );
		         int eventType = xpp.getEventType();
		         String currentTag = "";
		         while (eventType != XmlPullParser.END_DOCUMENT) {
		          if(eventType == XmlPullParser.START_DOCUMENT) {
		              currentTag = "";
		          } else if(eventType == XmlPullParser.START_TAG) {
		              currentTag = xpp.getName();
		          } else if(eventType == XmlPullParser.END_TAG) {
		              currentTag = "";
		          } else if(eventType == XmlPullParser.TEXT) {
		        	  
		        	  if(currentTag.equals("title")){
		        		  setTitle(xpp.getText());
		        	  }
		        	  
		        	  if(currentTag.equals("content")){

		        		  ArrayList<String> listItems=new ArrayList<String>();
		        		  
		        		  for(String item: xpp.getText().split("</P>")){
		        			  listItems.add(Html.fromHtml(item).toString());
		        		  }
		        		  
		        		  ArrayAdapter<String> adapter =new ArrayAdapter<String>(cont, android.R.layout.simple_list_item_1, listItems);
		        		  textView.setAdapter(adapter);
		        	  }
		        	  
		        	  if(currentTag.equals("updated")){
		        		  System.out.println("Updated "+xpp.getText());
		        	  }
		        	  
		              
		          }
		          eventType = xpp.next();
		         }
		         
		         
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void readWebpage() {
		DownloadWebPageTask task = new DownloadWebPageTask();
		task.execute(new String[] { "http://www.promet.si/rwproxy/RWProxy.ashx?cache=1&remoteUrl=http%3A//promet/stanje_pp_rss_si" });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		readWebpage();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		readWebpage();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		readWebpage();
		return super.onOptionsItemSelected(item);
	}

}
