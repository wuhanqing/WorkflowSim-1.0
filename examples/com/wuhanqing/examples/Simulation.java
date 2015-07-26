package com.wuhanqing.examples;

import java.io.*;
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

		long t1 = System.currentTimeMillis();

		for (int i = 0; i < 1000; i++) {

			String dataPath = "CyberShake_100_"+ (i+1);
			String path = "/Users/wuhanqing/workflow/Relation/" + dataPath;

			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
				RelationData data = (RelationData) ois.readObject();
				aaa = data.getData();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			if (aaa == null) {
				System.out.println("任务-虚拟机关系文件读取失败");
				return;
			}

			String file = "CyberShake_100_"+ (i+1) + ".xml";
			String daxPath = "/Users/wuhanqing/workflow/CyberShake100/" + file;


			String filePath = "/Users/wuhanqing/workflow/Montage300.txt";
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
			Example.simulation(HMPC_method, INVALID_planning, daxPath, filePath, file);
			
			try {
    			fos.write("\n".getBytes());
    			fos.close();
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
		}

		long t2 = System.currentTimeMillis() - t1;
		System.out.printf(Long.toString(t2));

	}
}
