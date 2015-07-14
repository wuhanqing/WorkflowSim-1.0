package org.workflowsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

public class HostComsumptionMonitorOld extends SimEntity
{

	protected List<? extends CondorVM> vmList;
	protected List<Double> napTimeList;
	protected List<Double> sleepTimeList;
	protected List<Double> deeperSleepTimeList;
	protected List<Integer> hostStateList;
	protected List<Double> hostSleepTimeList;
	protected List<Host> hostList;
	protected Map<Integer, List> vmToHostList;
	protected EnergySavingStatistics energySavStaic;
	protected List<CondorVM> vmInHostList;
	protected Datacenter datacenter;
	protected List<Double> hostWStatistics;

	public HostComsumptionMonitorOld(String name) throws Exception
	{
		super(name);
		setNapTimeList(new ArrayList<Double>());
		setSleepTimeList(new ArrayList<Double>());
		setDeeperSleepTimeList(new ArrayList<Double>());
		setHostStateList(new ArrayList<Integer>());
		setHostSleepTimeList(new ArrayList<Double>());
		energySavStaic = new EnergySavingStatistics();
		vmToHostList = new HashMap<Integer, List>();
		vmInHostList = new ArrayList<CondorVM>();
		hostList = new ArrayList<Host>();
		hostWStatistics = new ArrayList<Double>();
		// hostList = datacenter.getHostList();
		setId(super.getId());

	}

	@Override
	public void processEvent(SimEvent ev)
	{
		switch (ev.getTag())
		{
		case WorkflowSimTags.MONITORING:
			if (hostList.isEmpty())
			{
				hostList = datacenter.getHostList();
			}
			monitorVMStatus(ev);
			break;
		case WorkflowSimTags.HOST_SLEEP_TIMEING:
			if (hostList.isEmpty())
			{
				hostList = datacenter.getHostList();
			}
			sleepTimeUpdate();
			break;
		case WorkflowSimTags.HOST_SAVING_STATISTICS:
			if (hostList.isEmpty())
			{
				hostList = datacenter.getHostList();
			}
			updateEnergySaving();
			break;
		}
//		case WorkflowSimTags.UPDATE_STATE:
//			if (hostList.isEmpty())
//			{
//				hostList = datacenter.getHostList();
//			}
//			updateState(ev);

	}

	protected void monitorVMStatus(SimEvent ev)
	{

		vmList = (List) ev.getData();
		for (int i = 0; i < vmList.size(); i++)
		{
			CondorVM vm = vmList.get(i);
//			if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
//			{
//				vm.setState(WorkflowSimTags.VM_STATUS_NAP);
//			}
			if(vm.getCloudletScheduler().runningCloudlets() == 0)
			{
				vm.setState(WorkflowSimTags.VM_STATUS_NAP);
			}
			if(vm.getCloudletScheduler().runningCloudlets() > 0 && vm.getState() != WorkflowSimTags.VM_STATUS_BUSY)
			{
				vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
			}
		}
		setHostState();
	}

