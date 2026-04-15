package com.example.stockproject.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimePriceDto {

    private Long id;
    private String tpname;      // 종목명 또는 지수명
    private LocalDate tpdate;   // 날짜
    private LocalTime tptime;   // 시간
    private String tpprice;     // 가격

    public TimePriceDto() {}

    public TimePriceDto(String tpname, LocalDate tpdate, LocalTime tptime, String tpprice) {
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
