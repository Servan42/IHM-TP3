package downloader.fc;

import java.net.URL;
import java.net.URLConnection;
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

public class Downloader extends Task {
	public static final int CHUNK_SIZE = 1024;

	private ReentrantLock lock;
	private Condition pause;

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

		while (count >= 0 /* || !dl */) {
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
			System.out.println();
		}

		if (size < content_length) {
			temp.delete();
			throw new InterruptedException();
		}

		temp.renameTo(new File(filename));
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
		// notifyAll();
		// try {
		// lock.tryLock();
		// dl = true;
		// System.out.println("PLOP");
		// } finally {
		// lock.unlock();
		// }
		lock.lock();
		try {
			dl = true;
			pause.signal();
		} finally {
			lock.unlock();
		}
	}

	public void pause() {
		// try {
		// wait();
		// } catch (InterruptedException e) {
		// System.out.println(e.toString());
		// }
		// try {
		// lock.lock();
		// dl = false;
		// System.out.println("PLIP");
		// } catch(Exception e) {
		// System.out.println("Hola ! " + e.toString());
		// } finally {
		// lock.unlock();
		// }
		lock.lock();
		try {
			dl = false;
		} finally {
			lock.unlock();
		}
	}
};
