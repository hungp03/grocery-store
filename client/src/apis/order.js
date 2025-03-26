import axiosInstance from "@/utils/axios";
export const apiGetAllOrders = async (params = {}) => {
    const defaultParams = {
        sort: "id,desc",
        ...params,
    };

    return axiosInstance({
        url: "/all-orders",
        method: "get",
        params: defaultParams,
        paramsSerializer: {
            encode: (value) => value,
            serialize: (params) => {
                return Object.entries(params)
                    .map(([key, value]) => `${key}=${value}`)
                    .join('&');
            }
        }
    });
};

export const apiGetOrderDetail = async (oid) =>
    axiosInstance({
        url: `/order-detail/${oid}`,
        method: "get",
    })
export const apiGetOrderInfor = async (oid) =>
    axiosInstance({
        url: `/order-info/${oid}`,
        method: "get",
    })

export const apiGetMonthlyRevenue = async (month, year) =>
    axiosInstance({
        url: `monthly-orders-revenue`,
        method: `get`,
        params: {
            month: month,
            year: year
        }
    })

export const apiUpdateOrderStatus = async (orderId, status) =>
    axiosInstance({
        url: `update-order-status/${orderId}`,
        params: { status: status },
        method: 'put'
    })

export const apiGetSummary = async () =>
    axiosInstance({
        url: `admin/summary`,
    })