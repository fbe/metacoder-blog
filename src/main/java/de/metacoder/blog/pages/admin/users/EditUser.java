package de.metacoder.blog.pages.admin.users;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.tynamo.security.services.SecurityService;

import de.metacoder.blog.persistence.entities.User;
import de.metacoder.blog.persistence.repositories.UserRepository;

public class EditUser {
	
	@Inject
	SecurityService securityService;

	@Inject
	UserRepository userRepository;

	String username;

	@Property
	User user;

	public void onActivate(final String username) {
		this.username = username;
		user = userRepository.findOne(username);
	}

	public Object onSuccess() {
		// TODO implement me
		return Index.class;
	}
	
}