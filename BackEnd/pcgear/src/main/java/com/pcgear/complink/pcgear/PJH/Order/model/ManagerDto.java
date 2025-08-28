package com.pcgear.complink.pcgear.PJH.Order.model;

import lombok.Getter;

@Getter
public class ManagerDto {
        private final String managerName;

        public ManagerDto(Manager manager) {
                this.managerName = manager.getManagerName();

        }
}
