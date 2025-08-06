import React from "react";
import { FaUserCircle } from "react-icons/fa";

const Header = () => {
  return (
    <header className="header">
      <div className="header__container">
        <a className="header__logo" href="#">
          PCGear
        </a>
        <div className="header__actions">
          <a className="header__action-link" href="#">
            도움말
          </a>
          <span className="header__action-icon">🔔</span>
          <span className="header__action-icon">⚙️</span>
          <span className="header__action-icon">👤</span>
        </div>
      </div>
    </header>
  );
};

export default Header;
