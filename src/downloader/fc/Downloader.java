package downloader.fc;

import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.net.MalformedURLException;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.concurrent.Task;
import javafx.beans.property.ReadOnlyDoubleProperty;

/**
 * Main FC class. Downloads the page.
 * 
 * @author BLANCH
 */
public class Downloader extends Task {
	public static final int CHUNK_SIZE = 1024;

	private ReentrantLock lock;
	private Condition pause;
	private boolean stopDL;

	URL url;
	int content_length;
	BufferedInputStream in;

	String filename;
	File temp;
	FileOutputStream out;

	ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", 0);

	int size = 0;
	int count = 0;
	private boolean dl;

	public Downloader(String uri) {
		stopDL = false;
		try {
			url = new URL(uri);

			URLConnection connection = url.openConnection();
			content_length = connection.getContentLength();

			in = new BufferedInputStream(connection.getInputStream());

			String[] path = url.getFile().split("/");
			filename = path[path.length - 1];
			temp = File.createTempFile(filename, ".part");
			out = new FileOutputStream(temp);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		lock = new ReentrantLock();
		pause = lock.newCondition();
		dl = true;
	}

	public String toString() {
		return url.toString();
	}

	protected String download() throws InterruptedException {
		byte buffer[] = new byte[CHUNK_SIZE];

		while (count >= 0 && !stopDL) {
			lock.lock();
			try {
				while (!dl)
					pause.await();
				try {
					out.write(buffer, 0, count);
				} catch (IOException e) {
					continue;
				}

				size += count;
				updateProgress(1. * size, content_length);
				Thread.sleep(1000);

				try {
					count = in.read(buffer, 0, CHUNK_SIZE);
				} catch (IOException e) {
					continue;
				}
				
			} finally {
				lock.unlock();
			}
			System.out.print(".");
		}

		if (size < content_length || stopDL) {
			temp.delete();
			System.out.println("Download Interrupted");
			throw new InterruptedException();
		}

		temp.renameTo(new File(filename));
		System.out.println("Download Complete");
		System.out.format("Into path : %s/%s\n", Paths.get(".").toAbsolutePath().normalize().toString(), filename);
		return filename;
	}

	public String getFilename() {
		return filename;
	}

	@Override
	protected Object call() throws Exception {
		try {
			download();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return filename;
	}

	public void play() {
		lock.lock();
		try {
			dl = true;
			pause.signal();
		} finally {
			lock.unlock();
		}
	}

	public void pause() {
		lock.lock();
		try {
			dl = false;
		} finally {
			lock.unlock();
		}
	}
	
	public void setStopDL(){
		stopDL = true;
	}
};
