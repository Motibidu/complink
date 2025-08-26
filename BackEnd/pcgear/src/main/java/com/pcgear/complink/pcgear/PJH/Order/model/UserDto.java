package com.pcgear.complink.pcgear.PJH.Order.model;

import lombok.Getter;

@Getter
public class UserDto {
        private final String userName;

        public UserDto(User user) {
                this.userName = user.getUserName();

        }
}
