import { useState } from "react"
import { Button, Form, Input, Modal, message } from "antd"

export default function DeactivateAccountModal({ open, onClose }) {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  const handleDeactivate = async (values) => {
    try {
      setLoading(true)
      // Simulate API call - replace with actual API
      await new Promise((resolve) => setTimeout(resolve, 1000))

      message.success("Tài khoản đã được vô hiệu hóa")
      form.resetFields()
      onClose()
    } catch (error) {
      message.error(error.message || "Có lỗi xảy ra khi vô hiệu hóa tài khoản")
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal title="Vô hiệu hóa tài khoản" open={open} onCancel={onClose} footer={null} width={500}>
      <div className="py-2">
        <p className="text-red-500 mb-4">
          Cảnh báo: Hành động này sẽ vô hiệu hóa tài khoản của bạn. Bạn sẽ không thể đăng nhập cho đến khi liên hệ với
          đội ngũ hỗ trợ để kích hoạt lại.
        </p>

        <Form form={form} layout="vertical" onFinish={handleDeactivate}>
          <Form.Item
            label="Nhập mật khẩu để xác nhận"
            name="password"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu để xác nhận" }]}
          >
            <Input.Password placeholder="Nhập mật khẩu của bạn" />
          </Form.Item>

          <div className="flex justify-end gap-2 mt-4">
            <Button onClick={onClose}>Hủy</Button>
            <Button danger type="primary" htmlType="submit" loading={loading}>
              Xác nhận vô hiệu hóa
            </Button>
          </div>
        </Form>
      </div>
    </Modal>
  )
}

