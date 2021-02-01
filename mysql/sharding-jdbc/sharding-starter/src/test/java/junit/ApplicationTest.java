package junit;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuwei.sharding.jdbc.ShardingApplication;
import com.liuwei.sharding.jdbc.entity.ShardingUser;
import com.liuwei.sharding.jdbc.service.IShardingUserService;

@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=ShardingApplication.class)// 指定spring-boot的启动类 
public class ApplicationTest {
	
	private static Logger logger = Logger.getLogger ( ApplicationTest.class.getName ()) ;
	
	@Autowired
	IShardingUserService iShardingUserService;
	
	@Test
	public void test() {
		LambdaQueryWrapper<ShardingUser> queryWrapper = new LambdaQueryWrapper<ShardingUser>();
		queryWrapper.eq(ShardingUser::getClientSn, 12346);
		ShardingUser reuslt = iShardingUserService.getOne(queryWrapper);
		System.out.println(JSON.toJSONString(reuslt));
	}

}