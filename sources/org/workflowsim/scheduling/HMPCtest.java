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
import org.workflowsim.WorkflowSimTags;
import org.workflowsim.planning.HEFTPlanningAlgorithm;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


/**
 * The Round Robin algorithm.
 * 
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date May 12, 2014
 */
public class HMPCtest extends BaseSchedulingAlgorithm
{
	private Map<Job,Double> cloudlet2Rank;
    public CondorVM bestVm = null;
    private List hasChecked = new ArrayList<Boolean>();
    
    private List<CondorVM> idlevmList = new ArrayList<CondorVM>();
	private List<Double> ppr = new ArrayList<Double>();
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
	public void run()
	{
		
		Map<Task, Double> rank = HEFTPlanningAlgorithm.taskRank;
		cloudlet2Rank = new HashMap<Job, Double>();
		
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		cloudletList = getCloudletList();
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
//			for(int i = 0; i < cloud2Rank.size(); i++)
//			{
			ppr.clear();
			idlevmList.clear();
			CondorVM firstIdleVm = null;
			double minEnergy = Double.MAX_VALUE;
			CloudRank cloudRank1 = cloud2Rank.get(i); 
			Job job = cloudRank1.job;
			for(int j = 0; j < getVmList().size(); j++)
			{
				CondorVM vm = (CondorVM) getVmList().get(j);
				Host host = vm.getHost();
//				double W1 = host.getW();
				
				List<Pe> peList = host.getPeList();
				double currentMipsOfHost = host.getCpuUitlization(host.getVmList()) * host.getTotalMips();
				double currentUtil = currentMipsOfHost / host.getTotalMips();
				double W1 = host.getPower(currentUtil);
				
				double performance2EnergyRatio1 = currentMipsOfHost / W1;
				
				double time1 = job.getCloudletLength()/vm.getMips();
				double W = 0;
				
				List<Host> hostList = vm.getHost().getDatacenter().getHostList();
				
				for(int l = 0; l < hostList.size(); l++)
				{
					Host host2 = vm.getHost();
					Host host3 = hostList.get(l);
					if(host2.getId() != host3.getId())
					{
						W += host3.getPower(0) * time1;
					}
				}
				
				List<CondorVM> vmlist = vm.getHost().getDatacenter().getVmList();
				 double[] PPP = new double[vmlist.size()];
				 double max = -1;
				 int position = -1;
				for(int l = 0; l < vmlist.size(); l++) {
					CondorVM vm1 = vmlist.get(l);
					Double remainTime = vm1.getCloudletScheduler().updateVmProcessing(CloudSim.clock(), vm1.getHost().getVmScheduler().getAllocatedMipsForVm(vm1));
					Double time2 = job.getCloudletLength()/vm1.getMips();
					try {
						double averagePPP = vm1.getHost().getPowerModel().getAveragePPP();
						PPP[l] = (remainTime + time2) * averagePPP;
						if (max < PPP[l]) {
							max = PPP[l];
							position = l;
						}
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (position > -1) {
//					if (vmlist.get(position).getState() == WorkflowSimTags.VM_STATUS_IDLE) {
						bestVm = vmlist.get(position);
//					}
				}
				
				
//				double availableMips = host.getVmScheduler().getAvailableMips();
				if(vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) //&& vm.getCheck() == 0)
				{
					
					double assumeMips = currentMipsOfHost + vm.getMips();
					double assumeCpuUtilization = assumeMips / host.getTotalMips();
//					double utilization = availableMips/host.getTotalMips();
					double W2 = host.getPower(assumeCpuUtilization) * time1;
//					double increment = W2 - W1;
//					double time = job.getCloudletLength() / vm.getMips();
//					double energy = increment * time;
					
					double performance2EnergyRatio2 = assumeMips / W2;
					double performance2EnergyRatio3 = host.getPowerModel().getPerformancePerPower(assumeCpuUtilization);
					double performance2EnergyRatio = performance2EnergyRatio2 - performance2EnergyRatio1;
					
					W = W + W2;
					
					
					int mips = 0;
					for(int h = 0; h < vmlist.size(); h++)
					{
						CondorVM vm1 = vmlist.get(h);
						mips += vm1.getMips();
					}
					mips =  mips / vmlist.size();
					
//					if(W <= minEnergy && ((double)job.getCloudletLength()/vm.getMips()) <= ((double)job.getCloudletLength()/mips))
					if(W <= minEnergy)
					{
//						if((((double)job.getCloudletLength()/vm.getMips()) <= ((double)job.getCloudletLength()/mips)) ||  (((double)job.getCloudletLength()/vm.getMips() > 0) && ((double)job.getCloudletLength()/vm.getMips() < 150)))
						{
							minEnergy = W;
							idlevmList.add(vm);
							firstIdleVm = vm;
							ppr.add(performance2EnergyRatio3);
						}
						
					}
					
				}
//				else if(vm.getState() == WorkflowSimTags.VM_STATUS_BUSY && vm.getCloudletScheduler().runningCloudlets() > 0)
//				{
//					CloudletScheduler cloudletScheduler = vm.getCloudletScheduler();
//					double remainTime = cloudletScheduler.updateVmProcessing(CloudSim.clock(), cloudletScheduler.getCurrentMipsShare());
//					double time = job.getCloudletLength()/vm.getMips();
//					double totalTime = remainTime + time;
//					double assumeMips = vm.getMips();
//					double assumeCpuUtilization = (currentMipsOfHost - assumeMips) / host.getTotalMips();
//					double W2 = host.getPower(assumeCpuUtilization);
//					double increment = W1 - W2;
//					double energy = increment * totalTime;
//					
//					double Performance2energyRatio2 = assumeMips / W2;
//					
//					if(energy < minEnergy && remainTime > 100)
//					{
//						minEnergy = energy;
//						firstIdleVm = vm;
//					}
//				}else if(vm.getState() == WorkflowSimTags.VM_STATUS_BUSY && vm.getCloudletScheduler().runningCloudlets() == 0)
//				{
//					break;
//				}
			}
			
			if(!idlevmList.isEmpty())
			{
				double ppr1 = 0;
				CondorVM vm1 = null;
				
				for(int k = 0; k < idlevmList.size(); k++)
				{
					if(ppr1 < ppr.get(k))
					{
						ppr1 = ppr.get(k);
						vm1 = idlevmList.get(k);
					}
				}
				firstIdleVm = vm1;
//				firstIdleVm = bestVm;
			}
			
			if(firstIdleVm == null)
			{
				break;
			}
			
			if(firstIdleVm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
			{
				firstIdleVm.setState(WorkflowSimTags.VM_STATUS_BUSY);
				Cloudlet cloudlet = job.getCloudlet();
//				firstIdleVm.getScheduledCloudlets().add(cloudlet);
				cloudlet.setVmId(firstIdleVm.getId());
				getScheduledList().add(cloudlet);
//				cloud2Rank.remove(0);
			}
			bestVm = null;
//			int count1 = 0;
//			for(int k = 0; k < getVmList().size(); k++)
//			{
//				CondorVM vm = (CondorVM) getVmList().get(k);
//				if(vm.getState() == WorkflowSimTags.VM_STATUS_BUSY)
//				{
//					count1++;
//				}
//			}
//			
//			if(count1 == getVmList().size())
//			{
//				return;
//			}
			
		}
//		}

//		for(int i = 0; i < getVmList().size(); i++)
//		{
//			CondorVM vm =(CondorVM) getVmList().get(i);
//			vm.setCheck(0);
//
//		}
		
		

		
//		int vmIndex = 0;
//
//		int size = getCloudletList().size();
//		Collections.sort(getCloudletList(), new CloudletListComparator());
//		List vmList = getVmList();
//		Collections.sort(vmList, new VmListComparator());
//		for (int j = 0; j < size; j++)
//		{
//			Cloudlet cloudlet = (Cloudlet) getCloudletList().get(j);
//			int vmSize = vmList.size();
//			CondorVM firstIdleVm = null;// (CondorVM)getVmList().get(0);
//			for (int l = 0; l < vmSize; l++)
//			{
//				CondorVM vm = (CondorVM) vmList.get(l);
//				if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
//				{
//					firstIdleVm = vm;
//					break;
//				}
//			}
//			if (firstIdleVm == null)
//			{
//				break;
//			}
//			firstIdleVm.setState(WorkflowSimTags.VM_STATUS_BUSY);
//			cloudlet.setVmId(firstIdleVm.getId());
//			getScheduledList().add(cloudlet);
//			vmIndex = (vmIndex + 1) % vmList.size();

//		}

	}
	
	

	/**
	 * Sort it based on vm index
	 */
//	public class VmListComparator implements Comparator<CondorVM>
//	{
//		@Override
//		public int compare(CondorVM v1, CondorVM v2)
//		{
//			return Integer.compare(v1.getId(), v2.getId());
//		}
//	}
//
//	public class CloudletListComparator implements Comparator<Cloudlet>
//	{
//		@Override
//		public int compare(Cloudlet c1, Cloudlet c2)
//		{
//			return Integer.compare(c1.getCloudletId(), c2.getCloudletId());
//		}
//	}

}
