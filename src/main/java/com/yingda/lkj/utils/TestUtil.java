package com.yingda.lkj.utils;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author hood  2019/12/18
 */
public class TestUtil {

    @Data
    @ToString
    static class Dummy {
        private String name;

        public Dummy(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
        List<Dummy> dummies = List.of(new Dummy("1"), new Dummy("2"));

        Dummy reduce = dummies.stream().reduce(null, (x, y) -> y);
        System.out.println(reduce);
    }

}
