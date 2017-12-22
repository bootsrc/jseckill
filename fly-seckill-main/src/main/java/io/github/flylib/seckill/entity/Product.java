package io.github.flylib.seckill.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Product {
	/**
	 * id
	 */
	private Integer id;
	/**
	 * 产品名称 
	 */
	private String productName;
	/**
	 * 价格
	 */
	private BigDecimal price;
	/**
	 * 库存
	 */
	private int stock;
	/**
	 * 版本号
	 */
	private int version;
	/**
	 * 创建时间
	 */
	private Date createTime;

	public Product(Integer id){
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Product() {
	}

	public Product(Integer id, String productName, BigDecimal price, int stock, int version, Date createTime) {
		this.id = id;
		this.productName = productName;
		this.price = price;
		this.stock = stock;
		this.version = version;
		this.createTime = createTime;
	}
}
  