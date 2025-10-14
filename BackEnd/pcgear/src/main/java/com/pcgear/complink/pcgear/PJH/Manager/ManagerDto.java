package com.pcgear.complink.pcgear.PJH.Manager;

import lombok.Getter;

@Getter
public class ManagerDto {
        private final String managerId;
        private final String managerName;
        private final String managerPhoneNumber;

        public ManagerDto(Manager manager) {
                this.managerId= manager.getManagerId();
                this.managerName = manager.getManagerName();
                this.managerPhoneNumber= manager.getPhoneNumber();

        }
}
