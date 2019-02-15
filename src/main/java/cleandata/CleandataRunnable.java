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
		}catch (Exception e) {
			
			e.printStackTrace();
			
			CleandataRunnable r = new CleandataRunnable(queue, "r" + count);
			r.start();
		} /*catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/

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
		
}
