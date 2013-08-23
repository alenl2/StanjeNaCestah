package si.komp.stanjenacestah;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

	private class Data {
		public String title;
		public ArrayList<String> info;

		public Data(String title, ArrayList<String> info) {
			super();
			this.title = title;
			this.info = info;
		}
	}

	private class DownloadWebPageTask extends AsyncTask<String, Void, Data> {
		@Override
		protected Data doInBackground(String... urls) {
			ArrayList<String> listItems = new ArrayList<String>();
			String title = "";
			for (String url : urls) {
				try {
					DefaultHttpClient client = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(url);
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser xpp = factory.newPullParser();
					xpp.setInput(buffer);
					int eventType = xpp.getEventType();
					String currentTag = "";

					while (eventType != XmlPullParser.END_DOCUMENT) {
						if (eventType == XmlPullParser.START_DOCUMENT) {
							currentTag = "";
						} else if (eventType == XmlPullParser.START_TAG) {
							currentTag = xpp.getName();
						} else if (eventType == XmlPullParser.END_TAG) {
							currentTag = "";
						} else if (eventType == XmlPullParser.TEXT) {
							if (currentTag.equals("title")) {
								title = xpp.getText();
							}
							if (currentTag.equals("content")) {
								for (String item : xpp.getText().split("</P>")) {
									listItems.add(item);
								}
							}
							if (currentTag.equals("updated")) {
								System.out.println("Updated " + xpp.getText());
							}
						}
						eventType = xpp.next();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Data dat = new Data(title, listItems);
			return dat;
		}

		@Override
		protected void onPostExecute(final Data result) {
			setTitle(result.title);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(cont,
					android.R.layout.simple_list_item_1, result.info) {
				@Override
				public View getView(int position, View convertView,ViewGroup parent) {
					View row;
					if (null == convertView) {
						row = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent,false);
					} else {
						row = convertView;
					}

					TextView tv = (TextView) row.findViewById(android.R.id.text1);
					tv.setText(Html.fromHtml(result.info.get(position)));

					return row;
				}
			};
			textView.setAdapter(adapter);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		readWebpage();
		return super.onOptionsItemSelected(item);
	}

}