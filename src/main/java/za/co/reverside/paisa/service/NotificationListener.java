package za.co.reverside.paisa.service;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.reverside.paisa.domain.Notification;
import za.co.reverside.paisa.repository.MailRepository;

@Component
public class NotificationListener {
	
	@Autowired
	private MailRepository mailRepository;
	
	@Transactional
	@JmsListener(destination="q.notification")
	public void receive(Notification notification) throws AddressException, MessagingException{
		mailRepository.send(notification);
	}

}
