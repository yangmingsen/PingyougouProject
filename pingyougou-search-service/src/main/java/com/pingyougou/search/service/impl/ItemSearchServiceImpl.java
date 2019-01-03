package com.pingyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pingyougou.pojo.TbItem;
import com.pingyougou.search.service.ItemSearchService;

/***
 * 超时问题: 在我们执行搜索的时候,有可能时间会比较长
 * 那么长时间会造成什么问题呢？会造成时间的超时，而现在
 * 我们情况是控制层去调服务层，然后服务成去调Solr,在这期间
 * 很有可能时间是超过1秒的。而doubbx默认是1s内，如果超过1s
 * 就会报错。但是有时候服务器由于性能等问题会造成超时。这时必须
 * 设置timeout。 timeout既可以在服务层配置，也可以在控制层(@Refrence(timeout=..))配置。
 * 推荐将超时时间放在服务层，因为服务层笔控制层更加清楚整个业务逻辑。
 * @author yangmingsen
 *
 */
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Override//searchMap{"keywords":"",.,.,.,.}
	public Map<String, Object> search(Map searchMap) {
		
		//关键字空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
		Map<String,Object> map=new HashMap<>();
		//1.查询列表		
		map.putAll(searchList(searchMap));
		
		//2.分组查询商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);

		//3.查询品牌和规格列表
		String categoryName=(String)searchMap.get("category");
		if(!"".equals(categoryName)){//如果有分类名称
			map.putAll(searchBrandAndSpecList(categoryName));			
		}else{//如果没有分类名称，按照第一个查询
			if(categoryList.size()>0){
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}

		
		
		return map;
	}
	
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);	
		solrTemplate.commit();
	}

	
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品ID"+goodsIdList);
		Query query=new SimpleQuery();		
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

	

	/**
	 * 根据关键字搜索列表
	 * @param keywords
	 * @return
	 */
	private Map searchList(Map searchMap){
		Map map=new HashMap();
		
		//高亮选项初始化
		HighlightQuery query=new SimpleHighlightQuery();
		
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀 
		highlightOptions.setSimplePostfix("</em>");//高亮后缀
		query.setHighlightOptions(highlightOptions);//设置高亮选项
		
		//1.1按照关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		
		//1.2按分类筛选
		if(!"".equals(searchMap.get("category"))){	
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.3按分类筛选
		if(!"".equals(searchMap.get("brand"))){	//选择了品牌
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4按规格过滤 循环过滤
		if(searchMap.get("spec") != null){
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key:specMap.keySet() ){
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);				
			}			
		}
		
		//1.5按价格筛选.....
		if(!"".equals(searchMap.get("price"))){
			String[] price = ((String) searchMap.get("price")).split("-");
			if(!price[0].equals("0")){//如果区间起点不等于0
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);//>
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);				
			}		
			if(!price[1].equals("*")){//如果区间终点不等于*
				Criteria filterCriteria=new  Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);				
			}
		}	
		
		
		//1.6 分页查询		
		Integer pageNo= (Integer) searchMap.get("pageNo");//提取页码
		if(pageNo==null){
			pageNo=1;//默认第一页
		}
		Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数 
		if(pageSize==null){
			pageSize=20;//默认20
		}
		
		query.setOffset((pageNo-1)*pageSize);//从第几条记录查询																								
		query.setRows(pageSize);//行数	

		
		//1.7排序
		String sortValue= (String) searchMap.get("sort");//ASC  DESC  
		String sortField= (String) searchMap.get("sortField");//排序字段
		if(sortValue!=null && !sortValue.equals("")){  
			if(sortValue.equals("ASC")){
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){		
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}			
		}

		
		//**********  获取高亮结果集  *********
		//高亮对象  必须在这句代码之前筛选  
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		
		//HighlightEntry高亮入口集合
		for(HighlightEntry<TbItem> h: page.getHighlighted()){//循环高亮入口集合
			TbItem item = h.getEntity();//获取原实体类			
			if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
			}			
		}		
		map.put("rows",page.getContent());
		
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数

		return map;
	}
	
	/**
	 * 分组查询(查询商品分类)
	 * @param searchMap
	 * @return
	 */
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList<>();
		
		Query query = new SimpleQuery("*:*");
		
		//按照关键字查询 
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//相当于group by 
		query.setGroupOptions(groupOptions );
		
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query ,TbItem.class);
		
		//获取分组结果
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		
		//获取分组入口页
	    Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		List<GroupEntry<TbItem>> entryList= groupEntries.getContent();
		
		for(GroupEntry<TbItem> entry: entryList) {
			list.add(entry.getGroupValue());//将分组的结果添加到返回值中
		}
		
		return list;
		
	}
	
	@Autowired//注入Redis
	private RedisTemplate redisTemplate;
	
	
	/**
	 * 根据商品分类名称查询品牌和规格列表
	 * 数据来源: Redis(Redis 数据来源于工程pingyougou-sellergoods-service(ItemcatServiceImpl;TypeTemplateServiceImpl))
	 * @param category 商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		
		Map map = new HashMap();
		
		//1.根据商品分类名称得到模板Id
		Long templateId = (Long)redisTemplate.boundHashOps("itemCat").get(category);
		
		if(templateId != null) {//当无法获取模板iD时,就不执行2,3。保证程序的容错性
			//2.根据模板Id获取获取品牌参数
			List brandList = (List)redisTemplate.boundHashOps("brandList").get(templateId);
			System.out.println("品牌列条数 = "+brandList.size());
			map.put("brandList", brandList);
			//3.根据模板Id获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			System.out.println("规格列条数 = "+specList.size());
			map.put("specList", specList);
			
		}
		
		return map;
		
	}

	
	
	


	
	
}
