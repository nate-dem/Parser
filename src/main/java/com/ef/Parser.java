package com.ef;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ef.action.ParseAction;
import com.ef.config.AppConfig;

/*
 * Java parser that parses web server access log file and save to MySQL db.
 *
 * @author Natnael Demisse
 * @version 1.0
 * @since 2017-12-29
 */
public class Parser {

	public static void main(String[] args) {
		
	    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

	    ParseAction parseAction = ctx.getBean(ParseAction.class);
	    parseAction.execute(args);
		
		((AnnotationConfigApplicationContext)ctx).close();
	}
}
