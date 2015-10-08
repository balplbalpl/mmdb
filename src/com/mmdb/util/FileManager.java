package com.mmdb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.mmdb.core.utils.TimeUtil;
import com.mmdb.ruleEngine.Tool;

/**
 * 文件下载都要现在服务器保存,给前台返回链接,然后由前台实现下载...
 * 
 * @author xj
 * 
 */
public class FileManager implements Runnable {
	private final String filePath;
	private static FileManager instance = new FileManager();

	private FileManager() {
		// String path = this.getClass().getResource("").getPath();
		// path = path.substring(1, path.indexOf("WEB-INF"));
		filePath = Tool.getRealPath() + "temp/";
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		new Thread(this).start();
	}

	public static FileManager getInstance() {
		return instance;
	}

	public String getPath() {
		return filePath;
	}

	/**
	 * 文件名会自动加时间戳
	 * 
	 * @param prefix
	 *            配置项
	 * @param suffix
	 *            xml
	 * @return
	 */
	public File createFile(String prefix, String suffix) {
		String time = TimeUtil.getTime(TimeUtil.YMDHMS).replaceAll(" ", "_")
				.replaceAll(":", "-");
		String name = prefix + time + "." + suffix;
		File file = new File(filePath + name);
		return file;
	}

	public File createFile(InputStream inputStream, String prefix, String suffix) {
		File newFile = createFile(prefix, suffix);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		OutputStream outputStream = null;
		try {
			if (inputStream != null) {
				outputStream = new FileOutputStream(newFile);
				int bytesRead;
				byte[] buffer = new byte[2048];

				while ((bytesRead = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, bytesRead);
				}

				outputStream.flush();
				inputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return newFile;
	}

	public File createFile(String data, String prefix, String suffix) {
		File newFile = createFile(prefix, suffix);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		OutputStream outputStream = null;
		try {
			if (data != null) {
				outputStream = new FileOutputStream(newFile);
				outputStream.write(data.getBytes("utf-8"));
				outputStream.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return newFile;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(3600000);
				File file = new File(filePath);
				File[] listFiles = file.listFiles();
				for (File file2 : listFiles) {
					if (file2.exists()) {
						file.delete();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除全部,包含只文件夹
	 * 
	 * @param file
	 */
	public void deleteAll(File file) {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (File file2 : list) {
				deleteAll(file2);
			}
			file.delete();
		} else {
			file.delete();
		}
	}

	/**
	 * 清空文件管理的临时文件夹
	 */
	public void deleteAll() {
		deleteAll(new File(filePath));
	}

	public void copy(File sFile, File tFile) throws Exception {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(sFile);
			fos = new FileOutputStream(tFile);
			byte[] b = new byte[102400];
			int len = 0;
			while ((len = fis.read(b)) != -1) {
				fos.write(b, 0, len);
			}
			fos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
		}
	}
	
	public void copyFile(File target, byte[] data) {
		if (target.exists()) {
			target.delete();
		}
		try {
			target.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(target);
			fos.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		FileManager instance2 = FileManager.getInstance();
		File createFile = instance2.createFile("texstxasdf", "xml");
		System.out.println(createFile.getPath());
	}
}
