package com.ruoyi.bussiness.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.bussiness.domain.setting.DownloadSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.domain.setting.SmsSetting;
import com.ruoyi.bussiness.domain.setting.ThirdPaySetting;
import com.ruoyi.bussiness.mapper.SettingMapper;
import com.ruoyi.bussiness.service.SettingService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.SettingEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 配置业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 3:52 下午
 */
@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

    @Override
    public Setting get(String key) {
        return this.getById(key);
    }

    @Override
    public boolean saveUpdate(Setting setting) {
        return this.saveOrUpdate(setting);
    }

    public ThirdPaySetting getThirdPaySetting(String code) {
        Setting setting = this.get(SettingEnum.THIRD_CHANNL.name());
        if (!Objects.isNull(setting)) {
            List<ThirdPaySetting> list = JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), ThirdPaySetting.class);
            List<ThirdPaySetting> result = list.stream().filter(setting1 -> setting1.getCode().equals(code) && setting1.getStatus()==0).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(result)) {
                return result.get(0);
            }
        }
        return null;
    }
}