package com.tdt.springbatch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqGen")
    @SequenceGenerator(name = "seqGen", sequenceName = "seq", initialValue = 1)
    private Long id;

    @Column(length = 100)
    private String code;

    @Column(length = 100)
    private String stockCode;

    @Column
    private double price;

    @Column
    private int quantity;

    @Column
    private double total;

    @Column
    private Date orderDate;
}
