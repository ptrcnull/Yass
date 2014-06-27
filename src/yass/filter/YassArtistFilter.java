package yass.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JMenuItem;

import yass.YassSong;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. M�rz 2008
 */
public class YassArtistFilter extends YassFilter {
	Vector<String> artists = new Vector<String>();


	/**
	 *  Gets the iD attribute of the YassArtistFilter object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "artist";
	}


	/**
	 *  Gets the genericRules attribute of the YassArtistFilter object
	 *
	 * @param  data  Description of the Parameter
	 * @return       The genericRules value
	 */
	public String[] getGenericRules(Vector<YassSong> data) {
		int min = new Integer(getProperties().getProperty("group-min")).intValue();

		moveArticles = getProperties().get("use-articles").equals("true");
		if (moveArticles)
			loadArticles();
		
		artists.clear();
		Hashtable<String, Integer> count = new Hashtable<String, Integer>();
		for (Enumeration<?> e = data.elements(); e.hasMoreElements(); ) {
			YassSong s = (YassSong) e.nextElement();
			String artist = s.getArtist();
			if (artist == null || artist.length() < 1) {
				continue;
			}
			
			String language = s.getLanguage();
			if (moveArticles) {
				artist = getSortedArtist(artist, language);
			}
			
			if (!artists.contains(artist)) {
				Integer i = (Integer) count.get(artist);
				if (i == null) {
					i = new Integer(1);
				}
				count.put(artist, new Integer(i.intValue() + 1));
				if (i.intValue() >= min) {
					artists.addElement(artist);
				}
			}
		}
		Collections.sort(artists);
		
		return (String[]) artists.toArray(new String[]{});
	}

	private static boolean moveArticles = true;
	private static Hashtable<String, Vector<String>> lang_articles;
	private static String[] langArray, langID;
	
	/**
	 *  Description of the Method
	 */
	private void loadArticles() {
		Vector<String> langVector = new Vector<String>(); 
		Vector<String> langIDVector = new Vector<String>();
		
		StringTokenizer languages = new StringTokenizer(getProperties().getProperty("language-tag"), "|");
		while (languages.hasMoreTokens()) {
			String s = languages.nextToken();
			String sid = languages.nextToken();
			langVector.add(s);
			langIDVector.add(sid);
		}
		languages = new StringTokenizer(getProperties().getProperty("language-more-tag"), "|");
		while (languages.hasMoreTokens()) {
			String s = languages.nextToken();
			String sid = languages.nextToken();
			langVector.add(s);
			langIDVector.add(sid);
		}
		langArray = new String[langVector.size()];
		langID = new String[langVector.size()];
		langVector.toArray(langArray);
		langIDVector.toArray(langID);
		
		//---
		
		lang_articles = new Hashtable<String, Vector<String>>();
		String s = getProperties().getProperty("articles");
		StringTokenizer st = new StringTokenizer(s, ":");
		while (st.hasMoreTokens()) {
			String lang = st.nextToken();

			Vector<String> v = (Vector<String>) lang_articles.get(lang);
			if (v == null) {
				v = new Vector<String>();
				lang_articles.put(lang, v);
			} else {
				v.clear();
			}
			String articles = st.nextToken();
			StringTokenizer sta = new StringTokenizer(articles, "|");
			while (sta.hasMoreTokens()) {
				String article = sta.nextToken();
				article = article.toLowerCase();
				v.addElement(article);
			}
		}
	}
	
	public String getSortedArtist(String a, String lang) {
		if (langArray==null)
			loadArticles();
		
		if (lang == null) 
			return a;		

		for (int i = 0; i < langArray.length; i++) {
			if (langArray[i].equals(lang)) {
				lang = langID[i];
				break;
			}
		}

		Vector<?> v = (Vector<?>) lang_articles.get(lang);
		if (v == null) 
			return a;		

		String artist = a.toLowerCase();
		for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
			String article = (String) en.nextElement();
			if (artist.startsWith(article)) {
				int len = article.length();
				a = a.substring(len) + ", " + a.substring(0, len);
				return a;
			}
		}
		return a;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	public boolean allowCoverDrop(String rule) {
		if (rule.equals("all")) {
			return false;
		}
		if (rule.equals("others")) {
			return false;
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean accept(YassSong s) {
		String t = s.getArtist();
		boolean hit = false;

		if (moveArticles)
			t = getSortedArtist(t, s.getLanguage());
		
		if (rule.equals("all")) {
			hit = true;
		} else if (rule.equals("others")) {
			hit = artists.contains(t);
		} else {
			hit = t.equals(rule);
		}

		return hit;
	}
}

