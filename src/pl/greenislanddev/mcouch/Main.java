package pl.greenislanddev.mcouch;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.android.http.AndroidHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import pl.greenislanddev.mcouch.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.Intents.CouchbaseError;
import com.couchbase.android.Intents.CouchbaseStarted;

public class Main extends Activity {

	public static final String TAG = "Main";

	private static ServiceConnection couchServiceConnection;
	private static HttpClient httpClient;

	protected CouchDbInstance dbInstance;
	protected CouchDbConnector couchDbConnector;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Intent received: " + intent);
			if (CouchbaseStarted.ACTION.equals(intent.getAction())) {
				String host = CouchbaseStarted.getHost(intent);
				int port = CouchbaseStarted.getPort(intent);
				Log.i(TAG, "CouchbaseStarted on " + host + ":" + port);
				startEktorp(host, port);
			} else if (CouchbaseError.ACTION.equals(intent.getAction())) {
				String message = CouchbaseError.getMessage(intent);
				Log.e(TAG, message);
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		registerReceiver(mReceiver, new IntentFilter(CouchbaseStarted.ACTION));
		registerReceiver(mReceiver, new IntentFilter(CouchbaseError.ACTION));

		startCouchbase();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		unbindService(couchServiceConnection);
		shutdownClient();
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	public void startCouchbase() {
		Log.v(TAG, "starting Couchbase");
		CouchbaseMobile couch = new CouchbaseMobile(getBaseContext());
		couchServiceConnection = couch.startCouchbase();
	}

	protected void startEktorp(String host, int port) {
		Log.v(TAG, "starting ektorp");
		shutdownClient();

		httpClient = new AndroidHttpClient.Builder().host(host).port(port).maxConnections(100).build();
		dbInstance = new StdCouchDbInstance(httpClient);

		Log.v(TAG, "Couchbase started. Time to relax!");
	}

	private void shutdownClient() {
		if (httpClient != null) {
			httpClient.shutdown();
		}
	}
}