	protected void setHostState()
	{

		// ��ʼ��host״̬��Ĭ��ΪWORK״̬
		if (hostStateList.isEmpty())
		{
			for (int i = 0; i < datacenter.getHostList().size(); i++)
			{
				// datacenter.getHostList().get(i).setState(WorkflowSimTags.HOST_STATUS_WORK);
				hostStateList.add(datacenter.getHostList().get(i).getState());// Initialize
																				// the
																				// hostStateList
				hostSleepTimeList.add(0.0); // Initialize the hostSleepTimeList
			}
			energySavStaic.setHostSleepTimeList(hostSleepTimeList);
		}

		//vmList = datacenter.getVmList();
		// energySavStaic.setHostList(hostList);

		for (int i = 0; i < hostList.size(); i++)
		{
			Host host = hostList.get(i);

			// hostList��ÿһ̨����CPU�ϵĺ�����
			for (int j = 0; j < host.getPeList().size(); j++)
			{
				Pe pe = host.getPeList().get(j);
//				if (pe.getStatus() == Pe.FREE || pe.getStatus() == Pe.BUSY)
//				{
					PeProvisioner peProvisioner = pe.getPeProvisioner();
					PeProvisionerSimple peProvisionerSimple = (PeProvisionerSimple) peProvisioner;
					Map<String, List<Double>> peTable = peProvisionerSimple.getPeTable();

					int count_1 = 0;
					int count_2 = 0;
					if(peTable.isEmpty() && pe.getStatus() != Pe.NAP)
					{
						pe.setStatus(Pe.NAP);
						continue;
					}
					for (Map.Entry<String, List<Double>> peToVmMips : peTable.entrySet())
					{
						for (int k = 0; k < host.getVmList().size(); k++)
						{
							CondorVM vm_1 = (CondorVM) host.getVmList().get(k);
							if (vm_1.getUid() == peToVmMips.getKey())
							{
								count_2++;
								if (vm_1.getState() == WorkflowSimTags.VM_STATUS_BUSY || 
										                vm_1.getCloudletScheduler().runningCloudlets() > 0)
								{
									pe.setStatus(Pe.BUSY);
								}
								if (vm_1.getState() == WorkflowSimTags.VM_STATUS_NAP)
								{
									count_1++;
								}
								if (vm_1.getState() == WorkflowSimTags.VM_STATUS_IDLE)
								{
									count_1++;
									vm_1.setState(WorkflowSimTags.VM_STATUS_NAP);
								}
							}
						}
					}
					if (count_1 == count_2
							&& (pe.getStatus() == Pe.FREE || pe.getStatus() == Pe.BUSY))
					{
						pe.setStatus(Pe.NAP);
						pe.setLastMonitorTime(CloudSim.clock());

					}
					else if (count_1 != count_2 && pe.getStatus() == Pe.NAP)// ���Ĵ�Nap״̬ת��ΪBUSY��FREE
					{
						pe.setStatus(Pe.BUSY);
						pe.setSleepTime(pe.getSleepTime() + CloudSim.clock() - pe.getLastMonitorTime());
                        pe.setLastMonitorTime(CloudSim.clock());
					}

//				}
			}

			int count = 0;
			for (int j = 0; j < host.getPeList().size(); j++)
			{
				if (host.getPeList().get(j).getStatus() == Pe.NAP)
				{
					count++;
				}
			}
			// �������ӹ���״̬����˯��״̬�����������еĺ��Ķ�����˯�ߣ�
			if (host.getPeList().size() == count
					&& host.getState() != WorkflowSimTags.HOST_STATUS_SLEEP)
			{
				host.setState(WorkflowSimTags.HOST_STATUS_SLEEP);
				host.setLastMonitorTime(CloudSim.clock());
			}
			else if (host.getPeList().size() != count
					&& host.getState() == WorkflowSimTags.HOST_STATUS_SLEEP)
			{// ������˯��״̬�л�������״̬�Ĳ��裬��ʱ��Ҫ��ʱ�ĸ���������˯��ʱ�䡣
				host.setState(WorkflowSimTags.HOST_STATUS_WORK);
				host.setSleepTime(host.getSleepTime() + CloudSim.clock()
						- host.getLastMonitorTime());
				hostSleepTimeList.set(i, host.getSleepTime());
				host.setLastMonitorTime(CloudSim.clock());
			}
			else if (host.getState() == WorkflowSimTags.HOST_STATUS_WORK)
			{
				host.setLastMonitorTime(CloudSim.clock());
			}
		}

		//
		// //represent every VM in the host enter Nap-states, begin host_sleep.
		// if (count == host.getVmList().size() && host.getState() ==
		// WorkflowSimTags.HOST_STATUS_WORK)
		// {
		// host.setState(WorkflowSimTags.HOST_STATUS_SLEEP);
		// hostStateList.set((host.getId()), host.getState());
		// host.setLastMonitorTime(CloudSim.clock()); //set the lastMonitorTime
		// of host per clock.
		// }else if(count < host.getVmList().size() && host.getState() ==
		// WorkflowSimTags.HOST_STATUS_SLEEP)
		// {
		// host.setState(WorkflowSimTags.HOST_STATUS_WORK);
		// hostStateList.set((host.getId()), host.getState());
		// host.setLastMonitorTime(CloudSim.clock()); //set the lastMonitorTime
		// of host per clock.
		// }
		
//		sendNow(this.getId(), WorkflowSimTags.HOST_SLEEP_TIMEING);
		sleepTimeUpdate();
	}

	protected void sleepTimeUpdate()
	{

		for (int i = 0; i < hostList.size(); i++)
		{
			Host host = hostList.get(i);
			if (host.getState() == WorkflowSimTags.HOST_STATUS_SLEEP)
			{
				host.setSleepTime(host.getSleepTime() + CloudSim.clock()
						- host.getLastMonitorTime());
				hostSleepTimeList.set(i, host.getSleepTime());
				host.setLastMonitorTime(CloudSim.clock()); // set the
															// lastMonitorTime
															// of every host per
															// clock.
			}
			else if (hostStateList.get(i) == WorkflowSimTags.HOST_STATUS_WORK)
			{
				for (int j = 0; j < host.getPeList().size(); j++)
				{
					Pe pe = host.getPeList().get(j);
					if (pe.getStatus() == Pe.NAP)
					{
						pe.setSleepTime(pe.getSleepTime() + CloudSim.clock()
								- pe.getLastMonitorTime());
						pe.setLastMonitorTime(CloudSim.clock());
					}
				}

			}
			else if (hostStateList.get(i) == WorkflowSimTags.HOST_STATUS_DEEPERSLEEP)
			{
				// Waiting for coding.
			}

		}
		energySavStaic.setHostSleepTimeList(hostSleepTimeList);
//		sendNow(this.getId(), WorkflowSimTags.HOST_SAVING_STATISTICS);
		updateEnergySaving();
	}

