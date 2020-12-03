package com.yingda.lkj.service.impl.system;

import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.system.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2019/12/13
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    private BaseDao<User> userBaseDao;
    @Autowired
    public void setUserBaseDao(BaseDao<User> userBaseDao) {
        this.userBaseDao = userBaseDao;
    }

    @Override
    public void login(User user) {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        user.setLoginTime(current);
        user.setUpdateTime(current);
        userBaseDao.saveOrUpdate(user);
    }

    @Override
    public User getUserByUserName(String userName) {
        return userBaseDao.get("from User where userName = :userName", Map.of("userName", userName));
    }

    @Override
    public void updatePassword(String userId, String password) {
        userBaseDao.executeHql("update User set password = :password where id = :id", Map.of("password", password, "id", userId));
    }

    @Override
    public User getById(String id) {
        return userBaseDao.get(User.class, id);
    }

    @Override
    public List<User> getByIds(List<String> ids) {
        return userBaseDao.find(
                "from User where id in :ids",
                Map.of("ids", ids)
        );
    }
}
