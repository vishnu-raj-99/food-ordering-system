package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.exception.DomainException;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {
    private CustomerId customerId;
    private RestaurantId restaurantId;

    private StreetAddress streetAddress;

    private Money price;

    private List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessage;

//   ===========validations
//    ---checks order object is in correct state for initialization
    public void validateOrder(){
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    //------------validate initial order status
    private void validateInitialOrder() {
        if(orderStatus !=null || getId() !=null){
            throw new OrderDomainException("order is not in correct state for initialization");
        }
    }
//---------validdate total price of order
    private void validateTotalPrice() {
        if(price==null ){
            throw new DomainException("Total price must be greater than zero");
        }
    }
//------------vlidate items price
    private void validateItemsPrice() {
       Money orderItemsTotal= items.stream().map(orderItem -> {
            //alidateItemPrice(orderItem);
            return orderItem.getSubTotal();
        }).reduce(Money.ZERO,Money::add);

       if(!price.equals(orderItemsTotal)){
           throw new DomainException("price mismatch in total order price and items total price");
       }


    }

//    private void validateItemPrice(OrderItem orderItem) {
//    }


//   =======initialization======
    public void initializeOrder(){
        setId(new OrderId(UUID.randomUUID()));
        trackingId=new TrackingId(UUID.randomUUID());
        orderStatus=OrderStatus.PENDING;
    }

    public void initializeOrderItems(){
        long itemId=1;
        for(OrderItem orderItem: items){
            orderItem.initializeOrderItem(super.getId(),new OrderItemId(itemId++));
        }
    }

//    ==============state changing methods=============
    public void pay(){
        if(orderStatus!=OrderStatus.PENDING){
            throw new DomainException("Order is not in correct state for payment operation");
        }
        orderStatus=OrderStatus.PAID;
    }

    public void approve(){
        if(orderStatus!=OrderStatus.PAID){
            throw new DomainException("Order is not in correct state for approval");
        }
        orderStatus=OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages){
        if(orderStatus!=OrderStatus.PAID){
            throw new DomainException("Order is not in correct state for initCancel operation");
        }
        orderStatus=OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if(this.failureMessage!=null && failureMessages!=null){
            //difference btwn add and addAll
            this.failureMessage.addAll(failureMessages.stream().filter(message-> !message.isEmpty()).toList());
        }
        if(this.failureMessage==null){
            this.failureMessage=failureMessages;
        }

    }

    public void cancel(List<String> failureMessages){
        if(!(orderStatus==OrderStatus.CANCELLING || orderStatus==OrderStatus.PENDING )){
            throw new DomainException("Order is not in correct state for cancellation");
        }

        orderStatus=OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
    }







    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        streetAddress = builder.streetAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessage = builder.failureMessage;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public StreetAddress getStreetAddress() {
        return streetAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessage() {
        return failureMessage;
    }

    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress streetAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessage;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder streetAddress(StreetAddress val) {
            streetAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessage(List<String> val) {
            failureMessage = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }



}
