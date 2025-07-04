import React, { useEffect, useState } from "react";
import { MdDelete, MdModeEdit } from "react-icons/md";
import { message, Button, Modal, Table } from 'antd';
import { apiDeleteCategory, apiGetCategories } from "@/apis";
import { useSearchParams, useNavigate } from 'react-router-dom';
import category_default from "@/assets/category_default.png";
import { AddButton } from '@/components/admin';
import { RESPONSE_STATUS } from "@/utils/responseStatus";
const PAGE_SIZE = 10;

const Category = () => {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [currentPage, setCurrentPage] = useState(Number(params.get('page')) || 1);
  const [categories, setCategories] = useState(null);
  const [loading, setLoading] = useState(false);
  const [deleteCategoryId, setDeleteCategoryId] = useState(null);
  const [deleteMessageContent, setDeleteMessageContent] = useState('');

  const fetchCategories = async (queries) => {
    setLoading(true);
      const response = await apiGetCategories(queries);
      if (response.statusCode === RESPONSE_STATUS.SUCCESS){
        setCategories(response);
      }
      else {
        message.error("Có lỗi xảy ra khi lấy dữ liệu")
      }
      setLoading(false);
    
  };

  useEffect(() => {
    const queries = {
      page: currentPage,
      size: PAGE_SIZE,
    };
    fetchCategories(queries);
  }, [currentPage]);

  const handleDeleteCategory = async (cid) => {
    try {
      const response = await apiDeleteCategory(cid);
      if (response.statusCode === RESPONSE_STATUS.SUCCESS) {
        message.success('Xóa danh mục thành công!', 2);
        fetchCategories({ page: currentPage, size: PAGE_SIZE });
      } else {
        throw new Error('Xóa danh mục thất bại!');
      }
    } catch (error) {
      message.error('Xóa danh mục thất bại, hãy xóa những sản phẩm liên kết đến phân loại này', 2);
    }
  };

  const handleShowDeleteCategoryMessage = (cate, id) => {
    setDeleteMessageContent(`Phân loại: ${cate}`);
    setDeleteCategoryId(id);
  };

  const handleCloseDeleteCategoryMessage = () => {
    setDeleteCategoryId(null);
    setDeleteMessageContent('');
  };

  const columns = [
    // {
    //   title: 'ID',
    //   dataIndex: 'id',
    //   key: 'id',
    // },
    {
      title: 'Ảnh',
      dataIndex: 'imageUrl',
      key: 'imageUrl',
      render: (imageUrl) => (
        <img
          src={imageUrl || category_default
          }
          alt="Category"
          style={{ width: '60px', height: '60px', objectFit: 'cover' }}
        />
      ),
    },
    {
      title: 'Tên phân loại',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Sửa',
      key: 'edit',
      render: (_, record) => (
        <Button type="link" onClick={() => navigate(`${location.pathname}/edit/${record.id}`)}>
          <MdModeEdit className="w-5 h-5 inline-block" />
        </Button>
      ),
    },
    {
      title: 'Xóa',
      key: 'delete',
      render: (_, record) => (
        <Button
          type="link"
          danger
          onClick={() => handleShowDeleteCategoryMessage(record.name, record.id)}
        >
          <MdDelete className="w-5 h-5 inline-block" />
        </Button>
      ),
    },
  ];

 return (
    <>
      <div className="w-full p-4 sm:p-6 lg:p-8">
        <div className="overflow-x-auto">
          <Table
            dataSource={categories?.data?.result}
            columns={columns}
            rowKey="id"
            loading={loading}
            pagination={{
              current: currentPage,
              pageSize: PAGE_SIZE,
              onChange: (page) => {
                setCurrentPage(page);
              },
              total: categories?.data?.meta?.total,
            }}
          />
        </div>

        <Modal
          title="Xác nhận xóa"
          open={!!deleteCategoryId}
          onCancel={handleCloseDeleteCategoryMessage}
          footer={[
            <Button key="back" onClick={handleCloseDeleteCategoryMessage}>
              Đóng
            </Button>,
            <Button
              key="submit"
              type="primary"
              danger
              onClick={() => {
                handleDeleteCategory(deleteCategoryId);
                handleCloseDeleteCategoryMessage();
              }}
            >
              Xác nhận
            </Button>,
          ]}
        >
          <p>{deleteMessageContent}</p>
        </Modal>

        <div className="mt-4 text-center">
          <AddButton
            buttonName='+ Thêm phân loại'
            buttonClassName='bg-green-500 hover:bg-green-700 text-white py-2 px-4 rounded-md'
            toLink='add'
          />
        </div>
      </div>
    </>
  );
};

export default Category;