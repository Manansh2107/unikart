
package com.unikart.order;

import java.sql.Timestamp;

public class SellerOrderGroup {

    private int groupId;
    private int orderId;
    private Integer sellerId;
    private String groupStatus;
    private Timestamp createdAt;

    public SellerOrderGroup(int groupId, int orderId,
                            Integer sellerId,
                            String groupStatus,
                            Timestamp createdAt) {

        this.groupId = groupId;
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.groupStatus = groupStatus;
        this.createdAt = createdAt;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getOrderId() {
        return orderId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public String getGroupStatus() {
        return groupStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setGroupStatus(String groupStatus) {
        this.groupStatus = groupStatus;
    }
}
