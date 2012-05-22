package org.vailframework.coffeescript.walker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.vailframework.coffeescript.Main;

public class BatchCompiler {

	public void compile(final String srcDir, final String dstDir) {
		Path dir = Paths.get(srcDir);
		System.out.println("dir: " + dir);
		try {
		  Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String fullpath = file.toAbsolutePath().toString();
				String s = srcDir.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\:", "\\\\:");
				String localpath = fullpath.replaceFirst(s, "");
				if(localpath.startsWith(File.separator)) {
					localpath = localpath.substring(1);
				}
				if(localpath.endsWith(".coffee")) {
					if(localpath.equals("app.coffee")) {
						System.out.println("compile app.coffee directly to app.js");
						String target = (dstDir + java.io.File.separator + localpath).replaceAll("\\.coffee", ".js");
						FileInputStream fin = new FileInputStream(file.toFile());
						PrintStream fw = new java.io.PrintStream(new File(target));
						new Main().execute(new String[]{"--bare"}, fw, fin);
					} else {
						System.out.println("collecting " + localpath);
						// and compile all .coffee into out.js
					}
				}
				return FileVisitResult.CONTINUE;
			}

		  });
		} catch(Throwable e) {

		}
	}

	public static void main(String[] args) {
		new BatchCompiler().compile("c:\\projects\\vail\\app\\", "");
	}

}
