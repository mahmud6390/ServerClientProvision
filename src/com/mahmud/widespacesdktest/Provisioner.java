
package com.mahmud.widespacesdktest;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import Interfaces.IProvisionListener;
import Interfaces.IProvisionStatusObsrever;
import android.net.Uri;
import android.util.Log;

/**
 * This class demonstrates to contact with server side to make sure the
 * provision status and notify with all listener.It's implement two interface
 * class to notify the other listener and another interface using to add and
 * remove listener with this provision.
 * 
 * @author Mahmud
 * 
 */

public class Provisioner implements IProvisionListener,
		IProvisionStatusObsrever {

	private static Provisioner mProvisioner = null;

	public static ProvisionStatus provisionStatus = null;
	private CopyOnWriteArrayList<IProvisionStatusObsrever> mProvisionListenerList = new CopyOnWriteArrayList<IProvisionStatusObsrever>();
	private HashMap<String, Object> mapStoreKey=new HashMap<String, Object>();
	/**
	 * constant key names.Using to overcome typing mistake
	 */
	private static final String IS_SDK_ENABLED = "is_sdk_enable";
	private static final String SESSION_CRYPTO_KEY = "session_key";
	private static final String SESSION_KEY_INDEX = "key_index";

	private static final String URL = "http://engine.widespace.com/map/provisioning";
	private static final String KEY_APPID = "appId";
	private static final String KEY_SDKVER = "sdkVer";
	private static final String KEY_PLATFORM = "platform";

	private static final String VALUE_APPID = "com.widespace.wp.testapp";
	private static final String VALUE_SDKVER = "4.1.0";
	private static final String VALUE_PLATFORM = "ANDROID";

	/**
	 * For thread safe,this will do the same thing without having to check for
	 * instance every time. static is the same as check first time.
	 */
	static {
		mProvisioner = new Provisioner();
		provisionStatus =ProvisionStatus.UNPROVISIONED;
	}

	public static Provisioner getProvisionerInstance() {
		return mProvisioner;
	}

	private Provisioner() {
	}

	public enum ProvisionStatus {
		UNPROVISIONED, IN_PROGRESS, DONE, FAILED
	};

	/**
	 * This method is used for provisioning.First it get the server url by
	 * getServerUrl(URL) method.URL is a constant key string.If the url is not
	 * null then it sent to HTTP request by sendHttpRequest(url)
	 * method.Otherwise it's change the provision status ENUM value as FAILED.
	 * 
	 * @see #getServerUrl
	 * @see #sendHttpRequest
	 */

	public void provision() {
		
			String url = getServerUrl(URL);
			 System.out.println(url);
			if (url != null) {
				sendHttpRequest(url);
			} else {
				provisionStatus = Provisioner.ProvisionStatus.FAILED;

			}
		

	}

	/**
	 * This method used to Parse the server url via Uri.Builder.Use buildUpon()
	 * to obtain a builder representing an existing URI.Then append the query
	 * parameters as key,value pair of this uribuilder.Here key and value
	 * showing as constant variable to overcome the typing mistake
	 * 
	 * @param url
	 *            parse the url by Uri builder
	 * @return server url
	 * @throws UnsupportedOperationException
	 *             if the URI is opaque and the scheme is null
	 * @see android.net.Uri.Builder
	 */

	private String getServerUrl(String url) {
		Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
		uriBuilder.appendQueryParameter(KEY_APPID, VALUE_APPID);
		uriBuilder.appendQueryParameter(KEY_SDKVER, VALUE_SDKVER);
		uriBuilder.appendQueryParameter(KEY_PLATFORM, VALUE_PLATFORM);
		String urlString = null;
		try {
			urlString = uriBuilder.build().toString();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();

		}
		return urlString;
	}

	/**
	 * This method use to received server response data from http request.First it change the status as IN_PROGRESS mode.It
	 * execute on url by aync task class HttpRequestTask by
	 * httpRequest.execute(url).From HttpRequestTask class it call back two
	 * method as postHttpResponse and postErrorResponse.If server send the
	 * postHttpResponse callback it's mean we got the response data.Then it sent
	 * this to parse via parseProvisioningReply.If server response not found
	 * then it send the error response via postErrorResponse callback.After this
	 * we change the provision status as FAILED;
	 * 
	 * @see HttpRequestTask
	 * @param url
	 *            send http request with this url
	 *            @see #postHttpResponse
	 *            @see #postErrorResponse
	 */

	private void sendHttpRequest(String url) 
	{
		
		provisionStatus = ProvisionStatus.IN_PROGRESS;
		HttpRequestTask httpRequest = new HttpRequestTask() {

			@Override
			public void postHttpResponse(String responseData) {
				parseProvisioningReply(responseData);

			}

			@Override
			public void postErrorResponse() {
				provisionStatus = ProvisionStatus.FAILED;
				Log.e("Get error report", provisionStatus + "");

			}
		};
		httpRequest.execute(url);

	}

	/**
	 * This method work to parse server response raw string data. Here
	 * optJSONObject,optBoolean, working to returns the value mapped by name if
	 * it exists and is a JSONObject. After successfully parse it store the
	 * Object value as a HashMap with key name.Then it goes to
	 * onProvisionComplete method.
	 * 
	 * @param response
	 *            Server response string data
	 * 
	 * @see org.json.JSONObject.optJSONObject
	 * @see org.json.JSONObject.optBoolean
	 * @see org.json.JSONObject.optString
	 * @see org.json.JSONObject.optInt
	 * @see #storeKey
	 * @see #onProvisionCompleted
	 * @throws JSONException
	 *             If JSON parse exception occured
	 * @throws NullPointerException
	 *             If NULL value/Object try to parse
	 * 
	 * 
	 */
	private void parseProvisioningReply(String response) {
		JSONObject jsonObj;

		try {
			Log.v("response", response);
			jsonObj = new JSONObject(response);
			boolean status = jsonObj.optBoolean("sdkEnabled");

			JSONObject sessionObj = jsonObj.optJSONObject("sessionInfo");
			String key = sessionObj.optString("key");
			int keyIndex = sessionObj.optInt("keyIndex");
			 Log.v("Json values>>>", key + ":" + keyIndex+":"+status);
			storeKey(IS_SDK_ENABLED, status);
			storeKey(SESSION_CRYPTO_KEY, key);
			storeKey(SESSION_KEY_INDEX, keyIndex);

			onProvisionCompleted();
		} catch (JSONException e) {
			provisionStatus = ProvisionStatus.FAILED;
			System.out.println(provisionStatus + "");
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (NullPointerException e) {
			provisionStatus = ProvisionStatus.FAILED;
			e.printStackTrace();
		}
	}

	/**
	 * This method used when the provision is complete.It's called after
	 * complete parse server response data.It's a callback method which comes
	 * after implements IProvisionStatusObsrever interface.It change the
	 * provision status as DONE and call remove listener method to remove this
	 * listener though it is complete.
	 */

	@Override
	public void onProvisionCompleted() {
		provisionStatus = ProvisionStatus.DONE;
		removeListener();

		Log.v("On provision completed FROM provisionerclass", provisionStatus
				+ "");

	}

	/**
	 * This method also a callback method which call from Adspace class when
	 * init() method called. By this method it add the listener in a
	 * CopyOnWriteArrayList.Though it add the listener via thread here using
	 * CopyOnWriteArrayList to protect ConcurrentModificationException.
	 * 
	 * @param listner
	 *            IProvisionStatusObsrever type listener
	 * @see java.util.concurrent.CopyOnWriteArrayList
	 */

	@Override
	public void addListener(IProvisionStatusObsrever listner) {

		Log.v("add listener Iterater", "add");
		mProvisionListenerList.add(listner);

	}

	/**
	 * This method also a callback method which call when a listener provision
	 * status is DONE.It's notify all listener via onProvisionCompleted.First it
	 * Iterator on CopyOnWriteArrayList to find the listeners.After notify it's
	 * remove this listener by remove() method.Though add and remove listener
	 * work on a list and it's run by thread it's occurred concurrent
	 * exception.To protect this we are using CopyOnWriteArrayList
	 * 
	 * 
	 * @see java.util.concurrent.CopyOnWriteArrayList
	 * @throws ConcurrentModificationException
	 * @throws UnsupportedOperationException
	 */

	@Override
	public void removeListener() 
	{
		
		Iterator<IProvisionStatusObsrever> it = mProvisionListenerList
				.iterator();
		while (it.hasNext()) {
			try {
				IProvisionStatusObsrever provisionListener = it.next();
				provisionListener.onProvisionCompleted();
				it.remove();
				Log.v("remove listener Iterater", "remove");
			} catch (ConcurrentModificationException e) {
				e.printStackTrace();
			}
			catch (UnsupportedOperationException e) 
			{
				e.printStackTrace();
				// TODO: handle exception
			}

			
		}
	}

	/**
	 * This method use to read any Object from hashMap via keyname.
	 * 
	 * @param keyName
	 *            pass the keyname to read from HashMap.
	 * @return Object return Object from HashMap of this key
	 */
	public Object readKey(String keyName) {
		// TODO Auto-generated method stub
		return (Object) mapStoreKey.get(keyName);
	}

	/**
	 * This method use after complete parse the json data in parse.It's store
	 * the value in a HaspMap with key,value pair.It's called from
	 * parseProvisioningReply method.
	 * 
	 * @see #parseProvisioningReply(String)
	 * 
	 * @param keyName
	 *            name the key to store the value
	 * @param keyValue
	 *            value to store
	 */

	private void storeKey(String keyName, Object keyValue) {
		mapStoreKey.put(keyName, keyValue);
		// TODO Auto-generated method stub

	}

}
