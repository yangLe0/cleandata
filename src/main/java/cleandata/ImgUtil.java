package cleandata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import sun.misc.BASE64Decoder;

public final class ImgUtil {
	/**
	 *  base64字符合成图片
	 * @param imgStr
	 * @param id
	 * @param path
	 * @return
	 */
/*	public static JSONArray GenerateImageJson(JSONArray imgjson, String imgPath) {
		
		try {
			
			JSONArray newimages = new JSONArray();
			
			for(int i = 0,n = imgjson.size(); i < n; i++) {
				
				String id = imgjson.getJSONObject(i).getString("id");
				if(id.equals("baf953b8b1085a7bae648952a95bf4e5840d9845"))
					continue;

				String base64 = imgjson.getJSONObject(i).getString("base64");
				if (base64 == null)
					continue;	

				// Base64解码
				byte[] b = new BASE64Decoder().decodeBuffer(base64);

				//判断文件大小，格式是否符合要求
				if(!checkSize(b))
				{
					System.out.println("图片小于2KB   图片为： " + imgPath +"/" +id);
					continue;	
				}

				if(!verifyJPG(b))
				{
					System.out.println("图片格式不是jpg");
					continue;	
				}					

				for (int j = 0,len=b.length; j < len; ++j) {
					if (b[j] < 0) {// 调整异常数据
						b[j] += 256;
					}
				}

				// 生成jpg图片
				FileUtil.createFile(imgPath);
				String imgFilePath = imgPath + "/" + id + ".jpg";
				OutputStream out = new FileOutputStream(imgFilePath);
				out.write(b);
				out.flush();
				out.close();

				JSONObject imageinfo = new JSONObject();
				imageinfo.put("id", id);
				imageinfo.put("tmpPath", imgFilePath);
				newimages.add(imageinfo);				
			}

			List<String> wrongImgId = verify(imgPath);

			if(wrongImgId.isEmpty()) {

				return newimages;
			}else {

				for(int i = 0, l = wrongImgId.size(); i < l; i++) {

					for(int j = 0, n = newimages.size(); j < n; j++) {

						String id = wrongImgId.get(i).split("\\.")[0];
						JSONObject jsonObj = newimages.getJSONObject(j);

						if(id.equals(jsonObj.get("id"))) {
							newimages.remove(jsonObj);
							break;
						}
					}
				}
				return newimages;
			}

		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}


		//return newimages;
	}*/
	public static String GenerateImage(String imgStr, String id, String path) {

		FileUtil.createFile(path);

		// 图像数据为空
		if (imgStr == null)
			return null;
		//BASE64Decoder decoder = new BASE64Decoder();

		try {
			// Base64解码
			byte[] b = new BASE64Decoder().decodeBuffer(imgStr);

			//判断文件大小，格式是否符合要求
			if(!checkSize(b))
			{
				System.out.println("图片大小不符合要求");
				return null;
			}

			if(!verifyJPG(b))
			{
				System.out.println("图片格式不是jpg");
				return null;
			}					

			for (int i = 0,len=b.length; i < len; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}

			// 生成jpg图片
			String imgFilePath = path + "/" + id + ".jpg";
			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();

			//判断图片能否打开
			/*FileInputStream fi = new FileInputStream(new File(imgFilePath));
			if(ImageIO.read(fi) == null)
			{
				fi.close();
				System.out.println("图片打不开"+path+ "/" + id);
				return null;
			}		
			fi.close();
			return imgFilePath;*/
			imgFilePath = vertify(imgFilePath);
			return imgFilePath;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 判断图片能否打开
	 * @param imgFilePath
	 * @return
	 * @throws IOException 
	 */
	public static String vertify(String imgFilePath) throws IOException {
		String path = imgFilePath;
		FileInputStream fi = new FileInputStream(new File(path));
		try {
			if(ImageIO.read(fi) == null)
			{
				fi.close();
				return null;
			}
			fi.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			fi.close();
//			e.printStackTrace();
			System.out.println("---------------");
			File f = new File(path);
			f.delete();
			return null;
		}
		
		return path;		
	}
	
/*	public static List<String> verify(String imgDirPath) throws IOException{
		File f = new File(imgDirPath);
		File[] fs = f.listFiles();

		FileInputStream fi = null;

		List<String> wrongImgId=new ArrayList<String>();

		for(File file:fs) {
			fi = new FileInputStream(file);
			if(ImageIO.read(fi) == null)
			{
				fi.close();
				System.out.println("发生错误" + file.getName());
				wrongImgId.add(file.getName());
			}
			fi.close();
		}
		fi.close();
		return wrongImgId;

	}*/

	/**
	 * 校验格式是否为jpeg
	 * @param bytes
	 * @return
	 */
	public static boolean verifyJPG(byte[] bytes) {
		String suffix = null;
		try {			
			ImageInputStream imageInputstream = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes));
			//不使用磁盘缓存
			ImageIO.setUseCache(false);
			Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputstream);
			if (it.hasNext()) {
				ImageReader imageReader = it.next();
				suffix = imageReader.getFormatName().trim().toLowerCase();	             
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("suffix: " + suffix);
		return suffix.equals("jpeg");
	}

	/**
	 * 校验文件大小
	 *
	 * @param
	 * @return
	 */
	public static boolean checkSize(byte[] bytes) {
		//符合条件的照片大小（可配置） 单位：M
		double imgSize = 0.002;
		//图片转base64字符串一般会大，这个变量就是设置偏移量。可配置在文件中，随时修改。目前配的是0。后续看情况适当做修改
		double deviation = 0.0;
		int length = bytes.length;
		//原照片大小
		double size = (double) length / 1024 / 1024 * (1 - deviation);
		//System.out.println("照片大小为：" + size + "M");
		return size > imgSize;
	}
}
