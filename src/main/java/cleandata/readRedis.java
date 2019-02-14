package cleandata;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class readRedis {
	public static void main(String args[]) throws IOException {
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		jedis.auth("tqhy817@2017");
		readallRediskeys(jedis);
	}
	
	public static void readallRediskeys(Jedis jedis) throws IOException {
		Set<?> s = jedis.keys("*");
		Iterator<?> it = s.iterator();
		String FILEPATH = "/data/transnftp/data/";
		FileWriter writer = new FileWriter(FILEPATH + "category3.txt", true);		
		while(it.hasNext()) {
			String key = (String) it.next();
			String value = jedis.get(key);
			System.out.println(key + ": " + value);
			writer.write(key + ": " + value);
			writer.write(System.getProperty("line.separator"));
		}
		writer.close();
	}
}
