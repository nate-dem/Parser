package com.ef.observer;

public interface Observer<T> {
	
	public void update(T msg);

}
