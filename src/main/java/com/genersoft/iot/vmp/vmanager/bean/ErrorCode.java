package com.genersoft.iot.vmp.vmanager.bean;

/**
 * 全局错误码
 */
public enum ErrorCode {
    SUCCESS(0, "成功"),
    ERROR100(100, "失败"),
    ERROR400(400, "参数不全或者错误"),
    ERROR404(404, "资源未找到"),
    ERROR403(403, "无权限操作"),
    ERROR401(401, "请登录后重新请求"),
    ERROR500(500, "系统异常"),
    ERROR600(600, "流媒体异常"),
    ERROR601(601, "webrtc推流未找到"),
    ERROR602(602, "对讲指令下发失败，请联系管理人员"),
    ERROR603(603, "设备正在对讲请稍后");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
