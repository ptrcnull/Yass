package yass.filter;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import yass.I18;
import yass.YassSong;
import yass.YassSongList;
import yass.YassUtils;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. M�rz 2008
 */
public class YassFilter implements Cloneable {
	/**
	 *  Description of the Field
	 */
	protected String rule = null;
	private String label = null;

	private static Hashtable<String, YassFilter> hash = null;
	private static Vector<YassFilter> plugins = null;

	/**
	 *  Description of the Field
	 */
	private static Properties prop = null;

	/**
	 *  Description of the Field
	 */
	public static boolean isInterrupted = false;


	/**
	 *  Constructor for the YassFilterPlugin object
	 */
	public YassFilter() { }


	/**
	 *  Description of the Method
	 */
	public static void init() {
		hash = new Hashtable<String, YassFilter>();
		plugins = new Vector<YassFilter>();
		StringTokenizer st = new StringTokenizer(prop.getProperty("filter-plugins"), "|");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			addPlugin(s);
		}
	}


	/**
	 *  Adds a feature to the Plugin attribute of the YassFilter class
	 *
	 * @param  filtername  The feature to be added to the Plugin attribute
	 * @return             Description of the Return Value
	 */
	private static YassFilter addPlugin(String filtername) {
		YassFilter f = null;
		try {
			Class<?> c = YassUtils.forName(filtername);
			f = (YassFilter) c.newInstance();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		hash.put(f.getID(), f);
		plugins.addElement(f);
		return f;
	}


	/**
	 *  Gets the pluginIDList attribute of the YassFilter class
	 *
	 * @return    The pluginIDList value
	 */
	public static String[] getAllIDs() {
		String[] s = new String[plugins.size()];
		int i = 0;
		for (Enumeration<YassFilter> en = plugins.elements(); en.hasMoreElements(); ) {
			YassFilter f = (YassFilter) en.nextElement();
			s[i++] = f.getID();
		}
		return s;
	}


	/**
	 *  Gets the groupsLabel attribute of the YassGroups class
	 *
	 * @return    The groupsLabel value
	 */
	public static String[] getAllLabels() {
		String[] s = new String[plugins.size()];
		int i = 0;
		for (Enumeration<YassFilter> en = plugins.elements(); en.hasMoreElements(); ) {
			YassFilter f = (YassFilter) en.nextElement();
			s[i++] = f.getLabel();
		}
		return s;
	}


	/**
	 *  Gets the iDAt attribute of the YassFilter class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The iDAt value
	 */
	public static String getIDAt(int i) {
		YassFilter f = (YassFilter) plugins.elementAt(i);
		return f.getID();
	}


	/**
	 *  Gets the iD attribute of the YassFilter object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "unspecified";
	}


	/**
	 *  Gets the label attribute of the YassFilter object
	 *
	 * @return    The label value
	 */
	public String getLabel() {
		if (label == null) {
			label = I18.get("group_" + getID() + "_title");
		}
		return label;
	}


	/**
	 *  Sets the label attribute of the YassFilter object
	 *
	 * @param  s  The new label value
	 */
	public void setLabel(String s) {
		label = s;
	}


	/**
	 *  Sets the properties attribute of the YassFilter class
	 *
	 * @param  p  The new properties value
	 */
	public static void setProperties(Properties p) {
		prop = p;
	}


	/**
	 *  Gets the properties attribute of the YassFilter class
	 *
	 * @return    The properties value
	 */
	public static Properties getProperties() {
		return prop;
	}


	/**
	 *  Gets the genericRules attribute of the YassFilter object
	 *
	 * @param  songs  Description of the Parameter
	 * @return        The genericRules value
	 */
	public String[] getGenericRules(Vector<YassSong> songs) {
		return null;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean confirm() {
		return false;
	}


	/**
	 *  Gets the confirmString attribute of the YassFilter object
	 *
	 * @param  rule  Description of the Parameter
	 * @return       The confirmString value
	 */
	public String getConfirmString(String rule) {
		return null;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  songs  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	public boolean start(Vector<?> songs) {
		return true;
	}


	/**
	 *  Description of the Method
	 */
	public void stop() { }


	/**
	 *  Gets the sorting attribute of the YassFilter object
	 *
	 * @return    The sorting value
	 */
	public int getSorting() {
		return YassSongList.TITLE_COLUMN;
	}


	/**
	 *  Gets the extraInfo attribute of the YassFilter object
	 *
	 * @return    The extraInfo value
	 */
	public int getExtraInfo() {
		return YassSongList.TITLE_COLUMN;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean count() {
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean allowDrop(String s) {
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean allowCoverDrop(String s) {
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @param  s     Description of the Parameter
	 */
	public void drop(String rule, YassSong s) {
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean refreshCounters() {
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean renderTitle() {
		if (rule.equals("all")) {
			return true;
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean showTitle() {
		if (rule.equals("all")) {
			return false;
		}
		if (rule.equals("unspecified")) {
			return false;
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  group  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	public static YassFilter createFilter(String group) {
		if (hash == null) {
			init();
		}
		try {
			YassFilter f = (YassFilter) hash.get(group);
			YassFilter f2 = (YassFilter) f.clone();
			return f2;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 *  Sets the rule attribute of the YassFilter object
	 *
	 * @param  s  The new rule value
	 */
	public void setRule(String s) {
		rule = s;
	}


	/**
	 *  Gets the rule attribute of the YassFilter object
	 *
	 * @return    The rule value
	 */
	public final String getRule() {
		return rule;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  f    Description of the Parameter
	 * @param  str  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	public static boolean containsIgnoreCase(String f, String str) {
		if (f == null) {
			return false;
		}
		f = f.toLowerCase();
		if (f.indexOf(str) >= 0) {
			return true;
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean accept(YassSong s) {
		String t = s.getDirectory() + File.separator + s.getFilename();
		boolean hit = containsIgnoreCase(t, rule) ||
			containsIgnoreCase(s.getTitle(), rule) ||
			containsIgnoreCase(s.getArtist(), rule) ||
			containsIgnoreCase(s.getLanguage(), rule) ||
			containsIgnoreCase(s.getEdition(), rule) ||
			containsIgnoreCase(s.getGenre(), rule) ||
			containsIgnoreCase(s.getVersion(), rule);
		return hit;
	}
}

