package Interfaces;

/**
 * This interface class using as a callback method from provisioner class and
 * adspace class.When the provision is complete it pass the callback to adspace
 * class via this class;
 * 
 * @author Mahmud
 * 
 */

public interface IProvisionStatusObsrever {
	/**
	 * This method use as a callback method in AdSpace and Provosioner
	 * class.When provision status is DONE and it notify with it's listener then
	 * listener class override this method
	 */

	public void onProvisionCompleted();

}
