package com.ckt.yzf.bluetoothchat.UI;
public interface IDrawerPresenter {
	IDrawerPresenter getInstance();
	void dispatchEvent(int totalPages, int currentPage);
}