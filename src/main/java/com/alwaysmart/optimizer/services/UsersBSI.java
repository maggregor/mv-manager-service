package com.alwaysmart.optimizer.services;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.alwaysmart.optimizer.vo.DynamicFilteringVO;
import com.alwaysmart.optimizer.vo.UserVO;
import org.springframework.http.converter.json.MappingJacksonValue;

public interface UsersBSI {

	public UserVO getUserById(int id);

	public List<UserVO> getUsers();

	public UserVO saveUser(UserVO user);

	public DynamicFilteringVO getUserByFirstName(@NotNull String firstName);

	public MappingJacksonValue filterProperties(DynamicFilteringVO vo, String fieldName);

}
