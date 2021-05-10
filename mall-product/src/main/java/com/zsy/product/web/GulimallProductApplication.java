package com.zsy.product.web;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *      </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动。https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus；
 *              1）、使用@MapperScan
 *              2）、告诉MyBatis-Plus，sql映射文件位置
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 * 3、JSR303
 *   1）、给Bean添加校验注解:javax.validation.constraints，并定义自己的message提示
 *   2)、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *   3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *   4）、分组校验（多场景的复杂校验）
 *         1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *         2）、@Validated({AddGroup.class})
 *         3)、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效，只会在@Validated生效；
 *
 *   5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器 ConstraintValidator
 *      3）、关联自定义的校验器和自定义的校验注解
         *      @Documented
         * @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 })
         * @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
         * @Retention(RUNTIME)
         * public @interface ListValue {
 *
 * 4、统一的异常处理
 * @ControllerAdvice
 *  1）、编写异常处理类，使用@ControllerAdvice。
 *  2）、使用@ExceptionHandler标注方法可以处理的异常。
 *
 * 5. 整合SpringCache简化缓存开发
 *    1）、引入依赖
 *      spring-boot-starter-cache 、spring-boot-starter-data-redis
 *    2)、写配置
 *     （1）自动配置了哪些
 *        CacheAutoConfiguration会导入RedisCacheConfiguration，自动配置好缓存管理器RedisCacheManager
 *      (2) 配置使用redis缓存
 *    3)、 测试使用缓存
 *       @Cacheable: Triggers cache population ： 触发将数据保存到缓存的操作
 *       @CacheEvict: Triggers cache eviction.： 触发将数据从缓存删除的操作
 *       @CachePut: Updates the cache without interfering with the method execution. ： 不能影响方法更新缓存
 *       @Caching: Regroups multiple cache operations to be applied on a method. ： 组合以上多个操作
 *       @CacheConfig: Shares some common cache-related settings at class-level. ： 在类级别共享缓存的相同配置
 *    4）、在启动类上开启缓存 @EnableCaching
 *    5)、 只需要注解使用缓存
 *    6）、原理：
 *         CacheAutoConfiguration -> RedisCacheConfiguration
 *         自动配置了RedisCacheManager->初始化所有的缓存->每个缓存决定使用什么配置
 *         ->如果redisCacheConfiguration 有就用自己的，没有就使用默认配置-> 想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *         ->就会应用到当前RedisCacheManager管理的缓存分区中
 *    7)、使用建议
 *        是有分区名，但是不要定义自己的分区名，建议使用默认的，这样的好处在于删除缓存是根据分区来删除就可以
 *        @CacheEvict(value= "分区名" , allEntries = true)，这样可以删除分区的所有数据，很好的实现缓存更新，直接删除全部由联系的缓存
 *
 *
 *
 *
 *
 *
 *
 *
 */


