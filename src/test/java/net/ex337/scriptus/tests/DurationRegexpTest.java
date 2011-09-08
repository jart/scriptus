package net.ex337.scriptus.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ex337.scriptus.config.ScriptusConfig;

import junit.framework.TestCase;

public class DurationRegexpTest extends TestCase {
	
	public void testregexp() {
		
		
		Pattern p = Pattern.compile(ScriptusConfig.DURATION_FORMAT);
//		Pattern p = Pattern.compile("([0-9]+)");
		
		String[] tests = new String[] {
				"2w",
				"1y, 4s",
				"1M, 2d",
				"2h 3m 4s",
				"2   h   1345y",
				"1y 2M 3d 4h"
		};
		
		for(String test : tests) {
			
			Matcher m = p.matcher(test);
			
			assertTrue("found for "+test, m.find());
			
			m.reset();
			
			while(m.find()) {
				assertEquals("correct groups found", 2, m.groupCount());
				Integer.parseInt(m.group(1));
			}

		}

		String[] badTests = new String[] {
				"2",
				"y4",
				"76r"
		};

		for(String test : badTests) {
			
			Matcher m = p.matcher(test);
			
			assertFalse("no match on \""+test+"\"", m.find());
			

		}

	}
	
	public void testDateRollover() throws ParseException {
		
		String DATE_FORMAT="yyyy-MM-dd HH:mm";
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		
		Date d = sdf.parse("2011-12-31 12:00");
		
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		
		System.out.println(sdf.format(c.getTime()));
		
		c.add(Calendar.DATE, 1);

		System.out.println(sdf.format(c.getTime()));

	}

}
