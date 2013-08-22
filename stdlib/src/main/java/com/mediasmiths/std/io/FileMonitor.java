package com.mediasmiths.std.io;

import java.io.File;
import java.util.*;

/**
 * <p>
 * Title: File Monitor abstraction
 * </p>
 * 
 * <p>
 * Description: Listens for updates to Files (currently, not directories) and sends notifications
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * @version $Revision$
 */
public class FileMonitor {
	// Called by the timer. should tick() its Monitor
	private static class MonitorTask extends TimerTask {
		public final FileMonitor fmonitor;


		public MonitorTask(FileMonitor mon) {
			this.fmonitor = mon;
		}


		@Override
		public void run() {
			this.fmonitor.tick();
		}
	}

	// Represents a File being monitored by someone
	private static class FileMonitorRecord {
		private final File fileName; // The file being monitored

		private final IFileChangeListener listener; // The listener to callback on a change

		private long lastModified; // the last modified date


		public FileMonitorRecord(File f, IFileChangeListener l) {
			fileName = f;
			listener = l;

			// Set the initial lastModified time (so we don't immediately callback when the user adds a listener)
			lastModified = fileName.exists() ? fileName.lastModified() : -1;
		}


		public File getFile() {
			return fileName;
		}


		/**
		 * Update the last modified time if it's changed, calling the listener if it has
		 */
		public synchronized void tick() {
			long mod = fileName.exists() ? fileName.lastModified() : -1;

			if (mod != lastModified) {
				try {
					listener.fileChanged(fileName, lastModified, mod);
				}
				catch (Error e) {
					// ignore all errors from the listener
				}

				lastModified = mod;
			}
		}
	}

	// for the static methods
	private static FileMonitor monitor;

	private Timer timer = null;
	private long interval = 15 * 1000; // 15 second interval by default

	private List<FileMonitorRecord> monitored = new Vector<FileMonitorRecord>(10, 5);


	public synchronized void addMonitor(File f, IFileChangeListener listener) {
		monitored.add(new FileMonitorRecord(f, listener));

		// Potentially starts the timer
		monitorEvent(true);
	}


	public synchronized void delMonitor(File f) {
		FileMonitorRecord found = null;

		for (FileMonitorRecord rec : monitored) {
			if (rec.getFile().equals(f)) {
				found = rec;
				break;
			}
		}

		if (found != null) {
			monitored.remove(found);
		}

		// Potentially cancels the timer
		monitorEvent(false);
	}


	/**
	 * Updates each monitored file
	 */
	public synchronized void tick() {
		for (FileMonitorRecord rec : monitored) {
			rec.tick();
		}
	}


	private synchronized void monitorEvent(boolean added) {
		if (added && timer == null) { // We need to start the timer
			timer = new Timer("FileMonitor timer", true);
			timer.scheduleAtFixedRate(new MonitorTask(this), interval, interval);
		}
		else {
			if (monitored.size() == 0 && timer != null) {
				timer.cancel(); // Cancel and invalidate this timer

				timer = null; // free up the ref, since we can't use a timer once .cancel()ed
			}
		}
	}


	public synchronized static void add(File f, IFileChangeListener l) {
		if (monitor == null) {
			monitor = new FileMonitor();
		}

		monitor.addMonitor(f, l);
	}


	public static synchronized void del(File f) {
		if (monitor == null) {
			return;
		}

		monitor.delMonitor(f);
	}
}
