import React, { useState } from "react";
import { Form, Input, Button, Upload, Card, message } from "antd";
import { UploadOutlined } from "@ant-design/icons";
import { apiUploadImage, apiUpdateCategory } from "@/apis";
import category_default from "@/assets/category_default.png";
import { toast } from 'react-toastify';

function EditCategoryForm({ initialCategoryData }) {
  const [form] = Form.useForm();
  const [categoryImage, setCategoryImage] = useState(null);
  const [previewCategoryImage, setPreviewCategoryImage] = useState(
    initialCategoryData?.imageUrl && initialCategoryData.imageUrl.startsWith('https')
      ? initialCategoryData?.imageUrl
      : (initialCategoryData?.imageUrl ? `${import.meta.env.VITE_BACKEND_TARGET}/storage/category/${initialCategoryData.imageUrl}` : category_default)
  );

  const handleUpdateCategory = async (values) => {
    const categoryToUpdate = {
      id: initialCategoryData.id,
      name: values.name,
      imageUrl: initialCategoryData?.imageUrl,
    };

    try {
      // First check
      const resCheck = await apiUpdateCategory(categoryToUpdate);
      if (resCheck.statusCode === 400) {
        throw new Error(resCheck.message || "Có lỗi xảy ra khi tạo danh mục.");
      }

      // Upload image if there's a new one
      if (categoryImage) {
        const resUpLoad = await apiUploadImage(categoryImage, "category");
        categoryToUpdate.imageUrl = resUpLoad?.data?.fileName || initialCategoryData?.imageUrl;
      }

      // Final update with new image if applicable
      const res = await apiUpdateCategory(categoryToUpdate);
      toast.success("Sửa phân loại thành công!");
      //form.resetFields();
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
          setPreviewCategoryImage(reader.result);
        };
        reader.readAsDataURL(file);
        setCategoryImage(file);
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
    <Card title="Sửa phân loại" className="max-w-2xl mx-auto">
      <Form
        form={form}
        layout="vertical"
        onFinish={handleUpdateCategory}
        initialValues={{
          id: initialCategoryData?.id,
          name: initialCategoryData?.name,
        }}
      >
        {/* <Form.Item
          label="ID"
          name="id"
        >
          <Input disabled />
        </Form.Item> */}

        <Form.Item
          label="Tên phân loại"
          name="name"
          rules={[{ required: true, message: 'Vui lòng nhập tên phân loại!' }]}
        >
          <Input className="rounded-md"/>
        </Form.Item>

        <Form.Item label="Hình ảnh">
          <div className="mb-4">
            <img
              src={previewCategoryImage}
              alt="Category preview"
              style={{
                maxWidth: '100%',
                maxHeight: '200px',
                objectFit: 'contain',
                marginBottom: '1rem',
              }}
            />
          </div>
          <Upload {...uploadProps}>
            <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
          </Upload>
        </Form.Item>

        <Form.Item>
          <Button style={{ backgroundColor: '#10B981', color: 'white' }} type="primary" htmlType="submit" block>
            Lưu thay đổi
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
}

export default EditCategoryForm;