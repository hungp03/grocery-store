import React, { useState } from "react";
import { Form, Input, InputNumber, Button, Upload, Card, message } from "antd";
import { UploadOutlined } from "@ant-design/icons";
import { BackButton } from "@/components/admin";
import { CategoryComboBox } from "@/components/admin";
import product_default from "@/assets/product_default.png";
import { apiUploadImage, apiCreateProduct } from "@/apis";
import { toast } from "react-toastify";

const { TextArea } = Input;

const AddProduct = () => {
  const [form] = Form.useForm();
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [productImage, setProductImage] = useState(null);
  const [previewProductImage, setPreviewProductImage] = useState(null);

  const handleCreateProduct = async (values) => {
    if (!selectedCategory?.id) {
      message.error("Vui lòng chọn phân loại sản phẩm!");
      return;
    }

    const productToCreate = {
      productName: values.productName,
      price: values.price,
      quantity: values.quantity,
      sold: 0,
      description: values.description,
      category: { id: selectedCategory?.id },
    };

    try {
      if (productImage) {
        const resUpLoad = await apiUploadImage(productImage, "product");
        productToCreate.imageUrl = resUpLoad?.data?.fileName || null;
      }

      const resCreate = await apiCreateProduct(productToCreate);
      if (resCreate.statusCode === 400) {
        throw new Error(resCreate.message || "Có lỗi xảy ra khi tạo sản phẩm.");
      }

      toast.success("Thêm sản phẩm thành công!");
      form.resetFields();
      setPreviewProductImage(null);
      setProductImage(null);
      setSelectedCategory(null);
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
    <div className="w-full">
      <div className="mb-4">
        <BackButton turnBackPage="/admin/product" header="Quay lại" />
      </div>

      <Card title="Thêm sản phẩm mới" className="max-w-3xl mx-auto">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreateProduct}
        >
          <Form.Item
            label="Tên sản phẩm"
            name="productName"
            rules={[{ required: true, message: 'Vui lòng nhập tên sản phẩm!' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            label="Giá"
            name="price"
            rules={[{ required: true, message: 'Vui lòng nhập giá!' }]}
          >
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
            rules={[{ required: true, message: 'Vui lòng nhập số lượng!' }]}
          >
            <InputNumber
              className="w-full"
              min={0}
              precision={0}
            />
          </Form.Item>

          <Form.Item
            label="Mô tả"
            name="description"
            rules={[{ required: true, message: 'Vui lòng nhập mô tả sản phẩm!' }]}
          >
            <TextArea rows={4} />
          </Form.Item>

          <Form.Item
            label="Phân loại"
            required
            validateStatus={!selectedCategory ? "error" : "success"}
            help={!selectedCategory ? "Vui lòng chọn phân loại sản phẩm" : null}
          >
            <CategoryComboBox
              onSelectCategory={(value) => {
                setSelectedCategory(value);
              }}
            />
          </Form.Item>

          <Form.Item label="Hình ảnh">
            <div className="mb-4">
              <img
                src={previewProductImage || product_default}
                alt="Product preview"
                style={{
                  maxWidth: '100%',
                  height: '200px',
                  objectFit: 'contain',
                  marginBottom: '1rem',
                  borderRadius: '8px',
                  border: '1px solid #d9d9d9'
                }}
              />
            </div>
            
            <div className="flex gap-4">
              <Upload {...uploadProps}>
                <Button icon={<UploadOutlined />} size="large">
                  Chọn ảnh
                </Button>
              </Upload>

              <Button
                style={{ backgroundColor: '#10B981', color: 'white' , flex: 1}}
                htmlType="submit"
                size="large"
              >
                Lưu
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default AddProduct;