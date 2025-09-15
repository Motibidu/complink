package com.pcgear.complink.pcgear.PJH.Manager;

import lombok.Getter;

@Getter
public class ManagerDto {
        private final String managerName;
        private final String managerPhoneNumber;

        public ManagerDto(Manager manager) {
                this.managerName = manager.getManagerName();
                this.managerPhoneNumber= manager.getPhoneNumber();

        }
}
