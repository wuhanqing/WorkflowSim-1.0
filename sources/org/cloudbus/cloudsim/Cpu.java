package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Pe;



public class Cpu {
	
	public static final int FREE = 1;

	public static final int BUSY = 2;
	
	public static final int SLEEP = 3;
	
	public static final int LOCAL_NAP = 4;
	
	private double sleepTime = 0.0;
	
	public double cpuNapTimeWithoutHostSleep = 0.0;
	
	private double lastMonitorTime = 0.0;
	
	private int id;
	
	private int state;
	
	private List<Pe> peList;
	
	private double utilization;
	
	public Cpu(int id)
	{
		setId(id);
	}

	public double getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(double sleepTime) {
		this.sleepTime = sleepTime;
	}

	public double getLastMonitorTime() {
		return lastMonitorTime;
	}

	public void setLastMonitorTime(double lastMonitorTime) {
		this.lastMonitorTime = lastMonitorTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public List<Pe> getPeList() {
		return peList;
	}

	public void setPeList(List<Pe> peList) {
		this.peList = peList;
	}

	public double getUtilization() {
		return utilization;
	}

	public void setUtilization(double utilization) {
		this.utilization = utilization;
	}
	
//	public void setUtilization() {
//		List<Pe> peList = this.getPeList();
//		double utilization = 0;
//		for(int i = 0; i < peList.size(); i++)
//		{
//			Pe pe = peList.get(i);
//			utilization += pe.getPeProvisioner().getUtilization();
//		}
//		utilization = utilization / peList.size();
//		this.utilization = utilization;
//	}
	
}
