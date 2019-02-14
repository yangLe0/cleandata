package cleandata;

import java.io.File;

public final class FileUtil {
	/**
	 * 文件夹是否存在，不存在创建
	 * @param path
	 */
	public static void createFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
}
