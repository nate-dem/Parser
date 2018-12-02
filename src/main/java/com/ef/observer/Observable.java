package com.ef.observer;

public interface Observable {
	
	public void notifyObservers(String msg);
	
	public void addObserver(Observer observer);
	
	public void removeObserver(Observer observer);
	
}
