package com.sjsu.cmpe281.vmware.hw2;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.vim25.InvalidPowerState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ServiceInstance;
/**
 * @author Pratik Pandey.
 * 
 * This is the main class which act as command line which takes care of taking the inputs and handing over
 * it to CloudServer class either for making connection or disconnect and once connected take inputs from user, 
 * segregate it and perform the operations based on commands by making use of methods of CloudServerCommand class.
 */
public class VCenterServerCommandLine
{
	/**
	 * Used for logging purposes.
	 */
	private Logger logger = Logger.getLogger(VCenterServerCommandLine.class.getName());
	/**
	 * To store the object of root folder.
	 */
	private Folder cloudServerFolder;
	/**
	 * To store the object of service instance.
	 */
	private ServiceInstance serviceInstance;

	public static void main(String[] args) throws Exception
	{
		VCenterServerCommandLine vCenterServerCommandLine = new VCenterServerCommandLine();
		vCenterServerCommandLine.startExecution(args);
	}
	/**
	 * This method is used to take inputs from the command line as arguments and connects with the remote vCenter Server. 
	 * Further acts as a command line by taking inputs and performing corresponding operations. 
	 * @throws RemoteException 
	 * @throws RuntimeFault 
	 */
	private void startExecution(String[] args) throws RuntimeFault, RemoteException
	{
		Scanner scanner = new Scanner(System.in);
		CloudServer cloudServer = null;
		String value = null;
		try
		{
			System.out.println("CMPE 281 HW2 from Pratik Pandey");
			System.out.println("Welcome to Command line vCenter Cloud Access System");
			String ipAddress = getValue("-ip=", args);
			String username = getValue("-username=", args);
			String password = getValue("-password=", args);
			
			cloudServer = instantiateCloudServer(ipAddress, username, password, args);
			if (cloudServer != null)
			{
				cloudServerFolder = cloudServer.getCloudServerRootFolder();
				serviceInstance = cloudServer.getServerInstance();			
			}
			
			System.out.println("Successfully connected to vCenter Server!");
			System.out.println("Pratik-256>");
			value = scanner.nextLine();
			CloudServerCommand cloudServerCommand = new CloudServerCommand(cloudServerFolder, serviceInstance);
			do
			{
				String command = value;
				String[] cmdArrray = command.split(" ");
				List<String> responseList = new ArrayList<String>();
				boolean ipFlag = false;
				/*
				 * Performing various operations based on inputs as commands.
				 */
				if (cmdArrray[0].equalsIgnoreCase("help"))
				{
					responseList = cloudServerCommand.getHelpResponse();
				}
				else if (cmdArrray[0].equalsIgnoreCase("vm"))
				{
					if (cmdArrray.length > 2)
					{
						String vmName = cmdArrray[1];
						if (cmdArrray[2].equalsIgnoreCase("info"))
						{
							responseList = cloudServerCommand.getVMInfo(vmName);
						}
						else if (cmdArrray[2].equalsIgnoreCase("on"))
						{
							responseList = cloudServerCommand.startVM(vmName);
						}
						else if (cmdArrray[2].equalsIgnoreCase("off"))
						{
							responseList = cloudServerCommand.stopVM(vmName);
						}
						else if (cmdArrray[2].equalsIgnoreCase("shutdown"))
						{
							responseList = cloudServerCommand.shutdownVM(vmName);
						}
						else
						{
							System.out.println("Invalid Command, try again!!");
						}
					}
					else
					{
						responseList = cloudServerCommand.getAllVMList();
					}
				}
				else if (cmdArrray[0].equalsIgnoreCase("host"))
				{
					if (cmdArrray.length > 2)
					{
						String hostName = cmdArrray[1];
						ipFlag = cloudServerCommand.checkIPAddress(hostName);
						if(ipFlag){
							if (cmdArrray[2].equalsIgnoreCase("info"))
							{
								responseList = cloudServerCommand.getHostInfo(hostName);
							}
							else if (cmdArrray[2].equalsIgnoreCase("datastore"))
							{
								responseList = cloudServerCommand.getHostDataStores(hostName);
							}
							else if (cmdArrray[2].equalsIgnoreCase("network")) 
							{
								responseList = cloudServerCommand.getHostNetworks(hostName);
							}
							else
							{
								System.out.println("Invalid Command, try again!!");
							}
						}else{
							System.out.println("Invalid Host IP Address "+hostName);
						}
					}
					else
					{
						responseList = cloudServerCommand.getAllHostList();
					}
				}
				else if (cmdArrray[0].equalsIgnoreCase("exit"))
				{
					break;
				}
				printResponse(responseList);
				System.out.println("Pratik-256>");
				value = scanner.nextLine();
			}
			while (!value.equalsIgnoreCase("exit"));

		}catch(InvalidPowerState e){
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			System.out.println();
			System.out.println("********************************************************************************************");
			System.out.println("\t\t\t\t  Error Found");
			System.out.println("********************************************************************************************");
			System.out.println("The last attempted operation cannot be performed due to \"" 
					+ e.toString() + "\"\nerror occurred.");
			System.out.println();
			System.out.println("********************************************************************************************");
			System.out.println("\t\t\t\t  Description");
			System.out.println("********************************************************************************************");
			System.out.println("Virtual Machine is in an invalid power state !!!");
			System.out.println();
			System.out.println("********************************************************************************************");
			System.out.println("\t\t\t\t    Reason");
			System.out.println("********************************************************************************************");
			System.out.println("The reason behind this error is very simple that after shutdown of any virtual machine, the"
					+ "\nvirtual machine automatically changes its state to poweredoff. If you are trying to attain"
					+ "\nany state for any virtual machine whether it may be poweredOn or poweredOff or suspended"
					+ "\nwhich the virtual machine has already attained. It is obvious to get an error!!!");
			System.out.println();
			try
			{
				System.out.println("Time : "+sdf.format(serviceInstance.currentTime().getTime())+" when error occurred.");
				
			}
			catch (RuntimeFault e1)
			{
				e1.printStackTrace();
			}
			catch (RemoteException e1)
			{
				e1.printStackTrace();
			}
		}
		catch (Exception ex)
		{

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			System.out.println();
			System.out.println("********************************************************************************************");
			System.out.println("\t\t\t\t  Error Found");
			System.out.println("********************************************************************************************");
			System.out.println("The last attempted operation cannot be performed due to \"" 
					+ ex.toString() + "\"\nerror occurred.");
			System.out.println();
			System.out.println("Time : "+sdf.format(serviceInstance.currentTime().getTime())+" when error occurred.");
			System.out.println();
		}
		finally
		{
			System.out.println("********************************************************************************************");
			System.out.println("********************************************************************************************");
			scanner.close();
			if (cloudServer != null)
			{
				cloudServer.logoutFromCloudService();
			}
			System.out.println("Thank you for using Pratik's VCenter Command Line Server!! GoodBye!!");
		}
	}
	/**
	 * This method is used to pass the inputs to Cloud Server class taken either by defaults or by command line 
	 * arguments for connection with the remote vCenter Server.
	 */
	private CloudServer instantiateCloudServer(String ipAddress, String username, String password, String[] args)
	{
		CloudServer cloudServer = null;
		boolean useDefaultLogin = Boolean.parseBoolean(getValue("-default=", args));

		try
		{
			if(useDefaultLogin)
			{
				cloudServer = new CloudServer();	
			}
			else
			{
				cloudServer = new CloudServer(ipAddress, username, password);	
			}
		}
		catch (MalformedURLException | RemoteException e)
		{
			logger.log(Level.SEVERE, "Exception found while getting cloud server folder");
			e.printStackTrace();
		}

		return cloudServer;
	}
	/**
	 * This method is used to @return the value in String of the parameter passed as argument in command line.
	 */
	private String getValue(String parameterName, String[] args)
	{
		for (String value : args)
		{
			if (value.startsWith(parameterName))
			{
				return value.substring(parameterName.length());
			}
		}
		return null;
	}
	/**
	 * This method is used to print the results of the commands after being processed.
	 */
	private void printResponse(List<String> responseList)
	{
		for (String response : responseList)
		{
			System.out.println(response);
		}
	}
}
