package cn.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * 生成报表
	 * 
	 * @param fileName
	 * @param filePath
	 * @param dataList
	 */
	public static void createCvsFile(String fileName, String filePath, List<List<Object>> dataList, Object[] head) {

		BufferedWriter csvWtriter = null;
		File csvFile = null;

		try {
			List<Object> headList = Arrays.asList(head);

			csvFile = new File(filePath + fileName);
			File parent = csvFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			csvFile.createNewFile();
			// GB2312使正确读取分隔符","
			csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "gbk"), 1024);
			int num = headList.size() / 2;
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < num; i++) {
				buffer.append(" ,");
			}

			csvWtriter.write(buffer.toString() + fileName + buffer.toString());
			csvWtriter.newLine();

			// 写入文件头部
			writeRow(headList, csvWtriter);
			// 写入文件内容
			for (List<Object> row : dataList) {
				writeRow(row, csvWtriter);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("生成报表文件异常：" + e.getMessage());
		} finally {
			try {
				csvWtriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 写入文件
	 * 
	 * @param row
	 * @param csvWriter
	 * @throws IOException
	 */
	private static void writeRow(List<Object> row, BufferedWriter csvWriter) throws IOException {
		for (Object data : row) {
			StringBuffer sb = new StringBuffer();
			String rowStr = sb.append("\"").append(data).append("\",").toString();
			csvWriter.write(rowStr);
		}
		csvWriter.newLine();
	}

	/**
	 * 生成 zip打包文件
	 * 
	 * @param files
	 * @param strZipName
	 */
	public static void createZip(List<File> list, String strZipName) {
		try {
			byte[] buffer = new byte[1024];

			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(strZipName));

			// 需要同时下载的两个文件result.txt ，source.txt

			for (File file : list) {
				FileInputStream fis = new FileInputStream(file);

				out.putNextEntry(new ZipEntry(file.getName()));

				int len;

				// 读入需要下载的文件的内容，打包到zip文件

				while ((len = fis.read(buffer)) > 0) {

					out.write(buffer, 0, len);

				}

				out.closeEntry();

				fis.close();
			}

			out.close();

			System.out.println("生成Demo.zip成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("打包文件异常：" + e.getMessage());
		}
	}

	/**
	 * 获取文件大小
	 * 
	 * @param path
	 */
	@SuppressWarnings("resource")
	public static String getFileSize11(String path) {
		String size = null;
		try {
			size = new FileInputStream(new File(path)).available() / 1024 / 1024 + "M";
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("打包文件异常：" + e.getMessage());
		}
		return size;
	}

	public static String getFileSize(String path) {
		File file  = new File(path);
		String size = "";
		if (file.exists() && file.isFile()) {
			long fileS = file.length();
			DecimalFormat df = new DecimalFormat("#.00");
			if (fileS < 1024) {
				size = "1KB";
			} else {
				size = df.format((double) fileS / 1024) + "KB";
			} 
		} else if (file.exists() && file.isDirectory()) {
			size = "";
		} else {
			size = "0KB";
		}
		return size;
	}

	public static void main(String[] args) {
		// File[] files = {new File("C:/test/1255/20170913/3月实号包.csv"),new
		// File("C:/test/1255/20170913/6月实号包.csv"),new
		// File("C:/test/1255/20170913/未知号码包.csv")};
		// FileUtils.createZip(files,"C:/test/1255/20170913/Demo.zip");

		System.out.println(FileUtils.getFileSize("D:/test/mk0004.txt"));
	}

	// String therefileName = "thereCSV.csv";// 文件名称
	// String thereFilePath = "c:/test/"; // 文件路径
	//
	// String sixfileName = "sixCSV.csv";// 文件名称
	// String sixFilePath = "c:/test/"; // 文件路径
	//
	// String unkonwnfileName = "unkonwnCSV.csv";// 文件名称
	// String unknownFilePath = "c:/test/"; // 文件路径
	//

	// 文件下载，使用如下代码
	// response.setContentType("application/csv;charset=gb18030");
	// response.setHeader("Content-disposition", "attachment; filename="
	// + URLEncoder.encode(fileName, "UTF-8"));
	// ServletOutputStream out = response.getOutputStream();
	// csvWtriter = new BufferedWriter(new OutputStreamWriter(out,
	// "GB2312"), 1024);
}
