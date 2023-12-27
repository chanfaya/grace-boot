package com.grace.web.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户岗位关联表
 *
 * @author chanfa
 */
@Data
@TableName("sys_user_post")
public class SysUserPost implements Serializable {

    @Serial
    private static final long serialVersionUID = 2523216308376300542L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;
}
