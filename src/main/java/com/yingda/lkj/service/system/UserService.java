package com.yingda.lkj.service.system;

import com.yingda.lkj.beans.entity.system.User;

import java.util.List;

/**
 * @author hood  2019/12/13
 */
public interface UserService {
    void login(User user);

    User getUserByUserName(String userName);

    void updatePassword(String userId, String password);

    User getById(String id);
    List<User> getByIds(List<String> ids);
}
