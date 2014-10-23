
package com.mahmud.widespacesdktest;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.

import Interfaces.IProvisionStatusObsrever;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class demonstrates how to use the Ad library to register in View.AdSpace
 * that customizes the accessibility behavior of a View. Aiming to maximize
 * simplicity this example tweaks the text reported to accessibility services
 * but using these APIs a client can inject any accessibility Ad functionality
 * into a View.
 * 
 * @author Mahmud
 */
public class AdSpace implements IProvisionStatusObsrever {
	// constant value for runAd and prefetchAd differentiate in message queue
	protected static final int RUNADMESSAGE = 1;
	protected static final int PREFETCHMESSAGE = 2;

	// single tone Provisioner class with thread safe

	private Provisioner mProvisioner;
	// Runnable using for callbackfunction

	Runnable runnableRunAd;
	Message message = new Message();

	IProvisionStatusObsrever listener;

	public AdSpace() {
		// get the singletone object of Provisioner class.
		mProvisioner = Provisioner.getProvisionerInstance();
		listener = this;

	}

	/**
	 * The runAd method will check the provisioner and if the AdSpace is already
	 * provisioned then it will fire an internal callback which will log a
	 * message(“runAd() completed”) via a handler.If a user calls runAd before
	 * the provisioner is completed then only last runAd method will be
	 * triggered.So that we remove the last runnable from handler by
	 * removeCallbacks method.In runnable run method it's check the provision
	 * status by checkProvisionStatus method and post the listener in a handler.
	 * 
	 * @see #checkProvisionStatus(Runnable, int)
	 */
	public void runAd() {
		// Log.v("runad", Provisioner.provisionStatus + "");
		handler.removeCallbacks(runnableRunAd);

		runnableRunAd = new Runnable() {

			@Override
			public void run() {
				checkProvisionStatus(this, RUNADMESSAGE);
			}

		};
		handler.post(runnableRunAd);
	}

	/**
	 * The prefetchAd method will act almost same as the runAd method, but only
	 * difference is that it will store its callbacks into a queue.If a user
	 * calls prefetchAd method then the prefetch callbacks will be fired
	 * sequentially after the provisioner is completed.so that we post every
	 * prefetch runnable in a handler.In runnable run method it's check the
	 * provision status by checkProvisionStatus and post the listener in a
	 * handler.
	 * 
	 * @see #checkProvisionStatus(Runnable, int)
	 */

	public void prefetchAd() {
		Log.v("prefetchAd", Provisioner.provisionStatus + "");

		Runnable runnablePrefetchAd = new Runnable() {

			@Override
			public void run() {
				Log.v("provision ", Provisioner.provisionStatus + "");
				checkProvisionStatus(this, PREFETCHMESSAGE);

			}

		};
		handler.post(runnablePrefetchAd);
	}

	/**
	 * This method run from runAd() or prefetchAd() method to check the
	 * provision status.If the provision status is DONE it'll just send a
	 * message to handler to update the UI thread.
	 * If the provision is in FAILED or UNPROVISIONED it'll call the init method
	 * to communicate with http request and postthis runnable in handler.If the
	 * provision is it INPROGRESS stage it just return and post in a handler
	 * 
	 * @param r
	 *            pass runnable to post on a handler
	 * @param messageType
	 *            differentiate the message queue in handler after provision is
	 *            DONE
	 * @see #init()
	 */

	private void checkProvisionStatus(Runnable r, int messageType) {
		if (Provisioner.provisionStatus == Provisioner.ProvisionStatus.DONE) {
			if (messageType == RUNADMESSAGE) {
				message.what = RUNADMESSAGE;
			} else {
				message.what = PREFETCHMESSAGE;
			}

			handler.sendMessage(message);

		} else if ((Provisioner.provisionStatus == Provisioner.ProvisionStatus.FAILED)
				|| (Provisioner.provisionStatus == Provisioner.ProvisionStatus.UNPROVISIONED)) {

			init();// again start from hhtprequest;
			handler.post(r);

		} else// in progress
		{

			handler.post(r);
		}

	}

	/**
	 * This method call if the provision is in FAILED orUNPROVISION stage.It's
	 * pass the listener of this class object to provision class addListener
	 * method.It's also call the provision method of provisioner class to start
	 * HTTP request.
	 */

	private void init() {
		mProvisioner.addListener(listener);
		mProvisioner.provision();

	}

	/**
	 * This Handler using just working in a UI thread.After receive message it's
	 * just print the message.It's working on it's message queue.If we want to
	 * show something(video,add,image etc.) we can use this by handle message.
	 */

	private static Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RUNADMESSAGE:
				System.out.println("runAd() completed");
				break;
			case PREFETCHMESSAGE:
				System.out.println("prefetchAd() completed");
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		};

	};

	/**
	 * This is callBack method from Provision class.After complete the provision
	 * it's pass the status to AdSpace class
	 */

	@Override
	public void onProvisionCompleted() {

		System.out.println("provision complete from adspace"
				+ Provisioner.provisionStatus + "");

		// mProvisioner.removeListener(this);

	}

}
