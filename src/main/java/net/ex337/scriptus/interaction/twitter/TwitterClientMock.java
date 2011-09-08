package net.ex337.scriptus.interaction.twitter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.ex337.scriptus.exceptions.ScriptusRuntimeException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class TwitterClientMock implements TwitterClient {

	public TwitterClientMock() {
		//TODO load mocks
	}
	
	@Override
	public List<Tweet> getMentions() {

		List<Tweet> result = new ArrayList<Tweet>();
		
		URL u = this.getClass().getClassLoader().getResource("testTweets");

		if(u == null) {
			return result;
		}
		
		File testSources = new File(u.getFile());
		
		if( ! testSources.exists()) {
			return result;
		}

		for(File f : testSources.listFiles()) {
			try {
				String s = FileUtils.readFileToString(f);
				
				for(String line : StringUtils.split(s, "\n")) {
					String[] rows = StringUtils.split(line.trim(), "\t");
					
					result.add(new Tweet(Long.parseLong(rows[0]), rows[2], rows[1]));
					
				}
				
				
			} catch (IOException e) {
				throw new ScriptusRuntimeException(e);
			}
		}

		
		return result;
	}

	@Override
	public long tweet(String txt) {
		
		if(txt.length() > 140) {
			throw new ScriptusRuntimeException("tweet > 140 characters: "+txt);
		}
		
		return 0;
	}

	
	
}
