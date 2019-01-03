package com.pingyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pingyougou.mapper.TbItemMapper;
import com.pingyougou.pojo.TbItem;
import com.pingyougou.pojo.TbItemExample;
import com.pingyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrUtil {
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private SolrTemplate solrTemplate;

	/**
	 * 导入商品数据
	 */
	public void importItemData() {
		TbItemExample example=new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//通过审核才可以导入
		List<TbItem> itemList = itemMapper.selectByExample(example);
		System.out.println("===商品列表===");
		for(TbItem item:itemList){
			System.out.println(item.getId()+" title= "+item.getTitle());			
			Map specMap= JSON.parseObject(item.getSpec());//从数据库中提取规格数据然后将spec字段中的json字符串转换为map
			item.setSpecMap(specMap);//给带注解的字段赋值	
			
		}	
		
		//导入到Solr中
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
		
		System.out.println("===结束===");		

	}
	
	public static void main(String[] args) {
		ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil solrUtil=  (SolrUtil) context.getBean("solrUtil");
		solrUtil.importItemData();
	}

	
}
