package com.wuhanqing.examples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.workflowsim.Task;
import org.workflowsim.WorkflowParser;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

public class Simulation {

	static FileOutputStream fos;
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 100; i++) {
			
			
			String file = "Montage_100_"+ (i+1) + ".xml";
			String daxPath = "/Users/wuhanqing/Documents/workflowInstance/Montage100/" + file;
			
			ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;
			ReplicaCatalog.init(file_system);
			WorkflowParser parser = new WorkflowParser(7,"","",daxPath);
			parser.parseXmlFile(daxPath);
			List<Task> list = parser.getTaskList();
			
			
			String filePath = "/Users/wuhanqing/Documents/Montage100new.txt";
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
