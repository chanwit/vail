package org.vail.bootstrap.cli;

import java.lang.ProcessBuilder.Redirect;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

import org.jcoffeescript.Main;


public class Starter {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private Process p;
    private ProcessBuilder pb;
    private Thread monitor;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    public char PATH_SEPARATOR() {
        if(isWindows()) {
            return '\\';
        } else {
            return '/';
        }
    }

    public char JAR_SEPARATOR() {
        if(isWindows()) {
            return ';';
        } else {
            return ':';
        }
    }

    private String classpath(String path, String ...strings) {
        StringBuilder b = new StringBuilder();
        for(String s : strings) {
            if(s.endsWith(".jar")) {
                b.append(path + "\\lib\\jars\\" + s + ";");
            } else {
                b.append(path + "\\lib\\" + s +";");
            }
        }
        return b.toString();
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    Starter(Path workdir, String appPath, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;

        Path watchingPath = Paths.get(workdir.toString(), appPath);

        if (recursive) {
            System.out.format("Scanning %s ...\n", watchingPath);
            registerAll(watchingPath);
            System.out.println("Done.");
        } else {
            register(watchingPath);
        }

        // enable trace after initial registration
        this.trace = true;

        String VERTX_HOME= System.getenv("VERTX_HOME");
        String JAVA_HOME =System.getenv("JAVA_HOME");
        if(JAVA_HOME == null) {
            System.out.println("JAVA_HOME is not set. Execution aborted.");
            System.exit(0);
        }
        this.pb = new ProcessBuilder(JAVA_HOME + "\\bin\\java.exe",
                "-Djava.util.logging.config.file=" + VERTX_HOME + "\\conf\\logging.properties",
                "-Dvertx.install=" + VERTX_HOME,
                "-classpath",
                classpath(VERTX_HOME,
                        "javascript",
                        "ruby",
                        "groovy.jar",
                        "hazelcast.jar",
                        "jackson-core.jar",
                        "jackson-mapper.jar",
                        "js.jar",
                        "netty.jar",
                        "vert.x-core.jar",
                        "vert.x-platform.jar",
                        "vert.x-testframework.jar"),
                "org.vertx.java.deploy.impl.cli.VertxMgr",
                "run", "app.js");
        pb.redirectError(Redirect.INHERIT);
        pb.redirectOutput(Redirect.INHERIT);
        pb.directory(Paths.get(workdir.toString(), "target").toFile());
        this.p = pb.start();
        System.out.println(p);

        monitor = new Thread() {
            @Override
            public void run() {
                while(true) {
                    Integer exitCode = null;
                    try {
                        exitCode = p.exitValue();
                    } catch(IllegalThreadStateException e) {
                        exitCode = null;
                    }
                    if(exitCode != null) {
                        System.out.println("The process got killed. Restarting ");
                        try {
                            p = pb.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        monitor.setDaemon(true);
        monitor.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Exiting ...");
                p.destroy();
            }

        });
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {

        while(true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                Kind<?> kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = cast(event);
                Path name  = ev.context();
                Path child = dir.resolve(name);

                // print out event
                String eventName = event.kind().name();

                System.out.format("%s: %s\n", eventName, child);
                String filename = child.getFileName().toString();
                System.out.println(" >> " + filename.endsWith(".js"));
                System.out.println(" >> " + kind);
                if(filename.endsWith(".js") && kind == ENTRY_MODIFY) {
                    System.out.println("Destroying " + p);
                    p.destroy();
                    try {
                        p = pb.start();
                        System.out.println("Restarting " + p);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readable
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }

        }
    }


    public static void main(String[] args) throws IOException {
        String VAIL_HOME = System.getenv("VAIL_HOME");
        String WORK_DIR = System.getProperty("vail.work.dir");
        if(WORK_DIR == null) {
            WORK_DIR = System.getProperty("user.dir");
        }

        System.out.println(VAIL_HOME);
        System.out.println(WORK_DIR);

        compileAppJS(WORK_DIR);
        new Starter(Paths.get(WORK_DIR), "app", true).processEvents();
    }

	private static void compileAppJS(String workdir) {
		Path app    = Paths.get(workdir, "app",    "app.coffee");
		Path target = Paths.get(workdir, "target", "app.js");

		try {
			PrintStream fw = new java.io.PrintStream(target.toFile());
			FileInputStream fin = new FileInputStream(app.toFile());
			new Main().execute(new String[]{"--bare"}, fw, fin);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}