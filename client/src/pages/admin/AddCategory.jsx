import React, { useState } from 'react';
import { Form, Input, Button, Upload, Card, message } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { BackButton } from '@/components/admin';
import category_default from "@/assets/category_default.png";
import { apiCreateCategory, apiUploadImage, apiUpdateCategory } from '@/apis';
import { toast } from 'react-toastify';

const AddCategory = () => {
  const [form] = Form.useForm();
  const [categoryImage, setCategoryImage] = useState(null);
  const [previewCategoryImage, setPreviewCategoryImage] = useState(null);

  const handleCreateCategory = async (values) => {
    const categoryToCreate = {
      name: values.name,
    };

    try {
      const response = await apiCreateCategory(categoryToCreate);
      if (response.statusCode === -7) {
        throw new Error("Phân loại đã tồn tại trong hệ thống");
      }
      else if (response.statusCode !== 200) {
        throw new Error("Có lỗi xảy ra khi tạo phân loại");
      }
      
      // Upload ảnh nếu có
      if (categoryImage) {
        const resUpload = await apiUploadImage(categoryImage, "category");
        categoryToCreate.id = response.data.id;
        categoryToCreate.imageUrl = resUpload?.data?.fileName;
        await apiUpdateCategory(categoryToCreate);
      }

      toast.success("Thêm phân loại thành công!");
      form.resetFields();
      setCategoryImage(null);
      setPreviewCategoryImage(null);
    } catch (err) {
      toast.error(err.message);
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
      return false;
    },
    onChange: handleImageChange,
    maxCount: 1,
    showUploadList: false,
  };

  return (
    <div className='w-full'>
      <div className="mb-4">
        <BackButton turnBackPage="/admin/category" header="Quay lại" />
      </div>

      <Card title="Thêm phân loại mới" className="max-w-2xl mx-auto">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreateCategory}
        >
          <Form.Item
            label="Tên phân loại"
            name="name"
            rules={[{ required: true, message: 'Vui lòng nhập tên phân loại!' }]}
          >
            <Input className='rounded-md' />
          </Form.Item>

          <Form.Item label="Hình ảnh">
            <div className="mb-4">
              <img
                src={previewCategoryImage || category_default}
                alt="Category preview"
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
                style={{ backgroundColor: '#10B981', color: 'white', flex: 1 }}
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

export default AddCategory;