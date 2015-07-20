package com.wuhanqing.examples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.workflowsim.Task;
import org.workflowsim.WorkflowParser;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

public class Simulation {

	public static FileOutputStream fos;

	public static int[][] aaa;

	public static void main(String[] args) {
		
		for (int i = 0; i < 100; i++) {
			
			
			String file = "Montage_100_"+ (i+1) + ".xml";
			String daxPath = "D:/hanqingwu/git/WorkflowSim-1.0/config/dax/Montage_100.xml";

			//随机设置任务可执行的虚拟机数组
			ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;
			ReplicaCatalog.init(file_system);
			WorkflowParser parser = new WorkflowParser(7,"","",daxPath);
			parser.parseXmlFile(daxPath);
			List<Task> list = parser.getTaskList();
			for (Task t : list) {
				t.cloudletId = t.getCloudletId() - 1;
			}
			int vmNum = Example.vmNum;
			aaa = new int[list.size()][vmNum];
			for (int j = 0; j < list.size(); j++) {
				for (int k = 0; k < vmNum; k++) {
					aaa[j][k] = 1;
				}
			}

			Random random = new Random();
			//randomSize: 可执行VM的数量
			int randomSize = 0;
			Map<Integer, String> map = new HashMap<>();

			for (int j = 0; j < list.size(); j++) {
				for (int k = 0; k < randomSize; k++) {
					Integer num = random.nextInt(vmNum);
					while (map.containsKey(num)) {
						num = random.nextInt(vmNum);
					}
					map.put(num, "");
				}
				for (int a : map.keySet()) {
					aaa[j][a] = 0;
				}
				map.clear();
			}
			///////////////////////////////////

			String filePath;
			filePath = "D:/hanqingwu/git/Montage100new.txt";
			Parameters.SchedulingAlgorithm MAXMIN_method = Parameters.SchedulingAlgorithm.MAXMIN;
			Parameters.SchedulingAlgorithm MINMIN_method = Parameters.SchedulingAlgorithm.MINMIN;
			Parameters.SchedulingAlgorithm HMPC_method = Parameters.SchedulingAlgorithm.HMPCnew;
			Parameters.SchedulingAlgorithm INVALID_scheduling = Parameters.SchedulingAlgorithm.INVALID;
			
			Parameters.PlanningAlgorithm INVALID_planning = Parameters.PlanningAlgorithm.INVALID;
			Parameters.PlanningAlgorithm HEFT_method = Parameters.PlanningAlgorithm.HEFT;
			
			
			try {
    			fos = new FileOutputStream(filePath, true);
    			fos.write(file.getBytes());
    			fos.write("\n".getBytes());
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}

			Example.simulation(MAXMIN_method, INVALID_planning, daxPath, filePath, file);
			Example.simulation(MINMIN_method, INVALID_planning, daxPath, filePath, file);
			Example.simulation(INVALID_scheduling, HEFT_method, daxPath, filePath, file);
			Example.simulation(HMPC_method, HEFT_method, daxPath, filePath, file);
			
			try {
    			fos.write("\n".getBytes());
    			fos.close();
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
		}
				
	}
}
