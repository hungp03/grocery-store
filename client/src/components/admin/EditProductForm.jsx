import React, { useEffect, useState } from "react";
import { Form, Input, InputNumber, Button, Upload, Card, message } from "antd";
import { UploadOutlined } from "@ant-design/icons";
import { apiUploadImage, apiUpdateProduct2 } from "@/apis";
import product_default from "@/assets/product_default.png";
import { toast } from 'react-toastify';

const { TextArea } = Input;

const EditProductForm = ({ initialProductData }) => {
  const [form] = Form.useForm();
  const [productData, setProductData] = useState(initialProductData);
  const [productImage, setProductImage] = useState(null);
  const [previewProductImage, setPreviewProductImage] = useState(
    initialProductData?.imageUrl && initialProductData.imageUrl.startsWith('https')
      ? initialProductData?.imageUrl
      : (initialProductData?.imageUrl ? `${import.meta.env.VITE_BACKEND_TARGET}/storage/product/${initialProductData.imageUrl}` : product_default)
  );

  useEffect(() => {
    if (initialProductData) {
      setProductData(initialProductData);
      setPreviewProductImage(
        initialProductData?.imageUrl && initialProductData.imageUrl.startsWith('https')
          ? initialProductData.imageUrl
          : initialProductData?.imageUrl ? `${import.meta.env.VITE_BACKEND_TARGET}/storage/product/${initialProductData.imageUrl}` : product_default
      );
      form.setFieldsValue({
        id: initialProductData?.id,
        productName: initialProductData?.product_name || initialProductData?.productName,
        price: initialProductData?.price,
        quantity: initialProductData?.quantity,
        description: initialProductData?.description,
        rating: initialProductData?.rating,
        sold: initialProductData?.sold
      });
    }
  }, [initialProductData, form]);

  const handleUpdateProduct = async (values) => {
    const productToUpdate = {
      id: initialProductData?.id,
      productName: values.productName,
      price: values.price,
      imageUrl: initialProductData?.imageUrl,
      quantity: values.quantity,
      description: values.description,
      category: { id: productData?.category?.id }
    };

    try {
      if (productImage) {
        const resUpLoad = await apiUploadImage(productImage, "product");
        productToUpdate.imageUrl = resUpLoad?.data?.fileName || initialProductData?.imageUrl;
      }
      
      const resUpdate = await apiUpdateProduct2(productToUpdate);
      if (resUpdate.statusCode === 400) {
        throw new Error(resUpdate.message || "Có lỗi xảy ra khi tạo sản phẩm.");
      }
      
      // Cập nhật state local với dữ liệu mới
      setProductData({
        ...productData,
        ...productToUpdate
      });
      
      // Reset form sau khi lưu thay đổi
      //form.resetFields();

      toast.success("Sửa sản phẩm thành công!");
    } catch (err) {
      toast.error("Có lỗi xảy ra: " + err.message);
    }
  };

  const handleImageChange = (info) => {
    if (info.file) {
      const file = info.file.originFileObj;
      if (file) {
        const reader = new FileReader();
        reader.onloadend = () => {
          setPreviewProductImage(reader.result);
        };
        reader.readAsDataURL(file);
        setProductImage(file);
      }
    }
  };

  const uploadProps = {
    beforeUpload: (file) => {
      const isImage = file.type.startsWith('image/');
      if (!isImage) {
        message.error('Bạn chỉ có thể tải lên file hình ảnh!');
        return false;
      }
      return false; // Prevent auto upload
    },
    onChange: handleImageChange,
    maxCount: 1,
    showUploadList: false,
  };

  return (
    <Card title="Chỉnh sửa sản phẩm" className="max-w-3xl mx-auto">
      <Form
        form={form}
        layout="vertical"
        onFinish={handleUpdateProduct}
        initialValues={{
          id: productData?.id,
          productName: productData?.product_name || productData?.productName,
          price: productData?.price,
          quantity: productData?.quantity,
          description: productData?.description,
          rating: productData?.rating,
          sold: productData?.sold
        }}
      >
        {/* <Form.Item
          label="ID sản phẩm"
          name="id"
        >
          <Input disabled />
        </Form.Item> */}

        <Form.Item
          label="Tên sản phẩm"
          name="productName"
          rules={[{ required: true, message: 'Vui lòng nhập tên sản phẩm!' }]}>
          <Input className="rounded-md"/>
        </Form.Item>

        <Form.Item
          label="Giá"
          name="price"
          rules={[{ required: true, message: 'Vui lòng nhập giá!' }]}>
          <InputNumber 
            className="w-full"
            min={0}
            formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
            parser={value => value.replace(/\$\s?|(,*)/g, '')}
          />
        </Form.Item>

        <Form.Item
          label="Số lượng"
          name="quantity"
          rules={[{ required: true, message: 'Vui lòng nhập số lượng!' }]}>
          <InputNumber 
            className="w-full"
            min={0}
            precision={0}
          />
        </Form.Item>

        <Form.Item
          label="Mô tả"
          name="description"
          rules={[{ required: true, message: 'Vui lòng nhập mô tả!' }]}>
          <TextArea rows={4} />
        </Form.Item>

        <Form.Item
          label="Đánh giá"
          name="rating">
          <InputNumber 
            disabled
            className="w-full"
            min={0}
            max={5}
            precision={1}
          />
        </Form.Item>

        <Form.Item
          label="Số lượng đã bán"
          name="sold">
          <InputNumber 
            disabled
            className="w-full"
            min={0}
          />
        </Form.Item>

        <Form.Item label="Hình ảnh">
          <div className="mb-4">
            <img
              src={previewProductImage}
              alt="Product preview"
              style={{
                maxWidth: '100%',
                height: '200px',
                objectFit: 'contain',
                marginBottom: '1rem'
              }}
            />
          </div>
          <Upload {...uploadProps}>
            <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
          </Upload>
        </Form.Item>

        <Form.Item>
          <Button style={{ backgroundColor: '#10B981', color: 'white' }} htmlType="submit" block size="large">
            Lưu thay đổi
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default EditProductForm;
