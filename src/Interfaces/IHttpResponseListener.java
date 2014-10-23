
package Interfaces;

/**
 * IHttpResponseListener is an interface class to using the post message after
 * getting result from server.
 * 
 * @author Mahmud
 * 
 */
public interface IHttpResponseListener {
	/**
	 * This method call when it receive the server response data and pass the
	 * callback to it's parent class.
	 * 
	 * @param responseData
	 *            server response data
	 */
	public void postHttpResponse(String responseData);

	/**
	 * This method use to callback the server response to provisioner class.If
	 * server response is null or any exception occured this method fire the
	 * callback to provisioner class
	 */
	public void postErrorResponse();

}
