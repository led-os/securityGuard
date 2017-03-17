package com.vidmt.acmn.utils.java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vidmt.acmn.utils.java.CommUtil.ITreeIterator;

public final class FileUtil {
	public static File creatFile(File baseDir, String... segments) {
		StringBuilder sb = new StringBuilder();
		for (String s : segments) {
			sb.append("/").append(s);
		}
		String sTmp = sb.toString();
		sTmp = sTmp.replace("//", "/");
		// sTmp.replace("\\\\", "/");
		// sTmp.replace("\\/", "/");
		// sTmp.replace("/\\", "/");

		return new File(baseDir, sTmp);
	}

	public static File creatFile(String baseDir, String... segments) {
		StringBuilder sb = new StringBuilder();
		for (String s : segments) {
			sb.append("/").append(s);
		}
		String sTmp = sb.toString();
		sTmp = sTmp.replace("//", "/");
		// sTmp.replace("\\\\", "/");
		// sTmp.replace("\\/", "/");
		// sTmp.replace("/\\", "/");

		return new File(baseDir, sTmp);
	}

	/** 把文件中的字符串读出来 **/
	public static String readFile(File f, String cs) {
		byte[] bytes = readFile(f);
		return CommUtil.newString(bytes, cs);
	}

	public static byte[] readFile(File f) {
		if (f.length() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("file is too large");
		}
		ByteArrayOutputStream baos = null;

		BufferedInputStream bis = null;
		try {
			baos = new ByteArrayOutputStream((int) f.length());

			bis = new BufferedInputStream(new FileInputStream(f));
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bis.read(buffer)) > 0) {
				baos.write(buffer, 0, len);
			}
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommUtil.close(baos, bis);
		}
		return null;
	}

	/**
	 * <p>
	 * 把文件从src复制到dest
	 * </p>
	 * 
	 * @param src
	 * @param dest
	 */
	public static void copy(String src, String dest) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(src));
			bos = new BufferedOutputStream(new FileOutputStream(dest));
			byte b[] = new byte[1024];
			while (bis.read(b) > 0) {
				bos.write(b);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CommUtil.close(bis);
			CommUtil.close(bos);
		}
	}

	/**
	 * 根据byte数组，生成文件
	 */
	public static void saveToFile(byte[] data, File file) {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			CommUtil.close(bos);
		}
	}

	public static void deleteFile(File f) {
		if (f.isFile()) {
			f.delete();
			return;
		}
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File subF : files) {
				deleteFile(subF);
			}
			f.delete();
		}

	}

	/**
	 * <p>
	 * 递归列出rootpath下边所有文件
	 * </p>
	 * <p>
	 * list all the files of the rootPath cursively according the filter
	 * </p>
	 * 
	 * @param path
	 *            : the root directory
	 * @param filter
	 *            : the filter to filter the files
	 * @return the array of the files of the resutl
	 * @throws IOException
	 */
	public static String[] listDirFiles(String rootPath, final FileFilter filter) throws IOException {
		final List<String> fileList = new ArrayList<String>();

		// a interface to iterate a tree
		ITreeIterator<File> it = new ITreeIterator<File>() {
			@Override
			public List<File> getChildren(File t) {
				if (t == null) {
					return null;
				}
				File[] fileArr = t.listFiles(filter);
				if (CommUtil.isEmpty(fileArr)) {
					return null;
				}
				return Arrays.asList(fileArr);
			}

			@Override
			public void visit(File t) {
				if (t == null) {
					return;
				}
				if (t.isFile()) {
					try {
						fileList.add(t.getCanonicalPath());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
		CommUtil.quickPreIterateTree(new File(rootPath), it);
		return fileList.toArray(new String[0]);
	}

	public static void visitDir(String rootPath, final IVisitFile vf) {
		// a interface to iterate a tree
		ITreeIterator<File> it = new ITreeIterator<File>() {
			@Override
			public List<File> getChildren(File f) {
				if (f == null) {
					return null;
				}
				File[] fileArr = f.listFiles();
				if (CommUtil.isEmpty(fileArr)) {
					return null;
				}
				return Arrays.asList(fileArr);
			}

			@Override
			public void visit(File f) {
				if (f == null) {
					return;
				}
				vf.visit(f);
			}
		};
		CommUtil.quickPreIterateTree(new File(rootPath), it);
	}

	public interface IVisitFile {
		public void visit(File f);
	}

}
