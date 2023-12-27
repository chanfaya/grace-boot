package com.grace.web.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grace.common.constant.UserConstants;
import com.grace.common.exception.CustomException;
import com.grace.web.system.domain.SysPost;
import com.grace.web.system.mapper.SysPostMapper;
import com.grace.web.system.mapper.SysUserPostMapper;
import com.grace.web.system.service.ISysPostService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * SysPostServiceImpl
 *
 * @author chanfa
 */
@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {

    @Resource
    private SysPostMapper postMapper;

    @Resource
    private SysUserPostMapper userPostMapper;

    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    @Override
    public List<Long> selectPostListByUserId(Long userId) {
        return postMapper.selectPostListByUserId(userId);
    }

    /**
     * 校验岗位名称是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public String checkPostNameUnique(SysPost post) {
        LambdaQueryWrapper<SysPost> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysPost::getPostName, post.getPostName());
        if (ObjectUtil.isNotNull(post.getPostId())) {
            wrapper.ne(SysPost::getPostId, post.getPostId());
        }
        if (postMapper.selectCount(wrapper) > 0) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public String checkPostCodeUnique(SysPost post) {
        LambdaQueryWrapper<SysPost> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysPost::getPostCode, post.getPostCode());
        if (ObjectUtil.isNotNull(post.getPostId())) {
            wrapper.ne(SysPost::getPostId, post.getPostId());
        }
        if (postMapper.selectCount(wrapper) > 0) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    public int countUserPostById(Long postId) {
        return userPostMapper.countUserPostById(postId);
    }

    /**
     * 批量删除岗位信息
     *
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    @Override
    public int deletePostByIds(Long[] postIds) {
        for (Long postId : postIds) {
            SysPost post = postMapper.selectById(postId);
            if (countUserPostById(postId) > 0) {
                throw new CustomException(String.format("%1$s已分配,不能删除", post.getPostName()));
            }
        }
        return postMapper.deleteBatchIds(Arrays.asList(postIds));
    }
}
