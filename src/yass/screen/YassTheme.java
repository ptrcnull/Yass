package yass.screen;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.player.advanced.AdvancedPlayer;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    9. Juli 2009
 */
public class YassTheme {
	private static Properties prop = null;
	private static Color colors[] = null;
	private static Color pcolors[] = null;
	private static Color rcolors[] = null;

	private Hashtable<?, ?> mp3s = new Hashtable<Object, Object>();
	private Hashtable<String, Object> buffers = new Hashtable<String, Object>();
	private Hashtable<String, AudioFormat> formats = new Hashtable<String, AudioFormat>();
	private Hashtable<String, Thread> threads = new Hashtable<String, Thread>();
	private AdvancedPlayer advancedPlayer = null;

	private int BUFFER_SIZE = 16000;

	private static String userdir = System.getProperty("user.home") + File.separator + ".yass" + File.separator + "themes";

	private static String theme = "default";

	// http://unicode.org/charts/PDF/U2700.pdf
	private static String playerSymbols[] = new String[]{"\u2780", "\u278A", "\u2781", "\u278B", "\u2782", "\u278C"};


	/**
	 *  Constructor for the YassAudioSample object
	 */
	public YassTheme() { }


	/**
	 *  Sets the theme attribute of the YassTheme object
	 *
	 * @param  t  The new theme value
	 */
	public static void setTheme(String t) {
		theme = t;

		loadProperties();
	}


