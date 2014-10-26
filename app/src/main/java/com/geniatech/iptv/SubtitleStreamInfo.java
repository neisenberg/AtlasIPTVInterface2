package com.geniatech.iptv;

public class SubtitleStreamInfo {

	int pid;
	String lang;
	int type;
	int compositonPageId;
	int ancillaryPageId;

	public SubtitleStreamInfo(int subtPid, String subtLang, int subtType, int subtCompositonPageId, int subtAncillaryPageId) {
		pid = subtPid;
		lang = subtLang;
		type = subtType;
		compositonPageId = subtCompositonPageId;
		ancillaryPageId = subtAncillaryPageId;
	}

}
