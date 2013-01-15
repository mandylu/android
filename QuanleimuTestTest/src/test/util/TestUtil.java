package test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TestUtil {
	public static void copy(File source, File dest) throws Exception {
		try {
			FileOutputStream os = new FileOutputStream(dest);
			FileInputStream in = new FileInputStream(source);
			byte[] all = new byte[in.available()];
			in.read(all);
			
			os.write(all);
			in.close();
			os.close();
		}
		catch (Throwable t) {
			throw new Exception("copy failed " + t.getMessage());
		}
	}
}
