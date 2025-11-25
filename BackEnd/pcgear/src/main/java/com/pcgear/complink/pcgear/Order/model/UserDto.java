package com.pcgear.complink.pcgear.Order.model;

import com.pcgear.complink.pcgear.User.entity.UserEntity;

import lombok.Getter;

@Getter
public class UserDto {
        private final String managerName;
        private final String managerPhoneNumber;

        public UserDto(UserEntity manager) {
                this.managerName = manager.getName();
                this.managerPhoneNumber = manager.getTel();
        }

        
}
