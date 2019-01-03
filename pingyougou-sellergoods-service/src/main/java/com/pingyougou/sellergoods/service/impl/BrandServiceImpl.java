package com.pingyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pingyougou.mapper.TbBrandMapper;
import com.pingyougou.pojo.TbBrand;
import com.pingyougou.pojo.TbBrandExample;
import com.pingyougou.pojo.TbBrandExample.Criteria;
import com.pingyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;
	
	/**
	 * 查找数据库中所有的品牌
	 */
	@Override
	public List<TbBrand> findAll() {
		// TODO Auto-generated method stub
		return brandMapper.selectByExample(null);
	}

	/**
	 * 分页查找
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		PageHelper.startPage(pageNum, pageSize);//分页
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	
	}

	/**
	 * 添加一个品牌到数据库
	 */
	@Override
	public void add(TbBrand brand) {
		// TODO Auto-generated method stub
		
		brandMapper.insert(brand);
		
	}

	/**
	 * 根据id查找品牌
	 */
	@Override
	public TbBrand findOne(Long id) {
		// TODO Auto-generated method stub
		return brandMapper.selectByPrimaryKey(id);
	}

	/**
	 * 更新品牌
	 */
	@Override
	public void update(TbBrand brand) {
		// TODO Auto-generated method stub
		brandMapper.updateByPrimaryKey(brand);
	}

	/**
	 * 删除品牌根据id..
	 */
	@Override
	public void delete(Long[] ids) {
		// TODO Auto-generated method stub
		for(Long id: ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
	}

	/**
	 * 查找
	 */
	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		TbBrandExample example=new TbBrandExample();
		Criteria criteria = example.createCriteria();		
		if(brand!=null){
			if(brand.getName()!=null && brand.getName().length()>0){
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0){
				criteria.andFirstCharEqualTo(brand.getFirstChar());
			}		
		}		
		Page<TbBrand> page= (Page<TbBrand>)brandMapper.selectByExample(example);	
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	
	/**
	 * 列表数据
	 */
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}

}
