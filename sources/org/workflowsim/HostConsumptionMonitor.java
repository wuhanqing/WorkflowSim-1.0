package org.workflowsim;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jdk.nashorn.internal.objects.annotations.Where;

import org.cloudbus.cloudsim.Cpu;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

import com.sun.xml.internal.bind.util.Which;

public class HostConsumptionMonitor extends SimEntity
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
	protected List<Double> hostConsumptionList;
	protected List<Double> datacenterConsumptionList;
	protected double hostConsumption;
	protected double datacenterConsumption;
	
	public HostConsumptionMonitor(String name) throws Exception
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
		hostConsumptionList = new ArrayList<Double>();
		datacenterConsumptionList = new ArrayList<Double>();
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
		
		List<Host> hostList = datacenter.getHostList();
//		for(int i = 0; i < hostList.size(); i++)
//		{
//			double vmUtilization = 0;
//			Host host = hostList.get(i);
//			List<CondorVM> vmList = host.getVmList();			
//			for(int j = 0; j < vmList.size(); j++)
//			{
//				CondorVM vm = vmList.get(j);
//				vmUtilization += vm.getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
//			}
//		
//		}
		
		
		for(int i = 0; i < hostList.size(); i++)
		{
			List<Cpu> cpuList = hostList.get(i).getCpuList();
			double hostUtilization = 0;
			for(int j = 0; j < cpuList.size(); j++)
			{
				List<Pe> peList = cpuList.get(j).getPeList();
				double cpuUtilization = 0;
				for(int k = 0; k < peList.size(); k++)
				{
					Pe pe = peList.get(k);
					double utilization = pe.getPeProvisioner().getUtilization();
					if(utilization == 0)
					{
						pe.setStatus(Pe.NAP);
						pe.setSleepTime(CloudSim.clock()-pe.getLastMonitorTime());
						pe.setLastMonitorTime(CloudSim.clock());
					}
					if(utilization > 0 && pe.getStatus() == Pe.NAP)
					{
						pe.setStatus(Pe.BUSY);
						pe.setSleepTime(CloudSim.clock()-pe.getLastMonitorTime());
						pe.setLastMonitorTime(CloudSim.clock());
					}
					if(utilization > 0 && pe.getStatus() == Pe.BUSY)
					{
						pe.setLastMonitorTime(CloudSim.clock());
					}
					cpuUtilization += utilization;
				}
				cpuUtilization = cpuUtilization/peList.size();
				Cpu cpu = cpuList.get(j);
				cpu.setUtilization(cpuUtilization);
				cpuUtilization = 0;
			}
			Host host = hostList.get(i);
			double time = host.getLastMonitorTime();
			double lastW = host.getW();
			
			if(time != CloudSim.clock())
			{
				Double[] w2t = {lastW, CloudSim.clock() - time};
				host.getW2Time().add(w2t);
				host.setLastMonitorTime(CloudSim.clock());
				host.setW(host.getPower(host.getCpuUitlization(host.getVmList()))); //set the new power
			}
			
			HostWStatistics(host);
			
		}
		TotalHostW();
		
		
	}
	
	public void HostWStatistics(Host host)
	{
//		MultiMap<Double, Double> w2Time = host.getW2Time();
//		Set<Double> wSet = w2Time.keySet();
		double power = 0;

		Iterator<Double[]> key = host.getW2Time().iterator();
		while(key.hasNext()){
			Double[] w2t = key.next();
			Double w = w2t[0];
			Double time = w2t[1];
			power += w * time;
		}
		host.setConsumption(power);
		host.getConsumptionList().add(power);

	}
	
	//datacenter consumption
	public void TotalHostW()
	{
		Iterator<Host> list = hostList.iterator();
		double consumption = 0;
		while(list.hasNext())
		{
			Host host = list.next();
			consumption += host.getConsumption();
		}
		datacenter.setConsumption(consumption);
		datacenter.getConsumptionList().add(consumption);
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

	public void setDatacenter(Datacenter datacenter) {
		// TODO Auto-generated method stub
		this.datacenter = datacenter;
	}

	public List<Host> getHostList() {
		return hostList;
	}

	public void setHostList(List<Host> hostList) {
		this.hostList = hostList;
	}
	
	
}
