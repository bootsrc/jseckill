package io.github.flylib.seckill.web.req;

public class SecKillRequest {

    private Integer userId;

    private Integer productId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public SecKillRequest(Integer userId, Integer productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public SecKillRequest() {
    }
}
