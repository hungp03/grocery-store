import React, { useState, useEffect } from "react";
import { ProductBanner, ProductCard } from "@/components";
import { apiGetProducts } from "@/apis";
import { RESPONSE_STATUS } from "@/utils/responseStatus";
import { PropagateLoader } from 'react-spinners';
const FeatureProduct = ({ flag = 'new' }) => {
  const [products, setProducts] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchProduct = async () => {
    setLoading(true);
    let response;
    if (flag === "new") {
      response = await apiGetProducts({ page: 1, size: 12, sort: "createdAt,desc" });
    } else if (flag === "recommendation") {
      response = await apiGetProducts({page: 1, size: 12, sort: "sold,rating,desc"});
    }
    if (response.statusCode === RESPONSE_STATUS.SUCCESS) {
      setProducts(response.data.result);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchProduct();
  }, [flag]);


  return (
    <div className="w-main">
      <h2 className="text-[20px] uppercase font-semibold py-[10px] border-b-4 border-main">
        {flag === "new" ? 'Sản phẩm mới': 'Có thể bạn sẽ thích'}
      </h2>

      <div className="grid grid-cols-6 gap-4 mt-4">
        {loading ? (
          <div className="absolute top-0 left-0 w-full h-full flex justify-center items-center min-h-[20vh] z-10">
            <PropagateLoader color="#36d7b7" loading={loading} size={20} />
          </div>
        ) : (
          products?.map((e) => (
            <ProductCard key={e.id} productData={e} />
          ))
        )}
      </div>
      {flag === "new" ? <ProductBanner /> : null}
    </div>
  );
};

export default FeatureProduct;
