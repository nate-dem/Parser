package com.ef.observer;

public interface Observable <T> {
	
	public void notifyObservers(T msg);
	
	public void addObserver(Observer<T> observer);
	
	public void removeObserver(Observer<T> observer);
	
}
