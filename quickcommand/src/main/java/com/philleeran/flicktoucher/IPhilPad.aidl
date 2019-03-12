package com.philleeran.flicktoucher;

interface IPhilPad{
	int test(in int a);
	void notifyReDrawGridView();
	void reShowHotSpotViews(String type, boolean isCheck);
	void notiHotspotsSetting(boolean isStart);
	void notiHotspotsVisible(boolean bVisible);
	void notifyReDrawGridViewBackground();
	void setPremium(boolean enable);
	void hotspotEnable(boolean enable);
	void setShowPadView(boolean enable);
}

