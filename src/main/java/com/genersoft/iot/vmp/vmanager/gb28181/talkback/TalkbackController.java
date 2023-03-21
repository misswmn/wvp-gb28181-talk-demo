package com.genersoft.iot.vmp.vmanager.gb28181.talkback;

import com.genersoft.iot.vmp.common.VideoManagerConstants;
import com.genersoft.iot.vmp.conf.exception.SsrcTransactionNotFoundException;
import com.genersoft.iot.vmp.conf.security.SecurityUtils;
import com.genersoft.iot.vmp.conf.security.dto.LoginUser;
import com.genersoft.iot.vmp.gb28181.bean.BroadcastItem;
import com.genersoft.iot.vmp.gb28181.bean.Device;
import com.genersoft.iot.vmp.gb28181.transmit.cmd.impl.SIPCommander;
import com.genersoft.iot.vmp.media.zlm.dto.MediaServerItem;
import com.genersoft.iot.vmp.media.zlm.dto.StreamAuthorityInfo;
import com.genersoft.iot.vmp.service.IDeviceService;
import com.genersoft.iot.vmp.service.IPlayService;
import com.genersoft.iot.vmp.service.IUserService;
import com.genersoft.iot.vmp.storager.IRedisCatchStorage;
import com.genersoft.iot.vmp.utils.Md5Utils;
import com.genersoft.iot.vmp.utils.Threads;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：
 * @date ：Created in 2023/3/16 10:10
 * @description：语音对讲controller
 * @modified By：
 * @version: ${1.0.0}
 */
@CrossOrigin
@RestController
@RequestMapping("/api/talkback")
public class TalkbackController {
    private final static Logger logger = LoggerFactory.getLogger(TalkbackController.class);

    @Autowired
    private SIPCommander cmder;
    @Autowired
    private IDeviceService deviceService;
    @Autowired
    private IRedisCatchStorage redisCatchStorage;
    @Autowired
    private IPlayService playService;
    @Autowired
    private IUserService userService;

    /**
     * 获取webrtc推流地址
     * @param deviceId
     * @param channelId
     * @return
     */
    @GetMapping("/getWebRtcAddr/{deviceId}/{channelId}")
    public WVPResult getWebRtcAddr(@PathVariable("deviceId") String deviceId, @PathVariable("channelId") String channelId) {

        //首先判断设备是否正在对讲
        if (redisCatchStorage.isBroadcastItem(deviceId)) {
            return WVPResult.fail(ErrorCode.ERROR603);
        }

        Device device = deviceService.getDevice(deviceId);
        MediaServerItem mediaServerItem = playService.getNewMediaServerItem(device);

        if (mediaServerItem == null) {
            logger.error("流媒体未找到");
            return WVPResult.fail(ErrorCode.ERROR600);
        }

        Map<String, Object> result = new HashMap<>(16);
        String app = "audio";
        String stream = deviceId + "_" + channelId;
        String type = "push";
        LoginUser userInfo = SecurityUtils.getUserInfo();
        String sign = Md5Utils.hash(userService.getUserByUsername(userInfo.getUsername()).getPushKey()); //获取推流鉴权密钥
        //示例 https://192.168.126.111:9443/index/api/webrtc?app=live&stream=test&type=play&sign=...
        String webRtcPushUrl = String.format("https://%s:%s/index/api/webrtc?app=%s&stream=%s&type=%s&sign=%s", mediaServerItem.getIp(), mediaServerItem.getHttpSSlPort(), app, stream, type,sign);
        result.put("app",app);
        result.put("stream",stream);
        result.put("type",type);
        result.put("sign",sign);
        result.put("webRtcPushUrl", webRtcPushUrl);
        logger.info("获取webrtc推流地址:{}",webRtcPushUrl);
        return WVPResult.success(result);
    }

    /**
     * 开始语音对讲
     * @param deviceId
     * @param channelId
     * @param app
     * @param stream
     * @return
     */
    @GetMapping("/start/{deviceId}/{channelId}/{app}/{stream}")
    public WVPResult start(@PathVariable("deviceId") String deviceId, @PathVariable("channelId") String channelId,
                           @PathVariable("app") String app, @PathVariable("stream") String stream) throws InterruptedException {
        BroadcastItem broadcastItem = new BroadcastItem();

        String lock = null;

        //1.首先确认该流是否推到流媒体
        StreamAuthorityInfo streamAuthorityInfo = redisCatchStorage.getStreamAuthorityInfo(app, stream);
        if (streamAuthorityInfo == null) {
            logger.error("webrtc推流未找到");
            return WVPResult.fail(ErrorCode.ERROR601);
        }
        broadcastItem.setApp(app);
        broadcastItem.setStream(stream);
        //2.确认推到流媒体，下发语音对讲指令协商对讲端口
        Device device = deviceService.getDevice(deviceId);
        try {
            redisCatchStorage.addBroadcastItem(deviceId,broadcastItem);
            cmder.audioBroadcastCmd(device);
         //3.等待设备返回结果
            lock = VideoManagerConstants.BROADCAST_LOCK + deviceId;
            boolean lockStatus = Threads.addLock(lock);
            if (lockStatus) {
                Object broadcastLock = Threads.getLock(lock);
                synchronized (broadcastLock) {
                    Threads.getLock(lock).wait(5000);

                    broadcastItem = redisCatchStorage.queryBroadcastItem(deviceId);

                    if (broadcastItem.getIpcAudioPort() == null || broadcastItem.getLocalPort() == null) {
                        throw new Exception();
                    }
                }
            }

        } catch (Exception e) {
            logger.error("语音对讲信令协商失败：{}:{}",app,stream);
            redisCatchStorage.deleteBroadcastItem(deviceId);
            return  WVPResult.fail(ErrorCode.ERROR602);
        } finally {
            Threads.removeLock(lock);
        }
        logger.info("开启语音对讲--->{}:{}",deviceId,channelId);
        return WVPResult.success();
    }

    /**
     * 停止语音对讲
     * @param deviceId
     * @param channelId
     * @return
     */
    @GetMapping("/stop/{deviceId}/{channelId}")
    public WVPResult stop(@PathVariable("deviceId") String deviceId, @PathVariable("channelId") String channelId) {
        logger.info("停止语音对讲--->{}:{}",deviceId,channelId);
        try {
            //告诉设备停止语音对讲，发送bye
            BroadcastItem broadcastItem = redisCatchStorage.queryBroadcastItem(deviceId);
            Device device = deviceService.getDevice(deviceId);
            cmder.audioByeCmd(device,broadcastItem,channelId);
            //改变语音对讲状态记录
            redisCatchStorage.deleteBroadcastItem(deviceId);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SsrcTransactionNotFoundException e) {
            e.printStackTrace();
        }

        return WVPResult.success();
    }

}
