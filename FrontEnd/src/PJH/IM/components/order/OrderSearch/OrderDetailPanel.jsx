import React from "react";
import OrderDetailView from "./OrderDetailView";
import EmptyDetails from "./EmptyDetails";

const OrderDetailPanel = ({ order, onDeleteOrder }) => (
  <div className="custom-panel">
    <div className="p-3 p-lg-4">
      {order ? (
        <OrderDetailView order={order} onDeleteOrder={onDeleteOrder} />
      ) : (
        <EmptyDetails />
      )}
    </div>
  </div>
);

export default OrderDetailPanel;