	protected void updateEnergySaving()
	{
		// ��ʼ��ÿ��
		if (hostWStatistics.isEmpty())
		{
			for (int i = 0; i < hostList.size(); i++)
			{
				hostWStatistics.add((double) 0);
			}
		}

		for (int i = 0; i < hostList.size(); i++)
		{
			Host host = hostList.get(i);
			double peSleepTime = 0;
			for (int j = 0; j < host.getPeList().size(); j++)
			{
				Pe pe = host.getPeList().get(j);
				peSleepTime += pe.getSleepTime();
			}
			// ����˯��״̬ʱ����ʡ�Ĺ�
			double peSleepSaveKw_h = peSleepTime * host.getSleepPerPE_W();

			double hostSleepKw_h = host.getSleepTime() * host.getSleep_W();
			double hostWorkKw_h = host.getWork_W()
					* (CloudSim.clock() - host.getSleepTime());

			double hostKw_h = hostWorkKw_h - peSleepSaveKw_h + hostSleepKw_h;
			host.setHostKw_h(hostKw_h);

			hostWStatistics.set(i, hostKw_h);

		}

		// if(vmToHostList.isEmpty()) //Initialize the map between host Id and
		// vm.
		// {
		// for(int i = 0; i < hostList.size(); i++)
		// {
		// Host host = hostList.get(i);
		// int count = 0;
		// vmToHostList.put(i, host.getVmList());
		// for(int j = 0; j < host.getVmList().size(); j++)
		// {
		// CondorVM vm = (CondorVM) host.getVmList().get(j);
		// if(vm.getState() == WorkflowSimTags.VM_STATUS_NAP)
		// {
		// count++;
		// }
		// }
		// if(host.getState() == WorkflowSimTags.HOST_STATUS_SLEEP) //the Host
		// entry into SLEEP status.
		// {
		// energySavStaic.updateEnergySaving(host);
		//
		// }else if(count > 0 && count < host.getVmList().size() - 1)
		// //represent the count of the VMs entry NAP status less than
		// { //the amount of the VMs in the Host.
		// for(int j = 0; j < host.getVmList().size(); j++)
		// {
		// CondorVM vm = (CondorVM)host.getVmList().get(j);
		// if(vm.getState() == WorkflowSimTags.VM_STATUS_NAP)
		// {
		// energySavStaic.updateEnergySaving(vmList.get(j));
		// }
		// }
		//
		// }else if(count == 0) //represent the count of the VM_STATUS == NAP is
		// 0.
		// {
		//
		// }
		//
		// }
		// }

	}
	
//	protected void updateState(SimEvent ev)
//	{
//		CondorVM vm = (CondorVM) ev.getData();
//		if(vm.getCloudletScheduler().runningCloudlets() == 0)
//		{
//			vm.setState(WorkflowSimTags.VM_STATUS_IDLE);
//		}
//		
//	}

	@Override
	public void shutdownEntity()
	{
		Log.printLine(getName() + " is shutting down...");
		
	}

	@Override
	public void startEntity()
	{
		Log.printLine(getName() + " is starting...");

		// schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * get() and set()
	 */

	public List<Double> getNapTimeList()
	{
		return napTimeList;
	}

	public List<Double> getSleepTimeList()
	{
		return sleepTimeList;
	}

	public List<Double> getDeeperSleepTimeList()
	{
		return deeperSleepTimeList;
	}

	public List<Integer> getHostStateList()
	{
		return hostStateList;
	}

	public List<Double> getHostSleepTimeList()
	{
		return hostSleepTimeList;
	}

	public List<Host> getHostList()
	{
		return hostList;
	}

	public void setHostList(List<Host> hostList)
	{
		this.hostList = hostList;
	}

	public void setHostSleepTimeList(List<Double> hostSleepTimeList)
	{
		this.hostSleepTimeList = hostSleepTimeList;
	}

	public void setHostStateList(List<Integer> hostStateList)
	{
		this.hostStateList = hostStateList;
	}

	public void setSleepTimeList(List<Double> sleepTimeList)
	{
		this.sleepTimeList = sleepTimeList;
	}

	public void setDeeperSleepTimeList(List<Double> deeperSleepTimeList)
	{
		this.deeperSleepTimeList = deeperSleepTimeList;
	}

	public void setNapTimeList(List<Double> napTimeList)
	{
		this.napTimeList = napTimeList;
	}

	public Map<Integer, List> getVmToHostLis()
	{
		return vmToHostList;
	}

	public void setVmToHostLis(Map<Integer, List> vmToHostLis)
	{
		this.vmToHostList = vmToHostLis;
	}

	public Datacenter getDatacenter()
	{
		return datacenter;
	}

	public void setDatacenter(Datacenter datacenter)
	{
		this.datacenter = datacenter;
	}

	public List<Double> getHostWStatistics()
	{
		return hostWStatistics;
	}

	public void setHostWStatistics(List<Double> hostWStatistics)
	{
		this.hostWStatistics = hostWStatistics;
	}

	
	// public int getId()
	// {
	// return id;
	// }
	//
	// public void setId(int id)
	// {
	// this.id = id;
	// }

}