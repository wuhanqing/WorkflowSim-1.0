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

public class HostComsumptionMonitorNew extends SimEntity
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

	public HostComsumptionMonitorNew(String name) throws Exception
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
	
	public void processEvent(SimEvent ev)
	{
		switch (ev.getTag())
		{
		case WorkflowSimTags.MONITORING:
			if (hostList.isEmpty())
			{
				hostList = datacenter.getHostList();
			}
			monitorPeStatus(ev);
			break;
//		case WorkflowSimTags.HOST_SLEEP_TIMEING:
//			if (hostList.isEmpty())
//			{
//				hostList = datacenter.getHostList();
//			}
//			sleepTimeUpdate();
//			break;
//		case WorkflowSimTags.HOST_SAVING_STATISTICS:
//			if (hostList.isEmpty())
//			{
//				hostList = datacenter.getHostList();
//			}
//			updateEnergySaving();
//			break;
		}
//		case WorkflowSimTags.UPDATE_STATE:
//			if (hostList.isEmpty())
//			{
//				hostList = datacenter.getHostList();
//			}
//			updateState(ev);

	}
	
	

	private void monitorPeStatus(SimEvent ev) {
		
		
		
	}

	public List<Double> getNapTimeList() {
		return napTimeList;
	}

	public void setNapTimeList(List<Double> napTimeList) {
		this.napTimeList = napTimeList;
	}

	public List<Double> getSleepTimeList() {
		return sleepTimeList;
	}

	public void setSleepTimeList(List<Double> sleepTimeList) {
		this.sleepTimeList = sleepTimeList;
	}

	public List<Double> getDeeperSleepTimeList() {
		return deeperSleepTimeList;
	}

	public void setDeeperSleepTimeList(List<Double> deeperSleepTimeList) {
		this.deeperSleepTimeList = deeperSleepTimeList;
	}

	public List<Integer> getHostStateList() {
		return hostStateList;
	}

	public void setHostStateList(List<Integer> hostStateList) {
		this.hostStateList = hostStateList;
	}

	public List<Double> getHostSleepTimeList() {
		return hostSleepTimeList;
	}

	public void setHostSleepTimeList(List<Double> hostSleepTimeList) {
		this.hostSleepTimeList = hostSleepTimeList;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}
	
	
}
