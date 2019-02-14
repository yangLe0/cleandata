package cleandata;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;

public final class CleandataRunnable extends Thread {
	private String threadName;
	LinkedBlockingQueue<?> queue;
	
	//write category file path
	static final String FILEPATH = "/data/transnftp/data/ebay/json/";
	
	//write image path
	static final String IMAPATH = "/data/transnftp/data/ebay/image/";
	
	Jedis jedis = new Jedis("127.0.0.1", 6379);

	CleandataRunnable(LinkedBlockingQueue<?> queue, String name) {
		this.queue = queue;
		threadName = name;
		System.out.println("Creating " + threadName);
	}

	@Override
	public void run() {
		System.out.println("Running " + threadName);		
		FileUtil.createFile(FILEPATH);
		int count = 0;
		jedis.auth("tqhy817@2017");
		try {	
			while (true) {
				long startTime = System.currentTimeMillis();// current time
				//long startMem = Runtime.getRuntime().freeMemory();
				JSONObject jsondata = JSONObject.parseObject((String) queue.take());
				/**
				 * 排除分类3空项
				 */
				String category3 = jsondata.get("category3").toString().trim();	
			
				if(category3.length() <= 0) {
					System.out.println("category3为空 id: " + jsondata.get("id"));
					continue;
				}
				category3 = category3.replace("/", "-");
				
				/**
				 * 验证图片
				 */
				JSONArray images = JSONArray.parseArray(jsondata.get("images").toString());
				JSONArray newimages = new JSONArray();
				for (int i = 0, len=images.size();i < len; i++) {
					
					String base64 = images.getJSONObject(i).getString("base64");
					String id = images.getJSONObject(i).getString("id");
					if(id.equals("baf953b8b1085a7bae648952a95bf4e5840d9845"))
						continue;
					
					String tmpPath = ImgUtil.GenerateImage(base64, id, IMAPATH + jsondata.getString("id"));// 合成图片
						
					if(tmpPath != null) {
						JSONObject imageinfo = new JSONObject();
						imageinfo.put("tmpPath", tmpPath);
						imageinfo.put("id", id);
						newimages.add(imageinfo);
					}
					
				}
				//++++++++++++++++++++++++

				//newimages = ImgUtil.GenerateImageJson(images, IMAPATH + jsondata.getString("id"));
				//++++++++++++++++++++++++
				/**
				 * 排除图片不合格项
				 */
				if(newimages.size() == 0)
				{
					System.out.println("没有图片 id: " + jsondata.get("id"));
					continue;
				}
				
				jsondata.put("images", newimages);
				/**
				 * 新json写进分类文件
				 */
				
				FileWriter writer = new FileWriter(FILEPATH + category3 + ".json", true);
				writer.write(jsondata.toJSONString());
				writer.write(System.getProperty("line.separator"));
				writer.close();
				/**
				 * 分类放redis里
				 */
				setObject(category3, jedis);
				
				long endTime = System.currentTimeMillis();
				//long endMem = Runtime.getRuntime().freeMemory();
				count++;
				System.out.println("use time：" + (endTime - startTime) + "ms   " + threadName + " current count：" + count);// + "  Use memory: "+ (startMem - endMem));// + " nowTime" + new Date());
								
				if(queue.isEmpty()) {
					break;
				}
			}
			Date time = new Date();
			System.out.println("End：" + time.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 分类存入redis，计数器
	 * @param category
	 * @param jedis
	 */
	public void setObject(String category,Jedis jedis) {
		
		if (jedis.get(category) == null) {
			jedis.set(category, "1");
		} else {
			int count = Integer.parseInt(jedis.get(category));
			count++;
			jedis.set(category, String.valueOf(count));
		}
	}
	
/*	public void run() {
		System.out.println("Running " + threadName);
		
		try {
			while (queue.isEmpty()) {
				Thread.currentThread().sleep(10);
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// 写入文件地址
		//final String filePath = "D:\\test1\\category\\";
		File sf = new File(FILEPATH);
		if (!sf.exists()) {
			sf.mkdirs();
		}
		
		// 存入图片地址
		//final String imgPath = "D:\\test1\\img\\";
		
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		
		String category3=null;
		//String Eid=null;
		String base64=null;
		String id=null;
		//String imagePath=null;
		String tmpPath=null;
		//String catePath=null;
		JSONObject jsondata=null;
		long startTime=0;
		long endTime=0;
		JSONArray images=null;
		//boolean verify = true;
		JSONArray newimages=null;
		JSONObject imageinfo=null;
		String content=null;
		FileWriter writer=null;
		//File tmpimage=null;
		FileInputStream fi=null;
		//BufferedImage sourceImg=null;
		//int picWidth=0;
		while (!queue.isEmpty()) {

			try {
				startTime = System.currentTimeMillis();// 获取当前时间
				jsondata = (JSONObject) queue.take();

				images = JSONArray.parseArray(jsondata.get("images").toString());
				newimages = new JSONArray();
				//Eid = jsondata.getString("id"); // 商品ID
				//verify = true;
				// 图片合成、转存
				for (int i = 0; i < images.size(); i++) {

					base64 = images.getJSONObject(i).getString("base64");
					id = images.getJSONObject(i).getString("id");
					//imagePath = IMAPATH + Eid;

					tmpPath = GenerateImage(base64, id, IMAPATH + jsondata.getString("id"));// 合成图片
					// 验证图片
					//tmpimage = new File(tmpPath);
					fi = new FileInputStream(new File(tmpPath));
					//sourceImg = ImageIO.read(fi);
					//picWidth = ImageIO.read(fi).getWidth();
					if (ImageIO.read(fi).getWidth() < 0) {
						//verify = false;
						System.out.println("图片损坏：" + tmpPath);
						continue;
					}
					fi.close();

					imageinfo = new JSONObject();
					imageinfo.put("tmpPath", tmpPath);
					imageinfo.put("id", id);
					newimages.add(imageinfo);

				}
				category3 = jsondata.get("category3").toString();
				//if (verify == true) {
					jsondata.put("images", newimages);
					content = jsondata.toJSONString();
					// 当前分类文件地址
					
					//catePath = FILEPATH + category3 + ".json";
					writer = new FileWriter(FILEPATH + category3 + ".json", true);
					writer.write(content);
					writer.write(System.getProperty("line.separator"));
					writer.close();
					setObject(category3,jedis);// 分类放redis里
				//}
				endTime = System.currentTimeMillis();
				System.out.println(
						"程序运行时间：" + (endTime - startTime) + "ms   " + threadName + " 处理category3：" + category3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}*/

	/**
	 * 放文件进队列，所以处理的是文件
	 * 
	 * @param category
	 */
	// @Override
	/*
	 * public void run1(){ System.out.println("Running "+threadName); while(true) {
	 * try { File file = (File) queue.take(); BufferedReader reader = new
	 * BufferedReader(new FileReader(file)); String str; int count=0; //存入图片地址
	 * String imgPath = "D:\\test1\\img\\"; //写入文件地址 String filePath =
	 * "D:\\test1\\category\\"; File sf = new File(filePath); if (!sf.exists()) {
	 * sf.mkdirs(); } while((str=reader.readLine())!=null) { JSONObject jsondata =
	 * JSONObject.parseObject(str); String category3 =
	 * jsondata.get("category3").toString(); if(category3.length()>0) {
	 * 
	 * count++;
	 * System.out.println("count:"+count+"  当前分类："+jsondata.get("category3").
	 * toString()+"threadName:"+threadName); JSONArray images =
	 * JSONArray.parseArray(jsondata.get("images").toString()); JSONArray newimages
	 * = new JSONArray(); String Eid=jsondata.getString("id"); //商品ID boolean
	 * verify=true; //图片合成、转存 for(int i=0;i<images.size();i++) {
	 * 
	 * String base64=images.getJSONObject(i).getString("base64"); String
	 * id=images.getJSONObject(i).getString("id"); String imagePath = imgPath+Eid;
	 * 
	 * String tmpPath=GenerateImage(base64,id,imagePath);//合成图片 //验证图片 File tmpimage
	 * = new File(tmpPath); FileInputStream fi = new FileInputStream(tmpimage);
	 * BufferedImage sourceImg =ImageIO.read(fi); int picWidth=
	 * sourceImg.getWidth(); if(picWidth<0) { verify=false;
	 * System.out.println("图片损坏："+tmpPath); } fi.close();
	 * 
	 * JSONObject imageinfo = new JSONObject(); imageinfo.put("tmpPath", tmpPath);
	 * imageinfo.put("id", id); newimages.add(imageinfo);
	 * 
	 * }
	 * 
	 * if(verify==true) { jsondata.put("images", newimages); String
	 * content=jsondata.toJSONString(); //当前分类文件地址 String catePath =
	 * filePath+category3+".json"; FileWriter writer = new FileWriter(catePath,
	 * true); writer.write(content);
	 * writer.write(System.getProperty("line.separator")); writer.close();
	 * setObject(category3);//分类放redis里 } } } reader.close();
	 * 
	 * } catch (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); } }
	 * 
	 * }
	 */
	
	
	
}
