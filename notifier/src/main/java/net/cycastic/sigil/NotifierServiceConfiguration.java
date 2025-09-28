package net.cycastic.sigil;

import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import net.cycastic.sigil.configuration.mail.MailSettings;
import net.cycastic.sigil.service.email.EmailSender;
import net.cycastic.sigil.service.impl.SQSNotifierService;
import net.cycastic.sigil.service.impl.email.EmailSenderImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NotifierServiceConfiguration {
	/*
	 * You need this main method (empty) or explicit <start-class>example.FunctionConfiguration</start-class>
	 * in the POM to ensure boot plug-in makes the correct entry
	 */
	public static void main(String[] args) {
		// empty unless using Custom runtime at which point it should include
		// SpringApplication.run(NotifierServiceConfiguration.class, args);
	}

	@Bean
	public EmailSender mailSender(MailSettings mailSettings){
		return new EmailSenderImpl(mailSettings);
	}

	@Bean
	public Function<SQSEvent, SQSBatchResponse> processSQSEvent(SQSNotifierService sqsNotifierService) {
		return sqsNotifierService::process;
	}
}
