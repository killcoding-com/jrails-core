# JRAILS CORE 是什么？
- JRC是一个相对较新的Web应用程序框架,构建在Java语言之上并使用了更简易的方法实现了ORM & MVC,并引用了RESTful风格.
- JRC作为[KILLCODING.COM](KILLCODING.COM)的底层框架,而它能做的不仅仅如此.
- JRC坚决地抛弃了XML配置,在大多数情况下你无须写配置文件.
- JRC会自动将你编写的Javascript变成微型的*.mini.js.


### 以下是常用的方法

### Config Route
| Source: WEB-INF/config/route.yml
```
...
your: 
  controller: Product
  action: helloWorld
...
```

| Source: WEB-INF/src/app/controller
```
package app.controller;
import net.rails.web.Controller;
...
public class ProductController extends Controller {  

	public void helloWorldAction() throws IOException, ServletException{
		text("Hello World!");
	}
	
}
...
```
| 浏览器打开 [http://15001.krcloud01-a.killcoding.net/your](https://killcoding.com)


### Class ActiveRecord
| Source: WEB-INF/src/test/ActiveRecordTest
```
...
public void testCreateProduct() throws Exception {
   //Product extends ActiveRecord
   Product product = new Product(g); 
   product.setId(Support.code().id());
   product.setCode("IP6P");
   product.setName("IPhone 6 Plus");
   product.setPrice(8888.00F);
   boolean result = product.save();
   assertEquals(true,result);
}
...

```

| mysql> select id,code,name,price from product;                                                              
                                                    
| id | code | name | price |   
| ------ | ------ | ------ | ------ |
| 7etxvn25rqldrnry | IP6P | IPhone 6 Plus | 8888 | 

| 1 row in set (0.00 sec)   


### Class Query
| Source: WEB-INF/src/test/ActiveRecordTest
```
...
import net.rails.sql.query.Query;
...
public void testGetFirstProduct() throws Exception {
	Query query = new Query(new Product(g));
	Product product = query.first();
	System.out.println("Product: " +  product);
	assertNotNull("First Product",product);
}
...
```
| Product: {code=IP6P,price=8888.0,name=IPhone 6 Plus,...}



