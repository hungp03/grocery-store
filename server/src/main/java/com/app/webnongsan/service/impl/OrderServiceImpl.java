package com.app.webnongsan.service.impl;

import com.app.webnongsan.domain.*;
import com.app.webnongsan.domain.request.CheckoutRequestDTO;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.order.OrderDTO;
import com.app.webnongsan.domain.response.order.WeeklyRevenue;
import com.app.webnongsan.repository.OrderDetailRepository;
import com.app.webnongsan.repository.OrderRepository;
import com.app.webnongsan.repository.ProductRepository;
import com.app.webnongsan.repository.UserRepository;
import com.app.webnongsan.service.CartService;
import com.app.webnongsan.service.OrderService;
import com.app.webnongsan.service.UserService;
import com.app.webnongsan.util.PaginationHelper;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final PaginationHelper paginationHelper;
    private final CartService cartService;

    private Order get(long id) {
        log.debug("Fetching order by ID: {}", id);
        return this.orderRepository.findById(id).orElse(null);
    }

    @Override
    public Optional<OrderDTO> findOrder(long id) {
        Optional<Order> orderOptional = this.orderRepository.findById(id);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            return Optional.of(this.convertToOrderDTO(order));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public PaginationDTO getAll(Specification<Order> spec, Pageable pageable) {
        log.info("Fetching all orders with pagination");
        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);
        log.debug("Found {} orders", ordersPage.getTotalElements());

        Page<OrderDTO> orderDTOPage = ordersPage.map(this::convertToOrderDTO);

        return paginationHelper.fetchAllEntities(orderDTOPage);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        log.debug("Converting Order to OrderDTO for order ID: {}", order.getId());
        OrderDTO res = new OrderDTO();
        res.setId(order.getId());
        res.setOrderTime(order.getOrderTime());
        res.setDeliveryTime(order.getDeliveryTime());
        res.setPhone(order.getPhone());
        res.setStatus(order.getStatus());
        res.setPaymentMethod(order.getPaymentMethod());
        res.setAddress(order.getAddress());
        res.setTotal_price(order.getTotal_price());
        res.setUserEmail(order.getUser().getEmail());
        res.setUserId(order.getUser().getId());
        res.setUserName(order.getUser().getName());
        return res;
    }

    private void increaseProductSales(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

        for (OrderDetail detail : orderDetails) {
            Product product = detail.getProduct();
            product.setSold(product.getSold() + detail.getQuantity());
            productRepository.save(product);
        }
    }

    private void restoreProductStock(long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

        for (OrderDetail orderDetail : orderDetails) {
            Product product = orderDetail.getProduct();
            product.setQuantity(product.getQuantity() + orderDetail.getQuantity());
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, int status) {
        log.info("Updating status for order ID: {} to status: {}", orderId, status);
        Order order = this.get(orderId);
        if (order.getStatus() == 3) {
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
    public void create(CheckoutRequestDTO request) {
        log.info("Creating new order for user ID: {}", SecurityUtil.getUserId());
        long uid = SecurityUtil.getUserId();
        User currentUser = userService.getUserById(uid);
        // Tạo order mới
        Order order = new Order();
        order.setUser(currentUser);
        order.setAddress(request.getAddress());
        order.setPhone(request.getPhone());
        order.setTotal_price(request.getTotalPrice());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(0);

        Order savedOrder = orderRepository.save(order);
        log.info("Successfully created order with ID: {}", savedOrder.getId());
        List<Long> purchasedProductIds = new ArrayList<>();

        List<OrderDetail> orderDetails = request.getItems().stream().map(item -> {
            log.debug("Processing order item for product ID: {}", item.getProductId());
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceInvalidException("Product not found: " + item.getProductId()));

            if (product.getQuantity() < item.getQuantity()) {
                throw new ResourceInvalidException("Not enough stock for product: " + product.getId());
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
            purchasedProductIds.add(product.getId());
            // Tạo order detail
            OrderDetailId id = new OrderDetailId(savedOrder.getId(), product.getId());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setId(id);
            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(product);
            orderDetail.setQuantity(item.getQuantity());
            orderDetail.setUnit_price(product.getPrice());
            return orderDetail;
        }).toList();
        cartService.deleteSelectedItems(purchasedProductIds);
        // Lưu tất cả orderDetails bằng batch save
        orderDetailRepository.saveAll(orderDetails);
        log.info("Successfully created order details for order ID: {}", savedOrder.getId());
    }

    @Override
    public PaginationDTO getOrdersByCurrentUser(Integer status, Pageable pageable) {
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
        Page<OrderDTO> orderDTOPage = ordersPage.map(this::convertToOrderDTO);

        return paginationHelper.fetchAllEntities(orderDTOPage);
    }

    @Override
    @Transactional
    public List<WeeklyRevenue> getMonthlyRevenue(int month, int year) {
        log.info("Fetching monthly revenue for month: {} and year: {}", month, year);
        List<Object[]> res = orderRepository.GetRevenueByWeekCycle(month, year);
        List<WeeklyRevenue> weeklyRevenues = new ArrayList<>();

        for (Object[] result : res) {
            String days = String.valueOf(result[0]);
            double totalRevenue = ((Number) result[1]).doubleValue();
            weeklyRevenues.add(new WeeklyRevenue(days, totalRevenue));
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
