
package com.mahmud.widespacesdktest;

import java.net.SocketTimeoutException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import Interfaces.IHttpResponseListener;
import android.os.AsyncTask;
import android.util.Log;

/**
 * HttpRequestTask is a abstract class which extends AyncTask class to work in
 * a background process.Which are working separately from UI thread.It's
 * implement IHttpResponseListener interface to callback the parent class after
 * getting response data from server.
 * 
 * @see android.os.AsyncTask
 * 
 * @author Mahmud
 * 
 */
public abstract class HttpRequestTask extends AsyncTask<String, Void, String>
		implements IHttpResponseListener {
	/**
	 * This method start when any class execute this class.In this method it's
	 * working as a pre Execute.After onPreExecute it's autometically goes to
	 * background method.
	 */

	@Override
	protected void onPreExecute() {

		Log.v("Provision status in onPreExecute", Provisioner.provisionStatus
				+ "");

	}

	/**
	 * This method work in a background process to connect with server,get
	 * response.
	 * HttpParams Represents a collection of HTTP protocol and framework
	 * parameters. Here total connection timeout is 3 sec so it's divided into
	 * connection timeout and data received timeout. After connection establish
	 * send httpParams to HTTPClient class. To execute HTTP requests while
	 * handling cookies, authentication, connection management, and other
	 * features. Thread safety of HTTP clients depends on the implementation and
	 * configuration of the specific client. Then we send the data as GET method
	 * via HttpGet class.After execute the request via httpClient class we
	 * receive the response which as HttpResponse formate.To make this as String
	 * Format we using EntityUtils.toString(res.getEntity());
	 * 
	 * @param serverUrl
	 *            this is VarArg and it receive the server urls
	 * @throws ConnectTimeoutException
	 *             if can not connect with server within 2 sec
	 * @throws SocketTimeoutException
	 *             if can not connect with server within 1 sec
	 * @throws Exception
	 *             Other exception comes
	 * @return String After getting response it return the string value of this
	 *         response or null
	 */

	@Override
	protected String doInBackground(String... serverUrl) {
		String response = null;
		try {
			
			Log.v("Provision status", Provisioner.provisionStatus + "");
			String url = serverUrl[0];
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is
			// established.Total 3 Second,It consider 2 sec for connection
			// establish and 1 second to data receive.
			int timeoutConnection = 2000;
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 1000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			// Create a new HttpClient and Post Header
			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpGet getRequest = new HttpGet(url);
			HttpResponse res = httpClient.execute(getRequest);
			response = EntityUtils.toString(res.getEntity());

			System.out.println("Response: " + response);

		} catch (ConnectTimeoutException e) {

			e.printStackTrace();
		} catch (SocketTimeoutException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return response;
	}

	/**
	 * This method execute after complete the background process. In this method
	 * we send the response string to provisioner class via callback
	 * postHttpResponse.If the server response string is null then it fire the
	 * callback method postErrorResponse to provisioner class.
	 */

	@Override
	protected void onPostExecute(String result) {

		System.out.println(result + "");
		if (result != null) {
			postHttpResponse(result);
		} else {
			postErrorResponse();
		}

	}

	/**
	 * This method use if response data is null.Sometimes any timeout exception
	 * occured some calling already executed in background.But we finished the
	 * call and back to the handler to execute this again as the provision
	 * status is FAILED
	 */
	@Override
	protected void onCancelled(String result) {
		// TODO Auto-generated method stub
		postErrorResponse();
	}

}
