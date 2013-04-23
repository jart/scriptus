package net.ex337.scriptus.tests;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class TestMisc {

//	private static final Pattern HASHTAG_REGEXP = Pattern.compile("#([A-Za-z0-9]*)");
	
	public static void main(String[] args) throws MalformedURLException, IOException {
	    
	    String s = IOUtils.toString(new URL("https://my.inglease.be/csc/").openStream());

	    System.out.println(s);
	    
	    
//	    InetAddress a = InetAddress.getLocalHost();
//	    
//	    String hostName = null;
//	    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//	    {
//	        while (interfaces.hasMoreElements()) {
//	            NetworkInterface nic = interfaces.nextElement();
//	            Enumeration<InetAddress> addresses = nic.getInetAddresses();
//	            while (hostName == null && addresses.hasMoreElements()) {
//	                InetAddress address = addresses.nextElement();
////	                if (!address.isLoopbackAddress()) {
////	                    hostName = address.getHostName();
////	                }
//	                System.out.println(address.getHostName() + "/"+address.getHostAddress());
//	            }
//	        }
//	    }	   
//	    
//	    System.out.print(hostName);
	    
	    
//		{
//			HttpURLConnection c = (HttpURLConnection) new URL("http://www.google.com/robots.txt").openConnection();
//			c.setConnectTimeout(50000);
//			c.connect();
//			String content = IOUtils.toString(c.getInputStream(), c.getContentEncoding());
//
//			System.out.println(content.length());
//		}
//		
//		{
//			HttpURLConnection c = (HttpURLConnection) new URL("https://encrypted.google.com/robots.txt").openConnection();
//			c.setConnectTimeout(50000);
//			c.connect();
//			String content = IOUtils.toString(c.getInputStream(), c.getContentEncoding());
//
//			System.out.println(content.length());
//		}
//		
		
//		for(String s : new String[]{
//				"@robotoscriptu #HrSw 6 //are you gonna go my way?",
//				"#HrSw @test #foo",
//				"#ffoo-bar"
//		}){
//			System.out.println(s);
//			Matcher m = HASHTAG_REGEXP.matcher(s);
//			while(m.find()){
//				System.out.println("\t"+m.group(1));
//			}
//		}
	}

}
