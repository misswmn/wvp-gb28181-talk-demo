package com.genersoft.iot.vmp.gb28181.bean;

/**
 * @author
 * @date ：Created in 2022/8/24 15:17
 * @description：
 * @modified By：
 * @version: ${1.0.0}
 */
public class BroadcastItem {

    public BroadcastItem() {
    }

    public BroadcastItem(String deviceId, String localIp, Integer localPort) {
        this.deviceId = deviceId;
        this.localIp = localIp;
        this.localPort = localPort;
    }

    private String deviceId;
    private String channelId;
    private String localIp;
    private String ipcIp;
    private Integer localPort;
    private Integer ipcAudioPort;
    // 0是udp 1是tcp
    private Integer udpOrTcp;
    private String callId;
    private String ssrc;
    private String fromTag;
    private String toTag;
    private String viaBranch;
    private String app;
    private String stream;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getIpcIp() {
        return ipcIp;
    }

    public void setIpcIp(String ipcIp) {
        this.ipcIp = ipcIp;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public Integer getIpcAudioPort() {
        return ipcAudioPort;
    }

    public void setIpcAudioPort(Integer ipcAudioPort) {
        this.ipcAudioPort = ipcAudioPort;
    }

    public Integer getUdpOrTcp() {
        return udpOrTcp;
    }

    public void setUdpOrTcp(Integer udpOrTcp) {
        this.udpOrTcp = udpOrTcp;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getSsrc() {
        return ssrc;
    }

    public void setSsrc(String ssrc) {
        this.ssrc = ssrc;
    }

    public String getFromTag() {
        return fromTag;
    }

    public void setFromTag(String fromTag) {
        this.fromTag = fromTag;
    }

    public String getToTag() {
        return toTag;
    }

    public void setToTag(String toTag) {
        this.toTag = toTag;
    }

    public String getViaBranch() {
        return viaBranch;
    }

    public void setViaBranch(String viaBranch) {
        this.viaBranch = viaBranch;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }
}
