package org.workflowsim.planning;

import java.util.List;

import org.workflowsim.CondorVM;
import org.workflowsim.Task;


public class Determine extends BasePlanningAlgorithm{

	@Override
	public void run() throws Exception {
		List<Task> taskList = getTaskList();
		List<CondorVM> vmList = getVmList();
		taskList.get(0).setVmId(1);
		taskList.get(1).setVmId(1);
		taskList.get(2).setVmId(3);
		taskList.get(3).setVmId(3);
		taskList.get(4).setVmId(4);
		taskList.get(5).setVmId(1);
		taskList.get(6).setVmId(1);
		taskList.get(7).setVmId(3);
		taskList.get(8).setVmId(1);
		taskList.get(9).setVmId(3);
		taskList.get(10).setVmId(1);
		taskList.get(11).setVmId(4);
		taskList.get(12).setVmId(2);
		taskList.get(13).setVmId(1);
		taskList.get(14).setVmId(4);
		taskList.get(15).setVmId(1);
		taskList.get(16).setVmId(4);
		taskList.get(17).setVmId(1);
		taskList.get(18).setVmId(4);
		taskList.get(19).setVmId(1);

		System.out.println(taskList);
 	}

}
