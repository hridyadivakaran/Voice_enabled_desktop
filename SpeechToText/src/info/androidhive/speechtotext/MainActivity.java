package info.androidhive.speechtotext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView txtSpeechInput,url;
	private ImageButton btnSpeak;
	private Button set;
	private final int REQ_CODE_SPEECH_INPUT = 100;
	String cmd="",URL="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//URL="http://192.168.43.207:8081/VoiceController/index.jsp";
		
		txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
		set=(Button)findViewById(R.id.set);
		url=(TextView)findViewById(R.id.url);
		
		set.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				URL=url.getText().toString();
				Toast.makeText(getApplicationContext(), "Server url set.. Now you can continue..", Toast.LENGTH_LONG).show();
			}
		});

		// hide the action bar
		getActionBar().hide();

		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				promptSpeechInput();
			}
		});

	}

	/**
	 * Showing google speech input dialog
	 * */
	private void promptSpeechInput() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.speech_prompt));
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.speech_not_supported),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Receiving speech input
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQ_CODE_SPEECH_INPUT: {
			if (resultCode == RESULT_OK && null != data) {

				ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				cmd=result.get(0);
				txtSpeechInput.setText(cmd);
				new CommandTransfer().execute();
			}
			//break;
//			new CommandTransfer().execute();
		}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public class CommandTransfer extends AsyncTask<String, String, String>
	{

	    ProgressDialog pd;
	    protected void onPreExecute()
	    {
	    	super.onPreExecute();
	    	pd=new ProgressDialog(MainActivity.this);
	    	pd.setCancelable(false);
	    	pd.setMessage("Sending Data");
	    	pd.setTitle("Uploaing position..");
	    	pd.show();
	    	Log.d("inside onpre","onpre");
	    }
	   
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String str="";
			Log.d("inside inback","inside inback");
			List<NameValuePair> pdat=new ArrayList<NameValuePair>(2);
			pdat.add(new BasicNameValuePair("command",cmd));		
			
			HttpClient client=new DefaultHttpClient();
			HttpPost mypdat=new HttpPost(URL);
			Log.d("iinback","inside inback");
			try 
			{
				mypdat.setEntity(new UrlEncodedFormEntity(pdat));
				Log.d("post data","post data");
			} 
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			try 
			{
				Log.d("inside responce","response");
				HttpResponse re=client.execute(mypdat);
				HttpEntity entity=re.getEntity();
				str=EntityUtils.toString(entity);
				//Log.d("response",str);
				int status=re.getStatusLine().getStatusCode();
				Log.d("Staus code",""+status);
				if(status==200)
				{
					Log.d("insdie status check",str);
					return str;					
				}
			} 
			catch (ClientProtocolException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
				//return string if status 200 otherwise null
			
			
			
			
			return null;
		}
		
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			//Log.d("onpost",result);
			
			pd.dismiss();
		}
	}
}
