package com.ruoyi.bussiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import com.ruoyi.bussiness.domain.TNotice;

/**
 * 通知公告Service接口
 * 
 * @author ruoyi
 * @date 2023-07-20
 */
public interface ITNoticeService extends IService<TNotice>
{
    /**
     * 查询通知公告
     * 
     * @param noticeId 通知公告主键
     * @return 通知公告
     */
    public TNotice selectTNoticeByNoticeId(Long noticeId);

    /**
     * 查询通知公告列表
     * 
     * @param tNotice 通知公告
     * @return 通知公告集合
     */
    public List<TNotice> selectTNoticeList(TNotice tNotice);

    /**
     * 新增通知公告
     * 
     * @param tNotice 通知公告
     * @return 结果
     */
    public int insertTNotice(TNotice tNotice);

    /**
     * 修改通知公告
     * 
     * @param tNotice 通知公告
     * @return 结果
     */
    public int updateTNotice(TNotice tNotice);

    /**
     * 批量删除通知公告
     * 
     * @param noticeIds 需要删除的通知公告主键集合
     * @return 结果
     */
    public int deleteTNoticeByNoticeIds(Long[] noticeIds);

    /**
     * 删除通知公告信息
     * 
     * @param noticeId 通知公告主键
     * @return 结果
     */
    public int deleteTNoticeByNoticeId(Long noticeId);
}
