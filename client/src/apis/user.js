import axiosInstance from "@/utils/axios";

export const apiRegister = async (data) =>
    axiosInstance({
        url: "/auth/register",
        method: "post",
        data,
        withCredentials: true
    });

export const apiLogin = async (data) =>
    axiosInstance({
        url: "/auth/login",
        method: "post",
        data,
        withCredentials: true
    });

export const apiLoginGoogle = async (data) => {
    return axiosInstance({
        url: "/auth/signin/google",
        method: "post",
        data,
        withCredentials: true
    });
};


export const apiGetCurrentUser = async () =>
    axiosInstance({
        url: "/auth/account",
        method: 'get',
    });

export const apiForgotPassword = async (data) =>
    axiosInstance({
        url: "/auth/forgot",
        method: 'post',
        data
    });

export const apiResetPassword = async (data) =>
    axiosInstance({
        url: "/auth/reset-password",
        method: 'put',
        params: {
            token: data.token
        }, data: {
            newPassword: data.newPassword,
            confirmPassword: data.confirmPassword
        }
    });

export const apiVerifyOtp = async (email, otp) =>
    axiosInstance({
        url: "auth/validate-otp",
        method: 'post',
        data: {
            email: email,
            otp: otp
        }
    });

export const apiLogout = async () =>
    axiosInstance({
        url: "/auth/logout",
        method: 'post',
        withCredentials: true,
    });


export const apiGetAllUser = async (params) =>
    axiosInstance({
        url: "/users",
        method: "get",
        params,
    });


export const apiUpdateCurrentUser = async (formData) => {
    return axiosInstance.put("/users/account", formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
};




export const getUserById = async (id) => {
    return axiosInstance({
        url: `/users/${id}`,
        method: 'get',
    });
}

export const apiUpdatePassword = async (data) =>
    axiosInstance({ url: "/users/update-password", method: 'put', data });

export const apiAddOrUpdateCart = async (pid, quantity) => {
    return axiosInstance({
        url: '/cart',
        method: 'post',
        data: {
            id: {
                productId: pid
            },
            quantity: quantity
        }
    })
}

export const apiDeleteCart = async (pid) => {
    return axiosInstance({
        url: `/cart/${pid}`,
        method: 'delete'
    })
}

export const apiGetCart = async (page, size) => {
    return axiosInstance({
        url: '/cart',
        method: 'get',
        params: { page, size }
    })
}

export const apiDeleteWishlist = async (pid) => {
    return axiosInstance({
        url: `/wishlist/${pid}`,
        method: 'delete',
    })
}

export const apiGetWishlist = async (page, size) => {
    return axiosInstance({
        url: '/wishlist',
        method: 'get',
        params: { page, size }
    })
}

export const apiAddWishList = async (pid) => {
    return axiosInstance({
        url: '/wishlist',
        method: 'post',
        data: {
            id: {
                productId: pid
            }
        }
    })
}

export const apiSetStatusUser = async (user) => {
    return axiosInstance({
        url: "/users/status",
        method: 'put',
        data: user
    });
};
// Tạo order
export const apiCreateOrder = async (data) => {
    const requestBody = {
        address: data.address,
        phone: data.phone,
        paymentMethod: data.paymentMethod,
        totalPrice: data.totalPrice,
        items: data.items
    };

    return axiosInstance({
        url: `/checkout`,
        method: 'post',
        data: requestBody,
        headers: {
            'Content-Type': 'application/json'
        }
    });
}
// Lấy các sản phẩm được chọn trong cart
export const apiGetSelectedCart = async (pids) => {
    return axiosInstance({
        url: `cart/product-selected?productIds=${pids?.join(',')}`,
        method: 'get',
    });
};

export const apiPaymentVNPay = async (params) =>
    axiosInstance({
        url: `payment/vn-pay`,
        method: 'get',
        params,
    })

export const apiGetLoggedInDevices = async () =>
    axiosInstance({
        url: '/users/devices',
        method: 'get',
        withCredentials: true
    });