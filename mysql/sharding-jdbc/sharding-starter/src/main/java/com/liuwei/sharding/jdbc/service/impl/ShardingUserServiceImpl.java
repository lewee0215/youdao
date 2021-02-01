package com.liuwei.sharding.jdbc.service.impl;

import com.liuwei.sharding.jdbc.entity.ShardingUser;
import com.liuwei.sharding.jdbc.mapper.ShardingUserMapper;
import com.liuwei.sharding.jdbc.service.IShardingUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2021-01-29
 */
@Service
public class ShardingUserServiceImpl extends ServiceImpl<ShardingUserMapper, ShardingUser> implements IShardingUserService {

}
