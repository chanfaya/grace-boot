package com.grace.web.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grace.web.system.domain.SysPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SysPostMapper
 *
 * @author chanfa
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {

    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    List<Long> selectPostListByUserId(Long userId);

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    List<SysPost> selectPostsByUserName(String userName);
}