	/**
	 *  Description of the Method
	 */
	public static void loadProperties() {
		prop = new Properties();
		InputStream is = null;
		try {
			URL u = getResource("config.xml");
			is = u.openStream();
			prop.loadFromXML(is);
		}
		catch (Exception e) {
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception e) {}
			}
		}
		Vector<Color> cols = new Vector<Color>();
		int i = 0;
		while (true) {
			String c = prop.getProperty("color-screen-" + i++);
			if (c == null) {
				break;
			}
			String alpha = "#FF";
			String rgb = c;
			boolean hasAlpha = c.length() > 7;
			if (hasAlpha) {
				rgb = c.substring(0, 7);
				alpha = "#" + c.substring(7);
			}
			int rgba = Integer.decode(rgb).intValue();
			int a = Integer.decode(alpha).intValue();
			rgba = rgba | (a << 24);
			cols.addElement(new Color(rgba, true));
		}
		colors = (Color[]) cols.toArray(new Color[]{});

		i = 1;
		cols.clear();
		while (true) {
			String c = prop.getProperty("color-player-" + i++);
			if (c == null) {
				break;
			}
			String alpha = "#FF";
			String rgb = c;
			boolean hasAlpha = c.length() > 7;
			if (hasAlpha) {
				rgb = c.substring(0, 7);
				alpha = "#" + c.substring(7);
			}
			int rgba = Integer.decode(rgb).intValue();
			int a = Integer.decode(alpha).intValue();
			rgba = rgba | (a << 24);
			cols.addElement(new Color(rgba, true));
		}
		pcolors = (Color[]) cols.toArray(new Color[]{});

		i = 0;
		cols.clear();
		while (true) {
			String c = prop.getProperty("color-renderer-" + i++);
			if (c == null) {
				break;
			}
			String alpha = "#FF";
			String rgb = c;
			boolean hasAlpha = c.length() > 7;
			if (hasAlpha) {
				rgb = c.substring(0, 7);
				alpha = "#" + c.substring(7);
			}
			int rgba = Integer.decode(rgb).intValue();
			int a = Integer.decode(alpha).intValue();
			rgba = rgba | (a << 24);
			cols.addElement(new Color(rgba, true));
		}
		rcolors = (Color[]) cols.toArray(new Color[]{});
	}


	/**
	 *  Gets the color attribute of the YassTheme class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The color value
	 */
	public Color getColor(int i) {
		return colors[i];
	}


	/**
	 *  Gets the playerColor attribute of the YassTheme class
	 *
	 * @return    The playerColor value
	 */
	public static Color[] getPlayerColors() {
		return pcolors;
	}


	/**
	 *  Gets the playerColors attribute of the YassTheme class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The playerColors value
	 */
	public static Color getPlayerColor(int i) {
		return pcolors[i];
	}


	/**
	 *  Gets the rendererColors attribute of the YassTheme class
	 *
	 * @return    The rendererColors value
	 */
	public static Color[] getRendererColors() {
		return rcolors;
	}


	/**
	 *  Gets the rendererColor attribute of the YassTheme class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The rendererColor value
	 */
	public static Color getRendererColor(int i) {
		return rcolors[i];
	}


	/**
	 *  Gets the theme attribute of the YassTheme class
	 *
	 * @return    The theme value
	 */
	public static String getTheme() {
		return theme;
	}


	/**
	 *  Gets the playerSymbol attribute of the YassTheme object
	 *
	 * @param  t       Description of the Parameter
	 * @param  active  Description of the Parameter
	 * @return         The playerSymbol value
	 */
	public String getPlayerSymbol(int t, boolean active) {
		int i = t * 2;
		if (active) {
			i++;
		}
		return playerSymbols[i];
	}


	/**
	 *  Description of the Method
	 *
	 * @param  sampleName  Description of the Parameter
	 */
	public void loadSample(String sampleName) {
		if (buffers.get(sampleName) != null) {
			return;
		}

		URL url = null;
		if (sampleName.startsWith("file:")) {
			String name = sampleName.substring(5);
			File f = new File(name);
			try {
				url = f.toURL();
			}
			catch (Exception e) {
			}
		} else {
			url = getResource(sampleName);
		}

		if (url == null) {
			return;
		}

		if (sampleName.endsWith(".mp3")) {
			loadMP3(sampleName, url);
			return;
		}

		AudioInputStream stream = null;
		try {
			stream = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
			AudioFormat format = stream.getFormat();
			long len = stream.getFrameLength() * format.getFrameSize();
			if (len <= 0) {
				return;
			}

			Vector<Byte> buffer = new Vector<Byte>((int) len);
			int bytesRead = 0;
			byte[] bufferData = new byte[BUFFER_SIZE];
			while (bytesRead != -1) {
				try {
					bytesRead = stream.read(bufferData, 0, bufferData.length - 1);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				if (bytesRead >= 0) {
					for (int i = 1; i <= bytesRead; i++) {
						buffer.add(new Byte(bufferData[i]));
					}
				}
			}
			buffers.put(sampleName, buffer);
			formats.put(sampleName, format);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				stream.close();
			}
			catch (Exception e) {}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  sampleName  Description of the Parameter
	 */
	public void unloadSample(String sampleName) {
		interruptSample(sampleName);
		buffers.remove(sampleName);
		formats.remove(sampleName);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  sampleName  Description of the Parameter
	 */
	public void interruptSample(String sampleName) {
		PlayThread t = (PlayThread) threads.get(sampleName);
		if (t != null) {
			//System.out.println("closing " + sampleName + "");
			t.interrupt = true;
			if (sampleName.endsWith(".mp3") && t.player != null) {
				t.player.close();
			}
			int n = 100;
			while (!t.finished && n > 0) {
				try {
					Thread.currentThread();
					Thread.sleep(10);
				}
				catch (Exception e) {
				}
				//System.out.println("waiting to close thread (" + n + ")");
			}
		}
	}


	/**
	 *  Description of the Method
	 */
	public void interruptAll() {
		for (Enumeration<String> en = threads.keys(); en.hasMoreElements(); ) {
			interruptSample((String) en.nextElement());
		}
	}


	/**
	 *  Description of the Method
	 */
	public void unloadAll() {
		for (Enumeration<String> en = threads.keys(); en.hasMoreElements(); ) {
			unloadSample((String) en.nextElement());
		}
	}


	/**
	 *  Gets the playing attribute of the YassTheme object
	 *
	 * @param  sampleName  Description of the Parameter
	 * @return             The playing value
	 */
	public boolean isPlaying(String sampleName) {
		PlayThread t = (PlayThread) threads.get(sampleName);
		if (t != null) {
			return !t.interrupt;
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  sampleName  Description of the Parameter
	 * @param  loop        Description of the Parameter
	 */
	public void playSample(String sampleName, boolean loop) {
		interruptSample(sampleName);
		Thread t = new PlayThread(sampleName, loop);
		threads.put(sampleName, t);
		t.start();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  sampleName   Description of the Parameter
	 * @param  loop         Description of the Parameter
	 * @param  startMillis  Description of the Parameter
	 */
	public void playSample(String sampleName, boolean loop, int startMillis) {
		interruptSample(sampleName);
		Thread t = new PlayThread(sampleName, loop, startMillis);
		threads.put(sampleName, t);
		t.start();
	}


	class PlayThread extends Thread {
		public boolean interrupt = false;
		public boolean finished = false;
		private boolean loop = false;
		private String sampleName = null;
		public AdvancedPlayer player = null;
		private int startMillis = 0;
		private float fps = 0;

		private byte[] tmpBuff = null;


		/**
		 *  Constructor for the PlayThread object
		 *
		 * @param  sampleName  Description of the Parameter
		 * @param  loop        Description of the Parameter
		 */
		public PlayThread(String sampleName, boolean loop) {
			this.sampleName = sampleName;
			this.loop = loop;
		}


		/**
		 *  Constructor for the PlayThread object
		 *
		 * @param  sampleName   Description of the Parameter
		 * @param  loop         Description of the Parameter
		 * @param  startMillis  Description of the Parameter
		 */
		public PlayThread(String sampleName, boolean loop, int startMillis) {
			this.sampleName = sampleName;
			this.loop = loop;
			this.startMillis = startMillis;

			ByteArrayInputStream bin = null;
			AudioInputStream in = null;
			try {
				byte[] buffer = (byte[]) buffers.get(sampleName);
				if (buffer != null) {
					bin = new ByteArrayInputStream(buffer);
					in = AudioSystem.getAudioInputStream(bin);
					if (in != null) {
						AudioFormat baseFormat = in.getFormat();
						fps = baseFormat.getFrameRate();
					}
				}
			}
			catch (Exception e) {
				fps = 0;
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (Exception e) {}
				}
				if (bin != null) {
					try {
						bin.close();
					}
					catch (Exception e) {}
				}
			}
		}


		public void run() {
			interrupt = false;
			finished = false;

			if (sampleName.endsWith(".mp3")) {
				boolean once = true;
				while (!interrupt && once) {

					byte[] buffer = (byte[]) buffers.get(sampleName);
					if (buffer == null) {
						interrupt = true;
						break;
					}
					ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
					try {
						if (startMillis == 0 || fps == 0) {
							player = new AdvancedPlayer(bin);
							player.play();
						} else {
							player = new AdvancedPlayer(bin);
							int skip = Math.max(0, (int) Math.floor(fps * (startMillis / 1000.0)));
							player.play(skip, Integer.MAX_VALUE);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						interrupt = true;
					}
					finally {
					}
					once = loop;
				}
				finished = true;
				threads.remove(sampleName);
				return;
			}
			AudioFormat format = (AudioFormat) formats.get(sampleName);
			Vector<?> buffer = (Vector<?>) buffers.get(sampleName);

			if (format == null || buffer == null) {
				finished = true;
				threads.remove(sampleName);
				return;
			}

			int noChannels = format.getChannels();
			int sampleSize = format.getSampleSizeInBits();
			int frameSize = format.getFrameSize();

			// open line
			SourceDataLine line = null;
			try {
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
				line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(format);
				line.start();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				finished = true;
			}

			double framePos = 1;
			int lengthInFrames = (int) (buffer.size() / frameSize) - 1;
			tmpBuff = new byte[BUFFER_SIZE];

			boolean once = true;
			while (!interrupt && once) {
				framePos = 1;
				while (!interrupt && Math.floor(framePos) <= lengthInFrames) {
					int bufferPos = 0;
					while (!interrupt && (bufferPos < BUFFER_SIZE) & (Math.floor(framePos) <= lengthInFrames)) {
						int noFrame = (int) Math.floor(framePos);
						int pos = (int) ((noFrame - 1) * frameSize);
						for (int i = 0; i < frameSize && !interrupt; i++) {
							Byte sample = (Byte) buffer.get(pos + i + 1);
							tmpBuff[bufferPos++] = sample.byteValue();
						}
						framePos++;
					}
					if (interrupt) {
						break;
					}
					if (bufferPos > 0) {
						int len = bufferPos;
						int i = 0;
						while (len > 0) {
							int available = line.available();
							while (available < 1 && !interrupt) {
								try {
									sleep(10);
								}
								catch (Exception e) {}
							}
							if (interrupt) {
								break;
							}
							int n = Math.min(available, len);
							n = ((int) (n / frameSize)) * frameSize;
							line.write(tmpBuff, i, n);
							i += n;
							len -= n;
						}
					}
				}
				once = loop;
			}
			try {
				line.flush();
				line.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				finished = true;
			}
			finished = true;
			threads.remove(sampleName);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  url         Description of the Parameter
	 * @param  sampleName  Description of the Parameter
	 */
	private void loadMP3(String sampleName, URL url) {
		byte[] buffer = null;
		InputStream is = null;
		BufferedInputStream bfi = null;
		ByteArrayOutputStream bout = null;
		try {
			is = url.openStream();
			bfi = new BufferedInputStream(is, 4096);
			bout = new ByteArrayOutputStream();
			int readP;
			byte[] bufferP = new byte[1024];
			while ((readP = bfi.read(bufferP)) > -1) {
				bout.write(bufferP, 0, readP);
			}
			buffer = bout.toByteArray();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (bout != null) {
				try {
					bout.close();
				}
				catch (Exception e) {}
			}
			if (bfi != null) {
				try {
					bfi.close();
				}
				catch (Exception e) {}
			}
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception e) {}
			}
		}
		buffers.put(sampleName, buffer);
	}


	/**
	 *  Gets the resource attribute of the YassTheme class
	 *
	 * @param  s  Description of the Parameter
	 * @return    The resource value
	 */
	public static URL getResource(String s) {
		if (new File(userdir).exists()) {
			File f = new File(userdir + File.separator + theme + File.separator + s);
			try {
				return f.toURL();
			}
			catch (Exception e) {
			}
		}

		String filename = "/themes/" + theme + "/" + s;
		URL url = YassTheme.class.getClass().getResource(filename);
		java.net.URLConnection uc = null;
		try {
			uc = url.openConnection();
			if (uc.getContentLength() > 0) {
				return url;
			}
		}
		catch (Exception e) {
		}
		finally {
			try {
				if (uc != null) {
					uc.getOutputStream().close();
					uc.getInputStream().close();
				}
			}
			catch (Exception e) {}
		}
		filename = "/themes/default/" + s;
		return YassTheme.class.getClass().getResource(filename);
	}


	/**
	 *  Gets the image attribute of the YassTheme object
	 *
	 * @param  s  Description of the Parameter
	 * @return    The image value
	 */
	public BufferedImage getImage(String s) {
		URL u = getResource(s);

		BufferedImage img = null;
		try {
			img = javax.imageio.ImageIO.read(u);
		}
		catch (Exception e) {
			img = null;
		}
		return img;
	}
}

