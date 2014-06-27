package yass;

import java.awt.image.BufferedImage;

import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.Processor;
import javax.media.Time;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.swing.JComponent;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    21. August 2007
 */
public class YassVideo {
	YassProperties prop;
	Processor mediaPlayer = null;
	//Player mediaPlayer = null;
	FrameGrabbingControl fg = null;
	GainControl gain = null;
	long dur = 0;
	double duration = 0;
	long time = 0, vgap = 0;

	YassVideoRenderer renderer = null;
	JComponent videoComponent = null;


	/**
	 *  Constructor for the YassVideo object
	 *
	 * @param  p  Description of the Parameter
	 * @param  c  Description of the Parameter
	 */
	public YassVideo(YassProperties p, JComponent c) {
		prop = p;
		videoComponent = c;
	}


	/**
	 *  Description of the Method
	 */
	public void playVideo() {
		if (mediaPlayer != null) {
			videoComponent.setIgnoreRepaint(true);
			mediaPlayer.start();
		}
	}


	/**
	 *  Description of the Method
	 */
	public void stopVideo() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			setTime((int) (time / (1000 * 1000)));
			refreshPlayer();
		}
	}


	/**
	 *  Description of the Method
	 */
	public void pauseVideo() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  onoff  Description of the Parameter
	 */
	public void muteVideo(boolean onoff) {
		if (mediaPlayer != null && gain != null) {
			gain.setLevel(onoff ? 0 : .7f);
			// System.out.println("video mute " + onoff);
		}
	}


	/**
	 *  Sets the time attribute of the YassVideo object
	 *
	 * @param  ms  The new time value
	 */
	public void setTime(int ms) {
		long newtime = ms * 1000L * 1000L;
		if (time == newtime) {
			return;
		}

		time = newtime;
		refreshPlayer();
	}


	/**
	 *  Gets the time attribute of the YassVideo object
	 *
	 * @return    The time value
	 */
	public int getTime() {
		return (int) (time / (1000.0 * 1000.0));
	}


	/**
	 *  Description of the Method
	 */
	public void refreshPlayer() {
		if (mediaPlayer != null) {
			long tt = vgap + time;
			if (tt < 0) {
				tt = 0;
			}
			if (tt > dur) {
				tt = dur;
			}
			mediaPlayer.setMediaTime(new Time(tt));
		}
	}


	/**
	 *  Gets the frame attribute of the YassVideo object
	 *
	 * @return    The frame value
	 */
	public BufferedImage getFrame() {
		if (renderer == null) {
			return null;
		}
		return renderer.grabImage();
	}


	/**
	 *  Sets the videoGap attribute of the YassVideo object
	 *
	 * @param  ms  The new videoGap value
	 * @return     Description of the Return Value
	 */
	public boolean setVideoGap(double ms) {
		long newgap = (long) (ms * 1000L * 1000L);
		if (vgap == newgap) {
			return false;
		}

		vgap = newgap;
		refreshPlayer();
		return true;
	}


	private String vdFile = null;

	private boolean closed = true;


	/**
	 *  Description of the Method
	 *
	 * @param  vd  Description of the Parameter
	 */
	public void setVideo(String vd) {
		if (!YassVideoUtils.useFOBS) {
			return;
		}

		// storeAction.setEnabled(false);

		if (vd == null) {
			closeVideo();
			return;
		}

		vdFile = vd.replace('\\', '/');

		YassVideoUtils.initVideo();

		javax.media.Manager.setHint(javax.media.Manager.LIGHTWEIGHT_RENDERER, true);
		javax.media.Manager.setHint(javax.media.Manager.PLUGIN_PLAYER, true);

		if (!closed) {
			mediaPlayer.stop();
			mediaPlayer.close();
		}

		try {
			mediaPlayer = Manager.createProcessor(new javax.media.MediaLocator("file:" + vdFile));
		}
		catch (Exception e) {
			System.out.println("Cannot create video player");
			noVideo();
			return;
		}

		boolean result = waitForState(mediaPlayer, Processor.Configured);
		if (result == false) {
			System.out.println("Cannot configure video player");
			noVideo();
			return;
		}

		renderer = new YassVideoRenderer();
		mediaPlayer.setContentDescriptor(null);
		TrackControl[] tc = mediaPlayer.getTrackControls();
		if (tc != null) {
			TrackControl vtc = null;
			for (int i = 0; i < tc.length; i++) {
				if (tc[i].getFormat() instanceof VideoFormat) {
					vtc = tc[i];
					break;
				}
			}
			if (vtc != null) {
				try {
					vtc.setRenderer(renderer);
				}
				catch (Throwable e) {}
			}
		}

		result = waitForState(mediaPlayer, Processor.Realized);
		if (result == false) {
			System.out.println("Cannot realize video player");
			noVideo();
			return;
		}

		closed = false;

		fg = (FrameGrabbingControl) mediaPlayer.getControl("javax.media.control.FrameGrabbingControl");
		gain = (GainControl) mediaPlayer.getControl("javax.media.GainControl");

		dur = mediaPlayer.getDuration().getNanoseconds();
		if (dur < 0) {
			dur = 0;
		}
		duration = dur / (1000 * 1000 * 1000.0);

		if (videoComponent != null) {
			renderer.setComponent(videoComponent);
		}

		mediaPlayer.setMediaTime(new Time(vgap + time));
		// causes random crashes
		// if (mediaPlayer.getTargetState() < Player.Started) {
		//	mediaPlayer.prefetch();
		//}
		muteVideo(true);
	}


	/**
	 *  Description of the Method
	 */
	public void closeVideo() {
		if (!closed) {
			mediaPlayer.stop();
			mediaPlayer.close();
			closed = true;
		}

		noVideo();
		vdFile = null;
	}


	/**
	 *  Description of the Method
	 */
	public void noVideo() {
		closed = true;
	}



	/**
	 *  ************************************************************* Convenience
	 *  methods to handle processor's state changes.
	 *  **************************************************************
	 */
	private Integer stateLock = new Integer(0);
	private boolean failed = false;


	/**
	 *  Gets the stateLock attribute of the YassVideo object
	 *
	 * @return    The stateLock value
	 */
	Integer getStateLock() {
		return stateLock;
	}


	/**
	 *  Sets the failed attribute of the YassVideo object
	 */
	void setFailed() {
		failed = true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  p      Description of the Parameter
	 * @param  state  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	private synchronized boolean waitForState(Processor p, int state) {
		p.addControllerListener(new StateListener());
		failed = false;
		if (state == Processor.Configured) {
			p.configure();
		} else if (state == Processor.Realized) {
			p.realize();
		}
		while (p.getState() < state && !failed) {
			synchronized (getStateLock()) {
				try {
					getStateLock().wait();
				}
				catch (InterruptedException ie) {
					return false;
				}
			}
		}
		if (failed) {
			return false;
		} else {
			return true;
		}
	}


	/**
	 *  Description of the Class
	 *
	 * @author     Saruta
	 * @created    31. Juli 2008
	 */
	class StateListener implements ControllerListener {
		/**
		 *  Description of the Method
		 *
		 * @param  ce  Description of the Parameter
		 */
		public void controllerUpdate(ControllerEvent ce) {
			if (ce instanceof ControllerClosedEvent) {
				setFailed();
			}
			if (ce instanceof ControllerEvent) {
				synchronized (getStateLock()) {
					getStateLock().notifyAll();
				}
			}
		}
	}
}


