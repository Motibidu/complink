import React from "react";
import { NavLink } from "react-router-dom";
import { useState } from "react";
import {
  FaTachometerAlt,
  FaBoxes,
  FaTruck,
  FaShoppingCart,
  FaUserTie,
  FaChevronDown,
  FaChevronRight,
} from "react-icons/fa";
import { RiBillFill } from "react-icons/ri";
import { FaRegistered } from "react-icons/fa6";
import { MdSell } from "react-icons/md";

const Sidebar = () => {
  const [clickedItem, setClickedItem] = useState(null);
  const navItems = [
    {
      name: "기초 등록",
      path: "/inventory",
      icon: <FaRegistered />,
      children: [
        // 하위 메뉴 항목 추가
        { name: "품목등록", path: "/registers/item" },
        { name: "사원(담당)등록", path: "/registers/manager" },
        { name: "거래처등록", path: "/registers/customer" },
      ],
    },
    {
      name: "주문서",
      path: "/order",
      icon: <RiBillFill />,
      children: [
        { name: "주문서 조회", path: "/orders/search" },
        { name: "주문서 입력", path: "/orders/new" },
      ],
    },
    {
      name: "재고 관리",
      path: "/inventory",
      icon: <FaBoxes />,
      children: [
        // 하위 메뉴 항목 추가
        { name: "재고조회", path: "/inventory/status" },
      ],
    },
    {
      name: "판매 관리",
      path: "/sales",
      icon: <MdSell />,
      children: [
        // 하위 메뉴 항목 추가
        { name: "판매입력", path: "/sells/new" },
        { name: "판매조회", path: "/sells/search" },
      ],
    },
    {
      name: "조립/출고",
      icon: <MdSell />,
      children: [
        // 하위 메뉴 항목 추가
        { name: "조립/출고 대기 리스트", path: "/orders/assembly-queue" },
      ],
    },
  ];
  const handleItemClick = (itemName) => {
    // 이미 열려있는 메뉴를 다시 클릭하면 닫기
    if (clickedItem === itemName) {
      setClickedItem(null);
    } else {
      // 새로운 메뉴를 열기
      setClickedItem(itemName);
    }
  };

  return (
    <nav className="sidebar">
      <ul className="sidebar__list">
        {navItems.map((item) => {
          return (
            <div key={item.name} className="sidebar__item__wrapper">
              <li className="sidebar__item">
                {item.children ? (
                  <div
                    className={`sidebar__item__link ${
                      item.name === clickedItem
                        ? "sidebar__item__link--active"
                        : ""
                    }`}
                    onClick={() => {
                      handleItemClick(item.name);
                    }}
                  >
                    <div className="sidebar__item__icon">{item.icon}</div>
                    <span className="sidebar__item__name">{item.name}</span>
                    <div className="sidebar__item__arrow">
                      {clickedItem === item.name ? (
                        <FaChevronDown />
                      ) : (
                        <FaChevronRight />
                      )}
                    </div>
                  </div>
                ) : (
                  <NavLink
                    to={item.path}
                    className={({ isActive }) =>
                      isActive
                        ? "sidebar__item__navlink sidebar__item__navlink--active"
                        : "sidebar__item__navlink"
                    }
                  >
                    <div className="sidebar__item__icon">{item.icon}</div>
                    <span className="sidebar__item__text">{item.name}</span>
                  </NavLink>
                )}
              </li>
              {item.children && clickedItem === item.name ? (
                <ul className="submenu__list">
                  {item.children.map((item) => {
                    return (
                      <li key={item.name} className="submenu__item">
                        <NavLink
                          to={item.path}
                          className={({ isActive }) =>
                            `submenu__item__navlink ${
                              isActive ? "submenu__item__navlink--active" : ""
                            }`
                          }
                        >
                          {item.name}
                        </NavLink>
                      </li>
                    );
                  })}
                </ul>
              ) : (
                ""
              )}
            </div>
          );
        })}
      </ul>
    </nav>
  );
};

export default Sidebar;
