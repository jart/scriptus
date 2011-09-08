package net.ex337.scriptus.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMisc {

	private static final Pattern HASHTAG_REGEXP = Pattern.compile("#([A-Za-z0-9]*)");
	
	public static void main(String[] args) {
		for(String s : new String[]{
				"@robotoscriptu #HrSw 6 //are you gonna go my way?",
				"#HrSw @test #foo",
				"#ffoo-bar"
		}){
			System.out.println(s);
			Matcher m = HASHTAG_REGEXP.matcher(s);
			while(m.find()){
				System.out.println("\t"+m.group(1));
			}
		}
	}

}
