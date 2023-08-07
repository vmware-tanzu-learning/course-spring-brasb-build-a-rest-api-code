package example.cashcard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
//import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
public class CashCardApplication {

	public static void main(String[] args) {

		MyApplicationListener myApplicationListener = new MyApplicationListener();
		SpringApplication springApplication = new SpringApplication(CashCardApplication.class);
		springApplication.addListeners(myApplicationListener);

		// Lesson 2.4 - Eliminate compile errors
		myApplicationListener.onApplicationEvent(new MyApplicationStartingEvent(springApplication, args));
//		myApplicationListener.onApplicationEvent(new MyApplicationStartingEvent(new DefaultBootstrapContext(), springApplication, args));

		springApplication.run(args);

	}

}

class MyApplicationListener implements ApplicationListener<MyApplicationStartingEvent> {

	private static final Logger log = LoggerFactory.getLogger(MyApplicationListener.class);

	@Override
	public void onApplicationEvent(MyApplicationStartingEvent event) {
		log.info("MyApplicationStartingEvent fired!");
	}

}

class MyApplicationStartingEvent extends ApplicationStartingEvent {

	private static final Logger log = LoggerFactory.getLogger(MyApplicationStartingEvent.class);

	/**
	 * This was deprecated in Spring Boot 2.4, removed in 2.6.
	 * This is an example to be used in Lesson 2.1
	 *
	 * @param application
	 * @param args
	 */
	@Deprecated
	public MyApplicationStartingEvent(SpringApplication application, String[] args) {

		// Lesson 2.4 - This signature has been removed
		super(application, args);

		log.warn("My parent is deprecated!");

	}

	public MyApplicationStartingEvent(ConfigurableBootstrapContext bootstrapContext, SpringApplication application, String[] args) {
		super(bootstrapContext, application, args);
		log.info("My parent is Ok!");
	}

}