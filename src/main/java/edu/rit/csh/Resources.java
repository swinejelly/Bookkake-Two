package edu.rit.csh;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.rit.csh.auth.LDAPProxy;

public class Resources {
	public static SessionFactory sessionFactory;
	public static LDAPProxy ldapProxy;
	public static String googleBooksApiKey;
	public static ThreadPoolExecutor threadExecutor;
	
	static {
		sessionFactory = new Configuration().configure().buildSessionFactory();
		threadExecutor = new ThreadPoolExecutor(2, 8, 
				30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8));
		try {
			ldapProxy = new LDAPProxy("/ldap.properties");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try (InputStream stream = 
				"".getClass().getResourceAsStream("/googlebooks.properties")){ 
		Properties props = new Properties();
		props.load(stream);
		stream.close();
		googleBooksApiKey = props.getProperty("key");
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
}
