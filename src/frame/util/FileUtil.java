package frame.util;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.nutz.mvc.upload.TempFile;

public class FileUtil {

	private ServletContext context;

	public String upload(TempFile file) {
		String fileName = file.getMeta().getFileLocalName(); // 原文件名称
		String path = context.getRealPath("/") + "WEB-INF/lib/" + fileName;
		File f = new File(path);

		if (f.exists()) {
			org.nutz.lang.Files.deleteFile(f);
		}
		try {
			org.nutz.lang.Files.move(file.getFile(), f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}
}
