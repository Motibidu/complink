import Header from "./Header";
import Sidebar from "./Sidebar";

const Layout = ({ children }) => {
  return (
    <div className="layout">
      <Header />
      <div className="layout__container">
        <Sidebar />
        <div className="layout__content">{children}</div>
      </div>
    </div>
  );
};

export default Layout;
