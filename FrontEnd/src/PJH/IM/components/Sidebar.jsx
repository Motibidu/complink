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

const Sidebar = () => {
  const [clickedItem, setClickedItem] = useState(null);
  const navItems = [
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
        { name: "창고등록", path: "/inventory/warehouse-register" },
        { name: "재고조회", path: "/inventory/stock-search" },
        { name: "재고이동", path: "/inventory/stock-move" },
      ],
    },
    { name: "발주 관리", path: "/purchases", icon: <FaShoppingCart /> },
    { name: "입/출고 관리", path: "/shipments", icon: <FaTruck /> },
    { name: "거래처 관리", path: "/suppliers", icon: <FaUserTie /> },
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
    // <nav className="sidebar">
    //   <ul className="sidebar__menu">
    //     {navItems.map((item) => (
    //       <li key={item.name} className="sidebar__menu-item">
    //         <div
    //           className="sidebar__menu-link-wrapper"
    //           onClick={() => handleItemClick(item.name)}
    //         >
    //           {!item.children ? (
    //             <NavLink
    //               to={item.path}
    //               className={({ isActive }) =>
    //                 isActive
    //                   ? "sidebar__menu-link sidebar__menu-link--active"
    //                   : "sidebar__menu-link"
    //               }
    //             >
    //               <div className="sidebar__menu-icon">{item.icon}</div>
    //               <span className="sidebar__menu-text">{item.name}</span>
    //             </NavLink>
    //           ) : (
    //             // 하위 메뉴가 있는 항목은 링크 기능을 제거한 div 사용
    //             <div
    //               className={`sidebar__menu-link ${
    //                 clickedItem === item.name
    //                   ? "sidebar__menu-link--active"
    //                   : ""
    //               }`}
    //             >
    //               <div className="sidebar__menu-icon">{item.icon}</div>
    //               <span className="sidebar__menu-text">{item.name}</span>
    //               <div className="sidebar__submenu-toggle-icon">
    //                 {clickedItem === item.name ? (
    //                   <FaChevronDown />
    //                 ) : (
    //                   <FaChevronRight />
    //                 )}
    //               </div>
    //             </div>
    //           )}
    //         </div>

    //         {/* 하위 메뉴를 조건부로 렌더링 */}
    //         {item.children && clickedItem === item.name && (
    //           <ul className="sidebar__submenu">
    //             {item.children.map((child) => (
    //               <li key={child.name} className="sidebar__submenu-item">
    //                 <NavLink
    //                   to={child.path}
    //                   className={({ isActive }) =>
    //                     isActive
    //                       ? "sidebar__submenu-link sidebar__submenu-link--active"
    //                       : "sidebar__submenu-link"
    //                   }
    //                 >
    //                   {child.name}
    //                 </NavLink>
    //               </li>
    //             ))}
    //           </ul>
    //         )}
    //       </li>
    //     ))}
    //   </ul>
    // </nav>
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
