package org.workflowsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.workflowsim.reclustering.ReclusteringEngine;
import org.workflowsim.utils.Parameters;


public class EnergySavingMonitor extends SimEntity {
	
	protected List<? extends Vm> vmList;
	
	protected List<Long> napTimeList;
	
	protected List<Long> sleepTimeList;
	
	protected List<Long> deeperSleepTimeList;
	
	protected int hostState;
	
	protected List<Integer> hostStateList;
	
	
	public EnergySavingMonitor(String name) {
		super(name);
		setNapTimeList(new ArrayList<Long>());
		setSleepTimeList(new ArrayList<Long>());
		setDeeperSleepTimeList(new ArrayList<Long>());
		
	}

	public void shutdownEntity() {
        Log.printLine(getName() + " is shutting down...");
    }

	@Override
	public void startEntity() {
        Log.printLine(getName() + " is starting...");
      //  schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
    }

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) 
		{
			case WorkflowSimTags.MONITOR_BEGIN:
				monitorVMStatus(ev);
				break;
		}
		
	}
	
	public void setNapTimeList(List<Long> napTimeList)
	{
		this.napTimeList = napTimeList;
	}
	
	public void setSleepTimeList(List<Long> sleepTimeList)
	{
		this.sleepTimeList = sleepTimeList;
	}
	
	public void setDeeperSleepTimeList(List<Long> deeperSleepTimeList)
	{
		this.deeperSleepTimeList = deeperSleepTimeList;
	}
	
	protected void monitorVMStatus(SimEvent ev)
	{
		CondorVM vm = (CondorVM) ev.getData();
		if(vm.getState() == WorkflowSimTags.VM_STATUS_NAP)
		{
			
		}
		chkHostState(vm);
		
	}
	
	protected void chkHostState(CondorVM vm_chk)
	{
		Host host = vm_chk.getHost();
		List<Vm> vmList = host.getVmList();
		int count = 0;
		for(int i = 0; i < host.getVmList().size(); i++)
		{
			CondorVM vm = (CondorVM) vmList.get(i);
			if(vm.getState() == WorkflowSimTags.VM_STATUS_NAP)
			{
				count++;
			}
		}
		if(count == host.getVmList().size())
		{
			hostStateList.set((host.getId()), WorkflowSimTags.HOST_STATUS_SLEEP);
			
		}
	}

}
