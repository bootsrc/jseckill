package io.github.flylib.seckill.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author twc
 */
public class User {
	/**
	 * 主键
	 */
	private Integer id;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 手机号码
	 */
	private String phone;
	/**
	 * 创建时间
	 */
	private Date createTime;

    public User(Integer id) {
    	this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public User() {
    }

    public User(Integer id, String username, String phone, Date createTime) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.createTime = createTime;
    }
}
