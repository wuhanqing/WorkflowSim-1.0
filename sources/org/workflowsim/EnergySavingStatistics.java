package org.workflowsim;

import java.util.List;
import org.cloudbus.cloudsim.Host;

public class EnergySavingStatistics
{
	
	protected List<Double> hostSleepTimeList;
	protected List<Integer> hostStateList;
	protected List<Host> hostList;
	protected List<Double> lastVmUpdateTimeList;
	protected List<Double> lastHostUpdateTimeList;
	
	public void updateEnergySaving(CondorVM vm)
	{
		
	}
	
	public void updateEnergySaving(Host host)
	{
		
		
	}

	public List<Double> getHostSleepTimeList()
	{
		return hostSleepTimeList;
	}

	public void setHostSleepTimeList(List<Double> hostSleepTimeList)
	{
		this.hostSleepTimeList = hostSleepTimeList;
	}

	public List<Host> getHostList()
	{
		return hostList;
	}

	public void setHostList(List<Host> hostList)
	{
		this.hostList = hostList;
	}

	public List<Integer> getHostStateList()
	{
		return hostStateList;
	}

	public void setHostStateList(List<Integer> hostStateList)
	{
		this.hostStateList = hostStateList;
	}

	public List<Double> getLastVmUpdateTimeList()
	{
		return lastVmUpdateTimeList;
	}

	public void setLastVmUpdateTimeList(List<Double> lastVmUpdateTimeList)
	{
		this.lastVmUpdateTimeList = lastVmUpdateTimeList;
	}

	public List<Double> getLastHostUpdateTimeList()
	{
		return lastHostUpdateTimeList;
	}

	public void setLastHostUpdateTimeList(List<Double> lastHostUpdateTimeList)
	{
		this.lastHostUpdateTimeList = lastHostUpdateTimeList;
	}
	
	
	
}