import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from 'react-hook-form';
import { toast } from "react-toastify";
import { apiForgotPassword, apiVerifyOtp } from "@/apis";
import { Button } from '@/components/index';

const ForgotPassword = ({ onClose }) => {
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors } } = useForm();
  const [email, setEmail] = useState('');
  const [showOtpInput, setShowOtpInput] = useState(false);
  const [otp, setOtp] = useState('');

  const handleForgotPassword = async (data) => {
    setEmail(data.email);
    const response = await apiForgotPassword({ email: data.email });
    if (response.statusCode !== 200) {
      toast.info(response?.message);
    } else {
      toast.success("Mã OTP đã được gửi, vui lòng nhập OTP");
      setShowOtpInput(true);
    }
  };

  const handleVerifyOtp = async () => {
    const response = await apiVerifyOtp(email, otp);
    if (response.statusCode === 200) {
      toast.success("Xác minh OTP thành công, vui lòng đặt lại mật khẩu");
      navigate(`/reset-password?token=${response.data.tempToken}`);
    } else {
      toast.error("Mã OTP không hợp lệ");
    }
  };

  return (
    <div className="absolute animate-fade-in top-0 left-0 bottom-0 right-0 bg-overlay flex flex-col items-center justify-center py-8 z-50">
      <div className="flex flex-col gap-4">
        {!showOtpInput ? (
          <>
            <label htmlFor="email">Nhập email của bạn</label>
            <input
              type="email"
              id="email"
              className="w-[800px] p-4 border-b outline-none rounded placeholder:text-sm"
              placeholder="youremail@email.com"
              onChange={e => setEmail(e.target.value)}
              {...register("email", {
                required: 'Email is required',
                pattern: {
                  value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                  message: 'Please enter a valid email address'
                }
              })}
            />
            {errors.email && <span className="text-red-500 text-sm">{errors.email.message}</span>}
            <Button fw={true} handleOnClick={handleSubmit(handleForgotPassword)}>Xác nhận</Button>
          </>
        ) : (
          <>
            <label htmlFor="otp">Nhập mã OTP</label>
            <input
              type="text"
              id="otp"
              className="w-[800px] p-4 border-b outline-none rounded placeholder:text-sm"
              placeholder="Nhập mã OTP"
              value={otp}
              onChange={e => setOtp(e.target.value)}
            />
            <Button fw={true} handleOnClick={handleVerifyOtp}>Xác minh OTP</Button>
          </>
        )}
        <span
          className="w-full text-gray-700 hover:text-blue-700 hover:underline cursor-pointer"
          onClick={onClose}
        >
          Cancel
        </span>
      </div>
    </div>
  );
};

export default ForgotPassword;
