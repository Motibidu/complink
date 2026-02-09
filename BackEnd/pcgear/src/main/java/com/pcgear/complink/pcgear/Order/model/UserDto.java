package com.pcgear.complink.pcgear.Order.model;

import com.pcgear.complink.pcgear.User.entity.UserEntity;

import lombok.Getter;

@Getter
public class UserDto {
        private final String managerName;
        private final String managerPhoneNumber;

        public UserDto(UserEntity manager) {

                if (manager == null) {
                        this.managerName = "미지정";
                        this.managerPhoneNumber = "미지정";

                } else {
                        this.managerName = manager.getName();
                        this.managerPhoneNumber = manager.getTel();
                }
        }
}
