import React from 'react';
import { navigation } from '@/utils/constants';
import { NavLink } from "react-router-dom";

const Navigation = () => {
  return (
    <div className="w-main mx-auto">
      <div className="h-[48px] py-2 border-y flex items-center text-sm justify-start">
        {navigation.map((e) => (
          <NavLink
            key={e.id}
            to={e.path}
            className={({ isActive }) =>
              isActive
                ? "pr-12 hover:text-main text-main md:first:pl-0 first:pl-4"
                : "pr-12 hover:text-main"
            }
          >
            {e.value}
          </NavLink>
        ))}
      </div>
    </div>
  );
}

export default Navigation;
