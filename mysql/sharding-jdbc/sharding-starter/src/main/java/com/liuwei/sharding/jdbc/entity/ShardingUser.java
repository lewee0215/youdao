package com.liuwei.sharding.jdbc.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2021-01-29
 */
@TableName("sharding_user_0")
public class ShardingUser implements Serializable {

    private static final long serialVersionUID=1L;

    private String shardingKey;

    private Integer clientSn;

    private String name;

    private String headIcon;


    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    public Integer getClientSn() {
        return clientSn;
    }

    public void setClientSn(Integer clientSn) {
        this.clientSn = clientSn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }

    @Override
    public String toString() {
        return "ShardingUser{" +
        "shardingKey=" + shardingKey +
        ", clientSn=" + clientSn +
        ", name=" + name +
        ", headIcon=" + headIcon +
        "}";
    }
}
