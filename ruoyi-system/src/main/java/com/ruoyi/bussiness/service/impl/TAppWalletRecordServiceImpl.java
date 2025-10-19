package com.ruoyi.bussiness.service.impl;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.util.*;

import com.ruoyi.bussiness.domain.TAppUser;
import com.ruoyi.bussiness.domain.TAppWalletRecord;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.domain.setting.VipLevelSetting;
import com.ruoyi.bussiness.domain.vo.AgencyAppUserDataVo;
import com.ruoyi.bussiness.domain.vo.AgencyDataVo;
import com.ruoyi.bussiness.domain.vo.DailyDataVO;
import com.ruoyi.bussiness.domain.vo.UserDataVO;
import com.ruoyi.bussiness.mapper.TAppUserMapper;
import com.ruoyi.bussiness.mapper.TAppWalletRecordMapper;
import com.ruoyi.bussiness.service.ITAppWalletRecordService;
import com.ruoyi.bussiness.service.ITCollectionOrderService;
import com.ruoyi.bussiness.service.SettingService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.CachePrefix;
import com.ruoyi.common.enums.RecordEnum;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.util.BotMessageBuildUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 用户信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2023-07-04
 */
@Service
@Slf4j
public class TAppWalletRecordServiceImpl extends ServiceImpl<TAppWalletRecordMapper, TAppWalletRecord> implements ITAppWalletRecordService
{
    @Resource
    private TAppWalletRecordMapper tAppWalletRecordMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ITCollectionOrderService collectionOrderService;

    @Resource
    private TAppUserMapper tAppUserMapper;

    @Resource
    private SettingService settingService;

    /**
     * 查询用户信息
     * 
     * @param id 用户信息主键
     * @return 用户信息
     */
    @Override
    public TAppWalletRecord selectTAppWalletRecordById(Long id)
    {
        return tAppWalletRecordMapper.selectTAppWalletRecordById(id);
    }

    /**
     * 查询用户信息列表
     * 
     * @param tAppWalletRecord 用户信息
     * @return 用户信息
     */
    @Override
    public List<TAppWalletRecord> selectTAppWalletRecordList(TAppWalletRecord tAppWalletRecord)
    {
        return tAppWalletRecordMapper.selectTAppWalletRecordList(tAppWalletRecord);
    }

    /**
     * 新增用户信息
     * 
     * @param tAppWalletRecord 用户信息
     * @return 结果
     */
    @Override
    public int insertTAppWalletRecord(TAppWalletRecord tAppWalletRecord)
    {
        tAppWalletRecord.setCreateTime(DateUtils.getNowDate());
        return tAppWalletRecordMapper.insertTAppWalletRecord(tAppWalletRecord);
    }

    /**
     * 修改用户信息
     * 
     * @param tAppWalletRecord 用户信息
     * @return 结果
     */
    @Override
    public int updateTAppWalletRecord(TAppWalletRecord tAppWalletRecord)
    {
        tAppWalletRecord.setUpdateTime(DateUtils.getNowDate());
        return tAppWalletRecordMapper.updateTAppWalletRecord(tAppWalletRecord);
    }

    /**
     * 批量删除用户信息
     * 
     * @param ids 需要删除的用户信息主键
     * @return 结果
     */
    @Override
    public int deleteTAppWalletRecordByIds(Long[] ids)
    {
        return tAppWalletRecordMapper.deleteTAppWalletRecordByIds(ids);
    }

    /**
     * 删除用户信息信息
     * 
     * @param id 用户信息主键
     * @return 结果
     */
    @Override
    public int deleteTAppWalletRecordById(Long id)
    {
        return tAppWalletRecordMapper.deleteTAppWalletRecordById(id);
    }

