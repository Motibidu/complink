import React from "react";
import OrderDetailView from "./OrderDetailView";
import EmptyDetails from "./EmptyDetails";

const OrderDetailPanel = ({ order, handleCancelOrder, onStatusUpdate }) => (
  <div className="custom-panel">
    <div className="p-3 p-lg-4">
      {order ? (
        <OrderDetailView order={order} handleCancelOrder={handleCancelOrder} onStatusUpdate={onStatusUpdate}/>
      ) : (
        <EmptyDetails />
      )}
    </div>
  </div>
);

export default OrderDetailPanel;
