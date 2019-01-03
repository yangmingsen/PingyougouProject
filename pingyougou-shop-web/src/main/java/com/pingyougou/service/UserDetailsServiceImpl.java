package com.pingyougou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pingyougou.pojo.TbSeller;
import com.pingyougou.sellergoods.service.SellerService;

/**
 * 认证类
 * @author yangmingsen
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService {

	private SellerService sellerService;

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("经过这里UserDetailServiceImpl");
	
		//构建角色列表
		List<GrantedAuthority> grantAuths = new ArrayList();
		grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		
		//得到商家
		TbSeller seller = sellerService.findOne(username);
		if(seller!=null){
			if(seller.getStatus().equals("1")){
				return new User(username,seller.getPassword(),grantAuths);
			}else{
				return null;
			}			
		}else{
			return null;
		}

	}

}
