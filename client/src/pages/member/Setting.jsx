import { useState } from "react"; 
import { Button, Form, Input, Modal, Tabs, Divider, message, Typography } from "antd";
import { LockIcon, ShieldIcon, AlertTriangleIcon } from "lucide-react";
import { apiUpdatePassword } from "@/apis"; // API gọi tới backend

const { Title } = Typography;

export default function Settings() {
  const [form] = Form.useForm();
  const [deactivateModalOpen, setDeactivateModalOpen] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [loading, setLoading] = useState(false);

  const onPasswordChange = async (values) => {
    try {
      setLoading(true);
      const res = await apiUpdatePassword(values); // Gọi API đổi mật khẩu
      if (res.statusCode === 200) {
        messageApi.success("Đổi mật khẩu thành công!");
        form.resetFields(); // Reset form sau khi đổi mật khẩu thành công
      } else {
        throw new Error(res.message || "Có lỗi xảy ra");
      }
    } catch (error) {
      messageApi.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full">
      {contextHolder}
      <Title level={4} className="px-6 py-4 border-b">Cài đặt tài khoản</Title>

      <Tabs className="p-6 max-w-2xl mx-auto"
        defaultActiveKey="1"
        items={[
          {
            key: "1",
            label: (
              <span className="flex items-center gap-2">
                <LockIcon size={16} />
                Mật khẩu
              </span>
            ),
            children: (
              <div className="py-4">
                <Form form={form} layout="vertical" onFinish={onPasswordChange} className="space-y-4">
                  <Form.Item
                    label="Mật khẩu hiện tại"
                    name="currentPassword"
                    rules={[{ required: true, message: "Vui lòng nhập mật khẩu hiện tại" }]}
                  >
                    <Input.Password placeholder="Nhập mật khẩu hiện tại" />
                  </Form.Item>

                  <Form.Item
                    label="Mật khẩu mới"
                    name="newPassword"
                    rules={[
                      { required: true, message: "Vui lòng nhập mật khẩu mới" },
                      { min: 6, message: "Mật khẩu phải có ít nhất 6 ký tự" },
                    ]}
                  >
                    <Input.Password placeholder="Nhập mật khẩu mới" />
                  </Form.Item>

                  <Form.Item
                    label="Xác nhận mật khẩu mới"
                    name="confirmPassword"
                    dependencies={["newPassword"]}
                    rules={[
                      { required: true, message: "Vui lòng xác nhận mật khẩu" },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue("newPassword") === value) {
                            return Promise.resolve();
                          }
                          return Promise.reject(new Error("Mật khẩu xác nhận không khớp"));
                        },
                      }),
                    ]}
                  >
                    <Input.Password placeholder="Xác nhận mật khẩu mới" />
                  </Form.Item>

                  <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading} className="bg-main">
                      Đổi mật khẩu
                    </Button>
                  </Form.Item>
                </Form>
              </div>
            ),
          },
          {
            key: "2",
            label: (
              <span className="flex items-center gap-2">
                <ShieldIcon size={16} />
                Bảo mật
              </span>
            ),
            children: (
              <div className="py-4 space-y-6">
                <div>
                  <div className="flex justify-between items-center mb-2">
                    <div>
                      <h3 className="text-base font-medium">Thiết bị đã đăng nhập</h3>
                      <p className="text-sm text-gray-500">Tính năng đang được phát triển</p>
                    </div>
                  </div>
                  <Divider />
                </div>

                <div className="pt-4">
                  <h3 className="text-base font-medium text-red-500 flex items-center gap-2">
                    <AlertTriangleIcon size={18} />
                    Vô hiệu hóa tài khoản
                  </h3>
                  <p className="text-sm text-gray-500 mb-4">
                    Vô hiệu hóa tài khoản của bạn sẽ ngăn chặn bạn truy cập vào tài khoản của mình. Bạn có thể kích hoạt lại tài khoản bất kỳ lúc nào bằng cách liên hệ đội ngũ hỗ trợ.
                  </p>
                  <Button danger onClick={() => setDeactivateModalOpen(true)}>
                    Vô hiệu hóa
                  </Button>
                </div>
              </div>
            ),
          },
        ]}
      />
    </div>
  );
}
