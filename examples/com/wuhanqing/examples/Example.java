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
package com.wuhanqing.examples;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Cpu;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmAllocationPolicyWuhanqing;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.examples.power.random.RandomConstants;
import org.cloudbus.cloudsim.examples.power.random.RandomHelper;
import org.cloudbus.cloudsim.examples.power.random.RandomRunner;
import org.cloudbus.cloudsim.power.models.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.workflowsim.CondorVM;
import org.workflowsim.HostConsumptionMonitor;
import org.workflowsim.HostType;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.Job;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

import sun.nio.cs.ext.IBM935;

/**
 * This WorkflowSimExample creates a workflow planner, a workflow engine, and
 * one schedulers, one data centers and 20 vms. You should change daxPath at least. 
 * You may change other parameters as well.
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class Example {

    public static int vmNum = 55;

    ////////////////////////// STATIC METHODS ///////////////////////
    /**
     * Creates main() to run this example
     * This example has only one datacenter and one storage
     */
    public static void simulation(Parameters.SchedulingAlgorithm sch_method, Parameters.PlanningAlgorithm pln_method,
    		String daxPath, String filePath, String fileName) {


       try {
    	   
    	    String code = "i";
            // First step: Initialize the WorkflowSim package. 

            /**
             * However, the exact number of vms may not necessarily be vmNum If
             * the data center or the host doesn't have sufficient resources the
             * exact vmNum would be smaller than that. Take care.
             */
            int vmNum = Example.vmNum;//number of vms;
            int hostNum = 10;
            /**
             * Should change this based on real physical path
             */
//            String daxPath = "/Users/wuhanqing/Dropbox/Java/WorkflowSim-1.0-master/config/dax/Sipht_700_2.xml";
            if(daxPath == null){
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }
            File daxFile = new File(daxPath);
            if(!daxFile.exists()){
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }

            /**
             * Since we are using MINMIN scheduling algorithm, the planning algorithm should be INVALID 
             * such that the planner would not override the result of the scheduler
             */
//            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.MAXMIN;
//            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.HEFT;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

            /**
             * No overheads 
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);
            
            /**
             * No Clustering
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

//            RandomHelper.createCloudletList(2, RandomConstants.NUMBER_OF_VMS);
//             Helper.createVmList(2, 50);

            /**
             * Initialize static parameters
             */
            Parameters.init(vmNum, daxPath, null,
                    null, op, cp, sch_method, pln_method,
                    null, 0);
            ReplicaCatalog.init(file_system);

            // before creating any entities.
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0",hostNum);
            
        //    EnergySavingMonitor energySavingMonitor = new EnergySavingMonitor("Monitor_0", datacenter0);

            /**
             * Create a WorkflowPlanner with one schedulers.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine.
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            /**
             * Create a list of VMs.The userId of a vm is basically the id of the scheduler
             * that controls this vm. 
             */
            List<CondorVM> vmlist0 = Helper.createCondorVmList(wfEngine.getSchedulerId(0), vmNum, hostNum);

            /**
             * Submits this list of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);

            /**
             * Binds the data centers with the scheduler.
             */
            wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);

            CloudSim.startSimulation();


            List<Job> outputList0 = wfEngine.getJobsReceivedList();

            CloudSim.stopSimulation();

            
            printJobList(outputList0, filePath);
            printHostConsume(datacenter0, filePath);

        } catch (Exception e) {
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    protected static WorkflowDatacenter createDatacenter(String name, int hostNum) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<Host>();

        hostList = Helper.createCondorHostList(hostNum);
        
        

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now
        WorkflowDatacenter datacenter = null;


        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a storage object.
        /**
         * The bandwidth within a data center in MB/s.
         */
        int maxTransferRate = 15;// the number comes from the futuregrid site, you can specify your bw
        
        try {
            HarddriveStorage s1 = new HarddriveStorage(name, 1e12);
            s1.setMaxTransferRate(maxTransferRate);
            storageList.add(s1);
            
            HostConsumptionMonitor hostComsumptionMonitor = new HostConsumptionMonitor("Monitor_0");
            datacenter = new WorkflowDatacenter(hostComsumptionMonitor, name, characteristics, new VmAllocationPolicyWuhanqing(hostList), storageList, 0);
           // energySavingMonitor.setDatacenter(datacenter);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        

        return datacenter;
    }

    /**
     * Prints the job objects
     *
     * @param list list of jobs
     */
    protected static void printJobList(List<Job> list, String filePath) {
        int size = list.size();
        Job job;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Depth");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            job = list.get(i);
            Log.print(indent + job.getCloudletId() + indent + indent);

            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + job.getResourceId() + indent + indent + indent + job.getVmId()
                        + indent + indent + indent + dft.format(job.getActualCPUTime())
                        + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth());
            } else if (job.getCloudletStatus() == Cloudlet.FAILED) {
                Log.print("FAILED");

                Log.printLine(indent + indent + job.getResourceId() + indent + indent + indent + job.getVmId()
                        + indent + indent + indent + dft.format(job.getActualCPUTime())
                        + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth());
            }
            
            if (i == size - 1) {
            	try {
            		Double d = job.getFinishTime();
					FileOutputStream fos = new FileOutputStream(filePath, true);
					fos.write((dft.format(d) + ",").getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            
        }

    }
    
    protected static void printHostConsume(Datacenter datacenter, String filePath)
    {   
    	HostConsumptionMonitor hostConsumptionMonitor = datacenter.getHostConsumptionMonitor();
    	List<Host> hostList = hostConsumptionMonitor.getHostList();
    	Iterator<Host> list = hostList.iterator();
    	while(list.hasNext())
    	{
    		Host host = list.next();
    	}

//    	List<Double> hostSleepTimeList = hostConsumptionMonitor.getHostSleepTimeList();
    	
    	Log.printLine();
    	Log.printLine("The host consumption as follows:");
    	Log.printLine("======================================");
    	Log.printLine("Model" + "     " + "Consumption" );
    			
    	DecimalFormat dft = new DecimalFormat("###.##");
    	
    	for(int i = 0; i < hostList.size(); i++)
    	{
    		Log.printLine(hostList.get(i).getName() + "       " 
    				+ dft.format(hostList.get(i).getConsumption()) + "       ");
    	}
    	Log.printLine();
    	Log.printLine("The datacenter consumption is:" + "  " + dft.format(datacenter.getConsumption()));
    	
    	try {
    		Double d = datacenter.getConsumption();
			FileOutputStream fos = new FileOutputStream(filePath, true);
			fos.write((dft.format(d)).getBytes());
			fos.write("\n".getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
}