    @Override
    public void generateRecord(Long userId, BigDecimal amount, int type, String createBy, String serialId, String remark, BigDecimal before, BigDecimal after, String coin, String adminParentIds) {
        TAppWalletRecord record = new TAppWalletRecord();
        BigDecimal price = amount;
        record.setSerialId(serialId);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setCreateTime(new Date());
        record.setType(type);
        record.setRemark(remark);
        record.setCreateBy(createBy);
        record.setBeforeAmount(before);
        record.setAfterAmount(after);
        record.setSymbol(coin);
        record.setAdminParentIds(adminParentIds);
        record.setOperateTime(new Date());
        try {
            if (!coin.equals("usdt")){
                log.debug("帐变记录币种获取汇率：  币种：{}",coin);
                price = redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + coin.toLowerCase());
                log.debug("帐变记录获取汇率：  币种：{}  汇率：{}",coin,price);
                record.setUAmount(amount.multiply(price));
            }else{
                record.setUAmount(amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        insertTAppWalletRecord(record);
    }

    @Override
    public List<UserDataVO> selectUserDataList(TAppWalletRecord tAppWalletRecord) {
        List<UserDataVO> userDataVOS = tAppWalletRecordMapper.selectUserDataList(tAppWalletRecord);
        for (UserDataVO userDataVO : userDataVOS) {
            BigDecimal decimal = collectionOrderService.selectCollectionAmountByUserId(userDataVO.getUserId());
            userDataVO.setTotalCollectAmount(null == decimal?BigDecimal.ZERO:decimal);
            BigDecimal allWithdraw = tAppWalletRecordMapper.getAllWithdrawOfUser(userDataVO.getUserId());
            userDataVO.setTotalWithdrawAmount(allWithdraw);
        }
        return userDataVOS;
    }

    /**
     * 代理数据统计
     *
     * @param appWalletRecord
     * @return
     */
    @Override
    public List<AgencyDataVo> selectAgencyList(TAppWalletRecord appWalletRecord) {
        List<AgencyDataVo> agencyDataVos = tAppWalletRecordMapper.selectAgencyList(appWalletRecord);
        for (AgencyDataVo agencyDataVo : agencyDataVos) {
            BigDecimal decimal = collectionOrderService.selectCollectionAmountByAgencyId(agencyDataVo.getAgencyId());
            agencyDataVo.setCollectionAmount(null == decimal?BigDecimal.ZERO:decimal);
        }
        return agencyDataVos;
    }

    @Override
        public List<DailyDataVO> dailyData(TAppWalletRecord appWalletRecord) {
        List<DailyDataVO> dailyDataVOS = tAppWalletRecordMapper.dailyData(appWalletRecord);
        dailyDataVOS.removeAll(Collections.singleton(null));
        for (DailyDataVO dailyDataVO : dailyDataVOS) {
            if(null != appWalletRecord.getParams() && null != appWalletRecord.getParams().get("beginTime")){
                String beginTime = (String) appWalletRecord.getParams().get("beginTime");
                String endTime = (String) appWalletRecord.getParams().get("endTime");
                BigDecimal dayCollectionAmount = collectionOrderService.getDayCollectionAmount(beginTime,endTime);
                dailyDataVO.setTotalCollectAmount(dayCollectionAmount==null?BigDecimal.ZERO:dayCollectionAmount);
            }
        }
        return dailyDataVOS;
    }

    @Override
    public List<AgencyAppUserDataVo> selectAgencyAppUserList(TAppWalletRecord appWalletRecord) {
        List<AgencyAppUserDataVo> agencyAppUserDataVos = tAppWalletRecordMapper.selectAgencyAppUserList(appWalletRecord);
        for (AgencyAppUserDataVo agencyAppUserDataVo : agencyAppUserDataVos) {
            BigDecimal decimal = collectionOrderService.selectCollectionAmountDetail(agencyAppUserDataVo.getAppUserId(), appWalletRecord.getAdminParentIds());
            agencyAppUserDataVo.setCollectionAmount(null ==decimal?BigDecimal.ZERO:decimal);
        }
        return agencyAppUserDataVos;
    }

    @Override
    public  Map<String, BigDecimal> statisticsAmount() {
        Map<String, BigDecimal> map = new HashMap<>();
        BigDecimal statisticsAmount =  tAppWalletRecordMapper.statisticsAmount();
        map.put("statisticsAmount",statisticsAmount);
        return map;
    }

    @Override
    public void sjLevel(Long userId,BigDecimal czje) {
        for (int i = 0; i < 8; i++) {
            TAppUser tAppUser = tAppUserMapper.selectTAppUserByUserId(userId);

            BigDecimal amountTotal = new BigDecimal(0);
            BigDecimal bigDecimal = tAppWalletRecordMapper.selectByUserIdSumAmount(userId);
            if (bigDecimal != null){
                amountTotal = amountTotal.add(bigDecimal);
            }else{
                amountTotal = amountTotal.add(czje);
            }

            Setting vip_level_setting = settingService.get("VIP_LEVEL_SETTING");


            VipLevelSetting vipLevelSetting = JSONUtil.toBean(vip_level_setting.getSettingValue(), VipLevelSetting.class);
            if(!vipLevelSetting.getIsOpen()){
                return;
            }
            String vip0End = vipLevelSetting.getVip0End();
            String vip1End = vipLevelSetting.getVip1End();
            String vip2End = vipLevelSetting.getVip2End();
            String vip3End = vipLevelSetting.getVip3End();
            String vip4End = vipLevelSetting.getVip4End();
            String vip5End = vipLevelSetting.getVip5End();
            String vip6End = vipLevelSetting.getVip6End();
            String vip7End = vipLevelSetting.getVip7End();
            switch (tAppUser.getLevel()){
                case 0:
                    if(amountTotal.compareTo(new BigDecimal(vip0End)) > 0){
                        tAppUser.setLevel(1);
                    }
                    break;
                case 1:
                    if(amountTotal.compareTo(new BigDecimal(vip1End)) > 0){
                        tAppUser.setLevel(2);
                    }
                    break;
                case 2:
                    if(amountTotal.compareTo(new BigDecimal(vip2End)) > 0){
                        tAppUser.setLevel(3);
                    }
                    break;
                case 3:
                    if(amountTotal.compareTo(new BigDecimal(vip3End)) > 0){
                        tAppUser.setLevel(4);
                    }
                    break;
                case 4:
                    if(amountTotal.compareTo(new BigDecimal(vip4End)) > 0){
                        tAppUser.setLevel(5);
                    }
                    break;
                case 5:
                    if(amountTotal.compareTo(new BigDecimal(vip5End)) > 0){
                        tAppUser.setLevel(6);
                    }
                    break;
                case 6:
                    if(amountTotal.compareTo(new BigDecimal(vip6End)) > 0){
                        tAppUser.setLevel(7);
                    }
                    break;
                case 7:
                    if(amountTotal.compareTo(new BigDecimal(vip7End)) > 0){
                        tAppUser.setLevel(8);
                    }
                    break;
            }
            tAppUserMapper.updateTAppUser(tAppUser);
        }


    }


}
