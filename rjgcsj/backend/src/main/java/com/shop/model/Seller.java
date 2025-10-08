package com.shop.model;

import lombok.Data;
import javax.persistence.*;

@Data
//@Entity
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private boolean enabled = true;
}