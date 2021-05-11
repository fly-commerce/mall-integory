package com.zsy.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsy.common.utils.PageUtils;
import com.zsy.common.utils.Query;
import com.zsy.product.dao.CategoryDao;
import com.zsy.product.entity.CategoryEntity;
import com.zsy.product.service.CategoryBrandRelationService;
import com.zsy.product.service.CategoryService;
import com.zsy.product.vo.Catalogs2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author ZSY
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Cacheable(value = "category" ,key = "#root.method.name", sync = true)
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2、组装成父子的树形结构
        //2.1）、找到所有的一级分类，给children设置子分类
        System.out.println("调用查询数据库方法");
        return entities.stream()
                // 过滤找出一级分类
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                // 处理，给一级菜单递归设置子菜单
                .peek(menu -> menu.setChildren(getChildless(menu, entities)))
                // 按sort属性排序
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO  1、检查当前删除的菜单，是否被别的地方引用

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatalogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * 也可以这么写，但是不推介,下面的方式是制定删除某些key值，这样的话在增加缓存时，还要改这里，所以还设有直接删除整个分区清晰，这要求key按照分区分类
     *  @Caching(evict = {
     *             @CacheEvict(value = "category" , key = "'getLevel1Categories'"),
     *             @CacheEvict(value = "category" , key = "'listWithTree'")
     *     })
     * @param category
     */

    @Transactional
    @Override
    @CacheEvict(value = "category" , allEntries = true)
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> getChildless(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .peek(categoryEntity -> {
                    //1、找到子菜单
                    categoryEntity.setChildren(getChildless(categoryEntity, all));
                })
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

    /**
     * 1、每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
     * 2、@Cacheable 代表当前方法的结果需要缓存，如果缓存中有，方法都不用调用，如果缓存中没有，会调用方法。最后将方法的结果放入缓存
     * TODO: 3、默认行为
     *   3.1 如果缓存中有，方法不再调用
     *   3.2 key是默认生成的:缓存的名字::SimpleKey::[](自动生成key值)
     *   3.3 缓存的value值，默认使用jdk序列化机制，将序列化的数据存到redis中
     *   3.4 默认时间是 -1：
     *
     *  TODO: 自定义操作：key的生成
     *    1. 指定生成缓存的key：key属性指定，接收一个 SpEl
     *    2. 指定缓存的数据的存活时间:配置文档中修改存活时间 ttl
     *    3. 将数据保存为json格式: 自定义配置类 MyCacheManager
     * <p>
     * 4、Spring-Cache的不足之处：
     * TODO: 1）、读模式
     *  缓存穿透：查询一个null数据。解决方案：缓存空数据
     *  缓存击穿：大量并发进来同时查询一个正好过期的数据。解决方案：加锁 ? 默认是无加锁的;使用sync = true来解决击穿问题,只有读模式才有sync
     *  缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间
     * TODO: 2)、写模式：（缓存与数据库一致）
     *  1）、读写加锁。
     *  2）、引入Canal,感知到MySQL的更新去更新Redis
     *  3）、读多写多，直接去数据库查询就行
     * <p>
     * TODO: 总结：
     *  常规数据（读多写少，即时性，一致性要求不高的数据，完全可以使用Spring-Cache）：写模式(只要缓存的数据有过期时间就足够了)
     *  特殊数据：特殊设计
     * <p>
     * 原理：
     * CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
     *
     * @return
     */

    @Override
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("get Level 1 Categories........");
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    /**
     * 缓存里的数据如何和数据库的数据保持一致？？
     * 缓存数据一致性
     * 1)、双写模式 -- 在更新和增加都要更新redis中的数据，会产生脏数据
     *     解决方案：可以加锁
     * 2)、失效模式--直接删除
     *     解决方案：可以加锁
     *3）、如果更新频繁的数据，就可以不用缓存，使用缓存一定要加上缓存失效时间，让数据主动更新缓存
     *    读写数据要加读写锁
     *4）、可以用canal来完成缓存更新操作，canal还可以用于分析系统
     * @return
     */
    public Map<String, List<Catalogs2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        //1、占分布式锁。去redis占坑
        //（锁的粒度，越细越快:具体缓存的是某个数据，11号商品） product-11-lock
        // redis分布式锁的方式就是set key value NX实现的
        //RLock catalogJsonLock = redissonClient.getLock("catalogJson-lock");
        //创建读锁，给锁设置过期时间必须加锁是原子的
        // TODO: 锁的名称要注意，最好各个业务有自己唯一的锁
        //       锁的粒度，具体缓存的是某个数据 11-号商铺   product-11-lock product-12-lock
        String uuid = UUID.randomUUID().toString();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("catalogJson-lock");
        RLock rLock = readWriteLock.readLock();
        Map<String, List<Catalogs2Vo>> dataFromDb = null;
        try {
            rLock.lock();
            //加锁成功...执行业务
            dataFromDb = getCatalogJsonFromDB();
        } finally {
            rLock.unlock();
        }
        return dataFromDb;
    }

    /**
     * 从数据库查询并封装数据::分布式锁
     * 这里用的redis加分布式锁，
     *
     * @return
     */
    public Map<String, List<Catalogs2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //1、占分布式锁。去redis占坑      设置过期时间必须和加锁是同步的，保证原子性（避免死锁）
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            Map<String, List<Catalogs2Vo>> dataFromDb = null;
            try {
                //加锁成功...执行业务
                dataFromDb = getCatalogJsonFromDB();
            } finally {
                // lua 脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 删除锁
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList("lock"), uuid);
            }
            //先去redis查询下保证当前的锁是自己的
            //获取值对比，对比成功删除=原子性 lua脚本解锁
            // String lockValue = stringRedisTemplate.opsForValue().get("lock");
            // if (uuid.equals(lockValue)) {
            //     //删除我自己的锁
            //     stringRedisTemplate.delete("lock");
            // }
            return dataFromDb;
        } else {
            System.out.println("获取分布式锁失败...等待重试...");
            //加锁失败...重试机制
            //休眠一百毫秒
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();     //自旋的方式
        }
    }

    /**
     * @Description 从缓存中获取数据，分布式系统不能使用本地缓存，要用缓存中间件redis
     * 问题： TODO: 产生堆外内存溢出，OutOfDirectMemoryError
     * 原因：TODO: Lettuce的bug导致的netty堆外内存，netty没有指定堆内存，redis是Lettuce进行连接，Lettuce使用netty。
     * 解决方案： 不能使用-Dio.netty.maxDirectMemory
     *      1>升级客户端
     *      2> 切换使用jedis
     * 扫盲：lettuce、jedis操作redis的底层客户端，spring再次封装成redisTemplate
     * 问题2:
     *   关于缓存穿透，缓存雪崩，缓存击穿问题
     * @Author fly-ftx
     * @Date 19:40 2021/5/9
     * @Param []
     * @return java.util.Map<java.lang.String,java.util.List<com.zsy.product.vo.Catalogs2Vo>>
     **/
    @Override
    public Map<String, List<Catalogs2Vo>> getCatalogJson() {
        long random = 1;
        // 1.从缓存中读取分类信息
        // 缓存中存json字符串，JSON跨语言，跨平台的
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            // 2. 缓存中没有，查询数据库，在这里查询数据库要进行加锁，查询到了数据要立即放在缓存中，不然是没有锁住的,查询数据库，并加入缓存中。
            return getCatalogJsonFromDB();
        }
        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalogs2Vo>>>() {
        });
    }

    /**
     * 加缓存前,只读取数据库的操作
     * 这里要就如锁，锁的目的主要是为了解决缓存缓存击穿问题，当缓存失效的时候只需要一次来查询数据数据库，
     *
     * @return
     */
    public Map<String, List<Catalogs2Vo>> getCatalogJsonFromDB() {
        System.out.println("查询了数据库");
        // 这里加入了本地锁,但是不能锁住所有的实例，这里要用分布式锁
        synchronized (this) {
            // 这里还要查询缓存
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            if(!StringUtils.isEmpty(catalogJSON)) {
                return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalogs2Vo>>>() {});
            }
            // 性能优化：将数据库的多次查询变为一次
            List<CategoryEntity> selectList = this.baseMapper.selectList(null);
            //1、查出所有分类
            //1、1）查出所有一级分类
            List<CategoryEntity> level1Categories = getParentCid(selectList, 0L);

            //封装数据
            Map<String, List<Catalogs2Vo>> parentCid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                //1、每一个的一级分类,查到这个一级分类的二级分类
                List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());

                //2、封装上面的结果
                List<Catalogs2Vo> catalogs2Vos = null;
                if (categoryEntities != null) {
                    catalogs2Vos = categoryEntities.stream().map(l2 -> {
                        Catalogs2Vo catalogs2Vo = new Catalogs2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                        //1、找当前二级分类的三级分类封装成vo
                        List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());

                        if (level3Catelog != null) {
                            List<Catalogs2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                                //2、封装成指定格式
                                Catalogs2Vo.Category3Vo category3Vo = new Catalogs2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                                return category3Vo;
                            }).collect(Collectors.toList());
                            catalogs2Vo.setCatalog3List(category3Vos);
                        }

                        return catalogs2Vo;
                    }).collect(Collectors.toList());
                }

                return catalogs2Vos;
            }));
            // 查询缓存，存入到redis中，这里如果不加入到缓存中，下一个线程获取数据时还是会走数据库，不会走缓存的。
            long randomDay = 1;
            redisTemplate.opsForValue().set("Cache_catalogJSON" , JSON.toJSONString(parentCid) , 1+randomDay , TimeUnit.DAYS);
            return parentCid;
        }
    }

    /**
     * @Description 多数据的操作就在该集合中进行查找，不需要递归的查询数据库
     * @Author fly-ftx
     * @Date 19:30 2021/5/9
     * @Param [selectList, parentCid]
     * @return java.util.List<com.zsy.product.entity.CategoryEntity>
     **/
    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> result = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return result;
    }
}
