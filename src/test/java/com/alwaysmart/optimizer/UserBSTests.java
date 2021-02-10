package com.alwaysmart.optimizer;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.alwaysmart.optimizer.vo.DynamicFilteringVO;
import com.alwaysmart.optimizer.vo.UserVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.alwaysmart.optimizer.services.UsersBSI;

@RunWith(MockitoJUnitRunner.class)
public class UserBSTests {

	@Mock
	private UsersBSI usersBS;

	@Test
	public void testGetUserById() {
		UserVO userVO = new UserVO(1, "Josh", "Jenny");
		when(usersBS.getUserById(10)).thenReturn(userVO);
		assertTrue(userVO.getId() == usersBS.getUserById(10).getId());
	}
	
	@Test
	public void testGetUserByFirstName() {
		DynamicFilteringVO vo = new DynamicFilteringVO("1", "Josh", "Jenny");
		when(usersBS.getUserByFirstName("At")).thenReturn(vo);
		assertTrue(vo.getId() == usersBS.getUserByFirstName("At").getId());
	}

}
