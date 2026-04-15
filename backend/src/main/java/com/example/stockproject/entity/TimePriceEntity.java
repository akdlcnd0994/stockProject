package com.example.stockproject.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "timeprice")
public class TimePriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tpname", nullable = false)
    private String tpname;  // 종목명 또는 지수명

    @Column(name = "tpdate", nullable = false)
    private LocalDate tpdate;  // 날짜

    @Column(name = "tptime", nullable = false)
    private LocalTime tptime;  // 시간

    @Column(name = "tp", nullable = false)
    private String tpprice;  // 가격

    public TimePriceEntity() {}

    public TimePriceEntity(String tpname, LocalDate tpdate, LocalTime tptime, String tpprice) {
        this.tpname = tpname;
        this.tpdate = tpdate;
        this.tptime = tptime;
        this.tpprice = tpprice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTpname() {
        return tpname;
    }

    public void setTpname(String tpname) {
        this.tpname = tpname;
    }

    public LocalDate getTpdate() {
        return tpdate;
    }

    public void setTpdate(LocalDate tpdate) {
        this.tpdate = tpdate;
    }

    public LocalTime getTptime() {
        return tptime;
    }

    public void setTptime(LocalTime tptime) {
        this.tptime = tptime;
    }

    public String getTpprice() {
        return tpprice;
    }

    public void setTpprice(String tpprice) {
        this.tpprice = tpprice;
    }
}
