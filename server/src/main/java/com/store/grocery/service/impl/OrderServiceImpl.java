package com.store.grocery.service.impl;

import com.store.grocery.domain.*;
import com.store.grocery.dto.request.order.OrderRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.order.OrderResponse;
import com.store.grocery.dto.response.order.WeeklyRevenueResponse;
import com.store.grocery.mapper.OrderMapper;
import com.store.grocery.repository.OrderDetailRepository;
import com.store.grocery.repository.OrderRepository;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.repository.UserRepository;
import com.store.grocery.service.CartService;
import com.store.grocery.service.EmailService;
import com.store.grocery.service.OrderService;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.store.grocery.util.exception.ResourceNotFoundException;
import com.store.grocery.util.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final EmailService emailService;
    private final OrderMapper orderMapper;

    private Order get(long id) {
        log.debug("Fetching order by ID: {}", id);
        return this.orderRepository.findById(id).orElse(null);
    }

    @Override
    public OrderResponse findOrder(long id) {
        log.info("Fetching order by ID: {}", id);
        Order order = this.orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public PaginationResponse getAllOrder(Specification<Order> spec, Pageable pageable) {
        log.info("Fetching all orders with pagination");
        Page<OrderResponse> ordersPage = orderRepository.findAll(spec, pageable).map(orderMapper::toOrderResponse);
        log.debug("Found {} orders", ordersPage.getTotalElements());
        return PaginationResponse.from(ordersPage, pageable);
    }

    private void increaseProductSales(Long orderId) {
        log.info("Increasing product sales for order ID: {}", orderId);
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail detail : orderDetails) {
            Product product = detail.getProduct();
            product.setSold(product.getSold() + detail.getQuantity());
            productRepository.save(product);
        }
        log.info("Successfully increased product sales for order ID: {}", orderId);
    }

    private void restoreProductStock(long orderId) {
        log.info("Restoring product stock for order ID: {}", orderId);
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

        for (OrderDetail orderDetail : orderDetails) {
            Product product = orderDetail.getProduct();
            product.setQuantity(product.getQuantity() + orderDetail.getQuantity());
            productRepository.save(product);
        }
        log.info("Successfully restored product stock for order ID: {}", orderId);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, int status) {
        log.info("Updating status for order ID: {} to status: {}", orderId, status);
        Order order = this.get(orderId);
        if (order.getStatus() == 3) {
            log.warn("Cannot change status for order ID: {}, order has been cancelled", orderId);
            throw new ResourceInvalidException("Không thể thay đổi trạng thái đơn hàng đã hủy");
        }
        order.setStatus(status);

        if (status == 2 || status == 3) {
            order.setDeliveryTime(Instant.now());
        }

        if (status == 2) {
            increaseProductSales(orderId);
        }

        // Nếu đơn hàng bị hủy (status == 3), hoàn lại số lượng sản phẩm
        if (status == 3) {
            restoreProductStock(orderId);
        }

        orderRepository.save(order);
        log.info("Successfully updated status for order ID: {}", orderId);
    }

    @Override
    @Transactional
    public Long create(OrderRequest request) {
        log.info("Creating new order for user ID: {}", SecurityUtil.getUserId());
        long uid = SecurityUtil.getUserId();
        User currentUser = userRepository.findById(uid)
                .orElseThrow(() -> new UserNotFoundException("User với ID " + uid + " không tồn tại"));
        Order order = Order.builder()
                .user(currentUser)
                .address(request.getAddress())
                .phone(request.getPhone())
                .totalPrice(request.getTotalPrice())
                .paymentMethod(request.getPaymentMethod())
                .status(0).build();

        Order savedOrder = orderRepository.save(order);
        log.info("Successfully created order with ID: {}", savedOrder.getId());

        List<Long> purchasedProductIds = new ArrayList<>();
        List<OrderDetail> orderDetails = request.getItems().stream().map(item -> {
            log.debug("Processing order item for product ID: {}", item.getProductId());
            Product product = productRepository.findByIdAndIsActiveTrue(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + item.getProductId()));

            if (product.getQuantity() < item.getQuantity()) {
                throw new ResourceInvalidException("Số lượng hàng không đủ cho sản phẩm id: " + product.getId());
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
            purchasedProductIds.add(product.getId());

            OrderDetailId id = new OrderDetailId(savedOrder.getId(), product.getId());
            return OrderDetail.builder()
                    .id(id)
                    .order(savedOrder)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice()).build();
        }).toList();

        cartService.deleteSelectedItems(purchasedProductIds);
        orderDetailRepository.saveAll(orderDetails);
        log.info("Successfully created order details for order ID: {}", savedOrder.getId());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Transaction committed successfully, sending order email...");
                emailService.sendOrderEmail(currentUser, request);
            }
        });
        return savedOrder.getId();
    }


    @Override
    public PaginationResponse getMyOrders(Integer status, Pageable pageable) {
        log.info("Fetching orders for user ID: {}", SecurityUtil.getUserId());
        long uid = SecurityUtil.getUserId();
        // Thêm sort
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        Page<Order> ordersPage = (status != null)
                ? orderRepository.findByUserIdAndStatus(uid, status, sortedPageable)
                : orderRepository.findByUserId(uid, sortedPageable);
        log.debug("Found {} orders for user ID: {}", ordersPage.getTotalElements(), uid);

        PaginationResponse paginationResponse = PaginationResponse.from(ordersPage.map(orderMapper::toMyOrderResponse), sortedPageable);
        log.info("Returning paginated orders for user ID: {}", uid);
        return paginationResponse;
    }

    @Override
    @Transactional
    public List<WeeklyRevenueResponse> getMonthlyRevenue(int month, int year) {
        log.info("Fetching monthly revenue for month: {} and year: {}", month, year);
        List<Object[]> res = orderRepository.GetMonthlyWeeklyRevenue(month, year);
        List<WeeklyRevenueResponse> weeklyRevenues = new ArrayList<>();

        for (Object[] result : res) {
            String days = String.valueOf(result[0]);
            double totalRevenue = ((Number) result[1]).doubleValue();
            weeklyRevenues.add(new WeeklyRevenueResponse(days, totalRevenue));
        }
        return weeklyRevenues;
    }

    @Override
    public List<Object> getOverviewStats() {
        log.info("Fetching overview stats");
        long totalUsers = userRepository.count();
        double totalProfit = orderRepository.sumTotalPriceByStatus(2);
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        return Arrays.asList(totalProfit, totalUsers, totalProducts, totalOrders);
    }
}
