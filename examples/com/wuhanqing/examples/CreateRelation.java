package com.wuhanqing.examples;

import org.cloudbus.cloudsim.File;
import org.workflowsim.Task;
import org.workflowsim.WorkflowParser;
import org.workflowsim.utils.ReplicaCatalog;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by wuhanqing on 15/7/26.
 */
public class CreateRelation {

    public static int[][] aaa;

    public static int cloutLetSize = 1000;

    public static void main(String[] args) {




        for (int i = 0; i < 1000; i++) {
            String file = "CyberShake_100_"+ (i+1);
            String path = "/Users/wuhanqing/workflow/Relation/" + file;

            int vmNum = Example.vmNum;
            aaa = new int[cloutLetSize][vmNum];
            for (int j = 0; j < cloutLetSize; j++) {
                for (int k = 0; k < vmNum; k++) {
                    aaa[j][k] = 1;
                }
            }

            Random random = new Random();
            //randomSize: 可执行VM的数量
            int randomSize = 0;
            Map<Integer, String> map = new HashMap<>();

            for (int j = 0; j < cloutLetSize; j++) {
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

            RelationData data = new RelationData();
            data.setData(aaa);
            data.setCloudLetSize(cloutLetSize);
            data.setVmNum(vmNum);


            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                oos.writeObject(data);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
