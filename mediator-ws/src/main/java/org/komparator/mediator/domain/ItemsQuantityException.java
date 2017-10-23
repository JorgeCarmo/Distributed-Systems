package org.komparator.mediator.domain;

/** Exception used to signal a problem with the items quantity. */
public class ItemsQuantityException extends Exception {

	private static final long serialVersionUID = 1L;

	public ItemsQuantityException() {
	}

	public ItemsQuantityException(String message) {
		super(message);
	}

}
