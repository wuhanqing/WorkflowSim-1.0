/**
 * Copyright 2013-2014 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.lists.VmList;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.WorkflowSimTags;
import org.workflowsim.planning.HEFTPlanningAlgorithm;
import org.workflowsim.utils.ReplicaCatalog;

import com.sun.org.apache.bcel.internal.generic.LLOAD;


/**
 * The Round Robin algorithm.
 * 
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date May 12, 2014
 */
public class HMPCnew extends BaseSchedulingAlgorithm
{
	private Map<Job,Double> cloudlet2Rank;
    public CondorVM bestVm = null;
    private List hasChecked = new ArrayList<Boolean>();
    
    private boolean flag = false;
    private List<CondorVM> idlevmList = new ArrayList<CondorVM>();
	private List<Double> powerList = new ArrayList<Double>();
	private class CloudRank implements Comparable<CloudRank> {

        public Job job;
        public Double rank;
        
        public CloudRank(Job job, Double rank) {
            this.job = job;
            this.rank = rank;
        }

        @Override
        public int compareTo(CloudRank o) {
            return o.rank.compareTo(rank);
        }
    }

	/**
	 * The main function
	 */
	@Override
	public void run() throws Exception
	{
		for (int i = 0; i < getVmList().size(); i++) {
			CondorVM vm = (CondorVM) getVmList().get(i);
			if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
				break;
			}
			if (i == getVmList().size() - 1) {
				return;
			}
		}

		System.out.println(CloudSim.clock());
		Map<Task, Double> rank = HEFTPlanningAlgorithm.taskRank;
		cloudlet2Rank = new HashMap<Job, Double>();
		
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		cloudletList = getCloudletList();
		for (int i = 0; i < cloudletList.size(); i++) {
			Cloudlet cloudlet = cloudletList.get(i);
			if (cloudlet.getCheck() == -1) {
				cloudlet.setCheck(1);
				cloudlet.setVmId(-1);
			}
		}
		
		for(int i = 0; i < cloudletList.size(); i++)
		{
			double taskRank = 0;
			Job job = (Job) cloudletList.get(i);
			Cloudlet cloudlet = cloudletList.get(i);
			job.setCloudlet(cloudlet);
			List<Task> taskList = new ArrayList<Task>();
			taskList = job.getTaskList();
			for (int j = 0; j < taskList.size(); j++)
			{
				Task task = taskList.get(j);

				if (rank.containsKey(task))
				{
					taskRank += rank.get(task);
				}
			}
			cloudlet2Rank.put(job, taskRank);
		}
		List<CloudRank> cloud2Rank = new ArrayList<CloudRank>(); 
		for (Job job : cloudlet2Rank.keySet()) {
			cloud2Rank.add(new CloudRank(job, cloudlet2Rank.get(job)));
        }
		Collections.sort(cloud2Rank);
		
		for(int i = 0; i < cloud2Rank.size(); i++)
		{

			for (int j = 0; i < getVmList().size(); j++) {
				CondorVM vm = (CondorVM) getVmList().get(j);
				if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
					break;
				}
				if (j == getVmList().size() - 1) {
					return;
				}
			}

			powerList.clear();
			idlevmList.clear();
			CondorVM firstIdleVm = null;
			double minEnergy = Double.MAX_VALUE;
			double minTime = 0;
			CloudRank cloudRank1 = cloud2Rank.get(i); 
			Job job = cloudRank1.job;
			
			long averageBW = 0;
			for (int j = 0; j < getVmList().size(); j++) {
				CondorVM vm = (CondorVM) getVmList().get(j);
				long l = vm.getBw();
				averageBW += l;
			}
			averageBW = averageBW / getVmList().size();
			
			double timeNeed = 0;
			
