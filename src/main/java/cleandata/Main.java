package cleandata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.LinkedBlockingQueue;


public class Main {
	
	public static void main(String args[]) throws Exception{
		
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();		
		//线程数量
		int threadnum = 15;		
		for(int i = 0; i < threadnum; i++) {
			CleandataRunnable r = new CleandataRunnable(queue, "r" + i);
			r.start();
		}
		
		findfile(queue);
	}
	
	public static void findfile(LinkedBlockingQueue<String> queue) throws Exception {

		final String dirPath = "/data/transnftp/upload/ebay";	
//		final String dirPath = "D:\\test1";	
		File f = new File(dirPath);
		File[] fs = f.listFiles();
		
		int count = 0;
		
		BufferedReader reader = null;
		String str = null;
		
		try {
			for(File file:fs) {	//file:  D:\test\3f008a67753f44496647c30d46a635cf.json
				if(file.getName().endsWith(".json")) {
					reader = new BufferedReader(new FileReader(file));					
					while((str=reader.readLine()) != null) {
						
						queue.put(str);
						if (queue.size() > 1000) {
							 System.out.println("queue is full，wait 500 ms");
							 Thread.sleep(500);
						}
						
						//System.out.println("all count: " + count);
						//System.out.println("all count:" + count + " queue's number:" + queue.size());
					}
					
					count++;
					System.out.println("read over" + file.getName() + " 个数：" + count);
					reader.close();
				}
			}
			
		} catch (Exception e) {}
	}
	
	
/*	public static void findfile1 (LinkedBlockingQueue queue) throws Exception {
		CleandataRunnable r1 = new CleandataRunnable(queue,"r1");
		CleandataRunnable r2 = new CleandataRunnable(queue,"r2");
		CleandataRunnable r3 = new CleandataRunnable(queue,"r3");
		CleandataRunnable r4 = new CleandataRunnable(queue,"r4");
		r1.start();
		r2.start();
		r3.start();
		r4.start();
		
		String dirPath = "D:\\test";
		File f = new File(dirPath);
		File[] fs = f.listFiles();
		int count=0;
		for(File file:fs) {	//file:  D:\test\3f008a67753f44496647c30d46a635cf.json
			System.out.println(file);
			if(file.getName().endsWith(".json")) {
				queue.put(file);
			
			}
			
		}
		
		
	}*/
	
}
