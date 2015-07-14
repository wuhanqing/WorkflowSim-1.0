/**
 * Copyright 2012-2013 University Of Southern California
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowSimTags;
import org.workflowsim.planning.HEFTPlanningAlgorithm;

/**
 * Static algorithm. Do not schedule at all and reply on Workflow Planner to set
 * the mapping relationship. But StaticSchedulingAlgorithm would check whether a
 * job has been assigned a VM in this stage (in case your implementation of
 * planning algorithm forgets it)
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Jun 17, 2013
 */
public class StaticSchedulingAlgorithm extends BaseSchedulingAlgorithm {

    public StaticSchedulingAlgorithm() {
        super();
    }

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
    
    @Override
    public void run() throws Exception {

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
		
    	
        Map mId2Vm = new HashMap<Integer, CondorVM>();

        for (int i = 0; i < getVmList().size(); i++) {
            CondorVM vm = (CondorVM) getVmList().get(i);
            if (vm != null) {
                mId2Vm.put(vm.getId(), vm);
            }
        }

        int size = getCloudletList().size();

        for (int i = 0; i < size; i++) {
//        	Cloudlet cloudlet = (Cloudlet) cloud2Rank.get(i).job;
            Cloudlet cloudlet = (Cloudlet) getCloudletList().get(i);
            /**
             * Make sure cloudlet is matched to a VM. It should be done in the
             * Workflow Planner. If not, throws an exception because
             * StaticSchedulingAlgorithm itself does not do the mapping.
             */
            if (cloudlet.getVmId() < 0 || !mId2Vm.containsKey(cloudlet.getVmId())) {
                Log.printLine("Cloudlet " + cloudlet.getCloudletId() + " is not matched."
                        + "It is possible a stage-in job");
                cloudlet.setVmId(0);

            }
            CondorVM vm = (CondorVM) mId2Vm.get(cloudlet.getVmId());
            if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
                getScheduledList().add(cloudlet);
                Log.printLine("Schedules " + cloudlet.getCloudletId() + " with "
                        + cloudlet.getCloudletLength() + " to VM " + cloudlet.getVmId());
            }
        }
    }
}