			for(int j = 0; j < getVmList().size(); j++)
			{
				CondorVM vm = (CondorVM) getVmList().get(j);
				
//				if (vm.getRemainTime() > 0 && vm.getRemainTime() < CloudSim.clock()) {
//					vm.setRemainTime(0);
//					vm.setMarkTime(CloudSim.clock());
//				}
				List<Host> hostList = vm.getHost().getDatacenter().getHostList();
				
				Host host = vm.getHost();
				
				double currentMipsOfHost = host.getCpuUitlization(host.getVmList()) * host.getTotalMips();
				
				timeNeed = job.getCloudletLength() / vm.getMips();
				WorkflowDatacenter datacenter = (WorkflowDatacenter) host.getDatacenter();
				
				double transTime = datacenter.processDataStageIn(job.getFileList(), job, vm);//
				if (job.getCloudletId() == WorkflowPlanner.totalTaskNum) {
					transTime = 0;//
				}
				timeNeed += transTime;//
//				if (job.getCloudletId() != WorkflowPlanner.totalTaskNum) {
//					System.out.print("任务"+job.getCloudletId()+"在虚拟机"+vm.getId()+" 上执行时间（算上传输时间）："+timeNeed+", ");
//				}
				
				double W = 0;
				double nextFinish = vm.getCloudletScheduler().updateVmProcessing(CloudSim.clock(), host.getAllocatedMipsForVm(vm));
				double t = 0;
				if (nextFinish - CloudSim.clock() > 0) {
					t = nextFinish - CloudSim.clock(); //
				}
				if (vm.getRemainTime() > CloudSim.clock() && vm.getState() == WorkflowSimTags.VM_STATUS_BUSY) {
					
//					double totalTime = 0;
//					if (vm.getRemainTime() >= nextFinish) {
//						totalTime = timeNeed + vm.getRemainTime() - nextFinish;
//					}else {
//						totalTime = timeNeed + nextFinish;
//					}
					double d = 0;
					if (vm.getRemainTime() > CloudSim.clock()) {
						d = vm.getRemainTime() - CloudSim.clock();
					}
					
					
					/**
					 * 需要大搞一下这句里的job.getCloudletFileSize()
					 */
//					double totalTime = timeNeed + nextFinish + d + ((job.getCloudletFileSize() * 8) / averageBW);
//					double totalTime = timeNeed + nextFinish + vm.getRemainTime();
					
					
					double totalTime = timeNeed + d + vm.getnTime();
					double tt = 0;
					for (int k = 0; k < hostList.size(); k++) {
						for (int k2 = 0; k2 < hostList.get(k).getVmList().size(); k2++) {
							CondorVM v = (CondorVM) hostList.get(k).getVmList().get(k2);
							if (v.getRemainTime() > timeNeed) {
								tt = v.getRemainTime();
							}
						}
						
					}
					double W1 = host.getPower(host.getCpuUitlization(host.getVmList())) * totalTime;
//					double W1 = host.getPower(host.getCpuUitlization(host.getVmList())) * timeNeed + host.getAlgorithmW();
					host.setAlgorithmW(W1);
					for(int l = 0; l < hostList.size(); l++)
					{
						Host host2 = vm.getHost();
						Host host3 = hostList.get(l);
						if(host2.getId() != host3.getId())
						{
							W += host3.getPower(host3.getCpuUitlization(host3.getVmList())) * totalTime;
//							W += host3.getPower(host3.getCpuUitlization(host3.getVmList())) * timeNeed + host.getAlgorithmW();
							host3.setAlgorithmW(W);
						}
					}
					if(W + W1 <= minEnergy)
					{
						minEnergy = W + W1;
						idlevmList.add(vm);
						firstIdleVm = vm;
						minTime = timeNeed;
					}
					if (job.getCloudletId() != WorkflowPlanner.totalTaskNum) {
//						System.out.println("任务"+job.getCloudletId()+"在虚拟机"+vm.getId()+" 上执行时间（算上传输时间）："+timeNeed+", " + "还需"+totalTime+"时间能够完成" + "; 完成时刻："+(timeNeed+CloudSim.clock())+"; 此次能耗统计："+(W+W1));
					}
				}
				
				List<CondorVM> vmlist = vm.getHost().getDatacenter().getVmList();
				
				if(vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
				{
					for(int l = 0; l < hostList.size(); l++)
					{
						Host host2 = vm.getHost();
						Host host3 = hostList.get(l);
						if(host2.getId() != host3.getId())
						{
							W += host3.getPower( host3.getCpuUitlization(host3.getVmList()) ) * timeNeed;
						}
					}
					
					double assumeMips = currentMipsOfHost + vm.getMips();
					double assumeCpuUtilization = assumeMips / host.getTotalMips();
//					double utilization = availableMips/host.getTotalMips();
					double W2 = host.getPower(assumeCpuUtilization) * timeNeed;

//					double performance2EnergyRatio3 = host.getPowerModel().getPerformancePerPower(assumeCpuUtilization);
					
					W = W + W2;
					
					int mips = 0;
					for(int h = 0; h < vmlist.size(); h++)
					{
						CondorVM vm1 = vmlist.get(h);
						mips += vm1.getMips();
					}
					mips =  mips / vmlist.size();
					
//					if(W <= minEnergy && ((double)job.getCloudletLength()/vm.getMips()) <= ((double)job.getCloudletLength()/mips))
					if(W < minEnergy)
					{
//						if((((double)job.getCloudletLength()/vm.getMips()) <= ((double)job.getCloudletLength()/mips)) ||  (((double)job.getCloudletLength()/vm.getMips() > 0) && ((double)job.getCloudletLength()/vm.getMips() < 150)))
						{
							minEnergy = W;
							idlevmList.add(vm);
							firstIdleVm = vm;
							powerList.add(W);
							minTime = timeNeed;
						}
						
					}
					
					if (job.getCloudletId() != WorkflowPlanner.totalTaskNum) {
//						System.out.println("任务"+job.getCloudletId()+"在虚拟机"+vm.getId()+" 上执行时间（算上传输时间）："+timeNeed+", " + "还需"+timeNeed+"时间能够完成"+"; 完成时刻："+(timeNeed+CloudSim.clock())+"; 此次能耗统计："+W);
					}
					
				}

			}
			
			if(firstIdleVm == null)
			{
				break;
			}
			
			Cloudlet cloudlet = job.getCloudlet();
			
			if (cloudlet.getVmId() != -1) {
				CondorVM vm = (CondorVM) getVmList().get(cloudlet.getVmId());
				if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
	                vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
	                getScheduledList().add(cloudlet);
//	                vm.setRemainTime(timeNeed + CloudSim.clock());
//	                vm.setMarkTime(CloudSim.clock());
	                
//	                for (int j = 0; j < job.getFileList().size(); j++) {
//						File file = (File) job.getFileList().get(j);
//						List<File> fileList = job.getFileList();
//						boolean b = WorkflowDatacenter.isRealInputF(fileList, file);
//						if (b && !fileList.contains(file.getName()) && flag) {
//							firstIdleVm.getFileList().add(file.getName());
//						}
//						firstIdleVm.setnTime(job.getCloudletLength() / firstIdleVm.getMips());
//					}
//	                firstIdleVm.setnTime(firstIdleVm.getnTime() + minTime);
					Log.printLine("Schedules " + cloudlet.getCloudletId() + " with "
	                        + cloudlet.getCloudletLength() + " to VM " + cloudlet.getVmId());
					continue;
	            }
				
				if(firstIdleVm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
				{
					firstIdleVm.setState(WorkflowSimTags.VM_STATUS_BUSY);
					cloudlet.setVmId(firstIdleVm.getId());
					getScheduledList().add(cloudlet);
					if (firstIdleVm.getRemainTime() == 0) {
						firstIdleVm.setRemainTime(minTime + CloudSim.clock());
						firstIdleVm.setMarkTime(CloudSim.clock());
					} else if (firstIdleVm.getRemainTime() > 0) {
						firstIdleVm.setRemainTime(firstIdleVm.getRemainTime() + minTime);
						firstIdleVm.setMarkTime(CloudSim.clock());
					}
					
					
//					for (int j = 0; j < job.getFileList().size(); j++) {
//						File file = (File) job.getFileList().get(j);
//						List<File> fileList = job.getFileList();
//						boolean b = WorkflowDatacenter.isRealInputF(fileList, file);
//						if (b && !fileList.contains(file.getName()) && flag) {
//							firstIdleVm.getFileList().add(file.getName());
//						}
//						firstIdleVm.setnTime(job.getCloudletLength() / firstIdleVm.getMips());
//					}
//					firstIdleVm.setnTime(firstIdleVm.getnTime() + minTime);
					Log.printLine("Schedules " + cloudlet.getCloudletId() + " with "
	                        + cloudlet.getCloudletLength() + " to VM " + cloudlet.getVmId());
					continue;
				}
//				continue;

			}
			if (cloudlet.getVmId() == -1) {
				if(firstIdleVm.getState() == WorkflowSimTags.VM_STATUS_BUSY)
				{
//					cloudlet.setVmId(firstIdleVm.getId());
//					if (firstIdleVm.getRemainTime() == 0) {
//						firstIdleVm.setRemainTime(minTime + CloudSim.clock());
//						firstIdleVm.setMarkTime(CloudSim.clock());
//					} else if (firstIdleVm.getRemainTime() > 0) {
//						firstIdleVm.setRemainTime(firstIdleVm.getRemainTime() + minTime);
//						firstIdleVm.setMarkTime(CloudSim.clock());
//					}
					
//					for (int j = 0; j < job.getFileList().size(); j++) {
//						File file = (File) job.getFileList().get(j);
//						List<File> fileList = job.getFileList();
//						boolean b = WorkflowDatacenter.isRealInputF(fileList, file);
//						if (b && !fileList.contains(file.getName()) && flag) {
//							firstIdleVm.getFileList().add(file.getName());
//						}
//						firstIdleVm.setnTime(job.getCloudletLength() / firstIdleVm.getMips());
//					}
					firstIdleVm.setnTime(firstIdleVm.getnTime() + minTime);
//					Log.printLine("Schedules " + cloudlet.getCloudletId() + " with "
//	                        + cloudlet.getCloudletLength() + " to VM " + firstIdleVm.getId() + "not real");
					continue;
				}
				
				if(firstIdleVm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
				{
					firstIdleVm.setState(WorkflowSimTags.VM_STATUS_BUSY);
					cloudlet.setVmId(firstIdleVm.getId());
					getScheduledList().add(cloudlet);
					if (firstIdleVm.getRemainTime() == 0) {
						firstIdleVm.setRemainTime(minTime + CloudSim.clock());
						firstIdleVm.setMarkTime(CloudSim.clock());
					} else if (firstIdleVm.getRemainTime() > 0) {
						firstIdleVm.setRemainTime(firstIdleVm.getRemainTime() + minTime);
						firstIdleVm.setMarkTime(CloudSim.clock());
					}
					if (job.getCloudletId() != WorkflowPlanner.totalTaskNum) {
						for (int j = 0; j < job.getFileList().size(); j++) {
							File file = (File) job.getFileList().get(j);
							List<String> fileList = firstIdleVm.getFileList();
							boolean b = WorkflowDatacenter.isRealInputF(job.getFileList(), file);
							if (b && !fileList.contains(file.getName())) {
								firstIdleVm.getFileList().add(file.getName());
							}
						}
					}
//					firstIdleVm.setnTime(firstIdleVm.getnTime() + minTime);
					Log.printLine("Schedules " + cloudlet.getCloudletId() + " with "
	                        + cloudlet.getCloudletLength() + " to VM " + cloudlet.getVmId());
					continue;
				}
				
			}
			
			bestVm = null;
			
		}
		
		flag = true;
	}

}

