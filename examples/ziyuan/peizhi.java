package ziyuan;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.commons.beanutils.*;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import sun.tools.jar.resources.jar;

public class peizhi {
	static double totalLength = 1000000000;
	static double vs1 = 2500;
	static double vs2 = 2000;
	static double vs3 = 1000;
	static double vs4 = 500;

	static double computerPower = 300000;
	static double timeLimit = 15000;

	public static void main(String[] args) throws IllegalAccessException,
			InvocationTargetException {
		
		Integer[] peizhi = {1,1,1,1};
		int[] c = {40,30,20,10};
		Vm vm1 = new Vm();
		vm1.setVs(vs1);
		vm1.setC(c[0]);
		vm1.setNum(peizhi[0]);
		for(int i = 0; i < 1000; i++)
		{
			
		}
		
		
		
		
		// Integer[] peizhi = {1,1,1,1};
		// int[] c = {40,30,20,10};
		//
		// Vm vm1 = new Vm();
		// vm1.setVs(vs1);
		// vm1.setNum(peizhi[0]);
		// vm1.setC(c[0]);
		//
		// Vm vm2 = new Vm();
		// vm2.setNum(peizhi[1]);
		// vm2.setVs(vs2);
		// vm2.setC(c[1]);
		//
		// Vm vm3 = new Vm();
		// vm3.setNum(peizhi[2]);
		// vm3.setVs(vs3);
		// vm3.setC(c[2]);
		//
		// Vm vm4 = new Vm();
		// vm4.setVs(vs4);
		// vm4.setNum(peizhi[3]);
		// vm4.setC(c[3]);
		//
		// double best = 0.0;
		//
		// double x = 0;
		// double X = Double.MAX_VALUE;
		//
		//
		// List<Integer[]> population = new ArrayList<Integer[]>();
		// List<Double> solution = new ArrayList<Double>();
		//
		// for(int i = 0; i < 50; i++)
		// {
		// Integer[] a = {100,100,100,100};
		// population.add(a);
		// solution.add(0.0);
		// }
		//
		// List<Integer> idle = new ArrayList<Integer>();
		// idle.add(0);idle.add(0);idle.add(0);idle.add(0);
		//
		// for(int i = 0; i < 10000; i++)
		// {
		// Random random = new Random();
		//
		// for(int j = 0; j < population.size(); j++)
		// {
		// Integer[] a = population.get(j);
		// double power = a[0]*vm1.getVs() + a[1]*vm2.getVs() + a[2]*vm3.getVs()
		// + a[3]*vm4.getVs();
		// if(power > computerPower)
		// {
		// int index = random.nextInt(4);
		// if(a[index] > 0)
		// {
		// a[index] = a[index] -1;
		// }
		// }
		// }
		//
		//
		// for(int j = 0; j < population.size(); j++)
		// {
		// Integer[] a = population.get(j);
		// double power1 = (a[0] * vm1.getVs());
		// double power2 = (a[1] * vm2.getVs());
		// double power3 = (a[2] * vm3.getVs());
		// double power4 = (a[3] * vm4.getVs());
		// double time = totalLength / (power1 + power2 + power3 + power4);
		// x = (time * (a[0] * vm1.getC() + a[1] * vm2.getC() +a[2] * vm3.getC()
		// + a[3] * vm4.getC()));
		// solution.set(j, x);
		// }
		//
		// double min = solution.get(0);
		// int k = 0;
		// int d = 0; // 最小值所在的位置
		// while(k < solution.size())
		// {
		// double index = solution.get(k);
		// k++;
		// if(min > index)
		// {
		// min = index;
		// d = k - 1;
		// k = 0;
		// // Integer[] best1 = {1,1,1,1};
		// // = solution.get(d);
		// // BeanUtils.copyProperties(best1, population.get(d));
		//
		// // population.set(random.nextInt(50), best1);
		// }
		// }
		// Integer[] aa = population.get(d);
		// double time = totalLength / (aa[0]*vm1.getC() + aa[1]*vm2.getC() +
		// aa[2]*vm3.getC() + aa[3]*vm4.getC());
		// if(X > solution.get(d) && time < timeLimit)
		// {
		// X = solution.get(d);
		//
		// Integer[] a = population.get(d);//最优的值
		// vm1.setNum(a[0]);
		// vm2.setNum(a[1]);
		// vm3.setNum(a[2]);
		// vm4.setNum(a[3]);
		// }
		//
		//
		// // double cross = random.nextDouble();
		// //
		// // if(cross < 0.7)
		// // {
		// // int p1 = random.nextInt(50);//种群中哪两个进行交叉
		// // int p2 = random.nextInt(50);
		// //
		// // int p3 = random.nextInt(4);//交叉位置
		// // int p4 = random.nextInt(4);
		// //
		// // Integer[] a1 = population.get(p1);
		// // Integer[] a2 = population.get(p2);
		// //
		// // Integer[] index1 = a1;
		// // Integer[] index2 = a2;
		// //
		// // index1[p3] = a2[p3];
		// // index2[p4] = a1[p4];
		// //
		// // a1 = index1;
		// // a2 = index2;
		// //
		// // population.set(p1, a1);
		// // population.set(p2, a2);
		// //
		// // }
		//
		// double variation = random.nextDouble();
		// if(variation < 1)
		// {
		// int p1 = random.nextInt(50);//种群中交叉的个数
		// int p2 = random.nextInt(50);
		// int p3 = random.nextInt(50);
		//
		// int p4 = random.nextInt(4);//种群中交叉的位置
		// int p5 = random.nextInt(4);
		// int p6 = random.nextInt(4);
		//
		// Integer[] a1 = population.get(p1);
		// Integer[] a2 = population.get(p2);
		// Integer[] a3 = population.get(p3);
		//
		// a1[p4] = a1[p4] + 1;
		// a2[p5] = a1[p5] + 1;
		// a3[p6] = a1[p6] + 1;
		//
		// population.set(p1, a1);
		// population.set(p2, a2);
		// population.set(p3, a3);
		//
		// }
		//
		// }
		//
		// System.out.println(vm1.getNum());
		// System.out.println(vm2.getNum());
		// System.out.println(vm3.getNum());
		// System.out.println(vm4.getNum());
		// System.out.println();
		// System.out.println(X);
		//
		// }

	}
}
