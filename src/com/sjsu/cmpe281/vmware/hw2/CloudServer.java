package com.sjsu.cmpe281.vmware.hw2;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ServiceInstance;
/**
 * @author Pratik Pandey.
 * 
 * This class contains only server related data and operations like making connection with the server and to 
 * gracefully disconnect with server.
 */
public class CloudServer
{
	/**
	 * Used for logging purposes.
	 */
	private final Logger logger = Logger.getLogger(CloudServer.class.getName());
	/**
	 * The below default constant values are only used if one of the argument sets the default value to true 
	 * (like -default=true) otherwise it will read the values from command line arguments. 
	 */
	private static final String DEFAULT_IP = "130.65.159.14";
	private static final String DEFAULT_LOGIN_USERNAME = "cmpe281_sec3_student@vsphere.local";
	private static final String DEFAULT_LOGIN_PASSWORD = "cmpe-LXKN";
	/**
	 * The below constants are used to build the URL for connection purpose.
	 */
	private static final String CLOUD_SERVER_URL = "/sdk/vimService";
	private static final String URL_PROTOCOL = "https://";

	private String ipAddress;
	private String username;
	private String password;
	
	private ServiceInstance serviceInstance;
	/**
	 * Used when taking the input values as arguments from command line.
	 */
	public CloudServer(String ipAddress, String username, String password) throws MalformedURLException, RemoteException
	{
		this.ipAddress = ipAddress;
		this.username = username;
		this.password = password;
		serviceInstance = getServerInstance();
	}
	/**
	 * Used when using default contant values.
	 */
	public CloudServer() throws MalformedURLException, RemoteException
	{
		this.ipAddress = DEFAULT_IP;
		this.username = DEFAULT_LOGIN_USERNAME;
		this.password = DEFAULT_LOGIN_PASSWORD;
		serviceInstance = getServerInstance();
	}
	/**
	 * This method is used to @return the service instance object after successful connection with the 
	 * remote vCenter Server.
	 */
	public ServiceInstance getServerInstance() throws MalformedURLException, RemoteException
	{
		logger.log(Level.INFO, "Using IP : "+ipAddress + " Username : " + username + " Password : " + password);
		
		URL serverUrl = new URL(CloudServer.URL_PROTOCOL + ipAddress + CloudServer.CLOUD_SERVER_URL);
		return new ServiceInstance(serverUrl, username , password , true);
	}
	/**
	 * This method is used to @return the root folder object by using service instance object.
	 */
	public Folder getCloudServerRootFolder() throws MalformedURLException, RemoteException
	{
		logger.log(Level.INFO, "Trying to get the root folder from the cloud server");
		return serviceInstance.getRootFolder();		
	}
	/**
	 * This method is used to gracefully close the connection.
	 */
	public void logoutFromCloudService()
	{
		logger.log(Level.INFO, "Logging out from server");
		serviceInstance.getServerConnection().logout();
	}
}
