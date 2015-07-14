package org.workflowsim;

public class HostType_Without_Sleep_Nap
{
    private static final int BASE = 500;
    
	public static final String Acer_AC100 = "Acer_AC100";
	
	public static final String Hitachi_DL2 = "Hitachi_DL2";
	
	public static final String Huawei_RH2288 = "Huawei_RH2288";
	
	public static final String IBM_x3550 = "IBM_x3550";
	
	/** 4cores  */
	public static final double Acer_AC100_W_WORK = 58.0;
	public static final double Acer_AC100_W_IDLE = 21.5;
	public static final double Acer_AC100_W_NAP_PER_PE = 0;
	public static final double Acer_AC100_W_SLEEP = 21.5;
	
	/** 4cores  */
	public static final double Hitachi_DL2_W_WORK = 94.0;
	public static final double Hitachi_DL2_W_IDLE = 45.8;
	public static final double Hitachi_DL2_W_NAP_PER_PE = 0;
	public static final double Hitachi_DL2_W_SLEEP = 45.8;
	
	/** 8cores  */
	public static final double Huawei_RH2288_W_WORK = 236;
	public static final double Huawei_RH2288_W_IDLE = 66.8;
	public static final double Huawei_RH2288_W_NAP_PER_PE = 0;
	public static final double Huawei_RH2288_W_SLEEP = 66.8;
	
	/** 8cores  */
	public static final double IBM_x3550_W_WORK = 190;
	public static final double IBM_x3550_W_IDLE = 56.5;
	public static final double IBM_x3550_W_NAP_PER_PE = 0;
	public static final double IBM_x3550_W_SLEEP = 56.5;
	
	
	private HostType_Without_Sleep_Nap() {
        throw new UnsupportedOperationException("WorkflowSim Tags cannot be instantiated");
    }
	
}
