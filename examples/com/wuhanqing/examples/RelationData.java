package com.wuhanqing.examples;

import java.io.Serializable;

/**
 * Created by wuhanqing on 15/7/26.
 */
public class RelationData implements Serializable{

    public int[][] data;
    public int vmNum;
    public int cloudLetSize;

    public int[][] getData() {
        return data;
    }

    public void setData(int[][] data) {
        this.data = data;
    }

    public int getVmNum() {
        return vmNum;
    }

    public int getCloudLetSize() {
        return cloudLetSize;
    }

    public void setCloudLetSize(int cloudLetSize) {
        this.cloudLetSize = cloudLetSize;
    }

    public void setVmNum(int vmNum) {
        this.vmNum = vmNum;
    }
}
