package com.sjsu.cmpe281.vmware.hw2;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
/**
 * @author Pratik Pandey.
 * 
 * This class contains all the operations which has to be performed after user has fired any
 * command and returns the result after performing the corresponding operation.  
 */
class CloudServerCommand
{
	/**
	 * Used for logging purposes.
	 */
	private final Logger logger = Logger.getLogger(CloudServerCommand.class.getName());
	/**
	 * Used to check the allowed pattern of IP Address. 
	 */
	private static final Pattern PATTERN = Pattern.compile(
	        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	private final Folder rootFolder;
	private final ServiceInstance serviceInstance;
	
	public CloudServerCommand(Folder rootFolder, ServiceInstance serviceInstance)
	{
		this.rootFolder = rootFolder;
		this.serviceInstance = serviceInstance;
	}
	/**
	 * This method is used to @return the list of all the available commands to User when 
	 * needed for execution.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> getHelpResponse()
	{
		List<String> responseList = new ArrayList<String>();
		responseList.add("\n-----------------------------------------------------------------------------");
		responseList.add("\t\t\t List of Available Commands");
		responseList.add("-----------------------------------------------------------------------------");
		responseList.add("exit" + "\t\t\t" + "Exit the program");
		responseList.add("help" + "\t\t\t" + "Print out the usage, e.g., the entire list of commands");
		responseList.add("host" + "\t\t\t" + "Enumerate all hosts");
		responseList.add("host hname info" + "\t\t" + "Show info of host hname, e.g., host 130.65.159.11 info");
		responseList.add("host hname datastore" + "\t"
				+ "Enumerate datastores of host hname, e.g., host 130.65.159.11 datastore");
		responseList.add(
				"Host hname network" + "\t" + "Enumerate datastores of host hname, e.g., host 130.65.159.11 network");
		responseList.add("vm" + "\t\t\t" + "Enumerate all virtual machines");
		responseList.add("vm vname info" + "\t\t" + "Show info of VM vname, e.g., vm demo-centos7-123 info");
		responseList.add("vm vname on" + "\t\t"
				+ "Power on VM vname and wait until task completes, e.g., vm demo-centos7-123 on");
		responseList.add("vm vname off" + "\t\t"
				+ "Power off VM vname and wait until task completes, e.g., vm demo-centos7-123 off");
		responseList.add("vm vname shutdown" + "\t" + "Shutdown guest of VM vname, e.g., vm demo-centos7-123 shutdown");
		return responseList;
	}
	/**
	 * This method is used to @return boolean and verify whether the provided IP address is correct or not.
	 */
	public Boolean checkIPAddress(String ipAddress) throws InvalidProperty, RuntimeFault, RemoteException
	{
	    return PATTERN.matcher(ipAddress).matches();
	}
	/**
	 * This method is used to @return the list of all hosts present in the remote vCenter Server.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> getAllHostList() throws InvalidProperty , RuntimeFault , RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity[] managedEntities = retrieveMultipleME(rootFolder, "HostSystem");
		if (managedEntities == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		if (managedEntities.length == 0)
		{
			logger.log(Level.INFO, "There are no host present");
			responseList.add("There are no host present");
		}
		else
		{
			for (int i = 0; i < managedEntities.length; i++)
			{
				HostSystem hostSystem = (HostSystem) managedEntities[i];
				if (hostSystem != null)
				{
					String ipAddress = hostSystem.getName();
					responseList.add("host[" + i + "]: Name = " + ipAddress);
				}
			}
		}
		return responseList;
	}
	/**
	 * This method is used to @return the list of all required information of a particular host present 
	 * in the remote vCenter Server.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> getHostInfo(String hostName) throws InvalidProperty , RuntimeFault , RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity managedEntity = retrieveSingleME(rootFolder, "HostSystem", hostName);
		if (managedEntity == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		else
		{
			HostSystem hs = (HostSystem) managedEntity;
			responseList.add("Name = " + managedEntity.getName());
			responseList.add("Product Full Name  = " + hs.getConfig().getProduct().getFullName());
			responseList.add("CPU Cores  = " + hs.getHardware().getCpuInfo().getNumCpuCores());
			responseList.add("RAM  = " + (((hs.getHardware().getMemorySize()/1024)/1024)/1024)+" GB.");
		}
		return responseList;
	}
	/**
	 * This method is used to @return the list of all required information of a particular VM present 
	 * in the remote vCenter Server.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> getVMInfo(String vmName) throws InvalidProperty, RuntimeFault, RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity managedEntity = retrieveSingleME(rootFolder, "VirtualMachine", vmName);
		if (managedEntity == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		else
		{
			VirtualMachine virtualMachine = (VirtualMachine) managedEntity;
			responseList.add("Name = " + virtualMachine.getName());
			responseList.add("GuestFullName = " + virtualMachine.getGuest().getGuestFullName()); 
			responseList.add("GuestState = " + virtualMachine.getGuest().getGuestState()); 
			responseList.add("IP addr = " + virtualMachine.getGuest().getIpAddress());
			responseList.add("Tool running status = " + virtualMachine.getGuest().getToolsRunningStatus());
			responseList.add("Power state = " + virtualMachine.getRuntime().getPowerState()); 
		}
		return responseList;
	}
	/**
	 * This method is used to @return the list of all VMs present in the remote vCenter Server.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> getAllVMList() throws InvalidProperty, RuntimeFault, RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity[] managedEntities = retrieveMultipleME(rootFolder, "VirtualMachine");
		if (managedEntities == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		if (managedEntities.length == 0)
		{
			logger.log(Level.INFO, "There are no host present");
			responseList.add("There are no host present");
		}
		else
		{
			for (int i = 0; i < managedEntities.length; i++)
			{
				VirtualMachine virtualMachine = (VirtualMachine) managedEntities[i];
				if (virtualMachine != null)
				{
					String vmName = virtualMachine.getName();
					responseList.add("vm[" + i + "]: Name = " + vmName);
				}
			}
		}
		return responseList;
	}
	/**
	 * This method is used to @return multiple managed entities based on inventory object.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	private ManagedEntity[] retrieveMultipleME(Folder rootFolder, String type)
			throws InvalidProperty , RuntimeFault , RemoteException
	{
		return new InventoryNavigator(rootFolder).searchManagedEntities(type);
	}
	/**
	 * This method is used to @return managed entity based on inventory object and particular VM or host.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	private ManagedEntity retrieveSingleME(Folder rootFolder, String type, String specific)
			throws InvalidProperty , RuntimeFault , RemoteException
	{
		return new InventoryNavigator(rootFolder).searchManagedEntity(type, specific);
	}
	/**
	 * This method is used to @return the list of all required information related to datastore 
	 * of particular host present in the remote vCenter Server.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> getHostDataStores(String hostName) throws InvalidProperty, RuntimeFault, RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity managedEntity = retrieveSingleME(rootFolder, "HostSystem", hostName);
		if (managedEntity == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		else{
			HostSystem hs = (HostSystem) managedEntity;
			Datastore[] ds = hs.getDatastores();
			responseList.add("Name = " + hostName);
			if (ds != null){
				for (int i = 0; i < ds.length; i++)
				{
					responseList.add("Datastore[" + i + "]: Name = " + ds[i].getSummary().getName() 
					+ ", Capacity = " + (((ds[i].getSummary().getCapacity()/1024)/1024)/1024) + " GB, FreeSpace = " 
					+ (((ds[i].getSummary().getFreeSpace()/1024)/1024)/1024) + " GB.");
				}
			}
		}
		return responseList;
	}
	/**
	 * This method is used to @return the list of all required information related to network 
	 * of particular host present in the remote vCenter Server.
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> getHostNetworks(String hostName) throws InvalidProperty, RuntimeFault, RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity managedEntity = retrieveSingleME(rootFolder, "HostSystem", hostName);
		if (managedEntity == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		else{
			HostSystem hs = (HostSystem) managedEntity;
			Network []network = hs.getNetworks();
			responseList.add("Name = " + hostName);
			if (network != null){
				for (int i = 0; i < network.length; i++)
				{
					responseList.add("Network[" + i + "]: Name = " + network[i].getName());
				}
			}
		}
		return responseList;
	}
	/**
	 * This method is used to start a particular VM and @return the list of all required 
	 * information like status and completion time. 
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> startVM(String vmName) throws InvalidProperty, RuntimeFault, RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity managedEntity = retrieveSingleME(rootFolder, "VirtualMachine", vmName);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		if (managedEntity == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		else{
			VirtualMachine virtualMachine = (VirtualMachine) managedEntity;
			Task task = virtualMachine.powerOnVM_Task(null);
			responseList.add("Name = " + virtualMachine.getName());
			if(task.waitForMe()==Task.SUCCESS)
			{
				responseList.add("Power on VM: status = " + "success" + ", completion time = " + sdf.format(task.getTaskInfo().getCompleteTime().getTime()));
			}
			else{
				responseList.add("Power on VM: status = " + "The attempted operation cannot be performed in the current state (Powered on)." + ", completion time = " + sdf.format(task.getTaskInfo().getCompleteTime().getTime()));
			}	
		}
		return responseList;
	}
	/**
	 * This method is used to stop a particular VM and @return the list of all required 
	 * information like status and completion time. 
	 * @throws InvalidProperty, RuntimeFault, RemoteException
	 */
	public List<String> stopVM(String vmName) throws InvalidProperty, RuntimeFault, RemoteException
	{
		List<String> responseList = new ArrayList<String>();
		ManagedEntity managedEntity = retrieveSingleME(rootFolder, "VirtualMachine", vmName);
		if (managedEntity == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		else
		{
			VirtualMachine virtualMachine = (VirtualMachine) managedEntity;
			Task task = virtualMachine.powerOffVM_Task();
			responseList.add("Name = " + virtualMachine.getName());
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			if(task.waitForMe()==Task.SUCCESS)
			{
				responseList.add("Power off VM: status = " + "success" + ", completion time = " +  sdf.format(task.getTaskInfo().getCompleteTime().getTime()));
			}
			else{
				responseList.add("Power off VM: status = " + "The attempted operation cannot be performed in the current state (Powered off)." + ", completion time = " +  sdf.format(task.getTaskInfo().getCompleteTime().getTime()));
			}
		}
		return responseList;
	}
	/**
	 * This method is used to shutdown a particular VM and @return the list of all required 
	 * information like status and completion time. 
	 * @throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException 
	 */
	public List<String> shutdownVM(String vmName) throws InvalidProperty, RuntimeFault, RemoteException, InvalidProperty, RuntimeFault, RemoteException, InterruptedException
	{
		List<String> responseList = new ArrayList<String>();
		int defaultTimeOut = 60*1000*3;
		ManagedEntity managedEntity = retrieveSingleME(rootFolder, "VirtualMachine", vmName);
		if (managedEntity == null)
		{
			logger.log(Level.SEVERE, "There was some problem in accessing the server");
			responseList.add("There was some problem in accessing the server");
		}
		else
		{
			VirtualMachine virtualMachine = (VirtualMachine) managedEntity;
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			responseList.add("Name = " + virtualMachine.getName());	
			virtualMachine.shutdownGuest();
		   boolean isConditionMet = true;
	       while (isConditionMet && defaultTimeOut>0) {
	        	/*
				 * By knowing the fact that after shutdown of any VM, the VM automatically changes its state
				 * to poweredoff. After this condition met, I instantly get the current time of the same VM
				 * which becomes the completion time of this operation. If this condition is not met, then I 
				 * wait for 1 sec and try to check it again and again untill the condition met.
				 */
	            isConditionMet = ((virtualMachine.getRuntime().getPowerState().toString()).equals("poweredOff"));
	            //System.out.println("isConditionMet"+isConditionMet);
	            if (isConditionMet) {
	            	isConditionMet = false; 
	            	responseList.add("Shutdown guest: completed"+", time = " + sdf.format(serviceInstance.currentTime().getTime()));
	            	break;
	            } else {
	                try
					{
						Thread.sleep(1000); //Wait for 1 Seconds.
						isConditionMet = true;
						defaultTimeOut -=1000;
						//System.out.println("defaultTimeOut"+defaultTimeOut);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
	            }
	        }
		}
		return responseList;
	}

}