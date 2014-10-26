package com.geniatech.iptv;

public class AudioStreamInfo {

	public int pid;
	public String lang;
	public int format;
	public String formatName;

	public AudioStreamInfo(int audioPid, String audioLang, int audioFormat, String audioFormatName) {
		pid = audioPid;
		lang = audioLang;
		format = audioFormat;
		formatName = audioFormatName;
	}

}
