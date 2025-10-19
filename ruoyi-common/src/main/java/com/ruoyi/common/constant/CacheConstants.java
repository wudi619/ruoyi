package com.ruoyi.common.constant;

/**
 * 缓存的key 常量
 * 
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 验证码失效时间
     */
    public static final int REGISTER_CODE_TIME = 5* 60;
    /**
     * 充值次数
     */
    public static final String WITHDRAW = "withdraw:";
    /**
     * 充值消息KEY
     */
    public static final String WITHDRAW_KEY = "withdraw_key";
    /**
     * 提现消息KEY
     */
    public static final String RECHARGE_KEY = "recharge_key";
    /**
     * 实名认证消息KEY
     */
    public static final String VERIFIED_KEY = "verified_key";

    /**
     * 充值消息KEY
     */
    public static final String WITHDRAW_KEY_BOT = "withdraw_key_bot";
    /**
     * 提现消息KEY
     */
    public static final String RECHARGE_KEY_BOT = "recharge_key_bot";
}
